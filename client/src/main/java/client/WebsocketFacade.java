package client;

import chess.ChessMove;
import model.GameData;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;
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

    @OnClose
    public void onClose(Session session, CloseReason reason)
    {
        System.out.println("WebSocket closed: " + reason.getReasonPhrase());
    }

    @OnMessage
    public void interpretMessage(String message)
    {
        if (message == null) { return; }

        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        if (base == null) { return; }

        switch (base.getServerMessageType())
        {
            case LOAD_GAME -> {
                var obj = gson.fromJson(message, com.google.gson.JsonObject.class);
                GameData game = gson.fromJson(obj.get("game"), GameData.class);
                notificationMenu.notification(new LoadGameMessage(game));
            }
            case NOTIFICATION -> {
                var obj = gson.fromJson(message, com.google.gson.JsonObject.class);
                String msg = obj.get("message").getAsString();
                notificationMenu.notification(new NotificationMessage(msg));
            }
            case ERROR -> {
                var obj = gson.fromJson(message, com.google.gson.JsonObject.class);
                String err = obj.get("errorMessage").getAsString();
                notificationMenu.notification(new ErrorMessage(err));
            }
        }
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
        if (session == null || !session.isOpen())
        {
            System.out.println("Session is closed or null");
            return;
        }
        session.getBasicRemote().sendText(message);
    }
}
