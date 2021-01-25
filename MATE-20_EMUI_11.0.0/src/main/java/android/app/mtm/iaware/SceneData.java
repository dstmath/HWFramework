package android.app.mtm.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class SceneData implements Parcelable {
    public static final Parcelable.Creator<SceneData> CREATOR = new Parcelable.Creator<SceneData>() {
        /* class android.app.mtm.iaware.SceneData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SceneData createFromParcel(Parcel source) {
            return new SceneData(source.readInt(), source.readInt(), source.readBundle());
        }

        @Override // android.os.Parcelable.Creator
        public SceneData[] newArray(int size) {
            return new SceneData[size];
        }
    };
    private Bundle mBundle;
    private int mFeatureId;
    private int mTypeId;

    public SceneData(int typeId, int featureId, Bundle bundle) {
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

    @Override // java.lang.Object
    public String toString() {
        return "SceneData[mTypeId=" + this.mTypeId + ", mFeatureId=" + this.mFeatureId + ", mBundle=" + this.mBundle + "]";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTypeId);
        dest.writeInt(this.mFeatureId);
        dest.writeBundle(this.mBundle);
    }
}
