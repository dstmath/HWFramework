package com.leisen.wallet.sdk.newhttp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.leisen.wallet.sdk.util.LogUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class AsyncHttpResponseHandlerX implements ResponseHandlerInterfaceX {
    protected static final int BUFFER_SIZE = 4096;
    protected static final int CANCEL_MESSAGE = 6;
    public static final String DEFAULT_CAHRSET = "UTF_8";
    protected static final int FALIURE_MESSAGE = 1;
    protected static final int FINISH_MESSAGE = 3;
    protected static final int PROGRESS_MESSAGE = 4;
    protected static final int RETRY_MESSAGE = 5;
    protected static final int START_MESSAGE = 2;
    protected static final int SUCCESS_MESSAGE = 0;
    private static final String TAG = "AsyncHttpResHandlerX";
    private Handler handler;
    private URL requestURI = null;
    private String responseCharset = DEFAULT_CAHRSET;
    private boolean useSynchronousMode;

    private static class ResponderHandler extends Handler {
        private final AsyncHttpResponseHandlerX mResponder;

        public ResponderHandler(AsyncHttpResponseHandlerX mResponder2) {
            this.mResponder = mResponder2;
        }

        public void handleMessage(Message msg) {
            this.mResponder.handleMessage(msg);
        }
    }

    public abstract void onFailure(int i, byte[] bArr, Throwable th);

    public abstract void onSuccess(int i, byte[] bArr);

    public AsyncHttpResponseHandlerX() {
        setUseSynchronousMode(true);
    }

    public URL getRequestURI() {
        return this.requestURI;
    }

    public void setRequestURI(URL requestURI2) {
        this.requestURI = requestURI2;
    }

    public void setUseSynchronousMode(boolean useSynchronousMode2) {
        if (!useSynchronousMode2 && Looper.myLooper() == null) {
            useSynchronousMode2 = true;
        }
        if (!useSynchronousMode2 && this.handler == null) {
            this.handler = new ResponderHandler(this);
        } else if (useSynchronousMode2 && this.handler != null) {
            this.handler = null;
        }
        this.useSynchronousMode = useSynchronousMode2;
    }

    public boolean getUseSynchronousMode() {
        return this.useSynchronousMode;
    }

    public void setCharset(String charset) {
        this.responseCharset = charset;
    }

    public String getCharset() {
        return this.responseCharset == null ? DEFAULT_CAHRSET : this.responseCharset;
    }

    public void sendResponseMessage(HttpURLConnection urlConnection) throws IOException {
        if (!Thread.currentThread().isInterrupted()) {
            int status = urlConnection.getResponseCode();
            byte[] responseBody = getResponseData(urlConnection);
            if (!Thread.currentThread().isInterrupted()) {
                LogUtil.e(TAG, "==>" + status + "==" + new String(responseBody));
                if (status > 300) {
                    sendFailureMessage(status, responseBody, new HttpRetryException(urlConnection.getResponseMessage(), status));
                } else {
                    sendSuccessMessage(status, responseBody);
                }
            }
        }
    }

    public void sendStartMessage() {
        sendMessage(obtainMessage(2, null));
    }

    public void sendFinishMessage() {
        sendMessage(obtainMessage(3, null));
    }

    public void sendProgressMessage(int bytesWritten, int bytesTotal) {
        sendMessage(obtainMessage(4, new Object[]{Integer.valueOf(bytesWritten), Integer.valueOf(bytesTotal)}));
    }

    public void sendCancelMessage() {
        sendMessage(obtainMessage(6, null));
    }

    public void sendSuccessMessage(int statusCode, byte[] responseBody) {
        sendMessage(obtainMessage(0, new Object[]{Integer.valueOf(statusCode), responseBody}));
    }

    public void sendFailureMessage(int statusCode, byte[] responseBody, Throwable error) {
        sendMessage(obtainMessage(1, new Object[]{Integer.valueOf(statusCode), responseBody, error}));
    }

    public void sendRetryMessage(int retryNo) {
        sendMessage(obtainMessage(5, new Object[]{Integer.valueOf(retryNo)}));
    }

    private void sendMessage(Message msg) {
        if (getUseSynchronousMode() || this.handler == null) {
            handleMessage(msg);
        } else if (!Thread.currentThread().isInterrupted()) {
            this.handler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != 6) {
            switch (i) {
                case 0:
                    Object[] response = (Object[]) msg.obj;
                    if (response == null || response.length < 2) {
                        Log.e(TAG, "SUCCESS_MESSAGE didn't got enough params");
                        return;
                    } else {
                        onSuccess(((Integer) response[0]).intValue(), (byte[]) response[1]);
                        return;
                    }
                case 1:
                    Object[] response2 = (Object[]) msg.obj;
                    if (response2 == null || response2.length < 3) {
                        Log.e(TAG, "FAILURE_MESSAGE didn't got enough params");
                        return;
                    } else {
                        onFailure(((Integer) response2[0]).intValue(), (byte[]) response2[1], (Throwable) response2[2]);
                        return;
                    }
                case 2:
                    onStart();
                    return;
                case 3:
                    onFinish();
                    return;
                case 4:
                    Object[] response3 = (Object[]) msg.obj;
                    if (response3 == null || response3.length < 2) {
                        Log.e(TAG, "PROGRESS_MESSAGE didn't got enough params");
                        return;
                    }
                    try {
                        onProgress(((Integer) response3[0]).intValue(), ((Integer) response3[1]).intValue());
                        return;
                    } catch (Throwable t) {
                        Log.e(TAG, "custom onProgress contains an error", t);
                        return;
                    }
                default:
                    return;
            }
        } else {
            onCancel();
        }
    }

    private Message obtainMessage(int responseMessageId, Object responseMessageData) {
        if (this.handler != null) {
            return Message.obtain(this.handler, responseMessageId, responseMessageData);
        }
        Message msg = Message.obtain();
        if (msg == null) {
            return msg;
        }
        msg.what = responseMessageId;
        msg.obj = responseMessageData;
        return msg;
    }

    private byte[] getResponseData(HttpURLConnection urlConnection) throws IOException {
        if (urlConnection != null) {
            try {
                InputStream instream = urlConnection.getInputStream();
                ByteArrayOutputStream outS = new ByteArrayOutputStream();
                long contentLength = (long) urlConnection.getContentLength();
                try {
                    byte[] buffer = new byte[(contentLength < 0 ? 4096 : (int) contentLength)];
                    int count = 0;
                    while (true) {
                        int read = instream.read(buffer);
                        int l = read;
                        if (read != -1 && !Thread.currentThread().isInterrupted()) {
                            count += l;
                            outS.write(buffer, 0, l);
                            try {
                                sendProgressMessage(count, (int) (contentLength <= 0 ? 1 : contentLength));
                            } catch (Throwable th) {
                                th = th;
                            }
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    AsyncHttpClientX.silentCloseInputStream(instream);
                    throw th;
                }
                try {
                    AsyncHttpClientX.silentCloseInputStream(instream);
                    return outS.toByteArray();
                } catch (OutOfMemoryError e) {
                    System.gc();
                    throw new IOException("File too large to fit into available memory");
                }
            } catch (OutOfMemoryError e2) {
                System.gc();
                throw new IOException("File too large to fit into available memory");
            }
        } else {
            return null;
        }
    }

    public void onProgress(int bytesWritten, int totalSize) {
    }

    public void onStart() {
    }

    public void onFinish() {
    }

    public void onCancel() {
    }
}
