package ohos.media.routecore.adapter;

import android.media.MediaRouter;
import ohos.app.Context;

public class AVRouterAdapter {
    private static final int AVROUTE_TYPE_LIVE_AUDIO = 1;
    private static final int AVROUTE_TYPE_LIVE_VIDEO = 2;
    private static final int AVROUTE_TYPE_REMOTE_DISPLAY = 4;
    private static final int AVROUTE_TYPE_USER = 8388608;
    private static final AVRouterAdapter ROUTER = new AVRouterAdapter();
    private static final int ROUTE_TYPE_ANY = 8388615;
    private MediaRouter mediaRouter;

    private AVRouterAdapter() {
    }

    public static AVRouterAdapter getInstance() {
        return ROUTER;
    }

    public boolean init(Context context) {
        if (this.mediaRouter == null) {
            if (context == null) {
                return false;
            }
            Object hostContext = context.getHostContext();
            if (!(hostContext instanceof android.content.Context)) {
                return false;
            }
            this.mediaRouter = new MediaRouter((android.content.Context) hostContext);
        }
        return true;
    }

    public MediaRouter getMediaRouter() {
        return this.mediaRouter;
    }

    public AVRouteInfoAdapter getDefaultAVRoute() {
        MediaRouter mediaRouter2 = this.mediaRouter;
        if (mediaRouter2 == null) {
            return null;
        }
        return new AVRouteInfoAdapter(mediaRouter2.getDefaultRoute());
    }

    public AVRouteInfoAdapter getAVRouteAt(int i) {
        MediaRouter mediaRouter2 = this.mediaRouter;
        if (mediaRouter2 == null) {
            return null;
        }
        return new AVRouteInfoAdapter(mediaRouter2.getRouteAt(i));
    }

    public int getAVRouteCount() {
        MediaRouter mediaRouter2 = this.mediaRouter;
        if (mediaRouter2 == null) {
            return 0;
        }
        return mediaRouter2.getRouteCount();
    }

    public AVRouteInfoAdapter getSelectedAVRoute() {
        return getSelectedAVRoute(ROUTE_TYPE_ANY);
    }

    public AVRouteInfoAdapter getSelectedAVRoute(int i) {
        MediaRouter mediaRouter2 = this.mediaRouter;
        if (mediaRouter2 == null) {
            return null;
        }
        return new AVRouteInfoAdapter(mediaRouter2.getSelectedRoute(i));
    }

    public void selectAVRoute(int i, AVRouteInfoAdapter aVRouteInfoAdapter) {
        MediaRouter mediaRouter2 = this.mediaRouter;
        if (mediaRouter2 != null) {
            mediaRouter2.selectRoute(i, aVRouteInfoAdapter.routeInfo);
        }
    }
}
