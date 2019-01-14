package maps.gr.nearbypharmacies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static String TAG = "MAPS";
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LatLng user_coordinates = null;
    private ProgressDialog pDialog = null;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private boolean didRequest = false;
    private ArrayList<Integer> id = new ArrayList<>();
    private ArrayList<String> tel = new ArrayList<>();
    private ArrayList<String> fav = new ArrayList<>();
    private ArrayList<String> city = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> address = new ArrayList<>();
    private ArrayList<String> distance = new ArrayList<>();
    private ArrayList<String> lat = new ArrayList<>();
    private ArrayList<String> lng = new ArrayList<>();
    private SupportMapFragment mapFragment;
    private RelativeLayout info;
    private TextView nameTV;
    private TextView addressTV;
    private TextView telTV;
    private TextView distanceTV;
    private AccessToken fbtoken = null;


    //private String URL = "http://192.168.1.129/nearby_pharmacies/request.php?";
    private String URL = "https://nearbypharmacies.herokuapp.com/request.php?";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        info = findViewById(R.id.info);
        nameTV = findViewById(R.id.name);
        addressTV = findViewById(R.id.address);
        telTV = findViewById(R.id.tel);
        distanceTV = findViewById(R.id.distance);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendViewToBack(v);
            }
        });
        Globals global = new Globals(getApplicationContext());
        if ( !global.isNetworkConnected() ){
            global.dialog(null, "Ενεργοποιήστε το Internet για να συνεχίσετε", false);
        } else {
            MyApplication app = (MyApplication)getApplication();
            fbtoken = app.getFbLoginStatus();
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
            mapFragment.getMapAsync(this);
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Φόρτωση φαρμακείων...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
    }


    public void request(String url){
        StringRequest strReq = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d(TAG, response.toString());
                didRequest = true;
                readResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });
        pDialog.dismiss();
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq, TAG);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_COARSE_LOCATION: {
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED  ||
                            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                    }
                } else {

                }
                return;
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if ( !didRequest ) {
                user_coordinates = new LatLng(location.getLatitude(), location.getLongitude());
                String user = "";
                if ( fbtoken != null ){
                    user = fbtoken.getToken();
                }
                request(URL + "lat=" + user_coordinates.latitude+"&lng="+user_coordinates.longitude+"&user="+user);
                Log.d("USER-COORDINATES", user_coordinates.toString());
                //IF THE EMULATOR DOES NOT SUPPORT MOCK LOCATIONS)
                if ( user_coordinates.longitude < 0 ){
                    user_coordinates = new LatLng(40.5169248, 21.2544023);
                }
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(user_coordinates, 10);
                mMap.animateCamera(cameraUpdate);
                mMap.setMyLocationEnabled(true);
                mLocationManager.removeUpdates(this);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    private void readResponse(String response) {
        if (!response.isEmpty()) {
            //Log.d(TAG, response);
            try {
                JSONObject jsonObj;
                jsonObj = new JSONObject(response);
                JSONArray jsonArray = jsonObj.getJSONArray("response");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    lat.add(i, jsonObject.getString("lat"));
                    lng.add(i, jsonObject.getString("lng"));
                    name.add(i, jsonObject.getString("name"));
                    address.add(i, jsonObject.getString("address"));
                    city.add(i, jsonObject.getString("city"));
                    tel.add(i, jsonObject.getString("tel"));
                    id.add(i, jsonObject.getInt("id"));
                    fav.add(i, jsonObject.getString("fav"));
                    distance.add(i, jsonObject.getString("distance"));
                    double latitude = Double.parseDouble(lat.get(i));
                    double longitude = Double.parseDouble(lng.get(i));
                    LatLng marker = new LatLng(latitude, longitude);
                    addMarkers(marker, name.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void addMarkers(LatLng marker, String name){
        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        mMap.addMarker(markerOptions.position(marker).title(name));
        mMap.setOnMarkerClickListener(this);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        String marker_id = marker.getId();
        //Log.d(TAG, marker_id);
        String[] parts = marker_id.split("m");
        int id = Integer.parseInt(parts[1]);
        nameTV.setText(name.get(id));
        addressTV.setText(address.get(id));
        telTV.setText(tel.get(id));
        if ( isNumeric(distance.get(id)) ){
            double dis = Double.parseDouble(distance.get(id));
            distanceTV.setText("Απόσταση: "+ String.format("%.1f", (double) dis) + " km");
        } else{
            distanceTV.setText("Απόσταση: "+distance.get(id) + " km");
        }
        info.bringToFront();
        return false;
    }


    private Intent getListData(boolean showFav){
        Intent intent = new Intent(this, ListsActivity.class);
        intent.putIntegerArrayListExtra("id", id);
        intent.putStringArrayListExtra("name", name);
        intent.putStringArrayListExtra("city", city);
        intent.putStringArrayListExtra("distance", distance);
        intent.putStringArrayListExtra("lat", lat);
        intent.putStringArrayListExtra("lng", lng);
        intent.putStringArrayListExtra("fav", fav);
        intent.putExtra("user_lat", user_coordinates.latitude+"");
        intent.putExtra("user_lng", user_coordinates.longitude+"");
        if ( showFav ){
            intent.putExtra("show_favorites", "1");
        }
        return intent;
    }


    public void showFav(View view) {
        if ( fbtoken == null ){
            Intent intent = new Intent(this, FBActivity.class);
            startActivity(intent);
        } else{
            startActivity(getListData(true));
        }
    }


    public void showList(View view){
        startActivity(getListData(false));
    }


    @Override
    protected void onRestart(){
        super.onRestart();
        Intent intent = getIntent();
        if ( intent != null && intent.getStringExtra("activity") != null ) {
            showList(null);
        }
    }


    private boolean isNumeric(String str){
        try {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    public static void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup)child.getParent();
        if ( null != parent ) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }
}
