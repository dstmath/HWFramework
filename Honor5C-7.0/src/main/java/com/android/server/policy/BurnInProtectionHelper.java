package com.android.server.policy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.DisplayManagerInternal;
import android.os.SystemClock;
import android.view.Display;
import android.view.animation.LinearInterpolator;
import com.android.server.LocalServices;
import java.io.PrintWriter;

public class BurnInProtectionHelper implements DisplayListener, AnimatorListener, AnimatorUpdateListener {
    private static final String ACTION_BURN_IN_PROTECTION = "android.internal.policy.action.BURN_IN_PROTECTION";
    private static final long BURNIN_PROTECTION_MINIMAL_INTERVAL_MS = 0;
    private static final long BURNIN_PROTECTION_WAKEUP_INTERVAL_MS = 0;
    public static final int BURN_IN_MAX_RADIUS_DEFAULT = -1;
    private static final int BURN_IN_SHIFT_STEP = 2;
    private static final long CENTERING_ANIMATION_DURATION_MS = 100;
    private static final boolean DEBUG = false;
    private static final String TAG = "BurnInProtection";
    private final AlarmManager mAlarmManager;
    private int mAppliedBurnInXOffset;
    private int mAppliedBurnInYOffset;
    private boolean mBurnInProtectionActive;
    private final PendingIntent mBurnInProtectionIntent;
    private BroadcastReceiver mBurnInProtectionReceiver;
    private final int mBurnInRadiusMaxSquared;
    private final ValueAnimator mCenteringAnimator;
    private final Display mDisplay;
    private final DisplayManagerInternal mDisplayManagerInternal;
    private boolean mFirstUpdate;
    private int mLastBurnInXOffset;
    private int mLastBurnInYOffset;
    private final int mMaxHorizontalBurnInOffset;
    private final int mMaxVerticalBurnInOffset;
    private final int mMinHorizontalBurnInOffset;
    private final int mMinVerticalBurnInOffset;
    private int mXOffsetDirection;
    private int mYOffsetDirection;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.BurnInProtectionHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.BurnInProtectionHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.BurnInProtectionHelper.<clinit>():void");
    }

    public BurnInProtectionHelper(Context context, int minHorizontalOffset, int maxHorizontalOffset, int minVerticalOffset, int maxVerticalOffset, int maxOffsetRadius) {
        this.mLastBurnInXOffset = 0;
        this.mXOffsetDirection = 1;
        this.mLastBurnInYOffset = 0;
        this.mYOffsetDirection = 1;
        this.mAppliedBurnInXOffset = 0;
        this.mAppliedBurnInYOffset = 0;
        this.mBurnInProtectionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                BurnInProtectionHelper.this.updateBurnInProtection();
            }
        };
        this.mMinHorizontalBurnInOffset = minHorizontalOffset;
        this.mMaxHorizontalBurnInOffset = maxHorizontalOffset;
        this.mMinVerticalBurnInOffset = minVerticalOffset;
        this.mMaxVerticalBurnInOffset = maxVerticalOffset;
        if (maxOffsetRadius != BURN_IN_MAX_RADIUS_DEFAULT) {
            this.mBurnInRadiusMaxSquared = maxOffsetRadius * maxOffsetRadius;
        } else {
            this.mBurnInRadiusMaxSquared = BURN_IN_MAX_RADIUS_DEFAULT;
        }
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        context.registerReceiver(this.mBurnInProtectionReceiver, new IntentFilter(ACTION_BURN_IN_PROTECTION));
        Intent intent = new Intent(ACTION_BURN_IN_PROTECTION);
        intent.setPackage(context.getPackageName());
        intent.setFlags(1073741824);
        this.mBurnInProtectionIntent = PendingIntent.getBroadcast(context, 0, intent, 134217728);
        DisplayManager displayManager = (DisplayManager) context.getSystemService("display");
        this.mDisplay = displayManager.getDisplay(0);
        displayManager.registerDisplayListener(this, null);
        this.mCenteringAnimator = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
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

    private void updateBurnInProtection() {
        if (this.mBurnInProtectionActive) {
            if (this.mFirstUpdate) {
                this.mFirstUpdate = DEBUG;
            } else {
                adjustOffsets();
                this.mAppliedBurnInXOffset = this.mLastBurnInXOffset;
                this.mAppliedBurnInYOffset = this.mLastBurnInYOffset;
                this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplay.getDisplayId(), this.mLastBurnInXOffset, this.mLastBurnInYOffset);
            }
            long nowWall = System.currentTimeMillis();
            long nextWall = nowWall + BURNIN_PROTECTION_MINIMAL_INTERVAL_MS;
            this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + (((nextWall - (nextWall % BURNIN_PROTECTION_WAKEUP_INTERVAL_MS)) + BURNIN_PROTECTION_WAKEUP_INTERVAL_MS) - nowWall), this.mBurnInProtectionIntent);
            return;
        }
        this.mAlarmManager.cancel(this.mBurnInProtectionIntent);
        this.mCenteringAnimator.start();
    }

    public void cancelBurnInProtection() {
        if (this.mBurnInProtectionActive) {
            this.mBurnInProtectionActive = DEBUG;
            updateBurnInProtection();
        }
    }

    private void adjustOffsets() {
        do {
            int xChange = this.mXOffsetDirection * BURN_IN_SHIFT_STEP;
            this.mLastBurnInXOffset += xChange;
            if (this.mLastBurnInXOffset > this.mMaxHorizontalBurnInOffset || this.mLastBurnInXOffset < this.mMinHorizontalBurnInOffset) {
                this.mLastBurnInXOffset -= xChange;
                this.mXOffsetDirection *= BURN_IN_MAX_RADIUS_DEFAULT;
                int yChange = this.mYOffsetDirection * BURN_IN_SHIFT_STEP;
                this.mLastBurnInYOffset += yChange;
                if (this.mLastBurnInYOffset > this.mMaxVerticalBurnInOffset || this.mLastBurnInYOffset < this.mMinVerticalBurnInOffset) {
                    this.mLastBurnInYOffset -= yChange;
                    this.mYOffsetDirection *= BURN_IN_MAX_RADIUS_DEFAULT;
                }
            }
            if (this.mBurnInRadiusMaxSquared == BURN_IN_MAX_RADIUS_DEFAULT) {
                return;
            }
        } while ((this.mLastBurnInXOffset * this.mLastBurnInXOffset) + (this.mLastBurnInYOffset * this.mLastBurnInYOffset) > this.mBurnInRadiusMaxSquared);
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + TAG);
        prefix = prefix + "  ";
        pw.println(prefix + "mBurnInProtectionActive=" + this.mBurnInProtectionActive);
        pw.println(prefix + "mHorizontalBurnInOffsetsBounds=(" + this.mMinHorizontalBurnInOffset + ", " + this.mMaxHorizontalBurnInOffset + ")");
        pw.println(prefix + "mVerticalBurnInOffsetsBounds=(" + this.mMinVerticalBurnInOffset + ", " + this.mMaxVerticalBurnInOffset + ")");
        pw.println(prefix + "mBurnInRadiusMaxSquared=" + this.mBurnInRadiusMaxSquared);
        pw.println(prefix + "mLastBurnInOffset=(" + this.mLastBurnInXOffset + ", " + this.mLastBurnInYOffset + ")");
        pw.println(prefix + "mOfsetChangeDirections=(" + this.mXOffsetDirection + ", " + this.mYOffsetDirection + ")");
    }

    public void onDisplayAdded(int i) {
    }

    public void onDisplayRemoved(int i) {
    }

    public void onDisplayChanged(int displayId) {
        if (displayId != this.mDisplay.getDisplayId()) {
            return;
        }
        if (this.mDisplay.getState() == 3 || this.mDisplay.getState() == 4) {
            startBurnInProtection();
        } else {
            cancelBurnInProtection();
        }
    }

    public void onAnimationStart(Animator animator) {
    }

    public void onAnimationEnd(Animator animator) {
        if (animator == this.mCenteringAnimator && !this.mBurnInProtectionActive) {
            this.mAppliedBurnInXOffset = 0;
            this.mAppliedBurnInYOffset = 0;
            this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplay.getDisplayId(), 0, 0);
        }
    }

    public void onAnimationCancel(Animator animator) {
    }

    public void onAnimationRepeat(Animator animator) {
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (!this.mBurnInProtectionActive) {
            float value = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            this.mDisplayManagerInternal.setDisplayOffsets(this.mDisplay.getDisplayId(), (int) (((float) this.mAppliedBurnInXOffset) * value), (int) (((float) this.mAppliedBurnInYOffset) * value));
        }
    }
}
