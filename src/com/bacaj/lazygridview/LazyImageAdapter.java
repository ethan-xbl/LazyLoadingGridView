package com.bacaj.lazygridview;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.bacaj.lazygridview.ImageLoader.ImageLoadListener;

public class LazyImageAdapter extends BaseAdapter implements ImageLoadListener {

	private static final int PROGRESSBARINDEX = 0;
	private static final int IMAGEVIEWINDEX = 1;

	
	private Context mContext = null;
	private OnClickListener mItemClickListener;
	private Handler mHandler;
	private ImageLoader mImageLoader = null;
	private File mDirectory;

	/**
	 * Lazy loading image adapter
	 * @param aContext
	 * @param lClickListener click listener to attach to each item
	 * @param lPath the path where the images are located
	 * @throws Exception when path can't be read from or is not a valid directory
	 */
	public LazyImageAdapter(
			Context aContext,
			OnClickListener lClickListener,
			String lPath
		) throws Exception {
		
		mContext = aContext;
		mItemClickListener = lClickListener;
		mDirectory = new File(lPath);
		
		// Do some error checking
		if(!mDirectory.canRead()){
			throw new Exception("Can't read this path");
		}
		else if(!mDirectory.isDirectory()){
			throw new Exception("Path is a not a directory");
		}
		
		mImageLoader = new ImageLoader(this);
		mImageLoader.start();
		mHandler = new Handler();

	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();

		// stop the thread we started
		mImageLoader.stopThread();
	}

	public int getCount() {
		return mDirectory.listFiles().length;
	}

	public Object getItem(int aPosition) {
		String lPath = null;
		File []lFiles = mDirectory.listFiles();
		if(aPosition < lFiles.length){
			lPath = mDirectory.listFiles()[aPosition].getAbsolutePath();
		}
		
		return lPath;
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public View getView(final int aPosition, View aConvertView, ViewGroup parent) {
		final ViewSwitcher lViewSwitcher;
		String lPath = (String)getItem(aPosition);

		// logic for conserving resources see google video on making your ui fast
		// and responsive
		if (null == aConvertView) {
			lViewSwitcher = new ViewSwitcher(mContext);
			lViewSwitcher.setPadding(8, 8, 8, 8);
			
			ProgressBar lProgress = new ProgressBar(mContext);
			lProgress.setLayoutParams(new ViewSwitcher.LayoutParams(80, 80));
			lViewSwitcher.addView(lProgress);
			ImageView lImage = new ImageView(mContext);
			lImage.setLayoutParams(new ViewSwitcher.LayoutParams(100, 100));

			lViewSwitcher.addView(lImage);

			// attach the onclick listener
			lViewSwitcher.setOnClickListener(mItemClickListener);
			
		} else {
			lViewSwitcher = (ViewSwitcher) aConvertView;
		}
	

		ViewTagInformation lTagHolder = (ViewTagInformation) lViewSwitcher
				.getTag();

		if (lTagHolder == null || 
			!lTagHolder.aImagePath.equals(lPath)) {
			// The Tagholder is null meaning this is a first time load
			// or this view is being recycled with a different image
			
			// Create a ViewTag to store information for later
			ViewTagInformation lNewTag = new ViewTagInformation();
			lNewTag.aImagePath = lPath;
			lViewSwitcher.setTag(lNewTag);

			// Grab the image view
			// Have the progress bar display
			// Then queue the image loading
			ImageView lImageView = (ImageView) lViewSwitcher.getChildAt(1);
			lViewSwitcher.setDisplayedChild(PROGRESSBARINDEX);
			mImageLoader.queueImageLoad(lPath, lImageView, lViewSwitcher);
			

		}

		return lViewSwitcher;
	}

	
	public void handleImageLoaded(
			final ViewSwitcher aViewSwitcher,
			final ImageView aImageView, 
			final Bitmap aBitmap) {
		
		// The enqueue the following in the UI thread
		mHandler.post(new Runnable() {
			public void run() {
				
				// set the bitmap in the ImageView
				aImageView.setImageBitmap(aBitmap);
				
				// explicitly tell the view switcher to show the second view
				aViewSwitcher.setDisplayedChild(IMAGEVIEWINDEX);
			}
		});

	}


}

/**
 *  View holder pattern as described in google sample code
 *  we may want to add more attributes to this if the path was
 *  say being stored in a sqlite database
 * @author bacaj
 */
class ViewTagInformation {
	String aImagePath;
}
