package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessMsg extends AManagedObject {
    public static final Parcelable.Creator<BusinessMsg> CREATOR = new Parcelable.Creator<BusinessMsg>() {
        /* class com.huawei.nb.model.coordinator.BusinessMsg.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BusinessMsg createFromParcel(Parcel parcel) {
            return new BusinessMsg(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public BusinessMsg[] newArray(int i) {
            return new BusinessMsg[i];
        }
    };
    private Long id;
    private String msg_type;
    private String params;
    private String service_id;

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
        return "com.huawei.nb.model.coordinator.BusinessMsg";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public BusinessMsg(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.service_id = cursor.getString(2);
        this.msg_type = cursor.getString(3);
        this.params = cursor.getString(4);
    }

    public BusinessMsg(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.service_id = parcel.readByte() == 0 ? null : parcel.readString();
        this.msg_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.params = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private BusinessMsg(Long l, String str, String str2, String str3) {
        this.id = l;
        this.service_id = str;
        this.msg_type = str2;
        this.params = str3;
    }

    public BusinessMsg() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getService_id() {
        return this.service_id;
    }

    public void setService_id(String str) {
        this.service_id = str;
        setValue();
    }

    public String getMsg_type() {
        return this.msg_type;
    }

    public void setMsg_type(String str) {
        this.msg_type = str;
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
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.service_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.service_id);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.msg_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.msg_type);
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
    public AEntityHelper<BusinessMsg> getHelper() {
        return BusinessMsgHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "BusinessMsg { id: " + this.id + ", service_id: " + this.service_id + ", msg_type: " + this.msg_type + ", params: " + this.params + " }";
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
