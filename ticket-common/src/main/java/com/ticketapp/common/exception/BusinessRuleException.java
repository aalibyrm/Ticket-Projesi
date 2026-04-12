package com.ticketapp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * İş kuralı ihlalleri için → HTTP 400 BAD_REQUEST.
 * <p>
 * Örnekler:
 * - Geçersiz ticket durum geçişi (CLOSED → IN_PROGRESS)
 * - Duplicate kaynak (aynı departman, aynı team adı)
 * <p>
 * Validation hatalarından ({@code @Valid}) ayrıdır:
 * onlar MethodArgumentNotValidException ile yakalanır.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessRuleException extends BaseException {

    public BusinessRuleException(String code, String message) {
        super(code, message);
    }
}
