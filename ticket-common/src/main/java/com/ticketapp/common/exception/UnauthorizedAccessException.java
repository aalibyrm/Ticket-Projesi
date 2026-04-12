package com.ticketapp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Yetkisiz erişim denemeleri için → HTTP 403 FORBIDDEN.
 * <p>
 * Spring Security'nin AccessDeniedException'ından ayrı tutuldu:
 * bu exception domain katmanından (Service) fırlatılır,
 * AccessDeniedException ise güvenlik filtre zincirinden gelir.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends BaseException {

    public UnauthorizedAccessException(String message) {
        super("UNAUTHORIZED_ACCESS", message);
    }

    public UnauthorizedAccessException(String code, String message) {
        super(code, message);
    }
}
