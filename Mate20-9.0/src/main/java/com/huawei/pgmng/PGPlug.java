package com.huawei.pgmng;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PGPlug implements Runnable, Handler.Callback {
    private static final int EVENT_DISPATCH_MESSAGE = 1000;
    private static final int MAX_TRY_COUNT = 10;
    private String TAG = "PGPlug";
    private Handler mCallbackHandler;
    private IPGPlugCallbacks mCallbacks;
    private String mSocket = "pg-socket";
    private int retries = 0;

    public PGPlug(IPGPlugCallbacks callbacks, String logTag) {
        this.mCallbacks = callbacks;
        if (logTag != null) {
            this.TAG = logTag;
        }
    }

    public void run() {
        if (this.mCallbacks == null) {
            Log.e(this.TAG, "client callback is null");
            return;
        }
        HandlerThread thread = new HandlerThread(this.TAG + ".CallbackHandler");
        thread.start();
        this.mCallbackHandler = new Handler(thread.getLooper(), this);
        this.retries = 10;
        while (this.retries > 0) {
            try {
                listenToSocket();
            } catch (Exception e) {
                Log.e(this.TAG, "Error in connect to PG, sleep 5s try again, retries =" + ((10 - this.retries) + 1), e);
                SystemClock.sleep(5000);
            }
            this.retries--;
        }
        this.mCallbacks.onConnectedTimeout();
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 1000) {
            String event = (String) msg.obj;
            try {
                String[] token = event.split("\\|");
                if (!this.mCallbacks.onEvent(Integer.parseInt(token[0]), token[1])) {
                    String str = this.TAG;
                    Log.w(str, "Unhandled event " + event);
                }
            } catch (Exception e) {
                String str2 = this.TAG;
                Log.e(str2, "Error handling '" + event + "': " + e);
            }
        }
        return true;
    }

    private void listenToSocket() throws IOException {
        LocalSocket socket = null;
        BufferedReader in = null;
        try {
            LocalSocket socket2 = new LocalSocket();
            socket2.connect(new LocalSocketAddress(this.mSocket, LocalSocketAddress.Namespace.ABSTRACT));
            BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream(), "UTF-8"));
            this.mCallbacks.onDaemonConnected();
            this.retries = 10;
            while (true) {
                String readLine = in2.readLine();
                String content = readLine;
                if (readLine == null) {
                    try {
                        in2.close();
                        socket2.close();
                        return;
                    } catch (IOException ex) {
                        Log.w(this.TAG, "Failed closing socket", ex);
                        throw ex;
                    }
                } else if (this.mCallbackHandler != null) {
                    this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(1000, content));
                }
            }
        } catch (IOException ex2) {
            Log.e(this.TAG, "Communications to PG-server error", ex2);
            throw ex2;
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex3) {
                    Log.w(this.TAG, "Failed closing socket", ex3);
                    throw ex3;
                }
            }
            if (socket != null) {
                socket.close();
            }
            throw th;
        }
    }
}
