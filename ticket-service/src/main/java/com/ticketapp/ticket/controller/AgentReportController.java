package com.ticketapp.ticket.controller;

import com.ticketapp.ticket.dto.AgentPerformanceDto;
import com.ticketapp.ticket.service.AgentReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class AgentReportController {
    private final AgentReportService agentReportService;

    @GetMapping("/agent/{agentId}")
    public AgentPerformanceDto getAgentPerformance(@PathVariable String agentId) {
        return agentReportService.getAgentPerformance(agentId);
    }
}
