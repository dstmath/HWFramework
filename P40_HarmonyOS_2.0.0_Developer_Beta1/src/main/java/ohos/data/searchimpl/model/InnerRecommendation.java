package ohos.data.searchimpl.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class InnerRecommendation implements Parcelable {
    public static final Parcelable.Creator<InnerRecommendation> CREATOR = new Parcelable.Creator<InnerRecommendation>() {
        /* class ohos.data.searchimpl.model.InnerRecommendation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public InnerRecommendation createFromParcel(Parcel parcel) {
            return new InnerRecommendation(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public InnerRecommendation[] newArray(int i) {
            return new InnerRecommendation[i];
        }
    };
    private long count;
    private String field;
    private List<InnerIndexData> indexDataList;
    private String value;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public InnerRecommendation(String str, String str2, List<InnerIndexData> list, long j) {
        this.field = str;
        this.value = str2;
        this.indexDataList = list;
        this.count = j;
    }

    private InnerRecommendation(Parcel parcel) {
        this.field = parcel.readString();
        this.value = parcel.readString();
        this.indexDataList = parcel.readArrayList(InnerIndexData.class.getClassLoader());
        this.count = parcel.readLong();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.field);
        parcel.writeString(this.value);
        parcel.writeList(this.indexDataList);
        parcel.writeLong(this.count);
    }

    @Override // java.lang.Object
    public String toString() {
        return "InnerRecommendation{field=" + this.field + ",value=" + this.value + ",count=" + this.count + "}";
    }

    public void setField(String str) {
        this.field = str;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public void setIndexDataList(List<InnerIndexData> list) {
        this.indexDataList = list;
    }

    public void setCount(long j) {
        this.count = j;
    }

    public String getField() {
        return this.field;
    }

    public String getValue() {
        return this.value;
    }

    public List<InnerIndexData> getIndexDataList() {
        return this.indexDataList;
    }

    public long getCount() {
        return this.count;
    }
}
