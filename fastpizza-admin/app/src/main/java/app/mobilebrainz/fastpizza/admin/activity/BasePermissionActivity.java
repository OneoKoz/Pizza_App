package app.mobilebrainz.fastpizza.admin.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import app.mobilebrainz.fastpizza.admin.R;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Базовая активность для задания разрешений приложению
 */
public abstract class BasePermissionActivity extends AppCompatActivity {

    private static final int READ_EXTERNAL_STORAGE_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPermissions();
    }

    private void setupPermissions() {
        setupPermission(
                READ_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE_CODE,
                R.string.perm_read_external_storage_message
        );
    }

    private void setupPermission(String name, int code, @StringRes int messageRes) {
        if (checkSelfPermission(name) != PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(name)) {
                new AlertDialog.Builder(this)
                        .setMessage(messageRes)
                        .setTitle(R.string.permission_requied)
                        .setPositiveButton("OK", (d, w) -> requestPermissions(new String[]{name}, code))
                        .create()
                        .show();
            } else {
                requestPermissions(new String[]{name}, code);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_CODE :
                if (grantResults.length == 0 || grantResults[0] != PERMISSION_GRANTED) {
                    //toast("READ_EXTERNAL_STORAGE permission has been denied by user");
                } else {
                    //toast("READ_EXTERNAL_STORAGE permission has been granted by user");
                }
                break;
        }

    }
}
