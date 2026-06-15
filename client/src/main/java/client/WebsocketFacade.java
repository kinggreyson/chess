package client;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebsocketFacade {
    private Session session;
    private final NotificationMenu notificationMenu;
    private final Gson gson = new Gson();

    public WebsocketFacade(String url, NotificationMenu notificationMenu) throws Exception
    {
        URI uri = new URI(url.replace("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.notificationMenu = notificationMenu;
        container.connectToServer(this, uri);
    }

    @OnOpen
    public void open(Session session)
    {
        this.session = session;
    }

    @OnMessage
    public void recMessage(String message)
    {
        interpretMessage(message);
    }

    public void interpretMessage(String message)
    {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        notificationMenu.notification(serverMessage);
    }

    public void connect(String authToken, int gameID) throws IOException
    {
        var sendConnect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        send(gson.toJson(sendConnect));
    }

    public void move(String authToken, int gameID, ChessMove move) throws IOException
    {
        var sendMove = new MakeMoveCommand(authToken, gameID, move);
        send(gson.toJson(sendMove));
    }

    public void leave(String authToken, int gameID) throws IOException
    {
        var sendLeave = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        send(gson.toJson(sendLeave));
    }

    public void resign(String authToken, int gameID) throws IOException
    {
        var sendResign = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        send(gson.toJson(sendResign));
    }


    private void send(String message) throws IOException
    {
        session.getBasicRemote().sendText(message);
    }
}
