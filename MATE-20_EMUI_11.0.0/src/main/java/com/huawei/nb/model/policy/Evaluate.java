package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Evaluate extends AManagedObject {
    public static final Parcelable.Creator<Evaluate> CREATOR = new Parcelable.Creator<Evaluate>() {
        /* class com.huawei.nb.model.policy.Evaluate.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Evaluate createFromParcel(Parcel parcel) {
            return new Evaluate(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Evaluate[] newArray(int i) {
            return new Evaluate[i];
        }
    };
    private Integer stub;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.policy.Evaluate";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Evaluate(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.stub = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
    }

    public Evaluate(Parcel parcel) {
        super(parcel);
        if (parcel.readByte() == 0) {
            this.stub = null;
            parcel.readInt();
            return;
        }
        this.stub = Integer.valueOf(parcel.readInt());
    }

    private Evaluate(Integer num) {
        this.stub = num;
    }

    public Evaluate() {
    }

    public Integer getStub() {
        return this.stub;
    }

    public void setStub(Integer num) {
        this.stub = num;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.stub != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.stub.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
        parcel.writeInt(1);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Evaluate> getHelper() {
        return EvaluateHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Evaluate { stub: " + this.stub + " }";
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
