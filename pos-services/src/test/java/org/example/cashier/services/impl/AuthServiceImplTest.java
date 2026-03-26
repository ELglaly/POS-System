package org.example.cashier.services.impl;

import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock UserRepository  userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .username("cashier")
                .passwordHash("$2a$12$hashedPassword")
                .role(UserRole.CASHIER)
                .active(true)
                .failedLoginAttempts(0)
                .build();
    }

    // ── Password login ────────────────────────────────────────────────────

    @Nested
    @DisplayName("login(username, password)")
    class PasswordLogin {

        @Test
        @DisplayName("returns user on correct credentials")
        void successfulLogin() {
            when(userRepository.findByUsernameAndActiveTrue("cashier"))
                    .thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("secret", activeUser.getPasswordHash()))
                    .thenReturn(true);

            Optional<User> result = authService.login("cashier", "secret");

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("cashier");
        }

        @Test
        @DisplayName("resets failure count and records last-login timestamp on success")
        void resetsFailureCountOnSuccess() {
            activeUser.setFailedLoginAttempts(3);
            when(userRepository.findByUsernameAndActiveTrue("cashier"))
                    .thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            authService.login("cashier", "secret");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();
            assertThat(saved.getFailedLoginAttempts()).isZero();
            assertThat(saved.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("returns empty for unknown username")
        void unknownUsername() {
            when(userRepository.findByUsernameAndActiveTrue("ghost")).thenReturn(Optional.empty());

            assertThat(authService.login("ghost", "any")).isEmpty();
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns empty for wrong password and increments failure counter")
        void wrongPassword() {
            when(userRepository.findByUsernameAndActiveTrue("cashier"))
                    .thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThat(authService.login("cashier", "wrong")).isEmpty();

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("locks account after 5 consecutive failures")
        void locksAfterFiveFailures() {
            activeUser.setFailedLoginAttempts(4);
            when(userRepository.findByUsernameAndActiveTrue("cashier"))
                    .thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            authService.login("cashier", "wrong");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getLockedUntil()).isNotNull();
            assertThat(captor.getValue().getLockedUntil())
                    .isAfter(LocalDateTime.now().plusMinutes(14));
        }

        @Test
        @DisplayName("returns empty for a currently locked account without checking password")
        void rejectsLockedAccount() {
            activeUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
            when(userRepository.findByUsernameAndActiveTrue("cashier"))
                    .thenReturn(Optional.of(activeUser));

            assertThat(authService.login("cashier", "correct")).isEmpty();
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }
    }

    // ── Barcode login ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("loginWithBarcode(pin)")
    class BarcodeLogin {

        @Test
        @DisplayName("returns user when PIN matches an active account")
        void successfulBarcodeLogin() {
            when(userRepository.findByBarcodePinAndActiveTrue("ABCDEF123456"))
                    .thenReturn(Optional.of(activeUser));

            Optional<User> result = authService.loginWithBarcode("ABCDEF123456");

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("cashier");
        }

        @Test
        @DisplayName("returns empty for unknown PIN")
        void unknownPin() {
            when(userRepository.findByBarcodePinAndActiveTrue(anyString()))
                    .thenReturn(Optional.empty());

            assertThat(authService.loginWithBarcode("BADPIN000000")).isEmpty();
        }

        @Test
        @DisplayName("returns empty for locked account even with valid PIN")
        void lockedAccountBarcode() {
            activeUser.setLockedUntil(LocalDateTime.now().plusMinutes(5));
            when(userRepository.findByBarcodePinAndActiveTrue("ABCDEF123456"))
                    .thenReturn(Optional.of(activeUser));

            assertThat(authService.loginWithBarcode("ABCDEF123456")).isEmpty();
        }

        @Test
        @DisplayName("records last-login on successful barcode login")
        void recordsLastLogin() {
            when(userRepository.findByBarcodePinAndActiveTrue("ABCDEF123456"))
                    .thenReturn(Optional.of(activeUser));

            authService.loginWithBarcode("ABCDEF123456");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getLastLoginAt()).isNotNull();
        }
    }
}
