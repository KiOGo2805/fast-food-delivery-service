package com.java.fastfood.controller;

import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.dto.PlaceOrderRequest;
import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.security.CurrentUser;
import com.java.fastfood.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request,
                                     @AuthenticationPrincipal CurrentUser user) {
        return orderService.placeOrder(user.username(), request);
    }

    @GetMapping("/mine")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal CurrentUser user) {
        return orderService.listMyOrders(user.username());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Integer id, @AuthenticationPrincipal CurrentUser user) {
        return orderService.getOrder(id, user.username(), user.hasRole("ADMIN"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam OrderStatus newStatus) {

        orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
}
