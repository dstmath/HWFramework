package com.huawei.nb.notification;

import android.os.Parcel;
import android.os.Parcelable;

public class ModelObserverInfo extends ObserverInfo implements Parcelable {
    public static final Parcelable.Creator<ModelObserverInfo> CREATOR = new Parcelable.Creator<ModelObserverInfo>() {
        public ModelObserverInfo createFromParcel(Parcel in) {
            return new ModelObserverInfo(in);
        }

        public ModelObserverInfo[] newArray(int size) {
            return new ModelObserverInfo[size];
        }
    };
    private Class modelClazz;

    public Class getModelClazz() {
        return this.modelClazz;
    }

    public ModelObserverInfo(ObserverType type, Class modelClazz2, String pkgName) {
        super(type, pkgName);
        this.modelClazz = modelClazz2;
    }

    public ModelObserverInfo(ObserverType type, Class modelClazz2) {
        super(type, null);
        this.modelClazz = modelClazz2;
    }

    protected ModelObserverInfo(Parcel in) {
        super(in);
        this.modelClazz = (Class) in.readSerializable();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeSerializable(this.modelClazz);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (this.modelClazz != ((ModelObserverInfo) o).modelClazz) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (super.hashCode() * 31) + (this.modelClazz != null ? this.modelClazz.hashCode() : 0);
    }

    public String toString() {
        return super.toString() + "\tModelObserverInfo{" + "modelClazz=" + this.modelClazz + '}';
    }
}
