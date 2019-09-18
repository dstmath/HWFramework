package android.support.v4.media;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.GuardedBy;
import android.support.v4.media.MediaLibraryService2;
import android.support.v4.media.MediaSession2;
import android.support.v4.media.MediaSession2ImplBase;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@TargetApi(19)
class MediaLibrarySessionImplBase extends MediaSession2ImplBase implements MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl {
    private final MediaBrowserServiceCompat mBrowserServiceLegacyStub = new MediaLibraryService2LegacyStub(this);
    @GuardedBy("mLock")
    private final ArrayMap<MediaSession2.ControllerInfo, Set<String>> mSubscriptions = new ArrayMap<>();

    MediaLibrarySessionImplBase(MediaLibraryService2.MediaLibrarySession instance, Context context, String id, BaseMediaPlayer player, MediaPlaylistAgent playlistAgent, VolumeProviderCompat volumeProvider, PendingIntent sessionActivity, Executor callbackExecutor, MediaSession2.SessionCallback callback) {
        super(instance, context, id, player, playlistAgent, volumeProvider, sessionActivity, callbackExecutor, callback);
        this.mBrowserServiceLegacyStub.attachToBaseContext(context);
        this.mBrowserServiceLegacyStub.onCreate();
    }

    public MediaLibraryService2.MediaLibrarySession getInstance() {
        return (MediaLibraryService2.MediaLibrarySession) super.getInstance();
    }

    public MediaLibraryService2.MediaLibrarySession.MediaLibrarySessionCallback getCallback() {
        return (MediaLibraryService2.MediaLibrarySession.MediaLibrarySessionCallback) super.getCallback();
    }

    public IBinder getLegacySessionBinder() {
        return this.mBrowserServiceLegacyStub.onBind(new Intent(MediaBrowserServiceCompat.SERVICE_INTERFACE));
    }

