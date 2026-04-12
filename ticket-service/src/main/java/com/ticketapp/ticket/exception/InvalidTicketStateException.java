package com.ticketapp.ticket.exception;

import com.ticketapp.common.exception.BusinessRuleException;

/**
 * Geçersiz ticket durum geçişlerinde fırlatılır (örn: CLOSED → IN_PROGRESS).
 * GlobalExceptionHandler (common) → HTTP 400 döner.
 */
public class InvalidTicketStateException extends BusinessRuleException {

    public InvalidTicketStateException(String message) {
        super("INVALID_TICKET_STATE", message);
    }
}
