package websocket.messages;

import model.GameData;

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;

    public ErrorMessage(String error)
    {
        super(ServerMessageType.ERROR); //UserGameCommand(Parent class handles)
        this.errorMessage = error;
    }

    public String errorResult()
    {
        return errorMessage;
    }

}
