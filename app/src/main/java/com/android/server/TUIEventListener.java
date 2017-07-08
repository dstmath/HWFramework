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
    private Context mContext;
    private TrustedUIService mTrustedUIService;
    WakeLock mWakeLock;

    TUIEventListener(TrustedUIService service, Context context) {
        this.mTrustedUIService = service;
        this.mContext = context;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(536870938, "*tui*");
    }

    private void handleTUIEvent(String event) {
        Log.d(TAG, "handleEvent '" + event + "'");
        if (event.compareTo(TUI_MSG_CONNECTED) == 0) {
            Log.d(TAG, "WakeLock acquire");
            this.mWakeLock.acquire();
            this.mTrustedUIService.setTrustedUIStatus(true);
        } else if (event.compareTo(TUI_MSG_DISCONNECTED) == 0) {
            Log.d(TAG, "WakeLock release");
            this.mWakeLock.release();
            this.mTrustedUIService.setTrustedUIStatus(false);
        } else {
            Log.d(TAG, " do nothing");
        }
    }

    private void listenToTUISocket() {
        Throwable th;
        LocalSocket localSocket = null;
        InputStream inputStream = null;
        try {
            LocalSocket socket = new LocalSocket();
            try {
                socket.connect(new LocalSocketAddress(TUI_SOCKET, Namespace.RESERVED));
                inputStream = socket.getInputStream();
                byte[] buffer = new byte[DumpState.DUMP_MESSAGES];
                while (true) {
                    int count = inputStream.read(buffer);
                    if (count < 0) {
                        break;
                    }
                    int start = 0;
                    for (int i = 0; i < count; i++) {
                        if (buffer[i] == null) {
                            handleTUIEvent(new String(buffer, start, i - start));
                            start = i + 1;
                        }
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
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
                localSocket = socket;
            } catch (IOException e3) {
                localSocket = socket;
            } catch (Throwable th2) {
                th = th2;
                localSocket = socket;
            }
        } catch (IOException e4) {
            try {
                Log.e(TAG, "Could not open listener socket");
                if (localSocket != null) {
                    try {
                        localSocket.close();
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
                Log.d(TAG, "Failed to connect to TUI daemon", new IllegalStateException());
            } catch (Throwable th3) {
                th = th3;
                if (localSocket != null) {
                    try {
                        localSocket.close();
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
                throw th;
            }
        }
        Log.d(TAG, "Failed to connect to TUI daemon", new IllegalStateException());
    }

    public void run() {
        try {
            if (!this.mTrustedUIService.TUIServiceLibraryInit()) {
                Log.e(TAG, " TUIServiceLibraryInit failed.");
            }
            listenToTUISocket();
        } catch (Throwable t) {
            Log.e(TAG, "Fatal error " + t + " in TUIListener thread!");
        }
    }
}
