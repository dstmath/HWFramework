package android.cover;

import android.content.Context;
import android.os.IBinder;
import android.view.View;

public class DefaultCoverManager implements IHwCoverManager {
    private static DefaultCoverManager mInstance = null;

    public static synchronized DefaultCoverManager getDefault() {
        DefaultCoverManager defaultCoverManager;
        synchronized (DefaultCoverManager.class) {
            if (mInstance == null) {
                mInstance = new DefaultCoverManager();
            }
            defaultCoverManager = mInstance;
        }
        return defaultCoverManager;
    }

    @Override // android.cover.IHwCoverManager
    public boolean isCoverOpen() {
        return true;
    }

    @Override // android.cover.IHwCoverManager
    public boolean setCoverForbiddened(boolean forbiddened) {
        return false;
    }

    @Override // android.cover.IHwCoverManager
    public int getHallState(int hallType) {
        return -1;
    }

    @Override // android.cover.IHwCoverManager
    public boolean registerHallCallback(String receiverName, int hallType, IHallCallback callback) {
        return false;
    }

    @Override // android.cover.IHwCoverManager
    public boolean unRegisterHallCallback(String receiverName, int hallType) {
        return false;
    }

    @Override // android.cover.IHwCoverManager
    public boolean unRegisterHallCallbackEx(int hallType, IHallCallback callback) {
        return false;
    }

    public void addCoverItemView(View view, boolean isNeed) {
    }

    public void addCoverItemView(View view, boolean isNeed, int activTime) {
    }

    public void addCoverItemView(View view, boolean isNeed, boolean mDisablePower) {
    }

    public void addCoverItemView(View view, boolean isNeed, boolean mDisablePower, int activTime) {
    }

    public void removeCoverItemView(View view) {
    }

    public void setCoverViewBinder(IBinder binder, Context context) {
    }
}
