package android.location;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

abstract class LocalListenerHelper<TListener> {
    private final Context mContext;
    private final HashMap<TListener, Handler> mListeners = new HashMap();
    private final String mTag;

    protected interface ListenerOperation<TListener> {
        void execute(TListener tListener) throws RemoteException;
    }

    protected abstract boolean registerWithServer() throws RemoteException;

    protected abstract void unregisterFromServer() throws RemoteException;

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
            if (removed ? this.mListeners.isEmpty() : false) {
                try {
                    unregisterFromServer();
                } catch (RemoteException e) {
                    Log.v(this.mTag, "Error handling last listener removal", e);
                }
            }
        }
        return;
    }

    protected Context getContext() {
        return this.mContext;
    }

    private void executeOperation(ListenerOperation<TListener> operation, TListener listener) {
        try {
            operation.execute(listener);
        } catch (RemoteException e) {
            Log.e(this.mTag, "Error in monitored listener.", e);
        }
    }

    protected void foreach(final ListenerOperation<TListener> operation) {
        synchronized (this.mListeners) {
            Collection<Entry<TListener, Handler>> listeners = new ArrayList(this.mListeners.entrySet());
        }
        for (final Entry<TListener, Handler> listener : listeners) {
            if (listener.getValue() == null) {
                executeOperation(operation, listener.getKey());
            } else {
                ((Handler) listener.getValue()).post(new Runnable() {
                    public void run() {
                        LocalListenerHelper.this.executeOperation(operation, listener.getKey());
                    }
                });
            }
        }
    }
}
