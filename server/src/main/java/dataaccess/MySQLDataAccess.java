package dataaccess;

import com.google.gson.Gson;

import java.sql.SQLException;

//SQL Imports


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

    //TODO users
    //TODO auth
    //TODO games
}
