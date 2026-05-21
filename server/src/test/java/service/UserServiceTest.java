package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DataAccess dataAccess = new DataAccessMemory();
        userService = new UserService(dataAccess);
    }

    // Register Tests
    @Test
    public void registerSuccess() throws DataAccessException {
        UserService.RegisterRequest request = new UserService.RegisterRequest("testUser", "password", "test@email.com");
        UserService.RegisterResult result = userService.register(request);
        assertEquals("testUser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerDuplicateUsername() throws DataAccessException {
        UserService.RegisterRequest request = new UserService.RegisterRequest("testUser", "password", "test@email.com");
        userService.register(request);
        assertThrows(AlreadyTakenException.class, () -> userService.register(request));
    }

    @Test
    public void registerMissingFields() {
        UserService.RegisterRequest request = new UserService.RegisterRequest(null, "password", "test@email.com");
        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    // Login Tests
    @Test
    public void loginSuccess() throws DataAccessException {
        UserService.RegisterRequest registerRequest = new UserService.RegisterRequest("testUser", "password", "test@email.com");
        userService.register(registerRequest);
        UserService.LoginRequest loginRequest = new UserService.LoginRequest("testUser", "password");
        UserService.LoginResult result = userService.login(loginRequest);
        assertEquals("testUser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginWrongPassword() throws DataAccessException {
        UserService.RegisterRequest registerRequest = new UserService.RegisterRequest("testUser", "password", "test@email.com");
        userService.register(registerRequest);
        UserService.LoginRequest loginRequest = new UserService.LoginRequest("testUser", "wrongPassword");
        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }

    @Test
    public void loginNonExistentUser() {
        UserService.LoginRequest loginRequest = new UserService.LoginRequest("nobody", "password");
        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }

    // Logout Tests
    @Test
    public void logoutSuccess() throws DataAccessException {
        UserService.RegisterRequest request = new UserService.RegisterRequest("testUser", "password", "test@email.com");
        UserService.RegisterResult result = userService.register(request);
        assertDoesNotThrow(() -> userService.logout(result.authToken()));
    }

    @Test
    public void logoutInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> userService.logout("invalidToken"));
    }
}
