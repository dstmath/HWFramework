package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DisabledGeoFence extends AManagedObject {
    public static final Parcelable.Creator<DisabledGeoFence> CREATOR = new Parcelable.Creator<DisabledGeoFence>() {
        public DisabledGeoFence createFromParcel(Parcel in) {
            return new DisabledGeoFence(in);
        }

        public DisabledGeoFence[] newArray(int size) {
            return new DisabledGeoFence[size];
        }
    };
    private String mFenceID;
    private Long mID;
    private String mName;

    public DisabledGeoFence(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mID = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.mFenceID = cursor.getString(2);
        this.mName = cursor.getString(3);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DisabledGeoFence(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mID = null;
            in.readLong();
        } else {
            this.mID = Long.valueOf(in.readLong());
        }
        this.mFenceID = in.readByte() == 0 ? null : in.readString();
        this.mName = in.readByte() != 0 ? in.readString() : str;
    }

    private DisabledGeoFence(Long mID2, String mFenceID2, String mName2) {
        this.mID = mID2;
        this.mFenceID = mFenceID2;
        this.mName = mName2;
    }

    public DisabledGeoFence() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getMID() {
        return this.mID;
    }

    public void setMID(Long mID2) {
        this.mID = mID2;
        setValue();
    }

    public String getMFenceID() {
        return this.mFenceID;
    }

    public void setMFenceID(String mFenceID2) {
        this.mFenceID = mFenceID2;
        setValue();
    }

    public String getMName() {
        return this.mName;
    }

    public void setMName(String mName2) {
        this.mName = mName2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mID != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mID.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.mFenceID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mFenceID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mName);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<DisabledGeoFence> getHelper() {
        return DisabledGeoFenceHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.policy.DisabledGeoFence";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DisabledGeoFence { mID: ").append(this.mID);
        sb.append(", mFenceID: ").append(this.mFenceID);
        sb.append(", mName: ").append(this.mName);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
