package com.ticketapp.support.interfaces;

import com.ticketapp.support.dto.TeamMemberResDto;
import com.ticketapp.support.model.TeamMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeamMemberMapper {
    @Mapping(source = "team.name",target = "teamName")
    TeamMemberResDto memberDto (TeamMember teamMember);

    List<TeamMemberResDto> toResDto(List<TeamMember> teamMembers);
}
