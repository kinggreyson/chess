package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
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
                gameOver BOOLEAN DEFAULT FALSE,
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
        update("DELETE FROM users");
        update("DELETE FROM auth");
        update("DELETE FROM games");
    }

    //USER DATA
    @Override
    public void createUser(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        update("INSERT INTO users (username, password, email) VALUES (?, ?, ?)",
                user.username(), hashedPassword, user.email()); //update handles states
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
        update("INSERT INTO auth (authToken, username) VALUES (?, ?)", auth, username());
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
        update("DELETE FROM auth WHERE authToken = ?");
    }

    //Games
    @Override
    public int createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game, gameOver) VALUES (?, ?, ?, ?,?)";
        String gameJson = gson.toJson(game.game());
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            state.setString(1, game.whiteUsername());
            state.setString(2, game.blackUsername());
            state.setString(3, game.gameName());
            state.setString(4, gameJson);
            state.setBoolean(5, game.gameOver());
            state.executeUpdate();
            try (ResultSet keys = state.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game, gameOver FROM games WHERE gameID = ?";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setInt(1, gameID);
            try (ResultSet rs = state.executeQuery()) {
                if (rs.next()) {
                    return readGame(rs);
                }
            }
        } catch (SQLException error) {
            throw new DataAccessException("Unable to get game: " + error.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game, gameOver FROM games ORDER BY gameID";
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement);
             ResultSet rs = state.executeQuery()) {
            while (rs.next()) {
                games.add(readGame(rs));
            }
        } catch (SQLException error) {
            throw new DataAccessException("Unable to list games: " + error.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ?, gameOver = ? WHERE gameID = ?";
        String gameJson = gson.toJson(game.game());
        try (var connection = DatabaseManager.getConnection();
             var state = connection.prepareStatement(statement)) {
            state.setString(1, game.whiteUsername());
            state.setString(2, game.blackUsername());
            state.setString(3, game.gameName());
            state.setString(4, gameJson);
            state.setBoolean(5, game.gameOver());
            state.setInt(6, game.gameID());
            state.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game: " + e.getMessage());
        }
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        boolean gameOver = rs.getBoolean("gameOver");
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");
        ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game, gameOver);
        }

    //Helper function to remove repetitiveness of each function that don't return data
    private void update(String newStatement, Object... parameter) throws DataAccessException
    {
        try (var connection = DatabaseManager.getConnection();
            var state = connection.prepareStatement(newStatement))
        {
            parameters(state, parameter);
            state.executeUpdate();
        } catch (SQLException error)
        {
            throw new DataAccessException(error.getMessage());
        }

    }

    //send as basic object set as correct data type
    private void parameters(PreparedStatement state, Object ... parameter) throws SQLException
    {
        for (int i = 0; i < parameter.length; i++)
        {
            if (parameter[i] instanceof String string) //Check if String
            {
                state.setString(i+1, string);
            }
            else if (parameter[i] instanceof Integer integer) //Check if Int
            {
                state.setInt(i+1, integer);
            }
            else if (parameter[i] instanceof Boolean bool) //Check if Bool
            {
                state.setBoolean(i + 1, bool);
            }
        }
    }
}
