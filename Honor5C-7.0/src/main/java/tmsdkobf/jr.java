package tmsdkobf;

import android.os.HandlerThread;
import tmsdk.common.creator.ManagerCreatorC;

/* compiled from: Unknown */
public class jr {
    private long lU;
    private String mPkgName;
    private qe up;

    public jr(long j, String str) {
        this.lU = j;
        this.mPkgName = str == null ? "no_pkg_name-" : str + "-";
        this.up = (qe) ManagerCreatorC.getManager(qe.class);
    }

    private String bE(String str) {
        return str == null ? null : this.mPkgName + str;
    }

    public void a(int i, Runnable runnable, String str) {
        this.up.a(i, runnable, bE(str), this.lU);
    }

    public void a(Runnable runnable) {
        this.up.a(runnable);
    }

    public void a(Runnable runnable, String str) {
        this.up.b(runnable, bE(str), this.lU);
    }

    public void b(Runnable runnable, String str) {
        this.up.c(runnable, bE(str), this.lU);
    }

    public HandlerThread bF(String str) {
        return this.up.a(bE(str), 0, this.lU);
    }

    public Thread c(Runnable runnable, String str) {
        return this.up.a(runnable, bE(str), this.lU);
    }

    public HandlerThread d(String str, int i) {
        return this.up.a(bE(str), i, this.lU);
    }
}
