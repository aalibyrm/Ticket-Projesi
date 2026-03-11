package com.ticketapp.support.repository;

import com.ticketapp.support.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,Long> {

    Department findDepartmentByName(String name);
    boolean existsByName(String name);
}
