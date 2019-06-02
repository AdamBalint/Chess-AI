import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.Timer;
import java.util.concurrent.ExecutionException;


public class Board implements ActionListener{
	
	private int board[][] = new int[8][8]; //the board
	private Point selected; //which piece is selected
	private ArrayList<Point> selectedMoves = new ArrayList<Point>(); //moves of the selected piece
	private boolean squareSelected = false; //if a square is selected or not
	private boolean pcPromo = false; //if a promotion is happening
	private Point pcPromoLoc; //where the promotion is happening
	private Timer t; //timer to automate AI turns
	
	public Board(){
		//setting up the board and player defaults
		setUpPieces();
		Globals.p1 = new Player();
		Globals.p2 = new Player();
		Globals.p1.setPlayerNumber(1);
		Globals.p1.startTurn();
		Globals.p2.setPlayerNumber(2);

		t = new Timer(500, this);
		t.start();
	}
	
	//resets the board and players with default chess board
	public void resetGame(){
		Globals.p1.setPlayerNumber(1);
		Globals.p1.startTurn();
		Globals.p2.setPlayerNumber(2);
		Globals.p2.endTurn();
		
		Globals.p1.removeCaptures();
		Globals.p2.removeCaptures();
		board = new int[8][8];
		if (board[0][4] != -6)
			Globals.p2.setCastling(1, true);
		if (board[7][4] != 6)
			Globals.p1.setCastling(1, true);
		if (board[0][0] != -4)
			Globals.p2.setCastling(0, true);
		if (board[0][7] != -4)
			Globals.p2.setCastling(2, true);
		if (board[7][0] != 4)
			Globals.p1.setCastling(0, true);
		if (board[7][7] != 4)
			Globals.p1.setCastling(2, true);
		setUpPieces();
	}
	
	//resets the board with a custom chess board
	public void resetGame(int[][] tmpBoard){
		Globals.p1.setPlayerNumber(1);
		Globals.p1.startTurn();
		Globals.p2.setPlayerNumber(2);
		Globals.p2.endTurn();
		
		Globals.p1.removeCaptures();
		Globals.p2.removeCaptures();
		board = tmpBoard;
		getCurrentBoardSituation();
		//setUpPieces();
	}
	
	//sets the if the computers are AI or not
	public void setAI(boolean p1, boolean p2){
		Globals.p1.setHuman(p1);
		Globals.p2.setHuman(p2);
		Globals.gameEnded = 0;
		//System.exit(0);
	}
	
	//sets the number of plies the computers use
	public void setPlies(int ply1, int ply2, boolean p1Smart, boolean p2Smart){
		Globals.p1.setPly(ply1);
		Globals.p2.setPly(ply2);
		Globals.p1.setSmartPly(p1Smart);
		Globals.p2.setSmartPly(p2Smart);
	}
	
	//sets up the default board configuration
	private void setUpPieces() {
		// TODO Auto-generated method stub
		int blackBack[] = {-4,-3,-2,-5,-6,-2,-3,-4};
		int blackFront[] = {-1,-1,-1,-1,-1,-1,-1,-1};
		int whiteBack[] = {4,3,2,5,6,2,3,4};
		int whiteFront[] = {1,1,1,1,1,1,1,1};
		board[0] = blackBack;
		board[1] = blackFront;
		board[6] = whiteFront;
		board[7] = whiteBack;
		
		//checks if there is check, checkmate or stalemate
		getCurrentBoardSituation();

	}
	
	//starts the timer
	public void startGame(){
		t.start();
	}
	
