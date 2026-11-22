package com.java.fastfood.grpc;

import com.java.fastfood.contract.product.ProductAvailabilityResponse;
import com.java.fastfood.contract.product.ProductLookupRequest;
import com.java.fastfood.contract.product.ProductLookupServiceGrpc;
import com.java.fastfood.contract.product.ProductPriceResponse;
import com.java.fastfood.contract.product.ProductStockResponse;
import com.java.fastfood.exception.ProductNotFoundException;
import com.java.fastfood.service.ProductService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLookupGrpcService extends ProductLookupServiceGrpc.ProductLookupServiceImplBase {

    private final ProductService productService;

    @Override
    public void getProductPrice(ProductLookupRequest request, StreamObserver<ProductPriceResponse> observer) {
        respond(observer, () -> {
            var price = productService.getPrice(request.getProductId());
            return ProductPriceResponse.newBuilder()
                    .setProductId(price.productId())
                    .setPrice(price.price().toPlainString())
                    .build();
        });
    }

    @Override
    public void getProductStock(ProductLookupRequest request, StreamObserver<ProductStockResponse> observer) {
        respond(observer, () -> {
            var stock = productService.getStock(request.getProductId());
            return ProductStockResponse.newBuilder()
                    .setProductId(stock.productId())
                    .setAvailableQuantity(stock.availableQuantity())
                    .build();
        });
    }

    @Override
    public void getProductAvailability(
            ProductLookupRequest request,
            StreamObserver<ProductAvailabilityResponse> observer) {
        respond(observer, () -> {
            var availability = productService.getAvailability(request.getProductId());
            return ProductAvailabilityResponse.newBuilder()
                    .setProductId(availability.productId())
                    .setAvailable(availability.available())
                    .build();
        });
    }

    private <T> void respond(StreamObserver<T> observer, LookupCall<T> call) {
        try {
            observer.onNext(call.execute());
            observer.onCompleted();
        } catch (ProductNotFoundException exception) {
            observer.onError(Status.NOT_FOUND.withDescription(exception.getMessage()).asRuntimeException());
        } catch (RuntimeException exception) {
            observer.onError(Status.INTERNAL.withDescription("Product lookup failed").withCause(exception)
                    .asRuntimeException());
        }
    }

    @FunctionalInterface
    private interface LookupCall<T> {
        T execute();
    }
}
