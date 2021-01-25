package android.common;

import android.content.Context;
import android.graphics.Point;
import android.view.animation.Animation;

public class HwAnimationManagerDummy implements HwAnimationManager {
    private static HwAnimationManager sHwAnimationManager = null;

    public static HwAnimationManager getDefault() {
        if (sHwAnimationManager == null) {
            sHwAnimationManager = new HwAnimationManagerDummy();
        }
        return sHwAnimationManager;
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
