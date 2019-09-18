package android.support.v4.media;

import android.content.ComponentName;
import android.content.Context;
import android.media.browse.MediaBrowser;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import java.util.List;

@RequiresApi(21)
class MediaBrowserCompatApi21 {
    static final String NULL_MEDIA_ITEM_ID = "android.support.v4.media.MediaBrowserCompat.NULL_MEDIA_ITEM";

    interface ConnectionCallback {
        void onConnected();

        void onConnectionFailed();

        void onConnectionSuspended();
    }

    static class ConnectionCallbackProxy<T extends ConnectionCallback> extends MediaBrowser.ConnectionCallback {
        protected final T mConnectionCallback;

        public ConnectionCallbackProxy(T connectionCallback) {
            this.mConnectionCallback = connectionCallback;
        }

        public void onConnected() {
            this.mConnectionCallback.onConnected();
        }

        public void onConnectionSuspended() {
            this.mConnectionCallback.onConnectionSuspended();
        }

        public void onConnectionFailed() {
            this.mConnectionCallback.onConnectionFailed();
        }
    }

    static class MediaItem {
        public static int getFlags(Object itemObj) {
            return ((MediaBrowser.MediaItem) itemObj).getFlags();
        }

        public static Object getDescription(Object itemObj) {
            return ((MediaBrowser.MediaItem) itemObj).getDescription();
        }

        private MediaItem() {
        }
    }

    interface SubscriptionCallback {
        void onChildrenLoaded(@NonNull String str, List<?> list);

        void onError(@NonNull String str);
    }

    static class SubscriptionCallbackProxy<T extends SubscriptionCallback> extends MediaBrowser.SubscriptionCallback {
        protected final T mSubscriptionCallback;

        public SubscriptionCallbackProxy(T callback) {
            this.mSubscriptionCallback = callback;
        }

        public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowser.MediaItem> children) {
            this.mSubscriptionCallback.onChildrenLoaded(parentId, children);
        }

        public void onError(@NonNull String parentId) {
            this.mSubscriptionCallback.onError(parentId);
        }
    }

    public static Object createConnectionCallback(ConnectionCallback callback) {
        return new ConnectionCallbackProxy(callback);
    }

    public static Object createBrowser(Context context, ComponentName serviceComponent, Object callback, Bundle rootHints) {
        return new MediaBrowser(context, serviceComponent, (MediaBrowser.ConnectionCallback) callback, rootHints);
    }

    public static void connect(Object browserObj) {
        ((MediaBrowser) browserObj).connect();
    }

    public static void disconnect(Object browserObj) {
        ((MediaBrowser) browserObj).disconnect();
    }

    public static boolean isConnected(Object browserObj) {
        return ((MediaBrowser) browserObj).isConnected();
    }

    public static ComponentName getServiceComponent(Object browserObj) {
        return ((MediaBrowser) browserObj).getServiceComponent();
    }

    public static String getRoot(Object browserObj) {
        return ((MediaBrowser) browserObj).getRoot();
    }

    public static Bundle getExtras(Object browserObj) {
        return ((MediaBrowser) browserObj).getExtras();
    }

    public static Object getSessionToken(Object browserObj) {
        return ((MediaBrowser) browserObj).getSessionToken();
    }

    public static Object createSubscriptionCallback(SubscriptionCallback callback) {
        return new SubscriptionCallbackProxy(callback);
    }

    public static void subscribe(Object browserObj, String parentId, Object subscriptionCallbackObj) {
        ((MediaBrowser) browserObj).subscribe(parentId, (MediaBrowser.SubscriptionCallback) subscriptionCallbackObj);
    }

    public static void unsubscribe(Object browserObj, String parentId) {
        ((MediaBrowser) browserObj).unsubscribe(parentId);
    }

    private MediaBrowserCompatApi21() {
    }
}
