package android.rms.iaware;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class RPolicyData implements Parcelable {
    public static final Parcelable.Creator<RPolicyData> CREATOR = new Parcelable.Creator<RPolicyData>() {
        /* class android.rms.iaware.RPolicyData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RPolicyData createFromParcel(Parcel source) {
            return new RPolicyData(source.readInt(), source.readInt(), source.readBundle());
        }

        @Override // android.os.Parcelable.Creator
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

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTypeId);
        dest.writeInt(this.mFeatureId);
        dest.writeBundle(this.mBundle);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
