package android.os;

import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class JankCpuInfo implements Parcelable {
    public static final Creator<JankCpuInfo> CREATOR = new Creator<JankCpuInfo>() {
        public JankCpuInfo createFromParcel(Parcel in) {
            return new JankCpuInfo(in, null);
        }

        public JankCpuInfo[] newArray(int size) {
            return new JankCpuInfo[size];
        }
    };
    private final ArrayList<ProcStats> mProcStats;
    private int mRelCpuTime;
    private int mRelIoWaitTime;
    private int mTotalTime;

    public static class ProcStats {
        public String name;
        public final int pid;
        public int rel_stime;
        public long rel_uptime;
        public int rel_utime;

        public ProcStats(int _pid, String _name, long _uptime, int _utime, int _stime) {
            this.pid = _pid;
            this.name = _name;
            this.rel_uptime = _uptime;
            this.rel_utime = _utime;
            this.rel_stime = _stime;
        }
    }

    /* synthetic */ JankCpuInfo(Parcel in, JankCpuInfo -this1) {
        this(in);
    }

    public JankCpuInfo() {
        this.mProcStats = new ArrayList();
        this.mRelCpuTime = 0;
        this.mRelIoWaitTime = 0;
        this.mTotalTime = 0;
    }

    private JankCpuInfo(Parcel in) {
        this.mProcStats = new ArrayList();
        this.mRelCpuTime = in.readInt();
        this.mRelIoWaitTime = in.readInt();
        this.mTotalTime = in.readInt();
        int N = in.readInt();
        for (int i = 0; i < N; i++) {
            this.mProcStats.add(new ProcStats(in.readInt(), in.readString(), in.readLong(), in.readInt(), in.readInt()));
        }
    }

    public void addProcstats(int _pid, String _name, long _uptime, int _utime, int _stime) {
        this.mProcStats.add(new ProcStats(_pid, _name, _uptime, _utime, _stime));
    }

    public void setVal(int _RelCpuTime, int _RelIoWaitTime, int _TotalTime) {
        this.mRelCpuTime = _RelCpuTime;
        this.mRelIoWaitTime = _RelIoWaitTime;
        this.mTotalTime = _TotalTime;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeInt(this.mRelCpuTime);
        dest.writeInt(this.mRelIoWaitTime);
        dest.writeInt(this.mTotalTime);
        int N = this.mProcStats.size();
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            dest.writeInt(((ProcStats) this.mProcStats.get(i)).pid);
            dest.writeString(((ProcStats) this.mProcStats.get(i)).name);
            dest.writeInt(((ProcStats) this.mProcStats.get(i)).rel_utime);
            dest.writeInt(((ProcStats) this.mProcStats.get(i)).rel_stime);
            dest.writeLong(((ProcStats) this.mProcStats.get(i)).rel_uptime);
        }
    }
}
