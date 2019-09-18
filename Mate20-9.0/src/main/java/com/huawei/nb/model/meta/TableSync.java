package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class TableSync extends AManagedObject {
    public static final Parcelable.Creator<TableSync> CREATOR = new Parcelable.Creator<TableSync>() {
        public TableSync createFromParcel(Parcel in) {
            return new TableSync(in);
        }

        public TableSync[] newArray(int size) {
            return new TableSync[size];
        }
    };
    private Integer mChannel;
    private String mCloudUri;
    private String mDBName;
    private Integer mId;
    private Integer mSyncMode;
    private Integer mSyncTime;
    private String mTableName;
    private Integer mTitle;

    public TableSync(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mDBName = cursor.getString(2);
        this.mTableName = cursor.getString(3);
        this.mCloudUri = cursor.getString(4);
        this.mTitle = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mChannel = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mSyncMode = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mSyncTime = !cursor.isNull(8) ? Integer.valueOf(cursor.getInt(8)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TableSync(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mDBName = in.readByte() == 0 ? null : in.readString();
        this.mTableName = in.readByte() == 0 ? null : in.readString();
        this.mCloudUri = in.readByte() == 0 ? null : in.readString();
        this.mTitle = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mChannel = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mSyncMode = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.mSyncTime = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private TableSync(Integer mId2, String mDBName2, String mTableName2, String mCloudUri2, Integer mTitle2, Integer mChannel2, Integer mSyncMode2, Integer mSyncTime2) {
        this.mId = mId2;
        this.mDBName = mDBName2;
        this.mTableName = mTableName2;
        this.mCloudUri = mCloudUri2;
        this.mTitle = mTitle2;
        this.mChannel = mChannel2;
        this.mSyncMode = mSyncMode2;
        this.mSyncTime = mSyncTime2;
    }

    public TableSync() {
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

    public String getMTableName() {
        return this.mTableName;
    }

    public void setMTableName(String mTableName2) {
        this.mTableName = mTableName2;
        setValue();
    }

    public String getMCloudUri() {
        return this.mCloudUri;
    }

    public void setMCloudUri(String mCloudUri2) {
        this.mCloudUri = mCloudUri2;
        setValue();
    }

    public Integer getMTitle() {
        return this.mTitle;
    }

    public void setMTitle(Integer mTitle2) {
        this.mTitle = mTitle2;
        setValue();
    }

    public Integer getMChannel() {
        return this.mChannel;
    }

    public void setMChannel(Integer mChannel2) {
        this.mChannel = mChannel2;
        setValue();
    }

    public Integer getMSyncMode() {
        return this.mSyncMode;
    }

    public void setMSyncMode(Integer mSyncMode2) {
        this.mSyncMode = mSyncMode2;
        setValue();
    }

    public Integer getMSyncTime() {
        return this.mSyncTime;
    }

    public void setMSyncTime(Integer mSyncTime2) {
        this.mSyncTime = mSyncTime2;
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
        if (this.mTableName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mTableName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mCloudUri != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mCloudUri);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mTitle != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mTitle.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mChannel != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mChannel.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSyncMode != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mSyncMode.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mSyncTime != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mSyncTime.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<TableSync> getHelper() {
        return TableSyncHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.TableSync";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("TableSync { mId: ").append(this.mId);
        sb.append(", mDBName: ").append(this.mDBName);
        sb.append(", mTableName: ").append(this.mTableName);
        sb.append(", mCloudUri: ").append(this.mCloudUri);
        sb.append(", mTitle: ").append(this.mTitle);
        sb.append(", mChannel: ").append(this.mChannel);
        sb.append(", mSyncMode: ").append(this.mSyncMode);
        sb.append(", mSyncTime: ").append(this.mSyncTime);
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
