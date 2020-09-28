package android.os;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.util.MathUtils;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public abstract class VibrationEffect implements Parcelable {
    public static final Parcelable.Creator<VibrationEffect> CREATOR = new Parcelable.Creator<VibrationEffect>() {
        /* class android.os.VibrationEffect.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VibrationEffect createFromParcel(Parcel in) {
            int token = in.readInt();
            if (token == 1) {
                return new OneShot(in);
            }
            if (token == 2) {
                return new Waveform(in);
            }
            if (token == 3) {
                return new Prebaked(in);
            }
            throw new IllegalStateException("Unexpected vibration event type token in parcel.");
        }

        @Override // android.os.Parcelable.Creator
        public VibrationEffect[] newArray(int size) {
            return new VibrationEffect[size];
        }
    };
    public static final int DEFAULT_AMPLITUDE = -1;
    public static final int EFFECT_CLICK = 0;
    public static final int EFFECT_DOUBLE_CLICK = 1;
    public static final int EFFECT_HEAVY_CLICK = 5;
    public static final int EFFECT_POP = 4;
    public static final int EFFECT_STRENGTH_LIGHT = 0;
    public static final int EFFECT_STRENGTH_MEDIUM = 1;
    public static final int EFFECT_STRENGTH_STRONG = 2;
    public static final int EFFECT_TEXTURE_TICK = 21;
    public static final int EFFECT_THUD = 3;
    public static final int EFFECT_TICK = 2;
    public static final int MAX_AMPLITUDE = 255;
    private static final int PARCEL_TOKEN_EFFECT = 3;
    private static final int PARCEL_TOKEN_ONE_SHOT = 1;
    private static final int PARCEL_TOKEN_WAVEFORM = 2;
    public static final int[] RINGTONES = {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

    @Retention(RetentionPolicy.SOURCE)
    public @interface EffectType {
    }

    public abstract long getDuration();

    public abstract void validate();

    public static VibrationEffect createOneShot(long milliseconds, int amplitude) {
        VibrationEffect effect = new OneShot(milliseconds, amplitude);
        effect.validate();
        return effect;
    }

    public static VibrationEffect createWaveform(long[] timings, int repeat) {
        int[] amplitudes = new int[timings.length];
        for (int i = 0; i < timings.length / 2; i++) {
            amplitudes[(i * 2) + 1] = -1;
        }
        return createWaveform(timings, amplitudes, repeat);
    }

    public static VibrationEffect createWaveform(long[] timings, int[] amplitudes, int repeat) {
        VibrationEffect effect = new Waveform(timings, amplitudes, repeat);
        effect.validate();
        return effect;
    }

    public static VibrationEffect createPredefined(int effectId) {
        return get(effectId, true);
    }

    public static VibrationEffect get(int effectId) {
        return get(effectId, true);
    }

    public static VibrationEffect get(int effectId, boolean fallback) {
        VibrationEffect effect = new Prebaked(effectId, fallback);
        effect.validate();
        return effect;
    }

    public static VibrationEffect get(Uri uri, Context context) {
        Uri mappedUri;
        ContentResolver cr = context.getContentResolver();
        Uri uncanonicalUri = cr.uncanonicalize(uri);
        if (uncanonicalUri == null) {
            uncanonicalUri = uri;
        }
        String[] uris = context.getResources().getStringArray(R.array.config_ringtoneEffectUris);
        int i = 0;
        while (i < uris.length && i < RINGTONES.length) {
            if (uris[i] != null && (mappedUri = cr.uncanonicalize(Uri.parse(uris[i]))) != null && mappedUri.equals(uncanonicalUri)) {
                return get(RINGTONES[i]);
            }
            i++;
        }
        return null;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    protected static int scale(int amplitude, float gamma, int maxAmplitude) {
        return (int) (((float) maxAmplitude) * MathUtils.pow(((float) amplitude) / 255.0f, gamma));
    }

    public static class OneShot extends VibrationEffect implements Parcelable {
        public static final Parcelable.Creator<OneShot> CREATOR = new Parcelable.Creator<OneShot>() {
            /* class android.os.VibrationEffect.OneShot.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public OneShot createFromParcel(Parcel in) {
                in.readInt();
                return new OneShot(in);
            }

            @Override // android.os.Parcelable.Creator
            public OneShot[] newArray(int size) {
                return new OneShot[size];
            }
        };
        private final int mAmplitude;
        private final long mDuration;

        public OneShot(Parcel in) {
            this.mDuration = in.readLong();
            this.mAmplitude = in.readInt();
        }

        public OneShot(long milliseconds, int amplitude) {
            this.mDuration = milliseconds;
            this.mAmplitude = amplitude;
        }

        @Override // android.os.VibrationEffect
        public long getDuration() {
            return this.mDuration;
        }

        public int getAmplitude() {
            return this.mAmplitude;
        }

        public OneShot scale(float gamma, int maxAmplitude) {
            if (maxAmplitude > 255 || maxAmplitude < 0) {
                throw new IllegalArgumentException("Amplitude is negative or greater than MAX_AMPLITUDE");
            }
            return new OneShot(this.mDuration, scale(this.mAmplitude, gamma, maxAmplitude));
        }

        public OneShot resolve(int defaultAmplitude) {
            if (defaultAmplitude > 255 || defaultAmplitude < 0) {
                throw new IllegalArgumentException("Amplitude is negative or greater than MAX_AMPLITUDE");
            } else if (this.mAmplitude == -1) {
                return new OneShot(this.mDuration, defaultAmplitude);
            } else {
                return this;
            }
        }

        @Override // android.os.VibrationEffect
        public void validate() {
            int i = this.mAmplitude;
            if (i < -1 || i == 0 || i > 255) {
                throw new IllegalArgumentException("amplitude must either be DEFAULT_AMPLITUDE, or between 1 and 255 inclusive (amplitude=" + this.mAmplitude + ")");
            } else if (this.mDuration <= 0) {
                throw new IllegalArgumentException("duration must be positive (duration=" + this.mDuration + ")");
            }
        }

        public boolean equals(Object o) {
            if (!(o instanceof OneShot)) {
                return false;
            }
            OneShot other = (OneShot) o;
            if (other.mDuration == this.mDuration && other.mAmplitude == this.mAmplitude) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return 17 + (((int) this.mDuration) * 37) + (this.mAmplitude * 37);
        }

        public String toString() {
            return "OneShot{mDuration=" + this.mDuration + ", mAmplitude=" + this.mAmplitude + "}";
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(1);
            out.writeLong(this.mDuration);
            out.writeInt(this.mAmplitude);
        }
    }

    public static class Waveform extends VibrationEffect implements Parcelable {
        public static final Parcelable.Creator<Waveform> CREATOR = new Parcelable.Creator<Waveform>() {
            /* class android.os.VibrationEffect.Waveform.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Waveform createFromParcel(Parcel in) {
                in.readInt();
                return new Waveform(in);
            }

            @Override // android.os.Parcelable.Creator
            public Waveform[] newArray(int size) {
                return new Waveform[size];
            }
        };
        private final int[] mAmplitudes;
        private final int mRepeat;
        private final long[] mTimings;

        public Waveform(Parcel in) {
            this(in.createLongArray(), in.createIntArray(), in.readInt());
        }

        public Waveform(long[] timings, int[] amplitudes, int repeat) {
            this.mTimings = new long[timings.length];
            System.arraycopy(timings, 0, this.mTimings, 0, timings.length);
            this.mAmplitudes = new int[amplitudes.length];
            System.arraycopy(amplitudes, 0, this.mAmplitudes, 0, amplitudes.length);
            this.mRepeat = repeat;
        }

        public long[] getTimings() {
            return this.mTimings;
        }

        public int[] getAmplitudes() {
            return this.mAmplitudes;
        }

        public int getRepeatIndex() {
            return this.mRepeat;
        }

        @Override // android.os.VibrationEffect
        public long getDuration() {
            if (this.mRepeat >= 0) {
                return Long.MAX_VALUE;
            }
            long duration = 0;
            for (long d : this.mTimings) {
                duration += d;
            }
            return duration;
        }

        public Waveform scale(float gamma, int maxAmplitude) {
            if (maxAmplitude > 255 || maxAmplitude < 0) {
                throw new IllegalArgumentException("Amplitude is negative or greater than MAX_AMPLITUDE");
            } else if (gamma == 1.0f && maxAmplitude == 255) {
                return new Waveform(this.mTimings, this.mAmplitudes, this.mRepeat);
            } else {
                int[] iArr = this.mAmplitudes;
                int[] scaledAmplitudes = Arrays.copyOf(iArr, iArr.length);
                for (int i = 0; i < scaledAmplitudes.length; i++) {
                    scaledAmplitudes[i] = scale(scaledAmplitudes[i], gamma, maxAmplitude);
                }
                return new Waveform(this.mTimings, scaledAmplitudes, this.mRepeat);
            }
        }

        public Waveform resolve(int defaultAmplitude) {
            if (defaultAmplitude > 255 || defaultAmplitude < 0) {
                throw new IllegalArgumentException("Amplitude is negative or greater than MAX_AMPLITUDE");
            }
            int[] iArr = this.mAmplitudes;
            int[] resolvedAmplitudes = Arrays.copyOf(iArr, iArr.length);
            for (int i = 0; i < resolvedAmplitudes.length; i++) {
                if (resolvedAmplitudes[i] == -1) {
                    resolvedAmplitudes[i] = defaultAmplitude;
                }
            }
            return new Waveform(this.mTimings, resolvedAmplitudes, this.mRepeat);
        }

        @Override // android.os.VibrationEffect
        public void validate() {
            long[] jArr = this.mTimings;
            if (jArr.length != this.mAmplitudes.length) {
                throw new IllegalArgumentException("timing and amplitude arrays must be of equal length (timings.length=" + this.mTimings.length + ", amplitudes.length=" + this.mAmplitudes.length + ")");
            } else if (hasNonZeroEntry(jArr)) {
                for (long timing : this.mTimings) {
                    if (timing < 0) {
                        throw new IllegalArgumentException("timings must all be >= 0 (timings=" + Arrays.toString(this.mTimings) + ")");
                    }
                }
                int[] iArr = this.mAmplitudes;
                for (int amplitude : iArr) {
                    if (amplitude < -1 || amplitude > 255) {
                        throw new IllegalArgumentException("amplitudes must all be DEFAULT_AMPLITUDE or between 0 and 255 (amplitudes=" + Arrays.toString(this.mAmplitudes) + ")");
                    }
                }
                int i = this.mRepeat;
                if (i < -1 || i >= this.mTimings.length) {
                    throw new IllegalArgumentException("repeat index must be within the bounds of the timings array (timings.length=" + this.mTimings.length + ", index=" + this.mRepeat + ")");
                }
            } else {
                throw new IllegalArgumentException("at least one timing must be non-zero (timings=" + Arrays.toString(this.mTimings) + ")");
            }
        }

        public boolean equals(Object o) {
            if (!(o instanceof Waveform)) {
                return false;
            }
            Waveform other = (Waveform) o;
            if (!Arrays.equals(this.mTimings, other.mTimings) || !Arrays.equals(this.mAmplitudes, other.mAmplitudes) || this.mRepeat != other.mRepeat) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return 17 + (Arrays.hashCode(this.mTimings) * 37) + (Arrays.hashCode(this.mAmplitudes) * 37) + (this.mRepeat * 37);
        }

        public String toString() {
            return "Waveform{mTimings=" + Arrays.toString(this.mTimings) + ", mAmplitudes=" + Arrays.toString(this.mAmplitudes) + ", mRepeat=" + this.mRepeat + "}";
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(2);
            out.writeLongArray(this.mTimings);
            out.writeIntArray(this.mAmplitudes);
            out.writeInt(this.mRepeat);
        }

        private static boolean hasNonZeroEntry(long[] vals) {
            for (long val : vals) {
                if (val != 0) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class Prebaked extends VibrationEffect implements Parcelable {
        public static final Parcelable.Creator<Prebaked> CREATOR = new Parcelable.Creator<Prebaked>() {
            /* class android.os.VibrationEffect.Prebaked.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Prebaked createFromParcel(Parcel in) {
                in.readInt();
                return new Prebaked(in);
            }

            @Override // android.os.Parcelable.Creator
            public Prebaked[] newArray(int size) {
                return new Prebaked[size];
            }
        };
        private final int mEffectId;
        private int mEffectStrength;
        private final boolean mFallback;

        /* JADX INFO: this call moved to the top of the method (can break code semantics) */
        public Prebaked(Parcel in) {
            this(in.readInt(), in.readByte() != 0);
            this.mEffectStrength = in.readInt();
        }

        public Prebaked(int effectId, boolean fallback) {
            this.mEffectId = effectId;
            this.mFallback = fallback;
            this.mEffectStrength = 1;
        }

        public int getId() {
            return this.mEffectId;
        }

        public boolean shouldFallback() {
            return this.mFallback;
        }

        @Override // android.os.VibrationEffect
        public long getDuration() {
            return -1;
        }

        public void setEffectStrength(int strength) {
            if (isValidEffectStrength(strength)) {
                this.mEffectStrength = strength;
                return;
            }
            throw new IllegalArgumentException("Invalid effect strength: " + strength);
        }

        public int getEffectStrength() {
            return this.mEffectStrength;
        }

        private static boolean isValidEffectStrength(int strength) {
            if (strength == 0 || strength == 1 || strength == 2) {
                return true;
            }
            return false;
        }

        @Override // android.os.VibrationEffect
        public void validate() {
            int i = this.mEffectId;
            if (i != 0 && i != 1 && i != 2 && i != 3 && i != 4 && i != 5 && i != 21 && (i < RINGTONES[0] || this.mEffectId > RINGTONES[RINGTONES.length - 1])) {
                throw new IllegalArgumentException("Unknown prebaked effect type (value=" + this.mEffectId + ")");
            } else if (!isValidEffectStrength(this.mEffectStrength)) {
                throw new IllegalArgumentException("Unknown prebaked effect strength (value=" + this.mEffectStrength + ")");
            }
        }

        public boolean equals(Object o) {
            if (!(o instanceof Prebaked)) {
                return false;
            }
            Prebaked other = (Prebaked) o;
            if (this.mEffectId == other.mEffectId && this.mFallback == other.mFallback && this.mEffectStrength == other.mEffectStrength) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return 17 + (this.mEffectId * 37) + (this.mEffectStrength * 37);
        }

        public String toString() {
            return "Prebaked{mEffectId=" + this.mEffectId + ", mEffectStrength=" + this.mEffectStrength + ", mFallback=" + this.mFallback + "}";
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(3);
            out.writeInt(this.mEffectId);
            out.writeByte(this.mFallback ? (byte) 1 : 0);
            out.writeInt(this.mEffectStrength);
        }
    }
}
