package com.android.server.display;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.MathUtils;
import android.util.Slog;
import android.view.animation.AnimationUtils;
import com.android.internal.app.ColorDisplayController;
import com.android.server.SystemService;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ColorDisplayService extends SystemService implements ColorDisplayController.Callback {
    private static final ColorMatrixEvaluator COLOR_MATRIX_EVALUATOR = new ColorMatrixEvaluator();
    private static final float[] MATRIX_IDENTITY = new float[16];
    private static final String TAG = "ColorDisplayService";
    private static final long TRANSITION_DURATION = 3000;
    private AutoMode mAutoMode;
    /* access modifiers changed from: private */
    public boolean mBootCompleted;
    /* access modifiers changed from: private */
    public ValueAnimator mColorMatrixAnimator;
    private final float[] mColorTempCoefficients = new float[9];
    /* access modifiers changed from: private */
    public ColorDisplayController mController;
    /* access modifiers changed from: private */
    public int mCurrentUser = -10000;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public Boolean mIsActivated;
    private float[] mMatrixNight = new float[16];
    /* access modifiers changed from: private */
    public ContentObserver mUserSetupObserver;

    private abstract class AutoMode implements ColorDisplayController.Callback {
        public abstract void onStart();

        public abstract void onStop();

        private AutoMode() {
        }
    }

    private static class ColorMatrixEvaluator implements TypeEvaluator<float[]> {
        private final float[] mResultMatrix;

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

    private class CustomAutoMode extends AutoMode implements AlarmManager.OnAlarmListener {
        private final AlarmManager mAlarmManager;
        private LocalTime mEndTime;
        private LocalDateTime mLastActivatedTime;
        private LocalTime mStartTime;
        private final BroadcastReceiver mTimeChangedReceiver;

        CustomAutoMode() {
            super();
            this.mAlarmManager = (AlarmManager) ColorDisplayService.this.getContext().getSystemService("alarm");
            this.mTimeChangedReceiver = new BroadcastReceiver(ColorDisplayService.this) {
                public void onReceive(Context context, Intent intent) {
                    CustomAutoMode.this.updateActivated();
                }
            };
        }

        /* access modifiers changed from: private */
        public void updateActivated() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = ColorDisplayService.getDateTimeBefore(this.mStartTime, now);
            LocalDateTime end = ColorDisplayService.getDateTimeAfter(this.mEndTime, start);
            boolean activate = now.isBefore(end);
            if (this.mLastActivatedTime != null && this.mLastActivatedTime.isBefore(now) && this.mLastActivatedTime.isAfter(start) && (this.mLastActivatedTime.isAfter(end) || now.isBefore(end))) {
                activate = ColorDisplayService.this.mController.isActivated();
            }
            if (ColorDisplayService.this.mIsActivated == null || ColorDisplayService.this.mIsActivated.booleanValue() != activate) {
                ColorDisplayService.this.mController.setActivated(activate);
            }
            updateNextAlarm(ColorDisplayService.this.mIsActivated, now);
        }

        private void updateNextAlarm(Boolean activated, LocalDateTime now) {
            LocalDateTime next;
            if (activated != null) {
                if (activated.booleanValue()) {
                    next = ColorDisplayService.getDateTimeAfter(this.mEndTime, now);
                } else {
                    next = ColorDisplayService.getDateTimeAfter(this.mStartTime, now);
                }
                this.mAlarmManager.setExact(1, next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), ColorDisplayService.TAG, this, null);
            }
        }

        public void onStart() {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            ColorDisplayService.this.getContext().registerReceiver(this.mTimeChangedReceiver, intentFilter);
            this.mStartTime = ColorDisplayService.this.mController.getCustomStartTime();
            this.mEndTime = ColorDisplayService.this.mController.getCustomEndTime();
            this.mLastActivatedTime = ColorDisplayService.this.mController.getLastActivatedTime();
            updateActivated();
        }

        public void onStop() {
            ColorDisplayService.this.getContext().unregisterReceiver(this.mTimeChangedReceiver);
            this.mAlarmManager.cancel(this);
            this.mLastActivatedTime = null;
        }

        public void onActivated(boolean activated) {
            this.mLastActivatedTime = ColorDisplayService.this.mController.getLastActivatedTime();
            updateNextAlarm(Boolean.valueOf(activated), LocalDateTime.now());
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
            Slog.d(ColorDisplayService.TAG, "onAlarm");
            updateActivated();
        }
    }

    private class TwilightAutoMode extends AutoMode implements TwilightListener {
        private final TwilightManager mTwilightManager;

        TwilightAutoMode() {
            super();
            this.mTwilightManager = (TwilightManager) ColorDisplayService.this.getLocalService(TwilightManager.class);
        }

        private void updateActivated(TwilightState state) {
            if (state != null) {
                boolean activate = state.isNight();
                LocalDateTime lastActivatedTime = ColorDisplayService.this.mController.getLastActivatedTime();
                if (lastActivatedTime != null) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime sunrise = state.sunrise();
                    LocalDateTime sunset = state.sunset();
                    if (lastActivatedTime.isBefore(now) && (lastActivatedTime.isBefore(sunrise) ^ lastActivatedTime.isBefore(sunset))) {
                        activate = ColorDisplayService.this.mController.isActivated();
                    }
                }
                if (ColorDisplayService.this.mIsActivated == null || ColorDisplayService.this.mIsActivated.booleanValue() != activate) {
                    ColorDisplayService.this.mController.setActivated(activate);
                }
            }
        }

        public void onStart() {
            this.mTwilightManager.registerListener(this, ColorDisplayService.this.mHandler);
            updateActivated(this.mTwilightManager.getLastTwilightState());
        }

        public void onStop() {
            this.mTwilightManager.unregisterListener(this);
        }

        public void onActivated(boolean activated) {
        }

        public void onTwilightStateChanged(TwilightState state) {
            StringBuilder sb = new StringBuilder();
            sb.append("onTwilightStateChanged: isNight=");
            sb.append(state == null ? null : Boolean.valueOf(state.isNight()));
            Slog.d(ColorDisplayService.TAG, sb.toString());
            updateActivated(state);
        }
    }

    static {
        Matrix.setIdentityM(MATRIX_IDENTITY, 0);
    }

    public ColorDisplayService(Context context) {
        super(context);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
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
                    if (ColorDisplayService.isUserSetupCompleted(cr, ColorDisplayService.this.mCurrentUser)) {
                        cr.unregisterContentObserver(this);
                        ContentObserver unused = ColorDisplayService.this.mUserSetupObserver = null;
                        if (ColorDisplayService.this.mBootCompleted) {
                            ColorDisplayService.this.setUp();
                        }
                    }
                }
            };
            cr.registerContentObserver(Settings.Secure.getUriFor("user_setup_complete"), false, this.mUserSetupObserver, this.mCurrentUser);
        } else if (this.mBootCompleted) {
            setUp();
        }
    }

    /* access modifiers changed from: private */
    public static boolean isUserSetupCompleted(ContentResolver cr, int userHandle) {
        return Settings.Secure.getIntForUser(cr, "user_setup_complete", 0, userHandle) == 1;
    }

    /* access modifiers changed from: private */
    public void setUp() {
        Slog.d(TAG, "setUp: currentUser=" + this.mCurrentUser);
        this.mController = new ColorDisplayController(getContext(), this.mCurrentUser);
        this.mController.setListener(this);
        onDisplayColorModeChanged(this.mController.getColorMode());
        this.mIsActivated = null;
        setCoefficientMatrix(getContext(), DisplayTransformManager.needsLinearColorMatrix());
        setMatrix(this.mController.getColorTemperature(), this.mMatrixNight);
        onAutoModeChanged(this.mController.getAutoMode());
        if (this.mIsActivated == null) {
            onActivated(this.mController.isActivated());
        }
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

    public void onDisplayColorModeChanged(int mode) {
        float[] fArr;
        if (mode != -1) {
            if (this.mColorMatrixAnimator != null) {
                this.mColorMatrixAnimator.cancel();
            }
            setCoefficientMatrix(getContext(), DisplayTransformManager.needsLinearColorMatrix(mode));
            setMatrix(this.mController.getColorTemperature(), this.mMatrixNight);
            DisplayTransformManager dtm = (DisplayTransformManager) getLocalService(DisplayTransformManager.class);
            if (this.mIsActivated == null || !this.mIsActivated.booleanValue()) {
                fArr = MATRIX_IDENTITY;
            } else {
                fArr = this.mMatrixNight;
            }
            dtm.setColorMode(mode, fArr);
        }
    }

    public void onAccessibilityTransformChanged(boolean state) {
        onDisplayColorModeChanged(this.mController.getColorMode());
    }

    private void setCoefficientMatrix(Context context, boolean needsLinear) {
        int i;
        Resources resources = context.getResources();
        if (needsLinear) {
            i = 17236022;
        } else {
            i = 17236023;
        }
        String[] coefficients = resources.getStringArray(i);
        int i2 = 0;
        while (i2 < 9 && i2 < coefficients.length) {
            this.mColorTempCoefficients[i2] = Float.parseFloat(coefficients[i2]);
            i2++;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: java.lang.Object[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void applyTint(boolean immediate) {
        if (this.mColorMatrixAnimator != null) {
            this.mColorMatrixAnimator.cancel();
        }
        final DisplayTransformManager dtm = (DisplayTransformManager) getLocalService(DisplayTransformManager.class);
        float[] from = dtm.getColorMatrix(100);
        final float[] to = this.mIsActivated.booleanValue() ? this.mMatrixNight : MATRIX_IDENTITY;
        if (immediate) {
            dtm.setColorMatrix(100, to);
            return;
        }
        ColorMatrixEvaluator colorMatrixEvaluator = COLOR_MATRIX_EVALUATOR;
        Object[] objArr = new Object[2];
        objArr[0] = from == null ? MATRIX_IDENTITY : from;
        objArr[1] = to;
        this.mColorMatrixAnimator = ValueAnimator.ofObject(colorMatrixEvaluator, objArr);
        this.mColorMatrixAnimator.setDuration(TRANSITION_DURATION);
        this.mColorMatrixAnimator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563661));
        this.mColorMatrixAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
                ValueAnimator unused = ColorDisplayService.this.mColorMatrixAnimator = null;
            }
        });
        this.mColorMatrixAnimator.start();
    }

    private void setMatrix(int colorTemperature, float[] outTemp) {
        if (outTemp.length != 16) {
            Slog.d(TAG, "The display transformation matrix must be 4x4");
            return;
        }
        Matrix.setIdentityM(this.mMatrixNight, 0);
        float squareTemperature = (float) (colorTemperature * colorTemperature);
        float red = (this.mColorTempCoefficients[0] * squareTemperature) + (((float) colorTemperature) * this.mColorTempCoefficients[1]) + this.mColorTempCoefficients[2];
        float green = (this.mColorTempCoefficients[3] * squareTemperature) + (((float) colorTemperature) * this.mColorTempCoefficients[4]) + this.mColorTempCoefficients[5];
        outTemp[0] = red;
        outTemp[5] = green;
        outTemp[10] = (this.mColorTempCoefficients[6] * squareTemperature) + (((float) colorTemperature) * this.mColorTempCoefficients[7]) + this.mColorTempCoefficients[8];
    }

    public static LocalDateTime getDateTimeBefore(LocalTime localTime, LocalDateTime compareTime) {
        LocalDateTime ldt = LocalDateTime.of(compareTime.getYear(), compareTime.getMonth(), compareTime.getDayOfMonth(), localTime.getHour(), localTime.getMinute());
        return ldt.isAfter(compareTime) ? ldt.minusDays(1) : ldt;
    }

    public static LocalDateTime getDateTimeAfter(LocalTime localTime, LocalDateTime compareTime) {
        LocalDateTime ldt = LocalDateTime.of(compareTime.getYear(), compareTime.getMonth(), compareTime.getDayOfMonth(), localTime.getHour(), localTime.getMinute());
        return ldt.isBefore(compareTime) ? ldt.plusDays(1) : ldt;
    }
}
