package android.support.v4.media;

import android.os.BadParcelableException;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaLibraryService2;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.MediaSessionManager;
import java.util.List;
import java.util.concurrent.Executor;

class MediaLibraryService2LegacyStub extends MediaBrowserServiceCompat {
    /* access modifiers changed from: private */
    public final MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl mLibrarySession;

    MediaLibraryService2LegacyStub(MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl session) {
        this.mLibrarySession = session;
    }

    public MediaBrowserServiceCompat.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle extras) {
        if (MediaUtils2.isDefaultLibraryRootHint(extras)) {
            return MediaUtils2.sDefaultBrowserRoot;
        }
        MediaLibraryService2.LibraryRoot libraryRoot = this.mLibrarySession.getCallback().onGetLibraryRoot(this.mLibrarySession.getInstance(), getController(), extras);
        if (libraryRoot == null) {
            return null;
        }
        return new MediaBrowserServiceCompat.BrowserRoot(libraryRoot.getRootId(), libraryRoot.getExtras());
    }

    public void onLoadChildren(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        onLoadChildren(parentId, result, null);
    }

    public void onLoadChildren(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result, Bundle options) {
        result.detach();
        MediaSession2.ControllerInfo controller = getController();
        Executor callbackExecutor = this.mLibrarySession.getCallbackExecutor();
        final Bundle bundle = options;
        final MediaSession2.ControllerInfo controllerInfo = controller;
        final String str = parentId;
        final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result2 = result;
        AnonymousClass1 r0 = new Runnable() {
            public void run() {
                if (bundle != null) {
                    bundle.setClassLoader(MediaLibraryService2LegacyStub.this.mLibrarySession.getContext().getClassLoader());
                    try {
                        int page = bundle.getInt(MediaBrowserCompat.EXTRA_PAGE);
                        int pageSize = bundle.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
                        if (page > 0 && pageSize > 0) {
                            result2.sendResult(MediaUtils2.convertToMediaItemList(MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onGetChildren(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controllerInfo, str, page, pageSize, bundle)));
                            return;
                        }
                    } catch (BadParcelableException e) {
                    }
                }
                result2.sendResult(MediaUtils2.convertToMediaItemList(MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onGetChildren(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controllerInfo, str, 1, Integer.MAX_VALUE, null)));
            }
        };
        callbackExecutor.execute(r0);
    }

    public void onLoadItem(final String itemId, final MediaBrowserServiceCompat.Result<MediaBrowserCompat.MediaItem> result) {
        result.detach();
        final MediaSession2.ControllerInfo controller = getController();
        this.mLibrarySession.getCallbackExecutor().execute(new Runnable() {
            public void run() {
                MediaItem2 item = MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onGetItem(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controller, itemId);
                if (item == null) {
                    result.sendResult(null);
                } else {
                    result.sendResult(MediaUtils2.convertToMediaItem(item));
                }
            }
        });
    }

    public void onSearch(String query, Bundle extras, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        final Bundle bundle = extras;
        result.detach();
        final MediaSession2.ControllerInfo controller = getController();
        bundle.setClassLoader(this.mLibrarySession.getContext().getClassLoader());
        try {
            int page = bundle.getInt(MediaBrowserCompat.EXTRA_PAGE);
            int pageSize = bundle.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if (page <= 0 || pageSize <= 0) {
                final String str = query;
                try {
                    this.mLibrarySession.getCallbackExecutor().execute(new Runnable() {
                        public void run() {
                            MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onSearch(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controller, str, bundle);
                        }
                    });
                } catch (BadParcelableException e) {
                }
            } else {
                Executor callbackExecutor = this.mLibrarySession.getCallbackExecutor();
                final MediaSession2.ControllerInfo controllerInfo = controller;
                final String str2 = query;
                final int i = page;
                final int i2 = pageSize;
                final Bundle bundle2 = bundle;
                final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result2 = result;
                AnonymousClass3 r1 = new Runnable() {
                    public void run() {
                        List<MediaItem2> searchResult = MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onGetSearchResult(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controllerInfo, str2, i, i2, bundle2);
                        if (searchResult == null) {
                            result2.sendResult(null);
                        } else {
                            result2.sendResult(MediaUtils2.convertToMediaItemList(searchResult));
                        }
                    }
                };
                callbackExecutor.execute(r1);
                String str3 = query;
            }
        } catch (BadParcelableException e2) {
            String str4 = query;
        }
    }

    public void onCustomAction(String action, Bundle extras, MediaBrowserServiceCompat.Result<Bundle> result) {
    }

    private MediaSession2.ControllerInfo getController() {
        List<MediaSession2.ControllerInfo> controllers = this.mLibrarySession.getConnectedControllers();
        MediaSessionManager.RemoteUserInfo info = getCurrentBrowserInfo();
        if (info == null) {
            return null;
        }
        for (int i = 0; i < controllers.size(); i++) {
            MediaSession2.ControllerInfo controller = controllers.get(i);
            if (controller.getPackageName().equals(info.getPackageName()) && controller.getUid() == info.getUid()) {
                return controller;
            }
        }
        return null;
    }
}
