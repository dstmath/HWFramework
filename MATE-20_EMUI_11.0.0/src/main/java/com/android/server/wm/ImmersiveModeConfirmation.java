package com.android.server.wm;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;

public class ImmersiveModeConfirmation {
    private static final String CONFIRMED = "confirmed";
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_SHOW_EVERY_TIME = false;
    private static final String TAG = "ImmersiveModeConfirmation";
    private static boolean sConfirmed;
    private ClingWindowView mClingWindow;
    private final Runnable mConfirm = new Runnable() {
        /* class com.android.server.wm.ImmersiveModeConfirmation.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            if (!ImmersiveModeConfirmation.sConfirmed) {
                boolean unused = ImmersiveModeConfirmation.sConfirmed = true;
                ImmersiveModeConfirmation.saveSetting(ImmersiveModeConfirmation.this.mContext);
            }
            ImmersiveModeConfirmation.this.handleHide();
        }
    };
    private final Context mContext;
    private final H mHandler;
    private int mLockTaskState = 0;
    private final long mPanicThresholdMs;
    private long mPanicTime;
    private final long mShowDelayMs;
    private boolean mVrModeEnabled;
    private WindowManager mWindowManager;
    private final IBinder mWindowToken = new Binder();

    ImmersiveModeConfirmation(Context context, Looper looper, boolean vrModeEnabled) {
        Display display = context.getDisplay();
        Context uiContext = ActivityThread.currentActivityThread().getSystemUiContext();
        this.mContext = display.getDisplayId() == 0 ? uiContext : uiContext.createDisplayContext(display);
        this.mHandler = new H(looper);
        this.mShowDelayMs = getNavBarExitDuration() * 3;
        this.mPanicThresholdMs = (long) context.getResources().getInteger(17694815);
        this.mVrModeEnabled = vrModeEnabled;
    }

    private long getNavBarExitDuration() {
        Animation exit = AnimationUtils.loadAnimation(this.mContext, 17432752);
        if (exit != null) {
            return exit.getDuration();
        }
        return 0;
    }

    static boolean loadSetting(int currentUserId, Context context) {
        boolean wasConfirmed = sConfirmed;
        sConfirmed = false;
        String value = null;
        try {
            value = Settings.Secure.getStringForUser(context.getContentResolver(), "immersive_mode_confirmations", -2);
            sConfirmed = CONFIRMED.equals(value);
        } catch (Throwable t) {
            Slog.w(TAG, "Error loading confirmations, value=" + value, t);
        }
        if (sConfirmed != wasConfirmed) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static void saveSetting(Context context) {
        try {
            Settings.Secure.putStringForUser(context.getContentResolver(), "immersive_mode_confirmations", sConfirmed ? CONFIRMED : null, -2);
        } catch (Throwable t) {
            Slog.w(TAG, "Error saving confirmations, sConfirmed=" + sConfirmed, t);
        }
    }

    /* access modifiers changed from: package-private */
    public void immersiveModeChangedLw(String pkg, boolean isImmersiveMode, boolean userSetupComplete, boolean navBarEmpty) {
        this.mHandler.removeMessages(1);
        if (!isImmersiveMode) {
            this.mHandler.sendEmptyMessage(2);
        } else if (!PolicyControl.disableImmersiveConfirmation(pkg) && !sConfirmed && userSetupComplete && !this.mVrModeEnabled && !navBarEmpty && !UserManager.isDeviceInDemoMode(this.mContext) && this.mLockTaskState != 1) {
            this.mHandler.sendEmptyMessageDelayed(1, this.mShowDelayMs);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onPowerKeyDown(boolean isScreenOn, long time, boolean inImmersiveMode, boolean navBarEmpty) {
        if (isScreenOn || time - this.mPanicTime >= this.mPanicThresholdMs) {
            if (!isScreenOn || !inImmersiveMode || navBarEmpty) {
                this.mPanicTime = 0;
            } else {
                this.mPanicTime = time;
            }
            return false;
        } else if (this.mClingWindow == null) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void confirmCurrentPrompt() {
        if (this.mClingWindow != null) {
            this.mHandler.post(this.mConfirm);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHide() {
        if (this.mClingWindow != null) {
            getWindowManager().removeView(this.mClingWindow);
            this.mClingWindow = null;
        }
    }

    private WindowManager.LayoutParams getClingWindowLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1, -1, 2014, 16777504, -3);
        lp.privateFlags |= 16;
        lp.setTitle(TAG);
        lp.windowAnimations = 16974586;
        lp.token = getWindowToken();
        return lp;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FrameLayout.LayoutParams getBubbleLayoutParams() {
        return new FrameLayout.LayoutParams(this.mContext.getResources().getDimensionPixelSize(17105210), -2, 49);
    }

    /* access modifiers changed from: package-private */
    public IBinder getWindowToken() {
        return this.mWindowToken;
    }

    /* access modifiers changed from: private */
    public class ClingWindowView extends FrameLayout {
        private static final int ANIMATION_DURATION = 250;
        private static final int BGCOLOR = Integer.MIN_VALUE;
        private static final int OFFSET_DP = 96;
        private ViewGroup mClingLayout;
        private final ColorDrawable mColor = new ColorDrawable(0);
        private ValueAnimator mColorAnim;
        private final Runnable mConfirm;
        private ViewTreeObserver.OnComputeInternalInsetsListener mInsetsListener = new ViewTreeObserver.OnComputeInternalInsetsListener() {
            /* class com.android.server.wm.ImmersiveModeConfirmation.ClingWindowView.AnonymousClass2 */
            private final int[] mTmpInt2 = new int[2];

            public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo inoutInfo) {
                ClingWindowView.this.mClingLayout.getLocationInWindow(this.mTmpInt2);
                inoutInfo.setTouchableInsets(3);
                Region region = inoutInfo.touchableRegion;
                int[] iArr = this.mTmpInt2;
                region.set(iArr[0], iArr[1], iArr[0] + ClingWindowView.this.mClingLayout.getWidth(), this.mTmpInt2[1] + ClingWindowView.this.mClingLayout.getHeight());
            }
        };
        private final Interpolator mInterpolator;
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            /* class com.android.server.wm.ImmersiveModeConfirmation.ClingWindowView.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    ClingWindowView clingWindowView = ClingWindowView.this;
                    clingWindowView.post(clingWindowView.mUpdateLayoutRunnable);
                }
            }
        };
        private Runnable mUpdateLayoutRunnable = new Runnable() {
            /* class com.android.server.wm.ImmersiveModeConfirmation.ClingWindowView.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (ClingWindowView.this.mClingLayout != null && ClingWindowView.this.mClingLayout.getParent() != null) {
                    ClingWindowView.this.mClingLayout.setLayoutParams(ImmersiveModeConfirmation.this.getBubbleLayoutParams());
                }
            }
        };

        ClingWindowView(Context context, Runnable confirm) {
            super(context);
            this.mConfirm = confirm;
            setBackground(this.mColor);
            setImportantForAccessibility(2);
            this.mInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563662);
        }

        @Override // android.view.View, android.view.ViewGroup
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            DisplayMetrics metrics = new DisplayMetrics();
            ImmersiveModeConfirmation.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float density = metrics.density;
            getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsListener);
            this.mClingLayout = (ViewGroup) View.inflate(getContext(), 17367167, null);
            ((Button) this.mClingLayout.findViewById(16909225)).setOnClickListener(new View.OnClickListener() {
                /* class com.android.server.wm.ImmersiveModeConfirmation.ClingWindowView.AnonymousClass4 */

                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    ClingWindowView.this.mConfirm.run();
                }
            });
            addView(this.mClingLayout, ImmersiveModeConfirmation.this.getBubbleLayoutParams());
            if (ActivityManager.isHighEndGfx()) {
                final View cling = this.mClingLayout;
                cling.setAlpha(0.0f);
                cling.setTranslationY(-96.0f * density);
                postOnAnimation(new Runnable() {
                    /* class com.android.server.wm.ImmersiveModeConfirmation.ClingWindowView.AnonymousClass5 */

                    @Override // java.lang.Runnable
                    public void run() {
                        cling.animate().alpha(1.0f).translationY(0.0f).setDuration(250).setInterpolator(ClingWindowView.this.mInterpolator).withLayer().start();
                        ClingWindowView.this.mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), 0, Integer.valueOf((int) ClingWindowView.BGCOLOR));
                        ClingWindowView.this.mColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            /* class com.android.server.wm.ImmersiveModeConfirmation.ClingWindowView.AnonymousClass5.AnonymousClass1 */

                            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                            public void onAnimationUpdate(ValueAnimator animation) {
                                ClingWindowView.this.mColor.setColor(((Integer) animation.getAnimatedValue()).intValue());
                            }
                        });
                        ClingWindowView.this.mColorAnim.setDuration(250L);
                        ClingWindowView.this.mColorAnim.setInterpolator(ClingWindowView.this.mInterpolator);
                        ClingWindowView.this.mColorAnim.start();
                    }
                });
            } else {
                this.mColor.setColor(BGCOLOR);
            }
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CONFIGURATION_CHANGED"));
        }

        @Override // android.view.View, android.view.ViewGroup
        public void onDetachedFromWindow() {
            this.mContext.unregisterReceiver(this.mReceiver);
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motion) {
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private WindowManager getWindowManager() {
        if (this.mWindowManager == null) {
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        }
        return this.mWindowManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShow() {
        this.mClingWindow = new ClingWindowView(this.mContext, this.mConfirm);
        this.mClingWindow.setSystemUiVisibility(768);
        getWindowManager().addView(this.mClingWindow, getClingWindowLayoutParams());
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        private static final int HIDE = 2;
        private static final int SHOW = 1;
        private boolean isShow = false;

        H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 1) {
                if (i == 2) {
                    ImmersiveModeConfirmation.this.handleHide();
                }
            } else if (this.isShow) {
                ImmersiveModeConfirmation.this.handleShow();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onVrStateChangedLw(boolean enabled) {
        this.mVrModeEnabled = enabled;
        if (this.mVrModeEnabled) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(2);
        }
    }

    /* access modifiers changed from: package-private */
    public void onLockTaskModeChangedLw(int lockTaskState) {
        this.mLockTaskState = lockTaskState;
    }
}
