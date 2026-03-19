package com.ticketapp.ticket.service;

import com.ticketapp.ticket.dto.*;
import com.ticketapp.ticket.interfaces.SupportClient;
import com.ticketapp.ticket.interfaces.TicketMapper;
import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.model.TicketStatus;
import com.ticketapp.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketProducer ticketProducer;
    private final CommentService commentService;
    private final TicketMapper ticketMapper;
    private final SupportClient supportClient;
    private final TicketProcessService ticketProcessService;

    public TicketResponseDto createTicket(TicketRequestDto requestDto, String userId) {
        DepartmentResponseDto departmentResponseDto = supportClient.getDepartmentByTopic(requestDto.getTopicId());
        TeamResponseDto teamResponseDto = supportClient.assignTeam(departmentResponseDto.getId());
        Ticket ticket = ticketMapper.ticketDto(requestDto);

        ticket.setDepartmentId(departmentResponseDto.getId());
        ticket.setTeamId(teamResponseDto.getId());
        ticket.setUserId(userId);
        //ticket.setStatus(TicketStatus.NEW);

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketEventDto event = new TicketEventDto(
                savedTicket.getId(),
                savedTicket.getStatus().toString(),
                savedTicket.getUserId(),
                "Müşteri yeni bir ticket oluşturdu.");
        ticketProducer.sendMessage(event);

        ticketProcessService.startTicketProcess(savedTicket.getId(),
                savedTicket.getPriority().name());

        return ticketMapper.toTicketResponseDto(savedTicket);
    }

    public List<TicketResponseDto> getAllTickets(String userId, String role) {
        // Müşteri istek attıysa kendi biletlerini görür
        if (role.contains("CUSTOMER") && !role.contains("AGENT") && !role.contains("TEAM_LEADER")) {
            List<Ticket> ticketList = ticketRepository.findByUserId(userId);

            return ticketMapper.toTicketResponseDtoList(ticketList);
        }
        List<Ticket> ticketList = ticketRepository.findAll();
        // Şimdilik destek ekibi tüm biletleri görebilir.
        return ticketMapper.toTicketResponseDtoList(ticketList);
    }

    public void deleteTicket(String id) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        TicketEventDto event = new TicketEventDto(
                ticket.getId(),
                ticket.getStatus().toString(),
                ticket.getUserId(),
                "Ticket başarıyla silindi.");
        ticketProducer.sendMessage(event);

        ticketRepository.deleteById(id);
    }

    public TicketResponseDto assignTicket(String ticketId, String agentId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        ticket.setAssigneeId(agentId);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        Ticket savedTicket = ticketRepository.save(ticket);

        TicketEventDto event = new TicketEventDto(
                savedTicket.getId(),
                savedTicket.getStatus().toString(),
                savedTicket.getUserId(),
                "Ticket bir Agent'a atandı.");
        ticketProducer.sendMessage(event);

        return ticketMapper.toTicketResponseDto(savedTicket);
    }

    public TicketResponseDto sendToApproval(String id, String agentId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        if (ticket.getAssigneeId() == null || !ticket.getAssigneeId().equals(agentId))
            throw new RuntimeException(
                    "Bu ticket size atanmamış! Sadece size atanan ticketları onaya gönderebilirsiniz.");

        if (!TicketStatus.IN_PROGRESS.equals(ticket.getStatus()))
            throw new RuntimeException("Sadece 'In Progress' durumundaki ticketlar onaya gönderilebilir.");

        ticket.setStatus(TicketStatus.RESOLVED);
        Ticket savedTicket = ticketRepository.save(ticket);

        TicketEventDto event = new TicketEventDto(
                savedTicket.getId(),
                savedTicket.getStatus().toString(),
                savedTicket.getUserId(),
                "Ticket müşteri onayına gönderildi.");
        ticketProducer.sendMessage(event);

        return ticketMapper.toTicketResponseDto(savedTicket);
    }

    public TicketResponseDto customerDecision(String ticketId, boolean approved, String userId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        if (!ticket.getUserId().equals(userId))
            throw new RuntimeException("Bu işlem için yetkiniz yok, ticket size ait değil!");

        if (!TicketStatus.RESOLVED.equals(ticket.getStatus())) {
            throw new RuntimeException(
                    "Bu ticket şu an onay bekliyor durumunda değil. Mevcut durum: " + ticket.getStatus());
        }

        Ticket savedTicket;

        if (approved) {
            ticket.setStatus(TicketStatus.CLOSED);
            savedTicket = ticketRepository.save(ticket);

            TicketEventDto event = new TicketEventDto(
                    savedTicket.getId(),
                    savedTicket.getStatus().toString(),
                    savedTicket.getUserId(),
                    "Müşteri çözümü onayladı. Ticket kapatıldı.");
            ticketProducer.sendMessage(event);
        } else {

            ticket.setStatus(TicketStatus.IN_PROGRESS);
            savedTicket = ticketRepository.save(ticket);

            TicketEventDto event = new TicketEventDto(
                    savedTicket.getId(),
                    savedTicket.getStatus().toString(),
                    savedTicket.getUserId(),
                    "Müşteri çözümü reddetti. Ticket Agent'a geri gönderildi.");
            ticketProducer.sendMessage(event);
        }

        return ticketMapper.toTicketResponseDto(savedTicket);
    }

    public TicketDetailDto ticketDetails(String ticketId, String userId, String role) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        if (role.contains("CUSTOMER") && !ticket.getUserId().equals(userId)) {
            throw new RuntimeException("Bu ticket detaylarını görmeye yetkiniz yok!");
        }

        List<CommentResponseDto> commentList = commentService.getCommentsByTicketId(ticketId, role);

        return ticketMapper.toTicketDetailDto(ticket, commentList);
    }
}
