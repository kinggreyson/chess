package service;
import dataaccess.BadRequestException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.UUID;

public class UserService
{
    private final DataAccess userData;

    public UserService(DataAccess dataAccess)
    {
        this.userData = dataAccess;
    }

    public record RegisterRequest(String username, String password, String email)
    {}
    public record RegisterResult(String username, String authToken)
    {}

    public record LoginRequest(String username, String password)
    {}
    public record LoginResult(String username, String authToken)
    {}

    public RegisterResult register(RegisterRequest req) throws DataAccessException
    {
        // 400 Check - Empty username, password, or email
        if (req.username == null || req.password == null || req.email == null)
        {
            throw new DataAccessException("Error: bad request");
        }

        //403 Check - Username already taken
        if (userData.getUser(req.username) != null)
        {
            throw new DataAccessException(("Error: already taken"));
        }

        UserData user = new UserData(req.username(), req.password(), req.email());
        userData.createUser(user);
        String token = UUID.randomUUID().toString(); //Create randomized token
        AuthData auth = new AuthData(token, req.username());
        userData.createAuth(auth); //authToken

        return new RegisterResult(req.username(), token);
    }

    public LoginResult login(UserService.LoginRequest req) throws DataAccessException
    {
        //400 Check - Empty Username or password
        if (req.username == null || req.password == null)
        {
            throw new BadRequestException("Error: bad request");
        }

        UserData user = userData.getUser(req.username());

        //401 Check - Incorrect username or password
        if (user == null || !user.password().equals(req.password()))
        {
            throw new UnauthorizedException("Error: unauthorized");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, req.username());
        userData.createAuth(auth);

        return new LoginResult(req.username(), token);
    }

    public void logout(String authToken) throws DataAccessException
    {
        //401 - AuthToken
        if (userData.getAuth(authToken) == null)
        {
            throw new UnauthorizedException("Error: unauthorized");
        }
        //delete authToken
        userData.deleteAuth(authToken);
    }
}


