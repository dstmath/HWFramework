package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfig extends AManagedObject {
    public static final Parcelable.Creator<AiModelConfig> CREATOR = new Parcelable.Creator<AiModelConfig>() {
        /* class com.huawei.nb.model.aimodel.AiModelConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelConfig createFromParcel(Parcel parcel) {
            return new AiModelConfig(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelConfig[] newArray(int i) {
            return new AiModelConfig[i];
        }
    };
    private String data_path;
    private Long id;
    private String key_path;
    private String reserved_1;
    private Long status;
    private Long type;
    private Long version;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModelConfig";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelConfig(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.data_path = cursor.getString(2);
        this.key_path = cursor.getString(3);
        this.version = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.type = cursor.isNull(5) ? null : Long.valueOf(cursor.getLong(5));
        this.status = !cursor.isNull(6) ? Long.valueOf(cursor.getLong(6)) : l;
        this.reserved_1 = cursor.getString(7);
    }

    public AiModelConfig(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.data_path = parcel.readByte() == 0 ? null : parcel.readString();
        this.key_path = parcel.readByte() == 0 ? null : parcel.readString();
        this.version = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.type = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.status = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.reserved_1 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private AiModelConfig(Long l, String str, String str2, Long l2, Long l3, Long l4, String str3) {
        this.id = l;
        this.data_path = str;
        this.key_path = str2;
        this.version = l2;
        this.type = l3;
        this.status = l4;
        this.reserved_1 = str3;
    }

    public AiModelConfig() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getData_path() {
        return this.data_path;
    }

    public void setData_path(String str) {
        this.data_path = str;
        setValue();
    }

    public String getKey_path() {
        return this.key_path;
    }

    public void setKey_path(String str) {
        this.key_path = str;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long l) {
        this.version = l;
        setValue();
    }

    public Long getType() {
        return this.type;
    }

    public void setType(Long l) {
        this.type = l;
        setValue();
    }

    public Long getStatus() {
        return this.status;
    }

    public void setStatus(Long l) {
        this.status = l;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String str) {
        this.reserved_1 = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.data_path != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.data_path);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.key_path != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.key_path);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.version != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.version.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.type.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.status != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.status.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved_1);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModelConfig> getHelper() {
        return AiModelConfigHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelConfig { id: " + this.id + ", data_path: " + this.data_path + ", key_path: " + this.key_path + ", version: " + this.version + ", type: " + this.type + ", status: " + this.status + ", reserved_1: " + this.reserved_1 + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
