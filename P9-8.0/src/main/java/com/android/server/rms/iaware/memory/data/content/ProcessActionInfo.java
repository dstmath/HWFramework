package com.android.server.rms.iaware.memory.data.content;

import android.annotation.SuppressLint;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.iaware.memory.utils.MemoryConstant.MemActionType;
import java.util.ArrayList;

public class ProcessActionInfo {
    private final int mCurAdj;
    private final int mImpactFactor;
    private final ArrayList<String> mPackageName = new ArrayList();
    private final int mPid;
    private final String mProcessName;
    private final int mSizeRecycled;
    private final MemActionType mType;
    private final int mUid;

    @SuppressLint({"PreferForInArrayList"})
    public ProcessActionInfo(ProcessInfo info, MemActionType type, int size, int impact) {
        this.mPid = info.mPid;
        this.mUid = info.mUid;
        this.mCurAdj = info.mCurAdj;
        this.mProcessName = info.mProcessName;
        this.mType = type;
        this.mSizeRecycled = size;
        this.mImpactFactor = impact;
        for (String s : info.mPackageName) {
            this.mPackageName.add(s);
        }
    }

    public int getPid() {
        return this.mPid;
    }

    public int getUid() {
        return this.mUid;
    }

    public int getOomAdj() {
        return this.mCurAdj;
    }

    public MemActionType getType() {
        return this.mType;
    }

    public int getSizeRecycled() {
        return this.mSizeRecycled;
    }

    public int getImpactFactor() {
        return this.mImpactFactor;
    }

    public String getProcName() {
        return this.mProcessName;
    }

    public ArrayList<String> getPkgName() {
        return this.mPackageName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getPid()).append(" ").append(getProcName()).append(" ").append(getOomAdj()).append(" ").append(this.mType).append(" ").append(this.mSizeRecycled).append(" ").append(this.mImpactFactor);
        return sb.toString();
    }
}
