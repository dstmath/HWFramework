package com.huawei.nb.ai;

import com.huawei.nb.model.aimodel.AiModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AiModelAttributes {
    private static final Object INSTANCE_LOCK = new Object();
    private static volatile List<String> mEncryptedAttributes = null;

    private AiModelAttributes() {
    }

    public static List<String> getEncryptedAttributes() {
        List<String> list;
        synchronized (INSTANCE_LOCK) {
            if (mEncryptedAttributes == null) {
                mEncryptedAttributes = new ArrayList();
                mEncryptedAttributes.add("file_path");
            }
            list = mEncryptedAttributes;
        }
        return list;
    }

    public static Map<String, Supplier<Object>> getAttributes(AiModel model) {
        Map<String, Supplier<Object>> attributes = new HashMap<>();
        model.getClass();
        attributes.put("key", AiModelAttributes$$Lambda$0.get$Lambda(model));
        model.getClass();
        attributes.put("origin_id", AiModelAttributes$$Lambda$1.get$Lambda(model));
        model.getClass();
        attributes.put("serial_number", AiModelAttributes$$Lambda$2.get$Lambda(model));
        model.getClass();
        attributes.put("name", AiModelAttributes$$Lambda$3.get$Lambda(model));
        model.getClass();
        attributes.put("description", AiModelAttributes$$Lambda$4.get$Lambda(model));
        model.getClass();
        attributes.put("file_path", AiModelAttributes$$Lambda$5.get$Lambda(model));
        model.getClass();
        attributes.put("model_type", AiModelAttributes$$Lambda$6.get$Lambda(model));
        model.getClass();
        attributes.put("parent_id", AiModelAttributes$$Lambda$7.get$Lambda(model));
        model.getClass();
        attributes.put("top_model_id", AiModelAttributes$$Lambda$8.get$Lambda(model));
        model.getClass();
        attributes.put("is_preset_model", AiModelAttributes$$Lambda$9.get$Lambda(model));
        model.getClass();
        attributes.put("platform", AiModelAttributes$$Lambda$10.get$Lambda(model));
        model.getClass();
        attributes.put("tech_domain", AiModelAttributes$$Lambda$11.get$Lambda(model));
        model.getClass();
        attributes.put("busi_domain", AiModelAttributes$$Lambda$12.get$Lambda(model));
        model.getClass();
        attributes.put("region", AiModelAttributes$$Lambda$13.get$Lambda(model));
        model.getClass();
        attributes.put("chip_type", AiModelAttributes$$Lambda$14.get$Lambda(model));
        model.getClass();
        attributes.put("version", AiModelAttributes$$Lambda$15.get$Lambda(model));
        model.getClass();
        attributes.put("format", AiModelAttributes$$Lambda$16.get$Lambda(model));
        model.getClass();
        attributes.put("storage_type", AiModelAttributes$$Lambda$17.get$Lambda(model));
        model.getClass();
        attributes.put("size", AiModelAttributes$$Lambda$18.get$Lambda(model));
        model.getClass();
        attributes.put("suffix", AiModelAttributes$$Lambda$19.get$Lambda(model));
        model.getClass();
        attributes.put("create_type", AiModelAttributes$$Lambda$20.get$Lambda(model));
        model.getClass();
        attributes.put("create_user", AiModelAttributes$$Lambda$21.get$Lambda(model));
        model.getClass();
        attributes.put("create_time", AiModelAttributes$$Lambda$22.get$Lambda(model));
        model.getClass();
        attributes.put("expired_time", AiModelAttributes$$Lambda$23.get$Lambda(model));
        model.getClass();
        attributes.put("last_update_time", AiModelAttributes$$Lambda$24.get$Lambda(model));
        model.getClass();
        attributes.put("last_update_type", AiModelAttributes$$Lambda$25.get$Lambda(model));
        model.getClass();
        attributes.put("cloud_update_time", AiModelAttributes$$Lambda$26.get$Lambda(model));
        model.getClass();
        attributes.put("is_need_authority", AiModelAttributes$$Lambda$27.get$Lambda(model));
        model.getClass();
        attributes.put("is_encrypt", AiModelAttributes$$Lambda$28.get$Lambda(model));
        model.getClass();
        attributes.put("is_compressed", AiModelAttributes$$Lambda$29.get$Lambda(model));
        model.getClass();
        attributes.put("encrypt_desc", AiModelAttributes$$Lambda$30.get$Lambda(model));
        model.getClass();
        attributes.put("compression_desc", AiModelAttributes$$Lambda$31.get$Lambda(model));
        model.getClass();
        attributes.put("reserved_attributes", AiModelAttributes$$Lambda$32.get$Lambda(model));
        model.getClass();
        attributes.put("cloud_update_policy", AiModelAttributes$$Lambda$33.get$Lambda(model));
        model.getClass();
        attributes.put("allowed_user", AiModelAttributes$$Lambda$34.get$Lambda(model));
        model.getClass();
        attributes.put("current_business", AiModelAttributes$$Lambda$35.get$Lambda(model));
        model.getClass();
        attributes.put("usable_condition", AiModelAttributes$$Lambda$36.get$Lambda(model));
        model.getClass();
        attributes.put("is_none", AiModelAttributes$$Lambda$37.get$Lambda(model));
        model.getClass();
        attributes.put("none_type", AiModelAttributes$$Lambda$38.get$Lambda(model));
        model.getClass();
        attributes.put("priority", AiModelAttributes$$Lambda$39.get$Lambda(model));
        model.getClass();
        attributes.put("check_code", AiModelAttributes$$Lambda$40.get$Lambda(model));
        model.getClass();
        attributes.put("reserved_1", AiModelAttributes$$Lambda$41.get$Lambda(model));
        model.getClass();
        attributes.put("reserved_2", AiModelAttributes$$Lambda$42.get$Lambda(model));
        model.getClass();
        attributes.put("resid", AiModelAttributes$$Lambda$43.get$Lambda(model));
        model.getClass();
        attributes.put("sha256", AiModelAttributes$$Lambda$44.get$Lambda(model));
        model.getClass();
        attributes.put("verify_result", AiModelAttributes$$Lambda$45.get$Lambda(model));
        return attributes;
    }

    public static Map<String, Supplier<Object>> getPrimaryKeyAttributes(AiModel model) {
        Map<String, Supplier<Object>> keyAttrs = new HashMap<>();
        model.getClass();
        keyAttrs.put("id", AiModelAttributes$$Lambda$46.get$Lambda(model));
        return keyAttrs;
    }

    public static Map<String, Supplier<Object>> getJsonAttributes(AiModel model) {
        Map<String, Supplier<Object>> jsonAttrs = new HashMap<>();
        model.getClass();
        jsonAttrs.put("encrypt_desc", AiModelAttributes$$Lambda$47.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("compression_desc", AiModelAttributes$$Lambda$48.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("reserved_attributes", AiModelAttributes$$Lambda$49.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("cloud_update_policy", AiModelAttributes$$Lambda$50.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("allowed_user", AiModelAttributes$$Lambda$51.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("current_business", AiModelAttributes$$Lambda$52.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("usable_condition", AiModelAttributes$$Lambda$53.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("reserved_1", AiModelAttributes$$Lambda$54.get$Lambda(model));
        model.getClass();
        jsonAttrs.put("reserved_2", AiModelAttributes$$Lambda$55.get$Lambda(model));
        return jsonAttrs;
    }

    public static Map<String, Consumer<String>> setAttributes(AiModel model) {
        Map<String, Consumer<String>> attributes = new HashMap<>();
        model.getClass();
        attributes.put("key", AiModelAttributes$$Lambda$56.get$Lambda(model));
        model.getClass();
        attributes.put("serial_number", AiModelAttributes$$Lambda$57.get$Lambda(model));
        model.getClass();
        attributes.put("name", AiModelAttributes$$Lambda$58.get$Lambda(model));
        model.getClass();
        attributes.put("description", AiModelAttributes$$Lambda$59.get$Lambda(model));
        model.getClass();
        attributes.put("file_path", AiModelAttributes$$Lambda$60.get$Lambda(model));
        model.getClass();
        attributes.put("platform", AiModelAttributes$$Lambda$61.get$Lambda(model));
        model.getClass();
        attributes.put("tech_domain", AiModelAttributes$$Lambda$62.get$Lambda(model));
        model.getClass();
        attributes.put("busi_domain", AiModelAttributes$$Lambda$63.get$Lambda(model));
        model.getClass();
        attributes.put("region", AiModelAttributes$$Lambda$64.get$Lambda(model));
        model.getClass();
        attributes.put("chip_type", AiModelAttributes$$Lambda$65.get$Lambda(model));
        model.getClass();
        attributes.put("format", AiModelAttributes$$Lambda$66.get$Lambda(model));
        model.getClass();
        attributes.put("storage_type", AiModelAttributes$$Lambda$67.get$Lambda(model));
        model.getClass();
        attributes.put("suffix", AiModelAttributes$$Lambda$68.get$Lambda(model));
        model.getClass();
        attributes.put("create_type", AiModelAttributes$$Lambda$69.get$Lambda(model));
        model.getClass();
        attributes.put("create_user", AiModelAttributes$$Lambda$70.get$Lambda(model));
        model.getClass();
        attributes.put("last_update_type", AiModelAttributes$$Lambda$71.get$Lambda(model));
        model.getClass();
        attributes.put("encrypt_desc", AiModelAttributes$$Lambda$72.get$Lambda(model));
        model.getClass();
        attributes.put("compression_desc", AiModelAttributes$$Lambda$73.get$Lambda(model));
        model.getClass();
        attributes.put("reserved_attributes", AiModelAttributes$$Lambda$74.get$Lambda(model));
        model.getClass();
        attributes.put("cloud_update_policy", AiModelAttributes$$Lambda$75.get$Lambda(model));
        model.getClass();
        attributes.put("allowed_user", AiModelAttributes$$Lambda$76.get$Lambda(model));
        model.getClass();
        attributes.put("current_business", AiModelAttributes$$Lambda$77.get$Lambda(model));
        model.getClass();
        attributes.put("usable_condition", AiModelAttributes$$Lambda$78.get$Lambda(model));
        model.getClass();
        attributes.put("check_code", AiModelAttributes$$Lambda$79.get$Lambda(model));
        model.getClass();
        attributes.put("reserved_1", AiModelAttributes$$Lambda$80.get$Lambda(model));
        model.getClass();
        attributes.put("reserved_2", AiModelAttributes$$Lambda$81.get$Lambda(model));
        model.getClass();
        attributes.put("resid", AiModelAttributes$$Lambda$82.get$Lambda(model));
        return attributes;
    }

    public static AiModel copyAiModel(AiModel model) {
        AiModel newModel = new AiModel();
        newModel.setId(model.getId());
        newModel.setKey(model.getKey());
        newModel.setOrigin_id(model.getOrigin_id());
        newModel.setSerial_number(model.getSerial_number());
        newModel.setName(model.getName());
        newModel.setDescription(model.getDescription());
        newModel.setFile_path(model.getFile_path());
        newModel.setModel_type(model.getModel_type());
        newModel.setParent_id(model.getParent_id());
        newModel.setTop_model_id(model.getTop_model_id());
        newModel.setIs_preset_model(model.getIs_preset_model());
        newModel.setPlatform(model.getPlatform());
        newModel.setTech_domain(model.getTech_domain());
        newModel.setBusi_domain(model.getBusi_domain());
        newModel.setRegion(model.getRegion());
        newModel.setChip_type(model.getChip_type());
        newModel.setVersion(model.getVersion());
        newModel.setFormat(model.getFormat());
        newModel.setStorage_type(model.getStorage_type());
        newModel.setSize(model.getSize());
        newModel.setSuffix(model.getSuffix());
        newModel.setCreate_type(model.getCreate_type());
        newModel.setCreate_user(model.getCreate_user());
        newModel.setCreate_time(model.getCreate_time());
        newModel.setExpired_time(model.getExpired_time());
        newModel.setLast_update_time(model.getLast_update_time());
        newModel.setLast_update_type(model.getLast_update_type());
        newModel.setCloud_update_time(model.getCloud_update_time());
        newModel.setIs_need_authority(model.getIs_need_authority());
        newModel.setIs_encrypt(model.getIs_encrypt());
        newModel.setIs_compressed(model.getIs_compressed());
        newModel.setEncrypt_desc(model.getEncrypt_desc());
        newModel.setCompression_desc(model.getCompression_desc());
        newModel.setReserved_attributes(model.getReserved_attributes());
        newModel.setCloud_update_policy(model.getCloud_update_policy());
        newModel.setAllowed_user(model.getAllowed_user());
        newModel.setCurrent_business(model.getCurrent_business());
        newModel.setUsable_condition(model.getUsable_condition());
        newModel.setIs_none(model.getIs_none());
        newModel.setNone_type(model.getNone_type());
        newModel.setPriority(model.getPriority());
        newModel.setCheck_code(model.getCheck_code());
        newModel.setReserved_1(model.getReserved_1());
        newModel.setReserved_2(model.getReserved_2());
        newModel.setResid(model.getResid());
        newModel.setSha256(model.getSha256());
        newModel.setVerify_result(model.getVerify_result());
        return newModel;
    }
}
