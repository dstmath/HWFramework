package com.android.server.fingerprint;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.android.biometric.FingerprintEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartfingerprintopt.BuildConfig;
import java.util.ArrayList;

public final class HwFingerprintSets implements Parcelable {
    public static final Parcelable.Creator<HwFingerprintSets> CREATOR = new Parcelable.Creator<HwFingerprintSets>() {
        /* class com.android.server.fingerprint.HwFingerprintSets.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwFingerprintSets createFromParcel(Parcel in) {
            return new HwFingerprintSets(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwFingerprintSets[] newArray(int size) {
            return new HwFingerprintSets[size];
        }
    };
    private static final String TAG = "HwFingerprintSets";
    public ArrayList<HwFingerprintGroup> mFingerprintGroups;

    public static class HwFingerprintGroup {
        public static final int DESCRIPTION_LEN = 256;
        public ArrayList<FingerprintEx> mFingerprints;
        public int mGroupId;

        public HwFingerprintGroup() {
            this.mFingerprints = new ArrayList<>();
        }

        private HwFingerprintGroup(Parcel in) {
            this.mFingerprints = new ArrayList<>();
            this.mGroupId = in.readInt();
            SlogEx.i(HwFingerprintSets.TAG, "HwFingerprintGroup, mGroupId=" + this.mGroupId);
            int fpCount = in.readInt();
            SlogEx.i(HwFingerprintSets.TAG, "HwFingerprintGroup, fpCount=" + fpCount);
            for (int i = 0; i < fpCount; i++) {
                this.mFingerprints.add(new FingerprintEx(BuildConfig.FLAVOR, this.mGroupId, in.readInt(), 0));
            }
        }
    }

    public HwFingerprintSets() {
        this.mFingerprintGroups = new ArrayList<>();
    }

    private HwFingerprintSets(Parcel in) {
        this.mFingerprintGroups = new ArrayList<>();
        int groupCount = in.readInt();
        SlogEx.i(TAG, "HwFingerprintSets, groupCount=" + groupCount);
        for (int i = 0; i < groupCount; i++) {
            this.mFingerprintGroups.add(new HwFingerprintGroup(in));
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
    }
}
