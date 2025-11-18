/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.h2.service;

import com.ubaidsample.h2.dto.request.UserRequestDTO;
import com.ubaidsample.h2.dto.response.UserResponseDTO;
import com.ubaidsample.h2.entity.User;
import com.ubaidsample.h2.exception.MissingInputException;
import com.ubaidsample.h2.exception.ResourceNotFoundException;
import com.ubaidsample.h2.repository.UserRepository;
import com.ubaidsample.h2.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    private final ModelMapper modelMapper;

    @Transactional
    public UserResponseDTO save(UserRequestDTO request) {
        log.info("UserService -> save() called");
        if (request == null) {
            throw new MissingInputException("Info must not be null");
        }
        // Convert the DTO to the entity
        User entity = modelMapper.map(request, User.class);
        // Save the new user
        User response = repository.save(entity);
        // Convert the entity to the DTO
        return modelMapper.map(response, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        log.info("UserService -> findAll() called");
        List<User> entity = repository.findAll();
        if (entity.isEmpty()) {
            throw new ResourceNotFoundException("Nothing found in the database");
        }
        return entity.stream()
                .map(user -> {
                    // Convert the entity to the DTO
                    UserResponseDTO dto = MapperUtil.map(user, UserResponseDTO.class);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        log.info("UserService -> findById() called");
        if (id == null) {
            throw new MissingInputException("ID must not be null");
        }
        // Convert the entity to the DTO
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
        // Fetch existing user
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
        // Save updated user
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
        // Fetch existing user
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database with id " + id));
        // Apply updates
        updates.getUserName().ifPresent(entity::setUserName);
        updates.getEmail().ifPresent(entity::setEmail);
        updates.getPassword().ifPresent(entity::setPassword);
        updates.getDateOfBirth().ifPresent(entity::setDateOfBirth);
        updates.getDateOfLeaving().ifPresent(entity::setDateOfLeaving);
        updates.getPostalCode().ifPresent(entity::setPostalCode);
        // Save updated user
        User repositoryResponse = repository.save(response);
        // Convert the entity to the DTO
        return modelMapper.map(repositoryResponse, UserResponseDTO.class);
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
