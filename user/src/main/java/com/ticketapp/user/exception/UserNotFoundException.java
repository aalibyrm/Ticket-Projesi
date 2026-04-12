package com.ticketapp.user.exception;

import com.ticketapp.common.exception.ResourceNotFoundException;

/**
 * Belirtilen ID'ye sahip kullanıcı Keycloak üzerinde bulunamadığında fırlatılır.
 * GlobalExceptionHandler (common) → HTTP 404 döner.
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String userId) {
        super("User", userId);
    }
}
