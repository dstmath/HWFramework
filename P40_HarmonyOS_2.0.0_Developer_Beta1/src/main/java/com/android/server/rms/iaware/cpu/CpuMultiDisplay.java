package com.android.server.rms.iaware.cpu;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.Display;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuMultiDisplay {
    private static final int INVALID_VALUE = -1;
    private static final int PHONE_DISPLAY_ID = 0;
    private static final Object SLOCK = new Object();
    private static final String TAG = "CpuMultiDisplay";
    private static CpuMultiDisplay sInstance;
    private static AtomicBoolean sIsFeatureEnable = new AtomicBoolean(false);
    private ArrayMap<Integer, Integer> mAllAppMap = new ArrayMap<>();
    private Context mContext;
    private Set<Integer> mDisplayIdList = new ArraySet();
    private DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.rms.iaware.cpu.CpuMultiDisplay.AnonymousClass1 */

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
            synchronized (CpuMultiDisplay.this.mDisplayIdList) {
                CpuMultiDisplay.this.mDisplayIdList.add(Integer.valueOf(displayId));
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
            synchronized (CpuMultiDisplay.this.mDisplayIdList) {
                CpuMultiDisplay.this.mDisplayIdList.remove(Integer.valueOf(displayId));
            }
            CpuMultiDisplay.this.resetDisplayRemoveVip(displayId);
        }
    };
    private DisplayManager mDisplayManager;
    private ArrayMap<Integer, Integer> mPidToDisplayIdMap = new ArrayMap<>();

    private CpuMultiDisplay() {
    }

    private void displayInit() {
        Object obj = this.mContext.getSystemService("display");
        if (obj instanceof DisplayManager) {
            this.mDisplayManager = (DisplayManager) obj;
            Display[] displayList = this.mDisplayManager.getDisplays();
            synchronized (this.mDisplayIdList) {
                for (Display displayOne : displayList) {
                    this.mDisplayIdList.add(Integer.valueOf(displayOne.getDisplayId()));
                }
            }
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
        }
    }

    public static CpuMultiDisplay getInstance() {
        CpuMultiDisplay cpuMultiDisplay;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuMultiDisplay();
            }
            cpuMultiDisplay = sInstance;
        }
        return cpuMultiDisplay;
    }

    public void enable(Context context) {
        if (sIsFeatureEnable.get()) {
            AwareLog.i(TAG, "CpuMultiDisplay has already enabled!");
            return;
        }
        this.mContext = context;
        displayInit();
        sIsFeatureEnable.set(true);
    }

    public void disable() {
        if (sIsFeatureEnable.get()) {
            sIsFeatureEnable.set(false);
            synchronized (this.mPidToDisplayIdMap) {
                this.mPidToDisplayIdMap.clear();
            }
            synchronized (this.mDisplayIdList) {
                this.mDisplayIdList.clear();
            }
            DisplayManager displayManager = this.mDisplayManager;
            if (displayManager != null) {
                displayManager.unregisterDisplayListener(this.mDisplayListener);
            }
        }
    }

    public void addPidDisplayInfo(int pid, int displayId) {
        if (sIsFeatureEnable.get()) {
            synchronized (this.mAllAppMap) {
                this.mAllAppMap.put(Integer.valueOf(pid), Integer.valueOf(displayId));
            }
        }
    }

    public void removePidDisplayInfo(int pid) {
        if (sIsFeatureEnable.get()) {
            synchronized (this.mAllAppMap) {
                this.mAllAppMap.remove(Integer.valueOf(pid));
            }
        }
    }

    public boolean isPhoneDisplay(int pid) {
        if (!sIsFeatureEnable.get()) {
            return true;
        }
        synchronized (this.mAllAppMap) {
            Integer displayId = this.mAllAppMap.get(Integer.valueOf(pid));
            if (displayId == null) {
                return true;
            }
            if (displayId.intValue() == 0) {
                return true;
            }
            return false;
        }
    }

    public boolean isInFocusPidList(int pid) {
        synchronized (this.mPidToDisplayIdMap) {
            if (!sIsFeatureEnable.get() || !this.mPidToDisplayIdMap.containsKey(Integer.valueOf(pid))) {
                return false;
            }
            return true;
        }
    }

    private Set<Integer> getRemovePidList(int pid, int displayId) {
        Set<Integer> removePidList = new ArraySet<>();
        AwareLog.d(TAG, "current pid is : " + pid + "  DisplayId: " + displayId);
        for (Map.Entry<Integer, Integer> entry : this.mPidToDisplayIdMap.entrySet()) {
            int curPid = entry.getKey().intValue();
            if (entry.getValue().intValue() == displayId && curPid != pid) {
                removePidList.add(Integer.valueOf(curPid));
            }
        }
        return removePidList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetDisplayRemoveVip(int displayId) {
        synchronized (this.mPidToDisplayIdMap) {
            Iterator<Map.Entry<Integer, Integer>> it = this.mPidToDisplayIdMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Integer> entry = it.next();
                int curPid = entry.getKey().intValue();
                if (entry.getValue().intValue() == displayId) {
                    it.remove();
                    CpuVipThread.getInstance().setDisplayToVip(false, curPid);
                    AwareLog.d(TAG, "display Id: " + displayId + " is remove, and reset vip pid:" + curPid);
                }
            }
        }
    }

    public void multiDisplayProcessDie(int pid) {
        if (sIsFeatureEnable.get()) {
            synchronized (this.mPidToDisplayIdMap) {
                if (this.mPidToDisplayIdMap.containsKey(Integer.valueOf(pid))) {
                    this.mPidToDisplayIdMap.remove(Integer.valueOf(pid));
                    AwareLog.d(TAG, "process died, remove pid:" + pid);
                }
            }
        }
    }

    public void multiDisplayFocusProcess(int pid, int displayId) {
        if (sIsFeatureEnable.get()) {
            if (pid < 0 || displayId == -1) {
                AwareLog.d(TAG, "input pid or displayId invalid");
                return;
            }
            synchronized (this.mPidToDisplayIdMap) {
                for (Integer num : getRemovePidList(pid, displayId)) {
                    int removePid = num.intValue();
                    this.mPidToDisplayIdMap.remove(Integer.valueOf(removePid));
                    CpuVipThread.getInstance().setDisplayToVip(false, removePid);
                }
                if (this.mPidToDisplayIdMap.containsKey(Integer.valueOf(pid))) {
                    this.mPidToDisplayIdMap.put(Integer.valueOf(pid), Integer.valueOf(displayId));
                    AwareLog.d(TAG, "pid:" + pid + " is in Map");
                    return;
                }
                synchronized (this.mDisplayIdList) {
                    if (this.mDisplayIdList.contains(Integer.valueOf(displayId))) {
                        this.mPidToDisplayIdMap.put(Integer.valueOf(pid), Integer.valueOf(displayId));
                        CpuVipThread.getInstance().setDisplayToVip(true, pid);
                    }
                }
                AwareLog.d(TAG, "[" + pid + ":" + displayId + "] add in map");
            }
        }
    }

    public void multiDisplayResetVip(int pid) {
        if (sIsFeatureEnable.get()) {
            if (pid < 0) {
                AwareLog.d(TAG, "input invalid!");
                return;
            }
            synchronized (this.mPidToDisplayIdMap) {
                if (this.mPidToDisplayIdMap.containsKey(Integer.valueOf(pid))) {
                    this.mPidToDisplayIdMap.remove(Integer.valueOf(pid));
                    CpuVipThread.getInstance().setDisplayToVip(false, pid);
                }
            }
        }
    }
}
