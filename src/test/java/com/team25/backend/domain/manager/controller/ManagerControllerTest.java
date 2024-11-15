package com.team25.backend.domain.manager.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team25.backend.domain.manager.dto.request.*;
import com.team25.backend.domain.manager.dto.response.*;
import com.team25.backend.domain.manager.service.ManagerService;
import com.team25.backend.domain.user.entity.User;
import com.team25.backend.global.exception.GlobalExceptionHandler;
import com.team25.backend.global.resolver.CustomAuthenticationPrincipalArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {

    @MockBean
    private ManagerService managerService;

    @MockBean
    private CustomAuthenticationPrincipalArgumentResolver customAuthenticationPrincipalArgumentResolver;

    @Autowired
    private ManagerController managerController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

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

        mockMvc = MockMvcBuilders.standaloneSetup(managerController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilter((request, response, chain) -> {
                response.setCharacterEncoding("UTF-8");
                chain.doFilter(request, response);
            })
            .build();

        User testUser = new User("user", "testuuid", "ROLE_USER");

        when(customAuthenticationPrincipalArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(customAuthenticationPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
            .thenReturn(testUser);

        SecurityContextHolder.getContext()
            .setAuthentication(new TestingAuthenticationToken("user", null, "ROLE_USER"));
    }

    @Test
    @DisplayName("매니저 목록 조회 테스트")
    public void testGetManagers() throws Exception {
        // Given
        String date = "2023-10-05";
        String region = "서울";

        List<ManagerByDateAndRegionResponse> managerResponses = new ArrayList<>();
        managerResponses.add(new ManagerByDateAndRegionResponse(
            1L, "Manager Name", "profile.jpg", "5 years", "Comment", "남성"
        ));

        when(managerService.getManagersByDateAndRegion(date, region)).thenReturn(managerResponses);

        // When & Then
        mockMvc.perform(get("/api/managers")
                .param("date", date)
                .param("region", region))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("매니저 조회에 성공하였습니다.")))
            .andExpect(jsonPath("$.data[0].managerId", is(1)))
            .andExpect(jsonPath("$.data[0].name", is("Manager Name")))
            .andExpect(jsonPath("$.data[0].profileImage", is("profile.jpg")))
            .andExpect(jsonPath("$.data[0].career", is("5 years")))
            .andExpect(jsonPath("$.data[0].comment", is("Comment")))
            .andExpect(jsonPath("$.data[0].gender", is("남성")));
    }

    @Test
    @DisplayName("매니저 등록 테스트")
    void testCreateManager() throws Exception {
        // Given
        ManagerCreateRequest request = new ManagerCreateRequest(
            "Manager Name", "profile.jpg", "5 years", "Comment", "certificate.jpg", "남성"
        );

        ManagerCreateResponse response = new ManagerCreateResponse();

        when(managerService.createManager(any(User.class), any(ManagerCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/manager")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("매니저 등록을 성공했습니다.")))
            .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("특정 매니저 프로필 조회 테스트")
    public void testGetManagerProfileById() throws Exception {
        // Given
        Long managerId = 1L;

        ManagerProfileResponse response = new ManagerProfileResponse(
            "Manager Name",
            "profile.jpg",
            "5 years",
            "Comment",
            "Seoul",
            "남성",
            new ManagerProfileResponse.WorkingHourResponse(
                "09:00", "18:00",
                "09:00", "18:00",
                "09:00", "18:00",
                "09:00", "18:00",
                "09:00", "18:00",
                "00:00", "00:00",
                "00:00", "00:00"
            )
        );

        when(managerService.getManagerProfile(managerId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/manager/profile/{manager_id}", managerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("프로필 조회를 성공했습니다.")))
            .andExpect(jsonPath("$.data.name", is("Manager Name")))
            .andExpect(jsonPath("$.data.profileImage", is("profile.jpg")));
    }

    @Test
    @DisplayName("로그인한 매니저 프로필 조회 테스트")
    public void testGetManagerProfileForLoggedInUser() throws Exception {
        // Given
        ManagerProfileResponse response = new ManagerProfileResponse(
            "Manager Name",
            "profile.jpg",
            "5 years",
            "Comment",
            "Seoul",
            "남성",
            new ManagerProfileResponse.WorkingHourResponse(
                "09:00", "18:00",
                "09:00", "18:00",
                "09:00", "18:00",
                "09:00", "18:00",
                "09:00", "18:00",
                "00:00", "00:00",
                "00:00", "00:00"
            )
        );

        when(managerService.getManagerProfile(any(User.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/manager/me/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("프로필 조회를 성공했습니다.")))
            .andExpect(jsonPath("$.data.name", is("Manager Name")))
            .andExpect(jsonPath("$.data.profileImage", is("profile.jpg")));
    }

    @Test
    @DisplayName("근무 시간 변경 테스트")
    public void testAddWorkingHour() throws Exception {
        // Given
        ManagerWorkingHourUpdateRequest request = new ManagerWorkingHourUpdateRequest(
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "00:00", "00:00",
            "00:00", "00:00"
        );

        ManagerWorkingHourUpdateResponse response = new ManagerWorkingHourUpdateResponse(
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "09:00", "18:00",
            "00:00", "00:00",
            "00:00", "00:00"
        );

        when(managerService.updateWorkingHour(any(User.class), any(ManagerWorkingHourUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/manager/time")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("근무 시간을 성공적으로 변경했습니다.")))
            .andExpect(jsonPath("$.data.monStartTime", is("09:00")))
            .andExpect(jsonPath("$.data.monEndTime", is("18:00")));
    }

    @Test
    @DisplayName("프로필 이미지 변경 테스트")
    public void testUpdateProfileImage() throws Exception {
        // Given
        ManagerProfileImageUpdateRequest request = new ManagerProfileImageUpdateRequest("new_profile.jpg");

        ManagerProfileImageUpdateResponse response = new ManagerProfileImageUpdateResponse("new_profile.jpg");

        when(managerService.updateProfileImage(any(User.class), any(ManagerProfileImageUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/manager/image")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("프로필 사진을 성공적으로 변경했습니다.")))
            .andExpect(jsonPath("$.data.profileImage", is("new_profile.jpg")));
    }

    @Test
    @DisplayName("코멘트 변경 테스트")
    public void testUpdateComment() throws Exception {
        // Given
        ManagerCommentUpdateRequest request = new ManagerCommentUpdateRequest("Updated comment");

        ManagerCommentUpdateResponse response = new ManagerCommentUpdateResponse("Updated comment");

        when(managerService.updateComment(any(User.class), any(ManagerCommentUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/manager/comment")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("코멘트를 성공적으로 변경했습니다.")))
            .andExpect(jsonPath("$.data.comment", is("Updated comment")));
    }

    @Test
    @DisplayName("근무 지역 변경 테스트")
    public void testUpdateLocation() throws Exception {
        // Given
        ManagerLocationUpdateRequest request = new ManagerLocationUpdateRequest("Seoul");

        ManagerLocationUpdateResponse response = new ManagerLocationUpdateResponse("Seoul");

        when(managerService.updateLocation(any(User.class), any(ManagerLocationUpdateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/manager/location")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(true)))
            .andExpect(jsonPath("$.message", is("근무 지역을 성공적으로 변경했습니다.")))
            .andExpect(jsonPath("$.data.workingRegion", is("Seoul")));
    }
}