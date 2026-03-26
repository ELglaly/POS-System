package org.example.cashier.services.impl;

import org.example.cashier.core.entity.User;
import org.example.cashier.core.enums.UserRole;
import org.example.cashier.data.repository.UserRepository;
import org.example.cashier.reports.barcode.ZXingBarcodeEngine;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserManagementServiceImpl")
class UserManagementServiceImplTest {

    @Mock UserRepository     userRepository;
    @Mock PasswordEncoder    passwordEncoder;
    @Mock ZXingBarcodeEngine barcodeEngine;

    @InjectMocks UserManagementServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(5L).username("alice").fullName("Alice Smith")
                .passwordHash("$hashed$").role(UserRole.CASHIER).active(true).build();

        when(passwordEncoder.encode(anyString())).thenAnswer(i -> "hashed:" + i.getArgument(0));
    }

    // ── createUser ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("creates and saves a new user with hashed password")
        void createsUser() {
            when(userRepository.existsByUsernameIgnoreCase("bob")).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            User created = userService.createUser("bob", "Bob Jones", "pass1234", UserRole.CASHIER);

            assertThat(created.getUsername()).isEqualTo("bob");
            assertThat(created.getFullName()).isEqualTo("Bob Jones");
            assertThat(created.getRole()).isEqualTo(UserRole.CASHIER);
            assertThat(created.getPasswordHash()).isEqualTo("hashed:pass1234");
            assertThat(created.isActive()).isTrue();
        }

        @Test
        @DisplayName("throws when username is blank")
        void blankUsername() {
            assertThatThrownBy(() ->
                    userService.createUser("  ", "Name", "password", UserRole.CASHIER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username is required");
        }

        @Test
        @DisplayName("throws when password is shorter than 4 characters")
        void shortPassword() {
            assertThatThrownBy(() ->
                    userService.createUser("bob", "Bob", "abc", UserRole.CASHIER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("4 characters");
        }

        @Test
        @DisplayName("throws when username is already taken")
        void duplicateUsername() {
            when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(true);

            assertThatThrownBy(() ->
                    userService.createUser("alice", "Alice", "password", UserRole.CASHIER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already taken");
        }

        @Test
        @DisplayName("defaults role to CASHIER when null is passed")
        void defaultRoleCashier() {
            when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            User created = userService.createUser("bob", null, "pass1234", null);

            assertThat(created.getRole()).isEqualTo(UserRole.CASHIER);
        }
    }

    // ── updateUser ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("updates username, full name, and role")
        void updatesFields() {
            when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByUsernameIgnoreCase("alice")).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            User updated = userService.updateUser(5L, "alice", "Alice Updated", null, UserRole.MANAGER);

            assertThat(updated.getUsername()).isEqualTo("alice");
            assertThat(updated.getFullName()).isEqualTo("Alice Updated");
            assertThat(updated.getRole()).isEqualTo(UserRole.MANAGER);
        }

        @Test
        @DisplayName("leaves password unchanged when rawPassword is blank")
        void keepsPasswordWhenBlank() {
            when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            userService.updateUser(5L, "alice", "Alice", "", UserRole.CASHIER);

            verify(passwordEncoder, never()).encode(anyString());
            assertThat(existingUser.getPasswordHash()).isEqualTo("$hashed$");
        }

        @Test
        @DisplayName("rehashes password when a new rawPassword is provided")
        void updatesPassword() {
            when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            userService.updateUser(5L, "alice", "Alice", "newpass99", UserRole.CASHIER);

            verify(passwordEncoder).encode("newpass99");
        }

        @Test
        @DisplayName("throws when user not found")
        void userNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.updateUser(99L, "x", "y", null, UserRole.CASHIER))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    // ── deleteUser ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser soft-deletes by setting active = false")
    void softDelete() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(5L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    @DisplayName("deleteUser is a no-op for an unknown id")
    void deleteNonExistentIsNoOp() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        userService.deleteUser(999L);

        verify(userRepository, never()).save(any());
    }

    // ── getUserBarcodeLabel ───────────────────────────────────────────────

    @Test
    @DisplayName("uses fullName as display name when available")
    void barcodeLabelUsesFullName() {
        existingUser.setBarcodePin("ABCDEF123456");
        when(barcodeEngine.generateUserBarcodeLabel(eq("ABCDEF123456"), eq("Alice Smith")))
                .thenReturn(new byte[]{1, 2, 3});

        byte[] result = userService.getUserBarcodeLabel(existingUser);

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("falls back to username when fullName is null")
    void barcodeLabelFallsBackToUsername() {
        existingUser.setFullName(null);
        existingUser.setBarcodePin("ABCDEF123456");
        when(barcodeEngine.generateUserBarcodeLabel(eq("ABCDEF123456"), eq("alice")))
                .thenReturn(new byte[]{9, 8, 7});

        byte[] result = userService.getUserBarcodeLabel(existingUser);

        assertThat(result).containsExactly(9, 8, 7);
    }
}
