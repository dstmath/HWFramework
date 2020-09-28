package android.os;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioAttributes;
import android.util.Log;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class Vibrator {
    private static final String TAG = "Vibrator";
    public static final int VIBRATION_INTENSITY_HIGH = 3;
    public static final int VIBRATION_INTENSITY_LOW = 1;
    public static final int VIBRATION_INTENSITY_MEDIUM = 2;
    public static final int VIBRATION_INTENSITY_OFF = 0;
    private int mDefaultHapticFeedbackIntensity;
    private int mDefaultNotificationVibrationIntensity;
    private int mDefaultRingVibrationIntensity;
    private final String mPackageName;

    @Retention(RetentionPolicy.SOURCE)
    public @interface VibrationIntensity {
    }

    public abstract void cancel();

    public abstract boolean hasAmplitudeControl();

    public abstract boolean hasVibrator();

    public abstract void vibrate(int i, String str, VibrationEffect vibrationEffect, String str2, AudioAttributes audioAttributes);

    @UnsupportedAppUsage
    public Vibrator() {
        this.mPackageName = ActivityThread.currentPackageName();
        loadVibrationIntensities(ActivityThread.currentActivityThread().getSystemContext());
    }

    protected Vibrator(Context context) {
        this.mPackageName = context.getOpPackageName();
        loadVibrationIntensities(context);
    }

    private void loadVibrationIntensities(Context context) {
        this.mDefaultHapticFeedbackIntensity = loadDefaultIntensity(context, R.integer.config_defaultHapticFeedbackIntensity);
        this.mDefaultNotificationVibrationIntensity = loadDefaultIntensity(context, R.integer.config_defaultNotificationVibrationIntensity);
        this.mDefaultRingVibrationIntensity = loadDefaultIntensity(context, R.integer.config_defaultRingVibrationIntensity);
    }

    private int loadDefaultIntensity(Context ctx, int resId) {
        if (ctx != null) {
            return ctx.getResources().getInteger(resId);
        }
        return 2;
    }

    public int getDefaultHapticFeedbackIntensity() {
        return this.mDefaultHapticFeedbackIntensity;
    }

    public int getDefaultNotificationVibrationIntensity() {
        return this.mDefaultNotificationVibrationIntensity;
    }

    public int getDefaultRingVibrationIntensity() {
        return this.mDefaultRingVibrationIntensity;
    }

    @Deprecated
    public void vibrate(long milliseconds) {
        vibrate(milliseconds, (AudioAttributes) null);
    }

    @Deprecated
    public void vibrate(long milliseconds, AudioAttributes attributes) {
        try {
            vibrate(VibrationEffect.createOneShot(milliseconds, -1), attributes);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to create VibrationEffect", iae);
        }
    }

    @Deprecated
    public void vibrate(long[] pattern, int repeat) {
        vibrate(pattern, repeat, null);
    }

    @Deprecated
    public void vibrate(long[] pattern, int repeat, AudioAttributes attributes) {
        if (repeat < -1 || repeat >= pattern.length) {
            Log.e(TAG, "vibrate called with repeat index out of bounds (pattern.length=" + pattern.length + ", index=" + repeat + ")");
            throw new ArrayIndexOutOfBoundsException();
        }
        try {
            vibrate(VibrationEffect.createWaveform(pattern, repeat), attributes);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to create VibrationEffect", iae);
        }
    }

    public void vibrate(VibrationEffect vibe) {
        vibrate(vibe, (AudioAttributes) null);
    }

    public void vibrate(VibrationEffect vibe, AudioAttributes attributes) {
        vibrate(Process.myUid(), this.mPackageName, vibe, null, attributes);
    }
}
