package ohos.media.routecore;

import ohos.media.routecore.adapter.AVRouteInfoAdapter;

public class AVRouteInfo {
    public static final int AVDEVICE_TYPE_BLUETOOTH = 1;
    public static final int AVDEVICE_TYPE_UNKNOWN = 0;
    private static final int TYPE_UNKNOWN = 0;
    AVRouteInfoAdapter adapter;

    AVRouteInfo(Object obj) {
        if (obj instanceof AVRouteInfoAdapter) {
            this.adapter = (AVRouteInfoAdapter) obj;
        }
    }

    public int getAVDeviceType() {
        AVRouteInfoAdapter aVRouteInfoAdapter = this.adapter;
        if (aVRouteInfoAdapter == null) {
            return 0;
        }
        return aVRouteInfoAdapter.getAVDeviceType();
    }

    public int getAVRouteTypes() {
        AVRouteInfoAdapter aVRouteInfoAdapter = this.adapter;
        if (aVRouteInfoAdapter == null) {
            return 0;
        }
        return aVRouteInfoAdapter.getAVRouteTypes();
    }

    public CharSequence getName() {
        AVRouteInfoAdapter aVRouteInfoAdapter = this.adapter;
        if (aVRouteInfoAdapter == null) {
            return null;
        }
        return aVRouteInfoAdapter.getName();
    }
}
