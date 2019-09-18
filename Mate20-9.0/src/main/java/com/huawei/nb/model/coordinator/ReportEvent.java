package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ReportEvent extends AManagedObject {
    public static final Parcelable.Creator<ReportEvent> CREATOR = new Parcelable.Creator<ReportEvent>() {
        public ReportEvent createFromParcel(Parcel in) {
            return new ReportEvent(in);
        }

        public ReportEvent[] newArray(int size) {
            return new ReportEvent[size];
        }
    };
    private Integer eventNo;
    private String id;
    private String params;
    private String type;

    public ReportEvent(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.eventNo = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.id = cursor.getString(2);
        this.type = cursor.getString(3);
        this.params = cursor.getString(4);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ReportEvent(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.eventNo = null;
            in.readInt();
        } else {
            this.eventNo = Integer.valueOf(in.readInt());
        }
        this.id = in.readByte() == 0 ? null : in.readString();
        this.type = in.readByte() == 0 ? null : in.readString();
        this.params = in.readByte() != 0 ? in.readString() : str;
    }

    private ReportEvent(Integer eventNo2, String id2, String type2, String params2) {
        this.eventNo = eventNo2;
        this.id = id2;
        this.type = type2;
        this.params = params2;
    }

    public ReportEvent() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getEventNo() {
        return this.eventNo;
    }

    public void setEventNo(Integer eventNo2) {
        this.eventNo = eventNo2;
        setValue();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id2) {
        this.id = id2;
        setValue();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
        setValue();
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String params2) {
        this.params = params2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.eventNo != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.eventNo.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeString(this.id);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.type);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.params != null) {
            out.writeByte((byte) 1);
            out.writeString(this.params);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ReportEvent> getHelper() {
        return ReportEventHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.ReportEvent";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ReportEvent { eventNo: ").append(this.eventNo);
        sb.append(", id: ").append(this.id);
        sb.append(", type: ").append(this.type);
        sb.append(", params: ").append(this.params);
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
