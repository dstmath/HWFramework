package com.android.server.rms.memrepair;

import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ProcStateData {
    private static final String TAG = "AwareMem_PSData";
    private int mCustomProcState;
    private AtomicLong mLastPss = new AtomicLong(0);
    private long mLastPssTime;
    private AtomicLong mMaxPss = new AtomicLong(0);
    private int mMergeCount = 0;
    private AtomicLong mMinPss = new AtomicLong(0);
    private int mNextMergeCount = 0;
    private final Object mObjectLock = new Object();
    private int mPid;
    private String mProcName;
    private List<Long> mStatePssList = new ArrayList();

    public ProcStateData(int pid, String procName, int customProcState) {
        this.mPid = pid;
        this.mProcName = procName;
        this.mCustomProcState = customProcState;
    }

    public void addPssToList(long pss, long now, long intervalTime, int sampleCount) {
        AwareLog.d(TAG, "pss=" + pss + ";now=" + now + ";intervalTime=" + (((long) (1 << this.mMergeCount)) * intervalTime));
        this.mLastPss.set(pss);
        synchronized (this.mObjectLock) {
            int lastIndex = this.mStatePssList.size() - 1;
            if (lastIndex < 0) {
                this.mMinPss.set(pss);
                this.mMaxPss.set(pss);
            } else {
                long j;
                AtomicLong atomicLong = this.mMinPss;
                if (this.mMinPss.get() < pss) {
                    j = this.mMinPss.get();
                } else {
                    j = pss;
                }
                atomicLong.set(j);
                atomicLong = this.mMaxPss;
                if (this.mMaxPss.get() > pss) {
                    j = this.mMaxPss.get();
                } else {
                    j = pss;
                }
                atomicLong.set(j);
            }
            if (lastIndex <= -1 || now - this.mLastPssTime > ((long) (1 << this.mMergeCount)) * intervalTime) {
                this.mStatePssList.add(Long.valueOf(pss));
                this.mLastPssTime = now;
                this.mNextMergeCount = 1;
            } else {
                this.mStatePssList.set(lastIndex, Long.valueOf(((((Long) this.mStatePssList.get(lastIndex)).longValue() * ((long) this.mNextMergeCount)) + pss) / ((long) (this.mNextMergeCount + 1))));
                this.mNextMergeCount++;
            }
            if (this.mStatePssList.size() == sampleCount * 2) {
                for (int i = 0; i < this.mStatePssList.size(); i += 2) {
                    this.mStatePssList.set(i / 2, Long.valueOf((((Long) this.mStatePssList.get(i)).longValue() + ((Long) this.mStatePssList.get(i + 1)).longValue()) / 2));
                }
                for (int j2 = this.mStatePssList.size() - 1; j2 > sampleCount - 1; j2--) {
                    this.mStatePssList.remove(j2);
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

    public long getMinPss() {
        return this.mMinPss.get();
    }

    public long getMaxPss() {
        return this.mMaxPss.get();
    }

    public long getLastPss() {
        return this.mLastPss.get();
    }

    public int getMergeCount() {
        return this.mMergeCount;
    }

    public List<Long> getStatePssList() {
        ArrayList<Long> cloneList = new ArrayList();
        synchronized (this.mObjectLock) {
            if (this.mStatePssList.size() > 0) {
                cloneList.addAll(this.mStatePssList);
                return cloneList;
            }
            AwareLog.d(TAG, "mStatePssList size is zero");
            return null;
        }
    }
}