    public void notifyChildrenChanged(final String parentId, final int itemCount, final Bundle extras) {
        if (TextUtils.isEmpty(parentId)) {
            throw new IllegalArgumentException("query shouldn't be empty");
        } else if (itemCount >= 0) {
            List<MediaSession2.ControllerInfo> controllers = getConnectedControllers();
            MediaSession2ImplBase.NotifyRunnable runnable = new MediaSession2ImplBase.NotifyRunnable() {
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onChildrenChanged(parentId, itemCount, extras);
                }
            };
            for (int i = 0; i < controllers.size(); i++) {
                if (isSubscribed(controllers.get(i), parentId)) {
                    notifyToController(controllers.get(i), runnable);
                }
            }
        } else {
            throw new IllegalArgumentException("itemCount shouldn't be negative");
        }
    }

    public void notifyChildrenChanged(MediaSession2.ControllerInfo controller, String parentId, int itemCount, Bundle extras) {
        if (controller == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (TextUtils.isEmpty(parentId)) {
            throw new IllegalArgumentException("query shouldn't be empty");
        } else if (itemCount >= 0) {
            final MediaSession2.ControllerInfo controllerInfo = controller;
            final String str = parentId;
            final int i = itemCount;
            final Bundle bundle = extras;
            AnonymousClass2 r1 = new MediaSession2ImplBase.NotifyRunnable() {
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    if (!MediaLibrarySessionImplBase.this.isSubscribed(controllerInfo, str)) {
                        if (MediaSession2ImplBase.DEBUG) {
                            Log.d("MS2ImplBase", "Skipping notifyChildrenChanged() to " + controllerInfo + " because it hasn't subscribed");
                            MediaLibrarySessionImplBase.this.dumpSubscription();
                        }
                        return;
                    }
                    callback.onChildrenChanged(str, i, bundle);
                }
            };
            notifyToController(controller, r1);
        } else {
            throw new IllegalArgumentException("itemCount shouldn't be negative");
        }
    }

    public void notifySearchResultChanged(MediaSession2.ControllerInfo controller, final String query, final int itemCount, final Bundle extras) {
        if (controller == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (!TextUtils.isEmpty(query)) {
            notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onSearchResultChanged(query, itemCount, extras);
                }
            });
        } else {
            throw new IllegalArgumentException("query shouldn't be empty");
        }
    }

    public void onGetLibraryRootOnExecutor(MediaSession2.ControllerInfo controller, final Bundle rootHints) {
        final MediaLibraryService2.LibraryRoot root = getCallback().onGetLibraryRoot(getInstance(), controller, rootHints);
        notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                Bundle bundle = rootHints;
                Bundle bundle2 = null;
                String rootId = root == null ? null : root.getRootId();
                if (root != null) {
                    bundle2 = root.getExtras();
                }
                callback.onGetLibraryRootDone(bundle, rootId, bundle2);
            }
        });
    }

    public void onGetItemOnExecutor(MediaSession2.ControllerInfo controller, final String mediaId) {
        final MediaItem2 result = getCallback().onGetItem(getInstance(), controller, mediaId);
        notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                callback.onGetItemDone(mediaId, result);
            }
        });
    }

    public void onGetChildrenOnExecutor(MediaSession2.ControllerInfo controller, String parentId, int page, int pageSize, Bundle extras) {
        List<MediaItem2> result = getCallback().onGetChildren(getInstance(), controller, parentId, page, pageSize, extras);
        if (result == null || result.size() <= pageSize) {
            final String str = parentId;
            final int i = page;
            final int i2 = pageSize;
            final List<MediaItem2> list = result;
            final Bundle bundle = extras;
            AnonymousClass6 r2 = new MediaSession2ImplBase.NotifyRunnable() {
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onGetChildrenDone(str, i, i2, list, bundle);
                }
            };
            notifyToController(controller, r2);
            return;
        }
        throw new IllegalArgumentException("onGetChildren() shouldn't return media items more than pageSize. result.size()=" + result.size() + " pageSize=" + pageSize);
    }

    public void onSubscribeOnExecutor(MediaSession2.ControllerInfo controller, String parentId, Bundle option) {
        synchronized (this.mLock) {
            Set<String> subscription = this.mSubscriptions.get(controller);
            if (subscription == null) {
                subscription = new HashSet<>();
                this.mSubscriptions.put(controller, subscription);
            }
            subscription.add(parentId);
        }
        getCallback().onSubscribe(getInstance(), controller, parentId, option);
    }

    public void onUnsubscribeOnExecutor(MediaSession2.ControllerInfo controller, String parentId) {
        getCallback().onUnsubscribe(getInstance(), controller, parentId);
        synchronized (this.mLock) {
            this.mSubscriptions.remove(controller);
        }
    }

    public void onSearchOnExecutor(MediaSession2.ControllerInfo controller, String query, Bundle extras) {
        getCallback().onSearch(getInstance(), controller, query, extras);
    }

    public void onGetSearchResultOnExecutor(MediaSession2.ControllerInfo controller, String query, int page, int pageSize, Bundle extras) {
        List<MediaItem2> result = getCallback().onGetSearchResult(getInstance(), controller, query, page, pageSize, extras);
        if (result == null || result.size() <= pageSize) {
            final String str = query;
            final int i = page;
            final int i2 = pageSize;
            final List<MediaItem2> list = result;
            final Bundle bundle = extras;
            AnonymousClass7 r2 = new MediaSession2ImplBase.NotifyRunnable() {
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onGetSearchResultDone(str, i, i2, list, bundle);
                }
            };
            notifyToController(controller, r2);
            return;
        }
        throw new IllegalArgumentException("onGetSearchResult() shouldn't return media items more than pageSize. result.size()=" + result.size() + " pageSize=" + pageSize);
    }

    /* access modifiers changed from: private */
    public boolean isSubscribed(MediaSession2.ControllerInfo controller, String parentId) {
        synchronized (this.mLock) {
            Set<String> subscriptions = this.mSubscriptions.get(controller);
            if (subscriptions != null) {
                if (subscriptions.contains(parentId)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void dumpSubscription() {
        if (DEBUG) {
            synchronized (this.mLock) {
                Log.d("MS2ImplBase", "Dumping subscription, controller sz=" + this.mSubscriptions.size());
                for (int i = 0; i < this.mSubscriptions.size(); i++) {
                    Log.d("MS2ImplBase", "  controller " + this.mSubscriptions.valueAt(i));
                    Iterator it = this.mSubscriptions.valueAt(i).iterator();
                    while (it.hasNext()) {
                        Log.d("MS2ImplBase", "  - " + ((String) it.next()));
                    }
                }
            }
        }
    }
}
