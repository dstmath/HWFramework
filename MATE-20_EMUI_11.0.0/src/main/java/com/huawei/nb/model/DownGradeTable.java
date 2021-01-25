package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DownGradeTable extends AManagedObject {
    public static final Parcelable.Creator<DownGradeTable> CREATOR = new Parcelable.Creator<DownGradeTable>() {
        /* class com.huawei.nb.model.DownGradeTable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DownGradeTable createFromParcel(Parcel parcel) {
            return new DownGradeTable(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DownGradeTable[] newArray(int i) {
            return new DownGradeTable[i];
        }
    };
    private String mDBName;
    private String mFileName;
    private Integer mFromVersion;
    private Integer mId;
    private String mSqlText;
    private Integer mToVersion;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsMeta";
    }

    public String getDatabaseVersion() {
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.DownGradeTable";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public DownGradeTable(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mDBName = cursor.getString(2);
        this.mFromVersion = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mToVersion = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
        this.mFileName = cursor.getString(5);
        this.mSqlText = cursor.getString(6);
    }

    public DownGradeTable(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mDBName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mFromVersion = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mToVersion = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mFileName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mSqlText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private DownGradeTable(Integer num, String str, Integer num2, Integer num3, String str2, String str3) {
        this.mId = num;
        this.mDBName = str;
        this.mFromVersion = num2;
        this.mToVersion = num3;
        this.mFileName = str2;
        this.mSqlText = str3;
    }

    public DownGradeTable() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getMDBName() {
        return this.mDBName;
    }

    public void setMDBName(String str) {
        this.mDBName = str;
        setValue();
    }

    public Integer getMFromVersion() {
        return this.mFromVersion;
    }

    public void setMFromVersion(Integer num) {
        this.mFromVersion = num;
        setValue();
    }

    public Integer getMToVersion() {
        return this.mToVersion;
    }

    public void setMToVersion(Integer num) {
        this.mToVersion = num;
        setValue();
    }

    public String getMFileName() {
        return this.mFileName;
    }

    public void setMFileName(String str) {
        this.mFileName = str;
        setValue();
    }

    public String getMSqlText() {
        return this.mSqlText;
    }

    public void setMSqlText(String str) {
        this.mSqlText = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.mId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.mDBName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mDBName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mFromVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mFromVersion.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mToVersion != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mToVersion.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mFileName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mFileName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSqlText != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mSqlText);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DownGradeTable> getHelper() {
        return DownGradeTableHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DownGradeTable { mId: " + this.mId + ", mDBName: " + this.mDBName + ", mFromVersion: " + this.mFromVersion + ", mToVersion: " + this.mToVersion + ", mFileName: " + this.mFileName + ", mSqlText: " + this.mSqlText + " }";
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
