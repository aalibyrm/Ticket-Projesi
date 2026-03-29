package com.ticketapp.ticket.service;

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
        String slaRemainingDuration;
        String slaWarningDuration;

        switch (priority) {
            case "HIGH":
                slaRemainingDuration = "PT4H";
                slaWarningDuration = "PT3H";
                break;
            case "MEDIUM":
                slaRemainingDuration = "PT8H";
                slaWarningDuration = "PT6H";
                break;
            default:
                slaRemainingDuration = "PT24H";
                slaWarningDuration = "PT20H";
                break;
        }

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
