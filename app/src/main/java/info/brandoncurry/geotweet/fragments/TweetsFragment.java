package info.brandoncurry.geotweet.fragments;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import info.brandoncurry.geotweet.R;
import info.brandoncurry.geotweet.activities.MainActivity;
import info.brandoncurry.geotweet.adapters.TweetAdapter;
import info.brandoncurry.geotweet.data.MapData;
import info.brandoncurry.geotweet.model.Tweet;
import info.brandoncurry.geotweet.util.Helper;

/**
 *  This fragment is used to display a list of tweets
 */

public class TweetsFragment extends Fragment {

	private ListView listView;

	public SwipeRefreshLayout layout;//Expose the layout for the AsyncTask
	private View footerView;

	RelativeLayout pDialog;

	TweetAdapter tweetAdapter;
	ArrayList<Tweet> tweets;

	@Override
	public void onResume() {
		super.onResume();
		new SearchTweetsTask().execute(searchValue);
	}

	//API vars
	String searchValue;
	String latesttweetid;
	String count = "15";
	String radius = "5";
	String longitude, latitude;

	//ListView vars
	Boolean initialload = true;
	Boolean isLoading = true;

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		searchValue = MainActivity.searchKey;//This value will always reference it's parent activity for a search term. Default is the name of the current city.
		longitude = String.valueOf(MainActivity.longitude);
		latitude = String.valueOf(MainActivity.latitude);

