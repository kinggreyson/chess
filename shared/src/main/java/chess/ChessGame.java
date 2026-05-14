package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;
    private boolean WKingMove = false;
    private boolean BKingMove = false;
    private boolean WRook1Move = false;
    private boolean WRook8Move = false;
    private boolean BRook1Move = false;
    private boolean BRook8Move = false;
    private ChessMove LastMove = null;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

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

        for (ChessMove move : moves) {
            if (!KingInCheck(move, piece.getTeamColor()))
            {
                possible.add(move);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING)
        {
            for (ChessMove Castling : Castling(startPosition, piece.getTeamColor()))
            {
                if (!KingInCheck(Castling, piece.getTeamColor()))
                {
                    possible.add(Castling);
                }
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) //Check EnPassant
        {
            possible.addAll(EnPassant(startPosition, piece)); //Add each move through EnPassant function
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
        ChessPosition start = move.getStartPosition();
        ChessPiece piece1 = board.getPiece(start);

        if(piece1 == null) //Piece handler
        {
            throw new InvalidMoveException("No piece at position");
        }
        if(piece1.getTeamColor() != turn)//Turn handler
        {
            throw new InvalidMoveException("Not your turn");
        }
        Collection<ChessMove> possible= validMoves(start);
        if (possible == null || !possible.contains(move)) //Impossible Move handler
        {
            throw new InvalidMoveException("Impossible Move");
        }

        //Promotion Handler
        ChessPiece.PieceType type = move.getPromotionPiece() != null ? move.getPromotionPiece() : piece1.getPieceType();

        if (piece1.getPieceType() == ChessPiece.PieceType.KING)
        {
            if (piece1.getTeamColor() == TeamColor.WHITE)
            {
                WKingMove = true;
            }
            else
            {
                BKingMove = true;
            }
        }

        if (piece1.getPieceType() == ChessPiece.PieceType.ROOK)
        {
            if (start.equals(new ChessPosition(1,1)))
            {
                WRook1Move = true;
            }
            if (start.equals(new ChessPosition(1,8)))
            {
                WRook8Move = true;
            }
            if (start.equals(new ChessPosition(8,1)))
            {
                BRook1Move = true;
            }
            if (start.equals(new ChessPosition(8,8)))
            {
                BRook8Move = true;
            }
        }

        int RKMove = move.getEndPosition().getColumn() - start.getColumn();
        if (piece1.getPieceType() == ChessPiece.PieceType.KING && Math.abs(RKMove) == 2) // Get absolute value of difference of move
        {
            int row = start.getRow();
            if (RKMove == -2) //toward Rook1
            {
                board.addPiece(new ChessPosition(row, 4), new ChessPiece(piece1.getTeamColor(), ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(row, 1), null); // Switched Rook position to 4, leaving 1 empty
            }
            else //Toward Rook8
            {
                board.addPiece(new ChessPosition(row, 6), new ChessPiece(piece1.getTeamColor(), ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(row, 8), null); // Switched Rook position to 4, leaving 1 empty
            }
        }

        //Remove Pawn in EnPassant
        if (piece1.getPieceType() == ChessPiece.PieceType.PAWN
                && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()
                && board.getPiece(move.getEndPosition()) == null)
        {   //Move Pawn Column not row
            board.addPiece(new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn()), null);
        }

        board.addPiece(move.getEndPosition(), new ChessPiece(piece1.getTeamColor(), type));
        board.addPiece(start, null);

        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        LastMove = move;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingLocation = getKing(teamColor);
        if (kingLocation == null) {
            return false;
        }

        TeamColor opposite = ((teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE); //Figure out opposite color

        for (int i = 1; i <= 8; i++) //Check every spot on board
        {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece1 = board.getPiece(pos);
                if (piece1 != null && piece1.getTeamColor() == opposite) {
                    Collection<ChessMove> moves = piece1.pieceMoves(board, pos);
                    for (ChessMove move : moves) {  //Checks every opponent end position in relation to kings position
                        if (move.getEndPosition().equals(kingLocation))
                        {
                            return true;
                        }
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
        if (!isInCheck(teamColor)) {
            return false;
        }
        return noMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return noMoves(teamColor);

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

    //NEW FUNCTIONS
    private ChessPosition getKing(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) //Check every spot on board
        {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece1 = board.getPiece(pos);
                if (piece1 != null && piece1.getTeamColor() == teamColor && piece1.getPieceType() == ChessPiece.PieceType.KING) //Determines correct piece and color
                {
                    return pos; //Return King's current position
                }
            }
        }
        return null;
    }

    private boolean noMoves(TeamColor teamColor)
    {
        for (int i = 1; i <= 8; i++) //Check every spot on board
        {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece1 = board.getPiece(pos);
                if (piece1 != null && piece1.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (moves != null && !moves.isEmpty()) { //Checks if moves list isn't empty
                        return false;
                    }
                }

            }
        }
        return true;
    }

    private boolean KingInCheck(ChessMove move, TeamColor teamColor)
    {
        //simulate move on copied board
        ChessBoard copy =  Copy();
        ChessPiece piece1 = copy.getPiece(move.getStartPosition());

        ChessPiece.PieceType type = move.getPromotionPiece() != null ? move.getPromotionPiece() : piece1.getPieceType();

        copy.addPiece(move.getEndPosition(), new ChessPiece(piece1.getTeamColor(), type));
        copy.addPiece(move.getStartPosition(), null);

        ChessBoard OG = this.board;
        this.board = copy;
        boolean Check = isInCheck(teamColor);
        this.board = OG;

        return Check;
    }

    private ChessBoard Copy() //COPY the chessboard for simulation
    {
        ChessBoard copy = new ChessBoard();
        for (int i = 1; i <= 8; i++) //Check every spot on board
        {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece1 = board.getPiece(pos);
                if (piece1 != null)
                {
                    copy.addPiece(pos, new ChessPiece(piece1.getTeamColor(), piece1.getPieceType()));
                }
            }
        }
        return copy;
    }

    //Implement Castling and EnPassent helper functions here:

    private Collection<ChessMove> Castling(ChessPosition king, TeamColor color)
    {
        Collection<ChessMove> moves = new ArrayList<>();
        boolean kingMove = (color == TeamColor.WHITE) ? WKingMove : BKingMove;
        if (kingMove || isInCheck(color))
        {
            return moves;
        }
        int row = (color == TeamColor.WHITE) ? 1 : 8;
        boolean Rook1Moved = (color == TeamColor.WHITE) ? WRook1Move : BRook1Move;
        boolean Rook8Moved = (color == TeamColor.WHITE) ? WRook8Move : BRook8Move;

        //Side toward 1 Rook
        if (!Rook1Moved)
        {
            ChessPiece rook = board.getPiece(new ChessPosition(row, 1));
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK
                    && board.getPiece(new ChessPosition(row, 2)) == null
                    && board.getPiece(new ChessPosition(row, 3)) == null
                    && board.getPiece(new ChessPosition(row, 4)) == null
                    && !CastlingCheck(new ChessPosition(row, 2), color)
                    && !CastlingCheck(new ChessPosition(row, 3), color)
                    && !CastlingCheck(new ChessPosition(row, 4), color))
            {
                moves.add(new ChessMove(king, new ChessPosition(row, 3), null));
            }
        }
        if (!Rook8Moved)
        {
            ChessPiece rook = board.getPiece(new ChessPosition(row, 8));
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK
                    && board.getPiece(new ChessPosition(row, 6)) == null
                    && board.getPiece(new ChessPosition(row, 7)) == null
                    && !CastlingCheck(new ChessPosition(row, 6), color)
                    && !CastlingCheck(new ChessPosition(row, 7), color))
            {
                moves.add(new ChessMove(king, new ChessPosition(row, 7), null));
            }
        }
        //Side toward 8 Rook
        return moves;
    }

    private Boolean CastlingCheck(ChessPosition pos, TeamColor color) // Checks if Castling move can be made without putting king in check
    {
        TeamColor opposite = (color == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int i = 1; i <= 8; i++) //Check every spot on board
        {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos1 = new ChessPosition(i, j);
                ChessPiece piece1 = board.getPiece(pos1);
                if (piece1 != null && piece1.getTeamColor() == opposite) {
                    for (ChessMove move : piece1.pieceMoves(board, pos1)) {
                        if (move.getEndPosition().equals(pos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Collection<ChessMove> EnPassant(ChessPosition pos, ChessPiece pawn)
    {
        Collection<ChessMove> moves = new ArrayList<>(); //EnPassant move list
        if (LastMove == null)
        {
            return moves;
        }

        ChessPiece LastPiece = board.getPiece(LastMove.getEndPosition());
        if (LastPiece == null || LastPiece.getPieceType() != ChessPiece.PieceType.PAWN) // Check if Enemy Pawn
        {
            return moves;
        }
        if (LastPiece.getTeamColor() == pawn.getTeamColor())
        {
            return moves;
        }

        //Check if pawn moved two
        int DoubleMove = Math.abs(LastMove.getEndPosition().getRow() - LastMove.getStartPosition().getRow());
        if (DoubleMove != 2)
        {
            return moves;
        }

        //Check if enemy pawn is beside friendly one
        if(LastMove.getEndPosition().getRow() != pos.getRow())
        {
            return moves;
        }
        int ColCheck = Math.abs(LastMove.getEndPosition().getColumn() - pos.getColumn());
        if (ColCheck != 1)
        {
            return moves;
        }

        //Add sideways EnPassant movement
        int forward = (pawn.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
        ChessPosition target = new ChessPosition(pos.getRow() + forward, LastMove.getEndPosition().getColumn());
        moves.add(new ChessMove(pos, target, null));
        return moves;
    }
}
