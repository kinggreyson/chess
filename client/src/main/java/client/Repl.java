package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ServerFacade server;
    private final Prelogin prelogin;
    private Postlogin postlogin;
    private boolean loggedIn = false;

    public Repl(int port)
    {
        this.server = new ServerFacade(port);
        this.prelogin = new Prelogin(server, this);
    }

    public void chessRun()
    {
        System.out.println(SET_TEXT_COLOR_GREEN + SET_TEXT_BOLD + "♔ CHESS ♛");
        System.out.println(RESET_TEXT_COLOR + RESET_TEXT_BOLD_FAINT + "Type 'help' to start");

        Scanner input = new Scanner(System.in);
        while (true)
        {
            printPrompt();
            String newLine = input.nextLine().trim();
            if (newLine.isEmpty())
            {
                continue;
            }
            String[] list = newLine.split("\\s+"); //Correct Spacing
            String userInput = list[0].toLowerCase(); //Convert strings to avoid confusion

            try
            {
                if (!loggedIn)
                {
                    if(userInput.equals("quit"))
                    {
                        System.out.println(SET_TEXT_COLOR_GREEN + "♗ Thanks for playing! ♞");
                        break;
                    }
                    prelogin.options(userInput, list); //Future Function in prelogin

                }
                else
                {
                    postlogin.options(userInput, list); //Future Function in postlogin
                }
            } catch (Exception error)
            {
                System.out.println(SET_TEXT_COLOR_RED + "Error: " + error.getMessage() + RESET_TEXT_COLOR);
            }
        }
    }

    public void loginSet(String username, String authToken)
    {
        this.loggedIn = true;
        this.postlogin = new Postlogin(server, this, username, authToken);
    }

    public void LogoutSet()
    {
        this.loggedIn = false;
        this.postlogin = null;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    private void printPrompt()
    {
        if (loggedIn && postlogin != null)
        {
            System.out.print(SET_TEXT_COLOR_GREEN + "♜ {" + postlogin.getUsername() + "} ♙ -->" + RESET_TEXT_COLOR);
        }
        else
        {
            System.out.print(SET_TEXT_COLOR_GREEN + "♟ {GUEST} ♖ -->" + RESET_TEXT_COLOR);
        }
    }


}
