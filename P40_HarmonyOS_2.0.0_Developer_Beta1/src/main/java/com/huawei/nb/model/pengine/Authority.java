package com.huawei.nb.model.pengine;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class Authority extends AManagedObject {
    public static final Parcelable.Creator<Authority> CREATOR = new Parcelable.Creator<Authority>() {
        /* class com.huawei.nb.model.pengine.Authority.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Authority createFromParcel(Parcel parcel) {
            return new Authority(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Authority[] newArray(int i) {
            return new Authority[i];
        }
    };
    private String column0;
    private String column1;
    private String column2;
    private String column3;
    private String column4;
    private String column5;
    private String entity;
    private Integer id;
    private String right;
    private Integer type;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsPengineData";
    }

    public String getDatabaseVersion() {
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.pengine.Authority";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public Authority(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.type = !cursor.isNull(2) ? Integer.valueOf(cursor.getInt(2)) : num;
        this.entity = cursor.getString(3);
        this.right = cursor.getString(4);
        this.column0 = cursor.getString(5);
        this.column1 = cursor.getString(6);
        this.column2 = cursor.getString(7);
        this.column3 = cursor.getString(8);
        this.column4 = cursor.getString(9);
        this.column5 = cursor.getString(10);
    }

    public Authority(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.type = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.entity = parcel.readByte() == 0 ? null : parcel.readString();
        this.right = parcel.readByte() == 0 ? null : parcel.readString();
        this.column0 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column3 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column4 = parcel.readByte() == 0 ? null : parcel.readString();
        this.column5 = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private Authority(Integer num, Integer num2, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8) {
        this.id = num;
        this.type = num2;
        this.entity = str;
        this.right = str2;
        this.column0 = str3;
        this.column1 = str4;
        this.column2 = str5;
        this.column3 = str6;
        this.column4 = str7;
        this.column5 = str8;
    }

    public Authority() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer num) {
        this.type = num;
        setValue();
    }

    public String getEntity() {
        return this.entity;
    }

    public void setEntity(String str) {
        this.entity = str;
        setValue();
    }

    public String getRight() {
        return this.right;
    }

    public void setRight(String str) {
        this.right = str;
        setValue();
    }

    public String getColumn0() {
        return this.column0;
    }

    public void setColumn0(String str) {
        this.column0 = str;
        setValue();
    }

    public String getColumn1() {
        return this.column1;
    }

    public void setColumn1(String str) {
        this.column1 = str;
        setValue();
    }

    public String getColumn2() {
        return this.column2;
    }

    public void setColumn2(String str) {
        this.column2 = str;
        setValue();
    }

    public String getColumn3() {
        return this.column3;
    }

    public void setColumn3(String str) {
        this.column3 = str;
        setValue();
    }

    public String getColumn4() {
        return this.column4;
    }

    public void setColumn4(String str) {
        this.column4 = str;
        setValue();
    }

    public String getColumn5() {
        return this.column5;
    }

    public void setColumn5(String str) {
        this.column5 = str;
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
        if (this.type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.type.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.entity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.entity);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.right != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.right);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column0 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column3);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column4 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column4);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.column5 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.column5);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<Authority> getHelper() {
        return AuthorityHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "Authority { id: " + this.id + ", type: " + this.type + ", entity: " + this.entity + ", right: " + this.right + ", column0: " + this.column0 + ", column1: " + this.column1 + ", column2: " + this.column2 + ", column3: " + this.column3 + ", column4: " + this.column4 + ", column5: " + this.column5 + " }";
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
