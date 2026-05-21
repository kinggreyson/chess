package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private UserService userService;
    private String authToken;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DataAccess dataAccess = new DataAccessMemory();
        gameService = new GameService(dataAccess);
        userService = new UserService(dataAccess);

        // Register and login a user to get a valid authToken
        UserService.RegisterRequest request = new UserService.RegisterRequest("testUser", "password", "test@email.com");
        UserService.RegisterResult result = userService.register(request);
        authToken = result.authToken();
    }

    // List Games Tests
    @Test
    public void listGamesSuccess() throws DataAccessException {
        assertDoesNotThrow(() -> gameService.listGames(authToken));
    }

    @Test
    public void listGamesInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("invalidToken"));
    }

    // Create Game Tests
    @Test
    public void createGameSuccess() throws DataAccessException {
        GameService.CreateGameRequest request = new GameService.CreateGameRequest(authToken, "testGame");
        GameService.CreateGameResult result = gameService.createGame(request);
        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameInvalidToken() {
        GameService.CreateGameRequest request = new GameService.CreateGameRequest("invalidToken", "testGame");
        assertThrows(UnauthorizedException.class, () -> gameService.createGame(request));
    }

    @Test
    public void createGameMissingName() {
        GameService.CreateGameRequest request = new GameService.CreateGameRequest(authToken, null);
        assertThrows(BadRequestException.class, () -> gameService.createGame(request));
    }

    // Join Game Tests
    @Test
    public void joinGameSuccess() throws DataAccessException {
        GameService.CreateGameRequest createRequest = new GameService.CreateGameRequest(authToken, "testGame");
        GameService.CreateGameResult createResult = gameService.createGame(createRequest);
        GameService.JoinGameRequest joinRequest = new GameService.JoinGameRequest(authToken, "WHITE", createResult.gameID());
        assertDoesNotThrow(() -> gameService.joinGame(joinRequest));
    }

    @Test
    public void joinGameInvalidToken() throws DataAccessException {
        GameService.CreateGameRequest createRequest = new GameService.CreateGameRequest(authToken, "testGame");
        GameService.CreateGameResult createResult = gameService.createGame(createRequest);
        GameService.JoinGameRequest joinRequest = new GameService.JoinGameRequest("invalidToken", "WHITE", createResult.gameID());
        assertThrows(UnauthorizedException.class, () -> gameService.joinGame(joinRequest));
    }

    @Test
    public void joinGameColorTaken() throws DataAccessException {
        GameService.CreateGameRequest createRequest = new GameService.CreateGameRequest(authToken, "testGame");
        GameService.CreateGameResult createResult = gameService.createGame(createRequest);
        GameService.JoinGameRequest joinRequest = new GameService.JoinGameRequest(authToken, "WHITE", createResult.gameID());
        gameService.joinGame(joinRequest);
        assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(joinRequest));
    }

    @Test
    public void joinGameNotFound() {
        GameService.JoinGameRequest joinRequest = new GameService.JoinGameRequest(authToken, "WHITE", 9999);
        assertThrows(BadRequestException.class, () -> gameService.joinGame(joinRequest));
    }
}
