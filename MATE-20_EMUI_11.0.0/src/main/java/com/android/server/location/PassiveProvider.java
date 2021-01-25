package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.WorkSource;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.location.AbstractLocationProvider;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class PassiveProvider extends AbstractLocationProvider {
    private static final ProviderProperties PROPERTIES = new ProviderProperties(false, false, false, false, false, false, false, 1, 2);
    private boolean mReportLocation = false;

    public PassiveProvider(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager) {
        super(context, locationProviderManager);
        setProperties(PROPERTIES);
        setEnabled(true);
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void setRequest(ProviderRequest request, WorkSource source) {
        this.mReportLocation = request.reportLocation;
    }

    public void updateLocation(Location location) {
        if (this.mReportLocation) {
            reportLocation(location);
        }
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void sendExtraCommand(String command, Bundle extras) {
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println(" report location=" + this.mReportLocation);
    }
}
