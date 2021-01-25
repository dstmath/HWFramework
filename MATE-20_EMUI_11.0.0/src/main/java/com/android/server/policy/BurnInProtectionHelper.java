package com.android.server.policy;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerInternal;
import android.os.SystemClock;
import android.view.Display;
import android.view.animation.LinearInterpolator;
import com.android.server.LocalServices;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class BurnInProtectionHelper implements DisplayManager.DisplayListener, Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {
    private static final String ACTION_BURN_IN_PROTECTION = "android.internal.policy.action.BURN_IN_PROTECTION";
    private static final long BURNIN_PROTECTION_FIRST_WAKEUP_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    private static final long BURNIN_PROTECTION_MINIMAL_INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long BURNIN_PROTECTION_SUBSEQUENT_WAKEUP_INTERVAL_MS = TimeUnit.MINUTES.toMillis(2);
    public static final int BURN_IN_MAX_RADIUS_DEFAULT = -1;
    private static final int BURN_IN_SHIFT_STEP = 2;
    private static final long CENTERING_ANIMATION_DURATION_MS = 100;
    private static final boolean DEBUG = false;
    private static final String TAG = "BurnInProtection";
    private final AlarmManager mAlarmManager;
    private int mAppliedBurnInXOffset = 0;
    private int mAppliedBurnInYOffset = 0;
    private boolean mBurnInProtectionActive;
    private final PendingIntent mBurnInProtectionIntent;
    private BroadcastReceiver mBurnInProtectionReceiver = new BroadcastReceiver() {
        /* class com.android.server.policy.BurnInProtectionHelper.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            BurnInProtectionHelper.this.updateBurnInProtection();
        }
    };
    private final int mBurnInRadiusMaxSquared;
    private final ValueAnimator mCenteringAnimator;
    private final Display mDisplay;
    private final DisplayManagerInternal mDisplayManagerInternal;
    private boolean mFirstUpdate;
    private int mLastBurnInXOffset = 0;
    private int mLastBurnInYOffset = 0;
    private final int mMaxHorizontalBurnInOffset;
    private final int mMaxVerticalBurnInOffset;
    private final int mMinHorizontalBurnInOffset;
    private final int mMinVerticalBurnInOffset;
    private int mXOffsetDirection = 1;
    private int mYOffsetDirection = 1;

    public BurnInProtectionHelper(Context context, int minHorizontalOffset, int maxHorizontalOffset, int minVerticalOffset, int maxVerticalOffset, int maxOffsetRadius) {
        this.mMinHorizontalBurnInOffset = minHorizontalOffset;
        this.mMaxHorizontalBurnInOffset = maxHorizontalOffset;
        this.mMinVerticalBurnInOffset = minVerticalOffset;
        this.mMaxVerticalBurnInOffset = maxVerticalOffset;
        if (maxOffsetRadius != -1) {
            this.mBurnInRadiusMaxSquared = maxOffsetRadius * maxOffsetRadius;
        } else {
            this.mBurnInRadiusMaxSquared = -1;
        }
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        context.registerReceiver(this.mBurnInProtectionReceiver, new IntentFilter(ACTION_BURN_IN_PROTECTION));
        Intent intent = new Intent(ACTION_BURN_IN_PROTECTION);
        intent.setPackage(context.getPackageName());
        intent.setFlags(1073741824);
        this.mBurnInProtectionIntent = PendingIntent.getBroadcast(context, 0, intent, DumpState.DUMP_HWFEATURES);
        DisplayManager displayManager = (DisplayManager) context.getSystemService("display");
        this.mDisplay = displayManager.getDisplay(0);
        displayManager.registerDisplayListener(this, null);
        this.mCenteringAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        this.mCenteringAnimator.setDuration(CENTERING_ANIMATION_DURATION_MS);
        this.mCenteringAnimator.setInterpolator(new LinearInterpolator());
        this.mCenteringAnimator.addListener(this);
        this.mCenteringAnimator.addUpdateListener(this);
    }

    public void startBurnInProtection() {
        if (!this.mBurnInProtectionActive) {
            this.mBurnInProtectionActive = true;
            this.mFirstUpdate = true;
            this.mCenteringAnimator.cancel();
            updateBurnInProtection();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBurnInProtection() {
        long interval;
        if (this.mBurnInProtectionActive) {
            if (this.mFirstUpdate) {
                interval = BURNIN_PROTECTION_FIRST_WAKEUP_INTERVAL_MS;
            } else {
                interval = BURNIN_PROTECTION_SUBSEQUENT_WAKEUP_INTERVAL_MS;
            }
            if (this.mFirstUpdate) {
                this.mFirstUpdate = false;
            } else {
                adjustOffsets();
                this.mAppliedBurnInXOffset = this.mLastBurnInXOffset;
                this.mAppliedBurnInYOffset = this.mLastBurnInYOffset;
                this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplay.getDisplayId(), this.mLastBurnInXOffset, this.mLastBurnInYOffset);
            }
            long nowWall = System.currentTimeMillis();
            long nowElapsed = SystemClock.elapsedRealtime();
            long nextWall = BURNIN_PROTECTION_MINIMAL_INTERVAL_MS + nowWall;
            this.mAlarmManager.setExact(3, (((nextWall - (nextWall % interval)) + interval) - nowWall) + nowElapsed, this.mBurnInProtectionIntent);
            return;
        }
        this.mAlarmManager.cancel(this.mBurnInProtectionIntent);
        this.mCenteringAnimator.start();
    }

    public void cancelBurnInProtection() {
        if (this.mBurnInProtectionActive) {
            this.mBurnInProtectionActive = false;
            updateBurnInProtection();
        }
    }

    private void adjustOffsets() {
        int xChange;
        int i;
        int i2;
        do {
            int xChange2 = this.mXOffsetDirection * 2;
            this.mLastBurnInXOffset += xChange2;
            int i3 = this.mLastBurnInXOffset;
            if (i3 > this.mMaxHorizontalBurnInOffset || i3 < this.mMinHorizontalBurnInOffset) {
                this.mLastBurnInXOffset -= xChange2;
                this.mXOffsetDirection *= -1;
                int yChange = this.mYOffsetDirection * 2;
                this.mLastBurnInYOffset += yChange;
                int i4 = this.mLastBurnInYOffset;
                if (i4 > this.mMaxVerticalBurnInOffset || i4 < this.mMinVerticalBurnInOffset) {
                    this.mLastBurnInYOffset -= yChange;
                    this.mYOffsetDirection *= -1;
                }
            }
            xChange = this.mBurnInRadiusMaxSquared;
            if (xChange != -1) {
                i = this.mLastBurnInXOffset;
                i2 = this.mLastBurnInYOffset;
            } else {
                return;
            }
        } while ((i * i) + (i2 * i2) > xChange);
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        String prefix2 = prefix + "  ";
        pw.println(prefix2 + "mBurnInProtectionActive=" + this.mBurnInProtectionActive);
        pw.println(prefix2 + "mHorizontalBurnInOffsetsBounds=(" + this.mMinHorizontalBurnInOffset + ", " + this.mMaxHorizontalBurnInOffset + ")");
        pw.println(prefix2 + "mVerticalBurnInOffsetsBounds=(" + this.mMinVerticalBurnInOffset + ", " + this.mMaxVerticalBurnInOffset + ")");
        StringBuilder sb = new StringBuilder();
        sb.append(prefix2);
        sb.append("mBurnInRadiusMaxSquared=");
        sb.append(this.mBurnInRadiusMaxSquared);
        pw.println(sb.toString());
        pw.println(prefix2 + "mLastBurnInOffset=(" + this.mLastBurnInXOffset + ", " + this.mLastBurnInYOffset + ")");
        pw.println(prefix2 + "mOfsetChangeDirections=(" + this.mXOffsetDirection + ", " + this.mYOffsetDirection + ")");
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayAdded(int i) {
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayRemoved(int i) {
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayChanged(int displayId) {
        if (displayId != this.mDisplay.getDisplayId()) {
            return;
        }
        if (this.mDisplay.getState() == 3 || this.mDisplay.getState() == 4 || this.mDisplay.getState() == 6) {
            startBurnInProtection();
        } else {
            cancelBurnInProtection();
        }
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animator) {
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animator) {
        if (animator == this.mCenteringAnimator && !this.mBurnInProtectionActive) {
            this.mAppliedBurnInXOffset = 0;
            this.mAppliedBurnInYOffset = 0;
            this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplay.getDisplayId(), 0, 0);
        }
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationCancel(Animator animator) {
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationRepeat(Animator animator) {
    }

    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (!this.mBurnInProtectionActive) {
            float value = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplay.getDisplayId(), (int) (((float) this.mAppliedBurnInXOffset) * value), (int) (((float) this.mAppliedBurnInYOffset) * value));
        }
    }
}
