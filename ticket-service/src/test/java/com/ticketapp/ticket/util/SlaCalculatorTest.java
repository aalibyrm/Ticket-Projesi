package com.ticketapp.ticket.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SlaCalculator saf unit testi: Spring bağlamı, mock, DB gerektirmez.
 * Tüm öncelik dallarını parametrik olarak kapsar.
 */
class SlaCalculatorTest {

    // --- getSlaDuration ---

    @ParameterizedTest(name = "getSlaDuration({0}) → {1}")
    @CsvSource({
            "HIGH,   PT4H",
            "MEDIUM, PT8H",
            "LOW,    PT24H",
            "OTHER,  PT24H"   // bilinmeyen öncelik → varsayılan
    })
    void getSlaDuration_shouldReturnCorrectDuration(String priority, String expected) {
        assertThat(SlaCalculator.getSlaDuration(priority.trim()))
                .isEqualTo(expected.trim());
    }

    // --- getSlaWarningDuration ---

    @ParameterizedTest(name = "getSlaWarningDuration({0}) → {1}")
    @CsvSource({
            "HIGH,   PT2H",
            "MEDIUM, PT4H",
            "LOW,    PT12H",
            "OTHER,  PT12H"   // bilinmeyen öncelik → varsayılan
    })
    void getSlaWarningDuration_shouldReturnCorrectDuration(String priority, String expected) {
        assertThat(SlaCalculator.getSlaWarningDuration(priority.trim()))
                .isEqualTo(expected.trim());
    }
}
