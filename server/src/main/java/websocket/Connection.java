package websocket;
import io.javalin.websocket.WsContext;
import java.util.ArrayList;
import java.util.Objects;

public class Connection
{

    public static class Conn {
        public String username;
        public int gameID;
        public WsContext current;

        public Conn(String username, int gameID, WsContext current) {
            this.username = username;
            this.gameID = gameID;
            this.current = current;
        }
    }
        private final ArrayList<Conn> conn = new ArrayList<>();

        //If player or observer joins
        public void join(String username, int gameID, WsContext current)
        {
            conn.add(new Conn(username,gameID,current));
        }

        //If player or observer leaves
        public void remove(String username, int gameID)
        {
            //iterate through the list
            conn.removeIf(item -> item.gameID == gameID && item.username.equals(username));
        }

        //Sends a message to a specific player
        public void message(String username, int gameID, String message)
        {
            for (Conn item: conn)
            {
                if (item.gameID == gameID && item.username.equals(username))
                {
                    item.current.send(message);
                }
            }
        }

        //Highlights notification to other player for example if the other player made a move
        //Send to everyone
        public void highlight(int gameID, String outUser, String send)
        {
            conn.removeIf(item -> !item.current.session.isOpen());
            for (Conn item : conn)
            {
                //Need to exclude player notification is about
                if(item.gameID  == gameID && !Objects.equals(outUser, item.username))
                {
                    item.current.send(send);
                }
            }
        }

    }
