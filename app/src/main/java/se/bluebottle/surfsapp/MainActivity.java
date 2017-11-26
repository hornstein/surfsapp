package se.bluebottle.surfsapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        RequestQueue queue = Volley.newRequestQueue(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView myAwesomeTextView = (TextView)findViewById(R.id.textview1);
        myAwesomeTextView.setText("My Awesome App");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MyCurrentLoctionListener locationListener = new MyCurrentLoctionListener(myAwesomeTextView, queue, mFirebaseAnalytics);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: should requestPermissions...
            myAwesomeTextView.setText("Permission denied...");
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


        String locationProvider = LocationManager.GPS_PROVIDER;
        //String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        double latitude=57.0;
        double longitude=12.0;
        if(lastKnownLocation!=null)
        {
            latitude=lastKnownLocation.getLatitude();
            longitude=lastKnownLocation.getLongitude();
        }
        String myLocation = "Latitude = " + latitude + " Longitude = " + longitude;

        myAwesomeTextView.setText(myLocation);

        final String url = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/" + String.format(Locale.US, "%.2f", longitude) + "/lat/" + String.format(Locale.US, "%.2f", latitude) + "/data.json";
        //final String url = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/" + longitude + "/lat/" + latitude + "/data.json";

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        TextView myAwesomeTextView = (TextView)findViewById(R.id.textview1);
                        try {
                            JSONArray timeseries = response.getJSONArray("timeSeries");
                            JSONArray parameters = ((JSONObject)timeseries.get(0)).getJSONArray("parameters");
                            JSONArray wdvalues = ((JSONObject)parameters.get(13)).getJSONArray("values");
                            String wdstring = (wdvalues.get(0)).toString();
                            JSONArray wsvalues = ((JSONObject)parameters.get(14)).getJSONArray("values");
                            String wsstring = (wsvalues.get(0)).toString();
                            myAwesomeTextView.setText("Wind dir: " + wdstring + " speed: " + wsstring);
                            Bundle bundle = new Bundle();
                            bundle.putString("user_id", "1");
                            bundle.putString("spot", "stenudden");
                            bundle.putString("wind_direction", wdstring );
                            bundle.putString("wind_speed", wsstring );
                            mFirebaseAnalytics.logEvent("surf_session", bundle);

                        } catch (JSONException e) {
                            myAwesomeTextView.setText(e.toString());

                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorstring=error.toString();
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);

        mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }
}
