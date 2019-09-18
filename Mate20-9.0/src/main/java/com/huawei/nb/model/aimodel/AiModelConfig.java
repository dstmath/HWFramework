package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfig extends AManagedObject {
    public static final Parcelable.Creator<AiModelConfig> CREATOR = new Parcelable.Creator<AiModelConfig>() {
        public AiModelConfig createFromParcel(Parcel in) {
            return new AiModelConfig(in);
        }

        public AiModelConfig[] newArray(int size) {
            return new AiModelConfig[size];
        }
    };
    private String data_path;
    private Long id;
    private String key_path;
    private String reserved_1;
    private Long status;
    private Long type;
    private Long version;

    public AiModelConfig(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.data_path = cursor.getString(2);
        this.key_path = cursor.getString(3);
        this.version = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.type = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.status = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
        this.reserved_1 = cursor.getString(7);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AiModelConfig(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.data_path = in.readByte() == 0 ? null : in.readString();
        this.key_path = in.readByte() == 0 ? null : in.readString();
        this.version = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.type = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.status = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.reserved_1 = in.readByte() != 0 ? in.readString() : str;
    }

    private AiModelConfig(Long id2, String data_path2, String key_path2, Long version2, Long type2, Long status2, String reserved_12) {
        this.id = id2;
        this.data_path = data_path2;
        this.key_path = key_path2;
        this.version = version2;
        this.type = type2;
        this.status = status2;
        this.reserved_1 = reserved_12;
    }

    public AiModelConfig() {
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

    public String getData_path() {
        return this.data_path;
    }

    public void setData_path(String data_path2) {
        this.data_path = data_path2;
        setValue();
    }

    public String getKey_path() {
        return this.key_path;
    }

    public void setKey_path(String key_path2) {
        this.key_path = key_path2;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version2) {
        this.version = version2;
        setValue();
    }

    public Long getType() {
        return this.type;
    }

    public void setType(Long type2) {
        this.type = type2;
        setValue();
    }

    public Long getStatus() {
        return this.status;
    }

    public void setStatus(Long status2) {
        this.status = status2;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String reserved_12) {
        this.reserved_1 = reserved_12;
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
        if (this.data_path != null) {
            out.writeByte((byte) 1);
            out.writeString(this.data_path);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.key_path != null) {
            out.writeByte((byte) 1);
            out.writeString(this.key_path);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.version != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.version.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.type.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.status != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.status.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved_1);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<AiModelConfig> getHelper() {
        return AiModelConfigHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelConfig";
    }

    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AiModelConfig { id: ").append(this.id);
        sb.append(", data_path: ").append(this.data_path);
        sb.append(", key_path: ").append(this.key_path);
        sb.append(", version: ").append(this.version);
        sb.append(", type: ").append(this.type);
        sb.append(", status: ").append(this.status);
        sb.append(", reserved_1: ").append(this.reserved_1);
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
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
