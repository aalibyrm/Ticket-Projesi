package com.ticketapp.ticket.exception;

import com.ticketapp.common.exception.ResourceNotFoundException;

/**
 * Belirtilen ID'ye sahip ticket bulunamdığında fırlatılır.
 * GlobalExceptionHandler (common) → HTTP 404 döner.
 */
public class TicketNotFoundException extends ResourceNotFoundException {

    public TicketNotFoundException(String ticketId) {
        super("Ticket", ticketId);
    }
}
