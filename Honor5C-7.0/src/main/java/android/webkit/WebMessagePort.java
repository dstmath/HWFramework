package android.webkit;

import android.os.Handler;

public abstract class WebMessagePort {

    public static abstract class WebMessageCallback {
        public void onMessage(WebMessagePort port, WebMessage message) {
        }
    }

    public abstract void close();

    public abstract void postMessage(WebMessage webMessage);

    public abstract void setWebMessageCallback(WebMessageCallback webMessageCallback);

    public abstract void setWebMessageCallback(WebMessageCallback webMessageCallback, Handler handler);
}
