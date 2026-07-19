package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.UserRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.UserResponseDTO;
import com.gustcustodio.e_commerce_api.entities.User;
import com.gustcustodio.e_commerce_api.factories.UserFactory;
import com.gustcustodio.e_commerce_api.repositories.UserRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE = "Resource not found";
    private static final String USERNAME_NOT_FOUND_EXCEPTION_MESSAGE = "User not found";

    private Long existingId, nonExistingId;
    private User validUser, userAdmin, userClient;
    private UserRequestDTO userRequestDTO;
    private Integer pageNumber, pageSize;
    private PageRequest pageRequest;
    private List<User> users;
    private Page<User> usersPage;
    private String validEmail, invalidEmail;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 1000L;
        validUser = UserFactory.createUserAdminAndClient();
        userAdmin = UserFactory.createUserAdmin();
        userClient = UserFactory.createUserClient();
        userRequestDTO = new UserRequestDTO("Updated User Name", "123.456.789-10", "987654321", "updated@email.com", "newPassword123");
        pageNumber = 0;
        pageSize = 5;
        pageRequest = PageRequest.of(pageNumber, pageSize);
        users = List.of(validUser, userAdmin, userClient);
        usersPage = new PageImpl<>(users, pageRequest, 3L);
        validEmail = "user@email.com";
        invalidEmail = "invalid@email.com";
        encodedPassword = "$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe";

        when(userRepository.findById(existingId)).thenReturn(Optional.of(validUser));
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(validUser));
        when(userRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());
        when(userRepository.findAll(pageRequest)).thenReturn(usersPage);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authenticationService.loggedUser()).thenReturn(validUser);
        when(passwordEncoder.encode(userRequestDTO.password())).thenReturn(encodedPassword);
        doNothing().when(userRepository).delete(any(User.class));
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenEmailExists() {
        var result = userService.loadUserByUsername(validEmail);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(validUser, result);
        verify(userRepository, times(1)).findByEmail(validEmail);
    }

    @Test
    public void loadUserByUsernameShouldThrowsUsernameNotFoundExceptionWhenEmailDoesNotExist() {
        UsernameNotFoundException exception =
                Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(invalidEmail));
        Assertions.assertEquals(USERNAME_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(invalidEmail);
    }

    @Test
    public void findLoggedUserShouldReturnUserResponseDTOWhenUserLogged() {
        UserResponseDTO result = userService.findLoggedUser();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(validUser.getId(), result.id());
        Assertions.assertEquals(validUser.getName(), result.name());
        verify(authenticationService, times(1)).loggedUser();
    }

    @Test
    public void findUserByIdShouldReturnUserResponseDTOWhenIdExists() {
        UserResponseDTO result = userService.findUserById(existingId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(validUser.getId(), result.id());
        Assertions.assertEquals(validUser.getName(), result.name());
        verify(userRepository, times(1)).findById(existingId);
    }

    @Test
    public void findUserByIdShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistingId);
    }

    @Test
    public void findAllUsersShouldReturnPagedUserResponseDTOWhenUsersExist() {
        Page<UserResponseDTO> result = userService.findAllUsers(pageNumber, pageSize);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.getContent().size());
        Assertions.assertEquals(3L, result.getTotalElements());
        verify(userRepository, times(1)).findAll(pageRequest);
    }

    @Test
    public void findAllUsersShouldReturnEmptyPageWhenNoUsersExist() {
        when(userRepository.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(), pageRequest, 0L));
        Page<UserResponseDTO> result = userService.findAllUsers(pageNumber, pageSize);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0L, result.getTotalElements());
        verify(userRepository, times(1)).findAll(pageRequest);
    }

    @Test
    public void updateLoggedUserShouldReturnUserResponseDTOWhenUserLogged() {
        UserResponseDTO result = userService.updateLoggedUser(userRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(validUser.getId(), result.id());
        verify(authenticationService, times(1)).loggedUser();
        verify(passwordEncoder, times(1)).encode(userRequestDTO.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateLoggedUserShouldUpdateUserFieldsCorrectly() {
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        userService.updateLoggedUser(userRequestDTO);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        assertUserFields(userArgumentCaptor.getValue(), userRequestDTO);
    }

    @Test
    public void updateUserByIdShouldReturnUserResponseDTOWhenIdExists() {
        UserResponseDTO result = userService.updateUserById(existingId, userRequestDTO);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(validUser.getId(), result.id());
        verify(userRepository, times(1)).findById(existingId);
        verify(passwordEncoder, times(1)).encode(userRequestDTO.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateUserByIdShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.updateUserById(nonExistingId, userRequestDTO));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistingId);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void updateUserByIdShouldUpdateUserFieldsCorrectly() {
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        userService.updateUserById(existingId, userRequestDTO);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        assertUserFields(userArgumentCaptor.getValue(), userRequestDTO);
    }

    @Test
    public void deleteUserByIdShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> userService.deleteUserById(existingId));
        verify(userRepository, times(1)).findById(existingId);
        verify(userRepository, times(1)).delete(validUser);
    }

    @Test
    public void deleteUserByIdShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        ResourceNotFoundException exception =
                Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserById(nonExistingId));
        Assertions.assertEquals(RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE, exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistingId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    public void deleteLoggedUserShouldDoNothingWhenUserLogged() {
        Assertions.assertDoesNotThrow(() -> userService.deleteLoggedUser());
        verify(authenticationService, times(1)).loggedUser();
        verify(userRepository, times(1)).delete(validUser);
    }

    private void assertUserFields(User user, UserRequestDTO dto) {
        Assertions.assertEquals(dto.name(), user.getName());
        Assertions.assertEquals(dto.cpf(), user.getCpf());
        Assertions.assertEquals(dto.phone(), user.getPhone());
        Assertions.assertEquals(dto.email(), user.getEmail());
        Assertions.assertEquals(encodedPassword, user.getPassword());
    }

}