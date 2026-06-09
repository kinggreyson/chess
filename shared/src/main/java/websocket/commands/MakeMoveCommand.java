package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move;

    public MakeMoveCommand(String authToken, int gameID, ChessMove move)
    {
        super(CommandType.MAKE_MOVE, authToken, gameID); //UserGameCommand(Parent class handles)
        this.move = move;
    }

    public ChessMove moveResult()
    {
        return move;
    }

}
