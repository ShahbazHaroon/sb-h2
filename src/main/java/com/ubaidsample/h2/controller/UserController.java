/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.h2.controller;

import com.ubaidsample.h2.dto.request.UserRequestDTO;
import com.ubaidsample.h2.dto.response.UserResponseDTO;
import com.ubaidsample.h2.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @Operation(summary = "Create new user")
    @ApiResponse(responseCode = "201", description = "User created")
    @PostMapping
    public ResponseEntity<UserResponseDTO> save(@Valid @RequestBody UserRequestDTO request) {
        log.info("UserController -> save() called");
        var response = service.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all users", description = "Retrieves all user profiles")
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        log.info("UserController -> findAll() called");
        var response = service.findAll();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a user profile")
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable(value = "id") Long id) {
        log.info("UserController -> findById() called with ID: {}", id);
        var response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user by ID", description = "Updates a user profile")
    @ApiResponse(responseCode = "200", description = "User updated")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UserRequestDTO request) {
        log.info("UserController -> update() called with ID: {}", id);
        var response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user by ID", description = "Updates a user profile")
    @ApiResponse(responseCode = "200", description = "User updated")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> partialUpdate(@PathVariable Long id, @Valid @RequestBody Map<String, Object> updates) {
        log.info("UserController -> partialUpdate() called with ID: {}", id);
        var response = service.partialUpdate(id, updates);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user by ID", description = "Deletes a user profile")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable(value = "id") Long id) {
        log.info("UserController -> deleteById() called with ID: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}