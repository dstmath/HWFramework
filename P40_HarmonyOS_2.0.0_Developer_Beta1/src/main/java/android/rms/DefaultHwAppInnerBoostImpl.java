package android.rms;

import android.view.InputEvent;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwAppInnerBoostImpl implements IHwAppInnerBoost {
    private static final Object SLOCK = new Object();
    private static DefaultHwAppInnerBoostImpl sInstance;

    public static IHwAppInnerBoost getDefault() {
        DefaultHwAppInnerBoostImpl defaultHwAppInnerBoostImpl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new DefaultHwAppInnerBoostImpl();
            }
            defaultHwAppInnerBoostImpl = sInstance;
        }
        return defaultHwAppInnerBoostImpl;
    }

    @Override // android.rms.IHwAppInnerBoost
    public void onJitter(long skippedFrames) {
    }

    @Override // android.rms.IHwAppInnerBoost
    public void initialize(String packageName) {
    }

    @Override // android.rms.IHwAppInnerBoost
    public void onInputEvent(InputEvent event) {
    }

    @Override // android.rms.IHwAppInnerBoost
    public void onScrollState(boolean isFinished) {
    }

    @Override // android.rms.IHwAppInnerBoost
    public void onTraversal() {
    }
}
