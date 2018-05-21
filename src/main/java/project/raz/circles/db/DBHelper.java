package project.raz.circles.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

// Class for creating the DB.

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String cmd = "Create table "+Constants.TABLE_NAME+" ( "+Constants.COLUMN_ID+" INTEGER PRIMARY KEY , "+
											 Constants.COLUMN_NAME+" TEXT ,"+
											 Constants.COLUMN_ADDRESS+" TEXT ,"+
											 Constants.COLUMN_LAT+" TEXT ,"+
											 Constants.COLUMN_LNG+" TEXT ,"+
											 Constants.COLUMN_DISTANCE+" TEXT ,"+
											 Constants.COLUMN_IMAGEURL+" TEXT );";
											
		
		
										
		
		try { 
					
			db.execSQL(cmd);
			
		} catch (SQLiteException e) {
			e.getCause();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
