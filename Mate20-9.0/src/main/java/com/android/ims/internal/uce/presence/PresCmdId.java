package com.android.ims.internal.uce.presence;

import android.os.Parcel;
import android.os.Parcelable;

public class PresCmdId implements Parcelable {
    public static final Parcelable.Creator<PresCmdId> CREATOR = new Parcelable.Creator<PresCmdId>() {
        public PresCmdId createFromParcel(Parcel source) {
            return new PresCmdId(source);
        }

        public PresCmdId[] newArray(int size) {
            return new PresCmdId[size];
        }
    };
    public static final int UCE_PRES_CMD_GETCONTACTCAP = 2;
    public static final int UCE_PRES_CMD_GETCONTACTLISTCAP = 3;
    public static final int UCE_PRES_CMD_GET_VERSION = 0;
    public static final int UCE_PRES_CMD_PUBLISHMYCAP = 1;
    public static final int UCE_PRES_CMD_REENABLE_SERVICE = 5;
    public static final int UCE_PRES_CMD_SETNEWFEATURETAG = 4;
    public static final int UCE_PRES_CMD_UNKNOWN = 6;
    private int mCmdId;

    public int getCmdId() {
        return this.mCmdId;
    }

    public void setCmdId(int nCmdId) {
        this.mCmdId = nCmdId;
    }

    public PresCmdId() {
        this.mCmdId = 6;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCmdId);
    }

    private PresCmdId(Parcel source) {
        this.mCmdId = 6;
        readFromParcel(source);
    }

    public void readFromParcel(Parcel source) {
        this.mCmdId = source.readInt();
    }
}
