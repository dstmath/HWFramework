package com.android.server.policy;

import android.os.IBinder;
import android.os.SystemClock;
import android.view.WindowManagerInternal.AppTransitionListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import com.android.server.LocalServices;
import com.android.server.statusbar.StatusBarManagerInternal;

public class StatusBarController extends BarController {
    private static final long TRANSITION_DURATION = 120;
    private final AppTransitionListener mAppTransitionListener = new AppTransitionListener() {
        public void onAppTransitionPendingLocked() {
            StatusBarController.this.mHandler.post(new Runnable() {
                public void run() {
                    StatusBarManagerInternal statusbar = StatusBarController.this.getStatusBarInternal();
                    if (statusbar != null) {
                        statusbar.appTransitionPending();
                    }
                }
            });
        }

        public int onAppTransitionStartingLocked(int transit, IBinder openToken, IBinder closeToken, final Animation openAnimation, final Animation closeAnimation) {
            StatusBarController.this.mHandler.post(new Runnable() {
                public void run() {
                    StatusBarManagerInternal statusbar = StatusBarController.this.getStatusBarInternal();
                    if (statusbar != null) {
                        long startTime = StatusBarController.calculateStatusBarTransitionStartTime(openAnimation, closeAnimation);
                        long duration = (closeAnimation == null && openAnimation == null) ? 0 : StatusBarController.TRANSITION_DURATION;
                        statusbar.appTransitionStarting(startTime, duration);
                    }
                }
            });
            return 0;
        }

        public void onAppTransitionCancelledLocked(int transit) {
            StatusBarController.this.mHandler.post(new Runnable() {
                public void run() {
                    StatusBarManagerInternal statusbar = StatusBarController.this.getStatusBarInternal();
                    if (statusbar != null) {
                        statusbar.appTransitionCancelled();
                    }
                }
            });
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
            StatusBarController.this.mHandler.post(new Runnable() {
                public void run() {
                    StatusBarManagerInternal statusbar = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
                    if (statusbar != null) {
                        statusbar.appTransitionFinished();
                    }
                }
            });
        }
    };

    public StatusBarController() {
        super("StatusBar", 67108864, 268435456, 1073741824, 1, 67108864, 8);
    }

    protected boolean skipAnimation() {
        return this.mWin.getAttrs().height == -1;
    }

    public AppTransitionListener getAppTransitionListener() {
        return this.mAppTransitionListener;
    }

    private static long calculateStatusBarTransitionStartTime(Animation openAnimation, Animation closeAnimation) {
        if (openAnimation == null || closeAnimation == null) {
            return SystemClock.uptimeMillis();
        }
        TranslateAnimation openTranslateAnimation = findTranslateAnimation(openAnimation);
        TranslateAnimation closeTranslateAnimation = findTranslateAnimation(closeAnimation);
        if (openTranslateAnimation != null) {
            return ((SystemClock.uptimeMillis() + openTranslateAnimation.getStartOffset()) + ((long) (((float) openTranslateAnimation.getDuration()) * findAlmostThereFraction(openTranslateAnimation.getInterpolator())))) - TRANSITION_DURATION;
        } else if (closeTranslateAnimation != null) {
            return SystemClock.uptimeMillis();
        } else {
            return SystemClock.uptimeMillis();
        }
    }

    private static TranslateAnimation findTranslateAnimation(Animation animation) {
        if (animation instanceof TranslateAnimation) {
            return (TranslateAnimation) animation;
        }
        if (animation instanceof AnimationSet) {
            AnimationSet set = (AnimationSet) animation;
            for (int i = 0; i < set.getAnimations().size(); i++) {
                Animation a = (Animation) set.getAnimations().get(i);
                if (a instanceof TranslateAnimation) {
                    return (TranslateAnimation) a;
                }
            }
        }
        return null;
    }

    private static float findAlmostThereFraction(Interpolator interpolator) {
        float val = 0.5f;
        for (float adj = 0.25f; adj >= 0.01f; adj /= 2.0f) {
            if (interpolator.getInterpolation(val) < 0.99f) {
                val += adj;
            } else {
                val -= adj;
            }
        }
        return val;
    }
}
