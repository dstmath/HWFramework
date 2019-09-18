package com.huawei.nb.client.kv;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.nb.client.DataServiceProxy;
import com.huawei.nb.client.DualProxy;
import com.huawei.nb.container.ObjectContainer;
import com.huawei.nb.kv.Key;
import com.huawei.nb.kv.KvPair;
import com.huawei.nb.kv.Value;
import com.huawei.nb.notification.KeyObserver;
import com.huawei.nb.notification.KeyObserverInfo;
import com.huawei.nb.notification.KvLocalObservable;
import com.huawei.nb.notification.ObserverType;
import com.huawei.nb.service.IKvServiceCall;
import com.huawei.nb.utils.logger.DSLog;
import java.util.Collections;
import java.util.List;

public class KvClient<K extends Key, V extends Value> extends DualProxy<DataServiceProxy, IKvServiceCall> {
    private static final String KV_SERVICE_ACTION = "com.huawei.nb.service.KvService.START";
    private static final String KV_SERVICE_NAME = "NaturalBase KV Service";
    private static final String NB_PKG_NAME = "com.huawei.nb.service";
    private static final String TAG = "KvClient";

    public KvClient(Context context) {
        super(context, KV_SERVICE_NAME, KV_SERVICE_ACTION, new DataServiceProxy(context));
    }

    public void disconnect() {
        disconnectInner();
    }

    /* access modifiers changed from: protected */
    public IKvServiceCall asInterface(IBinder binder) {
        return IKvServiceCall.Stub.asInterface(binder);
    }

    /* access modifiers changed from: protected */
    public KvLocalObservable newLocalObservable() {
        return new KvLocalObservable(this.callbackManager);
    }

    public DataServiceProxy getDataServiceProxy() {
        return (DataServiceProxy) this.secondary;
    }

    public boolean put(K key, V value) {
        if (key != null && value != null) {
            return put(Collections.singletonList(new KvPair(key, value)));
        }
        DSLog.e("put failed: error: invalid parameter.", new Object[0]);
        return false;
    }

    public V get(K key) {
        if (key == null) {
            DSLog.e("get failed: error: invalid parameter.", new Object[0]);
            return null;
        }
        List<KvPair> retList = get(Collections.singletonList(key));
        KvPair pair = (retList == null || retList.isEmpty()) ? null : retList.get(0);
        return pair == null ? null : pair.getValue();
    }

    public boolean delete(K key) {
        if (key != null) {
            return delete(Collections.singletonList(key));
        }
        DSLog.e("delete failed: error: invalid parameter.", new Object[0]);
        return false;
    }

