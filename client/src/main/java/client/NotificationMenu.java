package client;
import websocket.messages.ServerMessage;

public interface NotificationMenu {
    void notification(ServerMessage message);
}
