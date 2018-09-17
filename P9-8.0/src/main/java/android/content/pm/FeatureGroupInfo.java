package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class FeatureGroupInfo implements Parcelable {
    public static final Creator<FeatureGroupInfo> CREATOR = new Creator<FeatureGroupInfo>() {
        public FeatureGroupInfo createFromParcel(Parcel source) {
            FeatureGroupInfo group = new FeatureGroupInfo();
            group.features = (FeatureInfo[]) source.createTypedArray(FeatureInfo.CREATOR);
            return group;
        }

        public FeatureGroupInfo[] newArray(int size) {
            return new FeatureGroupInfo[size];
        }
    };
    public FeatureInfo[] features;

    public FeatureGroupInfo(FeatureGroupInfo other) {
        this.features = other.features;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(this.features, flags);
    }
}
