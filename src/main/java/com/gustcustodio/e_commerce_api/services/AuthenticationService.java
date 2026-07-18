package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.config.TokenProvider;
import com.gustcustodio.e_commerce_api.dtos.LoginRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.RegisterRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.RegisterResponseDTO;
import com.gustcustodio.e_commerce_api.dtos.LoginResponseDTO;
import com.gustcustodio.e_commerce_api.entities.Role;
import com.gustcustodio.e_commerce_api.entities.RoleType;
import com.gustcustodio.e_commerce_api.entities.User;
import com.gustcustodio.e_commerce_api.repositories.RoleRepository;
import com.gustcustodio.e_commerce_api.repositories.UserRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.CredentialsException;
import com.gustcustodio.e_commerce_api.services.exceptions.ForbiddenException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private TokenProvider tokenProvider;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        User user = new User();
        copyDtoToEntity(registerRequestDTO, user);
        Role role = roleRepository.findByAuthority(RoleType.ROLE_CLIENT.name()).orElseThrow(ResourceNotFoundException::new);
        user.getRoles().add(role);
        user = userRepository.save(user);
        return new RegisterResponseDTO(user);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.password()));
            String token = tokenProvider.generateToken(authentication);
            return new LoginResponseDTO(token);
        } catch (BadCredentialsException e) {
            throw new CredentialsException();
        }
    }

    public void validateUser(Long userId) {
        User currentUser = loggedUser();
        if (!currentUser.hasRole("ROLE_ADMIN") && !currentUser.getId().equals(userId)) throw new ForbiddenException();
    }

    protected User loggedUser() {
        try {
            return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new UsernameNotFoundException("Username not found");
        }
    }

    private void copyDtoToEntity(RegisterRequestDTO dto, User entity) {
        entity.setName(dto.name());
        entity.setEmail(dto.email());
        entity.setPassword(passwordEncoder.encode(dto.password()));
    }

}
