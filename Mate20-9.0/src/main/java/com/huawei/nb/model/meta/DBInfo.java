package com.huawei.nb.model.meta;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class DBInfo extends AManagedObject {
    public static final Parcelable.Creator<DBInfo> CREATOR = new Parcelable.Creator<DBInfo>() {
        public DBInfo createFromParcel(Parcel in) {
            return new DBInfo(in);
        }

        public DBInfo[] newArray(int size) {
            return new DBInfo[size];
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

    public DBInfo(Cursor cursor) {
        Boolean valueOf;
        Boolean valueOf2;
        boolean z = true;
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.dbName = cursor.getString(2);
        this.dbPath = cursor.getString(3);
        this.dbType = cursor.isNull(4) ? null : Integer.valueOf(cursor.getInt(4));
        this.description = cursor.getString(5);
        this.modelXml = cursor.getString(6);
        this.dataXml = cursor.getString(7);
        if (cursor.isNull(8)) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(cursor.getInt(8) != 0);
        }
        this.isEncrypt = valueOf;
        this.ownerBusinessId = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.recovery = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        if (cursor.isNull(11)) {
            valueOf2 = null;
        } else {
            valueOf2 = Boolean.valueOf(cursor.getInt(11) == 0 ? false : z);
        }
        this.isCreate = valueOf2;
        this.initMode = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.capacity = cursor.isNull(13) ? null : Long.valueOf(cursor.getLong(13));
        this.actionAfterFull = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.config = cursor.getString(15);
        this.shareMode = !cursor.isNull(16) ? Integer.valueOf(cursor.getInt(16)) : num;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DBInfo(Parcel in) {
        super(in);
        Boolean valueOf;
        Boolean valueOf2;
        boolean z = true;
        Integer num = null;
        if (in.readByte() == 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.dbName = in.readByte() == 0 ? null : in.readString();
        this.dbPath = in.readByte() == 0 ? null : in.readString();
        this.dbType = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.description = in.readByte() == 0 ? null : in.readString();
        this.modelXml = in.readByte() == 0 ? null : in.readString();
        this.dataXml = in.readByte() == 0 ? null : in.readString();
        if (in.readByte() == 0) {
            valueOf = null;
        } else {
            valueOf = Boolean.valueOf(in.readByte() != 0);
        }
        this.isEncrypt = valueOf;
        this.ownerBusinessId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.recovery = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        if (in.readByte() == 0) {
            valueOf2 = null;
        } else {
            valueOf2 = Boolean.valueOf(in.readByte() == 0 ? false : z);
        }
        this.isCreate = valueOf2;
        this.initMode = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.capacity = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.actionAfterFull = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.config = in.readByte() == 0 ? null : in.readString();
        this.shareMode = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private DBInfo(Integer mId2, String dbName2, String dbPath2, Integer dbType2, String description2, String modelXml2, String dataXml2, Boolean isEncrypt2, Integer ownerBusinessId2, Integer recovery2, Boolean isCreate2, Integer initMode2, Long capacity2, Integer actionAfterFull2, String config2, Integer shareMode2) {
        this.mId = mId2;
        this.dbName = dbName2;
        this.dbPath = dbPath2;
        this.dbType = dbType2;
        this.description = description2;
        this.modelXml = modelXml2;
        this.dataXml = dataXml2;
        this.isEncrypt = isEncrypt2;
        this.ownerBusinessId = ownerBusinessId2;
        this.recovery = recovery2;
        this.isCreate = isCreate2;
        this.initMode = initMode2;
        this.capacity = capacity2;
        this.actionAfterFull = actionAfterFull2;
        this.config = config2;
        this.shareMode = shareMode2;
    }

    public DBInfo() {
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

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName2) {
        this.dbName = dbName2;
        setValue();
    }

    public String getDbPath() {
        return this.dbPath;
    }

    public void setDbPath(String dbPath2) {
        this.dbPath = dbPath2;
        setValue();
    }

    public Integer getDbType() {
        return this.dbType;
    }

    public void setDbType(Integer dbType2) {
        this.dbType = dbType2;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
        setValue();
    }

    public String getModelXml() {
        return this.modelXml;
    }

    public void setModelXml(String modelXml2) {
        this.modelXml = modelXml2;
        setValue();
    }

    public String getDataXml() {
        return this.dataXml;
    }

    public void setDataXml(String dataXml2) {
        this.dataXml = dataXml2;
        setValue();
    }

    public Boolean getIsEncrypt() {
        return this.isEncrypt;
    }

    public void setIsEncrypt(Boolean isEncrypt2) {
        this.isEncrypt = isEncrypt2;
        setValue();
    }

    public Integer getOwnerBusinessId() {
        return this.ownerBusinessId;
    }

    public void setOwnerBusinessId(Integer ownerBusinessId2) {
        this.ownerBusinessId = ownerBusinessId2;
        setValue();
    }

    public Integer getRecovery() {
        return this.recovery;
    }

    public void setRecovery(Integer recovery2) {
        this.recovery = recovery2;
        setValue();
    }

    public Boolean getIsCreate() {
        return this.isCreate;
    }

    public void setIsCreate(Boolean isCreate2) {
        this.isCreate = isCreate2;
        setValue();
    }

    public Integer getInitMode() {
        return this.initMode;
    }

    public void setInitMode(Integer initMode2) {
        this.initMode = initMode2;
        setValue();
    }

    public Long getCapacity() {
        return this.capacity;
    }

    public void setCapacity(Long capacity2) {
        this.capacity = capacity2;
        setValue();
    }

    public Integer getActionAfterFull() {
        return this.actionAfterFull;
    }

    public void setActionAfterFull(Integer actionAfterFull2) {
        this.actionAfterFull = actionAfterFull2;
        setValue();
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config2) {
        this.config = config2;
        setValue();
    }

    public Integer getShareMode() {
        return this.shareMode;
    }

    public void setShareMode(Integer shareMode2) {
        this.shareMode = shareMode2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        byte b;
        byte b2;
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.dbName != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dbName);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dbPath != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dbPath);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dbType != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.dbType.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.description != null) {
            out.writeByte((byte) 1);
            out.writeString(this.description);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.modelXml != null) {
            out.writeByte((byte) 1);
            out.writeString(this.modelXml);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.dataXml != null) {
            out.writeByte((byte) 1);
            out.writeString(this.dataXml);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isEncrypt != null) {
            out.writeByte((byte) 1);
            if (this.isEncrypt.booleanValue()) {
                b2 = 1;
            } else {
                b2 = 0;
            }
            out.writeByte(b2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ownerBusinessId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.ownerBusinessId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.recovery != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.recovery.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.isCreate != null) {
            out.writeByte((byte) 1);
            if (this.isCreate.booleanValue()) {
                b = 1;
            } else {
                b = 0;
            }
            out.writeByte(b);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.initMode != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.initMode.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.capacity != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.capacity.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.actionAfterFull != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.actionAfterFull.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.config != null) {
            out.writeByte((byte) 1);
            out.writeString(this.config);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.shareMode != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.shareMode.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<DBInfo> getHelper() {
        return DBInfoHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.meta.DBInfo";
    }

    public String getDatabaseName() {
        return "dsMeta";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DBInfo { mId: ").append(this.mId);
        sb.append(", dbName: ").append(this.dbName);
        sb.append(", dbPath: ").append(this.dbPath);
        sb.append(", dbType: ").append(this.dbType);
        sb.append(", description: ").append(this.description);
        sb.append(", modelXml: ").append(this.modelXml);
        sb.append(", dataXml: ").append(this.dataXml);
        sb.append(", isEncrypt: ").append(this.isEncrypt);
        sb.append(", ownerBusinessId: ").append(this.ownerBusinessId);
        sb.append(", recovery: ").append(this.recovery);
        sb.append(", isCreate: ").append(this.isCreate);
        sb.append(", initMode: ").append(this.initMode);
        sb.append(", capacity: ").append(this.capacity);
        sb.append(", actionAfterFull: ").append(this.actionAfterFull);
        sb.append(", config: ").append(this.config);
        sb.append(", shareMode: ").append(this.shareMode);
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
        return "0.0.4";
    }

    public int getEntityVersionCode() {
        return 4;
    }
}
