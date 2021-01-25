package com.huawei.nb.query;

import android.os.Parcel;
import android.os.Parcelable;

public class RawQuery implements IQuery {
    public static final Parcelable.Creator<RawQuery> CREATOR = new Parcelable.Creator<RawQuery>() {
        /* class com.huawei.nb.query.RawQuery.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RawQuery createFromParcel(Parcel parcel) {
            return new RawQuery(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RawQuery[] newArray(int i) {
            return new RawQuery[i];
        }
    };
    private String dbName;
    private String rawSQL;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.nb.query.IQuery
    public String getEntityName() {
        return null;
    }

    @Override // com.huawei.nb.query.IQuery
    public boolean isValid() {
        return true;
    }

    private RawQuery() {
    }

    protected RawQuery(Parcel parcel) {
        this.dbName = parcel.readString();
        this.rawSQL = parcel.readString();
    }

    public static RawQuery select(String str) {
        RawQuery rawQuery = new RawQuery();
        rawQuery.rawSQL = str;
        return rawQuery;
    }

    public RawQuery from(String str) {
        this.dbName = str;
        return this;
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getRawSQL() {
        return this.rawSQL;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.dbName);
        parcel.writeString(this.rawSQL);
    }
}
