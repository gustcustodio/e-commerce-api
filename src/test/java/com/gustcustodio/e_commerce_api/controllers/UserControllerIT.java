package com.gustcustodio.e_commerce_api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustcustodio.e_commerce_api.dtos.UserRequestDTO;
import com.gustcustodio.e_commerce_api.entities.User;
import com.gustcustodio.e_commerce_api.repositories.UserRepository;
import com.gustcustodio.e_commerce_api.utils.TokenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private UserRepository userRepository;

    private Long existingId, nonExistingId;
    private UserRequestDTO userRequestDTO;
    private String adminUsername, clientUsername;
    private String adminPassword, clientPassword;
    private String adminToken, clientToken, invalidToken;

    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        userRequestDTO = new UserRequestDTO("Updated User Name", "441.766.180-43", "987654321", "updated@email.com", "newPassword123");
        adminUsername = "dickens@email.com";
        adminPassword = "12345678";
        clientUsername = "tolstoi@email.com";
        clientPassword = "12345678";
        adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
        clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        invalidToken = adminToken + "123";
    }

    @Test
    public void findLoggedUserShouldReturnUserResponseDTOWhenLoggedAsAdmin() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Charles Dickens"))
                .andExpect(jsonPath("$.email").value(adminUsername));
    }

    @Test
    public void findLoggedUserShouldReturnUserResponseDTOWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + clientToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Liev Tolstói"))
                .andExpect(jsonPath("$.email").value(clientUsername));
    }

    @Test
    public void findLoggedUserShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findLoggedUserShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/me")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findUserByIdShouldReturnUserResponseDTOWhenLoggedAsAdminAndIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    public void findUserByIdShouldReturnForbiddenWhenLoggedAsClientAndRequestsAnotherUser() throws Exception {
        Long anotherUserId = 2L;
        ResultActions result = mockMvc.perform(get("/users/{id}", anotherUserId)
                .header("Authorization", "Bearer " + clientToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void findUserByIdShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findUserByIdShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findUserByIdShouldReturnNotFoundWhenLoggedAsAdminAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void findAllUsersShouldReturnPageOfUserResponseDTOWhenLoggedAsAdmin() throws Exception {
        ResultActions result = mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.content[0].email").exists());
    }

    @Test
    public void findAllUsersShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + clientToken)
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void findAllUsersShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + invalidToken)
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void findAllUsersShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(get("/users")
                .param("pageNumber", "0")
                .param("pageSize", "5")
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateLoggedUserShouldReturnUserResponseDTOWhenLoggedAsAdminAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/me")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(userRequestDTO.name())));
    }

    @Test
    public void updateLoggedUserShouldReturnUserResponseDTOWhenLoggedAsClientAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/me")
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(userRequestDTO.name())));
    }

    @Test
    public void updateLoggedUserShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/me")
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateLoggedUserShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/me")
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidUserRequestDTO")
    public void updateLoggedUserShouldReturnUnprocessableEntityWhenLoggedAsAdminAndDataIsInvalid(
            String name,
            String cpf,
            String phone,
            String email,
            String password
    ) throws Exception {
        UserRequestDTO invalidDTO = new UserRequestDTO(name, cpf, phone, email, password);
        ResultActions result = mockMvc.perform(put("/users/me")
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void updateUserByIdShouldReturnUserResponseDTOWhenLoggedAsAdminAndIdExistsAndDataIsValid() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.name", is(userRequestDTO.name())));
    }

    @Test
    public void updateUserByIdShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void updateUserByIdShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateUserByIdShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/{id}", existingId)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void updateUserByIdShouldReturnNotFoundWhenLoggedAsAdminAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(put("/users/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(userRequestDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource(value = "providerOfInvalidUserRequestDTO")
    public void updateUserByIdShouldReturnUnprocessableEntityWhenLoggedAsAdminAndDataIsInvalid(
            String name,
            String cpf,
            String phone,
            String email,
            String password
    ) throws Exception {
        UserRequestDTO invalidDTO = new UserRequestDTO(name, cpf, phone, email, password);
        ResultActions result = mockMvc.perform(put("/users/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteLoggedUserShouldReturnNoContentWhenLoggedAsAdmin() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/me")
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteLoggedUserShouldReturnNoContentWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/me")
                .header("Authorization", "Bearer " + clientToken));
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteLoggedUserShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/me")
                .header("Authorization", "Bearer " + invalidToken));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteLoggedUserShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/me"));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteUserByIdShouldReturnNoContentWhenLoggedAsAdminAndIdExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{id}", existingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNoContent());
        Optional<User> user = userRepository.findById(existingId);
        Assertions.assertTrue(user.isEmpty());
    }

    @Test
    public void deleteUserByIdShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{id}", existingId)
                .header("Authorization", "Bearer " + clientToken));
        result.andExpect(status().isForbidden());
    }

    @Test
    public void deleteUserByIdShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{id}", existingId)
                .header("Authorization", "Bearer " + invalidToken));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteUserByIdShouldReturnUnauthorizedWhenNotLogged() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{id}", existingId));
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteUserByIdShouldReturnNotFoundWhenLoggedAsAdminAndIdDoesNotExists() throws Exception {
        ResultActions result = mockMvc.perform(delete("/users/{id}", nonExistingId)
                .header("Authorization", "Bearer " + adminToken));
        result.andExpect(status().isNotFound());
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
