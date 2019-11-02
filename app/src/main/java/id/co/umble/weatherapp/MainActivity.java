package id.co.umble.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvCity, tvTime, tvCurrentTemp, tvWeather, tvMinMaxTemp;
    private TextView tvWind, tvPressure, tvCloudiness, tvHumidity, tvSunrise, tvSunset;
    private Button btnMap;
    private ToggleButton toggleDegree;
    private ImageView imgWeather;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // CONSTS
    private static final String API_WEATHER_HEAD = "https://api.openweathermap.org/data/2.5/weather?";
    private static final String APP_ID = "&appid=fdf871cedaf3413c6a23230372c30a02";
    private static final String  CELSIUS = "\u2103";
    private static final String  FAHRENHEIT = "\u2109";

    // Customizable var
    private String apiCurrentWeather;
    private double longitude, latitude;
    private String units = "metric";
    private String degree = CELSIUS;
    private String speedUnit = " m/s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing
        tvCity = findViewById(R.id.tv_city);
        tvTime = findViewById(R.id.tv_time);
        tvCurrentTemp = findViewById(R.id.tv_current_temp);
        tvWeather = findViewById(R.id.tv_weather);
        tvMinMaxTemp = findViewById(R.id.tv_min_max_temp);
        tvWind = findViewById(R.id.tv_wind_value);
        tvCloudiness = findViewById(R.id.tv_cloudiness_value);
        tvHumidity = findViewById(R.id.tv_humidity_value);
        tvSunrise = findViewById(R.id.tv_sunrise_value);
        tvSunset = findViewById(R.id.tv_sunset_value);
        tvPressure = findViewById(R.id.tv_pressure_value);
        imgWeather = findViewById(R.id.img_weather);
        toggleDegree = findViewById(R.id.toggle_units);
        btnMap = findViewById(R.id.btn_map);

        toggleDegree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTemp();
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapView.class);
                startActivity(intent);
            }
        });

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
                            // Take Lat Lon Device
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            // Request GET
                            apiCurrentWeather = API_WEATHER_HEAD + "lat=" + latitude + "&lon=" + longitude + "&units=" + units + APP_ID;
                            Log.d("API", apiCurrentWeather);
                            getRequest(apiCurrentWeather);
                        } else{
                            Toast.makeText(MainActivity.this, "Please turn on your location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//        Toast.makeText(MainActivity.this, "Please turn on your location", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause(){
        super .onPause();

        toggleDegree.setChecked(false);
    }

    public void changeTemp(){
        if(toggleDegree.isChecked()){
            degree = FAHRENHEIT;
            units = "imperial";
            speedUnit = " mph";
        } else{
            degree = CELSIUS;
            units = "metric";
            speedUnit = " m/s";
        }

        onResume();
    }

    public void getRequest(String url){
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Take JSON Object of "main" and JSON Array of "weather"
                JSONObject main = response.optJSONObject("main");
                JSONObject weather = response.optJSONArray("weather").optJSONObject(0);

                // Set City
                setCity(response);

                // Set Time
                setTime();

                // Set Image
                setImage(weather);

                // Set Current Temperature
                setCurrentTemp(main);

                // Set Weather
                setWeather(weather);

                // Set Min Max Temperature
                setMinMax(main);

                // Set wind speed
                setWind(response);

                // Set Pressure
                setPressure(main);

                // Set Humidity
                setHumidity(main);

                // Set Cloudiness
                setCloudiness(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Call API fail", Toast.LENGTH_SHORT).show();
            }
        });

        VolleyRequest.getInstance(this).addToRequestQueue(jsonRequest);
    }

    // Set City
    public void setCity(JSONObject object){
        tvCity.setText(object.optString("name"));
    }

    // Set Time
    public void setTime(){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyy", Locale.ENGLISH);
        tvTime.setText(sdf.format(date));
    }

    // Set Current Temperature
    public void setCurrentTemp(JSONObject object){
        int temp = (int)object.optDouble("temp");
        String strTemp = temp + degree;
        tvCurrentTemp.setText(strTemp);
    }

    public void setImage(JSONObject object){
        String icon = "https://openweathermap.org/img/wn/" + object.optString("icon") + ".png";
        Glide.with(this).load(icon).into(imgWeather);
    }

    // Set Weather
    public void setWeather(JSONObject object){
        tvWeather.setText(object.optString("main"));
    }

    // Set Min Max Temp
    public void setMinMax(JSONObject object){
        int min = (int)object.optDouble("temp_min");
        int max = (int)object.optDouble("temp_max");
        String minMaxTemp = min + degree + "\u2193" +  " / " + max + degree + "\u2191";
        tvMinMaxTemp.setText(minMaxTemp);
    }

    // Set Wind
    public void setWind(JSONObject object){
        String windSpeed = object.optJSONObject("wind").optString("speed") + speedUnit;
        tvWind.setText(windSpeed);
    }

    // Set Pressure
    public void setPressure(JSONObject object){
        String pressure = object.optInt("pressure") + " hPh";
        tvPressure.setText(pressure);
    }

    // Set Humidity
    public void setHumidity(JSONObject object){
        String humidity = object.optInt("humidity") + " %";
        tvHumidity.setText(humidity);
    }

    // Set Cloudiness
    public void setCloudiness(JSONObject object){
        String cloudiness = object.optJSONObject("clouds").optInt("all") + " %";
        tvCloudiness.setText(cloudiness);
    }

    //TODO
    // Create separate weather object, don't need all this function in MainActivity
    // Create multiple theme for different weather
}
