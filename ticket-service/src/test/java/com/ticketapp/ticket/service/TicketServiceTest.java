package com.ticketapp.ticket.service;

import com.ticketapp.common.dto.TicketEventDto;
import com.ticketapp.common.exception.UnauthorizedAccessException;
import com.ticketapp.ticket.client.SupportClient;
import com.ticketapp.ticket.dto.DepartmentResponseDto;
import com.ticketapp.ticket.dto.TeamResponseDto;
import com.ticketapp.ticket.dto.TicketRequestDto;
import com.ticketapp.ticket.dto.TicketResponseDto;
import com.ticketapp.ticket.exception.InvalidTicketStateException;
import com.ticketapp.ticket.exception.TicketNotFoundException;
import com.ticketapp.ticket.mapper.TicketMapper;
import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.model.TicketPriority;
import com.ticketapp.ticket.model.TicketStatus;
import com.ticketapp.ticket.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * TicketService birim testleri.
 *
 * Öğrenilen kavramlar:
 *  - @ExtendWith(MockitoExtension.class): Spring bağlamı olmadan Mockito kullanımı
 *  - @Mock: bağımlılığı sahte nesneyle değiştirme
 *  - @InjectMocks: mock'ların enjekte edileceği gerçek nesne
 *  - given(...).willReturn(...): davranış tanımlama (BDD stili)
 *  - verify(...): beklenen çağrının gerçekleştiğini kontrol etme
 *  - assertThatThrownBy(...): exception fırlatma testi
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock TicketRepository ticketRepository;
    @Mock TicketProducer ticketProducer;
    @Mock TicketMapper ticketMapper;
    @Mock SupportClient supportClient;
    @Mock TicketProcessService ticketProcessService;
    @Mock CamundaMessageService camundaMessageService;

    @InjectMocks
    TicketService ticketService;

    // ── createTicket ──────────────────────────────────────────────────────────

    @Test
    void createTicket_shouldSaveTicketAndSendKafkaEvent() {
        // given – girdi hazırla
        TicketRequestDto request = new TicketRequestDto(
                "Uygulama açılmıyor", "500 hatası", 1L,
                null, TicketPriority.HIGH, null, null);
        String userId = "user-123";

        DepartmentResponseDto dept = new DepartmentResponseDto(10L, "IT");
        TeamResponseDto team = new TeamResponseDto(20L, "Alpha", "IT", null);

        Ticket ticket = new Ticket();
        ticket.setId("ticket-uuid");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setUserId(userId);

        TicketResponseDto expected = new TicketResponseDto(
                "ticket-uuid", "Uygulama açılmıyor", "500 hatası",
                TicketStatus.NEW, TicketPriority.HIGH, null, userId, null);

        given(supportClient.getDepartmentByTopic(1L)).willReturn(dept);
        given(supportClient.assignTeam(10L)).willReturn(team);
        given(ticketMapper.ticketDto(request)).willReturn(ticket);
        given(ticketRepository.save(ticket)).willReturn(ticket);
        given(ticketMapper.toTicketResponseDto(ticket)).willReturn(expected);

        // when
        TicketResponseDto result = ticketService.createTicket(request, userId);

        // then
        assertThat(result.getId()).isEqualTo("ticket-uuid");
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
        verify(ticketRepository).save(ticket);
        verify(ticketProcessService).startTicketProcess("ticket-uuid", "HIGH");
        verify(ticketProducer).sendMessage(any(TicketEventDto.class));
    }

    // ── getAllTickets ─────────────────────────────────────────────────────────

    @Test
    void getAllTickets_whenCustomer_shouldReturnOnlyOwnTickets() {
        // Müşteri rolü → sadece kendi ticketları
        String userId = "user-123";
        String role = "[CUSTOMER]";   // Keycloak JWT'den gelen format

        Ticket ticket = new Ticket();
        ticket.setUserId(userId);

        given(ticketRepository.findByUserId(userId)).willReturn(List.of(ticket));
        given(ticketMapper.toTicketResponseDtoList(List.of(ticket)))
                .willReturn(List.of(new TicketResponseDto()));

        List<TicketResponseDto> result = ticketService.getAllTickets(userId, role);

        assertThat(result).hasSize(1);
        verify(ticketRepository).findByUserId(userId);
        verify(ticketRepository, never()).findAll();
    }

    @Test
    void getAllTickets_whenAgent_shouldReturnAllTickets() {
        // Destek ekibi → tüm ticketlar
        String userId = "agent-123";
        String role = "[AGENT]";

        given(ticketRepository.findAll()).willReturn(List.of(new Ticket(), new Ticket()));
        given(ticketMapper.toTicketResponseDtoList(any()))
                .willReturn(List.of(new TicketResponseDto(), new TicketResponseDto()));

        List<TicketResponseDto> result = ticketService.getAllTickets(userId, role);

        assertThat(result).hasSize(2);
        verify(ticketRepository).findAll();
        verify(ticketRepository, never()).findByUserId(any());
    }

    // ── deleteTicket ──────────────────────────────────────────────────────────

    @Test
    void deleteTicket_whenTicketExists_shouldDeleteAndPublishEvent() {
        String ticketId = "ticket-id";
        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setUserId("user-123");

        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(ticket));

        ticketService.deleteTicket(ticketId);

        verify(ticketProducer).sendMessage(any(TicketEventDto.class));
        verify(ticketRepository).deleteById(ticketId);
    }

    @Test
    void deleteTicket_whenTicketNotFound_shouldThrowTicketNotFoundException() {
        given(ticketRepository.findById("ghost-id")).willReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.deleteTicket("ghost-id"))
                .isInstanceOf(TicketNotFoundException.class);

        verify(ticketRepository, never()).deleteById(any());
    }

    // ── assignTicket ──────────────────────────────────────────────────────────

    @Test
    void assignTicket_shouldSetAssigneeAndSendEvent() {
        String ticketId = "ticket-id";
        String agentId = "agent-123";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setUserId("customer-123");
        ticket.setTeamId(5L);

        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(ticket));
        given(ticketRepository.save(ticket)).willReturn(ticket);
        given(ticketMapper.toTicketResponseDto(ticket)).willReturn(new TicketResponseDto());

        ticketService.assignTicket(ticketId, agentId);

        assertThat(ticket.getAssigneeId()).isEqualTo(agentId);
        verify(ticketProducer).sendMessage(any(TicketEventDto.class));
        verify(camundaMessageService).sendTicketAssigned(ticketId, agentId, 5L);
    }

    // ── sendToApproval ────────────────────────────────────────────────────────

    @Test
    void sendToApproval_whenNotAssignedToRequestingAgent_shouldThrowUnauthorized() {
        Ticket ticket = new Ticket();
        ticket.setId("ticket-id");
        ticket.setAssigneeId("other-agent");
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.sendToApproval("ticket-id", "my-agent-id"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void sendToApproval_whenStatusNotInProgress_shouldThrowInvalidState() {
        String agentId = "agent-123";
        Ticket ticket = new Ticket();
        ticket.setId("ticket-id");
        ticket.setAssigneeId(agentId);
        ticket.setStatus(TicketStatus.NEW);   // IN_PROGRESS değil

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.sendToApproval("ticket-id", agentId))
                .isInstanceOf(InvalidTicketStateException.class);
    }

    @Test
    void sendToApproval_whenValid_shouldSendZeebeMessageAndKafkaEvent() {
        String agentId = "agent-123";
        Ticket ticket = new Ticket();
        ticket.setId("ticket-id");
        ticket.setAssigneeId(agentId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUserId("customer-1");

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));
        given(ticketMapper.toTicketResponseDto(ticket)).willReturn(new TicketResponseDto());

        ticketService.sendToApproval("ticket-id", agentId);

        verify(camundaMessageService).sendAgentSendSolution("ticket-id");
        verify(ticketProducer).sendMessage(any(TicketEventDto.class));
    }

    // ── customerDecision ──────────────────────────────────────────────────────

    @Test
    void customerDecision_whenApproved_shouldCallApproveMessage() {
        String ticketId = "ticket-id";
        String userId = "user-123";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setUserId(userId);
        ticket.setStatus(TicketStatus.WAITING_FOR_CUSTOMER);

        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(ticket));
        given(ticketMapper.toTicketResponseDto(ticket)).willReturn(new TicketResponseDto());

        ticketService.customerDecision(ticketId, true, userId);

        verify(camundaMessageService).sendCustomerApproved(ticketId);
        verify(ticketProducer).sendMessage(any(TicketEventDto.class));
    }

    @Test
    void customerDecision_whenRejected_shouldCallRejectMessage() {
        String ticketId = "ticket-id";
        String userId = "user-123";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setUserId(userId);
        ticket.setStatus(TicketStatus.WAITING_FOR_CUSTOMER);

        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(ticket));
        given(ticketMapper.toTicketResponseDto(ticket)).willReturn(new TicketResponseDto());

        ticketService.customerDecision(ticketId, false, userId);

        verify(camundaMessageService).sendCustomerRejected(ticketId);
    }

    @Test
    void customerDecision_whenUserIsNotOwner_shouldThrowUnauthorized() {
        Ticket ticket = new Ticket();
        ticket.setId("ticket-id");
        ticket.setUserId("real-owner");
        ticket.setStatus(TicketStatus.WAITING_FOR_CUSTOMER);

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.customerDecision("ticket-id", true, "wrong-user"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void customerDecision_whenStatusIsNotWaitingForCustomer_shouldThrowInvalidState() {
        String userId = "user-123";
        Ticket ticket = new Ticket();
        ticket.setId("ticket-id");
        ticket.setUserId(userId);
        ticket.setStatus(TicketStatus.NEW);   // WAITING_FOR_CUSTOMER değil

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.customerDecision("ticket-id", true, userId))
                .isInstanceOf(InvalidTicketStateException.class);
    }

    // ── getTicketById ─────────────────────────────────────────────────────────

    @Test
    void getTicketById_whenCustomerTriesToAccessOthersTicket_shouldThrowUnauthorized() {
        Ticket ticket = new Ticket();
        ticket.setId("ticket-id");
        ticket.setUserId("another-user");

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                ticketService.getTicketById("ticket-id", "my-user-id", "[CUSTOMER]"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void getTicketById_whenCustomerAccessesOwnTicket_shouldReturnDto() {
        String userId = "user-123";
        Ticket ticket = new Ticket();
        ticket.setId("ticket-id");
        ticket.setUserId(userId);

        TicketResponseDto expected = new TicketResponseDto();
        expected.setId("ticket-id");

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));
        given(ticketMapper.toTicketResponseDto(ticket)).willReturn(expected);

        TicketResponseDto result = ticketService.getTicketById("ticket-id", userId, "[CUSTOMER]");

        assertThat(result.getId()).isEqualTo("ticket-id");
    }
}
