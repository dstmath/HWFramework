package com.android.server;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.PowerManager;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: TrustedUIService */
final class TUIEventListener implements Runnable {
    private static final String TAG = "TrustedUIListener";
    private static final String TUI_MSG_CONNECTED = "connected_tui";
    private static final String TUI_MSG_DISCONNECTED = "disconnected_tui";
    private static final String TUI_SOCKET = "tui_daemon";
    private static boolean hasDeamon = false;
    private Context mContext;
    private TrustedUIService mTrustedUIService;
    PowerManager.WakeLock mWakeLock;

    TUIEventListener(TrustedUIService service, Context context) {
        this.mTrustedUIService = service;
        this.mContext = context;
        Context context2 = this.mContext;
        Context context3 = this.mContext;
        this.mWakeLock = ((PowerManager) context2.getSystemService("power")).newWakeLock(536870938, "*tui*");
    }

    private void handleTUIEvent(String event) {
        Log.d(TAG, "handleEvent '" + event + "'");
        if (event.compareTo(TUI_MSG_CONNECTED) == 0) {
            Log.d(TAG, "WakeLock acquire");
            if (!this.mWakeLock.isHeld()) {
                this.mWakeLock.acquire();
            } else {
                Log.d(TAG, "have lock already, so no need to acquire WakeLock ");
            }
            this.mTrustedUIService.setTrustedUIStatus(true);
        } else if (event.compareTo(TUI_MSG_DISCONNECTED) == 0) {
            Log.d(TAG, "WakeLock release");
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            } else {
                Log.d(TAG, "do not have lock, so no need to release WakeLock ");
            }
            this.mTrustedUIService.setTrustedUIStatus(false);
        } else {
            Log.d(TAG, " do nothing");
        }
    }

    private static void setHasDeamon(boolean has) {
        hasDeamon = has;
    }

    private void listenToTUISocket() {
        LocalSocket socket = null;
        InputStream inputStream = null;
        try {
            socket = new LocalSocket();
            socket.connect(new LocalSocketAddress(TUI_SOCKET, LocalSocketAddress.Namespace.RESERVED));
            inputStream = socket.getInputStream();
            byte[] buffer = new byte[512];
            if (!hasDeamon) {
                setHasDeamon(true);
            }
            while (true) {
                int count = inputStream.read(buffer);
                if (count < 0) {
                    break;
                }
                int start = 0;
                for (int i = 0; i < count; i++) {
                    if (buffer[i] == 0) {
                        handleTUIEvent(new String(buffer, start, i - start));
                        start = i + 1;
                    }
                }
            }
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "WakeLock release finally");
                this.mWakeLock.release();
                this.mTrustedUIService.setTrustedUIStatus(false);
            }
            try {
                socket.close();
            } catch (IOException e) {
                Log.w(TAG, "IOException closing socket");
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    Log.w(TAG, "IOException closing inputStream");
                }
            }
        } catch (IOException e3) {
            Log.e(TAG, "Could not open listener socket");
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "WakeLock release finally");
                this.mWakeLock.release();
                this.mTrustedUIService.setTrustedUIStatus(false);
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e4) {
                    Log.w(TAG, "IOException closing socket");
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "WakeLock release finally");
                this.mWakeLock.release();
                this.mTrustedUIService.setTrustedUIStatus(false);
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e5) {
                    Log.w(TAG, "IOException closing socket");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                    Log.w(TAG, "IOException closing inputStream");
                }
            }
            throw th;
        }
        Log.d(TAG, "Failed to connect to TUI daemon", new IllegalStateException());
    }

    public void run() {
        while (true) {
            try {
                if (!this.mTrustedUIService.TUIServiceLibraryInit()) {
                    Log.e(TAG, " TUIServiceLibraryInit failed.");
                }
                listenToTUISocket();
                if (!hasDeamon) {
                    Log.d(TAG, "no need loop tui services");
                    return;
                }
                Log.d(TAG, "loop tui services");
            } catch (Throwable t) {
                Log.e(TAG, "Fatal error " + t + " in TUIListener thread!");
            }
        }
    }
}
