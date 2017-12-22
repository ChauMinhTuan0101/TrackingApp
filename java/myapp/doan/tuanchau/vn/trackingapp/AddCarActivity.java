package myapp.doan.tuanchau.vn.trackingapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class AddCarActivity extends AppCompatActivity {

    Button btnSubmit;
    String phonenum="";
    EditText edtDriverName, edtCarModel;
    Date currentTime = Calendar.getInstance().getTime();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);
        btnSubmit = findViewById(R.id.btnSaveDriver);
        edtDriverName = findViewById(R.id.edtDriverName);
        edtCarModel = findViewById(R.id.edtCarModel);

        Intent intent=getIntent();
        phonenum = intent.getStringExtra("Phonenum");

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddCar().execute();
                Intent intent = new Intent(AddCarActivity.this,ListOnline.class);
                intent.putExtra("DriverName", edtDriverName.getText().toString());
                intent.putExtra("CarModel", edtCarModel.getText().toString());
                    startActivity(intent);
                    finish();
            }
        });



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
    private class AddCar extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("http://trackingcar.us-west-2.elasticbeanstalk.com/api/addCar");
                JSONObject postData = new JSONObject();
                //postData.put("IMEI", "4");
                postData.put("CreatedDate",currentTime);
                postData.put("PhoneNumber",phonenum);
                postData.put("DriverName",edtDriverName.getText().toString());
                postData.put("Name",edtCarModel.getText().toString());
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
//                    Toast.LENGTH_LONG);
        }

    }

}
