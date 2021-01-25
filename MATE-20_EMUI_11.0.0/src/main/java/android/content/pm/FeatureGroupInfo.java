package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;

public final class FeatureGroupInfo implements Parcelable {
    public static final Parcelable.Creator<FeatureGroupInfo> CREATOR = new Parcelable.Creator<FeatureGroupInfo>() {
        /* class android.content.pm.FeatureGroupInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FeatureGroupInfo createFromParcel(Parcel source) {
            FeatureGroupInfo group = new FeatureGroupInfo();
            group.features = (FeatureInfo[]) source.createTypedArray(FeatureInfo.CREATOR);
            return group;
        }

        @Override // android.os.Parcelable.Creator
        public FeatureGroupInfo[] newArray(int size) {
            return new FeatureGroupInfo[size];
        }
    };
    public FeatureInfo[] features;

    public FeatureGroupInfo() {
    }

    public FeatureGroupInfo(FeatureGroupInfo other) {
        this.features = other.features;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(this.features, flags);
    }
}
