package com.huawei.server.rme.hyperhold;

import java.util.HashSet;
import java.util.Set;

public final class AppInfo {
    private int activeScore;
    private int anonTotal;
    private String appName;
    private int dailyFreezeTimes;
    private int dailyUseTimes;
    private int foregroundTime;
    private int freezeCount;
    private int historyAnon;
    private int inactScore;
    private boolean isAlive;
    private boolean isThirdParty;
    private int lastAnon;
    private int memUsed;
    private MemcgInfo memcgInfo;
    private int nowScore;
    private Set<Integer> pidSet = new HashSet();
    private int realRatio;
    private int reclaimRatio;
    private int refault;
    private double startHour;
    private StatisticTime statisticTime;
    private int swapRatio;
    private int uid;
    private boolean visible;

    public static class StatisticTime {
        private String historyBackgroundTime = "00:00:00";
        private String historyFreezeTime = "00:00:00";
        private String lastBackgroundTime = "1970-01-01 08:00:00";
        private String lastFreezeTime = "1970-01-01 08:00:00";

        public String swapToString() {
            return ", lastBackgroundTime: " + this.lastBackgroundTime + ", lastFreezeTime: " + this.lastFreezeTime + ", historyBackgroundTime: " + this.historyBackgroundTime + ", historyFreezeTime: " + this.historyFreezeTime;
        }
    }

    public static class MemcgInfo {
        private int freezeTimes;
        private int pageInTotal;
        private int swapInSize;
        private int swapInTotal;
        private int swapOutSize;
        private int swapOutTotal;
        private int swapSizeCur;
        private int swapSizeMax;
        private int unFreezeTimes;

        static /* synthetic */ int access$1908(MemcgInfo x0) {
            int i = x0.freezeTimes;
            x0.freezeTimes = i + 1;
            return i;
        }

        static /* synthetic */ int access$2008(MemcgInfo x0) {
            int i = x0.unFreezeTimes;
            x0.unFreezeTimes = i + 1;
            return i;
        }

        public MemcgInfo(Builder builder) {
            this.swapOutTotal = builder.swapOutTotal;
            this.swapOutSize = builder.swapOutSize;
            this.swapInSize = builder.swapInSize;
            this.swapInTotal = builder.swapInTotal;
            this.pageInTotal = builder.pageInTotal;
            this.swapSizeCur = builder.swapSizeCur;
            this.swapSizeMax = builder.swapSizeMax;
            this.freezeTimes = builder.freezeTimes;
            this.unFreezeTimes = builder.unFreezeTimes;
        }

        public String swapToString() {
            return "swapOutTotal: " + this.swapOutTotal + ", swapOutSize: " + this.swapOutSize + ", swapInSize: " + this.swapInSize + ", swapInTotal: " + this.swapInTotal + ", pageInTotal: " + this.pageInTotal + ", swapSizeCur: " + this.swapSizeCur + ", swapSizeMax: " + this.swapSizeMax + ", freezeTimes: " + this.freezeTimes + ", unFreezeTimes: " + this.unFreezeTimes;
        }

        public static class Builder {
            private int freezeTimes = 0;
            private int pageInTotal = 0;
            private int swapInSize = 0;
            private int swapInTotal = 0;
            private int swapOutSize = 0;
            private int swapOutTotal = 0;
            private int swapSizeCur = 0;
            private int swapSizeMax = 0;
            private int unFreezeTimes = 0;

            public Builder swapOutTotal(int swapOutTotal2) {
                this.swapOutTotal = swapOutTotal2;
                return this;
            }

            public Builder swapOutSize(int swapOutSize2) {
                this.swapOutSize = swapOutSize2;
                return this;
            }

            public Builder swapInSize(int swapInSize2) {
                this.swapInSize = swapInSize2;
                return this;
            }

            public Builder swapInTotal(int swapInTotal2) {
                this.swapInTotal = swapInTotal2;
                return this;
            }

            public Builder pageInTotal(int pageInTotal2) {
                this.pageInTotal = pageInTotal2;
                return this;
            }

            public Builder swapSizeCur(int swapSizeCur2) {
                this.swapSizeCur = swapSizeCur2;
                return this;
            }

            public Builder swapSizeMax(int swapSizeMax2) {
                this.swapSizeMax = swapSizeMax2;
                return this;
            }

            public Builder freezeTimes(int freezeTimes2) {
                this.freezeTimes = freezeTimes2;
                return this;
            }

