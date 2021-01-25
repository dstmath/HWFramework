package com.android.internal.os;

import android.os.IInstalld;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;

public class HwZygoteInitImpl implements HwZygoteInit {
    private static final String TAG = "HwZygoteInitImpl";
    private static HwZygoteInitImpl sInstance;

    public static synchronized HwZygoteInit getDefault() {
        HwZygoteInitImpl hwZygoteInitImpl;
        synchronized (HwZygoteInitImpl.class) {
            if (sInstance == null) {
                sInstance = new HwZygoteInitImpl();
            }
            hwZygoteInitImpl = sInstance;
        }
        return hwZygoteInitImpl;
    }

    public int[] getDexOptNeededForMapleSystemServer(IInstalld installd, String[] classPathElements, String instructionSet) {
        int[] retDexOpt;
        if (installd == null || classPathElements == null) {
            return null;
        }
        int size = classPathElements.length;
        String systemServerFilter = SystemProperties.get("dalvik.vm.systemservercompilerfilter", "speed");
        String[] instructionSets = new String[size];
        String[] compilerFilters = new String[size];
        String[] clContexts = new String[size];
        boolean[] newProfiles = new boolean[size];
        boolean[] downGrades = new boolean[size];
        int[] uids = new int[size];
        for (int i = 0; i < size; i++) {
            instructionSets[i] = instructionSet;
            compilerFilters[i] = systemServerFilter;
            clContexts[i] = StorageManagerExt.INVALID_KEY_DESC;
            newProfiles[i] = false;
            downGrades[i] = false;
            uids[i] = 1000;
        }
        try {
            retDexOpt = installd.getDexOptNeeded(classPathElements, instructionSets, compilerFilters, clContexts, newProfiles, downGrades, uids);
        } catch (Exception e) {
            Log.e(TAG, "getMapleSSDexOptNeeded catch exception!");
            retDexOpt = null;
        }
        if (retDexOpt == null) {
            Log.e(TAG, "Failed to getMapleSSDexOptNeeded: retDexOpt is null!");
            return retDexOpt;
        } else if (retDexOpt.length == size) {
            return retDexOpt;
        } else {
            Log.e(TAG, "Failed to getMapleSSDexOptNeeded!");
            return null;
        }
    }
}
