package com.huawei.nb.model.search;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class IndexMapping extends AManagedObject {
    public static final Parcelable.Creator<IndexMapping> CREATOR = new Parcelable.Creator<IndexMapping>() {
        public IndexMapping createFromParcel(Parcel in) {
            return new IndexMapping(in);
        }

        public IndexMapping[] newArray(int size) {
            return new IndexMapping[size];
        }
    };
    private String columnName;
    private String columnNums;
    private String fieldName;
    private Integer id;
    private Integer indexMappingType;
    private Boolean isColumnNum;

    public IndexMapping(Cursor cursor) {
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
        this.isColumnNum = valueOf;
        this.columnName = cursor.getString(4);
        this.columnNums = cursor.getString(5);
        this.indexMappingType = !cursor.isNull(6) ? Integer.valueOf(cursor.getInt(6)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public IndexMapping(Parcel in) {
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
        this.isColumnNum = valueOf;
        this.columnName = in.readByte() == 0 ? null : in.readString();
        this.columnNums = in.readByte() == 0 ? null : in.readString();
        this.indexMappingType = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private IndexMapping(Integer id2, String fieldName2, Boolean isColumnNum2, String columnName2, String columnNums2, Integer indexMappingType2) {
        this.id = id2;
        this.fieldName = fieldName2;
        this.isColumnNum = isColumnNum2;
        this.columnName = columnName2;
        this.columnNums = columnNums2;
        this.indexMappingType = indexMappingType2;
    }

    public IndexMapping() {
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

    public Boolean getIsColumnNum() {
        return this.isColumnNum;
    }

    public void setIsColumnNum(Boolean isColumnNum2) {
        this.isColumnNum = isColumnNum2;
        setValue();
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName2) {
        this.columnName = columnName2;
        setValue();
    }

    public String getColumnNums() {
        return this.columnNums;
    }

    public void setColumnNums(String columnNums2) {
        this.columnNums = columnNums2;
        setValue();
    }

    public Integer getIndexMappingType() {
        return this.indexMappingType;
    }

    public void setIndexMappingType(Integer indexMappingType2) {
        this.indexMappingType = indexMappingType2;
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
        if (this.isColumnNum != null) {
            out.writeByte((byte) 1);
            if (this.isColumnNum.booleanValue()) {
                b = 1;
            } else {
                b = 0;
            }
            out.writeByte(b);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.columnName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.columnName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.columnNums != null) {
            out.writeByte((byte) 1);
            out.writeString(this.columnNums);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.indexMappingType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.indexMappingType.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<IndexMapping> getHelper() {
        return IndexMappingHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.search.IndexMapping";
    }

    public String getDatabaseName() {
        return "dsSearch";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("IndexMapping { id: ").append(this.id);
        sb.append(", fieldName: ").append(this.fieldName);
        sb.append(", isColumnNum: ").append(this.isColumnNum);
        sb.append(", columnName: ").append(this.columnName);
        sb.append(", columnNums: ").append(this.columnNums);
        sb.append(", indexMappingType: ").append(this.indexMappingType);
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
