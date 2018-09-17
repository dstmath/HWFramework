package sun.nio.ch;

class NativeThreadSet {
    static final /* synthetic */ boolean -assertionsDisabled = (NativeThreadSet.class.desiredAssertionStatus() ^ 1);
    private long[] elts;
    private int used = 0;
    private boolean waitingToEmpty;

    NativeThreadSet(int n) {
        this.elts = new long[n];
    }

    int add() {
        long th = NativeThread.current();
        if (th == 0) {
            th = -1;
        }
        synchronized (this) {
            int start = 0;
            if (this.used >= this.elts.length) {
                int on = this.elts.length;
                long[] nelts = new long[(on * 2)];
                System.arraycopy(this.elts, 0, nelts, 0, on);
                this.elts = nelts;
                start = on;
            }
            for (int i = start; i < this.elts.length; i++) {
                if (this.elts[i] == 0) {
                    this.elts[i] = th;
                    this.used++;
                    return i;
                }
            }
            if (-assertionsDisabled) {
                return -1;
            }
            throw new AssertionError();
        }
    }

    void remove(int i) {
        synchronized (this) {
            this.elts[i] = 0;
            this.used--;
            if (this.used == 0 && this.waitingToEmpty) {
                notifyAll();
            }
        }
    }

    synchronized void signalAndWait() {
        boolean interrupted = false;
        while (this.used > 0) {
            int u = this.used;
            for (long th : this.elts) {
                if (th != 0) {
                    if (th != -1) {
                        NativeThread.signal(th);
                    }
                    u--;
                    if (u == 0) {
                        break;
                    }
                }
            }
            this.waitingToEmpty = true;
            try {
                wait(50);
                this.waitingToEmpty = false;
            } catch (InterruptedException e) {
                interrupted = true;
                this.waitingToEmpty = false;
            } catch (Throwable th2) {
                this.waitingToEmpty = false;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
