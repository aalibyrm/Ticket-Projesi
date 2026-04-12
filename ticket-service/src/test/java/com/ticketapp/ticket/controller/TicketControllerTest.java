package com.ticketapp.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.dto.TicketRequestDto;
import com.ticketapp.ticket.dto.TicketResponseDto;
import com.ticketapp.ticket.model.CommentType;
import com.ticketapp.ticket.model.TicketPriority;
import com.ticketapp.ticket.model.TicketStatus;
import com.ticketapp.ticket.service.CommentService;
import com.ticketapp.ticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TicketController dilimi testi (@WebMvcTest).
 *
 * Öğrenilen kavramlar:
 *  - @WebMvcTest: Sadece web katmanını yükler (Service/Repo yüklenmez)
 *  - @MockitoBean: Spring bağlamına Mockito mock kaydeder
 *  - jwt(): Spring Security Test - gerçek Keycloak olmadan JWT simüle eder
 *  - MockMvc: HTTP isteklerini bellekte gönderir, gerçek sunucu gerektirmez
 *
 * Neden JwtDecoder mock'lanıyor?
 *  SecurityConfig, oauth2ResourceServer(jwt) tanımlar; bu bir JwtDecoder bean
 *  gerektirir. Test sırasında Keycloak çalışmadığından mock bean enjekte edilir.
 *  jwt() post processor, token doğrulamayı tamamen atlar ve kimlik bilgisini
 *  doğrudan SecurityContext'e yazar.
 */
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // SecurityConfig'in ihtiyaç duyduğu JwtDecoder bean'ini mock'la
    @MockitoBean JwtDecoder jwtDecoder;

    @MockitoBean TicketService ticketService;
    @MockitoBean CommentService commentService;

    // ── GET /api/v1/tickets ───────────────────────────────────────────────────

    @Test
    void getAllTickets_whenAuthenticated_shouldReturn200() throws Exception {
        TicketResponseDto ticket = new TicketResponseDto(
                "t1", "Başlık", "Açıklama",
                TicketStatus.NEW, TicketPriority.HIGH, null, "user-123", null);

        // JWT claim'ini ayarla: realm_access.roles → CUSTOMER
        given(ticketService.getAllTickets(eq("user-123"), any()))
                .willReturn(List.of(ticket));

        mockMvc.perform(get("/api/v1/tickets")
                        .with(jwt()
                                .jwt(j -> j
                                        .subject("user-123")
                                        .claim("realm_access", Map.of("roles", List.of("CUSTOMER")))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("t1"))
                .andExpect(jsonPath("$[0].status").value("NEW"));
    }

    @Test
    void getAllTickets_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /api/v1/tickets/create-ticket ────────────────────────────────────

    @Test
    void createTicket_whenCustomer_shouldReturn200() throws Exception {
        TicketRequestDto request = new TicketRequestDto(
                "Sunucu çöktü", "500 hatası", 1L,
                null, TicketPriority.HIGH, null, null);

        TicketResponseDto response = new TicketResponseDto(
                "new-id", "Sunucu çöktü", "500 hatası",
                TicketStatus.NEW, TicketPriority.HIGH, null, "user-123", null);

        given(ticketService.createTicket(any(), eq("user-123"))).willReturn(response);

        mockMvc.perform(post("/api/v1/tickets/create-ticket")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()
                                .jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("new-id"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void createTicket_whenRequestBodyInvalid_shouldReturn400() throws Exception {
        // title boş → @NotBlank ihlali
        TicketRequestDto badRequest = new TicketRequestDto(
                "", "açıklama", 1L, null, TicketPriority.HIGH, null, null);

        mockMvc.perform(post("/api/v1/tickets/create-ticket")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(badRequest))
                        .with(jwt()
                                .jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                        ))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/v1/tickets/{ticketId} ─────────────────────────────────────

    @Test
    void deleteTicket_whenTeamLeader_shouldReturn200() throws Exception {
        doNothing().when(ticketService).deleteTicket("ticket-id");

        mockMvc.perform(delete("/api/v1/tickets/ticket-id")
                        .with(jwt()
                                .jwt(j -> j.subject("leader-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_TEAM_LEADER"))
                        ))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/v1/tickets/{ticketId}/assign ───────────────────────────────

    @Test
    void assignTicket_whenAgent_shouldReturn200() throws Exception {
        TicketResponseDto response = new TicketResponseDto();
        response.setId("ticket-id");
        response.setAssigneeId("agent-123");

        given(ticketService.assignTicket(eq("ticket-id"), eq("agent-123"))).willReturn(response);

        mockMvc.perform(patch("/api/v1/tickets/ticket-id/assign")
                        .with(jwt()
                                .jwt(j -> j.subject("agent-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_AGENT"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value("agent-123"));
    }

    // ── PATCH /api/v1/tickets/{ticketId}/send-to-approval ─────────────────────

    @Test
    void sendToApproval_whenAgent_shouldReturn200() throws Exception {
        TicketResponseDto response = new TicketResponseDto();
        response.setId("ticket-id");

        given(ticketService.sendToApproval(eq("ticket-id"), eq("agent-123"))).willReturn(response);

        mockMvc.perform(patch("/api/v1/tickets/ticket-id/send-to-approval")
                        .with(jwt()
                                .jwt(j -> j.subject("agent-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_AGENT"))
                        ))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/v1/tickets/{ticketId}/customer-decision ───────────────────

    @Test
    void customerDecision_whenCustomer_shouldReturn200() throws Exception {
        TicketResponseDto response = new TicketResponseDto();
        response.setId("ticket-id");

        given(ticketService.customerDecision(eq("ticket-id"), eq(true), eq("user-123")))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/tickets/ticket-id/customer-decision")
                        .param("approved", "true")
                        .with(jwt()
                                .jwt(j -> j.subject("user-123"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                        ))
                .andExpect(status().isOk());
    }

    // ── GET /api/v1/tickets/{ticketId}/details ───────────────────────────────

    @Test
    void ticketDetails_shouldReturnTicketWithComments() throws Exception {
        TicketResponseDto ticket = new TicketResponseDto(
                "t1", "Başlık", "Açıklama",
                TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM, null, "user-123", "agent-1");

        CommentResponseDto comment = new CommentResponseDto(
                "c1", "Bir yorum", CommentType.EXTERNAL, null);

        given(ticketService.getTicketById(eq("t1"), eq("user-123"), any()))
                .willReturn(ticket);
        given(commentService.getCommentsByTicketId(eq("t1"), any()))
                .willReturn(List.of(comment));

        mockMvc.perform(get("/api/v1/tickets/t1/details")
                        .with(jwt()
                                .jwt(j -> j
                                        .subject("user-123")
                                        .claim("realm_access", Map.of("roles", List.of("CUSTOMER")))
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("t1"))
                .andExpect(jsonPath("$.comments[0].id").value("c1"));
    }
}
