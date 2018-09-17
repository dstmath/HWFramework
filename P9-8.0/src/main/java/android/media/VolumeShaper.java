package android.media;

import android.hardware.camera2.params.TonemapCurve;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;

public final class VolumeShaper implements AutoCloseable {
    private int mId;
    private final WeakReference<PlayerBase> mWeakPlayerBase;

    public static final class Configuration implements Parcelable {
        public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
            public Configuration createFromParcel(Parcel p) {
                int type = p.readInt();
                int id = p.readInt();
                if (type == 0) {
                    return new Configuration(id);
                }
                int optionFlags = p.readInt();
                double durationMs = p.readDouble();
                int interpolatorType = p.readInt();
                float firstSlope = p.readFloat();
                float lastSlope = p.readFloat();
                int length = p.readInt();
                float[] times = new float[length];
                float[] volumes = new float[length];
                for (int i = 0; i < length; i++) {
                    times[i] = p.readFloat();
                    volumes[i] = p.readFloat();
                }
                return new Configuration(type, id, optionFlags, durationMs, interpolatorType, times, volumes, null);
            }

            public Configuration[] newArray(int size) {
                return new Configuration[size];
            }
        };
        public static final Configuration CUBIC_RAMP = new Builder().setInterpolatorType(2).setCurve(new float[]{TonemapCurve.LEVEL_BLACK, 1.0f}, new float[]{TonemapCurve.LEVEL_BLACK, 1.0f}).setDuration(1000).build();
        public static final int INTERPOLATOR_TYPE_CUBIC = 2;
        public static final int INTERPOLATOR_TYPE_CUBIC_MONOTONIC = 3;
        public static final int INTERPOLATOR_TYPE_LINEAR = 1;
        public static final int INTERPOLATOR_TYPE_STEP = 0;
        public static final Configuration LINEAR_RAMP = new Builder().setInterpolatorType(1).setCurve(new float[]{TonemapCurve.LEVEL_BLACK, 1.0f}, new float[]{TonemapCurve.LEVEL_BLACK, 1.0f}).setDuration(1000).build();
        private static final int MAXIMUM_CURVE_POINTS = 16;
        public static final int OPTION_FLAG_CLOCK_TIME = 2;
        private static final int OPTION_FLAG_PUBLIC_ALL = 3;
        public static final int OPTION_FLAG_VOLUME_IN_DBFS = 1;
        public static final Configuration SCURVE_RAMP;
        public static final Configuration SINE_RAMP;
        static final int TYPE_ID = 0;
        static final int TYPE_SCALE = 1;
        private final double mDurationMs;
        private final int mId;
        private final int mInterpolatorType;
        private final int mOptionFlags;
        private final float[] mTimes;
        private final int mType;
        private final float[] mVolumes;

        public static final class Builder {
            private double mDurationMs = 1000.0d;
            private int mId = -1;
            private int mInterpolatorType = 2;
            private int mOptionFlags = 2;
            private float[] mTimes = null;
            private int mType = 1;
            private float[] mVolumes = null;

            public Builder(Configuration configuration) {
                this.mType = configuration.getType();
                this.mId = configuration.getId();
                this.mOptionFlags = configuration.getAllOptionFlags();
                this.mInterpolatorType = configuration.getInterpolatorType();
                this.mDurationMs = (double) configuration.getDuration();
                this.mTimes = (float[]) configuration.getTimes().clone();
                this.mVolumes = (float[]) configuration.getVolumes().clone();
            }

            public Builder setId(int id) {
                if (id < -1) {
                    throw new IllegalArgumentException("invalid id: " + id);
                }
                this.mId = id;
                return this;
            }

            public Builder setInterpolatorType(int interpolatorType) {
                switch (interpolatorType) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        this.mInterpolatorType = interpolatorType;
                        return this;
                    default:
                        throw new IllegalArgumentException("invalid interpolatorType: " + interpolatorType);
                }
            }

