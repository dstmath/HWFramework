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
    private static final int INVALID_VALUE = -1;
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
    @Override // com.huawei.nb.client.Proxy
    public IKvServiceCall asInterface(IBinder iBinder) {
        return IKvServiceCall.Stub.asInterface(iBinder);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.nb.client.Proxy
    public KvLocalObservable newLocalObservable() {
        return new KvLocalObservable();
    }

    public DataServiceProxy getDataServiceProxy() {
        return (DataServiceProxy) this.secondary;
    }

    public boolean put(K k, V v) {
        if (k != null && v != null) {
            return put(Collections.singletonList(new KvPair(k, v)));
        }
        DSLog.e("put failed: error: invalid parameter.", new Object[0]);
        return false;
    }

    public V get(K k) {
        if (k == null) {
            DSLog.e("get failed: error: invalid parameter.", new Object[0]);
            return null;
        }
        List<KvPair> list = get(Collections.singletonList(k));
        KvPair kvPair = (list == null || list.isEmpty()) ? null : list.get(0);
        if (kvPair == null) {
            return null;
        }
        return (V) kvPair.getValue();
    }

    public boolean delete(K k) {
        if (k != null) {
            return delete(Collections.singletonList(k));
        }
        DSLog.e("delete failed: error: invalid parameter.", new Object[0]);
        return false;
    }

    public boolean put(List<KvPair> list) {
        if (list == null || list.isEmpty()) {
            DSLog.et(TAG, "put failed: error: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "put failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).put(new ObjectContainer(KvPair.class, list, this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "put failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public List<KvPair> get(List<K> list) {
        List<KvPair> list2;
        if (list == null || list.isEmpty()) {
            DSLog.et(TAG, "get failed: error: invalid parameter.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "get failed: error: not connected to kv service.", new Object[0]);
            return null;
        } else {
            try {
                ObjectContainer objectContainer = ((IKvServiceCall) this.remote).get(new ObjectContainer(list.get(0).getClass(), list, this.pkgName));
                if (objectContainer == null) {
                    list2 = null;
                } else {
                    list2 = objectContainer.get();
                }
                if (list2 == null || list2.isEmpty()) {
                    return null;
                }
                return list2;
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "get failed: error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public boolean delete(List<K> list) {
        if (list == null || list.isEmpty()) {
            DSLog.et(TAG, "delete failed: error: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "delete failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).delete(new ObjectContainer(list.get(0).getClass(), list, this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "delete failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public boolean grant(K k, String str, int i) {
        if (k == null || str == null || "".equals(str.trim())) {
            DSLog.et(TAG, "grant failed: error: invalid parameter.", new Object[0]);
            return false;
        }
        if (!(i == 1 || i == 32 || i == 33)) {
            if (i == 0) {
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
            return ((IKvServiceCall) this.remote).grant(new ObjectContainer(k.getClass(), Collections.singletonList(k), this.pkgName), str, i);
        } catch (RemoteException | RuntimeException e) {
            DSLog.et(TAG, "grant failed: error: %s.", e.getMessage());
            return false;
        }
    }

    public String getVersion(K k) {
        if (k == null) {
            DSLog.et(TAG, "getVersion failed: invalid parameter.", new Object[0]);
            return null;
        } else if (this.remote == null) {
            DSLog.et(TAG, "getVersion failed: error: not connected to kv service.", new Object[0]);
            return null;
        } else {
            try {
                return ((IKvServiceCall) this.remote).getVersion(new ObjectContainer(k.getClass(), Collections.singletonList(k), this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "getVersion failed: error: %s.", e.getMessage());
                return null;
            }
        }
    }

    public boolean setVersion(K k, String str) {
        if (k == null) {
            DSLog.et(TAG, "setVersion failed: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "setVersion failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).setVersion(new ObjectContainer(k.getClass(), Collections.singletonList(k), this.pkgName), str);
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "setVersion failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public boolean setCloneStatus(K k, int i) {
        if (k == null) {
            DSLog.et(TAG, "setCloneStatus failed: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "setCloneStatus failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).setCloneStatus(new ObjectContainer(k.getClass(), Collections.singletonList(k), this.pkgName), i);
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "setCloneStatus failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public int getCloneStatus(K k) {
        if (k == null) {
            DSLog.et(TAG, "getCloneStatus failed: invalid parameter.", new Object[0]);
            return -1;
        } else if (this.remote == null) {
            DSLog.et(TAG, "getCloneStatus failed: error: not connected to kv service.", new Object[0]);
            return -1;
        } else {
            try {
                return ((IKvServiceCall) this.remote).getCloneStatus(new ObjectContainer(k.getClass(), Collections.singletonList(k), this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "getCloneStatus failed: error: %s.", e.getMessage());
                return -1;
            }
        }
    }

    public boolean setDataClearStatus(K k, int i) {
        if (k == null || i < 0) {
            DSLog.et(TAG, "setDataClearStatus failed: invalid parameter.", new Object[0]);
            return false;
        } else if (this.remote == null) {
            DSLog.et(TAG, "setDataClearStatus failed: error: not connected to kv service.", new Object[0]);
            return false;
        } else {
            try {
                return ((IKvServiceCall) this.remote).setDataClearStatus(new ObjectContainer(k.getClass(), Collections.singletonList(k), this.pkgName), i);
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "setDataClearStatus failed: error: %s.", e.getMessage());
                return false;
            }
        }
    }

    public int getDataClearStatus(K k) {
        if (k == null) {
            DSLog.et(TAG, "getDataClearStatus failed: invalid parameter.", new Object[0]);
            return -1;
        } else if (this.remote == null) {
            DSLog.et(TAG, "getDataClearStatus failed: error: not connected to kv service.", new Object[0]);
            return -1;
        } else {
            try {
                return ((IKvServiceCall) this.remote).getDataClearStatus(new ObjectContainer(k.getClass(), Collections.singletonList(k), this.pkgName));
            } catch (RemoteException | RuntimeException e) {
                DSLog.et(TAG, "getDataClearStatus failed: error: %s.", e.getMessage());
                return -1;
            }
        }
    }

    public void clearDataByOwner(String str) {
        if (str == null || str.length() < 1) {
            DSLog.et(TAG, "clearDataByOwner failed, reason is invalied parameters", new Object[0]);
        } else if (this.remote == null) {
            DSLog.et(TAG, "clearDataByOwner failed, reason is remote is null", new Object[0]);
        } else if (!"com.huawei.nb.service".equals(this.pkgName)) {
            DSLog.et(TAG, "clearDataByOwner failed, calling PkgName is " + this.pkgName, new Object[0]);
        } else {
            try {
                if (((IKvServiceCall) this.remote).clearDataByOwner(str)) {
                    DSLog.it(TAG, "clearDataByOwner success, pkg is " + str, new Object[0]);
                }
            } catch (RemoteException e) {
                DSLog.et(TAG, "clearDataByOwner failed, reason : " + e.getMessage(), new Object[0]);
            }
        }
    }

    public boolean subscribe(K k, KeyObserver keyObserver) {
        if (k == null || keyObserver == null) {
            DSLog.et(TAG, "subscribe failed: invalid parameter.", new Object[0]);
            return false;
        }
        KeyObserverInfo keyObserverInfo = new KeyObserverInfo(ObserverType.OBSERVER_KEY, k, this.pkgName);
        keyObserverInfo.setProxyId(Integer.valueOf(getId()));
        if (this.localObservable instanceof KvLocalObservable) {
            return ((KvLocalObservable) this.localObservable).registerObserver(keyObserverInfo, keyObserver);
        }
        return false;
    }

    public boolean unSubscribe(K k, KeyObserver keyObserver) {
        if (k == null || keyObserver == null) {
            DSLog.et(TAG, "unSubscribe failed: invalid parameter.", new Object[0]);
            return false;
        }
        KeyObserverInfo keyObserverInfo = new KeyObserverInfo(ObserverType.OBSERVER_KEY, k, this.pkgName);
        keyObserverInfo.setProxyId(Integer.valueOf(getId()));
        if (this.localObservable instanceof KvLocalObservable) {
            return ((KvLocalObservable) this.localObservable).unregisterObserver(keyObserverInfo, keyObserver);
        }
        return false;
    }
}
