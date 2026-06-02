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

private final String serverUrl;
private final Gson gson = new Gson();

public ServerFacade(int port)
{
    this.serverUrl = "http://localhost:" + port;
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

private <T> T request(String method, String path, Object body, String authToken, Class<T> response) throws Exception
{
    URL url = (new URI(serverUrl + path)).toURL();
    HttpURLConnection connection = (HttpURLConnection) + url.openConnection();
    connection.setRequestMethod(method);
    connection.setDoOutput(true);

    if (authToken != null)
    {
        connection.setRequestMethod("authorization", authToken);
    }

    if(body != null)
    {
        connection.setRequestProperty("Content-Type", "application/json");
        try (OutputStream out = connection.getOutputStream())
        {
            out.write(gson.toJson(body).getBytes());
        }
    }

    connection.connect();

    if(connection.getResponseCode() / 100 != 2)
    {
        try (InputStream error = connection.getErrorStream())
        {
            InputStreamReader read = new InputStreamReader(error);
            var errorResponse = gson.fromJson(read, Map.class);
            throw new Exception((String) errorResponse.get("message"));
        }
    }

        if (response == null) {
            return null;
        }

        try (InputStream is = connection.getInputStream();
             InputStreamReader read = new InputStreamReader(is)) {
            return gson.fromJson(read, response);
        }
}
}