	//called when a move is made
	private void gameMove(int x, int y){
		//if there is no piece promotion, and a selected piece is clicked, then deselect
		if (!pcPromo){
			if (squareSelected){
				if (selected.getX() == x && selected.getY() == y){
					resetSelected();
					return;
				} 
			}
			
			//For White
			if (Globals.p1.isPlayersTurn()){
				//Player 1 AI
				if (!Globals.p1.isHuman()){
					Point[] nextMove;
					try {
						//set the AI to taking turn and get the next move
						Globals.p1.setTakingTurn(true);
						nextMove = Globals.p1.decideMove(board);
						
						//next move consists of the current location, the next location and
						//information relating to special moves if used (castling, piece promotion, etc)
						Point c = nextMove[0];
						Point n = nextMove[1];
						Point spc = nextMove[2];
						
						//checks for castling and moving of the kings and rooks
						if (board[c.x][c.y] == 6 && Math.abs(y - c.y) == 2){
							//queen side castle
							if (c.y > y){
								board[n.x][n.y+1] = board[n.x][0];
								board[n.x][0] = 0;
								Globals.p1.setCastling(0, true); // set the rook to moved
								Globals.p1.setCastling(1, true); // set the king to moved
							}
							//king side castle
							else{
								board[n.x][n.y-1] = board[n.x][7];
								board[n.x][7] = 0;
								Globals.p1.setCastling(2, true); // set the rook to moved
								Globals.p1.setCastling(1, true); // set the king to moved
							}
								
						}
						//if the king is moved
						else if(board[c.x][c.y] == 6){
							Globals.p1.setCastling(1, true);
						}
						//if the left rook is moved
						else if (c.x == 7 && c.y == 0){
							Globals.p1.setCastling(0, true);
						}
						//if the right rook is moved
						else if (c.x == 7 && c.y == 7){
							Globals.p1.setCastling(2, true);
						}
						
						//checks for en passant
						boolean enPass = false;
						if (spc.x == 2){
							enPass = true;
							Globals.p1.addCapture(board[n.x+1][n.y]);
							board[n.x+1][n.y] = 0;
						}
						
						if (board[n.x][n.y] != 0 && !enPass){
							Globals.p1.addCapture(board[n.x][n.y]);
						}
						
						//moves the piece
						board[n.x][n.y] = board[c.x][c.y];
						board[c.x][c.y] = 0;
						
						//if there was a promotion, then replace by promoted piece
						if (spc.x == 0)
							board[n.x][n.y] = spc.y;
						
						Globals.p1.setTakingTurn(false);
						resetSelected();
						getCurrentBoardSituation();
						Globals.p1.endTurn();
						Globals.p2.startTurn();
						return;
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					
				}
				//Player 1 Human
				else{
					if (squareSelected && selectedMoves.contains(new Point(x,y))){				
						
						//if the king is selected and the distance of the move is 2,, then castle
						if (board[selected.x][selected.y] == 6 && Math.abs(y - selected.y) == 2){
							//queen side castle
							if (selected.y > y){
								board[x][y+1] = board[x][0];
								board[x][0] = 0;
								Globals.p1.setCastling(0, true); // set the rook to moved
								Globals.p1.setCastling(1, true); // set the king to moved
							}
							//king side castle
							else{
								board[x][y-1] = board[x][7];
								board[x][7] = 0;
								Globals.p1.setCastling(2, true); // set the rook to moved
								Globals.p1.setCastling(1, true); // set the king to moved
							}
								
						}
						//if the king is moved
						else if(board[selected.x][selected.y] == 6){
							Globals.p1.setCastling(1, true);
						}
						//if the left rook is moved
						else if (selected.x == 7 && selected.y == 0){
							Globals.p1.setCastling(0, true);
						}
						//if the right rook is moved
						else if (selected.x == 7 && selected.y == 7){
							Globals.p1.setCastling(2, true);
						}
						
						// update last pawn moved
						if (board[selected.x][selected.y] == 1  && Math.abs(selected.x - x) == 2)
							Globals.p1.setLastMovedPawn(new Point(x,y));
						else
							Globals.p1.setLastMovedPawn(new Point(-1,-1));
						
						//en passent logic
						boolean enPass = false;
						if (board[selected.x][selected.y] == 1 && Math.abs(y-selected.y) == 1 && Math.abs(x-selected.x) == 1 && board[x][y] == 0){
							enPass = true;
							Globals.p1.addCapture(board[x+1][y]);
							board[x+1][y] = 0;
						}
						
						//Add captured piece to player list
						if (board[x][y] != 0 && !enPass){
							Globals.p1.addCapture(board[x][y]);
						}
						
						//move piece
						board[x][y] = board[selected.x][selected.y];
						board[selected.x][selected.y] = 0;
						
						
						//if there is a promotion, then set promotion
						if (board[x][y] == 1 && x == 0){
							pcPromo = true;
							pcPromoLoc = new Point(x,y);
							Globals.d.setPiecePromotion(true);
						}
						
						//reset selected piece for next player
						resetSelected();
						getCurrentBoardSituation();
						Globals.p1.endTurn(); //end players turn
						Globals.p2.startTurn(); // start other player's turn
						return;			
					}
					//if nothing is selected, then set the selected piece
					else if (board[x][y] > 0){
						resetSelected();
						selected = new Point(x,y);
						squareSelected = true;
						
						selectedMoves.addAll(Arrays.asList(Globals.pcMoves.calculateMoves(x, y, board)));
						Globals.d.setPossibleMoves(selectedMoves.toArray(new Point[0]));
					}
				}
			}
			//For Black
			else if (Globals.p2.isPlayersTurn()){
				//AI
				if (!Globals.p2.isHuman()){
					Point[] nextMove;
					try {
						Globals.p2.setTakingTurn(true);
						nextMove = Globals.p2.decideMove(board); //get next move
						Point c = nextMove[0];
						Point n = nextMove[1];
						Point spc = nextMove[2];
						
						if (board[c.x][c.y] == -6 && Math.abs(y - c.y) == 2){
							//queen side castle
							if (c.y > y){
								board[n.x][n.y+1] = board[n.x][0];
								board[n.x][0] = 0;
								Globals.p2.setCastling(0, true); // set the rook to moved
								Globals.p2.setCastling(1, true); // set the king to moved
							}
							//king side castle
							else{
								board[n.x][n.y-1] = board[n.x][7];
								board[n.x][7] = 0;
								Globals.p2.setCastling(2, true); // set the rook to moved
								Globals.p2.setCastling(1, true); // set the king to moved
							}
								
						}
						//if the king is moved
						else if(board[c.x][c.y] == -6){
							Globals.p2.setCastling(1, true);
						}
						//if the left rook is moved
						else if (c.x == 0 && c.y == 0){
							Globals.p2.setCastling(0, true);
						}
						//if the right rook is moved
						else if (c.x == 0 && c.y == 7){
							Globals.p2.setCastling(2, true);
						}
						
						//checks en passant
						boolean enPass = false;
						if (spc.x == 2){
							enPass = true;
							Globals.p2.addCapture(board[n.x+1][n.y]);
							board[n.x+1][n.y] = 0;
						}
						
						if (board[n.x][n.y] != 0 && !enPass){
							Globals.p2.addCapture(board[n.x][n.y]);
						}
						
						//moves the piece
						board[n.x][n.y] = board[c.x][c.y];
						board[c.x][c.y] = 0;
						if (spc.x == 0)
							board[n.x][n.y] = spc.y;
						
						//end the turn
						Globals.p2.setTakingTurn(false);
						resetSelected();
						getCurrentBoardSituation();
						Globals.p2.endTurn();
						Globals.p1.startTurn();
						return;
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					
				}
				//for human
				else{
					if (squareSelected && selectedMoves.contains(new Point(x,y))){
						//if the king is selected and the distance of the move is 2,, then castle
						if (board[selected.x][selected.y] == -6 && Math.abs(y - selected.y) == 2){
							//queen side castle
							if (selected.y > y){
								board[x][y+1] = board[x][0];
								board[x][0] = 0;
								Globals.p2.setCastling(0, true); // set the rook to moved
								Globals.p2.setCastling(1, true); // set the king to moved
							}
							//king side castle
							else{
								board[x][y-1] = board[x][7];
								board[x][7] = 0;
								Globals.p2.setCastling(2, true); // set the rook to moved
								Globals.p2.setCastling(1, true); // set the king to moved
							}	
						}
						//if the kind is moved
						else if(board[selected.x][selected.y] == -6){
							Globals.p2.setCastling(1, true);
						}
						//if the left rook is moved
						else if (selected.x == 0 && selected.y == 0){
							Globals.p2.setCastling(0, true);
						}
						//if the right rook is moved
						else if (selected.x == 0 && selected.y == 7){
							Globals.p2.setCastling(2, true);
						}
						
						
						// update last pawn moved
						if (board[selected.x][selected.y] == -1 && Math.abs(selected.x - x) == 2)
							Globals.p2.setLastMovedPawn(new Point(x,y));
						else
							Globals.p2.setLastMovedPawn(new Point(-1,-1));
						
						//en passant logic
						boolean enPass = false;
						if (board[selected.x][selected.y] == -1 && Math.abs(y-selected.y) == 1 && Math.abs(x-selected.x) == 1 && board[x][y] == 0){
							enPass = true;
							Globals.p2.addCapture(board[x-1][y]);
							board[x-1][y] = 0;
						}
						
						if (board[x][y] != 0 && !enPass){
							Globals.p2.addCapture(board[x][y]);
						}
						
						//move piece
						board[x][y] = board[selected.x][selected.y];
						board[selected.x][selected.y] = 0;
						
						//if promotion, set so that code runs
						if (board[x][y] == -1 && x == 7){
							pcPromo = true;
							pcPromoLoc = new Point(x,y);
							Globals.d.setPiecePromotion(false);
						}
						
						//end turn
						resetSelected();
						getCurrentBoardSituation();
						Globals.p2.endTurn();
						Globals.p1.startTurn();
						return;
					}
					else if (board[x][y] < 0){
						resetSelected();
						selected = new Point(x,y);
						squareSelected = true;
						
						selectedMoves.addAll(Arrays.asList(Globals.pcMoves.calculateMoves(x, y, board)));
						Globals.d.setPossibleMoves(selectedMoves.toArray(new Point[0]));
						
					}
				}
			}
		}
	}
	
	//square pressed called from the display
	public void squarePressed(int x, int y) {
		if (!Globals.gameOver)
			gameMove(x,y);
		getCurrentBoardSituation();
	}

	//resets the selected piece and the possible moves in the display
	private void resetSelected() {
		// TODO Auto-generated method stub
		selected = null;
		squareSelected = false;
		selectedMoves = new ArrayList<Point>();
		Globals.d.setPossibleMoves(new Point[0]);
		Globals.d.repaint();
	}

	//gets the promoted piece from the display
	public void promotePiece(){
			board[pcPromoLoc.x][pcPromoLoc.y] = Globals.d.getPromotion();
			pcPromo = false;
			pcPromoLoc = null;
	}
	
	public int[][] getBoard(){
		return board;
	}
	
	//checks for checkmate, check and stalemate
	private void getCurrentBoardSituation(){
		//check checkmate
	
		HashSet<Point> wpm = new HashSet<Point>();
		HashSet<Point> wpam = new HashSet<>(); //all the white moves
		HashSet<Point> bpm = new HashSet<Point>(); //all the black moves
		HashSet<Point> bpam = new HashSet<>(); //all the white moves
		ArrayList<Point> wkm = new ArrayList<>(); //black king moves
		ArrayList<Point> bkm = new ArrayList<>(); //black king moves
		Point wKingLoc = new Point(-1,-1);
		Point bKingLoc = new Point(-1,-1);
		ArrayList<Integer[]> pcs = new ArrayList<>();
		
		//gathers information to use
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
				}
			}
		}
		
