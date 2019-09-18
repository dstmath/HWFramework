package android.telephony.ims.stub;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import java.util.Set;

@SystemApi
public final class ImsFeatureConfiguration implements Parcelable {
    public static final Parcelable.Creator<ImsFeatureConfiguration> CREATOR = new Parcelable.Creator<ImsFeatureConfiguration>() {
        public ImsFeatureConfiguration createFromParcel(Parcel in) {
            return new ImsFeatureConfiguration(in);
        }

        public ImsFeatureConfiguration[] newArray(int size) {
            return new ImsFeatureConfiguration[size];
        }
    };
    private final Set<FeatureSlotPair> mFeatures;

    public static class Builder {
        ImsFeatureConfiguration mConfig = new ImsFeatureConfiguration();

        public Builder addFeature(int slotId, int featureType) {
            this.mConfig.addFeature(slotId, featureType);
            return this;
        }

        public ImsFeatureConfiguration build() {
            return this.mConfig;
        }
    }

    public static final class FeatureSlotPair {
        public final int featureType;
        public final int slotId;

        public FeatureSlotPair(int slotId2, int featureType2) {
            this.slotId = slotId2;
            this.featureType = featureType2;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FeatureSlotPair that = (FeatureSlotPair) o;
            if (this.slotId != that.slotId) {
                return false;
            }
            if (this.featureType != that.featureType) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (31 * this.slotId) + this.featureType;
        }

        public String toString() {
            return "{s=" + this.slotId + ", f=" + this.featureType + "}";
        }
    }

    public ImsFeatureConfiguration() {
        this.mFeatures = new ArraySet();
    }

    public ImsFeatureConfiguration(Set<FeatureSlotPair> features) {
        this.mFeatures = new ArraySet();
        if (features != null) {
            this.mFeatures.addAll(features);
        }
    }

    public Set<FeatureSlotPair> getServiceFeatures() {
        return new ArraySet(this.mFeatures);
    }

    /* access modifiers changed from: package-private */
    public void addFeature(int slotId, int feature) {
        this.mFeatures.add(new FeatureSlotPair(slotId, feature));
    }

    protected ImsFeatureConfiguration(Parcel in) {
        int featurePairLength = in.readInt();
        this.mFeatures = new ArraySet(featurePairLength);
        for (int i = 0; i < featurePairLength; i++) {
            this.mFeatures.add(new FeatureSlotPair(in.readInt(), in.readInt()));
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        FeatureSlotPair[] featureSlotPairs = new FeatureSlotPair[this.mFeatures.size()];
        this.mFeatures.toArray(featureSlotPairs);
        dest.writeInt(featureSlotPairs.length);
        for (FeatureSlotPair featureSlotPair : featureSlotPairs) {
            dest.writeInt(featureSlotPair.slotId);
            dest.writeInt(featureSlotPair.featureType);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImsFeatureConfiguration)) {
            return false;
        }
        return this.mFeatures.equals(((ImsFeatureConfiguration) o).mFeatures);
    }

    public int hashCode() {
        return this.mFeatures.hashCode();
    }
}
