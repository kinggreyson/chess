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
import java.util.Map;


public class ServerFacade {

private final String serverUrl;
private final Gson gson = new Gson();

public ServerFacade(int port)
{
    this.serverUrl = "http://localhost:" + port;
}

public AuthData register(String username, String password, String email) throws Exception
{
    var makeup = Map.of("username", username, "password", password, "email", email);
    return request("POST", "/user", makeup, null, AuthData.class); //Register no authToken needed
}

public AuthData login(String username, String password) throws Exception
{
    var makeup = Map.of("username", username, "password", password);
    return request("POST", "/session", makeup, null, AuthData.class); //login no authToken needed
}

public void logout(String authToken) throws Exception
{
    request("DELETE", "/session", null, authToken, null); //Delete login, no Body/response needed
}

public int createGame(String authToken, String gameName) throws Exception
{
    var makeup = Map.of("gameName", gameName);
    var endpoint = request("POST", "/game", makeup, authToken, Map.class);
    int gameID = (int)(double) endpoint.get("gameID"); //convert gameID to double from object for JSON, then convert to int
    return (int) gameID;
}

public GameData[] listGames(String authToken) throws Exception
{
    var endpoint = request("GET", "/game", null, authToken, Map.class);
    var jsonGame = gson.toJson(endpoint.get("games")); //convert to json
    return gson.fromJson(jsonGame, GameData[].class);
}

public void joinGame(String authToken, int gameID, String playerColor) throws Exception
{
    var makeup = Map.of("gameID", gameID, "playerColor", playerColor); //ID and Color
    request("PUT", "/game", makeup, authToken, null);
}

public void clear() throws Exception
{
    request("DELETE", "/db", null, null, null); //Clear Database
}

private <T> T request(String method, String path, Object body, String authToken, Class<T> response) throws Exception
{
    URL url = (new URI(serverUrl + path)).toURL();
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setDoOutput(true);

    if (authToken != null)
    {
        connection.setRequestProperty("authorization", authToken); //logout, createGame, listGames, joinGame
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