		//gets the current situation
		int mate = Minimax.isEndGame(wpm, wpam, bpm, bpam, wKingLoc, bKingLoc, pcs, wkm, bkm);
		
		//sets flag depending on what is returned
		if (mate == 2){ 
			Globals.gameEnded = -2;
			Globals.gameOver = true;
			return;
		}else if (mate == -2){ 
			Globals.gameEnded = 2;
			Globals.gameOver = true;
			return;
		}else if (mate == 3){ 
			Globals.gameEnded = 3;
			Globals.gameOver = true;
			return;
		}else if (mate == -3){ 
			Globals.gameEnded = -3;
			Globals.gameOver = true;
			return;
		}else if (mate == 1){
			Globals.gameEnded = 1;
			return;
		}else if (mate == -1){ 
			Globals.gameEnded = -1;
			return;
		}
	
		Globals.gameEnded = 0;		
		Globals.d.repaint();
	}


	//the timer for the AIs
	public void actionPerformed(ActionEvent arg0) {
		if (!Globals.gameOver){
			if ((!Globals.p1.isHuman() && Globals.p1.isPlayersTurn() && !Globals.p1.isTakingTurn())
				|| (!Globals.p2.isHuman() && Globals.p2.isPlayersTurn() && !Globals.p2.isTakingTurn())){
				gameMove(-1,-1);
			}
		}
	}
	
}
