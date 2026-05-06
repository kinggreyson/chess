package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ChessPiece Pawn = board.getPiece(position);
        boolean White = Pawn.getTeamColor() == ChessGame.TeamColor.WHITE;
        int Forward;
        int Start;
        int Promotion;
        if(White) //Color/Chessboard position check
        {
           Forward = 1;
           Start = 2;
           Promotion = 8;
        }
        else
        {
            Forward = -1;
            Start = 7;
            Promotion = 1;
        }
            int newmove = position.getRow() + Forward;
            int column = position.getColumn();
            if (newmove >= 1 && newmove <= 8) {
                ChessPosition forwardPosition = new ChessPosition(newmove, column); //establish new position
                ChessPiece target = board.getPiece(forwardPosition); //Check for other pieces
                if (target == null) //other piece in square
                {
                    PromoteCheck(position, forwardPosition, Promotion, possibleMoves);
                    if (position.getRow() == Start) {
                        ChessPosition twoForward = new ChessPosition(newmove + Forward, column);
                        if (board.getPiece(twoForward) == null) {
                            possibleMoves.add(new ChessMove(position, twoForward, null)); //no promotion off start
                        }
                    }
                }
                int[][] captures = {{Forward, 1}, {Forward, -1}};
                for (int[] capture : captures)
                {
                    int capRow = position.getRow() + capture[0];
                    int capCol = position.getColumn() + capture[1];
                    if(capRow >= 1 && capRow <= 8 && capCol >=1 && capCol <=8)
                    {
                    ChessPosition cap = new ChessPosition(capRow, capCol);
                    ChessPiece enemy = board.getPiece(cap);
                    if (enemy != null && enemy.getTeamColor() != Pawn.getTeamColor())
                    {
                        PromoteCheck(position, cap, Promotion, possibleMoves);
                    }
                    }
            }

        }
        return possibleMoves;
    }
    private void PromoteCheck(ChessPosition from, ChessPosition to, int promotionRow, Collection<ChessMove> moves) {
    if (to.getRow() == promotionRow)
    {
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.KNIGHT));
    }
    else {
        moves.add(new ChessMove(from, to, null));
    }
    }

}


