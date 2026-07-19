package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.config.JwtAuthenticationFilter;
import com.gustcustodio.e_commerce_api.dtos.UserRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.UserResponseDTO;
import com.gustcustodio.e_commerce_api.services.UserService;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Long existingId, nonExistingId;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;
    private PageImpl<UserResponseDTO> page;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        userResponseDTO = new UserResponseDTO(1L, "Valid User", "391.385.040-65", "991233344", "user@email.com");
        userRequestDTO = new UserRequestDTO("Updated User Name", "441.766.180-43", "987654321", "updated@email.com", "newPassword123");
        page = new PageImpl<>(List.of(userResponseDTO));

        when(userService.findLoggedUser()).thenReturn(userResponseDTO);
        when(userService.findUserById(existingId)).thenReturn(userResponseDTO);
        when(userService.findUserById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
        when(userService.findAllUsers(0, 5)).thenReturn(page);
        when(userService.updateLoggedUser(any(UserRequestDTO.class))).thenReturn(userResponseDTO);
        when(userService.updateUserById(eq(existingId), any(UserRequestDTO.class))).thenReturn(userResponseDTO);
        when(userService.updateUserById(eq(nonExistingId), any(UserRequestDTO.class))).thenThrow(ResourceNotFoundException.class);
        doNothing().when(userService).deleteLoggedUser();
        doNothing().when(userService).deleteUserById(existingId);
        doThrow(ResourceNotFoundException.class).when(userService).deleteUserById(nonExistingId);
    }

    @Test
    public void findLoggedUserShouldReturnUserResponseDTO() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/me").accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(userResponseDTO.name())))
                .andExpect(jsonPath("$.email", is(userResponseDTO.email())));
    }

    @Test
    public void findUserByIdShouldReturnUserResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/{id}", existingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(userResponseDTO.name())))
                .andExpect(jsonPath("$.cpf", is(userResponseDTO.cpf())))
                .andExpect(jsonPath("$.phone", is(userResponseDTO.phone())))
                .andExpect(jsonPath("$.email", is(userResponseDTO.email())));
    }

    @Test
    public void findUserByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllUsersShouldReturnPage() throws Exception {
        ResultActions result = mockMvc.perform(get("/users")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(page.getContent().size())))
                .andExpect(jsonPath("$.content[0].id").value(existingId))
                .andExpect(jsonPath("$.content[0].name", is(userResponseDTO.name())));
    }

    @Test
    public void updateLoggedUserShouldReturnUserResponseDTO() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/me")
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(userResponseDTO.name())));
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidUserRequestDTO")
    public void updateLoggedUserShouldReturnUnprocessableEntityWhenUserRequestDTOHasInvalidData(
            String name,
            String cpf,
            String phone,
            String email,
            String password
    ) throws Exception {
        UserRequestDTO invalidDTO = new UserRequestDTO(name, cpf, phone, email, password);
        ResultActions result = mockMvc.perform(put("/users/me")
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateUserByIdShouldReturnUserResponseDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/{id}", existingId)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(userResponseDTO.name())));
    }

    @Test
    public void updateUserByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/{id}", nonExistingId)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidUserRequestDTO")
    public void updateUserByIdShouldReturnUnprocessableEntityWhenUserRequestDTOHasInvalidData(
            String name,
            String cpf,
            String phone,
            String email,
            String password
    ) throws Exception {
        UserRequestDTO invalidDTO = new UserRequestDTO(name, cpf, phone, email, password);
        ResultActions result = mockMvc.perform(put("/users/{id}", existingId)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteUserByIdShouldReturnNoContentWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{id}", existingId));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteUserByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{id}", nonExistingId));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteLoggedUserShouldReturnNoContent() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/me"));
        result.andExpect(status().isNoContent());
    }

    static Stream<Arguments> providerOfInvalidUserRequestDTO() {
        return Stream.of(
                // Invalid name - too short
                Arguments.of("Bob", "391.385.040-65", "987654321", "updated@email.com", "newPassword123"),
                // Invalid name - too long (51+ chars)
                Arguments.of("A".repeat(51), "391.385.040-65", "987654321", "updated@email.com", "newPassword123"),
                // Invalid name - null
                Arguments.of(null, "391.385.040-65", "987654321", "updated@email.com", "newPassword123"),
                // Invalid name - empty
                Arguments.of("", "391.385.040-65", "987654321", "updated@email.com", "newPassword123"),
                // Invalid CPF - null
                Arguments.of("Updated User Name", null, "987654321", "updated@email.com", "newPassword123"),
                // Invalid CPF - empty
                Arguments.of("Updated User Name", "", "987654321", "updated@email.com", "newPassword123"),
                // Invalid CPF - invalid format
                Arguments.of("Updated User Name", "123.456.789-10", "987654321", "updated@email.com", "newPassword123"),
                // Invalid phone - not 9 digits
                Arguments.of("Updated User Name", "391.385.040-65", "98765432", "updated@email.com", "newPassword123"),
                // Invalid phone - null
                Arguments.of("Updated User Name", "391.385.040-65", null, "updated@email.com", "newPassword123"),
                // Invalid phone - empty
                Arguments.of("Updated User Name", "391.385.040-65", "", "updated@email.com", "newPassword123"),
                // Invalid password - too short (< 8 chars)
                Arguments.of("Updated User Name", "391.385.040-65", "987654321", "updated@email.com", "short"),
                // Invalid password - null
                Arguments.of("Updated User Name", "391.385.040-65", "987654321", "updated@email.com", null),
                // Invalid password - empty
                Arguments.of("Updated User Name", "391.385.040-65", "987654321", "updated@email.com", ""),
                // Invalid email - null
                Arguments.of("Updated User Name", "391.385.040-65", "987654321", null, "newPassword123"),
                // Invalid email - empty
                Arguments.of("Updated User Name", "391.385.040-65", "987654321", "", "newPassword123"),
                // Invalid email - invalid format
                Arguments.of("Updated User Name", "391.385.040-65", "987654321", "email-invalido.com", "newPassword123")
        );
    }

}
