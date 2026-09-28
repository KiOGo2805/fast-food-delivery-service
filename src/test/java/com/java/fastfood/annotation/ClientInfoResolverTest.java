package com.java.fastfood.annotation;

import org.springframework.core.MethodParameter;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.java.fastfood.configuration.WebConfig;
import com.java.fastfood.controller.ProductController;
import com.java.fastfood.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebConfig.class)
class ClientInfoResolverTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void whenGetInfo_thenClientInfoIsResolved() throws Exception {
        mockMvc.perform(get("/api/products/info")
                        .header("User-Agent", "Mozilla/5.0")
                        .with(request -> {
                            request.setRemoteAddr("1.2.3.4");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userAgent").value("Mozilla/5.0"))
                .andExpect(jsonPath("$.remoteAddr").value("1.2.3.4"));
    }

    @Test
    void supportsParameter_returnsFalse_whenNoAnnotation() throws NoSuchMethodException {
        ClientInfoArgumentResolver resolver = new ClientInfoArgumentResolver();

        Method method = getClass().getDeclaredMethod("dummyMethod", ClientInfoData.class, String.class);

        MethodParameter annotatedParam = new MethodParameter(method, 0);
        MethodParameter unannotatedParam = new MethodParameter(method, 1);

        assertTrue(resolver.supportsParameter(annotatedParam), "Повинен повертати true для параметра з @ClientInfo");
        assertFalse(resolver.supportsParameter(unannotatedParam), "Повинен повертати false для параметра без анотації");
    }

    private void dummyMethod(@ClientInfo ClientInfoData data, String unannotatedParam) {
    }
}
