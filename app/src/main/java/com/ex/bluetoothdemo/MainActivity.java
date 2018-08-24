package com.ex.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ex.permission.OnPermission;
import com.ex.permission.Permission;
import com.ex.permission.XXPermissions;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }
    /**
     * Called when the activity is first created.
     */
    private Button autopairbtn = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autopairbtn = (Button) findViewById(R.id.button1);
        autopairbtn.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
//        XXPermissions.gotoPermissionSettings(this);
//        XXPermissions.with(this)
//                .permission(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
//                .request(new OnPermission() {
//                    @Override
//                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (!bluetoothAdapter.isEnabled()) {
                            bluetoothAdapter.enable();//异步的，不会等待结果，直接返回。
                        } else {
                            bluetoothAdapter.startDiscovery();
                        }
//                    }
//
//                    @Override
//                    public void noPermission(List<String> denied, boolean quick) {
//                        XXPermissions.gotoPermissionSettings(v.getContext());
//                    }
//                });

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();
}
