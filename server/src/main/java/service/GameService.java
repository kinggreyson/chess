package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public class GameService {
    private final DataAccess gameData;

    public GameService(DataAccess dataAccess)
    {
        this.gameData = dataAccess;
    }

    //createGame(createGameRequest)
    public record CreateGameRequest(String authToken, String gameName)
    {}
    //SERVICE gameID return
    public record CreateGameResult(int gameID)
    {}
    //SERVICE joinGame(joinGameRequest)
    public record JoinGameRequest(String authToken, String playerColor, int gameID)
    {}
    //listGames(listGamesRequest)
    public List<GameData> listGames(String authToken) throws DataAccessException
    {
        //401 Check - authToken not found
        if(gameData.getAuth(authToken) == null)
        {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return gameData.listGames();
    }

    //SERVICE createGameResult return
    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        // 401 check
        if (gameData.getAuth(request.authToken()) == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        // 400 check
        if (request.gameName() == null || request.gameName().isEmpty()) {
            throw new BadRequestException("Error: bad request");
        }

        GameData game = new GameData(0, null, null, request.gameName(), new ChessGame(), false);
        int gameID = gameData.createGame(game);
        return new CreateGameResult(gameID);
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException
    {
        AuthData token = gameData.getAuth(request.authToken);
        //401 Check - authToken not found
        if(token == null)
        {
            throw new UnauthorizedException("Error: unauthorized");
        }

        //400 Check - Check if game exists
        GameData game = gameData.getGame(request.gameID());
        if (game == null || request.playerColor() == null ||
                (!request.playerColor().equals("WHITE") && !request.playerColor().equals("BLACK")))
        {
            throw new BadRequestException("Error: bad request");
        }
        if (game.gameOver())
        {
            throw new BadRequestException("Error: game is already over");
        }

        //403 Check - Color already taken
        if(request.playerColor().equals("WHITE") && game.whiteUsername() != null)
        {
            throw new AlreadyTakenException("Error: already taken");
        }
        if(request.playerColor().equals("BLACK") && game.blackUsername() != null)
        {
            throw new AlreadyTakenException("Error: already taken");
        }

        //Add new player into game (joinGameResult)
        GameData newPlayer;
        //get newplayer color
        if(request.playerColor().equals("WHITE"))
        {
            newPlayer = new GameData(game.gameID(), token.username(), game.blackUsername(), game.gameName(), game.game(), false);
        }
        else
        {
            newPlayer = new GameData(game.gameID(), game.whiteUsername(), token.username(), game.gameName(), game.game(), false);
        }
        gameData.updateGame(newPlayer);
    }
}
