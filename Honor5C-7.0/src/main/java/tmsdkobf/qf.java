package tmsdkobf;

import android.os.HandlerThread;
import tmsdkobf.kg.a;

/* compiled from: Unknown */
public class qf extends HandlerThread {
    private long lU;
    private a vG;

    public qf(String str, int i, long j) {
        super(str, i);
        this.lU = j;
    }

    private a hH() {
        if (this.vG == null) {
            this.vG = kf.df();
        }
        return this.vG;
    }

    public long bI() {
        return this.lU;
    }

    public void run() {
        hH().beforeExecute(this, null);
        super.run();
        hH().b(this, null);
    }

    public synchronized void start() {
        hH().a(this, null);
        super.start();
    }
}
