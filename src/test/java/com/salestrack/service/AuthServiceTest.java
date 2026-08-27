package com.salestrack.service;

import com.salestrack.dto.auth.AuthResponse;
import com.salestrack.dto.auth.RegisterRequest;
import com.salestrack.entity.User;
import com.salestrack.exception.DuplicateResourceException;
import com.salestrack.repository.UserRepository;
import com.salestrack.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void register_throwsException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Ahmet", "ahmet@salestrack.com", "gizli1234");

        when(userRepository.existsByEmailIgnoreCase("ahmet@salestrack.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_hashesPasswordBeforeSaving() {
        RegisterRequest request = new RegisterRequest("Ahmet", "ahmet@salestrack.com", "gizli1234");

        when(userRepository.existsByEmailIgnoreCase("ahmet@salestrack.com")).thenReturn(false);
        when(passwordEncoder.encode("gizli1234")).thenReturn("HASHED_VALUE");
        when(jwtService.generateToken(any())).thenReturn("fake-token");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("HASHED_VALUE", savedUser.getPassword());
        assertNotEquals("gizli1234", savedUser.getPassword());
    }
}