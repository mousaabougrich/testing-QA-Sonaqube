package com.wallet.biochain.services;

import com.wallet.biochain.dto.UserDTO;
import com.wallet.biochain.entities.User;
import com.wallet.biochain.entities.Wallet;
import com.wallet.biochain.mappers.UserMapper;
import com.wallet.biochain.repositories.UserRepository;
import com.wallet.biochain.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPhoneNumber("+1234567890");
        user.setIsActive(true);

        userDTO = mock(UserDTO.class);
    }

    @Test
    void createUser_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.createUser("testuser", "test@example.com",
                "Test User", "+1234567890");

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDTO(user);
    }

    @Test
    void createUser_usernameExists_throws() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("testuser", "test@example.com",
                        "Test User", "+1234567890"));
    }

    @Test
    void createUser_emailExists_throws() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("testuser", "test@example.com",
                        "Test User", "+1234567890"));
    }

    @Test
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        Optional<UserDTO> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(userDTO, result.get());
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.getUserById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getUserByUsername_found() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        Optional<UserDTO> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
    }

    @Test
    void getUserByEmail_found() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        Optional<UserDTO> result = userService.getUserByEmail("test@example.com");

        assertTrue(result.isPresent());
    }

    @Test
    void updateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(1L, "Updated Name", "+9876543210");

        assertNotNull(result);
        assertEquals("Updated Name", user.getFullName());
        assertEquals("+9876543210", user.getPhoneNumber());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, "Name", "+1234567890"));
    }

    @Test
    void updateUser_nullValues_keepsOriginal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        String originalName = user.getFullName();
        String originalPhone = user.getPhoneNumber();

        userService.updateUser(1L, null, null);

        assertEquals(originalName, user.getFullName());
        assertEquals(originalPhone, user.getPhoneNumber());
    }

    @Test
    void deactivateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertFalse(user.getIsActive());
        verify(userRepository).save(user);
    }

    @Test
    void activateUser_success() {
        user.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.activateUser(1L);

        assertTrue(user.getIsActive());
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_success() {
        user.setWallets(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_withActiveWallets_throws() {
        Wallet wallet = new Wallet();
        wallet.setIsActive(true);
        user.setWallets(List.of(wallet));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class,
                () -> userService.deleteUser(1L));
    }

    @Test
    void searchUsers_returnsResults() {
        when(userRepository.searchUsers("test")).thenReturn(List.of(user));
        when(userMapper.toDTOList(anyList())).thenReturn(List.of(userDTO));

        List<UserDTO> results = userService.searchUsers("test");

        assertEquals(1, results.size());
    }

    @Test
    void usernameExists_true() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertTrue(userService.usernameExists("testuser"));
    }

    @Test
    void emailExists_false() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        assertFalse(userService.emailExists("test@example.com"));
    }

    @Test
    void countActiveUsers_returnsCount() {
        when(userRepository.countActiveUsers()).thenReturn(5L);
        assertEquals(5L, userService.countActiveUsers());
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDTOList(anyList())).thenReturn(List.of(userDTO));

        List<UserDTO> results = userService.getAllUsers();

        assertEquals(1, results.size());
    }

    @Test
    void getActiveUsers_returnsList() {
        when(userRepository.findByIsActive(true)).thenReturn(List.of(user));
        when(userMapper.toDTOList(anyList())).thenReturn(List.of(userDTO));

        List<UserDTO> results = userService.getActiveUsers();

        assertEquals(1, results.size());
    }
}
