package com.ticketapp.ticket.service;

import com.ticketapp.ticket.util.SlaCalculator;
import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketProcessService {
    private final ZeebeClient zeebeClient;

    public void startTicketProcess(String ticketId, String priority) {
        String slaRemainingDuration = SlaCalculator.getSlaDuration(priority);
        String slaWarningDuration = SlaCalculator.getSlaWarningDuration(priority);

        Map<String, Object> variables = new HashMap<>();
        variables.put("ticketId", ticketId);
        variables.put("priority", priority);
        variables.put("slaPaused", false);
        variables.put("slaPausedAt", null);
        variables.put("slaRemainingDuration", slaRemainingDuration);
        variables.put("slaWarningDuration", slaWarningDuration);
        variables.put("candidateGroups", "agents,teamLeaders");

        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_TicketManagement")
                .latestVersion()
                .variables(variables)
                .send()
                .join();
    }
}
