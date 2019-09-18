package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class RuleCondition extends AManagedObject {
    public static final Parcelable.Creator<RuleCondition> CREATOR = new Parcelable.Creator<RuleCondition>() {
        public RuleCondition createFromParcel(Parcel in) {
            return new RuleCondition(in);
        }

        public RuleCondition[] newArray(int size) {
            return new RuleCondition[size];
        }
    };
    private Integer eventActAttribute;
    private Integer eventActCompareType;
    private String eventActValue;
    private String eventActValueShift;
    private Integer eventActValueType;
    private Long groupId;
    private Long id;
    private Long itemId;
    private Integer matchMode;
    private Long operatorId;
    private Integer priority;
    private Integer type;

    public RuleCondition(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.groupId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.operatorId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.itemId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.priority = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.matchMode = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.eventActValue = cursor.getString(7);
        this.eventActValueShift = cursor.getString(8);
        this.eventActValueType = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.eventActCompareType = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.eventActAttribute = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.type = !cursor.isNull(12) ? Integer.valueOf(cursor.getInt(12)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RuleCondition(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.groupId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.operatorId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.itemId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.priority = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.matchMode = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.eventActValue = in.readByte() == 0 ? null : in.readString();
        this.eventActValueShift = in.readByte() == 0 ? null : in.readString();
        this.eventActValueType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.eventActCompareType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.eventActAttribute = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.type = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private RuleCondition(Long id2, Long groupId2, Long operatorId2, Long itemId2, Integer priority2, Integer matchMode2, String eventActValue2, String eventActValueShift2, Integer eventActValueType2, Integer eventActCompareType2, Integer eventActAttribute2, Integer type2) {
        this.id = id2;
        this.groupId = groupId2;
        this.operatorId = operatorId2;
        this.itemId = itemId2;
        this.priority = priority2;
        this.matchMode = matchMode2;
        this.eventActValue = eventActValue2;
        this.eventActValueShift = eventActValueShift2;
        this.eventActValueType = eventActValueType2;
        this.eventActCompareType = eventActCompareType2;
        this.eventActAttribute = eventActAttribute2;
        this.type = type2;
    }

    public RuleCondition() {
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

    public Long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Long groupId2) {
        this.groupId = groupId2;
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

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority2) {
        this.priority = priority2;
        setValue();
    }

    public Integer getMatchMode() {
        return this.matchMode;
    }

    public void setMatchMode(Integer matchMode2) {
        this.matchMode = matchMode2;
        setValue();
    }

    public String getEventActValue() {
        return this.eventActValue;
    }

    public void setEventActValue(String eventActValue2) {
        this.eventActValue = eventActValue2;
        setValue();
    }

    public String getEventActValueShift() {
        return this.eventActValueShift;
    }

    public void setEventActValueShift(String eventActValueShift2) {
        this.eventActValueShift = eventActValueShift2;
        setValue();
    }

    public Integer getEventActValueType() {
        return this.eventActValueType;
    }

    public void setEventActValueType(Integer eventActValueType2) {
        this.eventActValueType = eventActValueType2;
        setValue();
    }

    public Integer getEventActCompareType() {
        return this.eventActCompareType;
    }

    public void setEventActCompareType(Integer eventActCompareType2) {
        this.eventActCompareType = eventActCompareType2;
        setValue();
    }

    public Integer getEventActAttribute() {
        return this.eventActAttribute;
    }

    public void setEventActAttribute(Integer eventActAttribute2) {
        this.eventActAttribute = eventActAttribute2;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type2) {
        this.type = type2;
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
        if (this.groupId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.groupId.longValue());
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
        if (this.priority != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.priority.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.matchMode != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.matchMode.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.eventActValue != null) {
            out.writeByte((byte) 1);
            out.writeString(this.eventActValue);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.eventActValueShift != null) {
            out.writeByte((byte) 1);
            out.writeString(this.eventActValueShift);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.eventActValueType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.eventActValueType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.eventActCompareType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.eventActCompareType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.eventActAttribute != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.eventActAttribute.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.type.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RuleCondition> getHelper() {
        return RuleConditionHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.RuleCondition";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RuleCondition { id: ").append(this.id);
        sb.append(", groupId: ").append(this.groupId);
        sb.append(", operatorId: ").append(this.operatorId);
        sb.append(", itemId: ").append(this.itemId);
        sb.append(", priority: ").append(this.priority);
        sb.append(", matchMode: ").append(this.matchMode);
        sb.append(", eventActValue: ").append(this.eventActValue);
        sb.append(", eventActValueShift: ").append(this.eventActValueShift);
        sb.append(", eventActValueType: ").append(this.eventActValueType);
        sb.append(", eventActCompareType: ").append(this.eventActCompareType);
        sb.append(", eventActAttribute: ").append(this.eventActAttribute);
        sb.append(", type: ").append(this.type);
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
