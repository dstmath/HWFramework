package android.os;

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
    ArrayMap<IBinder, RemoteCallbackList<E>.Callback> mCallbacks = new ArrayMap<>();
    private boolean mKilled = false;
    private StringBuilder mRecentCallers;

    private final class Callback implements IBinder.DeathRecipient {
        final E mCallback;
        final Object mCookie;

        Callback(E callback, Object cookie) {
            this.mCallback = callback;
            this.mCookie = cookie;
        }

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
        int i = 0;
        while (i < itemCount) {
            try {
                action.accept(getBroadcastItem(i));
                i++;
            } catch (Throwable th) {
                finishBroadcast();
                throw th;
            }
        }
        finishBroadcast();
    }

    public <C> void broadcastForEachCookie(Consumer<C> action) {
        int itemCount = beginBroadcast();
        int i = 0;
        while (i < itemCount) {
            try {
                action.accept(getBroadcastCookie(i));
                i++;
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
            int size = this.mCallbacks.size();
            return size;
        }
    }

    public E getRegisteredCallbackItem(int index) {
        synchronized (this.mCallbacks) {
            if (this.mKilled) {
                return null;
            }
            E e = this.mCallbacks.valueAt(index).mCallback;
            return e;
        }
    }

    public Object getRegisteredCallbackCookie(int index) {
        synchronized (this.mCallbacks) {
            if (this.mKilled) {
                return null;
            }
            Object obj = this.mCallbacks.valueAt(index).mCookie;
            return obj;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
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

    private void logExcessiveCallbacks() {
        long size = (long) this.mCallbacks.size();
        if (size >= 3000) {
            if (size == 3000 && this.mRecentCallers == null) {
                this.mRecentCallers = new StringBuilder();
            }
            if (this.mRecentCallers != null && ((long) this.mRecentCallers.length()) < 1000) {
                this.mRecentCallers.append(Debug.getCallers(5));
                this.mRecentCallers.append(10);
                if (((long) this.mRecentCallers.length()) >= 1000) {
                    Slog.wtf(TAG, "More than 3000 remote callbacks registered. Recent callers:\n" + this.mRecentCallers.toString());
                    this.mRecentCallers = null;
                }
            }
        }
    }
}
