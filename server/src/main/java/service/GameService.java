package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public class GameService {
    private final DataAccess gameData;
    private int counterID = 1;

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
    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException
    {
        //401 Check - authToken not found
        if(gameData.getAuth(request.authToken) == null)
        {
            throw new UnauthorizedException("Error: unauthorized");
        }

        //400 Check - gameName is null or empty
        if(request.gameName == null || request.gameName().isEmpty())
        {
            throw new BadRequestException("Error: bad request");
        }

        int gameID = counterID++;
        //ID, WhitePlayer, BlackPlayer, gameName, ChessGame
        GameData newGame = new GameData(gameID, null, null, request.gameName, new ChessGame());
        gameData.createGame(newGame);
        return new CreateGameResult(gameID);
    }

    public void joinGameResult(JoinGameRequest request) throws DataAccessException
    {
        //401 Check - authToken not found
        if(gameData.getAuth(request.authToken) == null)
        {
            throw new UnauthorizedException("Error: unauthorized");
        }

        //400 Check - Check if game exists
        GameData game = gameData.getGame(request.gameID);
        if (game == null)
        {
            throw new BadRequestException("Error: bad request");
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
        AuthData auth = gameData.getAuth(request.authToken);
        if(request.playerColor().equals("WHITE"))
        {
            newPlayer = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
        }
        else
        {
            newPlayer = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
        }
        gameData.updateGame(newPlayer);
    }
}
