package com.fatih.microservices.order.controller;

import com.fatih.microservices.order.dto.CreateOrderRequest;
import com.fatih.microservices.order.dto.OrderResponse;
//import com.fatih.microservices.order.dto.UpdateOrderRequest;
import com.fatih.microservices.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.fatih.microservices.order.enums.OrderStatus;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        return orderService.createOrder(request);
    }

    @GetMapping
    public Page<OrderResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return orderService.getAllOrders(page, size);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {

        return orderService.getOrderById(id);
    }

//    @PutMapping("/{id}")
//    public OrderResponse updateOrder(
//            @PathVariable Long id,
//            @Valid @RequestBody UpdateOrderRequest request) {
//
//        return orderService.updateOrder(id, request);
//    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {

        orderService.deleteOrder(id);
    }
    @GetMapping("/status/{status}")
    public Page<OrderResponse> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return orderService.getOrdersByStatus(
                status,
                page,
                size
        );
    }
}