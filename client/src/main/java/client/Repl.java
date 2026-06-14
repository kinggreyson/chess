package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ServerFacade server;
    private final Prelogin prelogin;
    private Postlogin postlogin;
    private Game game;

    public Repl(int port)
    {
        this.server = new ServerFacade(port);
        this.prelogin = new Prelogin(server, this);
    }

    public void chessRun()
    {
        System.out.println(SET_TEXT_COLOR_GREEN + SET_TEXT_BOLD + "♔ CHESS ♛");
        System.out.println(RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT + "WELCOME TO GET STARTED TYPE 'help'");

        Scanner input = new Scanner(System.in);
        while (true)
        {
            printPrompt();
            String newLine = input.nextLine().trim();
            if (newLine.isEmpty()) //Formatting
            {
                continue;
            }
            String[] list = newLine.split("\\s+"); //Correct Spacing
            String userInput = list[0].toLowerCase(); //Convert strings to avoid confusion

            try
            {
                if(inGame())
                {
                    game.route(userInput, list);
                }
                else if(isLoggedIn())
                {
                    postlogin.options(userInput, list); //Send to postlogin since user is logged in
                }
                else
                {
                    if (userInput.equals("quit"))
                    {
                        break;
                    }
                    prelogin.options(userInput, list);
                }
            } catch (Exception error) //User Error field
            {
                System.out.println(SET_TEXT_COLOR_RED + "Error: " + error.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    public void login(String username, String authToken)
    {
        this.postlogin = new Postlogin(server, this, username, authToken);
    }

    public void logout()
    {
        this.postlogin = null;
    }

    private boolean inGame()
    {
        return game != null;
    }

    private boolean isLoggedIn() //Login Check
    {
        return postlogin != null;
    }

    public void leaveGame()
    {
        this.game = null;
    }

    public void startGame(Game game) {
        this.game = game;
    }


    private void printPrompt() //separate prompt for active user/guest
    {
        if (inGame())
        {
            System.out.print(SET_TEXT_COLOR_GREEN + "♜ {" + postlogin.getUsername() + "} ♙ -->" + RESET_TEXT_COLOR);
        }
        else if (isLoggedIn())
        {
            System.out.print(SET_TEXT_COLOR_GREEN + "♜ {" + postlogin.getUsername() + "} ♙ -->" + RESET_TEXT_COLOR);
        }
        else
        {
            System.out.print(SET_TEXT_COLOR_GREEN + "♟ {GUEST} ♖ -->" + RESET_TEXT_COLOR);
        }
    }


}
