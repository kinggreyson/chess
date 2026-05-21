package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(position);
        boolean isWhite = pawn.getTeamColor() == ChessGame.TeamColor.WHITE;
        int forward;
        int start;
        int promotion;

        if (isWhite) {
            forward = 1;
            start = 2;
            promotion = 8;
        } else {
            forward = -1;
            start = 7;
            promotion = 1;
        }

        int newMove = position.getRow() + forward;
        int column = position.getColumn();

        if (newMove >= 1 && newMove <= 8) {
            ChessPosition forwardPosition = new ChessPosition(newMove, column);
            ChessPiece target = board.getPiece(forwardPosition);

            if (target == null) {
                promoteCheck(position, forwardPosition, promotion, possibleMoves);
                if (position.getRow() == start) {
                    ChessPosition twoForward = new ChessPosition(newMove + forward, column);
                    if (board.getPiece(twoForward) == null) {
                        possibleMoves.add(new ChessMove(position, twoForward, null));
                    }
                }
            }

            int[][] captures = {{forward, 1}, {forward, -1}};
            for (int[] capture : captures) {
                int capRow = position.getRow() + capture[0];
                int capCol = position.getColumn() + capture[1];
                if (capRow >= 1 && capRow <= 8 && capCol >= 1 && capCol <= 8) {
                    ChessPosition cap = new ChessPosition(capRow, capCol);
                    ChessPiece enemy = board.getPiece(cap);
                    if (enemy != null && enemy.getTeamColor() != pawn.getTeamColor()) {
                        promoteCheck(position, cap, promotion, possibleMoves);
                    }
                }
            }
        }
        return possibleMoves;
    }

    private void promoteCheck(ChessPosition from, ChessPosition to, int promotionRow, Collection<ChessMove> moves) {
        if (to.getRow() == promotionRow) {
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(from, to, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}