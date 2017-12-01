package myapp.doan.tuanchau.vn.trackingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.identity.intents.AddressConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    ArrayList<eTracking> trackingList;
    private final static int LOGIN_PERMISSION = 1000;
    private static final int RC_SIGN_IN = 123;

    Button btnPhone, btnEmail;
    ProgressDialog pDialog;
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
        setContentView(R.layout.activity_main);
        String[] PERMISSIONS = {android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.READ_PHONE_STATE};
        if (!hasPermissions(this, PERMISSIONS)) {
            int PERMISSIONS_ALL =1 ;
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL);
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //btnEmail = findViewById(R.id.loginEmail);
        btnPhone = findViewById(R.id.loginPhone);
        trackingList  = new ArrayList<>();
       // new GetTracking().execute();
//        btnEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAllowNewEmailAccounts(true).build(),LOGIN_PERMISSION);
//
//            }
//        });
        //startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAllowNewEmailAccounts(true).build(),LOGIN_PERMISSION);
       btnPhone.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               FirebaseAuth auth = FirebaseAuth.getInstance();
//               if (auth.getCurrentUser() != null ) {
////                   Intent intent = new Intent(MainActivity.this,ListOnline.class);
////                   startActivity(intent);
////                   finish();
//               } else {
                   startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(Arrays.asList(
                           new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build())).build(), RC_SIGN_IN);
               //}
           }
       });

        //new AddCar().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_PERMISSION)
        {
            startNewActivity(resultCode,data);
        }
        if(requestCode == RC_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == ResultCodes.OK){
            DatabaseReference mdatabase= FirebaseDatabase.getInstance().getReference("Locations").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            mdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        Intent intent = new Intent(MainActivity.this, ListOnline.class);
                        intent.putExtra("Phonenum",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                        startActivity(intent);
                        finish();
                        return;
                    }
                    else{

                        Intent intent = new Intent(MainActivity.this, AddCarActivity.class);
                        intent.putExtra("Phonenum",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




//                    Intent intent = new Intent(MainActivity.this,ListOnline.class);
//                    startActivity(intent);
//                    finish();
//                    return;
                }else{
                    if(response == null)
                    {
                        Log.e("Login","Login Failed");
                        return;
                    }

                }

        }

    }
    private void startNewActivity(int resultCode, Intent data) {
        if(resultCode == RESULT_OK)
        {
            Intent intent = new Intent(MainActivity.this,ListOnline.class);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }
    private class HttpRequestTask extends AsyncTask<Void,Void, eTracking> {

        @Override
        protected eTracking doInBackground(Void... voids) {
            try{
                final String url = "http://trackingcar.us-west-2.elasticbeanstalk.com/api/getAllCar";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                eTracking et = restTemplate.getForObject(url,eTracking.class);
                return et;
            }catch(Exception e)
            {
                Log.e("Maps Activity ",e.getMessage(),e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(eTracking eTracking) {
//            Toast.makeText(getApplicationContext(), eTracking.getName(), Toast.LENGTH_SHORT).show();
            super.onPostExecute(eTracking);
        }

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
                postData.put("IMEI", "");
                postData.put("CreatedDate","");
                postData.put("Latitude","");
                postData.put("Longitude","");
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

    private class GetAllCar extends AsyncTask<Void, Void, Void >{

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String jsonStr =sh.makeServiceCall("http://trackingcar.us-west-2.elasticbeanstalk.com/api/getAllCar");
            Log.e("Server Say","Response From server: "+ jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONArray contacts = jsonObj.getJSONArray("data");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String imei = c.getString("IMEI");
                        String status = c.getString("Status");
                        String date = c.getString("CreatedDate");
                        String name = c.getString("Name");

                       // Toast.makeText(MainActivity.this, imei+" "+name, Toast.LENGTH_SHORT).show();
                        // adding each child node to HashMap key => value

                        eTracking et = new eTracking(name,imei,date);
                        trackingList.add(et);

                        // adding contact to contact list
                        //contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e("Server Said:", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e("Server Said: ", "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
           for(eTracking item: trackingList)
           {
               Toast.makeText(MainActivity.this, item.getName() + " " + item.getImei(), Toast.LENGTH_SHORT).show();
           }
        }

    }

}
