package com.ticketapp.support.interfaces;

import com.ticketapp.support.dto.TeamRequestDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    Team teamDto (TeamRequestDto teamRequestDto);

    @Mapping(source = "department.name", target = "departmentName")
    TeamResponseDto teamResponseDto(Team team);

    List<TeamResponseDto> toResponseDto (List<Team> teamList);
}
