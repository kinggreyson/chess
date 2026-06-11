package client;

import chess.ChessBoard;
import model.GameData;

import static ui.EscapeSequences.*;

public class Postlogin {
    private final ServerFacade server;
    private final Repl repl;
    private final String username;
    private final String authToken;
    private GameData[] games;

    public Postlogin(ServerFacade server, Repl repl, String username, String authToken) {
        this.server = server;
        this.repl = repl;
        this.username = username;
        this.authToken = authToken;
    }

    public String getUsername()
    {
        return username;
    }

    public void options(String userInput, String[] list) throws Exception
    {
        if (userInput.equals("help"))
        {
            help();
        }
        else if (userInput.equals("logout"))
        {
            logout();
        }
        else if (userInput.equals("create"))
        {
            createGame(list);
        }
        else if (userInput.equals("list"))
        {
            listGames();
        }
        else if (userInput.equals("play"))
        {
            playGame(list);
        }
        else if (userInput.equals("observe"))
        {
            observeGame(list);
        }
        else if (userInput.equals("quit")) {
            System.out.println(SET_TEXT_COLOR_GREEN + "♗ Thanks for playing! ♞");
            System.exit(0);
        }
        else
        {
            System.out.println(SET_TEXT_COLOR_RED + "Unknown entry: type 'help' for a list of available options" + RESET_TEXT_COLOR);
        }
    }

    private void help()
    {
        System.out.println(SET_TEXT_COLOR_GREEN + SET_TEXT_BOLD + "\nAvailable Commands:" + RESET_TEXT_BOLD_FAINT);
        System.out.println(SET_TEXT_COLOR_WHITE + "\n help"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Shows the help menu");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n logout"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Logout of your account");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n create <game name>"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Create a new game");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n list"
                + SET_TEXT_COLOR_LIGHT_GREY + " - List all available games");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n play <game number> <WHITE|BLACK>"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Join a game as a player");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n observe <game number>"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Observe a game");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n quit"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Exit the game" + RESET_TEXT_COLOR);
    }

    private void logout() throws Exception
    {
        server.logout(authToken);
        repl.logout();
        System.out.println(SET_TEXT_COLOR_GREEN + "Logged out" + RESET_TEXT_COLOR);

    }

    private void createGame(String[] list) throws Exception
    {
        if (list.length < 2)
        {
            System.out.println(SET_TEXT_COLOR_RED + "Error: input <game name>"
                    + RESET_TEXT_COLOR);
            return;
        }
        StringBuilder gameName = new StringBuilder();

        for (int i = 1; i < list.length; i++) //Gets rid of white spaces in gameName
        {
            if (i > 1)
            {
                gameName.append(" ");
            }
            gameName.append(list[i]);
        }

        int gameID = server.createGame(authToken, gameName.toString());
        System.out.println(SET_TEXT_COLOR_GREEN + "Game Created: " +gameName + RESET_TEXT_COLOR);
    }

    private void listGames() throws Exception
    {
        games = server.listGames(authToken);
        if (games.length == 0) //no available games
        {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "No games ongoing" + RESET_TEXT_COLOR);
            return;
        }
        System.out.println(SET_TEXT_COLOR_GREEN + SET_TEXT_BOLD + "\n LIST OF AVAILABLE GAMES" + RESET_TEXT_COLOR);

        for (int i = 0; i < games.length; i++) //iterate through list of games
        {
            GameData game = games[i];
            String white = game.whiteUsername() != null ? game.whiteUsername() : "Available";
            String black = game.blackUsername() != null ? game.blackUsername() : "Available";
            System.out.println(SET_TEXT_COLOR_WHITE + "  " + (i + 1) + ". " + game.gameName()
                    + SET_TEXT_COLOR_LIGHT_GREY + " | [White: " + white + " | Black: " + black + "]"
                    + RESET_TEXT_COLOR);
        }

    }

    private void playGame(String[] list) throws Exception
    {
        if (list.length < 3)
        {
            System.out.println(SET_TEXT_COLOR_RED + "Error: input <game number> <WHITE|BLACK>" + RESET_TEXT_COLOR);
            return;
        }

        if (games == null)
        {
            System.out.println(SET_TEXT_COLOR_RED + "Game not found, try list games" + RESET_TEXT_COLOR);
            return;
        }

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(list[1]); //Convert to int
        } catch (NumberFormatException exception) {
            System.out.println(SET_TEXT_COLOR_RED + "Invalid game number." + RESET_TEXT_COLOR);
            return;
        }

        if (gameNumber < 1 || gameNumber > games.length) {
            System.out.println(SET_TEXT_COLOR_RED + "Game number out of range." + RESET_TEXT_COLOR);
            return;
        }

        String color = list[2].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK"))  //Outside Color bounds
        {
            System.out.println(SET_TEXT_COLOR_RED + "Invalid color. Choose WHITE or BLACK." + RESET_TEXT_COLOR);
            return;
        }

        GameData game = games[gameNumber - 1];
        server.joinGame(authToken, game.gameID(), color);
        System.out.println(SET_TEXT_COLOR_GREEN + "Joined as " + color + RESET_TEXT_COLOR);

        ChessBoard board = new ChessBoard();
        board.resetBoard();
        boolean isWhite = color.equals("WHITE");
        ui.BoardDraw.board(board, isWhite);
    }

    private void observeGame(String[] tokens) throws Exception {
        if (tokens.length < 2) {
            System.out.println(SET_TEXT_COLOR_RED + "Error: input observe <Game Number>" + RESET_TEXT_COLOR);
            return;
        }
        if (games == null) {
            System.out.println(SET_TEXT_COLOR_RED + "Game not found, try list games" + RESET_TEXT_COLOR);
            return;
        }

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(tokens[1]); //Int Conversion
        } catch (NumberFormatException exception) {
            System.out.println(SET_TEXT_COLOR_RED + "Invalid game number." + RESET_TEXT_COLOR);
            return;
        }

        if (gameNumber < 1 || gameNumber > games.length) {
            System.out.println(SET_TEXT_COLOR_RED + "Game number out of range." + RESET_TEXT_COLOR);
            return;
        }

        System.out.println(SET_TEXT_COLOR_GREEN + "Watching Game " + gameNumber + RESET_TEXT_COLOR);
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        ui.BoardDraw.board(board, true);
    }
}
