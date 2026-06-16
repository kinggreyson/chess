package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    private final ChessMove move; // non-final so Gson can set it

    public MakeMoveCommand(String authToken, int gameID, ChessMove move)
    {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }

    public ChessMove moveResult()
    {
        return move;
    }
}