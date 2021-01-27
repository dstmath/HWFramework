package com.huawei.nb.searchmanager.client.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Recommendation implements Parcelable {
    public static final Parcelable.Creator<Recommendation> CREATOR = new Parcelable.Creator<Recommendation>() {
        /* class com.huawei.nb.searchmanager.client.model.Recommendation.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Recommendation createFromParcel(Parcel parcel) {
            return new Recommendation(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Recommendation[] newArray(int i) {
            return new Recommendation[i];
        }
    };
    private long count;
    private String field;
    private List<IndexData> indexDataList;
    private String value;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Recommendation(String str, String str2, List<IndexData> list, long j) {
        this.field = str;
        this.value = str2;
        this.indexDataList = list;
        this.count = j;
    }

    public Recommendation(Parcel parcel) {
        this.field = parcel.readString();
        this.value = parcel.readString();
        this.indexDataList = parcel.readArrayList(IndexData.class.getClassLoader());
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
        return "Recommendation[field=" + this.field + ",value=" + this.value + ",count=" + this.count + "]";
    }

    public void setField(String str) {
        this.field = str;
    }

    public void setValue(String str) {
        this.value = str;
    }

    public void setIndexDataList(List<IndexData> list) {
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

    public List<IndexData> getIndexDataList() {
        return this.indexDataList;
    }

    public long getCount() {
        return this.count;
    }
}
