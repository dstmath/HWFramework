package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ActionGroup extends AManagedObject {
    public static final Parcelable.Creator<ActionGroup> CREATOR = new Parcelable.Creator<ActionGroup>() {
        /* class com.huawei.nb.model.rule.ActionGroup.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ActionGroup createFromParcel(Parcel parcel) {
            return new ActionGroup(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ActionGroup[] newArray(int i) {
            return new ActionGroup[i];
        }
    };
    private Integer actionNumber;
    private Long id;
    private Integer relationType;
    private Long ruleId;

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
        return "com.huawei.nb.model.rule.ActionGroup";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public ActionGroup(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.ruleId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.actionNumber = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.relationType = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
    }

    public ActionGroup(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.ruleId = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.actionNumber = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.relationType = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private ActionGroup(Long l, Long l2, Integer num, Integer num2) {
        this.id = l;
        this.ruleId = l2;
        this.actionNumber = num;
        this.relationType = num2;
    }

    public ActionGroup() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long l) {
        this.ruleId = l;
        setValue();
    }

    public Integer getActionNumber() {
        return this.actionNumber;
    }

    public void setActionNumber(Integer num) {
        this.actionNumber = num;
        setValue();
    }

    public Integer getRelationType() {
        return this.relationType;
    }

    public void setRelationType(Integer num) {
        this.relationType = num;
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
        if (this.ruleId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.ruleId.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.actionNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.actionNumber.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.relationType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.relationType.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<ActionGroup> getHelper() {
        return ActionGroupHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "ActionGroup { id: " + this.id + ", ruleId: " + this.ruleId + ", actionNumber: " + this.actionNumber + ", relationType: " + this.relationType + " }";
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
