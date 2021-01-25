package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class SearchClick extends AManagedObject {
    public static final Parcelable.Creator<SearchClick> CREATOR = new Parcelable.Creator<SearchClick>() {
        /* class com.huawei.nb.model.search.SearchClick.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SearchClick createFromParcel(Parcel parcel) {
            return new SearchClick(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SearchClick[] newArray(int i) {
            return new SearchClick[i];
        }
    };
    private String browseDetail;
    private String browseType;
    private Integer id;
    private String keyword;
    private String resultType;
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
        return "com.huawei.nb.model.search.SearchClick";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public SearchClick(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.keyword = cursor.getString(2);
        this.searchTime = cursor.getString(3);
        this.resultType = cursor.getString(4);
        this.browseType = cursor.getString(5);
        this.browseDetail = cursor.getString(6);
    }

    public SearchClick(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.keyword = parcel.readByte() == 0 ? null : parcel.readString();
        this.searchTime = parcel.readByte() == 0 ? null : parcel.readString();
        this.resultType = parcel.readByte() == 0 ? null : parcel.readString();
        this.browseType = parcel.readByte() == 0 ? null : parcel.readString();
        this.browseDetail = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private SearchClick(Integer num, String str, String str2, String str3, String str4, String str5) {
        this.id = num;
        this.keyword = str;
        this.searchTime = str2;
        this.resultType = str3;
        this.browseType = str4;
        this.browseDetail = str5;
    }

    public SearchClick() {
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

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String str) {
        this.resultType = str;
        setValue();
    }

    public String getBrowseType() {
        return this.browseType;
    }

    public void setBrowseType(String str) {
        this.browseType = str;
        setValue();
    }

    public String getBrowseDetail() {
        return this.browseDetail;
    }

    public void setBrowseDetail(String str) {
        this.browseDetail = str;
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
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.resultType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.resultType);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.browseType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.browseType);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.browseDetail != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.browseDetail);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<SearchClick> getHelper() {
        return SearchClickHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "SearchClick { id: " + this.id + ", keyword: " + this.keyword + ", searchTime: " + this.searchTime + ", resultType: " + this.resultType + ", browseType: " + this.browseType + ", browseDetail: " + this.browseDetail + " }";
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
