package info.davidmcdonald.comics;

import info.davidmcdonald.comics.data.MyDB;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class ComicReader extends Activity {
	
	private int index = 0;	
    private ImageView img;
    private Comic comic = null;
    private GestureDetector gestureDetector = new GestureDetector( new myGestureDetector()); // gesture detector used for navigating pages
    private MyDB dba;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        dba = new MyDB(this);
        dba.open();
        
        // gather the extras sent to the activity
        Bundle extras = getIntent().getExtras(); 
        
        // initialize the ImageView by id from layout file
        img = (ImageView)findViewById(R.id.imgv1);
        
        // construct a new comic file from the bundled path string
        comic = new Comic(extras.getString("comic"));
        
        // get proper index if the comic has been previously opened
        if(dba.Exists(comic.getName()))
        	index = dba.getLastIndex(comic.getName());
        
        // if orientation is changed then grab the last index accessed
        if(savedInstanceState != null)
        	index = savedInstanceState.getInt("index");

        // attempt to set the ImageView to the comic's cached image
		try {
			img.setImageBitmap(comic.getImage(index));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
                  	
    }
      
    // Private class for gestures
    private class myGestureDetector extends SimpleOnGestureListener {
    	
    	// distances are probably measured in pixels
        private static final int SWIPE_MIN_DISTANCE = 120; // set the minimum distance to trigger a gesture
        private static final int SWIPE_MAX_OFF_PATH = 250; // set the maximum vertical distance allowed for swipe to still trigger a gesture 
        private static final int SWIPE_THRESHOLD_VELOCITY = 200; // set the minimum speed for which a swipe must be going to trigger a gesture
	     
    	@Override
    	// Method that's triggered when someone performs a "Fling" gesture on the device
    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    		
    		float diffAbs = Math.abs(e1.getY() - e2.getY()); // calculate the distance vertically the "fling" gesture traveled
    		float prevDiff = e2.getX() - e1.getX(); // calculate the distance from a left to right "fling"
    		float nextDiff = e1.getX() - e2.getX(); // calculate the distance from a right to left "fling"
    		
    		// if the the user swiped a vertical distance greater than the "SWIPE_MAX_OFF_PATH" then ignore the gesture
    		if(diffAbs > SWIPE_MAX_OFF_PATH)
    			return false;
    		
    		// Swipe from Right to Left
    		if (prevDiff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY ) {
				prevPage();
				return true;
			
			// Swipe from Left to Right
			} else if (nextDiff > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY ) {
				nextPage();
				return true;
			}
			 
			 return false;
         }
         
    	// Method for turning to the previous page
         private void prevPage()
         {
        	 	// if we are not currently at the first page then decrement the index and set the ImageView to the previous page
		    	if(index > 0) {
		    		try { img.setImageBitmap(comic.getImage(--index)); } catch (IOException e) { e.printStackTrace(); }
		    		
		    	// if we are at the first page then ignore the gesture and do nothing
		    	} else {
		    		// do nothing
		    	}

         }
         
         // Method for turning to the next page
         private void nextPage()
         {
        	 	// if we are not currently at the last page then increment the index and set the ImageView to the next page
		    	if(index < comic.getNumPages()) {
		    		try { img.setImageBitmap(comic.getImage(++index)); } catch (IOException e) { e.printStackTrace();	}
		    	
		    	// if we are at the last page then present a message to the user via a TOAST message
		    	} else {
					Toast toast = Toast.makeText(ComicReader.this, "End of File", Toast.LENGTH_SHORT);
			    	toast.show();
		    	}
         }

   }
   
    public boolean onTouchEvent(MotionEvent event) {
	   return gestureDetector.onTouchEvent(event);
   }

    public void saveItToDB(boolean complete) {
    	if (dba.Exists(comic.getName())) {
    		dba.updatecomic(comic.getName(), index, comic.getNumPages() , complete);
    	} else {
    		dba.insertcomic( comic.getName(), index, comic.getNumPages() , complete );
    	}
    	dba.close();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	// incase the orientation is changed save the index that was last accessed
    	savedInstanceState.putInt("index", index);
    	super.onSaveInstanceState(savedInstanceState);
    }

    @Override 
    public void onPause()
    {
    	super.onPause();

    	if (index==comic.getNumPages())
    		saveItToDB(true);
    	else
    		saveItToDB(false);
    	
    }
}