package com.ticketapp.ticket.service;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CamundaMessageService {
    private final ZeebeClient zeebeClient;

    public void sendTicketAssigned(String ticketId, String assignedAgent, Long assignedTeam) {
        zeebeClient.newPublishMessageCommand()
                .messageName("ticketAssigned")
                .correlationKey(ticketId)
                .variables(Map.of(
                        "assignedAgent", assignedAgent,
                        "assignedTeam", List.of(String.valueOf(assignedTeam))))
                .send()
                .join();
    }

    public void sendCustomerInfoProvided(String ticketId) {
        zeebeClient.newPublishMessageCommand()
                .messageName("customerInfoProvided")
                .correlationKey(ticketId)
                .send()
                .join();
    }

    public void sendCustomerApproved(String ticketId) {
        zeebeClient.newPublishMessageCommand()
                .messageName("customerApproved")
                .correlationKey(ticketId)
                .send()
                .join();
    }

    public void sendCustomerRejected(String ticketId) {
        zeebeClient.newPublishMessageCommand()
                .messageName("customerRejected")
                .correlationKey(ticketId)
                .send()
                .join();
    }

    public void sendCustomerExplicitClose(String ticketId){
        zeebeClient.newPublishMessageCommand()
                .messageName("customerExplicitClose")
                .correlationKey(ticketId)
                .send()
                .join();
    }

    public void sendCustomerRejectAtResolved(String ticketId) {
        zeebeClient.newPublishMessageCommand()
                .messageName("customerRejectAtResolved")
                .correlationKey(ticketId)
                .send()
                .join();
    }

    public void sendAgentSendSolution(String ticketId){
        zeebeClient.newPublishMessageCommand()
                .messageName("agentSendSolution")
                .correlationKey(ticketId)
                .send()
                .join();
    }
}
