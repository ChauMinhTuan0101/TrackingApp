package myapp.doan.tuanchau.vn.trackingapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            Toast.makeText(getApplicationContext(), eTracking.getName(), Toast.LENGTH_SHORT).show();
            super.onPostExecute(eTracking);
        }
    }
}
