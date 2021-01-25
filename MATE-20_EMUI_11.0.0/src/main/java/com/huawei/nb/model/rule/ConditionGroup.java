package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ConditionGroup extends AManagedObject {
    public static final Parcelable.Creator<ConditionGroup> CREATOR = new Parcelable.Creator<ConditionGroup>() {
        /* class com.huawei.nb.model.rule.ConditionGroup.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ConditionGroup createFromParcel(Parcel parcel) {
            return new ConditionGroup(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ConditionGroup[] newArray(int i) {
            return new ConditionGroup[i];
        }
    };
    private Integer conditionNumber;
    private Integer conditionType;
    private Long groupId;
    private Long id;
    private Long relationId;
    private Integer relationType;

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
        return "com.huawei.nb.model.rule.ConditionGroup";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ConditionGroup(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.groupId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.relationId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.conditionNumber = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.relationType = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.conditionType = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
    }

    public ConditionGroup(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.groupId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.relationId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.conditionNumber = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.relationType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.conditionType = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private ConditionGroup(Long l, Long l2, Long l3, Integer num, Integer num2, Integer num3) {
        this.id = l;
        this.groupId = l2;
        this.relationId = l3;
        this.conditionNumber = num;
        this.relationType = num2;
        this.conditionType = num3;
    }

    public ConditionGroup() {
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

    public Long getRelationId() {
        return this.relationId;
    }

    public void setRelationId(Long l) {
        this.relationId = l;
        setValue();
    }

    public Integer getConditionNumber() {
        return this.conditionNumber;
    }

    public void setConditionNumber(Integer num) {
        this.conditionNumber = num;
        setValue();
    }

    public Integer getRelationType() {
        return this.relationType;
    }

    public void setRelationType(Integer num) {
        this.relationType = num;
        setValue();
    }

    public Integer getConditionType() {
        return this.conditionType;
    }

    public void setConditionType(Integer num) {
        this.conditionType = num;
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
        if (this.relationId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.relationId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.conditionNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.conditionNumber.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.relationType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.relationType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.conditionType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.conditionType.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ConditionGroup> getHelper() {
        return ConditionGroupHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ConditionGroup { id: " + this.id + ", groupId: " + this.groupId + ", relationId: " + this.relationId + ", conditionNumber: " + this.conditionNumber + ", relationType: " + this.relationType + ", conditionType: " + this.conditionType + " }";
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
