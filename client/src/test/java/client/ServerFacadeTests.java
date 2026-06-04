package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    // Register Tests
    @Test
    @Order(1)
    public void registerSuccess() throws Exception {
        AuthData auth = facade.register("test", "password", "test@gmail.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("test", auth.username());
    }

    @Test
    @Order(2)
    public void registerDuplicate() throws Exception {
        facade.register("test", "password", "test@gmail.com");
        assertThrows(Exception.class, () -> facade.register("test", "password", "test@gmail.com"));
    }

    // Login Tests
    @Test
    @Order(3)
    public void loginSuccess() throws Exception {
        facade.register("test", "password", "test@gmail.com");
        AuthData auth = facade.login("test", "password");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("test", auth.username());
    }

    @Test
    @Order(4)
    public void loginWrongPassword() throws Exception {
        facade.register("test", "password", "test@gmail.com");
        assertThrows(Exception.class, () -> facade.login("test", "wrongPassword"));
    }

    // Logout Tests
    @Test
    @Order(5)
    public void logoutSuccess() throws Exception {
        AuthData auth = facade.register("test", "password", "test@gmail.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    @Order(6)
    public void logoutInvalidToken() {
        assertThrows(Exception.class, () -> facade.logout("invalidToken"));
    }

    // Create Game Tests
    @Test
    @Order(7)
    public void createGameSuccess() throws Exception {
        AuthData auth = facade.register("test", "password", "test@email.com");
        int gameID = facade.createGame(auth.authToken(), "game");
        assertTrue(gameID > 0);
    }

    @Test
    @Order(8)
    public void createGameInvalidToken() {
        assertThrows(Exception.class, () -> facade.createGame("invalidToken", "game"));
    }

    // List Games Tests
    @Test
    @Order(9)
    public void listGamesSuccess() throws Exception {
        AuthData auth = facade.register("test", "password", "test@gmail.com");
        facade.createGame(auth.authToken(), "game1");
        facade.createGame(auth.authToken(), "game2");
        GameData[] games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(2, games.length);
    }

    @Test
    @Order(10)
    public void listGamesInvalidToken() {
        assertThrows(Exception.class, () -> facade.listGames("invalidToken"));
    }

    // Join Game Tests
    @Test
    @Order(11)
    public void joinGameSuccess() throws Exception {
        AuthData auth = facade.register("test", "password", "test@gmail.com");
        int gameID = facade.createGame(auth.authToken(), "game");
        assertDoesNotThrow(() -> facade.joinGame(auth.authToken(), gameID, "WHITE"));
    }

    @Test
    @Order(12)
    public void joinGameInvalidToken() throws Exception {
        AuthData auth = facade.register("test", "password", "test@gmail.com");
        int gameID = facade.createGame(auth.authToken(), "game");
        assertThrows(Exception.class, () -> facade.joinGame("invalidToken", gameID, "WHITE"));
    }

    @Test
    @Order(13)
    public void joinGameColorTaken() throws Exception {
        AuthData auth = facade.register("test", "password", "test@gmail.com");
        int gameID = facade.createGame(auth.authToken(), "game");
        facade.joinGame(auth.authToken(), gameID, "WHITE");
        assertThrows(Exception.class, () -> facade.joinGame(auth.authToken(), gameID, "WHITE"));
    }
}