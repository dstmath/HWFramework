package com.huawei.nb.model.coordinator;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessMsg extends AManagedObject {
    public static final Parcelable.Creator<BusinessMsg> CREATOR = new Parcelable.Creator<BusinessMsg>() {
        public BusinessMsg createFromParcel(Parcel in) {
            return new BusinessMsg(in);
        }

        public BusinessMsg[] newArray(int size) {
            return new BusinessMsg[size];
        }
    };
    private Long id;
    private String msg_type;
    private String params;
    private String service_id;

    public BusinessMsg(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.service_id = cursor.getString(2);
        this.msg_type = cursor.getString(3);
        this.params = cursor.getString(4);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public BusinessMsg(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.service_id = in.readByte() == 0 ? null : in.readString();
        this.msg_type = in.readByte() == 0 ? null : in.readString();
        this.params = in.readByte() != 0 ? in.readString() : str;
    }

    private BusinessMsg(Long id2, String service_id2, String msg_type2, String params2) {
        this.id = id2;
        this.service_id = service_id2;
        this.msg_type = msg_type2;
        this.params = params2;
    }

    public BusinessMsg() {
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

    public String getService_id() {
        return this.service_id;
    }

    public void setService_id(String service_id2) {
        this.service_id = service_id2;
        setValue();
    }

    public String getMsg_type() {
        return this.msg_type;
    }

    public void setMsg_type(String msg_type2) {
        this.msg_type = msg_type2;
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
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.service_id != null) {
            out.writeByte((byte) 1);
            out.writeString(this.service_id);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.msg_type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.msg_type);
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

    public AEntityHelper<BusinessMsg> getHelper() {
        return BusinessMsgHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.coordinator.BusinessMsg";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("BusinessMsg { id: ").append(this.id);
        sb.append(", service_id: ").append(this.service_id);
        sb.append(", msg_type: ").append(this.msg_type);
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