            public Builder setOptionFlags(int optionFlags) {
                if ((optionFlags & -4) != 0) {
                    throw new IllegalArgumentException("invalid bits in flag: " + optionFlags);
                }
                this.mOptionFlags = (this.mOptionFlags & -4) | optionFlags;
                return this;
            }

            public Builder setDuration(long durationMillis) {
                if (durationMillis <= 0) {
                    throw new IllegalArgumentException("duration: " + durationMillis + " not positive");
                }
                this.mDurationMs = (double) durationMillis;
                return this;
            }

            public Builder setCurve(float[] times, float[] volumes) {
                Configuration.checkCurveForErrorsAndThrowException(times, volumes, (this.mOptionFlags & 1) != 0, false);
                this.mTimes = (float[]) times.clone();
                this.mVolumes = (float[]) volumes.clone();
                return this;
            }

            public Builder reflectTimes() {
                Configuration.checkCurveForErrorsAndThrowException(this.mTimes, this.mVolumes, (this.mOptionFlags & 1) != 0, true);
                int i = 0;
                while (i < this.mTimes.length / 2) {
                    float temp = this.mTimes[i];
                    this.mTimes[i] = 1.0f - this.mTimes[(this.mTimes.length - 1) - i];
                    this.mTimes[(this.mTimes.length - 1) - i] = 1.0f - temp;
                    temp = this.mVolumes[i];
                    this.mVolumes[i] = this.mVolumes[(this.mVolumes.length - 1) - i];
                    this.mVolumes[(this.mVolumes.length - 1) - i] = temp;
                    i++;
                }
                if ((this.mTimes.length & 1) != 0) {
                    this.mTimes[i] = 1.0f - this.mTimes[i];
                }
                return this;
            }

            public Builder invertVolumes() {
                int i;
                Configuration.checkCurveForErrorsAndThrowException(this.mTimes, this.mVolumes, (this.mOptionFlags & 1) != 0, true);
                float min = this.mVolumes[0];
                float max = this.mVolumes[0];
                for (i = 1; i < this.mVolumes.length; i++) {
                    if (this.mVolumes[i] < min) {
                        min = this.mVolumes[i];
                    } else if (this.mVolumes[i] > max) {
                        max = this.mVolumes[i];
                    }
                }
                float maxmin = max + min;
                for (i = 0; i < this.mVolumes.length; i++) {
                    this.mVolumes[i] = maxmin - this.mVolumes[i];
                }
                return this;
            }

            public Builder scaleToEndVolume(float volume) {
                boolean log = (this.mOptionFlags & 1) != 0;
                Configuration.checkCurveForErrorsAndThrowException(this.mTimes, this.mVolumes, log, true);
                Configuration.checkValidVolumeAndThrowException(volume, log);
                float startVolume = this.mVolumes[0];
                float endVolume = this.mVolumes[this.mVolumes.length - 1];
                int i;
                if (endVolume == startVolume) {
                    float offset = volume - startVolume;
                    for (i = 0; i < this.mVolumes.length; i++) {
                        this.mVolumes[i] = this.mVolumes[i] + (this.mTimes[i] * offset);
                    }
                } else {
                    float scale = (volume - startVolume) / (endVolume - startVolume);
                    for (i = 0; i < this.mVolumes.length; i++) {
                        this.mVolumes[i] = ((this.mVolumes[i] - startVolume) * scale) + startVolume;
                    }
                }
                Configuration.clampVolume(this.mVolumes, log);
                return this;
            }

