package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Action extends AManagedObject {
    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        public Action[] newArray(int size) {
            return new Action[size];
        }
    };
    private Integer actionType;
    private String extraInfo;
    private Long groupId;
    private Long id;
    private Long itemId;
    private Long operatorId;
    private String value;

    public Action(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.groupId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.operatorId = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.itemId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.value = cursor.getString(5);
        this.extraInfo = cursor.getString(6);
        this.actionType = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Action(Parcel in) {
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
        this.value = in.readByte() == 0 ? null : in.readString();
        this.extraInfo = in.readByte() == 0 ? null : in.readString();
        this.actionType = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private Action(Long id2, Long groupId2, Long operatorId2, Long itemId2, String value2, String extraInfo2, Integer actionType2) {
        this.id = id2;
        this.groupId = groupId2;
        this.operatorId = operatorId2;
        this.itemId = itemId2;
        this.value = value2;
        this.extraInfo = extraInfo2;
        this.actionType = actionType2;
    }

    public Action() {
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

    public String getValue() {
        return this.value;
    }

    public void setValue(String value2) {
        this.value = value2;
        setValue();
    }

    public String getExtraInfo() {
        return this.extraInfo;
    }

    public void setExtraInfo(String extraInfo2) {
        this.extraInfo = extraInfo2;
        setValue();
    }

    public Integer getActionType() {
        return this.actionType;
    }

    public void setActionType(Integer actionType2) {
        this.actionType = actionType2;
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
        if (this.value != null) {
            out.writeByte((byte) 1);
            out.writeString(this.value);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.extraInfo != null) {
            out.writeByte((byte) 1);
            out.writeString(this.extraInfo);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.actionType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.actionType.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<Action> getHelper() {
        return ActionHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.Action";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Action { id: ").append(this.id);
        sb.append(", groupId: ").append(this.groupId);
        sb.append(", operatorId: ").append(this.operatorId);
        sb.append(", itemId: ").append(this.itemId);
        sb.append(", value: ").append(this.value);
        sb.append(", extraInfo: ").append(this.extraInfo);
        sb.append(", actionType: ").append(this.actionType);
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
