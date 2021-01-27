package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class GuestInfo extends AManagedObject {
    public static final Parcelable.Creator<GuestInfo> CREATOR = new Parcelable.Creator<GuestInfo>() {
        /* class com.huawei.nb.model.meta.GuestInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GuestInfo createFromParcel(Parcel parcel) {
            return new GuestInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public GuestInfo[] newArray(int i) {
            return new GuestInfo[i];
        }
    };
    private Integer id;
    private String pkgName;

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
        return "com.huawei.nb.model.meta.GuestInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public GuestInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.pkgName = cursor.getString(2);
    }

    public GuestInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.pkgName = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private GuestInfo(Integer num, String str) {
        this.id = num;
        this.pkgName = str;
    }

    public GuestInfo() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String str) {
        this.pkgName = str;
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
        if (this.pkgName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.pkgName);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<GuestInfo> getHelper() {
        return GuestInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "GuestInfo { id: " + this.id + ", pkgName: " + this.pkgName + " }";
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
