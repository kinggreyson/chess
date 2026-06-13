package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class GamePoints {
    private final Connection connection = new Connection();
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public GamePoints(DataAccess dataAccess)
    {
        this.dataAccess = dataAccess;
    }

    //Link all the functions together
    public void messageDirect(WsMessageContext x)
    {
        //prevent crashing
        try {
            var route = gson.fromJson(x.message(), UserGameCommand.class);

            if (route.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE)
            {
                moveSet(x, route);
            }

            else if (route.getCommandType() == UserGameCommand.CommandType.LEAVE)
            {
                leaveSet(x, route);
            }

            else if (route.getCommandType() == UserGameCommand.CommandType.RESIGN)
            {
                resignSet(x, route);
            }

            else if (route.getCommandType() == UserGameCommand.CommandType.CONNECT)
            {
                connectSet(x, route);
            }

            else
            {
                errorSet(x, "Unknown Command Route");
            }
            }
        catch (Exception error)
        {
            errorSet(x, "error: " + error.getMessage());
        }

    }

    public void closeDirect(WsCloseContext x)
    {
        //Clean up unused games
    }

    public void disconnectDirect(WsErrorContext x)
    {
        //Server endpoint
    }

    private void connectSet(WsContext x, UserGameCommand command) throws DataAccessException
    {
        int id = command.getGameID();
        String username = confirmAuthToken(x, command);
        if (username == null)
        {
            return;
        }
        GameData game = confirmGameId(x, command);
        if(game == null)
        {
            return;
        }
        connection.join(username, id, x);

        var load = new LoadGameMessage(game);
        var sent = gson.toJson(load);
        x.send(sent);

        //notify of roles
        if(username.equals(game.whiteUsername()))
        {
            var notification = new NotificationMessage(username + " Is in at White");
            var send = gson.toJson(notification);
            connection.highlight(id, username, send);

        }
        else if(username.equals(game.blackUsername()))
        {
            var notification = new NotificationMessage(username + " Is in at Black");
            var send = gson.toJson(notification);
            connection.highlight(id, username, send);
        }
        else
        {
            var notification = new NotificationMessage(username + " Is watching");
            var send = gson.toJson(notification);
            connection.highlight(id, username, send);
        }
    }

    private void moveSet(WsContext x, UserGameCommand command) throws DataAccessException, InvalidMoveException
    {
        int id = command.getGameID();
        String username = confirmAuthToken(x, command);
        if (username == null)
        {
            return;
        }

        GameData game = confirmGameId(x, command);
        if (game == null)
        {
            return;
        }
        if(game.gameOver())
        {
            errorSet(x, "Error: Game already over");
            return;
        }

        var move = (MakeMoveCommand) command;
        ChessMove confMove = move.moveResult();
        ChessGame chessgame = game.game();

        //Turn check setup
        ChessGame.TeamColor color;
        if(username.equals(game.whiteUsername()))
        {
            color = ChessGame.TeamColor.WHITE;
        }
        else if(username.equals(game.blackUsername()))
        {
            color = ChessGame.TeamColor.BLACK;
        }
        else {
            errorSet(x, "Error, Observer can't play");
            return;
        }

        if (chessgame.getTeamTurn() != color)
        {
            errorSet(x, "Error: Not your turn");
            return;
        }

        //Move check
        var validMoves = chessgame.validMoves(confMove.getStartPosition());
        if (validMoves == null || !validMoves.contains(confMove))
        {
            errorSet(x, "Error: Can't do that move");
            return;
        }

        //Update game
        chessgame.makeMove(confMove);
        GameData gameMove = new GameData(game.gameID(),game.whiteUsername(),
                game.blackUsername(), game.gameName(), chessgame, game.gameOver());
        dataAccess.updateGame(gameMove);

        //Update board
        var load = new LoadGameMessage(gameMove);
        var sent = gson.toJson(load);
        connection.highlight(id, null, sent);
        x.send(sent);

        //Notify players/obeservers except the one who made the move
        var notification = new NotificationMessage(username + " Made a move");
        var send = gson.toJson(notification);
        connection.highlight(id, username, send);

        //Checkmate
        GameData gameOver = new GameData(game.gameID(),game.whiteUsername(),
                game.blackUsername(), game.gameName(), chessgame, true);
        if(chessgame.isInCheckmate(ChessGame.TeamColor.WHITE) || chessgame.isInCheckmate(ChessGame.TeamColor.BLACK))
        {
            if (chessgame.isInCheckmate(ChessGame.TeamColor.WHITE))
            {
                var checkmateNotification = new NotificationMessage(game.blackUsername() + " has won the game!");
                var mate = gson.toJson(checkmateNotification);
                connection.highlight(id, null, mate);
            }
            else if(chessgame.isInCheckmate(ChessGame.TeamColor.BLACK))
            {
                var checkmateNotification = new NotificationMessage(game.whiteUsername() + " has won the game!");
                var mate = gson.toJson(checkmateNotification);
                connection.highlight(id, null, mate);
            }

            dataAccess.updateGame(gameOver);
        }

        //Stalemate
        if(chessgame.isInStalemate(ChessGame.TeamColor.WHITE)
                || chessgame.isInStalemate(ChessGame.TeamColor.BLACK))
        {
            var stalemateNotification = new NotificationMessage(username + " The game ends in a tie");
            var stale = gson.toJson(stalemateNotification);
            connection.highlight(id, null, stale);
            dataAccess.updateGame(gameOver);
        }

        //Check
        if(chessgame.isInCheck(ChessGame.TeamColor.WHITE) || chessgame.isInCheck(ChessGame.TeamColor.BLACK))
        {
            if(chessgame.isInCheck(ChessGame.TeamColor.WHITE))
            {
                var checkNotification = new NotificationMessage(username + " (White) in check");
                var check = gson.toJson(checkNotification);
                connection.highlight(id, null, check);
            }
            if(chessgame.isInCheck(ChessGame.TeamColor.BLACK))
            {
                var checkNotification = new NotificationMessage(username + " (Black) in check");
                var check = gson.toJson(checkNotification);
                connection.highlight(id, null, check);
            }
        }
    }

    private void leaveSet(WsContext x, UserGameCommand command) throws DataAccessException
    {
        int id = command.getGameID();
        String username = confirmAuthToken(x, command);
        if (username == null)
        {
            return;
        }

        GameData game = confirmGameId(x, command);
        if (game == null)
        {
            return;
        }
        connection.remove(username, id);

        //Non-observer
        if (username.equals(game.whiteUsername()) || username.equals(game.blackUsername()))
        {
            if (username.equals(game.whiteUsername()))
            {
                GameData whiteLeft = new GameData(game.gameID(),null,
                        game.blackUsername(), game.gameName(), game.game(), game.gameOver());
                dataAccess.updateGame(whiteLeft);
                var notification = new NotificationMessage(username + " Has left the game (White)");
                var send = gson.toJson(notification);
                connection.highlight(id, username, send);
            }
            else
            {
                GameData blackLeft = new GameData(game.gameID(), game.whiteUsername(),
                        null, game.gameName(), game.game(), game.gameOver());
                dataAccess.updateGame(blackLeft);
                var notification = new NotificationMessage(username + " Has left the game (Black)");
                var send = gson.toJson(notification);
                connection.highlight(id, username, send);
            }
        }
        //Observer
        else
        {
            var notification = new NotificationMessage(username + " Has left the game (Observer)");
            var send = gson.toJson(notification);
            connection.highlight(id, username, send);
        }
    }

    private void resignSet(WsContext x, UserGameCommand command) throws DataAccessException
    {
        int id = command.getGameID();
        String username = confirmAuthToken(x, command);
        if (username == null)
        {
            return;
        }

        GameData game = confirmGameId(x, command);
        if (game == null)
        {
            return;
        }
        if(game.gameOver())
        {
            errorSet(x, "Error: Game already over");
            return;
        }
        if(!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername()))
        {
            errorSet(x, "Error: Observer can't end game");
            return;
        }


        GameData gameEnd = new GameData(game.gameID(),game.whiteUsername(),
                game.blackUsername(), game.gameName(), game.game(), true);
        dataAccess.updateGame(gameEnd);
        var notification = new NotificationMessage(username + " Gave up");
        var send = gson.toJson(notification);
        connection.highlight(id, null, send);
    }

    private void errorSet(WsContext x, String message)
    {
        var error = new ErrorMessage(message);
        x.send(gson.toJson(error));
    }

    private String confirmAuthToken( WsContext x, UserGameCommand command) throws DataAccessException
    {
        String auth = command.getAuthToken();
        AuthData authdata = dataAccess.getAuth(auth);
        if(authdata == null)
        {
            errorSet(x, "Error: Invalid authToken");
            return null;
        }
        return authdata.username();
    }

    private GameData confirmGameId(WsContext x, UserGameCommand command) throws DataAccessException
    {
        int id = command.getGameID();
        GameData game = dataAccess.getGame(id);
        if(game == null)
        {
            errorSet(x, "Error: Game doesn't exist");
            return null;
        }
        return game;
    }
}
