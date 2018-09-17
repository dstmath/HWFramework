package android.service.media;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.media.browse.MediaBrowser.MediaItem;
import android.media.browse.MediaBrowserUtils;
import android.media.session.MediaSession.Token;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.service.media.IMediaBrowserService.Stub;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class MediaBrowserService extends Service {
    private static final boolean DBG = false;
    public static final String KEY_MEDIA_ITEM = "media_item";
    private static final int RESULT_ERROR = -1;
    private static final int RESULT_FLAG_ON_LOAD_ITEM_NOT_IMPLEMENTED = 2;
    private static final int RESULT_FLAG_OPTION_NOT_HANDLED = 1;
    private static final int RESULT_OK = 0;
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";
    private static final String TAG = "MediaBrowserService";
    private ServiceBinder mBinder;
    private final ArrayMap<IBinder, ConnectionRecord> mConnections = new ArrayMap();
    private ConnectionRecord mCurConnection;
    private final Handler mHandler = new Handler();
    Token mSession;

    public class Result<T> {
        private Object mDebug;
        private boolean mDetachCalled;
        private int mFlags;
        private boolean mSendResultCalled;

        Result(Object debug) {
            this.mDebug = debug;
        }

        public void sendResult(T result) {
            if (this.mSendResultCalled) {
                throw new IllegalStateException("sendResult() called twice for: " + this.mDebug);
            }
            this.mSendResultCalled = true;
            onResultSent(result, this.mFlags);
        }

        public void detach() {
            if (this.mDetachCalled) {
                throw new IllegalStateException("detach() called when detach() had already been called for: " + this.mDebug);
            } else if (this.mSendResultCalled) {
                throw new IllegalStateException("detach() called when sendResult() had already been called for: " + this.mDebug);
            } else {
                this.mDetachCalled = true;
            }
        }

        boolean isDone() {
            return !this.mDetachCalled ? this.mSendResultCalled : true;
        }

        void setFlags(int flags) {
            this.mFlags = flags;
        }

        void onResultSent(T t, int flags) {
        }
    }

    public static final class BrowserRoot {
        public static final String EXTRA_OFFLINE = "android.service.media.extra.OFFLINE";
        public static final String EXTRA_RECENT = "android.service.media.extra.RECENT";
        public static final String EXTRA_SUGGESTED = "android.service.media.extra.SUGGESTED";
        private final Bundle mExtras;
        private final String mRootId;

        public BrowserRoot(String rootId, Bundle extras) {
            if (rootId == null) {
                throw new IllegalArgumentException("The root id in BrowserRoot cannot be null. Use null for BrowserRoot instead.");
            }
            this.mRootId = rootId;
            this.mExtras = extras;
        }

        public String getRootId() {
            return this.mRootId;
        }

        public Bundle getExtras() {
            return this.mExtras;
        }
    }

    private class ConnectionRecord {
        IMediaBrowserServiceCallbacks callbacks;
        String pkg;
        BrowserRoot root;
        Bundle rootHints;
        HashMap<String, List<Pair<IBinder, Bundle>>> subscriptions;

        /* synthetic */ ConnectionRecord(MediaBrowserService this$0, ConnectionRecord -this1) {
            this();
        }

        private ConnectionRecord() {
            this.subscriptions = new HashMap();
        }
    }

    private class ServiceBinder extends Stub {
        /* synthetic */ ServiceBinder(MediaBrowserService this$0, ServiceBinder -this1) {
            this();
        }

        private ServiceBinder() {
        }

        public void connect(String pkg, Bundle rootHints, IMediaBrowserServiceCallbacks callbacks) {
            final int uid = Binder.getCallingUid();
            if (MediaBrowserService.this.isValidPackage(pkg, uid)) {
                final IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks = callbacks;
                final String str = pkg;
                final Bundle bundle = rootHints;
                MediaBrowserService.this.mHandler.post(new Runnable() {
                    public void run() {
                        IBinder b = iMediaBrowserServiceCallbacks.asBinder();
                        MediaBrowserService.this.mConnections.remove(b);
                        ConnectionRecord connection = new ConnectionRecord(MediaBrowserService.this, null);
                        connection.pkg = str;
                        connection.rootHints = bundle;
                        connection.callbacks = iMediaBrowserServiceCallbacks;
                        connection.root = MediaBrowserService.this.onGetRoot(str, uid, bundle);
                        if (connection.root == null) {
                            Log.i(MediaBrowserService.TAG, "No root for client " + str + " from service " + getClass().getName());
                            try {
                                iMediaBrowserServiceCallbacks.onConnectFailed();
                                return;
                            } catch (RemoteException e) {
                                Log.w(MediaBrowserService.TAG, "Calling onConnectFailed() failed. Ignoring. pkg=" + str);
                                return;
                            }
                        }
                        try {
                            MediaBrowserService.this.mConnections.put(b, connection);
                            if (MediaBrowserService.this.mSession != null) {
                                iMediaBrowserServiceCallbacks.onConnect(connection.root.getRootId(), MediaBrowserService.this.mSession, connection.root.getExtras());
                            }
                        } catch (RemoteException e2) {
                            Log.w(MediaBrowserService.TAG, "Calling onConnect() failed. Dropping client. pkg=" + str);
                            MediaBrowserService.this.mConnections.remove(b);
                        }
                    }
                });
                return;
            }
            throw new IllegalArgumentException("Package/uid mismatch: uid=" + uid + " package=" + pkg);
        }

        public void disconnect(final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                public void run() {
                    ConnectionRecord old = (ConnectionRecord) MediaBrowserService.this.mConnections.remove(callbacks.asBinder());
                }
            });
        }

        public void addSubscriptionDeprecated(String id, IMediaBrowserServiceCallbacks callbacks) {
        }

        public void addSubscription(String id, IBinder token, Bundle options, IMediaBrowserServiceCallbacks callbacks) {
            final IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks = callbacks;
            final String str = id;
            final IBinder iBinder = token;
            final Bundle bundle = options;
            MediaBrowserService.this.mHandler.post(new Runnable() {
                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(iMediaBrowserServiceCallbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "addSubscription for callback that isn't registered id=" + str);
                    } else {
                        MediaBrowserService.this.addSubscription(str, connection, iBinder, bundle);
                    }
                }
            });
        }

        public void removeSubscriptionDeprecated(String id, IMediaBrowserServiceCallbacks callbacks) {
        }

        public void removeSubscription(final String id, final IBinder token, final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "removeSubscription for callback that isn't registered id=" + id);
                        return;
                    }
                    if (!MediaBrowserService.this.removeSubscription(id, connection, token)) {
                        Log.w(MediaBrowserService.TAG, "removeSubscription called for " + id + " which is not subscribed");
                    }
                }
            });
        }

        public void getMediaItem(final String mediaId, final ResultReceiver receiver, final IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new Runnable() {
                public void run() {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserService.TAG, "getMediaItem for callback that isn't registered id=" + mediaId);
                    } else {
                        MediaBrowserService.this.performLoadItem(mediaId, connection, receiver);
                    }
                }
            });
        }
    }

    public abstract BrowserRoot onGetRoot(String str, int i, Bundle bundle);

    public abstract void onLoadChildren(String str, Result<List<MediaItem>> result);

    public void onCreate() {
        super.onCreate();
        this.mBinder = new ServiceBinder(this, null);
    }

    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return this.mBinder;
        }
        return null;
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public void onLoadChildren(String parentId, Result<List<MediaItem>> result, Bundle options) {
        result.setFlags(1);
        onLoadChildren(parentId, result);
    }

    public void onLoadItem(String itemId, Result<MediaItem> result) {
        result.setFlags(2);
        result.sendResult(null);
    }

    public void setSessionToken(final Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Session token may not be null.");
        } else if (this.mSession != null) {
            throw new IllegalStateException("The session token has already been set.");
        } else {
            this.mSession = token;
            this.mHandler.post(new Runnable() {
                public void run() {
                    Iterator<ConnectionRecord> iter = MediaBrowserService.this.mConnections.values().iterator();
                    while (iter.hasNext()) {
                        ConnectionRecord connection = (ConnectionRecord) iter.next();
                        try {
                            connection.callbacks.onConnect(connection.root.getRootId(), token, connection.root.getExtras());
                        } catch (RemoteException e) {
                            Log.w(MediaBrowserService.TAG, "Connection for " + connection.pkg + " is no longer valid.");
                            iter.remove();
                        }
                    }
                }
            });
        }
    }

    public Token getSessionToken() {
        return this.mSession;
    }

    public final Bundle getBrowserRootHints() {
        if (this.mCurConnection == null) {
            throw new IllegalStateException("This should be called inside of onLoadChildren or onLoadItem methods");
        } else if (this.mCurConnection.rootHints == null) {
            return null;
        } else {
            return new Bundle(this.mCurConnection.rootHints);
        }
    }

    public void notifyChildrenChanged(String parentId) {
        notifyChildrenChangedInternal(parentId, null);
    }

    public void notifyChildrenChanged(String parentId, Bundle options) {
        if (options == null) {
            throw new IllegalArgumentException("options cannot be null in notifyChildrenChanged");
        }
        notifyChildrenChangedInternal(parentId, options);
    }

    private void notifyChildrenChangedInternal(final String parentId, final Bundle options) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                for (IBinder binder : MediaBrowserService.this.mConnections.keySet()) {
                    ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(binder);
                    List<Pair<IBinder, Bundle>> callbackList = (List) connection.subscriptions.get(parentId);
                    if (callbackList != null) {
                        for (Pair<IBinder, Bundle> callback : callbackList) {
                            if (MediaBrowserUtils.hasDuplicatedItems(options, (Bundle) callback.second)) {
                                MediaBrowserService.this.performLoadChildren(parentId, connection, (Bundle) callback.second);
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean isValidPackage(String pkg, int uid) {
        if (pkg == null) {
            return false;
        }
        for (String equals : getPackageManager().getPackagesForUid(uid)) {
            if (equals.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    private void addSubscription(String id, ConnectionRecord connection, IBinder token, Bundle options) {
        List<Pair<IBinder, Bundle>> callbackList = (List) connection.subscriptions.get(id);
        if (callbackList == null) {
            callbackList = new ArrayList();
        }
        for (Pair<IBinder, Bundle> callback : callbackList) {
            if (token == callback.first && MediaBrowserUtils.areSameOptions(options, (Bundle) callback.second)) {
                return;
            }
        }
        callbackList.add(new Pair(token, options));
        connection.subscriptions.put(id, callbackList);
        performLoadChildren(id, connection, options);
    }

    private boolean removeSubscription(String id, ConnectionRecord connection, IBinder token) {
        boolean z = false;
        if (token == null) {
            if (connection.subscriptions.remove(id) != null) {
                z = true;
            }
            return z;
        }
        boolean removed = false;
        List<Pair<IBinder, Bundle>> callbackList = (List) connection.subscriptions.get(id);
        if (callbackList != null) {
            Iterator<Pair<IBinder, Bundle>> iter = callbackList.iterator();
            while (iter.hasNext()) {
                if (token == ((Pair) iter.next()).first) {
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

    private void performLoadChildren(String parentId, ConnectionRecord connection, Bundle options) {
        final ConnectionRecord connectionRecord = connection;
        final String str = parentId;
        final Bundle bundle = options;
        Result<List<MediaItem>> result = new Result<List<MediaItem>>(this, parentId) {
            void onResultSent(List<MediaItem> list, int flag) {
                if (this.mConnections.get(connectionRecord.callbacks.asBinder()) == connectionRecord) {
                    List<MediaItem> filteredList = (flag & 1) != 0 ? this.applyOptions(list, bundle) : list;
                    try {
                        connectionRecord.callbacks.onLoadChildrenWithOptions(str, filteredList == null ? null : new ParceledListSlice(filteredList), bundle);
                    } catch (RemoteException e) {
                        Log.w(MediaBrowserService.TAG, "Calling onLoadChildren() failed for id=" + str + " package=" + connectionRecord.pkg);
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

    private List<MediaItem> applyOptions(List<MediaItem> list, Bundle options) {
        if (list == null) {
            return null;
        }
        int page = options.getInt("android.media.browse.extra.PAGE", -1);
        int pageSize = options.getInt("android.media.browse.extra.PAGE_SIZE", -1);
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

    private void performLoadItem(String itemId, ConnectionRecord connection, final ResultReceiver receiver) {
        Result<MediaItem> result = new Result<MediaItem>(this, itemId) {
            void onResultSent(MediaItem item, int flag) {
                if ((flag & 2) != 0) {
                    receiver.send(-1, null);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(MediaBrowserService.KEY_MEDIA_ITEM, item);
                receiver.send(0, bundle);
            }
        };
        this.mCurConnection = connection;
        onLoadItem(itemId, result);
        this.mCurConnection = null;
        if (!result.isDone()) {
            throw new IllegalStateException("onLoadItem must call detach() or sendResult() before returning for id=" + itemId);
        }
    }
}
