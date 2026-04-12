package com.ticketapp.common.exception;

/**
 * Tüm domain exception'larının atasıdır.
 * <p>
 * {@code code} alanı frontend / logging tarafında hata tipi tespiti için kullanılır
 * (örn: "TICKET_NOT_FOUND", "DUPLICATE_TEAM").
 * {@link GlobalExceptionHandler} bu alanı {@link ErrorResponse}'a aktarır.
 */
public abstract class BaseException extends RuntimeException {

    private final String code;

    protected BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
