package ohos.media.routecore;

import ohos.app.Context;
import ohos.media.routecore.adapter.AVRouteInfoAdapter;
import ohos.media.routecore.adapter.AVRouterAdapter;

public class AVRouter {
    public static final int AVROUTE_TYPE_LIVE_AUDIO = 1;
    public static final int AVROUTE_TYPE_LIVE_VIDEO = 2;
    public static final int AVROUTE_TYPE_USER = 8388608;
    private static final AVRouter ROUTER = new AVRouter();
    private static final AVRouterAdapter ROUTER_ADAPTER = AVRouterAdapter.getInstance();

    public static AVRouter getInstance() {
        return ROUTER;
    }

    private AVRouter() {
    }

    public synchronized boolean init(Context context) {
        return ROUTER_ADAPTER.init(context);
    }

    public AVRouteInfo getDefaultAVRoute() {
        AVRouteInfoAdapter defaultAVRoute = ROUTER_ADAPTER.getDefaultAVRoute();
        if (defaultAVRoute == null) {
            return null;
        }
        return new AVRouteInfo(defaultAVRoute);
    }

    public AVRouteInfo getAVRouteAt(int i) {
        AVRouteInfoAdapter aVRouteAt = ROUTER_ADAPTER.getAVRouteAt(i);
        if (aVRouteAt == null) {
            return null;
        }
        return new AVRouteInfo(aVRouteAt);
    }

    public int getAVRouteCount() {
        return ROUTER_ADAPTER.getAVRouteCount();
    }

    public AVRouteInfo getSelectedAVRoute() {
        AVRouteInfoAdapter selectedAVRoute = ROUTER_ADAPTER.getSelectedAVRoute();
        if (selectedAVRoute == null) {
            return null;
        }
        return new AVRouteInfo(selectedAVRoute);
    }

    public AVRouteInfo getSelectedAVRoute(int i) {
        AVRouteInfoAdapter selectedAVRoute = ROUTER_ADAPTER.getSelectedAVRoute(i);
        if (selectedAVRoute == null) {
            return null;
        }
        return new AVRouteInfo(selectedAVRoute);
    }

    public void selectAVRoute(int i, AVRouteInfo aVRouteInfo) {
        if (aVRouteInfo != null) {
            ROUTER_ADAPTER.selectAVRoute(i, aVRouteInfo.adapter);
        }
    }
}
