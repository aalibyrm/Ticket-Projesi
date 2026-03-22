package com.ticketapp.ticket.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SlaWorker {
    private final ZeebeClient zeebeClient;

    private final List<JobWorker> openWorkers = new ArrayList<>();

    @PostConstruct
    public void registerWorkers(){
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.sla.sendWarning").handler(this::handleSlaWarning).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.sla.recordBreach").handler(this::handleSlaBreach).open());
    }

    @PreDestroy
    public void closeWorkers() {
        openWorkers.forEach(JobWorker::close);
    }

    public void handleSlaWarning(JobClient jobClient, final ActivatedJob activatedJob){
        Map<String, Object> variables = activatedJob.getVariablesAsMap();

        String ticketId = (String)variables.get("ticketId");

        System.out.println("[SLA WARNING] ticketId: " + ticketId); //Kafka sonra

        jobClient.newCompleteCommand(activatedJob).send().join();
    }

    public void handleSlaBreach(JobClient jobClient, final ActivatedJob activatedJob){
        Map<String,Object> variables = activatedJob.getVariablesAsMap();

        String ticketId = (String)variables.get("ticketId");

        System.out.println("[SLA BREACH] ticketId: " + ticketId); //Kafka sonra

        jobClient.newCompleteCommand(activatedJob).send().join();
    }
}
