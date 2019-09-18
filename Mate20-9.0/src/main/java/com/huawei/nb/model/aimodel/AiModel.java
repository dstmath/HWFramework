package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class AiModel extends AManagedObject {
    public static final Parcelable.Creator<AiModel> CREATOR = new Parcelable.Creator<AiModel>() {
        public AiModel createFromParcel(Parcel in) {
            return new AiModel(in);
        }

        public AiModel[] newArray(int size) {
            return new AiModel[size];
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

    public AiModel(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AiModel(Parcel in) {
        super(in);
        Integer num = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readLong();
        } else {
            this.id = Long.valueOf(in.readLong());
        }
        this.key = in.readByte() == 0 ? null : in.readString();
        this.origin_id = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.serial_number = in.readByte() == 0 ? null : in.readString();
        this.name = in.readByte() == 0 ? null : in.readString();
        this.description = in.readByte() == 0 ? null : in.readString();
        this.file_path = in.readByte() == 0 ? null : in.readString();
        this.model_type = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.parent_id = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.top_model_id = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.is_preset_model = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.platform = in.readByte() == 0 ? null : in.readString();
        this.tech_domain = in.readByte() == 0 ? null : in.readString();
        this.busi_domain = in.readByte() == 0 ? null : in.readString();
        this.region = in.readByte() == 0 ? null : in.readString();
        this.chip_type = in.readByte() == 0 ? null : in.readString();
        this.version = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.format = in.readByte() == 0 ? null : in.readString();
        this.storage_type = in.readByte() == 0 ? null : in.readString();
        this.size = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.suffix = in.readByte() == 0 ? null : in.readString();
        this.create_type = in.readByte() == 0 ? null : in.readString();
        this.create_user = in.readByte() == 0 ? null : in.readString();
        this.create_time = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.expired_time = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.last_update_time = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.last_update_type = in.readByte() == 0 ? null : in.readString();
        this.cloud_update_time = in.readByte() == 0 ? null : Long.valueOf(in.readLong());
        this.is_need_authority = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.is_encrypt = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.is_compressed = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.encrypt_desc = in.readByte() == 0 ? null : in.readString();
        this.compression_desc = in.readByte() == 0 ? null : in.readString();
        this.reserved_attributes = in.readByte() == 0 ? null : in.readString();
        this.cloud_update_policy = in.readByte() == 0 ? null : in.readString();
        this.allowed_user = in.readByte() == 0 ? null : in.readString();
        this.current_business = in.readByte() == 0 ? null : in.readString();
        this.usable_condition = in.readByte() == 0 ? null : in.readString();
        this.is_none = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.none_type = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.priority = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.check_code = in.readByte() == 0 ? null : in.readString();
        this.reserved_1 = in.readByte() == 0 ? null : in.readString();
        this.reserved_2 = in.readByte() == 0 ? null : in.readString();
        this.resid = in.readByte() == 0 ? null : in.readString();
        this.sha256 = in.readByte() == 0 ? null : in.readString();
        this.verify_result = in.readByte() != 0 ? Integer.valueOf(in.readInt()) : num;
    }

    private AiModel(Long id2, String key2, Long origin_id2, String serial_number2, String name2, String description2, String file_path2, Integer model_type2, Long parent_id2, Long top_model_id2, Integer is_preset_model2, String platform2, String tech_domain2, String busi_domain2, String region2, String chip_type2, Long version2, String format2, String storage_type2, Long size2, String suffix2, String create_type2, String create_user2, Long create_time2, Long expired_time2, Long last_update_time2, String last_update_type2, Long cloud_update_time2, Integer is_need_authority2, Integer is_encrypt2, Integer is_compressed2, String encrypt_desc2, String compression_desc2, String reserved_attributes2, String cloud_update_policy2, String allowed_user2, String current_business2, String usable_condition2, Integer is_none2, Integer none_type2, Integer priority2, String check_code2, String reserved_12, String reserved_22, String resid2, String sha2562, Integer verify_result2) {
        this.id = id2;
        this.key = key2;
        this.origin_id = origin_id2;
        this.serial_number = serial_number2;
        this.name = name2;
        this.description = description2;
        this.file_path = file_path2;
        this.model_type = model_type2;
        this.parent_id = parent_id2;
        this.top_model_id = top_model_id2;
        this.is_preset_model = is_preset_model2;
        this.platform = platform2;
        this.tech_domain = tech_domain2;
        this.busi_domain = busi_domain2;
        this.region = region2;
        this.chip_type = chip_type2;
        this.version = version2;
        this.format = format2;
        this.storage_type = storage_type2;
        this.size = size2;
        this.suffix = suffix2;
        this.create_type = create_type2;
        this.create_user = create_user2;
        this.create_time = create_time2;
        this.expired_time = expired_time2;
        this.last_update_time = last_update_time2;
        this.last_update_type = last_update_type2;
        this.cloud_update_time = cloud_update_time2;
        this.is_need_authority = is_need_authority2;
        this.is_encrypt = is_encrypt2;
        this.is_compressed = is_compressed2;
        this.encrypt_desc = encrypt_desc2;
        this.compression_desc = compression_desc2;
        this.reserved_attributes = reserved_attributes2;
        this.cloud_update_policy = cloud_update_policy2;
        this.allowed_user = allowed_user2;
        this.current_business = current_business2;
        this.usable_condition = usable_condition2;
        this.is_none = is_none2;
        this.none_type = none_type2;
        this.priority = priority2;
        this.check_code = check_code2;
        this.reserved_1 = reserved_12;
        this.reserved_2 = reserved_22;
        this.resid = resid2;
        this.sha256 = sha2562;
        this.verify_result = verify_result2;
    }

    public AiModel() {
    }

    public int describeContents() {
        return 0;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id2) {
        this.id = id2;
        setValue();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
        setValue();
    }

    public Long getOrigin_id() {
        return this.origin_id;
    }

    public void setOrigin_id(Long origin_id2) {
        this.origin_id = origin_id2;
        setValue();
    }

    public String getSerial_number() {
        return this.serial_number;
    }

    public void setSerial_number(String serial_number2) {
        this.serial_number = serial_number2;
        setValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
        setValue();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description2) {
        this.description = description2;
        setValue();
    }

    public String getFile_path() {
        return this.file_path;
    }

    public void setFile_path(String file_path2) {
        this.file_path = file_path2;
        setValue();
    }

    public Integer getModel_type() {
        return this.model_type;
    }

    public void setModel_type(Integer model_type2) {
        this.model_type = model_type2;
        setValue();
    }

    public Long getParent_id() {
        return this.parent_id;
    }

    public void setParent_id(Long parent_id2) {
        this.parent_id = parent_id2;
        setValue();
    }

    public Long getTop_model_id() {
        return this.top_model_id;
    }

    public void setTop_model_id(Long top_model_id2) {
        this.top_model_id = top_model_id2;
        setValue();
    }

    public Integer getIs_preset_model() {
        return this.is_preset_model;
    }

    public void setIs_preset_model(Integer is_preset_model2) {
        this.is_preset_model = is_preset_model2;
        setValue();
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform2) {
        this.platform = platform2;
        setValue();
    }

    public String getTech_domain() {
        return this.tech_domain;
    }

    public void setTech_domain(String tech_domain2) {
        this.tech_domain = tech_domain2;
        setValue();
    }

    public String getBusi_domain() {
        return this.busi_domain;
    }

    public void setBusi_domain(String busi_domain2) {
        this.busi_domain = busi_domain2;
        setValue();
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region2) {
        this.region = region2;
        setValue();
    }

    public String getChip_type() {
        return this.chip_type;
    }

    public void setChip_type(String chip_type2) {
        this.chip_type = chip_type2;
        setValue();
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version2) {
        this.version = version2;
        setValue();
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format2) {
        this.format = format2;
        setValue();
    }

    public String getStorage_type() {
        return this.storage_type;
    }

    public void setStorage_type(String storage_type2) {
        this.storage_type = storage_type2;
        setValue();
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(Long size2) {
        this.size = size2;
        setValue();
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix2) {
        this.suffix = suffix2;
        setValue();
    }

    public String getCreate_type() {
        return this.create_type;
    }

    public void setCreate_type(String create_type2) {
        this.create_type = create_type2;
        setValue();
    }

    public String getCreate_user() {
        return this.create_user;
    }

    public void setCreate_user(String create_user2) {
        this.create_user = create_user2;
        setValue();
    }

    public Long getCreate_time() {
        return this.create_time;
    }

    public void setCreate_time(Long create_time2) {
        this.create_time = create_time2;
        setValue();
    }

    public Long getExpired_time() {
        return this.expired_time;
    }

    public void setExpired_time(Long expired_time2) {
        this.expired_time = expired_time2;
        setValue();
    }

    public Long getLast_update_time() {
        return this.last_update_time;
    }

    public void setLast_update_time(Long last_update_time2) {
        this.last_update_time = last_update_time2;
        setValue();
    }

    public String getLast_update_type() {
        return this.last_update_type;
    }

    public void setLast_update_type(String last_update_type2) {
        this.last_update_type = last_update_type2;
        setValue();
    }

    public Long getCloud_update_time() {
        return this.cloud_update_time;
    }

    public void setCloud_update_time(Long cloud_update_time2) {
        this.cloud_update_time = cloud_update_time2;
        setValue();
    }

    public Integer getIs_need_authority() {
        return this.is_need_authority;
    }

    public void setIs_need_authority(Integer is_need_authority2) {
        this.is_need_authority = is_need_authority2;
        setValue();
    }

    public Integer getIs_encrypt() {
        return this.is_encrypt;
    }

    public void setIs_encrypt(Integer is_encrypt2) {
        this.is_encrypt = is_encrypt2;
        setValue();
    }

    public Integer getIs_compressed() {
        return this.is_compressed;
    }

    public void setIs_compressed(Integer is_compressed2) {
        this.is_compressed = is_compressed2;
        setValue();
    }

    public String getEncrypt_desc() {
        return this.encrypt_desc;
    }

    public void setEncrypt_desc(String encrypt_desc2) {
        this.encrypt_desc = encrypt_desc2;
        setValue();
    }

    public String getCompression_desc() {
        return this.compression_desc;
    }

    public void setCompression_desc(String compression_desc2) {
        this.compression_desc = compression_desc2;
        setValue();
    }

    public String getReserved_attributes() {
        return this.reserved_attributes;
    }

    public void setReserved_attributes(String reserved_attributes2) {
        this.reserved_attributes = reserved_attributes2;
        setValue();
    }

    public String getCloud_update_policy() {
        return this.cloud_update_policy;
    }

    public void setCloud_update_policy(String cloud_update_policy2) {
        this.cloud_update_policy = cloud_update_policy2;
        setValue();
    }

    public String getAllowed_user() {
        return this.allowed_user;
    }

    public void setAllowed_user(String allowed_user2) {
        this.allowed_user = allowed_user2;
        setValue();
    }

    public String getCurrent_business() {
        return this.current_business;
    }

    public void setCurrent_business(String current_business2) {
        this.current_business = current_business2;
        setValue();
    }

    public String getUsable_condition() {
        return this.usable_condition;
    }

    public void setUsable_condition(String usable_condition2) {
        this.usable_condition = usable_condition2;
        setValue();
    }

    public Integer getIs_none() {
        return this.is_none;
    }

    public void setIs_none(Integer is_none2) {
        this.is_none = is_none2;
        setValue();
    }

    public Integer getNone_type() {
        return this.none_type;
    }

    public void setNone_type(Integer none_type2) {
        this.none_type = none_type2;
        setValue();
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority2) {
        this.priority = priority2;
        setValue();
    }

    public String getCheck_code() {
        return this.check_code;
    }

    public void setCheck_code(String check_code2) {
        this.check_code = check_code2;
        setValue();
    }

    public String getReserved_1() {
        return this.reserved_1;
    }

    public void setReserved_1(String reserved_12) {
        this.reserved_1 = reserved_12;
        setValue();
    }

    public String getReserved_2() {
        return this.reserved_2;
    }

    public void setReserved_2(String reserved_22) {
        this.reserved_2 = reserved_22;
        setValue();
    }

    public String getResid() {
        return this.resid;
    }

    public void setResid(String resid2) {
        this.resid = resid2;
        setValue();
    }

    public String getSha256() {
        return this.sha256;
    }

    public void setSha256(String sha2562) {
        this.sha256 = sha2562;
        setValue();
    }

    public Integer getVerify_result() {
        return this.verify_result;
    }

    public void setVerify_result(Integer verify_result2) {
        this.verify_result = verify_result2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        if (this.key != null) {
            out.writeByte((byte) 1);
            out.writeString(this.key);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.origin_id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.origin_id.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.serial_number != null) {
            out.writeByte((byte) 1);
            out.writeString(this.serial_number);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.name != null) {
            out.writeByte((byte) 1);
            out.writeString(this.name);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.description != null) {
            out.writeByte((byte) 1);
            out.writeString(this.description);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.file_path != null) {
            out.writeByte((byte) 1);
            out.writeString(this.file_path);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.model_type != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.model_type.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.parent_id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.parent_id.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top_model_id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.top_model_id.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.is_preset_model != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.is_preset_model.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.platform != null) {
            out.writeByte((byte) 1);
            out.writeString(this.platform);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.tech_domain != null) {
            out.writeByte((byte) 1);
            out.writeString(this.tech_domain);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.busi_domain != null) {
            out.writeByte((byte) 1);
            out.writeString(this.busi_domain);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.region != null) {
            out.writeByte((byte) 1);
            out.writeString(this.region);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.chip_type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.chip_type);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.version != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.version.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.format != null) {
            out.writeByte((byte) 1);
            out.writeString(this.format);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.storage_type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.storage_type);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.size != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.size.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.suffix != null) {
            out.writeByte((byte) 1);
            out.writeString(this.suffix);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.create_type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.create_type);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.create_user != null) {
            out.writeByte((byte) 1);
            out.writeString(this.create_user);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.create_time != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.create_time.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.expired_time != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.expired_time.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.last_update_time != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.last_update_time.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.last_update_type != null) {
            out.writeByte((byte) 1);
            out.writeString(this.last_update_type);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.cloud_update_time != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.cloud_update_time.longValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.is_need_authority != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.is_need_authority.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.is_encrypt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.is_encrypt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.is_compressed != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.is_compressed.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.encrypt_desc != null) {
            out.writeByte((byte) 1);
            out.writeString(this.encrypt_desc);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.compression_desc != null) {
            out.writeByte((byte) 1);
            out.writeString(this.compression_desc);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved_attributes != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved_attributes);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.cloud_update_policy != null) {
            out.writeByte((byte) 1);
            out.writeString(this.cloud_update_policy);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.allowed_user != null) {
            out.writeByte((byte) 1);
            out.writeString(this.allowed_user);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.current_business != null) {
            out.writeByte((byte) 1);
            out.writeString(this.current_business);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.usable_condition != null) {
            out.writeByte((byte) 1);
            out.writeString(this.usable_condition);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.is_none != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.is_none.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.none_type != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.none_type.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.priority != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.priority.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.check_code != null) {
            out.writeByte((byte) 1);
            out.writeString(this.check_code);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved_1 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved_1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.reserved_2 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.reserved_2);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.resid != null) {
            out.writeByte((byte) 1);
            out.writeString(this.resid);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.sha256 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.sha256);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.verify_result != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.verify_result.intValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<AiModel> getHelper() {
        return AiModelHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.aimodel.AiModel";
    }

    public String getDatabaseName() {
        return "dsAiModel";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("AiModel { id: ").append(this.id);
        sb.append(", key: ").append(this.key);
        sb.append(", origin_id: ").append(this.origin_id);
        sb.append(", serial_number: ").append(this.serial_number);
        sb.append(", name: ").append(this.name);
        sb.append(", description: ").append(this.description);
        sb.append(", file_path: ").append(this.file_path);
        sb.append(", model_type: ").append(this.model_type);
        sb.append(", parent_id: ").append(this.parent_id);
        sb.append(", top_model_id: ").append(this.top_model_id);
        sb.append(", is_preset_model: ").append(this.is_preset_model);
        sb.append(", platform: ").append(this.platform);
        sb.append(", tech_domain: ").append(this.tech_domain);
        sb.append(", busi_domain: ").append(this.busi_domain);
        sb.append(", region: ").append(this.region);
        sb.append(", chip_type: ").append(this.chip_type);
        sb.append(", version: ").append(this.version);
        sb.append(", format: ").append(this.format);
        sb.append(", storage_type: ").append(this.storage_type);
        sb.append(", size: ").append(this.size);
        sb.append(", suffix: ").append(this.suffix);
        sb.append(", create_type: ").append(this.create_type);
        sb.append(", create_user: ").append(this.create_user);
        sb.append(", create_time: ").append(this.create_time);
        sb.append(", expired_time: ").append(this.expired_time);
        sb.append(", last_update_time: ").append(this.last_update_time);
        sb.append(", last_update_type: ").append(this.last_update_type);
        sb.append(", cloud_update_time: ").append(this.cloud_update_time);
        sb.append(", is_need_authority: ").append(this.is_need_authority);
        sb.append(", is_encrypt: ").append(this.is_encrypt);
        sb.append(", is_compressed: ").append(this.is_compressed);
        sb.append(", encrypt_desc: ").append(this.encrypt_desc);
        sb.append(", compression_desc: ").append(this.compression_desc);
        sb.append(", reserved_attributes: ").append(this.reserved_attributes);
        sb.append(", cloud_update_policy: ").append(this.cloud_update_policy);
        sb.append(", allowed_user: ").append(this.allowed_user);
        sb.append(", current_business: ").append(this.current_business);
        sb.append(", usable_condition: ").append(this.usable_condition);
        sb.append(", is_none: ").append(this.is_none);
        sb.append(", none_type: ").append(this.none_type);
        sb.append(", priority: ").append(this.priority);
        sb.append(", check_code: ").append(this.check_code);
        sb.append(", reserved_1: ").append(this.reserved_1);
        sb.append(", reserved_2: ").append(this.reserved_2);
        sb.append(", resid: ").append(this.resid);
        sb.append(", sha256: ").append(this.sha256);
        sb.append(", verify_result: ").append(this.verify_result);
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
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    public String getEntityVersion() {
        return "0.0.3";
    }

    public int getEntityVersionCode() {
        return 3;
    }
}
