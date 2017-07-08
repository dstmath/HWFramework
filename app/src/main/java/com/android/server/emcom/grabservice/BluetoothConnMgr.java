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
    private UUID mUUID;

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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.emcom.grabservice.BluetoothConnMgr.ConnectState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.emcom.grabservice.BluetoothConnMgr.ConnectState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.emcom.grabservice.BluetoothConnMgr.ConnectState.<clinit>():void");
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;
        final /* synthetic */ BluetoothConnMgr this$0;

        public ConnectedThread(BluetoothConnMgr this$0, BluetoothSocket socket) {
            this.this$0 = this$0;
            this.mmSocket = socket;
            InputStream inputStream = null;
            OutputStream tmpOut = null;
            try {
                inputStream = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BluetoothConnMgr.TAG, "temp sockets not created");
            }
            this.mmInStream = inputStream;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            if (!this.mmSocket.isConnected()) {
                try {
                    this.mmSocket.connect();
                } catch (IOException e) {
                    Log.e(BluetoothConnMgr.TAG, "connect socket exception.");
                    this.this$0.connectionLost();
                    return;
                }
            }
            Log.d(BluetoothConnMgr.TAG, "bluetooth connected.");
            byte[] buffer = new byte[BluetoothConnMgr.BUFFER_SIZE];
            while (true) {
                try {
                    if (((byte) this.mmInStream.read()) == 90) {
                        BluetoothMessage msg = new BluetoothMessage();
                        if (msg.parse(this.mmInStream, buffer)) {
                            this.this$0.sendNewMsg(msg);
                        }
                    }
                } catch (IOException e2) {
                    Log.e(BluetoothConnMgr.TAG, "error occurs when reading.");
                    this.this$0.connectionLost();
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
        this.mUUID = UUID.fromString(BASE_UUID);
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
        if (msg.eventId == 2) {
            this.mHandler.obtainMessage(1, msg.eventType, AutoGrabTools.byteArrayToInt(msg.notifyVal)).sendToTarget();
        }
    }

    private void setState(ConnectState state) {
        this.mState = state;
        this.mHandler.obtainMessage(0).sendToTarget();
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
        this.mConnectedThread = new ConnectedThread(this, socket);
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
