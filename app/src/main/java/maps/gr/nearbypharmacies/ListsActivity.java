package maps.gr.nearbypharmacies;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class ListsActivity extends ListActivity {

    private String user_lat = "";
    private String user_lng = "";
    private static String TAG = "LISTS";
    AccessToken fbtoken;

    private String URL = "https://nearbypharmacies.herokuapp.com/fav.php?";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication app = (MyApplication)getApplication();
        fbtoken = app.getFbLoginStatus();
        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        Intent intent = getIntent();
        ArrayList<Integer> id = intent.getIntegerArrayListExtra("id");
        ArrayList<String> lat = intent.getStringArrayListExtra("lat");
        ArrayList<String> lng = intent.getStringArrayListExtra("lng");
        ArrayList<String> name = intent.getStringArrayListExtra("name");
        ArrayList<String> distance = intent.getStringArrayListExtra("distance");
        ArrayList<String> fav = intent.getStringArrayListExtra("fav");
        ArrayList<String> city = intent.getStringArrayListExtra("city");
        user_lat = intent.getStringExtra("user_lat");
        user_lng = intent.getStringExtra("user_lng");
        boolean show_favorites = false;
        if ( intent.getStringExtra("show_favorites") != null ){
            show_favorites = true;
        }
        Adapter adapter = new Adapter(this, android.R.layout.simple_list_item_1, id, name, city, distance, fav, lat, lng, show_favorites);
        setListAdapter(adapter);
    }


    private class Adapter extends ArrayAdapter<String>{
        String fav_response = "";
        private final Context context;
        ArrayList<Integer> id = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> city = new ArrayList<>();
        ArrayList<String> distance = new ArrayList<>();
        ArrayList<String> fav = new ArrayList<>();
        ArrayList<String> lat = new ArrayList<>();
        ArrayList<String> lng = new ArrayList<>();
        boolean show_favorites = false;


        public Adapter(Context context, int resource, ArrayList<Integer> id, ArrayList<String> name, ArrayList<String> city, ArrayList<String> distance, ArrayList<String> fav,
                       ArrayList<String> lat, ArrayList<String> lng, boolean show_favorites) {
            super(context, resource, name);
            this.context = context;
            this.id = id;
            this.name = name;
            this.city = city;
            this.distance = distance;
            this.fav = fav;
            this.lat = lat;
            this.lng = lng;
            this.show_favorites = show_favorites;
        }


        public void request(String url){
            StringRequest strReq = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if ( !response.isEmpty() ) {
                        try {
                            JSONObject jsonObj;
                            jsonObj = new JSONObject(response);
                            JSONObject json = jsonObj.getJSONObject("response");
                            fav_response = json.getString("message");
                            //Log.d(TAG, response);
                            Toast.makeText(getApplicationContext(), fav_response+"", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });
            AppSingleton.getInstance(context).addToRequestQueue(strReq, TAG);
        }


        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.list, parent, false);
            }
            final View view2 = view;
            Button starBtn = view.findViewById(R.id.star);
            if ( show_favorites ){
                starBtn.setVisibility(View.VISIBLE);
                if ( fav.get(position).equals("1") ){
                    //starBtn.setBackgroundResource(android.R.drawable.star_big_on);
                    view.setBackgroundResource(R.color.fav);
                } else{
                    //starBtn.setBackgroundResource(android.R.drawable.star_big_off);
                    view.setBackgroundResource(R.color.not);
                }
            } else{
                starBtn.setVisibility(View.GONE);
            }
            starBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = URL+"id="+id.get(position)+"&user="+fbtoken.getToken();
                    //Log.d(TAG, url);
                    request(url);
                    //Log.d(TAG, fav_response);
                    ViewGroup row = (ViewGroup) v.getParent();
                    if ( fav_response != null ) {
                        if ( ((ColorDrawable) row.getBackground()).getColor() == getResources().getColor(R.color.fav) ) {
                            Log.d(TAG, "πρόσθεση");
                            row.setBackgroundResource(R.color.not);
                            //starBtn2.setBackgroundResource(android.R.drawable.btn_star_big_off);
                        } else {
                            Log.d(TAG, "αφαίρεση");
                            row.setBackgroundResource(R.color.fav);
                            //starBtn2.setBackgroundResource(android.R.drawable.btn_star_big_on);
                        }
                    }
                }
            });
            TextView tv1 = view.findViewById(R.id.name);
            tv1.setPaintFlags(tv1.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
            tv1.setText(name.get(position) + "\n" + city.get(position));
            TextView tv2 = view.findViewById(R.id.distance);
            if ( isNumeric(distance.get(position)) ){
                double dis = Double.parseDouble(distance.get(position));
                tv2.setText(String.format("%.1f", (double) dis) + " km");
            } else{
                tv2.setText(distance.get(position) + " km");
            }
            Button navigationBtn = view.findViewById(R.id.navigation);
            navigationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if ( !user_lat.isEmpty() && !user_lng.isEmpty() ) {
                    String maps = "http://maps.google.com/maps?saddr=";
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(maps + user_lat + "," + user_lng + "&daddr=" + lat.get(position) + "," + lng.get(position)));
                    startActivity(intent);
                }
                }
            });
            return view;
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

    }
}
