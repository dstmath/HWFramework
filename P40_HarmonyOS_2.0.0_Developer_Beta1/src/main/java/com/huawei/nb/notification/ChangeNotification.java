package com.huawei.nb.notification;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Collections;
import java.util.List;

public class ChangeNotification implements Parcelable {
    public static final Parcelable.Creator<ChangeNotification> CREATOR = new Parcelable.Creator<ChangeNotification>() {
        /* class com.huawei.nb.notification.ChangeNotification.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ChangeNotification createFromParcel(Parcel parcel) {
            return new ChangeNotification(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ChangeNotification[] newArray(int i) {
            return new ChangeNotification[i];
        }
    };
    private Class clazz;
    private List deletedItems;
    private List insertedItems;
    private boolean isDeleteAll;
    private List updatedItems;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public ChangeNotification(Class cls, boolean z, List list, List list2, List list3) {
        this.clazz = cls;
        this.isDeleteAll = z;
        this.insertedItems = list == null ? Collections.emptyList() : list;
        this.updatedItems = list2 == null ? Collections.emptyList() : list2;
        this.deletedItems = list3 == null ? Collections.emptyList() : list3;
    }

    protected ChangeNotification(Parcel parcel) {
        this.clazz = (Class) parcel.readSerializable();
        this.isDeleteAll = parcel.readInt() != 1 ? false : true;
        this.insertedItems = parcel.readArrayList(this.clazz.getClassLoader());
        this.updatedItems = parcel.readArrayList(this.clazz.getClassLoader());
        this.deletedItems = parcel.readArrayList(this.clazz.getClassLoader());
    }

    public Class getType() {
        return this.clazz;
    }

    public boolean isDeleteAll() {
        return this.isDeleteAll;
    }

    public List getInsertedItems() {
        return this.insertedItems;
    }

    public List getUpdatedItems() {
        return this.updatedItems;
    }

    public List getDeletedItems() {
        return this.deletedItems;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(this.clazz);
        parcel.writeInt(this.isDeleteAll ? 1 : 0);
        parcel.writeList(this.insertedItems);
        parcel.writeList(this.updatedItems);
        parcel.writeList(this.deletedItems);
    }
}
