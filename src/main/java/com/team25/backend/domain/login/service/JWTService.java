package com.team25.backend.domain.login.service;

import com.team25.backend.domain.login.dto.response.TokenResponse;
import com.team25.backend.domain.user.dto.response.UserResponse;
import com.team25.backend.domain.login.entity.Refresh;
import com.team25.backend.global.util.JWTUtil;
import com.team25.backend.domain.login.repository.RefreshRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {
    private JWTUtil jwtUtil;
    private RefreshRepository refreshRepository;

    public JWTService(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    public TokenResponse generateJwtToken(UserResponse userDto){
        long minute = 60000L;
        long hour = minute * 60;
        long day = hour * 24;

        Long accessTokenExpiry = 15 * minute;  // 15분
        Long refreshTokenExpiry = 30 * day;  // 30일

        String access = jwtUtil.createJwt("access", userDto.uuid(), accessTokenExpiry);
        String refresh = jwtUtil.createJwt("refresh", userDto.uuid(), refreshTokenExpiry);
        addRefresh(userDto.uuid(), refresh, 120000L);

        return new TokenResponse(access, accessTokenExpiry, refresh, refreshTokenExpiry);
    }

    public void addRefresh(String uuid, String refreshToken, Long expiredMs) {
        Date date = new Date(System.currentTimeMillis() + expiredMs);
        Refresh refresh = new Refresh(uuid, refreshToken, date.toString());
        refreshRepository.save(refresh);
    }
}
