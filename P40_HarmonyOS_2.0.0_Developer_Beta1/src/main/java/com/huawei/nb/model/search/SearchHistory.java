package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class SearchHistory extends AManagedObject {
    public static final Parcelable.Creator<SearchHistory> CREATOR = new Parcelable.Creator<SearchHistory>() {
        /* class com.huawei.nb.model.search.SearchHistory.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SearchHistory createFromParcel(Parcel parcel) {
            return new SearchHistory(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SearchHistory[] newArray(int i) {
            return new SearchHistory[i];
        }
    };
    private Integer id;
    private String keyword;
    private String searchTime;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.search.SearchHistory";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public SearchHistory(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.keyword = cursor.getString(2);
        this.searchTime = cursor.getString(3);
    }

    public SearchHistory(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.keyword = parcel.readByte() == 0 ? null : parcel.readString();
        this.searchTime = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private SearchHistory(Integer num, String str, String str2) {
        this.id = num;
        this.keyword = str;
        this.searchTime = str2;
    }

    public SearchHistory() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String str) {
        this.keyword = str;
        setValue();
    }

    public String getSearchTime() {
        return this.searchTime;
    }

    public void setSearchTime(String str) {
        this.searchTime = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.keyword != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.keyword);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.searchTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.searchTime);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<SearchHistory> getHelper() {
        return SearchHistoryHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "SearchHistory { id: " + this.id + ", keyword: " + this.keyword + ", searchTime: " + this.searchTime + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
