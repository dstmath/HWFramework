package com.huawei.nb.notification;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.kv.Key;

public class KeyObserverInfo<K extends Key> extends ObserverInfo implements Parcelable {
    public static final Parcelable.Creator<KeyObserverInfo> CREATOR = new Parcelable.Creator<KeyObserverInfo>() {
        public KeyObserverInfo createFromParcel(Parcel in) {
            return new KeyObserverInfo(in);
        }

        public KeyObserverInfo[] newArray(int size) {
            return new KeyObserverInfo[size];
        }
    };
    private K key;

    public KeyObserverInfo(ObserverType type, K key2, String pkgName) {
        super(type, pkgName);
        this.key = key2;
    }

    public KeyObserverInfo(ObserverType type, K key2) {
        super(type, null);
        this.key = key2;
    }

    protected KeyObserverInfo(Parcel in) {
        super(in);
        Class clazz = (Class) in.readSerializable();
        if (clazz != null) {
            this.key = (Key) in.readParcelable(clazz.getClassLoader());
        }
    }

    public K getKey() {
        return this.key;
    }

    public void setKey(K key2) {
        this.key = key2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (this.key == null) {
            dest.writeSerializable(null);
            return;
        }
        dest.writeSerializable(this.key.getClass());
        dest.writeParcelable(this.key, 0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        return this.key.equals(((KeyObserverInfo) o).key);
    }

    public int hashCode() {
        return (super.hashCode() * 31) + (this.key != null ? this.key.hashCode() : 0);
    }

    public String toString() {
        return super.toString() + "\tKeyObserverInfo{" + "key=" + this.key + '}';
    }
}
