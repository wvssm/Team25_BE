package com.team25.backend.global.security.provider;

import static com.team25.backend.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

import com.team25.backend.global.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomAuthenticationProviderTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private CustomAuthenticationProvider customAuthenticationProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("유저가 존재할 때 인증이 성공하고 UsernamePasswordAuthenticationToken을 반환한다")
    void authenticate_유저가_존재할_때_인증_성공_토큰_반환() {
        // given
        String username = "testUser";
        UserDetails user = mock(UserDetails.class);
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(user);
        when(user.getAuthorities()).thenReturn(Collections.emptyList());

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null);

        // when
        Authentication result = customAuthenticationProvider.authenticate(authentication);

        // then
        assertEquals(user, result.getPrincipal());
        assertNull(result.getCredentials());
        assertEquals(user.getAuthorities(), result.getAuthorities());
    }

    @Test
    @DisplayName("유저가 존재하지 않을 때 예외를 발생시킨다")
    void authenticate_유저가_존재하지_않을_떄_예외_발생() {
        // given
        String username = "nonExistentUser";
        when(customUserDetailsService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException(USER_NOT_FOUND.getMessage()));

        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null);

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> customAuthenticationProvider.authenticate(authentication));
    }

    @Test
    @DisplayName("UsernamePasswordAuthenticationToken을 지원하는지 확인한다")
    void supports_UsernamePasswordAuthenticationTokend을_지원하면_TRUE() {
        // when
        boolean result = customAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class);

        // then
        assertEquals(true, result);
    }
}