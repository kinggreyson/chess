package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLDataAccessTests {

    private static MySQLDataAccess dataAccess;

    @BeforeAll
    public static void setUp() throws DataAccessException {
        dataAccess = new MySQLDataAccess();
    }

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        dataAccess.clear();
    }

    // Clear Tests
    @Test
    public void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("testUser", "password", "test@gmail.com"));
        assertDoesNotThrow(() -> dataAccess.clear());
        assertNull(dataAccess.getUser("testUser"));
    }

    // User Tests
    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@gmail.com");
        assertDoesNotThrow(() -> dataAccess.createUser(user));
        UserData retrieved = dataAccess.getUser("testUser");
        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.username());
    }

    @Test
    public void createUserDuplicate() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@gmail.com");
        dataAccess.createUser(user);
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user));
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@gmail.com");
        dataAccess.createUser(user);
        UserData retrieved = dataAccess.getUser("testUser");
        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.username());
        assertEquals("test@gmail.com", retrieved.email());
    }

    @Test
    public void getUserNotFound() throws DataAccessException {
        assertNull(dataAccess.getUser("nonExistentUser"));
    }

    @Test
    public void passwordIsHashed() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@gmail.com");
        dataAccess.createUser(user);
        UserData retrieved = dataAccess.getUser("testUser");
        assertNotNull(retrieved);
        assertNotEquals("password", retrieved.password());
        assertTrue(BCrypt.checkpw("password", retrieved.password()));
    }

    // Auth Tests
    @Test
    public void createAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("testToken", "testUser");
        assertDoesNotThrow(() -> dataAccess.createAuth(auth));
        AuthData retrieved = dataAccess.getAuth("testToken");
        assertNotNull(retrieved);
        assertEquals("testToken", retrieved.authToken());
    }

    @Test
    public void createAuthDuplicate() throws DataAccessException {
        AuthData auth = new AuthData("testToken", "testUser");
        dataAccess.createAuth(auth);
        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(auth));
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("testToken", "testUser");
        dataAccess.createAuth(auth);
        AuthData retrieved = dataAccess.getAuth("testToken");
        assertNotNull(retrieved);
        assertEquals("testToken", retrieved.authToken());
        assertEquals("testUser", retrieved.username());
    }

    @Test
    public void getAuthNotFound() throws DataAccessException {
        assertNull(dataAccess.getAuth("nonExistentToken"));
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("testToken", "testUser");
        dataAccess.createAuth(auth);
        assertDoesNotThrow(() -> dataAccess.deleteAuth("testToken"));
        assertNull(dataAccess.getAuth("testToken"));
    }

    @Test
    public void deleteAuthNotFound() throws DataAccessException {
        assertDoesNotThrow(() -> dataAccess.deleteAuth("nonExistentToken"));
    }

    // Game Tests
    @Test
    public void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "testGame", new ChessGame(), false);
        int gameID = dataAccess.createGame(game);
        assertTrue(gameID > 0);
        GameData retrieved = dataAccess.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals("testGame", retrieved.gameName());
    }

    @Test
    public void createGameMissingName() {
        GameData game = new GameData(0, null, null, null, new ChessGame(), false);
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(game));
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "testGame", new ChessGame(), false);
        int gameID = dataAccess.createGame(game);
        GameData retrieved = dataAccess.getGame(gameID);
        assertNotNull(retrieved);
        assertEquals("testGame", retrieved.gameName());
        assertEquals(gameID, retrieved.gameID());
    }

    @Test
    public void getGameNotFound() throws DataAccessException {
        assertNull(dataAccess.getGame(9999));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        dataAccess.createGame(new GameData(0, null, null, "game1", new ChessGame(), false));
        dataAccess.createGame(new GameData(0, null, null, "game2", new ChessGame(), false));
        var games = dataAccess.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesEmpty() throws DataAccessException {
        var games = dataAccess.listGames();
        assertEquals(0, games.size());
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        GameData game = new GameData(0, null, null, "testGame", new ChessGame(), false);
        int gameID = dataAccess.createGame(game);
        GameData updated = new GameData(gameID, "whitePlayer", null, "testGame", new ChessGame(), false);
        assertDoesNotThrow(() -> dataAccess.updateGame(updated));
        GameData retrieved = dataAccess.getGame(gameID);
        assertEquals("whitePlayer", retrieved.whiteUsername());
    }

    @Test
    public void updateGameNotFound() {
        GameData game = new GameData(14, "whitePlayer", null, "testGame", new ChessGame(), false);
        assertDoesNotThrow(() -> dataAccess.updateGame(game));
    }

    @Test
    public void gameStatePreserved() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(0, null, null, "testGame", chessGame, false);
        int gameID = dataAccess.createGame(game);
        GameData retrieved = dataAccess.getGame(gameID);
        assertNotNull(retrieved.game());
        assertEquals(chessGame, retrieved.game());
    }
}