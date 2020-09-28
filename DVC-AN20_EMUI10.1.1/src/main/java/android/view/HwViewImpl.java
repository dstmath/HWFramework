package android.view;

import android.content.Context;
import android.os.SystemClock;
import android.rms.iaware.AwareAppScheduleManager;
import com.huawei.pgmng.log.LogPower;

public class HwViewImpl implements IHwView {
    private static final String LAUNCHER_APP = "com.huawei.android.launcher";
    private static final long SCHEDULE_FRAME_CALL_INTERVAL = 3000;
    private static final String SYSTEM_UI_APP = "com.android.systemui";
    private static final String TAG = "HwViewImpl";
    private static HwViewImpl mInstance = null;
    private long mLastScheduleFrameTime;
    private boolean mViewClickSpeedUp;

    public static synchronized HwViewImpl getDefault() {
        HwViewImpl hwViewImpl;
        synchronized (HwViewImpl.class) {
            if (mInstance == null) {
                mInstance = new HwViewImpl();
            }
            hwViewImpl = mInstance;
        }
        return hwViewImpl;
    }

    private HwViewImpl() {
        this.mViewClickSpeedUp = false;
        this.mLastScheduleFrameTime = 0;
        this.mViewClickSpeedUp = AwareAppScheduleManager.getInstance().getClickViewSpeedUpStatus();
    }

    public void onClick(View view, Context context) {
        unFreezeApp(view, context);
    }

    private void unFreezeApp(View view, Context context) {
        if (view != null && view.getRootView() != null && view.getRootView().getContext() != null && context != null) {
            String rootViewPkg = view.getRootView().getContext().getPackageName();
            if (SYSTEM_UI_APP.equals(rootViewPkg)) {
                String viewPkg = context.getPackageName();
                if (!SYSTEM_UI_APP.equals(viewPkg)) {
                    LogPower.push(148, "onClick", viewPkg);
                }
            } else if (LAUNCHER_APP.equals(rootViewPkg)) {
                String viewPkg2 = context.getPackageName();
                if (!LAUNCHER_APP.equals(viewPkg2)) {
                    LogPower.push(148, "onClick", viewPkg2);
                }
            }
        }
    }

    public void scheduleFrameNow(boolean prePressed, View view) {
        if (this.mViewClickSpeedUp && prePressed) {
            long now = SystemClock.elapsedRealtime();
            if (now - this.mLastScheduleFrameTime < SCHEDULE_FRAME_CALL_INTERVAL) {
                this.mLastScheduleFrameTime = now;
                return;
            }
            ChoreographerEx.scheduleFrameNow(view);
            this.mLastScheduleFrameTime = now;
        }
    }
}
