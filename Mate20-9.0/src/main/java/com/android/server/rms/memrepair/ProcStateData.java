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
        this.mMergeCount = 0;
        this.mMinMem = new AtomicLong(0);
        this.mMaxMem = new AtomicLong(0);
        this.mNextMergeCount = 0;
        this.mInitMem = new AtomicLong(-1);
        this.mFlag = 0;
        this.mLastMem = new AtomicLong(0);
        this.mStateMemList = new ArrayList();
        this.mObjectLock = new Object();
        this.mPid = pid;
        this.mProcName = procName;
        this.mCustomProcState = customProcState;
        this.mFlag = isPss ? 1 : 2;
    }

    public void addMemToList(long Mem, long now, long intervalTime, int sampleCount) {
        long j = Mem;
        long j2 = now;
        AwareLog.d(TAG, "Mem=" + j + ";now=" + j2 + ";intervalTime=" + (((long) (1 << this.mMergeCount)) * intervalTime));
        this.mInitMem.compareAndSet(-1, j);
        this.mLastMem.set(j);
        synchronized (this.mObjectLock) {
            int lastIndex = this.mStateMemList.size() - 1;
            if (lastIndex < 0) {
                this.mMinMem.set(j);
                this.mMaxMem.set(j);
            } else {
                this.mMinMem.set(this.mMinMem.get() < j ? this.mMinMem.get() : j);
                this.mMaxMem.set(this.mMaxMem.get() > j ? this.mMaxMem.get() : j);
            }
            if (lastIndex <= -1 || j2 - this.mLastMemTime > ((long) (1 << this.mMergeCount)) * intervalTime) {
                this.mStateMemList.add(Long.valueOf(Mem));
                this.mLastMemTime = j2;
                this.mNextMergeCount = 1;
            } else {
                this.mStateMemList.set(lastIndex, Long.valueOf(((this.mStateMemList.get(lastIndex).longValue() * ((long) this.mNextMergeCount)) + j) / ((long) (this.mNextMergeCount + 1))));
                this.mNextMergeCount++;
            }
            int memSize = this.mStateMemList.size();
            if (memSize == 2 * sampleCount) {
                int i = 0;
                while (i < memSize) {
                    this.mStateMemList.set(i / 2, Long.valueOf((this.mStateMemList.get(i).longValue() + this.mStateMemList.get(i + 1).longValue()) / 2));
                    i += 2;
                    lastIndex = lastIndex;
                    long j3 = Mem;
                }
                for (int j4 = memSize - 1; j4 > sampleCount - 1; j4--) {
                    this.mStateMemList.remove(j4);
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
                return cloneList;
            }
            AwareLog.d(TAG, "mStateMemList size is zero");
            return null;
        }
    }

    public boolean isPss() {
        return this.mFlag == 1;
    }
}
