//package com.fatih.microservices.order.dto;
//
//import com.fatih.microservices.order.enums.OrderStatus;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Positive;
//
//import java.math.BigDecimal;
//
//public class UpdateOrderRequest {
//
//    @NotNull(message = "Quantity is required")
//    @Positive(message = "Quantity must be greater than zero")
//    private Integer quantity;
//
//    @NotNull(message = "Price is required")
//    @Positive(message = "Price must be greater than zero")
//    private BigDecimal price;
//
//    @NotNull(message = "Status is required")
//    private OrderStatus status;
//
//    public Integer getQuantity() {
//        return quantity;
//    }
//
//    public void setQuantity(Integer quantity) {
//        this.quantity = quantity;
//    }
//
//    public BigDecimal getPrice() {
//        return price;
//    }
//
//    public void setPrice(BigDecimal price) {
//        this.price = price;
//    }
//
//    public OrderStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(OrderStatus status) {
//        this.status = status;
//    }
//}