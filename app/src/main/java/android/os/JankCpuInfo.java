package android.os;

import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class JankCpuInfo implements Parcelable {
    public static final Creator<JankCpuInfo> CREATOR = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.JankCpuInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.JankCpuInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.JankCpuInfo.<clinit>():void");
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
