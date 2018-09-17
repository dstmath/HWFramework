package android.companion;

import android.os.Parcelable;

public interface DeviceFilter<D extends Parcelable> extends Parcelable {
    public static final int MEDIUM_TYPE_BLUETOOTH = 0;
    public static final int MEDIUM_TYPE_BLUETOOTH_LE = 1;
    public static final int MEDIUM_TYPE_WIFI = 2;

    String getDeviceDisplayName(D d);

    int getMediumType();

    boolean matches(D d);

    static <D extends Parcelable> boolean matches(DeviceFilter<D> filter, D device) {
        return filter != null ? filter.matches(device) : true;
    }
}
