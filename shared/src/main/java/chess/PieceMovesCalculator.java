package chess;
import java.util.Collection;
import java.util.ArrayList;

public interface PieceMovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);

    static Collection<ChessMove> slidingMoves(ChessBoard board, ChessPosition position, int[][] directions) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        for (int[] direction : directions) {
            int rowDir = direction[0];
            int colDir = direction[1];
            int currentRow = position.getRow() + rowDir;
            int currentCol = position.getColumn() + colDir;

            while (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
                ChessPiece target = board.getPiece(newPosition);

                if (target != null) {
                    if (target.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        possibleMoves.add(new ChessMove(position, newPosition, null));
                    }
                    break;
                }
                possibleMoves.add(new ChessMove(position, newPosition, null));
                currentRow += rowDir;
                currentCol += colDir;
            }
        }
        return possibleMoves;
    }

    static Collection<ChessMove> singleStepMoves(ChessBoard board, ChessPosition position, int[][] directions) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

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

