package maps.gr.nearbypharmacies;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;


public class Globals {

    private Context context;
    private Activity activity;


    public Globals(Context context){
        this.context = context;
    }


    public Globals(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }


    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }


    public void dialog(String header, String content, final boolean finish){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this.context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this.context);
        }
        builder.setTitle("Αναζήτηση Φαρμακείων")
                .setMessage(content)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if ( finish ) {
                            ((Activity) context).finish();
                        }
                    }
                })
                .show();
    }
}
