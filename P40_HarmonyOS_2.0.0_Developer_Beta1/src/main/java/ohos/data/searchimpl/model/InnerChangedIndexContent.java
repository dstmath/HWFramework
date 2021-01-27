package ohos.data.searchimpl.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class InnerChangedIndexContent implements Parcelable {
    public static final Parcelable.Creator<InnerChangedIndexContent> CREATOR = new Parcelable.Creator<InnerChangedIndexContent>() {
        /* class ohos.data.searchimpl.model.InnerChangedIndexContent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InnerChangedIndexContent createFromParcel(Parcel parcel) {
            return new InnerChangedIndexContent(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public InnerChangedIndexContent[] newArray(int i) {
            return new InnerChangedIndexContent[i];
        }
    };
    private List<InnerIndexData> deletedItems;
    private List<InnerIndexData> insertedItems;
    private List<InnerIndexData> updatedItems;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public InnerChangedIndexContent(List<InnerIndexData> list, List<InnerIndexData> list2, List<InnerIndexData> list3) {
        this.insertedItems = list == null ? new ArrayList<>(0) : list;
        this.updatedItems = list2 == null ? new ArrayList<>(0) : list2;
        this.deletedItems = list3 == null ? new ArrayList<>(0) : list3;
    }

    private InnerChangedIndexContent(Parcel parcel) {
        this.insertedItems = parcel.createTypedArrayList(InnerIndexData.CREATOR);
        this.updatedItems = parcel.createTypedArrayList(InnerIndexData.CREATOR);
        this.deletedItems = parcel.createTypedArrayList(InnerIndexData.CREATOR);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this.insertedItems);
        parcel.writeTypedList(this.updatedItems);
        parcel.writeTypedList(this.deletedItems);
    }

    public List<InnerIndexData> getInsertedItems() {
        return this.insertedItems;
    }

    public List<InnerIndexData> getUpdatedItems() {
        return this.updatedItems;
    }

    public List<InnerIndexData> getDeletedItems() {
        return this.deletedItems;
    }
}
