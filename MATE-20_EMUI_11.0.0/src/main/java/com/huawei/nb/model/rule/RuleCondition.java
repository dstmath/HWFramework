package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class RuleCondition extends AManagedObject {
    public static final Parcelable.Creator<RuleCondition> CREATOR = new Parcelable.Creator<RuleCondition>() {
        /* class com.huawei.nb.model.rule.RuleCondition.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RuleCondition createFromParcel(Parcel parcel) {
            return new RuleCondition(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RuleCondition[] newArray(int i) {
            return new RuleCondition[i];
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
        return "com.huawei.nb.model.rule.RuleCondition";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public RuleCondition(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
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

    public RuleCondition(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.groupId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.operatorId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.itemId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.priority = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.matchMode = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.eventActValue = parcel.readByte() == 0 ? null : parcel.readString();
        this.eventActValueShift = parcel.readByte() == 0 ? null : parcel.readString();
        this.eventActValueType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.eventActCompareType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.eventActAttribute = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.type = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private RuleCondition(Long l, Long l2, Long l3, Long l4, Integer num, Integer num2, String str, String str2, Integer num3, Integer num4, Integer num5, Integer num6) {
        this.id = l;
        this.groupId = l2;
        this.operatorId = l3;
        this.itemId = l4;
        this.priority = num;
        this.matchMode = num2;
        this.eventActValue = str;
        this.eventActValueShift = str2;
        this.eventActValueType = num3;
        this.eventActCompareType = num4;
        this.eventActAttribute = num5;
        this.type = num6;
    }

    public RuleCondition() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Long l) {
        this.groupId = l;
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

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer num) {
        this.priority = num;
        setValue();
    }

    public Integer getMatchMode() {
        return this.matchMode;
    }

    public void setMatchMode(Integer num) {
        this.matchMode = num;
        setValue();
    }

    public String getEventActValue() {
        return this.eventActValue;
    }

    public void setEventActValue(String str) {
        this.eventActValue = str;
        setValue();
    }

    public String getEventActValueShift() {
        return this.eventActValueShift;
    }

    public void setEventActValueShift(String str) {
        this.eventActValueShift = str;
        setValue();
    }

    public Integer getEventActValueType() {
        return this.eventActValueType;
    }

    public void setEventActValueType(Integer num) {
        this.eventActValueType = num;
        setValue();
    }

    public Integer getEventActCompareType() {
        return this.eventActCompareType;
    }

    public void setEventActCompareType(Integer num) {
        this.eventActCompareType = num;
        setValue();
    }

    public Integer getEventActAttribute() {
        return this.eventActAttribute;
    }

    public void setEventActAttribute(Integer num) {
        this.eventActAttribute = num;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer num) {
        this.type = num;
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
        if (this.groupId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.groupId.longValue());
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
        if (this.priority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.priority.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.matchMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.matchMode.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.eventActValue != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.eventActValue);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.eventActValueShift != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.eventActValueShift);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.eventActValueType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.eventActValueType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.eventActCompareType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.eventActCompareType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.eventActAttribute != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.eventActAttribute.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.type.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<RuleCondition> getHelper() {
        return RuleConditionHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "RuleCondition { id: " + this.id + ", groupId: " + this.groupId + ", operatorId: " + this.operatorId + ", itemId: " + this.itemId + ", priority: " + this.priority + ", matchMode: " + this.matchMode + ", eventActValue: " + this.eventActValue + ", eventActValueShift: " + this.eventActValueShift + ", eventActValueType: " + this.eventActValueType + ", eventActCompareType: " + this.eventActCompareType + ", eventActAttribute: " + this.eventActAttribute + ", type: " + this.type + " }";
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