		layout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_tweets, container, false);
		layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				new SearchTweetsTask().execute(searchValue);

			}
		});
		layout.setColorSchemeResources(R.color.PrimaryColor, R.color.PrimaryLightColor, R.color.smoke);

		listView = (ListView) layout.findViewById(R.id.tweets_list);
	    listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				//Stop the SwipeRefreshView from fetching data while not at the top of the listview
				int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
				layout.setEnabled((topRowVerticalPosition >= 0));
				//Stop the SwipeRefreshView from fetching data while not at the top of the listview

				if (tweetAdapter == null)
					return;

				if (tweetAdapter.getCount() == 0)
					return;

				int l = visibleItemCount + firstVisibleItem;
				if (l >= totalItemCount && !isLoading) {
					// Fetch new data
					isLoading = true;
					new SearchTweetsTask().execute(searchValue);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
		});

		footerView = inflater.inflate(R.layout.listview_footer, null);

	    return layout;

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new SearchTweetsTask().execute(searchValue);
	}

	
	public void updateList() {	
		if (initialload){
			tweetAdapter = new TweetAdapter(getActivity(), R.layout.fragment_tweets_row, tweets);
			listView.setAdapter(tweetAdapter);
			initialload = false;
		} else {
			tweetAdapter.addAll(tweets);
			tweetAdapter.notifyDataSetChanged();
		}
		isLoading = false;
	}
	
	//Connect to twitter api and get values.
	private class SearchTweetsTask extends AsyncTask<String, Void, Void>{



		private String URL_VALUE;
		private final String URL_BASE = "https://api.twitter.com";
		private final String URL_SEARCH = URL_BASE + "/1.1/search/tweets.json?count="+ count +"&geocode=" + latitude + "," + longitude + "," + radius + "mi"  + "&q=";
		private final String URL_PARAM = "&max_id=";
		private final String URL_AUTH = URL_BASE + "/oauth2/token";


		private final String CONSUMER_KEY = getResources().getString(R.string.twitter_api_consumer_key);;
		private final String CONSUMER_SECRET = getResources().getString(R.string.twitter_api_consumer_secret_key);;

		private String authenticateApp(){

			HttpURLConnection connection = null;
			OutputStream os = null;
			BufferedReader br = null;
			StringBuilder reply = null;

			try {
				URL url = new URL(URL_AUTH);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setDoInput(true);

				// Encoding keys
				String credentials = CONSUMER_KEY + ":" + CONSUMER_SECRET;
				String authorisation = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
				String parameter = "grant_type=client_credentials";

				// Sending credentials
				connection.addRequestProperty("Authorization", authorisation);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				connection.connect();
				
				// sending parameters to method
				os = connection.getOutputStream();
				os.write(parameter.getBytes());
				os.flush();
				os.close();

				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				reply = new StringBuilder();

				while ((line = br.readLine()) != null){            
					reply.append(line);	
				}

			} catch (Exception e) {
				
			}finally{
				if (connection != null) {
					connection.disconnect();
				}
			}
			return reply.toString();
		}
		

		//Showing the refresher while loading data in background
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			Log.d("CHEESE", URL_SEARCH);
			//Clear the map and get ready to add new geo points to it
			if(MapData.mapTweets!=null)
			MapData.mapTweets.clear();

			if (initialload){
				pDialog = (RelativeLayout) layout.findViewById(R.id.progressBarHolder);
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
				{
					listView.addFooterView(footerView);
				}
			} else {

				listView.addFooterView(footerView);
			}
		}
		


		//Get the latest tweets from the timeline of the user
		@Override
		protected Void doInBackground(String... param) {

			String searchValue = param[0];

				URL_VALUE = URL_SEARCH;
				try
				{
					searchValue = URLEncoder.encode(searchValue, "UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}


			tweets = new ArrayList<Tweet>();
			HttpURLConnection connection = null;
			BufferedReader br = null;

			try {
				URL url;
				//If tweets were previously loaded, load tweets starting from the last visible tweet
				if (null != latesttweetid && latesttweetid != "")
				{
					Long fromid = Long.parseLong(latesttweetid) - 1;
					url = new URL(URL_VALUE + searchValue + URL_PARAM + Long.toString(fromid));
				}
				else
				{
					url = new URL(URL_VALUE + searchValue);
				}

				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				String jsonString = authenticateApp();
				JSONObject jsonAccess = new JSONObject(jsonString);
				String tokenHolder = jsonAccess.getString("token_type") + " " + jsonAccess.getString("access_token");

				connection.setRequestProperty("Authorization", tokenHolder);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.connect();

				// get tweets from the api
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String line;
				StringBuilder reply = new StringBuilder();

				while ((line = br.readLine()) != null)
				{
					reply.append(line);	
				}
				
				JSONArray jsonArray;
				JSONObject jsonObject;

					JSONObject obj = new JSONObject(reply.toString());
					jsonArray = obj.getJSONArray("statuses");


				for (int i = 0; i < jsonArray.length(); i++)
				{
					
					jsonObject = (JSONObject) jsonArray.get(i);
					Tweet tweet = new Tweet();
					Log.d("USER OBJECT", jsonObject.getJSONObject("user").toString());
					tweet.setname(jsonObject.getJSONObject("user").getString("name"));
					tweet.setUserid(jsonObject.getJSONObject("user").getString("id"));
					tweet.setusername(jsonObject.getJSONObject("user").getString("screen_name"));
					tweet.seturlProfileImage(jsonObject.getJSONObject("user").getString("profile_image_url"));
					tweet.setmessage(jsonObject.getString("text"));
					tweet.setRetweetCount(jsonObject.getInt("retweet_count"));
					tweet.setDate(jsonObject.getString("created_at"));
					tweet.setTweetId(jsonObject.getString("id"));

					if(!jsonObject.isNull("geo"))//Only get geo data if the user had location, otherwise things will get pretty nasty ...
					{
						MapData.mapTweets.add(tweet);
						tweet.setLocation(parseLocation(jsonObject.getJSONObject("geo").getJSONArray("coordinates")));
						tweet.setGeoCode(parseGeoCode(jsonObject.getJSONObject("geo").getJSONArray("coordinates")));
					}
					
					try
					{
						if (jsonObject.has("extended_entities"))
						{
							String mediaurl = ((JSONObject) jsonObject.getJSONObject("extended_entities").getJSONArray("media").get(0)).getString("media_url");
							if (((JSONObject) jsonObject.getJSONObject("extended_entities").getJSONArray("media").get(0)).getString("type").equalsIgnoreCase("photo"))
							{
								tweet.setImageUrl(mediaurl);
							}

						}
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}

					latesttweetid = jsonObject.getString("id");//Last tweet id is the id of the last object

					tweets.add(i, tweet);
				}

			} catch (Exception e)
			{
				e.printStackTrace();

			}
			finally //Close the HTTP Connection when all is done
			{
				if(connection != null)
				{
					connection.disconnect();
				}
			}
			return null;
		}

		//Populate listview with tweets after background task has been completed
		@Override
		protected void onPostExecute(Void result){
			if (tweets != null && !tweets.isEmpty())
			{
				updateList();
			}
			else
			{
				if (initialload == true)
				{
					Helper.noConnection(getActivity(), true);
				}
			}

			if (pDialog.getVisibility() == View.VISIBLE)
			{
				pDialog.setVisibility(View.GONE);
				Helper.revealView(listView,layout);
				
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
				{
					listView.removeFooterView(footerView);
				}
				
			}
			else
			{
				listView.removeFooterView(footerView);
			}


			layout.setRefreshing(false);
		}

	}


	public String parseLocation(JSONArray coordinates) //Convert a geocode from a JSONArray to a city name
	{
		String serializedLocation = "";

		try
		{
			serializedLocation = getCity(coordinates.getDouble(0), coordinates.getDouble(1));

		}
		catch(JSONException e)
		{

		}

		return serializedLocation;
	}


	public String getCity(double latitude, double longitude) //Convert a pair of doubles to a city name
	{
		String city = "unknown";
		Geocoder gCoder = new Geocoder(getActivity());
		try
		{
			List<Address> addresses = gCoder.getFromLocation(latitude, longitude, 1);
			if (addresses != null && addresses.size() > 0)
				city = addresses.get(0).getLocality() + " " + addresses.get(0).getAdminArea();


		}
		catch(IOException e)
		{

		}

			return city;
	}

	public LatLng parseGeoCode(JSONArray coordinates) //Convert a geocode from a JsonArray to a LatLng
	{
		LatLng latlng = null;

		try
		{
			latlng = new LatLng(coordinates.getDouble(0), coordinates.getDouble(1));

		}
		catch(JSONException e)
		{

		}

		return latlng;

	}
	

}
