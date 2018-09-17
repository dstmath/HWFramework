package android.os;

import android.os.Parcelable.Creator;
import java.util.Arrays;

public abstract class VibrationEffect implements Parcelable {
    public static final Creator<VibrationEffect> CREATOR = new Creator<VibrationEffect>() {
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

        public VibrationEffect[] newArray(int size) {
            return new VibrationEffect[size];
        }
    };
    public static final int DEFAULT_AMPLITUDE = -1;
    public static final int EFFECT_CLICK = 0;
    public static final int EFFECT_DOUBLE_CLICK = 1;
    private static final int PARCEL_TOKEN_EFFECT = 3;
    private static final int PARCEL_TOKEN_ONE_SHOT = 1;
    private static final int PARCEL_TOKEN_WAVEFORM = 2;

    public static class OneShot extends VibrationEffect implements Parcelable {
        public static final Creator<OneShot> CREATOR = new Creator<OneShot>() {
            public OneShot createFromParcel(Parcel in) {
                in.readInt();
                return new OneShot(in);
            }

            public OneShot[] newArray(int size) {
                return new OneShot[size];
            }
        };
        private int mAmplitude;
        private long mTiming;

        public OneShot(Parcel in) {
            this(in.readLong(), in.readInt());
        }

        public OneShot(long milliseconds, int amplitude) {
            this.mTiming = milliseconds;
            this.mAmplitude = amplitude;
        }

        public long getTiming() {
            return this.mTiming;
        }

        public int getAmplitude() {
            return this.mAmplitude;
        }

        public void validate() {
            if (this.mAmplitude < -1 || this.mAmplitude == 0 || this.mAmplitude > 255) {
                throw new IllegalArgumentException("amplitude must either be DEFAULT_AMPLITUDE, or between 1 and 255 inclusive (amplitude=" + this.mAmplitude + ")");
            } else if (this.mTiming <= 0) {
                throw new IllegalArgumentException("timing must be positive (timing=" + this.mTiming + ")");
            }
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof OneShot)) {
                return false;
            }
            OneShot other = (OneShot) o;
            if (other.mTiming == this.mTiming && other.mAmplitude == this.mAmplitude) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            int result = ((int) this.mTiming) * 37;
            return this.mAmplitude * 37;
        }

        public String toString() {
            return "OneShot{mTiming=" + this.mTiming + ", mAmplitude=" + this.mAmplitude + "}";
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(1);
            out.writeLong(this.mTiming);
            out.writeInt(this.mAmplitude);
        }
    }

    public static class Prebaked extends VibrationEffect implements Parcelable {
        public static final Creator<Prebaked> CREATOR = new Creator<Prebaked>() {
            public Prebaked createFromParcel(Parcel in) {
                in.readInt();
                return new Prebaked(in);
            }

            public Prebaked[] newArray(int size) {
                return new Prebaked[size];
            }
        };
        private int mEffectId;

        public Prebaked(Parcel in) {
            this(in.readInt());
        }

        public Prebaked(int effectId) {
            this.mEffectId = effectId;
        }

        public int getId() {
            return this.mEffectId;
        }

        public void validate() {
            if (this.mEffectId != 0) {
                throw new IllegalArgumentException("Unknown prebaked effect type (value=" + this.mEffectId + ")");
            }
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Prebaked)) {
                return false;
            }
            if (this.mEffectId == ((Prebaked) o).mEffectId) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.mEffectId;
        }

        public String toString() {
            return "Prebaked{mEffectId=" + this.mEffectId + "}";
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(3);
            out.writeInt(this.mEffectId);
        }
    }

    public static class Waveform extends VibrationEffect implements Parcelable {
        public static final Creator<Waveform> CREATOR = new Creator<Waveform>() {
            public Waveform createFromParcel(Parcel in) {
                in.readInt();
                return new Waveform(in);
            }

            public Waveform[] newArray(int size) {
                return new Waveform[size];
            }
        };
        private int[] mAmplitudes;
        private int mRepeat;
        private long[] mTimings;

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

        public void validate() {
            int i = 0;
            if (this.mTimings.length != this.mAmplitudes.length) {
                throw new IllegalArgumentException("timing and amplitude arrays must be of equal length (timings.length=" + this.mTimings.length + ", amplitudes.length=" + this.mAmplitudes.length + ")");
            } else if (hasNonZeroEntry(this.mTimings)) {
                for (long timing : this.mTimings) {
                    if (timing < 0) {
                        throw new IllegalArgumentException("timings must all be >= 0 (timings=" + Arrays.toString(this.mTimings) + ")");
                    }
                }
                int[] iArr = this.mAmplitudes;
                int length = iArr.length;
                while (i < length) {
                    int amplitude = iArr[i];
                    if (amplitude < -1 || amplitude > 255) {
                        throw new IllegalArgumentException("amplitudes must all be DEFAULT_AMPLITUDE or between 0 and 255 (amplitudes=" + Arrays.toString(this.mAmplitudes) + ")");
                    }
                    i++;
                }
                if (this.mRepeat < -1 || this.mRepeat >= this.mTimings.length) {
                    throw new IllegalArgumentException("repeat index must be within the bounds of the timings array (timings.length=" + this.mTimings.length + ", index=" + this.mRepeat + ")");
                }
            } else {
                throw new IllegalArgumentException("at least one timing must be non-zero (timings=" + Arrays.toString(this.mTimings) + ")");
            }
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Waveform)) {
                return false;
            }
            Waveform other = (Waveform) o;
            if (Arrays.equals(this.mTimings, other.mTimings) && Arrays.equals(this.mAmplitudes, other.mAmplitudes) && this.mRepeat == other.mRepeat) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            int result = Arrays.hashCode(this.mTimings) * 37;
            result = Arrays.hashCode(this.mAmplitudes) * 37;
            return this.mRepeat * 37;
        }

        public String toString() {
            return "Waveform{mTimings=" + Arrays.toString(this.mTimings) + ", mAmplitudes=" + Arrays.toString(this.mAmplitudes) + ", mRepeat=" + this.mRepeat + "}";
        }

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

    public static VibrationEffect get(int effectId) {
        VibrationEffect effect = new Prebaked(effectId);
        effect.validate();
        return effect;
    }

    public int describeContents() {
        return 0;
    }
}
