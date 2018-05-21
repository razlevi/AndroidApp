package project.raz.circles.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
    Service to call Google Places API asynchronous.

    2 actions in the service: search by text, search nearby

 */

public class PlacesService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String PlacesBroadcast = "action.BROADCAST";
    private static final String ACTION_BY_NAME = "action.Name";
    private static final String ACTION_BY_NEARBY = "action.Near";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM_NAME = "EXTRA_PARAM_NAME";
    private static final String EXTRA_PARAM_LOCATION = "EXTRA_PARAM_LOCATION";
    private static final String EXTRA_PARAM_LOCATION2 = "EXTRA_PARAM_LOCATION2";
    public static final String EXTRA_RESULTS = "RESULTS";
    private static final String EXTRA_PARAM_DISTANCE ="DIST" ;

    public PlacesService() {
        super("PlacesService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionName(Context context, String Name) {
        Intent intent = new Intent(context, PlacesService.class);
        intent.setAction(ACTION_BY_NAME);
        intent.putExtra(EXTRA_PARAM_NAME, Name);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionNearBy(Context context, LatLng Location,int dist) {
        Intent intent = new Intent(context, PlacesService.class);
        intent.setAction(ACTION_BY_NEARBY);
        intent.putExtra(EXTRA_PARAM_LOCATION, Location.latitude);
        intent.putExtra(EXTRA_PARAM_LOCATION2, Location.longitude);
        intent.putExtra(EXTRA_PARAM_DISTANCE, dist);

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_BY_NAME.equals(action)) {
                final String name = intent.getStringExtra(EXTRA_PARAM_NAME);
                handleActionName(name);
            } else if (ACTION_BY_NEARBY.equals(action)) {
                double lat = intent.getDoubleExtra(EXTRA_PARAM_LOCATION,0);
                double lon = intent.getDoubleExtra(EXTRA_PARAM_LOCATION2,0);
                int dist = intent.getIntExtra(EXTRA_PARAM_DISTANCE, 500);
                handleActionNearBy(lat,lon,dist);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionName(String Name) {
        String urlString = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
                Name +
                "&key=AIzaSyA4LzX5hK2kzTmvQay-_AzEF5qkg6nHoD8";


        BufferedReader input = null;
        HttpURLConnection httpCon = null;
        InputStream input_stream = null;
        InputStreamReader input_stream_reader = null;
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            httpCon = (HttpURLConnection) url.openConnection();
            if (httpCon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //Log.e(TAG, "Cannot Connect to : " + urlString);
                //return null;
            }

            input_stream = httpCon.getInputStream();
            input_stream_reader = new InputStreamReader(input_stream);
            input = new BufferedReader(input_stream_reader);
            String line;
            while ((line = input.readLine()) != null) {
                response.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input_stream_reader.close();
                    input_stream.close();
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (httpCon != null) {
                    httpCon.disconnect();
                }
            }
        }

        String Result = response.toString();

        Intent intent = new Intent(PlacesBroadcast);
        intent.putExtra(EXTRA_RESULTS,Result);
        sendBroadcast(intent);

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNearBy(double lat, double lon,int dist) {
        String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                lat+ "," + lon +
                "&radius="+dist+"&types=food" +
                "&key=AIzaSyA4LzX5hK2kzTmvQay-_AzEF5qkg6nHoD8";


        BufferedReader input = null;
        HttpURLConnection httpCon = null;
        InputStream input_stream = null;
        InputStreamReader input_stream_reader = null;
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            httpCon = (HttpURLConnection) url.openConnection();
            if (httpCon.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //Log.e(TAG, "Cannot Connect to : " + urlString);
                //return null;
            }

            input_stream = httpCon.getInputStream();
            input_stream_reader = new InputStreamReader(input_stream);
            input = new BufferedReader(input_stream_reader);
            String line;
            while ((line = input.readLine()) != null) {
                response.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input_stream_reader.close();
                    input_stream.close();
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (httpCon != null) {
                    httpCon.disconnect();
                }
            }
        }

        String Result = response.toString();

        Intent intent = new Intent(PlacesBroadcast);
        intent.putExtra(EXTRA_RESULTS,Result);
        sendBroadcast(intent);
    }
}
