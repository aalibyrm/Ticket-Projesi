package com.ticketapp.ticket.controller;

import java.util.List;

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
		String userId = jwt.getSubject();

		return ticketService.createTicket(ticket, userId);
	}

	@GetMapping
	public List<Ticket> getAllTickets(@AuthenticationPrincipal Jwt jwt) {

		String userId = jwt.getSubject();
		var role = jwt.getClaimAsMap("realm_access").get("roles").toString();

		return ticketService.getAllTickets(userId, role);
	}

	// Ticket Silme Methodu
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('TEAM_LEADER')")
	public void deleteTicket(@PathVariable String id) {

		ticketService.deleteTicket(id);
	}

	// Ticket Atama Methodu
	@PatchMapping("/{id}/assign")
	@PreAuthorize("hasAnyRole('AGENT','TEAM_LEADER')")
	public Ticket assignTicket(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
		String agentId = jwt.getSubject();

		return ticketService.assignTicket(id, agentId);
	}

	// Ticketı Müşteri Onayına Gönderme Methodu
	@PatchMapping("/{id}/send-to-approval")
	@PreAuthorize("hasAnyRole('AGENT','TEAM_LEADER')")
	public Ticket sendToApproval(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {

		return ticketService.sendToApproval(id, jwt.getSubject());
	}

	// Müşterinin Ticketı Onaylama Methodu
	@PatchMapping("/{id}/customer-decision")
	@PreAuthorize("hasRole('CUSTOMER')")
	public Ticket customerDecision(
			@PathVariable String id,
			@RequestParam boolean approved,
			@AuthenticationPrincipal Jwt jwt) {

		String userId = jwt.getSubject();

		return ticketService.customerDecision(id, approved, userId);
	}

}