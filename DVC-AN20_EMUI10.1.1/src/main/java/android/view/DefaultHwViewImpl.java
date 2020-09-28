package android.view;

import android.content.Context;
import com.huawei.annotation.HwSystemApi;

public class DefaultHwViewImpl implements IHwView {
    private static DefaultHwViewImpl mInstance = null;

    public static synchronized DefaultHwViewImpl getDefault() {
        DefaultHwViewImpl defaultHwViewImpl;
        synchronized (DefaultHwViewImpl.class) {
            if (mInstance == null) {
                mInstance = new DefaultHwViewImpl();
            }
            defaultHwViewImpl = mInstance;
        }
        return defaultHwViewImpl;
    }

    @Override // android.view.IHwView
    @HwSystemApi
    public void onClick(View view, Context context) {
    }

    @Override // android.view.IHwView
    public void scheduleFrameNow(boolean prePressed, View view) {
    }
}
