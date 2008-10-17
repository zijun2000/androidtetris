package android.game.tetris;

import android.app.Activity;
import android.database.Cursor;
import android.game.score.ScoreManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.tools.AlertManager;
import android.view.KeyEvent;
import android.view.View;

public class TetrisView extends View implements ITetrisConstants {
	
	//members
	private boolean mHasFocus; 	//gameView is the focus and can be updated with setter below
	public  void setGameFocus( boolean hasFocus ) {	mHasFocus = hasFocus;	}
	private long mNextUpdate; 	//timestamp when next update can be called
	private long mLastGravity;	//allow updates of shape independantly from gravity by checking this
	private int mTicks;			//number of ticks that have been calculated
	private int mSeconds;		//number of seconds of gameplay
    private Paint mPaint;		//paint object to use in draws.
    private Activity mActivityHandle; //save ref to activity to be able to quit from here
    private ScoreManager scoreManager;
    
    
    //game specific
    private TetrisGrid grid; 			//game playfield/grid
    private TetrisShape currentShape;	//current shape controllable by the user
    private int currentAction;			//current gameaction fired by player
    
    /**
     * Constructor
     * 
     * Init View object
     * Instantiate objects
     * 
     * @param context - param needed for View superclass
     */
	public TetrisView(Activity context) {
		//init view obj
		super(context);
		mActivityHandle = context;
		setBackgroundColor(Color.BLACK);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		
		//inst objs
		grid = new TetrisGrid();
		currentShape = new TetrisShape(grid);
		mPaint = new Paint();
		scoreManager = new ScoreManager(context);
		
		//init
		init();
	}
	
	/**
     * Init members
	 */
	private void init() {

		//init members
		currentShape.isInited = false;
		currentAction = ACTION_NONE;
		mNextUpdate = 0;
		mTicks = 0;
		mSeconds = 0;
		mLastGravity = 1;

		grid.init();
		
		scoreManager.currentScore = 0;

	}

	public void restartGame() {
		init();
		currentShape.isGameOver = false;
	}


	public void quitGame() {
		mActivityHandle.finish();
	}
	
	/**
	 * recalculate grid pixel size
	 * 
	 * @note this WILL be called at least once on init
	 * 
	 * @param are all fired by environment
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		grid.setBackGroundDimentions(w, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * Handle key presses (make sure view is focusable)
	 * 
	 * @param are all fired by environment
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		//if DISREGARD_MULTIPLE_KEYPRESSED then check if its the first press of this key
		if(DISREGARD_MULTIPLE_KEYPRESSED && event.getRepeatCount() < 1)
		{
			switch(keyCode)
			{
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_4:
				{
					currentAction = ACTION_STRAFE_LEFT;
					break;
				}
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				case KeyEvent.KEYCODE_6:
				{
					currentAction = ACTION_STRAFE_RIGHT;
					break;
				}
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_2:
				{
					currentAction = ACTION_ROTATE_L;
					break;
				}
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_8:
				{
					currentAction = ACTION_ROTATE_R;
					break;
				}
				case KeyEvent.KEYCODE_5:
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_SPACE:
				case KeyEvent.KEYCODE_DPAD_CENTER:
				{
					currentAction = ACTION_MAKE_FALL;
					break;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Update user actions
	 * Update engine actions (gravity and line check)
	 */
	public void update() {
		long time = System.currentTimeMillis();

		
		if( mHasFocus )
		{
			//manage gameOver
			if(currentShape.isGameOver)
			{
				if(!AlertManager.IsAlertActive())
				{
					//SaveManager.getSingleton(mActivityHandle).saveScore("AAA",25);
					//SaveManager.getSingleton(mActivityHandle).saveScore("AAA",24);
					//SaveManager.getSingleton(mActivityHandle).saveScore("AAA",32);
					//boolean topScore = SaveManager.getSingleton(mActivityHandle).isTopScore(ScoreManager.currentScore);
					//int alertType = (topScore)? AlertManager.TYPE_TOP_SCORE:AlertManager.TYPE_GAME_OVER;
					//AlertManager.PushAlert(this, alertType);
				}

			}
			//normal state
			else if( time > mNextUpdate )
			{
				mNextUpdate = time + 1000 / FRAME_RATE;
				mTicks++;
				currentShape.update(currentAction);
				currentAction = ACTION_NONE;
				if(time - mLastGravity > GRAVITY_RATE || currentShape.IsFalling())
				{
					mLastGravity = time;
					boolean shapeIsLocked = currentShape.addGravity();
					if(shapeIsLocked)
					{
						int points = grid.update();
						if(points != 0)
							scoreManager.currentScore += points;
					}
				}
				
				if(mTicks/FRAME_RATE > mSeconds)
				{
					mSeconds = mTicks/FRAME_RATE;
				}
			}
		}
		else
		{
			//if paused you dont want to unpause and rush into a loop
			mNextUpdate = time + (1000 / OUT_OF_PAUSE_DELAY);
		}
		return;
	}
	
