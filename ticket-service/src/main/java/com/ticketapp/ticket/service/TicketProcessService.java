package com.ticketapp.ticket.service;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketProcessService {
    private final ZeebeClient zeebeClient;

    // Priority string mi olcak?
    public void startTicketProcess(String ticketId, String priority) {
        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_TicketManagement")
                .latestVersion()
                .variables(Map.of(
                        "ticketId", ticketId,
                        "priority", priority))
                .send()
                .join();
    }
}
