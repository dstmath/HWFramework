package android.support.v4.media;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.v4.app.BundleCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompatApi21;
import android.support.v4.media.MediaBrowserServiceCompatApi23;
import android.support.v4.media.MediaBrowserServiceCompatApi26;
import android.support.v4.media.MediaSessionManager;
import android.support.v4.media.session.IMediaSession;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class MediaBrowserServiceCompat extends Service {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final float EPSILON = 1.0E-5f;
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final String KEY_MEDIA_ITEM = "media_item";
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final String KEY_SEARCH_RESULTS = "search_results";
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final int RESULT_ERROR = -1;
    static final int RESULT_FLAG_ON_LOAD_ITEM_NOT_IMPLEMENTED = 2;
    static final int RESULT_FLAG_ON_SEARCH_NOT_IMPLEMENTED = 4;
    static final int RESULT_FLAG_OPTION_NOT_HANDLED = 1;
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final int RESULT_OK = 0;
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static final int RESULT_PROGRESS_UPDATE = 1;
    public static final String SERVICE_INTERFACE = "android.media.browse.MediaBrowserService";
    static final String TAG = "MBServiceCompat";
    final ArrayMap<IBinder, ConnectionRecord> mConnections = new ArrayMap<>();
    ConnectionRecord mCurConnection;
    final ServiceHandler mHandler = new ServiceHandler();
    private MediaBrowserServiceImpl mImpl;
    MediaSessionCompat.Token mSession;

    /* access modifiers changed from: package-private */
    public interface MediaBrowserServiceImpl {
        Bundle getBrowserRootHints();

        MediaSessionManager.RemoteUserInfo getCurrentBrowserInfo();

        List<MediaSessionManager.RemoteUserInfo> getSubscribingBrowsers(String str);

        void notifyChildrenChanged(String str, Bundle bundle);

        IBinder onBind(Intent intent);

        void onCreate();

        void setSessionToken(MediaSessionCompat.Token token);
    }

    /* access modifiers changed from: private */
    public interface ServiceCallbacks {
        IBinder asBinder();

        void onConnect(String str, MediaSessionCompat.Token token, Bundle bundle) throws RemoteException;

        void onConnectFailed() throws RemoteException;

        void onLoadChildren(String str, List<MediaBrowserCompat.MediaItem> list, Bundle bundle, Bundle bundle2) throws RemoteException;
    }

    @Nullable
    public abstract BrowserRoot onGetRoot(@NonNull String str, int i, @Nullable Bundle bundle);

    public abstract void onLoadChildren(@NonNull String str, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result);

    /* access modifiers changed from: package-private */
    public class MediaBrowserServiceImplBase implements MediaBrowserServiceImpl {
        private Messenger mMessenger;

        MediaBrowserServiceImplBase() {
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void onCreate() {
            this.mMessenger = new Messenger(MediaBrowserServiceCompat.this.mHandler);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public IBinder onBind(Intent intent) {
            if (MediaBrowserServiceCompat.SERVICE_INTERFACE.equals(intent.getAction())) {
                return this.mMessenger.getBinder();
            }
            return null;
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void setSessionToken(final MediaSessionCompat.Token token) {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplBase.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    Iterator<ConnectionRecord> iter = MediaBrowserServiceCompat.this.mConnections.values().iterator();
                    while (iter.hasNext()) {
                        ConnectionRecord connection = iter.next();
                        try {
                            connection.callbacks.onConnect(connection.root.getRootId(), token, connection.root.getExtras());
                        } catch (RemoteException e) {
                            Log.w(MediaBrowserServiceCompat.TAG, "Connection for " + connection.pkg + " is no longer valid.");
                            iter.remove();
                        }
                    }
                }
            });
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void notifyChildrenChanged(@NonNull final String parentId, final Bundle options) {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplBase.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    for (IBinder binder : MediaBrowserServiceCompat.this.mConnections.keySet()) {
                        ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(binder);
                        List<Pair<IBinder, Bundle>> callbackList = connection.subscriptions.get(parentId);
                        if (callbackList != null) {
                            for (Pair<IBinder, Bundle> callback : callbackList) {
                                if (MediaBrowserCompatUtils.hasDuplicatedItems(options, callback.second)) {
                                    MediaBrowserServiceCompat.this.performLoadChildren(parentId, connection, callback.second, options);
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public Bundle getBrowserRootHints() {
            if (MediaBrowserServiceCompat.this.mCurConnection == null) {
                throw new IllegalStateException("This should be called inside of onLoadChildren, onLoadItem, onSearch, or onCustomAction methods");
            } else if (MediaBrowserServiceCompat.this.mCurConnection.rootHints == null) {
                return null;
            } else {
                return new Bundle(MediaBrowserServiceCompat.this.mCurConnection.rootHints);
            }
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public MediaSessionManager.RemoteUserInfo getCurrentBrowserInfo() {
            if (MediaBrowserServiceCompat.this.mCurConnection != null) {
                return MediaBrowserServiceCompat.this.mCurConnection.browserInfo;
            }
            throw new IllegalStateException("This should be called inside of onLoadChildren, onLoadItem, onSearch, or onCustomAction methods");
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public List<MediaSessionManager.RemoteUserInfo> getSubscribingBrowsers(String parentId) {
            List<MediaSessionManager.RemoteUserInfo> result = new ArrayList<>();
            for (IBinder binder : MediaBrowserServiceCompat.this.mConnections.keySet()) {
                ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(binder);
                if (connection.subscriptions.get(parentId) != null) {
                    result.add(connection.browserInfo);
                }
            }
            return result;
        }
    }

    /* access modifiers changed from: package-private */
    @RequiresApi(21)
    public class MediaBrowserServiceImplApi21 implements MediaBrowserServiceImpl, MediaBrowserServiceCompatApi21.ServiceCompatProxy {
        Messenger mMessenger;
        final List<Bundle> mRootExtrasList = new ArrayList();
        Object mServiceObj;

        MediaBrowserServiceImplApi21() {
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void onCreate() {
            this.mServiceObj = MediaBrowserServiceCompatApi21.createService(MediaBrowserServiceCompat.this, this);
            MediaBrowserServiceCompatApi21.onCreate(this.mServiceObj);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public IBinder onBind(Intent intent) {
            return MediaBrowserServiceCompatApi21.onBind(this.mServiceObj, intent);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void setSessionToken(final MediaSessionCompat.Token token) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    if (!MediaBrowserServiceImplApi21.this.mRootExtrasList.isEmpty()) {
                        IMediaSession extraBinder = token.getExtraBinder();
                        if (extraBinder != null) {
                            for (Bundle rootExtras : MediaBrowserServiceImplApi21.this.mRootExtrasList) {
                                BundleCompat.putBinder(rootExtras, MediaBrowserProtocol.EXTRA_SESSION_BINDER, extraBinder.asBinder());
                            }
                        }
                        MediaBrowserServiceImplApi21.this.mRootExtrasList.clear();
                    }
                    MediaBrowserServiceCompatApi21.setSessionToken(MediaBrowserServiceImplApi21.this.mServiceObj, token.getToken());
                }
            });
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void notifyChildrenChanged(String parentId, Bundle options) {
            notifyChildrenChangedForFramework(parentId, options);
            notifyChildrenChangedForCompat(parentId, options);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompatApi21.ServiceCompatProxy
        public MediaBrowserServiceCompatApi21.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
            IBinder iBinder;
            Bundle rootExtras = null;
            if (!(rootHints == null || rootHints.getInt(MediaBrowserProtocol.EXTRA_CLIENT_VERSION, 0) == 0)) {
                rootHints.remove(MediaBrowserProtocol.EXTRA_CLIENT_VERSION);
                this.mMessenger = new Messenger(MediaBrowserServiceCompat.this.mHandler);
                rootExtras = new Bundle();
                rootExtras.putInt(MediaBrowserProtocol.EXTRA_SERVICE_VERSION, 2);
                BundleCompat.putBinder(rootExtras, MediaBrowserProtocol.EXTRA_MESSENGER_BINDER, this.mMessenger.getBinder());
                if (MediaBrowserServiceCompat.this.mSession != null) {
                    IMediaSession extraBinder = MediaBrowserServiceCompat.this.mSession.getExtraBinder();
                    if (extraBinder == null) {
                        iBinder = null;
                    } else {
                        iBinder = extraBinder.asBinder();
                    }
                    BundleCompat.putBinder(rootExtras, MediaBrowserProtocol.EXTRA_SESSION_BINDER, iBinder);
                } else {
                    this.mRootExtrasList.add(rootExtras);
                }
            }
            MediaBrowserServiceCompat.this.mCurConnection = new ConnectionRecord(clientPackageName, -1, clientUid, rootHints, null);
            BrowserRoot root = MediaBrowserServiceCompat.this.onGetRoot(clientPackageName, clientUid, rootHints);
            MediaBrowserServiceCompat.this.mCurConnection = null;
            if (root == null) {
                return null;
            }
            if (rootExtras == null) {
                rootExtras = root.getExtras();
            } else if (root.getExtras() != null) {
                rootExtras.putAll(root.getExtras());
            }
            return new MediaBrowserServiceCompatApi21.BrowserRoot(root.getRootId(), rootExtras);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompatApi21.ServiceCompatProxy
        public void onLoadChildren(String parentId, final MediaBrowserServiceCompatApi21.ResultWrapper<List<Parcel>> resultWrapper) {
            MediaBrowserServiceCompat.this.onLoadChildren(parentId, new Result<List<MediaBrowserCompat.MediaItem>>(parentId) {
                /* class android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21.AnonymousClass2 */

                /* access modifiers changed from: package-private */
                public void onResultSent(List<MediaBrowserCompat.MediaItem> list) {
                    ArrayList arrayList = null;
                    if (list != null) {
                        arrayList = new ArrayList();
                        for (MediaBrowserCompat.MediaItem item : list) {
                            Parcel parcel = Parcel.obtain();
                            item.writeToParcel(parcel, 0);
                            arrayList.add(parcel);
                        }
                    }
                    resultWrapper.sendResult(arrayList);
                }

                @Override // android.support.v4.media.MediaBrowserServiceCompat.Result
                public void detach() {
                    resultWrapper.detach();
                }
            });
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public List<MediaSessionManager.RemoteUserInfo> getSubscribingBrowsers(String parentId) {
            List<MediaSessionManager.RemoteUserInfo> result = new ArrayList<>();
            for (IBinder binder : MediaBrowserServiceCompat.this.mConnections.keySet()) {
                ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(binder);
                if (connection.subscriptions.get(parentId) != null) {
                    result.add(connection.browserInfo);
                }
            }
            return result;
        }

        /* access modifiers changed from: package-private */
        public void notifyChildrenChangedForFramework(String parentId, Bundle options) {
            MediaBrowserServiceCompatApi21.notifyChildrenChanged(this.mServiceObj, parentId);
        }

        /* access modifiers changed from: package-private */
        public void notifyChildrenChangedForCompat(final String parentId, final Bundle options) {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    for (IBinder binder : MediaBrowserServiceCompat.this.mConnections.keySet()) {
                        ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(binder);
                        List<Pair<IBinder, Bundle>> callbackList = connection.subscriptions.get(parentId);
                        if (callbackList != null) {
                            for (Pair<IBinder, Bundle> callback : callbackList) {
                                if (MediaBrowserCompatUtils.hasDuplicatedItems(options, callback.second)) {
                                    MediaBrowserServiceCompat.this.performLoadChildren(parentId, connection, callback.second, options);
                                }
                            }
                        }
                    }
                }
            });
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public Bundle getBrowserRootHints() {
            if (this.mMessenger == null) {
                return null;
            }
            if (MediaBrowserServiceCompat.this.mCurConnection == null) {
                throw new IllegalStateException("This should be called inside of onGetRoot, onLoadChildren, onLoadItem, onSearch, or onCustomAction methods");
            } else if (MediaBrowserServiceCompat.this.mCurConnection.rootHints == null) {
                return null;
            } else {
                return new Bundle(MediaBrowserServiceCompat.this.mCurConnection.rootHints);
            }
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public MediaSessionManager.RemoteUserInfo getCurrentBrowserInfo() {
            if (MediaBrowserServiceCompat.this.mCurConnection != null) {
                return MediaBrowserServiceCompat.this.mCurConnection.browserInfo;
            }
            throw new IllegalStateException("This should be called inside of onGetRoot, onLoadChildren, onLoadItem, onSearch, or onCustomAction methods");
        }
    }

    /* access modifiers changed from: package-private */
    @RequiresApi(23)
    public class MediaBrowserServiceImplApi23 extends MediaBrowserServiceImplApi21 implements MediaBrowserServiceCompatApi23.ServiceCompatProxy {
        MediaBrowserServiceImplApi23() {
            super();
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21, android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void onCreate() {
            this.mServiceObj = MediaBrowserServiceCompatApi23.createService(MediaBrowserServiceCompat.this, this);
            MediaBrowserServiceCompatApi21.onCreate(this.mServiceObj);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompatApi23.ServiceCompatProxy
        public void onLoadItem(String itemId, final MediaBrowserServiceCompatApi21.ResultWrapper<Parcel> resultWrapper) {
            MediaBrowserServiceCompat.this.onLoadItem(itemId, new Result<MediaBrowserCompat.MediaItem>(itemId) {
                /* class android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi23.AnonymousClass1 */

                /* access modifiers changed from: package-private */
                public void onResultSent(MediaBrowserCompat.MediaItem item) {
                    if (item == null) {
                        resultWrapper.sendResult(null);
                        return;
                    }
                    Parcel parcelItem = Parcel.obtain();
                    item.writeToParcel(parcelItem, 0);
                    resultWrapper.sendResult(parcelItem);
                }

                @Override // android.support.v4.media.MediaBrowserServiceCompat.Result
                public void detach() {
                    resultWrapper.detach();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    @RequiresApi(26)
    public class MediaBrowserServiceImplApi26 extends MediaBrowserServiceImplApi23 implements MediaBrowserServiceCompatApi26.ServiceCompatProxy {
        MediaBrowserServiceImplApi26() {
            super();
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi23, android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21, android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public void onCreate() {
            this.mServiceObj = MediaBrowserServiceCompatApi26.createService(MediaBrowserServiceCompat.this, this);
            MediaBrowserServiceCompatApi21.onCreate(this.mServiceObj);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompatApi26.ServiceCompatProxy
        public void onLoadChildren(String parentId, final MediaBrowserServiceCompatApi26.ResultWrapper resultWrapper, Bundle options) {
            MediaBrowserServiceCompat.this.onLoadChildren(parentId, new Result<List<MediaBrowserCompat.MediaItem>>(parentId) {
                /* class android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi26.AnonymousClass1 */

                /* access modifiers changed from: package-private */
                public void onResultSent(List<MediaBrowserCompat.MediaItem> list) {
                    List<Parcel> parcelList = null;
                    if (list != null) {
                        parcelList = new ArrayList<>();
                        for (MediaBrowserCompat.MediaItem item : list) {
                            Parcel parcel = Parcel.obtain();
                            item.writeToParcel(parcel, 0);
                            parcelList.add(parcel);
                        }
                    }
                    resultWrapper.sendResult(parcelList, getFlags());
                }

                @Override // android.support.v4.media.MediaBrowserServiceCompat.Result
                public void detach() {
                    resultWrapper.detach();
                }
            }, options);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21, android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public Bundle getBrowserRootHints() {
            if (MediaBrowserServiceCompat.this.mCurConnection == null) {
                return MediaBrowserServiceCompatApi26.getBrowserRootHints(this.mServiceObj);
            }
            if (MediaBrowserServiceCompat.this.mCurConnection.rootHints == null) {
                return null;
            }
            return new Bundle(MediaBrowserServiceCompat.this.mCurConnection.rootHints);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21
        public void notifyChildrenChangedForFramework(String parentId, Bundle options) {
            if (options != null) {
                MediaBrowserServiceCompatApi26.notifyChildrenChanged(this.mServiceObj, parentId, options);
            } else {
                super.notifyChildrenChangedForFramework(parentId, options);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @RequiresApi(28)
    public class MediaBrowserServiceImplApi28 extends MediaBrowserServiceImplApi26 {
        MediaBrowserServiceImplApi28() {
            super();
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImplApi21, android.support.v4.media.MediaBrowserServiceCompat.MediaBrowserServiceImpl
        public MediaSessionManager.RemoteUserInfo getCurrentBrowserInfo() {
            if (MediaBrowserServiceCompat.this.mCurConnection != null) {
                return MediaBrowserServiceCompat.this.mCurConnection.browserInfo;
            }
            MediaSessionManager.RemoteUserInfo userInfoObj = ((MediaBrowserService) this.mServiceObj).getCurrentBrowserInfo();
            return new MediaSessionManager.RemoteUserInfo(userInfoObj.getPackageName(), userInfoObj.getPid(), userInfoObj.getUid());
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceHandler extends Handler {
        private final ServiceBinderImpl mServiceBinderImpl = new ServiceBinderImpl();

        ServiceHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what) {
                case 1:
                    this.mServiceBinderImpl.connect(data.getString(MediaBrowserProtocol.DATA_PACKAGE_NAME), data.getInt(MediaBrowserProtocol.DATA_CALLING_PID), data.getInt(MediaBrowserProtocol.DATA_CALLING_UID), data.getBundle(MediaBrowserProtocol.DATA_ROOT_HINTS), new ServiceCallbacksCompat(msg.replyTo));
                    return;
                case 2:
                    this.mServiceBinderImpl.disconnect(new ServiceCallbacksCompat(msg.replyTo));
                    return;
                case 3:
                    this.mServiceBinderImpl.addSubscription(data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), BundleCompat.getBinder(data, MediaBrowserProtocol.DATA_CALLBACK_TOKEN), data.getBundle(MediaBrowserProtocol.DATA_OPTIONS), new ServiceCallbacksCompat(msg.replyTo));
                    return;
                case 4:
                    this.mServiceBinderImpl.removeSubscription(data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), BundleCompat.getBinder(data, MediaBrowserProtocol.DATA_CALLBACK_TOKEN), new ServiceCallbacksCompat(msg.replyTo));
                    return;
                case 5:
                    this.mServiceBinderImpl.getMediaItem(data.getString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID), (ResultReceiver) data.getParcelable(MediaBrowserProtocol.DATA_RESULT_RECEIVER), new ServiceCallbacksCompat(msg.replyTo));
                    return;
                case 6:
                    this.mServiceBinderImpl.registerCallbacks(new ServiceCallbacksCompat(msg.replyTo), data.getString(MediaBrowserProtocol.DATA_PACKAGE_NAME), data.getInt(MediaBrowserProtocol.DATA_CALLING_PID), data.getInt(MediaBrowserProtocol.DATA_CALLING_UID), data.getBundle(MediaBrowserProtocol.DATA_ROOT_HINTS));
                    return;
                case 7:
                    this.mServiceBinderImpl.unregisterCallbacks(new ServiceCallbacksCompat(msg.replyTo));
                    return;
                case 8:
                    this.mServiceBinderImpl.search(data.getString(MediaBrowserProtocol.DATA_SEARCH_QUERY), data.getBundle(MediaBrowserProtocol.DATA_SEARCH_EXTRAS), (ResultReceiver) data.getParcelable(MediaBrowserProtocol.DATA_RESULT_RECEIVER), new ServiceCallbacksCompat(msg.replyTo));
                    return;
                case 9:
                    this.mServiceBinderImpl.sendCustomAction(data.getString(MediaBrowserProtocol.DATA_CUSTOM_ACTION), data.getBundle(MediaBrowserProtocol.DATA_CUSTOM_ACTION_EXTRAS), (ResultReceiver) data.getParcelable(MediaBrowserProtocol.DATA_RESULT_RECEIVER), new ServiceCallbacksCompat(msg.replyTo));
                    return;
                default:
                    Log.w(MediaBrowserServiceCompat.TAG, "Unhandled message: " + msg + "\n  Service version: 2\n  Client version: " + msg.arg1);
                    return;
            }
        }

        @Override // android.os.Handler
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            Bundle data = msg.getData();
            data.setClassLoader(MediaBrowserCompat.class.getClassLoader());
            data.putInt(MediaBrowserProtocol.DATA_CALLING_UID, Binder.getCallingUid());
            data.putInt(MediaBrowserProtocol.DATA_CALLING_PID, Binder.getCallingPid());
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        public void postOrRun(Runnable r) {
            if (Thread.currentThread() == getLooper().getThread()) {
                r.run();
            } else {
                post(r);
            }
        }
    }

    /* access modifiers changed from: private */
    public class ConnectionRecord implements IBinder.DeathRecipient {
        public final MediaSessionManager.RemoteUserInfo browserInfo;
        public final ServiceCallbacks callbacks;
        public final int pid;
        public final String pkg;
        public BrowserRoot root;
        public final Bundle rootHints;
        public final HashMap<String, List<Pair<IBinder, Bundle>>> subscriptions = new HashMap<>();
        public final int uid;

        ConnectionRecord(String pkg2, int pid2, int uid2, Bundle rootHints2, ServiceCallbacks callback) {
            this.pkg = pkg2;
            this.pid = pid2;
            this.uid = uid2;
            this.browserInfo = new MediaSessionManager.RemoteUserInfo(pkg2, pid2, uid2);
            this.rootHints = rootHints2;
            this.callbacks = callback;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            MediaBrowserServiceCompat.this.mHandler.post(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.ConnectionRecord.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    MediaBrowserServiceCompat.this.mConnections.remove(ConnectionRecord.this.callbacks.asBinder());
                }
            });
        }
    }

    public static class Result<T> {
        private final Object mDebug;
        private boolean mDetachCalled;
        private int mFlags;
        private boolean mSendErrorCalled;
        private boolean mSendProgressUpdateCalled;
        private boolean mSendResultCalled;

        Result(Object debug) {
            this.mDebug = debug;
        }

        public void sendResult(T result) {
            if (this.mSendResultCalled || this.mSendErrorCalled) {
                throw new IllegalStateException("sendResult() called when either sendResult() or sendError() had already been called for: " + this.mDebug);
            }
            this.mSendResultCalled = true;
            onResultSent(result);
        }

        public void sendProgressUpdate(Bundle extras) {
            if (this.mSendResultCalled || this.mSendErrorCalled) {
                throw new IllegalStateException("sendProgressUpdate() called when either sendResult() or sendError() had already been called for: " + this.mDebug);
            }
            checkExtraFields(extras);
            this.mSendProgressUpdateCalled = true;
            onProgressUpdateSent(extras);
        }

        public void sendError(Bundle extras) {
            if (this.mSendResultCalled || this.mSendErrorCalled) {
                throw new IllegalStateException("sendError() called when either sendResult() or sendError() had already been called for: " + this.mDebug);
            }
            this.mSendErrorCalled = true;
            onErrorSent(extras);
        }

        public void detach() {
            if (this.mDetachCalled) {
                throw new IllegalStateException("detach() called when detach() had already been called for: " + this.mDebug);
            } else if (this.mSendResultCalled) {
                throw new IllegalStateException("detach() called when sendResult() had already been called for: " + this.mDebug);
            } else if (!this.mSendErrorCalled) {
                this.mDetachCalled = true;
            } else {
                throw new IllegalStateException("detach() called when sendError() had already been called for: " + this.mDebug);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isDone() {
            if (this.mDetachCalled || this.mSendResultCalled || this.mSendErrorCalled) {
                return true;
            }
            return MediaBrowserServiceCompat.DEBUG;
        }

        /* access modifiers changed from: package-private */
        public void setFlags(int flags) {
            this.mFlags = flags;
        }

        /* access modifiers changed from: package-private */
        public int getFlags() {
            return this.mFlags;
        }

        /* access modifiers changed from: package-private */
        public void onResultSent(T t) {
        }

        /* access modifiers changed from: package-private */
        public void onProgressUpdateSent(Bundle extras) {
            throw new UnsupportedOperationException("It is not supported to send an interim update for " + this.mDebug);
        }

        /* access modifiers changed from: package-private */
        public void onErrorSent(Bundle extras) {
            throw new UnsupportedOperationException("It is not supported to send an error for " + this.mDebug);
        }

        private void checkExtraFields(Bundle extras) {
            if (extras != null && extras.containsKey(MediaBrowserCompat.EXTRA_DOWNLOAD_PROGRESS)) {
                float value = extras.getFloat(MediaBrowserCompat.EXTRA_DOWNLOAD_PROGRESS);
                if (value < -1.0E-5f || value > 1.00001f) {
                    throw new IllegalArgumentException("The value of the EXTRA_DOWNLOAD_PROGRESS field must be a float number within [0.0, 1.0].");
                }
            }
        }
    }

    private class ServiceBinderImpl {
        ServiceBinderImpl() {
        }

        public void connect(final String pkg, final int pid, final int uid, final Bundle rootHints, final ServiceCallbacks callbacks) {
            if (MediaBrowserServiceCompat.this.isValidPackage(pkg, uid)) {
                MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                    /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        IBinder b = callbacks.asBinder();
                        MediaBrowserServiceCompat.this.mConnections.remove(b);
                        ConnectionRecord connection = new ConnectionRecord(pkg, pid, uid, rootHints, callbacks);
                        MediaBrowserServiceCompat.this.mCurConnection = connection;
                        connection.root = MediaBrowserServiceCompat.this.onGetRoot(pkg, uid, rootHints);
                        MediaBrowserServiceCompat.this.mCurConnection = null;
                        if (connection.root == null) {
                            Log.i(MediaBrowserServiceCompat.TAG, "No root for client " + pkg + " from service " + getClass().getName());
                            try {
                                callbacks.onConnectFailed();
                            } catch (RemoteException e) {
                                Log.w(MediaBrowserServiceCompat.TAG, "Calling onConnectFailed() failed. Ignoring. pkg=" + pkg);
                            }
                        } else {
                            try {
                                MediaBrowserServiceCompat.this.mConnections.put(b, connection);
                                b.linkToDeath(connection, 0);
                                if (MediaBrowserServiceCompat.this.mSession != null) {
                                    callbacks.onConnect(connection.root.getRootId(), MediaBrowserServiceCompat.this.mSession, connection.root.getExtras());
                                }
                            } catch (RemoteException e2) {
                                Log.w(MediaBrowserServiceCompat.TAG, "Calling onConnect() failed. Dropping client. pkg=" + pkg);
                                MediaBrowserServiceCompat.this.mConnections.remove(b);
                            }
                        }
                    }
                });
                return;
            }
            throw new IllegalArgumentException("Package/uid mismatch: uid=" + uid + " package=" + pkg);
        }

        public void disconnect(final ServiceCallbacks callbacks) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    ConnectionRecord old = MediaBrowserServiceCompat.this.mConnections.remove(callbacks.asBinder());
                    if (old != null) {
                        old.callbacks.asBinder().unlinkToDeath(old, 0);
                    }
                }
            });
        }

        public void addSubscription(final String id, final IBinder token, final Bundle options, final ServiceCallbacks callbacks) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserServiceCompat.TAG, "addSubscription for callback that isn't registered id=" + id);
                        return;
                    }
                    MediaBrowserServiceCompat.this.addSubscription(id, connection, token, options);
                }
            });
        }

        public void removeSubscription(final String id, final IBinder token, final ServiceCallbacks callbacks) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(callbacks.asBinder());
                    if (connection == null) {
                        Log.w(MediaBrowserServiceCompat.TAG, "removeSubscription for callback that isn't registered id=" + id);
                    } else if (!MediaBrowserServiceCompat.this.removeSubscription(id, connection, token)) {
                        Log.w(MediaBrowserServiceCompat.TAG, "removeSubscription called for " + id + " which is not subscribed");
                    }
                }
            });
        }

        public void getMediaItem(final String mediaId, final ResultReceiver receiver, final ServiceCallbacks callbacks) {
            if (!TextUtils.isEmpty(mediaId) && receiver != null) {
                MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                    /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass5 */

                    @Override // java.lang.Runnable
                    public void run() {
                        ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(callbacks.asBinder());
                        if (connection == null) {
                            Log.w(MediaBrowserServiceCompat.TAG, "getMediaItem for callback that isn't registered id=" + mediaId);
                            return;
                        }
                        MediaBrowserServiceCompat.this.performLoadItem(mediaId, connection, receiver);
                    }
                });
            }
        }

        public void registerCallbacks(final ServiceCallbacks callbacks, final String pkg, final int pid, final int uid, final Bundle rootHints) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    IBinder b = callbacks.asBinder();
                    MediaBrowserServiceCompat.this.mConnections.remove(b);
                    ConnectionRecord connection = new ConnectionRecord(pkg, pid, uid, rootHints, callbacks);
                    MediaBrowserServiceCompat.this.mConnections.put(b, connection);
                    try {
                        b.linkToDeath(connection, 0);
                    } catch (RemoteException e) {
                        Log.w(MediaBrowserServiceCompat.TAG, "IBinder is already dead.");
                    }
                }
            });
        }

        public void unregisterCallbacks(final ServiceCallbacks callbacks) {
            MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass7 */

                @Override // java.lang.Runnable
                public void run() {
                    IBinder b = callbacks.asBinder();
                    ConnectionRecord old = MediaBrowserServiceCompat.this.mConnections.remove(b);
                    if (old != null) {
                        b.unlinkToDeath(old, 0);
                    }
                }
            });
        }

        public void search(final String query, final Bundle extras, final ResultReceiver receiver, final ServiceCallbacks callbacks) {
            if (!TextUtils.isEmpty(query) && receiver != null) {
                MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                    /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass8 */

                    @Override // java.lang.Runnable
                    public void run() {
                        ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(callbacks.asBinder());
                        if (connection == null) {
                            Log.w(MediaBrowserServiceCompat.TAG, "search for callback that isn't registered query=" + query);
                            return;
                        }
                        MediaBrowserServiceCompat.this.performSearch(query, extras, connection, receiver);
                    }
                });
            }
        }

        public void sendCustomAction(final String action, final Bundle extras, final ResultReceiver receiver, final ServiceCallbacks callbacks) {
            if (!TextUtils.isEmpty(action) && receiver != null) {
                MediaBrowserServiceCompat.this.mHandler.postOrRun(new Runnable() {
                    /* class android.support.v4.media.MediaBrowserServiceCompat.ServiceBinderImpl.AnonymousClass9 */

                    @Override // java.lang.Runnable
                    public void run() {
                        ConnectionRecord connection = MediaBrowserServiceCompat.this.mConnections.get(callbacks.asBinder());
                        if (connection == null) {
                            Log.w(MediaBrowserServiceCompat.TAG, "sendCustomAction for callback that isn't registered action=" + action + ", extras=" + extras);
                            return;
                        }
                        MediaBrowserServiceCompat.this.performCustomAction(action, extras, connection, receiver);
                    }
                });
            }
        }
    }

    private static class ServiceCallbacksCompat implements ServiceCallbacks {
        final Messenger mCallbacks;

        ServiceCallbacksCompat(Messenger callbacks) {
            this.mCallbacks = callbacks;
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.ServiceCallbacks
        public IBinder asBinder() {
            return this.mCallbacks.getBinder();
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.ServiceCallbacks
        public void onConnect(String root, MediaSessionCompat.Token session, Bundle extras) throws RemoteException {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putInt(MediaBrowserProtocol.EXTRA_SERVICE_VERSION, 2);
            Bundle data = new Bundle();
            data.putString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID, root);
            data.putParcelable(MediaBrowserProtocol.DATA_MEDIA_SESSION_TOKEN, session);
            data.putBundle(MediaBrowserProtocol.DATA_ROOT_HINTS, extras);
            sendRequest(1, data);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.ServiceCallbacks
        public void onConnectFailed() throws RemoteException {
            sendRequest(2, null);
        }

        @Override // android.support.v4.media.MediaBrowserServiceCompat.ServiceCallbacks
        public void onLoadChildren(String mediaId, List<MediaBrowserCompat.MediaItem> list, Bundle options, Bundle notifyChildrenChangedOptions) throws RemoteException {
            Bundle data = new Bundle();
            data.putString(MediaBrowserProtocol.DATA_MEDIA_ITEM_ID, mediaId);
            data.putBundle(MediaBrowserProtocol.DATA_OPTIONS, options);
            data.putBundle(MediaBrowserProtocol.DATA_NOTIFY_CHILDREN_CHANGED_OPTIONS, notifyChildrenChangedOptions);
            if (list != null) {
                data.putParcelableArrayList(MediaBrowserProtocol.DATA_MEDIA_ITEM_LIST, list instanceof ArrayList ? (ArrayList) list : new ArrayList<>(list));
            }
            sendRequest(3, data);
        }

        private void sendRequest(int what, Bundle data) throws RemoteException {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = 2;
            msg.setData(data);
            this.mCallbacks.send(msg);
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public void attachToBaseContext(Context base) {
        attachBaseContext(base);
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 28) {
            this.mImpl = new MediaBrowserServiceImplApi28();
        } else if (Build.VERSION.SDK_INT >= 26) {
            this.mImpl = new MediaBrowserServiceImplApi26();
        } else if (Build.VERSION.SDK_INT >= 23) {
            this.mImpl = new MediaBrowserServiceImplApi23();
        } else if (Build.VERSION.SDK_INT >= 21) {
            this.mImpl = new MediaBrowserServiceImplApi21();
        } else {
            this.mImpl = new MediaBrowserServiceImplBase();
        }
        this.mImpl.onCreate();
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.mImpl.onBind(intent);
    }

    @Override // android.app.Service
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result, @NonNull Bundle options) {
        result.setFlags(1);
        onLoadChildren(parentId, result);
    }

    public void onLoadItem(String itemId, @NonNull Result<MediaBrowserCompat.MediaItem> result) {
        result.setFlags(2);
        result.sendResult(null);
    }

    public void onSearch(@NonNull String query, Bundle extras, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.setFlags(4);
        result.sendResult(null);
    }

    public void onCustomAction(@NonNull String action, Bundle extras, @NonNull Result<Bundle> result) {
        result.sendError(null);
    }

    public void setSessionToken(MediaSessionCompat.Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Session token may not be null.");
        } else if (this.mSession == null) {
            this.mSession = token;
            this.mImpl.setSessionToken(token);
        } else {
            throw new IllegalStateException("The session token has already been set.");
        }
    }

    @Nullable
    public MediaSessionCompat.Token getSessionToken() {
        return this.mSession;
    }

    public final Bundle getBrowserRootHints() {
        return this.mImpl.getBrowserRootHints();
    }

    @NonNull
    public final MediaSessionManager.RemoteUserInfo getCurrentBrowserInfo() {
        return this.mImpl.getCurrentBrowserInfo();
    }

    public void notifyChildrenChanged(@NonNull String parentId) {
        if (parentId != null) {
            this.mImpl.notifyChildrenChanged(parentId, null);
            return;
        }
        throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
    }

    public void notifyChildrenChanged(@NonNull String parentId, @NonNull Bundle options) {
        if (parentId == null) {
            throw new IllegalArgumentException("parentId cannot be null in notifyChildrenChanged");
        } else if (options != null) {
            this.mImpl.notifyChildrenChanged(parentId, options);
        } else {
            throw new IllegalArgumentException("options cannot be null in notifyChildrenChanged");
        }
    }

    @NonNull
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public List<MediaSessionManager.RemoteUserInfo> getSubscribingBrowsers(@NonNull String parentId) {
        if (parentId != null) {
            return this.mImpl.getSubscribingBrowsers(parentId);
        }
        throw new IllegalArgumentException("parentId cannot be null in getSubscribingBrowsers");
    }

    /* access modifiers changed from: package-private */
    public boolean isValidPackage(String pkg, int uid) {
        if (pkg == null) {
            return DEBUG;
        }
        for (String str : getPackageManager().getPackagesForUid(uid)) {
            if (str.equals(pkg)) {
                return true;
            }
        }
        return DEBUG;
    }

    /* access modifiers changed from: package-private */
    public void addSubscription(String id, ConnectionRecord connection, IBinder token, Bundle options) {
        List<Pair<IBinder, Bundle>> callbackList = connection.subscriptions.get(id);
        if (callbackList == null) {
            callbackList = new ArrayList();
        }
        for (Pair<IBinder, Bundle> callback : callbackList) {
            if (token == callback.first && MediaBrowserCompatUtils.areSameOptions(options, callback.second)) {
                return;
            }
        }
        callbackList.add(new Pair<>(token, options));
        connection.subscriptions.put(id, callbackList);
        performLoadChildren(id, connection, options, null);
    }

    /* access modifiers changed from: package-private */
    public boolean removeSubscription(String id, ConnectionRecord connection, IBinder token) {
        if (token != null) {
            boolean removed = DEBUG;
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
        } else if (connection.subscriptions.remove(id) != null) {
            return true;
        } else {
            return DEBUG;
        }
    }

    /* access modifiers changed from: package-private */
    public void performLoadChildren(final String parentId, final ConnectionRecord connection, final Bundle subscribeOptions, final Bundle notifyChildrenChangedOptions) {
        Result<List<MediaBrowserCompat.MediaItem>> result = new Result<List<MediaBrowserCompat.MediaItem>>(parentId) {
            /* class android.support.v4.media.MediaBrowserServiceCompat.AnonymousClass1 */

            /* access modifiers changed from: package-private */
            public void onResultSent(List<MediaBrowserCompat.MediaItem> list) {
                if (MediaBrowserServiceCompat.this.mConnections.get(connection.callbacks.asBinder()) == connection) {
                    try {
                        connection.callbacks.onLoadChildren(parentId, (getFlags() & 1) != 0 ? MediaBrowserServiceCompat.this.applyOptions(list, subscribeOptions) : list, subscribeOptions, notifyChildrenChangedOptions);
                    } catch (RemoteException e) {
                        Log.w(MediaBrowserServiceCompat.TAG, "Calling onLoadChildren() failed for id=" + parentId + " package=" + connection.pkg);
                    }
                } else if (MediaBrowserServiceCompat.DEBUG) {
                    Log.d(MediaBrowserServiceCompat.TAG, "Not sending onLoadChildren result for connection that has been disconnected. pkg=" + connection.pkg + " id=" + parentId);
                }
            }
        };
        this.mCurConnection = connection;
        if (subscribeOptions == null) {
            onLoadChildren(parentId, result);
        } else {
            onLoadChildren(parentId, result, subscribeOptions);
        }
        this.mCurConnection = null;
        if (!result.isDone()) {
            throw new IllegalStateException("onLoadChildren must call detach() or sendResult() before returning for package=" + connection.pkg + " id=" + parentId);
        }
    }

    /* access modifiers changed from: package-private */
    public List<MediaBrowserCompat.MediaItem> applyOptions(List<MediaBrowserCompat.MediaItem> list, Bundle options) {
        if (list == null) {
            return null;
        }
        int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE, -1);
        int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, -1);
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

    /* access modifiers changed from: package-private */
    public void performLoadItem(String itemId, ConnectionRecord connection, final ResultReceiver receiver) {
        Result<MediaBrowserCompat.MediaItem> result = new Result<MediaBrowserCompat.MediaItem>(itemId) {
            /* class android.support.v4.media.MediaBrowserServiceCompat.AnonymousClass2 */

            /* access modifiers changed from: package-private */
            public void onResultSent(MediaBrowserCompat.MediaItem item) {
                if ((getFlags() & 2) != 0) {
                    receiver.send(-1, null);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(MediaBrowserServiceCompat.KEY_MEDIA_ITEM, item);
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

    /* access modifiers changed from: package-private */
    public void performSearch(String query, Bundle extras, ConnectionRecord connection, final ResultReceiver receiver) {
        Result<List<MediaBrowserCompat.MediaItem>> result = new Result<List<MediaBrowserCompat.MediaItem>>(query) {
            /* class android.support.v4.media.MediaBrowserServiceCompat.AnonymousClass3 */

            /* access modifiers changed from: package-private */
            public void onResultSent(List<MediaBrowserCompat.MediaItem> items) {
                if ((getFlags() & 4) != 0 || items == null) {
                    receiver.send(-1, null);
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putParcelableArray(MediaBrowserServiceCompat.KEY_SEARCH_RESULTS, (Parcelable[]) items.toArray(new MediaBrowserCompat.MediaItem[0]));
                receiver.send(0, bundle);
            }
        };
        this.mCurConnection = connection;
        onSearch(query, extras, result);
        this.mCurConnection = null;
        if (!result.isDone()) {
            throw new IllegalStateException("onSearch must call detach() or sendResult() before returning for query=" + query);
        }
    }

    /* access modifiers changed from: package-private */
    public void performCustomAction(String action, Bundle extras, ConnectionRecord connection, final ResultReceiver receiver) {
        Result<Bundle> result = new Result<Bundle>(action) {
            /* class android.support.v4.media.MediaBrowserServiceCompat.AnonymousClass4 */

            /* access modifiers changed from: package-private */
            public void onResultSent(Bundle result) {
                receiver.send(0, result);
            }

            /* access modifiers changed from: package-private */
            @Override // android.support.v4.media.MediaBrowserServiceCompat.Result
            public void onProgressUpdateSent(Bundle data) {
                receiver.send(1, data);
            }

            /* access modifiers changed from: package-private */
            @Override // android.support.v4.media.MediaBrowserServiceCompat.Result
            public void onErrorSent(Bundle data) {
                receiver.send(-1, data);
            }
        };
        this.mCurConnection = connection;
        onCustomAction(action, extras, result);
        this.mCurConnection = null;
        if (!result.isDone()) {
            throw new IllegalStateException("onCustomAction must call detach() or sendResult() or sendError() before returning for action=" + action + " extras=" + extras);
        }
    }

    public static final class BrowserRoot {
        public static final String EXTRA_OFFLINE = "android.service.media.extra.OFFLINE";
        public static final String EXTRA_RECENT = "android.service.media.extra.RECENT";
        public static final String EXTRA_SUGGESTED = "android.service.media.extra.SUGGESTED";
        @Deprecated
        public static final String EXTRA_SUGGESTION_KEYWORDS = "android.service.media.extra.SUGGESTION_KEYWORDS";
        private final Bundle mExtras;
        private final String mRootId;

        public BrowserRoot(@NonNull String rootId, @Nullable Bundle extras) {
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
