package com.huawei.pgmng;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.TimedRemoteCaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PGPlug implements Runnable, Callback {
    private static final int EVENT_DISPATCH_MESSAGE = 1000;
    private static final int MAX_TRY_COUNT = 10;
    private String TAG;
    private Handler mCallbackHandler;
    private IPGPlugCallbacks mCallbacks;
    private String mSocket;
    private int retries;

    public PGPlug(IPGPlugCallbacks callbacks, String logTag) {
        this.TAG = "PGPlug";
        this.mSocket = "pg-socket";
        this.retries = 0;
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
        this.retries = MAX_TRY_COUNT;
        while (this.retries > 0) {
            try {
                listenToSocket();
            } catch (Exception e) {
                Log.e(this.TAG, "Error in connect to PG, sleep 5s try again, retries =" + ((10 - this.retries) + 1), e);
                SystemClock.sleep(TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
            }
            this.retries--;
        }
        this.mCallbacks.onConnectedTimeout();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_DISPATCH_MESSAGE /*1000*/:
                String event = msg.obj;
                try {
                    String[] token = event.split("\\|");
                    if (!this.mCallbacks.onEvent(Integer.parseInt(token[0]), token[1])) {
                        Log.w(this.TAG, "Unhandled event " + event);
                        break;
                    }
                } catch (Exception e) {
                    Log.e(this.TAG, "Error handling '" + event + "': " + e);
                    break;
                }
                break;
        }
        return true;
    }

    private void listenToSocket() throws IOException {
        IOException ex;
        Throwable th;
        LocalSocket localSocket = null;
        BufferedReader in = null;
        try {
            LocalSocket socket = new LocalSocket();
            try {
                socket.connect(new LocalSocketAddress(this.mSocket, Namespace.ABSTRACT));
                BufferedReader in2 = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                try {
                    this.mCallbacks.onDaemonConnected();
                    this.retries = MAX_TRY_COUNT;
                    while (true) {
                        String content = in2.readLine();
                        if (content == null) {
                            break;
                        } else if (this.mCallbackHandler != null) {
                            this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(EVENT_DISPATCH_MESSAGE, content));
                        }
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException ex2) {
                            Log.w(this.TAG, "Failed closing socket", ex2);
                            throw ex2;
                        }
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    ex2 = e;
                    in = in2;
                    localSocket = socket;
                    try {
                        Log.e(this.TAG, "Communications to PG-server error", ex2);
                        throw ex2;
                    } catch (Throwable th2) {
                        th = th2;
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ex22) {
                                Log.w(this.TAG, "Failed closing socket", ex22);
                                throw ex22;
                            }
                        }
                        if (localSocket != null) {
                            localSocket.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    localSocket = socket;
                    if (in != null) {
                        in.close();
                    }
                    if (localSocket != null) {
                        localSocket.close();
                    }
                    throw th;
                }
            } catch (IOException e2) {
                ex22 = e2;
                localSocket = socket;
                Log.e(this.TAG, "Communications to PG-server error", ex22);
                throw ex22;
            } catch (Throwable th4) {
                th = th4;
                localSocket = socket;
                if (in != null) {
                    in.close();
                }
                if (localSocket != null) {
                    localSocket.close();
                }
                throw th;
            }
        } catch (IOException e3) {
            ex22 = e3;
            Log.e(this.TAG, "Communications to PG-server error", ex22);
            throw ex22;
        }
    }
}
