package android.common;

import android.content.Context;
import android.view.animation.Animation;

public class HwAnimationManagerDummy implements HwAnimationManager {
    private static HwAnimationManager mHwAnimationManager = null;

    public static HwAnimationManager getDefault() {
        if (mHwAnimationManager == null) {
            mHwAnimationManager = new HwAnimationManagerDummy();
        }
        return mHwAnimationManager;
    }

    public Animation loadEnterAnimation(Context context, int delta) {
        return null;
    }

    public Animation loadExitAnimation(Context context, int delta) {
        return null;
    }
}
