package com.android.server.emcom.xengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XEngineConfigInfo {
    Map<String, String> autoGrabParams = new HashMap();
    int grade;
    public HicomFeaturesInfo hicomFeaturesInfo;
    boolean isForeground;
    int mUid;
    int mainCardPsStatus;
    String packageName;
    List<TimePairInfo> timeInfos = new ArrayList();
    List<BoostViewInfo> viewInfos = new ArrayList();

    public static class BoostViewInfo {
        String container;
        int grade;
        String keyword;
        int mainCardPsStatus;
        int maxCount;
        int maxDepth;
        String rootView;
        String version;

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{version=").append(this.version).append(", ").append("rootView=").append(this.rootView).append(", ").append("container=").append(this.container).append(", ").append("keyword=").append(this.keyword).append(", ").append("maxDepth=").append(this.maxDepth).append(", ").append("maxCount=").append(this.maxCount).append(", ").append("grade=").append(this.grade).append(", ").append("mainCardPsStatus=").append(this.mainCardPsStatus).append("}");
            return buffer.toString();
        }
    }

    public static class HicomFeaturesInfo {
        public int maxGrade;
        public int minGrade;
        public int multiFlow;
        public int multiPath;
        public int objectiveDelay;
        public int wifiMode;

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{multiFlow=").append(this.multiFlow).append(", ").append("multiPath=").append(this.multiPath).append(", ").append("wifiMode=").append(this.wifiMode).append(", ").append("objectiveDelay=").append(this.objectiveDelay).append("maxGrade=").append(this.maxGrade).append(", ").append("minGrade=").append(this.minGrade).append("}");
            return buffer.toString();
        }
    }

    public static class TimePairInfo {
        String endTime;
        String startTime;

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{startTime=").append(this.startTime).append(", ").append("endTime=").append(this.endTime).append("}");
            return buffer.toString();
        }
    }

    public String getPackageName() {
        return this.packageName;
    }

    public HicomFeaturesInfo getHicomFeaturesInfo() {
        return this.hicomFeaturesInfo;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("packageName=").append(this.packageName).append(", ").append("grade=").append(this.grade).append(", ").append("isForeground=").append(this.isForeground).append(", ").append("mainCardPsStatus=").append(this.mainCardPsStatus).append(", ").append("autoGrabParams=").append(this.autoGrabParams).append(", ").append("viewInfos=").append(this.viewInfos).append(", ").append("timeInfos=").append(this.timeInfos).append("hicomFeaturesInfos=").append(this.hicomFeaturesInfo).append(", ").append("timeInfos=").append(this.timeInfos);
        return buffer.toString();
    }
}
