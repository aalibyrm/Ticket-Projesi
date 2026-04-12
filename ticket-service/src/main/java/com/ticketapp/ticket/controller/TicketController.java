package com.ticketapp.ticket.controller;

import java.util.List;

import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.dto.TicketDetailDto;
import com.ticketapp.ticket.dto.TicketRequestDto;
import com.ticketapp.ticket.dto.TicketResponseDto;
import com.ticketapp.ticket.service.CommentService;
import com.ticketapp.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Ticket Yönetimi", description = "Ticket oluşturma, atama, onay ve durum işlemleri")
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    @Operation(summary = "Ticket oluştur", description = "Yeni bir destek talebi oluşturur. Sadece CUSTOMER rolü erişebilir.")
    @PostMapping("create-ticket")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TicketResponseDto createTicket(@Valid @RequestBody TicketRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {

        return ticketService.createTicket(requestDto, jwt.getSubject());
    }

    @Operation(summary = "Tüm ticketları listele", description = "CUSTOMER kendi ticketlarını, AGENT/TEAM_LEADER tüm ticketları görür.")
    @GetMapping
    public List<TicketResponseDto> getAllTickets(@AuthenticationPrincipal Jwt jwt) {
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();

        return ticketService.getAllTickets(jwt.getSubject(), role);
    }

    @Operation(summary = "Ticket sil", description = "Belirtilen ticketı siler. Sadece TEAM_LEADER rolü erişebilir.")
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public void deleteTicket(@PathVariable String ticketId) {

        ticketService.deleteTicket(ticketId);
    }

    @Operation(summary = "Ticket ata", description = "Ticketı mevcut kullanıcıya (agent) atar. AGENT veya TEAM_LEADER rolü gereklidir.")
    @PatchMapping("/{ticketId}/assign")
    @PreAuthorize("hasAnyRole('AGENT','TEAM_LEADER')")
    public TicketResponseDto assignTicket(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {

        return ticketService.assignTicket(ticketId, jwt.getSubject());
    }

    @Operation(summary = "Müşteri onayına gönder", description = "Tamamlanan ticketı müşteri onayı için gönderir.")
    @PatchMapping("/{ticketId}/send-to-approval")
    @PreAuthorize("hasAnyRole('AGENT','TEAM_LEADER')")
    public TicketResponseDto sendToApproval(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {

        return ticketService.sendToApproval(ticketId, jwt.getSubject());
    }

    @Operation(summary = "Müşteri kararı", description = "Müşteri ticketı onaylar (approved=true) veya reddeder (approved=false).")
    @PatchMapping("/{ticketId}/customer-decision")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TicketResponseDto customerDecision(
            @PathVariable String ticketId,
            @RequestParam boolean approved,
            @AuthenticationPrincipal Jwt jwt) {


        return ticketService.customerDecision(ticketId, approved, jwt.getSubject());
    }

    @Operation(summary = "Ticket detayı", description = "Ticket bilgileri ve tüm yorumlarını döner.")
    @GetMapping("/{ticketId}/details")
    public TicketDetailDto ticketDetails(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();

        TicketResponseDto ticket = ticketService.getTicketById(ticketId, jwt.getSubject(), role);
        List<CommentResponseDto> comments = commentService.getCommentsByTicketId(ticketId, role);
        return new TicketDetailDto(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedDate(),
                ticket.getUserId(),
                comments
        );
    }
}