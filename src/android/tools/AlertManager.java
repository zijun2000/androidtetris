package android.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class AlertManager {
	
	private static boolean alertActive = false;

	public static boolean IsAlertActive()
	{
		return alertActive ;
	}

	public static void PushAlert(Context c, CharSequence[] cS, OnClickListener l) {
	    
			alertActive = true;
			
		    new AlertDialog.Builder(c)
		      .setCancelable(false)
		      .setTitle("Quit")
		      .setMessage("Lost")
		      .setNeutralButton(cS[0], l)
		      .setPositiveButton(cS[1], l)
		      .show();
	}

	public static void Resolve() {
		alertActive = false;
	}    
}
