package com.gustcustodio.e_commerce_api.services;

import com.gustcustodio.e_commerce_api.dtos.UserRequestDTO;
import com.gustcustodio.e_commerce_api.dtos.UserResponseDTO;
import com.gustcustodio.e_commerce_api.entities.User;
import com.gustcustodio.e_commerce_api.repositories.UserRepository;
import com.gustcustodio.e_commerce_api.services.exceptions.DatabaseException;
import com.gustcustodio.e_commerce_api.services.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public UserResponseDTO findUserById(Long id) {
        User entity = userRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return new UserResponseDTO(entity);
    }

    public Page<UserResponseDTO> findAllUsers(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<User> page = userRepository.findAll(pageRequest);
        return page.map(UserResponseDTO::new);
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User entity = userRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        copyDtoToEntity(userRequestDTO, entity);
        entity = userRepository.save(entity);
        return new UserResponseDTO(entity);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException();
        try {
            userRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException();
        }
    }

    private void copyDtoToEntity(UserRequestDTO dto, User entity) {
        entity.setName(dto.name());
        entity.setCpf(dto.cpf());
        entity.setPhone(dto.phone());
        entity.setEmail(dto.email());
        entity.setPassword(dto.password());
    }

}
