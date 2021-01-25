package android.support.v4.media;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaController2;
import android.util.Log;
import java.util.List;
import java.util.concurrent.Executor;

public class MediaBrowser2 extends MediaController2 {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final String TAG = "MediaBrowser2";

    /* access modifiers changed from: package-private */
    public interface SupportLibraryImpl extends MediaController2.SupportLibraryImpl {
        void getChildren(@NonNull String str, int i, int i2, @Nullable Bundle bundle);

        void getItem(@NonNull String str);

        void getLibraryRoot(@Nullable Bundle bundle);

        void getSearchResult(@NonNull String str, int i, int i2, @Nullable Bundle bundle);

        void search(@NonNull String str, @Nullable Bundle bundle);

        void subscribe(@NonNull String str, @Nullable Bundle bundle);

        void unsubscribe(@NonNull String str);
    }

    public static class BrowserCallback extends MediaController2.ControllerCallback {
        public void onGetLibraryRootDone(@NonNull MediaBrowser2 browser, @Nullable Bundle rootHints, @Nullable String rootMediaId, @Nullable Bundle rootExtra) {
        }

        public void onChildrenChanged(@NonNull MediaBrowser2 browser, @NonNull String parentId, int itemCount, @Nullable Bundle extras) {
        }

        public void onGetChildrenDone(@NonNull MediaBrowser2 browser, @NonNull String parentId, int page, int pageSize, @Nullable List<MediaItem2> list, @Nullable Bundle extras) {
        }

        public void onGetItemDone(@NonNull MediaBrowser2 browser, @NonNull String mediaId, @Nullable MediaItem2 result) {
        }

        public void onSearchResultChanged(@NonNull MediaBrowser2 browser, @NonNull String query, int itemCount, @Nullable Bundle extras) {
        }

        public void onGetSearchResultDone(@NonNull MediaBrowser2 browser, @NonNull String query, int page, int pageSize, @Nullable List<MediaItem2> list, @Nullable Bundle extras) {
        }
    }

    public MediaBrowser2(@NonNull Context context, @NonNull SessionToken2 token, @NonNull Executor executor, @NonNull BrowserCallback callback) {
        super(context, token, executor, callback);
    }

    /* access modifiers changed from: package-private */
    @Override // android.support.v4.media.MediaController2
    public SupportLibraryImpl createImpl(@NonNull Context context, @NonNull SessionToken2 token, @NonNull Executor executor, @NonNull MediaController2.ControllerCallback callback) {
        if (token.isLegacySession()) {
            return new MediaBrowser2ImplLegacy(context, this, token, executor, (BrowserCallback) callback);
        }
        return new MediaBrowser2ImplBase(context, this, token, executor, (BrowserCallback) callback);
    }

    /* access modifiers changed from: package-private */
    @Override // android.support.v4.media.MediaController2
    public SupportLibraryImpl getImpl() {
        return (SupportLibraryImpl) super.getImpl();
    }

    /* access modifiers changed from: package-private */
    @Override // android.support.v4.media.MediaController2
    public BrowserCallback getCallback() {
        return (BrowserCallback) super.getCallback();
    }

    public void getLibraryRoot(@Nullable Bundle extras) {
        getImpl().getLibraryRoot(extras);
    }

    public void subscribe(@NonNull String parentId, @Nullable Bundle extras) {
        getImpl().subscribe(parentId, extras);
    }

    public void unsubscribe(@NonNull String parentId) {
        getImpl().unsubscribe(parentId);
    }

    public void getChildren(@NonNull String parentId, int page, int pageSize, @Nullable Bundle extras) {
        getImpl().getChildren(parentId, page, pageSize, extras);
    }

    public void getItem(@NonNull String mediaId) {
        getImpl().getItem(mediaId);
    }

    public void search(@NonNull String query, @Nullable Bundle extras) {
        getImpl().search(query, extras);
    }

    public void getSearchResult(@NonNull String query, int page, int pageSize, @Nullable Bundle extras) {
        getImpl().getSearchResult(query, page, pageSize, extras);
    }
}
