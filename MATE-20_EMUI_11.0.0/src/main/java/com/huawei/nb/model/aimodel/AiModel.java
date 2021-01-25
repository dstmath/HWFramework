package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModel extends AManagedObject {
    public static final Parcelable.Creator<AiModel> CREATOR = new Parcelable.Creator<AiModel>() {
        /* class com.huawei.nb.model.aimodel.AiModel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModel createFromParcel(Parcel parcel) {
            return new AiModel(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModel[] newArray(int i) {
            return new AiModel[i];
        }
    };
    private String allowed_user;
    private String busi_domain;
    private String check_code;
    private String chip_type;
    private String cloud_update_policy;
    private Long cloud_update_time;
    private String compression_desc;
    private Long create_time;
    private String create_type;
    private String create_user;
    private String current_business;
    private String description;
    private String encrypt_desc;
    private Long expired_time;
    private String file_path;
    private String format;
    private Long id;
    private Integer is_compressed;
    private Integer is_encrypt;
    private Integer is_need_authority;
    private Integer is_none = 0;
    private Integer is_preset_model;
    private String key;
    private Long last_update_time;
    private String last_update_type;
    private Integer model_type;
    private String name;
    private Integer none_type = 0;
    private Long origin_id;
    private Long parent_id;
    private String platform;
    private Integer priority;
    private String region;
    private String reserved_1;
    private String reserved_2;
    private String reserved_attributes;
    private String resid;
    private String serial_number;
    private String sha256;
    private Long size;
    private String storage_type;
    private String suffix;
    private String tech_domain;
    private Long top_model_id;
    private String usable_condition;
    private Integer verify_result;
    private Long version;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModel";
    }

    public String getEntityVersion() {
        return "0.0.3";
    }

    public int getEntityVersionCode() {
        return 3;
    }

    public AiModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.key = cursor.getString(2);
        this.origin_id = cursor.isNull(3) ? null : Long.valueOf(cursor.getLong(3));
        this.serial_number = cursor.getString(4);
        this.name = cursor.getString(5);
        this.description = cursor.getString(6);
        this.file_path = cursor.getString(7);
        this.model_type = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.parent_id = cursor.isNull(9) ? null : Long.valueOf(cursor.getLong(9));
        this.top_model_id = cursor.isNull(10) ? null : Long.valueOf(cursor.getLong(10));
        this.is_preset_model = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.platform = cursor.getString(12);
        this.tech_domain = cursor.getString(13);
        this.busi_domain = cursor.getString(14);
        this.region = cursor.getString(15);
        this.chip_type = cursor.getString(16);
        this.version = cursor.isNull(17) ? null : Long.valueOf(cursor.getLong(17));
        this.format = cursor.getString(18);
        this.storage_type = cursor.getString(19);
        this.size = cursor.isNull(20) ? null : Long.valueOf(cursor.getLong(20));
        this.suffix = cursor.getString(21);
        this.create_type = cursor.getString(22);
        this.create_user = cursor.getString(23);
        this.create_time = cursor.isNull(24) ? null : Long.valueOf(cursor.getLong(24));
        this.expired_time = cursor.isNull(25) ? null : Long.valueOf(cursor.getLong(25));
        this.last_update_time = cursor.isNull(26) ? null : Long.valueOf(cursor.getLong(26));
        this.last_update_type = cursor.getString(27);
        this.cloud_update_time = cursor.isNull(28) ? null : Long.valueOf(cursor.getLong(28));
        this.is_need_authority = cursor.isNull(29) ? null : Integer.valueOf(cursor.getInt(29));
        this.is_encrypt = cursor.isNull(30) ? null : Integer.valueOf(cursor.getInt(30));
        this.is_compressed = cursor.isNull(31) ? null : Integer.valueOf(cursor.getInt(31));
        this.encrypt_desc = cursor.getString(32);
        this.compression_desc = cursor.getString(33);
        this.reserved_attributes = cursor.getString(34);
        this.cloud_update_policy = cursor.getString(35);
        this.allowed_user = cursor.getString(36);
        this.current_business = cursor.getString(37);
        this.usable_condition = cursor.getString(38);
        this.is_none = cursor.isNull(39) ? null : Integer.valueOf(cursor.getInt(39));
        this.none_type = cursor.isNull(40) ? null : Integer.valueOf(cursor.getInt(40));
        this.priority = cursor.isNull(41) ? null : Integer.valueOf(cursor.getInt(41));
        this.check_code = cursor.getString(42);
        this.reserved_1 = cursor.getString(43);
        this.reserved_2 = cursor.getString(44);
        this.resid = cursor.getString(45);
        this.sha256 = cursor.getString(46);
        this.verify_result = !cursor.isNull(47) ? Integer.valueOf(cursor.getInt(47)) : num;
    }

    public AiModel(Parcel parcel) {
        super(parcel);
        Integer num = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readLong();
        } else {
            this.id = Long.valueOf(parcel.readLong());
        }
        this.key = parcel.readByte() == 0 ? null : parcel.readString();
        this.origin_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.serial_number = parcel.readByte() == 0 ? null : parcel.readString();
        this.name = parcel.readByte() == 0 ? null : parcel.readString();
        this.description = parcel.readByte() == 0 ? null : parcel.readString();
        this.file_path = parcel.readByte() == 0 ? null : parcel.readString();
        this.model_type = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.parent_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.top_model_id = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.is_preset_model = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.platform = parcel.readByte() == 0 ? null : parcel.readString();
        this.tech_domain = parcel.readByte() == 0 ? null : parcel.readString();
        this.busi_domain = parcel.readByte() == 0 ? null : parcel.readString();
        this.region = parcel.readByte() == 0 ? null : parcel.readString();
        this.chip_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.version = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.format = parcel.readByte() == 0 ? null : parcel.readString();
        this.storage_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.size = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.suffix = parcel.readByte() == 0 ? null : parcel.readString();
        this.create_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.create_user = parcel.readByte() == 0 ? null : parcel.readString();
        this.create_time = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.expired_time = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.last_update_time = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.last_update_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.cloud_update_time = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.is_need_authority = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.is_encrypt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.is_compressed = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.encrypt_desc = parcel.readByte() == 0 ? null : parcel.readString();
        this.compression_desc = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved_attributes = parcel.readByte() == 0 ? null : parcel.readString();
        this.cloud_update_policy = parcel.readByte() == 0 ? null : parcel.readString();
        this.allowed_user = parcel.readByte() == 0 ? null : parcel.readString();
        this.current_business = parcel.readByte() == 0 ? null : parcel.readString();
        this.usable_condition = parcel.readByte() == 0 ? null : parcel.readString();
        this.is_none = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.none_type = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.priority = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.check_code = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved_1 = parcel.readByte() == 0 ? null : parcel.readString();
        this.reserved_2 = parcel.readByte() == 0 ? null : parcel.readString();
        this.resid = parcel.readByte() == 0 ? null : parcel.readString();
        this.sha256 = parcel.readByte() == 0 ? null : parcel.readString();
        this.verify_result = parcel.readByte() != 0 ? Integer.valueOf(parcel.readInt()) : num;
    }

    private AiModel(Long l, String str, Long l2, String str2, String str3, String str4, String str5, Integer num, Long l3, Long l4, Integer num2, String str6, String str7, String str8, String str9, String str10, Long l5, String str11, String str12, Long l6, String str13, String str14, String str15, Long l7, Long l8, Long l9, String str16, Long l10, Integer num3, Integer num4, Integer num5, String str17, String str18, String str19, String str20, String str21, String str22, String str23, Integer num6, Integer num7, Integer num8, String str24, String str25, String str26, String str27, String str28, Integer num9) {
        this.id = l;
        this.key = str;
        this.origin_id = l2;
        this.serial_number = str2;
        this.name = str3;
        this.description = str4;
        this.file_path = str5;
        this.model_type = num;
        this.parent_id = l3;
        this.top_model_id = l4;
        this.is_preset_model = num2;
        this.platform = str6;
        this.tech_domain = str7;
        this.busi_domain = str8;
        this.region = str9;
        this.chip_type = str10;
        this.version = l5;
        this.format = str11;
        this.storage_type = str12;
        this.size = l6;
        this.suffix = str13;
        this.create_type = str14;
        this.create_user = str15;
        this.create_time = l7;
        this.expired_time = l8;
        this.last_update_time = l9;
        this.last_update_type = str16;
        this.cloud_update_time = l10;
        this.is_need_authority = num3;
        this.is_encrypt = num4;
        this.is_compressed = num5;
        this.encrypt_desc = str17;
        this.compression_desc = str18;
        this.reserved_attributes = str19;
        this.cloud_update_policy = str20;
        this.allowed_user = str21;
        this.current_business = str22;
        this.usable_condition = str23;
        this.is_none = num6;
        this.none_type = num7;
        this.priority = num8;
        this.check_code = str24;
        this.reserved_1 = str25;
        this.reserved_2 = str26;
        this.resid = str27;
        this.sha256 = str28;
        this.verify_result = num9;
    }

    public AiModel() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
        setValue();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String str) {
        this.key = str;
        setValue();
    }

    public Long getOrigin_id() {
        return this.origin_id;
    }

    public void setOrigin_id(Long l) {
        this.origin_id = l;
        setValue();
    }

    public String getSerial_number() {
        return this.serial_number;
    }

    public void setSerial_number(String str) {
        this.serial_number = str;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String str) {
        this.description = str;
        setValue();
    }

    public String getFile_path() {
        return this.file_path;
    }

    public void setFile_path(String str) {
        this.file_path = str;
        setValue();
    }

    public Integer getModel_type() {
        return this.model_type;
    }

    public void setModel_type(Integer num) {
        this.model_type = num;
        setValue();
    }

    public Long getParent_id() {
        return this.parent_id;
    }

    public void setParent_id(Long l) {
        this.parent_id = l;
        setValue();
    }

    public Long getTop_model_id() {
        return this.top_model_id;
    }

    public void setTop_model_id(Long l) {
        this.top_model_id = l;
        setValue();
    }

    public Integer getIs_preset_model() {
        return this.is_preset_model;
    }

    public void setIs_preset_model(Integer num) {
        this.is_preset_model = num;
        setValue();
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String str) {
        this.platform = str;
        setValue();
    }

    public String getTech_domain() {
        return this.tech_domain;
    }

    public void setTech_domain(String str) {
        this.tech_domain = str;
        setValue();
    }

    public String getBusi_domain() {
        return this.busi_domain;
    }

    public void setBusi_domain(String str) {
        this.busi_domain = str;
        setValue();
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String str) {
        this.region = str;
        setValue();
    }

    public String getChip_type() {
        return this.chip_type;
    }

    public void setChip_type(String str) {
        this.chip_type = str;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long l) {
        this.version = l;
        setValue();
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String str) {
        this.format = str;
        setValue();
    }

    public String getStorage_type() {
        return this.storage_type;
    }

    public void setStorage_type(String str) {
        this.storage_type = str;
        setValue();
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(Long l) {
        this.size = l;
        setValue();
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String str) {
        this.suffix = str;
        setValue();
    }

    public String getCreate_type() {
        return this.create_type;
    }

    public void setCreate_type(String str) {
        this.create_type = str;
        setValue();
    }

    public String getCreate_user() {
        return this.create_user;
    }

    public void setCreate_user(String str) {
        this.create_user = str;
        setValue();
    }

    public Long getCreate_time() {
        return this.create_time;
    }

    public void setCreate_time(Long l) {
        this.create_time = l;
        setValue();
    }

    public Long getExpired_time() {
        return this.expired_time;
    }

    public void setExpired_time(Long l) {
        this.expired_time = l;
        setValue();
    }

    public Long getLast_update_time() {
        return this.last_update_time;
    }

    public void setLast_update_time(Long l) {
        this.last_update_time = l;
        setValue();
    }

    public String getLast_update_type() {
        return this.last_update_type;
    }

    public void setLast_update_type(String str) {
        this.last_update_type = str;
        setValue();
    }

    public Long getCloud_update_time() {
        return this.cloud_update_time;
    }

    public void setCloud_update_time(Long l) {
        this.cloud_update_time = l;
        setValue();
    }

    public Integer getIs_need_authority() {
        return this.is_need_authority;
    }

    public void setIs_need_authority(Integer num) {
        this.is_need_authority = num;
        setValue();
    }

    public Integer getIs_encrypt() {
        return this.is_encrypt;
    }

    public void setIs_encrypt(Integer num) {
        this.is_encrypt = num;
        setValue();
    }

    public Integer getIs_compressed() {
        return this.is_compressed;
    }

    public void setIs_compressed(Integer num) {
        this.is_compressed = num;
        setValue();
    }

    public String getEncrypt_desc() {
        return this.encrypt_desc;
    }

    public void setEncrypt_desc(String str) {
        this.encrypt_desc = str;
        setValue();
    }

    public String getCompression_desc() {
        return this.compression_desc;
    }

    public void setCompression_desc(String str) {
        this.compression_desc = str;
        setValue();
    }

    public String getReserved_attributes() {
        return this.reserved_attributes;
    }

    public void setReserved_attributes(String str) {
        this.reserved_attributes = str;
        setValue();
    }

    public String getCloud_update_policy() {
        return this.cloud_update_policy;
    }

    public void setCloud_update_policy(String str) {
        this.cloud_update_policy = str;
        setValue();
    }

    public String getAllowed_user() {
        return this.allowed_user;
    }

    public void setAllowed_user(String str) {
        this.allowed_user = str;
        setValue();
    }

    public String getCurrent_business() {
        return this.current_business;
    }

    public void setCurrent_business(String str) {
        this.current_business = str;
        setValue();
    }

    public String getUsable_condition() {
        return this.usable_condition;
    }

    public void setUsable_condition(String str) {
        this.usable_condition = str;
        setValue();
    }

    public Integer getIs_none() {
        return this.is_none;
    }

    public void setIs_none(Integer num) {
        this.is_none = num;
        setValue();
    }

    public Integer getNone_type() {
        return this.none_type;
    }

    public void setNone_type(Integer num) {
        this.none_type = num;
        setValue();
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer num) {
        this.priority = num;
        setValue();
    }

    public String getCheck_code() {
        return this.check_code;
    }

    public void setCheck_code(String str) {
        this.check_code = str;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String str) {
        this.reserved_1 = str;
        setValue();
    }

    public String getReserved_2() {
        return this.reserved_2;
    }

    public void setReserved_2(String str) {
        this.reserved_2 = str;
        setValue();
    }

    public String getResid() {
        return this.resid;
    }

    public void setResid(String str) {
        this.resid = str;
        setValue();
    }

    public String getSha256() {
        return this.sha256;
    }

    public void setSha256(String str) {
        this.sha256 = str;
        setValue();
    }

    public Integer getVerify_result() {
        return this.verify_result;
    }

    public void setVerify_result(Integer num) {
        this.verify_result = num;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.key != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.key);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.origin_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.origin_id.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.serial_number != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.serial_number);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.description != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.description);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.file_path != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.file_path);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.model_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.model_type.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.parent_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.parent_id.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top_model_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.top_model_id.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.is_preset_model != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.is_preset_model.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.platform != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.platform);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.tech_domain != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.tech_domain);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.busi_domain != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.busi_domain);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.region != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.region);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.chip_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.chip_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.version != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.version.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.format != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.format);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.storage_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.storage_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.size != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.size.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.suffix != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.suffix);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.create_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.create_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.create_user != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.create_user);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.create_time != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.create_time.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.expired_time != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.expired_time.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.last_update_time != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.last_update_time.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.last_update_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.last_update_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.cloud_update_time != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.cloud_update_time.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.is_need_authority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.is_need_authority.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.is_encrypt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.is_encrypt.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.is_compressed != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.is_compressed.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.encrypt_desc != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.encrypt_desc);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.compression_desc != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.compression_desc);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved_attributes != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved_attributes);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.cloud_update_policy != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.cloud_update_policy);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.allowed_user != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.allowed_user);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.current_business != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.current_business);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.usable_condition != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.usable_condition);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.is_none != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.is_none.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.none_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.none_type.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.priority != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.priority.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.check_code != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.check_code);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved_1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.reserved_2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.reserved_2);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.resid != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.resid);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.sha256 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.sha256);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.verify_result != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.verify_result.intValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<AiModel> getHelper() {
        return AiModelHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "AiModel { id: " + this.id + ", key: " + this.key + ", origin_id: " + this.origin_id + ", serial_number: " + this.serial_number + ", name: " + this.name + ", description: " + this.description + ", file_path: " + this.file_path + ", model_type: " + this.model_type + ", parent_id: " + this.parent_id + ", top_model_id: " + this.top_model_id + ", is_preset_model: " + this.is_preset_model + ", platform: " + this.platform + ", tech_domain: " + this.tech_domain + ", busi_domain: " + this.busi_domain + ", region: " + this.region + ", chip_type: " + this.chip_type + ", version: " + this.version + ", format: " + this.format + ", storage_type: " + this.storage_type + ", size: " + this.size + ", suffix: " + this.suffix + ", create_type: " + this.create_type + ", create_user: " + this.create_user + ", create_time: " + this.create_time + ", expired_time: " + this.expired_time + ", last_update_time: " + this.last_update_time + ", last_update_type: " + this.last_update_type + ", cloud_update_time: " + this.cloud_update_time + ", is_need_authority: " + this.is_need_authority + ", is_encrypt: " + this.is_encrypt + ", is_compressed: " + this.is_compressed + ", encrypt_desc: " + this.encrypt_desc + ", compression_desc: " + this.compression_desc + ", reserved_attributes: " + this.reserved_attributes + ", cloud_update_policy: " + this.cloud_update_policy + ", allowed_user: " + this.allowed_user + ", current_business: " + this.current_business + ", usable_condition: " + this.usable_condition + ", is_none: " + this.is_none + ", none_type: " + this.none_type + ", priority: " + this.priority + ", check_code: " + this.check_code + ", reserved_1: " + this.reserved_1 + ", reserved_2: " + this.reserved_2 + ", resid: " + this.resid + ", sha256: " + this.sha256 + ", verify_result: " + this.verify_result + " }";
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
