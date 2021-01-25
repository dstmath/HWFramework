package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ReportEvent extends AManagedObject {
    public static final Parcelable.Creator<ReportEvent> CREATOR = new Parcelable.Creator<ReportEvent>() {
        /* class com.huawei.nb.model.coordinator.ReportEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ReportEvent createFromParcel(Parcel parcel) {
            return new ReportEvent(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ReportEvent[] newArray(int i) {
            return new ReportEvent[i];
        }
    };
    private Integer eventNo;
    private String id;
    private String params;
    private String type;

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
        return "com.huawei.nb.model.coordinator.ReportEvent";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ReportEvent(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.eventNo = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.id = cursor.getString(2);
        this.type = cursor.getString(3);
        this.params = cursor.getString(4);
    }

    public ReportEvent(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.eventNo = null;
            parcel.readInt();
        } else {
            this.eventNo = Integer.valueOf(parcel.readInt());
        }
        this.id = parcel.readByte() == 0 ? null : parcel.readString();
        this.type = parcel.readByte() == 0 ? null : parcel.readString();
        this.params = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private ReportEvent(Integer num, String str, String str2, String str3) {
        this.eventNo = num;
        this.id = str;
        this.type = str2;
        this.params = str3;
    }

    public ReportEvent() {
    }

    public Integer getEventNo() {
        return this.eventNo;
    }

    public void setEventNo(Integer num) {
        this.eventNo = num;
        setValue();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String str) {
        this.id = str;
        setValue();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String str) {
        this.type = str;
        setValue();
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String str) {
        this.params = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.eventNo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.eventNo.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.id);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.params != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.params);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ReportEvent> getHelper() {
        return ReportEventHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ReportEvent { eventNo: " + this.eventNo + ", id: " + this.id + ", type: " + this.type + ", params: " + this.params + " }";
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
