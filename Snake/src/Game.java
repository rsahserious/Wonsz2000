import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.AttributedCharacterIterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class Game extends JPanel
{
	private static final long serialVersionUID = 1L;

	static final short
		DIRECTION_UP = 0,
		DIRECTION_DOWN = 1,
		DIRECTION_LEFT = 2,
		DIRECTION_RIGHT = 3;
	
	static final int MAX_AREA_X = 17;
	static final int MAX_AREA_Y = 18;
	static final int AREA_OFFSET_X = 223;
	static final int AREA_OFFSET_Y = 30;
	static final int MAX_SNAKE_LENGTH = MAX_AREA_X * MAX_AREA_Y;
	int UPDATE_TIME = 140;
	static final int COLLISION_DELAY = 4;
	static final int TILE_SIZE = 23;
	static final int MAX_APPLES = MAX_AREA_Y * MAX_AREA_Y;
	static final int GAME_STATE_READY = 0;
	static final int GAME_STATE_RUNNING = 1;
	static final int GAME_STATE_FINISHED = 2;
	static final int GAME_STATE_IDLE = 3;
	
	int direction;
	int position[][] = new int[MAX_SNAKE_LENGTH][2];
	int length;
	int directionNext;
	int apple[][] = new int[MAX_APPLES][2];
	int collisionCount;
	boolean skipFrame = false;
	boolean level[][] = new boolean[MAX_AREA_X][MAX_AREA_Y];
	int gameState;
	double currentProgress;
	double progressFactor = 0.3;
	int currentLevel;
	int maxLevels;
	int points;
	int lives;
	int appleMultiplier = 1;
	int applesAtStart = 1;
	
	Timer updateTimer;
	Timer progressTimer;
	
	JProgressBar progressBar;
	JLabel l_points;
	JLabel l_levellabel;
	JLabel l_liveslabel;
		
	public Game(Container test)
	{
		maxLevels = getMaxLevels();
		
		setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setVisible(true);
		progressBar.setBounds(20, 140, 160, 35);
		add(progressBar);
		
		l_points = new JLabel("0", JLabel.CENTER);
		l_points.setVisible(true);
		l_points.setBounds(20, 180, 160, 40);
		l_points.setForeground(new Color(230, 230, 230));
		l_points.setFont(new Font("Tahoma", Font.BOLD, 32));
		add(l_points);
		
		l_levellabel = new JLabel("LEVEL", JLabel.CENTER);
		l_levellabel.setVisible(true);
		l_levellabel.setBounds(20, 235, 72, 50);
		l_levellabel.setForeground(new Color(200, 200, 200));
		l_levellabel.setFont(new Font("Tahoma", Font.BOLD, 18));
		add(l_levellabel);
		
		l_liveslabel = new JLabel("LIVES", JLabel.CENTER);
		l_liveslabel.setVisible(true);
		l_liveslabel.setBounds(110, 235, 72, 50);
		l_liveslabel.setForeground(new Color(200, 200, 200));
		l_liveslabel.setFont(new Font("Tahoma", Font.BOLD, 18));
		add(l_liveslabel);
		
		setBackground(Color.blue);
		setFocusable(true);
		requestFocusInWindow();

		restartGame();
		initGame();
	}
	
	private void restartGame()
	{
		currentLevel = 1;
		points = 0;
		lives = 3;
	}

	private void initGame()
	{
		direction = DIRECTION_UP;
		length = 4;
		directionNext = direction;
		collisionCount = 0;
		gameState = GAME_STATE_READY;
		currentProgress = 0.0;
		
		progressBar.setValue(0);

		position[0][0] = 8;
		position[0][1] = 18;
		
		position[1][0] = 8;
		position[1][1] = 19;
		
		position[2][0] = 8;
		position[2][1] = 20;
		
		position[3][0] = 8;
		position[3][1] = 21;
		
		loadLevel(currentLevel);
		clearApples();
		
		for(int i = 0; i < applesAtStart; i++) createRandomApple();
	}
	
	private void startGame()
	{
		gameState = GAME_STATE_RUNNING;
		
		updateTimer = new Timer();
		updateTimer.schedule(new UpdateTimer(), 0, UPDATE_TIME);
		
		progressTimer = new Timer();
		progressTimer.schedule(new ProgressTimer(), 0, 20);
	}
	
	private void stopGame()
	{
		gameState = GAME_STATE_IDLE;
		
		updateTimer.cancel();
	}
	
	public class UpdateTimer extends TimerTask
	{
		@Override
		public void run()
		{
			if(!skipFrame) frameRender();
			
			skipFrame = false;
		}
	}
	
	public class ProgressTimer extends TimerTask
	{
		@Override
		public void run()
		{
			updateProgress();
		}
	}
	
	public void updateProgress()
	{
		currentProgress += progressFactor;
		
		if(currentProgress >= 100.0)
		{
			for(int i = 0; i < appleMultiplier; i++)
				createRandomApple();
			
			currentProgress = 0.0;
		}
		
		progressBar.setValue((int)currentProgress);
	}
	
	private boolean checkCollision()
	{
		int nextStep[] = new int[2];
		
		switch(directionNext)
		{
			case DIRECTION_UP:
			{
				nextStep[0] = position[0][0];
				nextStep[1] = position[0][1] - 1;
				break;
			}
			case DIRECTION_DOWN:
			{
				nextStep[0] = position[0][0];
				nextStep[1] = position[0][1] + 1;
				break;
			}
			case DIRECTION_RIGHT:
			{
				nextStep[0] = position[0][0] + 1;
				nextStep[1] = position[0][1];
				break;
			}
			case DIRECTION_LEFT:
			{
				nextStep[0] = position[0][0] - 1;
				nextStep[1] = position[0][1];
				break;
			}
		}
		
		if(nextStep[0] == 8 && nextStep[1] == -1 && isCompleted())
		{
			finish();
			return false;
		}
		
		if(nextStep[0] < 0 || nextStep[0] >= MAX_AREA_X || nextStep[1] < 0 || nextStep[1] >= MAX_AREA_Y) // Goes out of area
			return true;
		
		for(int i = 0; i < length; i++)
			if(nextStep[0] == position[i][0] && nextStep[1] == position[i][1]) // Impacts the snake's tail
				return true;
		
		for(int x = 0; x < MAX_AREA_X; x++)
			for(int y = 0; y < MAX_AREA_Y; y++)
				if(nextStep[0] == x && nextStep[1] == y && level[x][y])
					return true;
		
		return false;
	}
	
	private void finish()
	{
		gameState = GAME_STATE_FINISHED;
		
		updateTimer.schedule(new UpdateTimer(), 0, 40 - ((length / MAX_SNAKE_LENGTH) * 38));
		progressTimer.cancel();
		
		functionLog("finish");
	}

	private int checkApple()
	{
		for(int i = 0; i < MAX_APPLES; i++)
		{
			if(apple[i][0] != -1 && apple[i][1] != -1 && apple[i][0] == position[0][0] && apple[i][1] == position[0][1])
				return i;
		}
		
		return -1;
	}
	
	public void frameRender() 
	{
		switch(gameState)
		{
			case GAME_STATE_RUNNING:
			{
				if(checkCollision())
				{
					// Game over
					
					if(++collisionCount == COLLISION_DELAY)
					{
						progressTimer.cancel();
						updateTimer.cancel();
						
						lives--;
						
						if(lives < 0)
						{
							JOptionPane.showMessageDialog(null, "Game over", "Snake", JOptionPane.ERROR_MESSAGE);
							restartGame();
						}
						
						initGame();
					}
				}
				else
				{
					collisionCount = 0;
					
					// Make step with current direction
					makeStep();
					
					// Check if the snake ate the apple
					int index;
					
					if((index = checkApple()) != -1)
					{
						currentProgress = 0.0;
						
						removeApple(index);
						increaseSnakeLength();
					}
				}
				
				break;
			}
			
			case GAME_STATE_FINISHED:
			{
				directionNext = DIRECTION_UP;
				makeStep();
				givePoints(1);
				
				if(position[length-1][1] < -5)
				{
					stopGame();
					currentLevel++;
					initGame();
				}

				break;
			}
		}
		
		// Repaint
		
		//debug("" + position[0][1] + " " + position[1][1] + " " + position[2][1] + " " + position[3][1] + " " + position[4][1]);
		
		repaint();
		
		//functionLog("frameRender");
	}

	private void makeStep()
	{
		for(int i = length - 1; i > 0; i--)
		{
			position[i][0] = position[i-1][0];
			position[i][1] = position[i-1][1];
		}
		
		direction = directionNext;
		
		switch(direction)
		{
			case DIRECTION_UP:
			{
				position[0][1] -= 1;
				break;
			}
			
			case DIRECTION_DOWN:
			{
				position[0][1] += 1;
				break;
			}
			
			case DIRECTION_LEFT:
			{
				position[0][0] -= 1;
				break;
			}
			
			case DIRECTION_RIGHT:
			{
				position[0][0] += 1;
				break;
			}
		}
	}

	private void debug(String string)
	{
		System.out.println(string);
	}
	
	public void functionLog(String string)
	{
		debug("@Function: " + string);
	}

	private int getSnakeTilePosition(int index, int xy)
	{
		return (xy == 0 ? AREA_OFFSET_X : AREA_OFFSET_Y) + (position[index][xy] * TILE_SIZE);
	}
	
	private int getApplePosition(int index, int xy)
	{
		return (xy == 0 ? AREA_OFFSET_X : AREA_OFFSET_Y) + (apple[index][xy] * TILE_SIZE);
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		Image logoImg = Toolkit.getDefaultToolkit().getImage("img/logo.png");
		Image levelImg = Toolkit.getDefaultToolkit().getImage("img/levelbrick.png");
		Image liveImg = Toolkit.getDefaultToolkit().getImage("img/live.png");
		Image brickImg = Toolkit.getDefaultToolkit().getImage("img/brick.png");
		Image exitImg = Toolkit.getDefaultToolkit().getImage("img/exit.png");
		Image appleImg = Toolkit.getDefaultToolkit().getImage("img/apple.png");
		Image faceImg = null;
		
		// Logo
		
		g.drawImage(logoImg, 0, 0, this);
		
		// Levels
		
		int
			n = maxLevels,
			ly = 0,
			lx = 0;
		
		while(n > 0)
		{
			g.drawImage(levelImg, 20 + (lx * 18), 275 + (ly * 18), this);
			
			if(maxLevels - n == currentLevel - 1)
			{
				g.setColor(new Color(220, 220, 220));
				g.fillRect(20 + (lx * 18) + 2, 275 + (ly * 18) + 2, 15, 15);
			}
			else if(maxLevels - n <= currentLevel - 1)
			{
				g.setColor(new Color(180, 180, 180));
				g.fillRect(20 + (lx * 18) + 2, 275 + (ly * 18) + 2, 15, 15);
			}

			n--;
			ly++;
			
			if(ly >= 10)
			{
				ly = 0;
				lx++;
			}
		}
		
		// Lives
		
		for(int i = 0; i < lives; i++)
		{
			g.drawImage(liveImg, 118, 278 + (i * 17), this);
		}
		
		// Board
		
		g.setColor(new Color(161, 161, 161));
		g.fillRect(AREA_OFFSET_X
				- TILE_SIZE, AREA_OFFSET_Y - TILE_SIZE, MAX_AREA_X * TILE_SIZE + TILE_SIZE, MAX_AREA_Y * TILE_SIZE + TILE_SIZE);
		
		// Bricks

		g.drawImage(exitImg, AREA_OFFSET_X - 2 * TILE_SIZE + (10 * TILE_SIZE), AREA_OFFSET_Y - TILE_SIZE, this);
		
		for(int i = 1; i <= 19; i++)
		{
			if(i != 10 || !isCompleted())
				g.drawImage(brickImg, AREA_OFFSET_X - 2 * TILE_SIZE + (i * TILE_SIZE), AREA_OFFSET_Y - TILE_SIZE, this);
			
			if(i != 10 || isSnakeOut()) 
				g.drawImage(brickImg, AREA_OFFSET_X - 2 * TILE_SIZE + (i * TILE_SIZE), AREA_OFFSET_Y + (18 * TILE_SIZE), this);
		}
		
		for(int i = 1; i <= 18; i++)
		{
			g.drawImage(brickImg, AREA_OFFSET_X - TILE_SIZE, AREA_OFFSET_Y - TILE_SIZE + (i * TILE_SIZE), this);
			g.drawImage(brickImg, AREA_OFFSET_X + 17 * TILE_SIZE, AREA_OFFSET_Y - TILE_SIZE + (i * TILE_SIZE), this);
		}
		
		for(int x = 0; x < MAX_AREA_X; x++)
		{
			for(int y = 0; y < MAX_AREA_Y; y++)
			{
				if(level[x][y])
				{
					g.drawImage(brickImg, AREA_OFFSET_X + (x * TILE_SIZE), AREA_OFFSET_Y + (y * TILE_SIZE), this);
				}
			}
		}
		
		// Apples
				
		for(int i = 0; i < MAX_APPLES; i++)
		{
			if(apple[i][0] == -1 && apple[i][1] == -1)
				continue;
			
			g.drawImage(appleImg, getApplePosition(i, 0) + 5, getApplePosition(i, 1) + 5, this);
		}
		
		// Snake
		
		//debug("" + position[0][0] + " | " + position[0][1]);
		
		for(int i = 0; i < length; i++)
		{
			g.setColor(Color.black);	
			
			g.fillRect(getSnakeTilePosition(i, 0) + 1, getSnakeTilePosition(i, 1) + 1, 21, 21);
			if(i < length - 1) g.fillRect(((getSnakeTilePosition(i, 0) + getSnakeTilePosition(i + 1, 0)) / 2) + 1, ((getSnakeTilePosition(i, 1) + getSnakeTilePosition(i + 1, 1)) / 2) + 1, 21, 21);
		}
		
		for(int i = 0; i < length; i++)
		{
			g.setColor(new Color(230, 230, 230));	
			
			g.fillRect(getSnakeTilePosition(i, 0) + 2, getSnakeTilePosition(i, 1) + 2, 19, 19);
			if(i < length - 1) g.fillRect(((getSnakeTilePosition(i, 0) + getSnakeTilePosition(i + 1, 0)) / 2) + 2, ((getSnakeTilePosition(i, 1) + getSnakeTilePosition(i + 1, 1)) / 2) + 2, 19, 19);
		}
		
		for(int i = 0; i < length; i++)
		{
			g.setColor(new Color(0, 0, 170));	
			
			g.fillRect(getSnakeTilePosition(i, 0) + 3, getSnakeTilePosition(i, 1) + 3, 17, 17);
			if(i < length - 1) g.fillRect(((getSnakeTilePosition(i, 0) + getSnakeTilePosition(i + 1, 0)) / 2) + 3, ((getSnakeTilePosition(i, 1) + getSnakeTilePosition(i + 1, 1)) / 2) + 3, 17, 17);
		}
				
		switch(direction)
		{
			case DIRECTION_DOWN: faceImg = Toolkit.getDefaultToolkit().getImage("img/face_d.png"); break;
			case DIRECTION_UP: faceImg = Toolkit.getDefaultToolkit().getImage("img/face_u.png"); break;
			case DIRECTION_LEFT: faceImg = Toolkit.getDefaultToolkit().getImage("img/face_l.png"); break;
			case DIRECTION_RIGHT: faceImg = Toolkit.getDefaultToolkit().getImage("img/face_r.png"); break;
		}
		
		g.drawImage(faceImg, getSnakeTilePosition(0, 0) + 1, getSnakeTilePosition(0, 1) + 1, this);
	}

	private int getMaxLevels()
	{
		int id = 1;

		while(true)
		{
			File file = new File(getLevelFile(id));
			
			if(file.exists())
				id++;
			else
				break;
		}
		
		return id - 1;
	}

	private boolean isCompleted()
	{
		for(int i = 0; i < MAX_APPLES; i++)
			if(apple[i][0] != -1 && apple[i][1] != -1)
				return false;
		
		return true;
	}

	private boolean isSnakeOut() 
	{
		for(int i = 0; i < length; i++)
			if(position[i][1] == MAX_AREA_Y)
				return false;
			
		return true;
	}

	public void directionCheck(KeyEvent e)
	{
		int key = e.getKeyCode();
		
		switch(key)
		{
			case KeyEvent.VK_LEFT:
			{
				if(direction != DIRECTION_RIGHT)
					directionNext = DIRECTION_LEFT;
				break;
			}
			
			case KeyEvent.VK_RIGHT:
			{
				if(direction != DIRECTION_LEFT)
					directionNext = DIRECTION_RIGHT;
				break;
			}
			
			case KeyEvent.VK_UP:
			{
				if(gameState == GAME_STATE_READY)
				{
					startGame();
					return;
				}
				
				if(direction != DIRECTION_DOWN)
					directionNext = DIRECTION_UP;
				break;
			}
			
			case KeyEvent.VK_DOWN:
			{
				if(direction != DIRECTION_UP)
					directionNext = DIRECTION_DOWN;
				break;
			}
		}
		
		if(gameState == GAME_STATE_RUNNING)
		{
			skipFrame = true;
			frameRender();
		}
	}

	private void createRandomApple()
	{
		for(int i = 0; i < MAX_APPLES; i++)
		{
			if(apple[i][0] == -1 && apple[i][1] == -1)
			{
				apple[i] = getRandomApple();
				//debug("aX: " + apple[i][0] + " | aY: " + apple[i][1]);
				
				break;
			}
		}
	}
	
	private void removeApple(int index)
	{
		apple[index][0] = -1;
		apple[index][1] = -1;
		
		repaint();
	}
	
	private int[] getRandomApple()
	{
		int area[][] = new int[MAX_AREA_X * MAX_AREA_Y][2];
		int idx = 0;
		
		for(int x = 0; x < MAX_AREA_X; x++)
		{
			for(int y = 0; y < MAX_AREA_Y; y++)
			{
				boolean skip = false;
				
				for(int i = 0; i < length; i++)
				{
					if(position[i][0] == x && position[i][1] == y)
					{
						skip = true;
						break;
					}
				}
				
				for(int bx = 0; bx < MAX_AREA_X; bx++)
				{
					for(int by = 0; by < MAX_AREA_Y; by++)
					{
						if(level[bx][by] && bx == x && by == y)
						{
							skip = true;
							break;
						}
					}
				}
				
				for(int i = 0; i < MAX_APPLES; i++)
				{
					if(apple[i][0] == x && apple[i][1] == y)
					{
						skip = true;
						break;
					}
				}
					
				if(!skip)
				{
					area[idx][0] = x;
					area[idx][1] = y;
	
					idx++;
				}
			}
		}
		
		if(idx == 0)
		{
			return new int[] {-1, -1};
		}
		else
		{
			int randomIdx = (int) (Math.random() * idx);
	
			return new int[] {area[randomIdx][0], area[randomIdx][1]};
		}
	}
	
	private void increaseSnakeLength()
	{
		length++;
		givePoints(1);
		
		position[length-1][0] = position[length-2][0];
		position[length-1][1] = position[length-2][1];
		
		functionLog("increaseSnakeLength");
	}
	
	private void givePoints(int p)
	{
		points += p;
		l_points.setText("" + points);
	}

	private String getLevelFile(int id)
	{
		return "levels/" + id + ".lvl";
	}
	
	public void loadLevel(int id)
	{
		try
		{
			FileInputStream fStream = new FileInputStream(getLevelFile(id));
			DataInputStream dStream = new DataInputStream(fStream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(dStream));
			
			// Bricks data
			for(int y = 0; y < MAX_AREA_Y; y++)
			{
				String a = reader.readLine();
				String[] tmp = a.split(" ", MAX_AREA_Y);

				for(int x = 0; x < MAX_AREA_X; x++)
				{
					if(tmp[x].equals("X"))
						level[x][y] = true;
					else
						level[x][y] = false;
				}
			}
			
			reader.readLine();
			
			// Level properties
			String a = reader.readLine();
			String[] tmp = a.split(" ");
			
			UPDATE_TIME = Integer.parseInt(tmp[0]);
			progressFactor = Double.parseDouble(tmp[1]);
			applesAtStart = Integer.parseInt(tmp[2]);
			appleMultiplier = Integer.parseInt(tmp[3]);
			
			reader.close();
			
			functionLog("loadLevel(" + id + ")");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void clearApples()
	{
		for(int i = 0; i < MAX_APPLES; i++)
			removeApple(i);
	}
}



