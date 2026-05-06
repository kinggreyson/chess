package chess;
import java.util.Collection;
import java.util.ArrayList;

public interface PieceMovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
