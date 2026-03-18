package com.mediverse.controller;

import com.mediverse.model.Department;
import com.mediverse.repository.DepartmentRepository;
import com.mediverse.security.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    DepartmentRepository departmentRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDepartment(@RequestBody Department request) {
        Department dept = new Department();
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());

        departmentRepository.save(dept);

        return ResponseEntity.ok(new MessageResponse("Department added successfully!"));
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }
}
