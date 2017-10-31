package myapp.doan.tuanchau.vn.trackingapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import info.hoang8f.widget.FButton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private ProgressDialog myProgress;
    float lat;
    MapRipple mapCircle;
    float lng;
    Button btnStart;
    //Marker currentMarker;
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;
    private static final String TAG = "TAG";
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= 23 && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int PERMISSION_ALL = 1;
        MapsInitializer.initialize(getApplicationContext());
        String[] PERMISSIONS = {android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_PHONE_STATE};
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Set callback listener, on Google Map ready.
        mapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);

            }
        });
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnStart.getText().toString() == "STOP TRACKING") {
                    StopRepeatingTask();
                    btnStart.setText("START TRACKING");
                }
                if(btnStart.getText().toString() == " START TRACKING")
                {
                    mHandler = new Handler();
                    StartRepeatingTask();
                    btnStart.setText("STOP TRACKING");

                }

            }
        });
        mHandler = new Handler();
        StartRepeatingTask();


//        gpsTracker = new GPSTracker(getApplicationContext());
//
//
//        lat = (float) gpsTracker.getLatitude();
//        lng = (float) gpsTracker.getLongitude();
//
//
//
//        Toast.makeText(this, gpsTracker.getLocality(this), Toast.LENGTH_LONG).show();
//
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 1000, 0,);
    }

    Runnable mTracking = new Runnable() {
        @Override
        public void run() {
            try{
                gpsTracker = new GPSTracker(getApplicationContext());
                gpsTracker.updateGPSCoordinates();
                lat = (float) gpsTracker.getLatitude();
                lng = (float) gpsTracker.getLongitude();
                LatLng latLng = new LatLng(lat,lng);
//                mMap.clear();
//
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.title("My Location");
//                    markerOptions.snippet("....");
//                    markerOptions.position(latLng);
//                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon));
//                    Marker currentMarker = mMap.addMarker(markerOptions);
////                }


                Toast.makeText(getApplicationContext(),String.valueOf(lat)+"\n"+String.valueOf(lng), Toast.LENGTH_LONG).show();
            }finally {
                mHandler.postDelayed(mTracking,mInterval);

            }
        }


    };

    @Override
    protected void onPause() {
        StopRepeatingTask();
        super.onPause();
    }

    @Override
    protected void onStop() {
        StopRepeatingTask();
        super.onStop();
    }

    @Override
    protected void onResume() {
        StartRepeatingTask();
        super.onResume();
    }

    public void StartRepeatingTask(){
        mTracking.run();


    }
    public void StopRepeatingTask(){
        mHandler.removeCallbacks(mTracking);
    }
    private void onMyMapReady(GoogleMap googleMap) {
        // Get Google Map from Fragment.
        mMap = googleMap;
        // Sét OnMapLoadedCallback Listener.
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                // Map loaded. Dismiss this dialog, removing it from the screen.
                //myProgress.dismiss();

                askPermissionsAndShowMyLocation();
            }
        });
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mapCircle = new MapRipple(mMap, new LatLng(lat,lng), getApplicationContext());
        mapCircle.startRippleMapAnimation();
        //mMap.setMyLocationEnabled(true);
    }
    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(this, permissions,
                        100);

                return;
            }
        }

        // Show current location on Map.
        this.showMyLocation();
    }

    // When you have the request results.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        switch (requestCode) {
            case 100: {

                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (read/write).
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    // Show current location on Map.
                    this.showMyLocation();
                }
                // Cancelled or denied.
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Criteria to find location provider.
        Criteria criteria = new Criteria();

        // Returns the name of the provider that best meets the given criteria.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {
            Toast.makeText(this, "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i(TAG, "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }


    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) {
            return;
        }

        // Millisecond
        final long MIN_TIME_BW_UPDATES = 1000;
        // Met
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

        Location myLocation = null;
        try {
//            locationManager.requestLocationUpdates(
//                    locationProvider,
//                    MIN_TIME_BW_UPDATES,
//                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

            myLocation = locationManager
                    .getLastKnownLocation(locationProvider);
        }
        catch (SecurityException e) {
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {

            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .bearing(90)
                    .tilt(40)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            // Add Marker to Map
            MarkerOptions option = new MarkerOptions();
            option.title("My Location");
            option.snippet("....");
            option.position(latLng);
            option.icon(BitmapDescriptorFactory.fromResource(R.drawable.caricon));
            Marker currentMarker = mMap.addMarker(option);
            currentMarker.showInfoWindow();

        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Location not found");
        }


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        onMyMapReady(googleMap);

    }
}
