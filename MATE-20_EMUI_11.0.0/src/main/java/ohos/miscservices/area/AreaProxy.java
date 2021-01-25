package ohos.miscservices.area;

import java.util.ArrayList;
import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.area.AreaProxyImpl;
import ohos.rpc.RemoteException;

public class AreaProxy implements IAreaSysAbility {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "AreaProxy");
    private static volatile AreaProxy sInstance = null;
    private final List<IAreaListener> mChangedListeners = new ArrayList();
    private final AreaProxyImpl.OnChangedCallback mChangedNotifier = new AreaProxyImpl.OnChangedCallback() {
        /* class ohos.miscservices.area.AreaProxy.AnonymousClass1 */

        @Override // ohos.miscservices.area.AreaProxyImpl.OnChangedCallback
        public void notifyChanged(String str) {
            synchronized (AreaProxy.this.mChangedListeners) {
                HiLog.info(AreaProxy.TAG, "Area changed, notify to %{public}d listeners", Integer.valueOf(AreaProxy.this.mChangedListeners.size()));
                for (IAreaListener iAreaListener : AreaProxy.this.mChangedListeners) {
                    iAreaListener.onChanged(str);
                }
            }
        }
    };
    private AreaProxyImpl mProxyImpl;

    private AreaProxy(Context context) {
        this.mProxyImpl = new AreaProxyImpl(context);
    }

    static IAreaSysAbility getAreaSysAbility(Context context) {
        if (sInstance == null) {
            synchronized (AreaProxy.class) {
                if (sInstance == null) {
                    sInstance = new AreaProxy(context);
                }
            }
        }
        return sInstance;
    }

    @Override // ohos.miscservices.area.IAreaSysAbility
    public String getISOAlpha2Code() throws RemoteException {
        String iSOAlpha2Code;
        synchronized (this.mChangedListeners) {
            iSOAlpha2Code = this.mProxyImpl.getISOAlpha2Code();
        }
        return iSOAlpha2Code;
    }

    @Override // ohos.miscservices.area.IAreaSysAbility
    public void addAreaListener(IAreaListener iAreaListener) throws RemoteException {
        if (iAreaListener == null) {
            HiLog.info(TAG, "add invaild listener", new Object[0]);
            return;
        }
        synchronized (this.mChangedListeners) {
            if (this.mChangedListeners.contains(iAreaListener)) {
                HiLog.info(TAG, "duplicate listener", new Object[0]);
                return;
            }
            this.mProxyImpl.setAreaChangedCb(this.mChangedNotifier);
            this.mProxyImpl.addAreaListener();
            this.mChangedListeners.add(iAreaListener);
            HiLog.info(TAG, "listener added: %{public}d", Integer.valueOf(this.mChangedListeners.size()));
        }
    }

    @Override // ohos.miscservices.area.IAreaSysAbility
    public void removeAreaListener(IAreaListener iAreaListener) throws RemoteException {
        if (iAreaListener == null) {
            HiLog.info(TAG, "remove invaild listener", new Object[0]);
            return;
        }
        synchronized (this.mChangedListeners) {
            HiLog.info(TAG, "listener: %{public}d", Integer.valueOf(this.mChangedListeners.size()));
            if (this.mChangedListeners.contains(iAreaListener)) {
                this.mChangedListeners.remove(iAreaListener);
                HiLog.info(TAG, "successful remove listener %{public}d", Integer.valueOf(this.mChangedListeners.size()));
            }
            if (this.mChangedListeners.isEmpty()) {
                HiLog.info(TAG, "No one listen, remove the service listener ", new Object[0]);
                this.mProxyImpl.removeAreaListener();
                this.mProxyImpl.removeAreaChangedCb();
            }
        }
    }
}
