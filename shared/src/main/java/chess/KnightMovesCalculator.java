package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[][] directions = {{2, 1}, {1, 2}, {2, -1}, {1, -2}, {-1, 2}, {-2, 1}, {-1, -2}, {-2, -1}};
        return PieceMovesCalculator.singleStepMoves(board, position, directions);
    }
}