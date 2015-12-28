package info.brandoncurry.geotweet.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;

import info.brandoncurry.geotweet.R;
import info.brandoncurry.geotweet.fragments.TweetsFragment;
import info.brandoncurry.geotweet.ui.TypefaceSpan;

public class SearchResultsActivity extends AppCompatActivity {

	// single fragment to set the view
	private Fragment mainFragment;

	// toolbar
	private Toolbar mToolbar;

	// Fragment Manager to handle different results/feeds
	public static FragmentManager fragmentManager;

	public static String searchKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		getSearchKey(getIntent());
		initToolBar();
		initView();

	}

	public void initToolBar()//Set up action bar title, logo and font
	{

		SpannableString customTitle = new SpannableString(String.valueOf(searchKey.charAt(0)).toUpperCase() + searchKey.substring(1, searchKey.length()) + " (" + MainActivity.currentCity + ")");//Title case the search key to make it look nice
		customTitle.setSpan(new TypefaceSpan(this, "OstrichSans-Black.otf"), 0, customTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mToolbar.setTitle(customTitle);
		mToolbar.setLogo(R.drawable.ic_launcher);

		setSupportActionBar(mToolbar);

		//Add back navigation for UX
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
	}

	public void initView()
	{
		mainFragment = new TweetsFragment();
		fragmentManager = getSupportFragmentManager();
		setFragment(mainFragment);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		getSearchKey(intent);
	}

	public static void setFragment(Fragment f)
	{
		fragmentManager.beginTransaction().replace(R.id.container, f).commit();
	}


	private void getSearchKey(Intent intent) //Get the searched keyword from the intent and update it in MainActivity
	{
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			searchKey = intent.getStringExtra(SearchManager.QUERY);
			MainActivity.searchKey = searchKey;
		}

	}


}
