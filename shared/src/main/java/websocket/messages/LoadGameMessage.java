package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage {
    private final GameData game;

    public LoadGameMessage(GameData game)
    {
        super(ServerMessageType.LOAD_GAME); //UserGameCommand(Parent class handles)
        this.game = game;
    }

    public GameData gameResult()
    {
        return game;
    }

}
