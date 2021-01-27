package android.os;

import android.os.IBinder;
import android.util.Log;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class TokenWatcher {
    private volatile boolean mAcquired = false;
    private Handler mHandler;
    private int mNotificationQueue = -1;
    private Runnable mNotificationTask = new Runnable() {
        /* class android.os.TokenWatcher.AnonymousClass1 */

        @Override // java.lang.Runnable
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
    private WeakHashMap<IBinder, Death> mTokens = new WeakHashMap<>();

    public abstract void acquired();

    public abstract void released();

    public TokenWatcher(Handler h, String tag) {
        this.mHandler = h;
        this.mTag = tag != null ? tag : "TokenWatcher";
    }

    public void acquire(IBinder token, String tag) {
        synchronized (this.mTokens) {
            if (!this.mTokens.containsKey(token)) {
                int oldSize = this.mTokens.size();
                Death d = new Death(token, tag);
                try {
                    token.linkToDeath(d, 0);
                    this.mTokens.put(token, d);
                    String str = this.mTag;
                    Log.d(str, "put token = " + token.toString() + ",mTokens.size() = " + this.mTokens.size());
                    if (oldSize == 0 && !this.mAcquired) {
                        sendNotificationLocked(true);
                        this.mAcquired = true;
                    }
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void cleanup(IBinder token, boolean unlink) {
        synchronized (this.mTokens) {
            Death d = this.mTokens.remove(token);
            String str = this.mTag;
            Log.d(str, "remove token = " + token.toString() + ",mTokens.size() = " + this.mTokens.size());
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
        Iterator<String> it = dumpInternal().iterator();
        while (it.hasNext()) {
            Log.i(this.mTag, it.next());
        }
    }

    public void dump(PrintWriter pw) {
        Iterator<String> it = dumpInternal().iterator();
        while (it.hasNext()) {
            pw.println(it.next());
        }
    }

    private ArrayList<String> dumpInternal() {
        ArrayList<String> a = new ArrayList<>();
        synchronized (this.mTokens) {
            Set<IBinder> keys = this.mTokens.keySet();
            a.add("Token count: " + this.mTokens.size());
            int i = 0;
            for (IBinder b : keys) {
                a.add("[" + i + "] " + this.mTokens.get(b).tag + " - " + b);
                i++;
            }
        }
        return a;
    }

    private void sendNotificationLocked(boolean on) {
        int i = this.mNotificationQueue;
        if (i == -1) {
            this.mNotificationQueue = on ? 1 : 0;
            this.mHandler.post(this.mNotificationTask);
        } else if (i != on) {
            this.mNotificationQueue = -1;
            this.mHandler.removeCallbacks(this.mNotificationTask);
        }
    }

    /* access modifiers changed from: private */
    public class Death implements IBinder.DeathRecipient {
        String tag;
        IBinder token;

        Death(IBinder token2, String tag2) {
            this.token = token2;
            this.tag = tag2;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            TokenWatcher.this.cleanup(this.token, false);
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.token != null) {
                    String str = TokenWatcher.this.mTag;
                    Log.w(str, "cleaning up leaked reference: " + this.tag);
                    TokenWatcher.this.release(this.token);
                }
            } finally {
                super.finalize();
            }
        }
    }
}
