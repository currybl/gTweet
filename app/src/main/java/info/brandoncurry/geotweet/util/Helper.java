package info.brandoncurry.geotweet.util;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import info.brandoncurry.geotweet.R;

public class Helper {
	
	private static boolean DISPLAY_DEBUG = true;
	
	public static void noConnection(final Context context, boolean calledFromFragment, String message) {
    	
        AlertDialog.Builder ab = null;
    	ab = new AlertDialog.Builder(context);
    	   
    	if (isOnline(context, false, false)){
    		String messageText = "";
        	if (message != null && DISPLAY_DEBUG){
        		messageText = "\n\n" + message;
        	}
        	
    		ab.setMessage(context.getResources().getString(R.string.dialog_connection_description) + messageText);
    	   	ab.setPositiveButton(context.getResources().getString(R.string.ok), null);
    	   	ab.setTitle(context.getResources().getString(R.string.dialog_connection_title));
    	} else {
    		ab.setMessage(context.getResources().getString(R.string.dialog_internet_description));
     	   	ab.setPositiveButton(context.getResources().getString(R.string.ok), null);
     	   	ab.setTitle(context.getResources().getString(R.string.dialog_internet_title));
    	}
    	
    	ab.show();
     }	

    public static void noConnection(final Context context, boolean calledFromFragment) {
        noConnection(context, calledFromFragment, null);
     }
    
    public static boolean isOnline(Context c, boolean calledFromFragment, boolean showDialog) {
    	ConnectivityManager cm = (ConnectivityManager) 
    	c.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo ni = cm.getActiveNetworkInfo();
    	 
    	if (ni != null && ni.isConnected())
    	  return true;
    	else
    	  if (showDialog){
    		  noConnection(c, calledFromFragment);
    	  }
    	  return false;
    }
    
    public static ImageLoader initializeImageLoader(Context c){
    	ImageLoader imageLoader = ImageLoader.getInstance();
    	if (!imageLoader.isInited()){	
    		//creating a configuration for imageloader
    		DisplayImageOptions options = new DisplayImageOptions.Builder()
    		.cacheInMemory(true)
    		.cacheOnDisk(true)
    		.build();
		
    		//set the configuration for imageloader
    		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c)
    		.defaultDisplayImageOptions(options)
    		.build();
    		imageLoader.init(config);
    	}
    	return imageLoader;
    }
    
    @SuppressLint("NewApi")
	public static void revealView(View toBeRevealed, View frame){
		try {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				// get the center for the clipping circle
				int cx = (frame.getLeft() + frame.getRight()) / 2;
				int cy = (frame.getTop() + frame.getBottom()) / 2;

				// get the final radius for the clipping circle
				int finalRadius = Math.max(frame.getWidth(), frame.getHeight());

				// create the animator for this view (the start radius is zero)
				Animator anim = ViewAnimationUtils.createCircularReveal(
						toBeRevealed, cx, cy, 0, finalRadius);

				// make the view visible and start the animation
				toBeRevealed.setVisibility(View.VISIBLE);
				anim.start();
			} else {
				toBeRevealed.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    
}
