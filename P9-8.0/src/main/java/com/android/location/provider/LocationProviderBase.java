package com.android.location.provider;

import android.location.ILocationManager;
import android.location.ILocationManager.Stub;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.location.ILocationProvider;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.util.FastPrintWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public abstract class LocationProviderBase {
    public static final String EXTRA_NO_GPS_LOCATION = "noGPSLocation";
    public static final String FUSED_PROVIDER = "fused";
    private final String TAG;
    private final IBinder mBinder;
    protected final ILocationManager mLocationManager = Stub.asInterface(ServiceManager.getService("location"));
    private final ProviderProperties mProperties;

    private final class Service extends ILocationProvider.Stub {
        /* synthetic */ Service(LocationProviderBase this$0, Service -this1) {
            this();
        }

        private Service() {
        }

        public void enable() {
            LocationProviderBase.this.onEnable();
        }

        public void disable() {
            LocationProviderBase.this.onDisable();
        }

        public void setRequest(ProviderRequest request, WorkSource ws) {
            LocationProviderBase.this.onSetRequest(new ProviderRequestUnbundled(request), ws);
        }

        public ProviderProperties getProperties() {
            return LocationProviderBase.this.mProperties;
        }

        public int getStatus(Bundle extras) {
            return LocationProviderBase.this.onGetStatus(extras);
        }

        public long getStatusUpdateTime() {
            return LocationProviderBase.this.onGetStatusUpdateTime();
        }

        public boolean sendExtraCommand(String command, Bundle extras) {
            return LocationProviderBase.this.onSendExtraCommand(command, extras);
        }

        public void dump(FileDescriptor fd, String[] args) {
            PrintWriter pw = new FastPrintWriter(new FileOutputStream(fd));
            LocationProviderBase.this.onDump(fd, pw, args);
            pw.flush();
        }
    }

    public abstract void onDisable();

    public abstract void onEnable();

    public abstract int onGetStatus(Bundle bundle);

    public abstract long onGetStatusUpdateTime();

    public abstract void onSetRequest(ProviderRequestUnbundled providerRequestUnbundled, WorkSource workSource);

    public LocationProviderBase(String tag, ProviderPropertiesUnbundled properties) {
        this.TAG = tag;
        this.mProperties = properties.getProviderProperties();
        this.mBinder = new Service(this, null);
    }

    public IBinder getBinder() {
        return this.mBinder;
    }

    public final void reportLocation(Location location) {
        try {
            this.mLocationManager.reportLocation(location, false);
        } catch (RemoteException e) {
            Log.e(this.TAG, "RemoteException", e);
        } catch (Exception e2) {
            Log.e(this.TAG, "Exception", e2);
        }
    }

    public void onDump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    public boolean onSendExtraCommand(String command, Bundle extras) {
        return false;
    }
}
