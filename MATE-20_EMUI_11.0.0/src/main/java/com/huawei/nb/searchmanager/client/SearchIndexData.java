package com.huawei.nb.searchmanager.client;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;

public class SearchIndexData implements Parcelable {
    public static final Parcelable.Creator<SearchIndexData> CREATOR = new Parcelable.Creator<SearchIndexData>() {
        /* class com.huawei.nb.searchmanager.client.SearchIndexData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SearchIndexData createFromParcel(Parcel parcel) {
            return new SearchIndexData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SearchIndexData[] newArray(int i) {
            return new SearchIndexData[i];
        }
    };
    private int dataType;
    private HashMap<String, String> fieldMap;
    private boolean status;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public SearchIndexData() {
    }

    protected SearchIndexData(Parcel parcel) {
        this.dataType = parcel.readInt();
        this.status = parcel.readByte() != 0;
        this.fieldMap = parcel.readHashMap(HashMap.class.getClassLoader());
    }

    public HashMap<String, String> getFieldMap() {
        return this.fieldMap;
    }

    public void setFieldMap(HashMap<String, String> hashMap) {
        this.fieldMap = hashMap;
    }

    public int getDataType() {
        return this.dataType;
    }

    public void setDataType(int i) {
        this.dataType = i;
    }

    public boolean isStatus() {
        return this.status;
    }

    public void setStatus(boolean z) {
        this.status = z;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.dataType);
        parcel.writeByte(this.status ? (byte) 1 : 0);
        parcel.writeMap(this.fieldMap);
    }
}
