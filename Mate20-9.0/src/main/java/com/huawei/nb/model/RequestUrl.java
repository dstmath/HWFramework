package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class RequestUrl extends AManagedObject {
    public static final Parcelable.Creator<RequestUrl> CREATOR = new Parcelable.Creator<RequestUrl>() {
        public RequestUrl createFromParcel(Parcel in) {
            return new RequestUrl(in);
        }

        public RequestUrl[] newArray(int size) {
            return new RequestUrl[size];
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

    public RequestUrl(Cursor cursor) {
        boolean z;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.url = cursor.getString(2);
        this.timeStamp = cursor.getLong(3);
        if (cursor.getInt(4) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isExpired = z;
        this.district = cursor.getString(5);
        this.appName = cursor.getString(6);
        this.serviceName = cursor.getString(7);
        this.key = cursor.getString(8);
        this.expiredTime = cursor.getLong(9);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RequestUrl(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.url = in.readByte() == 0 ? null : in.readString();
        this.timeStamp = in.readLong();
        this.isExpired = in.readByte() != 0;
        this.district = in.readByte() == 0 ? null : in.readString();
        this.appName = in.readByte() == 0 ? null : in.readString();
        this.serviceName = in.readByte() == 0 ? null : in.readString();
        this.key = in.readByte() != 0 ? in.readString() : str;
        this.expiredTime = in.readLong();
    }

    private RequestUrl(Integer id2, String url2, long timeStamp2, boolean isExpired2, String district2, String appName2, String serviceName2, String key2, long expiredTime2) {
        this.id = id2;
        this.url = url2;
        this.timeStamp = timeStamp2;
        this.isExpired = isExpired2;
        this.district = district2;
        this.appName = appName2;
        this.serviceName = serviceName2;
        this.key = key2;
        this.expiredTime = expiredTime2;
    }

    public RequestUrl() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
        setValue();
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
        setValue();
    }

    public boolean getIsExpired() {
        return this.isExpired;
    }

    public void setIsExpired(boolean isExpired2) {
        this.isExpired = isExpired2;
        setValue();
    }

    public String getDistrict() {
        return this.district;
    }

    public void setDistrict(String district2) {
        this.district = district2;
        setValue();
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName2) {
        this.appName = appName2;
        setValue();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName2) {
        this.serviceName = serviceName2;
        setValue();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
        setValue();
    }

    public long getExpiredTime() {
        return this.expiredTime;
    }

    public void setExpiredTime(long expiredTime2) {
        this.expiredTime = expiredTime2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.url != null) {
            out.writeByte((byte) 1);
            out.writeString(this.url);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeLong(this.timeStamp);
        if (this.isExpired) {
            b = 1;
        } else {
            b = 0;
        }
        out.writeByte(b);
        if (this.district != null) {
            out.writeByte((byte) 1);
            out.writeString(this.district);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.appName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.appName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.serviceName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.serviceName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.key != null) {
            out.writeByte((byte) 1);
            out.writeString(this.key);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeLong(this.expiredTime);
    }

    public AEntityHelper<RequestUrl> getHelper() {
        return RequestUrlHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.RequestUrl";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RequestUrl { id: ").append(this.id);
        sb.append(", url: ").append(this.url);
        sb.append(", timeStamp: ").append(this.timeStamp);
        sb.append(", isExpired: ").append(this.isExpired);
        sb.append(", district: ").append(this.district);
        sb.append(", appName: ").append(this.appName);
        sb.append(", serviceName: ").append(this.serviceName);
        sb.append(", key: ").append(this.key);
        sb.append(", expiredTime: ").append(this.expiredTime);
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
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
