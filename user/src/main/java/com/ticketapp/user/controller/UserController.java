package com.ticketapp.user.controller;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketapp.user.dto.UserDto;
import com.ticketapp.user.exception.UserNotFoundException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable String id) {
        try {
            var userResource = keycloak.realm(realm).users().get(id).toRepresentation();

            return new UserDto(
                userResource.getId(),
                userResource.getUsername(),
                userResource.getFirstName(),
                userResource.getLastName()
            );
        } catch (Exception e) {
            throw new UserNotFoundException(id);
        }
    }
}