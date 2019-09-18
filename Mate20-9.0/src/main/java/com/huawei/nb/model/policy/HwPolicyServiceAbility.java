package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class HwPolicyServiceAbility extends AManagedObject {
    public static final Parcelable.Creator<HwPolicyServiceAbility> CREATOR = new Parcelable.Creator<HwPolicyServiceAbility>() {
        public HwPolicyServiceAbility createFromParcel(Parcel in) {
            return new HwPolicyServiceAbility(in);
        }

        public HwPolicyServiceAbility[] newArray(int size) {
            return new HwPolicyServiceAbility[size];
        }
    };
    private Long id;
    private String name;
    private String reserve;
    private String type;
    private Long versionCode;
    private String versionName;

    public HwPolicyServiceAbility(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.type = cursor.getString(3);
        this.versionCode = !cursor.isNull(4) ? Long.valueOf(cursor.getLong(4)) : l;
        this.versionName = cursor.getString(5);
        this.reserve = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HwPolicyServiceAbility(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.name = in.readByte() == 0 ? null : in.readString();
        this.type = in.readByte() == 0 ? null : in.readString();
        this.versionCode = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.versionName = in.readByte() == 0 ? null : in.readString();
        this.reserve = in.readByte() != 0 ? in.readString() : str;
    }

    private HwPolicyServiceAbility(Long id2, String name2, String type2, Long versionCode2, String versionName2, String reserve2) {
        this.id = id2;
        this.name = name2;
        this.type = type2;
        this.versionCode = versionCode2;
        this.versionName = versionName2;
        this.reserve = reserve2;
    }

    public HwPolicyServiceAbility() {
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

    public Long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Long versionCode2) {
        this.versionCode = versionCode2;
        setValue();
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName2) {
        this.versionName = versionName2;
        setValue();
    }

    public String getReserve() {
        return this.reserve;
    }

    public void setReserve(String reserve2) {
        this.reserve = reserve2;
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
        if (this.versionCode != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.versionCode.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.versionName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.versionName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserve != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserve);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<HwPolicyServiceAbility> getHelper() {
        return HwPolicyServiceAbilityHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.policy.HwPolicyServiceAbility";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("HwPolicyServiceAbility { id: ").append(this.id);
        sb.append(", name: ").append(this.name);
        sb.append(", type: ").append(this.type);
        sb.append(", versionCode: ").append(this.versionCode);
        sb.append(", versionName: ").append(this.versionName);
        sb.append(", reserve: ").append(this.reserve);
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
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
