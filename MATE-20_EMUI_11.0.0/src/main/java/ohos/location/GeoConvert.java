package ohos.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ohos.hiviewdfx.HiLogLabel;
import ohos.location.common.LBSLog;

public final class GeoConvert {
    private static final HiLogLabel LABEL = new HiLogLabel(3, LBSLog.LOCATOR_LOG_ID, "GeoConvert");
    private GeoConvertAdapter geoConvertAdapter;
    private Locale mlocale;

    public GeoConvert(Locale locale) throws IllegalArgumentException {
        if (locale != null) {
            this.mlocale = locale;
            this.geoConvertAdapter = GeoConvertAdapter.getInstance();
            return;
        }
        throw new IllegalArgumentException("locale == null");
    }

    public GeoConvert() {
        this(Locale.getDefault());
    }

    public boolean isGeoAvailable() {
        return this.geoConvertAdapter.isGeoAvailable();
    }

    public List<GeoAddress> getAddressFromLocation(double d, double d2, int i) throws IOException {
        if (d < -90.0d || d > 90.0d) {
            throw new IllegalArgumentException("longitude is illegal, should between -90.0 to 90.0");
        } else if (d2 < -180.0d || d2 > 180.0d) {
            throw new IllegalArgumentException("longitude is illegal, should between -180.0 to 180.0");
        } else {
            ArrayList arrayList = new ArrayList();
            String addressFromLocation = this.geoConvertAdapter.getAddressFromLocation(d, d2, i, this.mlocale, arrayList);
            if (addressFromLocation == null) {
                return arrayList;
            }
            throw new IOException(addressFromLocation);
        }
    }

    public List<GeoAddress> getAddressFromLocationName(String str, int i) throws IOException {
        if (str != null) {
            ArrayList arrayList = new ArrayList();
            String addressFromLocationName = this.geoConvertAdapter.getAddressFromLocationName(str, i, this.mlocale, arrayList, 0.0d, 0.0d, 0.0d, 0.0d);
            if (addressFromLocationName == null) {
                return arrayList;
            }
            throw new IOException(addressFromLocationName);
        }
        throw new IllegalArgumentException("input any description of the location");
    }

    public List<GeoAddress> getAddressFromLocationName(String str, double d, double d2, double d3, double d4, int i) throws IOException {
        if (str == null) {
            throw new IllegalArgumentException("locationName == null");
        } else if (d < -90.0d || d > 90.0d) {
            throw new IllegalArgumentException("minLatitude is illegal, should between -90.0 to 90.0");
        } else if (d2 < -180.0d || d2 > 180.0d) {
            throw new IllegalArgumentException("minLongitude is illegal, should between -180.0 to 180.0");
        } else if (d3 < -90.0d || d3 > 90.0d) {
            throw new IllegalArgumentException("maxLatitude is illegal, should between -90.0 to 90.0");
        } else if (d4 < -180.0d || d4 > 180.0d) {
            throw new IllegalArgumentException("maxLongitude is illegal, should between -180.0 to 180.0");
        } else {
            ArrayList arrayList = new ArrayList();
            String addressFromLocationName = this.geoConvertAdapter.getAddressFromLocationName(str, i, this.mlocale, arrayList, d, d2, d3, d4);
            if (addressFromLocationName == null) {
                return arrayList;
            }
            throw new IOException(addressFromLocationName);
        }
    }
}
