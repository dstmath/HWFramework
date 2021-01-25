package android.support.v4.media;

import android.os.BadParcelableException;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaLibraryService2;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.MediaSessionManager;
import java.util.List;

/* access modifiers changed from: package-private */
public class MediaLibraryService2LegacyStub extends MediaBrowserServiceCompat {
    private final MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl mLibrarySession;

    MediaLibraryService2LegacyStub(MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl session) {
        this.mLibrarySession = session;
    }

    @Override // android.support.v4.media.MediaBrowserServiceCompat
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

    @Override // android.support.v4.media.MediaBrowserServiceCompat
    public void onLoadChildren(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        onLoadChildren(parentId, result, null);
    }

    @Override // android.support.v4.media.MediaBrowserServiceCompat
    public void onLoadChildren(final String parentId, final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result, final Bundle options) {
        result.detach();
        final MediaSession2.ControllerInfo controller = getController();
        this.mLibrarySession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaLibraryService2LegacyStub.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (options != null) {
                    options.setClassLoader(MediaLibraryService2LegacyStub.this.mLibrarySession.getContext().getClassLoader());
                    try {
                        int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
                        int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
                        if (page > 0 && pageSize > 0) {
                            result.sendResult(MediaUtils2.convertToMediaItemList(MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onGetChildren(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controller, parentId, page, pageSize, options)));
                            return;
                        }
                    } catch (BadParcelableException e) {
                    }
                }
                result.sendResult(MediaUtils2.convertToMediaItemList(MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onGetChildren(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controller, parentId, 1, Integer.MAX_VALUE, null)));
            }
        });
    }

    @Override // android.support.v4.media.MediaBrowserServiceCompat
    public void onLoadItem(final String itemId, final MediaBrowserServiceCompat.Result<MediaBrowserCompat.MediaItem> result) {
        result.detach();
        final MediaSession2.ControllerInfo controller = getController();
        this.mLibrarySession.getCallbackExecutor().execute(new Runnable() {
            /* class android.support.v4.media.MediaLibraryService2LegacyStub.AnonymousClass2 */

            @Override // java.lang.Runnable
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

    @Override // android.support.v4.media.MediaBrowserServiceCompat
    public void onSearch(final String query, final Bundle extras, final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();
        final MediaSession2.ControllerInfo controller = getController();
        extras.setClassLoader(this.mLibrarySession.getContext().getClassLoader());
        try {
            final int page = extras.getInt(MediaBrowserCompat.EXTRA_PAGE);
            final int pageSize = extras.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
            if (page <= 0 || pageSize <= 0) {
                try {
                    this.mLibrarySession.getCallbackExecutor().execute(new Runnable() {
                        /* class android.support.v4.media.MediaLibraryService2LegacyStub.AnonymousClass4 */

                        @Override // java.lang.Runnable
                        public void run() {
                            MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onSearch(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controller, query, extras);
                        }
                    });
                } catch (BadParcelableException e) {
                }
            } else {
                this.mLibrarySession.getCallbackExecutor().execute(new Runnable() {
                    /* class android.support.v4.media.MediaLibraryService2LegacyStub.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        List<MediaItem2> searchResult = MediaLibraryService2LegacyStub.this.mLibrarySession.getCallback().onGetSearchResult(MediaLibraryService2LegacyStub.this.mLibrarySession.getInstance(), controller, query, page, pageSize, extras);
                        if (searchResult == null) {
                            result.sendResult(null);
                        } else {
                            result.sendResult(MediaUtils2.convertToMediaItemList(searchResult));
                        }
                    }
                });
            }
        } catch (BadParcelableException e2) {
        }
    }

    @Override // android.support.v4.media.MediaBrowserServiceCompat
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
