package android.app;

import android.content.Context;
import android.media.MediaRouter;
import android.util.Log;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;

public class MediaRouteActionProvider extends ActionProvider {
    private static final String TAG = "MediaRouteActionProvider";
    private MediaRouteButton mButton;
    private final MediaRouterCallback mCallback = new MediaRouterCallback(this);
    private final Context mContext;
    private View.OnClickListener mExtendedSettingsListener;
    private int mRouteTypes;
    private final MediaRouter mRouter;

    public MediaRouteActionProvider(Context context) {
        super(context);
        this.mContext = context;
        this.mRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        setRouteTypes(1);
    }

    public void setRouteTypes(int types) {
        int i = this.mRouteTypes;
        if (i != types) {
            if (i != 0) {
                this.mRouter.removeCallback(this.mCallback);
            }
            this.mRouteTypes = types;
            if (types != 0) {
                this.mRouter.addCallback(types, this.mCallback, 8);
            }
            refreshRoute();
            MediaRouteButton mediaRouteButton = this.mButton;
            if (mediaRouteButton != null) {
                mediaRouteButton.setRouteTypes(this.mRouteTypes);
            }
        }
    }

    public void setExtendedSettingsClickListener(View.OnClickListener listener) {
        this.mExtendedSettingsListener = listener;
        MediaRouteButton mediaRouteButton = this.mButton;
        if (mediaRouteButton != null) {
            mediaRouteButton.setExtendedSettingsClickListener(listener);
        }
    }

    @Override // android.view.ActionProvider
    public View onCreateActionView() {
        throw new UnsupportedOperationException("Use onCreateActionView(MenuItem) instead.");
    }

    @Override // android.view.ActionProvider
    public View onCreateActionView(MenuItem item) {
        if (this.mButton != null) {
            Log.e(TAG, "onCreateActionView: this ActionProvider is already associated with a menu item. Don't reuse MediaRouteActionProvider instances! Abandoning the old one...");
        }
        this.mButton = new MediaRouteButton(this.mContext);
        this.mButton.setRouteTypes(this.mRouteTypes);
        this.mButton.setExtendedSettingsClickListener(this.mExtendedSettingsListener);
        this.mButton.setLayoutParams(new ViewGroup.LayoutParams(-2, -1));
        return this.mButton;
    }

    @Override // android.view.ActionProvider
    public boolean onPerformDefaultAction() {
        MediaRouteButton mediaRouteButton = this.mButton;
        if (mediaRouteButton != null) {
            return mediaRouteButton.showDialogInternal();
        }
        return false;
    }

    @Override // android.view.ActionProvider
    public boolean overridesItemVisibility() {
        return true;
    }

    @Override // android.view.ActionProvider
    public boolean isVisible() {
        return this.mRouter.isRouteAvailable(this.mRouteTypes, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshRoute() {
        refreshVisibility();
    }

    /* access modifiers changed from: private */
    public static class MediaRouterCallback extends MediaRouter.SimpleCallback {
        private final WeakReference<MediaRouteActionProvider> mProviderWeak;

        public MediaRouterCallback(MediaRouteActionProvider provider) {
            this.mProviderWeak = new WeakReference<>(provider);
        }

        @Override // android.media.MediaRouter.Callback, android.media.MediaRouter.SimpleCallback
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {
            refreshRoute(router);
        }

        @Override // android.media.MediaRouter.Callback, android.media.MediaRouter.SimpleCallback
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo info) {
            refreshRoute(router);
        }

        @Override // android.media.MediaRouter.Callback, android.media.MediaRouter.SimpleCallback
        public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo info) {
            refreshRoute(router);
        }

        private void refreshRoute(MediaRouter router) {
            MediaRouteActionProvider provider = this.mProviderWeak.get();
            if (provider != null) {
                provider.refreshRoute();
            } else {
                router.removeCallback(this);
            }
        }
    }
}
