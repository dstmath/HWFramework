package com.huawei.android.pushselfshow.richpush.html.a.a;

import android.location.LocationManager;
import com.huawei.android.pushselfshow.richpush.html.a.i;

public class a extends c {
    public a(LocationManager locationManager, i iVar) {
        super(locationManager, iVar, "GPSListener");
    }

    public void a(long j, float f) {
        if (!this.b) {
            if (this.a.getProvider("gps") == null) {
                a(com.huawei.android.pushselfshow.richpush.html.api.d.a.POSITION_UNAVAILABLE_GPS);
                return;
            }
            this.b = true;
            this.a.requestLocationUpdates("gps", j, f, this);
        }
    }
}
