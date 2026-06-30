package com.gustcustodio.e_commerce_api.factories;

import com.gustcustodio.e_commerce_api.entities.Role;
import com.gustcustodio.e_commerce_api.entities.User;

public class UserFactory {

    public static User createUserAdminAndClient() {
        User user = new User(1L, "Valid User", "391.385.040-65", "991233344", "user@email.com", "$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe");
        Role roleAdmin = new Role(1L, "ROLE_ADMIN");
        Role roleClient = new Role(2L, "ROLE_CLIENT");
        user.getRoles().add(roleAdmin);
        user.getRoles().add(roleClient);
        return user;
    }

    public static User createUserAdmin() {
        User user = new User(1L, "Valid Admin User", "441.766.180-43", "991234455", "admin@email.com", "$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe");
        Role role = new Role(1L, "ROLE_ADMIN");
        user.getRoles().add(role);
        return user;
    }

    public static User createUserClient() {
        User user = new User(2L, "Valid Client User", "563.557.910-86", "991235566", "client@email.com", "$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe");
        Role role = new Role(2L, "ROLE_CLIENT");
        user.getRoles().add(role);
        return user;
    }

}
