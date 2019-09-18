package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class UserInfo extends AManagedObject {
    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
    private Integer id;
    private Integer reservedInt1;
    private Integer reservedInt2;
    private String reservedStr1;
    private String reservedStr2;
    private Integer userId;
    private Long userSn;

    public UserInfo(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.userId = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.userSn = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.reservedInt1 = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.reservedInt2 = !cursor.isNull(5) ? Integer.valueOf(cursor.getInt(5)) : num;
        this.reservedStr1 = cursor.getString(6);
        this.reservedStr2 = cursor.getString(7);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UserInfo(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.userId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.userSn = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.reservedInt1 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reservedInt2 = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.reservedStr1 = in.readByte() == 0 ? null : in.readString();
        this.reservedStr2 = in.readByte() != 0 ? in.readString() : str;
    }

    private UserInfo(Integer id2, Integer userId2, Long userSn2, Integer reservedInt12, Integer reservedInt22, String reservedStr12, String reservedStr22) {
        this.id = id2;
        this.userId = userId2;
        this.userSn = userSn2;
        this.reservedInt1 = reservedInt12;
        this.reservedInt2 = reservedInt22;
        this.reservedStr1 = reservedStr12;
        this.reservedStr2 = reservedStr22;
    }

    public UserInfo() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId2) {
        this.userId = userId2;
        setValue();
    }

    public Long getUserSn() {
        return this.userSn;
    }

    public void setUserSn(Long userSn2) {
        this.userSn = userSn2;
        setValue();
    }

    public Integer getReservedInt1() {
        return this.reservedInt1;
    }

    public void setReservedInt1(Integer reservedInt12) {
        this.reservedInt1 = reservedInt12;
        setValue();
    }

    public Integer getReservedInt2() {
        return this.reservedInt2;
    }

    public void setReservedInt2(Integer reservedInt22) {
        this.reservedInt2 = reservedInt22;
        setValue();
    }

    public String getReservedStr1() {
        return this.reservedStr1;
    }

    public void setReservedStr1(String reservedStr12) {
        this.reservedStr1 = reservedStr12;
        setValue();
    }

    public String getReservedStr2() {
        return this.reservedStr2;
    }

    public void setReservedStr2(String reservedStr22) {
        this.reservedStr2 = reservedStr22;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.userId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.userId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.userSn != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.userSn.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reservedInt1 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.reservedInt1.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reservedInt2 != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.reservedInt2.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reservedStr1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reservedStr1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reservedStr2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reservedStr2);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<UserInfo> getHelper() {
        return UserInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.UserInfo";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("UserInfo { id: ").append(this.id);
        sb.append(", userId: ").append(this.userId);
        sb.append(", userSn: ").append(this.userSn);
        sb.append(", reservedInt1: ").append(this.reservedInt1);
        sb.append(", reservedInt2: ").append(this.reservedInt2);
        sb.append(", reservedStr1: ").append(this.reservedStr1);
        sb.append(", reservedStr2: ").append(this.reservedStr2);
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
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
