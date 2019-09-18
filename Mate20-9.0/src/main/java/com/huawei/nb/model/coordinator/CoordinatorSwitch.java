package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CoordinatorSwitch extends AManagedObject {
    public static final Parcelable.Creator<CoordinatorSwitch> CREATOR = new Parcelable.Creator<CoordinatorSwitch>() {
        public CoordinatorSwitch createFromParcel(Parcel in) {
            return new CoordinatorSwitch(in);
        }

        public CoordinatorSwitch[] newArray(int size) {
            return new CoordinatorSwitch[size];
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

    public CoordinatorSwitch(Cursor cursor) {
        boolean z;
        boolean z2;
        Long l = null;
        boolean z3 = true;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.serviceName = cursor.getString(2);
        this.packageName = cursor.getString(3);
        if (cursor.getInt(4) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isSwitchOn = z;
        if (cursor.getInt(5) != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.isAutoUpdate = z2;
        this.latestTimestamp = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
        this.canUseFlowData = cursor.getInt(7) == 0 ? false : z3;
        this.currentFlowData = cursor.getDouble(8);
        this.maxFlowData = cursor.getDouble(9);
        this.reserve1 = cursor.getString(10);
        this.reserve2 = cursor.getString(11);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CoordinatorSwitch(Parcel in) {
        super(in);
        boolean z;
        boolean z2;
        boolean z3 = true;
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.serviceName = in.readByte() == 0 ? null : in.readString();
        this.packageName = in.readByte() == 0 ? null : in.readString();
        if (in.readByte() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isSwitchOn = z;
        if (in.readByte() != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.isAutoUpdate = z2;
        this.latestTimestamp = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.canUseFlowData = in.readByte() == 0 ? false : z3;
        this.currentFlowData = in.readDouble();
        this.maxFlowData = in.readDouble();
        this.reserve1 = in.readByte() == 0 ? null : in.readString();
        this.reserve2 = in.readByte() != 0 ? in.readString() : str;
    }

    private CoordinatorSwitch(Long id2, String serviceName2, String packageName2, boolean isSwitchOn2, boolean isAutoUpdate2, Long latestTimestamp2, boolean canUseFlowData2, double currentFlowData2, double maxFlowData2, String reserve12, String reserve22) {
        this.id = id2;
        this.serviceName = serviceName2;
        this.packageName = packageName2;
        this.isSwitchOn = isSwitchOn2;
        this.isAutoUpdate = isAutoUpdate2;
        this.latestTimestamp = latestTimestamp2;
        this.canUseFlowData = canUseFlowData2;
        this.currentFlowData = currentFlowData2;
        this.maxFlowData = maxFlowData2;
        this.reserve1 = reserve12;
        this.reserve2 = reserve22;
    }

    public CoordinatorSwitch() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id2) {
        this.id = id2;
        setValue();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String serviceName2) {
        this.serviceName = serviceName2;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
        setValue();
    }

    public boolean getIsSwitchOn() {
        return this.isSwitchOn;
    }

    public void setIsSwitchOn(boolean isSwitchOn2) {
        this.isSwitchOn = isSwitchOn2;
        setValue();
    }

    public boolean getIsAutoUpdate() {
        return this.isAutoUpdate;
    }

    public void setIsAutoUpdate(boolean isAutoUpdate2) {
        this.isAutoUpdate = isAutoUpdate2;
        setValue();
    }

    public Long getLatestTimestamp() {
        return this.latestTimestamp;
    }

    public void setLatestTimestamp(Long latestTimestamp2) {
        this.latestTimestamp = latestTimestamp2;
        setValue();
    }

    public boolean getCanUseFlowData() {
        return this.canUseFlowData;
    }

    public void setCanUseFlowData(boolean canUseFlowData2) {
        this.canUseFlowData = canUseFlowData2;
        setValue();
    }

    public double getCurrentFlowData() {
        return this.currentFlowData;
    }

    public void setCurrentFlowData(double currentFlowData2) {
        this.currentFlowData = currentFlowData2;
        setValue();
    }

    public double getMaxFlowData() {
        return this.maxFlowData;
    }

    public void setMaxFlowData(double maxFlowData2) {
        this.maxFlowData = maxFlowData2;
        setValue();
    }

    public String getReserve1() {
        return this.reserve1;
    }

    public void setReserve1(String reserve12) {
        this.reserve1 = reserve12;
        setValue();
    }

    public String getReserve2() {
        return this.reserve2;
    }

    public void setReserve2(String reserve22) {
        this.reserve2 = reserve22;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b;
        byte b2;
        byte b3;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.serviceName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.serviceName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.packageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isSwitchOn) {
            b = 1;
        } else {
            b = 0;
        }
        out.writeByte(b);
        if (this.isAutoUpdate) {
            b2 = 1;
        } else {
            b2 = 0;
        }
        out.writeByte(b2);
        if (this.latestTimestamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.latestTimestamp.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.canUseFlowData) {
            b3 = 1;
        } else {
            b3 = 0;
        }
        out.writeByte(b3);
        out.writeDouble(this.currentFlowData);
        out.writeDouble(this.maxFlowData);
        if (this.reserve1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserve1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserve2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserve2);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<CoordinatorSwitch> getHelper() {
        return CoordinatorSwitchHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.CoordinatorSwitch";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("CoordinatorSwitch { id: ").append(this.id);
        sb.append(", serviceName: ").append(this.serviceName);
        sb.append(", packageName: ").append(this.packageName);
        sb.append(", isSwitchOn: ").append(this.isSwitchOn);
        sb.append(", isAutoUpdate: ").append(this.isAutoUpdate);
        sb.append(", latestTimestamp: ").append(this.latestTimestamp);
        sb.append(", canUseFlowData: ").append(this.canUseFlowData);
        sb.append(", currentFlowData: ").append(this.currentFlowData);
        sb.append(", maxFlowData: ").append(this.maxFlowData);
        sb.append(", reserve1: ").append(this.reserve1);
        sb.append(", reserve2: ").append(this.reserve2);
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
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }
}
