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
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.com.android.server.policy.stylus.StylusGestureSettings;
import java.util.LinkedHashMap;
import java.util.Locale;

public class RuleParserUtil {
    /* access modifiers changed from: private */
    public static final RuleNode.XmlValue DEFAULT_INT = new RuleNode.XmlValue(-1);
    /* access modifiers changed from: private */
    public static final RuleNode.XmlValue DEFAULT_STRING = new RuleNode.XmlValue(MemoryConstant.MEM_SCENE_DEFAULT);

    public enum AppMngTag implements TagEnum {
        TRISTATE("tristate", "tri") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, tristate);
            }
        },
        LEVEL(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, "lvl") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, level);
            }
        },
        TYPE(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE, HwSecDiagnoseConstant.ANTIMAL_APK_TYPE) {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecType(packageName));
            }
        },
        STATUS("status", "status") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecStat(processInfo, childItems.getIntegerMap()));
            }
        },
        RECENT("recent", "rec") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecRecent(packageName, childItems.getIntegerMap()));
            }
        },
        TYPE_TOPN("type_topn", "typen") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecTypeFreqTopN(packageName, childItems.getIntegerMap()));
            }
        },
        HABBIT_TOPN("habbit_topn", "habbitn") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecHabbitTopN(packageName, childItems.getIntegerMap()));
            }
        },
        DEVICELEVEL("devicelevel", "devicelevel") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngDeviceLevel());
            }
        },
        MEM_SIZE("mem_size", "memsize") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getMemorySize());
            }
        },
        POLICY("policy", "policy") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                LinkedHashMap<Integer, RuleNode> policyArray = childItems.getIntegerMap();
                if (policyArray == null || policyArray.size() != 1) {
                    return null;
                }
                return (RuleNode) policyArray.entrySet().iterator().next().getValue();
            }
        },
        OVERSEA("oversea", "oversea") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AppMngConfig.getRegionCode());
            }
        };
        
        private String mDesc;
        private String mUploadBDTag;

        public abstract RuleNode getAppliedValue(RuleNode.StringIntegerMap stringIntegerMap, AwareProcessInfo awareProcessInfo, int i, String str, int i2);

        private AppMngTag(String desc, String uploadBDTag) {
            this.mDesc = desc;
            this.mUploadBDTag = uploadBDTag;
        }

        public String getDesc() {
            return this.mDesc;
        }

        public String getUploadBDTag() {
            return this.mUploadBDTag;
        }

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
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, tristate);
            }
        },
        CALLER_ACTION("caller_action") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecCallerAction(packageName, status, childItems.getSortedList(), source));
            }
        },
        CALLER_STATUS("caller_status") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecCallerStatus(packageName, status, childItems.getSortedList()));
            }
        },
        TARGET_STATUS("target_status") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecTargetStatus(packageName, status, childItems.getSortedList()));
            }
        },
        TARGET_TYPE("target_type") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecTargetType(packageName, status, childItems.getSortedList()));
            }
        },
        OVERSEA("oversea") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecVerOversea(packageName, status));
            }
        },
        APPSRCRANGE("appsrcrange") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecAppSrcRange(packageName, status));
            }
        },
        APPOVERSEA("appoversea") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecAppOversea(packageName, status));
            }
        },
        SCREENSTATUS("screenstatus") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecScreenStatus(packageName, status));
            }
        },
        APPRECENT("apprecent") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecRecent(packageName, childItems.getIntegerMap()));
            }
        },
        REGION(StylusGestureSettings.STYLUS_GESTURE_REGION_SUFFIX) {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecRegion(packageName, status));
            }
        },
        DEVICELEVEL("devicelevel") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngDeviceLevel());
            }
        },
        MEM_SIZE("mem_size") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getMemorySize());
            }
        },
        BROADCAST("broadcast") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null || status.mAction == null) {
                    return null;
                }
                RuleNode node = childItems.get(new RuleNode.XmlValue(status.mAction));
                if (node == null) {
                    node = childItems.get(RuleParserUtil.DEFAULT_STRING);
                }
                return node;
            }

            public boolean isStringInXml() {
                return true;
            }
        },
        POLICY("policy") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || source == null || status == null) {
                    return null;
                }
                LinkedHashMap<Integer, RuleNode> policyArray = childItems.getIntegerMap();
                if (policyArray == null || policyArray.size() != 1) {
                    return null;
                }
                return (RuleNode) policyArray.entrySet().iterator().next().getValue();
            }
        };
        
        private String mDesc;

        public abstract RuleNode getAppliedValue(RuleNode.StringIntegerMap stringIntegerMap, String str, AppMngConstant.AppStartSource appStartSource, AwareAppStartStatusCache awareAppStartStatusCache, int i);

        private AppStartTag(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }

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
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, tristate);
            }
        },
        TYPE(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE) {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                int appType;
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                if (BroadcastExFeature.isBrGoogleApp(id)) {
                    appType = 10000;
                } else {
                    appType = AwareIntelligentRecg.getInstance().getAppMngSpecType(id);
                }
                return BroadcastTag.getNodeAt(childItems, appType);
            }
        },
        PROCSTATUS("procstatus") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, processInfo.getState());
            }
        },
        BROADCAST("broadcast") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                return childItems.get(new RuleNode.XmlValue(intent.getAction()));
            }

            public boolean isStringInXml() {
                return true;
            }
        },
        OVERSEA("oversea") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, AppMngConfig.getRegionCode());
            }
        },
        GOOGLESTATUS("googlestatus") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, AwareBroadcastPolicy.getGoogleConnStat());
            }
        },
        STATUS("status") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                return BroadcastTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecStat(processInfo, childItems.getIntegerMap()));
            }
        },
        POLICY("policy") {
            public RuleNode getAppliedValue(RuleNode.StringIntegerMap childItems, AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, int tristate) {
                if (childItems == null || processInfo == null || id == null || source == null || intent == null) {
                    return null;
                }
                LinkedHashMap<Integer, RuleNode> policyArray = childItems.getIntegerMap();
                if (policyArray == null || policyArray.size() != 1) {
                    return null;
                }
                return (RuleNode) policyArray.entrySet().iterator().next().getValue();
            }
        };
        
        private String mDesc;

        public abstract RuleNode getAppliedValue(RuleNode.StringIntegerMap stringIntegerMap, AppMngConstant.BroadcastSource broadcastSource, String str, Intent intent, AwareProcessInfo awareProcessInfo, int i);

        private BroadcastTag(String desc) {
            this.mDesc = desc;
        }

        public String getDesc() {
            return this.mDesc;
        }

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

    public interface TagEnum {
        boolean equals(Object obj);

        String getDesc();

        int hashCode();

        boolean isStringInXml();
    }
}
