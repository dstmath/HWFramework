package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DeviceToken extends AManagedObject {
    public static final Parcelable.Creator<DeviceToken> CREATOR = new Parcelable.Creator<DeviceToken>() {
        /* class com.huawei.nb.model.coordinator.DeviceToken.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceToken createFromParcel(Parcel parcel) {
            return new DeviceToken(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceToken[] newArray(int i) {
            return new DeviceToken[i];
        }
    };
    private Long id;
    private String reportFlag;
    private String reportTime;
    private String token;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsMeta";
    }

    public String getDatabaseVersion() {
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.DeviceToken";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DeviceToken(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.token = cursor.getString(2);
        this.reportFlag = cursor.getString(3);
        this.reportTime = cursor.getString(4);
    }

    public DeviceToken(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.token = parcel.readByte() == 0 ? null : parcel.readString();
        this.reportFlag = parcel.readByte() == 0 ? null : parcel.readString();
        this.reportTime = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DeviceToken(Long l, String str, String str2, String str3) {
        this.id = l;
        this.token = str;
        this.reportFlag = str2;
        this.reportTime = str3;
    }

    public DeviceToken() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String str) {
        this.token = str;
        setValue();
    }

    public String getReportFlag() {
        return this.reportFlag;
    }

    public void setReportFlag(String str) {
        this.reportFlag = str;
        setValue();
    }

    public String getReportTime() {
        return this.reportTime;
    }

    public void setReportTime(String str) {
        this.reportTime = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.token != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.token);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reportFlag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reportFlag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reportTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reportTime);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DeviceToken> getHelper() {
        return DeviceTokenHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DeviceToken { id: " + this.id + ", token: " + this.token + ", reportFlag: " + this.reportFlag + ", reportTime: " + this.reportTime + " }";
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
