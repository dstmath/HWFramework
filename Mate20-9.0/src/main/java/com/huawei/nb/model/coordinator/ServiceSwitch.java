package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ServiceSwitch extends AManagedObject {
    public static final Parcelable.Creator<ServiceSwitch> CREATOR = new Parcelable.Creator<ServiceSwitch>() {
        public ServiceSwitch createFromParcel(Parcel in) {
            return new ServiceSwitch(in);
        }

        public ServiceSwitch[] newArray(int size) {
            return new ServiceSwitch[size];
        }
    };
    private Integer _switch;
    private Long id;

    public ServiceSwitch(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this._switch = !cursor.isNull(2) ? Integer.valueOf(cursor.getInt(2)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ServiceSwitch(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this._switch = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private ServiceSwitch(Long id2, Integer _switch2) {
        this.id = id2;
        this._switch = _switch2;
    }

    public ServiceSwitch() {
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

    public Integer getSwitch() {
        return this._switch;
    }

    public void setSwitch(Integer _switch2) {
        this._switch = _switch2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this._switch != null) {
            out.writeByte((byte) 1);
            out.writeInt(this._switch.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ServiceSwitch> getHelper() {
        return ServiceSwitchHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.ServiceSwitch";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceSwitch { id: ").append(this.id);
        sb.append(", switch: ").append(this._switch);
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
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
