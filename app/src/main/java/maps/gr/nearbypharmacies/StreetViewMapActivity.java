package maps.gr.nearbypharmacies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;


public class StreetViewMapActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    StreetViewPanorama streetView;
    String lat = "";
    String lng = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view_map);
        StreetViewPanoramaFragment streetViewFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.g_map_street);
        streetViewFragment.getStreetViewPanoramaAsync(this);
        Intent intent = getIntent();
        lat = intent.getStringExtra("lat");
        lng = intent.getStringExtra("lng");
    }


    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetView = streetViewPanorama;
        if ( !lat.isEmpty() && !lng.isEmpty() ) {
            streetViewPanorama.setPosition(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
        }
    }
}
