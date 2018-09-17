package com.android.server;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
    WakeLock mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(536870938, "*tui*");

    TUIEventListener(TrustedUIService service, Context context) {
        this.mTrustedUIService = service;
        this.mContext = context;
    }

    private void handleTUIEvent(String event) {
        Log.d(TAG, "handleEvent '" + event + "'");
        if (event.compareTo(TUI_MSG_CONNECTED) == 0) {
            Log.d(TAG, "WakeLock acquire");
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "have lock already, so no need to acquire WakeLock ");
            } else {
                this.mWakeLock.acquire();
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

    /* JADX WARNING: Removed duplicated region for block: B:38:0x009f  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00b4 A:{SYNTHETIC, Splitter: B:40:0x00b4} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00b9 A:{SYNTHETIC, Splitter: B:43:0x00b9} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x00dc  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00f1 A:{SYNTHETIC, Splitter: B:54:0x00f1} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00f6 A:{SYNTHETIC, Splitter: B:57:0x00f6} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void listenToTUISocket() {
        Throwable th;
        LocalSocket socket = null;
        InputStream inputStream = null;
        try {
            LocalSocket socket2 = new LocalSocket();
            try {
                socket2.connect(new LocalSocketAddress(TUI_SOCKET, Namespace.RESERVED));
                inputStream = socket2.getInputStream();
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
                        if (buffer[i] == (byte) 0) {
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
                if (socket2 != null) {
                    try {
                        socket2.close();
                    } catch (IOException e) {
                        Log.w(TAG, "IOException closing socket");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        Log.w(TAG, "IOException closing inputStream");
                    }
                }
                socket = socket2;
            } catch (IOException e3) {
                socket = socket2;
                try {
                    Log.e(TAG, "Could not open listener socket");
                    if (this.mWakeLock.isHeld()) {
                    }
                    if (socket != null) {
                    }
                    if (inputStream != null) {
                    }
                    Log.d(TAG, "Failed to connect to TUI daemon", new IllegalStateException());
                } catch (Throwable th2) {
                    th = th2;
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
                        try {
                            inputStream.close();
                        } catch (IOException e5) {
                            Log.w(TAG, "IOException closing inputStream");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                socket = socket2;
                if (this.mWakeLock.isHeld()) {
                }
                if (socket != null) {
                }
                if (inputStream != null) {
                }
                throw th;
            }
        } catch (IOException e6) {
            Log.e(TAG, "Could not open listener socket");
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "WakeLock release finally");
                this.mWakeLock.release();
                this.mTrustedUIService.setTrustedUIStatus(false);
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e7) {
                    Log.w(TAG, "IOException closing socket");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e8) {
                    Log.w(TAG, "IOException closing inputStream");
                }
            }
            Log.d(TAG, "Failed to connect to TUI daemon", new IllegalStateException());
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
                if (hasDeamon) {
                    Log.d(TAG, "loop tui services");
                } else {
                    Log.d(TAG, "no need loop tui services");
                    return;
                }
            } catch (Throwable t) {
                Log.e(TAG, "Fatal error " + t + " in TUIListener thread!");
            }
        }
    }
}
