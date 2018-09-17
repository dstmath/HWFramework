package tmsdkobf;

import android.os.HandlerThread;
import tmsdkobf.iz.a;

public class pc extends HandlerThread {
    private long mr;
    private a sM;

    public pc(String str, int i, long j) {
        super(str, i);
        this.mr = j;
    }

    private a hE() {
        if (this.sM == null) {
            this.sM = iy.co();
        }
        return this.sM;
    }

    public long bL() {
        return this.mr;
    }

    public void run() {
        hE().beforeExecute(this, null);
        super.run();
        hE().b(this, null);
    }

    public synchronized void start() {
        hE().a(this, null);
        super.start();
    }
}
