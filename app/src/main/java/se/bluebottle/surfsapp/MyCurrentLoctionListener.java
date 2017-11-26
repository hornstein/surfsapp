package se.bluebottle.surfsapp;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by theho on 25/11/2017.
 */

public class MyCurrentLoctionListener implements android.location.LocationListener {

    private TextView myAwesomeTextView;
    private RequestQueue queue;
    private FirebaseAnalytics mFirebaseAnalytics;

    MyCurrentLoctionListener(TextView myTextView, RequestQueue myqueue, FirebaseAnalytics myanalytics){
        myAwesomeTextView=myTextView;
        queue=myqueue;
        mFirebaseAnalytics=myanalytics;
    }

    public void onLocationChanged(Location location) {
        double latitude=location.getLatitude();
        double longitude=location.getLongitude();

        String myLocation = "Latitude = " + location.getLatitude() + " Longitude = " + location.getLongitude();

        myAwesomeTextView.setText("Location changed...");

        //I make a log to see the results
        Log.e("MY CURRENT LOCATION", myLocation);


        final String url = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/" + String.format(Locale.US, "%.2f", longitude) + "/lat/" + String.format(Locale.US, "%.2f", latitude) + "/data.json";

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
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
                            bundle.putString("spot", "apelviken");
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
                        myAwesomeTextView.setText(errorstring);
                    }
                }
        );

        // add it to the RequestQueue
        queue.add(getRequest);


    }

    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    public void onProviderEnabled(String s) {

    }

    public void onProviderDisabled(String s) {

    }
}