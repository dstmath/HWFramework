package com.android.server.location;

import android.location.ILocationManager;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import android.util.PrintWriterPrinter;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class MockProvider implements LocationProviderInterface {
    private static final String TAG = "MockProvider";
    private boolean mEnabled;
    private final Bundle mExtras;
    private boolean mHasLocation;
    private boolean mHasStatus;
    private final Location mLocation;
    private final ILocationManager mLocationManager;
    private final String mName;
    private final ProviderProperties mProperties;
    private int mStatus;
    private long mStatusUpdateTime;

    public MockProvider(String name, ILocationManager locationManager, ProviderProperties properties) {
        this.mExtras = new Bundle();
        if (properties == null) {
            throw new NullPointerException("properties is null");
        }
        this.mName = name;
        this.mLocationManager = locationManager;
        this.mProperties = properties;
        this.mLocation = new Location(name);
    }

    public String getName() {
        return this.mName;
    }

    public ProviderProperties getProperties() {
        return this.mProperties;
    }

    public void disable() {
        this.mEnabled = false;
    }

    public void enable() {
        this.mEnabled = true;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    public int getStatus(Bundle extras) {
        if (!this.mHasStatus) {
            return 2;
        }
        extras.clear();
        extras.putAll(this.mExtras);
        return this.mStatus;
    }

    public long getStatusUpdateTime() {
        return this.mStatusUpdateTime;
    }

    public void setLocation(Location l) {
        this.mLocation.set(l);
        this.mHasLocation = true;
        if (this.mEnabled) {
            try {
                this.mLocationManager.reportLocation(this.mLocation, false);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling reportLocation");
            }
        }
    }

    public void clearLocation() {
        this.mHasLocation = false;
    }

    public void setStatus(int status, Bundle extras, long updateTime) {
        this.mStatus = status;
        this.mStatusUpdateTime = updateTime;
        this.mExtras.clear();
        if (extras != null) {
            this.mExtras.putAll(extras);
        }
        this.mHasStatus = true;
    }

    public void clearStatus() {
        this.mHasStatus = false;
        this.mStatusUpdateTime = 0;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        dump(pw, "");
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + this.mName);
        pw.println(prefix + "mHasLocation=" + this.mHasLocation);
        pw.println(prefix + "mLocation:");
        this.mLocation.dump(new PrintWriterPrinter(pw), prefix + "  ");
        pw.println(prefix + "mHasStatus=" + this.mHasStatus);
        pw.println(prefix + "mStatus=" + this.mStatus);
        pw.println(prefix + "mStatusUpdateTime=" + this.mStatusUpdateTime);
        pw.println(prefix + "mExtras=" + this.mExtras);
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        return false;
    }
}
