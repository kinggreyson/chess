These notes are for new things I've learned over the course of the semester long chess project.

Notes for CS 240 Chess Program:

Phase 0 Notes:
Things that matter for chess piece moves
-Position
-Color
-Piece Type

chess Board size

16x16

0 1 2 3 4 5 6 7
1			Chess Board Positions
2		(7,0) (7,1) (7,2) (7,3) (7,4) (7,5) (7,6) (7,7)
3		(6,0) (6,1) (6,2) (6,3) (6,4) (6,5) (6,6) (6,7)
4		(5,0) (5,1) (5,2) (5,3) (5,4) (5,5) (5,6) (5,7)
5		(4,0) (4,1) (4,2) (4,3) (4,4) (4,5) (4,6) (4,7)
6		(3,0) (3,1) (3,2) (3,3) (3,4) (3,5) (3,6) (3,7)
7		(2,0) (2,1) (2,2) (2,3) (2,4) (2,5) (2,6) (2,7)
        (1,0) (1,1) (1,2) (1,3) (1,4) (1,5) (1,6) (1,7)
        (0,0) (0,1) (0,2) (0,3) (0,4) (0,5) (0,6) (0,7)

Starting Positions:
Black Rook -    (7,0)		White Rook -    (0,0)
Black Knight -  (7,1)		White Knight -  (0,1)
Black Bishop -  (7,2)		White Bishop -  (0,2)
Black Queen -   (7,3)		White Queen -   (0,3)
Black King -    (7,4)		White King -    (0,4)
Black Bishop2 - (7,5)		White Bishop2 - (0,5)
Black Knight2 - (7,6)		White Knight2 - (0,6)
Black Rook2 -   (7,7)		White Rook2 -   (0,7)
Black Pawn0 -   (6,0)		White Pawn0 -   (1,0)
Black Pawn1 -   (6,1)		White Pawn1 -   (1,1)
Black Pawn2 -   (6,2)		White Pawn2 -   (1,2)
Black Pawn3 -   (6,3)		White Pawn3 -   (1,3)
Black Pawn4 -   (6,4)		White Pawn4 -   (1,4)
Black Pawn5 -   (6,5)		White Pawn5 -   (1,5)
Black Pawn6 -   (6,6)		White Pawn6 -   (1,6)
Black Pawn7 -   (6,7)		White Pawn7 -   (1,7)

Pseudocode for piece

List<ChessMove> possibleMoves;
RookMovesCalculator(position, color, board)
{
directions = [(1,0), (0,1), (-1,0), (0,-1)];
for (const [rowDir, colDir] of directions)
{
let currentRow = position.row + rowDir;
let currentCol = position.col + colDir;
        	while (currentRow >= 0 && currentRow <= 7 && currentCol >= 0 && currentCol <= 7) 
		{ 
                	if (board[currentRow][currentCol] != empty) 
	    		{
                		if (board[currentRow][currentCol] == enemy) 
				{
                    		add possibleMove;
                		}
                	break; // Cut off route
           		}
          	add possibleMove;
            	currentRow += rowDir; 
            	currentCol += colDir;
		} 
    }
}
KnightMovesCalculator(position, color, board)
{
        directions = [(2,1),(1,2),(2,-1),(1,-2),(-1,2),(-2,1),(-1,-2),(-2,-1)]
        for (const [rowDir, colDir] of directions)
        {       
                let currentRow = position.row + rowDir;
                let currentCol = position.col + colDir;
                if(currentRow >= 0 && currentRow <= 7 && currentCol >= 0 && currentCol <= 7)
                {
                        if (board[currentRow][currentCol] != friendly)
                        {
                                add possibleMove;
                        }
                }
        }
}
BishopMovesCalculator(position, color, board)
{
        directions = [(1,1), (1,-1), (-1,1), (-1,-1)];
        for (const [rowDir, colDir] of directions)
        {
                let currentRow = position.row + rowDir;
                let currentCol = position.col + colDir;
                while (currentRow >= 0 && currentRow <= 7 && currentCol >= 0 && currentCol <= 7) 
		        { 
                        if (board[currentRow][currentCol] != empty) 
	    		        {
                		        if (board[currentRow][currentCol] == enemy) 
				                {
                    		            add possibleMove;
                		        }
                	break; // Cut off route
           		}
          	add possibleMove;
                currentRow += rowDir; 
                currentCol += colDir;
		} 
    }
}
QueenMovesCalculator(position, color, board)
{
        directions = [(1,1), (1,-1), (-1,1), (-1,-1),(0,1),(1,0),(-1,0),(0,-1)];
        for (const [rowDir, colDir] of directions)
        {
                let currentRow = position.row + rowDir;
                let currentCol = position.col + colDir;
                while (currentRow >= 0 && currentRow <= 7 && currentCol >= 0 && currentCol <= 7) 
		        { 
                        if (board[currentRow][currentCol] != empty) 
	    		        {
                		        if (board[currentRow][currentCol] == enemy) 
				                {
                    		            add possibleMove;
                		        }
                	    break; // Cut off route
           		        }
          	    add possibleMove;
            	currentRow += rowDir; 
            	currentCol += colDir; 
                } 
        }
}
KingMovesCalculator(position, color, board)
{
        directions = [(1,1), (1,-1), (-1,1), (-1,-1),(0,1),(1,0),(-1,0),(0,-1)];
        for (const [rowDir, colDir] of directions)
        {
                let currentRow = position.row + rowDir;
                let currentCol = position.col + colDir;
                if(currentRow >= 0 && currentRow <= 7 && currentCol >= 0 && currentCol <= 7)
                {
                        if (board[currentRow][currentCol] != friendly)
                        {
                                if (!check((currentRow, currentCol), color, board))
                                {
                                        add possibleMove;
                                }
                        }
                }
        }
}

PawnMovesCalculator(position, color, board);
{
        if (color == white)
        {
                forward = (1,0);
                capture = [(1,1),(1,-1)];
        }
        else
        {
                forward = (-1,0);
                capture = [(-1,-1), (-1,1)];
        }
        if (board[position + forward] == empty) //Any turn
        {
                add move;
                if (((color == white && row.position == 1) || (color == black && row.position == 6)) && board[position + 2 * forward] == empty) //FIRST TURN
                {
                        add possibleMove;
                }
        }
        for (const [rowDir, colDir] of capture)
        {
                let currentRow = position.row + rowDir;
                let currentCol = position.col + colDir;
                if (currentRow >= 0 && currentRow <= 7 && currentCol >= 0 && currentCol <= 7)
                {
                        if (board[currentRow][currentCol] == opposite)
                        {
                                add possibleMove;
                        }
                }
        }
}

Phase 1:

Checklist to be Implemented:
Team's turn
Valid Moves
-Can't move into check
-Implement Check
-Implement Checkmate
-Implement stalemate

chessPiece handles most movement
-Use valid moves to validate moves and remove moves such as check/checkmate.
makeMove updates the state of the board depending on the move along with pawn promotions.

Phase 5:
var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
System.out.println("♕ 240 Chess Client: " + piece);

Phase 6:
Potentially needed code
/*public void leaveGame()
{
this.game = null;
}

    public void startGame(Game game) {
        this.game = game;
    }*/