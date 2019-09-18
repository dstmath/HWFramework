package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelHelper extends AEntityHelper<AiModel> {
    private static final AiModelHelper INSTANCE = new AiModelHelper();

    private AiModelHelper() {
    }

    public static AiModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModel object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String key = object.getKey();
        if (key != null) {
            statement.bindString(2, key);
        } else {
            statement.bindNull(2);
        }
        Long origin_id = object.getOrigin_id();
        if (origin_id != null) {
            statement.bindLong(3, origin_id.longValue());
        } else {
            statement.bindNull(3);
        }
        String serial_number = object.getSerial_number();
        if (serial_number != null) {
            statement.bindString(4, serial_number);
        } else {
            statement.bindNull(4);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(5, name);
        } else {
            statement.bindNull(5);
        }
        String description = object.getDescription();
        if (description != null) {
            statement.bindString(6, description);
        } else {
            statement.bindNull(6);
        }
        String file_path = object.getFile_path();
        if (file_path != null) {
            statement.bindString(7, file_path);
        } else {
            statement.bindNull(7);
        }
        Integer model_type = object.getModel_type();
        if (model_type != null) {
            statement.bindLong(8, (long) model_type.intValue());
        } else {
            statement.bindNull(8);
        }
        Long parent_id = object.getParent_id();
        if (parent_id != null) {
            statement.bindLong(9, parent_id.longValue());
        } else {
            statement.bindNull(9);
        }
        Long top_model_id = object.getTop_model_id();
        if (top_model_id != null) {
            statement.bindLong(10, top_model_id.longValue());
        } else {
            statement.bindNull(10);
        }
        Integer is_preset_model = object.getIs_preset_model();
        if (is_preset_model != null) {
            statement.bindLong(11, (long) is_preset_model.intValue());
        } else {
            statement.bindNull(11);
        }
        String platform = object.getPlatform();
        if (platform != null) {
            statement.bindString(12, platform);
        } else {
            statement.bindNull(12);
        }
        String tech_domain = object.getTech_domain();
        if (tech_domain != null) {
            statement.bindString(13, tech_domain);
        } else {
            statement.bindNull(13);
        }
        String busi_domain = object.getBusi_domain();
        if (busi_domain != null) {
            statement.bindString(14, busi_domain);
        } else {
            statement.bindNull(14);
        }
        String region = object.getRegion();
        if (region != null) {
            statement.bindString(15, region);
        } else {
            statement.bindNull(15);
        }
        String chip_type = object.getChip_type();
        if (chip_type != null) {
            statement.bindString(16, chip_type);
        } else {
            statement.bindNull(16);
        }
        Long version = object.getVersion();
        if (version != null) {
            statement.bindLong(17, version.longValue());
        } else {
            statement.bindNull(17);
        }
        String format = object.getFormat();
        if (format != null) {
            statement.bindString(18, format);
        } else {
            statement.bindNull(18);
        }
        String storage_type = object.getStorage_type();
        if (storage_type != null) {
            statement.bindString(19, storage_type);
        } else {
            statement.bindNull(19);
        }
        Long size = object.getSize();
        if (size != null) {
            statement.bindLong(20, size.longValue());
        } else {
            statement.bindNull(20);
        }
        String suffix = object.getSuffix();
        if (suffix != null) {
            statement.bindString(21, suffix);
        } else {
            statement.bindNull(21);
        }
        String create_type = object.getCreate_type();
        if (create_type != null) {
            statement.bindString(22, create_type);
        } else {
            statement.bindNull(22);
        }
        String create_user = object.getCreate_user();
        if (create_user != null) {
            statement.bindString(23, create_user);
        } else {
            statement.bindNull(23);
        }
        Long create_time = object.getCreate_time();
        if (create_time != null) {
            statement.bindLong(24, create_time.longValue());
        } else {
            statement.bindNull(24);
        }
        Long expired_time = object.getExpired_time();
        if (expired_time != null) {
            statement.bindLong(25, expired_time.longValue());
        } else {
            statement.bindNull(25);
        }
        Long last_update_time = object.getLast_update_time();
        if (last_update_time != null) {
            statement.bindLong(26, last_update_time.longValue());
        } else {
            statement.bindNull(26);
        }
        String last_update_type = object.getLast_update_type();
        if (last_update_type != null) {
            statement.bindString(27, last_update_type);
        } else {
            statement.bindNull(27);
        }
        Long cloud_update_time = object.getCloud_update_time();
        if (cloud_update_time != null) {
            statement.bindLong(28, cloud_update_time.longValue());
        } else {
            statement.bindNull(28);
        }
        Integer is_need_authority = object.getIs_need_authority();
        if (is_need_authority != null) {
            statement.bindLong(29, (long) is_need_authority.intValue());
        } else {
            statement.bindNull(29);
        }
        Integer is_encrypt = object.getIs_encrypt();
        if (is_encrypt != null) {
            statement.bindLong(30, (long) is_encrypt.intValue());
        } else {
            statement.bindNull(30);
        }
        Integer is_compressed = object.getIs_compressed();
        if (is_compressed != null) {
            statement.bindLong(31, (long) is_compressed.intValue());
        } else {
            statement.bindNull(31);
        }
        String encrypt_desc = object.getEncrypt_desc();
        if (encrypt_desc != null) {
            statement.bindString(32, encrypt_desc);
        } else {
            statement.bindNull(32);
        }
        String compression_desc = object.getCompression_desc();
        if (compression_desc != null) {
            statement.bindString(33, compression_desc);
        } else {
            statement.bindNull(33);
        }
        String reserved_attributes = object.getReserved_attributes();
        if (reserved_attributes != null) {
            statement.bindString(34, reserved_attributes);
        } else {
            statement.bindNull(34);
        }
        String cloud_update_policy = object.getCloud_update_policy();
        if (cloud_update_policy != null) {
            statement.bindString(35, cloud_update_policy);
        } else {
            statement.bindNull(35);
        }
        String allowed_user = object.getAllowed_user();
        if (allowed_user != null) {
            statement.bindString(36, allowed_user);
        } else {
            statement.bindNull(36);
        }
        String current_business = object.getCurrent_business();
        if (current_business != null) {
            statement.bindString(37, current_business);
        } else {
            statement.bindNull(37);
        }
        String usable_condition = object.getUsable_condition();
        if (usable_condition != null) {
            statement.bindString(38, usable_condition);
        } else {
            statement.bindNull(38);
        }
        Integer is_none = object.getIs_none();
        if (is_none != null) {
            statement.bindLong(39, (long) is_none.intValue());
        } else {
            statement.bindNull(39);
        }
        Integer none_type = object.getNone_type();
        if (none_type != null) {
            statement.bindLong(40, (long) none_type.intValue());
        } else {
            statement.bindNull(40);
        }
        Integer priority = object.getPriority();
        if (priority != null) {
            statement.bindLong(41, (long) priority.intValue());
        } else {
            statement.bindNull(41);
        }
        String check_code = object.getCheck_code();
        if (check_code != null) {
            statement.bindString(42, check_code);
        } else {
            statement.bindNull(42);
        }
        String reserved_1 = object.getReserved_1();
        if (reserved_1 != null) {
            statement.bindString(43, reserved_1);
        } else {
            statement.bindNull(43);
        }
        String reserved_2 = object.getReserved_2();
        if (reserved_2 != null) {
            statement.bindString(44, reserved_2);
        } else {
            statement.bindNull(44);
        }
        String resid = object.getResid();
        if (resid != null) {
            statement.bindString(45, resid);
        } else {
            statement.bindNull(45);
        }
        String sha256 = object.getSha256();
        if (sha256 != null) {
            statement.bindString(46, sha256);
        } else {
            statement.bindNull(46);
        }
        Integer verify_result = object.getVerify_result();
        if (verify_result != null) {
            statement.bindLong(47, (long) verify_result.intValue());
            return;
        }
        statement.bindNull(47);
    }

    public AiModel readObject(Cursor cursor, int offset) {
        return new AiModel(cursor);
    }

    public void setPrimaryKeyValue(AiModel object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, AiModel object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
