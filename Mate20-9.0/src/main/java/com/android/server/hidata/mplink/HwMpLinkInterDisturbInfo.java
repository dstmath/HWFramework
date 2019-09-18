package com.android.server.hidata.mplink;

import android.os.Parcel;

public class HwMpLinkInterDisturbInfo {
    public static final int INVAILD_VALUE = 0;
    public int mRat = 0;
    public int mUlbw = 0;
    public int mUlfreq = 0;

    public HwMpLinkInterDisturbInfo() {
    }

    public HwMpLinkInterDisturbInfo(Parcel p) {
        if (p != null) {
            this.mRat = p.readInt();
            this.mUlfreq = p.readInt();
            this.mUlbw = p.readInt();
        }
    }
}
