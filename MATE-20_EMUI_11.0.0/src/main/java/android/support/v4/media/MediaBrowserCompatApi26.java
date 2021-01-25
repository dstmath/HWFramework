package android.support.v4.media;

import android.media.browse.MediaBrowser;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaBrowserCompatApi21;
import java.util.List;

@RequiresApi(26)
class MediaBrowserCompatApi26 {

    interface SubscriptionCallback extends MediaBrowserCompatApi21.SubscriptionCallback {
        void onChildrenLoaded(@NonNull String str, List<?> list, @NonNull Bundle bundle);

        void onError(@NonNull String str, @NonNull Bundle bundle);
    }

    static Object createSubscriptionCallback(SubscriptionCallback callback) {
        return new SubscriptionCallbackProxy(callback);
    }

    public static void subscribe(Object browserObj, String parentId, Bundle options, Object subscriptionCallbackObj) {
        ((MediaBrowser) browserObj).subscribe(parentId, options, (MediaBrowser.SubscriptionCallback) subscriptionCallbackObj);
    }

    public static void unsubscribe(Object browserObj, String parentId, Object subscriptionCallbackObj) {
        ((MediaBrowser) browserObj).unsubscribe(parentId, (MediaBrowser.SubscriptionCallback) subscriptionCallbackObj);
    }

    static class SubscriptionCallbackProxy<T extends SubscriptionCallback> extends MediaBrowserCompatApi21.SubscriptionCallbackProxy<T> {
        SubscriptionCallbackProxy(T callback) {
            super(callback);
        }

        @Override // android.media.browse.MediaBrowser.SubscriptionCallback
        public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowser.MediaItem> children, @NonNull Bundle options) {
            ((SubscriptionCallback) this.mSubscriptionCallback).onChildrenLoaded(parentId, children, options);
        }

        @Override // android.media.browse.MediaBrowser.SubscriptionCallback
        public void onError(@NonNull String parentId, @NonNull Bundle options) {
            ((SubscriptionCallback) this.mSubscriptionCallback).onError(parentId, options);
        }
    }

    private MediaBrowserCompatApi26() {
    }
}
