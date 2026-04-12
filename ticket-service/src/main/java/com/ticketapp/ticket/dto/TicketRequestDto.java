package com.ticketapp.ticket.dto;

import com.ticketapp.ticket.model.TicketPriority;
import com.ticketapp.ticket.model.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Yeni ticket oluşturma isteği")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestDto {
    @Schema(description = "Ticket başlığı", example = "Uygulama açılmıyor")
    @NotBlank
    private String title;

    @Schema(description = "Sorunun ayrıntılı açıklaması", example = "Login ekranında 500 hatası alıyorum")
    @NotBlank
    private String description;

    @Schema(description = "Konunun bağlı olduğu topic ID", example = "1")
    @NotNull
    private Long topicId;

    @Schema(description = "Ticket durumu (genellikle boş bırakılır, sistem atar)")
    private TicketStatus status;

    @Schema(description = "Ticket önceliği", example = "HIGH")
    @NotNull
    private TicketPriority priority;

    @Schema(description = "Oluşturulma tarihi (boş bırakılabilir, sistem atar)")
    private LocalDateTime createdDate;

    @Schema(description = "Kullanıcı ID (JWT'den otomatik alınır, boş bırakılabilir)")
    private String userId;
}
