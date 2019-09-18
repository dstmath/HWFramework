package android.telecom.Logging;

import android.telecom.Log;

public abstract class Runnable {
    /* access modifiers changed from: private */
    public final Object mLock;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            synchronized (Runnable.this.mLock) {
                try {
                    Log.continueSession(Runnable.this.mSubsession, Runnable.this.mSubsessionName);
                    Runnable.this.loggedRun();
                    if (Runnable.this.mSubsession != null) {
                        Log.endSession();
                        Session unused = Runnable.this.mSubsession = null;
                    }
                } catch (Throwable th) {
                    if (Runnable.this.mSubsession != null) {
                        Log.endSession();
                        Session unused2 = Runnable.this.mSubsession = null;
                    }
                    throw th;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Session mSubsession;
    /* access modifiers changed from: private */
    public final String mSubsessionName;

    public abstract void loggedRun();

    public Runnable(String subsessionName, Object lock) {
        if (lock == null) {
            this.mLock = new Object();
        } else {
            this.mLock = lock;
        }
        this.mSubsessionName = subsessionName;
    }

    public final Runnable getRunnableToCancel() {
        return this.mRunnable;
    }

    public Runnable prepare() {
        cancel();
        this.mSubsession = Log.createSubsession();
        return this.mRunnable;
    }

    public void cancel() {
        synchronized (this.mLock) {
            Log.cancelSubsession(this.mSubsession);
            this.mSubsession = null;
        }
    }
}
