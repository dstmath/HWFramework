package android.service.media;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.media.browse.MediaBrowser;
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
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class MediaBrowserService extends Service {
    private static final boolean DBG = false;
    public static final String KEY_MEDIA_ITEM = "media_item";
    private static final int RESULT_FLAG_OPTION_NOT_HANDLED = 1;
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";
    private static final String TAG = "MediaBrowserService";
    private ServiceBinder mBinder;
    private final ArrayMap<IBinder, ConnectionRecord> mConnections;
    private ConnectionRecord mCurConnection;
    private final Handler mHandler;
    Token mSession;

    /* renamed from: android.service.media.MediaBrowserService.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Token val$token;

        AnonymousClass1(Token val$token) {
            this.val$token = val$token;
        }

        public void run() {
            for (IBinder key : MediaBrowserService.this.mConnections.keySet()) {
                ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(key);
                try {
                    connection.callbacks.onConnect(connection.root.getRootId(), this.val$token, connection.root.getExtras());
                } catch (RemoteException e) {
                    Log.w(MediaBrowserService.TAG, "Connection for " + connection.pkg + " is no longer valid.");
                    MediaBrowserService.this.mConnections.remove(key);
                }
            }
        }
    }

    /* renamed from: android.service.media.MediaBrowserService.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String val$parentId;

        AnonymousClass2(String val$parentId, Bundle val$options) {
            this.val$parentId = val$parentId;
            this.val$options = val$options;
        }

        public void run() {
            for (IBinder binder : MediaBrowserService.this.mConnections.keySet()) {
                ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(binder);
                List<Pair<IBinder, Bundle>> callbackList = (List) connection.subscriptions.get(this.val$parentId);
                if (callbackList != null) {
                    for (Pair<IBinder, Bundle> callback : callbackList) {
                        if (MediaBrowserUtils.hasDuplicatedItems(this.val$options, (Bundle) callback.second)) {
                            MediaBrowserService.this.performLoadChildren(this.val$parentId, connection, (Bundle) callback.second);
                        }
                    }
                }
            }
        }
    }

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

    /* renamed from: android.service.media.MediaBrowserService.3 */
    class AnonymousClass3 extends Result<List<MediaItem>> {
        final /* synthetic */ MediaBrowserService this$0;
        final /* synthetic */ ConnectionRecord val$connection;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String val$parentId;

        AnonymousClass3(MediaBrowserService this$0, MediaBrowserService this$0_1, Object $anonymous0, ConnectionRecord val$connection, String val$parentId, Bundle val$options) {
            this.this$0 = this$0_1;
            this.val$connection = val$connection;
            this.val$parentId = val$parentId;
            this.val$options = val$options;
            super($anonymous0);
        }

        void onResultSent(List<MediaItem> list, int flag) {
            if (this.this$0.mConnections.get(this.val$connection.callbacks.asBinder()) == this.val$connection) {
                List<MediaItem> filteredList;
                if ((flag & MediaBrowserService.RESULT_FLAG_OPTION_NOT_HANDLED) != 0) {
                    filteredList = this.this$0.applyOptions(list, this.val$options);
                } else {
                    filteredList = list;
                }
                try {
                    this.val$connection.callbacks.onLoadChildrenWithOptions(this.val$parentId, filteredList == null ? null : new ParceledListSlice(filteredList), this.val$options);
                } catch (RemoteException e) {
                    Log.w(MediaBrowserService.TAG, "Calling onLoadChildren() failed for id=" + this.val$parentId + " package=" + this.val$connection.pkg);
                }
            }
        }
    }

    /* renamed from: android.service.media.MediaBrowserService.4 */
    class AnonymousClass4 extends Result<MediaItem> {
        final /* synthetic */ MediaBrowserService this$0;
        final /* synthetic */ ResultReceiver val$receiver;

        AnonymousClass4(MediaBrowserService this$0, MediaBrowserService this$0_1, Object $anonymous0, ResultReceiver val$receiver) {
            this.this$0 = this$0_1;
            this.val$receiver = val$receiver;
            super($anonymous0);
        }

        void onResultSent(MediaItem item, int flag) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(MediaBrowserService.KEY_MEDIA_ITEM, item);
            this.val$receiver.send(0, bundle);
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

        private ConnectionRecord() {
            this.subscriptions = new HashMap();
        }
    }

    private class ServiceBinder extends Stub {

        /* renamed from: android.service.media.MediaBrowserService.ServiceBinder.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$pkg;
            final /* synthetic */ Bundle val$rootHints;
            final /* synthetic */ int val$uid;

            AnonymousClass1(IMediaBrowserServiceCallbacks val$callbacks, String val$pkg, Bundle val$rootHints, int val$uid) {
                this.val$callbacks = val$callbacks;
                this.val$pkg = val$pkg;
                this.val$rootHints = val$rootHints;
                this.val$uid = val$uid;
            }

            public void run() {
                IBinder b = this.val$callbacks.asBinder();
                MediaBrowserService.this.mConnections.remove(b);
                ConnectionRecord connection = new ConnectionRecord(null);
                connection.pkg = this.val$pkg;
                connection.rootHints = this.val$rootHints;
                connection.callbacks = this.val$callbacks;
                connection.root = MediaBrowserService.this.onGetRoot(this.val$pkg, this.val$uid, this.val$rootHints);
                if (connection.root == null) {
                    Log.i(MediaBrowserService.TAG, "No root for client " + this.val$pkg + " from service " + getClass().getName());
                    try {
                        this.val$callbacks.onConnectFailed();
                        return;
                    } catch (RemoteException e) {
                        Log.w(MediaBrowserService.TAG, "Calling onConnectFailed() failed. Ignoring. pkg=" + this.val$pkg);
                        return;
                    }
                }
                try {
                    MediaBrowserService.this.mConnections.put(b, connection);
                    if (MediaBrowserService.this.mSession != null) {
                        this.val$callbacks.onConnect(connection.root.getRootId(), MediaBrowserService.this.mSession, connection.root.getExtras());
                    }
                } catch (RemoteException e2) {
                    Log.w(MediaBrowserService.TAG, "Calling onConnect() failed. Dropping client. pkg=" + this.val$pkg);
                    MediaBrowserService.this.mConnections.remove(b);
                }
            }
        }

        /* renamed from: android.service.media.MediaBrowserService.ServiceBinder.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;

            AnonymousClass2(IMediaBrowserServiceCallbacks val$callbacks) {
                this.val$callbacks = val$callbacks;
            }

            public void run() {
                if (((ConnectionRecord) MediaBrowserService.this.mConnections.remove(this.val$callbacks.asBinder())) == null) {
                }
            }
        }

        /* renamed from: android.service.media.MediaBrowserService.ServiceBinder.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$id;
            final /* synthetic */ Bundle val$options;
            final /* synthetic */ IBinder val$token;

            AnonymousClass3(IMediaBrowserServiceCallbacks val$callbacks, String val$id, IBinder val$token, Bundle val$options) {
                this.val$callbacks = val$callbacks;
                this.val$id = val$id;
                this.val$token = val$token;
                this.val$options = val$options;
            }

            public void run() {
                ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(this.val$callbacks.asBinder());
                if (connection == null) {
                    Log.w(MediaBrowserService.TAG, "addSubscription for callback that isn't registered id=" + this.val$id);
                } else {
                    MediaBrowserService.this.addSubscription(this.val$id, connection, this.val$token, this.val$options);
                }
            }
        }

        /* renamed from: android.service.media.MediaBrowserService.ServiceBinder.4 */
        class AnonymousClass4 implements Runnable {
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$id;
            final /* synthetic */ IBinder val$token;

            AnonymousClass4(IMediaBrowserServiceCallbacks val$callbacks, String val$id, IBinder val$token) {
                this.val$callbacks = val$callbacks;
                this.val$id = val$id;
                this.val$token = val$token;
            }

            public void run() {
                ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(this.val$callbacks.asBinder());
                if (connection == null) {
                    Log.w(MediaBrowserService.TAG, "removeSubscription for callback that isn't registered id=" + this.val$id);
                    return;
                }
                if (!MediaBrowserService.this.removeSubscription(this.val$id, connection, this.val$token)) {
                    Log.w(MediaBrowserService.TAG, "removeSubscription called for " + this.val$id + " which is not subscribed");
                }
            }
        }

        /* renamed from: android.service.media.MediaBrowserService.ServiceBinder.5 */
        class AnonymousClass5 implements Runnable {
            final /* synthetic */ IMediaBrowserServiceCallbacks val$callbacks;
            final /* synthetic */ String val$mediaId;
            final /* synthetic */ ResultReceiver val$receiver;

            AnonymousClass5(IMediaBrowserServiceCallbacks val$callbacks, String val$mediaId, ResultReceiver val$receiver) {
                this.val$callbacks = val$callbacks;
                this.val$mediaId = val$mediaId;
                this.val$receiver = val$receiver;
            }

            public void run() {
                ConnectionRecord connection = (ConnectionRecord) MediaBrowserService.this.mConnections.get(this.val$callbacks.asBinder());
                if (connection == null) {
                    Log.w(MediaBrowserService.TAG, "getMediaItem for callback that isn't registered id=" + this.val$mediaId);
                } else {
                    MediaBrowserService.this.performLoadItem(this.val$mediaId, connection, this.val$receiver);
                }
            }
        }

        private ServiceBinder() {
        }

        public void connect(String pkg, Bundle rootHints, IMediaBrowserServiceCallbacks callbacks) {
            int uid = Binder.getCallingUid();
            if (MediaBrowserService.this.isValidPackage(pkg, uid)) {
                MediaBrowserService.this.mHandler.post(new AnonymousClass1(callbacks, pkg, rootHints, uid));
                return;
            }
            throw new IllegalArgumentException("Package/uid mismatch: uid=" + uid + " package=" + pkg);
        }

        public void disconnect(IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new AnonymousClass2(callbacks));
        }

        public void addSubscriptionDeprecated(String id, IMediaBrowserServiceCallbacks callbacks) {
        }

        public void addSubscription(String id, IBinder token, Bundle options, IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new AnonymousClass3(callbacks, id, token, options));
        }

        public void removeSubscriptionDeprecated(String id, IMediaBrowserServiceCallbacks callbacks) {
        }

        public void removeSubscription(String id, IBinder token, IMediaBrowserServiceCallbacks callbacks) {
            MediaBrowserService.this.mHandler.post(new AnonymousClass4(callbacks, id, token));
        }

        public void getMediaItem(String mediaId, ResultReceiver receiver, IMediaBrowserServiceCallbacks callbacks) {
            if (!TextUtils.isEmpty(mediaId) && receiver != null) {
                MediaBrowserService.this.mHandler.post(new AnonymousClass5(callbacks, mediaId, receiver));
            }
        }
    }

    public abstract BrowserRoot onGetRoot(String str, int i, Bundle bundle);

    public abstract void onLoadChildren(String str, Result<List<MediaItem>> result);

    public MediaBrowserService() {
        this.mConnections = new ArrayMap();
        this.mHandler = new Handler();
    }

    public void onCreate() {
        super.onCreate();
        this.mBinder = new ServiceBinder();
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
        result.setFlags(RESULT_FLAG_OPTION_NOT_HANDLED);
        onLoadChildren(parentId, result);
    }

    public void onLoadItem(String itemId, Result<MediaItem> result) {
        result.sendResult(null);
    }

    public void setSessionToken(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Session token may not be null.");
        } else if (this.mSession != null) {
            throw new IllegalStateException("The session token has already been set.");
        } else {
            this.mSession = token;
            this.mHandler.post(new AnonymousClass1(token));
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

    private void notifyChildrenChangedInternal(String parentId, Bundle options) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
        }
        this.mHandler.post(new AnonymousClass2(parentId, options));
    }

    private boolean isValidPackage(String pkg, int uid) {
        if (pkg == null) {
            return DBG;
        }
        String[] packages = getPackageManager().getPackagesForUid(uid);
        int N = packages.length;
        for (int i = 0; i < N; i += RESULT_FLAG_OPTION_NOT_HANDLED) {
            if (packages[i].equals(pkg)) {
                return true;
            }
        }
        return DBG;
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
        boolean z = DBG;
        if (token == null) {
            if (connection.subscriptions.remove(id) != null) {
                z = true;
            }
            return z;
        }
        boolean removed = DBG;
        List<Pair<IBinder, Bundle>> callbackList = (List) connection.subscriptions.get(id);
        if (callbackList != null) {
            for (Pair<IBinder, Bundle> callback : callbackList) {
                if (token == callback.first) {
                    removed = true;
                    callbackList.remove(callback);
                }
            }
            if (callbackList.size() == 0) {
                connection.subscriptions.remove(id);
            }
        }
        return removed;
    }

    private void performLoadChildren(String parentId, ConnectionRecord connection, Bundle options) {
        Result<List<MediaItem>> result = new AnonymousClass3(this, this, parentId, connection, parentId, options);
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
        int page = options.getInt(MediaBrowser.EXTRA_PAGE, -1);
        int pageSize = options.getInt(MediaBrowser.EXTRA_PAGE_SIZE, -1);
        if (page == -1 && pageSize == -1) {
            return list;
        }
        int fromIndex = pageSize * page;
        int toIndex = fromIndex + pageSize;
        if (page < 0 || pageSize < RESULT_FLAG_OPTION_NOT_HANDLED || fromIndex >= list.size()) {
            return Collections.EMPTY_LIST;
        }
        if (toIndex > list.size()) {
            toIndex = list.size();
        }
        return list.subList(fromIndex, toIndex);
    }

    private void performLoadItem(String itemId, ConnectionRecord connection, ResultReceiver receiver) {
        Result<MediaItem> result = new AnonymousClass4(this, this, itemId, receiver);
        this.mCurConnection = connection;
        onLoadItem(itemId, result);
        this.mCurConnection = null;
        if (!result.isDone()) {
            throw new IllegalStateException("onLoadItem must call detach() or sendResult() before returning for id=" + itemId);
        }
    }
}
