package ohos.rpc;

import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;

public class RemoteObjectCallbackList<E extends IRemoteBroker> {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "RemoteObjectCallbackList");
    private boolean mAlive = true;
    private Object[] mBroadcastBackup = null;
    private int mBroadcastCount = -1;
    private final Map<IRemoteObject, RemoteObjectCallbackList<E>.RemoteObjectCallback> mCallbacks = new HashMap();

    public void onRemoteCallbackDied(E e) {
    }

    /* access modifiers changed from: private */
    public final class RemoteObjectCallback implements IRemoteObject.DeathRecipient {
        final E mCallback;
        final Object mCookie;

        RemoteObjectCallback(E e, Object obj) {
            this.mCallback = e;
            this.mCookie = obj;
        }

        /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: ohos.rpc.RemoteObjectCallbackList */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            synchronized (RemoteObjectCallbackList.this.mCallbacks) {
                RemoteObjectCallbackList.this.mCallbacks.remove(this.mCallback.asObject());
            }
            RemoteObjectCallbackList.this.onRemoteCallbackDied(this.mCallback, this.mCookie);
        }
    }

    public void onRemoteCallbackDied(E e, Object obj) {
        onRemoteCallbackDied(e);
    }

    public int getRemoteObjectCallbackCount() {
        synchronized (this.mCallbacks) {
            if (!this.mAlive) {
                return 0;
            }
            return this.mCallbacks.size();
        }
    }

    /* JADX WARN: Type inference failed for: r2v6, types: [E, E extends ohos.rpc.IRemoteBroker] */
    public E getRemoteObjectCallbackInterface(IRemoteObject iRemoteObject) {
        synchronized (this.mCallbacks) {
            if (this.mAlive && iRemoteObject != null) {
                if (this.mCallbacks.containsKey(iRemoteObject)) {
                    return this.mCallbacks.get(iRemoteObject).mCallback;
                }
            }
            return null;
        }
    }

    public Object getRemoteObjectCallbackCookie(IRemoteObject iRemoteObject) {
        synchronized (this.mCallbacks) {
            if (this.mAlive && iRemoteObject != null) {
                if (this.mCallbacks.containsKey(iRemoteObject)) {
                    return this.mCallbacks.get(iRemoteObject).mCookie;
                }
            }
            return null;
        }
    }

    public boolean register(E e) {
        return register(e, null);
    }

    public boolean register(E e, Object obj) {
        synchronized (this.mCallbacks) {
            if (!this.mAlive) {
                HiLog.error(TAG, "callback list is unregistered", new Object[0]);
                return false;
            } else if (e == null) {
                HiLog.error(TAG, "input callback is null", new Object[0]);
                return false;
            } else {
                IRemoteObject asObject = e.asObject();
                if (asObject == null) {
                    return false;
                }
                RemoteObjectCallbackList<E>.RemoteObjectCallback remoteObjectCallback = new RemoteObjectCallback(e, obj);
                if (!asObject.addDeathRecipient(remoteObjectCallback, 0)) {
                    HiLog.error(TAG, "fail to add death recipient when registering", new Object[0]);
                    return false;
                }
                this.mCallbacks.put(asObject, remoteObjectCallback);
                return true;
            }
        }
    }

    public boolean unregister(E e) {
        synchronized (this.mCallbacks) {
            if (e == null) {
                return false;
            }
            RemoteObjectCallbackList<E>.RemoteObjectCallback remove = this.mCallbacks.remove(e.asObject());
            if (remove == null) {
                HiLog.error(TAG, "input callback is null", new Object[0]);
                return false;
            }
            IRemoteObject asObject = remove.mCallback.asObject();
            if (asObject == null) {
                return false;
            }
            if (!asObject.removeDeathRecipient(remove, 0)) {
                HiLog.error(TAG, "fail to remove death recipient when unregistering", new Object[0]);
            }
            return true;
        }
    }

    public void unregisterAll() {
        synchronized (this.mCallbacks) {
            for (Map.Entry<IRemoteObject, RemoteObjectCallbackList<E>.RemoteObjectCallback> entry : this.mCallbacks.entrySet()) {
                RemoteObjectCallbackList<E>.RemoteObjectCallback value = entry.getValue();
                if (value != null) {
                    IRemoteObject asObject = value.mCallback.asObject();
                    if (asObject != null) {
                        if (!asObject.removeDeathRecipient(value, 0)) {
                            HiLog.error(TAG, "fail to remove death recipient when unregistering all", new Object[0]);
                        }
                    }
                }
            }
            this.mCallbacks.clear();
            this.mAlive = false;
        }
    }

    public int startBroadcast() {
        synchronized (this.mCallbacks) {
            int i = 0;
            if (this.mBroadcastCount > 0) {
                HiLog.error(TAG, "already in a process of starting broadcasting", new Object[0]);
            }
            int size = this.mCallbacks.size();
            this.mBroadcastCount = size;
            if (size <= 0) {
                return 0;
            }
            Object[] objArr = this.mBroadcastBackup;
            if (objArr == null || objArr.length < size) {
                objArr = new Object[size];
                this.mBroadcastBackup = objArr;
            }
            for (Map.Entry<IRemoteObject, RemoteObjectCallbackList<E>.RemoteObjectCallback> entry : this.mCallbacks.entrySet()) {
                objArr[i] = entry.getValue();
                i++;
            }
            return size;
        }
    }

    public void stopBroadcast() {
        synchronized (this.mCallbacks) {
            if (this.mBroadcastCount < 0) {
                HiLog.error(TAG, "already in a process of stopping broadcasting", new Object[0]);
                return;
            }
            Object[] objArr = this.mBroadcastBackup;
            if (objArr != null) {
                int i = this.mBroadcastCount;
                for (int i2 = 0; i2 < i; i2++) {
                    objArr[i2] = null;
                }
            }
            this.mBroadcastCount = -1;
        }
    }

    /* JADX WARN: Type inference failed for: r3v5, types: [E, E extends ohos.rpc.IRemoteBroker] */
    public E getBroadcastInterface(int i) {
        int i2;
        Object[] objArr = this.mBroadcastBackup;
        if (objArr == null || (i2 = this.mBroadcastCount) != objArr.length || i < 0 || i >= i2) {
            HiLog.error(TAG, "invalid index in getBroadcastInterface", new Object[0]);
            return null;
        }
        Object obj = objArr[i];
        if (obj == null) {
            return null;
        }
        return ((RemoteObjectCallback) obj).mCallback;
    }

    public Object getBroadcastCookie(int i) {
        int i2;
        Object[] objArr = this.mBroadcastBackup;
        if (objArr == null || (i2 = this.mBroadcastCount) != objArr.length || i < 0 || i >= i2) {
            HiLog.error(TAG, "invalid index in getBroadcastCookie", new Object[0]);
            return null;
        }
        Object obj = objArr[i];
        if (obj == null) {
            return null;
        }
        return ((RemoteObjectCallback) obj).mCookie;
    }
}
