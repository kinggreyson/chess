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
import java.util.UUID;

public class Server {

    private final Javalin javalin;
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;
    private final Gson gson = new Gson();


    public Server() {
        DataAccess dataAccess = new DataAccessMemory();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new io.javalin.json.JavalinGson());
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

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void clear(Context ctx) throws DataAccessException
    {
        clearService.clear();
        ctx.status(200);
        ctx.json("{}");
    }

    private void register(Context ctx) throws DataAccessException
    {
        UserService.RegisterRequest request = gson.fromJson(ctx.body(), UserService.RegisterRequest.class);
        UserService.RegisterResult result = userService.register(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void login(Context ctx) throws DataAccessException
    {
        UserService.LoginRequest request = gson.fromJson(ctx.body(), UserService.LoginRequest.class);
        UserService.LoginResult result = userService.login(request);
        ctx.status(200);
        ctx.json(result);
    }

    private void logout(Context ctx) throws DataAccessException
    {
        String authToken = ctx.header("authorization");
        userService.logout(authToken);
        ctx.status(200);
        ctx.json("{}");
    }

    private void listGames(Context ctx) throws DataAccessException
    {
        String authToken = ctx.header("authorization");
        List<GameData> games = gameService.listGames(authToken);
        ctx.status(200);
        ctx.json(Map.of("games", games));
    }

    private void createGame(Context ctx) throws DataAccessException
    {
        String authToken = ctx.header("authorization");
        GameService.CreateGameRequest request = gson.fromJson(ctx.body(), GameService.CreateGameRequest.class);
        GameService.CreateGameResult result = gameService.createGame(new GameService.CreateGameRequest(authToken, request.gameName()));
        ctx.status(200);
        ctx.json(result);
    }

    private void joinGame(Context ctx) throws DataAccessException
    {
        String authToken = ctx.header("authorization");
        GameService.JoinGameRequest request = gson.fromJson(ctx.body(), GameService.JoinGameRequest.class);
        gameService.joinGame(new GameService.JoinGameRequest(authToken, request.playerColor(), request.gameID()));
        ctx.status(200);
        ctx.json("{}");
    }
}
