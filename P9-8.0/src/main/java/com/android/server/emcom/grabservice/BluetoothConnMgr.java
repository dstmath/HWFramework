package com.android.server.emcom.grabservice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnMgr {
    private static final String BASE_UUID = "00000000-0000-1000-8000-00805f9b34fb";
    private static final int BUFFER_SIZE = 4096;
    private static final String EMCOM_BT_NAME = "AutoGrabService";
    private static final int RELISTEN_INTERVAL = 1000;
    private static final String TAG = "GrabService";
    private AcceptThread mAcceptThread;
    private BluetoothAdapter mAdapter;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;
    private volatile ConnectState mState;
    private UUID mUUID = UUID.fromString(BASE_UUID);

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothConnMgr.this.mAdapter.listenUsingRfcommWithServiceRecord(BluetoothConnMgr.EMCOM_BT_NAME, BluetoothConnMgr.this.mUUID);
            } catch (IOException e) {
                Log.e(BluetoothConnMgr.TAG, "listen rfcomm error.", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            if (this.mmServerSocket == null) {
                BluetoothConnMgr.this.listenConnectRequest();
                return;
            }
            BluetoothSocket socket = null;
            try {
                socket = this.mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(BluetoothConnMgr.TAG, "accept sercer socket error.");
            }
            if (socket != null) {
                BluetoothConnMgr.this.manageConnectedSocket(socket);
            } else {
                SystemClock.sleep(1000);
                BluetoothConnMgr.this.listenConnectRequest();
            }
            closeSvrSocket();
        }

        public void closeSvrSocket() {
            if (this.mmServerSocket != null) {
                try {
                    this.mmServerSocket.close();
                } catch (IOException e) {
                    Log.e(BluetoothConnMgr.TAG, "close bluetooth server socket error");
                }
            }
        }
    }

    enum ConnectState {
        Listening,
        Connected,
        Disconnected
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BluetoothConnMgr.TAG, "temp sockets not created");
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            if (!this.mmSocket.isConnected()) {
                try {
                    this.mmSocket.connect();
                } catch (IOException e) {
                    Log.e(BluetoothConnMgr.TAG, "connect socket exception.");
                    BluetoothConnMgr.this.connectionLost();
                    return;
                }
            }
            Log.d(BluetoothConnMgr.TAG, "bluetooth connected.");
            byte[] buffer = new byte[4096];
            while (true) {
                try {
                    if (((byte) this.mmInStream.read()) == BluetoothMessage.START_OF_FRAME) {
                        BluetoothMessage msg = new BluetoothMessage();
                        if (msg.parse(this.mmInStream, buffer)) {
                            BluetoothConnMgr.this.sendNewMsg(msg);
                        }
                    }
                } catch (IOException e2) {
                    Log.e(BluetoothConnMgr.TAG, "error occurs when reading.");
                    BluetoothConnMgr.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] buffer) {
            if (this.mmOutStream != null) {
                try {
                    this.mmOutStream.write(buffer);
                } catch (IOException e) {
                    Log.e(BluetoothConnMgr.TAG, "exception ocuurs during write.", e);
                }
            }
        }

        public void cancel() {
            if (this.mmSocket != null) {
                try {
                    this.mmSocket.close();
                } catch (IOException e) {
                    Log.e(BluetoothConnMgr.TAG, "close  connect socket failed", e);
                }
            }
        }
    }

    public BluetoothConnMgr(Handler handler) {
        this.mHandler = handler;
    }

    public boolean isBluetoothEnable() {
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.mAdapter == null || !this.mAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    private void sendNewMsg(BluetoothMessage msg) {
        if (msg.eventId == (byte) 2) {
            this.mHandler.obtainMessage(2, msg.eventType, AutoGrabTools.byteArrayToInt(msg.notifyVal)).sendToTarget();
        }
    }

    private void setState(ConnectState state) {
        this.mState = state;
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    public ConnectState getState() {
        return this.mState;
    }

    private void connectionLost() {
        listenConnectRequest();
    }

    public void sendMessage(BluetoothMessage msg) {
        if (this.mConnectedThread != null) {
            Log.d(TAG, "send bluetooth message.");
            this.mConnectedThread.write(msg.encode());
        }
    }

    public void listenConnectRequest() {
        if (!isBluetoothEnable()) {
            Log.w(TAG, "bluetooth is not enable.");
        } else if (this.mState == ConnectState.Listening) {
            Log.d(TAG, "is already listening.");
        } else {
            cancleCurrentThread();
            setState(ConnectState.Listening);
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        if (socket == null) {
            listenConnectRequest();
            return;
        }
        cancleCurrentThread();
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        setState(ConnectState.Connected);
    }

    private void cancleCurrentThread() {
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.closeSvrSocket();
            this.mAcceptThread = null;
        }
    }

    public void stopAllThreads() {
        cancleCurrentThread();
        setState(ConnectState.Disconnected);
    }
}
