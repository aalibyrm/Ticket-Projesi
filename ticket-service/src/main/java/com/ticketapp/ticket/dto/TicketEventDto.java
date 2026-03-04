package com.ticketapp.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketEventDto {
	private String ticketId;
    private String status;
    private String userId;
    private String message;
}
