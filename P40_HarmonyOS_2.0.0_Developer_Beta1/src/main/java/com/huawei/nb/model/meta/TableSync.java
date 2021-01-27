package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class TableSync extends AManagedObject {
    public static final Parcelable.Creator<TableSync> CREATOR = new Parcelable.Creator<TableSync>() {
        /* class com.huawei.nb.model.meta.TableSync.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TableSync createFromParcel(Parcel parcel) {
            return new TableSync(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public TableSync[] newArray(int i) {
            return new TableSync[i];
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
        return "com.huawei.nb.model.meta.TableSync";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public TableSync(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mDBName = cursor.getString(2);
        this.mTableName = cursor.getString(3);
        this.mCloudUri = cursor.getString(4);
        this.mTitle = cursor.isNull(5) ? null : Integer.valueOf(cursor.getInt(5));
        this.mChannel = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.mSyncMode = cursor.isNull(7) ? null : Integer.valueOf(cursor.getInt(7));
        this.mSyncTime = !cursor.isNull(8) ? Integer.valueOf(cursor.getInt(8)) : num;
    }

    public TableSync(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.mDBName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTableName = parcel.readByte() == 0 ? null : parcel.readString();
        this.mCloudUri = parcel.readByte() == 0 ? null : parcel.readString();
        this.mTitle = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mChannel = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mSyncMode = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mSyncTime = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private TableSync(Integer num, String str, String str2, String str3, Integer num2, Integer num3, Integer num4, Integer num5) {
        this.mId = num;
        this.mDBName = str;
        this.mTableName = str2;
        this.mCloudUri = str3;
        this.mTitle = num2;
        this.mChannel = num3;
        this.mSyncMode = num4;
        this.mSyncTime = num5;
    }

    public TableSync() {
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

    public String getMTableName() {
        return this.mTableName;
    }

    public void setMTableName(String str) {
        this.mTableName = str;
        setValue();
    }

    public String getMCloudUri() {
        return this.mCloudUri;
    }

    public void setMCloudUri(String str) {
        this.mCloudUri = str;
        setValue();
    }

    public Integer getMTitle() {
        return this.mTitle;
    }

    public void setMTitle(Integer num) {
        this.mTitle = num;
        setValue();
    }

    public Integer getMChannel() {
        return this.mChannel;
    }

    public void setMChannel(Integer num) {
        this.mChannel = num;
        setValue();
    }

    public Integer getMSyncMode() {
        return this.mSyncMode;
    }

    public void setMSyncMode(Integer num) {
        this.mSyncMode = num;
        setValue();
    }

    public Integer getMSyncTime() {
        return this.mSyncTime;
    }

    public void setMSyncTime(Integer num) {
        this.mSyncTime = num;
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
        if (this.mTableName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mTableName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mCloudUri != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mCloudUri);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mTitle != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mTitle.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mChannel != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mChannel.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSyncMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mSyncMode.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mSyncTime != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mSyncTime.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<TableSync> getHelper() {
        return TableSyncHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "TableSync { mId: " + this.mId + ", mDBName: " + this.mDBName + ", mTableName: " + this.mTableName + ", mCloudUri: " + this.mCloudUri + ", mTitle: " + this.mTitle + ", mChannel: " + this.mChannel + ", mSyncMode: " + this.mSyncMode + ", mSyncTime: " + this.mSyncTime + " }";
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
