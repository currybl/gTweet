package info.brandoncurry.geotweet.adapters;

/**
 *  This class is used to create an adapter of the tweets, and fill the listview
 */

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import info.brandoncurry.geotweet.R;
import info.brandoncurry.geotweet.model.Tweet;
import info.brandoncurry.geotweet.ui.RoundedImageView;
import info.brandoncurry.geotweet.util.Helper;


public class TweetAdapter extends ArrayAdapter<Tweet> {

	private Context context;
	private ArrayList<Tweet> tweets;
	private DisplayImageOptions options;
	ImageLoader imageLoader;

	public TweetAdapter(Context context, int viewResourceId, ArrayList<Tweet> tweets) {
		super(context, viewResourceId, tweets);
		this.context = context;
		this.tweets = tweets;
		imageLoader = Helper.initializeImageLoader(context);
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.placeholder)
		.cacheInMemory(true)
		.cacheOnDisk(true)
		.build();
	}
	
	@Override
	public View getView(int posicao, View view, ViewGroup parent){
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.tweet_list_item, parent, false);
		}

		final Tweet tweet = tweets.get(posicao);
		
		if (tweet != null) {

			TextView username = (TextView) view.findViewById(R.id.name);
			RoundedImageView imagem = (RoundedImageView) view.findViewById(R.id.profilepic);
			ImageView photo = (ImageView) view.findViewById(R.id.media);
			TextView message = (TextView) view.findViewById(R.id.message);
			TextView date = (TextView) view.findViewById(R.id.date);
			TextView location = (TextView) view.findViewById(R.id.location);
			TextView reply = (TextView) view.findViewById(R.id.reply);

			username.setText("@" + tweet.getusername());
			date.setText(tweet.getData());
			message.setText(Html.fromHtml(tweet.getmessage()));

			location.setText(tweet.getLocation());
			imageLoader.displayImage(tweet.geturlProfileImage(), imagem);
			
			if (tweet.getImageUrl() != null)
			{
				photo.setVisibility(View.VISIBLE);
				imageLoader.displayImage(tweet.getImageUrl(), photo, options);
				
				photo.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View arg0)
					{
						tweet.viewMedia(context, tweet);

	                }
	            });
			}
			else
			{
				photo.setImageDrawable(null);
				photo.setVisibility(View.GONE);
			}
			
			view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0)
				{

						tweet.share(context, tweet);
                }
            });
			
			view.findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0)
				{
						tweet.open(context, tweet);
				}
			});

			view.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0)
				{
						tweet.showInfo(context, tweet);
				}
			});

			
			
		}

		return view;
	}
}

