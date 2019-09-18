package android.common;

import android.content.Context;
import android.view.animation.Animation;

public interface HwAnimationManager {
    Animation loadEnterAnimation(Context context, int i);

    Animation loadExitAnimation(Context context, int i);
}
