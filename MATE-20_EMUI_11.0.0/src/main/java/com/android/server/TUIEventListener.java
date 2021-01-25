package com.android.server;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.PowerManager;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/* access modifiers changed from: package-private */
/* compiled from: TrustedUIService */
public final class TUIEventListener implements Runnable {
    private static final String TAG = "TrustedUIListener";
    private static final String TUI_MSG_CHAR_SET = "US-ASCII";
    private static final String TUI_MSG_CONNECTED = "connected_tui";
    private static final String TUI_MSG_DISCONNECTED = "disconnected_tui";
    private static final int TUI_MSG_MAX_LEN = 20;
    private static final String TUI_SOCKET = "tui_daemon";
    private static boolean hasDeamon = false;
    private Context mContext;
    private TrustedUIService mTrustedUIService;
    PowerManager.WakeLock mWakeLock;

    TUIEventListener(TrustedUIService service, Context context) {
        this.mTrustedUIService = service;
        this.mContext = context;
        if (this.mContext.getSystemService("power") instanceof PowerManager) {
            this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(536870938, "*tui*");
        } else {
            Log.e(TAG, "power wake lock get handle failed");
        }
    }

    private String parseEvent(byte[] buffer) {
        CharsetDecoder coder = Charset.forName(TUI_MSG_CHAR_SET).newDecoder();
        for (int i = 0; i < buffer.length; i++) {
            try {
                if (buffer[i] == 0) {
                    return coder.decode(ByteBuffer.wrap(buffer, 0, i)).toString();
                }
            } catch (CharacterCodingException e) {
                Log.e(TAG, "parse buffer to string error in TUIListener thread!");
                return "";
            }
        }
        return "";
    }

    private void processTUIEvent(byte[] buffer) {
        String event = parseEvent(buffer);
        Log.e(TAG, "process TUI event : " + event + "'");
        if (event.compareTo(TUI_MSG_CONNECTED) == 0) {
            Log.d(TAG, "WakeLock acquire");
            this.mTrustedUIService.getScreenSize();
            TrustedUIService trustedUIService = this.mTrustedUIService;
            trustedUIService.sendTUICmd(26, 0, trustedUIService.screenInfo);
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

    private void waitTUIEvent() {
        LocalSocket socket = null;
        InputStream inputStream = null;
        try {
            LocalSocket socket2 = new LocalSocket();
            socket2.connect(new LocalSocketAddress(TUI_SOCKET, LocalSocketAddress.Namespace.RESERVED));
            InputStream inputStream2 = socket2.getInputStream();
            if (!hasDeamon) {
                setHasDeamon(true);
            }
            byte[] buffer = new byte[20];
            while (inputStream2.read(buffer) >= 0) {
                processTUIEvent(buffer);
            }
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "WakeLock release finally");
                this.mWakeLock.release();
                this.mTrustedUIService.setTrustedUIStatus(false);
            }
            try {
                socket2.close();
            } catch (IOException e) {
                Log.w(TAG, "IOException closing socket");
            }
            try {
                inputStream2.close();
            } catch (IOException e2) {
                Log.w(TAG, "IOException closing inputStream");
            }
        } catch (IOException e3) {
            Log.e(TAG, "Could not open listener socket");
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "WakeLock release finally");
                this.mWakeLock.release();
                this.mTrustedUIService.setTrustedUIStatus(false);
            }
            if (0 != 0) {
                try {
                    socket.close();
                } catch (IOException e4) {
                    Log.w(TAG, "IOException closing socket");
                }
            }
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (this.mWakeLock.isHeld()) {
                Log.d(TAG, "WakeLock release finally");
                this.mWakeLock.release();
                this.mTrustedUIService.setTrustedUIStatus(false);
            }
            if (0 != 0) {
                try {
                    socket.close();
                } catch (IOException e5) {
                    Log.w(TAG, "IOException closing socket");
                }
            }
            if (0 != 0) {
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

    @Override // java.lang.Runnable
    public void run() {
        while (true) {
            try {
                if (!this.mTrustedUIService.TUIServiceLibraryInit()) {
                    Log.e(TAG, " TUIServiceLibraryInit failed.");
                }
                this.mTrustedUIService.getScreenSize();
                TrustedUIService trustedUIService = this.mTrustedUIService;
                TrustedUIService trustedUIService2 = this.mTrustedUIService;
                trustedUIService.sendTUICmd(26, 0, this.mTrustedUIService.screenInfo);
                waitTUIEvent();
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
