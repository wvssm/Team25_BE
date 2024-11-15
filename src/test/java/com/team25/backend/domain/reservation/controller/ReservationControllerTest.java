package com.team25.backend.domain.reservation.controller;

import static com.team25.backend.global.exception.ErrorCode.INVALID_ARRIVAL_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.team25.backend.domain.patient.dto.response.PatientResponse;
import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.reservation.dto.request.CancelRequest;
import com.team25.backend.domain.reservation.dto.request.ReservationstatusRequest;
import com.team25.backend.domain.reservation.dto.response.ReservationResponse;
import com.team25.backend.domain.reservation.enumdomain.ReservationStatus;
import com.team25.backend.domain.reservation.enumdomain.ServiceType;
import com.team25.backend.domain.reservation.enumdomain.Transportation;
import com.team25.backend.domain.reservation.service.ReservationService;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.exception.CustomException;
import com.team25.backend.global.exception.GlobalExceptionHandler;
import com.team25.backend.global.resolver.CustomAuthenticationPrincipalArgumentResolver;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

    @MockBean
    ReservationService reservationService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomAuthenticationPrincipalArgumentResolver customAuthenticationPrincipalArgumentResolver;

    @Autowired
    private ReservationController reservationController;

    private ObjectMapper objectMapper;
    private PatientResponse patient;

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

        mockMvc = MockMvcBuilders.standaloneSetup(reservationController)
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

        patient = new PatientResponse(
            "산지니",
            "01012345678",
            PatientGender.MALE,
            "가족",
            LocalDate.of(1974, 4, 15).toString(),
            "01012344221"
        );
        SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken("user", null, "ROLE_USER"));
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 도착지 누락")
    void createReservationWithFailure() throws Exception {
        String content = """
            {
                "managerId": 1,
                "departureLocation": "부산광역시 금정구 부산대학로",
                "arrivalLocation": "",
                "reservationDateTime": "2024-09-04 15:32",
                "serviceType": "정기동행",
                "transportation": "대중교통",
                "price": 100,
                "patient": {
                    "name": "산지니",
                    "phoneNumber": "010-1234-5678",
                    "patientGender": "남성",
                    "patientRelation": "가족",
                    "birthDate": "1974-04-15",
                    "nokPhone": "010-1234-4221"
                }
            }
            """;

        doThrow(new CustomException(INVALID_ARRIVAL_ADDRESS))
            .when(reservationService)
            .createReservation(any(), any());

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            // Response 기본 구조 검증
            .andExpect(jsonPath("$.status").value(false))
            .andExpect(jsonPath("$.message").value(INVALID_ARRIVAL_ADDRESS.getMessage()))
            .andExpect(jsonPath("$.data").doesNotExist())  // 에러시 data는 null
            // 응답 바디 전체 검증
            .andExpect(result -> {
                String responseBody = result.getResponse().getContentAsString();
                assertThat(responseBody).contains("유효하지 않는 도착 장소입니다");
                // JSON 구조 검증
                JsonNode root = objectMapper.readTree(responseBody);
                assertThat(root.has("status")).isTrue();
                assertThat(root.has("message")).isTrue();
                assertThat(root.get("status").asBoolean()).isFalse();
            })
            .andDo(print());
    }

    @Test
    @DisplayName("예약 생성 성공 테스트")
    void createReservationTest() throws Exception {
        String content = """
            {
                "managerId": 1,
                "departureLocation": "부산광역시 금정구 부산대학로",
                "arrivalLocation": "부산대학교병원",
                "reservationDateTime": "2024-09-04 15:32",
                "serviceType": "정기동행",
                "transportation": "대중교통",
                "price": 100,
                "patient": {
                    "name": "산지니",
                    "phoneNumber": "010-1234-5678",
                    "patientGender": "남성",
                    "patientRelation": "가족",
                    "birthDate": "1974-04-15",
                    "nokPhone": "010-1234-4221"
                }
            }
            """;

        when(reservationService.createReservation(any(), any())).thenReturn(new ReservationResponse(
            1L,
            1L,
            "부산광역시 금정구 부산대학로",
            "부산대학교병원",
            LocalDateTime.of(2024, 9, 4, 15, 32),
            ServiceType.REGULAR_ACCOMPANIMENT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            ReservationStatus.HOLD,
            new PatientResponse(
                "산지니",
                "01012345678",
                PatientGender.MALE,
                "가족",
                LocalDate.of(1974, 4, 15).toString(),
                "01012344221"
            )
        ));

        mockMvc.perform(post("/api/reservations")
                .contentType(APPLICATION_JSON)
                .content(content))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("예약이 접수되었습니다"))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                ReservationResponse response = getJsonNode(result);
                assertThat(response).isNotNull();
                assertThat(response.reservationId()).isEqualTo(1L);
                assertThat(response.departureLocation()).isEqualTo("부산광역시 금정구 부산대학로");
                assertThat(response.arrivalLocation()).isEqualTo("부산대학교병원");
                assertThat(response.reservationDateTime()).isEqualTo("2024-09-04T15:32");
                assertThat(response.price()).isEqualTo(10000);
                assertThat(response.patient()).isNotNull();
                assertThat(response.transportation()).isEqualTo(
                    Transportation.PUBLIC_TRANSPORTATION);
                assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.HOLD);
                assertThat(response.serviceType()).isEqualTo(ServiceType.REGULAR_ACCOMPANIMENT);
                assertThat(response.patient().name()).isEqualTo("산지니");
                assertThat(response.patient().phoneNumber()).isEqualTo("01012345678");
                assertThat(response.patient().birthDate()).isEqualTo(
                    LocalDate.of(1974, 4, 15).toString());
                assertThat(response.patient().nokPhone()).isEqualTo("01012344221");
                assertThat(response.patient().patientRelation()).isEqualTo("가족");
            })
            .andDo(print());
    }


    @Test
    @DisplayName("특정 예약 조회 테스트")
    public void retrieveReservationByReservationId() throws Exception {
        // given
        String content = """
            {
                "reservationId": 1"
            }
            """;

        // when
        when(reservationService.getReservationById(any(User.class), anyLong())).thenReturn(
            new ReservationResponse(
                1L,
                1L,
                "부산광역시 금정구 부산대학로",
                "부산대학교병원",
                LocalDateTime.of(2024, 9, 4, 15, 32),
                ServiceType.REGULAR_ACCOMPANIMENT,
                Transportation.PUBLIC_TRANSPORTATION,
                10000,
                ReservationStatus.HOLD,
                patient
            )
        );

        // then
        mockMvc.perform(get("/api/reservations/1")
                .contentType(APPLICATION_JSON)
                .content(content))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("예악 조회가 성공하였습니다."))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                ReservationResponse response = getJsonNode(result);

                assertThat(response).isNotNull();
                assertThat(response.reservationId()).isEqualTo(1L);
                assertThat(response.departureLocation()).isEqualTo("부산광역시 금정구 부산대학로");
                assertThat(response.arrivalLocation()).isEqualTo("부산대학교병원");
                assertThat(response.reservationDateTime()).isEqualTo("2024-09-04T15:32");
                assertThat(response.price()).isEqualTo(10000);
                assertThat(response.patient()).isNotNull();
                assertThat(response.transportation()).isEqualTo(
                    Transportation.PUBLIC_TRANSPORTATION);
                assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.HOLD);
                assertThat(response.serviceType()).isEqualTo(ServiceType.REGULAR_ACCOMPANIMENT);
                assertThat(response.patient().name()).isEqualTo("산지니");
                assertThat(response.patient().phoneNumber()).isEqualTo("01012345678");
                assertThat(response.patient().birthDate()).isEqualTo(
                    LocalDate.of(1974, 4, 15).toString());
                assertThat(response.patient().nokPhone()).isEqualTo("01012344221");
                assertThat(response.patient().patientRelation()).isEqualTo("가족");
            });
    }

    @Test
    @DisplayName("예약 취소 테스트")
    public void cancelReservationTest() throws Exception {
        // given
        String content = """
              {
                "cancelReason": "단순변심",
                "cancelDetail": "단순 변심으로 인한 예약 취소입니다."
              }
            """;

        // when
        when(reservationService.cancelReservation(any(User.class), any(CancelRequest.class),
            anyLong()))
            .thenReturn(new ReservationResponse(
                1L,
                1L,
                "부산광역시 금정구 부산대학로",
                "부산대학교병원",
                LocalDateTime.of(2024, 9, 4, 15, 32),
                ServiceType.REGULAR_ACCOMPANIMENT,
                Transportation.PUBLIC_TRANSPORTATION,
                10000,
                ReservationStatus.CANCEL,
                patient
            ));

        // then
        mockMvc.perform(patch("/api/reservations/cancel/1")
                .contentType(APPLICATION_JSON)
                .content(content))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("예약 취수가 접수되었습니다"))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                ReservationResponse response = getJsonNode(result);

                assertThat(response).isNotNull();
                assertThat(response.reservationId()).isEqualTo(1L);
                assertThat(response.departureLocation()).isEqualTo("부산광역시 금정구 부산대학로");
                assertThat(response.arrivalLocation()).isEqualTo("부산대학교병원");
                assertThat(response.reservationDateTime()).isEqualTo("2024-09-04T15:32");
                assertThat(response.price()).isEqualTo(10000);
                assertThat(response.patient()).isNotNull();
                assertThat(response.transportation()).isEqualTo(
                    Transportation.PUBLIC_TRANSPORTATION);
                assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.CANCEL);
                assertThat(response.serviceType()).isEqualTo(ServiceType.REGULAR_ACCOMPANIMENT);
                assertThat(response.patient().name()).isEqualTo("산지니");
                assertThat(response.patient().phoneNumber()).isEqualTo("01012345678");
                assertThat(response.patient().birthDate()).isEqualTo(
                    LocalDate.of(1974, 4, 15).toString());
                assertThat(response.patient().nokPhone()).isEqualTo("01012344221");
                assertThat(response.patient().patientRelation()).isEqualTo("가족");
            });
    }

    @Test
    @DisplayName("예약 전체 조회 테스트")
    public void retrieveAllReservationTest() throws Exception {
        // given
        ArrayList<ReservationResponse> reservationResponses = new ArrayList<>();
        reservationResponses.add(new ReservationResponse(2L, 1L,
            "부산광역시 금정구 부산대학로2",
            "부산대학교병원2",
            LocalDateTime.of(2024, 9, 5, 15, 32),
            ServiceType.REGULAR_ACCOMPANIMENT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            ReservationStatus.HOLD,
            patient
        ));
        reservationResponses.add(new ReservationResponse(3L, 1L,
            "부산광역시 금정구 부산대학로3",
            "부산대학교병원3",
            LocalDateTime.of(2024, 9, 5, 15, 32),
            ServiceType.REGULAR_ACCOMPANIMENT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            ReservationStatus.CONFIRMED,
            patient
        ));

        // when
        when(reservationService.getAllReservations(any(User.class))).thenReturn(
            reservationResponses);

        // then
        mockMvc.perform(get("/api/reservations")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("사용자의 예약 목록을 조회하였습니다."))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                String responseBody = result.getResponse().getContentAsString();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode dataNode = rootNode.get("data");
                List<ReservationResponse> list = objectMapper.convertValue(dataNode,
                    new TypeReference<>() {
                    });
                assertThat(list).hasSize(2);
                for (ReservationResponse reservationResponse : list) {
                    assertThat(reservationResponse).isNotNull();
                    assertThat(reservationResponse.managerId()).isEqualTo(1L);
                    assertThat(reservationResponse.patient()).isNotNull();
                    assertThat(reservationResponse.serviceType()).isEqualTo(
                        ServiceType.REGULAR_ACCOMPANIMENT);
                    assertThat(reservationResponse.price()).isEqualTo(10000);
                    assertThat(reservationResponse.transportation()).isEqualTo(
                        Transportation.PUBLIC_TRANSPORTATION);
                    assertThat(reservationResponse.reservationDateTime()).isEqualTo(
                        "2024-09-05T15:32");
                    if (reservationResponse.reservationId() == 2L) {
                        assertThat(reservationResponse.departureLocation()).isEqualTo(
                            "부산광역시 금정구 부산대학로2");
                        assertThat(reservationResponse.arrivalLocation()).isEqualTo("부산대학교병원2");
                        assertThat(reservationResponse.reservationStatus()).isEqualTo(
                            ReservationStatus.HOLD);
                    } else if (reservationResponse.reservationId() == 3L) {
                        assertThat(reservationResponse.departureLocation()).isEqualTo(
                            "부산광역시 금정구 부산대학로3");
                        assertThat(reservationResponse.arrivalLocation()).isEqualTo("부산대학교병원3");
                        assertThat(reservationResponse.reservationStatus()).isEqualTo(
                            ReservationStatus.CONFIRMED);
                    }
                }

            });
    }

    @Test
    @DisplayName("예약 상태 변경 테스트")
    public void changeReservationStatusTest() throws Exception {
        // give
        String content = """
            {
                "reservationStatus": "확정"
            }
            """;

        // when
        when(reservationService.changeReservationStatus(any(User.class), anyLong(), any(
            ReservationstatusRequest.class))).thenReturn(new ReservationResponse(2L, 1L,
            "부산광역시 금정구 부산대학로",
            "부산대학교병원",
            LocalDateTime.of(2024, 9, 4, 15, 32),
            ServiceType.REGULAR_ACCOMPANIMENT,
            Transportation.PUBLIC_TRANSPORTATION,
            10000,
            ReservationStatus.CONFIRMED,
            patient
        ));

        // then
        mockMvc.perform(patch("/api/reservations/change/2")
                .contentType(APPLICATION_JSON)
                .content(content))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("예약 상태가 변경되었습니다"))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                ReservationResponse response = getJsonNode(result);

                assertThat(response).isNotNull();
                assertThat(response.reservationId()).isEqualTo(2L);
                assertThat(response.departureLocation()).isEqualTo("부산광역시 금정구 부산대학로");
                assertThat(response.arrivalLocation()).isEqualTo("부산대학교병원");
                assertThat(response.reservationDateTime()).isEqualTo("2024-09-04T15:32");
                assertThat(response.price()).isEqualTo(10000);
                assertThat(response.patient()).isNotNull();
                assertThat(response.transportation()).isEqualTo(
                    Transportation.PUBLIC_TRANSPORTATION);
                assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
                assertThat(response.serviceType()).isEqualTo(ServiceType.REGULAR_ACCOMPANIMENT);
                assertThat(response.patient().name()).isEqualTo("산지니");
                assertThat(response.patient().phoneNumber()).isEqualTo("01012345678");
                assertThat(response.patient().birthDate()).isEqualTo(
                    LocalDate.of(1974, 4, 15).toString());
                assertThat(response.patient().nokPhone()).isEqualTo("01012344221");
                assertThat(response.patient().patientRelation()).isEqualTo("가족");
            });
    }


    @Test
    @DisplayName("매니저 담당 예약 조회 테스트")
    public void findManagerReservationTest() throws Exception {
        // given
        ArrayList<ReservationResponse> reservationResponseList = new ArrayList<>();
        reservationResponseList.add(new ReservationResponse(11L, 5L, "department", "arrival",
            LocalDateTime.of(2024, 5, 15, 12, 14), ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION, 50000, ReservationStatus.CONFIRMED, patient));
        reservationResponseList.add(new ReservationResponse(12L, 5L, "department2", "arrival2",
            LocalDateTime.of(2024, 5, 15, 12, 14), ServiceType.CLINIC_ESCORT,
            Transportation.PUBLIC_TRANSPORTATION, 50000, ReservationStatus.CONFIRMED, patient));
        when(reservationService.getManagerReservation(any(User.class))).thenReturn(
            reservationResponseList);

        // when
        mockMvc.perform(get("/api/reservations/manager")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(true))
            .andExpect(jsonPath("$.message").value("매니저용 전체 예약 목록을 조회하였습니다."))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andExpect(result -> {
                String responseBody = result.getResponse().getContentAsString();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode dataNode = rootNode.get("data");
                List<ReservationResponse> list = objectMapper.convertValue(dataNode,
                    new TypeReference<>() {
                    });
                assertThat(list).hasSize(2);
                for (ReservationResponse reservationResponse : list) {
                    assertThat(reservationResponse).isNotNull();
                    assertThat(reservationResponse.managerId()).isEqualTo(5L);
                    assertThat(reservationResponse.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
                    assertThat(reservationResponse.reservationDateTime()).isEqualTo("2024-05-15T12:14");
                    assertThat(reservationResponse.patient()).isNotNull();
                    assertThat(reservationResponse.transportation()).isEqualTo(Transportation.PUBLIC_TRANSPORTATION);
                    if (reservationResponse.reservationId() == 11L) {
                        assertThat(reservationResponse.departureLocation()).isEqualTo("department");
                        assertThat(reservationResponse.arrivalLocation()).isEqualTo("arrival");
                    }else if(reservationResponse.reservationId() == 12L){
                        assertThat(reservationResponse.departureLocation()).isEqualTo("department2");
                        assertThat(reservationResponse.arrivalLocation()).isEqualTo("arrival2");
                    }
                }
            });

        // then
    }


    private ReservationResponse getJsonNode(MvcResult result)
        throws UnsupportedEncodingException, JsonProcessingException {
        String responseBody = result.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseBody);
        return objectMapper.treeToValue(rootNode.get("data"), ReservationResponse.class);
    }
}

