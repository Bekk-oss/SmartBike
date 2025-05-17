package com.sarmale.arduinobtexample_v3;


import static com.sarmale.arduinobtexample_v3.MainActivity.ERROR_READ;
import static com.sarmale.arduinobtexample_v3.MainActivity.TYPE_1;
import static com.sarmale.arduinobtexample_v3.MainActivity.TYPE_2;
import static com.sarmale.arduinobtexample_v3.MainActivity.handler;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

//Class that given an open BT Socket will
//Open, manage and close the data Stream from the Arduino BT device
public class ConnectedThread extends Thread {
    private static final String TAG = "TestLogs";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;
    // Define your custom message delimiter
    private String messageDelimiter = "\n";
    // Use a StringBuilder to accumulate characters until a complete message is received
    private StringBuilder messageBuffer = new StringBuilder();

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        // Input and Output streams members of the class
        // We won't use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void setMessageDelimiter(String delimiter) {
        messageDelimiter = delimiter;
    }

    public String getValueRead() {
        return valueRead;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));

            int receivedChar;
            while ((receivedChar = reader.read()) != -1) {
                char receivedCharChar = (char) receivedChar;

                // Check if it's the end of a message
                if (receivedCharChar == messageDelimiter.charAt(0)) {
                    // Process the complete message
                    String completeMessage = messageBuffer.toString();

                    // Identify the message type based on some criteria
                    if (completeMessage.startsWith("Temp")) {
                        // Handle Type 1 message
                        handler.obtainMessage(ERROR_READ, completeMessage).sendToTarget();
                    } else if (completeMessage.startsWith("Vehicle")) {
                        Log.d("Test1", "Received Type 2 message: " + completeMessage);
                        // Handle Type 2 message
                        handler.obtainMessage(TYPE_1, completeMessage).sendToTarget();
                        // Update a different UI element or perform other actions
                    } else if (completeMessage.startsWith("BPM")) {

                        // Handle Type 2 message
                        handler.obtainMessage(TYPE_2, completeMessage).sendToTarget();
                        // Update a different UI element or perform other actions
                        //Add more cases when needed
                    }

                    // Clear the message buffer for the next message
                    messageBuffer = new StringBuilder();
                } else {
                    // Append the character to the message buffer
                    messageBuffer.append(receivedCharChar);
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "Input stream was disconnected", e);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
