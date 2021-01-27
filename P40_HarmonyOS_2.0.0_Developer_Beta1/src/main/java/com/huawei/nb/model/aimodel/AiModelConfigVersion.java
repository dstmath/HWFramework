package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfigVersion extends AManagedObject {
    public static final Parcelable.Creator<AiModelConfigVersion> CREATOR = new Parcelable.Creator<AiModelConfigVersion>() {
        /* class com.huawei.nb.model.aimodel.AiModelConfigVersion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelConfigVersion createFromParcel(Parcel parcel) {
            return new AiModelConfigVersion(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelConfigVersion[] newArray(int i) {
            return new AiModelConfigVersion[i];
        }
    };
    private Long id;
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
        return "com.huawei.nb.model.aimodel.AiModelConfigVersion";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelConfigVersion(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.type = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.version = !cursor.isNull(3) ? Long.valueOf(cursor.getLong(3)) : l;
    }

    public AiModelConfigVersion(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.type = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.version = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private AiModelConfigVersion(Long l, Long l2, Long l3) {
        this.id = l;
        this.type = l2;
        this.version = l3;
    }

    public AiModelConfigVersion() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getType() {
        return this.type;
    }

    public void setType(Long l) {
        this.type = l;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long l) {
        this.version = l;
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
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.type.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.version != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.version.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModelConfigVersion> getHelper() {
        return AiModelConfigVersionHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelConfigVersion { id: " + this.id + ", type: " + this.type + ", version: " + this.version + " }";
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
