package websocket.messages;

public class NotificationMessage extends ServerMessage{
    private final String message;

    public NotificationMessage(String message)
    {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String messageResult()
    {
        return message;
    }
}
