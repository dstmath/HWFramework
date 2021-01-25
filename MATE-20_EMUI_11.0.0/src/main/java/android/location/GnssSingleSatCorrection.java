package android.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.Preconditions;

@SystemApi
public final class GnssSingleSatCorrection implements Parcelable {
    public static final Parcelable.Creator<GnssSingleSatCorrection> CREATOR = new Parcelable.Creator<GnssSingleSatCorrection>() {
        /* class android.location.GnssSingleSatCorrection.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GnssSingleSatCorrection createFromParcel(Parcel parcel) {
            boolean hasReflectingPlane = (parcel.readInt() & 8) != 0;
            Builder singleSatCorrectionBuilder = new Builder().setConstellationType(parcel.readInt()).setSatelliteId(parcel.readInt()).setCarrierFrequencyHz(parcel.readFloat()).setProbabilityLineOfSight(parcel.readFloat()).setExcessPathLengthMeters(parcel.readFloat()).setExcessPathLengthUncertaintyMeters(parcel.readFloat());
            if (hasReflectingPlane) {
                singleSatCorrectionBuilder.setReflectingPlane(GnssReflectingPlane.CREATOR.createFromParcel(parcel));
            }
            return singleSatCorrectionBuilder.build();
        }

        @Override // android.os.Parcelable.Creator
        public GnssSingleSatCorrection[] newArray(int i) {
            return new GnssSingleSatCorrection[i];
        }
    };
    public static final int HAS_EXCESS_PATH_LENGTH_MASK = 2;
    public static final int HAS_EXCESS_PATH_LENGTH_UNC_MASK = 4;
    public static final int HAS_PROB_SAT_IS_LOS_MASK = 1;
    public static final int HAS_REFLECTING_PLANE_MASK = 8;
    private final float mCarrierFrequencyHz;
    private final int mConstellationType;
    private final float mExcessPathLengthMeters;
    private final float mExcessPathLengthUncertaintyMeters;
    private final float mProbSatIsLos;
    private final GnssReflectingPlane mReflectingPlane;
    private final int mSatId;
    private final int mSingleSatCorrectionFlags;

    private GnssSingleSatCorrection(Builder builder) {
        this.mSingleSatCorrectionFlags = builder.mSingleSatCorrectionFlags;
        this.mSatId = builder.mSatId;
        this.mConstellationType = builder.mConstellationType;
        this.mCarrierFrequencyHz = builder.mCarrierFrequencyHz;
        this.mProbSatIsLos = builder.mProbSatIsLos;
        this.mExcessPathLengthMeters = builder.mExcessPathLengthMeters;
        this.mExcessPathLengthUncertaintyMeters = builder.mExcessPathLengthUncertaintyMeters;
        this.mReflectingPlane = builder.mReflectingPlane;
    }

    public int getSingleSatelliteCorrectionFlags() {
        return this.mSingleSatCorrectionFlags;
    }

    public int getConstellationType() {
        return this.mConstellationType;
    }

    public int getSatelliteId() {
        return this.mSatId;
    }

    public float getCarrierFrequencyHz() {
        return this.mCarrierFrequencyHz;
    }

    public float getProbabilityLineOfSight() {
        return this.mProbSatIsLos;
    }

    public float getExcessPathLengthMeters() {
        return this.mExcessPathLengthMeters;
    }

    public float getExcessPathLengthUncertaintyMeters() {
        return this.mExcessPathLengthUncertaintyMeters;
    }

    public GnssReflectingPlane getReflectingPlane() {
        return this.mReflectingPlane;
    }

    public boolean hasValidSatelliteLineOfSight() {
        return (this.mSingleSatCorrectionFlags & 1) != 0;
    }

    public boolean hasExcessPathLength() {
        return (this.mSingleSatCorrectionFlags & 2) != 0;
    }

    public boolean hasExcessPathLengthUncertainty() {
        return (this.mSingleSatCorrectionFlags & 4) != 0;
    }

    public boolean hasReflectingPlane() {
        return (this.mSingleSatCorrectionFlags & 8) != 0;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("GnssSingleSatCorrection:\n");
        builder.append(String.format("   %-29s = %s\n", "SingleSatCorrectionFlags = ", Integer.valueOf(this.mSingleSatCorrectionFlags)));
        builder.append(String.format("   %-29s = %s\n", "ConstellationType = ", Integer.valueOf(this.mConstellationType)));
        builder.append(String.format("   %-29s = %s\n", "SatId = ", Integer.valueOf(this.mSatId)));
        builder.append(String.format("   %-29s = %s\n", "CarrierFrequencyHz = ", Float.valueOf(this.mCarrierFrequencyHz)));
        builder.append(String.format("   %-29s = %s\n", "ProbSatIsLos = ", Float.valueOf(this.mProbSatIsLos)));
        builder.append(String.format("   %-29s = %s\n", "ExcessPathLengthMeters = ", Float.valueOf(this.mExcessPathLengthMeters)));
        builder.append(String.format("   %-29s = %s\n", "ExcessPathLengthUncertaintyMeters = ", Float.valueOf(this.mExcessPathLengthUncertaintyMeters)));
        if (hasReflectingPlane()) {
            builder.append(String.format("   %-29s = %s\n", "ReflectingPlane = ", this.mReflectingPlane));
        }
        return builder.toString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mSingleSatCorrectionFlags);
        parcel.writeInt(this.mConstellationType);
        parcel.writeInt(this.mSatId);
        parcel.writeFloat(this.mCarrierFrequencyHz);
        parcel.writeFloat(this.mProbSatIsLos);
        parcel.writeFloat(this.mExcessPathLengthMeters);
        parcel.writeFloat(this.mExcessPathLengthUncertaintyMeters);
        if (hasReflectingPlane()) {
            this.mReflectingPlane.writeToParcel(parcel, flags);
        }
    }

    public static final class Builder {
        private float mCarrierFrequencyHz;
        private int mConstellationType;
        private float mExcessPathLengthMeters;
        private float mExcessPathLengthUncertaintyMeters;
        private float mProbSatIsLos;
        private GnssReflectingPlane mReflectingPlane;
        private int mSatId;
        private int mSingleSatCorrectionFlags;

        public Builder setConstellationType(int constellationType) {
            this.mConstellationType = constellationType;
            return this;
        }

        public Builder setSatelliteId(int satId) {
            this.mSatId = satId;
            return this;
        }

        public Builder setCarrierFrequencyHz(float carrierFrequencyHz) {
            this.mCarrierFrequencyHz = carrierFrequencyHz;
            return this;
        }

        public Builder setProbabilityLineOfSight(float probSatIsLos) {
            Preconditions.checkArgumentInRange(probSatIsLos, 0.0f, 1.0f, "probSatIsLos should be between 0 and 1.");
            this.mProbSatIsLos = probSatIsLos;
            this.mSingleSatCorrectionFlags = (byte) (this.mSingleSatCorrectionFlags | 1);
            return this;
        }

        public Builder setExcessPathLengthMeters(float excessPathLengthMeters) {
            this.mExcessPathLengthMeters = excessPathLengthMeters;
            this.mSingleSatCorrectionFlags = (byte) (this.mSingleSatCorrectionFlags | 2);
            return this;
        }

        public Builder setExcessPathLengthUncertaintyMeters(float excessPathLengthUncertaintyMeters) {
            this.mExcessPathLengthUncertaintyMeters = excessPathLengthUncertaintyMeters;
            this.mSingleSatCorrectionFlags = (byte) (this.mSingleSatCorrectionFlags | 4);
            return this;
        }

        public Builder setReflectingPlane(GnssReflectingPlane reflectingPlane) {
            this.mReflectingPlane = reflectingPlane;
            if (reflectingPlane != null) {
                this.mSingleSatCorrectionFlags = (byte) (this.mSingleSatCorrectionFlags | 8);
            } else {
                this.mSingleSatCorrectionFlags = (byte) (this.mSingleSatCorrectionFlags & -9);
            }
            return this;
        }

        public GnssSingleSatCorrection build() {
            return new GnssSingleSatCorrection(this);
        }
    }
}
