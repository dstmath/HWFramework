package huawei.android.hwanimation;

import android.common.HwAnimationManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class HwAnimationManagerImpl implements HwAnimationManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "AnimationUtils";
    private static HwAnimationManager mHwAnimationManager = null;

    public static synchronized HwAnimationManager getDefault() {
        HwAnimationManager hwAnimationManager;
        synchronized (HwAnimationManagerImpl.class) {
            if (mHwAnimationManager == null) {
                mHwAnimationManager = new HwAnimationManagerImpl();
            }
            hwAnimationManager = mHwAnimationManager;
        }
        return hwAnimationManager;
    }

    public Animation loadEnterAnimation(Context context, int delta) {
        Context hwextContext = null;
        try {
            hwextContext = context.createPackageContext("androidhwext", 0);
        } catch (NameNotFoundException e) {
        }
        if (hwextContext == null) {
            return null;
        }
        int rotateEnterAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_enter", "anim", "androidhwext");
        if (rotateEnterAnimationId != 0) {
            return AnimationUtils.loadAnimation(hwextContext, rotateEnterAnimationId);
        }
        return null;
    }

    public Animation loadExitAnimation(Context context, int delta) {
        Context hwextContext = null;
        try {
            hwextContext = context.createPackageContext("androidhwext", 0);
        } catch (NameNotFoundException e) {
        }
        if (hwextContext == null) {
            return null;
        }
        int rotateExitAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_exit", "anim", "androidhwext");
        if (rotateExitAnimationId != 0) {
            return AnimationUtils.loadAnimation(hwextContext, rotateExitAnimationId);
        }
        return null;
    }
}
