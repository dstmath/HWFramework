package ohos.media.routecore.adapter;

import android.media.MediaRouter;

public class AVRouteInfoAdapter {
    public static final int AVDEVICE_TYPE_BLUETOOTH = 1;
    public static final int AVDEVICE_TYPE_UNKNOWN = 0;
    private static final int TYPE_UNKNOWN = 0;
    MediaRouter.RouteInfo routeInfo;

    AVRouteInfoAdapter(MediaRouter.RouteInfo routeInfo2) {
        this.routeInfo = routeInfo2;
    }

    public int getAVDeviceType() {
        MediaRouter.RouteInfo routeInfo2 = this.routeInfo;
        if (routeInfo2 != null && routeInfo2.getDeviceType() == 3) {
            return 1;
        }
        return 0;
    }

    public int getAVRouteTypes() {
        MediaRouter.RouteInfo routeInfo2 = this.routeInfo;
        if (routeInfo2 == null) {
            return 0;
        }
        return routeInfo2.getSupportedTypes();
    }

    public CharSequence getName() {
        MediaRouter.RouteInfo routeInfo2 = this.routeInfo;
        if (routeInfo2 == null) {
            return null;
        }
        return routeInfo2.getName();
    }
}
