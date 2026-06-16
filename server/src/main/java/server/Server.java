package server;

import com.google.gson.Gson; //Pulled Gson
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;
import service.ClearService;
import service.UserService;
import java.util.List;
import java.util.Map;
import websocket.GamePoints;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;
    private final GamePoints gamePoints;
    private final Gson gson = new Gson();


    public Server()
    {
        try {
            DataAccess dataAccess = new MySQLDataAccess();
            userService = new UserService(dataAccess);
            gameService = new GameService(dataAccess);
            clearService = new ClearService(dataAccess);
            gamePoints = new GamePoints(dataAccess);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new io.javalin.json.JavalinGson());
            config.jetty.modifyWebSocketServletFactory(factory -> {
                factory.setIdleTimeout(java.time.Duration.ofMinutes(30));
                //Extended duration so websocket doesn't disconnect during game
            });
        });

        // Register your endpoints and exception handlers here.

        //Endpoints
        javalin.delete("/db", this::clear);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        //Websocket Endpoints
        javalin.ws("/ws", ws -> {
            ws.onMessage(gamePoints :: messageDirect);
            ws.onClose(gamePoints :: closeDirect);
            ws.onError(gamePoints :: disconnectDirect);
        });


        //Exceptions
        javalin.exception(AlreadyTakenException.class, (e, ctx) -> {
            ctx.status(403);
            ctx.json(Map.of("message", e.getMessage()));
        });
        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401);
            ctx.json(Map.of("message", e.getMessage()));
        });
        javalin.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(400);
            ctx.json(Map.of("message", e.getMessage()));
        });
        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(Map.of("message", "Error: " + e.getMessage()));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }



    private void clear(Context ctx) throws DataAccessException
    {
        clearService.clear();
        ctx.status(200);
        ctx.json("{}");
    }

    private void register(Context x) throws DataAccessException
    {
        UserService.RegisterRequest request = gson.fromJson(x.body(), UserService.RegisterRequest.class);
        UserService.RegisterResult result = userService.register(request);
        x.status(200);
        x.json(result);
    }

    private void login(Context x) throws DataAccessException
    {
        UserService.LoginRequest request = gson.fromJson(x.body(), UserService.LoginRequest.class);
        UserService.LoginResult result = userService.login(request);
        x.status(200);
        x.json(result);
    }

    private void logout(Context x) throws DataAccessException
    {
        String authToken = x.header("authorization");
        userService.logout(authToken);
        x.status(200);
        x.json("{}");
    }

    private void listGames(Context x) throws DataAccessException
    {
        String authToken = x.header("authorization");
        List<GameData> games = gameService.listGames(authToken);
        x.status(200);
        x.json(Map.of("games", games));
    }

    private void createGame(Context x) throws DataAccessException {
        String authToken = x.header("authorization");
        GameService.CreateGameRequest request = gson.fromJson(x.body(), GameService.CreateGameRequest.class);
        GameService.CreateGameResult result = gameService.createGame(new GameService.CreateGameRequest(authToken, request.gameName()));
        x.status(200);
        x.json(result);
    }

    private void joinGame(Context x) throws DataAccessException
    {
        String authToken = x.header("authorization");
        GameService.JoinGameRequest request = gson.fromJson(x.body(), GameService.JoinGameRequest.class);
        gameService.joinGame(new GameService.JoinGameRequest(authToken, request.playerColor(), request.gameID()));
        x.status(200);
        x.json("{}");
    }
}
