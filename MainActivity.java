package com.sarmale.arduinobtexample_v3;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    // Global variables we will use in the
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    //We will use a Handler to get the BT Connection statys
    // Static handler for updating the UI
    public static Handler handler;
    final public static int ERROR_READ = 0; // used in bluetooth handler to identify message update
    final public static int TYPE_1 = 1;
    final public static int TYPE_2 = 2;
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable

    Button showMap;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showMap = findViewById(R.id.showMap);
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        //Intances of BT Manager and BT Adapter needed to work with BT in Android.
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //Intances of the Android UI elements that will will use during the execution of the APP
        TextView btReadings = findViewById(R.id.btReadings);
        TextView btDevices = findViewById(R.id.btDevices);
        TextView testTextView = findViewById(R.id.test);
        TextView heartBeat = findViewById(R.id.test);

        Button connectToDevice = (Button) findViewById(R.id.connectToDevice);
        Button seachDevices = (Button) findViewById(R.id.seachDevices);
        Button clearValues = (Button) findViewById(R.id.refresh);
        Log.d(TAG, "Begin Execution");



        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case ERROR_READ:
                        // Handle error message
                        String errorMessage = msg.obj.toString();
                        btReadings.setText(errorMessage);
                        break;

                    case TYPE_1:
                        // Handle Type 1 message
                        String type1Message = msg.obj.toString();
                        Log.d("Test1", "Updating testTextView with: " + type1Message);
                        testTextView.setText(type1Message);
                        // Update UI for Type 1
                        break;
                    case TYPE_2:
                        // Handle Type 1 message
                        String type2Message = msg.obj.toString();
                        Log.d("Test1", "Updating testTextView with: " + type2Message);
                        heartBeat.setText(type2Message);
                        // Update UI for Type 2 heartbeat
                        break;
                    // Add more cases as needed
                }
            }
        };




        // Set a listener event on a button to clear the texts
        clearValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btDevices.setText("");
                btReadings.setText("");
            }
        });

        // Create an Observable from RxAndroid
        //The code will be executed when an Observer subscribes to the the Observable
        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();  // Use start() instead of run()

            // Wait for the connection to be established (you may want to add a timeout mechanism)
            connectThread.join();

            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();  // Use start() instead of run()

                // Continuously read values until interrupted
                while (!emitter.isDisposed()) {
                    String valueRead = connectedThread.getValueRead();
                    if (valueRead != null) {
                        emitter.onNext(valueRead);
                    }
                }

                // Close the BT stream when the observable is disposed
                connectedThread.cancel();
            }

            // Close the socket connection when the observable is disposed
            connectThread.cancel();

            // Notify observers that the observable has completed
            emitter.onComplete();
        });


        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View view) {
                btReadings.setText("");
                if (arduinoBTModule != null) {
                    //We subscribe to the observable until the onComplete() is called
                    //We also define control the thread management with
                    // subscribeOn:  the thread in which you want to execute the action
                    // observeOn: the thread in which you want to get the response
                    connectToBTObservable.
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribeOn(Schedulers.io()).
                            subscribe(valueRead -> {
                                //valueRead returned by the onNext() from the Observable
                                btReadings.setText(valueRead);
                                //We just scratched the surface with RxAndroid
                            });

                }
            }
        });

        seachDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter == null) {
                    Log.d(TAG, "Device doesn't support Bluetooth");
                } else {
                    Log.d(TAG, "Device supports Bluetooth");

                    if (!bluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth is disabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                            // Request Bluetooth permission here.
                            return;
                        }

                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }

                    String btDevicesString = "";
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                            btDevicesString = btDevicesString + deviceName + " || " + deviceHardwareAddress + "\n";

                            if (deviceName.equals("HC-05")) {
                                Log.d(TAG, "HC-05 found");
                                arduinoUUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                                connectToDevice.setEnabled(true);
                            }
                        }

                        // Update UI on the main thread
                        String finalBtDevicesString = btDevicesString;
                        runOnUiThread(() -> btDevices.setText(finalBtDevicesString));
                    }
                }
                Log.d(TAG, "Button Pressed");
            }
        });
    }
    public static int getErrorRead() {
        return ERROR_READ;
    }
}

