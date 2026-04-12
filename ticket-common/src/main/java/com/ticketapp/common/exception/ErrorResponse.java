package com.ticketapp.common.exception;

import java.time.LocalDateTime;

/**
 * Tüm servislerden frontend'e dönen standart hata DTO'su.
 * <p>
 * Format:
 * <pre>
 * {
 *   "code":      "TICKET_NOT_FOUND",
 *   "message":   "Ticket bulunamadi: abc-123",
 *   "timestamp": "2026-04-12T01:00:00"
 * }
 * </pre>
 * Record kullanımı: immutable, auto-generated equals/hashCode/toString,
 * Jackson ile kutudan çıkar şekilde serialize olur.
 */
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, LocalDateTime.now());
    }
}
