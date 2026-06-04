package client;

import model.AuthData;
import static ui.EscapeSequences.*;


public class Prelogin {
    private final ServerFacade server;
    private final Repl repl;

    public Prelogin(ServerFacade server, Repl repl)
    {
        this.server = server;
        this.repl = repl;
    }

    public void options(String userInput, String[] list) throws Exception
    {
        if (userInput.equals("help"))
        {
            help();
        }
        else if (userInput.equals("login"))
        {
            login(list);
        }
        else if (userInput.equals("register"))
        {
            register(list);
        }

        else
        {
            System.out.println(SET_TEXT_COLOR_RED + "Unknown entry: type 'help' for a list of available options" + RESET_TEXT_COLOR);
        }
    }

    private void help()
    {
        System.out.println(SET_TEXT_COLOR_GREEN + SET_TEXT_BOLD + "\n Available Options" + RESET_TEXT_BOLD_FAINT);
        System.out.println(SET_TEXT_COLOR_WHITE + "\n help"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Shows the help menu");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n login (username) + (password)"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Login to your account");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n register (username) + (password)"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Register your new account");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n quit"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Exit the game" + RESET_TEXT_COLOR);
    }

    private void login(String[] list) throws Exception
    {
        if (list.length < 3) //Check if user entered in both username and password fields
        {
            System.out.println(SET_TEXT_COLOR_RED + "Error: input username and password fields"
            + RESET_TEXT_COLOR);
            return;
        }
        String username = list[1];
        String password = list[2];

        AuthData auth = server.login(username, password);
        repl.loginSet(auth.username(), auth.authToken());
        System.out.println(SET_TEXT_COLOR_GREEN +
                "Welcome, ♜ {" + username + "} ♖" + RESET_TEXT_COLOR);
    }

    private void register(String[] list) throws Exception
    {
        if (list.length < 4)
        {
            System.out.println(SET_TEXT_COLOR_RED + "Error: input username, password, and email fields"
                    + RESET_TEXT_COLOR);
            return;
        }
        String username = list[1];
        String password = list[2];
        String email = list[3];

        AuthData auth = server.register(username, password, email);
        repl.loginSet(auth.username(), auth.authToken());
        System.out.println(SET_TEXT_COLOR_GREEN + "Registered as " + username + RESET_TEXT_COLOR);
    }

}
