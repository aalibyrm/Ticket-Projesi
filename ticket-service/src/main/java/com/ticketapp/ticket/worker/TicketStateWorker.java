package com.ticketapp.ticket.worker;

import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.model.TicketStatus;
import com.ticketapp.ticket.repository.TicketRepository;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TicketStateWorker {
    private final TicketRepository ticketRepository;
    private final ZeebeClient zeebeClient;

    private final List<JobWorker> openWorkers = new ArrayList<>();

    @PostConstruct
    public void registerWorkers() {
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setNew").handler(this::handleSetNew).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setInProgress").handler(this::handleSetInProgress)
                .open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setPendingCustomerInput")
                .handler(this::handleSetPending).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setWaitingForCustomer")
                .handler(this::handleSetWaiting).open());
        openWorkers.add(
                zeebeClient.newWorker().jobType("ticket.state.setResolved").handler(this::handleSetResolved).open());
        openWorkers
                .add(zeebeClient.newWorker().jobType("ticket.state.setClosed").handler(this::handleSetClosed).open());
    }

    @PreDestroy
    public void closeWorkers() {
        openWorkers.forEach(JobWorker::close);
    }

    public void handleSetNew(JobClient jobClient, final ActivatedJob activatedJob) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");
        String priority = (String) variables.get("priority");
        String slaRemainingDuration;
        String slaWarningDuration;

        if ("HIGH".equals(priority)) {
            slaRemainingDuration = "PT4H";
            slaWarningDuration = "PT3H";
        } else if ("MEDIUM".equals(priority)) {
            slaRemainingDuration = "PT8H";
            slaWarningDuration = "PT6H";
        } else {
            slaRemainingDuration = "PT24H";
            slaWarningDuration = "PT20H";
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        String olaStartTime = LocalDateTime.now().toString();
        ticket.setStatus(TicketStatus.NEW);
        ticketRepository.save(ticket);

        Map<String, Object> result = new HashMap<>();
        result.put("ticketId", ticketId);
        result.put("olaStartTime", olaStartTime);
        result.put("slaRemainingDuration", slaRemainingDuration);
        result.put("slaWarningDuration", slaWarningDuration);
        result.put("candidateGroups", "agents,teamLeaders");

        jobClient.newCompleteCommand(activatedJob)
                .variables(result)
                .send().join();
    }

    public void handleSetInProgress(JobClient jobClient, final ActivatedJob activatedJob) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");
        Object raw = variables.get("slaPaused");
        boolean slaPaused = raw != null && (Boolean) raw;
        String slaRemainingDuration = (String) variables.get("slaRemainingDuration");
        String slaWarningDuration = (String) variables.get("slaWarningDuration");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        if (slaRemainingDuration == null || slaWarningDuration == null) {
            String priority = ticket.getPriority() != null ? ticket.getPriority().name() : "LOW";
            if ("HIGH".equals(priority)) {
                slaRemainingDuration = slaRemainingDuration != null ? slaRemainingDuration : "PT4H";
                slaWarningDuration = slaWarningDuration != null ? slaWarningDuration : "PT3H";
            } else if ("MEDIUM".equals(priority)) {
                slaRemainingDuration = slaRemainingDuration != null ? slaRemainingDuration : "PT8H";
                slaWarningDuration = slaWarningDuration != null ? slaWarningDuration : "PT6H";
            } else {
                slaRemainingDuration = slaRemainingDuration != null ? slaRemainingDuration : "PT24H";
                slaWarningDuration = slaWarningDuration != null ? slaWarningDuration : "PT20H";
            }
        }

        long pausedMs = 0;
        if (slaPaused && ticket.getSlaPausedAt() != null) {
            pausedMs = Duration.between(ticket.getSlaPausedAt(), LocalDateTime.now()).toMillis();
            Duration remaining = Duration.parse(slaRemainingDuration).minusMillis(pausedMs);
            if (remaining.isNegative() || remaining.isZero()) {
                remaining = Duration.ofSeconds(1);
            }
            slaRemainingDuration = remaining.toString();

            Duration warning = Duration.parse(slaWarningDuration).minusMillis(pausedMs);
            if (warning.isNegative() || warning.isZero()) {
                warning = Duration.ofSeconds(1);
            }
            slaWarningDuration = warning.toString();
        }

        long currentPaused = ticket.getSlaTotalPausedMillis() != null
                ? ticket.getSlaTotalPausedMillis()
                : 0L;
        ticket.setSlaTotalPausedMillis(currentPaused + pausedMs);

        ticket.setSlaPausedAt(null);

        if (ticket.getSlaStartedAt() == null) {
            ticket.setSlaStartedAt(LocalDateTime.now());
        }

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        Map<String, Object> result = new HashMap<>();
        result.put("slaRemainingDuration", slaRemainingDuration);
        result.put("slaWarningDuration", slaWarningDuration);
        result.put("slaPaused", false);

        jobClient.newCompleteCommand(activatedJob)
                .variables(result)
                .send().join();
    }

    public void handleSetPending(JobClient jobClient, final ActivatedJob activatedJob) {
        handleSlaPause(jobClient, activatedJob, TicketStatus.PENDING_CUSTOMER_INPUT);
    }

    public void handleSetWaiting(JobClient jobClient, final ActivatedJob activatedJob) {
        handleSlaPause(jobClient, activatedJob, TicketStatus.WAITING_FOR_CUSTOMER);
    }

    public void handleSetResolved(JobClient jobClient, final ActivatedJob activatedJob) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));
        LocalDateTime now = LocalDateTime.now();

        ticket.setResolvedAt(now);

        ticket.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(ticket);

        jobClient.newCompleteCommand(activatedJob).send().join();
    }

    public void handleSetClosed(JobClient jobClient, final ActivatedJob activatedJob) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));
        LocalDateTime now = LocalDateTime.now();

        ticket.setClosedAt(now);
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);

        jobClient.newCompleteCommand(activatedJob).send().join();
    }

    private void handleSlaPause(JobClient jobClient, final ActivatedJob activatedJob, TicketStatus status) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));
        LocalDateTime now = LocalDateTime.now();
        ticket.setSlaPausedAt(now);

        ticket.setStatus(status);
        ticketRepository.save(ticket);

        jobClient.newCompleteCommand(activatedJob)
                .variable("slaPausedAt", now.toString())
                .variable("slaPaused", true)
                .send().join();
    }
}
