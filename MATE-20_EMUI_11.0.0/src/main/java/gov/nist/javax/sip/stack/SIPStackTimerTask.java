package gov.nist.javax.sip.stack;

import java.util.TimerTask;

public abstract class SIPStackTimerTask extends TimerTask {
    /* access modifiers changed from: protected */
    public abstract void runTask();

    @Override // java.util.TimerTask, java.lang.Runnable
    public final void run() {
        try {
            runTask();
        } catch (Throwable e) {
            System.out.println("SIP stack timer task failed due to exception:");
            e.printStackTrace();
        }
    }
}
