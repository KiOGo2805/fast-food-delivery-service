package com.java.fastfood.product.client;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "product.client")
public class ProductClientProperties {

    private String transport = "http";

    @NotBlank
    private String httpBaseUrl = "http://localhost:8082";

    @NotBlank
    private String grpcTarget = "localhost:9090";

    @Min(1)
    private int connectTimeoutMs = 1000;

    @Min(1)
    private int readTimeoutMs = 2000;

    @Min(1)
    private int deadlineMs = 2000;

    @AssertTrue(message = "product.client.transport must be either 'http' or 'grpc'")
    public boolean isTransportSupported() {
        return "http".equals(transport) || "grpc".equals(transport);
    }
}
