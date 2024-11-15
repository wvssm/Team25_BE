package com.team25.backend.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class JWTUtilTest {
    private JWTUtil jwtUtil;

    @BeforeEach
    public void setUp(){
        String secret = "myTestSecretKey12345myTestSecretKey12345";
        jwtUtil = new JWTUtil(secret);
    }

    @Test
    @DisplayName("JWT Util이 토큰의 uuid를 올바르게 추출하는가")
    void getUuid() {
        // given
        String category = "access";
        String uuid = "test-uuid";
        Long expiration = 60000L;

        // when
        String token = jwtUtil.createJwt(category, uuid, expiration);

        // then
        assertEquals(uuid, jwtUtil.getUuid(token));
    }

    @Test
    @DisplayName("JWT Util이 토큰의 category를 올바르게 추출하는가")
    void getCategory() {
        // given
        String category = "access";
        String uuid = "test-uuid";
        Long expiration = 60000L;

        // when
        String token = jwtUtil.createJwt(category, uuid, expiration);

        // then
        assertEquals(category, jwtUtil.getCategory(token));
    }

    @Test
    @DisplayName("JWT Util이 토큰의 만료되지 않았을 때 올바르게 판단하는가")
    void isExpired() {
        // given
        String category = "access";
        String uuid = "test-uuid";
        Long expiration = 60000L;

        // when
        String token = jwtUtil.createJwt(category, uuid, expiration);

        // then
        assertFalse(jwtUtil.isExpired(token));
    }

    @Test
    @DisplayName("JWT Util이 토큰이 만료되었을 때 올바르게 판단하는가")
    void isExpired2() throws InterruptedException {
        // given
        String token = jwtUtil.createJwt("access", "test-uuid", 1000L);

        // when
        Thread.sleep(1500);

        // then
        assertThatThrownBy(() -> jwtUtil.isExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("JWT Util이 토큰을 생성하고 해당 토큰의 내용이 올바른가")
    void createJwt() {
        // given
        String category = "access";
        String uuid = "test-uuid";
        Long expiration = 60000L;

        // when
        String token = jwtUtil.createJwt(category, uuid, expiration);

        // then
        // parsing
        Claims claims = Jwts.parser()
                .setSigningKey(new SecretKeySpec("myTestSecretKey12345myTestSecretKey12345".getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .build()
                .parseClaimsJws(token)
                .getBody();

        // check category and uuid
        assertEquals(category, claims.get("category", String.class));
        assertEquals(uuid, claims.get("uuid", String.class));

        Date issuedAt = claims.getIssuedAt();
        Date expirationDate = claims.getExpiration();

        // check expiration
        assertNotNull(issuedAt);
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
        assertTrue(expirationDate.getTime() - issuedAt.getTime() <= expiration);

    }
}