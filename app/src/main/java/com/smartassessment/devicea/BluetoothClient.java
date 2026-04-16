package com.smartassessment.devicea;

import android.bluetooth.*;
import java.io.*;
import java.util.UUID;

public class BluetoothClient {

    public interface ClientCallback {
        void onMessage(String msg);
        void onStatus(String status);
    }

    private ClientCallback callback;
    private BluetoothSocket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private boolean isConnected = false;
    private String lastMac;

    private static final UUID UUID_SPP =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothClient(ClientCallback cb) {
        this.callback = cb;
    }

    public void connect(String mac) {
        lastMac = mac;

        new Thread(() -> {
            try {
                BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice dev = bt.getRemoteDevice(mac);

                socket = dev.createRfcommSocketToServiceRecord(UUID_SPP);
                bt.cancelDiscovery();
                socket.connect();

                isConnected = true;
                callback.onStatus("Connected");

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                listen();

            } catch (Exception e) {
                isConnected = false;
                callback.onStatus("Failed: " + e);
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
                isConnected = false;
                callback.onStatus("Disconnected");
            }
        }).start();
    }

    public void send(String msg) {
        new Thread(() -> {
            try {
                if (writer != null && isConnected) {
                    writer.write(msg + "\n");
                    writer.flush();
                }
            } catch (Exception e) {
                callback.onStatus("Send error");
            }
        }).start();
    }
}