package android.rms.iaware.memrepair;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class MemRepairPkgInfo implements Parcelable {
    public static final Parcelable.Creator<MemRepairPkgInfo> CREATOR = new Parcelable.Creator<MemRepairPkgInfo>() {
        public MemRepairPkgInfo createFromParcel(Parcel source) {
            return new MemRepairPkgInfo(source);
        }

        public MemRepairPkgInfo[] newArray(int size) {
            return new MemRepairPkgInfo[size];
        }
    };
    private static final int TYPE_EMERG_BG_THRESHOLD = 4;
    private static final int TYPE_EMERG_FG_THRESHOLD = 2;
    private boolean mAwareProtected;
    private boolean mCanClean;
    private final String mPkgName;
    private List<MemRepairProcInfo> mProcessList = new ArrayList();
    private int mThresHoldType;

    public MemRepairPkgInfo(String pkgName) {
        this.mPkgName = pkgName;
        this.mThresHoldType = 0;
        this.mAwareProtected = false;
        this.mCanClean = true;
    }

    protected MemRepairPkgInfo(Parcel source) {
        this.mPkgName = source.readString();
        this.mThresHoldType = source.readInt();
        boolean z = true;
        this.mAwareProtected = source.readInt() == 1;
        this.mCanClean = source.readInt() != 1 ? false : z;
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
        return this.mCanClean;
    }

    public void addProcInfo(MemRepairProcInfo procInfo) {
        this.mProcessList.add(procInfo);
        updateThresHoldType(procInfo.getThresHoldType());
        this.mAwareProtected = procInfo.isAwareProtected() ? true : this.mAwareProtected;
        this.mCanClean = isCanClean(procInfo) ? this.mCanClean : false;
    }

    private boolean isCanClean(MemRepairProcInfo procInfo) {
        if ((this.mThresHoldType & 2) != 0 || (this.mThresHoldType & 4) != 0) {
            return true;
        }
        if (!this.mAwareProtected && procInfo.getCleanType() != 0) {
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
        return this.mAwareProtected;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MemRepairPkgInfo [");
        sb.append("package=");
        sb.append(this.mPkgName);
        sb.append(",type=");
        sb.append(this.mThresHoldType);
        sb.append(",protected=");
        sb.append(this.mAwareProtected);
        if (this.mProcessList != null && this.mProcessList.size() > 0) {
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
        dest.writeInt(this.mAwareProtected ? 1 : 0);
        dest.writeInt(this.mCanClean ? 1 : 0);
        if (this.mProcessList == null || this.mProcessList.size() < 1) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(this.mProcessList.size());
        for (MemRepairProcInfo procInfo : this.mProcessList) {
            procInfo.writeToParcel(dest, flags);
        }
    }
}
