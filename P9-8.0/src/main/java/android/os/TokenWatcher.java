package android.os;

import android.os.IBinder.DeathRecipient;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class TokenWatcher {
    private volatile boolean mAcquired = false;
    private Handler mHandler;
    private int mNotificationQueue = -1;
    private Runnable mNotificationTask = new Runnable() {
        public void run() {
            int value;
            synchronized (TokenWatcher.this.mTokens) {
                value = TokenWatcher.this.mNotificationQueue;
                TokenWatcher.this.mNotificationQueue = -1;
            }
            if (value == 1) {
                TokenWatcher.this.acquired();
            } else if (value == 0) {
                TokenWatcher.this.released();
            }
        }
    };
    private String mTag;
    private WeakHashMap<IBinder, Death> mTokens = new WeakHashMap();

    private class Death implements DeathRecipient {
        String tag;
        IBinder token;

        Death(IBinder token, String tag) {
            this.token = token;
            this.tag = tag;
        }

        public void binderDied() {
            TokenWatcher.this.cleanup(this.token, false);
        }

        protected void finalize() throws Throwable {
            try {
                if (this.token != null) {
                    Log.w(TokenWatcher.this.mTag, "cleaning up leaked reference: " + this.tag);
                    TokenWatcher.this.release(this.token);
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }
    }

    public abstract void acquired();

    public abstract void released();

    public TokenWatcher(Handler h, String tag) {
        this.mHandler = h;
        if (tag == null) {
            tag = "TokenWatcher";
        }
        this.mTag = tag;
    }

    public void acquire(IBinder token, String tag) {
        synchronized (this.mTokens) {
            int oldSize = this.mTokens.size();
            Death d = new Death(token, tag);
            try {
                token.linkToDeath(d, 0);
                this.mTokens.put(token, d);
                Log.d(this.mTag, "put token = " + token.toString() + ",mTokens.size() = " + this.mTokens.size());
                if (oldSize == 0 && (this.mAcquired ^ 1) != 0) {
                    sendNotificationLocked(true);
                    this.mAcquired = true;
                }
            } catch (RemoteException e) {
            }
        }
    }

    public void cleanup(IBinder token, boolean unlink) {
        synchronized (this.mTokens) {
            Death d = (Death) this.mTokens.remove(token);
            Log.d(this.mTag, "remove token = " + token.toString() + ",mTokens.size() = " + this.mTokens.size());
            if (unlink && d != null) {
                d.token.unlinkToDeath(d, 0);
                d.token = null;
            }
            if (this.mTokens.size() == 0 && this.mAcquired) {
                sendNotificationLocked(false);
                this.mAcquired = false;
            }
        }
    }

    public void release(IBinder token) {
        cleanup(token, true);
    }

    public boolean isAcquired() {
        boolean z;
        synchronized (this.mTokens) {
            z = this.mAcquired;
        }
        return z;
    }

    public void dump() {
        for (String s : dumpInternal()) {
            Log.i(this.mTag, s);
        }
    }

    public void dump(PrintWriter pw) {
        for (String s : dumpInternal()) {
            pw.println(s);
        }
    }

    private ArrayList<String> dumpInternal() {
        ArrayList<String> a = new ArrayList();
        synchronized (this.mTokens) {
            Set<IBinder> keys = this.mTokens.keySet();
            a.add("Token count: " + this.mTokens.size());
            int i = 0;
            for (IBinder b : keys) {
                a.add("[" + i + "] " + ((Death) this.mTokens.get(b)).tag + " - " + b);
                i++;
            }
        }
        return a;
    }

    private void sendNotificationLocked(boolean on) {
        int value = on ? 1 : 0;
        if (this.mNotificationQueue == -1) {
            this.mNotificationQueue = value;
            this.mHandler.post(this.mNotificationTask);
        } else if (this.mNotificationQueue != value) {
            this.mNotificationQueue = -1;
            this.mHandler.removeCallbacks(this.mNotificationTask);
        }
    }
}
