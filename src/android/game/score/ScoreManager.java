package android.game.score;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScoreManager {

    private static final String DATABASE_TABLE_SCORES = "scores";
    private static final String DATABASE_TABLE_SCORES_ID = "id";
    private static final String DATABASE_TABLE_SCORES_NAME = "name";
    public static final String DATABASE_TABLE_SCORES_SCORE = "score";
    private static final int TOP_SCORE_NB = 10;
    
	public int currentScore;
	private Context ctx;
	private ScoreDBHelper helper;
	
	public ScoreManager(Context context) {
		ctx = context;
		helper = new ScoreDBHelper(ctx);
	}
	
    public Cursor getTopScores()
    {
    	//SQLiteDatabase database = helper.getReadableDatabase();
    	SQLiteDatabase database = helper.getWritableDatabase();
    	SQLiteCursor c = (SQLiteCursor) database.rawQuery("SELECT score FROM scores", null); 
    	//Cursor c = database.rawQuery("SELECT * FROM "+DATABASE_TABLE_SCORES+" ORDER BY "+DATABASE_TABLE_SCORES_SCORE, null);
    	//Cursor c = database.query(DATABASE_TABLE_SCORES, new String[]{DATABASE_TABLE_SCORES_SCORE}, null, null, null, null, null);
    	database.close();
    	return c;
    }
    

    public boolean isTopScore()
    {
    	Cursor c = getTopScores();	

    	if(c.getCount() >= TOP_SCORE_NB)
    	{
    		return currentScore > c.getInt(c.getColumnIndex(DATABASE_TABLE_SCORES_SCORE));
    	}
    	else
    		return true;
    }
    
	public void saveScore(String player)
	{
    	//Cursor c = getTopScores();	
    	
    	SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put(DATABASE_TABLE_SCORES_NAME, player);
        initialValues.put(DATABASE_TABLE_SCORES_SCORE, currentScore);
    	long id = database.insert(DATABASE_TABLE_SCORES, null, initialValues);
    	database.close();
	}
	
	public class ScoreDBHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "data";
		private static final int DATABASE_VERSION = 1;
	    
		public ScoreDBHelper( Context context ) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table "+DATABASE_TABLE_SCORES
            		+" ("+DATABASE_TABLE_SCORES_ID+" integer primary key autoincrement,"
            		+" "+DATABASE_TABLE_SCORES_NAME+" text not null, " 
            		+" "+DATABASE_TABLE_SCORES_SCORE+" integer not null);");
		}

		@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE_SCORES);
            onCreate(db);
		}
		
	}
}
