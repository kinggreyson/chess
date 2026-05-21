package server;

import com.google.gson.Gson; //Pulled Gson
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.AlreadyReportedResponse;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
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

    public Server() {
        DataAccess dataAccess = new DataAccessMemory();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        clearService = new ClearService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

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
        javalin.exception(AlreadyReportedResponse.class, (e, ctx) -> {
            ctx.status(403);
            ctx.json(Map.of("message", e.getMessage()));
        });
        javalin.exception(UnauthorizedResponse.class, (e, ctx) -> {
            ctx.status(401);
            ctx.json(Map.of("message", e.getMessage()));
        });
        javalin.exception(BadRequestResponse.class, (e, ctx) -> {
            ctx.status(400);
            ctx.json(Map.of("message", e.getMessage()));
        });
        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(Map.of("message", "Error" + e.getMessage()));
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

    }

    private void login(Context ctx) throws DataAccessException
    {

    }

    private void logout(Context ctx) throws DataAccessException
    {

    }

    private void listGames(Context ctx) throws DataAccessException
    {

    }

    private void createGame(Context ctx) throws DataAccessException
    {

    }

    private void joinGame(Context ctx) throws DataAccessException
    {

    }
}
