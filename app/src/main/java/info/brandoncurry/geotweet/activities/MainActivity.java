package info.brandoncurry.geotweet.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.widget.CompoundButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import info.brandoncurry.geotweet.R;
import info.brandoncurry.geotweet.fragments.TweetMapFragment;
import info.brandoncurry.geotweet.fragments.TweetsFragment;
import info.brandoncurry.geotweet.ui.TypefaceSpan;

public class MainActivity extends AppCompatActivity implements  ConnectionCallbacks, OnConnectionFailedListener
{

	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;

	// single fragment to set the view
	private Fragment mainFragment;

	// toolbar
	private Toolbar mToolbar;

	// Fragment Manager to handle different results/feeds
	public static FragmentManager fragmentManager;

	public static Menu menu;

	private Location mLocation;

	public static double longitude, latitude;
	public static String searchKey = "";
	public static String currentCity;



	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpApiClient();

	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_actions, menu);

		final SwitchCompat mapToggle = (SwitchCompat) MenuItemCompat.getActionView(menu.findItem(R.id.action_map));
		mapToggle.setThumbResource(R.drawable.ic_action_map);
		mapToggle.setTrackResource(android.R.color.transparent);

		mapToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setFragment(new TweetMapFragment());
					mapToggle.setThumbResource(R.drawable.ic_action_list);
				} else {
					setFragment(new TweetsFragment());
					mapToggle.setThumbResource(R.drawable.ic_action_map);
				}
			}
		});


		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return super.onCreateOptionsMenu(MainActivity.menu);

	}

	public void onBackPressed()//Prompt the user if they want to exit
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						finish();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Exit " + getString(R.string.app_name)).setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

	}


	public void initToolBar()//Set up action bar title, logo and font
	{
		SpannableString customTitle = new SpannableString("  " + getString(R.string.app_name) + " " + currentCity);
		customTitle.setSpan(new TypefaceSpan(MainActivity.this, "OstrichSans-Black.otf"), 0, customTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mToolbar.setTitle(customTitle);
		mToolbar.setLogo(R.drawable.ic_launcher);
		setSupportActionBar(mToolbar);
	}


	public void initView()//Display the starting fragment
	{

		Bundle bundle = new Bundle();

		String lat = String.valueOf(latitude);
		String lon = String.valueOf(longitude);
		String key = searchKey;

		Log.d("Cheese", "Long " + lon);
		Log.d("Cheese", "Lat " + lat);
		Log.d("Cheese", "Key " + key);

		bundle.putString("latitude", lat);
		bundle.putString("longitude", lon);
		bundle.putString("searchKey", key);

		mainFragment = new TweetsFragment();
		mainFragment.setArguments(bundle);

		fragmentManager = getSupportFragmentManager();
		setFragment(mainFragment);
	}


	public static void setFragment(Fragment f)//Set the fragment of the activity
	{
		fragmentManager.beginTransaction().replace(R.id.container, f).commit();
	}

	public void setUpApiClient()
	{
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}
	}

	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	public Location getLocation()
	{
		mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

		if (mLocation != null)
		{
			latitude = mLocation.getLatitude();
			longitude = mLocation.getLongitude();
			getCity(latitude, longitude);
		}
		else//Something is wrong with location service, ask to generate a random location
		{
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
						case DialogInterface.BUTTON_POSITIVE:
							getRandomLocation();
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							//No button clicked
							break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Current location unavailable. Check out tweets from a random location?").setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();
		}

		return mLocation;
	}


	public void getRandomLocation()
	{
		Random r = new Random();
		int rLatitude = r.nextInt(180) - 90 ;
		int rLongitude = r.nextInt(360) - 180;

		latitude = rLatitude;
		longitude = rLongitude;
		getCity(latitude, longitude);
	}
	public void getCity(double latitude, double longitude)
	{
		Geocoder gCoder = new Geocoder(MainActivity.this);
		try
		{
			List<Address> addresses = gCoder.getFromLocation(latitude, longitude, 1);
			if (addresses != null && addresses.size() > 0)
			{
				currentCity = addresses.get(0).getLocality();
				searchKey = currentCity;
			}

		}
		catch(IOException e)
		{

		}
	}

	@Override
	public void onConnected(Bundle bundle)
	{
		getLocation();
		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		initToolBar();
		initView();
	}

	@Override
	public void onConnectionSuspended(int i)
	{

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}
}
