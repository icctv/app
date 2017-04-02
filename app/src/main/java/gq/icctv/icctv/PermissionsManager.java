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


    public PermissionsManager(Context context, AppCompatActivity activity) {
        this.activity = activity;
        this.context = context;
    }

    public void request() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity, Manifest.permission.CAMERA)) {
            // TODO: Show explanation for permissions
        } else {
            ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        }
    }

    public boolean check() {
        boolean granted = false;
        if(ContextCompat.checkSelfPermission(this.context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            granted = true;
        }
        return granted;
    }
}
