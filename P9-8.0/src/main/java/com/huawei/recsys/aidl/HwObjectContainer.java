package com.huawei.recsys.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwObjectContainer<T> implements Parcelable {
    public static final Creator<HwObjectContainer> CREATOR = new Creator<HwObjectContainer>() {
        public HwObjectContainer createFromParcel(Parcel in) {
            return new HwObjectContainer(in);
        }

        public HwObjectContainer[] newArray(int size) {
            return new HwObjectContainer[size];
        }
    };
    private Class<T> clazz;
    private List<T> objects;

    public HwObjectContainer(Class<T> clazz, List<T> objects) {
        if (clazz == null) {
            throw new IllegalArgumentException();
        }
        this.clazz = clazz;
        this.objects = objects;
    }

    public HwObjectContainer(Class<T> clazz) {
        this(clazz, new ArrayList());
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

    protected HwObjectContainer(Parcel in) {
        this.clazz = (Class) in.readSerializable();
        if (this.clazz != null) {
            this.objects = in.readArrayList(this.clazz.getClassLoader());
        } else {
            this.objects = Collections.emptyList();
        }
    }

    public Class type() {
        return this.clazz;
    }

    public List<T> get() {
        return this.objects;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.clazz);
        dest.writeList(this.objects);
    }

    public int describeContents() {
        return 0;
    }
}
