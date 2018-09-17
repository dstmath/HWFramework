package tmsdkobf;

public class iz extends Thread {
    private long mr;
    private a sM;
    private Runnable sN;

    public interface a {
        void a(Thread thread, Runnable runnable);

        void b(Thread thread, Runnable runnable);

        void beforeExecute(Thread thread, Runnable runnable);
    }

    public iz(ThreadGroup threadGroup, Runnable runnable, String str, long j) {
        super(threadGroup, runnable, str);
        this.sN = runnable;
        this.mr = j;
    }

    public void a(a aVar) {
        this.sM = aVar;
    }

    public long bL() {
        return this.mr;
    }

    public void run() {
        if (this.sM != null) {
            this.sM.beforeExecute(this, this.sN);
        }
        super.run();
        if (this.sM != null) {
            this.sM.b(this, this.sN);
        }
    }

    public synchronized void start() {
        if (this.sM != null) {
            this.sM.a(this, this.sN);
        }
        super.start();
    }
}
