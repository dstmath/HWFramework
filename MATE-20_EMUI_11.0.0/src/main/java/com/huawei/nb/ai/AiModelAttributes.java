package com.huawei.nb.ai;

import com.huawei.nb.model.aimodel.AiModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AiModelAttributes {
    private static final int AIMODEL_ATTRIBUTED_COUNT_HASHMAP = 32;
    private static final Object INSTANCE_LOCK = new Object();
    private static volatile List<String> mEncryptedAttributes;

    private AiModelAttributes() {
    }

    public static List<String> getEncryptedAttributes() {
        List<String> list;
        synchronized (INSTANCE_LOCK) {
            if (mEncryptedAttributes == null) {
                mEncryptedAttributes = new ArrayList(1);
                mEncryptedAttributes.add("file_path");
            }
            list = mEncryptedAttributes;
        }
        return list;
    }

    public static Map<String, Supplier<Object>> getAttributes(AiModel aiModel) {
        HashMap hashMap = new HashMap(32);
        aiModel.getClass();
        hashMap.put("key", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$4GEexj4ZdcH72H04iKkpi3O0KyI */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getKey();
            }
        });
        aiModel.getClass();
        hashMap.put("origin_id", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$w4orDeVZJfg1lahnHFN5_9KkO6g */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getOrigin_id();
            }
        });
        aiModel.getClass();
        hashMap.put("serial_number", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$8qY_Ipk1zaGN8EPjRcQYHAYiCJc */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getSerial_number();
            }
        });
        aiModel.getClass();
        hashMap.put("name", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$ckaX3nT15_qQtX9uHjOZuGIXLDo */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getName();
            }
        });
        aiModel.getClass();
        hashMap.put("description", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$LgIMGizqcTQWOASI1UTY3pJKz3Y */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getDescription();
            }
        });
        aiModel.getClass();
        hashMap.put("file_path", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$O62_sNoyLMspcnKWjxy86YWeRak */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getFile_path();
            }
        });
        aiModel.getClass();
        hashMap.put("model_type", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$zkfC_QRHpod8fnanPTkrW3unw */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getModel_type();
            }
        });
        aiModel.getClass();
        hashMap.put("parent_id", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$QxSpI17juIrycPIu6utj70JQn8w */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getParent_id();
            }
        });
        aiModel.getClass();
        hashMap.put("top_model_id", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$O5uooY_JZuJX1KfNtrQ8WY92uQs */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getTop_model_id();
            }
        });
        aiModel.getClass();
        hashMap.put("is_preset_model", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$xPOVjfEu6pQvsRvO29XJHfVXsj0 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getIs_preset_model();
            }
        });
        aiModel.getClass();
        hashMap.put("platform", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$O5SH2IFDFVR5UHrlBHmR15zB5I */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getPlatform();
            }
        });
        aiModel.getClass();
        hashMap.put("tech_domain", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$ln46hg78LWnqKtYUoqTng7veQQ */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getTech_domain();
            }
        });
        aiModel.getClass();
        hashMap.put("busi_domain", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$MeazqxVvrp4vfy79MHYu6jW5aM */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getBusi_domain();
            }
        });
        aiModel.getClass();
        hashMap.put("region", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$SSGJCvuzF98I7lR_FpxAxUEdaQ */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getRegion();
            }
        });
        aiModel.getClass();
        hashMap.put("chip_type", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$WlUWE7ugB6wam5itxKCEVVZONjA */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getChip_type();
            }
        });
        aiModel.getClass();
        hashMap.put("version", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$beLlcwewwZgTMyhcEKjHMylQtow */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getVersion();
            }
        });
        aiModel.getClass();
        hashMap.put("format", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$VWVpAyD04K9H8_38cObntcFx_M */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getFormat();
            }
        });
        aiModel.getClass();
        hashMap.put("storage_type", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$1EXT9rNQPDPgxmXLnXijdWIDX2c */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getStorage_type();
            }
        });
        aiModel.getClass();
        hashMap.put("size", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$7Pqolk2sby0_xuyHVwNnBAnlkE8 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getSize();
            }
        });
        aiModel.getClass();
        hashMap.put("suffix", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$wbUU0uHCI7OSHV2GrUiXn7yG0 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getSuffix();
            }
        });
        aiModel.getClass();
        hashMap.put("create_type", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$zeGiouEm5HTOSk2n5wYb3x5d5YU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCreate_type();
            }
        });
        aiModel.getClass();
        hashMap.put("create_user", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$nWEBjGjG1xadnr7p7yJCcy0da8c */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCreate_user();
            }
        });
        aiModel.getClass();
        hashMap.put("create_time", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$MJMzzR11KmT4fw6JR5NMihnvDBI */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCreate_time();
            }
        });
        aiModel.getClass();
        hashMap.put("expired_time", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$E3ABrUH5PEdi8N28Vs1BtnIEgtM */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getExpired_time();
            }
        });
        aiModel.getClass();
        hashMap.put("last_update_time", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$hzy4h9Z2wZbRHOS4_YMglyw_Jo */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getLast_update_time();
            }
        });
        aiModel.getClass();
        hashMap.put("last_update_type", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$uck4SJTu1uUEEw0O9s5RPfXNd9M */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getLast_update_type();
            }
        });
        aiModel.getClass();
        hashMap.put("cloud_update_time", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$f7QDXxkvLyd_ouSRyy4YYpF6b6k */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCloud_update_time();
            }
        });
        aiModel.getClass();
        hashMap.put("is_need_authority", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$i7uP6oG4WCgi24UkkuS4D2E1KOQ */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getIs_need_authority();
            }
        });
        aiModel.getClass();
        hashMap.put("is_encrypt", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$s4SZAN7OQdu8v5_9QDkI38x420 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getIs_encrypt();
            }
        });
        aiModel.getClass();
        hashMap.put("is_compressed", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$n5iywGGQJYneHqMzH3mMLnfk */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getIs_compressed();
            }
        });
        aiModel.getClass();
        hashMap.put("encrypt_desc", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$F0YvqEloqH4rOFnbdBIsOtL7RU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getEncrypt_desc();
            }
        });
        aiModel.getClass();
        hashMap.put("compression_desc", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$eb3bynKrYDpLCS_t4e8sL0xTT2E */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCompression_desc();
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_attributes", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$TTXO7WKgJtMQyBL37iqdFhYcqDU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getReserved_attributes();
            }
        });
        aiModel.getClass();
        hashMap.put("cloud_update_policy", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$hELrKya14lNcmh2H55O2imqJ5i4 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCloud_update_policy();
            }
        });
        aiModel.getClass();
        hashMap.put("allowed_user", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$SXbSv53_wnieIJaaWqxrheJlKnU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getAllowed_user();
            }
        });
        aiModel.getClass();
        hashMap.put("current_business", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$T3Yci00yVIxFY1y903IGJpFTKXA */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCurrent_business();
            }
        });
        aiModel.getClass();
        hashMap.put("usable_condition", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$782yEwdT8NhAilIL4s2hJKZYOOY */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getUsable_condition();
            }
        });
        aiModel.getClass();
        hashMap.put("is_none", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$T87DQvi8zJDjpbxCB7CrvUeeOLU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getIs_none();
            }
        });
        aiModel.getClass();
        hashMap.put("none_type", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$CJK1tTpbpA3it7e_CpffiIDt7J8 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getNone_type();
            }
        });
        aiModel.getClass();
        hashMap.put("priority", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$MBaXlA_NB8mFMoIIFVm39LZpkio */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getPriority();
            }
        });
        aiModel.getClass();
        hashMap.put("check_code", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$aU1o42PZp5A8WLtDyMgPfTwdEE */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCheck_code();
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_1", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$lzGRXXuI16_ShGd_FL5sduJpPG8 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getReserved_1();
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_2", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$bJDAyeUdxTYHI4E2s_4da90o2T8 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getReserved_2();
            }
        });
        aiModel.getClass();
        hashMap.put("resid", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$zk39hcyHxn7ds1bgCzmIY2Od4U */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getResid();
            }
        });
        aiModel.getClass();
        hashMap.put("sha256", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$hW0_13Xgv9uC4hw9gr81mctjaE */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getSha256();
            }
        });
        aiModel.getClass();
        hashMap.put("verify_result", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$Q1btpU2mcgjnaZFFmYV6w2w98BA */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getVerify_result();
            }
        });
        return hashMap;
    }

    public static Map<String, Supplier<Object>> getPrimaryKeyAttributes(AiModel aiModel) {
        HashMap hashMap = new HashMap(1);
        aiModel.getClass();
        hashMap.put("id", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$3fqFNMrBlnVPfbNygUdORBdLqFU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getId();
            }
        });
        return hashMap;
    }

    public static Map<String, Supplier<Object>> getJsonAttributes(AiModel aiModel) {
        HashMap hashMap = new HashMap(32);
        aiModel.getClass();
        hashMap.put("encrypt_desc", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$F0YvqEloqH4rOFnbdBIsOtL7RU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getEncrypt_desc();
            }
        });
        aiModel.getClass();
        hashMap.put("compression_desc", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$eb3bynKrYDpLCS_t4e8sL0xTT2E */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCompression_desc();
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_attributes", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$TTXO7WKgJtMQyBL37iqdFhYcqDU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getReserved_attributes();
            }
        });
        aiModel.getClass();
        hashMap.put("cloud_update_policy", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$hELrKya14lNcmh2H55O2imqJ5i4 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCloud_update_policy();
            }
        });
        aiModel.getClass();
        hashMap.put("allowed_user", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$SXbSv53_wnieIJaaWqxrheJlKnU */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getAllowed_user();
            }
        });
        aiModel.getClass();
        hashMap.put("current_business", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$T3Yci00yVIxFY1y903IGJpFTKXA */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getCurrent_business();
            }
        });
        aiModel.getClass();
        hashMap.put("usable_condition", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$782yEwdT8NhAilIL4s2hJKZYOOY */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getUsable_condition();
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_1", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$lzGRXXuI16_ShGd_FL5sduJpPG8 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getReserved_1();
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_2", new Supplier() {
            /* class com.huawei.nb.ai.$$Lambda$bJDAyeUdxTYHI4E2s_4da90o2T8 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return AiModel.this.getReserved_2();
            }
        });
        return hashMap;
    }

    public static Map<String, Consumer<String>> setAttributes(AiModel aiModel) {
        HashMap hashMap = new HashMap(32);
        aiModel.getClass();
        hashMap.put("key", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$5lry82AwOE91GHJgHxk_ErJX1Cw */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setKey((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("serial_number", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$lhAU8Lzl7PsBVq0oxmNofMDwEU */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setSerial_number((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("name", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$z5opzuP7sQUtTy74BSoOeIJpJKw */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setName((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("description", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$Iru2Dj_gUoGBx62LbY5tN65dmg */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setDescription((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("file_path", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$zdTInKuxv6WBcDMR_vIFxi7Dl4c */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setFile_path((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("platform", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$QMNSgRFFR290GqpJJ2uv4C47Aqo */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setPlatform((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("tech_domain", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$M9yDeiMK1U3Nwhcelo4W6Y0YY4 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setTech_domain((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("busi_domain", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$_O_30OV2pPZFEuHKZBJcSnfWJvs */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setBusi_domain((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("region", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$lAMaPUNixmyzljgKqJoyOuh5Sbg */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setRegion((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("chip_type", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$q_rDImYlNGEKu_8wZJUtiwkuJ0 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setChip_type((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("format", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$QmmnjCLH4FyJ_FPfrPymWFCoaA */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setFormat((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("storage_type", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$cS5wVeuG5zU1FrVY_glajLURg5g */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setStorage_type((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("suffix", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$i7IImeWJvG0ae7Ds1RyRXaKb5c */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setSuffix((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("create_type", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$pOkgdEMQVEeVtGp2OARt4gMM7Ww */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setCreate_type((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("create_user", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$QFzp__xRf9YEh7nuvAvj1U8qZiw */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setCreate_user((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("last_update_type", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$4BzGUU8txUn7_SaoPkJj5_I5F6Y */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setLast_update_type((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("encrypt_desc", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$Xrrkr2HEQR012_mqoNqJtkuSxm4 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setEncrypt_desc((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("compression_desc", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$1J1KGiT5CZydXom3NutZBqOuiAI */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setCompression_desc((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_attributes", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$u9iyZdFDvIyAs4U0KfbknMxoVpU */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setReserved_attributes((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("cloud_update_policy", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$wsbD98ymfRyz7oA6GZN12WTKPO4 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setCloud_update_policy((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("allowed_user", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$Vx0LYNpk26zNiiMTpUEsUI0c0OQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setAllowed_user((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("current_business", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$qcGBPSTY_VnGx81u9H6qUmgIb_s */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setCurrent_business((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("usable_condition", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$Zp4Raxicsaut2jtczi97X8X59jI */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setUsable_condition((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("check_code", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$LYhWsbzjQwcjlwxnuZKdmMGYjAI */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setCheck_code((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_1", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$SS4q0KpIhG2lqE4LFKImWXHuvI */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setReserved_1((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("reserved_2", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$FpiOQW7ZW68BOSqfI1JRFZNgg */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setReserved_2((String) obj);
            }
        });
        aiModel.getClass();
        hashMap.put("resid", new Consumer() {
            /* class com.huawei.nb.ai.$$Lambda$eW9eSZT3ce40OkuBywLwMQNQis */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AiModel.this.setResid((String) obj);
            }
        });
        return hashMap;
    }

    private static void setExtendedAttributes(AiModel aiModel, AiModel aiModel2) {
        aiModel.setReserved_1(aiModel2.getReserved_1());
        aiModel.setReserved_2(aiModel2.getReserved_2());
        aiModel.setResid(aiModel2.getResid());
        aiModel.setSha256(aiModel2.getSha256());
        aiModel.setVerify_result(aiModel2.getVerify_result());
    }

    public static AiModel copyAiModel(AiModel aiModel) {
        AiModel aiModel2 = new AiModel();
        aiModel2.setId(aiModel.getId());
        aiModel2.setKey(aiModel.getKey());
        aiModel2.setOrigin_id(aiModel.getOrigin_id());
        aiModel2.setSerial_number(aiModel.getSerial_number());
        aiModel2.setName(aiModel.getName());
        aiModel2.setDescription(aiModel.getDescription());
        aiModel2.setFile_path(aiModel.getFile_path());
        aiModel2.setModel_type(aiModel.getModel_type());
        aiModel2.setParent_id(aiModel.getParent_id());
        aiModel2.setTop_model_id(aiModel.getTop_model_id());
        aiModel2.setIs_preset_model(aiModel.getIs_preset_model());
        aiModel2.setPlatform(aiModel.getPlatform());
        aiModel2.setTech_domain(aiModel.getTech_domain());
        aiModel2.setBusi_domain(aiModel.getBusi_domain());
        aiModel2.setRegion(aiModel.getRegion());
        aiModel2.setChip_type(aiModel.getChip_type());
        aiModel2.setVersion(aiModel.getVersion());
        aiModel2.setFormat(aiModel.getFormat());
        aiModel2.setStorage_type(aiModel.getStorage_type());
        aiModel2.setSize(aiModel.getSize());
        aiModel2.setSuffix(aiModel.getSuffix());
        aiModel2.setCreate_type(aiModel.getCreate_type());
        aiModel2.setCreate_user(aiModel.getCreate_user());
        aiModel2.setCreate_time(aiModel.getCreate_time());
        aiModel2.setExpired_time(aiModel.getExpired_time());
        aiModel2.setLast_update_time(aiModel.getLast_update_time());
        aiModel2.setLast_update_type(aiModel.getLast_update_type());
        aiModel2.setCloud_update_time(aiModel.getCloud_update_time());
        aiModel2.setIs_need_authority(aiModel.getIs_need_authority());
        aiModel2.setIs_encrypt(aiModel.getIs_encrypt());
        aiModel2.setIs_compressed(aiModel.getIs_compressed());
        aiModel2.setEncrypt_desc(aiModel.getEncrypt_desc());
        aiModel2.setCompression_desc(aiModel.getCompression_desc());
        aiModel2.setReserved_attributes(aiModel.getReserved_attributes());
        aiModel2.setCloud_update_policy(aiModel.getCloud_update_policy());
        aiModel2.setAllowed_user(aiModel.getAllowed_user());
        aiModel2.setCurrent_business(aiModel.getCurrent_business());
        aiModel2.setUsable_condition(aiModel.getUsable_condition());
        aiModel2.setIs_none(aiModel.getIs_none());
        aiModel2.setNone_type(aiModel.getNone_type());
        aiModel2.setPriority(aiModel.getPriority());
        aiModel2.setCheck_code(aiModel.getCheck_code());
        setExtendedAttributes(aiModel2, aiModel);
        return aiModel2;
    }
}
