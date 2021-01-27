package com.huawei.nb.searchmanager.client.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class ChangedIndexContent implements Parcelable {
    public static final Parcelable.Creator<ChangedIndexContent> CREATOR = new Parcelable.Creator<ChangedIndexContent>() {
        /* class com.huawei.nb.searchmanager.client.model.ChangedIndexContent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ChangedIndexContent createFromParcel(Parcel parcel) {
            return new ChangedIndexContent(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public ChangedIndexContent[] newArray(int i) {
            return new ChangedIndexContent[i];
        }
    };
    private List<IndexData> deletedItems;
    private List<IndexData> insertedItems;
    private List<IndexData> updatedItems;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public ChangedIndexContent(List<IndexData> list, List<IndexData> list2, List<IndexData> list3) {
        this.insertedItems = list == null ? new ArrayList<>(0) : list;
        this.updatedItems = list2 == null ? new ArrayList<>(0) : list2;
        this.deletedItems = list3 == null ? new ArrayList<>(0) : list3;
    }

    private ChangedIndexContent(Parcel parcel) {
        this.insertedItems = parcel.createTypedArrayList(IndexData.CREATOR);
        this.updatedItems = parcel.createTypedArrayList(IndexData.CREATOR);
        this.deletedItems = parcel.createTypedArrayList(IndexData.CREATOR);
    }

    public List<IndexData> getInsertedItems() {
        return this.insertedItems;
    }

    public List<IndexData> getUpdatedItems() {
        return this.updatedItems;
    }

    public List<IndexData> getDeletedItems() {
        return this.deletedItems;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this.insertedItems);
        parcel.writeTypedList(this.updatedItems);
        parcel.writeTypedList(this.deletedItems);
    }
}
