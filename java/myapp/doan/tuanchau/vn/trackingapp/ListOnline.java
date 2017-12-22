package myapp.doan.tuanchau.vn.trackingapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
//import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.service.carrier.CarrierService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.nearby.messages.Distance;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class ListOnline extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener{

    DatabaseReference onlineRef, currentUserRef, counterRef,locations;
    FirebaseRecyclerAdapter<User,ListOnlineViewHolder> adapter;

    RecyclerView listOnline;
    RecyclerView.LayoutManager layoutManager;
    GPSTracker gpsTracker;
    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    Handler handler;
    Runnable m_handlerTask;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISTANCE =10;
    String lat,lng;
    String IMEI="";
    String userID= "";
    String carModel = "";
    String DriverName = "";
    Date currentTime = Calendar.getInstance().getTime();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_online);
        gpsTracker = new GPSTracker(this);
        lat = String.valueOf(gpsTracker.getLatitude());
        lng = String.valueOf(gpsTracker.getLongitude());
        listOnline = findViewById(R.id.listOnline);
        listOnline.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listOnline.setLayoutManager(layoutManager);

        Toolbar toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle("Tracking System");
        setSupportActionBar(toolbar);
        locations = FirebaseDatabase.getInstance().getReference("Locations");
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        counterRef =FirebaseDatabase.getInstance().getReference("lastOnline");
        currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION

            },MY_PERMISSION_REQUEST_CODE);

        }
        else
        {
            if(checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
        setupSystem();
        updateList();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId().toString();

        Intent intent = getIntent();
        carModel = intent.getStringExtra("CarModel");
        DriverName = intent.getStringExtra("DriverName");
        handler = new Handler();
        m_handlerTask = new Runnable() {
            @Override
            public void run() {

                Log.d("Handler", "is running");
                gpsTracker = new GPSTracker(getApplicationContext()); // Service Class GPSTracker
                gpsTracker.updateGPSCoordinates();

                new AddTracking().execute();
                Log.d("TAG", "Posted");
                handler.postDelayed(m_handlerTask, (3000));
            }
        };
        handler.postDelayed(m_handlerTask, (3000));




    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;

        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null)
        {
            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(new MapTracking(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        String.valueOf(mLastLocation.getLatitude()),
                        String.valueOf(mLastLocation.getLongitude()),
                        FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
                        ));
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        else {
                //Toast.makeText(this, "Can't get Locaiton", Toast.LENGTH_SHORT).show();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RES_REQUEST).show();

            }
            else
            {
//                Toast.makeText(this, "Not supported Device", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void updateList() {
        adapter = new FirebaseRecyclerAdapter<User, ListOnlineViewHolder>(
                User.class,
                R.layout.user_layout,
                ListOnlineViewHolder.class,
                counterRef
        ) {
            @Override
            protected void populateViewHolder(ListOnlineViewHolder viewHolder, final User model, int position) {
                if (model.getPhonenumber().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                        Log.e("A",model.getPhonenumber());
                        viewHolder.txtEmail.setText(model.getPhonenumber().toString() + " (me)");
                }
                else viewHolder.txtEmail.setText(model.getPhonenumber());
               viewHolder.itemClickListenener = new ItemClickListenener() {
                   @Override
                   public void onClick(View view, int position) {
                       if(model.getPhonenumber().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()) || model.getPhonenumber() != null)
                       {
                           Intent map  = new Intent(ListOnline.this,MapsActivity.class);
                           Log.d("phone",model.getPhonenumber());
                           map.putExtra("phone", model.getPhonenumber());
                           map.putExtra("email","none");
                           map.putExtra("lat",mLastLocation.getLatitude());
                           map.putExtra("lng", mLastLocation.getLongitude());
                           map.putExtra("uid",userID);
                           Log.d("s",String.valueOf(mLastLocation.getLatitude()));
                           startActivity(map);
                       }
                       }

               };

            }
        };
        adapter.notifyDataSetChanged();
        listOnline.setAdapter(adapter);

    }

    private void setupSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Boolean.class))
                {
                    currentUserRef.onDisconnect().removeValue();
                    counterRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        counterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    User user = postSnapshot.getValue(User.class);
                    Log.d("LOG",user.getEmail() + " is " + user.getStatus());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length >0 && grantResults[0]  == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_join:
                currentUserRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()));
                break;
            case R.id.action_logout:
                currentUserRef.removeValue();
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)
        {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation  =location;
        displayLocation();

    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();



    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private class AddCar extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://trackingcar.us-west-2.elasticbeanstalk.com/api/addCar");
                JSONObject postData = new JSONObject();
                postData.put("IMEI", "4");
                Log.e("params", postData.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postData));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
//            Toast.makeText(getApplicationContext(), result,
//                    Toast.LENGTH_LONG).show();
        }

    }
    private class AddTracking extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://trackingcar.us-west-2.elasticbeanstalk.com/api/AddTracking");
                JSONObject postData = new JSONObject();
                postData.put("PhoneNumber",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                //postData.put("CreatedDate",currentTime);
                postData.put("DriverName", DriverName);
                postData.put("Latitude",lat);
                postData.put("Longitude",lng);
                postData.put("Name",carModel);
                Log.e("params", postData.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postData));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
//            Toast.makeText(getApplicationContext(), result,
//                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        this.finish();
        super.onPause();
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
