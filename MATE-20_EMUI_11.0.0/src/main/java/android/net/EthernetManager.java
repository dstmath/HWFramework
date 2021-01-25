package android.net;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.IEthernetServiceListener;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

public class EthernetManager {
    private static final int MSG_AVAILABILITY_CHANGED = 1000;
    private static final String TAG = "EthernetManager";
    private final Context mContext;
    private final Handler mHandler = new Handler() {
        /* class android.net.EthernetManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1000) {
                boolean isAvailable = true;
                if (msg.arg1 != 1) {
                    isAvailable = false;
                }
                Iterator it = EthernetManager.this.mListeners.iterator();
                while (it.hasNext()) {
                    ((Listener) it.next()).onAvailabilityChanged((String) msg.obj, isAvailable);
                }
            }
        }
    };
    private final ArrayList<Listener> mListeners = new ArrayList<>();
    private final IEthernetManager mService;
    private final IEthernetServiceListener.Stub mServiceListener = new IEthernetServiceListener.Stub() {
        /* class android.net.EthernetManager.AnonymousClass2 */

        @Override // android.net.IEthernetServiceListener
        public void onAvailabilityChanged(String iface, boolean isAvailable) {
            EthernetManager.this.mHandler.obtainMessage(1000, isAvailable ? 1 : 0, 0, iface).sendToTarget();
        }
    };

    public interface Listener {
        @UnsupportedAppUsage
        void onAvailabilityChanged(String str, boolean z);
    }

    public EthernetManager(Context context, IEthernetManager service) {
        this.mContext = context;
        this.mService = service;
    }

    @UnsupportedAppUsage
    public IpConfiguration getConfiguration(String iface) {
        try {
            return this.mService.getConfiguration(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void setConfiguration(String iface, IpConfiguration config) {
        try {
            this.mService.setConfiguration(iface, config);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean isAvailable() {
        if (getAvailableInterfaces() == null || getAvailableInterfaces().length <= 0) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    public boolean isAvailable(String iface) {
        try {
            return this.mService.isAvailable(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void addListener(Listener listener) {
        if (listener != null) {
            this.mListeners.add(listener);
            if (this.mListeners.size() == 1) {
                try {
                    this.mService.addListener(this.mServiceListener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        } else {
            throw new IllegalArgumentException("listener must not be null");
        }
    }

    @UnsupportedAppUsage
    public String[] getAvailableInterfaces() {
        try {
            return this.mService.getAvailableInterfaces();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    @UnsupportedAppUsage
    public void removeListener(Listener listener) {
        if (listener != null) {
            this.mListeners.remove(listener);
            if (this.mListeners.isEmpty()) {
                try {
                    this.mService.removeListener(this.mServiceListener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        } else {
            throw new IllegalArgumentException("listener must not be null");
        }
    }
}
