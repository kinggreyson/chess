package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class ServerFacade {

private final String Url;
private final Gson gson = new Gson();

public ServerFacade(int port)
{
    this.Url = "http://localhost:" + port;
}

public AuthData register(String username, String password, String email)
{

}

public AuthData login(String username, String password)
{

}

public void logout(String authToken)
{

}

public int createGame(String authToken, String gameName)
{

}

public GameData[] listGames(String authToken)
{

}

public void joinGame(String authToken, int gameID, String playerColor)
{

}
}
