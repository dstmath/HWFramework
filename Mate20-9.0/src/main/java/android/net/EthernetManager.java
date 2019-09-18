package android.net;

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
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1000) {
                boolean z = true;
                if (msg.arg1 != 1) {
                    z = false;
                }
                boolean isAvailable = z;
                Iterator it = EthernetManager.this.mListeners.iterator();
                while (it.hasNext()) {
                    ((Listener) it.next()).onAvailabilityChanged((String) msg.obj, isAvailable);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<Listener> mListeners = new ArrayList<>();
    private final IEthernetManager mService;
    private final IEthernetServiceListener.Stub mServiceListener = new IEthernetServiceListener.Stub() {
        public void onAvailabilityChanged(String iface, boolean isAvailable) {
            EthernetManager.this.mHandler.obtainMessage(1000, isAvailable, 0, iface).sendToTarget();
        }
    };

    public interface Listener {
        void onAvailabilityChanged(String str, boolean z);
    }

    public EthernetManager(Context context, IEthernetManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public IpConfiguration getConfiguration(String iface) {
        try {
            return this.mService.getConfiguration(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setConfiguration(String iface, IpConfiguration config) {
        try {
            this.mService.setConfiguration(iface, config);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isAvailable() {
        return getAvailableInterfaces().length > 0;
    }

    public boolean isAvailable(String iface) {
        try {
            return this.mService.isAvailable(iface);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public String[] getAvailableInterfaces() {
        try {
            return this.mService.getAvailableInterfaces();
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

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
