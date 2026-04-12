package com.ticketapp.user.service;

import com.ticketapp.common.dto.UserDto;
import com.ticketapp.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public UserDto getUserById(String id) {
        try {
            var userResource = keycloak.realm(realm).users().get(id).toRepresentation();
            log.debug("Kullanici bulundu: id={}, username={}", id, userResource.getUsername());
            return new UserDto(
                    userResource.getId(),
                    userResource.getUsername(),
                    userResource.getEmail(),
                    userResource.getFirstName(),
                    userResource.getLastName()
            );
        } catch (Exception e) {
            log.warn("Kullanici bulunamadi: id={}", id);
            throw new UserNotFoundException(id);
        }
    }
}
