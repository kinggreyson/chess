package chess;

import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
       PieceMovesCalculator Moves = null;
       if (type == PieceType.ROOK){
            Moves = new RookMovesCalculator();
       }
        if (type == PieceType.KNIGHT){
            Moves = new RookMovesCalculator();
        }
        if (type == PieceType.BISHOP){
            Moves = new RookMovesCalculator();
        }
        if (type == PieceType.QUEEN){
            Moves = new QueenMovesCalculator();
        }
        if (type == PieceType.KING){
            Moves = new RookMovesCalculator();
        }
        /*else
        {
            Moves = new PawnMovesCalculator();
        }*/
        return Moves.pieceMoves(board, myPosition);
    }
}