            public Builder unFreezeTimes(int unFreezeTimes2) {
                this.unFreezeTimes = unFreezeTimes2;
                return this;
            }

            public MemcgInfo create() {
                return new MemcgInfo(this);
            }
        }
    }

    public static class AppInfoBase {
        private String appName;
        private int pid;
        private int uid;

        public AppInfoBase(int uid2, int pid2, String appName2) {
            this.uid = uid2;
            this.pid = pid2;
            this.appName = appName2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getUid() {
            return this.uid;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getPid() {
            return this.pid;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getAppName() {
            return this.appName;
        }
    }

    public AppInfo(AppInfoBase appInfoBase, double startHour2, int reclaimRatio2, int swapRatio2, MemcgInfo memcgInfo2) {
        this.uid = appInfoBase.getUid();
        if (appInfoBase.getPid() > 0) {
            this.pidSet.add(Integer.valueOf(appInfoBase.getPid()));
        }
        this.startHour = startHour2;
        this.visible = false;
        this.freezeCount = 0;
        this.activeScore = -1;
        this.inactScore = -1;
        this.memUsed = -1;
        this.realRatio = 0;
        this.anonTotal = 0;
        this.lastAnon = 0;
        this.historyAnon = 0;
        this.foregroundTime = 0;
        this.reclaimRatio = reclaimRatio2;
        this.swapRatio = swapRatio2;
        this.refault = 0;
        this.nowScore = 300;
        this.appName = appInfoBase.getAppName();
        this.memcgInfo = memcgInfo2;
        this.isThirdParty = false;
        this.isAlive = false;
        this.statisticTime = new StatisticTime();
        this.dailyUseTimes = 0;
        this.dailyFreezeTimes = 0;
    }

    public void incForegroundTime() {
        this.foregroundTime++;
    }

    public void setForgroundTime(int time) {
        this.foregroundTime = time;
    }

    public int getForgroundTime() {
        return this.foregroundTime;
    }

    public void setLastAnon(int anon) {
        this.lastAnon = anon;
    }

    public void setHistoryAnon(int anon) {
        this.historyAnon = anon;
    }

    public int getLastAnon() {
        return this.lastAnon;
    }

    public int getHistoryAnon() {
        return this.historyAnon;
    }

    public void setThirdParty() {
        this.isThirdParty = true;
    }

    public boolean getThirdParty() {
        return this.isThirdParty;
    }

    public void setIsAlive(boolean isAlive2) {
        this.isAlive = isAlive2;
    }

    public boolean getIsAlive() {
        return this.isAlive;
    }

    public void setNowScore(int score) {
        this.nowScore = score;
    }

    public int getNowScore() {
        return this.nowScore;
    }

    public MemcgInfo getMemcgInfo() {
        return this.memcgInfo;
    }

    public void setMemcgInfo(MemcgInfo memcgInfo2) {
        this.memcgInfo = memcgInfo2;
    }

    public int getReclaimRatio() {
        int i = this.nowScore;
        if (i == 0) {
            return 0;
        }
        if (i >= 401) {
            return 100;
        }
        return this.reclaimRatio;
    }

    public int getSwapRatio() {
        int i = this.nowScore;
        if (i == 0) {
            return 0;
        }
        if (i >= 401) {
            return 100;
        }
        return this.swapRatio;
    }

    public int getRefault() {
        return this.refault;
    }

    public int getAnonTotal() {
        return this.anonTotal;
    }

    public int getRealRatio() {
        return this.realRatio;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setRealRatio(int realRatio2) {
        this.realRatio = realRatio2;
    }

    public void setAnonTotal(int anonTotal2) {
        this.anonTotal = anonTotal2;
    }

    public void setReclaimRatio(int reclaimRatio2) {
        this.reclaimRatio = reclaimRatio2;
    }

    public void setSwapRatio(int swapRatio2) {
        this.swapRatio = swapRatio2;
    }

    public void setRefault(int refault2) {
        this.refault = refault2;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible2) {
        this.visible = visible2;
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int uid2) {
        this.uid = uid2;
    }

    public Set<Integer> getPids() {
        return this.pidSet;
    }

    public void addPids(Integer pid) {
        this.pidSet.add(pid);
    }

    public void delPid(Integer pid) {
        this.pidSet.remove(pid);
    }

    public double getStartHour() {
        return this.startHour;
    }

    public void setStartHour(double startHour2) {
        this.startHour = startHour2;
    }

    public int getActiveScore() {
        return this.activeScore;
    }

    public void setActiveScore(int score) {
        this.activeScore = score;
    }

    public int getInactScore() {
        return this.inactScore;
    }

    public void setInactScore(int score) {
        this.inactScore = score;
    }

    public void setSwapOutTotal(int swapOutTotal) {
        this.memcgInfo.swapOutTotal = swapOutTotal;
    }

    public void setSwapOutSize(int swapOutSize) {
        this.memcgInfo.swapOutSize = swapOutSize;
    }

    public void setSwapInSize(int swapInSize) {
        this.memcgInfo.swapInSize = swapInSize;
    }

    public void setSwapInTotal(int swapInTotal) {
        this.memcgInfo.swapInTotal = swapInTotal;
    }

    public void setPageInTotal(int pageInTotal) {
        this.memcgInfo.pageInTotal = pageInTotal;
    }

    public void setSwapSizeCur(int swapSizeCur) {
        this.memcgInfo.swapSizeCur = swapSizeCur;
    }

    public void setSwapSizeMax(int swapSizeMax) {
        this.memcgInfo.swapSizeMax = swapSizeMax;
    }

    public void setFreezeTimes(int freezeTimes) {
        this.memcgInfo.freezeTimes = freezeTimes;
    }

    public void setUnFreezeTimes(int unFreezeTimes) {
        this.memcgInfo.unFreezeTimes = unFreezeTimes;
    }

    public int getSwapOutTotal() {
        return this.memcgInfo.swapOutTotal;
    }

    public int getSwapOutSize() {
        return this.memcgInfo.swapOutSize;
    }

    public int getSwapInSize() {
        return this.memcgInfo.swapInSize;
    }

    public int getSwapInTotal() {
        return this.memcgInfo.swapInTotal;
    }

    public int getPageInTotal() {
        return this.memcgInfo.pageInTotal;
    }

    public int getSwapSizeCur() {
        return this.memcgInfo.swapSizeCur;
    }

    public int getSwapSizeMax() {
        return this.memcgInfo.swapSizeMax;
    }

    public int getFreezeTimes() {
        return this.memcgInfo.freezeTimes;
    }

    public int getUnFreezeTimes() {
        return this.memcgInfo.unFreezeTimes;
    }

    public void incFreezeTimes() {
        MemcgInfo.access$1908(this.memcgInfo);
    }

    public void incUnFreezeTimes() {
        MemcgInfo.access$2008(this.memcgInfo);
    }

    public String swapToString() {
        return "appName: " + this.appName + ", score: " + this.nowScore + ", reclaimRatio: " + getReclaimRatio() + ", swapRatio: " + getSwapRatio() + ", reclaimRefault: " + getRefault() + ", foregroundTime: " + this.foregroundTime + ", " + this.memcgInfo.swapToString() + this.statisticTime.swapToString() + ", history Anon: " + this.historyAnon + "KB.";
    }

    public void setLastBackgroundTime(String lastBackgroundTime) {
        this.statisticTime.lastBackgroundTime = lastBackgroundTime;
    }

    public void setLastFreezeTime(String lastFreezeTime) {
        this.statisticTime.lastFreezeTime = lastFreezeTime;
    }

    public void setHistoryBackgroundTime(String historyBackgroundTime) {
        this.statisticTime.historyBackgroundTime = historyBackgroundTime;
    }

    public void setHistoryFreezeTime(String historyFreezeTime) {
        this.statisticTime.historyFreezeTime = historyFreezeTime;
    }

    public String getLastBackgroundTime() {
        return this.statisticTime.lastBackgroundTime;
    }

    public String getLastFreezeTime() {
        return this.statisticTime.lastFreezeTime;
    }

    public String getHistoryBackgroundTime() {
        return this.statisticTime.historyBackgroundTime;
    }

    public String getHistoryFreezeTime() {
        return this.statisticTime.historyFreezeTime;
    }

    public int getDailyUseTime() {
        return this.dailyUseTimes;
    }

    public void setDailyUseTime(int dailyUseTimes2) {
        this.dailyUseTimes = dailyUseTimes2;
    }

    public int getDailyFreezeTimes() {
        return this.dailyFreezeTimes;
    }

    public void setDailyFreezeTimes(int dailyFreezeTimes2) {
        this.dailyFreezeTimes = dailyFreezeTimes2;
    }
}
