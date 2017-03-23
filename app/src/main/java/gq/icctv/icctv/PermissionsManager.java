package gq.icctv.icctv;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class PermissionsManager {

    static final int PERMISSIONS_REQUEST_CAMERA = 1;
    Context context;
    AppCompatActivity activity;


    public PermissionsManager(Context c, AppCompatActivity a) {
        activity = a;
        context = c;
    }

    public void request() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA)) {
            // TODO: Show explanation for permissions
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_CAMERA);
        }
    }

    public boolean check() {
        boolean granted = (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        return granted;
    }
}
