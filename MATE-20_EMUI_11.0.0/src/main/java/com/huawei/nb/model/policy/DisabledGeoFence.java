package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DisabledGeoFence extends AManagedObject {
    public static final Parcelable.Creator<DisabledGeoFence> CREATOR = new Parcelable.Creator<DisabledGeoFence>() {
        /* class com.huawei.nb.model.policy.DisabledGeoFence.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DisabledGeoFence createFromParcel(Parcel parcel) {
            return new DisabledGeoFence(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DisabledGeoFence[] newArray(int i) {
            return new DisabledGeoFence[i];
        }
    };
    private String mFenceID;
    private Long mID;
    private String mName;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.policy.DisabledGeoFence";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DisabledGeoFence(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mID = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.mFenceID = cursor.getString(2);
        this.mName = cursor.getString(3);
    }

    public DisabledGeoFence(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mID = null;
            parcel.readLong();
        } else {
            this.mID = Long.valueOf(parcel.readLong());
        }
        this.mFenceID = parcel.readByte() == 0 ? null : parcel.readString();
        this.mName = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DisabledGeoFence(Long l, String str, String str2) {
        this.mID = l;
        this.mFenceID = str;
        this.mName = str2;
    }

    public DisabledGeoFence() {
    }

    public Long getMID() {
        return this.mID;
    }

    public void setMID(Long l) {
        this.mID = l;
        setValue();
    }

    public String getMFenceID() {
        return this.mFenceID;
    }

    public void setMFenceID(String str) {
        this.mFenceID = str;
        setValue();
    }

    public String getMName() {
        return this.mName;
    }

    public void setMName(String str) {
        this.mName = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mID.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.mFenceID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mFenceID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mName);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DisabledGeoFence> getHelper() {
        return DisabledGeoFenceHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DisabledGeoFence { mID: " + this.mID + ", mFenceID: " + this.mFenceID + ", mName: " + this.mName + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
