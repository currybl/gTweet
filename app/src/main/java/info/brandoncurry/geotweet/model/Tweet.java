/*
 * @author: Diogo Alves <diogo.alves.ti@gmail.com>
 */

package info.brandoncurry.geotweet.model;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import info.brandoncurry.geotweet.R;
import info.brandoncurry.geotweet.activities.MediaActivity;

/**
 *  This class is used to save & get tweets for the listview, and apply a date format
 */

public class Tweet {
	
	private String name;
	private String userid;
	private String username;
	private String urlProfileImage;
	private String message;
	private String tweetDate;
	private String tweetId;
	private String imageUrl;
	private String location;
	private LatLng geocode;
	private int retweetCount;
	
	public Tweet() {
	}
	
	public String getname() {
		return name;
	}

	public void setname(String name) {
		this.name = name;
	}

	public String getUserid() {return userid;}

	public void setUserid(String userid) { this.userid = userid; }
	
	public String getusername() {
		return username;
	}
	
	public void setusername(String username) {
		this.username = username;
	}
	
	public String geturlProfileImage() {
		return urlProfileImage;
	}

	public void seturlProfileImage(String url) {
		this.urlProfileImage = url;
	}

	public String getmessage() {
		return message;
	}
	
	public void setmessage(String message) {
		this.message = message;
	}
	
	public String getData() {
		return tweetDate;
	}
	
	public void setTweetId(String tweetid) {
		this.tweetId = tweetid;
	}
	
	public String getTweetId() {
		return tweetId;
	}
	
	public void setRetweetCount(int count){
		this.retweetCount = count;
	}
	
	public int getRetweetCount(){
		return retweetCount;
	}
	
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}

	public String getLocation() {return location;}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public void setGeoCode(LatLng geocode) { this.geocode = geocode;}

	public LatLng getGeoCode() {return geocode;}
	
	public void setDate(String date) {
		String dateTimeZone = removeTimeZone(date);
		this.tweetDate = fomatData(dateTimeZone);
	}
	
	@SuppressLint("SimpleDateFormat")
	private String fomatData(String data){
		String strData = null;
		TimeZone tzUTC = TimeZone.getTimeZone("UTC");
		SimpleDateFormat formatEntry = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
		formatEntry.setTimeZone(tzUTC);
		SimpleDateFormat formatFinal = new SimpleDateFormat("EEE, dd/MM/yy, 'at' HH:mm");
		
		try {
			strData = formatFinal.format(formatEntry.parse(data));
		} catch (ParseException e) {
		Log.e("Error parsing data", Log.getStackTraceString(e));
		}
		return strData;
	}

	//Action Methods

	public void share(Context c, Tweet tweet)
	{
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		String link = ("http://twitter.com/" + tweet.getusername() + "/status/" + tweet.getTweetId());
		// this is the text that will be shared
		sendIntent.putExtra(Intent.EXTRA_TEXT, link);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, tweet.getusername() + c.getResources().getString(R.string.tweet_share_header));

		sendIntent.setType("text/plain");
		c.startActivity(Intent.createChooser(sendIntent, c.getResources().getString(R.string.share_header)));
	}

	public void open(Context c, Tweet tweet)
	{
		String link = ("http://twitter.com/" + tweet.getusername() + "/status/" + tweet.getTweetId());
		final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
		c.startActivity(intent);
	}

	public void showInfo(final Context c, final Tweet tweet)
	{
		new AlertDialog.Builder(c)
				.setTitle("Tweet Info")
				.setMessage("Tweet ID: " + tweet.getTweetId() + System.getProperty("line.separator") + System.getProperty("line.separator") + "User ID: " + tweet.getUserid() + System.getProperty("line.separator") + System.getProperty("line.separator") + "Tweet: " + tweet.getmessage())
				.setPositiveButton(R.string.view, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						open(c, tweet);
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which)
					{
						//Just cancel
					}
				})
				.setIcon(R.drawable.ic_action_twitter_circled)
				.show();
	}

	public void viewMedia(Context c, Tweet tweet)
	{
		Intent commentIntent = new Intent(c, MediaActivity.class);
		commentIntent.putExtra(MediaActivity.TYPE, MediaActivity.TYPE_IMG);
		commentIntent.putExtra(MediaActivity.URL, tweet.getImageUrl());
		c.startActivity(commentIntent);
	}
	
	private String removeTimeZone(String date){
		// make it pretty
		return date.replaceFirst("(\\s[+|-]\\d{4})", "");
	}



}
