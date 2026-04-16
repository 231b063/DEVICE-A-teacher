package com.smartassessment.devicea;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements BluetoothClient.ClientCallback {

    EditText etMac;
    Button btnConnect, btnSend; // if present
    TextView tvLog;

    BluetoothClient client;

    // runtime permission launcher (for Android 12+)
    ActivityResultLauncher<String[]> requestPermsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMac = findViewById(R.id.etMac); // your EditText id
        btnConnect = findViewById(R.id.btnConnect);
        tvLog = findViewById(R.id.tvLog);

        client = new BluetoothClient(this);

        // permission launcher
        requestPermsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                (ActivityResultCallback<java.util.Map<String, Boolean>>) result -> {
                    boolean ok = true;
                    for (Boolean granted : result.values()) ok &= granted;
                    if (ok) tvLog.append("\nPermissions granted.");
                    else tvLog.append("\nPermissions denied. App may not connect.");
                }
        );

        btnConnect.setOnClickListener(v -> {
            String mac = etMac.getText().toString().trim();
            if (mac.isEmpty()) {
                tvLog.append("\nEnter Device B MAC address.");
                return;
            }
            ensurePermissionsThenConnect(mac);
        });
    }

    private void ensurePermissionsThenConnect(String mac) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            String[] perms = new String[] {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            boolean allGranted = true;
            for (String p : perms) {
                if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                requestPermsLauncher.launch(perms);
                return;
            }
        }
        // permissions OK (or not required) — connect
        tvLog.append("\nConnecting to " + mac + " ...");
        client.connect(mac);
    }

    @Override
    public void onMessage(String msg) {
        runOnUiThread(() -> tvLog.append("\nRecv: " + msg));
    }

    @Override
    public void onStatus(String status) {
        runOnUiThread(() -> tvLog.append("\n" + status));
    }
}
