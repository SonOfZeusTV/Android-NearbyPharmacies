package maps.gr.nearbypharmacies;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MyApplication extends Application {

    private static Context mContext;
    private static String TAG = "APPLICATION";


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }

    public AccessToken getFbLoginStatus(){
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        //Log.d(TAG, "user: "+accessToken);
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if ( isLoggedIn ){
            return accessToken;
        } else{
            return null;
        }
    }
}