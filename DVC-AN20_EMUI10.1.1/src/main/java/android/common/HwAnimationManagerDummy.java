package android.common;

import android.content.Context;
import android.graphics.Point;
import android.view.animation.Animation;

public class HwAnimationManagerDummy implements HwAnimationManager {
    private static HwAnimationManager mHwAnimationManager = null;

    public static HwAnimationManager getDefault() {
        if (mHwAnimationManager == null) {
            mHwAnimationManager = new HwAnimationManagerDummy();
        }
        return mHwAnimationManager;
    }

    @Override // android.common.HwAnimationManager
    public Animation loadEnterAnimation(Context context, int delta) {
        return null;
    }

    @Override // android.common.HwAnimationManager
    public Animation loadExitAnimation(Context context, int delta) {
        return null;
    }

    @Override // android.common.HwAnimationManager
    public void setAnimationBounds(Animation animation, Point offsets) {
    }
}
