package com.team25.backend.domain.payment.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team25.backend.domain.payment.dto.response.*;
import com.team25.backend.domain.payment.service.PaymentService;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.exception.GlobalExceptionHandler;
import com.team25.backend.global.resolver.CustomAuthenticationPrincipalArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private CustomAuthenticationPrincipalArgumentResolver customAuthenticationPrincipalArgumentResolver;

    @Autowired
    private PaymentController paymentController;

    private ObjectMapper objectMapper;

    private User testUser;

    private BillingKeyResponse billingKeyResponse;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .addModule(new JavaTimeModule())
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .enable(MapperFeature.USE_ANNOTATIONS)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
                .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilter((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                })
                .build();

        testUser = new User("user", "test-uuid", "ROLE_USER");

        when(customAuthenticationPrincipalArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(customAuthenticationPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(testUser);

        billingKeyResponse = new BillingKeyResponse("0000", "정상 처리되었습니다", "bid123", "2024-11-11T00:00:00.000+0900", "04", "삼성", "UT0015870m01162411111558592787", "66457ce0-1942-4f3e-b25c-a33c3baa0741");

        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("user", null, "ROLE_USER"));
    }

    @Test
    @DisplayName("빌링키 발급 테스트")
    void createBillingKey_Success() throws Exception {
        // Given
        String content = """
        {
          "encData": "755AEDECC755900457D3CF9C2EAE4A8B573AAB95662B9E3B5A55E760F74FE449287A47E8DC972BF4C5B7622ECBE12FA7B721A563679FFFF24BA0868E2B6E20F7DC9FE21BF2BCD4C3259BAD73CBBE3A28",
          "cardAlias": "테스트"
        }
    """;

        BillingKeyResponse mockResponse = new BillingKeyResponse(
                "0000",
                "정상 처리되었습니다",
                "bid123",
                "2024-11-11T00:00:00.000+0900",
                "04",
                "삼성",
                "UT0015870m01162411111558592787",
                "66457ce0-1942-4f3e-b25c-a33c3baa0741"
        );

        when(paymentService.createBillingKey(any(), any()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/payment/billing-key")
                        .contentType(APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("빌링키 발급을 성공했습니다."))
                .andExpect(jsonPath("$.data.resultCode").value("0000"))
                .andExpect(jsonPath("$.data.resultMsg").value("정상 처리되었습니다"))
                .andDo(print());
    }
}
