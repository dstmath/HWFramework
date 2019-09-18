package android.media;

import android.content.Context;
import android.media.MediaController2;
import android.media.update.ApiLoader;
import android.media.update.MediaBrowser2Provider;
import android.os.Bundle;
import java.util.List;
import java.util.concurrent.Executor;

public class MediaBrowser2 extends MediaController2 {
    private final MediaBrowser2Provider mProvider = ((MediaBrowser2Provider) getProvider());

    public static class BrowserCallback extends MediaController2.ControllerCallback {
        public void onGetLibraryRootDone(MediaBrowser2 browser, Bundle rootHints, String rootMediaId, Bundle rootExtra) {
        }

        public void onChildrenChanged(MediaBrowser2 browser, String parentId, int itemCount, Bundle extras) {
        }

        public void onGetChildrenDone(MediaBrowser2 browser, String parentId, int page, int pageSize, List<MediaItem2> list, Bundle extras) {
        }

        public void onGetItemDone(MediaBrowser2 browser, String mediaId, MediaItem2 result) {
        }

        public void onSearchResultChanged(MediaBrowser2 browser, String query, int itemCount, Bundle extras) {
        }

        public void onGetSearchResultDone(MediaBrowser2 browser, String query, int page, int pageSize, List<MediaItem2> list, Bundle extras) {
        }
    }

    public MediaBrowser2(Context context, SessionToken2 token, Executor executor, BrowserCallback callback) {
        super(context, token, executor, callback);
    }

    /* access modifiers changed from: package-private */
    public MediaBrowser2Provider createProvider(Context context, SessionToken2 token, Executor executor, MediaController2.ControllerCallback callback) {
        return ApiLoader.getProvider().createMediaBrowser2(context, this, token, executor, (BrowserCallback) callback);
    }

    public void getLibraryRoot(Bundle rootHints) {
        this.mProvider.getLibraryRoot_impl(rootHints);
    }

    public void subscribe(String parentId, Bundle extras) {
        this.mProvider.subscribe_impl(parentId, extras);
    }

    public void unsubscribe(String parentId) {
        this.mProvider.unsubscribe_impl(parentId);
    }

    public void getChildren(String parentId, int page, int pageSize, Bundle extras) {
        this.mProvider.getChildren_impl(parentId, page, pageSize, extras);
    }

    public void getItem(String mediaId) {
        this.mProvider.getItem_impl(mediaId);
    }

    public void search(String query, Bundle extras) {
        this.mProvider.search_impl(query, extras);
    }

    public void getSearchResult(String query, int page, int pageSize, Bundle extras) {
        this.mProvider.getSearchResult_impl(query, page, pageSize, extras);
    }
}
