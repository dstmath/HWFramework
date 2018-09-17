package com.android.server.emcom.xengine;

import java.util.ArrayList;
import java.util.List;

public class XEngineForegroundAppInfo {
    private int mGrade;
    private HiParam mHiParam;
    private volatile boolean mIsTimeArrive;
    private String mPackageName;
    private List<TimePair> mTimeList = new ArrayList();
    private int mainCardPsStatus;

    public static class HiParam {
        private int mMaxGrade;
        private int mMinGrade;
        private int mMultiFlow;
        private int mMultiPath;
        private int mObjectiveDelay;
        private String mPackageName;
        private int mWifiMode;

        public HiParam(String packName, int mf, int mp, int wifiMode, int objDelay, int maxGrade, int minGrade) {
            this.mMultiFlow = mf;
            this.mMultiPath = mp;
            this.mWifiMode = wifiMode;
            this.mPackageName = packName;
            this.mObjectiveDelay = objDelay;
            this.mMaxGrade = maxGrade;
            this.mMinGrade = minGrade;
        }

        public int getMultiFlow() {
            return this.mMultiFlow;
        }

        public int getMultiPath() {
            return this.mMultiPath;
        }

        public int getWifiMode() {
            return this.mWifiMode;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public int getObjectiveDelay() {
            return this.mObjectiveDelay;
        }

        public int getMaxGrade() {
            return this.mMaxGrade;
        }

        public int getMinGrade() {
            return this.mMinGrade;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{MultiFlow=").append(this.mMultiFlow).append(", ").append("MultiPath=").append(this.mMultiPath).append(", ").append("mWifiMode=").append(this.mWifiMode).append(", ").append("ObjectiveDelay=").append(this.mObjectiveDelay).append(", ").append("mMaxGrade=").append(this.mMaxGrade).append(", ").append("mMinGrade=").append(this.mMinGrade).append("}");
            return buffer.toString();
        }
    }

    public static class TimePair {
        private String mEndTime;
        private String mPackageName;
        private String mStartTime;

        public TimePair(String pkgName, String start, String end) {
            this.mPackageName = pkgName;
            this.mStartTime = start;
            this.mEndTime = end;
        }

        public String getStartTime() {
            return this.mStartTime;
        }

        public String getEndTime() {
            return this.mEndTime;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{startTime=").append(this.mStartTime).append(", ").append("endTime=").append(this.mEndTime).append("}");
            return buffer.toString();
        }
    }

    public XEngineForegroundAppInfo(String packageName, int grade, int mainCardPsStatus) {
        this.mPackageName = packageName;
        this.mGrade = grade;
        this.mainCardPsStatus = mainCardPsStatus;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getGrade() {
        return this.mGrade;
    }

    public int getMainCardPsStatus() {
        return this.mainCardPsStatus;
    }

    public boolean isTimeArrive() {
        return this.mIsTimeArrive;
    }

    public void setTimeArrive(boolean isTimeArrive) {
        this.mIsTimeArrive = isTimeArrive;
    }

    public List<TimePair> getTimes() {
        return this.mTimeList;
    }

    public HiParam getParam() {
        return this.mHiParam;
    }

    public void setParam(HiParam hiParam) {
        this.mHiParam = hiParam;
    }

    public boolean isTimeTask() {
        return this.mTimeList.isEmpty() ^ 1;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{packageName=").append(this.mPackageName).append(", ").append("grade=").append(this.mGrade).append(", ").append("mainCardPsStatus=").append(this.mainCardPsStatus).append(", ").append("accelerateTimes=").append(this.mTimeList).append("}");
        return buffer.toString();
    }
}
