package android.rms.iaware.memrepair;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class MemRepairPkgInfo implements Parcelable {
    public static final Creator<MemRepairPkgInfo> CREATOR = new Creator<MemRepairPkgInfo>() {
        public MemRepairPkgInfo createFromParcel(Parcel source) {
            return new MemRepairPkgInfo(source);
        }

        public MemRepairPkgInfo[] newArray(int size) {
            return new MemRepairPkgInfo[size];
        }
    };
    private boolean mAwareProtected;
    private final String mPkgName;
    private List<MemRepairProcInfo> mProcessList = new ArrayList();
    private int mThresHoldType;

    public MemRepairPkgInfo(String pkgName) {
        this.mPkgName = pkgName;
        this.mThresHoldType = 0;
        this.mAwareProtected = false;
    }

    protected MemRepairPkgInfo(Parcel source) {
        boolean z = true;
        this.mPkgName = source.readString();
        this.mThresHoldType = source.readInt();
        if (source.readInt() != 1) {
            z = false;
        }
        this.mAwareProtected = z;
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

    public void addProcInfo(MemRepairProcInfo procInfo) {
        this.mProcessList.add(procInfo);
        updateThresHoldType(procInfo.getThresHoldType());
        this.mAwareProtected = procInfo.isAwareProtected() ? true : this.mAwareProtected;
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
        sb.append("package=").append(this.mPkgName);
        sb.append(",type=").append(this.mThresHoldType);
        sb.append(",protected=").append(this.mAwareProtected);
        if (this.mProcessList != null && this.mProcessList.size() > 0) {
            sb.append(",processList=").append(this.mProcessList.toString());
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
