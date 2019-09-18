package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Business extends AManagedObject {
    public static final Parcelable.Creator<Business> CREATOR = new Parcelable.Creator<Business>() {
        public Business createFromParcel(Parcel in) {
            return new Business(in);
        }

        public Business[] newArray(int size) {
            return new Business[size];
        }
    };
    private Integer businessType;
    private String description;
    private Long id;
    private Integer level;
    private String name;
    private Long parentId;

    public Business(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.businessType = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.level = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.description = cursor.getString(5);
        this.parentId = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Business(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.name = in.readByte() == 0 ? null : in.readString();
        this.businessType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.level = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.description = in.readByte() == 0 ? null : in.readString();
        this.parentId = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private Business(Long id2, String name2, Integer businessType2, Integer level2, String description2, Long parentId2) {
        this.id = id2;
        this.name = name2;
        this.businessType = businessType2;
        this.level = level2;
        this.description = description2;
        this.parentId = parentId2;
    }

    public Business() {
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

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
        setValue();
    }

    public Integer getBusinessType() {
        return this.businessType;
    }

    public void setBusinessType(Integer businessType2) {
        this.businessType = businessType2;
        setValue();
    }

    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(Integer level2) {
        this.level = level2;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
        setValue();
    }

    public Long getParentId() {
        return this.parentId;
    }

    public void setParentId(Long parentId2) {
        this.parentId = parentId2;
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
        if (this.name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.businessType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.businessType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.level != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.level.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.description != null) {
            out.writeByte((byte) 1);
            out.writeString(this.description);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.parentId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.parentId.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<Business> getHelper() {
        return BusinessHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.Business";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Business { id: ").append(this.id);
        sb.append(", name: ").append(this.name);
        sb.append(", businessType: ").append(this.businessType);
        sb.append(", level: ").append(this.level);
        sb.append(", description: ").append(this.description);
        sb.append(", parentId: ").append(this.parentId);
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
