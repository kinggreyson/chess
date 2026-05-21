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
    private boolean wKingMove = false;
    private boolean bKingMove = false;
    private boolean wRook1Move = false;
    private boolean wRook8Move = false;
    private boolean bRook1Move = false;
    private boolean bRook8Move = false;
    private ChessMove lastMove = null;

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
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.turn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {
        return turn;
    }

    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> possible = new ArrayList<>();

        for (ChessMove move : moves) {
            if (!kingInCheck(move, piece.getTeamColor())) {
                possible.add(move);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            for (ChessMove castlingMove : castling(startPosition, piece.getTeamColor())) {
                if (!kingInCheck(castlingMove, piece.getTeamColor())) {
                    possible.add(castlingMove);
                }
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            possible.addAll(enPassant(startPosition, piece));
        }
        return possible;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null) {
            throw new InvalidMoveException("No piece at position");
        }
        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException("Not your turn");
        }
        Collection<ChessMove> possible = validMoves(start);
        if (possible == null || !possible.contains(move)) {
            throw new InvalidMoveException("Impossible Move");
        }

        ChessPiece.PieceType type = move.getPromotionPiece() != null ? move.getPromotionPiece() : piece.getPieceType();

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                wKingMove = true;
            } else {
                bKingMove = true;
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (start.equals(new ChessPosition(1, 1))) { wRook1Move = true; }
            if (start.equals(new ChessPosition(1, 8))) { wRook8Move = true; }
            if (start.equals(new ChessPosition(8, 1))) { bRook1Move = true; }
            if (start.equals(new ChessPosition(8, 8))) { bRook8Move = true; }
        }

        int rookKingMove = move.getEndPosition().getColumn() - start.getColumn();
        if (piece.getPieceType() == ChessPiece.PieceType.KING && Math.abs(rookKingMove) == 2) {
            int row = start.getRow();
            if (rookKingMove == -2) {
                board.addPiece(new ChessPosition(row, 4), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(row, 1), null);
            } else {
                board.addPiece(new ChessPosition(row, 6), new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK));
                board.addPiece(new ChessPosition(row, 8), null);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                && move.getStartPosition().getColumn() != move.getEndPosition().getColumn()
                && board.getPiece(move.getEndPosition()) == null) {
            board.addPiece(new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn()), null);
        }

        board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), type));
        board.addPiece(start, null);

        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        lastMove = move;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingLocation = getKing(teamColor);
        if (kingLocation == null) {
            return false;
        }
        TeamColor opposite = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == opposite) {
                    if (attacksPosition(piece, pos, kingLocation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return noMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return noMoves(teamColor);
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    private boolean attacksPosition(ChessPiece piece, ChessPosition piecePos, ChessPosition target) {
        for (ChessMove move : piece.pieceMoves(board, piecePos)) {
            if (move.getEndPosition().equals(target)) {
                return true;
            }
        }
        return false;
    }

    private ChessPosition getKing(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor
                        && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }

    private boolean noMoves(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean kingInCheck(ChessMove move, TeamColor teamColor) {
        ChessBoard copy = copyBoard();
        ChessPiece piece = copy.getPiece(move.getStartPosition());
        ChessPiece.PieceType type = move.getPromotionPiece() != null ? move.getPromotionPiece() : piece.getPieceType();
        copy.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), type));
        copy.addPiece(move.getStartPosition(), null);

        ChessBoard originalBoard = this.board;
        this.board = copy;
        boolean inCheck = isInCheck(teamColor);
        this.board = originalBoard;

        return inCheck;
    }

    private ChessBoard copyBoard() {
        ChessBoard copy = new ChessBoard();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null) {
                    copy.addPiece(pos, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return copy;
    }

    private Collection<ChessMove> castling(ChessPosition king, TeamColor color) {
        Collection<ChessMove> moves = new ArrayList<>();
        boolean kingMoved = (color == TeamColor.WHITE) ? wKingMove : bKingMove;
        if (kingMoved || isInCheck(color)) {
            return moves;
        }
        int row = (color == TeamColor.WHITE) ? 1 : 8;
        boolean rook1Moved = (color == TeamColor.WHITE) ? wRook1Move : bRook1Move;
        boolean rook8Moved = (color == TeamColor.WHITE) ? wRook8Move : bRook8Move;

        if (!rook1Moved) {
            ChessPiece rook = board.getPiece(new ChessPosition(row, 1));
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK
                    && board.getPiece(new ChessPosition(row, 2)) == null
                    && board.getPiece(new ChessPosition(row, 3)) == null
                    && board.getPiece(new ChessPosition(row, 4)) == null
                    && !castlingCheck(new ChessPosition(row, 2), color)
                    && !castlingCheck(new ChessPosition(row, 3), color)
                    && !castlingCheck(new ChessPosition(row, 4), color)) {
                moves.add(new ChessMove(king, new ChessPosition(row, 3), null));
            }
        }
        if (!rook8Moved) {
            ChessPiece rook = board.getPiece(new ChessPosition(row, 8));
            if (rook != null && rook.getPieceType() == ChessPiece.PieceType.ROOK
                    && board.getPiece(new ChessPosition(row, 6)) == null
                    && board.getPiece(new ChessPosition(row, 7)) == null
                    && !castlingCheck(new ChessPosition(row, 6), color)
                    && !castlingCheck(new ChessPosition(row, 7), color)) {
                moves.add(new ChessMove(king, new ChessPosition(row, 7), null));
            }
        }
        return moves;
    }

    private boolean castlingCheck(ChessPosition pos, TeamColor color) {
        TeamColor opposite = (color == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos1 = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos1);
                if (piece != null && piece.getTeamColor() == opposite) {
                    if (attacksPosition(piece, pos1, pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Collection<ChessMove> enPassant(ChessPosition pos, ChessPiece pawn) {
        Collection<ChessMove> moves = new ArrayList<>();
        if (lastMove == null) {
            return moves;
        }

        ChessPiece lastPiece = board.getPiece(lastMove.getEndPosition());
        if (lastPiece == null || lastPiece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return moves;
        }
        if (lastPiece.getTeamColor() == pawn.getTeamColor()) {
            return moves;
        }

        int doubleMove = Math.abs(lastMove.getEndPosition().getRow() - lastMove.getStartPosition().getRow());
        if (doubleMove != 2) {
            return moves;
        }

        if (lastMove.getEndPosition().getRow() != pos.getRow()) {
            return moves;
        }
        int colCheck = Math.abs(lastMove.getEndPosition().getColumn() - pos.getColumn());
        if (colCheck != 1) {
            return moves;
        }

        int forward = (pawn.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
        ChessPosition target = new ChessPosition(pos.getRow() + forward, lastMove.getEndPosition().getColumn());
        moves.add(new ChessMove(pos, target, null));
        return moves;
    }
}