package com.leisen.wallet.sdk.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.leisen.wallet.sdk.util.LogUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.ByteArrayBuffer;

public abstract class AsyncHttpResponseHandler implements ResponseHandlerInterface {
    protected static final int BUFFER_SIZE = 4096;
    protected static final int CANCEL_MESSAGE = 6;
    public static final String DEFAULT_CAHRSET = "UTF_8";
    protected static final int FALIURE_MESSAGE = 1;
    protected static final int FINISH_MESSAGE = 3;
    protected static final int PROGRESS_MESSAGE = 4;
    protected static final int RETRY_MESSAGE = 5;
    protected static final int START_MESSAGE = 2;
    protected static final int SUCCESS_MESSAGE = 0;
    private Handler handler;
    private Header[] requestHeaders = null;
    private URI requestURI = null;
    private String responseCharset = DEFAULT_CAHRSET;
    private boolean useSynchronousMode;

    private static class ResponderHandler extends Handler {
        private final AsyncHttpResponseHandler mResponder;

        public ResponderHandler(AsyncHttpResponseHandler mResponder) {
            this.mResponder = mResponder;
        }

        public void handleMessage(Message msg) {
            this.mResponder.handleMessage(msg);
        }
    }

    public abstract void onFailure(int i, Header[] headerArr, byte[] bArr, Throwable th);

    public abstract void onSuccess(int i, Header[] headerArr, byte[] bArr);

    public AsyncHttpResponseHandler() {
        setUseSynchronousMode(true);
    }

    public URI getRequestURI() {
        return this.requestURI;
    }

    public void setRequestURI(URI requestURI) {
        this.requestURI = requestURI;
    }

    public Header[] getRequestHeaders() {
        return this.requestHeaders;
    }

    public void setRequestHeaders(Header[] requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public void setUseSynchronousMode(boolean useSynchronousMode) {
        if (!useSynchronousMode && Looper.myLooper() == null) {
            useSynchronousMode = true;
        }
        if (!useSynchronousMode && this.handler == null) {
            this.handler = new ResponderHandler(this);
        } else if (useSynchronousMode && this.handler != null) {
            this.handler = null;
        }
        this.useSynchronousMode = useSynchronousMode;
    }

    public boolean getUseSynchronousMode() {
        return this.useSynchronousMode;
    }

    public void setCharset(String charset) {
        this.responseCharset = charset;
    }

    public String getCharset() {
        return this.responseCharset != null ? this.responseCharset : DEFAULT_CAHRSET;
    }

    public void sendResponseMessage(HttpResponse httpResponse) throws IOException {
        if (!Thread.currentThread().isInterrupted()) {
            StatusLine status = httpResponse.getStatusLine();
            byte[] responseBody = getResponseData(httpResponse.getEntity());
            if (!Thread.currentThread().isInterrupted()) {
                LogUtil.e("AysncHttpClient", "==>" + status.getStatusCode() + "==" + new String(responseBody));
                if (status.getStatusCode() <= 300) {
                    sendSuccessMessage(status.getStatusCode(), httpResponse.getAllHeaders(), responseBody);
                } else {
                    sendFailureMessage(status.getStatusCode(), httpResponse.getAllHeaders(), responseBody, new HttpResponseException(status.getStatusCode(), status.getReasonPhrase()));
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

    public void sendSuccessMessage(int statusCode, Header[] headers, byte[] responseBody) {
        sendMessage(obtainMessage(0, new Object[]{Integer.valueOf(statusCode), headers, responseBody}));
    }

    public void sendFailureMessage(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        sendMessage(obtainMessage(1, new Object[]{Integer.valueOf(statusCode), headers, responseBody, error}));
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

    protected void handleMessage(Message message) {
        Object[] response;
        switch (message.what) {
            case 0:
                response = message.obj;
                if (response != null && response.length >= 3) {
                    onSuccess(((Integer) response[0]).intValue(), (Header[]) response[1], (byte[]) response[2]);
                    return;
                } else {
                    Log.e("AsyncHttpResponseHandler", "SUCCESS_MESSAGE didn't got enough params");
                    return;
                }
            case 1:
                response = (Object[]) message.obj;
                if (response != null && response.length >= 4) {
                    onFailure(((Integer) response[0]).intValue(), (Header[]) response[1], (byte[]) response[2], (Throwable) response[3]);
                    return;
                } else {
                    Log.e("AsyncHttpResponseHandler", "FAILURE_MESSAGE didn't got enough params");
                    return;
                }
            case 2:
                onStart();
                return;
            case 3:
                onFinish();
                return;
            case 4:
                response = (Object[]) message.obj;
                if (response != null && response.length >= 2) {
                    try {
                        onProgress(((Integer) response[0]).intValue(), ((Integer) response[1]).intValue());
                        return;
                    } catch (Throwable t) {
                        Log.e("AsyncHttpResponseHandler", "custom onProgress contains an error", t);
                        return;
                    }
                }
                Log.e("AsyncHttpResponseHandler", "PROGRESS_MESSAGE didn't got enough params");
                return;
            case 6:
                onCancel();
                return;
            default:
                return;
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

    /* JADX WARNING: Missing block: B:25:?, code:
            com.leisen.wallet.sdk.http.AsyncHttpClient.silentCloseInputStream(r7);
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            return r1.toByteArray();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] getResponseData(HttpEntity entity) throws IOException {
        if (entity == null) {
            return null;
        }
        InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        long contentLength = entity.getContentLength();
        if ((contentLength <= 2147483647L ? 1 : null) == null) {
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
        }
        try {
            ByteArrayBuffer buffer = new ByteArrayBuffer(((contentLength > 0 ? 1 : (contentLength == 0 ? 0 : -1)) >= 0 ? 1 : null) == null ? 4096 : (int) contentLength);
            byte[] tmp = new byte[4096];
            int count = 0;
            while (true) {
                int l = instream.read(tmp);
                if (l != -1) {
                    if (!Thread.currentThread().isInterrupted()) {
                        count += l;
                        buffer.append(tmp, 0, l);
                        sendProgressMessage(count, (int) (((contentLength > 0 ? 1 : (contentLength == 0 ? 0 : -1)) > 0 ? 1 : null) == null ? 1 : contentLength));
                    }
                }
                break;
            }
        } catch (OutOfMemoryError e) {
            System.gc();
            throw new IOException("File too large to fit into available memory");
        } catch (Throwable th) {
            AsyncHttpClient.silentCloseInputStream(instream);
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
