package com.android.server.display;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.util.MathUtils;
import android.util.Slog;
import android.view.animation.AnimationUtils;
import com.android.internal.app.NightDisplayController;
import com.android.internal.app.NightDisplayController.Callback;
import com.android.internal.app.NightDisplayController.LocalTime;
import com.android.server.SystemService;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NightDisplayService extends SystemService implements Callback {
    private static final ColorMatrixEvaluator COLOR_MATRIX_EVALUATOR = new ColorMatrixEvaluator();
    private static final float[] MATRIX_IDENTITY = new float[16];
    private static final String TAG = "NightDisplayService";
    private static final long TRANSITION_DURATION = 3000;
    private static final float[] mColorTempCoefficients = new float[]{0.0f, -9.623533E-9f, -1.8935904E-8f, 0.0f, 1.5304548E-4f, 3.024122E-4f, 1.0f, 0.39078277f, -0.1986509f};
    private AutoMode mAutoMode;
    private boolean mBootCompleted;
    private ValueAnimator mColorMatrixAnimator;
    private NightDisplayController mController;
    private int mCurrentUser = -10000;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean mIgnoreAllColorMatrixChanges = new AtomicBoolean();
    private Boolean mIsActivated;
    private float[] mMatrixNight = new float[16];
    private ContentObserver mUserSetupObserver;
    private final IVrStateCallbacks mVrStateCallbacks = new Stub() {
        public void onVrStateChanged(final boolean enabled) {
            NightDisplayService.this.mIgnoreAllColorMatrixChanges.set(enabled);
            NightDisplayService.this.mHandler.post(new Runnable() {
                public void run() {
                    if (NightDisplayService.this.mColorMatrixAnimator != null) {
                        NightDisplayService.this.mColorMatrixAnimator.cancel();
                    }
                    DisplayTransformManager dtm = (DisplayTransformManager) NightDisplayService.this.-wrap1(DisplayTransformManager.class);
                    if (enabled) {
                        dtm.setColorMatrix(100, NightDisplayService.MATRIX_IDENTITY);
                    } else if (NightDisplayService.this.mController != null && NightDisplayService.this.mController.isActivated()) {
                        NightDisplayService.this.setMatrix(NightDisplayService.this.mController.getColorTemperature(), NightDisplayService.this.mMatrixNight);
                        dtm.setColorMatrix(100, NightDisplayService.this.mMatrixNight);
                    }
                }
            });
        }
    };

    private abstract class AutoMode implements Callback {
        /* synthetic */ AutoMode(NightDisplayService this$0, AutoMode -this1) {
            this();
        }

        public abstract void onStart();

        public abstract void onStop();

        private AutoMode() {
        }
    }

    private static class ColorMatrixEvaluator implements TypeEvaluator<float[]> {
        private final float[] mResultMatrix;

        /* synthetic */ ColorMatrixEvaluator(ColorMatrixEvaluator -this0) {
            this();
        }

        private ColorMatrixEvaluator() {
            this.mResultMatrix = new float[16];
        }

        public float[] evaluate(float fraction, float[] startValue, float[] endValue) {
            for (int i = 0; i < this.mResultMatrix.length; i++) {
                this.mResultMatrix[i] = MathUtils.lerp(startValue[i], endValue[i], fraction);
            }
            return this.mResultMatrix;
        }
    }

    private class CustomAutoMode extends AutoMode implements OnAlarmListener {
        private final AlarmManager mAlarmManager;
        private LocalTime mEndTime;
        private Calendar mLastActivatedTime;
        private LocalTime mStartTime;
        private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                CustomAutoMode.this.updateActivated();
            }
        };

        CustomAutoMode() {
            super(NightDisplayService.this, null);
            this.mAlarmManager = (AlarmManager) NightDisplayService.this.getContext().getSystemService("alarm");
        }

        private void updateActivated() {
            Calendar now = Calendar.getInstance();
            Calendar startTime = this.mStartTime.getDateTimeBefore(now);
            Calendar endTime = this.mEndTime.getDateTimeAfter(startTime);
            boolean activate = now.before(endTime);
            if (this.mLastActivatedTime != null) {
                TimeZone currentTimeZone = now.getTimeZone();
                if (!currentTimeZone.equals(this.mLastActivatedTime.getTimeZone())) {
                    int year = this.mLastActivatedTime.get(1);
                    int dayOfYear = this.mLastActivatedTime.get(6);
                    int hourOfDay = this.mLastActivatedTime.get(11);
                    int minute = this.mLastActivatedTime.get(12);
                    this.mLastActivatedTime.setTimeZone(currentTimeZone);
                    this.mLastActivatedTime.set(1, year);
                    this.mLastActivatedTime.set(6, dayOfYear);
                    this.mLastActivatedTime.set(11, hourOfDay);
                    this.mLastActivatedTime.set(12, minute);
                }
                if (this.mLastActivatedTime.before(now) && this.mLastActivatedTime.after(startTime) && (this.mLastActivatedTime.after(endTime) || now.before(endTime))) {
                    activate = NightDisplayService.this.mController.isActivated();
                }
            }
            if (NightDisplayService.this.mIsActivated == null || NightDisplayService.this.mIsActivated.booleanValue() != activate) {
                NightDisplayService.this.mController.setActivated(activate);
            }
            updateNextAlarm(NightDisplayService.this.mIsActivated, now);
        }

        private void updateNextAlarm(Boolean activated, Calendar now) {
            if (activated != null) {
                Calendar next;
                if (activated.booleanValue()) {
                    next = this.mEndTime.getDateTimeAfter(now);
                } else {
                    next = this.mStartTime.getDateTimeAfter(now);
                }
                this.mAlarmManager.setExact(1, next.getTimeInMillis(), NightDisplayService.TAG, this, null);
            }
        }

        public void onStart() {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            NightDisplayService.this.getContext().registerReceiver(this.mTimeChangedReceiver, intentFilter);
            this.mStartTime = NightDisplayService.this.mController.getCustomStartTime();
            this.mEndTime = NightDisplayService.this.mController.getCustomEndTime();
            this.mLastActivatedTime = NightDisplayService.this.mController.getLastActivatedTime();
            updateActivated();
        }

        public void onStop() {
            NightDisplayService.this.getContext().unregisterReceiver(this.mTimeChangedReceiver);
            this.mAlarmManager.cancel(this);
            this.mLastActivatedTime = null;
        }

        public void onActivated(boolean activated) {
            this.mLastActivatedTime = NightDisplayService.this.mController.getLastActivatedTime();
            updateNextAlarm(Boolean.valueOf(activated), Calendar.getInstance());
        }

        public void onCustomStartTimeChanged(LocalTime startTime) {
            this.mStartTime = startTime;
            this.mLastActivatedTime = null;
            updateActivated();
        }

        public void onCustomEndTimeChanged(LocalTime endTime) {
            this.mEndTime = endTime;
            this.mLastActivatedTime = null;
            updateActivated();
        }

        public void onAlarm() {
            Slog.d(NightDisplayService.TAG, "onAlarm");
            updateActivated();
        }
    }

    private class TwilightAutoMode extends AutoMode implements TwilightListener {
        private final TwilightManager mTwilightManager;

        TwilightAutoMode() {
            super(NightDisplayService.this, null);
            this.mTwilightManager = (TwilightManager) NightDisplayService.this.-wrap1(TwilightManager.class);
        }

        private void updateActivated(TwilightState state) {
            if (state != null) {
                boolean activate = state.isNight();
                Calendar lastActivatedTime = NightDisplayService.this.mController.getLastActivatedTime();
                if (lastActivatedTime != null) {
                    Calendar now = Calendar.getInstance();
                    Calendar sunrise = state.sunrise();
                    Calendar sunset = state.sunset();
                    if (lastActivatedTime.before(now) && (lastActivatedTime.after(sunrise) ^ lastActivatedTime.after(sunset)) != 0) {
                        activate = NightDisplayService.this.mController.isActivated();
                    }
                }
                if (NightDisplayService.this.mIsActivated == null || NightDisplayService.this.mIsActivated.booleanValue() != activate) {
                    NightDisplayService.this.mController.setActivated(activate);
                }
            }
        }

        public void onStart() {
            this.mTwilightManager.registerListener(this, NightDisplayService.this.mHandler);
            updateActivated(this.mTwilightManager.getLastTwilightState());
        }

        public void onStop() {
            this.mTwilightManager.unregisterListener(this);
        }

        public void onActivated(boolean activated) {
        }

        public void onTwilightStateChanged(TwilightState state) {
            Object obj = null;
            String str = NightDisplayService.TAG;
            StringBuilder append = new StringBuilder().append("onTwilightStateChanged: isNight=");
            if (state != null) {
                obj = Boolean.valueOf(state.isNight());
            }
            Slog.d(str, append.append(obj).toString());
            updateActivated(state);
        }
    }

    static {
        Matrix.setIdentityM(MATRIX_IDENTITY, 0);
    }

    public NightDisplayService(Context context) {
        super(context);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase >= 500) {
            IVrManager vrManager = IVrManager.Stub.asInterface(getBinderService("vrmanager"));
            if (vrManager != null) {
                try {
                    vrManager.registerListener(this.mVrStateCallbacks);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failed to register VR mode state listener: " + e);
                }
            }
        }
        if (phase >= 1000) {
            this.mBootCompleted = true;
            if (this.mCurrentUser != -10000 && this.mUserSetupObserver == null) {
                setUp();
            }
        }
    }

    public void onStartUser(int userHandle) {
        super.onStartUser(userHandle);
        if (this.mCurrentUser == -10000) {
            onUserChanged(userHandle);
        }
    }

    public void onSwitchUser(int userHandle) {
        super.onSwitchUser(userHandle);
        onUserChanged(userHandle);
    }

    public void onStopUser(int userHandle) {
        super.onStopUser(userHandle);
        if (this.mCurrentUser == userHandle) {
            onUserChanged(-10000);
        }
    }

    private void onUserChanged(int userHandle) {
        final ContentResolver cr = getContext().getContentResolver();
        if (this.mCurrentUser != -10000) {
            if (this.mUserSetupObserver != null) {
                cr.unregisterContentObserver(this.mUserSetupObserver);
                this.mUserSetupObserver = null;
            } else if (this.mBootCompleted) {
                tearDown();
            }
        }
        this.mCurrentUser = userHandle;
        if (this.mCurrentUser == -10000) {
            return;
        }
        if (!isUserSetupCompleted(cr, this.mCurrentUser)) {
            this.mUserSetupObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean selfChange, Uri uri) {
                    if (NightDisplayService.isUserSetupCompleted(cr, NightDisplayService.this.mCurrentUser)) {
                        cr.unregisterContentObserver(this);
                        NightDisplayService.this.mUserSetupObserver = null;
                        if (NightDisplayService.this.mBootCompleted) {
                            NightDisplayService.this.setUp();
                        }
                    }
                }
            };
            cr.registerContentObserver(Secure.getUriFor("user_setup_complete"), false, this.mUserSetupObserver, this.mCurrentUser);
        } else if (this.mBootCompleted) {
            setUp();
        }
    }

    private static boolean isUserSetupCompleted(ContentResolver cr, int userHandle) {
        return Secure.getIntForUser(cr, "user_setup_complete", 0, userHandle) == 1;
    }

    private void setUp() {
        Slog.d(TAG, "setUp: currentUser=" + this.mCurrentUser);
        this.mController = new NightDisplayController(getContext(), this.mCurrentUser);
        this.mController.setListener(this);
        setMatrix(this.mController.getColorTemperature(), this.mMatrixNight);
        onAutoModeChanged(this.mController.getAutoMode());
        if (this.mIsActivated == null) {
            onActivated(this.mController.isActivated());
        }
        applyTint(false);
    }

    private void tearDown() {
        Slog.d(TAG, "tearDown: currentUser=" + this.mCurrentUser);
        if (this.mController != null) {
            this.mController.setListener(null);
            this.mController = null;
        }
        if (this.mAutoMode != null) {
            this.mAutoMode.onStop();
            this.mAutoMode = null;
        }
        if (this.mColorMatrixAnimator != null) {
            this.mColorMatrixAnimator.end();
            this.mColorMatrixAnimator = null;
        }
        this.mIsActivated = null;
    }

    public void onActivated(boolean activated) {
        if (this.mIsActivated == null || this.mIsActivated.booleanValue() != activated) {
            Slog.i(TAG, activated ? "Turning on night display" : "Turning off night display");
            this.mIsActivated = Boolean.valueOf(activated);
            if (this.mAutoMode != null) {
                this.mAutoMode.onActivated(activated);
            }
            applyTint(false);
        }
    }

    public void onAutoModeChanged(int autoMode) {
        Slog.d(TAG, "onAutoModeChanged: autoMode=" + autoMode);
        if (this.mAutoMode != null) {
            this.mAutoMode.onStop();
            this.mAutoMode = null;
        }
        if (autoMode == 1) {
            this.mAutoMode = new CustomAutoMode();
        } else if (autoMode == 2) {
            this.mAutoMode = new TwilightAutoMode();
        }
        if (this.mAutoMode != null) {
            this.mAutoMode.onStart();
        }
    }

    public void onCustomStartTimeChanged(LocalTime startTime) {
        Slog.d(TAG, "onCustomStartTimeChanged: startTime=" + startTime);
        if (this.mAutoMode != null) {
            this.mAutoMode.onCustomStartTimeChanged(startTime);
        }
    }

    public void onCustomEndTimeChanged(LocalTime endTime) {
        Slog.d(TAG, "onCustomEndTimeChanged: endTime=" + endTime);
        if (this.mAutoMode != null) {
            this.mAutoMode.onCustomEndTimeChanged(endTime);
        }
    }

    public void onColorTemperatureChanged(int colorTemperature) {
        setMatrix(colorTemperature, this.mMatrixNight);
        applyTint(true);
    }

    private void applyTint(boolean immediate) {
        if (this.mColorMatrixAnimator != null) {
            this.mColorMatrixAnimator.cancel();
        }
        if (!this.mIgnoreAllColorMatrixChanges.get()) {
            final DisplayTransformManager dtm = (DisplayTransformManager) -wrap1(DisplayTransformManager.class);
            float[] from = dtm.getColorMatrix(100);
            final float[] to = this.mIsActivated.booleanValue() ? this.mMatrixNight : MATRIX_IDENTITY;
            if (immediate) {
                dtm.setColorMatrix(100, to);
            } else {
                TypeEvaluator typeEvaluator = COLOR_MATRIX_EVALUATOR;
                Object[] objArr = new Object[2];
                if (from == null) {
                    from = MATRIX_IDENTITY;
                }
                objArr[0] = from;
                objArr[1] = to;
                this.mColorMatrixAnimator = ValueAnimator.ofObject(typeEvaluator, objArr);
                this.mColorMatrixAnimator.setDuration(TRANSITION_DURATION);
                this.mColorMatrixAnimator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563661));
                this.mColorMatrixAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animator) {
                        dtm.setColorMatrix(100, (float[]) animator.getAnimatedValue());
                    }
                });
                this.mColorMatrixAnimator.addListener(new AnimatorListenerAdapter() {
                    private boolean mIsCancelled;

                    public void onAnimationCancel(Animator animator) {
                        this.mIsCancelled = true;
                    }

                    public void onAnimationEnd(Animator animator) {
                        if (!this.mIsCancelled) {
                            dtm.setColorMatrix(100, to);
                        }
                        NightDisplayService.this.mColorMatrixAnimator = null;
                    }
                });
                this.mColorMatrixAnimator.start();
            }
        }
    }

    private void setMatrix(int colorTemperature, float[] outTemp) {
        if (outTemp.length != 16) {
            Slog.d(TAG, "The display transformation matrix must be 4x4");
            return;
        }
        Matrix.setIdentityM(this.mMatrixNight, 0);
        float squareTemperature = (float) (colorTemperature * colorTemperature);
        float green = ((mColorTempCoefficients[1] * squareTemperature) + (((float) colorTemperature) * mColorTempCoefficients[4])) + mColorTempCoefficients[7];
        float blue = ((mColorTempCoefficients[2] * squareTemperature) + (((float) colorTemperature) * mColorTempCoefficients[5])) + mColorTempCoefficients[8];
        outTemp[0] = ((mColorTempCoefficients[0] * squareTemperature) + (((float) colorTemperature) * mColorTempCoefficients[3])) + mColorTempCoefficients[6];
        outTemp[5] = green;
        outTemp[10] = blue;
    }
}
