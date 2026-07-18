package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.UserRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.UserResponseDTO;
import com.gustcustodio.e_commerce_api.entities.User;
import com.gustcustodio.e_commerce_api.repositories.UserRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy AuthenticationService authenticationService, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public UserResponseDTO findLoggedUser() {
        return new UserResponseDTO(authenticationService.loggedUser());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id) {
        User entity = userRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return new UserResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllUsers(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<User> page = userRepository.findAll(pageRequest);
        return page.map(UserResponseDTO::new);
    }

    @Transactional
    public UserResponseDTO updateLoggedUser(UserRequestDTO userRequestDTO) {
        User entity = authenticationService.loggedUser();
        return processUpdate(entity, userRequestDTO);
    }

    @Transactional
    public UserResponseDTO updateUserById(Long id, UserRequestDTO userRequestDTO) {
        User entity = userRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return processUpdate(entity, userRequestDTO);
    }

    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        userRepository.delete(user);
    }

    @Transactional
    public void deleteLoggedUser() {
        User user = authenticationService.loggedUser();
        userRepository.delete(user);
    }

    private UserResponseDTO processUpdate(User entity, UserRequestDTO dto) {
        copyDtoToEntity(dto, entity);
        entity = userRepository.save(entity);
        return new UserResponseDTO(entity);
    }

    private void copyDtoToEntity(UserRequestDTO dto, User entity) {
        entity.setName(dto.name());
        entity.setCpf(dto.cpf());
        entity.setPhone(dto.phone());
        entity.setEmail(dto.email());
        entity.setPassword(passwordEncoder.encode(dto.password()));
    }

}
