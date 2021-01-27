package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class MetaEventStatus extends AManagedObject {
    public static final Parcelable.Creator<MetaEventStatus> CREATOR = new Parcelable.Creator<MetaEventStatus>() {
        /* class com.huawei.nb.model.collectencrypt.MetaEventStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaEventStatus createFromParcel(Parcel parcel) {
            return new MetaEventStatus(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaEventStatus[] newArray(int i) {
            return new MetaEventStatus[i];
        }
    };
    private Date mBegin;
    private Date mEnd;
    private String mEventParam;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private String mStatus;
    private String mStatusName;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaEventStatus";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaEventStatus(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mStatusName = cursor.getString(2);
        this.mStatus = cursor.getString(3);
        this.mBegin = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mEnd = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mEventParam = cursor.getString(6);
        this.mReservedInt = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.mReservedText = cursor.getString(8);
    }

    public MetaEventStatus(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mStatusName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mStatus = parcel.readByte() == 0 ? null : parcel.readString();
        this.mBegin = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mEnd = parcel.readByte() == 0 ? null : new Date(parcel.readLong());
        this.mEventParam = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaEventStatus(Integer num, String str, String str2, Date date, Date date2, String str3, Integer num2, String str4) {
        this.mId = num;
        this.mStatusName = str;
        this.mStatus = str2;
        this.mBegin = date;
        this.mEnd = date2;
        this.mEventParam = str3;
        this.mReservedInt = num2;
        this.mReservedText = str4;
    }

    public MetaEventStatus() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getMStatusName() {
        return this.mStatusName;
    }

    public void setMStatusName(String str) {
        this.mStatusName = str;
        setValue();
    }

    public String getMStatus() {
        return this.mStatus;
    }

    public void setMStatus(String str) {
        this.mStatus = str;
        setValue();
    }

    public Date getMBegin() {
        return this.mBegin;
    }

    public void setMBegin(Date date) {
        this.mBegin = date;
        setValue();
    }

    public Date getMEnd() {
        return this.mEnd;
    }

    public void setMEnd(Date date) {
        this.mEnd = date;
        setValue();
    }

    public String getMEventParam() {
        return this.mEventParam;
    }

    public void setMEventParam(String str) {
        this.mEventParam = str;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer num) {
        this.mReservedInt = num;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String str) {
        this.mReservedText = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mStatusName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mStatusName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mStatus != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mStatus);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mBegin != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mBegin.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEnd != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.mEnd.getTime());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mEventParam != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mEventParam);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReservedInt.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReservedText);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<MetaEventStatus> getHelper() {
        return MetaEventStatusHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaEventStatus { mId: " + this.mId + ", mStatusName: " + this.mStatusName + ", mStatus: " + this.mStatus + ", mBegin: " + this.mBegin + ", mEnd: " + this.mEnd + ", mEventParam: " + this.mEventParam + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
