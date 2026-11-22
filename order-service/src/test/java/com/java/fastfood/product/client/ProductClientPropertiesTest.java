package com.java.fastfood.product.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ProductClientPropertiesTest {

    @Test
    void unsupportedTransportFailsApplicationStartup() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProductLookupClientConfiguration.class)
                .withPropertyValues("product.client.transport=unsupported")
                .run(context -> assertThat(context).hasFailed());
    }
}
