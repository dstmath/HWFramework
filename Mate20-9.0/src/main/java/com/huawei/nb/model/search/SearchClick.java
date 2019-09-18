package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class SearchClick extends AManagedObject {
    public static final Parcelable.Creator<SearchClick> CREATOR = new Parcelable.Creator<SearchClick>() {
        public SearchClick createFromParcel(Parcel in) {
            return new SearchClick(in);
        }

        public SearchClick[] newArray(int size) {
            return new SearchClick[size];
        }
    };
    private String browseDetail;
    private String browseType;
    private Integer id;
    private String keyword;
    private String resultType;
    private String searchTime;

    public SearchClick(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.keyword = cursor.getString(2);
        this.searchTime = cursor.getString(3);
        this.resultType = cursor.getString(4);
        this.browseType = cursor.getString(5);
        this.browseDetail = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SearchClick(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.keyword = in.readByte() == 0 ? null : in.readString();
        this.searchTime = in.readByte() == 0 ? null : in.readString();
        this.resultType = in.readByte() == 0 ? null : in.readString();
        this.browseType = in.readByte() == 0 ? null : in.readString();
        this.browseDetail = in.readByte() != 0 ? in.readString() : str;
    }

    private SearchClick(Integer id2, String keyword2, String searchTime2, String resultType2, String browseType2, String browseDetail2) {
        this.id = id2;
        this.keyword = keyword2;
        this.searchTime = searchTime2;
        this.resultType = resultType2;
        this.browseType = browseType2;
        this.browseDetail = browseDetail2;
    }

    public SearchClick() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String keyword2) {
        this.keyword = keyword2;
        setValue();
    }

    public String getSearchTime() {
        return this.searchTime;
    }

    public void setSearchTime(String searchTime2) {
        this.searchTime = searchTime2;
        setValue();
    }

    public String getResultType() {
        return this.resultType;
    }

    public void setResultType(String resultType2) {
        this.resultType = resultType2;
        setValue();
    }

    public String getBrowseType() {
        return this.browseType;
    }

    public void setBrowseType(String browseType2) {
        this.browseType = browseType2;
        setValue();
    }

    public String getBrowseDetail() {
        return this.browseDetail;
    }

    public void setBrowseDetail(String browseDetail2) {
        this.browseDetail = browseDetail2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.keyword != null) {
            out.writeByte((byte) 1);
            out.writeString(this.keyword);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.searchTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.searchTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.resultType != null) {
            out.writeByte((byte) 1);
            out.writeString(this.resultType);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.browseType != null) {
            out.writeByte((byte) 1);
            out.writeString(this.browseType);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.browseDetail != null) {
            out.writeByte((byte) 1);
            out.writeString(this.browseDetail);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<SearchClick> getHelper() {
        return SearchClickHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.SearchClick";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("SearchClick { id: ").append(this.id);
        sb.append(", keyword: ").append(this.keyword);
        sb.append(", searchTime: ").append(this.searchTime);
        sb.append(", resultType: ").append(this.resultType);
        sb.append(", browseType: ").append(this.browseType);
        sb.append(", browseDetail: ").append(this.browseDetail);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
