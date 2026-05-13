package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard(); //initialize board/starting white team
        this.board.resetBoard();
        this.turn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team; //Set team
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) //if piece doesn't exist
        {
            return null;
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition); //pull every move
        Collection<ChessMove> possible = new ArrayList<>(); //Figure out the possible moves

        for (ChessMove move : moves)
        {
            //Cant leave king in check
            // if move doesn't put king in check... Add it to possible list
        }
        return possible;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingLocation = getKing(teamColor);
        if (kingLocation == null)
        {
            return false;
        }

        TeamColor opposite = ((teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE); //Figure out opposite color

        for (int i = 1; i<=8; i++) //Check every spot on board
        {
            for (int j = 1; j<=8;j++)
            {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece1 = board.getPiece(pos);
                if (piece1 != null && piece1.getTeamColor() == opposite)
                {
                    Collection<ChessMove> moves = piece1.pieceMoves(board, pos);
                    for (ChessMove move : moves)
                    {
                        if(move.getEndPosition().equals(kingLocation)); //Checks every opponent end position in relation to kings position
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if(!isInCheck(teamColor))
        {
            return false;
        }
        return true;
        //Need a function to determine if any moves are left...
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor))
        {
            return false;
        }
        return true;
        //Need a function to determine if any moves are left...
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
       return board;
    }

    //CREATE getKing function to figure out kings position
    //CREATE a noMoves function
}