	/**
	 * Paint game
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		
		//so ugly to update in draw but im to lazy to implement a real gameloop
		update(); //TODO seperate update from draw, and call both inside a clean gameloop
		
		super.onDraw(canvas);
		
		//paint elems
		grid.paint(canvas, mPaint);
		
		//paint hud
		
		paintHud(canvas, mPaint);
		
		//make sure draw will be recalled.
		invalidate();
	}

	private void paintHud(Canvas canvas, Paint paint) {
		int x,y,i,offset,tmpX,tmpY;
		//right hud
		//score
		x = getRight()-HUD_SCORE_TEXT_OFFSET;
		y = getTop()+MARGIN_TOP+HUD_SCORE_Y_START;
		mPaint.setTextAlign(Align.RIGHT);
		mPaint.setColor(HUD_SCORE_WORD_COLOR);
		canvas.drawText("Score", x, y, mPaint);
		y+=HUD_SCORE_INTERLINE;
		mPaint.setColor(HUD_SCORE_NUM_COLOR);
		canvas.drawText(""+scoreManager.currentScore, x, y, mPaint);
		
		//next shape
		x = getRight()-HUD_NEXT_TEXT_OFFSET;
		y = getTop()+MARGIN_TOP+HUD_NEXT_WORD_Y_START;
		mPaint.setColor(HUD_NEXT_WORD_COLOR);
		canvas.drawText("Next", x, y, mPaint);
		mPaint.setColor(HUD_NEXT_SHAPE_COLOR);
		x = getRight() - HUD_NEXT_SHAPE_X_START;
		y = getTop()+MARGIN_TOP+HUD_NEXT_SHAPE_Y_START;
		//int t = currentShape.getNextType();
		offset = (currentShape.getNextType()*SHAPE_TABLE_TYPE_OFFSET)+START_ORIENTATION*SHAPE_TABLE_ELEMS_PER_ROW;
		i = 0;
		tmpX = x;
		tmpY = y;
		do {
			canvas.drawRect(tmpX, tmpY, tmpX+HUD_NEXT_SHAPE_CELL_SIZE, tmpY+HUD_NEXT_SHAPE_CELL_SIZE, mPaint);
			switch(SHAPE_TABLE[offset+i])
			{
				case C_LEFT:
					tmpX=x-HUD_NEXT_SHAPE_CELL_OFFSET;
					tmpY=y;
					break;
				case C_RIGHT:
					tmpX=x+HUD_NEXT_SHAPE_CELL_OFFSET;
					tmpY=y;
					break;
				case C_UP:
					tmpX=x;
					tmpY=y-HUD_NEXT_SHAPE_CELL_OFFSET;
					break;
				case C_DOWN:
					tmpX=x;
					tmpY=y+HUD_NEXT_SHAPE_CELL_OFFSET;
					break;
				case C_LEFT+C_DOWN:
					tmpX=x-HUD_NEXT_SHAPE_CELL_OFFSET;
					tmpY=y+HUD_NEXT_SHAPE_CELL_OFFSET;
					break;
				case C_RIGHT+C_DOWN:
					tmpX=x+HUD_NEXT_SHAPE_CELL_OFFSET;
					tmpY=y+HUD_NEXT_SHAPE_CELL_OFFSET;
					break;
				case C_LEFT+C_UP:
					tmpX=x-HUD_NEXT_SHAPE_CELL_OFFSET;
					tmpY=y-HUD_NEXT_SHAPE_CELL_OFFSET;
					break;
				case C_RIGHT+C_UP:
					tmpX=x+HUD_NEXT_SHAPE_CELL_OFFSET;
					tmpY=y-HUD_NEXT_SHAPE_CELL_OFFSET;
					break;
				case C_RIGHT*2:
					tmpX=x-(HUD_NEXT_SHAPE_CELL_OFFSET*2);//i am cheating here and moving to the left for better hud display (anchor is on right)
					tmpY=y;
					break;
				default:
					//need to manage
					break;
			}
			i++;
		} while (i < MAX_ELEMS);

		scoreManager.saveScore("AAA");
		Cursor c = scoreManager.getTopScores();
		c.moveToFirst(); 
		for(int j=0;j<c.getCount();j++)
		{
			int test = c.getInt(c.getColumnIndex(ScoreManager.DATABASE_TABLE_SCORES_SCORE));
			c.moveToNext();
		}
		
		
		//normal align
		mPaint.setTextAlign(Align.LEFT);
	}


}
