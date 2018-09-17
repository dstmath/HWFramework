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
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public final class MediaBrowser {
    private static final int CONNECT_STATE_CONNECTED = 3;
    private static final int CONNECT_STATE_CONNECTING = 2;
    private static final int CONNECT_STATE_DISCONNECTED = 1;
    private static final int CONNECT_STATE_DISCONNECTING = 0;
    private static final int CONNECT_STATE_SUSPENDED = 4;
    private static final boolean DBG = false;
    public static final String EXTRA_PAGE = "android.media.browse.extra.PAGE";
    public static final String EXTRA_PAGE_SIZE = "android.media.browse.extra.PAGE_SIZE";
    private static final String TAG = "MediaBrowser";
    private final ConnectionCallback mCallback;
    private final Context mContext;
    private volatile Bundle mExtras;
    private final Handler mHandler = new Handler();
    private volatile Token mMediaSessionToken;
    private final Bundle mRootHints;
    private volatile String mRootId;
    private IMediaBrowserService mServiceBinder;
    private IMediaBrowserServiceCallbacks mServiceCallbacks;
    private final ComponentName mServiceComponent;
    private MediaServiceConnection mServiceConnection;
    private volatile int mState = 1;
    private final ArrayMap<String, Subscription> mSubscriptions = new ArrayMap();

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

        public void onError(String mediaId) {
        }
    }

    public static class MediaItem implements Parcelable {
        public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
            public MediaItem createFromParcel(Parcel in) {
                return new MediaItem(in, null);
            }

            public MediaItem[] newArray(int size) {
                return new MediaItem[size];
            }
        };
        public static final int FLAG_BROWSABLE = 1;
        public static final int FLAG_PLAYABLE = 2;
        private final MediaDescription mDescription;
        private final int mFlags;

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
            return 0;
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
            return (this.mFlags & 1) != 0;
        }

        public boolean isPlayable() {
            return (this.mFlags & 2) != 0;
        }

        public MediaDescription getDescription() {
            return this.mDescription;
        }

        public String getMediaId() {
            return this.mDescription.getMediaId();
        }
    }

    private class MediaServiceConnection implements ServiceConnection {
        /* synthetic */ MediaServiceConnection(MediaBrowser this$0, MediaServiceConnection -this1) {
            this();
        }

        private MediaServiceConnection() {
        }

        public void onServiceConnected(final ComponentName name, final IBinder binder) {
            postOrRun(new Runnable() {
                public void run() {
                    if (MediaServiceConnection.this.isCurrent("onServiceConnected")) {
                        MediaBrowser.this.mServiceBinder = Stub.asInterface(binder);
                        MediaBrowser.this.mServiceCallbacks = MediaBrowser.this.getNewServiceCallbacks();
                        MediaBrowser.this.mState = 2;
                        try {
                            MediaBrowser.this.mServiceBinder.connect(MediaBrowser.this.mContext.getPackageName(), MediaBrowser.this.mRootHints, MediaBrowser.this.mServiceCallbacks);
                        } catch (RemoteException e) {
                            Log.w(MediaBrowser.TAG, "RemoteException during connect for " + MediaBrowser.this.mServiceComponent);
                        }
                    }
                }
            });
        }

        public void onServiceDisconnected(final ComponentName name) {
            postOrRun(new Runnable() {
                public void run() {
                    if (MediaServiceConnection.this.isCurrent("onServiceDisconnected")) {
                        MediaBrowser.this.mServiceBinder = null;
                        MediaBrowser.this.mServiceCallbacks = null;
                        MediaBrowser.this.mState = 4;
                        MediaBrowser.this.mCallback.onConnectionSuspended();
                    }
                }
            });
        }

        private void postOrRun(Runnable r) {
            if (Thread.currentThread() == MediaBrowser.this.mHandler.getLooper().getThread()) {
                r.run();
            } else {
                MediaBrowser.this.mHandler.post(r);
            }
        }

        private boolean isCurrent(String funcName) {
            if (MediaBrowser.this.mServiceConnection == this && MediaBrowser.this.mState != 0 && MediaBrowser.this.mState != 1) {
                return true;
            }
            if (!(MediaBrowser.this.mState == 0 || MediaBrowser.this.mState == 1)) {
                Log.i(MediaBrowser.TAG, funcName + " for " + MediaBrowser.this.mServiceComponent + " with mServiceConnection=" + MediaBrowser.this.mServiceConnection + " this=" + this);
            }
            return false;
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
        private final List<SubscriptionCallback> mCallbacks = new ArrayList();
        private final List<Bundle> mOptionsList = new ArrayList();

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
            for (int i = 0; i < this.mOptionsList.size(); i++) {
                if (MediaBrowserUtils.areSameOptions((Bundle) this.mOptionsList.get(i), options)) {
                    return (SubscriptionCallback) this.mCallbacks.get(i);
                }
            }
            return null;
        }

        public void putCallback(Bundle options, SubscriptionCallback callback) {
            for (int i = 0; i < this.mOptionsList.size(); i++) {
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
        Binder mToken = new Binder();

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
        if (this.mState == 0 || this.mState == 1) {
            this.mState = 2;
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (MediaBrowser.this.mState != 0) {
                        MediaBrowser.this.mState = 2;
                        if (MediaBrowser.this.mServiceBinder != null) {
                            throw new RuntimeException("mServiceBinder should be null. Instead it is " + MediaBrowser.this.mServiceBinder);
                        } else if (MediaBrowser.this.mServiceCallbacks != null) {
                            throw new RuntimeException("mServiceCallbacks should be null. Instead it is " + MediaBrowser.this.mServiceCallbacks);
                        } else {
                            Intent intent = new Intent("android.media.browse.MediaBrowserService");
                            intent.setComponent(MediaBrowser.this.mServiceComponent);
                            MediaBrowser.this.mServiceConnection = new MediaServiceConnection(MediaBrowser.this, null);
                            boolean bound = false;
                            try {
                                bound = MediaBrowser.this.mContext.bindService(intent, MediaBrowser.this.mServiceConnection, 1);
                            } catch (Exception e) {
                                Log.e(MediaBrowser.TAG, "Failed binding to service " + MediaBrowser.this.mServiceComponent);
                            }
                            if (!bound) {
                                MediaBrowser.this.forceCloseConnection();
                                MediaBrowser.this.mCallback.onConnectionFailed();
                            }
                        }
                    }
                }
            });
            return;
        }
        throw new IllegalStateException("connect() called while neither disconnecting nor disconnected (state=" + getStateLabel(this.mState) + ")");
    }

    public void disconnect() {
        this.mState = 0;
        this.mHandler.post(new Runnable() {
            public void run() {
                if (MediaBrowser.this.mServiceCallbacks != null) {
                    try {
                        MediaBrowser.this.mServiceBinder.disconnect(MediaBrowser.this.mServiceCallbacks);
                    } catch (RemoteException e) {
                        Log.w(MediaBrowser.TAG, "RemoteException during connect for " + MediaBrowser.this.mServiceComponent);
                    }
                }
                int state = MediaBrowser.this.mState;
                MediaBrowser.this.forceCloseConnection();
                if (state != 0) {
                    MediaBrowser.this.mState = state;
                }
            }
        });
    }

    private void forceCloseConnection() {
        if (this.mServiceConnection != null) {
            this.mContext.unbindService(this.mServiceConnection);
        }
        this.mState = 1;
        this.mServiceConnection = null;
        this.mServiceBinder = null;
        this.mServiceCallbacks = null;
        this.mRootId = null;
        this.mMediaSessionToken = null;
    }

    public boolean isConnected() {
        return this.mState == 3;
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
            throw new IllegalArgumentException("options cannot be null");
        }
        subscribeInternal(parentId, new Bundle(options), callback);
    }

    public void unsubscribe(String parentId) {
        unsubscribeInternal(parentId, null);
    }

    public void unsubscribe(String parentId, SubscriptionCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        unsubscribeInternal(parentId, callback);
    }

    public void getItem(final String mediaId, final ItemCallback cb) {
        if (TextUtils.isEmpty(mediaId)) {
            throw new IllegalArgumentException("mediaId cannot be empty.");
        } else if (cb == null) {
            throw new IllegalArgumentException("cb cannot be null.");
        } else if (this.mState != 3) {
            Log.i(TAG, "Not connected, unable to retrieve the MediaItem.");
            this.mHandler.post(new Runnable() {
                public void run() {
                    cb.onError(mediaId);
                }
            });
        } else {
            try {
                this.mServiceBinder.getMediaItem(mediaId, new ResultReceiver(this.mHandler) {
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == 0 && resultData != null && (resultData.containsKey("media_item") ^ 1) == 0) {
                            Parcelable item = resultData.getParcelable("media_item");
                            if (item == null || ((item instanceof MediaItem) ^ 1) == 0) {
                                cb.onItemLoaded((MediaItem) item);
                                return;
                            } else {
                                cb.onError(mediaId);
                                return;
                            }
                        }
                        cb.onError(mediaId);
                    }
                }, this.mServiceCallbacks);
            } catch (RemoteException e) {
                Log.i(TAG, "Remote error getting media item.");
                this.mHandler.post(new Runnable() {
                    public void run() {
                        cb.onError(mediaId);
                    }
                });
            }
        }
    }

    private void subscribeInternal(String parentId, Bundle options, SubscriptionCallback callback) {
        if (TextUtils.isEmpty(parentId)) {
            throw new IllegalArgumentException("parentId cannot be empty.");
        } else if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        } else {
            Subscription sub = (Subscription) this.mSubscriptions.get(parentId);
            if (sub == null) {
                sub = new Subscription();
                this.mSubscriptions.put(parentId, sub);
            }
            sub.putCallback(options, callback);
            if (isConnected()) {
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
            throw new IllegalArgumentException("parentId cannot be empty.");
        }
        Subscription sub = (Subscription) this.mSubscriptions.get(parentId);
        if (sub != null) {
            if (callback == null) {
                try {
                    if (isConnected()) {
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
                        if (isConnected()) {
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
            case 0:
                return "CONNECT_STATE_DISCONNECTING";
            case 1:
                return "CONNECT_STATE_DISCONNECTED";
            case 2:
                return "CONNECT_STATE_CONNECTING";
            case 3:
                return "CONNECT_STATE_CONNECTED";
            case 4:
                return "CONNECT_STATE_SUSPENDED";
            default:
                return "UNKNOWN/" + state;
        }
    }

    private final void onServiceConnected(IMediaBrowserServiceCallbacks callback, String root, Token session, Bundle extra) {
        final IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks = callback;
        final String str = root;
        final Token token = session;
        final Bundle bundle = extra;
        this.mHandler.post(new Runnable() {
            public void run() {
                if (!MediaBrowser.this.isCurrent(iMediaBrowserServiceCallbacks, "onConnect")) {
                    return;
                }
                if (MediaBrowser.this.mState != 2) {
                    Log.w(MediaBrowser.TAG, "onConnect from service while mState=" + MediaBrowser.getStateLabel(MediaBrowser.this.mState) + "... ignoring");
                    return;
                }
                MediaBrowser.this.mRootId = str;
                MediaBrowser.this.mMediaSessionToken = token;
                MediaBrowser.this.mExtras = bundle;
                MediaBrowser.this.mState = 3;
                MediaBrowser.this.mCallback.onConnected();
                for (Entry<String, Subscription> subscriptionEntry : MediaBrowser.this.mSubscriptions.entrySet()) {
                    String id = (String) subscriptionEntry.getKey();
                    Subscription sub = (Subscription) subscriptionEntry.getValue();
                    List<SubscriptionCallback> callbackList = sub.getCallbacks();
                    List<Bundle> optionsList = sub.getOptionsList();
                    for (int i = 0; i < callbackList.size(); i++) {
                        try {
                            MediaBrowser.this.mServiceBinder.addSubscription(id, ((SubscriptionCallback) callbackList.get(i)).mToken, (Bundle) optionsList.get(i), MediaBrowser.this.mServiceCallbacks);
                        } catch (RemoteException e) {
                            Log.d(MediaBrowser.TAG, "addSubscription failed with RemoteException parentId=" + id);
                        }
                    }
                }
            }
        });
    }

    private final void onConnectionFailed(final IMediaBrowserServiceCallbacks callback) {
        this.mHandler.post(new Runnable() {
            public void run() {
                Log.e(MediaBrowser.TAG, "onConnectFailed for " + MediaBrowser.this.mServiceComponent);
                if (!MediaBrowser.this.isCurrent(callback, "onConnectFailed")) {
                    return;
                }
                if (MediaBrowser.this.mState != 2) {
                    Log.w(MediaBrowser.TAG, "onConnect from service while mState=" + MediaBrowser.getStateLabel(MediaBrowser.this.mState) + "... ignoring");
                    return;
                }
                MediaBrowser.this.forceCloseConnection();
                MediaBrowser.this.mCallback.onConnectionFailed();
            }
        });
    }

    private final void onLoadChildren(IMediaBrowserServiceCallbacks callback, String parentId, ParceledListSlice list, Bundle options) {
        final IMediaBrowserServiceCallbacks iMediaBrowserServiceCallbacks = callback;
        final String str = parentId;
        final Bundle bundle = options;
        final ParceledListSlice parceledListSlice = list;
        this.mHandler.post(new Runnable() {
            public void run() {
                if (MediaBrowser.this.isCurrent(iMediaBrowserServiceCallbacks, "onLoadChildren")) {
                    Subscription subscription = (Subscription) MediaBrowser.this.mSubscriptions.get(str);
                    if (subscription != null) {
                        SubscriptionCallback subscriptionCallback = subscription.getCallback(bundle);
                        if (subscriptionCallback != null) {
                            List data = parceledListSlice == null ? null : parceledListSlice.getList();
                            if (bundle == null) {
                                if (data == null) {
                                    subscriptionCallback.onError(str);
                                } else {
                                    subscriptionCallback.onChildrenLoaded(str, data);
                                }
                            } else if (data == null) {
                                subscriptionCallback.onError(str, bundle);
                            } else {
                                subscriptionCallback.onChildrenLoaded(str, data, bundle);
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean isCurrent(IMediaBrowserServiceCallbacks callback, String funcName) {
        if (this.mServiceCallbacks == callback && this.mState != 0 && this.mState != 1) {
            return true;
        }
        if (!(this.mState == 0 || this.mState == 1)) {
            Log.i(TAG, funcName + " for " + this.mServiceComponent + " with mServiceConnection=" + this.mServiceCallbacks + " this=" + this);
        }
        return false;
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
