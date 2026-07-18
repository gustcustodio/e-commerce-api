package com.gustcustodio.e_commerce_api.controllers;

import com.gustcustodio.e_commerce_api.dtos.UserRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.UserResponseDTO;
import com.gustcustodio.e_commerce_api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/profile")
    public ResponseEntity<UserResponseDTO> findLoggedUser() {
        UserResponseDTO userResponseDTO = userService.findLoggedUser();
        return ResponseEntity.ok(userResponseDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
        UserResponseDTO dto = userService.findUserById(id);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> findAllUsers(@RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                              @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        Page<UserResponseDTO> page = userService.findAllUsers(pageNumber, pageSize);
        return ResponseEntity.ok(page);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/profile")
    public ResponseEntity<UserResponseDTO> updateLoggedUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = userService.updateLoggedUser(userRequestDTO);
        return ResponseEntity.ok(userResponseDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<UserResponseDTO> updateUserById(@PathVariable Long id, @Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = userService.updateUserById(id, userRequestDTO);
        return ResponseEntity.ok(userResponseDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping(value = "/profile")
    public ResponseEntity<Void> deleteLoggedUser() {
        userService.deleteLoggedUser();
        return ResponseEntity.noContent().build();
    }

}
