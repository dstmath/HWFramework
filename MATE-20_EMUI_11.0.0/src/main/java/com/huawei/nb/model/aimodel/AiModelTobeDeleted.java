package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelTobeDeleted extends AManagedObject {
    public static final Parcelable.Creator<AiModelTobeDeleted> CREATOR = new Parcelable.Creator<AiModelTobeDeleted>() {
        /* class com.huawei.nb.model.aimodel.AiModelTobeDeleted.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelTobeDeleted createFromParcel(Parcel parcel) {
            return new AiModelTobeDeleted(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelTobeDeleted[] newArray(int i) {
            return new AiModelTobeDeleted[i];
        }
    };
    private Long aimodel_id;
    private String file_path;
    private Long id;
    private String name;
    private Long time_expired;

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
        return "com.huawei.nb.model.aimodel.AiModelTobeDeleted";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelTobeDeleted(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.aimodel_id = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.name = cursor.getString(3);
        this.file_path = cursor.getString(4);
        this.time_expired = !cursor.isNull(5) ? Long.valueOf(cursor.getLong(5)) : l;
    }

    public AiModelTobeDeleted(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.aimodel_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.file_path = parcel.readByte() == 0 ? null : parcel.readString();
        this.time_expired = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private AiModelTobeDeleted(Long l, Long l2, String str, String str2, Long l3) {
        this.id = l;
        this.aimodel_id = l2;
        this.name = str;
        this.file_path = str2;
        this.time_expired = l3;
    }

    public AiModelTobeDeleted() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public Long getAimodel_id() {
        return this.aimodel_id;
    }

    public void setAimodel_id(Long l) {
        this.aimodel_id = l;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public String getFile_path() {
        return this.file_path;
    }

    public void setFile_path(String str) {
        this.file_path = str;
        setValue();
    }

    public Long getTime_expired() {
        return this.time_expired;
    }

    public void setTime_expired(Long l) {
        this.time_expired = l;
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
        if (this.aimodel_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.aimodel_id.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.file_path != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.file_path);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.time_expired != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.time_expired.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModelTobeDeleted> getHelper() {
        return AiModelTobeDeletedHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelTobeDeleted { id: " + this.id + ", aimodel_id: " + this.aimodel_id + ", name: " + this.name + ", file_path: " + this.file_path + ", time_expired: " + this.time_expired + " }";
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
