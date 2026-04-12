package com.ticketapp.notification.client;

import com.ticketapp.common.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserClient icin Circuit Breaker fallback.
 * user-service erisilemedığinde bildirim anonim kullanici bilgisiyle islenir.
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDto getUserById(String id) {
        log.warn("[CIRCUIT BREAKER] user-service erisilemedi, bildirim ham veriyle isleniyor: userId={}", id);
        return new UserDto(id, "unknown", null, "Bilinmiyor", "Bilinmiyor");
    }
}
