package com.ticketapp.ticket.service;

import com.ticketapp.ticket.dto.AgentPerformanceDto;
import com.ticketapp.ticket.client.UserClient;
import com.ticketapp.ticket.model.TicketStatus;
import com.ticketapp.ticket.repository.SlaViolationRepository;
import com.ticketapp.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentReportService {
    private final TicketRepository ticketRepository;
    private final SlaViolationRepository slaViolationRepository;
    private final UserClient userClient;


    public AgentPerformanceDto getAgentPerformance(String agentId){
        userClient.getUserById(agentId); //Agent kontrolu

        long totalTickets = ticketRepository.countByAssigneeId(agentId);
        long resolvedTickets = ticketRepository.countByAssigneeIdAndStatus(agentId, TicketStatus.RESOLVED);
        long closedTickets = ticketRepository.countByAssigneeIdAndStatus(agentId, TicketStatus.CLOSED);
        Double avg = ticketRepository.findAverageResolutionTimeByAssigneeId(agentId);
        long slaViolations = slaViolationRepository.countByViolatingAgentId(agentId);

        double avgHours = (avg != null) ? avg / 3_600_000.0 : 0.0;

        double slaComplianceRate = (totalTickets == 0) ? 100.0
                : (totalTickets - slaViolations) / (double) totalTickets * 100.0;

        return new AgentPerformanceDto(
                agentId,
                totalTickets,
                resolvedTickets,
                closedTickets,
                slaViolations,
                avgHours,
                slaComplianceRate
        );
    }
}
