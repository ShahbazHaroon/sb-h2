/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.h2.service;

import com.ubaidsample.h2.dto.request.PageRequestDTO;
import com.ubaidsample.h2.dto.request.UserPartialUpdateRequestDTO;
import com.ubaidsample.h2.dto.request.UserRequestDTO;
import com.ubaidsample.h2.dto.response.PageResponseDTO;
import com.ubaidsample.h2.dto.response.UserResponseDTO;
import com.ubaidsample.h2.entity.User;
import com.ubaidsample.h2.exception.ResourceAlreadyExistsException;
import com.ubaidsample.h2.exception.ResourceNotFoundException;
import com.ubaidsample.h2.repository.UserRepository;
import com.ubaidsample.h2.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    private final ModelMapper modelMapper;

    @Transactional
    public UserResponseDTO save(UserRequestDTO request) {
        log.info("UserService -> save() called with idempotencyKey={}", request.getIdempotencyKey());
        // Check if this idempotency key was already processed
        return findByIdempotencyKey(request);

    }

    private UserResponseDTO findByIdempotencyKey(UserRequestDTO request) {
        return repository.findByIdempotencyKey(request.getIdempotencyKey())
                .map(user -> {
                    log.info("Returning existing user for idempotencyKey={}", request.getIdempotencyKey());
                    return modelMapper.map(user, UserResponseDTO.class);
                })
                .orElseGet(() -> saveNewUser(request));
    }

    private UserResponseDTO saveNewUser(UserRequestDTO request) {
        // Convert the DTO to the entity
        User entity = modelMapper.map(request, User.class);
        try {
            // Save the new data
            User response = repository.saveAndFlush(entity);
            // Convert the entity to the DTO
            return modelMapper.map(response, UserResponseDTO.class);
        } catch (DataIntegrityViolationException ex) {
            return handleConstraintViolation(request, ex);
        }
    }

    private UserResponseDTO handleConstraintViolation(UserRequestDTO request, DataIntegrityViolationException ex) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        String constraintName = null;
        if (rootCause instanceof org.hibernate.exception.ConstraintViolationException hibernateCve) {
            constraintName = hibernateCve.getConstraintName();

            /* else if (rootCause instanceof java.sql.SQLIntegrityConstraintViolationException sqlCve) {
                // For MySQL: parse SQL message or vendor error code
                constraintName = extractConstraintNameFromMessage(sqlCve.getMessage());

            } else if (rootCause instanceof org.postgresql.util.PSQLException pgEx) {
                // For Postgres: use ServerErrorMessage for constraint
                if (pgEx.getServerErrorMessage() != null) {
                    constraintName = pgEx.getServerErrorMessage().getConstraint();
                }

            } else if (rootCause instanceof oracle.jdbc.OracleDatabaseException oracleEx) {
                // For Oracle: parse error code or message
                constraintName = extractOracleConstraint(oracleEx);
            }*/

            if (constraintName != null) {
                throw ex;
            }
            return switch (constraintName) {
                case "uk_user_email" -> throw new ResourceAlreadyExistsException("User already exists with email: " + request.getEmail());
                case "uk_user_username" -> throw new ResourceAlreadyExistsException("User already exists with username: " + request.getUserName());
                case "uk_user_idempotency_key" -> repository.findByIdempotencyKey(request.getIdempotencyKey())
                        .map(user -> {
                            log.warn("Concurrent idempotent request detected; returning original result for key={}", request.getIdempotencyKey());
                            return modelMapper.map(user, UserResponseDTO.class);
                        })
                        .orElseThrow(() -> ex);
                default -> throw ex;
            };
        }
        throw ex;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        log.info("UserService -> findAll() called");
        // Fetch existing
        List<User> entity = repository.findAll();
        if (entity.isEmpty()) {
            throw new ResourceNotFoundException("Nothing found in the database");
        }
        return entity.stream()
                .map(user -> {
                    // Convert the entity to the DTO
                    return MapperUtil.map(user, UserResponseDTO.class);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        log.info("UserService -> findById() called");
        // Fetch existing
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        // Convert the entity to the DTO
        return modelMapper.map(entity, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO request) {
        log.info("UserService -> update() called");
        // Fetch existing
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        // Update and map all fields except password
        modelMapper.map(request, entity);
        // Update password only if provided
        if (StringUtils.hasText(request.getPassword())) {
            entity.setPassword(request.getPassword());
        }
        // Save the entity and convert it to the DTO
        return modelMapper.map(repository.save(entity), UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO partialUpdate(Long id, UserPartialUpdateRequestDTO updates) {
        log.info("UserService -> partialUpdate() called");
        // Fetch existing
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        // Apply updates only if they are present
        Optional.ofNullable(updates.getUserName()).ifPresent(entity::setUserName);
        Optional.ofNullable(updates.getEmail()).ifPresent(entity::setEmail);
        // Update password only if provided and must not be blank
        Optional.ofNullable(updates.getPassword())
                .filter(StringUtils::hasText)
                .ifPresent(entity::setPassword);
        Optional.ofNullable(updates.getDateOfBirth()).ifPresent(entity::setDateOfBirth);
        Optional.ofNullable(updates.getDateOfLeaving()).ifPresent(entity::setDateOfLeaving);
        Optional.ofNullable(updates.getPostalCode()).ifPresent(entity::setPostalCode);
        // Save the entity and convert it to the DTO
        return modelMapper.map(repository.save(entity), UserResponseDTO.class);
    }

    @Transactional
    public void deactivate(Long userId) {
        log.info("UserService -> deactivate() called");
        // Fetch existing
        repository.findById(userId).ifPresent(user -> {
            user.getAuditHistoryDTO().setDeleted(true);
            user.getAuditHistoryDTO().setDeletedDate(LocalDateTime.now());
            repository.save(user);
        });
    }

    @Transactional
    public void activate(Long userId) {
        log.info("UserService -> activate() called");
        // Fetch existing
        repository.findById(userId).ifPresent(user -> {
            user.getAuditHistoryDTO().setDeleted(false);
            user.getAuditHistoryDTO().setDeletedDate(null);
            repository.save(user);
        });
    }

    public PageResponseDTO<UserResponseDTO> search(PageRequestDTO pageRequest) {
        log.info("UserService -> search() called");
        PaginationService<User, UserResponseDTO> paginationService =
                new PaginationService<>(repository, modelMapper, User.class, UserResponseDTO.class);
        return paginationService.getPaginatedData(pageRequest);
    }

    @Transactional
    public void delete(Long id) {
        log.info("UserService -> delete() called");
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Nothing found in the database with id " + id);
        }
        repository.deleteById(id);
    }
}