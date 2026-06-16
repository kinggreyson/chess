package client;

import chess.*;
import model.GameData;
import websocket.messages.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Game implements NotificationMenu {

    private final WebsocketFacade websocketFacade;
    private final int gameID;
    private final String authToken;
    private final String username;
    private final ChessGame.TeamColor color;
    private ChessGame game;
    private final Repl repl;
    private final Gson gson = new Gson();

    public Game(String authToken, int gameID, String username, ChessGame.TeamColor color,
                String url, Repl repl) throws Exception
    {
        this.websocketFacade = new WebsocketFacade(url, this);
        this.authToken = authToken;
        this.gameID = gameID;
        this.username = username;
        this.color = color;
        this.repl = repl;

        websocketFacade.connect(authToken, gameID);
    }

    //SERVER NOTIFICATIONS
    public void notification(ServerMessage serverMessage)
    {
        if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME)
        {
            LoadGameMessage loadGameMessage = (LoadGameMessage) serverMessage;
            GameData gameData = loadGameMessage.gameResult();
            game = gameData.game();
            boolean white = color != ChessGame.TeamColor.BLACK;
            ui.BoardDraw.board(game.getBoard(), white);
        }
        else if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION)
        {
            NotificationMessage notificationMessage = (NotificationMessage) serverMessage;
            System.out.println(SET_TEXT_COLOR_GREEN + notificationMessage.messageResult() + RESET_TEXT_COLOR );
        }
        else if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR)
        {
            ErrorMessage errorMessage = (ErrorMessage) serverMessage;
            System.out.println(SET_TEXT_COLOR_RED + errorMessage.errorResult() + RESET_TEXT_COLOR);
        }
    }

    //PLAYER INPUTS
    public void route(String userInput, String[] factors) throws Exception
    {
        if(userInput.equals("help"))
        {
            help();
        }
        else if(userInput.equals("redraw"))
        {
            redraw();
        }
        else if(userInput.equals("leave"))
        {
            leave();
        }
        else if(userInput.equals("move"))
        {
            move(factors);
        }
        else if(userInput.equals("resign"))
        {
            resign();
        }
        else if(userInput.equals("highlight"))
        {
            highlight(factors);
        }
        else
        {
            System.out.println(SET_TEXT_COLOR_RED + "Unavailable Request: Type 'help' to get list of inputs");
            return;
        }
    }

    private void help()
    {
        System.out.println(SET_TEXT_COLOR_GREEN + SET_TEXT_BOLD + "\nAvailable Commands:" + RESET_TEXT_BOLD_FAINT);
        System.out.println(SET_TEXT_COLOR_WHITE + "\n help"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Shows the help menu");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n redraw"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Redraws the board");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n leave"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Leaves the game");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n resign"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Surrenders to opponent");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n move <letter> <number>"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Moves the piece to indicated row/column");
        System.out.println(SET_TEXT_COLOR_WHITE + "\n highlight <letter> <number>"
                + SET_TEXT_COLOR_LIGHT_GREY + " - Highlights the square based on row/column");
    }

    private void redraw()
    {
        if (game == null)
        {
            System.out.println(SET_TEXT_COLOR_RED + "Game isn't loaded" + RESET_TEXT_COLOR);
            return;
        }
        boolean white = color != ChessGame.TeamColor.BLACK;
        ui.BoardDraw.board(game.getBoard(), white);
    }

    private void leave() throws IOException
    {
        websocketFacade.leave(authToken, gameID);
        repl.leaveGame();
    }

    private void move(String[] factors) throws IOException
    {
        if (factors.length < 3)
        {
            System.out.println(SET_TEXT_COLOR_RED +
                    "Error: type 'move <row/col> <row/col>' (start position/end position)" );
            return;
        }

        String start = factors[1];
        String end = factors[2];

        String colStart = String.valueOf(start.charAt(0));
        String rowStart = String.valueOf(start.charAt(1));
        String colEnd = String.valueOf(end.charAt(0));
        String rowEnd = String.valueOf(end.charAt(1));

        ChessPosition startSpot = colRow(colStart, rowStart);
        ChessPosition endSpot = colRow(colEnd, rowEnd);

        ChessPiece.PieceType promotion = null;
        if (factors.length == 4)
        {
            promotion = promote(factors[3]);
        }

        ChessMove chessMove = new ChessMove(startSpot, endSpot, promotion);
        websocketFacade.move(authToken, gameID, chessMove);
    }

    private void resign() throws IOException
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Confirm resign, 'yes' or 'no'");
        String confirmation = scanner.nextLine();
        if (confirmation.equals("yes"))
        {
            websocketFacade.resign(authToken, gameID);
            System.out.println(SET_TEXT_COLOR_GREEN + "Game resigned - you'll win the next one"
                    + RESET_TEXT_COLOR);

        }
        else if(confirmation.equals("no"))
        {
            System.out.println(SET_TEXT_COLOR_GREEN + "Back to the game" + RESET_TEXT_COLOR);
        }
        else
        {
            System.out.println(SET_TEXT_COLOR_RED + "Error : invalid input type resign [enter], " +
                    "then 'yes' or 'no' " + RESET_TEXT_COLOR);
        }
    }

    public void highlight(String[] factors)
    {
        if (factors.length < 2)
        {
            System.out.println(SET_TEXT_COLOR_RED +
                    "Error: type 'highlight <row/col>" );
            return;
        }

        String highlightPos = factors[1];

        String highlightCol = String.valueOf(highlightPos.charAt(0));
        String highlightRow = String.valueOf(highlightPos.charAt(1));

        ChessPosition highlightSquare = colRow(highlightCol, highlightRow);
        var validMoves = game.validMoves(highlightSquare);

        boolean colorSelect = color != ChessGame.TeamColor.BLACK;

        ui.BoardDraw.highlightBoard(game.getBoard(), colorSelect, validMoves, highlightSquare);
    }

    private ChessPosition colRow(String colLetter, String rowNumber)
    {
        int row;
        int col;
        //Convert letter input to number
        if (colLetter.equalsIgnoreCase("a")) {col = 1;}
        else if (colLetter.equalsIgnoreCase("b")) {col = 2;}
        else if (colLetter.equalsIgnoreCase("c")) {col = 3;}
        else if (colLetter.equalsIgnoreCase("d")) {col = 4;}
        else if (colLetter.equalsIgnoreCase("e")) {col = 5;}
        else if (colLetter.equalsIgnoreCase("f")) {col = 6;}
        else if (colLetter.equalsIgnoreCase("g")) {col = 7;}
        else {col = 8;}
        //convert number string to int
        if (rowNumber.equals("1")) {row = 1;}
        else if (rowNumber.equals("2")) {row = 2;}
        else if (rowNumber.equals("3")) {row = 3;}
        else if (rowNumber.equals("4")) {row = 4;}
        else if (rowNumber.equals("5")) {row = 5;}
        else if (rowNumber.equals("6")) {row = 6;}
        else if (rowNumber.equals("7")) {row = 7;}
        else {row = 8;}

        return new ChessPosition(row,col);
    }

    private ChessPiece.PieceType promote(String piece)
    {

        if (piece.equals("queen")) {return ChessPiece.PieceType.QUEEN;}
        else if (piece.equals("bishop")) {return ChessPiece.PieceType.BISHOP;}
        else if (piece.equals("rook")) {return ChessPiece.PieceType.ROOK;}
        else if (piece.equals("knight")) {return ChessPiece.PieceType.KNIGHT;}
        else {
            System.out.println(SET_TEXT_COLOR_RED + "Can't promote" + RESET_TEXT_COLOR);
            return ChessPiece.PieceType.PAWN;
        }
    }



}
