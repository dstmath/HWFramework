package android.media.browse;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ParceledListSlice;
import android.media.MediaDescription;
import android.media.session.MediaSession.Token;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.service.media.IMediaBrowserService;
import android.service.media.IMediaBrowserService.Stub;
import android.service.media.IMediaBrowserServiceCallbacks;
import android.service.media.MediaBrowserService;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public final class MediaBrowser {
    private static final int CONNECT_STATE_CONNECTED = 2;
    private static final int CONNECT_STATE_CONNECTING = 1;
    private static final int CONNECT_STATE_DISCONNECTED = 0;
    private static final int CONNECT_STATE_SUSPENDED = 3;
    private static final boolean DBG = false;
    public static final String EXTRA_PAGE = "android.media.browse.extra.PAGE";
    public static final String EXTRA_PAGE_SIZE = "android.media.browse.extra.PAGE_SIZE";
    private static final String TAG = "MediaBrowser";
    private final ConnectionCallback mCallback;
    private final Context mContext;
    private Bundle mExtras;
    private final Handler mHandler;
    private Token mMediaSessionToken;
    private final Bundle mRootHints;
    private String mRootId;
    private IMediaBrowserService mServiceBinder;
    private IMediaBrowserServiceCallbacks mServiceCallbacks;
    private final ComponentName mServiceComponent;
    private MediaServiceConnection mServiceConnection;
    private int mState;
    private final ArrayMap<String, Subscription> mSubscriptions;

    /* renamed from: android.media.browse.MediaBrowser.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ ServiceConnection val$thisConnection;

        AnonymousClass1(ServiceConnection val$thisConnection) {
            this.val$thisConnection = val$thisConnection;
        }

        public void run() {
            if (this.val$thisConnection == MediaBrowser.this.mServiceConnection) {
                MediaBrowser.this.forceCloseConnection();
                MediaBrowser.this.mCallback.onConnectionFailed();
            }
        }
    }

    /* renamed from: android.media.browse.MediaBrowser.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ ItemCallback val$cb;
        final /* synthetic */ String val$mediaId;

        AnonymousClass2(ItemCallback val$cb, String val$mediaId) {
            this.val$cb = val$cb;
            this.val$mediaId = val$mediaId;
        }

        public void run() {
            this.val$cb.onError(this.val$mediaId);
        }
    }

    /* renamed from: android.media.browse.MediaBrowser.3 */
    class AnonymousClass3 extends ResultReceiver {
        final /* synthetic */ ItemCallback val$cb;
        final /* synthetic */ String val$mediaId;

        AnonymousClass3(Handler $anonymous0, ItemCallback val$cb, String val$mediaId) {
            this.val$cb = val$cb;
            this.val$mediaId = val$mediaId;
            super($anonymous0);
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 0 && resultData != null && resultData.containsKey(MediaBrowserService.KEY_MEDIA_ITEM)) {
                Parcelable item = resultData.getParcelable(MediaBrowserService.KEY_MEDIA_ITEM);
                if (item instanceof MediaItem) {
                    this.val$cb.onItemLoaded((MediaItem) item);
                    return;
                } else {
                    this.val$cb.onError(this.val$mediaId);
                    return;
                }
            }
            this.val$cb.onError(this.val$mediaId);
        }
    }

    /* renamed from: android.media.browse.MediaBrowser.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ ItemCallback val$cb;
        final /* synthetic */ String val$mediaId;

        AnonymousClass4(ItemCallback val$cb, String val$mediaId) {
            this.val$cb = val$cb;
            this.val$mediaId = val$mediaId;
        }

        public void run() {
            this.val$cb.onError(this.val$mediaId);
        }
    }

    /* renamed from: android.media.browse.MediaBrowser.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ IMediaBrowserServiceCallbacks val$callback;
        final /* synthetic */ Bundle val$extra;
        final /* synthetic */ String val$root;
        final /* synthetic */ Token val$session;

        AnonymousClass5(IMediaBrowserServiceCallbacks val$callback, String val$root, Token val$session, Bundle val$extra) {
            this.val$callback = val$callback;
            this.val$root = val$root;
            this.val$session = val$session;
            this.val$extra = val$extra;
        }

        public void run() {
            if (!MediaBrowser.this.isCurrent(this.val$callback, "onConnect")) {
                return;
            }
            if (MediaBrowser.this.mState != MediaBrowser.CONNECT_STATE_CONNECTING) {
                Log.w(MediaBrowser.TAG, "onConnect from service while mState=" + MediaBrowser.getStateLabel(MediaBrowser.this.mState) + "... ignoring");
                return;
            }
            MediaBrowser.this.mRootId = this.val$root;
            MediaBrowser.this.mMediaSessionToken = this.val$session;
            MediaBrowser.this.mExtras = this.val$extra;
            MediaBrowser.this.mState = MediaBrowser.CONNECT_STATE_CONNECTED;
            MediaBrowser.this.mCallback.onConnected();
            for (Entry<String, Subscription> subscriptionEntry : MediaBrowser.this.mSubscriptions.entrySet()) {
                String id = (String) subscriptionEntry.getKey();
                Subscription sub = (Subscription) subscriptionEntry.getValue();
                List<SubscriptionCallback> callbackList = sub.getCallbacks();
                List<Bundle> optionsList = sub.getOptionsList();
                for (int i = MediaBrowser.CONNECT_STATE_DISCONNECTED; i < callbackList.size(); i += MediaBrowser.CONNECT_STATE_CONNECTING) {
                    try {
                        MediaBrowser.this.mServiceBinder.addSubscription(id, ((SubscriptionCallback) callbackList.get(i)).mToken, (Bundle) optionsList.get(i), MediaBrowser.this.mServiceCallbacks);
                    } catch (RemoteException e) {
                        Log.d(MediaBrowser.TAG, "addSubscription failed with RemoteException parentId=" + id);
                    }
                }
            }
        }
    }

    /* renamed from: android.media.browse.MediaBrowser.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ IMediaBrowserServiceCallbacks val$callback;

        AnonymousClass6(IMediaBrowserServiceCallbacks val$callback) {
            this.val$callback = val$callback;
        }

        public void run() {
            Log.e(MediaBrowser.TAG, "onConnectFailed for " + MediaBrowser.this.mServiceComponent);
            if (!MediaBrowser.this.isCurrent(this.val$callback, "onConnectFailed")) {
                return;
            }
            if (MediaBrowser.this.mState != MediaBrowser.CONNECT_STATE_CONNECTING) {
                Log.w(MediaBrowser.TAG, "onConnect from service while mState=" + MediaBrowser.getStateLabel(MediaBrowser.this.mState) + "... ignoring");
                return;
            }
            MediaBrowser.this.forceCloseConnection();
            MediaBrowser.this.mCallback.onConnectionFailed();
        }
    }

    /* renamed from: android.media.browse.MediaBrowser.7 */
    class AnonymousClass7 implements Runnable {
        final /* synthetic */ IMediaBrowserServiceCallbacks val$callback;
        final /* synthetic */ ParceledListSlice val$list;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String val$parentId;

        AnonymousClass7(IMediaBrowserServiceCallbacks val$callback, String val$parentId, Bundle val$options, ParceledListSlice val$list) {
            this.val$callback = val$callback;
            this.val$parentId = val$parentId;
            this.val$options = val$options;
            this.val$list = val$list;
        }

        public void run() {
            if (MediaBrowser.this.isCurrent(this.val$callback, "onLoadChildren")) {
                Subscription subscription = (Subscription) MediaBrowser.this.mSubscriptions.get(this.val$parentId);
                if (subscription != null) {
                    SubscriptionCallback subscriptionCallback = subscription.getCallback(this.val$options);
                    if (subscriptionCallback != null) {
                        List list = this.val$list == null ? null : this.val$list.getList();
                        if (this.val$options == null) {
                            if (list == null) {
                                subscriptionCallback.onError(this.val$parentId);
                            } else {
                                subscriptionCallback.onChildrenLoaded(this.val$parentId, list);
                            }
                        } else if (list == null) {
                            subscriptionCallback.onError(this.val$parentId, this.val$options);
                        } else {
                            subscriptionCallback.onChildrenLoaded(this.val$parentId, list, this.val$options);
                        }
                    }
                }
            }
        }
    }

    public static class ConnectionCallback {
        public void onConnected() {
        }

        public void onConnectionSuspended() {
        }

        public void onConnectionFailed() {
        }
    }

    public static abstract class ItemCallback {
        public void onItemLoaded(MediaItem item) {
        }

        public void onError(String itemId) {
        }
    }

    public static class MediaItem implements Parcelable {
        public static final Creator<MediaItem> CREATOR = null;
        public static final int FLAG_BROWSABLE = 1;
        public static final int FLAG_PLAYABLE = 2;
        private final MediaDescription mDescription;
        private final int mFlags;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.browse.MediaBrowser.MediaItem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.browse.MediaBrowser.MediaItem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.browse.MediaBrowser.MediaItem.<clinit>():void");
        }

        public MediaItem(MediaDescription description, int flags) {
            if (description == null) {
                throw new IllegalArgumentException("description cannot be null");
            } else if (TextUtils.isEmpty(description.getMediaId())) {
                throw new IllegalArgumentException("description must have a non-empty media id");
            } else {
                this.mFlags = flags;
                this.mDescription = description;
            }
        }

        private MediaItem(Parcel in) {
            this.mFlags = in.readInt();
            this.mDescription = (MediaDescription) MediaDescription.CREATOR.createFromParcel(in);
        }

        public int describeContents() {
            return MediaBrowser.CONNECT_STATE_DISCONNECTED;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mFlags);
            this.mDescription.writeToParcel(out, flags);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("MediaItem{");
            sb.append("mFlags=").append(this.mFlags);
            sb.append(", mDescription=").append(this.mDescription);
            sb.append('}');
            return sb.toString();
        }

        public int getFlags() {
            return this.mFlags;
        }

        public boolean isBrowsable() {
            return (this.mFlags & FLAG_BROWSABLE) != 0 ? true : MediaBrowser.DBG;
        }

        public boolean isPlayable() {
            return (this.mFlags & FLAG_PLAYABLE) != 0 ? true : MediaBrowser.DBG;
        }

        public MediaDescription getDescription() {
            return this.mDescription;
        }

        public String getMediaId() {
            return this.mDescription.getMediaId();
        }
    }

    private class MediaServiceConnection implements ServiceConnection {
        final /* synthetic */ MediaBrowser this$0;

        /* renamed from: android.media.browse.MediaBrowser.MediaServiceConnection.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ MediaServiceConnection this$1;
            final /* synthetic */ IBinder val$binder;
            final /* synthetic */ ComponentName val$name;

            AnonymousClass1(MediaServiceConnection this$1, ComponentName val$name, IBinder val$binder) {
                this.this$1 = this$1;
                this.val$name = val$name;
                this.val$binder = val$binder;
            }

            public void run() {
                if (this.this$1.isCurrent("onServiceConnected")) {
                    this.this$1.this$0.mServiceBinder = Stub.asInterface(this.val$binder);
                    this.this$1.this$0.mServiceCallbacks = this.this$1.this$0.getNewServiceCallbacks();
                    this.this$1.this$0.mState = MediaBrowser.CONNECT_STATE_CONNECTING;
                    try {
                        this.this$1.this$0.mServiceBinder.connect(this.this$1.this$0.mContext.getPackageName(), this.this$1.this$0.mRootHints, this.this$1.this$0.mServiceCallbacks);
                    } catch (RemoteException e) {
                        Log.w(MediaBrowser.TAG, "RemoteException during connect for " + this.this$1.this$0.mServiceComponent);
                    }
                }
            }
        }

        /* renamed from: android.media.browse.MediaBrowser.MediaServiceConnection.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ MediaServiceConnection this$1;
            final /* synthetic */ ComponentName val$name;

            AnonymousClass2(MediaServiceConnection this$1, ComponentName val$name) {
                this.this$1 = this$1;
                this.val$name = val$name;
            }

            public void run() {
                if (this.this$1.isCurrent("onServiceDisconnected")) {
                    this.this$1.this$0.mServiceBinder = null;
                    this.this$1.this$0.mServiceCallbacks = null;
                    this.this$1.this$0.mState = MediaBrowser.CONNECT_STATE_SUSPENDED;
                    this.this$1.this$0.mCallback.onConnectionSuspended();
                }
            }
        }

        /* synthetic */ MediaServiceConnection(MediaBrowser this$0, MediaServiceConnection mediaServiceConnection) {
            this(this$0);
        }

        private MediaServiceConnection(MediaBrowser this$0) {
            this.this$0 = this$0;
        }

        public void onServiceConnected(ComponentName name, IBinder binder) {
            postOrRun(new AnonymousClass1(this, name, binder));
        }

        public void onServiceDisconnected(ComponentName name) {
            postOrRun(new AnonymousClass2(this, name));
        }

        private void postOrRun(Runnable r) {
            if (Thread.currentThread() == this.this$0.mHandler.getLooper().getThread()) {
                r.run();
            } else {
                this.this$0.mHandler.post(r);
            }
        }

        private boolean isCurrent(String funcName) {
            if (this.this$0.mServiceConnection == this) {
                return true;
            }
            if (this.this$0.mState != 0) {
                Log.i(MediaBrowser.TAG, funcName + " for " + this.this$0.mServiceComponent + " with mServiceConnection=" + this.this$0.mServiceConnection + " this=" + this);
            }
            return MediaBrowser.DBG;
        }
    }

    private static class ServiceCallbacks extends IMediaBrowserServiceCallbacks.Stub {
        private WeakReference<MediaBrowser> mMediaBrowser;

        public ServiceCallbacks(MediaBrowser mediaBrowser) {
            this.mMediaBrowser = new WeakReference(mediaBrowser);
        }

        public void onConnect(String root, Token session, Bundle extras) {
            MediaBrowser mediaBrowser = (MediaBrowser) this.mMediaBrowser.get();
            if (mediaBrowser != null) {
                mediaBrowser.onServiceConnected(this, root, session, extras);
            }
        }

        public void onConnectFailed() {
            MediaBrowser mediaBrowser = (MediaBrowser) this.mMediaBrowser.get();
            if (mediaBrowser != null) {
                mediaBrowser.onConnectionFailed(this);
            }
        }

        public void onLoadChildren(String parentId, ParceledListSlice list) {
            onLoadChildrenWithOptions(parentId, list, null);
        }

        public void onLoadChildrenWithOptions(String parentId, ParceledListSlice list, Bundle options) {
            MediaBrowser mediaBrowser = (MediaBrowser) this.mMediaBrowser.get();
            if (mediaBrowser != null) {
                mediaBrowser.onLoadChildren(this, parentId, list, options);
            }
        }
    }

    private static class Subscription {
        private final List<SubscriptionCallback> mCallbacks;
        private final List<Bundle> mOptionsList;

        public Subscription() {
            this.mCallbacks = new ArrayList();
            this.mOptionsList = new ArrayList();
        }

        public boolean isEmpty() {
            return this.mCallbacks.isEmpty();
        }

        public List<Bundle> getOptionsList() {
            return this.mOptionsList;
        }

        public List<SubscriptionCallback> getCallbacks() {
            return this.mCallbacks;
        }

        public SubscriptionCallback getCallback(Bundle options) {
            for (int i = MediaBrowser.CONNECT_STATE_DISCONNECTED; i < this.mOptionsList.size(); i += MediaBrowser.CONNECT_STATE_CONNECTING) {
                if (MediaBrowserUtils.areSameOptions((Bundle) this.mOptionsList.get(i), options)) {
                    return (SubscriptionCallback) this.mCallbacks.get(i);
                }
            }
            return null;
        }

        public void putCallback(Bundle options, SubscriptionCallback callback) {
            for (int i = MediaBrowser.CONNECT_STATE_DISCONNECTED; i < this.mOptionsList.size(); i += MediaBrowser.CONNECT_STATE_CONNECTING) {
                if (MediaBrowserUtils.areSameOptions((Bundle) this.mOptionsList.get(i), options)) {
                    this.mCallbacks.set(i, callback);
                    return;
                }
            }
            this.mCallbacks.add(callback);
            this.mOptionsList.add(options);
        }
    }

    public static abstract class SubscriptionCallback {
        Binder mToken;

        public SubscriptionCallback() {
            this.mToken = new Binder();
        }

        public void onChildrenLoaded(String parentId, List<MediaItem> list) {
        }

        public void onChildrenLoaded(String parentId, List<MediaItem> list, Bundle options) {
        }

        public void onError(String parentId) {
        }

        public void onError(String parentId, Bundle options) {
        }
    }

    public MediaBrowser(Context context, ComponentName serviceComponent, ConnectionCallback callback, Bundle rootHints) {
        Bundle bundle = null;
        this.mHandler = new Handler();
        this.mSubscriptions = new ArrayMap();
        this.mState = CONNECT_STATE_DISCONNECTED;
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        } else if (serviceComponent == null) {
            throw new IllegalArgumentException("service component must not be null");
        } else if (callback == null) {
            throw new IllegalArgumentException("connection callback must not be null");
        } else {
            this.mContext = context;
            this.mServiceComponent = serviceComponent;
            this.mCallback = callback;
            if (rootHints != null) {
                bundle = new Bundle(rootHints);
            }
            this.mRootHints = bundle;
        }
    }

    public void connect() {
        if (this.mState != 0) {
            throw new IllegalStateException("connect() called while not disconnected (state=" + getStateLabel(this.mState) + ")");
        } else if (this.mServiceBinder != null) {
            throw new RuntimeException("mServiceBinder should be null. Instead it is " + this.mServiceBinder);
        } else if (this.mServiceCallbacks != null) {
            throw new RuntimeException("mServiceCallbacks should be null. Instead it is " + this.mServiceCallbacks);
        } else {
            this.mState = CONNECT_STATE_CONNECTING;
            Intent intent = new Intent(MediaBrowserService.SERVICE_INTERFACE);
            intent.setComponent(this.mServiceComponent);
            ServiceConnection thisConnection = new MediaServiceConnection();
            this.mServiceConnection = thisConnection;
            boolean bound = DBG;
            try {
                bound = this.mContext.bindService(intent, this.mServiceConnection, CONNECT_STATE_CONNECTING);
            } catch (Exception e) {
                Log.e(TAG, "Failed binding to service " + this.mServiceComponent);
            }
            if (!bound) {
                this.mHandler.post(new AnonymousClass1(thisConnection));
            }
        }
    }

    public void disconnect() {
        if (this.mServiceCallbacks != null) {
            try {
                this.mServiceBinder.disconnect(this.mServiceCallbacks);
            } catch (RemoteException e) {
                Log.w(TAG, "RemoteException during connect for " + this.mServiceComponent);
            }
        }
        forceCloseConnection();
    }

    private void forceCloseConnection() {
        if (this.mServiceConnection != null) {
            this.mContext.unbindService(this.mServiceConnection);
        }
        this.mState = CONNECT_STATE_DISCONNECTED;
        this.mServiceConnection = null;
        this.mServiceBinder = null;
        this.mServiceCallbacks = null;
        this.mRootId = null;
        this.mMediaSessionToken = null;
    }

    public boolean isConnected() {
        return this.mState == CONNECT_STATE_CONNECTED ? true : DBG;
    }

    public ComponentName getServiceComponent() {
        if (isConnected()) {
            return this.mServiceComponent;
        }
        throw new IllegalStateException("getServiceComponent() called while not connected (state=" + this.mState + ")");
    }

    public String getRoot() {
        if (isConnected()) {
            return this.mRootId;
        }
        throw new IllegalStateException("getRoot() called while not connected (state=" + getStateLabel(this.mState) + ")");
    }

    public Bundle getExtras() {
        if (isConnected()) {
            return this.mExtras;
        }
        throw new IllegalStateException("getExtras() called while not connected (state=" + getStateLabel(this.mState) + ")");
    }

    public Token getSessionToken() {
        if (isConnected()) {
            return this.mMediaSessionToken;
        }
        throw new IllegalStateException("getSessionToken() called while not connected (state=" + this.mState + ")");
    }

    public void subscribe(String parentId, SubscriptionCallback callback) {
        subscribeInternal(parentId, null, callback);
    }

    public void subscribe(String parentId, Bundle options, SubscriptionCallback callback) {
        if (options == null) {
            throw new IllegalArgumentException("options are null");
        }
        subscribeInternal(parentId, new Bundle(options), callback);
    }

    public void unsubscribe(String parentId) {
        unsubscribeInternal(parentId, null);
    }

    public void unsubscribe(String parentId, SubscriptionCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }
        unsubscribeInternal(parentId, callback);
    }

    public void getItem(String mediaId, ItemCallback cb) {
        if (TextUtils.isEmpty(mediaId)) {
            throw new IllegalArgumentException("mediaId is empty.");
        } else if (cb == null) {
            throw new IllegalArgumentException("cb is null.");
        } else if (this.mState != CONNECT_STATE_CONNECTED) {
            Log.i(TAG, "Not connected, unable to retrieve the MediaItem.");
            this.mHandler.post(new AnonymousClass2(cb, mediaId));
        } else {
            try {
                this.mServiceBinder.getMediaItem(mediaId, new AnonymousClass3(this.mHandler, cb, mediaId), this.mServiceCallbacks);
            } catch (RemoteException e) {
                Log.i(TAG, "Remote error getting media item.");
                this.mHandler.post(new AnonymousClass4(cb, mediaId));
            }
        }
    }

    private void subscribeInternal(String parentId, Bundle options, SubscriptionCallback callback) {
        if (TextUtils.isEmpty(parentId)) {
            throw new IllegalArgumentException("parentId is empty.");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        } else {
            Subscription sub = (Subscription) this.mSubscriptions.get(parentId);
            if (sub == null) {
                sub = new Subscription();
                this.mSubscriptions.put(parentId, sub);
            }
            sub.putCallback(options, callback);
            if (this.mState == CONNECT_STATE_CONNECTED) {
                if (options == null) {
                    try {
                        this.mServiceBinder.addSubscriptionDeprecated(parentId, this.mServiceCallbacks);
                    } catch (RemoteException e) {
                        Log.d(TAG, "addSubscription failed with RemoteException parentId=" + parentId);
                        return;
                    }
                }
                this.mServiceBinder.addSubscription(parentId, callback.mToken, options, this.mServiceCallbacks);
            }
        }
    }

    private void unsubscribeInternal(String parentId, SubscriptionCallback callback) {
        if (TextUtils.isEmpty(parentId)) {
            throw new IllegalArgumentException("parentId is empty.");
        }
        Subscription sub = (Subscription) this.mSubscriptions.get(parentId);
        if (sub != null) {
            if (callback == null) {
                try {
                    if (this.mState == CONNECT_STATE_CONNECTED) {
                        this.mServiceBinder.removeSubscriptionDeprecated(parentId, this.mServiceCallbacks);
                        this.mServiceBinder.removeSubscription(parentId, null, this.mServiceCallbacks);
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "removeSubscription failed with RemoteException parentId=" + parentId);
                }
            } else {
                List<SubscriptionCallback> callbacks = sub.getCallbacks();
                List<Bundle> optionsList = sub.getOptionsList();
                for (int i = callbacks.size() - 1; i >= 0; i--) {
                    if (callbacks.get(i) == callback) {
                        if (this.mState == CONNECT_STATE_CONNECTED) {
                            this.mServiceBinder.removeSubscription(parentId, callback.mToken, this.mServiceCallbacks);
                        }
                        callbacks.remove(i);
                        optionsList.remove(i);
                    }
                }
            }
            if (sub.isEmpty() || callback == null) {
                this.mSubscriptions.remove(parentId);
            }
        }
    }

    private static String getStateLabel(int state) {
        switch (state) {
            case CONNECT_STATE_DISCONNECTED /*0*/:
                return "CONNECT_STATE_DISCONNECTED";
            case CONNECT_STATE_CONNECTING /*1*/:
                return "CONNECT_STATE_CONNECTING";
            case CONNECT_STATE_CONNECTED /*2*/:
                return "CONNECT_STATE_CONNECTED";
            case CONNECT_STATE_SUSPENDED /*3*/:
                return "CONNECT_STATE_SUSPENDED";
            default:
                return "UNKNOWN/" + state;
        }
    }

    private final void onServiceConnected(IMediaBrowserServiceCallbacks callback, String root, Token session, Bundle extra) {
        this.mHandler.post(new AnonymousClass5(callback, root, session, extra));
    }

    private final void onConnectionFailed(IMediaBrowserServiceCallbacks callback) {
        this.mHandler.post(new AnonymousClass6(callback));
    }

    private final void onLoadChildren(IMediaBrowserServiceCallbacks callback, String parentId, ParceledListSlice list, Bundle options) {
        this.mHandler.post(new AnonymousClass7(callback, parentId, options, list));
    }

    private boolean isCurrent(IMediaBrowserServiceCallbacks callback, String funcName) {
        if (this.mServiceCallbacks == callback) {
            return true;
        }
        if (this.mState != 0) {
            Log.i(TAG, funcName + " for " + this.mServiceComponent + " with mServiceConnection=" + this.mServiceCallbacks + " this=" + this);
        }
        return DBG;
    }

    private ServiceCallbacks getNewServiceCallbacks() {
        return new ServiceCallbacks(this);
    }

    void dump() {
        Log.d(TAG, "MediaBrowser...");
        Log.d(TAG, "  mServiceComponent=" + this.mServiceComponent);
        Log.d(TAG, "  mCallback=" + this.mCallback);
        Log.d(TAG, "  mRootHints=" + this.mRootHints);
        Log.d(TAG, "  mState=" + getStateLabel(this.mState));
        Log.d(TAG, "  mServiceConnection=" + this.mServiceConnection);
        Log.d(TAG, "  mServiceBinder=" + this.mServiceBinder);
        Log.d(TAG, "  mServiceCallbacks=" + this.mServiceCallbacks);
        Log.d(TAG, "  mRootId=" + this.mRootId);
        Log.d(TAG, "  mMediaSessionToken=" + this.mMediaSessionToken);
    }
}
