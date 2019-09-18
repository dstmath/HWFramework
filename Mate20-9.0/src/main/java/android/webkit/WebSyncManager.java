package android.webkit;

import android.content.Context;
import android.os.Handler;

@Deprecated
abstract class WebSyncManager implements Runnable {
    protected static final String LOGTAG = "websync";
    protected WebViewDatabase mDataBase;
    protected Handler mHandler;

    /* access modifiers changed from: package-private */
    public abstract void syncFromRamToFlash();

    protected WebSyncManager(Context context, String name) {
    }

    /* access modifiers changed from: protected */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("doesn't implement Cloneable");
    }

    public void run() {
    }

    public void sync() {
    }

    public void resetSync() {
    }

    public void startSync() {
    }

    public void stopSync() {
    }

    /* access modifiers changed from: protected */
    public void onSyncInit() {
    }
}
