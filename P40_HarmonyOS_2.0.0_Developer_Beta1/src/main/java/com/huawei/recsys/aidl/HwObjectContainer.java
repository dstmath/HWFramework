package com.huawei.recsys.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwObjectContainer<T> implements Parcelable {
    public static final Parcelable.Creator<HwObjectContainer> CREATOR = new Parcelable.Creator<HwObjectContainer>() {
        /* class com.huawei.recsys.aidl.HwObjectContainer.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HwObjectContainer createFromParcel(Parcel in) {
            return new HwObjectContainer(in);
        }

        @Override // android.os.Parcelable.Creator
        public HwObjectContainer[] newArray(int size) {
            return new HwObjectContainer[size];
        }
    };
    private Class<T> clazz;
    private List<T> objects;

    public HwObjectContainer(Class<T> clazz2, List<T> objects2) {
        if (clazz2 != null) {
            this.clazz = clazz2;
            this.objects = objects2;
            return;
        }
        throw new IllegalArgumentException();
    }

    public HwObjectContainer(Class<T> clazz2) {
        this(clazz2, new ArrayList());
    }

    protected HwObjectContainer(Parcel in) {
        this.clazz = (Class) in.readSerializable();
        Class<T> cls = this.clazz;
        if (cls != null) {
            this.objects = in.readArrayList(cls.getClassLoader());
        } else {
            this.objects = Collections.emptyList();
        }
    }

    public HwObjectContainer add(T object) {
        if (object != null) {
            this.objects.add(object);
        }
        return this;
    }

    public void clearObjects() {
        this.objects.clear();
    }

    public Class type() {
        return this.clazz;
    }

    public List<T> get() {
        return this.objects;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.clazz);
        dest.writeList(this.objects);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
