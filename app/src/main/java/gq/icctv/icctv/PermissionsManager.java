package gq.icctv.icctv;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

class PermissionsManager {

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private Context context;
    private AppCompatActivity activity;


    PermissionsManager(Context context, AppCompatActivity activity) {
        this.activity = activity;
        this.context = context;
    }

    void request() {
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

    boolean handleRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionsManager.PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return true;
                } else {
                    return false;
                }
            }
            default: {
                return false;
            }
        }
    }
}
