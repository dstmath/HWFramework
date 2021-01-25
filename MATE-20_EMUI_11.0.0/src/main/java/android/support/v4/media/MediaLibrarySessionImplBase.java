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

/* access modifiers changed from: package-private */
@TargetApi(19)
public class MediaLibrarySessionImplBase extends MediaSession2ImplBase implements MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl {
    private final MediaBrowserServiceCompat mBrowserServiceLegacyStub = new MediaLibraryService2LegacyStub(this);
    @GuardedBy("mLock")
    private final ArrayMap<MediaSession2.ControllerInfo, Set<String>> mSubscriptions = new ArrayMap<>();

    MediaLibrarySessionImplBase(MediaLibraryService2.MediaLibrarySession instance, Context context, String id, BaseMediaPlayer player, MediaPlaylistAgent playlistAgent, VolumeProviderCompat volumeProvider, PendingIntent sessionActivity, Executor callbackExecutor, MediaSession2.SessionCallback callback) {
        super(instance, context, id, player, playlistAgent, volumeProvider, sessionActivity, callbackExecutor, callback);
        this.mBrowserServiceLegacyStub.attachToBaseContext(context);
        this.mBrowserServiceLegacyStub.onCreate();
    }

    @Override // android.support.v4.media.MediaSession2ImplBase, android.support.v4.media.MediaSession2.SupportLibraryImpl
    public MediaLibraryService2.MediaLibrarySession getInstance() {
        return (MediaLibraryService2.MediaLibrarySession) super.getInstance();
    }

