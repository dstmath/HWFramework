package android.app.mtm.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RSceneData implements Parcelable {
    public static final Creator<RSceneData> CREATOR = new Creator<RSceneData>() {
        public RSceneData createFromParcel(Parcel source) {
            return new RSceneData(source.readInt(), source.readInt(), source.readBundle());
        }

        public RSceneData[] newArray(int size) {
            return new RSceneData[size];
        }
    };
    private Bundle mBundle;
    private int mFeatureId;
    private int mTypeId;

    public RSceneData(int typeId, int featureId, Bundle bundle) {
        this.mTypeId = typeId;
        this.mFeatureId = featureId;
        this.mBundle = bundle;
    }

    public int getTypeId() {
        return this.mTypeId;
    }

    public void setTypeId(int typeId) {
        this.mTypeId = typeId;
    }

    public int getFeatureId() {
        return this.mFeatureId;
    }

    public void setFeatureId(int featureId) {
        this.mFeatureId = featureId;
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void setBundle(Bundle bundle) {
        this.mBundle = bundle;
    }

    public String toString() {
        return "RSceneData[mTypeId=" + this.mTypeId + ", mFeatureId=" + this.mFeatureId + ", mBundle=" + this.mBundle + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTypeId);
        dest.writeInt(this.mFeatureId);
        dest.writeBundle(this.mBundle);
    }
}
