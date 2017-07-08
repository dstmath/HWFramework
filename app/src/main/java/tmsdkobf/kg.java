package tmsdkobf;

/* compiled from: Unknown */
public class kg extends Thread {
    private long lU;
    private a vG;
    private Runnable vH;

    /* compiled from: Unknown */
    public interface a {
        void a(Thread thread, Runnable runnable);

        void b(Thread thread, Runnable runnable);

        void beforeExecute(Thread thread, Runnable runnable);
    }

    public kg(ThreadGroup threadGroup, Runnable runnable, String str, long j) {
        super(threadGroup, runnable, str);
        this.vH = runnable;
        this.lU = j;
    }

    public void a(a aVar) {
        this.vG = aVar;
    }

    public long bI() {
        return this.lU;
    }

    public void run() {
        if (this.vG != null) {
            this.vG.beforeExecute(this, this.vH);
        }
        super.run();
        if (this.vG != null) {
            this.vG.b(this, this.vH);
        }
    }

    public synchronized void start() {
        if (this.vG != null) {
            this.vG.a(this, this.vH);
        }
        super.start();
    }
}
