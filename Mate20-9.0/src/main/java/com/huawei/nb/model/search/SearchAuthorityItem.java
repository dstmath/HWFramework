package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class SearchAuthorityItem extends AManagedObject {
    public static final Parcelable.Creator<SearchAuthorityItem> CREATOR = new Parcelable.Creator<SearchAuthorityItem>() {
        public SearchAuthorityItem createFromParcel(Parcel in) {
            return new SearchAuthorityItem(in);
        }

        public SearchAuthorityItem[] newArray(int size) {
            return new SearchAuthorityItem[size];
        }
    };
    private String callingPkgName;
    private Integer id;
    private String pkgName;

    public SearchAuthorityItem(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.pkgName = cursor.getString(2);
        this.callingPkgName = cursor.getString(3);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SearchAuthorityItem(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.pkgName = in.readByte() == 0 ? null : in.readString();
        this.callingPkgName = in.readByte() != 0 ? in.readString() : str;
    }

    private SearchAuthorityItem(Integer id2, String pkgName2, String callingPkgName2) {
        this.id = id2;
        this.pkgName = pkgName2;
        this.callingPkgName = callingPkgName2;
    }

    public SearchAuthorityItem() {
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

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
        setValue();
    }

    public String getCallingPkgName() {
        return this.callingPkgName;
    }

    public void setCallingPkgName(String callingPkgName2) {
        this.callingPkgName = callingPkgName2;
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
        if (this.pkgName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.pkgName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.callingPkgName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.callingPkgName);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<SearchAuthorityItem> getHelper() {
        return SearchAuthorityItemHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.SearchAuthorityItem";
    }

    public String getDatabaseName() {
        return "dsSearch";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("SearchAuthorityItem { id: ").append(this.id);
        sb.append(", pkgName: ").append(this.pkgName);
        sb.append(", callingPkgName: ").append(this.callingPkgName);
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
        return "0.0.7";
    }

    public int getDatabaseVersionCode() {
        return 7;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
