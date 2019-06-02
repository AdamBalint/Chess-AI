import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Display extends JPanel implements MouseListener{

	private BufferedImage bg, marble; //background images
	private float alpha = 0.5f; //alpha to change background
	private Point pMoves[] = new Point[0]; //potential move storage for display
	private BufferedImage bPcs[] = new BufferedImage[6]; //store piece images
	private BufferedImage wPcs[] = new BufferedImage[6];
	private BufferedImage blank; //blank piece
	private int piecePromotion = 0; //checks if piece promotion is happening
	private Rectangle promoSlot[] = new Rectangle[4]; //used to check which piece was selected
	private int pcPromo;
	private BufferedImage gameState[] = new BufferedImage[3]; //stores the images for the different states
	
	
	public Display(){
		try{
			//read in all the images
			bg = ImageIO.read(new File("Images/bg.jpg"));
			marble = makeAlpha(ImageIO.read(new File("Images/marble.jpg")));
			String pcsNames[] = {"p","b","n","r","q","k"};
			blank = ImageIO.read(new File("Images/Pieces/blank.png"));
			gameState[0] = ImageIO.read(new File("Images/check.png"));
			gameState[1] = ImageIO.read(new File("Images/cm.png"));
			gameState[2] = ImageIO.read(new File("Images/sm.png"));
			
			for (int i = 0; i < 6; i++){
				wPcs[i] = ImageIO.read(new File("Images/Pieces/w" + pcsNames[i] + ".png"));
				bPcs[i] = ImageIO.read(new File("Images/Pieces/b" + pcsNames[i] + ".png"));
			}
			
		}catch(IOException e){
			
		}
		repaint();
	}
	
	//compose the marble background at 0.5 alpha so that it is slightly translucent
	private BufferedImage makeAlpha(BufferedImage read) {
		BufferedImage ret = new BufferedImage(read.getWidth(), read.getHeight(), java.awt.Transparency.TRANSLUCENT);
		Graphics2D g2d = ret.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2d.drawImage(read,null,0,0);
		g2d.dispose();
		return ret;
	}

	public void paintComponent(Graphics g){
		super.paintComponents(g);
		g.drawImage(bg, 0, 0, 810, 610, null);
		
		
		//Draw player info board
		g.setColor(new Color(0,0,0,200));
		g.fillRect(600, 50, 150, 50);
		g.fillRect(600, 310, 150, 50);
		
		g.setColor(new Color(150,150,150,150));
		g.fillRect(600, 100, 150, 190);
		g.fillRect(600, 360, 150, 190);
		
		//write names
		g.setColor(Color.white);
		g.setFont(new Font ("Arial", Font.BOLD, 20));
		g.drawChars(("White("+ (Globals.p1.isHuman() ? "P)" : "A)")).toCharArray(), 0, 8, 650, 85);
		g.drawChars(("Black("+ (Globals.p2.isHuman() ? "P)" : "A)")).toCharArray(), 0, 8, 650, 345);
		
		//draw the turn representation icon
		if (Math.abs(Globals.gameEnded) == 1) //for check
			g.drawImage(gameState[Math.abs(Globals.gameEnded)-1], 610, Globals.p1.isPlayersTurn() ? 55 : 315, 35, 35, null);
		else if (Math.abs(Globals.gameEnded) == 2) //for checkmate
			g.drawImage(gameState[Math.abs(Globals.gameEnded)-1], 610, Globals.p1.isPlayersTurn() ? 55 : 315, 35, 35, null);
		else if (Math.abs(Globals.gameEnded) == 3) //for stalemate
			g.drawImage(gameState[Math.abs(Globals.gameEnded)-1], 610, Globals.p1.isPlayersTurn() ? 55 : 315, 35, 35, null);
		else{ //for all other options
			g.setColor(Color.red);
			g.fillOval(615,  Globals.p1.isPlayersTurn() ? 70 : 330, 15, 15);
		}
		
		//Draw the board
		for (int i = 0; i < 8;  i++){
			for (int j = 0; j < 8; j++){
				g.setColor((i+j) % 2 == 0 ? Color.white : Color.black);
				g.fillRect(50+j*62, 50+i*62, 62, 62);				
			}
		}
		//if there are possible moves for the square then draw over with a green
		for (int k = 0; k < pMoves.length; k++){
				g.setColor(new Color (0,255,0,150));
				g.fillRect(50+pMoves[k].y*62, 50+pMoves[k].x*62, 62, 62);
		}
		//add the texture to the board
		g.drawImage(marble, 50, 50, 496, 496, null);
		
		//draw the pieces
		int[][] board = Globals.b.getBoard();
		for (int i = 0; i < 8; i++){
			for (int j = 0; j < 8; j++){
				int val = board[j][i];
				if (val == 0)
					g.drawImage(blank, 50+i*62, 50+j*62, 62, 62, null);
				else
					g.drawImage(val > 0 ? wPcs[val-1] : bPcs[(val*-1)-1], 50+i*62, 50+j*62, 62, 62, null);
			}
		}
		
		//display the captured pieces in the box
		ArrayList<Integer> p1Caps = Globals.p1.getCaptures();
		ArrayList<Integer> p2Caps = Globals.p2.getCaptures();
		int disp = -1;
		if (p1Caps.size() != 0){
			for (int i = 0; i < p1Caps.size(); i++){
				if (i%4 == 0)
					disp++;
				g.drawImage(bPcs[Math.abs(p1Caps.get(i))-1], 605 + (35 * (i%4)), 110 + (35 * disp), 30, 30, null);	
			}
		}else{
			g.drawImage(blank, 610, 110, 62, 62, null);
		}
		disp = -1;
		if (p2Caps.size() != 0){
			for (int i = 0; i < p2Caps.size(); i++){
				if (i%4 == 0)
					disp++;
				g.drawImage(wPcs[Math.abs(p2Caps.get(i))-1], 605 + (35 * (i%4)), 370 + (35 * disp), 30, 30, null);
			}
		}else{
			g.drawImage(blank, 610, 370, 62, 62, null);
		}
		
		//if a piece is being promoted then display the piece promotion screen over the board
		if (piecePromotion != 0){
			g.setColor(new Color(0,0,0,230));
			g.fillRect(100, 223, 400, 50);
			g.setColor(Color.white);
			g.drawChars("Select a piece to promote to".toCharArray(), 0, 28, 160, 255);
			
			g.setColor(new Color(150,150,150,200));
			g.fillRect(100, 273, 400, 100);
			//draw the pieces for the player being promoted
			if (piecePromotion == 1)
				for (int i = 0; i < 4; i++){
					promoSlot[i] = new Rectangle(120+i*95, 279, 62,62);
					g.drawImage(wPcs[i+1], 120+i*95, 279, 62, 62, null);
				}
			else
				for (int i = 0; i < 4; i++){
					promoSlot[i] = new Rectangle(120+i*95, 279, 62,62);
					g.drawImage(bPcs[i+1], 120+i*95, 279, 62, 62, null);
				}
		}
	}

	//store the possible moves
	public void setPossibleMoves(Point[] p){
		pMoves = p;
		repaint();
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	//gets where the mouse was pressed
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		int x = e.getX();
		int y = e.getY();
		
		//if there isn't a promotion find the closest square and return the value to the board
		if (piecePromotion == 0){
			if (x > 550 || y < 50 || y > 550 || x < 50)
				return;
				
			int closestSquare = Integer.MAX_VALUE;
			int closestX = 0, closestY = 0;
			for (int i = 0; i < 8; i++){
				for (int j = 0; j < 8; j++){
					int squareLoc = (int)(Math.pow((50+31+(i*62)) - x, 2) + Math.pow((50+31+(j*62))-y, 2));
					if (squareLoc < closestSquare){
						closestSquare = squareLoc;
						closestX = j;
						closestY = i;
					}
				}
			}
			Globals.b.squarePressed(closestX, closestY);
		}
		//if there is a promotion, find which piece was selected and return to the board
		else{
			
			for (int i = 0; i < 4; i++){
				if (promoSlot[i].contains(new Point(x,y)))
					pcPromo = i+2;
			}
			if (pcPromo != 0){
				if (piecePromotion == -1)
					pcPromo *= -1;
				Globals.b.promotePiece();
			}
		}
		
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	//set if a promotion is going on
	public void setPiecePromotion(boolean white) {
		piecePromotion = white ? 1 : -1;
		repaint();
	}
	
	//returns the piece that was selected
	public int getPromotion(){
		int tmp = pcPromo;
		pcPromo = 0;
		piecePromotion = 0;
		return tmp;
		
	}
	
}
