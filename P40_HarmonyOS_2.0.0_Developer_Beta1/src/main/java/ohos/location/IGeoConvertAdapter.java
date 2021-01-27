package ohos.location;

import java.util.List;
import java.util.Locale;
import ohos.annotation.SystemApi;
import ohos.rpc.IRemoteBroker;

@SystemApi
public interface IGeoConvertAdapter extends IRemoteBroker {
    String getAddressFromLocation(double d, double d2, int i, Locale locale, List<GeoAddress> list);

    String getAddressFromLocationName(String str, int i, Locale locale, List<GeoAddress> list, double d, double d2, double d3, double d4);

    boolean isGeoAvailable();
}
