package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelVersionControl extends AManagedObject {
    public static final Parcelable.Creator<AiModelVersionControl> CREATOR = new Parcelable.Creator<AiModelVersionControl>() {
        /* class com.huawei.nb.model.aimodel.AiModelVersionControl.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelVersionControl createFromParcel(Parcel parcel) {
            return new AiModelVersionControl(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelVersionControl[] newArray(int i) {
            return new AiModelVersionControl[i];
        }
    };
    private Long aimodel_id;
    private Long current_version;
    private Long id;

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
        return "com.huawei.nb.model.aimodel.AiModelVersionControl";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public AiModelVersionControl(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.aimodel_id = cursor.isNull(2) ? null : Long.valueOf(cursor.getLong(2));
        this.current_version = !cursor.isNull(3) ? Long.valueOf(cursor.getLong(3)) : l;
    }

    public AiModelVersionControl(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.aimodel_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.current_version = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private AiModelVersionControl(Long l, Long l2, Long l3) {
        this.id = l;
        this.aimodel_id = l2;
        this.current_version = l3;
    }

    public AiModelVersionControl() {
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

    public Long getCurrent_version() {
        return this.current_version;
    }

    public void setCurrent_version(Long l) {
        this.current_version = l;
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
        if (this.current_version != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.current_version.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModelVersionControl> getHelper() {
        return AiModelVersionControlHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModelVersionControl { id: " + this.id + ", aimodel_id: " + this.aimodel_id + ", current_version: " + this.current_version + " }";
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
