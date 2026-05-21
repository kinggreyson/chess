package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 1}, {1, 0}, {-1, 0}, {0, -1}};
        return PieceMovesCalculator.singleStepMoves(board, position, directions);
    }
}