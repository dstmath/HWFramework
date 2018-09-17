package android.app;

import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.SimpleCallback;
import android.util.Log;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import java.lang.ref.WeakReference;

public class MediaRouteActionProvider extends ActionProvider {
    private static final String TAG = "MediaRouteActionProvider";
    private MediaRouteButton mButton;
    private final MediaRouterCallback mCallback = new MediaRouterCallback(this);
    private final Context mContext;
    private OnClickListener mExtendedSettingsListener;
    private int mRouteTypes;
    private final MediaRouter mRouter;

    private static class MediaRouterCallback extends SimpleCallback {
        private final WeakReference<MediaRouteActionProvider> mProviderWeak;

        public MediaRouterCallback(MediaRouteActionProvider provider) {
            this.mProviderWeak = new WeakReference(provider);
        }

        public void onRouteAdded(MediaRouter router, RouteInfo info) {
            refreshRoute(router);
        }

        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
            refreshRoute(router);
        }

        public void onRouteChanged(MediaRouter router, RouteInfo info) {
            refreshRoute(router);
        }

        private void refreshRoute(MediaRouter router) {
            MediaRouteActionProvider provider = (MediaRouteActionProvider) this.mProviderWeak.get();
            if (provider != null) {
                provider.refreshRoute();
            } else {
                router.removeCallback(this);
            }
        }
    }

    public MediaRouteActionProvider(Context context) {
        super(context);
        this.mContext = context;
        this.mRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        setRouteTypes(1);
    }

    public void setRouteTypes(int types) {
        if (this.mRouteTypes != types) {
            if (this.mRouteTypes != 0) {
                this.mRouter.removeCallback(this.mCallback);
            }
            this.mRouteTypes = types;
            if (types != 0) {
                this.mRouter.addCallback(types, this.mCallback, 8);
            }
            refreshRoute();
            if (this.mButton != null) {
                this.mButton.setRouteTypes(this.mRouteTypes);
            }
        }
    }

    public void setExtendedSettingsClickListener(OnClickListener listener) {
        this.mExtendedSettingsListener = listener;
        if (this.mButton != null) {
            this.mButton.setExtendedSettingsClickListener(listener);
        }
    }

    public View onCreateActionView() {
        throw new UnsupportedOperationException("Use onCreateActionView(MenuItem) instead.");
    }

    public View onCreateActionView(MenuItem item) {
        if (this.mButton != null) {
            Log.e(TAG, "onCreateActionView: this ActionProvider is already associated with a menu item. Don't reuse MediaRouteActionProvider instances! Abandoning the old one...");
        }
        this.mButton = new MediaRouteButton(this.mContext);
        this.mButton.setRouteTypes(this.mRouteTypes);
        this.mButton.setExtendedSettingsClickListener(this.mExtendedSettingsListener);
        this.mButton.setLayoutParams(new LayoutParams(-2, -1));
        return this.mButton;
    }

    public boolean onPerformDefaultAction() {
        if (this.mButton != null) {
            return this.mButton.showDialogInternal();
        }
        return false;
    }

    public boolean overridesItemVisibility() {
        return true;
    }

    public boolean isVisible() {
        return this.mRouter.isRouteAvailable(this.mRouteTypes, 1);
    }

    private void refreshRoute() {
        refreshVisibility();
    }
}
