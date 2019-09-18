package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class Item extends AManagedObject {
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
    private Long deviceId;
    private Long id;
    private Date installTime;
    private String name;
    private Long parentId;
    private String type;

    public Item(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.type = cursor.getString(3);
        this.installTime = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.deviceId = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.parentId = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Item(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.name = in.readByte() == 0 ? null : in.readString();
        this.type = in.readByte() == 0 ? null : in.readString();
        this.installTime = in.readByte() == 0 ? null : new Date(in.readLong());
        this.deviceId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.parentId = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private Item(Long id2, String name2, String type2, Date installTime2, Long deviceId2, Long parentId2) {
        this.id = id2;
        this.name = name2;
        this.type = type2;
        this.installTime = installTime2;
        this.deviceId = deviceId2;
        this.parentId = parentId2;
    }

    public Item() {
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

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
        setValue();
    }

    public Date getInstallTime() {
        return this.installTime;
    }

    public void setInstallTime(Date installTime2) {
        this.installTime = installTime2;
        setValue();
    }

    public Long getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(Long deviceId2) {
        this.deviceId = deviceId2;
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
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.type);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.installTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.installTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.deviceId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.deviceId.longValue());
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

    public AEntityHelper<Item> getHelper() {
        return ItemHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.rule.Item";
    }

    public String getDatabaseName() {
        return "dsRule";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Item { id: ").append(this.id);
        sb.append(", name: ").append(this.name);
        sb.append(", type: ").append(this.type);
        sb.append(", installTime: ").append(this.installTime);
        sb.append(", deviceId: ").append(this.deviceId);
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
