package com.java.fastfood.controller;

import com.java.fastfood.domain.dto.OrderResponse;
import com.java.fastfood.domain.dto.PlaceOrderRequest;
import com.java.fastfood.domain.enums.OrderStatus;
import com.java.fastfood.security.CurrentUser;
import com.java.fastfood.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private OrderController controller() {
        return new OrderController(orderService);
    }

    private CurrentUser user(String username, boolean admin) {
        return new CurrentUser(username, admin ? List.of("USER", "ADMIN") : List.of("USER"));
    }

    @Test
    void placeOrder_delegatesUsernameAndRequestToService() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        OrderResponse expected = new OrderResponse();
        CurrentUser user = user("alice", false);
        when(orderService.placeOrder("alice", request)).thenReturn(expected);

        assertThat(controller().placeOrder(request, user)).isSameAs(expected);
    }

    @Test
    void myOrders_delegatesUsernameToService() {
        CurrentUser user = user("alice", false);
        List<OrderResponse> expected = List.of(new OrderResponse());
        when(orderService.listMyOrders("alice")).thenReturn(expected);

        assertThat(controller().myOrders(user)).isSameAs(expected);
    }

    @Test
    void getOrder_regularUser_passesIsAdminFalse() {
        CurrentUser user = user("alice", false);
        OrderResponse expected = new OrderResponse();
        when(orderService.getOrder(1, "alice", false)).thenReturn(expected);

        assertThat(controller().getOrder(1, user)).isSameAs(expected);
    }

    @Test
    void getOrder_adminUser_passesIsAdminTrue() {
        CurrentUser user = user("admin-user", true);
        OrderResponse expected = new OrderResponse();
        when(orderService.getOrder(1, "admin-user", true)).thenReturn(expected);

        assertThat(controller().getOrder(1, user)).isSameAs(expected);
    }

    @Test
    void deleteOrder_delegatesIdToService() {
        controller().deleteOrder(3);

        verify(orderService).deleteOrder(3);
    }

    @Test
    void updateOrderStatus_delegatesIdAndStatusAndReturnsOk() {
        assertThat(controller().updateOrderStatus(3, OrderStatus.COMPLETED).getStatusCode().value())
                .isEqualTo(200);

        verify(orderService).updateOrderStatus(3, OrderStatus.COMPLETED);
    }

    @Test
    void getAllOrders_returnsServiceResult() {
        List<OrderResponse> expected = List.of(new OrderResponse());
        when(orderService.getAllOrders()).thenReturn(expected);

        assertThat(controller().getAllOrders()).isSameAs(expected);
    }
}
