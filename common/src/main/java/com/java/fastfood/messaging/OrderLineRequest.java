package com.java.fastfood.messaging;

public record OrderLineRequest(int lineNumber, Integer productId, int quantity) {
}
