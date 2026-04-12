package com.ticketapp.ticket.dto;

import com.ticketapp.ticket.model.TicketPriority;
import com.ticketapp.ticket.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestDto {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private Long topicId;
    private TicketStatus status;
    @NotNull
    private TicketPriority priority;
    private LocalDateTime createdDate;
    private String userId;
}
