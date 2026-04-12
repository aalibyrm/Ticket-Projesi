package com.ticketapp.ticket.worker;

import com.ticketapp.ticket.exception.TicketNotFoundException;
import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.model.TicketStatus;
import com.ticketapp.ticket.repository.TicketRepository;
import com.ticketapp.ticket.util.SlaCalculator;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketStateWorker {
    private final TicketRepository ticketRepository;
    private final ZeebeClient zeebeClient;

    private final List<JobWorker> openWorkers = new ArrayList<>();

    @PostConstruct
    public void registerWorkers() {
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setNew").handler(this::handleSetNew).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setInProgress").handler(this::handleSetInProgress).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setPendingCustomerInput").handler(this::handleSetPending).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setWaitingForCustomer").handler(this::handleSetWaiting).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setResolved").handler(this::handleSetResolved).open());
        openWorkers.add(zeebeClient.newWorker().jobType("ticket.state.setClosed").handler(this::handleSetClosed).open());
    }

    @PreDestroy
    public void closeWorkers() {
        openWorkers.forEach(JobWorker::close);
    }

    public void handleSetNew(JobClient jobClient, final ActivatedJob activatedJob) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");
        String priority = (String) variables.get("priority");

        String slaRemainingDuration = SlaCalculator.getSlaDuration(priority);
        String slaWarningDuration = SlaCalculator.getSlaWarningDuration(priority);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        ticket.setStatus(TicketStatus.NEW);
        ticketRepository.save(ticket);

        log.info("[STATE] ticketId={} -> NEW | priority={}, slaRemaining={}, slaWarning={}",
                ticketId, priority, slaRemainingDuration, slaWarningDuration);

        Map<String, Object> result = new HashMap<>();
        result.put("ticketId", ticketId);
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
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (slaRemainingDuration == null || slaWarningDuration == null) {
            String priority = ticket.getPriority() != null ? ticket.getPriority().name() : "LOW";
            if (slaRemainingDuration == null) {
                slaRemainingDuration = SlaCalculator.getSlaDuration(priority);
            }
            if (slaWarningDuration == null) {
                slaWarningDuration = SlaCalculator.getSlaWarningDuration(priority);
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

        log.info("[STATE] ticketId={} -> IN_PROGRESS | slaPaused={}, slaRemaining={}", ticketId, slaPaused, slaRemainingDuration);

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
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        LocalDateTime now = LocalDateTime.now();

        ticket.setResolvedAt(now);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticketRepository.save(ticket);

        log.info("[STATE] ticketId={} -> RESOLVED", ticketId);

        jobClient.newCompleteCommand(activatedJob).send().join();
    }

    public void handleSetClosed(JobClient jobClient, final ActivatedJob activatedJob) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        LocalDateTime now = LocalDateTime.now();

        ticket.setClosedAt(now);
        ticket.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(ticket);

        log.info("[STATE] ticketId={} -> CLOSED", ticketId);

        jobClient.newCompleteCommand(activatedJob).send().join();
    }

    private void handleSlaPause(JobClient jobClient, final ActivatedJob activatedJob, TicketStatus status) {
        Map<String, Object> variables = activatedJob.getVariablesAsMap();
        String ticketId = (String) variables.get("ticketId");

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        LocalDateTime now = LocalDateTime.now();
        ticket.setSlaPausedAt(now);

        ticket.setStatus(status);
        ticketRepository.save(ticket);

        log.info("[STATE] ticketId={} -> {} (SLA durduruldu: {})", ticketId, status, now);

        jobClient.newCompleteCommand(activatedJob)
                .variable("slaPausedAt", now.toString())
                .variable("slaPaused", true)
                .send().join();
    }
}
