package android.app.mtm;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MultiTaskPolicy implements Parcelable {
    public static final Parcelable.Creator<MultiTaskPolicy> CREATOR = new Parcelable.Creator<MultiTaskPolicy>() {
        /* class android.app.mtm.MultiTaskPolicy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MultiTaskPolicy createFromParcel(Parcel source) {
            return new MultiTaskPolicy(source);
        }

        @Override // android.os.Parcelable.Creator
        public MultiTaskPolicy[] newArray(int size) {
            return new MultiTaskPolicy[size];
        }
    };
    static final String TAG = "MultiTaskPolicy";
    private int policy;
    private Bundle policyData;

    public MultiTaskPolicy(int srcPolicy, Bundle srcPolicyData) {
        this.policy = srcPolicy;
        this.policyData = srcPolicyData;
    }

    public MultiTaskPolicy(Parcel source) {
        this.policy = source.readInt();
        this.policyData = source.readBundle();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.policy);
        dest.writeBundle(this.policyData);
    }

    public int getPolicy() {
        return this.policy;
    }

    public Bundle getPolicyData() {
        return this.policyData;
    }
}
