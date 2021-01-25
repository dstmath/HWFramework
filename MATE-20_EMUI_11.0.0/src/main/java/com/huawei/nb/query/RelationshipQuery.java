package com.huawei.nb.query;

import android.os.Parcel;
import android.os.Parcelable;

public class RelationshipQuery implements IQuery {
    public static final Parcelable.Creator<RelationshipQuery> CREATOR = new Parcelable.Creator<RelationshipQuery>() {
        /* class com.huawei.nb.query.RelationshipQuery.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RelationshipQuery createFromParcel(Parcel parcel) {
            return new RelationshipQuery(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public RelationshipQuery[] newArray(int i) {
            return new RelationshipQuery[i];
        }
    };
    private String entityName;
    private String fieldName;
    private Object objectId;
    private RelationType type;

    public enum RelationType {
        TO_ONE,
        TO_MANY
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.nb.query.IQuery
    public boolean isValid() {
        return true;
    }

    public RelationshipQuery(String str, Object obj, String str2, RelationType relationType) {
        this.entityName = str;
        this.objectId = obj;
        this.fieldName = str2;
        this.type = relationType;
    }

    protected RelationshipQuery(Parcel parcel) {
        this.entityName = parcel.readString();
        this.objectId = parcel.readValue(null);
        this.fieldName = parcel.readString();
        this.type = RelationType.values()[parcel.readInt()];
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.entityName);
        parcel.writeValue(this.objectId);
        parcel.writeString(this.fieldName);
        parcel.writeInt(this.type.ordinal());
    }

    @Override // com.huawei.nb.query.IQuery
    public String getEntityName() {
        return this.entityName;
    }

    public Object getObjectId() {
        return this.objectId;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public RelationType getType() {
        return this.type;
    }
}
