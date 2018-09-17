package com.huawei.dubai.client;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Parcel;
import android.os.SystemClock;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BufferedLogClient implements Runnable, Callback {
    public static final int FAIL = -1;
    private static final int MAX_SOCKET_CONNECTION_RETRY = 10;
    private static final int MESSAGE_BUFFERED_LOG_RECEIVED = 2;
    private static final int MESSAGE_CONNECTION_FAILED = 1;
    private static final int MESSAGE_CONNECTION_SUCCESS = 0;
    private static final String SOCKET_NAME = "DUBAI_BUFFERED_LOG";
    public static final int SUCCESS = 0;
    private static final String TAG = "BufferedLogClient";
    private BufferLogCallback mCallback;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private InputStream mIn;
    private String mLogType;
    private OutputStream mOut;
    private LocalSocket mSocket;
    private Thread mThread;
    private int retry = 0;

    public interface BufferLogCallback {
        void onConnectionFailed();

        void onConnectionSuccess();

        void onLogReceived(int i, Parcel parcel);
    }

    public BufferedLogClient(BufferLogCallback callback, String logType) {
        if (callback == null || logType == null) {
            Log.e(TAG, "Invalid parameter");
        }
        this.mSocket = null;
        this.mIn = null;
        this.mOut = null;
        this.mCallback = callback;
        this.mLogType = logType;
        this.mThread = null;
        this.mHandlerThread = new HandlerThread("BufferedLogHandler");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper(), this);
    }

    public int startToListen() {
        if (this.mCallback == null) {
            Log.e(TAG, "Invalid client callback");
            return -1;
        }
        if (this.mThread == null || (this.mThread.isAlive() ^ 1) != 0) {
            this.mThread = new Thread(this, "BufferLogClient");
            this.mThread.start();
        }
        return 0;
    }

    public int sendBufferedLog(Parcel parcel) {
        return sendBufferedLog(0, parcel);
    }

    public int sendBufferedLog(int magic, Parcel parcel) {
        byte[] data = parcel.marshall();
        int length = data.length;
        if (this.mCallback == null) {
            Log.e(TAG, "Invalid client callback");
            return -1;
        } else if ((this.mSocket == null || this.mOut == null) && connect() < 0) {
            Log.e(TAG, "Failed to connect to server");
            return -1;
        } else {
            if (!(this.mSocket == null || this.mOut == null)) {
                try {
                    this.mOut.write(setTransmit(length, magic, data));
                } catch (IOException e) {
                    Log.e(TAG, "Failed to write message to server");
                    return -1;
                }
            }
            return 0;
        }
    }

    private int connect() {
        this.retry = 10;
        while (this.retry > 0) {
            try {
                return connectToServer();
            } catch (IOException e) {
                Log.e(TAG, "Failed to connect to dubai buffered log server, sleep 5s to try again, retry =" + ((10 - this.retry) + 1));
                SystemClock.sleep(5000);
                this.retry--;
            }
        }
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        }
        return -1;
    }

    private synchronized int connectToServer() throws IOException {
        if (this.mSocket != null && this.mIn != null && this.mOut != null) {
            return 0;
        }
        this.mSocket = new LocalSocket();
        LocalSocketAddress address = new LocalSocketAddress(SOCKET_NAME, Namespace.ABSTRACT);
        byte[] logType = this.mLogType.getBytes("UTF-8");
        int length = logType.length;
        if (length <= 0) {
            Log.e(TAG, "Invalid log type");
            return -1;
        }
        try {
            this.mSocket.connect(address);
            this.mIn = this.mSocket.getInputStream();
            this.mOut = this.mSocket.getOutputStream();
            this.mOut.write(setTransmit(length, 0, logType));
            if (this.mHandler != null) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
            }
            this.retry = 10;
            return 0;
        } catch (IOException ex) {
            Log.e(TAG, "Failed to communicate with dubai buffered log server");
            this.mSocket.close();
            this.mSocket = null;
            this.mIn = null;
            this.mOut = null;
            throw ex;
        }
    }

    private byte[] setTransmit(int len, int magic, byte[] data) {
        byte[] bLen = int2Byte(len);
        byte[] bMagic = int2Byte(magic);
        byte[] res = new byte[((bLen.length + bMagic.length) + data.length)];
        System.arraycopy(bLen, 0, res, 0, bLen.length);
        int offset = bLen.length + 0;
        System.arraycopy(bMagic, 0, res, offset, bMagic.length);
        System.arraycopy(data, 0, res, offset + bMagic.length, data.length);
        return res;
    }

    private byte[] int2Byte(int value) {
        return new byte[]{(byte) ((value >>> 0) & 255), (byte) ((value >>> 8) & 255), (byte) ((value >>> 16) & 255), (byte) ((value >>> 24) & 255)};
    }

    public void run() {
        if ((this.mSocket == null || this.mIn == null) && connect() != 0) {
            Log.e(TAG, "Failed to connect to server");
            return;
        }
        if (!(this.mSocket == null || this.mIn == null)) {
            try {
                listenToServer();
            } catch (Exception e) {
                Log.e(TAG, "Failed to listen to buffered log server");
            }
        }
        if (this.mHandler != null) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        }
    }

    private void listenToServer() throws IOException {
        Parcel parcel = Parcel.obtain();
        while (true) {
            try {
                int length = readInt(this.mIn);
                if (length < 0) {
                    Log.e(TAG, "Failed to read length");
                    break;
                }
                int magic = readInt(this.mIn);
                if (magic < 0) {
                    Log.e(TAG, "Failed to read magic");
                    break;
                }
                byte[] buffer = new byte[length];
                if (this.mIn.read(buffer) != length) {
                    Log.e(TAG, "Failed to read log message");
                    break;
                }
                parcel.unmarshall(buffer, 0, length);
                parcel.setDataPosition(0);
                if (this.mHandler != null) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(2, magic, 0, parcel));
                }
            } catch (IOException ex) {
                Log.e(TAG, "Failed to read socket outputstrem");
                throw ex;
            } catch (Throwable th) {
                try {
                    if (this.mSocket != null) {
                        this.mSocket.close();
                        this.mSocket = null;
                        this.mIn = null;
                        this.mOut = null;
                    }
                } catch (IOException ex2) {
                    Log.e(TAG, "Failed to close socket");
                    throw ex2;
                }
            }
        }
        try {
            if (this.mSocket != null) {
                this.mSocket.close();
                this.mSocket = null;
                this.mIn = null;
                this.mOut = null;
            }
        } catch (IOException ex22) {
            Log.e(TAG, "Failed to close socket");
            throw ex22;
        }
    }

    private static int readInt(InputStream in) throws IOException {
        try {
            return (((in.read() << 0) + (in.read() << 8)) + (in.read() << 16)) + (in.read() << 24);
        } catch (IOException ex) {
            Log.e(TAG, "Failed to read int");
            throw ex;
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
                this.mCallback.onConnectionSuccess();
                break;
            case 1:
                this.mCallback.onConnectionFailed();
                break;
            case 2:
                this.mCallback.onLogReceived(msg.arg1, msg.obj);
                break;
            default:
                Log.e(TAG, "Unknown message: " + msg.what);
                return false;
        }
        return true;
    }
}
