package com.huawei.nb.model.authority;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZGroup extends AManagedObject {
    public static final Parcelable.Creator<ZGroup> CREATOR = new Parcelable.Creator<ZGroup>() {
        public ZGroup createFromParcel(Parcel in) {
            return new ZGroup(in);
        }

        public ZGroup[] newArray(int size) {
            return new ZGroup[size];
        }
    };
    private Integer authority;
    private String groupName;
    private Long id;
    private boolean isGroupIdentifier;
    private String member;
    private String owner;
    private String reserved;
    private String tableName;

    public ZGroup(Cursor cursor) {
        boolean z;
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.groupName = cursor.getString(2);
        this.tableName = cursor.getString(3);
        this.owner = cursor.getString(4);
        if (cursor.getInt(5) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.isGroupIdentifier = z;
        this.member = cursor.getString(6);
        this.authority = !cursor.isNull(7) ? Integer.valueOf(cursor.getInt(7)) : num;
        this.reserved = cursor.getString(8);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ZGroup(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.groupName = in.readByte() == 0 ? null : in.readString();
        this.tableName = in.readByte() == 0 ? null : in.readString();
        this.owner = in.readByte() == 0 ? null : in.readString();
        this.isGroupIdentifier = in.readByte() != 0;
        this.member = in.readByte() == 0 ? null : in.readString();
        this.authority = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reserved = in.readByte() != 0 ? in.readString() : str;
    }

    private ZGroup(Long id2, String groupName2, String tableName2, String owner2, boolean isGroupIdentifier2, String member2, Integer authority2, String reserved2) {
        this.id = id2;
        this.groupName = groupName2;
        this.tableName = tableName2;
        this.owner = owner2;
        this.isGroupIdentifier = isGroupIdentifier2;
        this.member = member2;
        this.authority = authority2;
        this.reserved = reserved2;
    }

    public ZGroup() {
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

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName2) {
        this.groupName = groupName2;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName2) {
        this.tableName = tableName2;
        setValue();
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner2) {
        this.owner = owner2;
        setValue();
    }

    public boolean getIsGroupIdentifier() {
        return this.isGroupIdentifier;
    }

    public void setIsGroupIdentifier(boolean isGroupIdentifier2) {
        this.isGroupIdentifier = isGroupIdentifier2;
        setValue();
    }

    public String getMember() {
        return this.member;
    }

    public void setMember(String member2) {
        this.member = member2;
        setValue();
    }

    public Integer getAuthority() {
        return this.authority;
    }

    public void setAuthority(Integer authority2) {
        this.authority = authority2;
        setValue();
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.groupName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.groupName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tableName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tableName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.owner != null) {
            out.writeByte((byte) 1);
            out.writeString(this.owner);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isGroupIdentifier) {
            b = 1;
        } else {
            b = 0;
        }
        out.writeByte(b);
        if (this.member != null) {
            out.writeByte((byte) 1);
            out.writeString(this.member);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.authority != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.authority.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ZGroup> getHelper() {
        return ZGroupHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.authority.ZGroup";
    }

    public String getDatabaseName() {
        return "dsWeather";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ZGroup { id: ").append(this.id);
        sb.append(", groupName: ").append(this.groupName);
        sb.append(", tableName: ").append(this.tableName);
        sb.append(", owner: ").append(this.owner);
        sb.append(", isGroupIdentifier: ").append(this.isGroupIdentifier);
        sb.append(", member: ").append(this.member);
        sb.append(", authority: ").append(this.authority);
        sb.append(", reserved: ").append(this.reserved);
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
        return "0.0.17";
    }

    public int getDatabaseVersionCode() {
        return 17;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
