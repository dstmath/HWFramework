package org.apache.http.impl.conn.tsccm;

@Deprecated
public class WaitingThreadAborter {
    private boolean aborted;
    private WaitingThread waitingThread;

    public void abort() {
        this.aborted = true;
        if (this.waitingThread != null) {
            this.waitingThread.interrupt();
        }
    }

    public void setWaitingThread(WaitingThread waitingThread2) {
        this.waitingThread = waitingThread2;
        if (this.aborted) {
            waitingThread2.interrupt();
        }
    }
}
