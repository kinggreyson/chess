package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

//SQL Imports
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

    private final String[] createTables = {
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
            for (var table : createTables)
            {
                try (var preparedTable = connection.prepareStatement(table))
                {
                    preparedTable.executeUpdate();
                }
                catch (Exception e)
                {
                    throw new DataAccessException("Unable to configure database: " + e.getMessage());
                }
            }
        }
    }
}
