
public class Globals {

	public static Piece pcMoves = new Piece(); // piece class to get moves
	public static float pieceVals[] = {1,3,3,5,9,Float.MAX_VALUE}; //values of pieces
	public static int gameEnded = 0; // checks the state of the game, Check/Checkmate/Stalemate
	public static boolean gameOver = true; //checks if the game is over
	public static Player p1 = new Player(); //player 1
	public static Player p2 = new Player(); //player 2
	public static Display d = new Display(); //display to show the GUI
	public static Board b = new Board(); //board to do moves
	
	
	public Globals(){}
	
}

