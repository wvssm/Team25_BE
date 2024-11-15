package com.team25.backend.domain.accompany.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team25.backend.domain.accompany.dto.request.AccompanyRequest;
import com.team25.backend.domain.accompany.dto.response.AccompanyResponse;
import com.team25.backend.domain.accompany.enumdomain.AccompanyStatus;
import com.team25.backend.domain.accompany.service.AccompanyService;
import com.team25.backend.domain.patient.dto.response.PatientResponse;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.exception.GlobalExceptionHandler;
import com.team25.backend.global.resolver.CustomAuthenticationPrincipalArgumentResolver;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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

@WebMvcTest(AccompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccompanyControllerTest {


    @MockBean
    AccompanyService accompanyService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomAuthenticationPrincipalArgumentResolver customAuthenticationPrincipalArgumentResolver;

    @Autowired
    AccompanyController accompanyController;

    private ObjectMapper objectMapper;
    private PatientResponse patient;
    // @Autowired
    // private ResponseEntityExceptionHandler responseEntityExceptionHandler;

    @BeforeEach
    void setup() throws Exception {
        objectMapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .addModule(new JavaTimeModule())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .enable(MapperFeature.USE_ANNOTATIONS)
            .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
            .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
            .build();

        mockMvc = MockMvcBuilders.standaloneSetup(accompanyController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilter((request, response, chain) -> {
                response.setCharacterEncoding("UTF-8");
                chain.doFilter(request, response);
            })
            .build();

        User testUser = new User("user", "tesetuuid", "ROLE_USER");
        patient = new PatientResponse(
            "산지니",
            "01012345678",
            PatientGender.MALE,
            "가족",
            LocalDate.of(1974, 4, 15).toString(),
            "01012344221"
        );

        when(customAuthenticationPrincipalArgumentResolver.supportsParameter(any())).thenReturn(
            true);
        when(customAuthenticationPrincipalArgumentResolver.resolveArgument(any(), any(), any(),
            any()))
            .thenReturn(testUser);
        SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken("user", null, "ROLE_USER"));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("실시간 동행 현황 조회 컨트롤러 테스트")
    void getTrackingTest() throws Exception {
        // given
        String content = """
            {
                "reservationId": 1
            }
            """;

        when(accompanyService.getTrackingAccompanies(anyLong()))
            .thenReturn(
                Collections.singletonList(new AccompanyResponse(
                    AccompanyStatus.EXAMINATION,
                    LocalDateTime.of(2024, 11, 11, 15, 14),
                    "Describe"
                ))
            );

        mockMvc.perform(get("/api/tracking/1")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("실시간 동행현황이 조회되었습니다."))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                String responseBody = result.getResponse().getContentAsString();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode dataNode = rootNode.get("data");
                List<AccompanyResponse> list = objectMapper.convertValue(dataNode,
                    new TypeReference<>() {
                    });
                for (AccompanyResponse response : list) {
                    assertThat(response.status()).isEqualTo(AccompanyStatus.EXAMINATION);
                    assertThat(response.statusDate()).isEqualTo(
                        LocalDateTime.of(2024, 11, 11, 15, 14));
                    assertThat(response.statusDescribe()).isEqualTo("Describe");
                }
            });
    }

    @Test
    @DisplayName("실시간 동행 현황 생성 컨트롤러 테스트")
    void addTrackingTest() throws Exception {
        // given
        String content = """
            {
                "status": "진료 접수",
                "statusDate": "2024-11-11 14:52",
                "statusDescribe": "Description"
            }
            """;

        when(accompanyService.addTrackingAccompany(anyLong(), any(AccompanyRequest.class)))
            .thenReturn(
                new AccompanyResponse(
                    AccompanyStatus.EXAMINATION,
                    LocalDateTime.of(2024, 11, 11, 15, 14),
                    "Describe"
                )
            );

        mockMvc.perform(post("/api/manager/tracking/1")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("실시간 동행현황이 작성되었습니다."))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                String responseBody = result.getResponse().getContentAsString();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                AccompanyResponse response = objectMapper.treeToValue(rootNode.get("data"),
                    AccompanyResponse.class);

                assertThat(response.status()).isEqualTo(AccompanyStatus.EXAMINATION);
                assertThat(response.statusDate()).isEqualTo(LocalDateTime.of(2024, 11, 11, 15, 14));
                assertThat(response.statusDescribe()).isEqualTo("Describe");
            });
    }
}