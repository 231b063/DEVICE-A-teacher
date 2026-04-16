package com.smartassessment.devicea;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements BluetoothClient.ClientCallback {

    EditText etMac, etQuestion;
    Button btnConnect, btnSend;
    TextView tvLog;

    BluetoothClient client;
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMac = findViewById(R.id.etMac);
        etQuestion = findViewById(R.id.etQuestion);
        btnConnect = findViewById(R.id.btnConnect);
        btnSend = findViewById(R.id.btnSend);
        tvLog = findViewById(R.id.tvLog);

        client = new BluetoothClient(this);

        btnConnect.setOnClickListener(v -> {
            String mac = etMac.getText().toString();
            client.connect(mac);
        });

        btnSend.setOnClickListener(v -> {
            if (!isConnected) {
                tvLog.append("\nNot connected!");
                return;
            }
            String q = etQuestion.getText().toString();
            client.send("QUESTION:" + q);
        });
    }

    @Override
    public void onMessage(String msg) {
        runOnUiThread(() -> tvLog.append("\nRecv: " + msg));
    }

    @Override
    public void onStatus(String status) {
        runOnUiThread(() -> {
            tvLog.append("\n" + status);
            if (status.contains("Connected")) isConnected = true;
        });
    }
}