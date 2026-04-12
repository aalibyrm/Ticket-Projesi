package com.ticketapp.support.exception;

import com.ticketapp.common.exception.BusinessRuleException;

/**
 * Zaten var olan bir kaynağı tekrar oluşturmaya çalışıldığında fırlatılır.
 * Örnekler: aynı adla department/team oluşturma, ekipte zaten olan kullanıcı ekleme.
 * GlobalExceptionHandler (common) → HTTP 400 döner.
 */
public class DuplicateResourceException extends BusinessRuleException {

    public DuplicateResourceException(String code, String message) {
        super(code, message);
    }
}
