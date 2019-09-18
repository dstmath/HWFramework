package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class IndexField extends AManagedObject {
    public static final Parcelable.Creator<IndexField> CREATOR = new Parcelable.Creator<IndexField>() {
        public IndexField createFromParcel(Parcel in) {
            return new IndexField(in);
        }

        public IndexField[] newArray(int size) {
            return new IndexField[size];
        }
    };
    private String fieldName;
    private Integer id;
    private String indexStatus;
    private Integer indexType;
    private Boolean isFieldConstants;
    private String storeStatus;

    public IndexField(Cursor cursor) {
        Boolean valueOf;
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.fieldName = cursor.getString(2);
        if (cursor.isNull(3)) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(cursor.getInt(3) != 0);
        }
        this.isFieldConstants = valueOf;
        this.storeStatus = cursor.getString(4);
        this.indexStatus = cursor.getString(5);
        this.indexType = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public IndexField(Parcel in) {
        super(in);
        Boolean valueOf;
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.fieldName = in.readByte() == 0 ? null : in.readString();
        if (in.readByte() == 0) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(in.readByte() != 0);
        }
        this.isFieldConstants = valueOf;
        this.storeStatus = in.readByte() == 0 ? null : in.readString();
        this.indexStatus = in.readByte() == 0 ? null : in.readString();
        this.indexType = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private IndexField(Integer id2, String fieldName2, Boolean isFieldConstants2, String storeStatus2, String indexStatus2, Integer indexType2) {
        this.id = id2;
        this.fieldName = fieldName2;
        this.isFieldConstants = isFieldConstants2;
        this.storeStatus = storeStatus2;
        this.indexStatus = indexStatus2;
        this.indexType = indexType2;
    }

    public IndexField() {
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

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName2) {
        this.fieldName = fieldName2;
        setValue();
    }

    public Boolean getIsFieldConstants() {
        return this.isFieldConstants;
    }

    public void setIsFieldConstants(Boolean isFieldConstants2) {
        this.isFieldConstants = isFieldConstants2;
        setValue();
    }

    public String getStoreStatus() {
        return this.storeStatus;
    }

    public void setStoreStatus(String storeStatus2) {
        this.storeStatus = storeStatus2;
        setValue();
    }

    public String getIndexStatus() {
        return this.indexStatus;
    }

    public void setIndexStatus(String indexStatus2) {
        this.indexStatus = indexStatus2;
        setValue();
    }

    public Integer getIndexType() {
        return this.indexType;
    }

    public void setIndexType(Integer indexType2) {
        this.indexType = indexType2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b;
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.fieldName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.fieldName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isFieldConstants != null) {
            out.writeByte((byte) 1);
            if (this.isFieldConstants.booleanValue()) {
                b = 1;
            } else {
                b = 0;
            }
            out.writeByte(b);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.storeStatus != null) {
            out.writeByte((byte) 1);
            out.writeString(this.storeStatus);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.indexStatus != null) {
            out.writeByte((byte) 1);
            out.writeString(this.indexStatus);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.indexType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.indexType.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<IndexField> getHelper() {
        return IndexFieldHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.IndexField";
    }

    public String getDatabaseName() {
        return "dsSearch";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("IndexField { id: ").append(this.id);
        sb.append(", fieldName: ").append(this.fieldName);
        sb.append(", isFieldConstants: ").append(this.isFieldConstants);
        sb.append(", storeStatus: ").append(this.storeStatus);
        sb.append(", indexStatus: ").append(this.indexStatus);
        sb.append(", indexType: ").append(this.indexType);
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
