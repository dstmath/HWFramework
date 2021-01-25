package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ServiceSwitch extends AManagedObject {
    public static final Parcelable.Creator<ServiceSwitch> CREATOR = new Parcelable.Creator<ServiceSwitch>() {
        /* class com.huawei.nb.model.coordinator.ServiceSwitch.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ServiceSwitch createFromParcel(Parcel parcel) {
            return new ServiceSwitch(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ServiceSwitch[] newArray(int i) {
            return new ServiceSwitch[i];
        }
    };
    private Integer _switch;
    private Long id;

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
        return "com.huawei.nb.model.coordinator.ServiceSwitch";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ServiceSwitch(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this._switch = !cursor.isNull(2) ? Integer.valueOf(cursor.getInt(2)) : num;
    }

    public ServiceSwitch(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this._switch = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private ServiceSwitch(Long l, Integer num) {
        this.id = l;
        this._switch = num;
    }

    public ServiceSwitch() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Integer getSwitch() {
        return this._switch;
    }

    public void setSwitch(Integer num) {
        this._switch = num;
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
        if (this._switch != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this._switch.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ServiceSwitch> getHelper() {
        return ServiceSwitchHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ServiceSwitch { id: " + this.id + ", switch: " + this._switch + " }";
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
