package com.huawei.nb.notification;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.kv.Key;

public class KeyObserverInfo<K extends Key> extends ObserverInfo implements Parcelable {
    public static final Parcelable.Creator<KeyObserverInfo> CREATOR = new Parcelable.Creator<KeyObserverInfo>() {
        /* class com.huawei.nb.notification.KeyObserverInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KeyObserverInfo createFromParcel(Parcel parcel) {
            return new KeyObserverInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public KeyObserverInfo[] newArray(int i) {
            return new KeyObserverInfo[i];
        }
    };
    private K key;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public KeyObserverInfo(ObserverType observerType, K k, String str) {
        super(observerType, str);
        this.key = k;
    }

    public KeyObserverInfo(ObserverType observerType, K k) {
        super(observerType, null);
        this.key = k;
    }

    protected KeyObserverInfo(Parcel parcel) {
        super(parcel);
        Class cls = (Class) parcel.readSerializable();
        if (cls != null) {
            this.key = (K) ((Key) parcel.readParcelable(cls.getClassLoader()));
        }
    }

    public K getKey() {
        return this.key;
    }

    public void setKey(K k) {
        this.key = k;
    }

    @Override // com.huawei.nb.notification.ObserverInfo, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        K k = this.key;
        if (k == null) {
            parcel.writeSerializable(null);
            return;
        }
        parcel.writeSerializable(k.getClass());
        parcel.writeParcelable(this.key, 0);
    }

    @Override // com.huawei.nb.notification.ObserverInfo, java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !super.equals(obj)) {
            return false;
        }
        return this.key.equals(((KeyObserverInfo) obj).key);
    }

    @Override // com.huawei.nb.notification.ObserverInfo, java.lang.Object
    public int hashCode() {
        int hashCode = super.hashCode() * 31;
        K k = this.key;
        return hashCode + (k != null ? k.hashCode() : 0);
    }

    @Override // com.huawei.nb.notification.ObserverInfo, java.lang.Object
    public String toString() {
        return super.toString() + "\tKeyObserverInfo{key=" + this.key + '}';
    }
}
