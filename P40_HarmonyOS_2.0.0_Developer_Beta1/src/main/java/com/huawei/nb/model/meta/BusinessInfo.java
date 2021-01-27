package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessInfo extends AManagedObject {
    public static final Parcelable.Creator<BusinessInfo> CREATOR = new Parcelable.Creator<BusinessInfo>() {
        /* class com.huawei.nb.model.meta.BusinessInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BusinessInfo createFromParcel(Parcel parcel) {
            return new BusinessInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public BusinessInfo[] newArray(int i) {
            return new BusinessInfo[i];
        }
    };
    private String mBusinessName;
    private String mDescription;
    private Integer mId;

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
        return "com.huawei.nb.model.meta.BusinessInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public BusinessInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mBusinessName = cursor.getString(2);
        this.mDescription = cursor.getString(3);
    }

    public BusinessInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mBusinessName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mDescription = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private BusinessInfo(Integer num, String str, String str2) {
        this.mId = num;
        this.mBusinessName = str;
        this.mDescription = str2;
    }

    public BusinessInfo() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getMBusinessName() {
        return this.mBusinessName;
    }

    public void setMBusinessName(String str) {
        this.mBusinessName = str;
        setValue();
    }

    public String getMDescription() {
        return this.mDescription;
    }

    public void setMDescription(String str) {
        this.mDescription = str;
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
        if (this.mBusinessName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mBusinessName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mDescription != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mDescription);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<BusinessInfo> getHelper() {
        return BusinessInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "BusinessInfo { mId: " + this.mId + ", mBusinessName: " + this.mBusinessName + ", mDescription: " + this.mDescription + " }";
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