            public Builder scaleToStartVolume(float volume) {
                boolean log = (this.mOptionFlags & 1) != 0;
                Configuration.checkCurveForErrorsAndThrowException(this.mTimes, this.mVolumes, log, true);
                Configuration.checkValidVolumeAndThrowException(volume, log);
                float startVolume = this.mVolumes[0];
                float endVolume = this.mVolumes[this.mVolumes.length - 1];
                int i;
                if (endVolume == startVolume) {
                    float offset = volume - startVolume;
                    for (i = 0; i < this.mVolumes.length; i++) {
                        this.mVolumes[i] = this.mVolumes[i] + ((1.0f - this.mTimes[i]) * offset);
                    }
                } else {
                    float scale = (volume - endVolume) / (startVolume - endVolume);
                    for (i = 0; i < this.mVolumes.length; i++) {
                        this.mVolumes[i] = ((this.mVolumes[i] - endVolume) * scale) + endVolume;
                    }
                }
                Configuration.clampVolume(this.mVolumes, log);
                return this;
            }

            public Configuration build() {
                Configuration.checkCurveForErrorsAndThrowException(this.mTimes, this.mVolumes, (this.mOptionFlags & 1) != 0, true);
                return new Configuration(this.mType, this.mId, this.mOptionFlags, this.mDurationMs, this.mInterpolatorType, this.mTimes, this.mVolumes, null);
            }
        }

        /* synthetic */ Configuration(int type, int id, int optionFlags, double durationMs, int interpolatorType, float[] times, float[] volumes, Configuration -this7) {
            this(type, id, optionFlags, durationMs, interpolatorType, times, volumes);
        }

        public static int getMaximumCurvePoints() {
            return 16;
        }

        static {
            float[] times = new float[16];
            float[] sines = new float[16];
            float[] scurve = new float[16];
            for (int i = 0; i < 16; i++) {
                times[i] = ((float) i) / 15.0f;
                float sine = (float) Math.sin((((double) times[i]) * 3.141592653589793d) / 2.0d);
                sines[i] = sine;
                scurve[i] = sine * sine;
            }
            SINE_RAMP = new Builder().setInterpolatorType(2).setCurve(times, sines).setDuration(1000).build();
            SCURVE_RAMP = new Builder().setInterpolatorType(2).setCurve(times, scurve).setDuration(1000).build();
        }

        public String toString() {
            String str;
            StringBuilder append = new StringBuilder().append("VolumeShaper.Configuration{mType = ").append(this.mType).append(", mId = ").append(this.mId);
            if (this.mType == 0) {
                str = "}";
            } else {
                str = ", mOptionFlags = 0x" + Integer.toHexString(this.mOptionFlags).toUpperCase() + ", mDurationMs = " + this.mDurationMs + ", mInterpolatorType = " + this.mInterpolatorType + ", mTimes[] = " + Arrays.toString(this.mTimes) + ", mVolumes[] = " + Arrays.toString(this.mVolumes) + "}";
            }
            return append.append(str).toString();
        }

        public int hashCode() {
            if (this.mType == 0) {
                return Objects.hash(new Object[]{Integer.valueOf(this.mType), Integer.valueOf(this.mId)});
            }
            return Objects.hash(new Object[]{Integer.valueOf(this.mType), Integer.valueOf(this.mId), Integer.valueOf(this.mOptionFlags), Double.valueOf(this.mDurationMs), Integer.valueOf(this.mInterpolatorType), Integer.valueOf(Arrays.hashCode(this.mTimes)), Integer.valueOf(Arrays.hashCode(this.mVolumes))});
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (!(o instanceof Configuration)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            Configuration other = (Configuration) o;
            if (this.mType != other.mType || this.mId != other.mId) {
                z = false;
            } else if (this.mType != 0) {
                z = (this.mOptionFlags == other.mOptionFlags && this.mDurationMs == other.mDurationMs && this.mInterpolatorType == other.mInterpolatorType && Arrays.equals(this.mTimes, other.mTimes)) ? Arrays.equals(this.mVolumes, other.mVolumes) : false;
            }
            return z;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mType);
            dest.writeInt(this.mId);
            if (this.mType != 0) {
                dest.writeInt(this.mOptionFlags);
                dest.writeDouble(this.mDurationMs);
                dest.writeInt(this.mInterpolatorType);
                dest.writeFloat(TonemapCurve.LEVEL_BLACK);
                dest.writeFloat(TonemapCurve.LEVEL_BLACK);
                dest.writeInt(this.mTimes.length);
                for (int i = 0; i < this.mTimes.length; i++) {
                    dest.writeFloat(this.mTimes[i]);
                    dest.writeFloat(this.mVolumes[i]);
                }
            }
        }

        public Configuration(int id) {
            if (id < 0) {
                throw new IllegalArgumentException("negative id " + id);
            }
            this.mType = 0;
            this.mId = id;
            this.mInterpolatorType = 0;
            this.mOptionFlags = 0;
            this.mDurationMs = 0.0d;
            this.mTimes = null;
            this.mVolumes = null;
        }

        private Configuration(int type, int id, int optionFlags, double durationMs, int interpolatorType, float[] times, float[] volumes) {
            this.mType = type;
            this.mId = id;
            this.mOptionFlags = optionFlags;
            this.mDurationMs = durationMs;
            this.mInterpolatorType = interpolatorType;
            this.mTimes = times;
            this.mVolumes = volumes;
        }

        public int getType() {
            return this.mType;
        }

        public int getId() {
            return this.mId;
        }

        public int getInterpolatorType() {
            return this.mInterpolatorType;
        }

        public int getOptionFlags() {
            return this.mOptionFlags & 3;
        }

        int getAllOptionFlags() {
            return this.mOptionFlags;
        }

        public long getDuration() {
            return (long) this.mDurationMs;
        }

        public float[] getTimes() {
            return this.mTimes;
        }

        public float[] getVolumes() {
            return this.mVolumes;
        }

        private static String checkCurveForErrors(float[] times, float[] volumes, boolean log) {
            if (times == null) {
                return "times array must be non-null";
            }
            if (volumes == null) {
                return "volumes array must be non-null";
            }
            if (times.length != volumes.length) {
                return "array length must match";
            }
            if (times.length < 2) {
                return "array length must be at least 2";
            }
            if (times.length > 16) {
                return "array length must be no larger than 16";
            }
            if (times[0] != TonemapCurve.LEVEL_BLACK) {
                return "times must start at 0.f";
            }
            if (times[times.length - 1] != 1.0f) {
                return "times must end at 1.f";
            }
            int i;
            int i2;
            for (i = 1; i < times.length; i++) {
                if (times[i] > times[i - 1]) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                if (i2 == 0) {
                    return "times not monotonic increasing, check index " + i;
                }
            }
            if (log) {
                for (i = 0; i < volumes.length; i++) {
                    if (volumes[i] <= TonemapCurve.LEVEL_BLACK) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    if (i2 == 0) {
                        return "volumes for log scale cannot be positive, check index " + i;
                    }
                }
            } else {
                i = 0;
                while (i < volumes.length) {
                    if (volumes[i] >= TonemapCurve.LEVEL_BLACK) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    if (i2 != 0) {
                        if (((volumes[i] <= 1.0f ? 1 : 0) ^ 1) == 0) {
                            i++;
                        }
                    }
                    return "volumes for linear scale must be between 0.f and 1.f, check index " + i;
                }
            }
            return null;
        }

        private static void checkCurveForErrorsAndThrowException(float[] times, float[] volumes, boolean log, boolean ise) {
            String error = checkCurveForErrors(times, volumes, log);
            if (error == null) {
                return;
            }
            if (ise) {
                throw new IllegalStateException(error);
            }
            throw new IllegalArgumentException(error);
        }

        private static void checkValidVolumeAndThrowException(float volume, boolean log) {
            int i = 1;
            if (log) {
                if (volume > TonemapCurve.LEVEL_BLACK) {
                    i = 0;
                }
                if (i == 0) {
                    throw new IllegalArgumentException("dbfs volume must be 0.f or less");
                }
                return;
            }
            int i2;
            if (volume >= TonemapCurve.LEVEL_BLACK) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            if (i2 != 0) {
                if (volume > 1.0f) {
                    i = 0;
                }
                if ((i ^ 1) == 0) {
                    return;
                }
            }
            throw new IllegalArgumentException("volume must be >= 0.f and <= 1.f");
        }

        private static void clampVolume(float[] volumes, boolean log) {
            int i;
            Object obj;
            if (log) {
                for (i = 0; i < volumes.length; i++) {
                    if (volumes[i] <= TonemapCurve.LEVEL_BLACK) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        volumes[i] = TonemapCurve.LEVEL_BLACK;
                    }
                }
                return;
            }
            for (i = 0; i < volumes.length; i++) {
                if (volumes[i] >= TonemapCurve.LEVEL_BLACK) {
                    obj = 1;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    volumes[i] = TonemapCurve.LEVEL_BLACK;
                } else {
                    if (volumes[i] <= 1.0f) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        volumes[i] = 1.0f;
                    }
                }
            }
        }
    }

    public static final class Operation implements Parcelable {
        public static final Creator<Operation> CREATOR = new Creator<Operation>() {
            public Operation createFromParcel(Parcel p) {
                return new Operation(p.readInt(), p.readInt(), p.readFloat(), null);
            }

            public Operation[] newArray(int size) {
                return new Operation[size];
            }
        };
        private static final int FLAG_CREATE_IF_NEEDED = 16;
        private static final int FLAG_DEFER = 8;
        private static final int FLAG_JOIN = 4;
        private static final int FLAG_NONE = 0;
        private static final int FLAG_PUBLIC_ALL = 3;
        private static final int FLAG_REVERSE = 1;
        private static final int FLAG_TERMINATE = 2;
        public static final Operation PLAY = new Builder().build();
        public static final Operation REVERSE = new Builder().reverse().build();
        private final int mFlags;
        private final int mReplaceId;
        private final float mXOffset;

        public static final class Builder {
            int mFlags;
            int mReplaceId;
            float mXOffset;

            public Builder() {
                this.mFlags = 0;
                this.mReplaceId = -1;
                this.mXOffset = Float.NaN;
            }

            public Builder(Operation operation) {
                this.mReplaceId = operation.mReplaceId;
                this.mFlags = operation.mFlags;
                this.mXOffset = operation.mXOffset;
            }

            public Builder replace(int id, boolean join) {
                this.mReplaceId = id;
                if (join) {
                    this.mFlags |= 4;
                } else {
                    this.mFlags &= -5;
                }
                return this;
            }

            public Builder defer() {
                this.mFlags |= 8;
                return this;
            }

            public Builder terminate() {
                this.mFlags |= 2;
                return this;
            }

            public Builder reverse() {
                this.mFlags ^= 1;
                return this;
            }

            public Builder createIfNeeded() {
                this.mFlags |= 16;
                return this;
            }

            public Builder setXOffset(float xOffset) {
                if (xOffset < -0.0f) {
                    throw new IllegalArgumentException("Negative xOffset not allowed");
                } else if (xOffset > 1.0f) {
                    throw new IllegalArgumentException("xOffset > 1.f not allowed");
                } else {
                    this.mXOffset = xOffset;
                    return this;
                }
            }

            private Builder setFlags(int flags) {
                if ((flags & -4) != 0) {
                    throw new IllegalArgumentException("flag has unknown bits set: " + flags);
                }
                this.mFlags = (this.mFlags & -4) | flags;
                return this;
            }

            public Operation build() {
                return new Operation(this.mFlags, this.mReplaceId, this.mXOffset, null);
            }
        }

        /* synthetic */ Operation(int flags, int replaceId, float xOffset, Operation -this3) {
            this(flags, replaceId, xOffset);
        }

        public String toString() {
            return "VolumeShaper.Operation{mFlags = 0x" + Integer.toHexString(this.mFlags).toUpperCase() + ", mReplaceId = " + this.mReplaceId + ", mXOffset = " + this.mXOffset + "}";
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Integer.valueOf(this.mFlags), Integer.valueOf(this.mReplaceId), Float.valueOf(this.mXOffset)});
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (!(o instanceof Operation)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            Operation other = (Operation) o;
            if (this.mFlags != other.mFlags || this.mReplaceId != other.mReplaceId) {
                z = false;
            } else if (Float.compare(this.mXOffset, other.mXOffset) != 0) {
                z = false;
            }
            return z;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mFlags);
            dest.writeInt(this.mReplaceId);
            dest.writeFloat(this.mXOffset);
        }

        private Operation(int flags, int replaceId, float xOffset) {
            this.mFlags = flags;
            this.mReplaceId = replaceId;
            this.mXOffset = xOffset;
        }
    }

    public static final class State implements Parcelable {
        public static final Creator<State> CREATOR = new Creator<State>() {
            public State createFromParcel(Parcel p) {
                return new State(p.readFloat(), p.readFloat());
            }

            public State[] newArray(int size) {
                return new State[size];
            }
        };
        private float mVolume;
        private float mXOffset;

        public String toString() {
            return "VolumeShaper.State{mVolume = " + this.mVolume + ", mXOffset = " + this.mXOffset + "}";
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Float.valueOf(this.mVolume), Float.valueOf(this.mXOffset)});
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (!(o instanceof State)) {
                return false;
            }
            if (o == this) {
                return true;
            }
            State other = (State) o;
            if (this.mVolume != other.mVolume) {
                z = false;
            } else if (this.mXOffset != other.mXOffset) {
                z = false;
            }
            return z;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(this.mVolume);
            dest.writeFloat(this.mXOffset);
        }

        State(float volume, float xOffset) {
            this.mVolume = volume;
            this.mXOffset = xOffset;
        }

        public float getVolume() {
            return this.mVolume;
        }

        public float getXOffset() {
            return this.mXOffset;
        }
    }

    VolumeShaper(Configuration configuration, PlayerBase playerBase) {
        this.mWeakPlayerBase = new WeakReference(playerBase);
        this.mId = applyPlayer(configuration, new Builder().defer().build());
    }

    int getId() {
        return this.mId;
    }

    public void apply(Operation operation) {
        applyPlayer(new Configuration(this.mId), operation);
    }

    public void replace(Configuration configuration, Operation operation, boolean join) {
        this.mId = applyPlayer(configuration, new Builder(operation).replace(this.mId, join).build());
    }

    public float getVolume() {
        return getStatePlayer(this.mId).getVolume();
    }

    public void close() {
        try {
            applyPlayer(new Configuration(this.mId), new Builder().terminate().build());
        } catch (IllegalStateException e) {
        }
        if (this.mWeakPlayerBase != null) {
            this.mWeakPlayerBase.clear();
        }
    }

    protected void finalize() {
        close();
    }

    private int applyPlayer(Configuration configuration, Operation operation) {
        if (this.mWeakPlayerBase != null) {
            PlayerBase player = (PlayerBase) this.mWeakPlayerBase.get();
            if (player == null) {
                throw new IllegalStateException("player deallocated");
            }
            int id = player.playerApplyVolumeShaper(configuration, operation);
            if (id >= 0) {
                return id;
            }
            if (id == -38) {
                throw new IllegalStateException("player or VolumeShaper deallocated");
            }
            throw new IllegalArgumentException("invalid configuration or operation: " + id);
        }
        throw new IllegalStateException("uninitialized shaper");
    }

    private State getStatePlayer(int id) {
        if (this.mWeakPlayerBase != null) {
            PlayerBase player = (PlayerBase) this.mWeakPlayerBase.get();
            if (player == null) {
                throw new IllegalStateException("player deallocated");
            }
            State state = player.playerGetVolumeShaperState(id);
            if (state != null) {
                return state;
            }
            throw new IllegalStateException("shaper cannot be found");
        }
        throw new IllegalStateException("uninitialized shaper");
    }
}
