package info.brandoncurry.geotweet.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.ImageLoader;

import info.brandoncurry.geotweet.R;
import info.brandoncurry.geotweet.ui.TypefaceSpan;
import info.brandoncurry.geotweet.util.Helper;


public class MediaActivity extends AppCompatActivity {
	
	public static String TYPE = "type";
	public static String URL = "url";
	public static int TYPE_VID = 1;
	public static int TYPE_IMG = 2;
	public static int TYPE_AUDIO = 3;
	
	boolean systemUIVisible;

	int type;
	String url;
	Toolbar mToolbar;
    ProgressDialog pDialog;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mDecorView = getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(mOnSystemUiVisibilityChangeListener);
        
        setContentView(R.layout.activity_media);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        initToolBar();
		
        hideSystemUI();
		
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        
        Bundle extras = getIntent().getExtras();
        url = extras.getString(URL);
        type = extras.getInt(TYPE);
        
        if (type == TYPE_VID || type == TYPE_AUDIO){
        	videoView.setVisibility(View.VISIBLE);
        	
            pDialog = new ProgressDialog(this);
            if (type == TYPE_VID)
            	pDialog.setTitle(getResources().getString(R.string.opening_video));
            else
            	pDialog.setTitle(getResources().getString(R.string.opening_audio));
            pDialog.setMessage(getResources().getString(R.string.loading));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            try {
                MediaController mediacontroller = new MediaController(this);
                mediacontroller.setAnchorView(videoView);
                Uri video = Uri.parse(url);
                videoView.setMediaController(mediacontroller);
                videoView.setVideoURI(video);
            } catch (Exception e) {
                e.printStackTrace();
            }

            videoView.requestFocus();
            videoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    pDialog.dismiss();
                    videoView.start();
                }
            });
            
            videoView.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					pDialog.dismiss();
					return false;
				}
            	
            });
            
        } else if (type == TYPE_IMG){
        	imageView.setVisibility(View.VISIBLE);
        	
            imageView.setImageResource(0);
            ImageLoader imageLoader = Helper.initializeImageLoader(this);
            imageLoader.displayImage(url,imageView);
            
            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                	if (systemUIVisible)
                		hideSystemUI();
                	else
                		showSystemUI();
                }
            });
        }


    }

    public void initToolBar()//Set up action bar title, logo and font
    {

        SpannableString customTitle = new SpannableString("Tweet Media");
        customTitle.setSpan(new TypefaceSpan(this, "OstrichSans-Black.otf"), 0, customTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mToolbar.setTitle(customTitle);
        mToolbar.setLogo(R.drawable.ic_launcher);

        setSupportActionBar(mToolbar);

        //Add back navigation for UX
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media, menu);
        MenuItem item = menu.findItem(R.id.miShare);
        ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (url != null){
        	Uri uri = Uri.parse(url); 
        	Intent shareIntent = new Intent();
        	shareIntent.setAction(Intent.ACTION_SEND);
        	shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        
        	if (type == TYPE_IMG){
            	shareIntent.setType("image/*");
        	} if (type == TYPE_VID){
        		shareIntent.setType("video/*");
        	} if (type == TYPE_AUDIO){
        		shareIntent.setType("audio/*");
        	}
        
        	shareAction.setShareIntent(shareIntent);
        }
        
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.download:
			file_download(url, MediaActivity.this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
    public void file_download(String url,Context context) {
        url = url.replace(" ","%20");
        DownloadManager downloadManager = (DownloadManager) ((Activity) context).getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String name = URLUtil.guessFileName(url, null, MimeTypeMap.getFileExtensionFromUrl(url));
        String title = "File";
        if (type == TYPE_IMG){
        	title = getResources().getString(R.string.file_image);
    	} if (type == TYPE_VID){
    		title = getResources().getString(R.string.file_video);
    	} if (type == TYPE_AUDIO){
    		title = getResources().getString(R.string.file_audio);
    	}
    	
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setDescription(getResources().getString(R.string.downloading))
        		.setTitle(title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        downloadManager.enqueue(request);

    }
    
    private View.OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
		@Override
        public void onSystemUiVisibilityChange(int visibility) {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.VISIBLE) {
                showSystemUI();
            } else {
            	hideSystemUI();
            }
        }
    };
    
    @SuppressLint("InlinedApi")
    private void showSystemUI(){
    	getSupportActionBar().show();
        if (android.os.Build.VERSION.SDK_INT >= 19) 
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        systemUIVisible = true;
    }
    
    @SuppressLint("InlinedApi")
    private void hideSystemUI(){
    	if (getSupportActionBar() != null)
    		getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= 19) 
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        systemUIVisible = false;
    }
 
}
