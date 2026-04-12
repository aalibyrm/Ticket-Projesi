package com.ticketapp.ticket.config;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Slf4j
@Configuration
public class CamundaConfig {

    @Value("${zeebe.client.broker.gateway-address}")
    private String gatewayAddress;

    @Value("${zeebe.client.security.plaintext}")
    private boolean plaintextSecurity;

    @Bean
    public ZeebeClient zeebeClient() {
        return ZeebeClient.newClientBuilder()
                .grpcAddress(URI.create("http://"+gatewayAddress))
                .usePlaintext()
                .build();
    }

    @Bean
    public CommandLineRunner deployBpmn(ZeebeClient zeebeClient){
        return args -> {
            zeebeClient.newDeployResourceCommand()
                    .addResourceFromClasspath("ticket-management.bpmn")
                    .send().join();

            log.info("[CAMUNDA] BPMN deployed: ticket-management.bpmn");
        };
    }
}
