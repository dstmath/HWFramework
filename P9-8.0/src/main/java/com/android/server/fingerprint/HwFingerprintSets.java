package com.android.server.fingerprint;

import android.hardware.fingerprint.Fingerprint;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Slog;
import java.util.ArrayList;

public final class HwFingerprintSets implements Parcelable {
    public static final Creator<HwFingerprintSets> CREATOR = new Creator<HwFingerprintSets>() {
        public HwFingerprintSets createFromParcel(Parcel in) {
            return new HwFingerprintSets(in, null);
        }

        public HwFingerprintSets[] newArray(int size) {
            return new HwFingerprintSets[size];
        }
    };
    private static final String TAG = "HwFingerprintSets";
    public ArrayList<HwFingerprintGroup> mFingerprintGroups;

    public static class HwFingerprintGroup {
        public static final int DESCRIPTION_LEN = 256;
        public ArrayList<Fingerprint> mFingerprints;
        public int mGroupId;

        /* synthetic */ HwFingerprintGroup(Parcel in, HwFingerprintGroup -this1) {
            this(in);
        }

        public HwFingerprintGroup() {
            this.mFingerprints = new ArrayList();
        }

        private HwFingerprintGroup(Parcel in) {
            this.mFingerprints = new ArrayList();
            this.mGroupId = in.readInt();
            Slog.i(HwFingerprintSets.TAG, "HwFingerprintGroup, mGroupId=" + this.mGroupId);
            int fpCount = in.readInt();
            Slog.i(HwFingerprintSets.TAG, "HwFingerprintGroup, fpCount=" + fpCount);
            for (int i = 0; i < fpCount; i++) {
                int fingerid = in.readInt();
                Slog.i(HwFingerprintSets.TAG, "HwFingerprintGroup, fingerid=" + fingerid);
                this.mFingerprints.add(new Fingerprint("", this.mGroupId, fingerid, 0));
            }
        }
    }

    /* synthetic */ HwFingerprintSets(Parcel in, HwFingerprintSets -this1) {
        this(in);
    }

    public HwFingerprintSets() {
        this.mFingerprintGroups = new ArrayList();
    }

    private HwFingerprintSets(Parcel in) {
        this.mFingerprintGroups = new ArrayList();
        int groupCount = in.readInt();
        Slog.i(TAG, "HwFingerprintSets, groupCount=" + groupCount);
        for (int i = 0; i < groupCount; i++) {
            this.mFingerprintGroups.add(new HwFingerprintGroup(in, null));
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
    }
}
