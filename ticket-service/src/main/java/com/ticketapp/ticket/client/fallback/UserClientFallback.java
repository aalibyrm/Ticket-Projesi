package com.ticketapp.ticket.client.fallback;

import com.ticketapp.common.dto.UserDto;
import com.ticketapp.ticket.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserClient icin Circuit Breaker fallback.
 * user-service erisilemez durumda olduğunda varsayilan bir UserDto doner.
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDto getUserById(String id) {
        log.warn("[CIRCUIT BREAKER] user-service erisilemedigi icin fallback devreye girdi: userId={}", id);
        return new UserDto(id, "unknown", null, "Bilinmiyor", "Bilinmiyor");
    }
}
