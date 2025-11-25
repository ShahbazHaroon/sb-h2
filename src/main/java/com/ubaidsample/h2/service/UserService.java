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
import com.ubaidsample.h2.exception.MissingInputException;
import com.ubaidsample.h2.exception.ResourceAlreadyExistsException;
import com.ubaidsample.h2.exception.ResourceNotFoundException;
import com.ubaidsample.h2.repository.UserRepository;
import com.ubaidsample.h2.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        if (id == null) {
            throw new MissingInputException("ID must not be null");
        }
        // Fetch existing
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        // Convert the entity to the DTO
        return modelMapper.map(entity, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO request) {
        log.info("UserService -> update() called");
        if (id == null || request == null) {
            throw new MissingInputException("ID and update info must not be null");
        }
        // Fetch existing
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        // Update fields
        entity.setUserName(request.getUserName());
        entity.setEmail(request.getEmail());
        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword((request.getPassword()));
        }
        entity.setDateOfBirth(request.getDateOfBirth());
        entity.setDateOfLeaving(request.getDateOfLeaving());
        entity.setPostalCode(request.getPostalCode());
        // Save updated data
        User repositoryResponse = repository.save(entity);
        // Convert the entity to the DTO
        return modelMapper.map(repositoryResponse, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO partialUpdate(Long id, UserPartialUpdateRequestDTO updates) {
        log.info("UserService -> partialUpdate() called");
        if (id == null || updates == null) {
            throw new MissingInputException("ID and update info must not be null");
        }
        // Fetch existing
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        // Apply updates only if they are present
        Optional.ofNullable(updates.getUserName()).ifPresent(entity::setUserName);
        Optional.ofNullable(updates.getEmail()).ifPresent(entity::setEmail);
        Optional.ofNullable(updates.getPassword()).ifPresent(entity::setPassword);
        Optional.ofNullable(updates.getDateOfBirth()).ifPresent(entity::setDateOfBirth);
        Optional.ofNullable(updates.getDateOfLeaving()).ifPresent(entity::setDateOfLeaving);
        Optional.ofNullable(updates.getPostalCode()).ifPresent(entity::setPostalCode);
        // Save updated data
        User repositoryResponse = repository.save(entity);
        // Convert the entity to the DTO
        return modelMapper.map(repositoryResponse, UserResponseDTO.class);
    }

    @Transactional
    public void softDeleteById(Long userId) {
        log.info("UserService -> softDeleteById() called");
        // Fetch existing
        repository.findById(userId).ifPresent(user -> {
            user.getAuditHistoryDTO().setDeleted(true);
            user.getAuditHistoryDTO().setDeletedDate(LocalDateTime.now());
            repository.save(user);
        });
    }

    @Transactional
    public void restoreSoftDeleteById(Long userId) {
        log.info("UserService -> restoreSoftDeleteById() called");
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
        if (id == null) {
            throw new MissingInputException("ID must not be null");
        }
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Nothing found in the database with id " + id);
        }
        repository.deleteById(id);
    }
}