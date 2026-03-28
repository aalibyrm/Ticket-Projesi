package com.ticketapp.ticket.dto;

public record AgentPerformanceDto(
        String agentId,
        long totalTickets,
        long resolvedTickets,
        long closedTickets,
        long slaViolations,
        double avgResolutionTimeHours,
        double slaComplianceRate
) {}

