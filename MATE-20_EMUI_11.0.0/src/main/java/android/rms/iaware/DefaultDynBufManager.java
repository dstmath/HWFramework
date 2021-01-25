package android.rms.iaware;

import android.content.Context;
import android.rms.iaware.HwDynBufManager;
import android.view.InputEvent;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultDynBufManager implements HwDynBufManager.IDynBufManager {
    private static final Object SLOCK = new Object();
    private static DefaultDynBufManager sInstance;

    public static DefaultDynBufManager getDefault() {
        DefaultDynBufManager defaultDynBufManager;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new DefaultDynBufManager();
            }
            defaultDynBufManager = sInstance;
        }
        return defaultDynBufManager;
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void initFrameInterval(long frameIntervalNanos) {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void onVsync() {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public boolean canAddVsync() {
        return false;
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void notifyInputEvent(InputEvent event) {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public int getTargetBufCount() {
        return 0;
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void updateSurfaceBufCount(int count) {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void updateMultiViews() {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void init(Context context) {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void beginFling(boolean flinging, int hash) {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void endFling(boolean flinging, int hash) {
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public long updateSplineTime(boolean flinging, long currentTime, int hash) {
        return currentTime;
    }

    @Override // android.rms.iaware.HwDynBufManager.IDynBufManager
    public void updateSurfaceTexture() {
    }
}
