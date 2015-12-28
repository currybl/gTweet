package info.brandoncurry.geotweet.fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import info.brandoncurry.geotweet.R;
import info.brandoncurry.geotweet.activities.MainActivity;
import info.brandoncurry.geotweet.data.MapData;
import info.brandoncurry.geotweet.model.Tweet;


public class TweetMapFragment extends Fragment {

    private LinearLayout mapLayout;
    private MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mapLayout = (LinearLayout) inflater.inflate(R.layout.fragment_tweet_map, container, false);

        mMapView = (MapView) mapLayout.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {

                LatLng myPosition = new LatLng(MainActivity.latitude, MainActivity.longitude);//Shows users current position

                googleMap.addMarker(new MarkerOptions().position(myPosition).title("This is you"));

                for (Tweet tweet : MapData.mapTweets)
                {
                    addTweetToMap(tweet);
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 6));
                mMapView.onResume();
            }
        });
        mMapView.onResume();

        return mapLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MapsInitializer.initialize(getActivity());

        googleMap = mMapView.getMap();

    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void addTweetToMap(Tweet t)
    {
        MarkerOptions options = new MarkerOptions().position(t.getGeoCode());
        options.title(t.getmessage());
        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_twitter_circled)));
        googleMap.addMarker(options);
    }

}