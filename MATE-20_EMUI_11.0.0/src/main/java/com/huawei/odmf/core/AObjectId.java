package com.huawei.odmf.core;

import android.os.Parcel;
import android.os.Parcelable;

public class AObjectId implements Parcelable, ObjectId {
    public static final Parcelable.Creator<AObjectId> CREATOR = new Parcelable.Creator<AObjectId>() {
        /* class com.huawei.odmf.core.AObjectId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AObjectId createFromParcel(Parcel parcel) {
            return new AObjectId(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AObjectId[] newArray(int i) {
            return new AObjectId[i];
        }
    };
    private String entityName;
    private Object id;
    private String uriString;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public AObjectId(String str, Object obj) {
        this(str, obj, null);
    }

    public AObjectId(String str, Object obj, String str2) {
        this.entityName = str;
        this.id = obj;
        this.uriString = str2;
    }

    public AObjectId(Parcel parcel) {
        this.entityName = parcel.readString();
        this.id = parcel.readValue(getClass().getClassLoader());
        this.uriString = parcel.readString();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.entityName);
        parcel.writeValue(this.id);
        parcel.writeString(this.uriString);
    }

    @Override // com.huawei.odmf.core.ObjectId
    public String getEntityName() {
        return this.entityName;
    }

    @Override // com.huawei.odmf.core.ObjectId
    public void setEntityName(String str) {
        this.entityName = str;
    }

    @Override // com.huawei.odmf.core.ObjectId
    public Object getId() {
        return this.id;
    }

    @Override // com.huawei.odmf.core.ObjectId
    public void setId(Object obj) {
        this.id = obj;
    }

    @Override // com.huawei.odmf.core.ObjectId
    public String getUriString() {
        return this.uriString;
    }

    @Override // com.huawei.odmf.core.ObjectId
    public void setUriString(String str) {
        this.uriString = str;
    }

    @Override // java.lang.Object
    public int hashCode() {
        String str = this.entityName;
        int i = 0;
        int hashCode = ((str == null ? 0 : str.hashCode()) + 31) * 31;
        Object obj = this.id;
        int hashCode2 = (hashCode + (obj == null ? 0 : obj.hashCode())) * 31;
        String str2 = this.uriString;
        if (str2 != null) {
            i = str2.hashCode();
        }
        return hashCode2 + i;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        Object obj2;
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            AObjectId aObjectId = (AObjectId) obj;
            Object obj3 = this.id;
            if (obj3 == null || (obj2 = aObjectId.id) == null || !obj3.equals(obj2)) {
                return false;
            }
            String str = this.entityName;
            if (str == null ? aObjectId.entityName != null : !str.equals(aObjectId.entityName)) {
                return false;
            }
            String str2 = this.uriString;
            if (str2 != null) {
                return str2.equals(aObjectId.uriString);
            }
            return aObjectId.uriString == null;
        }
        return false;
    }

    @Override // java.lang.Object
    public String toString() {
        return this.entityName + ":" + this.id + ":" + this.uriString;
    }
}
