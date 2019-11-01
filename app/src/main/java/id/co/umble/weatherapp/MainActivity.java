package id.co.umble.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView tvCity, tvDate, tvCurrentTemp, tvWeather, tvMinMaxTemp;
    private String apiCurrentWeather = "https://api.openweathermap.org/data/2.5/weather?";
    private String units, degree;
    private static final String APP_ID = "&appid=fdf871cedaf3413c6a23230372c30a02";
    private double longitude, latitude;
    private FusedLocationProviderClient fusedLocationProviderClient;
//    private ImageView imgWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing
        tvCity = findViewById(R.id.tv_city);
        tvDate = findViewById(R.id.tv_date);
        tvCurrentTemp = findViewById(R.id.tv_current_temp);
        tvWeather = findViewById(R.id.tv_weather);
        tvMinMaxTemp = findViewById(R.id.tv_min_max_temp);
//        imgWeather = findViewById(R.id.img_weather);
        units = "metric";

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    protected void onResume(){
        super .onResume();

        // Take device location
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
//                            Log.d("Latitude1: ", Double.toString(latitude));
//                            Log.d("Longitude1: ", Double.toString(longitude));
                            // Take API from Openweather
                            apiCurrentWeather += "lat=" + latitude + "&lon=" + longitude + "&units=" + units + APP_ID;
                            Log.d("API", apiCurrentWeather);
                            getRequest(apiCurrentWeather);
                        } else{
                            Toast.makeText(MainActivity.this, "Please turn on your location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//        Toast.makeText(MainActivity.this, "Please turn on your location", Toast.LENGTH_SHORT).show();
    }

    public void getRequest(String url){
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Set City
                tvCity.setText(response.optString("name"));

                // Take JSON Object of "main" and JSON Array of "weather"
                JSONObject main = response.optJSONObject("main");
                JSONObject weather = response.optJSONArray("weather").optJSONObject(0);

                // Set Current Temperature
                int temp = (int)main.optDouble("temp");
                tvCurrentTemp.setText(String.valueOf(temp));

                // Set Weather
                tvWeather.setText(weather.optString("main"));

                // Set Min Max Temperature
                int min = (int)main.optDouble("temp_min");
                int max = (int)main.optDouble("temp_max");
                String minMaxTemp = min + "/" + max;
                tvMinMaxTemp.setText(minMaxTemp);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Call API fail", Toast.LENGTH_SHORT).show();
            }
        });

        VolleyRequest.getInstance(this).addToRequestQueue(jsonRequest);
    }


}
