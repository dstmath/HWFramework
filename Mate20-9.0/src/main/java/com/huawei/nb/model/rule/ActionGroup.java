package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ActionGroup extends AManagedObject {
    public static final Parcelable.Creator<ActionGroup> CREATOR = new Parcelable.Creator<ActionGroup>() {
        public ActionGroup createFromParcel(Parcel in) {
            return new ActionGroup(in);
        }

        public ActionGroup[] newArray(int size) {
            return new ActionGroup[size];
        }
    };
    private Integer actionNumber;
    private Long id;
    private Integer relationType;
    private Long ruleId;

    public ActionGroup(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.ruleId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.actionNumber = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.relationType = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ActionGroup(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.ruleId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.actionNumber = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.relationType = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private ActionGroup(Long id2, Long ruleId2, Integer actionNumber2, Integer relationType2) {
        this.id = id2;
        this.ruleId = ruleId2;
        this.actionNumber = actionNumber2;
        this.relationType = relationType2;
    }

    public ActionGroup() {
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

    public Long getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(Long ruleId2) {
        this.ruleId = ruleId2;
        setValue();
    }

    public Integer getActionNumber() {
        return this.actionNumber;
    }

    public void setActionNumber(Integer actionNumber2) {
        this.actionNumber = actionNumber2;
        setValue();
    }

    public Integer getRelationType() {
        return this.relationType;
    }

    public void setRelationType(Integer relationType2) {
        this.relationType = relationType2;
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
        if (this.ruleId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.ruleId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.actionNumber != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.actionNumber.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.relationType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.relationType.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ActionGroup> getHelper() {
        return ActionGroupHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.ActionGroup";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ActionGroup { id: ").append(this.id);
        sb.append(", ruleId: ").append(this.ruleId);
        sb.append(", actionNumber: ").append(this.actionNumber);
        sb.append(", relationType: ").append(this.relationType);
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
