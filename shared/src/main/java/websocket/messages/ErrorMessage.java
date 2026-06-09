package websocket.messages;

import model.GameData;

public class ErrorMessage extends ServerMessage {
    private final String error;

    public ErrorMessage(String error)
    {
        super(ServerMessageType.ERROR); //UserGameCommand(Parent class handles)
        this.error = error;
    }

    public String errorResult()
    {
        return error;
    }

}
