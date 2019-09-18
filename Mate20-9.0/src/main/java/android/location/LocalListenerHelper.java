package android.location;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

abstract class LocalListenerHelper<TListener> {
    private final Context mContext;
    private final HashMap<TListener, Handler> mListeners = new HashMap<>();
    private final String mTag;

    protected interface ListenerOperation<TListener> {
        void execute(TListener tlistener) throws RemoteException;
    }

    /* access modifiers changed from: protected */
    public abstract boolean registerWithServer() throws RemoteException;

    /* access modifiers changed from: protected */
    public abstract void unregisterFromServer() throws RemoteException;

    protected LocalListenerHelper(Context context, String name) {
        Preconditions.checkNotNull(name);
        this.mContext = context;
        this.mTag = name;
    }

    public boolean add(TListener listener, Handler handler) {
        Preconditions.checkNotNull(listener);
        synchronized (this.mListeners) {
            if (this.mListeners.isEmpty()) {
                try {
                    if (!registerWithServer()) {
                        Log.e(this.mTag, "Unable to register listener transport.");
                        return false;
                    }
                } catch (RemoteException e) {
                    Log.e(this.mTag, "Error handling first listener.", e);
                    return false;
                }
            }
            if (this.mListeners.containsKey(listener)) {
                return true;
            }
            this.mListeners.put(listener, handler);
            return true;
        }
    }

    public void remove(TListener listener) {
        Preconditions.checkNotNull(listener);
        synchronized (this.mListeners) {
            boolean removed = this.mListeners.containsKey(listener);
            this.mListeners.remove(listener);
            if (removed && this.mListeners.isEmpty()) {
                try {
                    unregisterFromServer();
                } catch (RemoteException e) {
                    Log.v(this.mTag, "Error handling last listener removal", e);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: private */
    public void executeOperation(ListenerOperation<TListener> operation, TListener listener) {
        try {
            operation.execute(listener);
        } catch (RemoteException e) {
            Log.e(this.mTag, "Error in monitored listener.", e);
        }
    }

    /* access modifiers changed from: protected */
    public void foreach(final ListenerOperation<TListener> operation) {
        Collection<Map.Entry<TListener, Handler>> listeners;
        synchronized (this.mListeners) {
            listeners = new ArrayList<>(this.mListeners.entrySet());
        }
        for (final Map.Entry<TListener, Handler> listener : listeners) {
            if (listener.getValue() == null) {
                executeOperation(operation, listener.getKey());
            } else {
                listener.getValue().post(new Runnable() {
                    public void run() {
                        LocalListenerHelper.this.executeOperation(operation, listener.getKey());
                    }
                });
            }
        }
    }
}
