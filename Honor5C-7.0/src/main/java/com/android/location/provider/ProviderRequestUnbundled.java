package com.android.location.provider;

import android.location.LocationRequest;
import com.android.internal.location.ProviderRequest;
import java.util.ArrayList;
import java.util.List;

public final class ProviderRequestUnbundled {
    private final ProviderRequest mRequest;

    public ProviderRequestUnbundled(ProviderRequest request) {
        this.mRequest = request;
    }

    public boolean getReportLocation() {
        return this.mRequest.reportLocation;
    }

    public long getInterval() {
        return this.mRequest.interval;
    }

    public List<LocationRequestUnbundled> getLocationRequests() {
        List<LocationRequestUnbundled> result = new ArrayList(this.mRequest.locationRequests.size());
        for (LocationRequest r : this.mRequest.locationRequests) {
            result.add(new LocationRequestUnbundled(r));
        }
        return result;
    }

    public String toString() {
        return this.mRequest.toString();
    }
}
