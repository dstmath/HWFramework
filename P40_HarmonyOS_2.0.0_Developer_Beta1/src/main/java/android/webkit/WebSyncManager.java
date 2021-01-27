package android.webkit;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.Handler;

@Deprecated
abstract class WebSyncManager implements Runnable {
    protected static final String LOGTAG = "websync";
    protected WebViewDatabase mDataBase;
    @UnsupportedAppUsage
    protected Handler mHandler;

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public abstract void syncFromRamToFlash();

    protected WebSyncManager(Context context, String name) {
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("doesn't implement Cloneable");
    }

    @Override // java.lang.Runnable
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
