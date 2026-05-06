package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        int[][] directions = {{2, 1}, {1, 2}, {2, -1}, {1, -2}, {-1, 2}, {-2, 1}, {-1, -2}, {-2, -1}};

        for (int[] direction : directions) { //Divide into each direction
            int row_dir = direction[0];
            int col_dir = direction[1];
            int currentRow = position.row + row_dir;
            int currentCol = position.col + col_dir;
            if (currentRow >= 1 && currentRow <= 8 && currentCol >= 1 && currentCol <= 8) {
                ChessPosition newPosition = new ChessPosition(currentRow, currentCol); //establish new position
                ChessPiece target = board.getPiece(newPosition); //Check for other pieces
                if (target != null) //other piece in square
                {
                    if (target.getTeamColor() != board.getPiece(position).getTeamColor()) //if piece is opposite color
                    {
                        possibleMoves.add(new ChessMove(position, newPosition, null));
                    }
                } else {
                    possibleMoves.add(new ChessMove(position, newPosition, null));
                }

                currentRow += row_dir;
                currentCol += col_dir;
            }

        }
        return possibleMoves;
    }

}

