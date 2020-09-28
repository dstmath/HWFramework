package android.service.media;

import android.annotation.UnsupportedAppUsage;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.media.browse.MediaBrowser;
import android.media.browse.MediaBrowserUtils;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.service.media.IMediaBrowserService;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class MediaBrowserService extends Service {
    private static final boolean DBG = false;
    @UnsupportedAppUsage
    public static final String KEY_MEDIA_ITEM = "media_item";
    private static final int RESULT_ERROR = -1;
    private static final int RESULT_FLAG_ON_LOAD_ITEM_NOT_IMPLEMENTED = 2;
    private static final int RESULT_FLAG_OPTION_NOT_HANDLED = 1;
    private static final int RESULT_OK = 0;
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";
    private static final String TAG = "MediaBrowserService";
    private ServiceBinder mBinder;
    private final ArrayMap<IBinder, ConnectionRecord> mConnections = new ArrayMap<>();
    private ConnectionRecord mCurConnection;
    private final Handler mHandler = new Handler();
    MediaSession.Token mSession;

    @Retention(RetentionPolicy.SOURCE)
    private @interface ResultFlags {
    }

    public abstract BrowserRoot onGetRoot(String str, int i, Bundle bundle);

    public abstract void onLoadChildren(String str, Result<List<MediaBrowser.MediaItem>> result);

    /* access modifiers changed from: private */
    public class ConnectionRecord implements IBinder.DeathRecipient {
        IMediaBrowserServiceCallbacks callbacks;
        int pid;
        String pkg;
        BrowserRoot root;
        Bundle rootHints;
        HashMap<String, List<Pair<IBinder, Bundle>>> subscriptions;
        int uid;

        private ConnectionRecord() {
            this.subscriptions = new HashMap<>();
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ConnectionRecord.AnonymousClass1 */

                public void run() {
                    MediaBrowserService.this.mConnections.remove(ConnectionRecord.this.callbacks.asBinder());
                }
            });
        }
    }

    public class Result<T> {
        private Object mDebug;
        private boolean mDetachCalled;
        @UnsupportedAppUsage
        private int mFlags;
        private boolean mSendResultCalled;

        Result(Object debug) {
            this.mDebug = debug;
        }

        public void sendResult(T result) {
            if (!this.mSendResultCalled) {
                this.mSendResultCalled = true;
                onResultSent(result, this.mFlags);
                return;
            }
            throw new IllegalStateException("sendResult() called twice for: " + this.mDebug);
        }

        public void detach() {
            if (this.mDetachCalled) {
                throw new IllegalStateException("detach() called when detach() had already been called for: " + this.mDebug);
            } else if (!this.mSendResultCalled) {
                this.mDetachCalled = true;
            } else {
                throw new IllegalStateException("detach() called when sendResult() had already been called for: " + this.mDebug);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isDone() {
            return this.mDetachCalled || this.mSendResultCalled;
        }

        /* access modifiers changed from: package-private */
        public void setFlags(int flags) {
            this.mFlags = flags;
        }

        /* access modifiers changed from: package-private */
        public void onResultSent(T t, int flags) {
        }
    }

    private class ServiceBinder extends IMediaBrowserService.Stub {
        private ServiceBinder() {
        }

        @Override // android.service.media.IMediaBrowserService
        public void connect(final String pkg, final Bundle rootHints, final IMediaBrowserServiceCallbacks callbacks) {
            final int pid = Binder.getCallingPid();
            final int uid = Binder.getCallingUid();
            if (MediaBrowserService.this.isValidPackage(pkg, uid)) {
                MediaBrowserService.this.mHandler.post(new Runnable() {
                    /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass1 */

                    public void run() {
                        IBinder b = callbacks.asBinder();
                        MediaBrowserService.this.mConnections.remove(b);
                        ConnectionRecord connection = new ConnectionRecord();
                        connection.pkg = pkg;
                        connection.pid = pid;
                        connection.uid = uid;
                        connection.rootHints = rootHints;
                        connection.callbacks = callbacks;
                        MediaBrowserService.this.mCurConnection = connection;
                        connection.root = MediaBrowserService.this.onGetRoot(pkg, uid, rootHints);
                        MediaBrowserService.this.mCurConnection = null;
                        if (connection.root == null) {
                            Log.i(MediaBrowserService.TAG, "No root for client " + pkg + " from service " + getClass().getName());
                            try {
                                callbacks.onConnectFailed();
                            } catch (RemoteException e) {
                                Log.w(MediaBrowserService.TAG, "Calling onConnectFailed() failed. Ignoring. pkg=" + pkg);
                            }
                        } else {
                            try {
                                MediaBrowserService.this.mConnections.put(b, connection);
                                b.linkToDeath(connection, 0);
                                if (MediaBrowserService.this.mSession != null) {
                                    callbacks.onConnect(connection.root.getRootId(), MediaBrowserService.this.mSession, connection.root.getExtras());
                                }
                            } catch (RemoteException e2) {
                                Log.w(MediaBrowserService.TAG, "Calling onConnect() failed. Dropping client. pkg=" + pkg);
                                MediaBrowserService.this.mConnections.remove(b);
                            }
                        }
                    }
                });
                return;
            }
            throw new IllegalArgumentException("Package/uid mismatch: uid=" + uid + " package=" + pkg);
        }

        @Override // android.service.media.IMediaBrowserService
        public void disconnect(final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass2 */

                public void run() {
                    ConnectionRecord old = (ConnectionRecord) MediaBrowserService.this.mConnections.remove(callbacks.asBinder());
                    if (old != null) {
                        old.callbacks.asBinder().unlinkToDeath(old, 0);
                    }
                }
            });
        }

        @Override // android.service.media.IMediaBrowserService
        public void addSubscriptionDeprecated(String id, IMediaBrowserServiceCallbacks callbacks) {
        }

        @Override // android.service.media.IMediaBrowserService
        public void addSubscription(final String id, final IBinder token, final Bundle options, final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass3 */

                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "addSubscription for callback that isn't registered id=" + id);
                        return;
                    }
                    MediaBrowserService.this.addSubscription(id, connection, token, options);
                }
            });
        }

        @Override // android.service.media.IMediaBrowserService
        public void removeSubscriptionDeprecated(String id, IMediaBrowserServiceCallbacks callbacks) {
        }

        @Override // android.service.media.IMediaBrowserService
        public void removeSubscription(final String id, final IBinder token, final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass4 */

                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "removeSubscription for callback that isn't registered id=" + id);
                    } else if (!MediaBrowserService.this.removeSubscription(id, connection, token)) {
                        Log.w(MediaBrowserService.TAG, "removeSubscription called for " + id + " which is not subscribed");
                    }
                }
            });
        }

        @Override // android.service.media.IMediaBrowserService
        public void getMediaItem(final String mediaId, final ResultReceiver receiver, final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.ServiceBinder.AnonymousClass5 */

                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "getMediaItem for callback that isn't registered id=" + mediaId);
                        return;
                    }
                    MediaBrowserService.this.performLoadItem(mediaId, connection, receiver);
                }
            });
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mBinder = new ServiceBinder();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }

    @Override // android.app.Service
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public void onLoadChildren(String parentId, Result<List<MediaBrowser.MediaItem>> result, Bundle options) {
        result.setFlags(1);
        onLoadChildren(parentId, result);
    }

    public void onLoadItem(String itemId, Result<MediaBrowser.MediaItem> result) {
        result.setFlags(2);
        result.sendResult(null);
    }

    public void setSessionToken(final MediaSession.Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Session token may not be null.");
        } else if (this.mSession == null) {
            this.mSession = token;
            this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.AnonymousClass1 */

                public void run() {
                    Iterator<ConnectionRecord> iter = MediaBrowserService.this.mConnections.values().iterator();
                    while (iter.hasNext()) {
                        ConnectionRecord connection = iter.next();
                        try {
                            connection.callbacks.onConnect(connection.root.getRootId(), token, connection.root.getExtras());
                        } catch (RemoteException e) {
                            Log.w(MediaBrowserService.TAG, "Connection for " + connection.pkg + " is no longer valid.");
                            iter.remove();
                        }
                    }
                }
            });
        } else {
            throw new IllegalStateException("The session token has already been set.");
        }
    }

    public MediaSession.Token getSessionToken() {
        return this.mSession;
    }

    public final Bundle getBrowserRootHints() {
        ConnectionRecord connectionRecord = this.mCurConnection;
        if (connectionRecord == null) {
            throw new IllegalStateException("This should be called inside of onGetRoot or onLoadChildren or onLoadItem methods");
        } else if (connectionRecord.rootHints == null) {
            return null;
        } else {
            return new Bundle(this.mCurConnection.rootHints);
        }
    }

    public final MediaSessionManager.RemoteUserInfo getCurrentBrowserInfo() {
        ConnectionRecord connectionRecord = this.mCurConnection;
        if (connectionRecord != null) {
            return new MediaSessionManager.RemoteUserInfo(connectionRecord.pkg, this.mCurConnection.pid, this.mCurConnection.uid);
        }
        throw new IllegalStateException("This should be called inside of onGetRoot or onLoadChildren or onLoadItem methods");
    }

    public void notifyChildrenChanged(String parentId) {
        notifyChildrenChangedInternal(parentId, null);
    }

    public void notifyChildrenChanged(String parentId, Bundle options) {
        if (options != null) {
            notifyChildrenChangedInternal(parentId, options);
            return;
        }
        throw new IllegalArgumentException("options cannot be null in notifyChildrenChanged");
    }

    private void notifyChildrenChangedInternal(final String parentId, final Bundle options) {
        if (parentId != null) {
            this.mHandler.post(new Runnable() {
                /* class android.service.media.MediaBrowserService.AnonymousClass2 */

                public void run() {
                    for (IBinder binder : MediaBrowserService.this.mConnections.keySet()) {
                        ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(binder);
                        List<Pair<IBinder, Bundle>> callbackList = connection.subscriptions.get(parentId);
                        if (callbackList != null) {
                            for (Pair<IBinder, Bundle> callback : callbackList) {
                                if (MediaBrowserUtils.hasDuplicatedItems(options, callback.second)) {
                                    MediaBrowserService.this.performLoadChildren(parentId, connection, callback.second);
                                }
                            }
                        }
                    }
                }
            });
            return;
        }
        throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidPackage(String pkg, int uid) {
        if (pkg == null) {
            return false;
        }
        for (String str : getPackageManager().getPackagesForUid(uid)) {
            if (str.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addSubscription(String id, ConnectionRecord connection, IBinder token, Bundle options) {
        List<Pair<IBinder, Bundle>> callbackList = connection.subscriptions.get(id);
        if (callbackList == null) {
            callbackList = new ArrayList();
        }
        for (Pair<IBinder, Bundle> callback : callbackList) {
            if (token == callback.first && MediaBrowserUtils.areSameOptions(options, callback.second)) {
                return;
            }
        }
        callbackList.add(new Pair<>(token, options));
        connection.subscriptions.put(id, callbackList);
        performLoadChildren(id, connection, options);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean removeSubscription(String id, ConnectionRecord connection, IBinder token) {
        if (token == null) {
            return connection.subscriptions.remove(id) != null;
        }
        boolean removed = false;
        List<Pair<IBinder, Bundle>> callbackList = connection.subscriptions.get(id);
        if (callbackList != null) {
            Iterator<Pair<IBinder, Bundle>> iter = callbackList.iterator();
            while (iter.hasNext()) {
                if (token == iter.next().first) {
                    removed = true;
                    iter.remove();
                }
            }
            if (callbackList.size() == 0) {
                connection.subscriptions.remove(id);
            }
        }
        return removed;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performLoadChildren(final String parentId, final ConnectionRecord connection, final Bundle options) {
        Result<List<MediaBrowser.MediaItem>> result = new Result<List<MediaBrowser.MediaItem>>(parentId) {
            /* class android.service.media.MediaBrowserService.AnonymousClass3 */

            /* access modifiers changed from: package-private */
            public void onResultSent(List<MediaBrowser.MediaItem> list, int flag) {
                if (MediaBrowserService.this.mConnections.get(connection.callbacks.asBinder()) == connection) {
                    List<MediaBrowser.MediaItem> filteredList = (flag & 1) != 0 ? MediaBrowserService.this.applyOptions(list, options) : list;
                    try {
                        connection.callbacks.onLoadChildrenWithOptions(parentId, filteredList == null ? null : new ParceledListSlice<>(filteredList), options);
                    } catch (RemoteException e) {
                        Log.w(MediaBrowserService.TAG, "Calling onLoadChildren() failed for id=" + parentId + " package=" + connection.pkg);
                    }
                }
            }
        };
        this.mCurConnection = connection;
        if (options == null) {
            onLoadChildren(parentId, result);
        } else {
            onLoadChildren(parentId, result, options);
        }
        this.mCurConnection = null;
        if (!result.isDone()) {
            throw new IllegalStateException("onLoadChildren must call detach() or sendResult() before returning for package=" + connection.pkg + " id=" + parentId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<MediaBrowser.MediaItem> applyOptions(List<MediaBrowser.MediaItem> list, Bundle options) {
        if (list == null) {
            return null;
        }
        int page = options.getInt(MediaBrowser.EXTRA_PAGE, -1);
        int pageSize = options.getInt(MediaBrowser.EXTRA_PAGE_SIZE, -1);
        if (page == -1 && pageSize == -1) {
            return list;
        }
        int fromIndex = pageSize * page;
        int toIndex = fromIndex + pageSize;
        if (page < 0 || pageSize < 1 || fromIndex >= list.size()) {
            return Collections.EMPTY_LIST;
        }
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        return list.subList(fromIndex, toIndex);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void performLoadItem(final String itemId, final ConnectionRecord connection, final ResultReceiver receiver) {
        Result<MediaBrowser.MediaItem> result = new Result<MediaBrowser.MediaItem>(itemId) {
            /* class android.service.media.MediaBrowserService.AnonymousClass4 */

            /* access modifiers changed from: package-private */
            public void onResultSent(MediaBrowser.MediaItem item, int flag) {
                if (MediaBrowserService.this.mConnections.get(connection.callbacks.asBinder()) == connection) {
                    if ((flag & 2) != 0) {
                        receiver.send(-1, null);
                        return;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(MediaBrowserService.KEY_MEDIA_ITEM, item);
                    receiver.send(0, bundle);
                }
            }
        };
        this.mCurConnection = connection;
        onLoadItem(itemId, result);
        this.mCurConnection = null;
        if (!result.isDone()) {
            throw new IllegalStateException("onLoadItem must call detach() or sendResult() before returning for id=" + itemId);
        }
    }

    public static final class BrowserRoot {
        public static final String EXTRA_OFFLINE = "android.service.media.extra.OFFLINE";
        public static final String EXTRA_RECENT = "android.service.media.extra.RECENT";
        public static final String EXTRA_SUGGESTED = "android.service.media.extra.SUGGESTED";
        private final Bundle mExtras;
        private final String mRootId;

        public BrowserRoot(String rootId, Bundle extras) {
            if (rootId != null) {
                this.mRootId = rootId;
                this.mExtras = extras;
                return;
            }
            throw new IllegalArgumentException("The root id in BrowserRoot cannot be null. Use null for BrowserRoot instead.");
        }

        public String getRootId() {
            return this.mRootId;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }
    }
}
