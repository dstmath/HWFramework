package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DBInfo extends AManagedObject {
    public static final Parcelable.Creator<DBInfo> CREATOR = new Parcelable.Creator<DBInfo>() {
        /* class com.huawei.nb.model.meta.DBInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DBInfo createFromParcel(Parcel parcel) {
            return new DBInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public DBInfo[] newArray(int i) {
            return new DBInfo[i];
        }
    };
    private Integer actionAfterFull;
    private Long capacity;
    private String config;
    private String dataXml;
    private String dbName;
    private String dbPath;
    private Integer dbType;
    private String description;
    private Integer initMode;
    private Boolean isCreate;
    private Boolean isEncrypt;
    private Integer mId;
    private String modelXml;
    private Integer ownerBusinessId;
    private Integer recovery;
    private Integer shareMode = 0;

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
        return "com.huawei.nb.model.meta.DBInfo";
    }

    public String getEntityVersion() {
        return "0.0.4";
    }

    public int getEntityVersionCode() {
        return 4;
    }

    public DBInfo(Cursor cursor) {
        Boolean bool;
        Boolean bool2;
        boolean z = false;
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dbName = cursor.getString(2);
        this.dbPath = cursor.getString(3);
        this.dbType = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.description = cursor.getString(5);
        this.modelXml = cursor.getString(6);
        this.dataXml = cursor.getString(7);
        if (cursor.isNull(8)) {
            bool = null;
        } else {
            bool = Boolean.valueOf(cursor.getInt(8) != 0);
        }
        this.isEncrypt = bool;
        this.ownerBusinessId = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.recovery = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        if (cursor.isNull(11)) {
            bool2 = null;
        } else {
            bool2 = Boolean.valueOf(cursor.getInt(11) != 0 ? true : z);
        }
        this.isCreate = bool2;
        this.initMode = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.capacity = cursor.isNull(13) ? null : Long.valueOf(cursor.getLong(13));
        this.actionAfterFull = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.config = cursor.getString(15);
        this.shareMode = !cursor.isNull(16) ? Integer.valueOf(cursor.getInt(16)) : num;
    }

    public DBInfo(Parcel parcel) {
        super(parcel);
        Boolean bool;
        Boolean bool2;
        boolean z = false;
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.mId = null;
            parcel.readInt();
        } else {
            this.mId = Integer.valueOf(parcel.readInt());
        }
        this.dbName = parcel.readByte() == 0 ? null : parcel.readString();
        this.dbPath = parcel.readByte() == 0 ? null : parcel.readString();
        this.dbType = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.description = parcel.readByte() == 0 ? null : parcel.readString();
        this.modelXml = parcel.readByte() == 0 ? null : parcel.readString();
        this.dataXml = parcel.readByte() == 0 ? null : parcel.readString();
        if (parcel.readByte() == 0) {
            bool = null;
        } else {
            bool = Boolean.valueOf(parcel.readByte() != 0);
        }
        this.isEncrypt = bool;
        this.ownerBusinessId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.recovery = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        if (parcel.readByte() == 0) {
            bool2 = null;
        } else {
            bool2 = Boolean.valueOf(parcel.readByte() != 0 ? true : z);
        }
        this.isCreate = bool2;
        this.initMode = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.capacity = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.actionAfterFull = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.config = parcel.readByte() == 0 ? null : parcel.readString();
        this.shareMode = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private DBInfo(Integer num, String str, String str2, Integer num2, String str3, String str4, String str5, Boolean bool, Integer num3, Integer num4, Boolean bool2, Integer num5, Long l, Integer num6, String str6, Integer num7) {
        this.mId = num;
        this.dbName = str;
        this.dbPath = str2;
        this.dbType = num2;
        this.description = str3;
        this.modelXml = str4;
        this.dataXml = str5;
        this.isEncrypt = bool;
        this.ownerBusinessId = num3;
        this.recovery = num4;
        this.isCreate = bool2;
        this.initMode = num5;
        this.capacity = l;
        this.actionAfterFull = num6;
        this.config = str6;
        this.shareMode = num7;
    }

    public DBInfo() {
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer num) {
        this.mId = num;
        setValue();
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String str) {
        this.dbName = str;
        setValue();
    }

    public String getDbPath() {
        return this.dbPath;
    }

    public void setDbPath(String str) {
        this.dbPath = str;
        setValue();
    }

    public Integer getDbType() {
        return this.dbType;
    }

    public void setDbType(Integer num) {
        this.dbType = num;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
        setValue();
    }

    public String getModelXml() {
        return this.modelXml;
    }

    public void setModelXml(String str) {
        this.modelXml = str;
        setValue();
    }

    public String getDataXml() {
        return this.dataXml;
    }

    public void setDataXml(String str) {
        this.dataXml = str;
        setValue();
    }

    public Boolean getIsEncrypt() {
        return this.isEncrypt;
    }

    public void setIsEncrypt(Boolean bool) {
        this.isEncrypt = bool;
        setValue();
    }

    public Integer getOwnerBusinessId() {
        return this.ownerBusinessId;
    }

    public void setOwnerBusinessId(Integer num) {
        this.ownerBusinessId = num;
        setValue();
    }

    public Integer getRecovery() {
        return this.recovery;
    }

    public void setRecovery(Integer num) {
        this.recovery = num;
        setValue();
    }

    public Boolean getIsCreate() {
        return this.isCreate;
    }

    public void setIsCreate(Boolean bool) {
        this.isCreate = bool;
        setValue();
    }

    public Integer getInitMode() {
        return this.initMode;
    }

    public void setInitMode(Integer num) {
        this.initMode = num;
        setValue();
    }

    public Long getCapacity() {
        return this.capacity;
    }

    public void setCapacity(Long l) {
        this.capacity = l;
        setValue();
    }

    public Integer getActionAfterFull() {
        return this.actionAfterFull;
    }

    public void setActionAfterFull(Integer num) {
        this.actionAfterFull = num;
        setValue();
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String str) {
        this.config = str;
        setValue();
    }

    public Integer getShareMode() {
        return this.shareMode;
    }

    public void setShareMode(Integer num) {
        this.shareMode = num;
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
        if (this.dbName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dbName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dbPath != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dbPath);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dbType != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.dbType.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.description != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.description);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.modelXml != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.modelXml);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.dataXml != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.dataXml);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isEncrypt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeByte(this.isEncrypt.booleanValue() ? (byte) 1 : 0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ownerBusinessId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.ownerBusinessId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.recovery != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.recovery.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.isCreate != null) {
            parcel.writeByte((byte) 1);
            parcel.writeByte(this.isCreate.booleanValue() ? (byte) 1 : 0);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.initMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.initMode.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.capacity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.capacity.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.actionAfterFull != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.actionAfterFull.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.config != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.config);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.shareMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.shareMode.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<DBInfo> getHelper() {
        return DBInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "DBInfo { mId: " + this.mId + ", dbName: " + this.dbName + ", dbPath: " + this.dbPath + ", dbType: " + this.dbType + ", description: " + this.description + ", modelXml: " + this.modelXml + ", dataXml: " + this.dataXml + ", isEncrypt: " + this.isEncrypt + ", ownerBusinessId: " + this.ownerBusinessId + ", recovery: " + this.recovery + ", isCreate: " + this.isCreate + ", initMode: " + this.initMode + ", capacity: " + this.capacity + ", actionAfterFull: " + this.actionAfterFull + ", config: " + this.config + ", shareMode: " + this.shareMode + " }";
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
