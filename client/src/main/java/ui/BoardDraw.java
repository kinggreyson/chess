package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class BoardDraw {

    //Board Color
    private static final String lightSquare = SET_BG_COLOR_CREAM;
    private static final String darkSquare = SET_BG_COLOR_DARK_GREEN;
    private static final String border = SET_BG_COLOR_DARK_GREY;

    //Piece Colors
    private static final String white = SET_TEXT_COLOR_WHITE + SET_TEXT_BOLD;
    private static final String black = SET_TEXT_COLOR_BLACK + SET_TEXT_BOLD;

    //Setup Board
    public static void board(ChessBoard board, boolean isWhitePerspective)
    {
        StringBuilder build = new StringBuilder();
        build.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("/n");

        if(isWhitePerspective)
        {
            whiteBoard(build, board);
        }
        else
        {
            blackBoard(build, board);
        }
        build.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("/n");
        System.out.print(build);
    }

    private static void whiteBoard(StringBuilder build, ChessBoard board) //perspective from white
    {
        columnLabel(build, true);
        for (int row = 8; row >= 1; row--)
        {
            rowBuild(build, board, row, false);
        }
    }

    private static void blackBoard(StringBuilder build, ChessBoard board) //perspective from black
    {

    }

    private static void columnLabel(StringBuilder build, boolean white)
    {

    }

    private static void rowBuild(StringBuilder build, ChessBoard board, int row, boolean white)
    {

    }



}
