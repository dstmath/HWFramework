package com.android.location.provider;

import android.location.ILocationManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.location.ILocationProvider;
import com.android.internal.location.ILocationProviderManager;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class LocationProviderBase {
    public static final String EXTRA_NO_GPS_LOCATION = "noGPSLocation";
    public static final String FUSED_PROVIDER = "fused";
    private final ArrayList<String> mAdditionalProviderPackages;
    private final IBinder mBinder = new Service();
    private volatile boolean mEnabled;
    @Deprecated
    protected final ILocationManager mLocationManager = ILocationManager.Stub.asInterface(ServiceManager.getService("location"));
    private volatile ILocationProviderManager mManager = null;
    private volatile ProviderProperties mProperties;
    private final String mTag;

    /* access modifiers changed from: protected */
    public abstract void onSetRequest(ProviderRequestUnbundled providerRequestUnbundled, WorkSource workSource);

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.location.provider.LocationProviderBase$Service, android.os.IBinder] */
    public LocationProviderBase(String tag, ProviderPropertiesUnbundled properties) {
        this.mTag = tag;
        this.mProperties = properties.getProviderProperties();
        this.mEnabled = true;
        this.mAdditionalProviderPackages = new ArrayList<>(0);
    }

    public IBinder getBinder() {
        return this.mBinder;
    }

    public void setEnabled(boolean enabled) {
        synchronized (this.mBinder) {
            if (this.mEnabled != enabled) {
                this.mEnabled = enabled;
            } else {
                return;
            }
        }
        ILocationProviderManager manager = this.mManager;
        if (manager != null) {
            try {
                manager.onSetEnabled(this.mEnabled);
            } catch (RemoteException | RuntimeException e) {
                Log.w(this.mTag, e);
            }
        }
    }

    public void setProperties(ProviderPropertiesUnbundled properties) {
        synchronized (this.mBinder) {
            this.mProperties = properties.getProviderProperties();
        }
        ILocationProviderManager manager = this.mManager;
        if (manager != null) {
            try {
                manager.onSetProperties(this.mProperties);
            } catch (RemoteException | RuntimeException e) {
                Log.w(this.mTag, e);
            }
        }
    }

    public void setAdditionalProviderPackages(List<String> packageNames) {
        synchronized (this.mBinder) {
            this.mAdditionalProviderPackages.clear();
            this.mAdditionalProviderPackages.addAll(packageNames);
        }
        ILocationProviderManager manager = this.mManager;
        if (manager != null) {
            try {
                manager.onSetAdditionalProviderPackages(this.mAdditionalProviderPackages);
            } catch (RemoteException | RuntimeException e) {
                Log.w(this.mTag, e);
            }
        }
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public void reportLocation(Location location) {
        ILocationProviderManager manager = this.mManager;
        if (manager != null) {
            try {
                manager.onReportLocation(location);
            } catch (RemoteException | RuntimeException e) {
                Log.w(this.mTag, e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onInit() {
        onEnable();
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void onEnable() {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void onDisable() {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void onDump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int onGetStatus(Bundle extras) {
        return 2;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public long onGetStatusUpdateTime() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean onSendExtraCommand(String command, Bundle extras) {
        return false;
    }

    private final class Service extends ILocationProvider.Stub {
        private Service() {
        }

        public void setLocationProviderManager(ILocationProviderManager manager) {
            synchronized (LocationProviderBase.this.mBinder) {
                try {
                    if (!LocationProviderBase.this.mAdditionalProviderPackages.isEmpty()) {
                        manager.onSetAdditionalProviderPackages(LocationProviderBase.this.mAdditionalProviderPackages);
                    }
                    manager.onSetProperties(LocationProviderBase.this.mProperties);
                    manager.onSetEnabled(LocationProviderBase.this.mEnabled);
                } catch (RemoteException e) {
                    Log.w(LocationProviderBase.this.mTag, e);
                }
                LocationProviderBase.this.mManager = manager;
            }
            LocationProviderBase.this.onInit();
        }

        public void setRequest(ProviderRequest request, WorkSource ws) {
            LocationProviderBase.this.onSetRequest(new ProviderRequestUnbundled(request), ws);
        }

        public int getStatus(Bundle extras) {
            return LocationProviderBase.this.onGetStatus(extras);
        }

        public long getStatusUpdateTime() {
            return LocationProviderBase.this.onGetStatusUpdateTime();
        }

        public void sendExtraCommand(String command, Bundle extras) {
            LocationProviderBase.this.onSendExtraCommand(command, extras);
        }
    }
}
