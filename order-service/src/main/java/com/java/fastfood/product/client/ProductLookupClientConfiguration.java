package com.java.fastfood.product.client;

import com.java.fastfood.product.port.ProductAvailabilityLookup;
import com.java.fastfood.product.port.ProductPriceLookup;
import com.java.fastfood.product.port.ProductStockLookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(ProductClientProperties.class)
public class ProductLookupClientConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "product.client", name = "transport", havingValue = "http", matchIfMissing = true)
    public ProductLookupHttpClient productLookupHttpClient(ProductClientProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getHttpBaseUrl())
                .requestFactory(requestFactory)
                .build();
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return proxyFactory.createClient(ProductLookupHttpClient.class);
    }

    @Bean(destroyMethod = "shutdownNow")
    @ConditionalOnProperty(prefix = "product.client", name = "transport", havingValue = "grpc")
    public io.grpc.ManagedChannel productLookupManagedChannel(ProductClientProperties properties) {
        return io.grpc.ManagedChannelBuilder.forTarget(properties.getGrpcTarget())
                .usePlaintext()
                .build();
    }

    @Bean
    @Primary
    public ResilientProductLookupDecorator resilientProductLookupDecorator(
            ProductPriceLookup priceDelegate,
            ProductStockLookup stockDelegate,
            ProductAvailabilityLookup availabilityDelegate) {
        return new ResilientProductLookupDecorator(priceDelegate, stockDelegate, availabilityDelegate);
    }
}