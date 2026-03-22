package com.ticketapp.ticket.service;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CamundaMessageService {
    private final ZeebeClient zeebeClient;

    public void sendCustomerInfoProvided(String ticketId){
        zeebeClient.newPublishMessageCommand()
                .messageName("customerInfoProvided")
                .correlationKey(ticketId)
                .send()
                .join();
    }

    public void sendCustomerApproved(String ticketId){
        zeebeClient.newPublishMessageCommand()
                .messageName("customerApproved")
                .correlationKey(ticketId)
                .send()
                .join();
    }

    public void sendCustomerRejected(String ticketId){
        zeebeClient.newPublishMessageCommand()
                .messageName("customerRejected")
                .correlationKey(ticketId)
                .send()
                .join();
    }
}