    @Override // android.support.v4.media.MediaSession2ImplBase, android.support.v4.media.MediaSession2.SupportLibraryImpl
    public MediaLibraryService2.MediaLibrarySession.MediaLibrarySessionCallback getCallback() {
        return (MediaLibraryService2.MediaLibrarySession.MediaLibrarySessionCallback) super.getCallback();
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public IBinder getLegacySessionBinder() {
        return this.mBrowserServiceLegacyStub.onBind(new Intent(MediaBrowserServiceCompat.SERVICE_INTERFACE));
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void notifyChildrenChanged(final String parentId, final int itemCount, final Bundle extras) {
        if (TextUtils.isEmpty(parentId)) {
            throw new IllegalArgumentException("query shouldn't be empty");
        } else if (itemCount >= 0) {
            List<MediaSession2.ControllerInfo> controllers = getConnectedControllers();
            MediaSession2ImplBase.NotifyRunnable runnable = new MediaSession2ImplBase.NotifyRunnable() {
                /* class android.support.v4.media.MediaLibrarySessionImplBase.AnonymousClass1 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
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

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void notifyChildrenChanged(final MediaSession2.ControllerInfo controller, final String parentId, final int itemCount, final Bundle extras) {
        if (controller == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (TextUtils.isEmpty(parentId)) {
            throw new IllegalArgumentException("query shouldn't be empty");
        } else if (itemCount >= 0) {
            notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
                /* class android.support.v4.media.MediaLibrarySessionImplBase.AnonymousClass2 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    if (MediaLibrarySessionImplBase.this.isSubscribed(controller, parentId)) {
                        callback.onChildrenChanged(parentId, itemCount, extras);
                    } else if (MediaSession2ImplBase.DEBUG) {
                        Log.d("MS2ImplBase", "Skipping notifyChildrenChanged() to " + controller + " because it hasn't subscribed");
                        MediaLibrarySessionImplBase.this.dumpSubscription();
                    }
                }
            });
        } else {
            throw new IllegalArgumentException("itemCount shouldn't be negative");
        }
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void notifySearchResultChanged(MediaSession2.ControllerInfo controller, final String query, final int itemCount, final Bundle extras) {
        if (controller == null) {
            throw new IllegalArgumentException("controller shouldn't be null");
        } else if (!TextUtils.isEmpty(query)) {
            notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
                /* class android.support.v4.media.MediaLibrarySessionImplBase.AnonymousClass3 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onSearchResultChanged(query, itemCount, extras);
                }
            });
        } else {
            throw new IllegalArgumentException("query shouldn't be empty");
        }
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void onGetLibraryRootOnExecutor(MediaSession2.ControllerInfo controller, final Bundle rootHints) {
        final MediaLibraryService2.LibraryRoot root = getCallback().onGetLibraryRoot(getInstance(), controller, rootHints);
        notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
            /* class android.support.v4.media.MediaLibrarySessionImplBase.AnonymousClass4 */

            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
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

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void onGetItemOnExecutor(MediaSession2.ControllerInfo controller, final String mediaId) {
        final MediaItem2 result = getCallback().onGetItem(getInstance(), controller, mediaId);
        notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
            /* class android.support.v4.media.MediaLibrarySessionImplBase.AnonymousClass5 */

            @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
            public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                callback.onGetItemDone(mediaId, result);
            }
        });
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void onGetChildrenOnExecutor(MediaSession2.ControllerInfo controller, final String parentId, final int page, final int pageSize, final Bundle extras) {
        final List<MediaItem2> result = getCallback().onGetChildren(getInstance(), controller, parentId, page, pageSize, extras);
        if (result == null || result.size() <= pageSize) {
            notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
                /* class android.support.v4.media.MediaLibrarySessionImplBase.AnonymousClass6 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onGetChildrenDone(parentId, page, pageSize, result, extras);
                }
            });
            return;
        }
        throw new IllegalArgumentException("onGetChildren() shouldn't return media items more than pageSize. result.size()=" + result.size() + " pageSize=" + pageSize);
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void onSubscribeOnExecutor(MediaSession2.ControllerInfo controller, String parentId, Bundle option) {
        synchronized (this.mLock) {
            Set<String> subscription = this.mSubscriptions.get(controller);
            if (subscription == null) {
                subscription = new HashSet();
                this.mSubscriptions.put(controller, subscription);
            }
            subscription.add(parentId);
        }
        getCallback().onSubscribe(getInstance(), controller, parentId, option);
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void onUnsubscribeOnExecutor(MediaSession2.ControllerInfo controller, String parentId) {
        getCallback().onUnsubscribe(getInstance(), controller, parentId);
        synchronized (this.mLock) {
            this.mSubscriptions.remove(controller);
        }
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void onSearchOnExecutor(MediaSession2.ControllerInfo controller, String query, Bundle extras) {
        getCallback().onSearch(getInstance(), controller, query, extras);
    }

    @Override // android.support.v4.media.MediaLibraryService2.MediaLibrarySession.SupportLibraryImpl
    public void onGetSearchResultOnExecutor(MediaSession2.ControllerInfo controller, final String query, final int page, final int pageSize, final Bundle extras) {
        final List<MediaItem2> result = getCallback().onGetSearchResult(getInstance(), controller, query, page, pageSize, extras);
        if (result == null || result.size() <= pageSize) {
            notifyToController(controller, new MediaSession2ImplBase.NotifyRunnable() {
                /* class android.support.v4.media.MediaLibrarySessionImplBase.AnonymousClass7 */

                @Override // android.support.v4.media.MediaSession2ImplBase.NotifyRunnable
                public void run(MediaSession2.ControllerCb callback) throws RemoteException {
                    callback.onGetSearchResultDone(query, page, pageSize, result, extras);
                }
            });
            return;
        }
        throw new IllegalArgumentException("onGetSearchResult() shouldn't return media items more than pageSize. result.size()=" + result.size() + " pageSize=" + pageSize);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSubscribed(MediaSession2.ControllerInfo controller, String parentId) {
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
    /* access modifiers changed from: public */
    private void dumpSubscription() {
        if (DEBUG) {
            synchronized (this.mLock) {
                Log.d("MS2ImplBase", "Dumping subscription, controller sz=" + this.mSubscriptions.size());
                for (int i = 0; i < this.mSubscriptions.size(); i++) {
                    Log.d("MS2ImplBase", "  controller " + this.mSubscriptions.valueAt(i));
                    Iterator<String> it = this.mSubscriptions.valueAt(i).iterator();
                    while (it.hasNext()) {
                        Log.d("MS2ImplBase", "  - " + it.next());
                    }
                }
            }
        }
    }
}
