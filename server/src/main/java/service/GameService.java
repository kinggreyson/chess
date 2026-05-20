package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
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
    public record createGameRequest(String authToken, String gameName)
    {}
    //SERVICE gameID return
    public record createGameResult(int gameID)
    {}
    //SERVICE joinGame(joinGameRequest)
    public record joinGame(String authToken, String playerColor, int gameID)
    {}
    //listGames(listGamesRequest)
    public List<GameData> listGames(String authToken) throws DataAccessException
    {
        //401 Check - authToken not found
        if(gameData.getAuth(authToken) == null)
        {
            throw new DataAccessException("Error: unauthorized");
        }
        return gameData.listGames();
    }

    //SERVICE createGameResult return
    public createGameResult createGame(createGameRequest request) throws DataAccessException
    {
        //401 Check - authToken not found
        if(gameData.getAuth(request.authToken) == null)
        {
            throw new DataAccessException("Error: unauthorized");
        }

        //400 Check - gameName is null or empty
        if(request.gameName == null || request.gameName().isEmpty())
        {
            throw new DataAccessException("Error: bad request");
        }

        int gameID = counterID++;
        //ID, WhitePlayer, BlackPlayer, gameName, ChessGame
        GameData newGame = new GameData(gameID, null, null, request.gameName, new ChessGame());
        gameData.createGame(newGame);
        return new createGameResult(gameID);
    }

    public void joinGameResult(joinGame request) throws DataAccessException
    {
        //401 Check - authToken not found
        if(gameData.getAuth(request.authToken) == null)
        {
            throw new DataAccessException("Error: unauthorized");
        }

        //400 Check - Check if game exists
        GameData game = gameData.getGame(request.gameID);
        if (game == null)
        {
            throw new DataAccessException("Error: bad request");
        }

        //403 Check - Color already taken
        if(request.playerColor().equals("WHITE") && game.whiteUsername() != null)
        {
            throw new DataAccessException("Error: already taken");
        }
        if(request.playerColor().equals("BLACK") && game.blackUsername() != null)
        {
            throw new DataAccessException("Error: already taken");
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
