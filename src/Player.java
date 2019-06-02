import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Player {

	private int playerNum = -1;
	private boolean myTurn = false;
	private ArrayList<Integer> captures = new ArrayList<>();
	private boolean isHuman = true;
	private int plyLevel = 4;
	private boolean[] castlingMoved = {false, false, false}; //rook, king, rook
	private Point lastMovedPawn = new Point(-1,-1);
	private boolean takingTurn = false;
	private boolean increase = false;
	private boolean smartPly = false;
	
	//player constructor
	public Player(){}
	public Player(int num){
		if (num == 1)
			myTurn = true;
		playerNum = num;
	}
	
	//gets if the player is taking their turn
	public boolean isTakingTurn(){
		return takingTurn;
	}
	//sets the ply used
	public void setPly (int p){
		plyLevel = p;
	}
	//sets if the player is taking their turn
	public void setTakingTurn(boolean tt){
		takingTurn = tt;
	}
	//removes all captures to reset player
	public void removeCaptures(){
		captures = new ArrayList<Integer>();
	}
	
	//gets the booleans to check for castling
	public boolean[] getCastlePcReqs(){
		return castlingMoved;
	}
	
	//gets if human or AI
	public boolean isHuman(){
		return isHuman;
	}
	//sets human or AI
	public void setHuman(boolean h){
		isHuman = h;
	}
	//gets if it is the players turn
	public boolean isPlayersTurn(){
		return myTurn;
	}
	//sets if using smart ply
	public void setSmartPly(boolean n){
		smartPly = n;
	}
	//adds a capture to the list and sorts it so pieces display in order of increasing value
	public void addCapture (int captured){
		captures.add(captured);
		Collections.sort(captures);
		if (playerNum == 1)
			Collections.reverse(captures);
	}
	//returns the captures
	public ArrayList<Integer> getCaptures(){
		return captures;
	}
	//sets the number of the player
	public void setPlayerNumber(int num){
		playerNum = num;
	}
	//end the player's turn
	public void endTurn(){
		myTurn = false;
	}
	//starts the player's turn
	public void startTurn(){
		myTurn = true;
	}

	//sets a flag for the castling
	public void setCastling(int i, boolean n){
		castlingMoved[i] = n;
	}
	//gets the last pawn moved - used for en passant
	public Point getLastMovedPawn(){
		return lastMovedPawn;
	}
	//sets the last pawn moved
	public void setLastMovedPawn(Point p){
		lastMovedPawn = p;
	}
	
	//decides the next move
	public Point[] decideMove(int[][] board) throws InterruptedException, ExecutionException{
		//if smartply used and it has not increased the ply, then increse the ply by 1 if there are less than 12 pieces ont he board
		if (smartPly && !increase){
			int pcCount = 0;
			for (int  i= 0; i < 8; i++){
				for (int j = 0; j < 8; j++){
					if (board[i][j] != 0)
						pcCount++;
				}
			}
			if (pcCount < 12){
				increase = true;
				plyLevel++;
			}
		}
		
		//make an arraylist of possible moves
		ArrayList<Point[]> possibleMoves = new ArrayList<Point[]>();
		Point nextMove[] = new Point[3]; //the move that will be returned
		
		//initialize alpha, beta and max
		float alpha = -Float.MAX_VALUE;
		float beta = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		//loop through the board
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				
				boolean brk = false; //boolean for the breaking if pruned
				//check if the piece belongs to the player
				if ((playerNum == 1 && board[i][j] > 0) || (playerNum == 2 && board[i][j] < 0)){
					
					//gets the moves for the piece
					Point[] moves = Globals.pcMoves.calculateMoves(i, j, board);
		
					//loops through all the moves
					for (int k = 0; k < moves.length; k++){

						Point tmp = moves[k];
												
						//move piece to location
						int nLocOPc = board[tmp.x][tmp.y];
						board[tmp.x][tmp.y] = board[i][j];
						board[i][j] = 0;
						
						//checks castling and stores old 
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
						else if (board[i][j] == 4 && playerNum == 1){
							if (i == 7 && j == 0){
								Globals.p1.setCastling(0, true);
							}else if (i == 7 && j == 7){
								Globals.p1.setCastling(2, true);
							}
						}
						
						boolean promotion = false;
						//if piece is promoted
						if ((playerNum == 1 && board[tmp.x][tmp.y] == 1 && tmp.x == 0) || (playerNum == 2 && board[tmp.x][tmp.y] == -1 && tmp.x == 7)){
							promotion = true;
						}
						
						///////////////////////////////////////////////////////////////////
						////////////////////En Passant start///////////////////////////////
						///////////////////////////////////////////////////////////////////
						
						//save original state info
						Point pLastMovedPawn = playerNum == 2 ? Globals.p2.getLastMovedPawn() : Globals.p1.getLastMovedPawn();
						boolean enPass = false;
						int enPassSide = 0;
						int takenPiece = 0;
						//check which pawn was moved last
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
						
						//check if en passant performed, and update the board accordingly
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
						
						
						
						//loop through all the possible promotions, or just play move
						for (int p = 0; p < (promotion ? 4 : 1); p++){
							
							//if promotion then replace the pawn with each of the promotion pieces
							if (promotion)
								board[tmp.x][tmp.y] = playerNum == 2 ? -(p+2) : p+2;
								
							//run the minimax
							Minimax minMax = new Minimax(playerNum, board, alpha, beta, plyLevel-1);
							float score = minMax.run();
							
							//if there is no next move yet, or if there is a new high score then update best move
							if (score > max || possibleMoves.size() == 0){
								possibleMoves = new ArrayList<Point[]>();
								Point[] tmpMov = new Point[3];
								tmpMov[0] = new Point(i,j);
								tmpMov[1] = tmp;
								//if the piece was a promotion, then add which piece it was promoted to
								//set special moves
								if (promotion){
									tmpMov[2] = new Point(0, board[tmp.x][tmp.y]);
								}else if (castle){
									tmpMov[2] = new Point(1, -1);
								}else if (enPass){
									tmpMov[2] = new Point(2, enPassSide);
								}
								else
									tmpMov[2] = new Point(-1, -1);
								
								//add to the possible moves arraylist & update the score
								possibleMoves.add(tmpMov);
								max = score;							
							}
							
							/////////////////////////////////////
							//Reset board to the original state//
							/////////////////////////////////////
							
							//reset castling
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
							//reset the castling info
							if (board[tmp.x][tmp.y] == 6 || board[tmp.x][tmp.y] == -6 || board[tmp.x][tmp.y] == 4 || board[tmp.x][tmp.y] == -4){
								for (int r = 0; r < 3; r++){
									if (playerNum == 2){
										Globals.p2.setCastling(r, oldReqs[r]);
									}else if (playerNum == 1){
										Globals.p1.setCastling(r, oldReqs[r]);
									}
										
								}
							}
							
							//if the piece was promoted, change it back to a pawn
							if (promotion)
								board[tmp.x][tmp.y] = playerNum==2 ? -1 : 1;
							
							//if en passant performed, revert the information
							if (enPass){
								if (playerNum == 2){
									board[tmp.x-1][tmp.y] = takenPiece;
									Globals.p2.setLastMovedPawn(pLastMovedPawn);
								}else if (playerNum == 1){
									board[tmp.x+1][tmp.y] = takenPiece;
									Globals.p1.setLastMovedPawn(pLastMovedPawn);
								}
							}
							
							//move piece back to original location
							board[i][j] = board[tmp.x][tmp.y];
							board[tmp.x][tmp.y] = nLocOPc;
							
							
							//if the pruning happens then break
							if (beta <= max){
								brk = true;
								break;
							}
							//otherwise update alpha
							alpha = Math.max(alpha, max);
						}
						if (brk) //if it broke, break out of the loop
							break;
					}
					
					
					//return the best next move
					if(brk){
						nextMove = possibleMoves.get(new Random().nextInt(possibleMoves.size()));
						return nextMove;
					}
				}
			}
		}
		//return the next move from the possible moves
		nextMove = possibleMoves.get(new Random().nextInt(possibleMoves.size()));
		return nextMove;
	}
}
