package android.rms.iaware.memrepair;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class MemRepairPkgInfo implements Parcelable {
    public static final Parcelable.Creator<MemRepairPkgInfo> CREATOR = new Parcelable.Creator<MemRepairPkgInfo>() {
        /* class android.rms.iaware.memrepair.MemRepairPkgInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MemRepairPkgInfo createFromParcel(Parcel source) {
            return new MemRepairPkgInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public MemRepairPkgInfo[] newArray(int size) {
            return new MemRepairPkgInfo[size];
        }
    };
    private static final int TYPE_EMERG_BG_THRESHOLD = 4;
    private static final int TYPE_EMERG_FG_THRESHOLD = 2;
    private boolean mIsAwareProtected;
    private boolean mIsClean;
    private final String mPkgName;
    private List<MemRepairProcInfo> mProcessList = new ArrayList();
    private int mThresHoldType;

    public MemRepairPkgInfo(String pkgName) {
        this.mPkgName = pkgName;
        this.mThresHoldType = 0;
        this.mIsAwareProtected = false;
        this.mIsClean = true;
    }

    protected MemRepairPkgInfo(Parcel source) {
        this.mPkgName = source.readString();
        this.mThresHoldType = source.readInt();
        boolean z = false;
        this.mIsAwareProtected = source.readInt() == 1;
        this.mIsClean = source.readInt() == 1 ? true : z;
        int processSize = source.readInt();
        if (processSize > 0) {
            this.mProcessList = new ArrayList(processSize);
        }
        for (int i = 0; i < processSize; i++) {
            this.mProcessList.add(new MemRepairProcInfo(source));
        }
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public List<MemRepairProcInfo> getProcessList() {
        return this.mProcessList;
    }

    public boolean getCanClean() {
        return this.mIsClean;
    }

    public void addProcInfo(MemRepairProcInfo procInfo) {
        this.mProcessList.add(procInfo);
        updateThresHoldType(procInfo.getThresHoldType());
        this.mIsAwareProtected = procInfo.isAwareProtected() ? true : this.mIsAwareProtected;
        this.mIsClean = isCanClean(procInfo) ? this.mIsClean : false;
    }

    private boolean isCanClean(MemRepairProcInfo procInfo) {
        int i = this.mThresHoldType;
        if ((i & 2) != 0 || (i & 4) != 0) {
            return true;
        }
        if (!this.mIsAwareProtected && procInfo.getCleanType() != 0) {
            return true;
        }
        return false;
    }

    public void updateThresHoldType(int type) {
        this.mThresHoldType |= type;
    }

    public int getThresHoldType() {
        return this.mThresHoldType;
    }

    public boolean isAwareProtected() {
        return this.mIsAwareProtected;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MemRepairPkgInfo [");
        sb.append("package=");
        sb.append(this.mPkgName);
        sb.append(",type=");
        sb.append(this.mThresHoldType);
        sb.append(",protected=");
        sb.append(this.mIsAwareProtected);
        List<MemRepairProcInfo> list = this.mProcessList;
        if (list != null && list.size() > 0) {
            sb.append(",processList=");
            sb.append(this.mProcessList.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPkgName);
        dest.writeInt(this.mThresHoldType);
        dest.writeInt(this.mIsAwareProtected ? 1 : 0);
        dest.writeInt(this.mIsClean ? 1 : 0);
        List<MemRepairProcInfo> list = this.mProcessList;
        if (list == null || list.size() < 1) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mProcessList.size());
        for (MemRepairProcInfo procInfo : this.mProcessList) {
            procInfo.writeToParcel(dest, flags);
        }
    }
}
