package android.support.v4.media;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.MediaSessionService2;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class MediaLibraryService2 extends MediaSessionService2 {
    public static final String SERVICE_INTERFACE = "android.media.MediaLibraryService2";

    @Override // android.support.v4.media.MediaSessionService2
    @NonNull
    public abstract MediaLibrarySession onCreateSession(String str);

    public static final class MediaLibrarySession extends MediaSession2 {

        /* access modifiers changed from: package-private */
        public interface SupportLibraryImpl extends MediaSession2.SupportLibraryImpl {
            @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
            MediaLibrarySessionCallback getCallback();

            @Override // android.support.v4.media.MediaSession2.SupportLibraryImpl
            MediaLibrarySession getInstance();

            IBinder getLegacySessionBinder();

            void notifyChildrenChanged(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str, int i, @Nullable Bundle bundle);

            void notifyChildrenChanged(@NonNull String str, int i, @Nullable Bundle bundle);

            void notifySearchResultChanged(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str, int i, @Nullable Bundle bundle);

            void onGetChildrenOnExecutor(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str, int i, int i2, @Nullable Bundle bundle);

            void onGetItemOnExecutor(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str);

            void onGetLibraryRootOnExecutor(@NonNull MediaSession2.ControllerInfo controllerInfo, @Nullable Bundle bundle);

            void onGetSearchResultOnExecutor(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str, int i, int i2, @Nullable Bundle bundle);

            void onSearchOnExecutor(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str, @Nullable Bundle bundle);

            void onSubscribeOnExecutor(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str, @Nullable Bundle bundle);

            void onUnsubscribeOnExecutor(@NonNull MediaSession2.ControllerInfo controllerInfo, @NonNull String str);
        }

        public static class MediaLibrarySessionCallback extends MediaSession2.SessionCallback {
            @Nullable
            public LibraryRoot onGetLibraryRoot(@NonNull MediaLibrarySession session, @NonNull MediaSession2.ControllerInfo controller, @Nullable Bundle rootHints) {
                return null;
            }

            @Nullable
            public MediaItem2 onGetItem(@NonNull MediaLibrarySession session, @NonNull MediaSession2.ControllerInfo controller, @NonNull String mediaId) {
                return null;
            }

            @Nullable
            public List<MediaItem2> onGetChildren(@NonNull MediaLibrarySession session, @NonNull MediaSession2.ControllerInfo controller, @NonNull String parentId, int page, int pageSize, @Nullable Bundle extras) {
                return null;
            }

            public void onSubscribe(@NonNull MediaLibrarySession session, @NonNull MediaSession2.ControllerInfo controller, @NonNull String parentId, @Nullable Bundle extras) {
            }

            public void onUnsubscribe(@NonNull MediaLibrarySession session, @NonNull MediaSession2.ControllerInfo controller, @NonNull String parentId) {
            }

            public void onSearch(@NonNull MediaLibrarySession session, @NonNull MediaSession2.ControllerInfo controller, @NonNull String query, @Nullable Bundle extras) {
            }

            @Nullable
            public List<MediaItem2> onGetSearchResult(@NonNull MediaLibrarySession session, @NonNull MediaSession2.ControllerInfo controller, @NonNull String query, int page, int pageSize, @Nullable Bundle extras) {
                return null;
            }
        }

        public static final class Builder extends MediaSession2.BuilderBase<MediaLibrarySession, Builder, MediaLibrarySessionCallback> {
            public Builder(@NonNull MediaLibraryService2 service, @NonNull Executor callbackExecutor, @NonNull MediaLibrarySessionCallback callback) {
                super(service);
                setSessionCallback(callbackExecutor, callback);
            }

            @Override // android.support.v4.media.MediaSession2.BuilderBase
            @NonNull
            public Builder setPlayer(@NonNull BaseMediaPlayer player) {
                return (Builder) super.setPlayer(player);
            }

            @Override // android.support.v4.media.MediaSession2.BuilderBase
            @NonNull
            public Builder setPlaylistAgent(@NonNull MediaPlaylistAgent playlistAgent) {
                return (Builder) super.setPlaylistAgent(playlistAgent);
            }

            @Override // android.support.v4.media.MediaSession2.BuilderBase
            @NonNull
            public Builder setVolumeProvider(@Nullable VolumeProviderCompat volumeProvider) {
                return (Builder) super.setVolumeProvider(volumeProvider);
            }

            @Override // android.support.v4.media.MediaSession2.BuilderBase
            @NonNull
            public Builder setSessionActivity(@Nullable PendingIntent pi) {
                return (Builder) super.setSessionActivity(pi);
            }

            @Override // android.support.v4.media.MediaSession2.BuilderBase
            @NonNull
            public Builder setId(@NonNull String id) {
                return (Builder) super.setId(id);
            }

            @Override // android.support.v4.media.MediaSession2.BuilderBase
            @NonNull
            public MediaLibrarySession build() {
                if (this.mCallbackExecutor == null) {
                    this.mCallbackExecutor = new MediaSession2.MainHandlerExecutor(this.mContext);
                }
                if (this.mCallback == null) {
                    this.mCallback = new MediaLibrarySessionCallback() {
                        /* class android.support.v4.media.MediaLibraryService2.MediaLibrarySession.Builder.AnonymousClass1 */
                    };
                }
                return new MediaLibrarySession(this.mContext, this.mId, this.mPlayer, this.mPlaylistAgent, this.mVolumeProvider, this.mSessionActivity, this.mCallbackExecutor, this.mCallback);
            }
        }

        MediaLibrarySession(Context context, String id, BaseMediaPlayer player, MediaPlaylistAgent playlistAgent, VolumeProviderCompat volumeProvider, PendingIntent sessionActivity, Executor callbackExecutor, MediaSession2.SessionCallback callback) {
            super(context, id, player, playlistAgent, volumeProvider, sessionActivity, callbackExecutor, callback);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2
        public SupportLibraryImpl createImpl(Context context, String id, BaseMediaPlayer player, MediaPlaylistAgent playlistAgent, VolumeProviderCompat volumeProvider, PendingIntent sessionActivity, Executor callbackExecutor, MediaSession2.SessionCallback callback) {
            return new MediaLibrarySessionImplBase(this, context, id, player, playlistAgent, volumeProvider, sessionActivity, callbackExecutor, callback);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2
        public SupportLibraryImpl getImpl() {
            return (SupportLibraryImpl) super.getImpl();
        }

        public void notifyChildrenChanged(@NonNull MediaSession2.ControllerInfo controller, @NonNull String parentId, int itemCount, @Nullable Bundle extras) {
            getImpl().notifyChildrenChanged(controller, parentId, itemCount, extras);
        }

        public void notifyChildrenChanged(@NonNull String parentId, int itemCount, @Nullable Bundle extras) {
            getImpl().notifyChildrenChanged(parentId, itemCount, extras);
        }

        public void notifySearchResultChanged(@NonNull MediaSession2.ControllerInfo controller, @NonNull String query, int itemCount, @Nullable Bundle extras) {
            getImpl().notifySearchResultChanged(controller, query, itemCount, extras);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaSession2
        public MediaLibrarySessionCallback getCallback() {
            return (MediaLibrarySessionCallback) super.getCallback();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.support.v4.media.MediaSessionService2
    public MediaSessionService2.SupportLibraryImpl createImpl() {
        return new MediaLibraryService2ImplBase();
    }

    @Override // android.support.v4.media.MediaSessionService2, android.app.Service
    public void onCreate() {
        super.onCreate();
        if (!(getSession() instanceof MediaLibrarySession)) {
            throw new RuntimeException("Expected MediaLibrarySession, but returned MediaSession2");
        }
    }

    @Override // android.support.v4.media.MediaSessionService2, android.app.Service
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public static final class LibraryRoot {
        public static final String EXTRA_OFFLINE = "android.media.extra.OFFLINE";
        public static final String EXTRA_RECENT = "android.media.extra.RECENT";
        public static final String EXTRA_SUGGESTED = "android.media.extra.SUGGESTED";
        private final Bundle mExtras;
        private final String mRootId;

        public LibraryRoot(@NonNull String rootId, @Nullable Bundle extras) {
            if (rootId != null) {
                this.mRootId = rootId;
                this.mExtras = extras;
                return;
            }
            throw new IllegalArgumentException("rootId shouldn't be null");
        }

        public String getRootId() {
            return this.mRootId;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }
    }
}
