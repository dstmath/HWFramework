package com.android.server.rms.memrepair;

import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ProcStateData {
    private static final int FLAG_PSS = 1;
    private static final int FLAG_VSS = 2;
    private static final String TAG = "AwareMem_PSData";
    private int mCustomProcState;
    private int mFlag;
    private AtomicLong mInitMem;
    private AtomicLong mLastMem;
    private long mLastMemTime;
    private AtomicLong mMaxMem;
    private int mMergeCount;
    private AtomicLong mMinMem;
    private int mNextMergeCount;
    private final Object mObjectLock;
    private int mPid;
    private String mProcName;
    private List<Long> mStateMemList;

    public ProcStateData(int pid, String procName, int customProcState) {
        this(pid, procName, customProcState, true);
    }

    public ProcStateData(int pid, String procName, int customProcState, boolean isPss) {
        this.mObjectLock = new Object();
        this.mFlag = 0;
        this.mPid = 0;
        this.mCustomProcState = 0;
        this.mMergeCount = 0;
        this.mNextMergeCount = 0;
        this.mLastMemTime = 0;
        this.mProcName = "";
        this.mInitMem = new AtomicLong(-1);
        this.mMinMem = new AtomicLong(0);
        this.mMaxMem = new AtomicLong(0);
        this.mLastMem = new AtomicLong(0);
        this.mStateMemList = new ArrayList();
        this.mPid = pid;
        this.mProcName = procName;
        this.mCustomProcState = customProcState;
        this.mFlag = isPss ? 1 : 2;
    }

    public void addMemToList(long memValue, long now, long intervalTime, int sampleCount) {
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "Mem=" + memValue + ";now=" + now + ";intervalTime=" + (((long) (1 << this.mMergeCount)) * intervalTime));
        }
        this.mInitMem.compareAndSet(-1, memValue);
        this.mLastMem.set(memValue);
        synchronized (this.mObjectLock) {
            int lastIndex = this.mStateMemList.size() - 1;
            if (lastIndex < 0) {
                this.mMinMem.set(memValue);
                this.mMaxMem.set(memValue);
            } else {
                this.mMinMem.set(this.mMinMem.get() < memValue ? this.mMinMem.get() : memValue);
                this.mMaxMem.set(this.mMaxMem.get() > memValue ? this.mMaxMem.get() : memValue);
            }
            long curIntervals = now - this.mLastMemTime;
            long maxIntervals = ((long) (1 << this.mMergeCount)) * intervalTime;
            if (lastIndex <= -1 || curIntervals > maxIntervals) {
                this.mStateMemList.add(Long.valueOf(memValue));
                this.mLastMemTime = now;
                this.mNextMergeCount = 1;
            } else {
                this.mStateMemList.set(lastIndex, Long.valueOf(((this.mStateMemList.get(lastIndex).longValue() * ((long) this.mNextMergeCount)) + memValue) / ((long) (this.mNextMergeCount + 1))));
                this.mNextMergeCount++;
            }
            int memSize = this.mStateMemList.size();
            if (memSize == sampleCount * 2) {
                int i = 0;
                while (i < memSize) {
                    this.mStateMemList.set(i / 2, Long.valueOf((this.mStateMemList.get(i).longValue() + this.mStateMemList.get(i + 1).longValue()) / 2));
                    i += 2;
                    lastIndex = lastIndex;
                }
                for (int j = memSize - 1; j > sampleCount - 1; j--) {
                    this.mStateMemList.remove(j);
                }
                this.mMergeCount++;
            }
        }
    }

    public int getPid() {
        return this.mPid;
    }

    public String getProcName() {
        return this.mProcName;
    }

    public int getState() {
        return this.mCustomProcState;
    }

    public long getInitMem() {
        return this.mInitMem.get();
    }

    public long getMinMem() {
        return this.mMinMem.get();
    }

    public long getMaxMem() {
        return this.mMaxMem.get();
    }

    public long getLastMem() {
        return this.mLastMem.get();
    }

    public int getMergeCount() {
        return this.mMergeCount;
    }

    public List<Long> getStateMemList() {
        ArrayList<Long> cloneList = new ArrayList<>();
        synchronized (this.mObjectLock) {
            if (this.mStateMemList.size() > 0) {
                cloneList.addAll(this.mStateMemList);
            } else {
                AwareLog.d(TAG, "stateMemList size is zero");
            }
        }
        return cloneList;
    }

    public boolean isPss() {
        return this.mFlag == 1;
    }
}
