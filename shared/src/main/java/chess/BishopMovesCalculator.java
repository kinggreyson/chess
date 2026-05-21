package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[][] directions = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
        return PieceMovesCalculator.slidingMoves(board, position, directions);
    }
}