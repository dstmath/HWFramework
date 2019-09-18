package com.android.server.rms.config;

import android.content.Context;
import android.rms.config.ResourceConfig;
import android.rms.iaware.IAwareDecrypt;
import android.rms.utils.Utils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public final class HwConfigReader {
    private static String CONFIG_CLOUD_UPDATE_FILEPATH = "/data/system/iaware/iaware_srms_config.xml";
    private static String CONFIG_CUST_FILEPATH = "/data/cust/xml/HwResourceManager.xml";
    private static String CONFIG_DEFAULT_FILEPATH = "/system/etc/HwResourceManager.xml";
    private static final boolean DEBGU_XML_PARSE = Utils.DEBUG;
    public static final int FILE_LOCATION_CLOUD = 0;
    public static final int FILE_LOCATION_LOCAL = 1;
    private static final String TAG = "RMS.HwConfigReader";
    private static final String XML_ATTR_BETA_COUNT_INTERVAL = "beta_count_interval";
    private static final String XML_ATTR_CONTENT = "content";
    private static final String XML_ATTR_COUNT_INTERVAL = "count_interval";
    private static final String XML_ATTR_ID = "id";
    private static final String XML_ATTR_IS_COUNT = "isCount";
    private static final String XML_ATTR_LOOP_INTERVAL = "loop_interval";
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_ATTR_NORMAL = "normal";
    private static final String XML_ATTR_RES_MAX_PEROID = "resource_max_peroid";
    private static final String XML_ATTR_RES_STRATEGY = "resource_strategy";
    private static final String XML_ATTR_RES_THRESHOLD = "resource_threshold";
    private static final String XML_ATTR_SAMPLE_BASE_PEROID = "base_peroid";
    private static final String XML_ATTR_SAMPLE_CYCLE_NUM = "cycle_num";
    private static final String XML_ATTR_SAVE_INTERVAL = "save_interval";
    private static final String XML_ATTR_TOTAL_LOOP_INTERVAL = "total_loop_interval";
    private static final String XML_ATTR_URGENT = "urgent";
    private static final String XML_ATTR_VALUE = "value";
    private static final String XML_ATTR_WARNING = "warning";
    private static final String XML_ATTR__MAX_KEEP_FILES = "max_keep_files";
    private static final String XML_TAG_GROUP = "group";
    private static final String XML_TAG_LEVEL = "level";
    private static final String XML_TAG_RESOURCE = "resource";
    private static final String XML_TAG_STATISTIC = "statistic";
    private static final String XML_TAG_SUBTYPE = "subType";
    private static final String XML_TAG_WHITELIST = "whitelist";
    private int mBetaCountInterval = -1;
    private ArrayList<Integer> mCountGroupID = new ArrayList<>();
    private int mCountInterval = -1;
    private HashMap<Integer, Group> mDynamicGoups = new HashMap<>();
    private HashMap<Integer, Group> mGoups = new HashMap<>();
    private int mMaxKeepFiles = -1;
    private int mSampleBasePeriod = -1;
    private int mSaveInterval = -1;

    private static class Group {
        public final int mID;
        public final boolean mIsCount;
        public final String mName;
        private final HashMap<Integer, ResourceConfig> mResConfigs = new HashMap<>();
        public final int mSampleCycleNum;
        private final HashMap<Integer, SubType> mSubTypes = new HashMap<>();
        private final HashMap<Integer, WhiteList> mWhiteLists = new HashMap<>();

        Group(int id, String name, int sampleCycleNum, boolean isCount) {
            this.mID = id;
            this.mName = name;
            this.mSampleCycleNum = sampleCycleNum;
            this.mIsCount = isCount;
        }

        public void addSubTypeItem(int id, SubType subType) {
            if (this.mSubTypes != null) {
                this.mSubTypes.put(Integer.valueOf(id), subType);
            }
        }

        public void addResConfigItem(int id, ResourceConfig config) {
            if (this.mResConfigs != null) {
                this.mResConfigs.put(Integer.valueOf(id), config);
            }
        }

        public void setWhiteList(Integer type, WhiteList whiteList) {
            this.mWhiteLists.put(type, whiteList);
        }

        public HashMap<Integer, SubType> getSubTypes() {
            return this.mSubTypes;
        }

        public HashMap<Integer, ResourceConfig> getResConfigs() {
            return this.mResConfigs;
        }

        public String getWhiteList() {
            return getWhiteList(0);
        }

        public String getWhiteList(Integer type) {
            WhiteList whiteList = this.mWhiteLists.get(type);
            if (whiteList != null) {
                return whiteList.getWhiteList();
            }
            return "";
        }
    }

    private static class SubType {
        public final int mID;
        private final ArrayList<Integer> mLevels = new ArrayList<>();
        public final int mLoopInterval;
        public final String mName;
        public final int mNormalThreshold;
        public final int mResourceMaxPeroid;
        public final int mResourceStrategy;
        public final int mResourceThreshold;
        public final int mTotalLoopInterval;
        public final int mUrgentThreshold;
        public final int mWarningThreshold;

        SubType(int id, String name, int resourceThreshold, int resourceStrategy, int resourceMaxPeroid, int loopInterval, int normalThreshold, int warningThreshold, int urgentThreshold, int totalLoopInterval) {
            this.mID = id;
            this.mName = name;
            this.mResourceThreshold = resourceThreshold;
            this.mResourceStrategy = resourceStrategy;
            this.mResourceMaxPeroid = resourceMaxPeroid;
            this.mLoopInterval = loopInterval;
            this.mNormalThreshold = normalThreshold;
            this.mWarningThreshold = warningThreshold;
            this.mUrgentThreshold = urgentThreshold;
            this.mTotalLoopInterval = totalLoopInterval;
        }

        public void addItem(int level) {
            this.mLevels.add(Integer.valueOf(level));
        }

        public ArrayList<Integer> getLevels() {
            return this.mLevels;
        }
    }

    private static class WhiteList {
        private final String mContent;
        private final String mName;

        WhiteList(String name, String content) {
            this.mName = name;
            this.mContent = content;
        }

        public String getWhiteList() {
            return this.mContent;
        }

        public String getWhiteListName() {
            return this.mName;
        }
    }

    public boolean updateResConfig(Context context) {
        this.mDynamicGoups.clear();
        boolean isLoadSuccess = parseConfig(context, 0);
        if (Utils.DEBUG || Log.HWLog) {
            Log.d(TAG, "updateResConfig() isLoadSuccess:" + isLoadSuccess);
        }
        return isLoadSuccess;
    }

    public boolean loadResConfig(Context context) {
        boolean isDynamicLoadSuccess = parseConfig(context, 0);
        boolean isStaticLoadSuccess = parseConfig(context, 1);
        if (Utils.DEBUG) {
            Log.d(TAG, "loadResConfig dynamicLoad:" + isDynamicLoadSuccess + ", staticLoad:" + isStaticLoadSuccess);
        }
        if (isDynamicLoadSuccess || isStaticLoadSuccess) {
            return true;
        }
        return false;
    }

    public boolean parseConfig(Context context, int fileLocation) {
        File fileForParse;
        if (context == null) {
            Log.e(TAG, "Context is null while parsing config file of RMS");
            return false;
        }
        if (fileLocation == 0) {
            fileForParse = new File(CONFIG_CLOUD_UPDATE_FILEPATH);
        } else {
            fileForParse = new File(CONFIG_CUST_FILEPATH);
        }
        InputStream is = null;
        Group group = null;
        SubType subType = null;
        boolean ret = true;
        try {
            if (fileForParse.exists()) {
                is = new FileInputStream(fileForParse);
            } else if (fileLocation != 0) {
                is = new FileInputStream(new File(CONFIG_DEFAULT_FILEPATH));
            }
            InputStream is2 = IAwareDecrypt.decryptInputStream(context, is);
            if (is2 == null) {
                ret = false;
            }
            if (ret) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(is2, null);
                XmlUtils.beginDocument(parser, XML_TAG_RESOURCE);
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (DEBGU_XML_PARSE) {
                        Log.d(TAG, "[res count] element:" + element);
                    }
                    if (element == null) {
                        break;
                    }
                    if (XML_TAG_STATISTIC.equals(element)) {
                        parseNodeStatistic(parser, fileLocation);
                    }
                    if (XML_TAG_GROUP.equals(element)) {
                        group = parseNodeGroup(parser, fileLocation);
                    } else if (XML_TAG_SUBTYPE.equals(element)) {
                        subType = parseNodeSubType(parser, group);
                    } else if ("level".equals(element)) {
                        int level = XmlUtils.readIntAttribute(parser, "value", 0);
                        if (subType != null) {
                            subType.addItem(level);
                        }
                        if (DEBGU_XML_PARSE) {
                            Log.d(TAG, "[res count] level: " + level);
                        }
                    } else if (XML_TAG_WHITELIST.equals(element)) {
                        int id = XmlUtils.readIntAttribute(parser, "id", 0);
                        String name = XmlUtils.readStringAttribute(parser, "name");
                        String content = XmlUtils.readStringAttribute(parser, XML_ATTR_CONTENT);
                        if (name == null) {
                            name = "";
                        }
                        if (!(content == null || group == null)) {
                            group.setWhiteList(Integer.valueOf(id), new WhiteList(name, content));
                        }
                    }
                }
            }
            if (is2 != null) {
                try {
                    is2.close();
                } catch (IOException e) {
                }
            }
            if (Utils.DEBUG) {
                dump();
            }
            return ret;
        } catch (Exception e2) {
            Log.e(TAG, "[res count] read xml failed.", e2);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    private void parseNodeStatistic(XmlPullParser parser, int fileLocation) {
        if (fileLocation != 0) {
            this.mSampleBasePeriod = XmlUtils.readIntAttribute(parser, XML_ATTR_SAMPLE_BASE_PEROID, -1);
            this.mSaveInterval = XmlUtils.readIntAttribute(parser, XML_ATTR_SAVE_INTERVAL, -1);
            this.mCountInterval = XmlUtils.readIntAttribute(parser, XML_ATTR_COUNT_INTERVAL, -1);
            this.mBetaCountInterval = XmlUtils.readIntAttribute(parser, XML_ATTR_BETA_COUNT_INTERVAL, this.mCountInterval);
            this.mMaxKeepFiles = XmlUtils.readIntAttribute(parser, XML_ATTR__MAX_KEEP_FILES, -1);
            if (DEBGU_XML_PARSE) {
                Log.d(TAG, "[res count] save intervel:" + this.mSaveInterval + " count intervel(minutes):" + this.mCountInterval + " sample base period:" + this.mSampleBasePeriod + " Beta Count Interval (minutes):" + this.mBetaCountInterval + " max keep files:" + this.mMaxKeepFiles);
            }
        }
    }

    private Group parseNodeGroup(XmlPullParser parser, int fileLocation) {
        int id = XmlUtils.readIntAttribute(parser, "id", -1);
        String name = XmlUtils.readStringAttribute(parser, "name");
        int cycleNum = XmlUtils.readIntAttribute(parser, XML_ATTR_SAMPLE_CYCLE_NUM, -1);
        boolean isCount = XmlUtils.readBooleanAttribute(parser, XML_ATTR_IS_COUNT, false);
        Group group = new Group(id, name, cycleNum, isCount);
        if (fileLocation == 0) {
            this.mDynamicGoups.put(Integer.valueOf(id), group);
        } else {
            this.mGoups.put(Integer.valueOf(id), group);
        }
        if (isCount && fileLocation == 1) {
            this.mCountGroupID.add(Integer.valueOf(id));
        }
        if (DEBGU_XML_PARSE) {
            Log.d(TAG, "[res count] group: " + id + " name: " + name + " cycle_num: " + cycleNum + " isCount: " + isCount);
        }
        return group;
    }

    private SubType parseNodeSubType(XmlPullParser parser, Group group) {
        XmlPullParser xmlPullParser = parser;
        Group group2 = group;
        int id = XmlUtils.readIntAttribute(xmlPullParser, "id", -1);
        String name = XmlUtils.readStringAttribute(xmlPullParser, "name");
        int resourceThreshold = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_RES_THRESHOLD, -1);
        int resourceStrategy = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_RES_STRATEGY, -1);
        int resourceMaxPeroid = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_RES_MAX_PEROID, -1);
        int loopInterval = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_LOOP_INTERVAL, -1);
        int normalThreshold = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_NORMAL, -1);
        int warningThreshold = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_WARNING, -1);
        int urgentThreshold = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_URGENT, -1);
        int totalLoopInterval = XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_TOTAL_LOOP_INTERVAL, -1);
        int urgentThreshold2 = urgentThreshold;
        int warningThreshold2 = warningThreshold;
        int normalThreshold2 = normalThreshold;
        int loopInterval2 = loopInterval;
        int resourceMaxPeroid2 = resourceMaxPeroid;
        int resourceStrategy2 = resourceStrategy;
        int resourceThreshold2 = resourceThreshold;
        SubType subType = new SubType(id, name, resourceThreshold, resourceStrategy, resourceMaxPeroid, loopInterval, normalThreshold2, warningThreshold2, urgentThreshold2, totalLoopInterval);
        SubType subType2 = subType;
        if (group2 != null) {
            group2.addSubTypeItem(id, subType2);
        }
        SubType subType3 = subType2;
        ResourceConfig config = new ResourceConfig(id, resourceThreshold2, resourceStrategy2, resourceMaxPeroid2, loopInterval2, name, normalThreshold2, warningThreshold2, urgentThreshold2, totalLoopInterval);
        if (group2 != null) {
            group2.addResConfigItem(id, config);
        }
        if (DEBGU_XML_PARSE) {
            Log.d(TAG, "[res count] subType: " + id + " name: " + name + " resourceThreshold: " + resourceThreshold2 + " resourceStrategy: " + resourceStrategy2 + " resourceMaxPeroid: " + resourceMaxPeroid2 + " loopInterval: " + loopInterval2 + " normalThreshold: " + normalThreshold2 + " warningThreshold: " + warningThreshold2 + " urgentThreshold: " + urgentThreshold2 + " totalLoopInterval: " + totalLoopInterval);
        } else {
            int i = warningThreshold2;
            int i2 = normalThreshold2;
            int i3 = loopInterval2;
            int i4 = resourceMaxPeroid2;
            int i5 = resourceStrategy2;
            int i6 = resourceThreshold2;
        }
        return subType3;
    }

    private void dump() {
        Log.d(TAG, "[res count] ====== dump reource manage config xml ======");
        Log.d(TAG, "[res count] sample base period (ms) : " + this.mSampleBasePeriod + " save interval (minutes):" + this.mSaveInterval + " count intervel (minutes):" + this.mCountInterval + " Beta Count Interval (minutes):" + this.mBetaCountInterval + " max keep files:" + this.mMaxKeepFiles);
        for (Map.Entry<Integer, Group> gEntry : this.mGoups.entrySet()) {
            Group g = (Group) gEntry.getValue();
            Log.d(TAG, "[res count] gourp id: " + g.mID + " name: " + g.mName + " sampleCycleNum: " + g.mSampleCycleNum + " isCount: " + g.mIsCount + " whitelist:" + g.getWhiteList());
            for (Map.Entry<Integer, SubType> sEntry : g.getSubTypes().entrySet()) {
                SubType s = (SubType) sEntry.getValue();
                Log.d(TAG, "[res count] subType id: " + s.mID + " name: " + s.mName + " ResourceThreshold: " + s.mResourceThreshold + " ResourceStrategy: " + s.mResourceStrategy + " ResourceMaxPeroid: " + s.mResourceMaxPeroid + " totalLoopInterval: " + s.mTotalLoopInterval + " LoopInterval: " + s.mLoopInterval + " NormalThreshold: " + s.mNormalThreshold + " WarningThreshold: " + s.mWarningThreshold + " UrgentThreshold: " + s.mUrgentThreshold + " Levles: " + s.getLevels());
            }
        }
        Log.d(TAG, "[res count] the IDs of the group which need to count: " + getCountGroupID());
    }

    public int getGroupNum() {
        if (this.mGoups != null) {
            return this.mGoups.size();
        }
        return 0;
    }

    public String getGroupName(int groupID) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).mName;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the group name: " + groupID, e);
            return "";
        }
    }

    public boolean isCount(int groupID) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).mIsCount;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the group isCount: " + groupID, e);
            return false;
        }
    }

    public int getGroupSampleCycleNum(int groupID) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).mSampleCycleNum;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the Group Sample Cycle Num: " + groupID, e);
            return -1;
        }
    }

    public String getWhiteList(int groupID) {
        return getWhiteList(groupID, 0);
    }

    public String getWhiteList(int groupID, int type) {
        String whiteListDynamic = "";
        String whiteListStatic = "";
        if (!(this.mDynamicGoups.size() == 0 || this.mDynamicGoups.get(Integer.valueOf(groupID)) == null)) {
            String tempWhiteListDynamic = this.mDynamicGoups.get(Integer.valueOf(groupID)).getWhiteList(Integer.valueOf(type));
            if (tempWhiteListDynamic != null) {
                whiteListDynamic = tempWhiteListDynamic;
                if (groupID == 32 && ((type == 3 || type == 4) && !whiteListDynamic.isEmpty())) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, "getWhiteList  id:" + groupID + ", dynamicList:" + whiteListDynamic + ", staticList:" + whiteListStatic + ", type:" + type);
                    }
                    return whiteListDynamic;
                }
            }
        }
        if (!(this.mGoups.size() == 0 || this.mGoups.get(Integer.valueOf(groupID)) == null)) {
            String tempWhiteListStatic = this.mGoups.get(Integer.valueOf(groupID)).getWhiteList(Integer.valueOf(type));
            if (tempWhiteListStatic != null) {
                whiteListStatic = tempWhiteListStatic;
            }
        }
        String whiteList = whiteListDynamic + CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER + whiteListStatic;
        if (Utils.DEBUG) {
            Log.d(TAG, "getWhiteList  id:" + groupID + ", dynamicList:" + whiteListDynamic + ", staticList:" + whiteListStatic + ", mergeResult:" + whiteList);
        }
        return whiteList;
    }

    public ResourceConfig getResConfig(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getResConfigs().get(Integer.valueOf(subType));
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the res config, group: " + groupID + " subType: " + subType, e);
            return null;
        }
    }

    public int getSubTypeNum(int groupID) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().size();
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType num, group: " + groupID, e);
            return 0;
        }
    }

    public String getSubTypeName(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mName;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType name, group: " + groupID + " subType: " + subType, e);
            return "";
        }
    }

    public int getResourceThreshold(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mResourceThreshold;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType ResourceThreshold, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public int getResourceStrategy(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mResourceStrategy;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType ResourceStrategy, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public int getResourceMaxPeroid(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mResourceMaxPeroid;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType ResourceMaxPeroid, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public int getTotalLoopInterval(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mTotalLoopInterval;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType TotalLoopInterval, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public int getLoopInterval(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mLoopInterval;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType LoopInterval, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public int getNormalThreshold(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mNormalThreshold;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType NormalThreshold, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public int getWarningThreshold(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mWarningThreshold;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType WarningThreshold, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public int getUrgentThreshold(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).mUrgentThreshold;
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType UrgentThreshold, group: " + groupID + " subType: " + subType, e);
            return -1;
        }
    }

    public ArrayList<Integer> getSubTypeLevels(int groupID, int subType) {
        try {
            return this.mGoups.get(Integer.valueOf(groupID)).getSubTypes().get(Integer.valueOf(subType)).getLevels();
        } catch (Exception e) {
            Log.e(TAG, "[res count] can't get the subType levels, group: " + groupID + " subType: " + subType, e);
            return null;
        }
    }

    public ArrayList<Integer> getCountGroupID() {
        return this.mCountGroupID;
    }

    public int getSaveInterval() {
        return this.mSaveInterval;
    }

    public int getCountInterval(boolean isBetaUser) {
        return isBetaUser ? this.mBetaCountInterval : this.mCountInterval;
    }

    public int getSampleBasePeriod() {
        return this.mSampleBasePeriod;
    }

    public int getMaxKeepFiles() {
        return this.mMaxKeepFiles;
    }
}
