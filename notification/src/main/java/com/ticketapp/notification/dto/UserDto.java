package com.ticketapp.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

	private String id;
	private String userName;
	private String email;
	private String firstName;
	private String lastName;
}
