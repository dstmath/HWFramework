package com.android.server;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.location.Country;
import android.location.CountryListener;
import android.location.ICountryDetector;
import android.location.ICountryListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.server.location.ComprehensiveCountryDetector;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class CountryDetectorService extends ICountryDetector.Stub implements Runnable {
    private static final boolean DEBUG = false;
    private static final String TAG = "CountryDetector";
    private final Context mContext;
    /* access modifiers changed from: private */
    public ComprehensiveCountryDetector mCountryDetector;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private CountryListener mLocationBasedDetectorListener;
    private final HashMap<IBinder, Receiver> mReceivers = new HashMap<>();
    private boolean mSystemReady;

    private final class Receiver implements IBinder.DeathRecipient {
        private final IBinder mKey;
        private final ICountryListener mListener;

        public Receiver(ICountryListener listener) {
            this.mListener = listener;
            this.mKey = listener.asBinder();
        }

        public void binderDied() {
            CountryDetectorService.this.removeListener(this.mKey);
        }

        public boolean equals(Object otherObj) {
            if (otherObj instanceof Receiver) {
                return this.mKey.equals(((Receiver) otherObj).mKey);
            }
            return false;
        }

        public int hashCode() {
            return this.mKey.hashCode();
        }

        public ICountryListener getListener() {
            return this.mListener;
        }
    }

    public CountryDetectorService(Context context) {
        this.mContext = context;
    }

    public Country detectCountry() {
        IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
        if (manager != null) {
            manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.COUNTRYDETECTOR_DETECTCOUNTRY);
        }
        if (!this.mSystemReady) {
            return null;
        }
        return this.mCountryDetector.detectCountry();
    }

    public void addCountryListener(ICountryListener listener) throws RemoteException {
        if (this.mSystemReady) {
            addListener(listener);
            return;
        }
        throw new RemoteException();
    }

    public void removeCountryListener(ICountryListener listener) throws RemoteException {
        if (this.mSystemReady) {
            removeListener(listener.asBinder());
            return;
        }
        throw new RemoteException();
    }

    private void addListener(ICountryListener listener) {
        synchronized (this.mReceivers) {
            Receiver r = new Receiver(listener);
            try {
                listener.asBinder().linkToDeath(r, 0);
                this.mReceivers.put(listener.asBinder(), r);
                if (this.mReceivers.size() == 1) {
                    Slog.d(TAG, "The first listener is added");
                    setCountryListener(this.mLocationBasedDetectorListener);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "linkToDeath failed:", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeListener(IBinder key) {
        synchronized (this.mReceivers) {
            this.mReceivers.remove(key);
            if (this.mReceivers.isEmpty()) {
                setCountryListener(null);
                Slog.d(TAG, "No listener is left");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyReceivers(Country country) {
        synchronized (this.mReceivers) {
            for (Receiver receiver : this.mReceivers.values()) {
                try {
                    receiver.getListener().onCountryDetected(country);
                } catch (RemoteException e) {
                    Slog.e(TAG, "notifyReceivers failed:", e);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void systemRunning() {
        BackgroundThread.getHandler().post(this);
    }

    private void initialize() {
        this.mCountryDetector = new ComprehensiveCountryDetector(this.mContext);
        this.mLocationBasedDetectorListener = new CountryListener() {
            public void onCountryDetected(final Country country) {
                CountryDetectorService.this.mHandler.post(new Runnable() {
                    public void run() {
                        CountryDetectorService.this.notifyReceivers(country);
                    }
                });
            }
        };
    }

    public void run() {
        this.mHandler = new Handler();
        initialize();
        this.mSystemReady = true;
    }

    /* access modifiers changed from: protected */
    public void setCountryListener(final CountryListener listener) {
        this.mHandler.post(new Runnable() {
            public void run() {
                CountryDetectorService.this.mCountryDetector.setCountryListener(listener);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public boolean isSystemReady() {
        return this.mSystemReady;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, fout)) {
        }
    }
}
