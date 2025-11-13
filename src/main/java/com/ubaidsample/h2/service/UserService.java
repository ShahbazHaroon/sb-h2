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
            throw new MissingInputException("User Info must not be null");
        }
        User user = modelMapper.map(request, User.class);
        User savedUser = repository.save(user);
        return modelMapper.map(savedUser, UserResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        log.info("UserService -> findAll() called");
        List<User> users = repository.findAll();
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No users found in the database");
        }
        List<UserResponseDTO> response = MapperUtil.mapAll(users, UserResponseDTO.class);
        return response;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        log.info("UserService -> findById() called");
        if (id == null) {
            throw new MissingInputException("User ID must not be null");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return modelMapper.map(user, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserRequestDTO request) {
        log.info("UserService -> update() called");
        if (id == null || request == null) {
            throw new MissingInputException("User ID and update info must not be null");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null) {
            user.setPassword((request.getPassword()));
        }
        User updatedUser = repository.save(user);
        return modelMapper.map(updatedUser, UserResponseDTO.class);
    }

    @Transactional
    public UserResponseDTO partialUpdate(Long id, Map<String, Object> updates) {
        log.info("UserService -> partialUpdate() called");
        if (id == null || updates == null || updates.isEmpty()) {
            throw new MissingInputException("User ID and update info must not be null or empty");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        updates.forEach((key, value) -> {
            switch (key) {
                case "userName":
                    user.setUserName((String) value);
                    break;
                case "email":
                    user.setEmail((String) value);
                    break;
                case "password":
                    user.setPassword(((String) value));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid field: " + key);
            }
        });
        User updatedUser = repository.save(user);
        return modelMapper.map(updatedUser, UserResponseDTO.class);
    }

    @Transactional
    public void delete(Long id) {
        log.info("UserService -> delete() called");
        if (id == null) {
            throw new MissingInputException("User ID must not be null");
        }
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        repository.deleteById(id);
    }
}
