package com.fatih.microservices.order.service;

import com.fatih.microservices.order.dto.*;
import com.fatih.microservices.order.entity.Order;
import com.fatih.microservices.order.entity.OrderItem;
import com.fatih.microservices.order.enums.OrderStatus;
import com.fatih.microservices.order.exception.ProductNotAvailableException;
import com.fatih.microservices.order.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.fatih.microservices.order.exception.OrderNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.fatih.microservices.order.entity.OrderItem;
import com.fatih.microservices.order.client.ProductClient;
import com.fatih.microservices.order.client.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import com.fatih.microservices.order.client.ProductClient;
import com.fatih.microservices.order.client.ProductResponse;
import com.fatih.microservices.order.kafka.OrderEventProducer;
import com.fatih.microservices.order.event.OrderCreatedEvent;
import com.fatih.microservices.order.event.PaymentCompletedEvent;
import com.fatih.microservices.order.event.PaymentFailedEvent;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;


    private final OrderEventProducer orderEventProducer;

    private String paymentMethod;


    public OrderService(
            OrderRepository orderRepository,
            ProductClient productClient,
            OrderEventProducer orderEventProducer) {

        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.orderEventProducer = orderEventProducer;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }



    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = new Order();

        order.setGuestName(request.getGuestName());
        order.setGuestEmail(request.getGuestEmail());
        order.setGuestPhone(request.getGuestPhone());

        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(
                request.getPaymentMethod().toUpperCase()
        );

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {

            ProductResponse product =
                    productClient.getProductById(
                            itemRequest.getProductId()
                    );
            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new ProductNotAvailableException(product.getId());

            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    itemRequest.getQuantity()
                            )
                    );

            OrderItem item = new OrderItem();

            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);

            order.addItem(item);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getGuestEmail(),
                        savedOrder.getTotalAmount(),
                        savedOrder.getPaymentMethod(),
                        savedOrder.getCreatedAt()
                );
        orderEventProducer.publishOrderCreated(event);

        return mapToResponse(savedOrder);
    }

    public void handlePaymentCompleted(
            PaymentCompletedEvent event) {

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseThrow(
                        () -> new OrderNotFoundException(
                                event.getOrderId()
                        )
                );

        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);
    }
    public void handlePaymentFailed(
            PaymentFailedEvent event) {

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseThrow(
                        () -> new OrderNotFoundException(
                                event.getOrderId()
                        )
                );

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }
    public Page<OrderResponse> getAllOrders(
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        return orderRepository
                .findAll(pageable)
                .map(this::mapToResponse);
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderItemResponse(
                                        item.getProductId(),
                                        item.getProductName(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getSubtotal()
                                )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getGuestName(),
                order.getGuestEmail(),
                order.getGuestPhone(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemResponses
        );
    }
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return mapToResponse(order);
    }


    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        orderRepository.delete(order);
    }
    public Page<OrderResponse> getOrdersByStatus(
            OrderStatus status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        return orderRepository
                .findByStatus(status, pageable)
                .map(this::mapToResponse);
    }




}