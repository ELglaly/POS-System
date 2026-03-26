package org.example.cashier.ui.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DateUtil")
class DateUtilTest {

    @Test
    @DisplayName("formats LocalDate as dd/MM/yyyy")
    void formatsLocalDate() {
        LocalDate date = LocalDate.of(2025, 3, 5);
        assertThat(DateUtil.format(date)).isEqualTo("05/03/2025");
    }

    @Test
    @DisplayName("formats single-digit day and month with leading zero")
    void leadingZeroes() {
        LocalDate date = LocalDate.of(2025, 1, 7);
        assertThat(DateUtil.format(date)).isEqualTo("07/01/2025");
    }

    @Test
    @DisplayName("returns empty string for null LocalDate")
    void nullDate() {
        assertThat(DateUtil.format((LocalDate) null)).isEmpty();
    }

    @Test
    @DisplayName("formats LocalDateTime as dd/MM/yyyy HH:mm")
    void formatsLocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2025, 12, 31, 23, 59);
        assertThat(DateUtil.format(dt)).isEqualTo("31/12/2025 23:59");
    }

    @Test
    @DisplayName("returns empty string for null LocalDateTime")
    void nullDateTime() {
        assertThat(DateUtil.format((LocalDateTime) null)).isEmpty();
    }

    @Test
    @DisplayName("parseDate round-trips through format")
    void parseRoundTrip() {
        LocalDate original = LocalDate.of(2026, 6, 15);
        String formatted = DateUtil.format(original);
        assertThat(DateUtil.parseDate(formatted)).isEqualTo(original);
    }
}
