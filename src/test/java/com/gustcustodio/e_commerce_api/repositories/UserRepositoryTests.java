package com.gustcustodio.e_commerce_api.repositories;

import com.gustcustodio.e_commerce_api.entities.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private String existingUserEmail;
    private String nonExistingUserEmail;

    @BeforeEach
    void setUp() throws Exception {
        existingUserEmail = "dickens@email.com";
        nonExistingUserEmail = "xpto@email.com";
    }

    @Test
    public void findByEmailShouldReturnNotEmptyOptionalWhenEmailExists() {
        Optional<User> result = userRepository.findByEmail(existingUserEmail);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(result.get().getName(), "Charles Dickens");
    }

    @Test
    public void findByEmailShouldReturnEmptyOptionalWhenEmailDoesNotExists() {
        Optional<User> result = userRepository.findByEmail(nonExistingUserEmail);
        Assertions.assertTrue(result.isEmpty());
    }

}
