package com.ticketapp.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kaynak bulunamadığında fırlatılır → HTTP 404.
 * <p>
 * Kullanım: {@code throw new ResourceNotFoundException("Ticket", ticketId)}
 * → code: "TICKET_NOT_FOUND", message: "Ticket bulunamadi: <id>"
 * <p>
 * Servis-spesifik exception'lar (TicketNotFoundException, UserNotFoundException vb.)
 * bu sınıftan türetilir.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
            resourceName.toUpperCase().replace(" ", "_") + "_NOT_FOUND",
            resourceName + " bulunamadi: " + identifier
        );
    }
}
