package com.leisen.wallet.sdk.newhttp;

import android.util.Log;
import com.leisen.wallet.sdk.util.LogUtil;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;

public class AsyncHttpRequestX implements Runnable {
    private static final String TAG = "AsyncHttpRequestX";
    private boolean cancelIsNotified = false;
    private int executionCount;
    private boolean isCancelled = false;
    private boolean isFinished = false;
    private final String request;
    private final ResponseHandlerInterfaceX responseHandler;
    private final HttpURLConnection urlConnection;

    public AsyncHttpRequestX(HttpURLConnection conn, String request2, ResponseHandlerInterfaceX responseHandler2) {
        this.urlConnection = conn;
        this.request = request2;
        this.responseHandler = responseHandler2;
    }

    public void run() {
        if (!isCancelled()) {
            if (this.responseHandler != null) {
                this.responseHandler.sendStartMessage();
            }
            if (!isCancelled()) {
                try {
                    makeRequestWithRetries();
                } catch (Exception e) {
                    if (isCancelled() || this.responseHandler == null) {
                        Log.e("AsyncHttpRequest", "makeRequestWithRetries returned error, but handler is null" + e);
                    } else {
                        this.responseHandler.sendFailureMessage(0, null, e);
                    }
                }
                if (!isCancelled()) {
                    if (this.responseHandler != null) {
                        this.responseHandler.sendFinishMessage();
                    }
                    this.isFinished = true;
                }
            }
        }
    }

    private void makeRequestWithRetries() throws IOException {
        boolean retry = true;
        int maxRetryCnt = 3;
        IOException cause = null;
        while (retry) {
            maxRetryCnt--;
            if (maxRetryCnt <= 0) {
                retry = false;
            }
            try {
                makeRequest();
                return;
            } catch (UnknownHostException e) {
                cause = new IOException("UnknownHostException exception:" + e.getMessage());
            } catch (NullPointerException e2) {
                cause = new IOException("NPE in HttpClient: " + e2.getMessage());
            } catch (IOException e3) {
                try {
                    if (!isCancelled()) {
                        cause = e3;
                    } else {
                        return;
                    }
                } catch (Exception e4) {
                    cause = new IOException("Unhandled exception: " + e4.getMessage());
                }
            }
        }
        Log.e(TAG, "Unhandled exception origin fcause" + cause.getMessage());
        throw cause;
    }

    private void makeRequest() throws IOException {
        if (!isCancelled()) {
            LogUtil.e(TAG, "==>get response before");
            this.urlConnection.connect();
            DataOutputStream wr = new DataOutputStream(this.urlConnection.getOutputStream());
            wr.writeBytes(this.request);
            wr.flush();
            wr.close();
            LogUtil.e(TAG, "==>get response after = " + this.urlConnection.getResponseCode());
            if (!isCancelled() && this.responseHandler != null) {
                this.responseHandler.sendResponseMessage(this.urlConnection);
            }
        }
    }

    public boolean isCancelled() {
        if (this.isCancelled) {
            sendCancelNotification();
        }
        return this.isCancelled;
    }

    private synchronized void sendCancelNotification() {
        if (!this.isFinished && this.isCancelled && !this.cancelIsNotified) {
            this.cancelIsNotified = true;
            if (this.responseHandler != null) {
                this.responseHandler.sendCancelMessage();
            }
        }
    }

    public boolean isDone() {
        return isCancelled() || this.isFinished;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        this.isCancelled = true;
        this.urlConnection.disconnect();
        return isCancelled();
    }
}
