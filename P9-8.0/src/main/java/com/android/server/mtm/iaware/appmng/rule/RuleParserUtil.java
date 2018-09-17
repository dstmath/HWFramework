package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.rule.RuleNode.StringIntegerMap;
import com.android.server.mtm.iaware.appmng.rule.RuleNode.XmlValue;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;

public class RuleParserUtil {
    private static final XmlValue DEFAULT_INT = new XmlValue(-1);
    private static final XmlValue DEFAULT_STRING = new XmlValue(MemoryConstant.MEM_SCENE_DEFAULT);

    public interface TagEnum {
        boolean equals(Object obj);

        String getDesc();

        int hashCode();

        boolean isStringInXml();
    }

    public enum AppMngTag implements TagEnum {
        TRISTATE("tristate") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, tristate);
            }
        },
        LEVEL(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL) {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, level);
            }
        },
        TYPE(HwSecDiagnoseConstant.ANTIMAL_APK_TYPE) {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecType(packageName));
            }
        },
        STATUS("status") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecStat(processInfo, childItems.getIntegerMap()));
            }
        },
        RECENT("recent") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecRecent(packageName, childItems.getIntegerMap()));
            }
        },
        TYPE_TOPN("type_topn") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecTypeFreqTopN(packageName, childItems.getIntegerMap()));
            }
        },
        HABBIT_TOPN("habbit_topn") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppMngSpecHabbitTopN(packageName, childItems.getIntegerMap()));
            }
        },
        POLICY("policy") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                LinkedHashMap<Integer, RuleNode> policyArray = childItems.getIntegerMap();
                if (policyArray == null || policyArray.size() != 1) {
                    return null;
                }
                return (RuleNode) ((Entry) policyArray.entrySet().iterator().next()).getValue();
            }
        },
        OVERSEA("oversea") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, AwareProcessInfo processInfo, int level, String packageName, int tristate) {
                if (childItems == null || processInfo == null || packageName == null) {
                    return null;
                }
                return AppMngTag.getNodeAt(childItems, AppMngConfig.getRegionCode());
            }
        };
        
        private String mDesc;

        public abstract RuleNode getAppliedValue(StringIntegerMap stringIntegerMap, AwareProcessInfo awareProcessInfo, int i, String str, int i2);

        private AppMngTag(String desc) {
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

        private static RuleNode getNodeAt(StringIntegerMap childItems, int key) {
            RuleNode node = childItems.get(new XmlValue(key));
            if (node == null) {
                return childItems.get(RuleParserUtil.DEFAULT_INT);
            }
            return node;
        }
    }

    public enum AppStartTag implements TagEnum {
        TRISTATE("tristate") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, tristate);
            }
        },
        CALLER_ACTION("caller_action") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecCallerAction(packageName, status, childItems.getSortedList(), source));
            }
        },
        CALLER_STATUS("caller_status") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecCallerStatus(packageName, status, childItems.getSortedList()));
            }
        },
        TARGET_STATUS("target_status") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecTargetStatus(packageName, status, childItems.getSortedList()));
            }
        },
        TARGET_TYPE("target_type") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null) {
                    return null;
                }
                return AppStartTag.getNodeAt(childItems, AwareIntelligentRecg.getInstance().getAppStartSpecTargetType(packageName, status, childItems.getSortedList()));
            }
        },
        BROADCAST("broadcast") {
            public RuleNode getAppliedValue(StringIntegerMap childItems, String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || packageName == null || source == null || status == null || status.mAction == null) {
                    return null;
                }
                RuleNode node = childItems.get(new XmlValue(status.mAction));
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
            public RuleNode getAppliedValue(StringIntegerMap childItems, String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
                if (childItems == null || source == null || status == null) {
                    return null;
                }
                LinkedHashMap<Integer, RuleNode> policyArray = childItems.getIntegerMap();
                if (policyArray == null || policyArray.size() != 1) {
                    return null;
                }
                return (RuleNode) ((Entry) policyArray.entrySet().iterator().next()).getValue();
            }
        };
        
        private String mDesc;

        public abstract RuleNode getAppliedValue(StringIntegerMap stringIntegerMap, String str, AppStartSource appStartSource, AwareAppStartStatusCache awareAppStartStatusCache, int i);

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

        private static RuleNode getNodeAt(StringIntegerMap childItems, int key) {
            RuleNode node = childItems.get(new XmlValue(key));
            if (node == null) {
                return childItems.get(RuleParserUtil.DEFAULT_INT);
            }
            return node;
        }
    }
}
