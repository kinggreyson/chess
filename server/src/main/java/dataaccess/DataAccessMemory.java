package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.awt.font.GlyphMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAccessMemory implements DataAccess{
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear()
    {
        //Clear each datatype
        users.clear();
        games.clear();
        auths.clear();
    }

    //USER SECTION
    @Override
    public void createUser(UserData user) throws DataAccessException
    {
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException
    {
        return users.get(username);
    }

    //GAME SECTION
    @Override
    public void createGame(GameData game) throws DataAccessException
    {
        games.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException
    {
        return games.get(gameID);
    }

    @Override
    public List<GameData>listGames() throws DataAccessException
    {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException
    {
        if (!games.containsKey(game.gameID())) //If gameID doesn't exist
        {
            throw new DataAccessException("Game not found");
        }
        games.put(game.gameID(), game);
    }

    //AUTH SECTION
    public void createAuth(AuthData auth) throws DataAccessException
    {
        auths.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) throws DataAccessException
    {
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) throws DataAccessException
    {
        auths.remove(authToken);
    }
}
