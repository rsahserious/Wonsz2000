import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Snake extends JFrame implements KeyListener
{
	Game game = new Game(getContentPane());

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
		    {
				new Snake();
			}
		});
	}
	
	public Snake()
	{
		JFrame frame = new JFrame("Snake");
		
		frame.addKeyListener(this);
		
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setSize(650, 502);
		frame.setResizable(false);
		frame.setFocusable(true);
		frame.setLocationRelativeTo(null);
		
		frame.add(game);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		game.directionCheck(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}
}