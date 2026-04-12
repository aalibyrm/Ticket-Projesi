package com.ticketapp.ticket.controller;

import com.ticketapp.ticket.dto.AgentPerformanceDto;
import com.ticketapp.ticket.service.AgentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Agent Raporları", description = "Agent performans ve istatistik raporları")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class AgentReportController {
    private final AgentReportService agentReportService;

    @Operation(summary = "Agent performans raporu", description = "Belirtilen agent'ın ticket çözme istatistiklerini döner.")
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasAnyRole('AGENT','TEAM_LEADER')")
    public AgentPerformanceDto getAgentPerformance(@PathVariable String agentId) {
        return agentReportService.getAgentPerformance(agentId);
    }
}
