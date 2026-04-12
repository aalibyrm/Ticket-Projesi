package com.ticketapp.ticket.dto;

import com.ticketapp.ticket.model.TicketPriority;
import com.ticketapp.ticket.model.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Ticket bilgileri yanıtı")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {
    @Schema(description = "Ticket benzersiz ID", example = "abc123")
    private String id;

    @Schema(description = "Ticket başlığı", example = "Uygulama açılmıyor")
    private String title;

    @Schema(description = "Sorunun açıklaması")
    private String description;

    @Schema(description = "Mevcut durum", example = "OPEN")
    private TicketStatus status;

    @Schema(description = "Öncelik seviyesi", example = "HIGH")
    private TicketPriority priority;

    @Schema(description = "Oluşturulma tarihi")
    private LocalDateTime createdDate;

    @Schema(description = "Talebi oluşturan kullanıcının ID'si")
    private String userId;

    @Schema(description = "Atanan agent'ın ID'si (atanmadıysa null)")
    private String assigneeId;
}
