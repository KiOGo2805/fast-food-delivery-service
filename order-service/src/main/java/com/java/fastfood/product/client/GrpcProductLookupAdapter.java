package com.java.fastfood.product.client;

import com.java.fastfood.contract.product.ProductLookupRequest;
import com.java.fastfood.contract.product.ProductLookupServiceGrpc;
import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.exception.ProductServiceUnavailableException;
import com.java.fastfood.product.port.ProductAvailabilityLookup;
import com.java.fastfood.product.port.ProductPriceLookup;
import com.java.fastfood.product.port.ProductStockLookup;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "product.client", name = "transport", havingValue = "grpc")
public class GrpcProductLookupAdapter implements
        ProductPriceLookup, ProductStockLookup, ProductAvailabilityLookup {

    private final ProductLookupServiceGrpc.ProductLookupServiceBlockingStub stub;
    private final long deadlineMs;

    public GrpcProductLookupAdapter(ManagedChannel channel, ProductClientProperties properties) {
        this.stub = ProductLookupServiceGrpc.newBlockingStub(channel);
        this.deadlineMs = properties.getDeadlineMs();
    }

    @Override
    public BigDecimal findPrice(Integer productId) {
        return call(productId, request -> new BigDecimal(
                stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS).getProductPrice(request).getPrice()));
    }

    @Override
    public int findAvailableStock(Integer productId) {
        return call(productId, request -> stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
                .getProductStock(request).getAvailableQuantity());
    }

    @Override
    public boolean isAvailable(Integer productId) {
        return call(productId, request -> stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
                .getProductAvailability(request).getAvailable());
    }

    private <T> T call(Integer productId, GrpcCall<T> action) {
        ProductLookupRequest request = ProductLookupRequest.newBuilder().setProductId(productId).build();
        try {
            return action.execute(request);
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new ProductNotFoundException(productId);
            }
            throw new ProductServiceUnavailableException(
                    "Product service gRPC call failed: " + exception.getStatus().getCode(), exception);
        }
    }

    @FunctionalInterface
    private interface GrpcCall<T> {
        T execute(ProductLookupRequest request);
    }
}
