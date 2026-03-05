package com.ticketapp.ticket.controller;

import java.util.List;

import com.ticketapp.ticket.dto.TicketDetailDto;
import com.ticketapp.ticket.service.TicketService;
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

import com.ticketapp.ticket.model.Ticket;


@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;


    // Ticket Oluşturma Methodu
    @PostMapping("create-ticket")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Ticket createTicket(@RequestBody Ticket ticket, @AuthenticationPrincipal Jwt jwt) {

        return ticketService.createTicket(ticket, jwt.getSubject());
    }

    @GetMapping
    public List<Ticket> getAllTickets(@AuthenticationPrincipal Jwt jwt) {
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();

        return ticketService.getAllTickets(jwt.getSubject(), role);
    }

    // Ticket Silme Methodu
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('TEAM_LEADER')")
    public void deleteTicket(@PathVariable String ticketId) {

        ticketService.deleteTicket(ticketId);
    }

    // Ticket Atama Methodu
    @PatchMapping("/{ticketId}/assign")
    @PreAuthorize("hasAnyRole('AGENT','TEAM_LEADER')")
    public Ticket assignTicket(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {

        return ticketService.assignTicket(ticketId, jwt.getSubject());
    }

    // Ticketı Müşteri Onayına Gönderme Methodu
    @PatchMapping("/{ticketId}/send-to-approval")
    @PreAuthorize("hasAnyRole('AGENT','TEAM_LEADER')")
    public Ticket sendToApproval(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {

        return ticketService.sendToApproval(ticketId, jwt.getSubject());
    }

    // Müşterinin Ticketı Onaylama Methodu
    @PatchMapping("/{ticketId}/customer-decision")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Ticket customerDecision(
            @PathVariable String ticketId,
            @RequestParam boolean approved,
            @AuthenticationPrincipal Jwt jwt) {


        return ticketService.customerDecision(ticketId, approved, jwt.getSubject());
    }

    @GetMapping("/{ticketId}/details")
    public TicketDetailDto ticketDetails(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();

        return ticketService.ticketDetails(ticketId, jwt.getSubject(), role);
    }
}