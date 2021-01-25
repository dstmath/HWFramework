package com.huawei.nb.notification;

import android.os.Parcel;
import android.os.Parcelable;

public class ModelObserverInfo extends ObserverInfo implements Parcelable {
    public static final Parcelable.Creator<ModelObserverInfo> CREATOR = new Parcelable.Creator<ModelObserverInfo>() {
        /* class com.huawei.nb.notification.ModelObserverInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ModelObserverInfo createFromParcel(Parcel parcel) {
            return new ModelObserverInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ModelObserverInfo[] newArray(int i) {
            return new ModelObserverInfo[i];
        }
    };
    private static final int HASHCODE_RANDOM = 31;
    private Class modelClazz;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public ModelObserverInfo(ObserverType observerType, Class cls, String str) {
        super(observerType, str);
        this.modelClazz = cls;
    }

    public ModelObserverInfo(ObserverType observerType, Class cls) {
        super(observerType, null);
        this.modelClazz = cls;
    }

    protected ModelObserverInfo(Parcel parcel) {
        super(parcel);
        this.modelClazz = (Class) parcel.readSerializable();
    }

    public Class getModelClazz() {
        return this.modelClazz;
    }

    @Override // com.huawei.nb.notification.ObserverInfo, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeSerializable(this.modelClazz);
    }

    @Override // com.huawei.nb.notification.ObserverInfo, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !super.equals(obj)) {
            return false;
        }
        return this.modelClazz == ((ModelObserverInfo) obj).modelClazz;
    }

    @Override // com.huawei.nb.notification.ObserverInfo, java.lang.Object
    public int hashCode() {
        int hashCode = super.hashCode() * HASHCODE_RANDOM;
        Class cls = this.modelClazz;
        return hashCode + (cls != null ? cls.hashCode() : 0);
    }

    @Override // com.huawei.nb.notification.ObserverInfo, java.lang.Object
    public String toString() {
        return super.toString() + "\tModelObserverInfo{modelClazz=" + this.modelClazz + '}';
    }
}
