package info.brandoncurry.geotweet.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Created by brandon.curry on 12/15/15.
 */
public class Geo
{

    private Context context;

    public String parseLocation(JSONArray coordinates, Context context)
    {
        String serializedLocation = "";
        //double lat = Double.parseDouble(coordinates.get(0))
        try
        {
            //serializedLocation = coordinates.getString(0) + "," + coordinates.getString(1);
            serializedLocation = getCity(coordinates.getDouble(0), coordinates.getDouble(1), context);

        }
        catch(JSONException e)
        {

        }

        return serializedLocation;
    }


    public LatLng parseCoordinates(JSONArray coordinates, Context context)
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


    public String getCity(double latitude, double longitude, Context context)
    {
        String city = "unknown";
        Geocoder gCoder = new Geocoder(context);
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

}
