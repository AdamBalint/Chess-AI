/* 
 * Adam Balint
 * 5141619
 * 2016/01/06
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.*;

public class Main {

	private static int[][] board = null;
	public Main(){
		
		
		//sets up the frame for selecting the option
		JFrame f2 = new JFrame("Chess-o-matic Setup");
		f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f2.getContentPane().setPreferredSize(new Dimension(250,400));
		f2.pack();
		JPanel cont = new JPanel();
		cont.setLayout(new GridLayout(4,1));
		
		JPanel p1 = new JPanel();
		JSlider p1PlySlider = new JSlider(JSlider.HORIZONTAL, 1, 6, 4);
		JCheckBox p1Smart = new JCheckBox("Smart Ply");
		p1Smart.setEnabled(false);
		p1PlySlider.setEnabled(false);
		String [] options = {"Player", "AI"};
		JComboBox<String> p1Options = new JComboBox<>(options);
		p1Options.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (p1Options.getSelectedIndex() == 1){
					p1PlySlider.setEnabled(true);
					p1Smart.setEnabled(true);
				}else{
					p1PlySlider.setEnabled(false);
					p1Smart.setEnabled(false);
					
				}
			}
		});
		
		p1.add(new JLabel("White: "));
		p1.add(p1Options);
		
		p1PlySlider.setMajorTickSpacing(1);
		p1PlySlider.setPaintTicks(true);
		p1PlySlider.setPaintLabels(true);
		p1.add(p1PlySlider);
		
		p1.add(p1Smart);
		JPanel p2 = new JPanel();
		JSlider p2PlySlider = new JSlider(JSlider.HORIZONTAL, 1, 6, 4);
		p2PlySlider.setEnabled(false);
		JCheckBox p2Smart = new JCheckBox("Smart Ply");
		p2Smart.setEnabled(false);
		JComboBox<String> p2Options = new JComboBox<>(options);
		p2Options.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (p2Options.getSelectedIndex() == 1){
					p2PlySlider.setEnabled(true);
					p2Smart.setEnabled(true);
				}else{
					p2PlySlider.setEnabled(false);
					p2Smart.setEnabled(false);
				}
			}
		});
		p2.add(new JLabel("Black: "));
		p2.add(p2Options);
		p2PlySlider.setMajorTickSpacing(1);
		p2PlySlider.setPaintTicks(true);
		p2PlySlider.setPaintLabels(true);
		p2.add(p2PlySlider);
		p2.add(p2Smart);
		
		JPanel randPane = new JPanel();
		
		JButton randButton = new JButton ("Random Color");
		randButton.addActionListener(new ActionListener(){
			//when the random button pressed, this swaps all the 
			//settings between the black and white side
			public void actionPerformed(ActionEvent arg0) {
				int[] selected = new int[2];
				selected[0] = p1Options.getSelectedIndex();
				selected[1] = p2Options.getSelectedIndex();
				int[] ply = new int[2];
				ply[0] = p1PlySlider.getValue();
				ply[1] = p2PlySlider.getValue();
				boolean[] smart = new boolean[2];
				smart[0] = p1Smart.isSelected();
				smart[1] = p2Smart.isSelected();
				int order = new Random().nextInt(100);
				p1Options.setSelectedIndex(selected[order%2]);
				p2Options.setSelectedIndex(selected[1-(order%2)]);
				p1PlySlider.setValue(ply[order%2]);
				p2PlySlider.setValue(ply[1-(order%2)]);
				p1Smart.setSelected((smart[order%2]));
				p2Smart.setSelected((smart[1-(order%2)]));
			}
		});
		randPane.add(randButton);
		JPanel startPane = new JPanel();
		JButton start = new JButton("Start");
		
		
		//Chess game window
		JFrame f = new JFrame("Chess-o-matic");
		//makes sure the AI doesn't keep running if window closed mid game
		f.addWindowListener(new java.awt.event.WindowAdapter(){
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        Globals.gameOver = true;
		    }
		});
		f.getContentPane().add(Globals.d);
		(Globals.d).addMouseListener(Globals.d);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.getContentPane().setPreferredSize(new Dimension(800,600));
		f.pack();
		f.setResizable(false);
		
		start.addActionListener(new ActionListener(){
			//if start pressed, then apply settings to the game and run
			public void actionPerformed(ActionEvent arg0) {
				int p1Ply = p1PlySlider.getValue();
				int p2Ply = p2PlySlider.getValue();
				
				if (p1Ply > 4 || p2Ply > 4){
					int decision = JOptionPane.showConfirmDialog(null, "A Ply greater than 4 has been selected. Making moves may take longer than usual. Would you like to continue?", "Warning", JOptionPane.YES_NO_OPTION);
					if(decision == 1)
						return;
				}
				
				if (board == null)
					Globals.b.resetGame();
				else
					Globals.b.resetGame(board);
				board = null;
				Globals.b.setAI(!p1Options.getSelectedItem().equals("AI"), !p2Options.getSelectedItem().equals("AI"));
				Globals.b.setPlies(p1Ply, p2Ply, true, false);
				
				Globals.gameEnded = 0;
				Globals.gameOver = false;			
				
				f.setVisible(true);
			}
		});
		startPane.add(start);
		JButton exit = new JButton("Exit");
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}	
		});
		
		startPane.add(exit);
		
		//set up custom board window
		JButton custBoard = new JButton("Custom Board");
		custBoard.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//displays custom board window
				JFrame tmp = new JFrame("Board Setup");
				JPanel grid = new JPanel();
				grid.setLayout(new GridLayout(8,8));
				JTextField t[][] = new JTextField[8][8];
				for (int i = 0; i < 8; i++){
					for (int j = 0; j < 8 ; j++){
						t[i][j] = new JTextField(1);
						t[i][j].setBackground((i+j)%2 == 0 ? Color.white : Color.black);
						t[i][j].setForeground((i+j)%2 == 0 ? Color.black : Color.white);
						grid.add(t[i][j]);
					}
				}
				tmp.add(new JLabel("<html>Legend:<br />Uppercase - White<br />Lowercase - black<br />K/k - King<br />Q/q - Queen<br />R/r - Rook<br />N/n - Knight<br />B/b - Bishop<br />P/p - Pawn</html>"), BorderLayout.EAST);
				tmp.add(grid);
				JButton s = new JButton("Save");
				//checks when save button clicked
				s.addActionListener(new ActionListener(){
					//when clicked, the input runs through a converter to convert
					//into the correct representation
					public void actionPerformed(ActionEvent arg0) {
						int[][] tmpBoard = new int[8][8];
						
						for (int i = 0; i < 8; i++){
							for (int j = 0; j < 8; j++){
								String tmpPc = t[i][j].getText();
								if (tmpPc.length() == 0)
									tmpPc = "0";
								if (tmpPc.length() > 1)
									return;
								char pc = tmpPc.toCharArray()[0];
								switch(pc){
								case 'K':
								case 'k':
									tmpBoard[i][j] = pc == 'K' ? 6 : -6;
									break;
								case 'Q':
								case 'q':
									tmpBoard[i][j] = pc == 'Q' ? 5 : -5;
									break;
								case 'R':
								case 'r':
									tmpBoard[i][j] = pc == 'R' ? 4 : -4;
									break;
								case 'N':
								case 'n':
									tmpBoard[i][j] = pc == 'N' ? 3 : -3;
									break;
								case 'B':
								case 'b':
									tmpBoard[i][j] = pc == 'B' ? 2 : -2;
									break;
								case 'P':
								case 'p':
									tmpBoard[i][j] = pc == 'P' ? 1 : -1;
									break;
								default:
									tmpBoard[i][j] = 0;
									break;
								}
							}
						}
						//sets the board as the board to use
						board = tmpBoard;
						tmp.dispose();
					}
					
				});
				tmp.add(s, BorderLayout.SOUTH);
				tmp.pack();
				tmp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				tmp.setVisible(true);
			}
			
		});
		
		//put together the settings window
		randPane.add(custBoard);
		cont.add(p1);
		cont.add(p2);
		
		cont.add(randPane);
		cont.add(startPane);
		f2.add(cont);
		f2.setResizable(false);
		f2.setVisible(true);
		
	}

	public static void main(String[] args) {
		new Main();

	}

}
