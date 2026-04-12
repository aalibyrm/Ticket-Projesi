package com.ticketapp.support.service;

import com.ticketapp.support.dto.DepartmentRequestDto;
import com.ticketapp.support.dto.DepartmentResponseDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.dto.TopicRequestDto;
import com.ticketapp.support.dto.TopicResponseDto;
import com.ticketapp.support.exception.DepartmentNotFoundException;
import com.ticketapp.support.exception.DuplicateResourceException;
import com.ticketapp.support.exception.TopicNotFoundException;
import com.ticketapp.support.interfaces.DepartmentMapper;
import com.ticketapp.support.interfaces.TeamMapper;
import com.ticketapp.support.interfaces.TopicMapper;
import com.ticketapp.support.model.Department;
import com.ticketapp.support.model.Team;
import com.ticketapp.support.model.Topic;
import com.ticketapp.support.repository.DepartmentRepository;
import com.ticketapp.support.repository.TeamRepository;
import com.ticketapp.support.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * DepartmentService birim testleri.
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock DepartmentRepository departmentRepository;
    @Mock TopicRepository topicRepository;
    @Mock DepartmentMapper departmentMapper;
    @Mock TopicMapper topicMapper;
    @Mock TeamRepository teamRepository;
    @Mock TeamMapper teamMapper;

    @InjectMocks
    DepartmentService departmentService;

    // ── findDepartmentByTopic ─────────────────────────────────────────────────

    @Test
    void findDepartmentByTopic_whenTopicExists_shouldReturnDepartmentDto() {
        Department dept = new Department(1L, "IT", List.of(), List.of());
        Topic topic = new Topic(5L, "network", "Network Sorunları", dept);
        DepartmentResponseDto expected = new DepartmentResponseDto(1L, "IT");

        given(topicRepository.findById(5L)).willReturn(Optional.of(topic));
        given(departmentMapper.departmentResponseDto(dept)).willReturn(expected);

        DepartmentResponseDto result = departmentService.findDepartmentByTopic(5L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("IT");
    }

    @Test
    void findDepartmentByTopic_whenTopicNotFound_shouldThrowTopicNotFoundException() {
        given(topicRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.findDepartmentByTopic(99L))
                .isInstanceOf(TopicNotFoundException.class);
    }

    // ── createDepartment ──────────────────────────────────────────────────────

    @Test
    void createDepartment_whenNameAlreadyExists_shouldThrowDuplicateResource() {
        DepartmentRequestDto request = new DepartmentRequestDto("IT");

        given(departmentRepository.existsByName("IT")).willReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createDepartment_whenValid_shouldSaveAndReturnDto() {
        DepartmentRequestDto request = new DepartmentRequestDto("Finance");
        Department dept = new Department(null, "Finance", List.of(), List.of());
        Department saved = new Department(2L, "Finance", List.of(), List.of());
        DepartmentResponseDto expected = new DepartmentResponseDto(2L, "Finance");

        given(departmentRepository.existsByName("Finance")).willReturn(false);
        given(departmentMapper.departmentDto(request)).willReturn(dept);
        given(departmentRepository.save(dept)).willReturn(saved);
        given(departmentMapper.departmentResponseDto(saved)).willReturn(expected);

        DepartmentResponseDto result = departmentService.createDepartment(request);

        assertThat(result.getId()).isEqualTo(2L);
        verify(departmentRepository).save(dept);
    }

    // ── addTopicToDepartment ──────────────────────────────────────────────────

    @Test
    void addTopicToDepartment_whenDepartmentNotFound_shouldThrowDepartmentNotFoundException() {
        given(departmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                departmentService.addTopicToDepartment(99L, new TopicRequestDto("network", "Network")))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    @Test
    void addTopicToDepartment_whenTopicNameAlreadyExists_shouldThrowDuplicateResource() {
        Topic existingTopic = new Topic(1L, "network", "Network", null);
        Department dept = new Department(1L, "IT", List.of(existingTopic), List.of());

        given(departmentRepository.findById(1L)).willReturn(Optional.of(dept));

        // Aynı isimli topic tekrar eklenemez
        assertThatThrownBy(() ->
                departmentService.addTopicToDepartment(1L, new TopicRequestDto("network", "Network")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void addTopicToDepartment_whenValid_shouldSaveTopicAndReturnDto() {
        Department dept = new Department(1L, "IT", new ArrayList<>(), List.of());
        TopicRequestDto request = new TopicRequestDto("printer", "Yazıcı Sorunları");
        Topic topic = new Topic(null, "printer", "Yazıcı Sorunları", dept);
        Topic savedTopic = new Topic(10L, "printer", "Yazıcı Sorunları", dept);
        TopicResponseDto expected = new TopicResponseDto(10L, "printer", "Yazıcı Sorunları");

        given(departmentRepository.findById(1L)).willReturn(Optional.of(dept));
        given(topicMapper.topicDto(request)).willReturn(topic);
        given(topicRepository.save(topic)).willReturn(savedTopic);
        given(topicMapper.topicResponseDto(savedTopic)).willReturn(expected);

        TopicResponseDto result = departmentService.addTopicToDepartment(1L, request);

        assertThat(result.getId()).isEqualTo(10L);
        verify(topicRepository).save(topic);
    }

    // ── listAllDepartments ────────────────────────────────────────────────────

    @Test
    void listAllDepartments_shouldReturnAllDepartments() {
        List<Department> departments = List.of(
                new Department(1L, "IT", List.of(), List.of()),
                new Department(2L, "HR", List.of(), List.of())
        );
        List<DepartmentResponseDto> expected = List.of(
                new DepartmentResponseDto(1L, "IT"),
                new DepartmentResponseDto(2L, "HR")
        );

        given(departmentRepository.findAll()).willReturn(departments);
        given(departmentMapper.toResponseDtoList(departments)).willReturn(expected);

        List<DepartmentResponseDto> result = departmentService.listAllDepartments();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("IT");
    }

    // ── getTeamsByDepartment ──────────────────────────────────────────────────

    @Test
    void getTeamsByDepartment_shouldReturnTeamsForDepartment() {
        Department dept = new Department(1L, "IT", List.of(), List.of());
        Team team = new Team(5L, "Alpha", dept, null, List.of());
        TeamResponseDto teamDto = new TeamResponseDto(5L, "Alpha", "IT", null);

        given(teamRepository.findTeamsByDepartmentId(1L)).willReturn(List.of(team));
        given(teamMapper.toResponseDto(List.of(team))).willReturn(List.of(teamDto));

        List<TeamResponseDto> result = departmentService.getTeamsByDepartment(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alpha");
    }
}
