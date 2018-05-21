package project.raz.circles.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import project.raz.circles.db.DBHandler;
import project.raz.circles.objects.Place;
import project.raz.circles.services.PlacesService;
import project.raz.circles.R;
import project.raz.circles.services.PowerConnectionReceiver;


// This is the main Activity. Is holds 1 or 2 Fragments depending on the screen orientation.
// On Landscape mode - 2 static fragments: Place List and Map fragment.
// On Portable mode - 1 dynamic fragment: Place List that changes to Map fragment on Click.

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, PlaceFrag.OnFragmentInteractionListener {

    protected boolean isPortable = false;
    protected Location mLocation;
    protected GoogleApiClient mGoogleApiClient;
    FrameLayout container;

    protected PlaceFrag frg_PlaceList;
    protected SupportMapFragment frg_Map;

    BroadcastReceiver reciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String results = intent.getStringExtra(PlacesService.EXTRA_RESULTS);
            ParseJSONResults(results);
        }
    };
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private PowerConnectionReceiver powerReciever;

    private void ParseJSONResults(String results) {
        final ArrayList<Place> list = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(results);

            if (json.has("status"))
                if (json.getString("status").equals("OK")) {
                    //Success Request
                    JSONArray results_array = json.getJSONArray("results");

                    for (int i = 0; i < results_array.length(); i++) {

                        Place obj = new Place();
                        JSONObject result = results_array.getJSONObject(i);


                        if (result.has("formatted_address")) {
                            String f_add = result.getString("formatted_address");
                            obj.setFormatted_address(f_add);
                        }

                        if (result.has("vicinity")) {
                            String f_add = result.getString("vicinity");
                            obj.setFormatted_address(f_add);
                        }


                        if (result.has("name")) {
                            String nam = result.getString("name");
                            obj.setName(nam);
                        }

                        if (result.has("photos")) {
                            JSONArray photos_array = result.getJSONArray("photos");
                            if (photos_array.length() > 0) {
                                JSONObject ref = photos_array.getJSONObject(0);
                                String photo_ref = ref.getString("photo_reference");
                                obj.setPhoto_reference(photo_ref);
                            }
                        }

                        if (result.has("geometry")) {
                            JSONObject geo = result.getJSONObject("geometry");
                            JSONObject loc = geo.getJSONObject("location");
                            double lat = loc.getDouble("lat");
                            double lng = loc.getDouble("lng");

                            obj.setX(lat);
                            obj.setY(lng);
                        }

                        if (mLocation != null) {
                            obj.setDistance(distFrom(obj.getX(), obj.getY(), mLocation.getLatitude(), mLocation.getLongitude()));
                        }
                        list.add(obj);

                    }
                } else {
                    Toast.makeText(MainActivity.this, "Bad Request try again ...", Toast.LENGTH_LONG).show();
                }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Response Not a JSON format ...", Toast.LENGTH_LONG).show();
        }

        if (!isPortable) {
            PlaceFrag frg = ((PlaceFrag) getSupportFragmentManager().findFragmentById(R.id.list_fragment));
            frg.SetNewList(list);
        } else {
            if (frg_PlaceList != null)
                frg_PlaceList.SetNewList(list);
        }


    }

    public float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; // kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(reciever);
        unregisterReceiver(powerReciever);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            ShowFavorites();
        }


        String orient = getResources().getString(R.string.screen_orientation);
        if (orient.equals("PORT"))
            isPortable = true;
        else
            isPortable = false;

        if (isPortable) {
            container = (FrameLayout) findViewById(R.id.container);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            frg_PlaceList = PlaceFrag.newInstance();
            ft.add(R.id.container, frg_PlaceList);
            ft.commit();
        }

        if (!isPortable)
            setUpMapIfNeeded();

        buildGoogleApiClient();


        Place p = new Place();

        try {
            p.setName(getIntent().getStringExtra("NAME"));
            p.setX(getIntent().getDoubleExtra("X", 0));
            p.setY(getIntent().getDoubleExtra("Y", 0));

            if (!p.getName().equals("")) {
                onPlaceClicked(p);
            }
        } catch (Exception ex) {

        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filterpower = new IntentFilter();
        filterpower.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filterpower.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");

        powerReciever= new PowerConnectionReceiver();
        registerReceiver(powerReciever, filterpower);
        
        if (!isPortable)
            setUpMapIfNeeded();

        IntentFilter filter = new IntentFilter(PlacesService.PlacesBroadcast);
        registerReceiver(reciever, filter);
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            if (isPortable) {
                mMap = frg_Map.getMap();
            } else {
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .getMap();
            }
        }
    }


    @Override
    public void onPlaceClicked(Place p) {
        final LatLng newPlace = new LatLng(p.getX(), p.getY());

        if (isPortable) {

            frg_Map = SupportMapFragment.newInstance();
            frg_Map.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    SetLastPosition(newPlace);
                }
            });

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.add(R.id.container, frg_Map);
            ft.addToBackStack(null);
            ft.commit();

            setUpMapIfNeeded();
        }


        if (mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(newPlace)
                    .title(p.getName()));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPlace, 10));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPlace, 15), 3000,
                    null);
        }
    }

    private void SetLastPosition(LatLng newPlace) {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(newPlace));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPlace, 10));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPlace, 15), 3000,
                    null);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {

            LatLng MyPos = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

            if (isPortable) {
                frg_PlaceList.SetCurrentLocation(MyPos);
            } else {
                PlaceFrag frg = ((PlaceFrag) getSupportFragmentManager().findFragmentById(R.id.list_fragment));
                frg.SetCurrentLocation(MyPos);
            }

            if (mMap != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(MyPos)
                        .title("My Position"));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyPos, 10));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyPos, 15), 3000,
                        null);
            }
        } else
            Toast.makeText(this, "NO LOCATION ", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_delete_favorites:
                deleteFavorites();
                return true;
            case R.id.action_change_units:
                changeUnits();
                return true;
            case R.id.action_show_favorites:
                ShowFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ShowFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }

    private void changeUnits() {
        if (isPortable) {
            frg_PlaceList.ChangeUnits();
        } else {
            PlaceFrag frg = ((PlaceFrag) getSupportFragmentManager().findFragmentById(R.id.list_fragment));
            frg.ChangeUnits();
        }
    }

    private void deleteFavorites() {
        DBHandler db = new DBHandler(this);
        db.deleteTable();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        boolean isCharging = intent.getBooleanExtra("CHARGE", false);
        boolean usbCharge = intent.getBooleanExtra("USB", false);
        boolean acCharge = intent.getBooleanExtra("AC", false);

        String msg = "";

        if (isCharging)
            msg = "Charging Started";
        else
            msg = "Charging End";


        if (usbCharge)
            msg += " (USB)";
        if (acCharge)
            msg += " (AC)";

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        super.onNewIntent(intent);
    }
}
