package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorAudit extends AManagedObject {
    public static final Parcelable.Creator<CoordinatorAudit> CREATOR = new Parcelable.Creator<CoordinatorAudit>() {
        /* class com.huawei.nb.model.coordinator.CoordinatorAudit.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CoordinatorAudit createFromParcel(Parcel parcel) {
            return new CoordinatorAudit(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CoordinatorAudit[] newArray(int i) {
            return new CoordinatorAudit[i];
        }
    };
    private String appPackageName;
    private Long dataSize;
    private Integer id;
    private Long isNeedRetry;
    private boolean isRequestSuccess;
    private String netWorkState;
    private String requestDate;
    private Long successTransferTime;
    private Long successVerifyTime;
    private Long timeStamp;
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
        return "com.huawei.nb.model.coordinator.CoordinatorAudit";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public CoordinatorAudit(Cursor cursor) {
        boolean z = false;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.appPackageName = cursor.getString(2);
        this.url = cursor.getString(3);
        this.netWorkState = cursor.getString(4);
        this.timeStamp = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.isNeedRetry = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.successVerifyTime = cursor.isNull(7) ? null : Long.valueOf(cursor.getLong(7));
        this.successTransferTime = cursor.isNull(8) ? null : Long.valueOf(cursor.getLong(8));
        this.dataSize = !cursor.isNull(9) ? Long.valueOf(cursor.getLong(9)) : l;
        this.requestDate = cursor.getString(10);
        this.isRequestSuccess = cursor.getInt(11) != 0 ? true : z;
    }

    public CoordinatorAudit(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.appPackageName = parcel.readByte() == 0 ? null : parcel.readString();
        this.url = parcel.readByte() == 0 ? null : parcel.readString();
        this.netWorkState = parcel.readByte() == 0 ? null : parcel.readString();
        this.timeStamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.isNeedRetry = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.successVerifyTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.successTransferTime = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.dataSize = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.requestDate = parcel.readByte() != 0 ? parcel.readString() : str;
        this.isRequestSuccess = parcel.readByte() != 0;
    }

    private CoordinatorAudit(Integer num, String str, String str2, String str3, Long l, Long l2, Long l3, Long l4, Long l5, String str4, boolean z) {
        this.id = num;
        this.appPackageName = str;
        this.url = str2;
        this.netWorkState = str3;
        this.timeStamp = l;
        this.isNeedRetry = l2;
        this.successVerifyTime = l3;
        this.successTransferTime = l4;
        this.dataSize = l5;
        this.requestDate = str4;
        this.isRequestSuccess = z;
    }

    public CoordinatorAudit() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getAppPackageName() {
        return this.appPackageName;
    }

    public void setAppPackageName(String str) {
        this.appPackageName = str;
        setValue();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str;
        setValue();
    }

    public String getNetWorkState() {
        return this.netWorkState;
    }

    public void setNetWorkState(String str) {
        this.netWorkState = str;
        setValue();
    }

    public Long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(Long l) {
        this.timeStamp = l;
        setValue();
    }

    public Long getIsNeedRetry() {
        return this.isNeedRetry;
    }

    public void setIsNeedRetry(Long l) {
        this.isNeedRetry = l;
        setValue();
    }

    public Long getSuccessVerifyTime() {
        return this.successVerifyTime;
    }

    public void setSuccessVerifyTime(Long l) {
        this.successVerifyTime = l;
        setValue();
    }

    public Long getSuccessTransferTime() {
        return this.successTransferTime;
    }

    public void setSuccessTransferTime(Long l) {
        this.successTransferTime = l;
        setValue();
    }

    public Long getDataSize() {
        return this.dataSize;
    }

    public void setDataSize(Long l) {
        this.dataSize = l;
        setValue();
    }

    public String getRequestDate() {
        return this.requestDate;
    }

    public void setRequestDate(String str) {
        this.requestDate = str;
        setValue();
    }

    public boolean getIsRequestSuccess() {
        return this.isRequestSuccess;
    }

    public void setIsRequestSuccess(boolean z) {
        this.isRequestSuccess = z;
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
        if (this.appPackageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.appPackageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.url != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.url);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.netWorkState != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.netWorkState);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.timeStamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.timeStamp.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isNeedRetry != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.isNeedRetry.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.successVerifyTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.successVerifyTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.successTransferTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.successTransferTime.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataSize != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.dataSize.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.requestDate != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.requestDate);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeByte(this.isRequestSuccess ? (byte) 1 : 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CoordinatorAudit> getHelper() {
        return CoordinatorAuditHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CoordinatorAudit { id: " + this.id + ", appPackageName: " + this.appPackageName + ", url: " + this.url + ", netWorkState: " + this.netWorkState + ", timeStamp: " + this.timeStamp + ", isNeedRetry: " + this.isNeedRetry + ", successVerifyTime: " + this.successVerifyTime + ", successTransferTime: " + this.successTransferTime + ", dataSize: " + this.dataSize + ", requestDate: " + this.requestDate + ", isRequestSuccess: " + this.isRequestSuccess + " }";
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
