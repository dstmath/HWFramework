package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Action extends AManagedObject {
    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        /* class com.huawei.nb.model.rule.Action.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Action createFromParcel(Parcel parcel) {
            return new Action(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Action[] newArray(int i) {
            return new Action[i];
        }
    };
    private Integer actionType;
    private String extraInfo;
    private Long groupId;
    private Long id;
    private Long itemId;
    private Long operatorId;
    private String value;

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
        return "com.huawei.nb.model.rule.Action";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Action(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.groupId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.operatorId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.itemId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.value = cursor.getString(5);
        this.extraInfo = cursor.getString(6);
        this.actionType = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
    }

    public Action(Parcel parcel) {
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
        this.value = parcel.readByte() == 0 ? null : parcel.readString();
        this.extraInfo = parcel.readByte() == 0 ? null : parcel.readString();
        this.actionType = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private Action(Long l, Long l2, Long l3, Long l4, String str, String str2, Integer num) {
        this.id = l;
        this.groupId = l2;
        this.operatorId = l3;
        this.itemId = l4;
        this.value = str;
        this.extraInfo = str2;
        this.actionType = num;
    }

    public Action() {
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

    public String getValue() {
        return this.value;
    }

    public void setValue(String str) {
        this.value = str;
        setValue();
    }

    public String getExtraInfo() {
        return this.extraInfo;
    }

    public void setExtraInfo(String str) {
        this.extraInfo = str;
        setValue();
    }

    public Integer getActionType() {
        return this.actionType;
    }

    public void setActionType(Integer num) {
        this.actionType = num;
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
        if (this.value != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.value);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.extraInfo != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.extraInfo);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.actionType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.actionType.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Action> getHelper() {
        return ActionHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Action { id: " + this.id + ", groupId: " + this.groupId + ", operatorId: " + this.operatorId + ", itemId: " + this.itemId + ", value: " + this.value + ", extraInfo: " + this.extraInfo + ", actionType: " + this.actionType + " }";
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
