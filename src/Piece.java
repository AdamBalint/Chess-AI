import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Piece {
	
	public Piece(){}
	
	public Point[] calculateMoves(int xLoc, int yLoc, int[][] board){
		ArrayList<Point> moves = new ArrayList<>();
		switch(Math.abs(board[xLoc][yLoc])){
		case 1: //pawn
			moves = getPawnMoves(xLoc, yLoc, board);
			break;
		case 2: //Bishop
			moves = getBishopMoves(xLoc, yLoc, board, true);
			break;
		case 3: //knight
			moves = getKnightMoves(xLoc, yLoc, board, true);
			break;
		case 4: //rook
			moves = getRookMoves(xLoc, yLoc, board, true);
			break;
		case 5: //queen
			moves = getQueenMoves(xLoc, yLoc, board, true);
			break;
		case 6: //king
			moves = getKingMoves(xLoc, yLoc, board, true);
			break;
		
		}
		return moves.toArray(new Point[0]);
	}
	
	public Point[] attackingMoves(int xLoc, int yLoc, int[][] board){
		ArrayList<Point> moves = new ArrayList<>();
		switch(Math.abs(board[xLoc][yLoc])){
		case 1: //pawn
			moves = getPawnAttMoves(xLoc, yLoc, board);
			break;
		case 2: //Bishop
			moves = getBishopMoves(xLoc, yLoc, board, false);
			break;
		case 3: //knight
			moves = getKnightMoves(xLoc, yLoc, board, false);
			break;
		case 4: //rook
			moves = getRookMoves(xLoc, yLoc, board, false);
			break;
		case 5: //queen
			moves = getQueenMoves(xLoc, yLoc, board, false);
			break;
		case 6: //king
			moves = getKingMoves(xLoc, yLoc, board, false);
			break;
		
		}
		return moves.toArray(new Point[0]);
	}

	private boolean onBoard(Point nLoc) {
		if (nLoc.x < 0 || nLoc.x > 7 || nLoc.y < 0 || nLoc.y > 7)
			return false;
		return true;
	}

	private int checkSideCol(Point oLoc, Point nLoc, int[][] board){
		int oPc = board[oLoc.x][oLoc.y];
		int pcAtnLoc = board[nLoc.x][nLoc.y];
		if (pcAtnLoc != 0){
			//if on the same side return 1, if an enemy is on the square return -1
			if ((pcAtnLoc > 0 && oPc > 0) || (pcAtnLoc < 0 && oPc < 0))
				return 1;
			else
				return -1;
		}
		//if the square is empty return 0
		return 0;
	}
	
	/******************************
	 ******Move Calculations*******
	 ******************************/
	
	//checks where the pawn can move
	private ArrayList<Point> getPawnMoves(int row, int col, int[][] board){
		ArrayList<Point> moves = new ArrayList<>();
		Point oLoc = new Point(row, col);
		int dir = board[row][col] < 0 ? 1 : -1;
		
		//check forward 1 square
		Point nLoc = new Point(row+dir, col);
		if (onBoard(nLoc) && (checkSideCol(oLoc, nLoc, board) == 0)){
			moves.add(nLoc);
			//checks forward 2 squares if the piece hasn't moved
			if ((board[row][col] > 0 && row == 6) || (board[row][col] < 0 && row == 1)){
				nLoc = new Point(row+2*dir, col);
				if (onBoard(nLoc) && (checkSideCol(oLoc, nLoc, board) == 0))
					moves.add(nLoc);
			}
		}
		
		//checks if it can take a piece, either normally or en passant
		nLoc = new Point(row+dir, col-1);
		if (onBoard(nLoc) && (checkSideCol(oLoc, nLoc, board) == -1)){
			moves.add(nLoc);
		}else if ((dir == 1 && new Point(row, col-1).equals(Globals.p1.getLastMovedPawn())) ||
				(dir == -1 && new Point(row, col-1).equals(Globals.p2.getLastMovedPawn()))){
			moves.add(nLoc);
		}
		nLoc = new Point(row+dir, col+1);
		if (onBoard(nLoc) && (checkSideCol(oLoc, nLoc, board) == -1)){
			moves.add(nLoc);
		}else if ((dir == 1 && new Point(row, col+1).equals(Globals.p1.getLastMovedPawn())) ||
				(dir == -1 && new Point(row, col+1).equals(Globals.p2.getLastMovedPawn()))){
			moves.add(nLoc);
		}
		for (int i = moves.size()-1; i >=0; i--){
			if (checkCheck(oLoc, moves.get(i), board) )
				moves.remove(i);
		}

		//returns all the possible moves
		return moves;
	}
	
	//checks where the pawn can attack
	private ArrayList<Point> getPawnAttMoves(int row, int col, int[][] board){
		ArrayList<Point> moves = new ArrayList<>();
		Point oLoc = new Point(row, col);
		int dir = board[row][col] < 0 ? 1 : -1;
		//check forward 1 square
		Point nLoc = new Point(row+dir, col);
		//checks if it can take a piece
		nLoc = new Point(row+dir, col-1);
		if (onBoard(nLoc)){
			moves.add(nLoc);
		}
		nLoc = new Point(row+dir, col+1);
		if (onBoard(nLoc)){
			moves.add(nLoc);
		}
		return moves;
	}
	

	//Checks where the knight can move
	private ArrayList<Point> getKnightMoves(int row, int col, int board[][], boolean cc) {
		ArrayList<Point> tmpMoves = new ArrayList<>();
		Point oLoc = new Point (row, col);
		tmpMoves.add(new Point(row+1, col+2));
		tmpMoves.add(new Point(row+1, col-2));
		tmpMoves.add(new Point(row-1, col+2));
		tmpMoves.add(new Point(row-1, col-2));
		tmpMoves.add(new Point(row-2, col-1));
		tmpMoves.add(new Point(row-2, col+1));
		tmpMoves.add(new Point(row+2, col-1));
		tmpMoves.add(new Point(row+2, col+1));
		
		for (int i = tmpMoves.size()-1; i >= 0; i--){
			//check if on the board or if it collides with a piece on the same side
			if (!onBoard(tmpMoves.get(i)) || checkSideCol(oLoc,tmpMoves.get(i), board) == 1){
				tmpMoves.remove(i);
			}
		}
	
		//if it needs to check check, then remove all moves that will cause the 
		//players king to be in check as they are invalid
		if (cc){
			for (int i = tmpMoves.size()-1; i >=0; i--){
				if (cc && checkCheck(oLoc, tmpMoves.get(i), board))
					tmpMoves.remove(i);
			}
		}
		return tmpMoves;
	}
	
	//Checks where the rook can move
	private ArrayList<Point> getRookMoves(int row, int col, int board[][], boolean cc) {
		ArrayList<Point> moves = new ArrayList<>();
		Point oLoc = new Point (row, col);
		
		Point nLoc = new Point();
		//checking right side
		for (int i = col+1; i < 8; i++){
			nLoc = new Point(row, i);
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
		}
		//checking left side
		for (int i = col-1; i >= 0; i--){
			nLoc = new Point(row, i);
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
		}
		//checking down side
		for (int i = row+1; i < 8; i++){
			nLoc = new Point(i, col);
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
		}
		//checking up side
		for (int i = row-1; i >= 0; i--){
			nLoc = new Point(i, col);
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
		}
		
		//if check check is true, remove all invalid moves
		if (cc){
			for (int i = moves.size()-1; i >= 0; i--){
				boolean check = checkCheck(new Point(row, col), moves.get(i), board);
				if (check)
					moves.remove(i);
			}
		}
		return moves;
	}
	
	//Checks where the bishops can move
	private ArrayList<Point> getBishopMoves(int row, int col, int board[][], boolean cc) {
		ArrayList<Point> moves = new ArrayList<>();
		Point oLoc = new Point (row, col);
		Point nLoc = new Point();
		//right-down direction
		for (int i = 1; i < 8-row; i++){
			nLoc = new Point(row+i, col+i);
			if (onBoard(nLoc)){
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
			}
		}
		//right-up direction
		for (int i = 1; i < 8-row; i++){
			nLoc = new Point(row+i, col-i);
			if (onBoard(nLoc)){
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
			}
		}
		//left-down direction
		for (int i = 1; i < row+1; i++){
			nLoc = new Point(row-i, col+i);
			if (onBoard(nLoc)){
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
			}
		}
		//left-up direction
		for (int i = 1; i < row+1; i++){
			nLoc = new Point(row-i, col-i);
			if (onBoard(nLoc)){
				boolean enCol = checkSideCol(oLoc, nLoc, board) == -1;
				boolean alCol = checkSideCol(oLoc, nLoc, board) == 1;
				if (enCol || alCol){
					if (enCol)
						moves.add(nLoc);
					break;
				}
				moves.add(nLoc);
			}
		}
		
		//removes all invalid moves if check check is true
		if (cc){
			for (int i = moves.size()-1; i >= 0; i--){
				if (checkCheck(new Point(row, col), moves.get(i), board))
					moves.remove(i);
			}
		}
		
		return moves;
	}

	private ArrayList<Point> getQueenMoves(int row, int col, int board[][], boolean cc) {
		ArrayList<Point> moves = new ArrayList<>();
		//queen is like a rook and a bishop combined, so just reuse
		moves.addAll(getBishopMoves(row, col, board,cc));
		moves.addAll(getRookMoves(row, col, board,cc));
		
		return moves;
	}

	private ArrayList<Point> getKingMoves(int row, int col, int board[][], boolean cc) {
		ArrayList<Point> moves = new ArrayList<>();
		Point oLoc = new Point (row, col);
		//Diagonals
		moves.add(new Point(row-1, col-1));//up-left
		moves.add(new Point(row+1, col-1));//up-right
		moves.add(new Point(row+1, col+1));//down-right
		moves.add(new Point(row-1, col+1));//down-left
		//vertical and horizontal
		moves.add(new Point(row, col-1));//up
		moves.add(new Point(row, col+1));//down
		moves.add(new Point(row-1, col));//left
		moves.add(new Point(row+1, col));//right
		
		for (int i = moves.size()-1; i >= 0; i--){
			if (!onBoard(moves.get(i)) || checkSideCol(oLoc, moves.get(i), board) == 1)
				moves.remove(i);
		}
		
		ArrayList<Point> tmpMovL = new ArrayList<>();
		ArrayList<Point> tmpMovR = new ArrayList<>();
		boolean white = board[row][col] == 6;
		//get castling info
		boolean[] castleInfo =  white ? Globals.p1.getCastlePcReqs() : Globals.p2.getCastlePcReqs();
		
		//if the king has not moved and is not in check
		if (cc){
			if ((!castleInfo[1]) && (white ? Globals.gameEnded != 1 : Globals.gameEnded != -1)){
				//if the left rook did not move
				if (!castleInfo[0]){
					for (int i = 1; i < 4; i++){
						if (board[row][i] != 0)
							break;
						tmpMovL.add(new Point(row, i));
					}
				}
				
				//if the right rook did not move
				if (!castleInfo[2]){
					for (int i = 5; i < 7; i++){
						if (board[row][i] != 0)
							break;
						tmpMovR.add(new Point(row, i));
					}
				}
			}
		}
		
		//remove all invalid moves if check check is selected
		HashSet eMoves = null;
		if (cc){
			eMoves = getAllDangerousSquares(row, col, board);
			//if the square is being attacked by the enemy then remove
			for (int i = moves.size()-1; i >= 0; i--){
				if (eMoves.contains(moves.get(i)) || checkCheck(oLoc, moves.get(i), board)){
					moves.remove(i);
				}
			}
			//check if the squares between the king and the rook are attacked
			//if they are, then remove the castling option
			//Queen side castle
			if (tmpMovL.size() == 3){
				boolean valid = true;
				for (int i = 2; i >= 0; i--){
					if (eMoves.contains(tmpMovL.get(i)) || checkCheck(oLoc, tmpMovL.get(i), board)){
						valid = false;
					}
				}
				if (valid){
					moves.add(new Point(row, col-2));
				}
			}
			//king side castle
			if (tmpMovR.size() == 2){
				
				boolean valid = true;
				for (int i = 1; i >= 0; i--){
					if (eMoves.contains(tmpMovR.get(i)) || checkCheck(oLoc, tmpMovR.get(i), board)){
						valid = false;
					}
				}
				if (valid){
					moves.add(new Point(row, col+2));
				}
			}
		}
		
		
		
		return moves;
	}
	
	//gets all the squares that are attacked by a certain side
	private HashSet<Point> getAllDangerousSquares(int row, int col, int[][] board) {
		int pc = board[row][col];
		int side = pc > 0 ? 1 : -1;
		HashSet<Point> enemyMoves = new HashSet<>();
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				if ((side == 1 && board[i][j] < 0) || (side == -1 && board[i][j] > 0)){
					enemyMoves.addAll(Arrays.asList(attackingMoves(i,j,board)));
				}
			}
		}
		return enemyMoves;
	}
	
	
	//checks if the move causes check when the piece is moved
	public boolean checkCheck(Point oLoc, Point nLoc, int board[][]){
		int side = board[oLoc.x][oLoc.y] > 0 ? 1 : -1;
		
		int nLocOPc = board[nLoc.x][nLoc.y];
		board[nLoc.x][nLoc.y] = board[oLoc.x][oLoc.y];
		board[oLoc.x][oLoc.y] = 0;
		
		HashSet<Point> eMoves = getAllDangerousSquares(nLoc.x, nLoc.y, board);
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				if ((side == -1 && board[i][j] == -6) || (side == 1 && board[i][j] == 6)){
					if (eMoves.contains(new Point(i,j))){
						board[oLoc.x][oLoc.y] = board[nLoc.x][nLoc.y];
						board[nLoc.x][nLoc.y] = nLocOPc;
						return true;
					}
					else{
						board[oLoc.x][oLoc.y] = board[nLoc.x][nLoc.y];
						board[nLoc.x][nLoc.y] = nLocOPc;
						return false;
					}
				}
			}
		}
		
		board[oLoc.x][oLoc.y] = board[nLoc.x][nLoc.y];
		board[nLoc.x][nLoc.y] = nLocOPc;
		return false;
	}
}
