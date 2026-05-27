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

    //TODO Create tables
    //TODO create configureDatabase() function
}
