package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorSwitch extends AManagedObject {
    public static final Parcelable.Creator<CoordinatorSwitch> CREATOR = new Parcelable.Creator<CoordinatorSwitch>() {
        /* class com.huawei.nb.model.coordinator.CoordinatorSwitch.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CoordinatorSwitch createFromParcel(Parcel parcel) {
            return new CoordinatorSwitch(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CoordinatorSwitch[] newArray(int i) {
            return new CoordinatorSwitch[i];
        }
    };
    private boolean canUseFlowData;
    private double currentFlowData;
    private Long id;
    private boolean isAutoUpdate;
    private boolean isSwitchOn;
    private Long latestTimestamp;
    private double maxFlowData;
    private String packageName;
    private String reserve1;
    private String reserve2;
    private String serviceName;

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
        return "com.huawei.nb.model.coordinator.CoordinatorSwitch";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public CoordinatorSwitch(Cursor cursor) {
        boolean z = false;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.serviceName = cursor.getString(2);
        this.packageName = cursor.getString(3);
        this.isSwitchOn = cursor.getInt(4) != 0;
        this.isAutoUpdate = cursor.getInt(5) != 0;
        this.latestTimestamp = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
        this.canUseFlowData = cursor.getInt(7) != 0 ? true : z;
        this.currentFlowData = cursor.getDouble(8);
        this.maxFlowData = cursor.getDouble(9);
        this.reserve1 = cursor.getString(10);
        this.reserve2 = cursor.getString(11);
    }

    public CoordinatorSwitch(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.serviceName = parcel.readByte() == 0 ? null : parcel.readString();
        this.packageName = parcel.readByte() == 0 ? null : parcel.readString();
        boolean z = true;
        this.isSwitchOn = parcel.readByte() != 0;
        this.isAutoUpdate = parcel.readByte() != 0;
        this.latestTimestamp = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.canUseFlowData = parcel.readByte() == 0 ? false : z;
        this.currentFlowData = parcel.readDouble();
        this.maxFlowData = parcel.readDouble();
        this.reserve1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserve2 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private CoordinatorSwitch(Long l, String str, String str2, boolean z, boolean z2, Long l2, boolean z3, double d, double d2, String str3, String str4) {
        this.id = l;
        this.serviceName = str;
        this.packageName = str2;
        this.isSwitchOn = z;
        this.isAutoUpdate = z2;
        this.latestTimestamp = l2;
        this.canUseFlowData = z3;
        this.currentFlowData = d;
        this.maxFlowData = d2;
        this.reserve1 = str3;
        this.reserve2 = str4;
    }

    public CoordinatorSwitch() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String str) {
        this.serviceName = str;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
        setValue();
    }

    public boolean getIsSwitchOn() {
        return this.isSwitchOn;
    }

    public void setIsSwitchOn(boolean z) {
        this.isSwitchOn = z;
        setValue();
    }

    public boolean getIsAutoUpdate() {
        return this.isAutoUpdate;
    }

    public void setIsAutoUpdate(boolean z) {
        this.isAutoUpdate = z;
        setValue();
    }

    public Long getLatestTimestamp() {
        return this.latestTimestamp;
    }

    public void setLatestTimestamp(Long l) {
        this.latestTimestamp = l;
        setValue();
    }

    public boolean getCanUseFlowData() {
        return this.canUseFlowData;
    }

    public void setCanUseFlowData(boolean z) {
        this.canUseFlowData = z;
        setValue();
    }

    public double getCurrentFlowData() {
        return this.currentFlowData;
    }

    public void setCurrentFlowData(double d) {
        this.currentFlowData = d;
        setValue();
    }

    public double getMaxFlowData() {
        return this.maxFlowData;
    }

    public void setMaxFlowData(double d) {
        this.maxFlowData = d;
        setValue();
    }

    public String getReserve1() {
        return this.reserve1;
    }

    public void setReserve1(String str) {
        this.reserve1 = str;
        setValue();
    }

    public String getReserve2() {
        return this.reserve2;
    }

    public void setReserve2(String str) {
        this.reserve2 = str;
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
        if (this.serviceName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.serviceName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.packageName);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeByte(this.isSwitchOn ? (byte) 1 : 0);
        parcel.writeByte(this.isAutoUpdate ? (byte) 1 : 0);
        if (this.latestTimestamp != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.latestTimestamp.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeByte(this.canUseFlowData ? (byte) 1 : 0);
        parcel.writeDouble(this.currentFlowData);
        parcel.writeDouble(this.maxFlowData);
        if (this.reserve1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserve1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserve2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserve2);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CoordinatorSwitch> getHelper() {
        return CoordinatorSwitchHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CoordinatorSwitch { id: " + this.id + ", serviceName: " + this.serviceName + ", packageName: " + this.packageName + ", isSwitchOn: " + this.isSwitchOn + ", isAutoUpdate: " + this.isAutoUpdate + ", latestTimestamp: " + this.latestTimestamp + ", canUseFlowData: " + this.canUseFlowData + ", currentFlowData: " + this.currentFlowData + ", maxFlowData: " + this.maxFlowData + ", reserve1: " + this.reserve1 + ", reserve2: " + this.reserve2 + " }";
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
