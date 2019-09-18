package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class EventProcessMethod extends AManagedObject {
    public static final Parcelable.Creator<EventProcessMethod> CREATOR = new Parcelable.Creator<EventProcessMethod>() {
        public EventProcessMethod createFromParcel(Parcel in) {
            return new EventProcessMethod(in);
        }

        public EventProcessMethod[] newArray(int size) {
            return new EventProcessMethod[size];
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

    public EventProcessMethod(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public EventProcessMethod(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.eventId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.operatorId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.itemId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.ruleId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.method = in.readByte() == 0 ? null : in.readString();
        this.methodArgs = in.readByte() == 0 ? null : in.readString();
        this.condition = in.readByte() == 0 ? null : in.readString();
        this.seqId = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private EventProcessMethod(Long id2, Long eventId2, Long operatorId2, Long itemId2, Long ruleId2, String method2, String methodArgs2, String condition2, Integer seqId2) {
        this.id = id2;
        this.eventId = eventId2;
        this.operatorId = operatorId2;
        this.itemId = itemId2;
        this.ruleId = ruleId2;
        this.method = method2;
        this.methodArgs = methodArgs2;
        this.condition = condition2;
        this.seqId = seqId2;
    }

    public EventProcessMethod() {
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

    public Long getEventId() {
        return this.eventId;
    }

    public void setEventId(Long eventId2) {
        this.eventId = eventId2;
        setValue();
    }

    public Long getOperatorId() {
        return this.operatorId;
    }

    public void setOperatorId(Long operatorId2) {
        this.operatorId = operatorId2;
        setValue();
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId2) {
        this.itemId = itemId2;
        setValue();
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long ruleId2) {
        this.ruleId = ruleId2;
        setValue();
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method2) {
        this.method = method2;
        setValue();
    }

    public String getMethodArgs() {
        return this.methodArgs;
    }

    public void setMethodArgs(String methodArgs2) {
        this.methodArgs = methodArgs2;
        setValue();
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition2) {
        this.condition = condition2;
        setValue();
    }

    public Integer getSeqId() {
        return this.seqId;
    }

    public void setSeqId(Integer seqId2) {
        this.seqId = seqId2;
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
        if (this.eventId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.eventId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.operatorId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.operatorId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.itemId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.itemId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ruleId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.ruleId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.method != null) {
            out.writeByte((byte) 1);
            out.writeString(this.method);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.methodArgs != null) {
            out.writeByte((byte) 1);
            out.writeString(this.methodArgs);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.condition != null) {
            out.writeByte((byte) 1);
            out.writeString(this.condition);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.seqId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.seqId.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<EventProcessMethod> getHelper() {
        return EventProcessMethodHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.EventProcessMethod";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("EventProcessMethod { id: ").append(this.id);
        sb.append(", eventId: ").append(this.eventId);
        sb.append(", operatorId: ").append(this.operatorId);
        sb.append(", itemId: ").append(this.itemId);
        sb.append(", ruleId: ").append(this.ruleId);
        sb.append(", method: ").append(this.method);
        sb.append(", methodArgs: ").append(this.methodArgs);
        sb.append(", condition: ").append(this.condition);
        sb.append(", seqId: ").append(this.seqId);
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
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
