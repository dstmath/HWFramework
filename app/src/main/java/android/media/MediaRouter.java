package android.media;

import android.Manifest.permission;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.WifiDisplay;
import android.hardware.display.WifiDisplayStatus;
import android.media.AudioAttributes.Builder;
import android.media.IRemoteVolumeObserver.Stub;
import android.media.session.MediaSession;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class MediaRouter {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int AVAILABILITY_FLAG_IGNORE_DEFAULT_ROUTE = 1;
    public static final int CALLBACK_FLAG_PASSIVE_DISCOVERY = 8;
    public static final int CALLBACK_FLAG_PERFORM_ACTIVE_SCAN = 1;
    public static final int CALLBACK_FLAG_REQUEST_DISCOVERY = 4;
    public static final int CALLBACK_FLAG_UNFILTERED_EVENTS = 2;
    private static final boolean DEBUG = false;
    static final int ROUTE_TYPE_ANY = 8388615;
    public static final int ROUTE_TYPE_LIVE_AUDIO = 1;
    public static final int ROUTE_TYPE_LIVE_VIDEO = 2;
    public static final int ROUTE_TYPE_REMOTE_DISPLAY = 4;
    public static final int ROUTE_TYPE_USER = 8388608;
    private static final String TAG = "MediaRouter";
    static final HashMap<Context, MediaRouter> sRouters = null;
    static Static sStatic;

    public static abstract class Callback {
        public abstract void onRouteAdded(MediaRouter mediaRouter, RouteInfo routeInfo);

        public abstract void onRouteChanged(MediaRouter mediaRouter, RouteInfo routeInfo);

        public abstract void onRouteGrouped(MediaRouter mediaRouter, RouteInfo routeInfo, RouteGroup routeGroup, int i);

        public abstract void onRouteRemoved(MediaRouter mediaRouter, RouteInfo routeInfo);

        public abstract void onRouteSelected(MediaRouter mediaRouter, int i, RouteInfo routeInfo);

        public abstract void onRouteUngrouped(MediaRouter mediaRouter, RouteInfo routeInfo, RouteGroup routeGroup);

        public abstract void onRouteUnselected(MediaRouter mediaRouter, int i, RouteInfo routeInfo);

        public abstract void onRouteVolumeChanged(MediaRouter mediaRouter, RouteInfo routeInfo);

        public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
        }
    }

    public static class SimpleCallback extends Callback {
        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
        }

        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
        }

        public void onRouteAdded(MediaRouter router, RouteInfo info) {
        }

        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
        }

        public void onRouteChanged(MediaRouter router, RouteInfo info) {
        }

        public void onRouteGrouped(MediaRouter router, RouteInfo info, RouteGroup group, int index) {
        }

        public void onRouteUngrouped(MediaRouter router, RouteInfo info, RouteGroup group) {
        }

        public void onRouteVolumeChanged(MediaRouter router, RouteInfo info) {
        }
    }

    static class CallbackInfo {
        public final Callback cb;
        public int flags;
        public final MediaRouter router;
        public int type;

        public CallbackInfo(Callback cb, int type, int flags, MediaRouter router) {
            this.cb = cb;
            this.type = type;
            this.flags = flags;
            this.router = router;
        }

        public boolean filterRouteEvent(RouteInfo route) {
            return filterRouteEvent(route.mSupportedTypes);
        }

        public boolean filterRouteEvent(int supportedTypes) {
            return ((this.flags & MediaRouter.ROUTE_TYPE_LIVE_VIDEO) == 0 && (this.type & supportedTypes) == 0) ? MediaRouter.DEBUG : true;
        }
    }

    public static class RouteCategory {
        final boolean mGroupable;
        boolean mIsSystem;
        CharSequence mName;
        int mNameResId;
        int mTypes;

        RouteCategory(CharSequence name, int types, boolean groupable) {
            this.mName = name;
            this.mTypes = types;
            this.mGroupable = groupable;
        }

        RouteCategory(int nameResId, int types, boolean groupable) {
            this.mNameResId = nameResId;
            this.mTypes = types;
            this.mGroupable = groupable;
        }

        public CharSequence getName() {
            return getName(MediaRouter.sStatic.mResources);
        }

        public CharSequence getName(Context context) {
            return getName(context.getResources());
        }

        CharSequence getName(Resources res) {
            if (this.mNameResId != 0) {
                return res.getText(this.mNameResId);
            }
            return this.mName;
        }

        public List<RouteInfo> getRoutes(List<RouteInfo> out) {
            if (out == null) {
                out = new ArrayList();
            } else {
                out.clear();
            }
            int count = MediaRouter.getRouteCountStatic();
            for (int i = 0; i < count; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                RouteInfo route = MediaRouter.getRouteAtStatic(i);
                if (route.mCategory == this) {
                    out.add(route);
                }
            }
            return out;
        }

        public int getSupportedTypes() {
            return this.mTypes;
        }

        public boolean isGroupable() {
            return this.mGroupable;
        }

        public boolean isSystem() {
            return this.mIsSystem;
        }

        public String toString() {
            return "RouteCategory{ name=" + this.mName + " types=" + MediaRouter.typesToString(this.mTypes) + " groupable=" + this.mGroupable + " }";
        }
    }

    public static class RouteInfo {
        public static final int DEVICE_TYPE_BLUETOOTH = 3;
        public static final int DEVICE_TYPE_SPEAKER = 2;
        public static final int DEVICE_TYPE_TV = 1;
        public static final int DEVICE_TYPE_UNKNOWN = 0;
        public static final int PLAYBACK_TYPE_LOCAL = 0;
        public static final int PLAYBACK_TYPE_REMOTE = 1;
        public static final int PLAYBACK_VOLUME_FIXED = 0;
        public static final int PLAYBACK_VOLUME_VARIABLE = 1;
        public static final int STATUS_AVAILABLE = 3;
        public static final int STATUS_CONNECTED = 6;
        public static final int STATUS_CONNECTING = 2;
        public static final int STATUS_IN_USE = 5;
        public static final int STATUS_NONE = 0;
        public static final int STATUS_NOT_AVAILABLE = 4;
        public static final int STATUS_SCANNING = 1;
        final RouteCategory mCategory;
        CharSequence mDescription;
        String mDeviceAddress;
        int mDeviceType;
        boolean mEnabled;
        String mGlobalRouteId;
        RouteGroup mGroup;
        Drawable mIcon;
        CharSequence mName;
        int mNameResId;
        int mPlaybackStream;
        int mPlaybackType;
        Display mPresentationDisplay;
        int mPresentationDisplayId;
        private int mRealStatusCode;
        final Stub mRemoteVolObserver;
        private int mResolvedStatusCode;
        private CharSequence mStatus;
        int mSupportedTypes;
        private Object mTag;
        VolumeCallbackInfo mVcb;
        int mVolume;
        int mVolumeHandling;
        int mVolumeMax;

        RouteInfo(RouteCategory category) {
            this.mPlaybackType = STATUS_NONE;
            this.mVolumeMax = 15;
            this.mVolume = 15;
            this.mVolumeHandling = STATUS_SCANNING;
            this.mPlaybackStream = STATUS_AVAILABLE;
            this.mPresentationDisplayId = -1;
            this.mEnabled = true;
            this.mRemoteVolObserver = new Stub() {

                /* renamed from: android.media.MediaRouter.RouteInfo.1.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ int val$direction;
                    final /* synthetic */ int val$value;

                    AnonymousClass1(int val$direction, int val$value) {
                        this.val$direction = val$direction;
                        this.val$value = val$value;
                    }

                    public void run() {
                        if (RouteInfo.this.mVcb == null) {
                            return;
                        }
                        if (this.val$direction != 0) {
                            RouteInfo.this.mVcb.vcb.onVolumeUpdateRequest(RouteInfo.this.mVcb.route, this.val$direction);
                        } else {
                            RouteInfo.this.mVcb.vcb.onVolumeSetRequest(RouteInfo.this.mVcb.route, this.val$value);
                        }
                    }
                }

                public void dispatchRemoteVolumeUpdate(int direction, int value) {
                    MediaRouter.sStatic.mHandler.post(new AnonymousClass1(direction, value));
                }
            };
            this.mCategory = category;
            this.mDeviceType = STATUS_NONE;
        }

        public CharSequence getName() {
            return getName(MediaRouter.sStatic.mResources);
        }

        public CharSequence getName(Context context) {
            return getName(context.getResources());
        }

        CharSequence getName(Resources res) {
            if (this.mNameResId == 0) {
                return this.mName;
            }
            CharSequence text = res.getText(this.mNameResId);
            this.mName = text;
            return text;
        }

        public CharSequence getDescription() {
            return this.mDescription;
        }

        public CharSequence getStatus() {
            return this.mStatus;
        }

        boolean setRealStatusCode(int statusCode) {
            if (this.mRealStatusCode == statusCode) {
                return MediaRouter.DEBUG;
            }
            this.mRealStatusCode = statusCode;
            return resolveStatusCode();
        }

        boolean resolveStatusCode() {
            int statusCode = this.mRealStatusCode;
            if (isSelected()) {
                switch (statusCode) {
                    case STATUS_SCANNING /*1*/:
                    case STATUS_AVAILABLE /*3*/:
                        statusCode = STATUS_CONNECTING;
                        break;
                }
            }
            if (this.mResolvedStatusCode == statusCode) {
                return MediaRouter.DEBUG;
            }
            int resId;
            this.mResolvedStatusCode = statusCode;
            switch (statusCode) {
                case STATUS_SCANNING /*1*/:
                    resId = 17040617;
                    break;
                case STATUS_CONNECTING /*2*/:
                    resId = 17040618;
                    break;
                case STATUS_AVAILABLE /*3*/:
                    resId = 17040619;
                    break;
                case STATUS_NOT_AVAILABLE /*4*/:
                    resId = 17040620;
                    break;
                case STATUS_IN_USE /*5*/:
                    resId = 17040621;
                    break;
                default:
                    resId = STATUS_NONE;
                    break;
            }
            this.mStatus = resId != 0 ? MediaRouter.sStatic.mResources.getText(resId) : null;
            return true;
        }

        public int getStatusCode() {
            return this.mResolvedStatusCode;
        }

        public int getSupportedTypes() {
            return this.mSupportedTypes;
        }

        public int getDeviceType() {
            return this.mDeviceType;
        }

        public boolean matchesTypes(int types) {
            return (this.mSupportedTypes & types) != 0 ? true : MediaRouter.DEBUG;
        }

        public RouteGroup getGroup() {
            return this.mGroup;
        }

        public RouteCategory getCategory() {
            return this.mCategory;
        }

        public Drawable getIconDrawable() {
            return this.mIcon;
        }

        public void setTag(Object tag) {
            this.mTag = tag;
            routeUpdated();
        }

        public Object getTag() {
            return this.mTag;
        }

        public int getPlaybackType() {
            return this.mPlaybackType;
        }

        public int getPlaybackStream() {
            return this.mPlaybackStream;
        }

        public int getVolume() {
            if (this.mPlaybackType != 0) {
                return this.mVolume;
            }
            int vol = STATUS_NONE;
            try {
                vol = MediaRouter.sStatic.mAudioService.getStreamVolume(this.mPlaybackStream);
            } catch (RemoteException e) {
                Log.e(MediaRouter.TAG, "Error getting local stream volume", e);
            }
            return vol;
        }

        public void requestSetVolume(int volume) {
            if (this.mPlaybackType == 0) {
                try {
                    MediaRouter.sStatic.mAudioService.setStreamVolume(this.mPlaybackStream, volume, STATUS_NONE, ActivityThread.currentPackageName());
                    return;
                } catch (RemoteException e) {
                    Log.e(MediaRouter.TAG, "Error setting local stream volume", e);
                    return;
                }
            }
            MediaRouter.sStatic.requestSetVolume(this, volume);
        }

        public void requestUpdateVolume(int direction) {
            if (this.mPlaybackType == 0) {
                try {
                    MediaRouter.sStatic.mAudioService.setStreamVolume(this.mPlaybackStream, Math.max(STATUS_NONE, Math.min(getVolume() + direction, getVolumeMax())), STATUS_NONE, ActivityThread.currentPackageName());
                    return;
                } catch (RemoteException e) {
                    Log.e(MediaRouter.TAG, "Error setting local stream volume", e);
                    return;
                }
            }
            MediaRouter.sStatic.requestUpdateVolume(this, direction);
        }

        public int getVolumeMax() {
            if (this.mPlaybackType != 0) {
                return this.mVolumeMax;
            }
            int volMax = STATUS_NONE;
            try {
                volMax = MediaRouter.sStatic.mAudioService.getStreamMaxVolume(this.mPlaybackStream);
            } catch (RemoteException e) {
                Log.e(MediaRouter.TAG, "Error getting local stream volume", e);
            }
            return volMax;
        }

        public int getVolumeHandling() {
            return this.mVolumeHandling;
        }

        public Display getPresentationDisplay() {
            return this.mPresentationDisplay;
        }

        boolean updatePresentationDisplay() {
            Display display = choosePresentationDisplay();
            if (this.mPresentationDisplay == display) {
                return MediaRouter.DEBUG;
            }
            this.mPresentationDisplay = display;
            return true;
        }

        private Display choosePresentationDisplay() {
            int i = STATUS_NONE;
            if ((this.mSupportedTypes & STATUS_CONNECTING) != 0) {
                Display[] displays = MediaRouter.sStatic.getAllPresentationDisplays();
                int length;
                Display display;
                if (this.mPresentationDisplayId >= 0) {
                    length = displays.length;
                    while (i < length) {
                        display = displays[i];
                        if (display.getDisplayId() == this.mPresentationDisplayId) {
                            return display;
                        }
                        i += STATUS_SCANNING;
                    }
                    return null;
                } else if (this.mDeviceAddress != null) {
                    length = displays.length;
                    while (i < length) {
                        display = displays[i];
                        if (display.getType() == STATUS_AVAILABLE && this.mDeviceAddress.equals(display.getAddress())) {
                            return display;
                        }
                        i += STATUS_SCANNING;
                    }
                    return null;
                } else if (this == MediaRouter.sStatic.mDefaultAudioVideo && displays.length > 0) {
                    return displays[STATUS_NONE];
                }
            }
            return null;
        }

        public String getDeviceAddress() {
            return this.mDeviceAddress;
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }

        public boolean isConnecting() {
            return this.mResolvedStatusCode == STATUS_CONNECTING ? true : MediaRouter.DEBUG;
        }

        public boolean isSelected() {
            return this == MediaRouter.sStatic.mSelectedRoute ? true : MediaRouter.DEBUG;
        }

        public boolean isDefault() {
            return this == MediaRouter.sStatic.mDefaultAudioVideo ? true : MediaRouter.DEBUG;
        }

        public void select() {
            MediaRouter.selectRouteStatic(this.mSupportedTypes, this, true);
        }

        void setStatusInt(CharSequence status) {
            if (!status.equals(this.mStatus)) {
                this.mStatus = status;
                if (this.mGroup != null) {
                    this.mGroup.memberStatusChanged(this, status);
                }
                routeUpdated();
            }
        }

        void routeUpdated() {
            MediaRouter.updateRoute(this);
        }

        public String toString() {
            return getClass().getSimpleName() + "{ name=" + getName() + ", description=" + getDescription() + ", status=" + getStatus() + ", category=" + getCategory() + ", supportedTypes=" + MediaRouter.typesToString(getSupportedTypes()) + ", presentationDisplay=" + this.mPresentationDisplay + " }";
        }
    }

    public static class RouteGroup extends RouteInfo {
        final ArrayList<RouteInfo> mRoutes;
        private boolean mUpdateName;

        RouteGroup(RouteCategory category) {
            super(category);
            this.mRoutes = new ArrayList();
            this.mGroup = this;
            this.mVolumeHandling = 0;
        }

        CharSequence getName(Resources res) {
            if (this.mUpdateName) {
                updateName();
            }
            return super.getName(res);
        }

        public void addRoute(RouteInfo route) {
            if (route.getGroup() != null) {
                throw new IllegalStateException("Route " + route + " is already part of a group.");
            } else if (route.getCategory() != this.mCategory) {
                throw new IllegalArgumentException("Route cannot be added to a group with a different category. (Route category=" + route.getCategory() + " group category=" + this.mCategory + ")");
            } else {
                int at = this.mRoutes.size();
                this.mRoutes.add(route);
                route.mGroup = this;
                this.mUpdateName = true;
                updateVolume();
                routeUpdated();
                MediaRouter.dispatchRouteGrouped(route, this, at);
            }
        }

        public void addRoute(RouteInfo route, int insertAt) {
            if (route.getGroup() != null) {
                throw new IllegalStateException("Route " + route + " is already part of a group.");
            } else if (route.getCategory() != this.mCategory) {
                throw new IllegalArgumentException("Route cannot be added to a group with a different category. (Route category=" + route.getCategory() + " group category=" + this.mCategory + ")");
            } else {
                this.mRoutes.add(insertAt, route);
                route.mGroup = this;
                this.mUpdateName = true;
                updateVolume();
                routeUpdated();
                MediaRouter.dispatchRouteGrouped(route, this, insertAt);
            }
        }

        public void removeRoute(RouteInfo route) {
            if (route.getGroup() != this) {
                throw new IllegalArgumentException("Route " + route + " is not a member of this group.");
            }
            this.mRoutes.remove(route);
            route.mGroup = null;
            this.mUpdateName = true;
            updateVolume();
            MediaRouter.dispatchRouteUngrouped(route, this);
            routeUpdated();
        }

        public void removeRoute(int index) {
            RouteInfo route = (RouteInfo) this.mRoutes.remove(index);
            route.mGroup = null;
            this.mUpdateName = true;
            updateVolume();
            MediaRouter.dispatchRouteUngrouped(route, this);
            routeUpdated();
        }

        public int getRouteCount() {
            return this.mRoutes.size();
        }

        public RouteInfo getRouteAt(int index) {
            return (RouteInfo) this.mRoutes.get(index);
        }

        public void setIconDrawable(Drawable icon) {
            this.mIcon = icon;
        }

        public void setIconResource(int resId) {
            setIconDrawable(MediaRouter.sStatic.mResources.getDrawable(resId));
        }

        public void requestSetVolume(int volume) {
            int maxVol = getVolumeMax();
            if (maxVol != 0) {
                float scaledVolume = ((float) volume) / ((float) maxVol);
                int routeCount = getRouteCount();
                for (int i = 0; i < routeCount; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                    RouteInfo route = getRouteAt(i);
                    route.requestSetVolume((int) (((float) route.getVolumeMax()) * scaledVolume));
                }
                if (volume != this.mVolume) {
                    this.mVolume = volume;
                    MediaRouter.dispatchRouteVolumeChanged(this);
                }
            }
        }

        public void requestUpdateVolume(int direction) {
            if (getVolumeMax() != 0) {
                int routeCount = getRouteCount();
                int volume = 0;
                for (int i = 0; i < routeCount; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                    RouteInfo route = getRouteAt(i);
                    route.requestUpdateVolume(direction);
                    int routeVol = route.getVolume();
                    if (routeVol > volume) {
                        volume = routeVol;
                    }
                }
                if (volume != this.mVolume) {
                    this.mVolume = volume;
                    MediaRouter.dispatchRouteVolumeChanged(this);
                }
            }
        }

        void memberNameChanged(RouteInfo info, CharSequence name) {
            this.mUpdateName = true;
            routeUpdated();
        }

        void memberStatusChanged(RouteInfo info, CharSequence status) {
            setStatusInt(status);
        }

        void memberVolumeChanged(RouteInfo info) {
            updateVolume();
        }

        void updateVolume() {
            int routeCount = getRouteCount();
            int volume = 0;
            for (int i = 0; i < routeCount; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                int routeVol = getRouteAt(i).getVolume();
                if (routeVol > volume) {
                    volume = routeVol;
                }
            }
            if (volume != this.mVolume) {
                this.mVolume = volume;
                MediaRouter.dispatchRouteVolumeChanged(this);
            }
        }

        void routeUpdated() {
            int types = 0;
            int count = this.mRoutes.size();
            if (count == 0) {
                MediaRouter.removeRouteStatic(this);
                return;
            }
            int i;
            int maxVolume = 0;
            boolean isLocal = true;
            int isFixedVolume = MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
            for (int i2 = 0; i2 < count; i2 += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                RouteInfo route = (RouteInfo) this.mRoutes.get(i2);
                types |= route.mSupportedTypes;
                int routeMaxVolume = route.getVolumeMax();
                if (routeMaxVolume > maxVolume) {
                    maxVolume = routeMaxVolume;
                }
                if (route.getPlaybackType() == 0) {
                    i = MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
                } else {
                    i = 0;
                }
                isLocal &= i;
                if (route.getVolumeHandling() == 0) {
                    i = MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
                } else {
                    i = 0;
                }
                isFixedVolume &= i;
            }
            this.mPlaybackType = isLocal ? 0 : MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
            if (isFixedVolume != 0) {
                i = 0;
            } else {
                i = MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
            }
            this.mVolumeHandling = i;
            this.mSupportedTypes = types;
            this.mVolumeMax = maxVolume;
            this.mIcon = count == MediaRouter.ROUTE_TYPE_LIVE_AUDIO ? ((RouteInfo) this.mRoutes.get(0)).getIconDrawable() : null;
            super.routeUpdated();
        }

        void updateName() {
            StringBuilder sb = new StringBuilder();
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                RouteInfo info = (RouteInfo) this.mRoutes.get(i);
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(info.mName);
            }
            this.mName = sb.toString();
            this.mUpdateName = MediaRouter.DEBUG;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append('[');
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(this.mRoutes.get(i));
            }
            sb.append(']');
            return sb.toString();
        }
    }

    static class Static implements DisplayListener {
        boolean mActivelyScanningWifiDisplays;
        final Context mAppContext;
        final IAudioRoutesObserver.Stub mAudioRoutesObserver;
        final IAudioService mAudioService;
        RouteInfo mBluetoothA2dpRoute;
        final CopyOnWriteArrayList<CallbackInfo> mCallbacks;
        final boolean mCanConfigureWifiDisplays;
        final ArrayList<RouteCategory> mCategories;
        IMediaRouterClient mClient;
        MediaRouterClientState mClientState;
        final AudioRoutesInfo mCurAudioRoutesInfo;
        int mCurrentUserId;
        RouteInfo mDefaultAudioVideo;
        boolean mDiscoverRequestActiveScan;
        int mDiscoveryRequestRouteTypes;
        final DisplayManager mDisplayService;
        final Handler mHandler;
        final IMediaRouterService mMediaRouterService;
        String mPreviousActiveWifiDisplayAddress;
        final Resources mResources;
        final ArrayList<RouteInfo> mRoutes;
        RouteInfo mSelectedRoute;
        final RouteCategory mSystemCategory;

        final class Client extends IMediaRouterClient.Stub {
            Client() {
            }

            public void onStateChanged() {
                Static.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (Client.this == Static.this.mClient) {
                            Static.this.updateClientState();
                        }
                    }
                });
            }
        }

        Static(Context appContext) {
            boolean z;
            this.mCallbacks = new CopyOnWriteArrayList();
            this.mRoutes = new ArrayList();
            this.mCategories = new ArrayList();
            this.mCurAudioRoutesInfo = new AudioRoutesInfo();
            this.mCurrentUserId = -1;
            this.mAudioRoutesObserver = new IAudioRoutesObserver.Stub() {

                /* renamed from: android.media.MediaRouter.Static.1.1 */
                class AnonymousClass1 implements Runnable {
                    final /* synthetic */ AudioRoutesInfo val$newRoutes;

                    AnonymousClass1(AudioRoutesInfo val$newRoutes) {
                        this.val$newRoutes = val$newRoutes;
                    }

                    public void run() {
                        Static.this.updateAudioRoutes(this.val$newRoutes);
                    }
                }

                public void dispatchAudioRoutesChanged(AudioRoutesInfo newRoutes) {
                    Static.this.mHandler.post(new AnonymousClass1(newRoutes));
                }
            };
            this.mAppContext = appContext;
            this.mResources = Resources.getSystem();
            this.mHandler = new Handler(appContext.getMainLooper());
            this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));
            this.mDisplayService = (DisplayManager) appContext.getSystemService(Context.DISPLAY_SERVICE);
            this.mMediaRouterService = IMediaRouterService.Stub.asInterface(ServiceManager.getService(Context.MEDIA_ROUTER_SERVICE));
            this.mSystemCategory = new RouteCategory(17040608, 3, (boolean) MediaRouter.DEBUG);
            this.mSystemCategory.mIsSystem = true;
            if (appContext.checkPermission(permission.CONFIGURE_WIFI_DISPLAY, Process.myPid(), Process.myUid()) == 0) {
                z = true;
            } else {
                z = MediaRouter.DEBUG;
            }
            this.mCanConfigureWifiDisplays = z;
        }

        void startMonitoringRoutes(Context appContext) {
            this.mDefaultAudioVideo = new RouteInfo(this.mSystemCategory);
            this.mDefaultAudioVideo.mNameResId = 17040604;
            this.mDefaultAudioVideo.mSupportedTypes = 3;
            this.mDefaultAudioVideo.updatePresentationDisplay();
            MediaRouter.addRouteStatic(this.mDefaultAudioVideo);
            MediaRouter.updateWifiDisplayStatus(this.mDisplayService.getWifiDisplayStatus());
            appContext.registerReceiver(new WifiDisplayStatusChangedReceiver(), new IntentFilter(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED));
            appContext.registerReceiver(new VolumeChangeReceiver(), new IntentFilter(AudioManager.VOLUME_CHANGED_ACTION));
            this.mDisplayService.registerDisplayListener(this, this.mHandler);
            AudioRoutesInfo newAudioRoutes = null;
            try {
                newAudioRoutes = this.mAudioService.startWatchingRoutes(this.mAudioRoutesObserver);
            } catch (RemoteException e) {
            }
            if (newAudioRoutes != null) {
                updateAudioRoutes(newAudioRoutes);
            }
            rebindAsUser(UserHandle.myUserId());
            if (this.mSelectedRoute == null) {
                MediaRouter.selectDefaultRouteStatic();
            }
        }

        void updateAudioRoutes(AudioRoutesInfo newRoutes) {
            Log.v(MediaRouter.TAG, "Updating audio routes: " + newRoutes);
            if (newRoutes.mainType != this.mCurAudioRoutesInfo.mainType) {
                int name;
                this.mCurAudioRoutesInfo.mainType = newRoutes.mainType;
                if ((newRoutes.mainType & MediaRouter.ROUTE_TYPE_LIVE_VIDEO) != 0 || (newRoutes.mainType & MediaRouter.ROUTE_TYPE_LIVE_AUDIO) != 0) {
                    name = 17040605;
                } else if ((newRoutes.mainType & MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY) != 0) {
                    name = 17040606;
                } else if ((newRoutes.mainType & MediaRouter.CALLBACK_FLAG_PASSIVE_DISCOVERY) != 0) {
                    name = 17040607;
                } else {
                    name = 17040604;
                }
                MediaRouter.sStatic.mDefaultAudioVideo.mNameResId = name;
                MediaRouter.dispatchRouteChanged(MediaRouter.sStatic.mDefaultAudioVideo);
            }
            int mainType = this.mCurAudioRoutesInfo.mainType;
            if (!TextUtils.equals(newRoutes.bluetoothName, this.mCurAudioRoutesInfo.bluetoothName)) {
                this.mCurAudioRoutesInfo.bluetoothName = newRoutes.bluetoothName;
                if (this.mCurAudioRoutesInfo.bluetoothName != null) {
                    if (MediaRouter.sStatic.mBluetoothA2dpRoute == null) {
                        RouteInfo info = new RouteInfo(MediaRouter.sStatic.mSystemCategory);
                        info.mName = this.mCurAudioRoutesInfo.bluetoothName;
                        info.mDescription = MediaRouter.sStatic.mResources.getText(17040609);
                        info.mSupportedTypes = MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
                        info.mDeviceType = 3;
                        MediaRouter.sStatic.mBluetoothA2dpRoute = info;
                        MediaRouter.addRouteStatic(MediaRouter.sStatic.mBluetoothA2dpRoute);
                    } else {
                        MediaRouter.sStatic.mBluetoothA2dpRoute.mName = this.mCurAudioRoutesInfo.bluetoothName;
                        MediaRouter.dispatchRouteChanged(MediaRouter.sStatic.mBluetoothA2dpRoute);
                    }
                } else if (MediaRouter.sStatic.mBluetoothA2dpRoute != null) {
                    MediaRouter.removeRouteStatic(MediaRouter.sStatic.mBluetoothA2dpRoute);
                    MediaRouter.sStatic.mBluetoothA2dpRoute = null;
                }
            }
            if (this.mBluetoothA2dpRoute != null) {
                boolean a2dpEnabled = isBluetoothA2dpOn();
                if (mainType != 0 && this.mSelectedRoute == this.mBluetoothA2dpRoute && !a2dpEnabled) {
                    MediaRouter.selectRouteStatic(MediaRouter.ROUTE_TYPE_LIVE_AUDIO, this.mDefaultAudioVideo, MediaRouter.DEBUG);
                } else if ((this.mSelectedRoute == this.mDefaultAudioVideo || this.mSelectedRoute == null) && a2dpEnabled) {
                    MediaRouter.selectRouteStatic(MediaRouter.ROUTE_TYPE_LIVE_AUDIO, this.mBluetoothA2dpRoute, MediaRouter.DEBUG);
                }
            }
        }

        boolean isBluetoothA2dpOn() {
            try {
                return this.mAudioService.isBluetoothA2dpOn();
            } catch (RemoteException e) {
                Log.e(MediaRouter.TAG, "Error querying Bluetooth A2DP state", e);
                return MediaRouter.DEBUG;
            }
        }

        void updateDiscoveryRequest() {
            int routeTypes = 0;
            int passiveRouteTypes = 0;
            boolean activeScan = MediaRouter.DEBUG;
            boolean activeScanWifiDisplay = MediaRouter.DEBUG;
            int count = this.mCallbacks.size();
            for (int i = 0; i < count; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                CallbackInfo cbi = (CallbackInfo) this.mCallbacks.get(i);
                if ((cbi.flags & 5) != 0) {
                    routeTypes |= cbi.type;
                } else if ((cbi.flags & MediaRouter.CALLBACK_FLAG_PASSIVE_DISCOVERY) != 0) {
                    passiveRouteTypes |= cbi.type;
                } else {
                    routeTypes |= cbi.type;
                }
                if ((cbi.flags & MediaRouter.ROUTE_TYPE_LIVE_AUDIO) != 0) {
                    activeScan = true;
                    if ((cbi.type & MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY) != 0) {
                        activeScanWifiDisplay = true;
                    }
                }
            }
            if (routeTypes != 0 || activeScan) {
                routeTypes |= passiveRouteTypes;
            }
            if (this.mCanConfigureWifiDisplays) {
                if (this.mSelectedRoute != null && this.mSelectedRoute.matchesTypes(MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY)) {
                    activeScanWifiDisplay = MediaRouter.DEBUG;
                }
                if (activeScanWifiDisplay) {
                    if (!this.mActivelyScanningWifiDisplays) {
                        this.mActivelyScanningWifiDisplays = true;
                        this.mDisplayService.startWifiDisplayScan();
                    }
                } else if (this.mActivelyScanningWifiDisplays) {
                    this.mActivelyScanningWifiDisplays = MediaRouter.DEBUG;
                    this.mDisplayService.stopWifiDisplayScan();
                }
            }
            if (routeTypes != this.mDiscoveryRequestRouteTypes || activeScan != this.mDiscoverRequestActiveScan) {
                this.mDiscoveryRequestRouteTypes = routeTypes;
                this.mDiscoverRequestActiveScan = activeScan;
                publishClientDiscoveryRequest();
            }
        }

        public void onDisplayAdded(int displayId) {
            updatePresentationDisplays(displayId);
        }

        public void onDisplayChanged(int displayId) {
            updatePresentationDisplays(displayId);
        }

        public void onDisplayRemoved(int displayId) {
            updatePresentationDisplays(displayId);
        }

        public Display[] getAllPresentationDisplays() {
            return this.mDisplayService.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        }

        private void updatePresentationDisplays(int changedDisplayId) {
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                RouteInfo route = (RouteInfo) this.mRoutes.get(i);
                if (route.updatePresentationDisplay() || (route.mPresentationDisplay != null && route.mPresentationDisplay.getDisplayId() == changedDisplayId)) {
                    MediaRouter.dispatchRoutePresentationDisplayChanged(route);
                }
            }
        }

        void setSelectedRoute(RouteInfo info, boolean explicit) {
            this.mSelectedRoute = info;
            publishClientSelectedRoute(explicit);
        }

        void rebindAsUser(int userId) {
            if (this.mCurrentUserId == userId && userId >= 0) {
                if (this.mClient != null) {
                    return;
                }
            }
            if (this.mClient != null) {
                try {
                    this.mMediaRouterService.unregisterClient(this.mClient);
                } catch (RemoteException ex) {
                    Log.e(MediaRouter.TAG, "Unable to unregister media router client.", ex);
                }
                this.mClient = null;
            }
            this.mCurrentUserId = userId;
            try {
                Client client = new Client();
                this.mMediaRouterService.registerClientAsUser(client, this.mAppContext.getPackageName(), userId);
                this.mClient = client;
            } catch (RemoteException ex2) {
                Log.e(MediaRouter.TAG, "Unable to register media router client.", ex2);
            }
            publishClientDiscoveryRequest();
            publishClientSelectedRoute(MediaRouter.DEBUG);
            updateClientState();
        }

        void publishClientDiscoveryRequest() {
            if (this.mClient != null) {
                try {
                    this.mMediaRouterService.setDiscoveryRequest(this.mClient, this.mDiscoveryRequestRouteTypes, this.mDiscoverRequestActiveScan);
                } catch (RemoteException ex) {
                    Log.e(MediaRouter.TAG, "Unable to publish media router client discovery request.", ex);
                }
            }
        }

        void publishClientSelectedRoute(boolean explicit) {
            String str = null;
            if (this.mClient != null) {
                try {
                    IMediaRouterService iMediaRouterService = this.mMediaRouterService;
                    IMediaRouterClient iMediaRouterClient = this.mClient;
                    if (this.mSelectedRoute != null) {
                        str = this.mSelectedRoute.mGlobalRouteId;
                    }
                    iMediaRouterService.setSelectedRoute(iMediaRouterClient, str, explicit);
                } catch (RemoteException ex) {
                    Log.e(MediaRouter.TAG, "Unable to publish media router client selected route.", ex);
                }
            }
        }

        void updateClientState() {
            int i;
            this.mClientState = null;
            if (this.mClient != null) {
                try {
                    this.mClientState = this.mMediaRouterService.getState(this.mClient);
                } catch (RemoteException ex) {
                    Log.e(MediaRouter.TAG, "Unable to retrieve media router client state.", ex);
                }
            }
            ArrayList arrayList = this.mClientState != null ? this.mClientState.routes : null;
            String str = this.mClientState != null ? this.mClientState.globallySelectedRouteId : null;
            int globalRouteCount = arrayList != null ? arrayList.size() : 0;
            for (i = 0; i < globalRouteCount; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                android.media.MediaRouterClientState.RouteInfo globalRoute = (android.media.MediaRouterClientState.RouteInfo) arrayList.get(i);
                RouteInfo route = findGlobalRoute(globalRoute.id);
                if (route == null) {
                    MediaRouter.addRouteStatic(makeGlobalRoute(globalRoute));
                } else {
                    updateGlobalRoute(route, globalRoute);
                }
            }
            if (str != null) {
                route = findGlobalRoute(str);
                if (route == null) {
                    Log.w(MediaRouter.TAG, "Could not find new globally selected route: " + str);
                } else if (route != this.mSelectedRoute) {
                    if (MediaRouter.DEBUG) {
                        Log.d(MediaRouter.TAG, "Selecting new globally selected route: " + route);
                    }
                    MediaRouter.selectRouteStatic(route.mSupportedTypes, route, MediaRouter.DEBUG);
                }
            } else if (!(this.mSelectedRoute == null || this.mSelectedRoute.mGlobalRouteId == null)) {
                if (MediaRouter.DEBUG) {
                    Log.d(MediaRouter.TAG, "Unselecting previous globally selected route: " + this.mSelectedRoute);
                }
                MediaRouter.selectDefaultRouteStatic();
            }
            int i2 = this.mRoutes.size();
            while (true) {
                i = i2 - 1;
                if (i2 > 0) {
                    route = (RouteInfo) this.mRoutes.get(i);
                    String globalRouteId = route.mGlobalRouteId;
                    if (globalRouteId != null) {
                        for (int j = 0; j < globalRouteCount; j += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                            if (globalRouteId.equals(((android.media.MediaRouterClientState.RouteInfo) arrayList.get(j)).id)) {
                                break;
                            }
                        }
                        MediaRouter.removeRouteStatic(route);
                    }
                    i2 = i;
                } else {
                    return;
                }
            }
        }

        void requestSetVolume(RouteInfo route, int volume) {
            if (route.mGlobalRouteId != null && this.mClient != null) {
                try {
                    this.mMediaRouterService.requestSetVolume(this.mClient, route.mGlobalRouteId, volume);
                } catch (RemoteException ex) {
                    Log.w(MediaRouter.TAG, "Unable to request volume change.", ex);
                }
            }
        }

        void requestUpdateVolume(RouteInfo route, int direction) {
            if (route.mGlobalRouteId != null && this.mClient != null) {
                try {
                    this.mMediaRouterService.requestUpdateVolume(this.mClient, route.mGlobalRouteId, direction);
                } catch (RemoteException ex) {
                    Log.w(MediaRouter.TAG, "Unable to request volume change.", ex);
                }
            }
        }

        RouteInfo makeGlobalRoute(android.media.MediaRouterClientState.RouteInfo globalRoute) {
            RouteInfo route = new RouteInfo(MediaRouter.sStatic.mSystemCategory);
            route.mGlobalRouteId = globalRoute.id;
            route.mName = globalRoute.name;
            route.mDescription = globalRoute.description;
            route.mSupportedTypes = globalRoute.supportedTypes;
            route.mDeviceType = globalRoute.deviceType;
            route.mEnabled = globalRoute.enabled;
            route.setRealStatusCode(globalRoute.statusCode);
            route.mPlaybackType = globalRoute.playbackType;
            route.mPlaybackStream = globalRoute.playbackStream;
            route.mVolume = globalRoute.volume;
            route.mVolumeMax = globalRoute.volumeMax;
            route.mVolumeHandling = globalRoute.volumeHandling;
            route.mPresentationDisplayId = globalRoute.presentationDisplayId;
            route.updatePresentationDisplay();
            return route;
        }

        void updateGlobalRoute(RouteInfo route, android.media.MediaRouterClientState.RouteInfo globalRoute) {
            boolean changed = MediaRouter.DEBUG;
            boolean volumeChanged = MediaRouter.DEBUG;
            boolean presentationDisplayChanged = MediaRouter.DEBUG;
            if (!Objects.equals(route.mName, globalRoute.name)) {
                route.mName = globalRoute.name;
                changed = true;
            }
            if (!Objects.equals(route.mDescription, globalRoute.description)) {
                route.mDescription = globalRoute.description;
                changed = true;
            }
            int oldSupportedTypes = route.mSupportedTypes;
            if (oldSupportedTypes != globalRoute.supportedTypes) {
                route.mSupportedTypes = globalRoute.supportedTypes;
                changed = true;
            }
            if (route.mEnabled != globalRoute.enabled) {
                route.mEnabled = globalRoute.enabled;
                changed = true;
            }
            if (route.mRealStatusCode != globalRoute.statusCode) {
                route.setRealStatusCode(globalRoute.statusCode);
                changed = true;
            }
            if (route.mPlaybackType != globalRoute.playbackType) {
                route.mPlaybackType = globalRoute.playbackType;
                changed = true;
            }
            if (route.mPlaybackStream != globalRoute.playbackStream) {
                route.mPlaybackStream = globalRoute.playbackStream;
                changed = true;
            }
            if (route.mVolume != globalRoute.volume) {
                route.mVolume = globalRoute.volume;
                changed = true;
                volumeChanged = true;
            }
            if (route.mVolumeMax != globalRoute.volumeMax) {
                route.mVolumeMax = globalRoute.volumeMax;
                changed = true;
                volumeChanged = true;
            }
            if (route.mVolumeHandling != globalRoute.volumeHandling) {
                route.mVolumeHandling = globalRoute.volumeHandling;
                changed = true;
                volumeChanged = true;
            }
            if (route.mPresentationDisplayId != globalRoute.presentationDisplayId) {
                route.mPresentationDisplayId = globalRoute.presentationDisplayId;
                route.updatePresentationDisplay();
                changed = true;
                presentationDisplayChanged = true;
            }
            if (changed) {
                MediaRouter.dispatchRouteChanged(route, oldSupportedTypes);
            }
            if (volumeChanged) {
                MediaRouter.dispatchRouteVolumeChanged(route);
            }
            if (presentationDisplayChanged) {
                MediaRouter.dispatchRoutePresentationDisplayChanged(route);
            }
        }

        RouteInfo findGlobalRoute(String globalRouteId) {
            int count = this.mRoutes.size();
            for (int i = 0; i < count; i += MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                RouteInfo route = (RouteInfo) this.mRoutes.get(i);
                if (globalRouteId.equals(route.mGlobalRouteId)) {
                    return route;
                }
            }
            return null;
        }
    }

    public static class UserRouteInfo extends RouteInfo {
        RemoteControlClient mRcc;
        SessionVolumeProvider mSvp;

        class SessionVolumeProvider extends VolumeProvider {

            /* renamed from: android.media.MediaRouter.UserRouteInfo.SessionVolumeProvider.1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ int val$volume;

                AnonymousClass1(int val$volume) {
                    this.val$volume = val$volume;
                }

                public void run() {
                    if (UserRouteInfo.this.mVcb != null) {
                        UserRouteInfo.this.mVcb.vcb.onVolumeSetRequest(UserRouteInfo.this.mVcb.route, this.val$volume);
                    }
                }
            }

            /* renamed from: android.media.MediaRouter.UserRouteInfo.SessionVolumeProvider.2 */
            class AnonymousClass2 implements Runnable {
                final /* synthetic */ int val$direction;

                AnonymousClass2(int val$direction) {
                    this.val$direction = val$direction;
                }

                public void run() {
                    if (UserRouteInfo.this.mVcb != null) {
                        UserRouteInfo.this.mVcb.vcb.onVolumeUpdateRequest(UserRouteInfo.this.mVcb.route, this.val$direction);
                    }
                }
            }

            public SessionVolumeProvider(int volumeControl, int maxVolume, int currentVolume) {
                super(volumeControl, maxVolume, currentVolume);
            }

            public void onSetVolumeTo(int volume) {
                MediaRouter.sStatic.mHandler.post(new AnonymousClass1(volume));
            }

            public void onAdjustVolume(int direction) {
                MediaRouter.sStatic.mHandler.post(new AnonymousClass2(direction));
            }
        }

        UserRouteInfo(RouteCategory category) {
            super(category);
            this.mSupportedTypes = MediaRouter.ROUTE_TYPE_USER;
            this.mPlaybackType = MediaRouter.ROUTE_TYPE_LIVE_AUDIO;
            this.mVolumeHandling = 0;
        }

        public void setName(CharSequence name) {
            this.mName = name;
            routeUpdated();
        }

        public void setName(int resId) {
            this.mNameResId = resId;
            this.mName = null;
            routeUpdated();
        }

        public void setDescription(CharSequence description) {
            this.mDescription = description;
            routeUpdated();
        }

        public void setStatus(CharSequence status) {
            setStatusInt(status);
        }

        public void setRemoteControlClient(RemoteControlClient rcc) {
            this.mRcc = rcc;
            updatePlaybackInfoOnRcc();
        }

        public RemoteControlClient getRemoteControlClient() {
            return this.mRcc;
        }

        public void setIconDrawable(Drawable icon) {
            this.mIcon = icon;
        }

        public void setIconResource(int resId) {
            setIconDrawable(MediaRouter.sStatic.mResources.getDrawable(resId));
        }

        public void setVolumeCallback(VolumeCallback vcb) {
            this.mVcb = new VolumeCallbackInfo(vcb, this);
        }

        public void setPlaybackType(int type) {
            if (this.mPlaybackType != type) {
                this.mPlaybackType = type;
                configureSessionVolume();
            }
        }

        public void setVolumeHandling(int volumeHandling) {
            if (this.mVolumeHandling != volumeHandling) {
                this.mVolumeHandling = volumeHandling;
                configureSessionVolume();
            }
        }

        public void setVolume(int volume) {
            volume = Math.max(0, Math.min(volume, getVolumeMax()));
            if (this.mVolume != volume) {
                this.mVolume = volume;
                if (this.mSvp != null) {
                    this.mSvp.setCurrentVolume(this.mVolume);
                }
                MediaRouter.dispatchRouteVolumeChanged(this);
                if (this.mGroup != null) {
                    this.mGroup.memberVolumeChanged(this);
                }
            }
        }

        public void requestSetVolume(int volume) {
            if (this.mVolumeHandling == MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                if (this.mVcb == null) {
                    Log.e(MediaRouter.TAG, "Cannot requestSetVolume on user route - no volume callback set");
                    return;
                }
                this.mVcb.vcb.onVolumeSetRequest(this, volume);
            }
        }

        public void requestUpdateVolume(int direction) {
            if (this.mVolumeHandling == MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                if (this.mVcb == null) {
                    Log.e(MediaRouter.TAG, "Cannot requestChangeVolume on user route - no volumec callback set");
                    return;
                }
                this.mVcb.vcb.onVolumeUpdateRequest(this, direction);
            }
        }

        public void setVolumeMax(int volumeMax) {
            if (this.mVolumeMax != volumeMax) {
                this.mVolumeMax = volumeMax;
                configureSessionVolume();
            }
        }

        public void setPlaybackStream(int stream) {
            if (this.mPlaybackStream != stream) {
                this.mPlaybackStream = stream;
                configureSessionVolume();
            }
        }

        private void updatePlaybackInfoOnRcc() {
            configureSessionVolume();
        }

        private void configureSessionVolume() {
            if (this.mRcc == null) {
                if (MediaRouter.DEBUG) {
                    Log.d(MediaRouter.TAG, "No Rcc to configure volume for route " + this.mName);
                }
                return;
            }
            MediaSession session = this.mRcc.getMediaSession();
            if (session == null) {
                if (MediaRouter.DEBUG) {
                    Log.d(MediaRouter.TAG, "Rcc has no session to configure volume");
                }
                return;
            }
            if (this.mPlaybackType == MediaRouter.ROUTE_TYPE_LIVE_AUDIO) {
                int volumeControl = 0;
                switch (this.mVolumeHandling) {
                    case MediaRouter.ROUTE_TYPE_LIVE_AUDIO /*1*/:
                        volumeControl = MediaRouter.ROUTE_TYPE_LIVE_VIDEO;
                        break;
                }
                if (this.mSvp != null && this.mSvp.getVolumeControl() == volumeControl) {
                    if (this.mSvp.getMaxVolume() != this.mVolumeMax) {
                    }
                }
                this.mSvp = new SessionVolumeProvider(volumeControl, this.mVolumeMax, this.mVolume);
                session.setPlaybackToRemote(this.mSvp);
            } else {
                Builder bob = new Builder();
                bob.setLegacyStreamType(this.mPlaybackStream);
                session.setPlaybackToLocal(bob.build());
                this.mSvp = null;
            }
        }
    }

    public static abstract class VolumeCallback {
        public abstract void onVolumeSetRequest(RouteInfo routeInfo, int i);

        public abstract void onVolumeUpdateRequest(RouteInfo routeInfo, int i);
    }

    static class VolumeCallbackInfo {
        public final RouteInfo route;
        public final VolumeCallback vcb;

        public VolumeCallbackInfo(VolumeCallback vcb, RouteInfo route) {
            this.vcb = vcb;
            this.route = route;
        }
    }

    static class VolumeChangeReceiver extends BroadcastReceiver {
        VolumeChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.VOLUME_CHANGED_ACTION) && intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1) == 3) {
                int newVolume = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, 0);
                if (newVolume != intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, 0)) {
                    MediaRouter.systemVolumeChanged(newVolume);
                }
            }
        }
    }

    static class WifiDisplayStatusChangedReceiver extends BroadcastReceiver {
        WifiDisplayStatusChangedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)) {
                MediaRouter.updateWifiDisplayStatus((WifiDisplayStatus) intent.getParcelableExtra(DisplayManager.EXTRA_WIFI_DISPLAY_STATUS));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaRouter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaRouter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaRouter.<clinit>():void");
    }

    static String typesToString(int types) {
        StringBuilder result = new StringBuilder();
        if ((types & ROUTE_TYPE_LIVE_AUDIO) != 0) {
            result.append("ROUTE_TYPE_LIVE_AUDIO ");
        }
        if ((types & ROUTE_TYPE_LIVE_VIDEO) != 0) {
            result.append("ROUTE_TYPE_LIVE_VIDEO ");
        }
        if ((types & ROUTE_TYPE_REMOTE_DISPLAY) != 0) {
            result.append("ROUTE_TYPE_REMOTE_DISPLAY ");
        }
        if ((ROUTE_TYPE_USER & types) != 0) {
            result.append("ROUTE_TYPE_USER ");
        }
        return result.toString();
    }

    public MediaRouter(Context context) {
        synchronized (Static.class) {
            if (sStatic == null) {
                Context appContext = context.getApplicationContext();
                sStatic = new Static(appContext);
                sStatic.startMonitoringRoutes(appContext);
            }
        }
    }

    public RouteInfo getDefaultRoute() {
        return sStatic.mDefaultAudioVideo;
    }

    public RouteCategory getSystemCategory() {
        return sStatic.mSystemCategory;
    }

    public RouteInfo getSelectedRoute() {
        return getSelectedRoute(ROUTE_TYPE_ANY);
    }

    public RouteInfo getSelectedRoute(int type) {
        if (sStatic.mSelectedRoute != null && (sStatic.mSelectedRoute.mSupportedTypes & type) != 0) {
            return sStatic.mSelectedRoute;
        }
        if (type == ROUTE_TYPE_USER) {
            return null;
        }
        return sStatic.mDefaultAudioVideo;
    }

    public boolean isRouteAvailable(int types, int flags) {
        int count = sStatic.mRoutes.size();
        for (int i = 0; i < count; i += ROUTE_TYPE_LIVE_AUDIO) {
            RouteInfo route = (RouteInfo) sStatic.mRoutes.get(i);
            if (route.matchesTypes(types) && ((flags & ROUTE_TYPE_LIVE_AUDIO) == 0 || route != sStatic.mDefaultAudioVideo)) {
                return true;
            }
        }
        return DEBUG;
    }

    public void addCallback(int types, Callback cb) {
        addCallback(types, cb, 0);
    }

    public void addCallback(int types, Callback cb, int flags) {
        int index = findCallbackInfo(cb);
        if (index >= 0) {
            CallbackInfo info = (CallbackInfo) sStatic.mCallbacks.get(index);
            info.type |= types;
            info.flags |= flags;
        } else {
            sStatic.mCallbacks.add(new CallbackInfo(cb, types, flags, this));
        }
        sStatic.updateDiscoveryRequest();
    }

    public void removeCallback(Callback cb) {
        int index = findCallbackInfo(cb);
        if (index >= 0) {
            sStatic.mCallbacks.remove(index);
            sStatic.updateDiscoveryRequest();
            return;
        }
        Log.w(TAG, "removeCallback(" + cb + "): callback not registered");
    }

    private int findCallbackInfo(Callback cb) {
        int count = sStatic.mCallbacks.size();
        for (int i = 0; i < count; i += ROUTE_TYPE_LIVE_AUDIO) {
            if (((CallbackInfo) sStatic.mCallbacks.get(i)).cb == cb) {
                return i;
            }
        }
        return -1;
    }

    public void selectRoute(int types, RouteInfo route) {
        if (route == null) {
            throw new IllegalArgumentException("Route cannot be null.");
        }
        selectRouteStatic(types, route, true);
    }

    public void selectRouteInt(int types, RouteInfo route, boolean explicit) {
        selectRouteStatic(types, route, explicit);
    }

    static void selectRouteStatic(int types, RouteInfo route, boolean explicit) {
        boolean z = true;
        Log.v(TAG, "Selecting route: " + route);
        if (!-assertionsDisabled) {
            if (!(route != null)) {
                throw new AssertionError();
            }
        }
        RouteInfo oldRoute = sStatic.mSelectedRoute;
        if (oldRoute != route) {
            if (route.matchesTypes(types)) {
                RouteInfo btRoute = sStatic.mBluetoothA2dpRoute;
                if (!(btRoute == null || (types & ROUTE_TYPE_LIVE_AUDIO) == 0 || (route != btRoute && route != sStatic.mDefaultAudioVideo))) {
                    try {
                        IAudioService iAudioService = sStatic.mAudioService;
                        if (route != btRoute) {
                            z = DEBUG;
                        }
                        iAudioService.setBluetoothA2dpOn(z);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error changing Bluetooth A2DP state", e);
                    }
                }
                WifiDisplay activeDisplay = sStatic.mDisplayService.getWifiDisplayStatus().getActiveDisplay();
                boolean oldRouteHasAddress = (oldRoute == null || oldRoute.mDeviceAddress == null) ? DEBUG : true;
                boolean newRouteHasAddress = route.mDeviceAddress != null ? true : DEBUG;
                if (activeDisplay != null || oldRouteHasAddress || newRouteHasAddress) {
                    if (!newRouteHasAddress || matchesDeviceAddress(activeDisplay, route)) {
                        if (!(activeDisplay == null || newRouteHasAddress)) {
                            sStatic.mDisplayService.disconnectWifiDisplay();
                        }
                    } else if (sStatic.mCanConfigureWifiDisplays) {
                        sStatic.mDisplayService.connectWifiDisplay(route.mDeviceAddress);
                    } else {
                        Log.e(TAG, "Cannot connect to wifi displays because this process is not allowed to do so.");
                    }
                }
                sStatic.setSelectedRoute(route, explicit);
                if (oldRoute != null) {
                    dispatchRouteUnselected(oldRoute.getSupportedTypes() & types, oldRoute);
                    if (oldRoute.resolveStatusCode()) {
                        dispatchRouteChanged(oldRoute);
                    }
                }
                if (route != null) {
                    if (route.resolveStatusCode()) {
                        dispatchRouteChanged(route);
                    }
                    dispatchRouteSelected(route.getSupportedTypes() & types, route);
                }
                sStatic.updateDiscoveryRequest();
                return;
            }
            Log.w(TAG, "selectRoute ignored; cannot select route with supported types " + typesToString(route.getSupportedTypes()) + " into route types " + typesToString(types));
        }
    }

    static void selectDefaultRouteStatic() {
        if (sStatic.mSelectedRoute == sStatic.mBluetoothA2dpRoute || sStatic.mBluetoothA2dpRoute == null || !sStatic.isBluetoothA2dpOn()) {
            selectRouteStatic(ROUTE_TYPE_ANY, sStatic.mDefaultAudioVideo, DEBUG);
        } else {
            selectRouteStatic(ROUTE_TYPE_ANY, sStatic.mBluetoothA2dpRoute, DEBUG);
        }
    }

    static boolean matchesDeviceAddress(WifiDisplay display, RouteInfo info) {
        boolean routeHasAddress = (info == null || info.mDeviceAddress == null) ? DEBUG : true;
        if (display == null && !routeHasAddress) {
            return true;
        }
        if (display == null || !routeHasAddress) {
            return DEBUG;
        }
        return display.getDeviceAddress().equals(info.mDeviceAddress);
    }

    public void addUserRoute(UserRouteInfo info) {
        addRouteStatic(info);
    }

    public void addRouteInt(RouteInfo info) {
        addRouteStatic(info);
    }

    static void addRouteStatic(RouteInfo info) {
        Log.v(TAG, "Adding route: " + info);
        RouteCategory cat = info.getCategory();
        if (!sStatic.mCategories.contains(cat)) {
            sStatic.mCategories.add(cat);
        }
        if (!cat.isGroupable() || (info instanceof RouteGroup)) {
            sStatic.mRoutes.add(info);
            dispatchRouteAdded(info);
            return;
        }
        RouteInfo group = new RouteGroup(info.getCategory());
        group.mSupportedTypes = info.mSupportedTypes;
        sStatic.mRoutes.add(group);
        dispatchRouteAdded(group);
        group.addRoute(info);
        info = group;
    }

    public void removeUserRoute(UserRouteInfo info) {
        removeRouteStatic(info);
    }

    public void clearUserRoutes() {
        int i = 0;
        while (i < sStatic.mRoutes.size()) {
            RouteInfo info = (RouteInfo) sStatic.mRoutes.get(i);
            if ((info instanceof UserRouteInfo) || (info instanceof RouteGroup)) {
                removeRouteStatic(info);
                i--;
            }
            i += ROUTE_TYPE_LIVE_AUDIO;
        }
    }

    public void removeRouteInt(RouteInfo info) {
        removeRouteStatic(info);
    }

    static void removeRouteStatic(RouteInfo info) {
        Log.v(TAG, "Removing route: " + info);
        if (sStatic.mRoutes.remove(info)) {
            RouteCategory removingCat = info.getCategory();
            int count = sStatic.mRoutes.size();
            boolean found = DEBUG;
            for (int i = 0; i < count; i += ROUTE_TYPE_LIVE_AUDIO) {
                if (removingCat == ((RouteInfo) sStatic.mRoutes.get(i)).getCategory()) {
                    found = true;
                    break;
                }
            }
            if (info.isSelected()) {
                selectDefaultRouteStatic();
            }
            if (!found) {
                sStatic.mCategories.remove(removingCat);
            }
            dispatchRouteRemoved(info);
        }
    }

    public int getCategoryCount() {
        return sStatic.mCategories.size();
    }

    public RouteCategory getCategoryAt(int index) {
        return (RouteCategory) sStatic.mCategories.get(index);
    }

    public int getRouteCount() {
        return sStatic.mRoutes.size();
    }

    public RouteInfo getRouteAt(int index) {
        return (RouteInfo) sStatic.mRoutes.get(index);
    }

    static int getRouteCountStatic() {
        return sStatic.mRoutes.size();
    }

    static RouteInfo getRouteAtStatic(int index) {
        return (RouteInfo) sStatic.mRoutes.get(index);
    }

    public UserRouteInfo createUserRoute(RouteCategory category) {
        return new UserRouteInfo(category);
    }

    public RouteCategory createRouteCategory(CharSequence name, boolean isGroupable) {
        return new RouteCategory(name, (int) ROUTE_TYPE_USER, isGroupable);
    }

    public RouteCategory createRouteCategory(int nameResId, boolean isGroupable) {
        return new RouteCategory(nameResId, (int) ROUTE_TYPE_USER, isGroupable);
    }

    public void rebindAsUser(int userId) {
        sStatic.rebindAsUser(userId);
    }

    static void updateRoute(RouteInfo info) {
        dispatchRouteChanged(info);
    }

    static void dispatchRouteSelected(int type, RouteInfo info) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteSelected(cbi.router, type, info);
            }
        }
    }

    static void dispatchRouteUnselected(int type, RouteInfo info) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteUnselected(cbi.router, type, info);
            }
        }
    }

    static void dispatchRouteChanged(RouteInfo info) {
        dispatchRouteChanged(info, info.mSupportedTypes);
    }

    static void dispatchRouteChanged(RouteInfo info, int oldSupportedTypes) {
        Log.v(TAG, "Dispatching route change: " + info);
        int newSupportedTypes = info.mSupportedTypes;
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            boolean oldVisibility = cbi.filterRouteEvent(oldSupportedTypes);
            boolean newVisibility = cbi.filterRouteEvent(newSupportedTypes);
            if (!oldVisibility && newVisibility) {
                cbi.cb.onRouteAdded(cbi.router, info);
                if (info.isSelected()) {
                    cbi.cb.onRouteSelected(cbi.router, newSupportedTypes, info);
                }
            }
            if (oldVisibility || newVisibility) {
                cbi.cb.onRouteChanged(cbi.router, info);
            }
            if (oldVisibility && !newVisibility) {
                if (info.isSelected()) {
                    cbi.cb.onRouteUnselected(cbi.router, oldSupportedTypes, info);
                }
                cbi.cb.onRouteRemoved(cbi.router, info);
            }
        }
    }

    static void dispatchRouteAdded(RouteInfo info) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteAdded(cbi.router, info);
            }
        }
    }

    static void dispatchRouteRemoved(RouteInfo info) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteRemoved(cbi.router, info);
            }
        }
    }

    static void dispatchRouteGrouped(RouteInfo info, RouteGroup group, int index) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent((RouteInfo) group)) {
                cbi.cb.onRouteGrouped(cbi.router, info, group, index);
            }
        }
    }

    static void dispatchRouteUngrouped(RouteInfo info, RouteGroup group) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent((RouteInfo) group)) {
                cbi.cb.onRouteUngrouped(cbi.router, info, group);
            }
        }
    }

    static void dispatchRouteVolumeChanged(RouteInfo info) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRouteVolumeChanged(cbi.router, info);
            }
        }
    }

    static void dispatchRoutePresentationDisplayChanged(RouteInfo info) {
        for (CallbackInfo cbi : sStatic.mCallbacks) {
            if (cbi.filterRouteEvent(info)) {
                cbi.cb.onRoutePresentationDisplayChanged(cbi.router, info);
            }
        }
    }

    static void systemVolumeChanged(int newValue) {
        RouteInfo selectedRoute = sStatic.mSelectedRoute;
        if (selectedRoute != null) {
            if (selectedRoute == sStatic.mBluetoothA2dpRoute || selectedRoute == sStatic.mDefaultAudioVideo) {
                dispatchRouteVolumeChanged(selectedRoute);
            } else if (sStatic.mBluetoothA2dpRoute != null) {
                try {
                    dispatchRouteVolumeChanged(sStatic.mAudioService.isBluetoothA2dpOn() ? sStatic.mBluetoothA2dpRoute : sStatic.mDefaultAudioVideo);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error checking Bluetooth A2DP state to report volume change", e);
                }
            } else {
                dispatchRouteVolumeChanged(sStatic.mDefaultAudioVideo);
            }
        }
    }

    static void updateWifiDisplayStatus(WifiDisplayStatus status) {
        WifiDisplay[] displays;
        WifiDisplay activeDisplay;
        int i;
        RouteInfo route;
        if (status.getFeatureState() == 3) {
            displays = status.getDisplays();
            activeDisplay = status.getActiveDisplay();
            if (!sStatic.mCanConfigureWifiDisplays) {
                if (activeDisplay != null) {
                    displays = new WifiDisplay[ROUTE_TYPE_LIVE_AUDIO];
                    displays[0] = activeDisplay;
                } else {
                    displays = WifiDisplay.EMPTY_ARRAY;
                }
            }
        } else {
            displays = WifiDisplay.EMPTY_ARRAY;
            activeDisplay = null;
        }
        String deviceAddress = activeDisplay != null ? activeDisplay.getDeviceAddress() : null;
        for (i = 0; i < displays.length; i += ROUTE_TYPE_LIVE_AUDIO) {
            WifiDisplay d = displays[i];
            if (shouldShowWifiDisplay(d, activeDisplay)) {
                route = findWifiDisplayRoute(d);
                if (route == null) {
                    route = makeWifiDisplayRoute(d, status);
                    addRouteStatic(route);
                } else {
                    boolean disconnected;
                    String address = d.getDeviceAddress();
                    if (address.equals(deviceAddress)) {
                        disconnected = DEBUG;
                    } else {
                        disconnected = address.equals(sStatic.mPreviousActiveWifiDisplayAddress);
                    }
                    updateWifiDisplayRoute(route, d, status, disconnected);
                }
                if (d.equals(activeDisplay)) {
                    selectRouteStatic(route.getSupportedTypes(), route, DEBUG);
                }
            }
        }
        int i2 = sStatic.mRoutes.size();
        while (true) {
            i = i2 - 1;
            if (i2 > 0) {
                route = (RouteInfo) sStatic.mRoutes.get(i);
                if (route.mDeviceAddress != null) {
                    d = findWifiDisplay(displays, route.mDeviceAddress);
                    if (d == null || !shouldShowWifiDisplay(d, activeDisplay)) {
                        removeRouteStatic(route);
                    }
                }
                i2 = i;
            } else {
                sStatic.mPreviousActiveWifiDisplayAddress = deviceAddress;
                return;
            }
        }
    }

    private static boolean shouldShowWifiDisplay(WifiDisplay d, WifiDisplay activeDisplay) {
        return !d.isRemembered() ? d.equals(activeDisplay) : true;
    }

    static int getWifiDisplayStatusCode(WifiDisplay d, WifiDisplayStatus wfdStatus) {
        int newStatus = wfdStatus.getScanState() == ROUTE_TYPE_LIVE_AUDIO ? ROUTE_TYPE_LIVE_AUDIO : d.isAvailable() ? d.canConnect() ? 3 : 5 : ROUTE_TYPE_REMOTE_DISPLAY;
        if (!d.equals(wfdStatus.getActiveDisplay())) {
            return newStatus;
        }
        switch (wfdStatus.getActiveDisplayState()) {
            case TextToSpeech.SUCCESS /*0*/:
                Log.e(TAG, "Active display is not connected!");
                return newStatus;
            case ROUTE_TYPE_LIVE_AUDIO /*1*/:
                return ROUTE_TYPE_LIVE_VIDEO;
            case ROUTE_TYPE_LIVE_VIDEO /*2*/:
                return 6;
            default:
                return newStatus;
        }
    }

    static boolean isWifiDisplayEnabled(WifiDisplay d, WifiDisplayStatus wfdStatus) {
        if (d.isAvailable()) {
            return !d.canConnect() ? d.equals(wfdStatus.getActiveDisplay()) : true;
        } else {
            return DEBUG;
        }
    }

    static RouteInfo makeWifiDisplayRoute(WifiDisplay display, WifiDisplayStatus wfdStatus) {
        RouteInfo newRoute = new RouteInfo(sStatic.mSystemCategory);
        newRoute.mDeviceAddress = display.getDeviceAddress();
        newRoute.mSupportedTypes = 7;
        newRoute.mVolumeHandling = 0;
        newRoute.mPlaybackType = ROUTE_TYPE_LIVE_AUDIO;
        newRoute.setRealStatusCode(getWifiDisplayStatusCode(display, wfdStatus));
        newRoute.mEnabled = isWifiDisplayEnabled(display, wfdStatus);
        newRoute.mName = display.getFriendlyDisplayName();
        newRoute.mDescription = sStatic.mResources.getText(17040610);
        newRoute.updatePresentationDisplay();
        newRoute.mDeviceType = ROUTE_TYPE_LIVE_AUDIO;
        return newRoute;
    }

    private static void updateWifiDisplayRoute(RouteInfo route, WifiDisplay display, WifiDisplayStatus wfdStatus, boolean disconnected) {
        boolean changed = DEBUG;
        String newName = display.getFriendlyDisplayName();
        if (!route.getName().equals(newName)) {
            route.mName = newName;
            changed = true;
        }
        boolean enabled = isWifiDisplayEnabled(display, wfdStatus);
        changed |= route.mEnabled != enabled ? ROUTE_TYPE_LIVE_AUDIO : 0;
        route.mEnabled = enabled;
        if (changed | route.setRealStatusCode(getWifiDisplayStatusCode(display, wfdStatus))) {
            dispatchRouteChanged(route);
        }
        if ((!enabled || disconnected) && route.isSelected()) {
            selectDefaultRouteStatic();
        }
    }

    private static WifiDisplay findWifiDisplay(WifiDisplay[] displays, String deviceAddress) {
        for (int i = 0; i < displays.length; i += ROUTE_TYPE_LIVE_AUDIO) {
            WifiDisplay d = displays[i];
            if (d.getDeviceAddress().equals(deviceAddress)) {
                return d;
            }
        }
        return null;
    }

    private static RouteInfo findWifiDisplayRoute(WifiDisplay d) {
        int count = sStatic.mRoutes.size();
        for (int i = 0; i < count; i += ROUTE_TYPE_LIVE_AUDIO) {
            RouteInfo info = (RouteInfo) sStatic.mRoutes.get(i);
            if (d.getDeviceAddress().equals(info.mDeviceAddress)) {
                return info;
            }
        }
        return null;
    }
}
