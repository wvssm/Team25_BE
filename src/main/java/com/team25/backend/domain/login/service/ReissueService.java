package com.team25.backend.domain.login.service;

import com.team25.backend.domain.login.dto.response.TokenResponse;
import com.team25.backend.domain.login.repository.RefreshRepository;
import com.team25.backend.domain.user.dto.response.UserResponse;
import com.team25.backend.global.exception.CustomException;
import com.team25.backend.global.util.JWTUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.team25.backend.global.exception.ErrorCode.NOT_EXISTED_REFRESH_TOKEN;
import static com.team25.backend.global.exception.ErrorCode.NOT_REFRESH_TOKEN;

@Service
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final JWTService jwtService;

    public ReissueService(JWTUtil jwtUtil, RefreshRepository refreshRepository, JWTService jwtService) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.jwtService = jwtService;
    }

    public void validateRefreshToken(String refresh){
        jwtUtil.isExpired(refresh);
        validateTokenCategory(refresh);
        validateTokenExistence(refresh);
    }

    private void validateTokenCategory(String refresh) {
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new CustomException(NOT_REFRESH_TOKEN);
        }
    }

    private void validateTokenExistence(String refresh) {
        if (!refreshRepository.existsByRefresh(refresh)) {
            throw new CustomException(NOT_EXISTED_REFRESH_TOKEN);
        }
    }

    @Transactional
    public TokenResponse getNewRefreshToken(String refresh){
        deleteRefreshToken(refresh);

        String userUUID = jwtUtil.getUuid(refresh);
        return jwtService.generateJwtToken(new UserResponse(null, userUUID,null));
    }

    @Transactional
    public void deleteRefreshToken(String refresh) {
        refreshRepository.deleteByRefresh(refresh);
    }

    public boolean isRefreshTokenExisted(String refresh){
        return refreshRepository.existsByRefresh(refresh);
    }
}
