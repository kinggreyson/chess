package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private ClearService clearService;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        DataAccess dataAccess = new DataAccessMemory();
        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        // Add data first
        UserService.RegisterRequest request = new UserService.RegisterRequest("test", "123", "test@email.com");
        userService.register(request);
        // Clear and verify no exception thrown
        assertDoesNotThrow(() -> clearService.clear());
        // Verify data is gone by registering same user again successfully
        assertDoesNotThrow(() -> userService.register(request));
    }
}
