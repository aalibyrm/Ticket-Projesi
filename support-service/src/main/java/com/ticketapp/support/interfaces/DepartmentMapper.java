package com.ticketapp.support.interfaces;

import com.ticketapp.support.dto.DepartmentRequestDto;
import com.ticketapp.support.dto.DepartmentResponseDto;
import com.ticketapp.support.model.Department;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    Department departmentDto(DepartmentRequestDto departmentRequestDto);
    DepartmentResponseDto departmentResponseDto(Department department);

    List<DepartmentResponseDto> toResponseDtoList (List<Department> departmentList);

}
