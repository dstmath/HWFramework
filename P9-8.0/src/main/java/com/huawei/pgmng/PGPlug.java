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
                SystemClock.sleep(TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
            }
            this.retries--;
        }
        this.mCallbacks.onConnectedTimeout();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1000:
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

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0055 A:{SYNTHETIC, Splitter: B:20:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005a A:{Catch:{ IOException -> 0x0073 }} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0055 A:{SYNTHETIC, Splitter: B:20:0x0055} */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x005a A:{Catch:{ IOException -> 0x0073 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void listenToSocket() throws IOException {
        IOException ex;
        Throwable th;
        LocalSocket socket = null;
        BufferedReader in = null;
        try {
            LocalSocket socket2 = new LocalSocket();
            try {
                socket2.connect(new LocalSocketAddress(this.mSocket, Namespace.ABSTRACT));
                BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream(), "UTF-8"));
                try {
                    this.mCallbacks.onDaemonConnected();
                    this.retries = 10;
                    while (true) {
                        String content = in2.readLine();
                        if (content == null) {
                            break;
                        } else if (this.mCallbackHandler != null) {
                            this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(1000, content));
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
                    if (socket2 != null) {
                        socket2.close();
                    }
                } catch (IOException e) {
                    ex2 = e;
                    in = in2;
                    socket = socket2;
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
                        if (socket != null) {
                            socket.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                    socket = socket2;
                    if (in != null) {
                    }
                    if (socket != null) {
                    }
                    throw th;
                }
            } catch (IOException e2) {
                ex22 = e2;
                socket = socket2;
                Log.e(this.TAG, "Communications to PG-server error", ex22);
                throw ex22;
            } catch (Throwable th4) {
                th = th4;
                socket = socket2;
                if (in != null) {
                }
                if (socket != null) {
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
