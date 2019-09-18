package com.huawei.nb.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DownGradeTable extends AManagedObject {
    public static final Parcelable.Creator<DownGradeTable> CREATOR = new Parcelable.Creator<DownGradeTable>() {
        public DownGradeTable createFromParcel(Parcel in) {
            return new DownGradeTable(in);
        }

        public DownGradeTable[] newArray(int size) {
            return new DownGradeTable[size];
        }
    };
    private String mDBName;
    private String mFileName;
    private Integer mFromVersion;
    private Integer mId;
    private String mSqlText;
    private Integer mToVersion;

    public DownGradeTable(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mDBName = cursor.getString(2);
        this.mFromVersion = cursor.isNull(3) ? null : Integer.valueOf(cursor.getInt(3));
        this.mToVersion = !cursor.isNull(4) ? Integer.valueOf(cursor.getInt(4)) : num;
        this.mFileName = cursor.getString(5);
        this.mSqlText = cursor.getString(6);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DownGradeTable(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mDBName = in.readByte() == 0 ? null : in.readString();
        this.mFromVersion = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mToVersion = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mFileName = in.readByte() == 0 ? null : in.readString();
        this.mSqlText = in.readByte() != 0 ? in.readString() : str;
    }

    private DownGradeTable(Integer mId2, String mDBName2, Integer mFromVersion2, Integer mToVersion2, String mFileName2, String mSqlText2) {
        this.mId = mId2;
        this.mDBName = mDBName2;
        this.mFromVersion = mFromVersion2;
        this.mToVersion = mToVersion2;
        this.mFileName = mFileName2;
        this.mSqlText = mSqlText2;
    }

    public DownGradeTable() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer mId2) {
        this.mId = mId2;
        setValue();
    }

    public String getMDBName() {
        return this.mDBName;
    }

    public void setMDBName(String mDBName2) {
        this.mDBName = mDBName2;
        setValue();
    }

    public Integer getMFromVersion() {
        return this.mFromVersion;
    }

    public void setMFromVersion(Integer mFromVersion2) {
        this.mFromVersion = mFromVersion2;
        setValue();
    }

    public Integer getMToVersion() {
        return this.mToVersion;
    }

    public void setMToVersion(Integer mToVersion2) {
        this.mToVersion = mToVersion2;
        setValue();
    }

    public String getMFileName() {
        return this.mFileName;
    }

    public void setMFileName(String mFileName2) {
        this.mFileName = mFileName2;
        setValue();
    }

    public String getMSqlText() {
        return this.mSqlText;
    }

    public void setMSqlText(String mSqlText2) {
        this.mSqlText = mSqlText2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mDBName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mDBName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mFromVersion != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mFromVersion.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mToVersion != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mToVersion.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mFileName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mFileName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSqlText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mSqlText);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<DownGradeTable> getHelper() {
        return DownGradeTableHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.DownGradeTable";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DownGradeTable { mId: ").append(this.mId);
        sb.append(", mDBName: ").append(this.mDBName);
        sb.append(", mFromVersion: ").append(this.mFromVersion);
        sb.append(", mToVersion: ").append(this.mToVersion);
        sb.append(", mFileName: ").append(this.mFileName);
        sb.append(", mSqlText: ").append(this.mSqlText);
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
        return "0.0.16";
    }

    public int getDatabaseVersionCode() {
        return 16;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
