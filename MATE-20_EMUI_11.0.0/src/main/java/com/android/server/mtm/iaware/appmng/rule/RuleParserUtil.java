package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Intent;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.BroadcastExFeature;
import java.util.LinkedHashMap;
import java.util.Locale;

public class RuleParserUtil {
    private static final RuleNode.XmlValue DEFAULT_INT = new RuleNode.XmlValue(-1);
    private static final RuleNode.XmlValue DEFAULT_STRING = new RuleNode.XmlValue(MemoryConstant.MEM_SCENE_DEFAULT);

    public interface TagEnum {
        boolean equals(Object obj);

        String getDesc();

        int hashCode();

        boolean isStringInXml();
    }

    public enum AppMngTag implements TagEnum {
        TRISTATE("tristate", "tri") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, tristate);
            }
        },
        LEVEL(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, "lvl") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, level);
            }
        },
        TYPE("type", "type") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecType(packageName));
            }
        },
        STATUS("status", "status") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecStat(processInfo, childItems.getIntegerMap()));
            }
        },
        RECENT("recent", "rec") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecRecent(packageName, childItems.getIntegerMap()));
            }
        },
        TYPE_TOPN("type_topn", "typen") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecTypeFreqTopN(packageName, childItems.getIntegerMap()));
            }
        },
        HABBIT_TOPN("habbit_topn", "habbitn") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecHabbitTopN(packageName, childItems.getIntegerMap()));
            }
        },
        DEVICELEVEL("devicelevel", "devicelevel") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngDeviceLevel());
            }
        },
        MEM_SIZE("mem_size", "memsize") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getMemorySize());
            }
        },
        POLICY(MemoryConstant.MEM_SYSTRIM_POLICY, MemoryConstant.MEM_SYSTRIM_POLICY) {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                LinkedHashMap<Integer, RuleNode> policyArray;
                if (childItems == null || processInfo == null || packageName == null || (policyArray = childItems.getIntegerMap()) == null || policyArray.size() != 1) {
                    return null;
                }
                return policyArray.entrySet().iterator().next().getValue();
            }
        },
        OVERSEA("oversea", "oversea") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AppMngConfig.getRegionCode());
            }
        },
        APPUNUSED("appunused", "appunused") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                LinkedHashMap<Integer, RuleNode> integerMap;
                if (childItems == null || processInfo == null || packageName == null || (integerMap = childItems.getIntegerMap()) == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppUnusedRecent(packageName, integerMap));
            }
        };
        
        private String mDesc;
        private String mUploadBdTag;

        public abstract RuleNode getAppliedValue(RuleNode.StringIntegerMap stringIntegerMap, AwareProcessInfo awareProcessInfo, int i, String str, int i2);

        private AppMngTag(String desc, String uploadBdTag) {
            this.mDesc = desc;
            this.mUploadBdTag = uploadBdTag;
        }

        @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
        public String getDesc() {
            return this.mDesc;
        }

        public String getUploadBdTag() {
            return this.mUploadBdTag;
        }

        @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
        public boolean isStringInXml() {
            return false;
        }

        public static TagEnum fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /* access modifiers changed from: private */
        public static RuleNode getNodeAt(RuleNode.StringIntegerMap childItems, int key) {
            RuleNode node = childItems.get(new RuleNode.XmlValue(key));
            if (node == null) {
                return childItems.get(RuleParserUtil.DEFAULT_INT);
            }
            return node;
        }
    }

    public enum AppStartTag implements TagEnum {
        TRISTATE("tristate") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, tristate);
            }
        },
        CALLER_ACTION("caller_action") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecCallerAction(packageName, status, childItems.getSortedList(), source));
            }
        },
        CALLER_STATUS("caller_status") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecCallerStatus(packageName, status, childItems.getSortedList()));
            }
        },
        TARGET_STATUS("target_status") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecTargetStatus(packageName, status, childItems.getSortedList()));
            }
        },
        TARGET_TYPE("target_type") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecTargetType(packageName, status, childItems.getSortedList()));
            }
        },
        OVERSEA("oversea") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecVerOversea(packageName, status));
            }
        },
        APPSRCRANGE("appsrcrange") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecAppSrcRange(packageName, status));
            }
        },
        APPOVERSEA("appoversea") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecAppOversea(packageName, status));
            }
        },
        SCREENSTATUS("screenstatus") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecScreenStatus(packageName, status));
            }
        },
        APPRECENT("apprecent") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecRecent(packageName, childItems.getIntegerMap()));
            }
        },
        REGION("region") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecRegion(packageName, status));
            }
        },
        DEVICELEVEL("devicelevel") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngDeviceLevel());
            }
        },
        MEM_SIZE("mem_size") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getMemorySize());
            }
        },
        BROADCAST("broadcast") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null || status.cacheAction == null) {
                    return null;
                }
                RuleNode node = childItems.get(new RuleNode.XmlValue(status.cacheAction));
                if (node == null) {
                    return childItems.get(RuleParserUtil.DEFAULT_STRING);
                }
                return node;
            }

            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag, com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
            public boolean isStringInXml() {
                return true;
            }
        },
        POLICY(MemoryConstant.MEM_SYSTRIM_POLICY) {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppStartTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                LinkedHashMap<Integer, RuleNode> policyArray;
                if (childItems == null || source == null || status == null || (policyArray = childItems.getIntegerMap()) == null || policyArray.size() != 1) {
                    return null;
                }
                return policyArray.entrySet().iterator().next().getValue();
            }
        };
        
        private String mDesc;

        public abstract RuleNode getAppliedValue(RuleNode.StringIntegerMap stringIntegerMap, String str, AppMngConstant.AppStartSource appStartSource, AwareAppStartStatusCache awareAppStartStatusCache, int i);

        private AppStartTag(String desc) {
            this.mDesc = desc;
        }

        @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
        public String getDesc() {
            return this.mDesc;
        }

        @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
        public boolean isStringInXml() {
            return false;
        }

        public static TagEnum fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /* access modifiers changed from: private */
        public static RuleNode getNodeAt(RuleNode.StringIntegerMap childItems, int key) {
            RuleNode node = childItems.get(new RuleNode.XmlValue(key));
            if (node == null) {
                return childItems.get(RuleParserUtil.DEFAULT_INT);
            }
            return node;
        }
    }

    public enum BroadcastTag implements TagEnum {
        TRISTATE("tristate") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                if (childItems == null || value == null || !value.isValid()) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, value.tristate);
            }
        },
        TYPE("type") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                int appType;
                if (childItems == null || value == null || !value.isValid()) {
                    return null;
                }
                if (BroadcastExFeature.isBrGoogleApp(value.id)) {
                    appType = 10000;
                } else {
                    appType = AwareIntelligentRecg.getInstance().getAppMngSpecType(value.id);
                }
                return BroadcastTag.getNodeAt(childItems, appType);
            }
        },
        PROCSTATUS("procstatus") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                if (childItems == null || value == null || !value.isValid()) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, value.processInfo.getState());
            }
        },
        BROADCAST("broadcast") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                if (childItems == null || value == null || !value.isValid()) {
                    return null;
                }
                return childItems.get(new RuleNode.XmlValue(value.intent.getAction()));
            }

            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag, com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
            public boolean isStringInXml() {
                return true;
            }
        },
        OVERSEA("oversea") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                if (childItems == null || value == null || !value.isValid()) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, AppMngConfig.getRegionCode());
            }
        },
        GOOGLESTATUS("googlestatus") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                if (childItems == null || value == null || !value.isValid()) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, AwareBroadcastPolicy.getGoogleConnStat());
            }
        },
        STATUS("status") {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                if (childItems == null || value == null || !value.isValid()) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecStat(value.processInfo, childItems.getIntegerMap()));
            }
        },
        POLICY(MemoryConstant.MEM_SYSTRIM_POLICY) {
            @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.BroadcastTag
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, BroadcastValue value) {
                LinkedHashMap<Integer, RuleNode> policyArray;
                if (childItems == null || value == null || !value.isValid() || (policyArray = childItems.getIntegerMap()) == null || policyArray.size() != 1) {
                    return null;
                }
                return policyArray.entrySet().iterator().next().getValue();
            }
        };
        
        private static final int BR_GOOGLE_APP = 10000;
        private String mDesc;

        public abstract RuleNode getAppliedValue(RuleNode.StringIntegerMap stringIntegerMap, BroadcastValue broadcastValue);

        private BroadcastTag(String desc) {
            this.mDesc = desc;
        }

        @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
        public String getDesc() {
            return this.mDesc;
        }

        @Override // com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum
        public boolean isStringInXml() {
            return false;
        }

        public static TagEnum fromString(String desc) {
            if (desc == null) {
                return null;
            }
            try {
                return valueOf(desc.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /* access modifiers changed from: private */
        public static RuleNode getNodeAt(RuleNode.StringIntegerMap childItems, int key) {
            RuleNode node = childItems.get(new RuleNode.XmlValue(key));
            if (node == null) {
                return childItems.get(RuleParserUtil.DEFAULT_INT);
            }
            return node;
        }
    }

    public static class BroadcastValue {
        public String id;
        public Intent intent;
        public AwareProcessInfo processInfo;
        public AppMngConstant.BroadcastSource source;
        public int tristate;

        /* access modifiers changed from: package-private */
        public boolean isValid() {
            return (this.processInfo == null || this.id == null || this.source == null || this.intent == null) ? false : true;
        }
    }
}
