package ui;

import chess.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import static ui.EscapeSequences.*;

public class BoardDraw {

    //Board Color
    private static final String LIGHT_SQUARE = SET_BG_COLOR_CREAM;
    private static final String DARK_SQUARE = SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER  = SET_BG_COLOR_DARK_GREY;

    //Piece Colors
    private static final String WHITE_PIECE = SET_TEXT_COLOR_RED + SET_TEXT_BOLD;
    private static final String BLACK_PIECE = SET_TEXT_COLOR_BLUE + SET_TEXT_BOLD;

    //Setup Board
    public static void board(ChessBoard board, boolean isWhitePerspective)
    {
        StringBuilder build = new StringBuilder();
        build.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");

        if(isWhitePerspective)
        {
            whiteBoard(build, board);
        }
        else
        {
            blackBoard(build, board);
        }
        build.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");
        System.out.print(build);
    }

    private static void whiteBoard(StringBuilder build, ChessBoard board) //perspective from WHITE_PIECE
    {
        columnLabel(build, true);
        for (int row = 8; row >= 1; row--)
        {
            rowLabel(build, board, row, false);
        }
        columnLabel(build, true);
    }

    private static void blackBoard(StringBuilder build, ChessBoard board) //perspective from black
    {
        columnLabel(build, false);
        for (int row = 1; row <= 8; row++)
        {
            rowLabel(build, board, row, false);
        }
        columnLabel(build, false);
    }

    private static void columnLabel(StringBuilder build, boolean isWhite)
    {
        build.append(BORDER).append(SET_TEXT_COLOR_WHITE).append("   ");
        List<String> column = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H"));
        if( !isWhite)
        {
            Collections.reverse(column);
        }
        for (String col : column)
        {
            build.append(" ").append(col).append(" ");
        }
        build.append("   ").append(RESET_BG_COLOR).append("\n");
    }

    private static void rowLabel(StringBuilder build, ChessBoard board, int row, boolean iswhite)
    {
         build.append(BORDER).append(SET_TEXT_COLOR_WHITE).append(" ").append(row).append(" ");
         for (int i = 0; i < 8; i++)
         {
             int col = iswhite ? i + 1: 8 - i;
             boolean isCream = ((row + col) & 1) == 0;
             String colour = isCream ? LIGHT_SQUARE : DARK_SQUARE;
             build.append(colour);

             ChessPosition position = new ChessPosition(row, col);
             ChessPiece piece = board.getPiece(position);
             build.append(pieceType(piece));

         }
        build.append(BORDER).append(SET_TEXT_COLOR_WHITE).append(" ").append(row).append(" ");
         build.append(RESET_BG_COLOR).append("\n");
    }

    private static String pieceType(ChessPiece piece)
    {
        if (piece == null)
        {
            return EMPTY;
        }

        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String colour = isWhite ? WHITE_PIECE : BLACK_PIECE;
        String type;
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            type = isWhite ? WHITE_KING : BLACK_KING;
        } else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
            type = isWhite ? WHITE_QUEEN : BLACK_QUEEN;
        } else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
            type = isWhite ? WHITE_BISHOP : BLACK_BISHOP;
        } else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
            type = isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            type = isWhite ? WHITE_ROOK : BLACK_ROOK;
        } else {
            type = isWhite ? WHITE_PAWN : BLACK_PAWN;
        }
        return colour + type + RESET_TEXT_COLOR;
    }



}
