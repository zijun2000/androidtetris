package android.game.tetris;

import android.content.Context;
import android.content.DialogInterface;
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
	private int mTicks;			//number of ticks that have been calculated
	private int mSeconds;		//number of seconds of gameplay
	private int mLastGravity;	//allow updates of shape independantly from gravity by checking this
    private Paint mPaint;		//paint object to use in draws.
    
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
	public TetrisView(Context context) {
		//init view obj
		super(context);
		setBackgroundColor(Color.BLACK);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		
		//inst objs
		grid = new TetrisGrid();
		currentShape = new TetrisShape(grid);
		mPaint = new Paint();
		
		//init
		init();
	}
	
	/**
     * Init members
	 */
	private void init() {

		//init members
		currentAction = ACTION_NONE;
		mNextUpdate = 0;
		mTicks = 0;
		mSeconds = 0;
		mLastGravity = 1;

		grid.init();
		
		ScoreManager.currentScore = 0;

	}

	public void restart() {
		init();
		currentShape.init();
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
					AlertManager.PushAlert(getContext(), new CharSequence[]{"Replay","QuitG"}, new  DialogInterface.OnClickListener(){
	
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// resolve alert
							switch(which)
							{
								default:
								break;
							}
							
							AlertManager.Resolve();
						}
					});
				}

			}
			//normal state
			else if( time > mNextUpdate )
			{
				mNextUpdate = time + 1000 / FRAME_RATE;
				mTicks++;
				currentShape.update(currentAction);
				currentAction = ACTION_NONE;
				if(mTicks/GRAVITY_RATE > mLastGravity || currentShape.IsFalling())
				{
					mLastGravity =  mTicks/GRAVITY_RATE;
					boolean shapeIsLocked = currentShape.addGravity();
					if(shapeIsLocked)
						grid.update();
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
		
		//right hud
		int y = getRight()-HUD_SCORE_TEXT_OFFSET;
		int h = getTop()+MARGIN_TOP+HUD_SCORE_Y_START;
		mPaint.setTextAlign(Align.RIGHT);
		mPaint.setColor(HUD_SCORE_WORD_COLOR);
		canvas.drawText("Score: ", y, h, mPaint);
		h+=HUD_SCORE_INTERLINE;
		mPaint.setColor(HUD_SCORE_NUM_COLOR);
		canvas.drawText(""+ScoreManager.currentScore, y, h, mPaint);
		//normal align
		mPaint.setTextAlign(Align.LEFT);
		
		//make sure draw will be recalled.
		invalidate();
	}





}
