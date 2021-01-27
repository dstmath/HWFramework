package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class RequestUrl extends AManagedObject {
    public static final Parcelable.Creator<RequestUrl> CREATOR = new Parcelable.Creator<RequestUrl>() {
        /* class com.huawei.nb.model.RequestUrl.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RequestUrl createFromParcel(Parcel parcel) {
            return new RequestUrl(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RequestUrl[] newArray(int i) {
            return new RequestUrl[i];
        }
    };
    private String appName;
    private String district;
    private long expiredTime;
    private Integer id;
    private boolean isExpired;
    private String key;
    private String serviceName;
    private long timeStamp;
    private String url;

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
        return "com.huawei.nb.model.RequestUrl";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RequestUrl(Cursor cursor) {
        boolean z = false;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.url = cursor.getString(2);
        this.timeStamp = cursor.getLong(3);
        this.isExpired = cursor.getInt(4) != 0 ? true : z;
        this.district = cursor.getString(5);
        this.appName = cursor.getString(6);
        this.serviceName = cursor.getString(7);
        this.key = cursor.getString(8);
        this.expiredTime = cursor.getLong(9);
    }

    public RequestUrl(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.url = parcel.readByte() == 0 ? null : parcel.readString();
        this.timeStamp = parcel.readLong();
        this.isExpired = parcel.readByte() != 0;
        this.district = parcel.readByte() == 0 ? null : parcel.readString();
        this.appName = parcel.readByte() == 0 ? null : parcel.readString();
        this.serviceName = parcel.readByte() == 0 ? null : parcel.readString();
        this.key = parcel.readByte() != 0 ? parcel.readString() : str;
        this.expiredTime = parcel.readLong();
    }

    private RequestUrl(Integer num, String str, long j, boolean z, String str2, String str3, String str4, String str5, long j2) {
        this.id = num;
        this.url = str;
        this.timeStamp = j;
        this.isExpired = z;
        this.district = str2;
        this.appName = str3;
        this.serviceName = str4;
        this.key = str5;
        this.expiredTime = j2;
    }

    public RequestUrl() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
        setValue();
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long j) {
        this.timeStamp = j;
        setValue();
    }

    public boolean getIsExpired() {
        return this.isExpired;
    }

    public void setIsExpired(boolean z) {
        this.isExpired = z;
        setValue();
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String str) {
        this.district = str;
        setValue();
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String str) {
        this.appName = str;
        setValue();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String str) {
        this.serviceName = str;
        setValue();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String str) {
        this.key = str;
        setValue();
    }

    public long getExpiredTime() {
        return this.expiredTime;
    }

    public void setExpiredTime(long j) {
        this.expiredTime = j;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.url != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.url);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.timeStamp);
        parcel.writeByte(this.isExpired ? (byte) 1 : 0);
        if (this.district != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.district);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.appName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.appName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.serviceName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.serviceName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.key != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.key);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.expiredTime);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RequestUrl> getHelper() {
        return RequestUrlHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RequestUrl { id: " + this.id + ", url: " + this.url + ", timeStamp: " + this.timeStamp + ", isExpired: " + this.isExpired + ", district: " + this.district + ", appName: " + this.appName + ", serviceName: " + this.serviceName + ", key: " + this.key + ", expiredTime: " + this.expiredTime + " }";
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
