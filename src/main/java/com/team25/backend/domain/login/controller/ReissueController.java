package com.team25.backend.domain.login.controller;

import com.team25.backend.domain.login.service.ReissueService;
import com.team25.backend.domain.login.dto.request.ReissueRequest;
import com.team25.backend.global.dto.response.ApiResponse;
import com.team25.backend.domain.login.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ReissueController {
    private final ReissueService reissueService;

    public ReissueController(ReissueService reissueService) {
        this.reissueService = reissueService;
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest reissueRequest){
        String refreshToken = reissueRequest.refreshToken();
        reissueService.validateRefreshToken(refreshToken);
        TokenResponse reissueResponse = reissueService.getNewRefreshToken(refreshToken);

        return ResponseEntity.ok(new ApiResponse<>(true, "토큰이 재발급 되었습니다.", reissueResponse));
    }
}
