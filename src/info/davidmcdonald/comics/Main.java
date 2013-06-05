package info.davidmcdonald.comics;

import info.davidmcdonald.comics.data.MyDB;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Main extends ListActivity {

	private ArrayList<String> dirEntries = new ArrayList<String>(); // stores names to populate the list view
	private ArrayList<String> dirEntriesTitles = new ArrayList<String>(); // stores names to populate the list view
	private String myDir = "/sdcard/Comics/";
	private File currentDir = new File("/sdcard/Comics/"); // sets the current directory at /sdcard/Comics/
    private MyDB db;
    private int showPosition = 0;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		db = new MyDB(this);
		db.open();
		
        // if orientation is changed then grab the last index accessed
        if(savedInstanceState != null)
        	myDir = savedInstanceState.getString("dir");		
        
		// open the list view to the base comic directory
		browseTo( new File(myDir));
	}
	
	// Go to the Parent Directory
	private void goToParent() {
		
		// if the parent directory exists go to it
		if(this.currentDir.getParent() != null) {
			this.browseTo(this.currentDir.getParentFile());
		}
	}
	
	// Go to specified directory
	private void browseTo(File dir) {
		
		// if the File is a directory then go to it and populate a new list view
		if (dir.isDirectory()) {
			
			//navigate to the comic after the last completed comic
			
			currentDir = dir;
			populateList(dir.listFiles());
		
		// if the File is a 'File' then open it with ComicReader.java
		} else {

			// open file in ComicReader
        	Intent ComicReaderIntent = new Intent(Main.this,ComicReader.class);
        	ComicReaderIntent.putExtra("comic", dir.getPath());
        	startActivity(ComicReaderIntent);	
        }
			 
	}

	// populate the list view
	private void populateList(File[] files)
	{
		// clear the current list
		this.dirEntries.clear();

    	// sort the files so they're shown alphabetically
		for(File file : files) {
			dirEntries.add(file.getPath().replace(currentDir.getAbsolutePath()+"/",""));
		}
		Collections.sort(dirEntries);

		dirEntriesTitles = updateListings(dirEntries); // add extra info to selections
		
		findLatest(dirEntries);
		
		// set the list view to the alphabetized strings
		setListAdapter(new ArrayAdapter<String>(this, R.layout.file_row, dirEntriesTitles));
		getListView().setSelection(showPosition);
	}
	
	private void findLatest(ArrayList<String> dirEntries2) {
		long lastDateIndex = 0;
		showPosition = 0;
		for(int i=0; i<dirEntries.size(); i++ )
		{
			long date = 0;
			if(db.Exists(dirEntries.get(i)) && db.getLastIndex(dirEntries.get(i))!=0)
				date = db.getRecordDate(dirEntries.get(i));
			if (lastDateIndex<date)
				showPosition = i;
		}
	}

	@Override
	// when an item in the list is selected navigate to the destination or open the file 
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		// grab the selected string from the list
		String selectedFileString = this.dirEntries.get(position);
		
		// if the string was the dot then refresh the current directory
		//if(selectedFileString.equals(".")) {
			
			//this.browseTo(this.currentDir);
		
		// if the string was double dots the browse to the parent directory
		//} else if (selectedFileString.equals("..")) {
			
		//	this.goToParent();
		
		// construct a new File with the absolute path and selected string to browse to
		//} else {
		
			File file = null;
			file = new File(currentDir.getAbsolutePath()+"/"+selectedFileString);
			browseTo(file);
		//}
	}
	
	public ArrayList<String> updateListings(ArrayList<String> comics)
	{
		ArrayList<String> newList =  new ArrayList<String>();;
		
		for (String s : comics)
		{				
			if(db.Exists(s)) 
				if(db.getLastIndex(s)==db.getPageCount(s))
					s = s + " - Completed";
				else if(db.getLastIndex(s)!=db.getPageCount(s) && db.getLastIndex(s)!=0)
					s = s + " - At page " + db.getLastIndex(s) + " of " + db.getPageCount(s);
			newList.add(s);
		}
		
		return newList;
	}
	
	public static boolean fileExists(String f) {
        File file = new File(f);
        if(file.exists())
        	return true;
        return false;
	}
	
	public void dumpDB(String in, String out)
	{
		try{
	        FileChannel inChannel = new FileInputStream(in).getChannel();
	        FileChannel outChannel = new FileOutputStream(out).getChannel();
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	        inChannel.close();
	        outChannel.close();
		}
		catch(Exception e){
			Log.d("exception", e.toString());
		}
	}
	
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	// incase the orientation is changed save the index that was last accessed
    	savedInstanceState.putString("dir", currentDir.getPath());
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override 
    public void onPause()
    {
    	super.onPause();
    }
    
    public void onResume()
    {
    	super.onResume();
		browseTo(currentDir);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK)
        	if(!(currentDir.toString().equals("/sdcard/Comics")))
        		this.goToParent();
    	return true;
    }
}