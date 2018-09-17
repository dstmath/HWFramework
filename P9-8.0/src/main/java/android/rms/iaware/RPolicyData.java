package android.rms.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RPolicyData implements Parcelable {
    public static final Creator<RPolicyData> CREATOR = new Creator<RPolicyData>() {
        public RPolicyData createFromParcel(Parcel source) {
            return new RPolicyData(source.readInt(), source.readInt(), source.readBundle());
        }

        public RPolicyData[] newArray(int size) {
            return new RPolicyData[size];
        }
    };
    private Bundle mBundle;
    private int mFeatureId;
    private int mTypeId;

    public RPolicyData(int typeId, int featureId, Bundle bundle) {
        this.mTypeId = typeId;
        this.mFeatureId = featureId;
        this.mBundle = bundle;
    }

    public int getTypeId() {
        return this.mTypeId;
    }

    public int getFeatureId() {
        return this.mFeatureId;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTypeId);
        dest.writeInt(this.mFeatureId);
        dest.writeBundle(this.mBundle);
    }

    public int describeContents() {
        return 0;
    }
}
