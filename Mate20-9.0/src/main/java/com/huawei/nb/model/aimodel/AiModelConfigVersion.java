package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfigVersion extends AManagedObject {
    public static final Parcelable.Creator<AiModelConfigVersion> CREATOR = new Parcelable.Creator<AiModelConfigVersion>() {
        public AiModelConfigVersion createFromParcel(Parcel in) {
            return new AiModelConfigVersion(in);
        }

        public AiModelConfigVersion[] newArray(int size) {
            return new AiModelConfigVersion[size];
        }
    };
    private Long id;
    private Long type;
    private Long version;

    public AiModelConfigVersion(Cursor cursor) {
        Long l = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.type = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.version = !cursor.isNull(3) ? Long.valueOf(cursor.getLong(3)) : l;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AiModelConfigVersion(Parcel in) {
        super(in);
        Long l = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.type = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.version = in.readByte() != 0 ? Long.valueOf(in.readLong()) : l;
    }

    private AiModelConfigVersion(Long id2, Long type2, Long version2) {
        this.id = id2;
        this.type = type2;
        this.version = version2;
    }

    public AiModelConfigVersion() {
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

    public Long getType() {
        return this.type;
    }

    public void setType(Long type2) {
        this.type = type2;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version2) {
        this.version = version2;
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
        if (this.type != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.type.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.version != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.version.longValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<AiModelConfigVersion> getHelper() {
        return AiModelConfigVersionHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelConfigVersion";
    }

    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AiModelConfigVersion { id: ").append(this.id);
        sb.append(", type: ").append(this.type);
        sb.append(", version: ").append(this.version);
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
