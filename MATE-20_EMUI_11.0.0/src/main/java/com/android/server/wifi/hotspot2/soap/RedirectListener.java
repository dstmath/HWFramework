package com.android.server.wifi.hotspot2.soap;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WifiDiagnostics;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Random;

public class RedirectListener extends NanoHTTPD {
    private static final String TAG = "PasspointRedirectListener";
    @VisibleForTesting
    static final int USER_TIMEOUT_MILLIS = 600000;
    private final Handler mHandler;
    private final String mPath;
    private RedirectCallback mRedirectCallback;
    private final URL mServerUrl;
    private Runnable mTimeOutTask = new Runnable() {
        /* class com.android.server.wifi.hotspot2.soap.$$Lambda$RedirectListener$bYoYNT_lbW4WMGJVpKLEbMvo1nY */

        @Override // java.lang.Runnable
        public final void run() {
            RedirectListener.this.lambda$new$0$RedirectListener();
        }
    };

    public interface RedirectCallback {
        void onRedirectReceived();

        void onRedirectTimedOut();
    }

    @VisibleForTesting
    RedirectListener(Looper looper, int port) throws IOException {
        super(InetAddress.getLocalHost().getHostAddress(), port);
        Random rnd = new Random(System.currentTimeMillis());
        this.mPath = "rnd" + Integer.toString(Math.abs(rnd.nextInt()), 36);
        this.mServerUrl = new URL("http", getHostname(), port, this.mPath);
        this.mHandler = new Handler(looper);
    }

    public /* synthetic */ void lambda$new$0$RedirectListener() {
        this.mRedirectCallback.onRedirectTimedOut();
    }

    public static RedirectListener createInstance(Looper looper) {
        try {
            ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLocalHost());
            RedirectListener redirectListener = new RedirectListener(looper, serverSocket.getLocalPort());
            redirectListener.setServerSocketFactory(new NanoHTTPD.ServerSocketFactory(serverSocket) {
                /* class com.android.server.wifi.hotspot2.soap.$$Lambda$RedirectListener$WD4dDuOi078sivttoxi_q1KrlBk */
                private final /* synthetic */ ServerSocket f$0;

                {
                    this.f$0 = r1;
                }

                @Override // fi.iki.elonen.NanoHTTPD.ServerSocketFactory
                public final ServerSocket create() {
                    return this.f$0.close();
                }
            });
            return redirectListener;
        } catch (IOException e) {
            Log.e(TAG, "fails to create an instance: " + e);
            return null;
        }
    }

    public boolean startServer(RedirectCallback callback, Handler startHandler) {
        if (callback == null || startHandler == null) {
            return false;
        }
        if (isAlive()) {
            Log.e(TAG, "redirect listener is already running");
            return false;
        }
        this.mRedirectCallback = callback;
        startHandler.post(new Runnable() {
            /* class com.android.server.wifi.hotspot2.soap.$$Lambda$RedirectListener$BV5GEu4y_1xZZPgxjUTYfPn5y4g */

            @Override // java.lang.Runnable
            public final void run() {
                RedirectListener.this.lambda$startServer$2$RedirectListener();
            }
        });
        this.mHandler.postDelayed(this.mTimeOutTask, WifiDiagnostics.MIN_DUMP_TIME_WINDOW_MILLIS);
        return true;
    }

    public /* synthetic */ void lambda$startServer$2$RedirectListener() {
        try {
            start();
        } catch (IOException e) {
            Log.e(TAG, "unable to start redirect listener: " + e);
        }
    }

    public void stopServer(Handler stopHandler) {
        if (this.mHandler.hasCallbacks(this.mTimeOutTask)) {
            this.mHandler.removeCallbacks(this.mTimeOutTask);
        }
        if (stopHandler != null && isServerAlive()) {
            stopHandler.post(new Runnable() {
                /* class com.android.server.wifi.hotspot2.soap.$$Lambda$RedirectListener$dKUAH_fXF5t_jzQdMpZ6UV6hOc */

                @Override // java.lang.Runnable
                public final void run() {
                    RedirectListener.this.lambda$stopServer$3$RedirectListener();
                }
            });
        }
    }

    public /* synthetic */ void lambda$stopServer$3$RedirectListener() {
        stop();
    }

    public boolean isServerAlive() {
        return isAlive();
    }

    public URL getServerUrl() {
        return this.mServerUrl;
    }

    @Override // fi.iki.elonen.NanoHTTPD
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        if (session.getMethod() != NanoHTTPD.Method.GET || !this.mServerUrl.getPath().equals(session.getUri())) {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_HTML, "");
        }
        this.mHandler.removeCallbacks(this.mTimeOutTask);
        this.mRedirectCallback.onRedirectReceived();
        return newFixedLengthResponse("");
    }
}
