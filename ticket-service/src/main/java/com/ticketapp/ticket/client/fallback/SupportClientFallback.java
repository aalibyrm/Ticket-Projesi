package com.ticketapp.ticket.client.fallback;

import com.ticketapp.common.exception.BusinessRuleException;
import com.ticketapp.ticket.client.SupportClient;
import com.ticketapp.ticket.dto.DepartmentResponseDto;
import com.ticketapp.ticket.dto.TeamResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SupportClient icin Circuit Breaker fallback.
 * support-service erisilemez durumda olduğunda exception firlatar;
 * ticket olusturma kritik bir islem oldugundan sessiz hata kabul edilmez.
 */
@Slf4j
@Component
public class SupportClientFallback implements SupportClient {

    @Override
    public DepartmentResponseDto getDepartmentByTopic(Long topicId) {
        log.error("[CIRCUIT BREAKER] support-service erisilemedigi icin fallback devreye girdi: topicId={}", topicId);
        throw new BusinessRuleException("SUPPORT_SERVICE_UNAVAILABLE",
                "Destek servisi su anda erisilemedigi icin ticket olusturulamadi. Lutfen daha sonra tekrar deneyin.");
    }

    @Override
    public boolean isUserInTeam(Long teamId, String userId) {
        log.warn("[CIRCUIT BREAKER] support-service erisilemedigi icin isUserInTeam fallback: teamId={}, userId={}", teamId, userId);
        return false;
    }

    @Override
    public TeamResponseDto assignTeam(Long departmentId) {
        log.error("[CIRCUIT BREAKER] support-service erisilemedigi icin assignTeam fallback: departmentId={}", departmentId);
        throw new BusinessRuleException("SUPPORT_SERVICE_UNAVAILABLE",
                "Destek servisi su anda erisilemedigi icin ekip atanamadi. Lutfen daha sonra tekrar deneyin.");
    }
}
