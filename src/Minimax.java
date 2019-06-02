import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class Minimax{
	private int playerNum;
	private int board[][];
	private float alpha, beta;
	private int ply;
	
	//get who is running the minimax and the parameters used
	public Minimax(int pnum, int[][] board, float alpha, float beta, int ply){
		playerNum = pnum;
		this.board = board;
		this.alpha = alpha;
		this.beta = beta;
		this.ply = ply;
	}
	
	//run the min 
	public float run(){
		return minValue(board, alpha, beta, ply);
	}

	
	private float maxValue(int[][] board, float alpha, float beta, int ply){
		if (ply == 0) //if at the leaf node run the heuristic
			return heuristic(board, true);
		
		//else continue with minimax
		float max = -Float.MAX_VALUE + 0.0001f;
		
		boolean moved = false;
		//loop through the board
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				if ((playerNum == 1 && board[i][j] > 0) || (playerNum == 2 && board[i][j] < 0)){
					Point[] moves = Globals.pcMoves.calculateMoves(i, j, board);
					
					// if no piece has any moves then return result of board, checkmate, stalemate
					if (moves.length != 0){
						moved = true;
					}
					
					//loop through all the moves
					for (int k = 0; k < moves.length; k++){
						Point tmp = moves[k];
						
						//move piece
						int nLocOPc = board[tmp.x][tmp.y];
						board[tmp.x][tmp.y] = board[i][j];
						board[i][j] = 0;
						
						boolean castle = false;
						int castleSide = 0;
						boolean[] oldReqs = playerNum == 2 ? Globals.p2.getCastlePcReqs() : Globals.p1.getCastlePcReqs();
						//if a king is selected and the distance moved is 2 squares
						if ((board[i][j] == 6 || board[i][j] == -6) && (Math.abs(tmp.y - j) == 2)){
							castle = true;
							//queen side castle
							if (tmp.y < j){
								castleSide = -1;
								//move rook
								board[tmp.x][tmp.y+1] = board[tmp.x][0];
								board[tmp.x][0] = 0;
								//update castling information
								if (playerNum == 2){
									Globals.p2.setCastling(0, true);
									Globals.p2.setCastling(1, true);
								}else{
									Globals.p1.setCastling(0, true);
									Globals.p1.setCastling(1, true);
								}
							}else{//king side castle
								castleSide = 1;
								//move rook
								board[tmp.x][tmp.y-1] = board[tmp.x][7];
								board[tmp.x][7] = 0;
								//update castling information
								if (playerNum == 2){
									Globals.p2.setCastling(1, true);
									Globals.p2.setCastling(2, true);
								}else{
									Globals.p1.setCastling(1, true);
									Globals.p1.setCastling(2, true);
								}
							}
						} //if the king moved
						else if (board[i][j] == 6 || board[i][j] == -6){
							if (playerNum == 2){
								Globals.p2.setCastling(1, true);
							}else{
								Globals.p1.setCastling(1, true);
							}
						}//if rook moved
						else if (board[i][j] == -4 && playerNum == 2){
							if (i == 0 && j == 0){
								Globals.p2.setCastling(0, true);
							}else if (i == 0 && j == 7){
								Globals.p2.setCastling(2, true);
							}
						}
						else if (board[i][j] == 4 && playerNum != 2){
							if (i == 7 && j == 0){
								Globals.p1.setCastling(0, true);
							}else if (i == 7 && j == 7){
								Globals.p1.setCastling(2, true);
							}
						}
						
						boolean promotion = false;
						//if the piece is promoted then set flag to true
						if ((playerNum == 1 && board[tmp.x][tmp.y] == 1 && tmp.x == 0) || (playerNum == 2 && board[tmp.x][tmp.y] == -1 && tmp.x == 7)){
							promotion = true;
						}
						
						///////////////////////////////////////////////////////////////////
						////////////////////En Passant start///////////////////////////////
						///////////////////////////////////////////////////////////////////
						//save state information
						Point pLastMovedPawn = playerNum == 2 ? Globals.p2.getLastMovedPawn() : Globals.p1.getLastMovedPawn();
						boolean enPass = false;
						int enPassSide = 0;
						int takenPiece = 0;
						//update last moved pawn
						if (playerNum == 2){
							if (board[tmp.x][tmp.y] == -1 && Math.abs(i - tmp.x) == 2){
								Globals.p2.setLastMovedPawn(tmp);
							}else{
								Globals.p2.setLastMovedPawn(new Point(-1,-1));
							}
						}else if (playerNum == 1){
							if (board[tmp.x][tmp.y] == 1 && Math.abs(i - tmp.x) == 2){
								Globals.p1.setLastMovedPawn(tmp);
							}else{
								Globals.p1.setLastMovedPawn(new Point(-1,-1));
							}
						}
						
						//check if en passant is performed and update the board accordingly
						if (playerNum == 2){
							if (board[tmp.x][tmp.y] == -1 && Math.abs(tmp.y - j) == 1 && Math.abs(tmp.x-i) == 1 && nLocOPc == 0){
								enPass = true;
								takenPiece = board[tmp.x-1][tmp.y];		
								board[tmp.x-1][tmp.y] = 0;
								if (tmp.y - j < 0){
									enPassSide = -1;
								}else{
									enPassSide = 1;
								}
							}
						}else if (playerNum == 1){
							if (board[tmp.x][tmp.y] == 1 && Math.abs(tmp.y - j) == 1 && Math.abs(tmp.x-i) == 1 && nLocOPc == 0){
								enPass = true;
								takenPiece = board[tmp.x+1][tmp.y];
								board[tmp.x+1][tmp.y] = 0;
								if (tmp.y - j < 0){
									enPassSide = -1;
								}else{
									enPassSide = 1;
								}
							}
						}
						///////////////////////////////////////////////////////////////////////////
						/////////////////////////////En Passant End////////////////////////////////
						///////////////////////////////////////////////////////////////////////////
						
						//if promoted, loop through all possible promotion pieces
						for (int p = 0; p < (promotion ? 4 : 1); p++){
							//if promoted then replace pawn with the promoted piece
							if (promotion)
								board[tmp.x][tmp.y] = playerNum == 2 ? -(p+2) : (p+2);
							
							//run minimax
							float score = minValue(board, alpha, beta, ply-1);
							max = Math.max(max, score);
							
							
							/////////////////////////////////////
							//Reset board to the original state//
							/////////////////////////////////////
							
							if (castle){
								//reset queen side castle
								if (castleSide == -1){
									board[tmp.x][0] = board[tmp.x][tmp.y + 1];
									board[tmp.x][tmp.y+1] = 0;
								}//king side castle
								else if (castleSide == 1){
									board[tmp.x][7] = board[tmp.x][tmp.y-1];
									board[tmp.x][tmp.y-1] = 0;
								}
							}
							
							if (board[tmp.x][tmp.y] == 6 || board[tmp.x][tmp.y] == -6 || board[tmp.x][tmp.y] == 4 || board[tmp.x][tmp.y] == -4){
								for (int r = 0; r < 3; r++){
									if (playerNum == 2){
										Globals.p2.setCastling(r, oldReqs[r]);
									}else if (playerNum == 1){
										Globals.p1.setCastling(r, oldReqs[r]);
									}
										
								}
							}
							
							
							//if there was a promotion, reset the piece to a pawn
							if (promotion){
								board[tmp.x][tmp.y] = playerNum == 2 ? -1 : 1;
							}
							
							//if en passant was performed, revert the changes
							if (enPass){
								if (playerNum == 2){
									board[tmp.x-1][tmp.y] = takenPiece;
									Globals.p2.setLastMovedPawn(pLastMovedPawn);
								}else if (playerNum == 1){
									board[tmp.x+1][tmp.y] = takenPiece;
									Globals.p1.setLastMovedPawn(pLastMovedPawn);
								}
							}
							
							//move the piece back to it's original position
							board[i][j] = board[tmp.x][tmp.y];
							board[tmp.x][tmp.y] = nLocOPc;
							
							//check if pruning takes place
							if (beta <= max){
								return max;
							}
							//update alpha
							alpha = Math.max(alpha, max);
						}
					}
				}
			}
		}
		//if there were no moves, then run the heuristic
		if (!moved){
			return heuristic(board, true);
		}
		
		return max;
	}
	
	
	private float minValue(int[][] board, float alpha, float beta, int ply){
		if (ply == 0) //if a leaf node then run the heuristic
			return heuristic(board, false);
		
		float min = Float.MAX_VALUE - 0.0001f;
		boolean moved = false;
		
		//loop through the board
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				if ((playerNum == 2 && board[i][j] > 0) || (playerNum == 1 && board[i][j] < 0)){
					Point[] moves = Globals.pcMoves.calculateMoves(i, j, board);
					
					
					if (moves.length != 0){
						moved = true;
					}
					
					//loop through all the moves
					for (int k = 0; k < moves.length; k++){
						Point tmp = moves[k];
						
						//move piece								
						int nLocOPc = board[tmp.x][tmp.y];
						board[tmp.x][tmp.y] = board[i][j];
						board[i][j] = 0;
						
						boolean castle = false;
						int castleSide = 0;
						boolean[] oldReqs = playerNum != 2 ? Globals.p2.getCastlePcReqs() : Globals.p1.getCastlePcReqs();
						//if a king is selected and the distance moved is 2 squares
						if ((board[i][j] == 6 || board[i][j] == -6) && (Math.abs(tmp.y - j) == 2)){
							castle = true;
							//queen side castle
							if (tmp.y < j){
								castleSide = -1;
								//move rook
								board[tmp.x][tmp.y+1] = board[tmp.x][0];
								board[tmp.x][0] = 0;
								//update castling information
								if (playerNum == 1){
									Globals.p2.setCastling(0, true);
									Globals.p2.setCastling(1, true);
								}else{
									Globals.p1.setCastling(0, true);
									Globals.p1.setCastling(1, true);
								}
							}else{//king side castle
								castleSide = 1;
								//move rook
								board[tmp.x][tmp.y-1] = board[tmp.x][7];
								board[tmp.x][7] = 0;
								//update castling information
								if (playerNum == 1){
									Globals.p2.setCastling(1, true);
									Globals.p2.setCastling(2, true);
								}else{
									Globals.p1.setCastling(1, true);
									Globals.p1.setCastling(2, true);
								}
							}
						} //if the king moved
						else if (board[i][j] == 6 || board[i][j] == -6){
							if (playerNum != 2){
								Globals.p2.setCastling(1, true);
							}else{
								Globals.p1.setCastling(1, true);
							}
						}//if rook moved
						else if (board[i][j] == -4 && playerNum != 2){
							if (i == 0 && j == 0){
								Globals.p2.setCastling(0, true);
							}else if (i == 0 && j == 7){
								Globals.p2.setCastling(2, true);
							}
						}
						else if (board[i][j] == 4 && playerNum == 2){
							if (i == 7 && j == 0){
								Globals.p1.setCastling(0, true);
							}else if (i == 7 && j == 7){
								Globals.p1.setCastling(2, true);
							}
						}
						
						boolean promotion = false;
						//check if piece promotion
						if ((playerNum == 1 && board[tmp.x][tmp.y] == 1 && tmp.x == 0) || (playerNum == 2 && board[tmp.x][tmp.y] == -1 && tmp.x == 7)){
							promotion = true;
						}
						
						///////////////////////////////////////////////////////////////////
						////////////////////En Passant start///////////////////////////////
						///////////////////////////////////////////////////////////////////
						//save all relevant state information
						Point pLastMovedPawn = playerNum != 2 ? Globals.p2.getLastMovedPawn() : Globals.p1.getLastMovedPawn();
						boolean enPass = false;
						int enPassSide = 0;
						int takenPiece = 0;
						
						//update last moved pawns
						if (playerNum != 2){
							if (board[tmp.x][tmp.y] == -1 && Math.abs(i - tmp.x) == 2){
								Globals.p2.setLastMovedPawn(tmp);
							}else{
								Globals.p2.setLastMovedPawn(new Point(-1,-1));
							}
						}else if (playerNum == 2){
							if (board[tmp.x][tmp.y] == 1 && Math.abs(i - tmp.x) == 2){
								Globals.p1.setLastMovedPawn(tmp);
							}else{
								Globals.p1.setLastMovedPawn(new Point(-1,-1));
							}
						}
						
						//check if en passant is performed and update the board accordingly
						if (playerNum != 2){
							if (board[tmp.x][tmp.y] == -1 && Math.abs(tmp.y - j) == 1 && Math.abs(tmp.x-i) == 1 && nLocOPc == 0){
								enPass = true;
								takenPiece = board[tmp.x-1][tmp.y];
								board[tmp.x-1][tmp.y] = 0;
								if (tmp.y - j < 0){
									enPassSide = -1;
								}else{
									enPassSide = 1;
								}
							}
						}else if (playerNum == 2){
							if (board[tmp.x][tmp.y] == 1 && Math.abs(tmp.y - j) == 1 && Math.abs(tmp.x-i) == 1 && nLocOPc == 0){
								enPass = true;
								takenPiece = board[tmp.x+1][tmp.y];
								board[tmp.x+1][tmp.y] = 0;
								if (tmp.y - j < 0){
									enPassSide = -1;
								}else{
									enPassSide = 1;
								}
							}
						}
						///////////////////////////////////////////////////////////////////////////
						/////////////////////////////En Passant End////////////////////////////////
						///////////////////////////////////////////////////////////////////////////
						
						
						//if promoted, loop through all promotion possibilities
						for (int p = 0; p < (promotion ? 4 : 1); p++){
							if (promotion)
								board[tmp.x][tmp.y] = playerNum == 2 ? (p+2) : -(p+2);
						
							//run minimax
							float score = maxValue(board, alpha, beta, ply-1);
							min = Math.min(min, score);
							
							/////////////////////////////////////
							//Reset board to the original state//
							/////////////////////////////////////
							
							if (castle){
								//reset queen side castle
								if (castleSide == -1){
									board[tmp.x][0] = board[tmp.x][tmp.y + 1];
									board[tmp.x][tmp.y+1] = 0;
								}//king side castle
								else if (castleSide == 1){
									board[tmp.x][7] = board[tmp.x][tmp.y-1];
									board[tmp.x][tmp.y-1] = 0;
								}
							}
							
							if (board[tmp.x][tmp.y] == 6 || board[tmp.x][tmp.y] == -6 || board[tmp.x][tmp.y] == 4 || board[tmp.x][tmp.y] == -4){
								for (int r = 0; r < 3; r++){
									if (playerNum != 2){
										Globals.p2.setCastling(r, oldReqs[r]);
									}else if (playerNum == 2){
										Globals.p1.setCastling(r, oldReqs[r]);
									}
										
								}
							}
							
							
							
							//if promoted then reset with pawn
							if (promotion){
								board[tmp.x][tmp.y] = playerNum != 2 ? 1 : -1;
							}
							
							
							//if en passant was performed, revert changes
							if (enPass){
								if (playerNum != 2){
									board[tmp.x-1][tmp.y] = takenPiece;
									Globals.p2.setLastMovedPawn(pLastMovedPawn);
								}else if (playerNum == 2){
									board[tmp.x+1][tmp.y] = takenPiece;
									Globals.p1.setLastMovedPawn(pLastMovedPawn);
								}
							}
							
							//move piece back
							board[i][j] = board[tmp.x][tmp.y];
							board[tmp.x][tmp.y] = nLocOPc;
							
							//check if pruned
							if (min <= alpha){
								return min;
							}
							beta = Math.min(beta, min);
						}
					}
				}
			}
		}
		//if no moves were made, then return the heuristic
		if (!moved){
			return heuristic(board, false);
		}
		
		return min;
	}
	

	//heuristic calculation
	public float heuristic(int board[][], boolean max){
		float score = 0;
		int[] pcNums = new int[6];
		HashSet<Point> wpm = new HashSet<Point>();
		HashSet<Point> wpam = new HashSet<>(); //all the white moves
		HashSet<Point> bpm = new HashSet<Point>();
		HashSet<Point> bpam = new HashSet<>(); //all the white moves
		ArrayList<Point> wkm = new ArrayList(); //black king moves
		ArrayList<Point> bkm = new ArrayList(); //black king moves
		Point wKingLoc = new Point(-1,-1);
		Point bKingLoc = new Point(-1,-1);
		boolean p1Castle[] = Globals.p1.getCastlePcReqs();
		boolean p2Castle[] = Globals.p2.getCastlePcReqs();
		ArrayList<Integer[]> pcs = new ArrayList<>();
		//gathers information
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				if (board[i][j] != 0){
					Integer[] tmpPcs = {i, j, board[i][j]};
					pcs.add(tmpPcs);
				}
				if (board[i][j] > 0){
					Point[] tmpMov = Globals.pcMoves.calculateMoves(i, j, board);
					Point[] tmpAMov = Globals.pcMoves.attackingMoves(i, j, board);
					if (board[i][j] == 6){
						wKingLoc = new Point(i,j);
						wkm.addAll(Arrays.asList(tmpMov));
					}
					wpm.addAll(Arrays.asList(tmpMov));
					wpam.addAll(Arrays.asList(tmpAMov));
					pcNums[Math.abs(board[i][j])-1] += 1;
				}
				else if (board[i][j] < 0){
					Point[] tmpMov = Globals.pcMoves.calculateMoves(i, j, board);
					Point[] tmpAMov = Globals.pcMoves.attackingMoves(i, j, board);
					if (board[i][j] == -6){
						bKingLoc = new Point(i,j);
						bkm.addAll(Arrays.asList(tmpMov));
					}
					bpm.addAll(Arrays.asList(tmpMov));
					bpam.addAll(Arrays.asList(tmpAMov));
					pcNums[Math.abs(board[i][j])-1] -= 1;
				}
			}
		}
		
		//gets the state of the game, checkmate, stalemate, check
		int mate = isEndGame(wpm, wpam, bpm, bpam, wKingLoc, bKingLoc, pcs, wkm, bkm);
		
		//little penalty to move king or rooks so it doesn't needlessly ruin a castling opportunity
		if ((playerNum == 2 && max) || (playerNum != 2 && !max)){
			if (p2Castle[1] == true){
				score -= 0.09;
			}else{
				if (p2Castle[0] == true){
					score -=0.04;
				}
				if (p2Castle[2] == true){
					score -= 0.04;
				}
			}
		}
		if ((playerNum == 1 && max) || (playerNum != 1 && !max)){
			if (p1Castle[1] == true){
				score -= 0.11;
			}else{
				if (p1Castle[0] == true){
					score -=0.04;
				}
				if (p1Castle[2] == true){
					score -= 0.04;
				}
			}
		}
		
		
		//if check then give a slight bonus
		if (Math.abs(mate) == 1){
			score += 0.1;
		}
		//if checkmate then the game is over and return an extreme score
		else if (Math.abs(mate) == 2){
			score += 100;
			if ((max && playerNum == 2) || (!max && playerNum == 2)){
				score *= -1;
		}
			return score;
		}
		//if stalemate then return 
		else if (Math.abs(mate) == 3){
			score = 0;
			if ((max && playerNum == 2) || (!max && playerNum == 2)){
				score *= -1;
			}
			return score;
		}
		
		//set the difference in pieces and get the point difference
		for (int i = 0; i < 6; i++){
			score += Globals.pieceVals[i] * pcNums[i];
		}
		//add a slight score for mobility
		score += 0.1 * (wpm.size() - bpm.size());
		
		//reverse score based on viewpoint
		if ((max && playerNum == 2) || (!max && playerNum == 2)){
				score *= -1;
		}
		
		return score;
		
	}
	
	//check the end game scenarios
	public static int isEndGame(HashSet<Point> wpm, HashSet<Point> wpam, HashSet<Point> bpm, HashSet<Point> bpam, Point wKingLoc, Point bKingLoc, ArrayList<Integer[]> pcs, ArrayList<Point> wkm, ArrayList<Point> bkm){
		//if the king is in check and tehre are no moves, then  checkmate
		if (wpm.size() == 0 && bpam.contains(wKingLoc)){
			return -2;
		}else if (bpm.size() == 0 && wpam.contains(bKingLoc)){
			return 2;
		}
		
		int numWBish = 0;
		int numBBish = 0;
		int numWKnight = 0;
		int numBKnight = 0;
		ArrayList<Point> wpLoc = new ArrayList<Point>();
		ArrayList<Point> bpLoc = new ArrayList<Point>();
		
		boolean wKingOnly = true;
		boolean bKingOnly = true;
		
		//get the number of pieces for bishops and knights
		for (int i = 0; i < pcs.size(); i++){
			if (pcs.get(i)[2] != -6 && pcs.get(i)[2] < 0)
				bKingOnly = false;
			else if (pcs.get(i)[2] != 6 && pcs.get(i)[2] > 0)
				wKingOnly = false;
			
			if (pcs.get(i)[2] == 1){
				wpLoc.add(new Point(pcs.get(i)[0], pcs.get(i)[1]));
			}
			else if (pcs.get(i)[2] == -1){
				bpLoc.add(new Point(pcs.get(i)[0], pcs.get(i)[1]));
			}
			else if (pcs.get(i)[2] == 3)
				numWKnight++;
			else if (pcs.get(i)[2] == -3)
				numBKnight++;
			else if (pcs.get(i)[2] == 2)
				numWBish++;
			else if (pcs.get(i)[2] == -2)
				numBBish++;
		}
		
		//if there are no moves for the kings
		if((wpm.size() == 0 && !bpam.contains(wKingLoc))){
			return 3;
		}else if ((bpm.size() == 0 && !wpam.contains(bKingLoc))){
			return -3;
		}
		//if there are only kings left, or if each king has a pawn that can't move
		else if ((bKingOnly && wKingOnly)){
			return !Globals.p2.isPlayersTurn() ? -3 : 3;
		}
		//if there are only kings with either a knight or a bishop
		else if ((bKingOnly && ((numWKnight == 1) ^ (numWBish == 1))) || 
				(wKingOnly && ((numBKnight == 1) ^ (numBBish == 1)))){
			return !Globals.p2.isPlayersTurn() ? -3 : 3;
		}
		//if the king has moves, but the enemy is attacking him, then check	
		if(wpm.size() > 0 && bpam.contains(wKingLoc)){
			return 1;
		}
		else if(bpm.size() > 0 && wpam.contains(bKingLoc)){
			return -1;
		}
		
		return 0;
	}
	
}
