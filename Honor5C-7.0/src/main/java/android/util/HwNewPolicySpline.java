package android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.System;
import huawei.com.android.internal.widget.HwFragmentLayout;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;

public final class HwNewPolicySpline extends Spline {
    private static boolean DEBUG = false;
    private static final String TAG = "HwNewPolicySpline";
    private static ContentResolver mContentResolver = null;
    private static final float maxBrightness = 255.0f;
    private static final float minBrightness = 4.0f;
    private float mDelta;
    private float mDeltaSaved;
    private boolean mIsReboot;
    private boolean mIsUserChange;
    private boolean mIsUserChangeSaved;
    private float mLastLuxDefaultBrightness;
    private float mLastLuxDefaultBrightnessSaved;
    private float mOffsetBrightness_last;
    private float mOffsetBrightness_lastSaved;
    private float mPosBrightness;
    private float mRatio;
    private float mStartLuxDefaultBrightness;
    private float mStartLuxDefaultBrightnessSaved;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.HwNewPolicySpline.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.HwNewPolicySpline.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.HwNewPolicySpline.<clinit>():void");
    }

    private HwNewPolicySpline(Context context) {
        this.mDelta = 0.0f;
        this.mRatio = HwFragmentMenuItemView.ALPHA_NORMAL;
        this.mPosBrightness = minBrightness;
        this.mIsReboot = false;
        this.mIsUserChange = false;
        this.mOffsetBrightness_last = minBrightness;
        this.mLastLuxDefaultBrightness = minBrightness;
        this.mStartLuxDefaultBrightness = minBrightness;
        this.mRatio = HwFragmentMenuItemView.ALPHA_NORMAL;
        this.mIsReboot = true;
        this.mPosBrightness = 0.0f;
        mContentResolver = context.getContentResolver();
        loadOffsetParas();
    }

    public void saveOffsetAlgorithmParas() {
    }

    public void loadOffsetParas() {
        boolean z = true;
        this.mDeltaSaved = System.getFloat(mContentResolver, "spline_delta", 0.0f);
        this.mDelta = this.mDeltaSaved;
        if (System.getInt(mContentResolver, "spline_is_user_change", 0) != 1) {
            z = false;
        }
        this.mIsUserChangeSaved = z;
        this.mIsUserChange = this.mIsUserChangeSaved;
        this.mOffsetBrightness_lastSaved = System.getFloat(mContentResolver, "spline_offset_brightness_last", minBrightness);
        this.mOffsetBrightness_last = this.mOffsetBrightness_lastSaved;
        this.mLastLuxDefaultBrightnessSaved = System.getFloat(mContentResolver, "spline_last_lux_default_brightness", minBrightness);
        this.mLastLuxDefaultBrightness = this.mLastLuxDefaultBrightnessSaved;
        this.mStartLuxDefaultBrightnessSaved = System.getFloat(mContentResolver, "spline_start_lux_default_brightness", minBrightness);
        this.mStartLuxDefaultBrightness = this.mStartLuxDefaultBrightnessSaved;
        if (DEBUG) {
            Slog.d(TAG, "Read:mOffsetBrightness_last=" + this.mOffsetBrightness_last + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + "mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
    }

    private void saveOffsetParas() {
        if (this.mDelta != this.mDeltaSaved) {
            System.putFloat(mContentResolver, "spline_delta", this.mDelta);
            this.mDeltaSaved = this.mDelta;
        }
        if (this.mIsUserChange != this.mIsUserChangeSaved) {
            System.putInt(mContentResolver, "spline_is_user_change", this.mIsUserChange ? 1 : 0);
            this.mIsUserChangeSaved = this.mIsUserChange;
        }
        if (this.mOffsetBrightness_last != this.mOffsetBrightness_lastSaved) {
            System.putFloat(mContentResolver, "spline_offset_brightness_last", this.mOffsetBrightness_last);
            this.mOffsetBrightness_lastSaved = this.mOffsetBrightness_last;
        }
        if (this.mLastLuxDefaultBrightness != this.mLastLuxDefaultBrightnessSaved) {
            System.putFloat(mContentResolver, "spline_last_lux_default_brightness", this.mLastLuxDefaultBrightness);
            this.mLastLuxDefaultBrightnessSaved = this.mLastLuxDefaultBrightness;
        }
        if (this.mStartLuxDefaultBrightness != this.mStartLuxDefaultBrightnessSaved) {
            System.putFloat(mContentResolver, "spline_start_lux_default_brightness", this.mStartLuxDefaultBrightness);
            this.mStartLuxDefaultBrightnessSaved = this.mStartLuxDefaultBrightness;
        }
        if (DEBUG) {
            Slog.d(TAG, "write:mPosBrightness =" + this.mPosBrightness + ",mOffsetBrightness_last=" + this.mOffsetBrightness_last + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + "mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
    }

    public static HwNewPolicySpline createHwNewPolicySpline(Context context) {
        return new HwNewPolicySpline(context);
    }

    public String toString() {
        return new StringBuilder().toString();
    }

    public float interpolate(float x) {
        if (this.mPosBrightness == 0.0f) {
            this.mIsReboot = true;
            this.mDelta = 0.0f;
        } else {
            this.mIsReboot = false;
            this.mDelta = this.mPosBrightness - this.mStartLuxDefaultBrightness;
        }
        if (DEBUG) {
            Slog.d(TAG, "interpolate,mPosBrightness=" + this.mPosBrightness + "lux=" + x + ",mIsReboot=" + this.mIsReboot + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta);
        }
        float value_interp = getInterpolatedValue(this.mPosBrightness, x) / maxBrightness;
        if (this.mPosBrightness != 0.0f) {
            saveOffsetParas();
        }
        return value_interp;
    }

    public void updateLevel(float PosBrightness) {
        if (!this.mIsReboot) {
            this.mIsUserChange = true;
        }
        this.mPosBrightness = PosBrightness;
        if (this.mPosBrightness == 0.0f) {
            this.mDelta = 0.0f;
            this.mOffsetBrightness_last = 0.0f;
            this.mLastLuxDefaultBrightness = 0.0f;
            this.mStartLuxDefaultBrightness = 0.0f;
        }
        saveOffsetParas();
    }

    public float getInterpolatedValue(float PosBrightness, float lux) {
        float offsetBrightness;
        float defaultBrightness = getDefaultBrightnessLevel(lux);
        if (this.mIsReboot) {
            this.mLastLuxDefaultBrightness = defaultBrightness;
            this.mStartLuxDefaultBrightness = defaultBrightness;
            this.mOffsetBrightness_last = defaultBrightness;
            this.mIsReboot = false;
            this.mIsUserChange = false;
        }
        if (this.mIsUserChange) {
            this.mStartLuxDefaultBrightness = this.mLastLuxDefaultBrightness;
        }
        if (this.mStartLuxDefaultBrightness < 100.0f) {
            this.mRatio = HwFragmentMenuItemView.ALPHA_DISABLE;
        } else {
            this.mRatio = HwRippleForegroundImpl.LINEAR_FROM;
        }
        if (Math.abs(defaultBrightness - this.mLastLuxDefaultBrightness) < 1.0E-7f) {
            if (PosBrightness == 0.0f) {
                offsetBrightness = defaultBrightness;
            } else if (this.mIsUserChange) {
                offsetBrightness = PosBrightness;
            } else {
                offsetBrightness = this.mOffsetBrightness_last;
            }
        } else if (PosBrightness == 0.0f) {
            offsetBrightness = defaultBrightness;
        } else {
            offsetBrightness = getOffsetBrightnessLevel_new(this.mStartLuxDefaultBrightness, defaultBrightness, PosBrightness);
        }
        if (DEBUG) {
            Slog.d(TAG, "offsetBrightness=" + offsetBrightness + ",mOffsetBrightness_last" + this.mOffsetBrightness_last + ",lux=" + lux + ",mPosBrightness=" + this.mPosBrightness + ",mIsUserChange=" + this.mIsUserChange + ",mDelta=" + this.mDelta + ",defaultBrightness=" + defaultBrightness + ",mStartLuxDefaultBrightness=" + this.mStartLuxDefaultBrightness + "mLastLuxDefaultBrightness=" + this.mLastLuxDefaultBrightness);
        }
        this.mLastLuxDefaultBrightness = defaultBrightness;
        this.mOffsetBrightness_last = offsetBrightness;
        return offsetBrightness;
    }

    public float getDefaultBrightnessLevel(float lux) {
        if (lux <= 25.0f) {
            return (1.699954f * lux) + minBrightness;
        }
        if (lux <= 1995.0f) {
            return (0.047821f * lux) + 45.30337f;
        }
        if (lux <= 3000.0f) {
            return (0.113724f * lux) - 86.1773f;
        }
        return maxBrightness;
    }

    float getOffsetBrightnessLevel_new(float brightnessStartOrig, float brightnessEndOrig, float brightnessStartNew) {
        boolean brighten = false;
        boolean darken = false;
        if (this.mIsUserChange) {
            this.mDelta = brightnessStartNew - brightnessStartOrig;
            this.mIsUserChange = false;
        }
        float ratio = HwFragmentMenuItemView.ALPHA_NORMAL;
        float ratio2 = HwFragmentMenuItemView.ALPHA_NORMAL;
        float diff = HwFragmentMenuItemView.ALPHA_NORMAL;
        if (brightnessStartOrig < brightnessEndOrig) {
            brighten = true;
            if (this.mDelta > 0.0f) {
                diff = Math.max(Math.abs(brightnessStartOrig - brightnessEndOrig), HwFragmentMenuItemView.ALPHA_NORMAL);
                ratio2 = Math.abs(this.mDelta) / diff;
                ratio2 = Math.max(Math.min((2.5f * ratio2) + HwFragmentMenuItemView.ALPHA_PRESSED, (-1.25f * ratio2) + 1.25f), 0.0f);
            }
            if (this.mDelta < 0.0f) {
                ratio = Math.max(Math.min((-0.02f * brightnessEndOrig) + 3.72f, HwFragmentMenuItemView.ALPHA_NORMAL), 0.0f);
            }
        }
        if (brightnessStartOrig > brightnessEndOrig) {
            darken = true;
            if (this.mDelta < 0.0f) {
                ratio2 = Math.abs(this.mDelta) / Math.max(Math.abs(brightnessStartOrig - brightnessEndOrig), diff);
                ratio2 = Math.max(Math.min((2.5f * ratio2) + HwFragmentMenuItemView.ALPHA_PRESSED, (-1.25f * ratio2) + 1.25f), 0.0f);
            }
            if (this.mDelta > 0.0f) {
                ratio = Math.max(Math.min((0.05f * brightnessEndOrig) - HwFragmentLayout.DISPLAY_RATE_SIXTY_PERCENT, HwFragmentMenuItemView.ALPHA_NORMAL), 0.0f);
            }
        }
        float beta_start = (float) Math.pow((double) (brightnessStartOrig / maxBrightness), (double) this.mRatio);
        float beta_end = (float) Math.pow((double) (brightnessEndOrig / maxBrightness), (double) this.mRatio);
        float delta = (((this.mDelta * beta_end) / beta_start) * ratio2) * ratio;
        if (DEBUG) {
            Slog.d(TAG, "delta=" + delta + ",mDelta=" + this.mDelta + ",mRatio=" + this.mRatio + ",ratio2=" + ratio2 + ",ratio=" + ratio + ",beta_start=" + beta_start + ",beta_end=" + beta_end);
        }
        float offsetBrightness = Math.min(Math.max(brightnessEndOrig + delta, minBrightness), maxBrightness);
        if (brighten && this.mDelta > 0.0f) {
            offsetBrightness = brightnessStartOrig <= 30.0f ? Math.max(brightnessStartNew, brightnessEndOrig) : Math.max(offsetBrightness, brightnessStartNew);
        }
        if (!darken || this.mDelta >= 0.0f) {
            return offsetBrightness;
        }
        return Math.min(offsetBrightness, brightnessStartNew);
    }
}
