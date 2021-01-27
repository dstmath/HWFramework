package com.huawei.server.rme.hyperhold;

import android.content.Context;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdvancedKillerPackageInfo {

    public enum FrequencyType {
        FREQUENCY_DEFAULT,
        FREQUENCY_HIGH,
        FREQUENCY_CRIT
    }

    public List<Integer> getPids() {
        return Collections.emptyList();
    }

    public void setTotalMem(long totalMem) {
    }

    public long getTotalMem() {
        return 0;
    }

    public int getAdjScore() {
        return 0;
    }

    public int getUid() {
        return 0;
    }

    public String getPackageName() {
        return "";
    }

    public String getFirstProcessName() {
        return "";
    }

    public int getWeight() {
        return -1;
    }

    public int getFloatingBarPosition() {
        return -1;
    }

    public List<Integer> killPackage(Context context, AtomicBoolean state, boolean needCheckAdj) {
        return Collections.emptyList();
    }

    public boolean canPackageBeKilled() {
        return false;
    }

    public boolean canPackageBeKilled(long mem) {
        return false;
    }

    public boolean isImportantSysApp() {
        return false;
    }

    public void killNotifyPackageTracker() {
    }

    public void killNotifyCachedClean() {
    }

    public FrequencyType getFreqType() {
        return FrequencyType.FREQUENCY_DEFAULT;
    }
}
