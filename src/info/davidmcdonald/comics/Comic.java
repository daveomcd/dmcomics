package info.davidmcdonald.comics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Comic {

	private int m_cachedIndex; // cached Pictures index
	private Bitmap m_cachedImage; // cached Picture
	
	private ZipFile m_comic = null; // .CBZ file
	private ArrayList<ZipEntry> m_pages = new ArrayList<ZipEntry>(); // Picture files from the ZipFile
	
	private String m_name = null; 

	// construct a new Comic
	public Comic(String file)
	{
		File tmpFile = new File(file); // build a temporary file
		ZipEntry zipentry = null; // build a ZipEntry
		m_name = tmpFile.getName(); // give comic a name
		
		// attempt to open the ZipFile file
		try {
			
			// construct the ZipFile and open
			m_comic = new ZipFile(tmpFile, ZipFile.OPEN_READ);
			
			// build an enumeration through the gathered entries of the ZipFile
			Enumeration<?> e = m_comic.entries();
			
			// add ONLY picture files to the pages list
			while(e.hasMoreElements())
			{
				// get the next page in the Enumeration
				zipentry = (ZipEntry) e.nextElement();
				
				// make sure the entry ends with a picture extension
				String name = zipentry.toString();
				if ( name.endsWith(".bmp") || name.endsWith(".BMP") || name.endsWith(".gif") || name.endsWith(".GIF") ||
					 name.endsWith(".jpg") || name.endsWith(".JPG") || name.endsWith(".png") || name.endsWith(".PNG")) {
					
					// add the picture to the comic's pages
					m_pages.add(zipentry);
				}
			}
			
			// Reset the ZipEntry to access the first page
			zipentry = m_pages.get(0);
			
			// get the image via bytes
			InputStream is = m_comic.getInputStream(zipentry);
			
			// buffer the image and decode
			BufferedInputStream bis = new BufferedInputStream(is);
			m_cachedImage = BitmapFactory.decodeStream(bis);
			
			m_cachedIndex = 0; // the cached image's index is set
						
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// get comic's file name
	public String getName()
	{
		return m_name;
	}
	
	// get the pages in ZipEntry format
	public ArrayList<ZipEntry> getPages()
	{
		return m_pages;
	}
	
	// get the total number of pages from the comic
	public int getNumPages()
	{
		return m_pages.size()-1; // indexed starting at zero
	}
	
	public Bitmap getCachedImage()
	{
		return m_cachedImage;
	}
	
	// get the image
	public Bitmap getImage(int index) throws IOException
	{
		// if requested image is already cached retrieve it
		if (index == m_cachedIndex)
			return m_cachedImage;
		
		// set the new index to the requested image's index
		m_cachedIndex = index;
		
		// get the image via bytes
		InputStream is = m_comic.getInputStream((ZipEntry) m_pages.get(index));
		
		// buffer the image and decode
		BufferedInputStream bis = new BufferedInputStream(is);
		m_cachedImage = BitmapFactory.decodeStream(bis);
		
		// return the buffered image
		return m_cachedImage;
	}
	
	// close the comic
	public void close() throws IOException
	{
		// close the ZipFile file
		m_comic.close();
	}
	
}
