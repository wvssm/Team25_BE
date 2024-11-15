package com.team25.backend.domain.report.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team25.backend.domain.report.dto.request.ReportRequest;
import com.team25.backend.domain.report.dto.response.ReportResponse;
import com.team25.backend.domain.report.enumdomain.MedicineTime;
import com.team25.backend.domain.report.service.ReportService;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.exception.GlobalExceptionHandler;
import com.team25.backend.global.resolver.CustomAuthenticationPrincipalArgumentResolver;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @MockBean
    ReportService reportService;

    @Autowired
    ReportController reportController;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomAuthenticationPrincipalArgumentResolver customAuthenticationPrincipalArgumentResolver;


    private ObjectMapper objectMapper;

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

        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilter((request, response, chain) -> {
                response.setCharacterEncoding("UTF-8");
                chain.doFilter(request, response);
            })
            .build();

        User testUser = new User("user", "tesetuuid", "ROLE_USER");
        when(customAuthenticationPrincipalArgumentResolver.supportsParameter(any())).thenReturn(
            true);
        when(customAuthenticationPrincipalArgumentResolver.resolveArgument(any(), any(), any(),
            any()))
            .thenReturn(testUser);

        SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken("user", null, "ROLE_USER"));
    }

    @Test
    @DisplayName("리포트 조회 컨트롤러 테스트")
    void getReport() throws Exception {
        // given
        ArrayList<ReportResponse> reports = new ArrayList<>();
        reports.add(
            new ReportResponse("doctorsummary", 3, MedicineTime.AFTER_MEAL.toString(), "아침점심"));
        reports.add(
            new ReportResponse("doctorsummary", 3, MedicineTime.IN_MEAL.toString(), "아침점심저녁"));
        when(reportService.getReport(anyLong()))
            .thenReturn(reports);

        // when & then
        mockMvc.perform(get("/api/reports/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("리포트 조회를 성공했습니다"))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    List<ReportResponse> list = objectMapper.convertValue(rootNode.get("data"),
                        new TypeReference<>() {
                        });
                    for (ReportResponse response : list) {
                        assertThat(response).isNotNull();
                        assertThat(response.doctorSummary()).isEqualTo("doctorsummary");
                        assertThat(response.frequency()).isEqualTo(3);
                        if(response.medicineTime().equals(MedicineTime.AFTER_MEAL)) {
                            assertThat(response.timeOfDays()).isEqualTo("아침점심");
                        }else if(response.medicineTime().equals(MedicineTime.IN_MEAL)) {
                            assertThat(response.timeOfDays()).isEqualTo("아침점심저녁");
                        }
                    }
                }
            ).andDo(print());

    }

    @Test
    @DisplayName("리포트 생성 컨트롤러 테스트")
    void createReport() throws Exception {
        // given
        String content = """
            {
              "doctorSummary": "의사 소견",
              "frequency": 2,
              "medicineTime": "식후 30분",
              "timeOfDays": "아침 저녁"
            }
            """;
        when(reportService.createReport(anyLong(), any(ReportRequest.class))).thenReturn(
            new ReportResponse("doctorsummary", 3, MedicineTime.AFTER_MEAL.toString(), "아침점심")
        );

        // when & then
        mockMvc.perform(post("/api/manager/reports/1").contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("리포트 생성이 완료되었습니다"))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                    ReportResponse response = extractResponseFromResult(result);
                    assertThat(response).isNotNull();
                    assertThat(response.doctorSummary()).isEqualTo("doctorsummary");
                    assertThat(response.frequency()).isEqualTo(3);
                    assertThat(response.medicineTime()).isEqualTo(MedicineTime.AFTER_MEAL.toString());
                    assertThat(response.timeOfDays()).isEqualTo("아침점심");
                }
            ).andDo(print());
    }

    private ReportResponse extractResponseFromResult(MvcResult result)
        throws UnsupportedEncodingException, JsonProcessingException {
        String responseBody = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        return objectMapper.treeToValue(rootNode.get("data"), ReportResponse.class);
    }
}
