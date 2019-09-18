package com.huawei.nb.model.authority;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ZFieldAuthorityGrant extends AManagedObject {
    public static final Parcelable.Creator<ZFieldAuthorityGrant> CREATOR = new Parcelable.Creator<ZFieldAuthorityGrant>() {
        public ZFieldAuthorityGrant createFromParcel(Parcel in) {
            return new ZFieldAuthorityGrant(in);
        }

        public ZFieldAuthorityGrant[] newArray(int size) {
            return new ZFieldAuthorityGrant[size];
        }
    };
    private Integer authority;
    private String fieldName;
    private Long filedId;
    private Long id;
    private String packageName;
    private Long packageUid;
    private String reserved;
    private Boolean supportGroupAuthority = true;
    private String sysAuthorityName;
    private Long tableId;
    private String tableName;

    public ZFieldAuthorityGrant(Cursor cursor) {
        Boolean bool = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.tableId = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.tableName = cursor.getString(3);
        this.filedId = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.fieldName = cursor.getString(5);
        this.packageUid = cursor.isNull(6) ? null : Long.valueOf(cursor.getLong(6));
        this.packageName = cursor.getString(7);
        this.authority = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.sysAuthorityName = cursor.getString(9);
        if (!cursor.isNull(10)) {
            bool = Boolean.valueOf(cursor.getInt(10) != 0);
        }
        this.supportGroupAuthority = bool;
        this.reserved = cursor.getString(11);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ZFieldAuthorityGrant(Parcel in) {
        super(in);
        Boolean valueOf;
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.tableId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.tableName = in.readByte() == 0 ? null : in.readString();
        this.filedId = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.fieldName = in.readByte() == 0 ? null : in.readString();
        this.packageUid = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.packageName = in.readByte() == 0 ? null : in.readString();
        this.authority = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.sysAuthorityName = in.readByte() == 0 ? null : in.readString();
        if (in.readByte() == 0) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(in.readByte() != 0);
        }
        this.supportGroupAuthority = valueOf;
        this.reserved = in.readByte() != 0 ? in.readString() : str;
    }

    private ZFieldAuthorityGrant(Long id2, Long tableId2, String tableName2, Long filedId2, String fieldName2, Long packageUid2, String packageName2, Integer authority2, String sysAuthorityName2, Boolean supportGroupAuthority2, String reserved2) {
        this.id = id2;
        this.tableId = tableId2;
        this.tableName = tableName2;
        this.filedId = filedId2;
        this.fieldName = fieldName2;
        this.packageUid = packageUid2;
        this.packageName = packageName2;
        this.authority = authority2;
        this.sysAuthorityName = sysAuthorityName2;
        this.supportGroupAuthority = supportGroupAuthority2;
        this.reserved = reserved2;
    }

    public ZFieldAuthorityGrant() {
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

    public Long getTableId() {
        return this.tableId;
    }

    public void setTableId(Long tableId2) {
        this.tableId = tableId2;
        setValue();
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName2) {
        this.tableName = tableName2;
        setValue();
    }

    public Long getFiledId() {
        return this.filedId;
    }

    public void setFiledId(Long filedId2) {
        this.filedId = filedId2;
        setValue();
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName2) {
        this.fieldName = fieldName2;
        setValue();
    }

    public Long getPackageUid() {
        return this.packageUid;
    }

    public void setPackageUid(Long packageUid2) {
        this.packageUid = packageUid2;
        setValue();
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
        setValue();
    }

    public Integer getAuthority() {
        return this.authority;
    }

    public void setAuthority(Integer authority2) {
        this.authority = authority2;
        setValue();
    }

    public String getSysAuthorityName() {
        return this.sysAuthorityName;
    }

    public void setSysAuthorityName(String sysAuthorityName2) {
        this.sysAuthorityName = sysAuthorityName2;
        setValue();
    }

    public Boolean getSupportGroupAuthority() {
        return this.supportGroupAuthority;
    }

    public void setSupportGroupAuthority(Boolean supportGroupAuthority2) {
        this.supportGroupAuthority = supportGroupAuthority2;
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
        if (this.tableId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.tableId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tableName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tableName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.filedId != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.filedId.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.fieldName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.fieldName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.packageUid != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.packageUid.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.packageName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.packageName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.authority != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.authority.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.sysAuthorityName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.sysAuthorityName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.supportGroupAuthority != null) {
            out.writeByte((byte) 1);
            if (this.supportGroupAuthority.booleanValue()) {
                b = 1;
            } else {
                b = 0;
            }
            out.writeByte(b);
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

    public AEntityHelper<ZFieldAuthorityGrant> getHelper() {
        return ZFieldAuthorityGrantHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.authority.ZFieldAuthorityGrant";
    }

    public String getDatabaseName() {
        return "dsWeather";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ZFieldAuthorityGrant { id: ").append(this.id);
        sb.append(", tableId: ").append(this.tableId);
        sb.append(", tableName: ").append(this.tableName);
        sb.append(", filedId: ").append(this.filedId);
        sb.append(", fieldName: ").append(this.fieldName);
        sb.append(", packageUid: ").append(this.packageUid);
        sb.append(", packageName: ").append(this.packageName);
        sb.append(", authority: ").append(this.authority);
        sb.append(", sysAuthorityName: ").append(this.sysAuthorityName);
        sb.append(", supportGroupAuthority: ").append(this.supportGroupAuthority);
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
