package com.android.server.policy;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
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
    private ClingWindowView mClingWindow;
    private final Runnable mConfirm = new Runnable() {
        public void run() {
            if (!ImmersiveModeConfirmation.this.mConfirmed) {
                ImmersiveModeConfirmation.this.mConfirmed = true;
                ImmersiveModeConfirmation.this.saveSetting();
            }
            ImmersiveModeConfirmation.this.handleHide();
        }
    };
    private boolean mConfirmed;
    private final Context mContext;
    private int mCurrentUserId;
    private final H mHandler;
    private final long mPanicThresholdMs;
    private long mPanicTime;
    private final long mShowDelayMs;
    boolean mVrModeEnabled = false;
    private final IVrStateCallbacks mVrStateCallbacks = new Stub() {
        public void onVrStateChanged(boolean enabled) throws RemoteException {
            ImmersiveModeConfirmation.this.mVrModeEnabled = enabled;
            if (ImmersiveModeConfirmation.this.mVrModeEnabled) {
                ImmersiveModeConfirmation.this.mHandler.removeMessages(1);
                ImmersiveModeConfirmation.this.mHandler.sendEmptyMessage(2);
            }
        }
    };
    private WindowManager mWindowManager;
    private final IBinder mWindowToken = new Binder();

    private class ClingWindowView extends FrameLayout {
        private static final int ANIMATION_DURATION = 250;
        private static final int BGCOLOR = Integer.MIN_VALUE;
        private static final int OFFSET_DP = 96;
        private ViewGroup mClingLayout;
        private final ColorDrawable mColor = new ColorDrawable(0);
        private ValueAnimator mColorAnim;
        private final Runnable mConfirm;
        private OnComputeInternalInsetsListener mInsetsListener = new OnComputeInternalInsetsListener() {
            private final int[] mTmpInt2 = new int[2];

            public void onComputeInternalInsets(InternalInsetsInfo inoutInfo) {
                ClingWindowView.this.mClingLayout.getLocationInWindow(this.mTmpInt2);
                inoutInfo.setTouchableInsets(3);
                inoutInfo.touchableRegion.set(this.mTmpInt2[0], this.mTmpInt2[1], this.mTmpInt2[0] + ClingWindowView.this.mClingLayout.getWidth(), this.mTmpInt2[1] + ClingWindowView.this.mClingLayout.getHeight());
            }
        };
        private final Interpolator mInterpolator;
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    ClingWindowView.this.post(ClingWindowView.this.mUpdateLayoutRunnable);
                }
            }
        };
        private Runnable mUpdateLayoutRunnable = new Runnable() {
            public void run() {
                if (ClingWindowView.this.mClingLayout != null && ClingWindowView.this.mClingLayout.getParent() != null) {
                    ClingWindowView.this.mClingLayout.setLayoutParams(ImmersiveModeConfirmation.this.getBubbleLayoutParams());
                }
            }
        };

        public ClingWindowView(Context context, Runnable confirm) {
            super(context);
            this.mConfirm = confirm;
            setBackground(this.mColor);
            setImportantForAccessibility(2);
            this.mInterpolator = AnimationUtils.loadInterpolator(this.mContext, 17563662);
        }

        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            DisplayMetrics metrics = new DisplayMetrics();
            ImmersiveModeConfirmation.this.mWindowManager.getDefaultDisplay().getMetrics(metrics);
            float density = metrics.density;
            getViewTreeObserver().addOnComputeInternalInsetsListener(this.mInsetsListener);
            this.mClingLayout = (ViewGroup) View.inflate(getContext(), 17367150, null);
            ((Button) this.mClingLayout.findViewById(16909119)).setOnClickListener(new OnClickListener() {
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
                    public void run() {
                        cling.animate().alpha(1.0f).translationY(0.0f).setDuration(250).setInterpolator(ClingWindowView.this.mInterpolator).withLayer().start();
                        ClingWindowView.this.mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(0), Integer.valueOf(Integer.MIN_VALUE)});
                        ClingWindowView.this.mColorAnim.addUpdateListener(new AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                ClingWindowView.this.mColor.setColor(((Integer) animation.getAnimatedValue()).intValue());
                            }
                        });
                        ClingWindowView.this.mColorAnim.setDuration(250);
                        ClingWindowView.this.mColorAnim.setInterpolator(ClingWindowView.this.mInterpolator);
                        ClingWindowView.this.mColorAnim.start();
                    }
                });
            } else {
                this.mColor.setColor(Integer.MIN_VALUE);
            }
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CONFIGURATION_CHANGED"));
        }

        public void onDetachedFromWindow() {
            this.mContext.unregisterReceiver(this.mReceiver);
        }

        public boolean onTouchEvent(MotionEvent motion) {
            return true;
        }
    }

    private final class H extends Handler {
        private static final int HIDE = 2;
        private static final int SHOW = 1;
        private boolean isShow;

        /* synthetic */ H(ImmersiveModeConfirmation this$0, H -this1) {
            this();
        }

        private H() {
            this.isShow = false;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (this.isShow) {
                        ImmersiveModeConfirmation.this.handleShow();
                        return;
                    }
                    return;
                case 2:
                    ImmersiveModeConfirmation.this.handleHide();
                    return;
                default:
                    return;
            }
        }
    }

    public ImmersiveModeConfirmation(Context context) {
        this.mContext = context;
        this.mHandler = new H(this, null);
        this.mShowDelayMs = getNavBarExitDuration() * 3;
        this.mPanicThresholdMs = (long) context.getResources().getInteger(17694790);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    private long getNavBarExitDuration() {
        Animation exit = AnimationUtils.loadAnimation(this.mContext, 17432612);
        return exit != null ? exit.getDuration() : 0;
    }

    public void loadSetting(int currentUserId) {
        this.mConfirmed = false;
        this.mCurrentUserId = currentUserId;
        String str = null;
        try {
            str = Secure.getStringForUser(this.mContext.getContentResolver(), "immersive_mode_confirmations", -2);
            this.mConfirmed = CONFIRMED.equals(str);
        } catch (Throwable t) {
            Slog.w(TAG, "Error loading confirmations, value=" + str, t);
        }
    }

    private void saveSetting() {
        try {
            Secure.putStringForUser(this.mContext.getContentResolver(), "immersive_mode_confirmations", this.mConfirmed ? CONFIRMED : null, -2);
        } catch (Throwable t) {
            Slog.w(TAG, "Error saving confirmations, mConfirmed=" + this.mConfirmed, t);
        }
    }

    void systemReady() {
        IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
                this.mVrModeEnabled = vrManager.getVrModeState();
            } catch (RemoteException e) {
            }
        }
    }

    public void immersiveModeChangedLw(String pkg, boolean isImmersiveMode, boolean userSetupComplete, boolean navBarEmpty) {
        this.mHandler.removeMessages(1);
        if (!isImmersiveMode) {
            this.mHandler.sendEmptyMessage(2);
        } else if (!PolicyControl.disableImmersiveConfirmation(pkg) && (this.mConfirmed ^ 1) != 0 && userSetupComplete && (this.mVrModeEnabled ^ 1) != 0 && (navBarEmpty ^ 1) != 0 && (UserManager.isDeviceInDemoMode(this.mContext) ^ 1) != 0) {
            this.mHandler.sendEmptyMessageDelayed(1, this.mShowDelayMs);
        }
    }

    public boolean onPowerKeyDown(boolean isScreenOn, long time, boolean inImmersiveMode, boolean navBarEmpty) {
        boolean z = false;
        if (isScreenOn || time - this.mPanicTime >= this.mPanicThresholdMs) {
            if (isScreenOn && inImmersiveMode && (navBarEmpty ^ 1) != 0) {
                this.mPanicTime = time;
            } else {
                this.mPanicTime = 0;
            }
            return false;
        }
        if (this.mClingWindow == null) {
            z = true;
        }
        return z;
    }

    public void confirmCurrentPrompt() {
        if (this.mClingWindow != null) {
            this.mHandler.post(this.mConfirm);
        }
    }

    private void handleHide() {
        if (this.mClingWindow != null) {
            this.mWindowManager.removeView(this.mClingWindow);
            this.mClingWindow = null;
        }
    }

    public LayoutParams getClingWindowLayoutParams() {
        LayoutParams lp = new LayoutParams(-1, -1, 2014, 16777504, -3);
        lp.privateFlags |= 16;
        lp.setTitle(TAG);
        lp.windowAnimations = 16974578;
        lp.token = getWindowToken();
        return lp;
    }

    public FrameLayout.LayoutParams getBubbleLayoutParams() {
        return new FrameLayout.LayoutParams(this.mContext.getResources().getDimensionPixelSize(17105053), -2, 49);
    }

    public IBinder getWindowToken() {
        return this.mWindowToken;
    }

    private void handleShow() {
        this.mClingWindow = new ClingWindowView(this.mContext, this.mConfirm);
        this.mClingWindow.setSystemUiVisibility(768);
        this.mWindowManager.addView(this.mClingWindow, getClingWindowLayoutParams());
    }
}
