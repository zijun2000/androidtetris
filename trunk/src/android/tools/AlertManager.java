package android.tools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.game.tetris.TetrisView;

public class AlertManager {

	public static final int TYPE_GAME_OVER = 0;
	public static final int GAME_OVER_RESTART = DialogInterface.BUTTON1;
	public static final int GAME_OVER_QUIT = DialogInterface.BUTTON2;
	public static final int TYPE_TOP_SCORE = 0;
	
	
	
	private static boolean alertActive = false;

	public static boolean IsAlertActive()
	{
		return alertActive ;
	}
	
	public static void PushAlert(TetrisView v, int type) {
		CharSequence title, msg, ok, cancel;
		OnClickListener l;
		switch(type)
		{
			case TYPE_GAME_OVER:
				title = "GameOver";
				msg = "Lost2";
				ok = "Replay";
				cancel = "QuitG";
				l = getGameOverClickListener(v);
				break;
			default:
				title = "GameOver";
				msg = "GameOver";
				ok = "GameOver";
				cancel = "GameOver";
				l = getGameOverClickListener(v);
				break;
		}
		
		alertActive = true;
		
	    new AlertDialog.Builder(v.getContext())
	      .setInverseBackgroundForced(true)
	      .setCancelable(false)
	      .setTitle(title)
	      .setMessage(msg)
	      .setNegativeButton(cancel, l)
	      .setPositiveButton(ok, l)
	      .show();

	}	

	public static void Resolve() {
		alertActive = false;
	}    
	
	private static OnClickListener getGameOverClickListener(final TetrisView v){
		return new  DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// resolve alert
				switch(which)
				{
					case GAME_OVER_RESTART:
						v.restartGame();
						break;
					case GAME_OVER_QUIT:
						v.quitGame();
						break;
				}
				
				AlertManager.Resolve();
			}
		};
	}
}
