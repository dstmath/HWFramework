package com.huawei.nb.container;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectContainer<T> implements Container<T>, Parcelable {
    public static final Parcelable.Creator<ObjectContainer> CREATOR = new Parcelable.Creator<ObjectContainer>() {
        public ObjectContainer createFromParcel(Parcel in) {
            return new ObjectContainer(in);
        }

        public ObjectContainer[] newArray(int size) {
            return new ObjectContainer[size];
        }
    };
    public static final ObjectContainer EMPTY = new ObjectContainer(Object.class, Collections.emptyList());
    private Class<T> clazz;
    private String databaseName;
    private String groupName;
    private List<T> objects;
    private String pkgName;

    public ObjectContainer(Class<T> clazz2, List<T> objects2, String pkgName2, String databaseName2, String groupName2) {
        this(clazz2, objects2, pkgName2, databaseName2);
        this.groupName = groupName2;
    }

    public ObjectContainer(Class<T> clazz2, List<T> objects2, String pkgName2, String databaseName2) {
        this(clazz2, objects2, pkgName2);
        this.databaseName = databaseName2;
    }

    public ObjectContainer(Class<T> clazz2, List<T> objects2, String pkgName2) {
        this(clazz2, objects2);
        this.pkgName = pkgName2;
    }

    public ObjectContainer(Class<T> clazz2, List<T> objects2) {
        if (clazz2 == null) {
            throw new IllegalArgumentException();
        }
        this.clazz = clazz2;
        this.objects = objects2;
    }

    public ObjectContainer(Class<T> clazz2) {
        this(clazz2, new ArrayList());
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public boolean add(T object) {
        if (object != null) {
            return this.objects.add(object);
        }
        return false;
    }

    public boolean remove(T t) {
        return this.objects.remove(t);
    }

    public boolean delete(T t) {
        return remove(t);
    }

    public void clear() {
        this.objects.clear();
    }

    public void clearObjects() {
        this.objects.clear();
    }

    protected ObjectContainer(Parcel in) {
        this.clazz = (Class) in.readSerializable();
        if (this.clazz != null) {
            this.objects = in.readArrayList(this.clazz.getClassLoader());
        } else {
            this.objects = Collections.emptyList();
        }
        if (in.readInt() == 1) {
            this.pkgName = in.readString();
        } else {
            this.pkgName = null;
        }
        if (in.readInt() == 1) {
            this.databaseName = in.readString();
        } else {
            this.databaseName = null;
        }
        if (in.readInt() == 1) {
            this.groupName = in.readString();
        } else {
            this.groupName = null;
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
        if (this.pkgName != null) {
            dest.writeInt(1);
            dest.writeString(this.pkgName);
        } else {
            dest.writeInt(0);
        }
        if (this.databaseName != null) {
            dest.writeInt(1);
            dest.writeString(this.databaseName);
        } else {
            dest.writeInt(0);
        }
        if (this.groupName != null) {
            dest.writeInt(1);
            dest.writeString(this.groupName);
            return;
        }
        dest.writeInt(0);
    }

    public ObjectContainer readFromParcel(Parcel in) {
        return new ObjectContainer(in);
    }

    public int describeContents() {
        return 0;
    }
}
