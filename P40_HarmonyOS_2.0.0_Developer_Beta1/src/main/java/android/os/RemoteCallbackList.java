package android.os;

import android.annotation.UnsupportedAppUsage;
import android.os.IBinder;
import android.os.IInterface;
import android.util.ArrayMap;
import android.util.Slog;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class RemoteCallbackList<E extends IInterface> {
    private static final String TAG = "RemoteCallbackList";
    private Object[] mActiveBroadcast;
    private int mBroadcastCount = -1;
    @UnsupportedAppUsage
    ArrayMap<IBinder, RemoteCallbackList<E>.Callback> mCallbacks = new ArrayMap<>();
    private boolean mKilled = false;
    private StringBuilder mRecentCallers;

    /* access modifiers changed from: private */
    public final class Callback implements IBinder.DeathRecipient {
        final E mCallback;
        final Object mCookie;

        Callback(E callback, Object cookie) {
            this.mCallback = callback;
            this.mCookie = cookie;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: android.os.RemoteCallbackList */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (RemoteCallbackList.this.mCallbacks) {
                RemoteCallbackList.this.mCallbacks.remove(this.mCallback.asBinder());
            }
            RemoteCallbackList.this.onCallbackDied(this.mCallback, this.mCookie);
        }
    }

    public boolean isContainIBinder(E callback) {
        boolean containsKey;
        IBinder binder = callback.asBinder();
        synchronized (this.mCallbacks) {
            containsKey = this.mCallbacks.containsKey(binder);
        }
        return containsKey;
    }

    public boolean register(E callback) {
        return register(callback, null);
    }

    public boolean register(E callback, Object cookie) {
        synchronized (this.mCallbacks) {
            if (this.mKilled) {
                return false;
            }
            logExcessiveCallbacks();
            IBinder binder = callback.asBinder();
            try {
                RemoteCallbackList<E>.Callback cb = new Callback(callback, cookie);
                binder.linkToDeath(cb, 0);
                this.mCallbacks.put(binder, cb);
                return true;
            } catch (RemoteException e) {
                return false;
            }
        }
    }

    public boolean unregister(E callback) {
        synchronized (this.mCallbacks) {
            RemoteCallbackList<E>.Callback cb = this.mCallbacks.remove(callback.asBinder());
            if (cb == null) {
                return false;
            }
            cb.mCallback.asBinder().unlinkToDeath(cb, 0);
            return true;
        }
    }

    public void kill() {
        synchronized (this.mCallbacks) {
            for (int cbi = this.mCallbacks.size() - 1; cbi >= 0; cbi--) {
                RemoteCallbackList<E>.Callback cb = this.mCallbacks.valueAt(cbi);
                cb.mCallback.asBinder().unlinkToDeath(cb, 0);
            }
            this.mCallbacks.clear();
            this.mKilled = true;
        }
    }

    public void onCallbackDied(E e) {
    }

    public void onCallbackDied(E callback, Object cookie) {
        onCallbackDied(callback);
    }

    public int beginBroadcast() {
        synchronized (this.mCallbacks) {
            if (this.mBroadcastCount <= 0) {
                int N = this.mCallbacks.size();
                this.mBroadcastCount = N;
                if (N <= 0) {
                    return 0;
                }
                Object[] active = this.mActiveBroadcast;
                if (active == null || active.length < N) {
                    Object[] objArr = new Object[N];
                    active = objArr;
                    this.mActiveBroadcast = objArr;
                }
                for (int i = 0; i < N; i++) {
                    active[i] = this.mCallbacks.valueAt(i);
                }
                return N;
            }
            throw new IllegalStateException("beginBroadcast() called while already in a broadcast");
        }
    }

    /* JADX WARN: Type inference failed for: r0v3, types: [E, E extends android.os.IInterface] */
    public E getBroadcastItem(int index) {
        return ((Callback) this.mActiveBroadcast[index]).mCallback;
    }

    public Object getBroadcastCookie(int index) {
        return ((Callback) this.mActiveBroadcast[index]).mCookie;
    }

    public void finishBroadcast() {
        synchronized (this.mCallbacks) {
            if (this.mBroadcastCount >= 0) {
                Object[] active = this.mActiveBroadcast;
                if (active != null) {
                    int N = this.mBroadcastCount;
                    for (int i = 0; i < N; i++) {
                        active[i] = null;
                    }
                }
                this.mBroadcastCount = -1;
            } else {
                throw new IllegalStateException("finishBroadcast() called outside of a broadcast");
            }
        }
    }

    public void broadcast(Consumer<E> action) {
        int itemCount = beginBroadcast();
        for (int i = 0; i < itemCount; i++) {
            try {
                action.accept(getBroadcastItem(i));
            } catch (Throwable th) {
                finishBroadcast();
                throw th;
            }
        }
        finishBroadcast();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: java.util.function.Consumer<C> */
    /* JADX WARN: Multi-variable type inference failed */
    public <C> void broadcastForEachCookie(Consumer<C> action) {
        int itemCount = beginBroadcast();
        for (int i = 0; i < itemCount; i++) {
            try {
                action.accept(getBroadcastCookie(i));
            } catch (Throwable th) {
                finishBroadcast();
                throw th;
            }
        }
        finishBroadcast();
    }

    public int getRegisteredCallbackCount() {
        synchronized (this.mCallbacks) {
            if (this.mKilled) {
                return 0;
            }
            return this.mCallbacks.size();
        }
    }

    /* JADX WARN: Type inference failed for: r1v5, types: [E, E extends android.os.IInterface] */
    public E getRegisteredCallbackItem(int index) {
        synchronized (this.mCallbacks) {
            if (this.mKilled) {
                return null;
            }
            return this.mCallbacks.valueAt(index).mCallback;
        }
    }

    public Object getRegisteredCallbackCookie(int index) {
        synchronized (this.mCallbacks) {
            if (this.mKilled) {
                return null;
            }
            return this.mCallbacks.valueAt(index).mCookie;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        synchronized (this.mCallbacks) {
            pw.print(prefix);
            pw.print("callbacks: ");
            pw.println(this.mCallbacks.size());
            pw.print(prefix);
            pw.print("killed: ");
            pw.println(this.mKilled);
            pw.print(prefix);
            pw.print("broadcasts count: ");
            pw.println(this.mBroadcastCount);
        }
    }

    private void logExcessiveCallbacks() {
        long size = (long) this.mCallbacks.size();
        if (size >= 3000) {
            if (size == 3000 && this.mRecentCallers == null) {
                this.mRecentCallers = new StringBuilder();
            }
            StringBuilder sb = this.mRecentCallers;
            if (sb != null && ((long) sb.length()) < 1000) {
                this.mRecentCallers.append(Debug.getCallers(5));
                this.mRecentCallers.append('\n');
                if (((long) this.mRecentCallers.length()) >= 1000) {
                    Slog.wtf(TAG, "More than 3000 remote callbacks registered. Recent callers:\n" + this.mRecentCallers.toString());
                    this.mRecentCallers = null;
                }
            }
        }
    }
}
