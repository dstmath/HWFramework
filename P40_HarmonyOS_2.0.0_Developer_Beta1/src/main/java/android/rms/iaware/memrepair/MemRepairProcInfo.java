package android.rms.iaware.memrepair;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class MemRepairProcInfo implements Parcelable {
    public static final Parcelable.Creator<MemRepairProcInfo> CREATOR = new Parcelable.Creator<MemRepairProcInfo>() {
        /* class android.rms.iaware.memrepair.MemRepairProcInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MemRepairProcInfo createFromParcel(Parcel source) {
            return new MemRepairProcInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public MemRepairProcInfo[] newArray(int size) {
            return new MemRepairProcInfo[size];
        }
    };
    private static final int FLAG_PSS = 1;
    private static final int FLAG_VSS = 2;
    private static final int PSS_LIST_MAX_SIZE = 10;
    private int mCleanType;
    private int mCurAdj;
    private final long mCurPss;
    private int mFlag;
    private boolean mIsAwareProtected;
    private final int mPid;
    private final String mProcName;
    private List<MemRepairProcPss> mProcPssList;
    private String mProcStatus;
    private int mThresHoldType;
    private final int mUid;

    private MemRepairProcInfo(int uid, int pid, String processName, long mem, int flag) {
        this(uid, pid, processName, mem);
        this.mFlag = flag;
    }

    public MemRepairProcInfo(int uid, int pid, String processName, long pss) {
        this.mProcPssList = new ArrayList();
        this.mUid = uid;
        this.mPid = pid;
        this.mProcName = processName;
        this.mCurPss = pss;
        this.mThresHoldType = 0;
        this.mCleanType = 0;
        this.mIsAwareProtected = false;
    }

    protected MemRepairProcInfo(Parcel source) {
        this.mProcPssList = new ArrayList();
        this.mFlag = source.readInt();
        this.mUid = source.readInt();
        this.mPid = source.readInt();
        this.mProcName = source.readString();
        this.mCurPss = source.readLong();
        this.mThresHoldType = source.readInt();
        this.mCurAdj = source.readInt();
        this.mCleanType = source.readInt();
        this.mIsAwareProtected = source.readInt() != 1 ? false : true;
        this.mProcStatus = source.readString();
        int pssSize = source.readInt();
        if (pssSize > 0 && pssSize < 10) {
            this.mProcPssList = new ArrayList(pssSize);
            for (int i = 0; i < pssSize; i++) {
                this.mProcPssList.add(new MemRepairProcPss(source));
            }
        }
    }

    public static MemRepairProcInfo createMemRepairProcInfo(int uid, int pid, String processName, long mem, boolean isPss) {
        return new MemRepairProcInfo(uid, pid, processName, mem, isPss ? 1 : 2);
    }

    public void addMemSets(long[] memSets, int setsCount, int procState, int mergeCount) {
        addPssSets(memSets, setsCount, procState, mergeCount);
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

    public void updateAppMngInfo(int cleanType, boolean isAwareProtected, String procStatus, int curAdj) {
        this.mCurAdj = curAdj;
        this.mCleanType = cleanType;
        this.mIsAwareProtected = isAwareProtected;
        this.mProcStatus = procStatus;
    }

    public int getCleanType() {
        return this.mCleanType;
    }

    public boolean isAwareProtected() {
        return this.mIsAwareProtected;
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

    @Override // java.lang.Object
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
        sb.append(this.mIsAwareProtected);
        sb.append(",procStatus=");
        String str = this.mProcStatus;
        if (str == null) {
            str = "None";
        }
        sb.append(str);
        List<MemRepairProcPss> list = this.mProcPssList;
        if (list != null && list.size() > 0) {
            sb.append(",procMemSet=");
            sb.append(this.mProcPssList.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mFlag);
        dest.writeInt(this.mUid);
        dest.writeInt(this.mPid);
        dest.writeString(this.mProcName);
        dest.writeLong(this.mCurPss);
        dest.writeInt(this.mThresHoldType);
        dest.writeInt(this.mCurAdj);
        dest.writeInt(this.mCleanType);
        dest.writeInt(this.mIsAwareProtected ? 1 : 0);
        dest.writeString(this.mProcStatus);
        List<MemRepairProcPss> list = this.mProcPssList;
        if (list == null || list.size() < 1) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mProcPssList.size());
        for (MemRepairProcPss procPss : this.mProcPssList) {
            procPss.writeToParcel(dest, flags);
        }
    }
}
