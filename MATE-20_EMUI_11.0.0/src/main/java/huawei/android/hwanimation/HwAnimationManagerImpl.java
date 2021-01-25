package huawei.android.hwanimation;

import android.common.HwAnimationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import com.huawei.hwanimation.HwClipRectAnimation;

public class HwAnimationManagerImpl implements HwAnimationManager {
    private static final String ANIM_STR_CONST = "anim";
    private static final String ENTER_STR_CONST = "_enter";
    private static final String EXIT_STR_CONST = "_exit";
    private static final String HWEXT_STR_CONST = "androidhwext";
    private static final boolean IS_DEBUG = false;
    private static final String SCREEN_ROTATE_STR_CONST = "screen_rotate_";
    private static final String TAG = HwAnimationManagerImpl.class.getSimpleName();
    private static HwAnimationManager sHwAnimationManager = null;

    public static synchronized HwAnimationManager getDefault() {
        HwAnimationManager hwAnimationManager;
        synchronized (HwAnimationManagerImpl.class) {
            if (sHwAnimationManager == null) {
                sHwAnimationManager = new HwAnimationManagerImpl();
            }
            hwAnimationManager = sHwAnimationManager;
        }
        return hwAnimationManager;
    }

    public Animation loadEnterAnimation(Context context, int delta) {
        if (context == null) {
            Log.e(TAG, " loadEnterAnimation context is null!");
            return null;
        }
        Context hwextContext = null;
        try {
            hwextContext = context.createPackageContext(HWEXT_STR_CONST, 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (hwextContext == null) {
            return null;
        }
        Resources resources = hwextContext.getResources();
        int rotateEnterAnimationId = resources.getIdentifier(SCREEN_ROTATE_STR_CONST + delta + ENTER_STR_CONST, ANIM_STR_CONST, HWEXT_STR_CONST);
        if (rotateEnterAnimationId != 0) {
            return AnimationUtils.loadAnimation(hwextContext, rotateEnterAnimationId);
        }
        return null;
    }

    public Animation loadExitAnimation(Context context, int delta) {
        if (context == null) {
            Log.e(TAG, " loadExitAnimation context is null!");
            return null;
        }
        Context hwextContext = null;
        try {
            hwextContext = context.createPackageContext(HWEXT_STR_CONST, 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (hwextContext == null) {
            return null;
        }
        Resources resources = hwextContext.getResources();
        int rotateExitAnimationId = resources.getIdentifier(SCREEN_ROTATE_STR_CONST + delta + EXIT_STR_CONST, ANIM_STR_CONST, HWEXT_STR_CONST);
        if (rotateExitAnimationId != 0) {
            return AnimationUtils.loadAnimation(hwextContext, rotateExitAnimationId);
        }
        return null;
    }

    public void setAnimationBounds(Animation animation, Point offsets) {
        if (animation instanceof HwClipRectAnimation) {
            ((HwClipRectAnimation) animation).setCropStartPoint(offsets);
        } else if (animation instanceof AnimationSet) {
            for (Animation anim : ((AnimationSet) animation).getAnimations()) {
                if (anim instanceof HwClipRectAnimation) {
                    ((HwClipRectAnimation) anim).setCropStartPoint(offsets);
                }
            }
        } else {
            Log.e(TAG, "wrong animation type");
        }
    }
}