    public boolean put(List<KvPair> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            DSLog.et(TAG, "put failed: error: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "put failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).put(new ObjectContainer(KvPair.class, pairs, this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "put failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public List<KvPair> get(List<K> keys) {
        if (keys == null || keys.isEmpty()) {
            DSLog.et(TAG, "get failed: error: invalid parameter.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "get failed: error: not connected to kv service.", new Object[0]);
            return null;
        } else {
            try {
                ObjectContainer outContainer = ((IKvServiceCall) this.remote).get(new ObjectContainer(((Key) keys.get(0)).getClass(), keys, this.pkgName));
                List retList = outContainer == null ? null : outContainer.get();
                if (retList == null || retList.isEmpty()) {
                    retList = null;
                }
                return retList;
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "get failed: error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public boolean delete(List<K> keys) {
        if (keys == null || keys.isEmpty()) {
            DSLog.et(TAG, "delete failed: error: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "delete failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).delete(new ObjectContainer(((Key) keys.get(0)).getClass(), keys, this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "delete failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public boolean grant(K key, String packageName, int authority) {
        if (key == null || packageName == null || "".equals(packageName.trim())) {
            DSLog.et(TAG, "grant failed: error: invalid parameter.", new Object[0]);
            return false;
        }
        if (!(authority == 1 || authority == 32 || authority == 33)) {
            if (authority == 0) {
                DSLog.it(TAG, "AUTH_NONE.", new Object[0]);
            } else {
                DSLog.et(TAG, "grant failed: only allow to grant query or update or cancel authority.", new Object[0]);
                return false;
            }
        }
        if (this.remote == null) {
            DSLog.et(TAG, "grant failed: error: not connected to kv service.", new Object[0]);
            return false;
        }
        try {
            return ((IKvServiceCall) this.remote).grant(new ObjectContainer(key.getClass(), Collections.singletonList(key), this.pkgName), packageName, authority);
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "grant failed: error: %s.", e.getMessage());
            return false;
        }
    }

    public String getVersion(K key) {
        if (key == null) {
            DSLog.et(TAG, "getVersion failed: invalid parameter.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "getVersion failed: error: not connected to kv service.", new Object[0]);
            return null;
        } else {
            try {
                return ((IKvServiceCall) this.remote).getVersion(new ObjectContainer(key.getClass(), Collections.singletonList(key), this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "getVersion failed: error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public boolean setVersion(K key, String version) {
        if (key == null) {
            DSLog.et(TAG, "setVersion failed: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "setVersion failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).setVersion(new ObjectContainer(key.getClass(), Collections.singletonList(key), this.pkgName), version);
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "setVersion failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public boolean setCloneStatus(K key, int clone) {
        if (key == null) {
            DSLog.et(TAG, "setCloneStatus failed: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "setCloneStatus failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).setCloneStatus(new ObjectContainer(key.getClass(), Collections.singletonList(key), this.pkgName), clone);
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "setCloneStatus failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public int getCloneStatus(K key) {
        if (key == null) {
            DSLog.et(TAG, "getCloneStatus failed: invalid parameter.", new Object[0]);
            return -1;
        } else if (this.remote == null) {
            DSLog.et(TAG, "getCloneStatus failed: error: not connected to kv service.", new Object[0]);
            return -1;
        } else {
            try {
                return ((IKvServiceCall) this.remote).getCloneStatus(new ObjectContainer(key.getClass(), Collections.singletonList(key), this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "getCloneStatus failed: error: %s.", e.getMessage());
                return -1;
            }
        }
    }

    public boolean setDataClearStatus(K key, int status) {
        if (key == null || status < 0) {
            DSLog.et(TAG, "setDataClearStatus failed: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "setDataClearStatus failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).setDataClearStatus(new ObjectContainer(key.getClass(), Collections.singletonList(key), this.pkgName), status);
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "setDataClearStatus failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public int getDataClearStatus(K key) {
        if (key == null) {
            DSLog.et(TAG, "getDataClearStatus failed: invalid parameter.", new Object[0]);
            return -1;
        } else if (this.remote == null) {
            DSLog.et(TAG, "getDataClearStatus failed: error: not connected to kv service.", new Object[0]);
            return -1;
        } else {
            try {
                return ((IKvServiceCall) this.remote).getDataClearStatus(new ObjectContainer(key.getClass(), Collections.singletonList(key), this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "getDataClearStatus failed: error: %s.", e.getMessage());
                return -1;
            }
        }
    }

    public void clearDataByOwner(String ownerPkgName) {
        if (ownerPkgName == null || ownerPkgName.length() < 1) {
            DSLog.et(TAG, "clearDataByOwner failed, reason is invalied parameters", new Object[0]);
        } else if (this.remote == null) {
            DSLog.et(TAG, "clearDataByOwner failed, reason is remote is null", new Object[0]);
        } else if (!"com.huawei.nb.service".equals(this.pkgName)) {
            DSLog.et(TAG, "clearDataByOwner failed, calling PkgName is " + this.pkgName, new Object[0]);
        } else {
            try {
                if (((IKvServiceCall) this.remote).clearDataByOwner(ownerPkgName)) {
                    DSLog.it(TAG, "clearDataByOwner success, pkg is " + ownerPkgName, new Object[0]);
                }
            } catch (RemoteException e) {
                DSLog.et(TAG, "clearDataByOwner failed, reason : " + e.getMessage(), new Object[0]);
            }
        }
    }

    public boolean subscribe(K key, KeyObserver observer) {
        if (key == null || observer == null) {
            DSLog.et(TAG, "subscribe failed: invalid parameter.", new Object[0]);
            return false;
        }
        KeyObserverInfo observerInfo = new KeyObserverInfo(ObserverType.OBSERVER_KEY, key, this.pkgName);
        observerInfo.setProxyId(Integer.valueOf(getId()));
        return ((KvLocalObservable) this.localObservable).registerObserver(observerInfo, observer);
    }

    public boolean unSubscribe(K key, KeyObserver observer) {
        if (key == null || observer == null) {
            DSLog.et(TAG, "unSubscribe failed: invalid parameter.", new Object[0]);
            return false;
        }
        KeyObserverInfo observerInfo = new KeyObserverInfo(ObserverType.OBSERVER_KEY, key, this.pkgName);
        observerInfo.setProxyId(Integer.valueOf(getId()));
        return ((KvLocalObservable) this.localObservable).unregisterObserver(observerInfo, observer);
    }
}
