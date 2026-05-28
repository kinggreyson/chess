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
                authtoken VARCHAR(256) NOT NULL,
                username VARCHAR(256) NOT NULL,
                PRIMARY KEY (authtoken)
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
                catch (Exception a)
                {
                    throw new DataAccessException("Unable to configure database: " + a.getMessage());
                }
            }
        }
    }

    //Clear Data
    @Override
    public void Clear() throws DataAccessException
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
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(statement)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get user: " + e.getMessage());
        }
        return null;
    }


    //TODO users
    //TODO auth
    //TODO games
}
