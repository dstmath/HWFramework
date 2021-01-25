package com.huawei.nb.kv;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.kv.Key;
import com.huawei.nb.kv.Value;
import java.util.Objects;

public class KvPair<K extends Key, V extends Value> implements Parcelable {
    public static final Parcelable.Creator<KvPair> CREATOR = new Parcelable.Creator<KvPair>() {
        /* class com.huawei.nb.kv.KvPair.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public KvPair createFromParcel(Parcel parcel) {
            return new KvPair(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public KvPair[] newArray(int i) {
            return new KvPair[i];
        }
    };
    private K key;
    private V val;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public KvPair(K k, V v) {
        this.key = k;
        this.val = v;
    }

    protected KvPair(Parcel parcel) {
        Class cls = (Class) parcel.readSerializable();
        if (cls != null) {
            this.key = (K) ((Key) parcel.readParcelable(cls.getClassLoader()));
        }
        Class cls2 = (Class) parcel.readSerializable();
        if (cls2 != null) {
            this.val = (V) ((Value) parcel.readParcelable(cls2.getClassLoader()));
        }
    }

    public void key(K k) {
        this.key = k;
    }

    public void value(V v) {
        this.val = v;
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.val;
    }

    @Override // java.lang.Object
    public String toString() {
        return "KvPair{key=" + this.key + ", val=" + this.val + '}';
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        KvPair kvPair = (KvPair) obj;
        return Objects.equals(this.key, kvPair.key) && Objects.equals(this.val, kvPair.val);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.key, this.val);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        K k = this.key;
        if (k == null) {
            parcel.writeSerializable(null);
        } else {
            parcel.writeSerializable(k.getClass());
            parcel.writeParcelable(this.key, i);
        }
        V v = this.val;
        if (v == null) {
            parcel.writeSerializable(null);
            return;
        }
        parcel.writeSerializable(v.getClass());
        parcel.writeParcelable(this.val, i);
    }
}
