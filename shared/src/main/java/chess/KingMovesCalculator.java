package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 1}, {1, 0}, {-1, 0}, {0, -1}};

        for (int[] direction : directions) {
            int rowDir = direction[0];
            int colDir = direction[1];
            int currentRow = position.getRow() + rowDir;
            int currentCol = position.getColumn() + colDir;

            if (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece target = board.getPiece(newPosition);

                if (target != null) {
                    if (target.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        possibleMoves.add(new ChessMove(position, newPosition, null));
                    }
                } else {
                    possibleMoves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
        return possibleMoves;
    }
}