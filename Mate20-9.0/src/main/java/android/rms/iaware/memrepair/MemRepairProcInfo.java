package android.rms.iaware.memrepair;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class MemRepairProcInfo implements Parcelable {
    public static final Parcelable.Creator<MemRepairProcInfo> CREATOR = new Parcelable.Creator<MemRepairProcInfo>() {
        public MemRepairProcInfo createFromParcel(Parcel source) {
            return new MemRepairProcInfo(source);
        }

        public MemRepairProcInfo[] newArray(int size) {
            return new MemRepairProcInfo[size];
        }
    };
    private static final int FLAG_PSS = 1;
    private static final int FLAG_VSS = 2;
    private boolean mAwareProtected;
    private int mCleanType;
    private int mCurAdj;
    private final long mCurPss;
    private int mFlag;
    private final int mPid;
    private final String mProcName;
    private List<MemRepairProcPss> mProcPssList;
    private String mProcStatus;
    private int mThresHoldType;
    private final int mUid;

    public static MemRepairProcInfo createMemRepairProcInfo(int uid, int pid, String processName, long mem, boolean isPss) {
        MemRepairProcInfo memRepairProcInfo = new MemRepairProcInfo(uid, pid, processName, mem, isPss ? 1 : 2);
        return memRepairProcInfo;
    }

    private MemRepairProcInfo(int uid, int pid, String processName, long mem, int flag) {
        this(uid, pid, processName, mem);
        this.mFlag = flag;
    }

    public MemRepairProcInfo(int uid, int pid, String processName, long pss) {
        this.mUid = uid;
        this.mPid = pid;
        this.mProcName = processName;
        this.mCurPss = pss;
        this.mThresHoldType = 0;
        this.mCleanType = 0;
        this.mAwareProtected = false;
        this.mProcPssList = new ArrayList();
    }

    protected MemRepairProcInfo(Parcel source) {
        this.mFlag = source.readInt();
        this.mUid = source.readInt();
        this.mPid = source.readInt();
        this.mProcName = source.readString();
        this.mCurPss = source.readLong();
        this.mThresHoldType = source.readInt();
        this.mCurAdj = source.readInt();
        this.mCleanType = source.readInt();
        this.mAwareProtected = source.readInt() != 1 ? false : true;
        this.mProcStatus = source.readString();
        int pssSize = source.readInt();
        if (pssSize > 0) {
            this.mProcPssList = new ArrayList(pssSize);
        }
        for (int i = 0; i < pssSize; i++) {
            this.mProcPssList.add(new MemRepairProcPss(source));
        }
    }

    public void addMemSets(long[] MemSets, int setsCount, int procState, int mergeCount) {
        addPssSets(MemSets, setsCount, procState, mergeCount);
    }

    public void addPssSets(long[] pssSets, int setsCount, int procState, int mergeCount) {
        if (pssSets != null && pssSets.length > 0 && setsCount > 0 && setsCount <= pssSets.length) {
            this.mProcPssList.add(new MemRepairProcPss(pssSets, setsCount, procState, mergeCount));
        }
    }

    public void updateThresHoldType(int type) {
        this.mThresHoldType |= type;
    }

    public int getThresHoldType() {
        return this.mThresHoldType;
    }

    public long getPss() {
        return getMem();
    }

    public long getMem() {
        return this.mCurPss;
    }

    public int getUid() {
        return this.mUid;
    }

    public int getPid() {
        return this.mPid;
    }

    public String getProcName() {
        return this.mProcName;
    }

    public void updateAppMngInfo(int cleanType, boolean awareProtected, String procStatus, int curAdj) {
        this.mCurAdj = curAdj;
        this.mCleanType = cleanType;
        this.mAwareProtected = awareProtected;
        this.mProcStatus = procStatus;
    }

    public int getCleanType() {
        return this.mCleanType;
    }

    public boolean isAwareProtected() {
        return this.mAwareProtected;
    }

    public String getProcStatus() {
        return this.mProcStatus;
    }

    public List<MemRepairProcPss> getProcPssList() {
        return this.mProcPssList;
    }

    public List<MemRepairProcPss> getProcMemList() {
        return getProcPssList();
    }

    public boolean isPss() {
        return this.mFlag == 1;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MemRepairProcInfo [");
        sb.append("uid=");
        sb.append(this.mUid);
        sb.append(",pid=");
        sb.append(this.mPid);
        sb.append(",process=");
        sb.append(this.mProcName);
        sb.append(",type=");
        sb.append(this.mThresHoldType);
        sb.append(",isPss=");
        sb.append(isPss());
        sb.append(",clean=");
        sb.append(this.mCleanType);
        sb.append(",adj=");
        sb.append(this.mCurAdj);
        sb.append(",protected=");
        sb.append(this.mAwareProtected);
        sb.append(",procStatus=");
        sb.append(this.mProcStatus != null ? this.mProcStatus : "None");
        if (this.mProcPssList != null && this.mProcPssList.size() > 0) {
            sb.append(",procMemSet=");
            sb.append(this.mProcPssList.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mFlag);
        dest.writeInt(this.mUid);
        dest.writeInt(this.mPid);
        dest.writeString(this.mProcName);
        dest.writeLong(this.mCurPss);
        dest.writeInt(this.mThresHoldType);
        dest.writeInt(this.mCurAdj);
        dest.writeInt(this.mCleanType);
        dest.writeInt(this.mAwareProtected ? 1 : 0);
        dest.writeString(this.mProcStatus);
        if (this.mProcPssList == null || this.mProcPssList.size() < 1) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mProcPssList.size());
        for (MemRepairProcPss procPss : this.mProcPssList) {
            procPss.writeToParcel(dest, flags);
        }
    }
}
