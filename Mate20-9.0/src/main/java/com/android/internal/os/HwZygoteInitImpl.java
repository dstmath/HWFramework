package com.android.internal.os;

import android.os.IInstalld;
import android.os.SystemProperties;
import android.util.Log;

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
        String[] strArr = classPathElements;
        if (installd == null || strArr == null) {
            return null;
        }
        int size = strArr.length;
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
            clContexts[i] = "";
            newProfiles[i] = false;
            downGrades[i] = false;
            uids[i] = 1000;
        }
        boolean[] zArr = newProfiles;
        String[] strArr2 = clContexts;
        try {
            retDexOpt = installd.getDexOptNeeded(strArr, instructionSets, compilerFilters, clContexts, newProfiles, downGrades, uids);
        } catch (Exception e) {
            Exception exc = e;
            Log.w(TAG, "getMapleSSDexOptNeeded catch exception!", e);
            retDexOpt = null;
        }
        if (retDexOpt == null) {
            Log.e(TAG, "Failed to getMapleSSDexOptNeeded: retDexOpt is null!");
        } else if (retDexOpt.length != size) {
            retDexOpt = null;
            Log.e(TAG, "Failed to getMapleSSDexOptNeeded!");
        }
        return retDexOpt;
    }
}
