package android.view;

import android.app.Activity;
import android.content.Context;
import android.rms.iaware.AppSceneRecogManager;

public class HwAppSceneImpl implements IHwAppSceneImpl {
    private static final HwAppSceneImpl INSTANCE = new HwAppSceneImpl();

    private HwAppSceneImpl() {
    }

    public static HwAppSceneImpl getDefault() {
        return INSTANCE;
    }

    public void changeActivityState(int flag, Activity activity) {
        switch (flag) {
            case 0:
                AppSceneRecogManager.getInstance().resumeActivity(activity);
                return;
            case 1:
                AppSceneRecogManager.getInstance().pauseActivity(activity);
                return;
            default:
                return;
        }
    }

    public void handleWindowFocusChanged(Context context) {
        AppSceneRecogManager.getInstance().windowFocusChanged(context);
    }
}
