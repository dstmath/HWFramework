package com.huawei.nb.model.rule;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Operator extends AManagedObject {
    public static final Parcelable.Creator<Operator> CREATOR = new Parcelable.Creator<Operator>() {
        /* class com.huawei.nb.model.rule.Operator.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Operator createFromParcel(Parcel parcel) {
            return new Operator(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Operator[] newArray(int i) {
            return new Operator[i];
        }
    };
    private Long id;
    private String name;
    private Long parentId;
    private String type;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsRule";
    }

    public String getDatabaseVersion() {
        return "0.0.4";
    }

    public int getDatabaseVersionCode() {
        return 4;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.rule.Operator";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Operator(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Long l = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.name = cursor.getString(2);
        this.type = cursor.getString(3);
        this.parentId = !cursor.isNull(4) ? Long.valueOf(cursor.getLong(4)) : l;
    }

    public Operator(Parcel parcel) {
        super(parcel);
        Long l = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.type = parcel.readByte() == 0 ? null : parcel.readString();
        this.parentId = parcel.readByte() != 0 ? Long.valueOf(parcel.readLong()) : l;
    }

    private Operator(Long l, String str, String str2, Long l2) {
        this.id = l;
        this.name = str;
        this.type = str2;
        this.parentId = l2;
    }

    public Operator() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public String getType() {
        return this.type;
    }

    public void setType(String str) {
        this.type = str;
        setValue();
    }

    public Long getParentId() {
        return this.parentId;
    }

    public void setParentId(Long l) {
        this.parentId = l;
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
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.parentId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.parentId.longValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Operator> getHelper() {
        return OperatorHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Operator { id: " + this.id + ", name: " + this.name + ", type: " + this.type + ", parentId: " + this.parentId + " }";
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
