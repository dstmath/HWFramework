package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class EventProcessMethod extends AManagedObject {
    public static final Parcelable.Creator<EventProcessMethod> CREATOR = new Parcelable.Creator<EventProcessMethod>() {
        /* class com.huawei.nb.model.rule.EventProcessMethod.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EventProcessMethod createFromParcel(Parcel parcel) {
            return new EventProcessMethod(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public EventProcessMethod[] newArray(int i) {
            return new EventProcessMethod[i];
        }
    };
    private String condition;
    private Long eventId;
    private Long id;
    private Long itemId;
    private String method;
    private String methodArgs;
    private Long operatorId;
    private Long ruleId;
    private Integer seqId;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsRule";
    }

    public String getDatabaseVersion() {
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.rule.EventProcessMethod";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public EventProcessMethod(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.eventId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.operatorId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.itemId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.ruleId = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.method = cursor.getString(6);
        this.methodArgs = cursor.getString(7);
        this.condition = cursor.getString(8);
        this.seqId = !cursor.isNull(9) ? Integer.valueOf(cursor.getInt(9)) : num;
    }

    public EventProcessMethod(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.eventId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.operatorId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.itemId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.ruleId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.method = parcel.readByte() == 0 ? null : parcel.readString();
        this.methodArgs = parcel.readByte() == 0 ? null : parcel.readString();
        this.condition = parcel.readByte() == 0 ? null : parcel.readString();
        this.seqId = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private EventProcessMethod(Long l, Long l2, Long l3, Long l4, Long l5, String str, String str2, String str3, Integer num) {
        this.id = l;
        this.eventId = l2;
        this.operatorId = l3;
        this.itemId = l4;
        this.ruleId = l5;
        this.method = str;
        this.methodArgs = str2;
        this.condition = str3;
        this.seqId = num;
    }

    public EventProcessMethod() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getEventId() {
        return this.eventId;
    }

    public void setEventId(Long l) {
        this.eventId = l;
        setValue();
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long l) {
        this.operatorId = l;
        setValue();
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long l) {
        this.itemId = l;
        setValue();
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long l) {
        this.ruleId = l;
        setValue();
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String str) {
        this.method = str;
        setValue();
    }

    public String getMethodArgs() {
        return this.methodArgs;
    }

    public void setMethodArgs(String str) {
        this.methodArgs = str;
        setValue();
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String str) {
        this.condition = str;
        setValue();
    }

    public Integer getSeqId() {
        return this.seqId;
    }

    public void setSeqId(Integer num) {
        this.seqId = num;
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
        if (this.eventId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.eventId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.operatorId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.operatorId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.itemId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.itemId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ruleId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.ruleId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.method != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.method);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.methodArgs != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.methodArgs);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.condition != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.condition);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.seqId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.seqId.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<EventProcessMethod> getHelper() {
        return EventProcessMethodHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "EventProcessMethod { id: " + this.id + ", eventId: " + this.eventId + ", operatorId: " + this.operatorId + ", itemId: " + this.itemId + ", ruleId: " + this.ruleId + ", method: " + this.method + ", methodArgs: " + this.methodArgs + ", condition: " + this.condition + ", seqId: " + this.seqId + " }";
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
