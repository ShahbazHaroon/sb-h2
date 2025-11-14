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
        User entity = modelMapper.map(request, User.class);
        User response = repository.save(entity);
        return modelMapper.map(response, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        log.info("UserService -> findAll() called");
        List<User> response = repository.findAll();
        if (response.isEmpty()) {
            throw new ResourceNotFoundException("Nothing found in the database");
        }
        List<UserResponseDTO> repositoryResponse = MapperUtil.mapAll(response, UserResponseDTO.class);
        return repositoryResponse;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        log.info("UserService -> findById() called");
        if (id == null) {
            throw new MissingInputException("ID must not be null");
        }
        User response = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database"));
        return modelMapper.map(response, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO request) {
        log.info("UserService -> update() called");
        if (id == null || request == null) {
            throw new MissingInputException("ID and update info must not be null");
        }
        User response = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database"));
        response.setUserName(request.getUserName());
        response.setEmail(request.getEmail());
        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword((request.getPassword()));
        }
        User repositoryResponse = repository.save(response);
        return modelMapper.map(repositoryResponse, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO partialUpdate(Long id, Map<String, Object> updates) {
        log.info("UserService -> partialUpdate() called");
        if (id == null || updates == null || updates.isEmpty()) {
            throw new MissingInputException("ID and update info must not be null or empty");
        }
        User response = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nothing found in the database"));
        updates.forEach((key, value) -> {
            switch (key) {
                case "userName":
                    response.setUserName((String) value);
                    break;
                case "email":
                    response.setEmail((String) value);
                    break;
                case "password":
                    response.setPassword(((String) value));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + key);
            }
        });
        User repositoryResponse = repository.save(response);
        return modelMapper.map(repositoryResponse, UserResponseDTO.class);
    }

    @Transactional
    public void delete(Long id) {
        log.info("UserService -> delete() called");
        if (id == null) {
            throw new MissingInputException("ID must not be null");
        }
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Nothing found in the database");
        }
        repository.deleteById(id);
    }
}
