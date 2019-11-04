package id.co.umble.weatherapp;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker mMarker;

    // CONSTS
    private static final String API_WEATHER_HEAD = "https://api.openweathermap.org/data/2.5/weather?";
    private static final String APP_ID = "&appid={API_KEY}";
    private static final String CELSIUS = "\u2103";

    // Customizable var
    private String apiCurrentWeather;
    private String units = "metric";
    private String weatherGlobal;
    private String temperature;
    private String wind;
    private String humidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
//                Toast.makeText(MapsActivity.this, latLng.latitude + " " + latLng.longitude, Toast.LENGTH_SHORT).show();
                String latitude = latLng.latitude + "";
                String longitude = latLng.longitude + "";

                // Request GET
                apiCurrentWeather = API_WEATHER_HEAD + "lat=" + latitude + "&lon=" + longitude + "&units=" + units + APP_ID;
                Log.d("API", apiCurrentWeather);
                getRequest(apiCurrentWeather);

                if(mMarker == null){
                    mMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(latLng.latitude + ", " + latLng.longitude));
                } else{
                    mMarker.remove();
                    mMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(latLng.latitude + ", " + latLng.longitude));
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.setSnippet(weatherGlobal
                        + ", " + temperature + CELSIUS
                        + ", Wind: " + wind + " m/s"
                        + ", Humidity: " + humidity + " %");
                return false;
            }
        });

        // Move camera to device location
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null){
                            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentPos).title("Current Position"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 10));
                        } else{
                            Toast.makeText(MapsActivity.this, "Please turn on your location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getRequest(String url){
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Take JSON Object of "main" and JSON Array of "weather"
                JSONObject main = response.optJSONObject("main");
                JSONObject weather = response.optJSONArray("weather").optJSONObject(0);

                temperature = main.optString("temp");
                humidity = main.optString("humidity");
                weatherGlobal = weather.optString("main");
                wind = response.optJSONObject("wind").optInt("speed") + "";

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Call API fail", Toast.LENGTH_SHORT).show();
            }
        });

        VolleyRequest.getInstance(this).addToRequestQueue(jsonRequest);
    }
}
