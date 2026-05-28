package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MySQLDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    public MySQLDataAccess() throws DataAccessException
    {
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(256) NOT NULL,
                password VARCHAR(256) NOT NULL,
                email VARCHAR(256) NOT NULL,
                PRIMARY KEY (username)
                )
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(256) NOT NULL,
                username VARCHAR(256) NOT NULL,
                PRIMARY KEY (authToken)
                )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(256),
                blackUsername VARCHAR(256),
                gameName VARCHAR(256) NOT NULL,
                game TEXT NOT NULL,
                PRIMARY KEY (gameID)
                )
            """
    };

    private void configureDatabase() throws DataAccessException
    {
        DatabaseManager.createDatabase();
        try (var connection = DatabaseManager.getConnection())
        {
            for (var table : createStatements)
            {
                try (var preparedStatement = connection.prepareStatement(table))
                {
                    preparedStatement.executeUpdate();
                }
            }
        }
        catch (Exception a)
        {
            throw new DataAccessException("Unable to configure database: " + a.getMessage());
        }
    }

    //Clear Data
    @Override
    public void clear() throws DataAccessException
    {
        try (var connection = DatabaseManager.getConnection())
        {
            try (var statement = connection.prepareStatement("DELETE FROM users"))
            {
                statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("DELETE FROM auth"))
            {
                statement.executeUpdate();
            }
            try (var statement = connection.prepareStatement("DELETE FROM games")) {
                statement.executeUpdate();
            }
        }
        catch (SQLException a)
        {
            throw new DataAccessException("Unable to clear database: " + a.getMessage());
        }
    }

    //USER DATA
    @Override
    public void createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setString(1, user.username());
            state.setString(2, hashedPassword);
            state.setString(3, user.email());
            state.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password, email FROM users WHERE username = ?";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setString(1, username);
            try (ResultSet rs = state.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get user: " + e.getMessage());
        }
        return null;
    }

    //Auth functions
    @Override
    public void createAuth(AuthData auth) throws DataAccessException
    {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement))
        {
            state.setString(1, auth.authToken());
            state.setString(2, auth.username());
            state.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new DataAccessException("Unable to create auth" + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setString(1, authToken);
            try (ResultSet rs = state.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("authToken"), rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get auth: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken = ?";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setString(1, authToken);
            state.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to delete auth: " + e.getMessage());
        }
    }
    //Games
    @Override
    public void createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        String gameJson = gson.toJson(game.game());
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            state.setString(1, game.whiteUsername());
            state.setString(2, game.blackUsername());
            state.setString(3, game.gameName());
            state.setString(4, gameJson);
            state.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID = ?";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setInt(1, gameID);
            try (ResultSet rs = state.executeQuery()) {
                if (rs.next()) {
                    return readGame(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement);
             ResultSet rs = state.executeQuery()) {
            while (rs.next()) {
                games.add(readGame(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        String gameJson = gson.toJson(game.game());
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setString(1, game.whiteUsername());
            state.setString(2, game.blackUsername());
            state.setString(3, game.gameName());
            state.setString(4, gameJson);
            state.setInt(5, game.gameID());
            state.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game: " + e.getMessage());
        }
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");
        ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
        }
}
