package com.huawei.nb.query;

import android.os.Parcel;
import android.os.Parcelable;

public class QueryContainer implements Parcelable {
    public static final Parcelable.Creator<QueryContainer> CREATOR = new Parcelable.Creator<QueryContainer>() {
        /* class com.huawei.nb.query.QueryContainer.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public QueryContainer createFromParcel(Parcel parcel) {
            return new QueryContainer(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public QueryContainer[] newArray(int i) {
            return new QueryContainer[i];
        }
    };
    private String pkgName;
    private IQuery query;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public QueryContainer(IQuery iQuery, String str) {
        this.query = iQuery;
        this.pkgName = str;
    }

    protected QueryContainer(Parcel parcel) {
        String readString = parcel.readString();
        if (Query.class.getName().equals(readString)) {
            this.query = (IQuery) parcel.readParcelable(Query.class.getClassLoader());
        } else if (RawQuery.class.getName().equals(readString)) {
            this.query = (IQuery) parcel.readParcelable(RawQuery.class.getClassLoader());
        } else if (RelationshipQuery.class.getName().equals(readString)) {
            this.query = (IQuery) parcel.readParcelable(RelationshipQuery.class.getClassLoader());
        } else {
            this.query = null;
        }
        if (parcel.readInt() == 1) {
            this.pkgName = parcel.readString();
        } else {
            this.pkgName = null;
        }
    }

    public IQuery getQuery() {
        return this.query;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.query.getClass().getName());
        parcel.writeParcelable(this.query, i);
        if (this.pkgName != null) {
            parcel.writeInt(1);
            parcel.writeString(this.pkgName);
            return;
        }
        parcel.writeInt(0);
    }
}
