package org.gymcrm.security;

import org.gymcrm.dao.UserDao;
import org.gymcrm.model.Role;
import org.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserDao userDao;

    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_returnsUserPrincipalWhenUserExists() {
        service = new CustomUserDetailsService(userDao);
        User user = new User();
        user.setUsername("john.doe");
        user.setRole(Role.TRAINEE);
        when(userDao.findByUsername("john.doe")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("john.doe");

        assertInstanceOf(UserPrincipal.class, result);
        assertEquals("john.doe", result.getUsername());
    }

    @Test
    void loadUserByUsername_throwsWhenUserDoesNotExist() {
        service = new CustomUserDetailsService(userDao);
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
    }
}