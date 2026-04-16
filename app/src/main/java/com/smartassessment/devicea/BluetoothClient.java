package com.smartassessment.devicea;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

public class BluetoothClient {

    public interface ClientCallback {
        void onMessage(String msg);
        void onStatus(String status);
    }

    private ClientCallback callback;

    public BluetoothClient(ClientCallback cb) {
        this.callback = cb;
    }

    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void connect(String mac) {
        new Thread(() -> {
            try {
                callback.onStatus("Connecting to " + mac + " ...");

                BluetoothDevice dev = bt.getRemoteDevice(mac);

                socket = dev.createRfcommSocketToServiceRecord(SPP_UUID);
                bt.cancelDiscovery();
                socket.connect();

                callback.onStatus("Connected!");

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                listen();

            } catch (Exception e) {
                callback.onStatus("Connection failed: " + e);
            }
        }).start();
    }

    private void listen() {
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    callback.onMessage(line);
                }
            } catch (Exception e) {
                callback.onStatus("Disconnected");
            }
        }).start();
    }

    public void send(String msg) {
        new Thread(() -> {
            try {
                if (writer != null) {
                    writer.write(msg + "\n");
                    writer.flush();
                }
            } catch (Exception e) {
                callback.onStatus("Send error: " + e);
            }
        }).start();
    }
}
