package info.davidmcdonald.comics.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class MyDB {

	private SQLiteDatabase db;
	private final Context context;
	private final MyDBhelper dbhelper;
	
	public MyDB(Context c) {
		context = c;
		dbhelper = new MyDBhelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
	}
	
	public void close() {
		db.close();
	}
	
	public void open() throws SQLiteException
	{
		try {
			db = dbhelper.getWritableDatabase();
		} catch(SQLiteException ex) {
			Log.v("Open database exception caught", ex.getMessage());
			db = dbhelper.getReadableDatabase();
		}
	}
	
	public long insertcomic(String title, int bookmark, int count, boolean is_complete )
	{
		try{
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(Constants.TITLE_NAME, title);
			newTaskValue.put(Constants.BOOKMARK, bookmark);
			newTaskValue.put(Constants.PAGE_COUNT, count);
			newTaskValue.put(Constants.IS_COMPLETE, is_complete);
			newTaskValue.put(Constants.DATE_NAME, java.lang.System.currentTimeMillis());
			
			return db.insert(Constants.TABLE_NAME, null, newTaskValue);
		} catch(SQLiteException ex) {
			Log.v("Insert into database exception caught", ex.getMessage());
			return -1;
		}
	}
	
	public long updatecomic( String title, int bookmark, int count, boolean is_complete )
	{
		try{
			ContentValues newTaskValue = new ContentValues();
			newTaskValue.put(Constants.TITLE_NAME, title);
			newTaskValue.put(Constants.BOOKMARK, bookmark);
			newTaskValue.put(Constants.PAGE_COUNT, count);
			newTaskValue.put(Constants.IS_COMPLETE, is_complete);
			newTaskValue.put(Constants.DATE_NAME, java.lang.System.currentTimeMillis());
			
			return db.update(Constants.TABLE_NAME, newTaskValue, "title=\""+title+"\"", null);
		} catch(SQLiteException ex) {
			Log.v("Update into database exception caught", ex.getMessage());
			return -1;
		}
	}
	
	public long getRecordDate(String title)
	{
		Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME + " where title=\""+title+"\"", null);
		cursor.moveToNext();
		int colIndex = cursor.getColumnIndex("recorddate");
		long date = cursor.getInt(colIndex);
		cursor.close();
		return date;
	}
	
	public int getPageCount(String title)
	{
		Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME + " where title=\""+title+"\"", null);
		cursor.moveToNext();
		int colIndex = cursor.getColumnIndex("page_count");
		int pgCount = cursor.getInt(colIndex);
		cursor.close();
		return pgCount;
	}
	
	public int getLastIndex(String title)
	{
		Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME + " where title=\""+title+"\"", null);
		cursor.moveToNext();
		int colIndex = cursor.getColumnIndex("bookmark");
		int index = cursor.getInt(colIndex);
		cursor.close();
		return index;
	}
	
	public int isComplete(String title)
	{
		Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME + " where title=\""+title+"\"", null);
		cursor.moveToNext();
		int colIndex = cursor.getColumnIndex("is_complete");
		int is_complete = cursor.getInt(colIndex);
		cursor.close();
		return is_complete;
	}
	
	public boolean Exists(String title)
	{
		Cursor cursor = db.rawQuery("select 1 from " + Constants.TABLE_NAME + " where title=\""+title+"\"", null);
		boolean exists = (cursor.getCount() > 0);
		cursor.close();
		return exists;
	}
	
	public Cursor getcomics()
	{
		Cursor c = db.query(Constants.TABLE_NAME, null, null, null, null, null, null);
		return c;
	}
}

