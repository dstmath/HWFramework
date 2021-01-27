package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class HwPolicyServiceAbility extends AManagedObject {
    public static final Parcelable.Creator<HwPolicyServiceAbility> CREATOR = new Parcelable.Creator<HwPolicyServiceAbility>() {
        /* class com.huawei.nb.model.policy.HwPolicyServiceAbility.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwPolicyServiceAbility createFromParcel(Parcel parcel) {
            return new HwPolicyServiceAbility(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public HwPolicyServiceAbility[] newArray(int i) {
            return new HwPolicyServiceAbility[i];
        }
    };
    private Long id;
    private String name;
    private String reserve;
    private String type;
    private Long versionCode;
    private String versionName;

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
        return "com.huawei.nb.model.policy.HwPolicyServiceAbility";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public HwPolicyServiceAbility(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.type = cursor.getString(3);
        this.versionCode = !cursor.isNull(4) ? Long.valueOf(cursor.getLong(4)) : l;
        this.versionName = cursor.getString(5);
        this.reserve = cursor.getString(6);
    }

    public HwPolicyServiceAbility(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.type = parcel.readByte() == 0 ? null : parcel.readString();
        this.versionCode = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.versionName = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserve = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private HwPolicyServiceAbility(Long l, String str, String str2, Long l2, String str3, String str4) {
        this.id = l;
        this.name = str;
        this.type = str2;
        this.versionCode = l2;
        this.versionName = str3;
        this.reserve = str4;
    }

    public HwPolicyServiceAbility() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String str) {
        this.type = str;
        setValue();
    }

    public Long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Long l) {
        this.versionCode = l;
        setValue();
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String str) {
        this.versionName = str;
        setValue();
    }

    public String getReserve() {
        return this.reserve;
    }

    public void setReserve(String str) {
        this.reserve = str;
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
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.versionCode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.versionCode.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.versionName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.versionName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserve != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserve);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<HwPolicyServiceAbility> getHelper() {
        return HwPolicyServiceAbilityHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "HwPolicyServiceAbility { id: " + this.id + ", name: " + this.name + ", type: " + this.type + ", versionCode: " + this.versionCode + ", versionName: " + this.versionName + ", reserve: " + this.reserve + " }";
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
