package tmsdkobf;

import android.os.HandlerThread;
import tmsdk.common.creator.ManagerCreatorC;

public class in {
    private long mr;
    private pb rI;
    private String rJ;

    public in(long j, String str) {
        this.mr = j;
        this.rJ = str == null ? "no_pkg_name-" : str + "-";
        this.rI = (pb) ManagerCreatorC.getManager(pb.class);
    }

    private String aG(String str) {
        return str == null ? null : this.rJ + str;
    }

    public void a(int i, Runnable runnable, String str) {
        this.rI.a(i, runnable, aG(str), this.mr);
    }

    public void a(Runnable runnable) {
        this.rI.a(runnable);
    }

    public void a(Runnable runnable, String str) {
        this.rI.c(runnable, aG(str), this.mr);
    }

    public void addTask(Runnable runnable, String str) {
        this.rI.b(runnable, aG(str), this.mr);
    }

    public HandlerThread newFreeHandlerThread(String str) {
        return this.rI.a(aG(str), 0, this.mr);
    }

    public Thread newFreeThread(Runnable runnable, String str) {
        return this.rI.a(runnable, aG(str), this.mr);
    }
}
