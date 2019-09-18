package com.android.server.media;

import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioRoutesInfo;
import android.media.IAudioRoutesObserver;
import android.media.IAudioService;
import android.media.IMediaRouterClient;
import android.media.IMediaRouterService;
import android.media.MediaRouterClientState;
import android.media.RemoteDisplayState;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.IntArray;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.Watchdog;
import com.android.server.media.AudioPlayerStateMonitor;
import com.android.server.media.RemoteDisplayProviderProxy;
import com.android.server.media.RemoteDisplayProviderWatcher;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class MediaRouterService extends IMediaRouterService.Stub implements Watchdog.Monitor {
    static final long CONNECTED_TIMEOUT = 60000;
    static final long CONNECTING_TIMEOUT = 5000;
    private static final boolean DEBUG = true;
    private static final String TAG = "MediaRouterService";
    static final long WAIT_MS = 500;
    BluetoothDevice mActiveBluetoothDevice;
    /* access modifiers changed from: private */
    public final IntArray mActivePlayerMinPriorityQueue = new IntArray();
    /* access modifiers changed from: private */
    public final IntArray mActivePlayerUidMinPriorityQueue = new IntArray();
    private final ArrayMap<IBinder, ClientRecord> mAllClientRecords = new ArrayMap<>();
    private final AudioPlayerStateMonitor mAudioPlayerStateMonitor;
    int mAudioRouteMainType = 0;
    private final IAudioService mAudioService;
    private ArrayList<BluetoothDevice> mConnectedBTDevicesList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCurrentUserId = -1;
    boolean mGlobalBluetoothA2dpOn = false;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final BroadcastReceiver mReceiver = new MediaRouterServiceBroadcastReceiver();
    private final SparseArray<UserRecord> mUserRecords = new SparseArray<>();

    final class ClientRecord implements IBinder.DeathRecipient {
        public boolean mActiveScan;
        public final IMediaRouterClient mClient;
        public final String mPackageName;
        public final int mPid;
        public int mRouteTypes;
        public String mSelectedRouteId;
        public final boolean mTrusted;
        public final int mUid;
        public final UserRecord mUserRecord;

        public ClientRecord(UserRecord userRecord, IMediaRouterClient client, int uid, int pid, String packageName, boolean trusted) {
            this.mUserRecord = userRecord;
            this.mClient = client;
            this.mUid = uid;
            this.mPid = pid;
            this.mPackageName = packageName;
            this.mTrusted = trusted;
        }

        public void dispose() {
            this.mClient.asBinder().unlinkToDeath(this, 0);
        }

        public void binderDied() {
            MediaRouterService.this.clientDied(this);
        }

        /* access modifiers changed from: package-private */
        public MediaRouterClientState getState() {
            if (this.mTrusted) {
                return this.mUserRecord.mRouterState;
            }
            return null;
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + this);
            String indent = prefix + "  ";
            pw.println(indent + "mTrusted=" + this.mTrusted);
            pw.println(indent + "mRouteTypes=0x" + Integer.toHexString(this.mRouteTypes));
            pw.println(indent + "mActiveScan=" + this.mActiveScan);
            pw.println(indent + "mSelectedRouteId=" + this.mSelectedRouteId);
        }

        public String toString() {
            return "Client " + this.mPackageName + " (pid " + this.mPid + ")";
        }
    }

    final class MediaRouterServiceBroadcastReceiver extends BroadcastReceiver {
        MediaRouterServiceBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED")) {
                BluetoothDevice btDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                synchronized (MediaRouterService.this.mLock) {
                    MediaRouterService.this.mActiveBluetoothDevice = btDevice;
                    MediaRouterService.this.mGlobalBluetoothA2dpOn = btDevice != null;
                }
            }
        }
    }

    static final class UserHandler extends Handler implements RemoteDisplayProviderWatcher.Callback, RemoteDisplayProviderProxy.Callback {
        private static final int MSG_CONNECTION_TIMED_OUT = 9;
        public static final int MSG_REQUEST_SET_VOLUME = 6;
        public static final int MSG_REQUEST_UPDATE_VOLUME = 7;
        public static final int MSG_SELECT_ROUTE = 4;
        public static final int MSG_START = 1;
        public static final int MSG_STOP = 2;
        public static final int MSG_UNSELECT_ROUTE = 5;
        private static final int MSG_UPDATE_CLIENT_STATE = 8;
        public static final int MSG_UPDATE_DISCOVERY_REQUEST = 3;
        private static final int PHASE_CONNECTED = 2;
        private static final int PHASE_CONNECTING = 1;
        private static final int PHASE_NOT_AVAILABLE = -1;
        private static final int PHASE_NOT_CONNECTED = 0;
        private static final int TIMEOUT_REASON_CONNECTION_LOST = 2;
        private static final int TIMEOUT_REASON_NOT_AVAILABLE = 1;
        private static final int TIMEOUT_REASON_WAITING_FOR_CONNECTED = 4;
        private static final int TIMEOUT_REASON_WAITING_FOR_CONNECTING = 3;
        private boolean mClientStateUpdateScheduled;
        private int mConnectionPhase = -1;
        private int mConnectionTimeoutReason;
        private long mConnectionTimeoutStartTime;
        private int mDiscoveryMode = 0;
        private final ArrayList<ProviderRecord> mProviderRecords = new ArrayList<>();
        private boolean mRunning;
        private RouteRecord mSelectedRouteRecord;
        private final MediaRouterService mService;
        private final ArrayList<IMediaRouterClient> mTempClients = new ArrayList<>();
        private final UserRecord mUserRecord;
        private final RemoteDisplayProviderWatcher mWatcher;

        static final class ProviderRecord {
            private RemoteDisplayState mDescriptor;
            private final RemoteDisplayProviderProxy mProvider;
            private final ArrayList<RouteRecord> mRoutes = new ArrayList<>();
            private final String mUniquePrefix;

            public ProviderRecord(RemoteDisplayProviderProxy provider) {
                this.mProvider = provider;
                this.mUniquePrefix = provider.getFlattenedComponentName() + ":";
            }

            public RemoteDisplayProviderProxy getProvider() {
                return this.mProvider;
            }

            public String getUniquePrefix() {
                return this.mUniquePrefix;
            }

            public boolean updateDescriptor(RemoteDisplayState descriptor) {
                boolean changed = false;
                if (this.mDescriptor != descriptor) {
                    this.mDescriptor = descriptor;
                    int targetIndex = 0;
                    if (descriptor != null) {
                        if (descriptor.isValid()) {
                            List<RemoteDisplayState.RemoteDisplayInfo> routeDescriptors = descriptor.displays;
                            int routeCount = routeDescriptors.size();
                            for (int i = 0; i < routeCount; i++) {
                                RemoteDisplayState.RemoteDisplayInfo routeDescriptor = routeDescriptors.get(i);
                                String descriptorId = routeDescriptor.id;
                                int sourceIndex = findRouteByDescriptorId(descriptorId);
                                if (sourceIndex < 0) {
                                    RouteRecord route = new RouteRecord(this, descriptorId, assignRouteUniqueId(descriptorId));
                                    this.mRoutes.add(targetIndex, route);
                                    route.updateDescriptor(routeDescriptor);
                                    changed = true;
                                    targetIndex++;
                                } else if (sourceIndex < targetIndex) {
                                    Slog.w(MediaRouterService.TAG, "Ignoring route descriptor with duplicate id: " + routeDescriptor);
                                } else {
                                    Collections.swap(this.mRoutes, sourceIndex, targetIndex);
                                    changed |= this.mRoutes.get(sourceIndex).updateDescriptor(routeDescriptor);
                                    targetIndex++;
                                }
                            }
                        } else {
                            Slog.w(MediaRouterService.TAG, "Ignoring invalid descriptor from media route provider: " + this.mProvider.getFlattenedComponentName());
                        }
                    }
                    for (int i2 = this.mRoutes.size() - 1; i2 >= targetIndex; i2--) {
                        this.mRoutes.remove(i2).updateDescriptor(null);
                        changed = true;
                    }
                }
                return changed;
            }

            public void appendClientState(MediaRouterClientState state) {
                int routeCount = this.mRoutes.size();
                for (int i = 0; i < routeCount; i++) {
                    state.routes.add(this.mRoutes.get(i).getInfo());
                }
            }

            public RouteRecord findRouteByUniqueId(String uniqueId) {
                int routeCount = this.mRoutes.size();
                for (int i = 0; i < routeCount; i++) {
                    RouteRecord route = this.mRoutes.get(i);
                    if (route.getUniqueId().equals(uniqueId)) {
                        return route;
                    }
                }
                return null;
            }

            private int findRouteByDescriptorId(String descriptorId) {
                int routeCount = this.mRoutes.size();
                for (int i = 0; i < routeCount; i++) {
                    if (this.mRoutes.get(i).getDescriptorId().equals(descriptorId)) {
                        return i;
                    }
                }
                return -1;
            }

            public void dump(PrintWriter pw, String prefix) {
                pw.println(prefix + this);
                String indent = prefix + "  ";
                this.mProvider.dump(pw, indent);
                int routeCount = this.mRoutes.size();
                if (routeCount != 0) {
                    for (int i = 0; i < routeCount; i++) {
                        this.mRoutes.get(i).dump(pw, indent);
                    }
                    return;
                }
                pw.println(indent + "<no routes>");
            }

            public String toString() {
                return "Provider " + this.mProvider.getFlattenedComponentName();
            }

            private String assignRouteUniqueId(String descriptorId) {
                return this.mUniquePrefix + descriptorId;
            }
        }

        static final class RouteRecord {
            private RemoteDisplayState.RemoteDisplayInfo mDescriptor;
            private final String mDescriptorId;
            private MediaRouterClientState.RouteInfo mImmutableInfo;
            private final MediaRouterClientState.RouteInfo mMutableInfo;
            private final ProviderRecord mProviderRecord;

            public RouteRecord(ProviderRecord providerRecord, String descriptorId, String uniqueId) {
                this.mProviderRecord = providerRecord;
                this.mDescriptorId = descriptorId;
                this.mMutableInfo = new MediaRouterClientState.RouteInfo(uniqueId);
            }

            public RemoteDisplayProviderProxy getProvider() {
                return this.mProviderRecord.getProvider();
            }

            public ProviderRecord getProviderRecord() {
                return this.mProviderRecord;
            }

            public String getDescriptorId() {
                return this.mDescriptorId;
            }

            public String getUniqueId() {
                return this.mMutableInfo.id;
            }

            public MediaRouterClientState.RouteInfo getInfo() {
                if (this.mImmutableInfo == null) {
                    this.mImmutableInfo = new MediaRouterClientState.RouteInfo(this.mMutableInfo);
                }
                return this.mImmutableInfo;
            }

            public boolean isValid() {
                return this.mDescriptor != null;
            }

            public boolean isEnabled() {
                return this.mMutableInfo.enabled;
            }

            public int getStatus() {
                return this.mMutableInfo.statusCode;
            }

            public boolean updateDescriptor(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                boolean changed = false;
                if (this.mDescriptor != descriptor) {
                    this.mDescriptor = descriptor;
                    if (descriptor != null) {
                        String name = computeName(descriptor);
                        if (!Objects.equals(this.mMutableInfo.name, name)) {
                            this.mMutableInfo.name = name;
                            changed = true;
                        }
                        String description = computeDescription(descriptor);
                        if (!Objects.equals(this.mMutableInfo.description, description)) {
                            this.mMutableInfo.description = description;
                            changed = true;
                        }
                        int supportedTypes = computeSupportedTypes(descriptor);
                        if (this.mMutableInfo.supportedTypes != supportedTypes) {
                            this.mMutableInfo.supportedTypes = supportedTypes;
                            changed = true;
                        }
                        boolean enabled = computeEnabled(descriptor);
                        if (this.mMutableInfo.enabled != enabled) {
                            this.mMutableInfo.enabled = enabled;
                            changed = true;
                        }
                        int statusCode = computeStatusCode(descriptor);
                        if (this.mMutableInfo.statusCode != statusCode) {
                            this.mMutableInfo.statusCode = statusCode;
                            changed = true;
                        }
                        int playbackType = computePlaybackType(descriptor);
                        if (this.mMutableInfo.playbackType != playbackType) {
                            this.mMutableInfo.playbackType = playbackType;
                            changed = true;
                        }
                        int playbackStream = computePlaybackStream(descriptor);
                        if (this.mMutableInfo.playbackStream != playbackStream) {
                            this.mMutableInfo.playbackStream = playbackStream;
                            changed = true;
                        }
                        int volume = computeVolume(descriptor);
                        if (this.mMutableInfo.volume != volume) {
                            this.mMutableInfo.volume = volume;
                            changed = true;
                        }
                        int volumeMax = computeVolumeMax(descriptor);
                        if (this.mMutableInfo.volumeMax != volumeMax) {
                            this.mMutableInfo.volumeMax = volumeMax;
                            changed = true;
                        }
                        int volumeHandling = computeVolumeHandling(descriptor);
                        if (this.mMutableInfo.volumeHandling != volumeHandling) {
                            this.mMutableInfo.volumeHandling = volumeHandling;
                            changed = true;
                        }
                        int presentationDisplayId = computePresentationDisplayId(descriptor);
                        if (this.mMutableInfo.presentationDisplayId != presentationDisplayId) {
                            this.mMutableInfo.presentationDisplayId = presentationDisplayId;
                            changed = true;
                        }
                    }
                }
                if (changed) {
                    this.mImmutableInfo = null;
                }
                return changed;
            }

            public void dump(PrintWriter pw, String prefix) {
                pw.println(prefix + this);
                String indent = prefix + "  ";
                pw.println(indent + "mMutableInfo=" + this.mMutableInfo);
                pw.println(indent + "mDescriptorId=" + this.mDescriptorId);
                pw.println(indent + "mDescriptor=" + this.mDescriptor);
            }

            public String toString() {
                return "Route " + this.mMutableInfo.name + " (" + this.mMutableInfo.id + ")";
            }

            private static String computeName(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                return descriptor.name;
            }

            private static String computeDescription(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                String description = descriptor.description;
                if (TextUtils.isEmpty(description)) {
                    return null;
                }
                return description;
            }

            private static int computeSupportedTypes(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                return 7;
            }

            private static boolean computeEnabled(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                switch (descriptor.status) {
                    case 2:
                    case 3:
                    case 4:
                        return true;
                    default:
                        return false;
                }
            }

            private static int computeStatusCode(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                switch (descriptor.status) {
                    case 0:
                        return 4;
                    case 1:
                        return 5;
                    case 2:
                        return 3;
                    case 3:
                        return 2;
                    case 4:
                        return 6;
                    default:
                        return 0;
                }
            }

            private static int computePlaybackType(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                return 1;
            }

            private static int computePlaybackStream(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                return 3;
            }

            private static int computeVolume(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                int volume = descriptor.volume;
                int volumeMax = descriptor.volumeMax;
                if (volume < 0) {
                    return 0;
                }
                if (volume > volumeMax) {
                    return volumeMax;
                }
                return volume;
            }

            private static int computeVolumeMax(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                int volumeMax = descriptor.volumeMax;
                if (volumeMax > 0) {
                    return volumeMax;
                }
                return 0;
            }

            private static int computeVolumeHandling(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                if (descriptor.volumeHandling != 1) {
                    return 0;
                }
                return 1;
            }

            private static int computePresentationDisplayId(RemoteDisplayState.RemoteDisplayInfo descriptor) {
                int displayId = descriptor.presentationDisplayId;
                if (displayId < 0) {
                    return -1;
                }
                return displayId;
            }
        }

        public UserHandler(MediaRouterService service, UserRecord userRecord) {
            super(Looper.getMainLooper(), null, true);
            this.mService = service;
            this.mUserRecord = userRecord;
            this.mWatcher = new RemoteDisplayProviderWatcher(service.mContext, this, this, this.mUserRecord.mUserId);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    start();
                    return;
                case 2:
                    stop();
                    return;
                case 3:
                    updateDiscoveryRequest();
                    return;
                case 4:
                    selectRoute((String) msg.obj);
                    return;
                case 5:
                    unselectRoute((String) msg.obj);
                    return;
                case 6:
                    requestSetVolume((String) msg.obj, msg.arg1);
                    return;
                case 7:
                    requestUpdateVolume((String) msg.obj, msg.arg1);
                    return;
                case 8:
                    updateClientState();
                    return;
                case 9:
                    connectionTimedOut();
                    return;
                default:
                    return;
            }
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.println(prefix + "Handler");
            pw.println(indent + "mRunning=" + this.mRunning);
            pw.println(indent + "mDiscoveryMode=" + this.mDiscoveryMode);
            pw.println(indent + "mSelectedRouteRecord=" + this.mSelectedRouteRecord);
            pw.println(indent + "mConnectionPhase=" + this.mConnectionPhase);
            pw.println(indent + "mConnectionTimeoutReason=" + this.mConnectionTimeoutReason);
            StringBuilder sb = new StringBuilder();
            sb.append(prefix + "  ");
            sb.append("mConnectionTimeoutStartTime=");
            sb.append(this.mConnectionTimeoutReason != 0 ? TimeUtils.formatUptime(this.mConnectionTimeoutStartTime) : "<n/a>");
            pw.println(sb.toString());
            this.mWatcher.dump(pw, prefix);
            int providerCount = this.mProviderRecords.size();
            if (providerCount != 0) {
                for (int i = 0; i < providerCount; i++) {
                    this.mProviderRecords.get(i).dump(pw, prefix);
                }
                return;
            }
            pw.println(indent + "<no providers>");
        }

        private void start() {
            if (!this.mRunning) {
                this.mRunning = true;
                this.mWatcher.start();
            }
        }

        private void stop() {
            if (this.mRunning) {
                this.mRunning = false;
                unselectSelectedRoute();
                this.mWatcher.stop();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x002d, code lost:
            if ((r1 & 4) == 0) goto L_0x0035;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002f, code lost:
            if (r5 == false) goto L_0x0033;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0031, code lost:
            r0 = 2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0033, code lost:
            r0 = 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0035, code lost:
            r0 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0038, code lost:
            if (r8.mDiscoveryMode == r0) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
            r8.mDiscoveryMode = r0;
            r2 = r8.mProviderRecords.size();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0043, code lost:
            r3 = r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0044, code lost:
            if (r3 >= r2) goto L_0x005a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0046, code lost:
            r8.mProviderRecords.get(r3).getProvider().setDiscoveryMode(r8.mDiscoveryMode);
            r4 = r3 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
            return;
         */
        private void updateDiscoveryRequest() {
            synchronized (this.mService.mLock) {
                try {
                    int count = this.mUserRecord.mClientRecords.size();
                    int i = 0;
                    boolean activeScan = false;
                    int routeTypes = 0;
                    int i2 = 0;
                    while (i2 < count) {
                        try {
                            ClientRecord clientRecord = this.mUserRecord.mClientRecords.get(i2);
                            routeTypes |= clientRecord.mRouteTypes;
                            activeScan |= clientRecord.mActiveScan;
                            i2++;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }

        private void selectRoute(String routeId) {
            if (routeId == null) {
                return;
            }
            if (this.mSelectedRouteRecord == null || !routeId.equals(this.mSelectedRouteRecord.getUniqueId())) {
                RouteRecord routeRecord = findRouteRecord(routeId);
                if (routeRecord != null) {
                    unselectSelectedRoute();
                    Slog.i(MediaRouterService.TAG, "Selected route:" + routeRecord);
                    this.mSelectedRouteRecord = routeRecord;
                    checkSelectedRouteState();
                    routeRecord.getProvider().setSelectedDisplay(routeRecord.getDescriptorId());
                    scheduleUpdateClientState();
                }
            }
        }

        private void unselectRoute(String routeId) {
            if (routeId != null && this.mSelectedRouteRecord != null && routeId.equals(this.mSelectedRouteRecord.getUniqueId())) {
                unselectSelectedRoute();
            }
        }

        private void unselectSelectedRoute() {
            if (this.mSelectedRouteRecord != null) {
                Slog.i(MediaRouterService.TAG, "Unselected route:" + this.mSelectedRouteRecord);
                this.mSelectedRouteRecord.getProvider().setSelectedDisplay(null);
                this.mSelectedRouteRecord = null;
                checkSelectedRouteState();
                scheduleUpdateClientState();
            }
        }

        private void requestSetVolume(String routeId, int volume) {
            if (this.mSelectedRouteRecord != null && routeId.equals(this.mSelectedRouteRecord.getUniqueId())) {
                this.mSelectedRouteRecord.getProvider().setDisplayVolume(volume);
            }
        }

        private void requestUpdateVolume(String routeId, int direction) {
            if (this.mSelectedRouteRecord != null && routeId.equals(this.mSelectedRouteRecord.getUniqueId())) {
                this.mSelectedRouteRecord.getProvider().adjustDisplayVolume(direction);
            }
        }

        public void addProvider(RemoteDisplayProviderProxy provider) {
            provider.setCallback(this);
            provider.setDiscoveryMode(this.mDiscoveryMode);
            provider.setSelectedDisplay(null);
            ProviderRecord providerRecord = new ProviderRecord(provider);
            this.mProviderRecords.add(providerRecord);
            providerRecord.updateDescriptor(provider.getDisplayState());
            scheduleUpdateClientState();
        }

        public void removeProvider(RemoteDisplayProviderProxy provider) {
            int index = findProviderRecord(provider);
            if (index >= 0) {
                this.mProviderRecords.remove(index).updateDescriptor(null);
                provider.setCallback(null);
                provider.setDiscoveryMode(0);
                checkSelectedRouteState();
                scheduleUpdateClientState();
            }
        }

        public void onDisplayStateChanged(RemoteDisplayProviderProxy provider, RemoteDisplayState state) {
            updateProvider(provider, state);
        }

        private void updateProvider(RemoteDisplayProviderProxy provider, RemoteDisplayState state) {
            int index = findProviderRecord(provider);
            if (index >= 0 && this.mProviderRecords.get(index).updateDescriptor(state)) {
                checkSelectedRouteState();
                scheduleUpdateClientState();
            }
        }

        private void checkSelectedRouteState() {
            if (this.mSelectedRouteRecord == null) {
                this.mConnectionPhase = -1;
                updateConnectionTimeout(0);
            } else if (!this.mSelectedRouteRecord.isValid() || !this.mSelectedRouteRecord.isEnabled()) {
                updateConnectionTimeout(1);
            } else {
                int oldPhase = this.mConnectionPhase;
                this.mConnectionPhase = getConnectionPhase(this.mSelectedRouteRecord.getStatus());
                if (oldPhase < 1 || this.mConnectionPhase >= 1) {
                    switch (this.mConnectionPhase) {
                        case 0:
                            updateConnectionTimeout(3);
                            break;
                        case 1:
                            if (oldPhase != 1) {
                                Slog.i(MediaRouterService.TAG, "Connecting to route: " + this.mSelectedRouteRecord);
                            }
                            updateConnectionTimeout(4);
                            break;
                        case 2:
                            if (oldPhase != 2) {
                                Slog.i(MediaRouterService.TAG, "Connected to route: " + this.mSelectedRouteRecord);
                            }
                            updateConnectionTimeout(0);
                            break;
                        default:
                            updateConnectionTimeout(1);
                            break;
                    }
                    return;
                }
                updateConnectionTimeout(2);
            }
        }

        private void updateConnectionTimeout(int reason) {
            if (reason != this.mConnectionTimeoutReason) {
                if (this.mConnectionTimeoutReason != 0) {
                    removeMessages(9);
                }
                this.mConnectionTimeoutReason = reason;
                this.mConnectionTimeoutStartTime = SystemClock.uptimeMillis();
                switch (reason) {
                    case 1:
                    case 2:
                        sendEmptyMessage(9);
                        return;
                    case 3:
                        sendEmptyMessageDelayed(9, MediaRouterService.CONNECTING_TIMEOUT);
                        return;
                    case 4:
                        sendEmptyMessageDelayed(9, 60000);
                        return;
                    default:
                        return;
                }
            }
        }

        private void connectionTimedOut() {
            if (this.mConnectionTimeoutReason == 0 || this.mSelectedRouteRecord == null) {
                Log.wtf(MediaRouterService.TAG, "Handled connection timeout for no reason.");
                return;
            }
            switch (this.mConnectionTimeoutReason) {
                case 1:
                    Slog.i(MediaRouterService.TAG, "Selected route no longer available: " + this.mSelectedRouteRecord);
                    break;
                case 2:
                    Slog.i(MediaRouterService.TAG, "Selected route connection lost: " + this.mSelectedRouteRecord);
                    break;
                case 3:
                    Slog.i(MediaRouterService.TAG, "Selected route timed out while waiting for connection attempt to begin after " + (SystemClock.uptimeMillis() - this.mConnectionTimeoutStartTime) + " ms: " + this.mSelectedRouteRecord);
                    break;
                case 4:
                    Slog.i(MediaRouterService.TAG, "Selected route timed out while connecting after " + (SystemClock.uptimeMillis() - this.mConnectionTimeoutStartTime) + " ms: " + this.mSelectedRouteRecord);
                    break;
            }
            this.mConnectionTimeoutReason = 0;
            unselectSelectedRoute();
        }

        private void scheduleUpdateClientState() {
            if (!this.mClientStateUpdateScheduled) {
                this.mClientStateUpdateScheduled = true;
                sendEmptyMessage(8);
            }
        }

        private void updateClientState() {
            this.mClientStateUpdateScheduled = false;
            MediaRouterClientState routerState = new MediaRouterClientState();
            int providerCount = this.mProviderRecords.size();
            for (int i = 0; i < providerCount; i++) {
                this.mProviderRecords.get(i).appendClientState(routerState);
            }
            try {
                synchronized (this.mService.mLock) {
                    this.mUserRecord.mRouterState = routerState;
                    int count = this.mUserRecord.mClientRecords.size();
                    for (int i2 = 0; i2 < count; i2++) {
                        this.mTempClients.add(this.mUserRecord.mClientRecords.get(i2).mClient);
                    }
                }
                int count2 = this.mTempClients.size();
                for (int i3 = 0; i3 < count2; i3++) {
                    this.mTempClients.get(i3).onStateChanged();
                }
                this.mTempClients.clear();
            } catch (RemoteException e) {
                Slog.w(MediaRouterService.TAG, "Failed to call onStateChanged. Client probably died.");
            } catch (Throwable th) {
                this.mTempClients.clear();
                throw th;
            }
        }

        private int findProviderRecord(RemoteDisplayProviderProxy provider) {
            int count = this.mProviderRecords.size();
            for (int i = 0; i < count; i++) {
                if (this.mProviderRecords.get(i).getProvider() == provider) {
                    return i;
                }
            }
            return -1;
        }

        private RouteRecord findRouteRecord(String uniqueId) {
            int count = this.mProviderRecords.size();
            for (int i = 0; i < count; i++) {
                RouteRecord record = this.mProviderRecords.get(i).findRouteByUniqueId(uniqueId);
                if (record != null) {
                    return record;
                }
            }
            return null;
        }

        private static int getConnectionPhase(int status) {
            if (status != 6) {
                switch (status) {
                    case 0:
                        break;
                    case 1:
                    case 3:
                        return 0;
                    case 2:
                        return 1;
                    default:
                        return -1;
                }
            }
            return 2;
        }
    }

    final class UserRecord {
        public final ArrayList<ClientRecord> mClientRecords = new ArrayList<>();
        public final UserHandler mHandler;
        public MediaRouterClientState mRouterState;
        public final int mUserId;

        public UserRecord(int userId) {
            this.mUserId = userId;
            this.mHandler = new UserHandler(MediaRouterService.this, this);
        }

        public void dump(final PrintWriter pw, String prefix) {
            pw.println(prefix + this);
            final String indent = prefix + "  ";
            int clientCount = this.mClientRecords.size();
            if (clientCount != 0) {
                for (int i = 0; i < clientCount; i++) {
                    this.mClientRecords.get(i).dump(pw, indent);
                }
            } else {
                pw.println(indent + "<no clients>");
            }
            pw.println(indent + "State");
            pw.println(indent + "mRouterState=" + this.mRouterState);
            if (!this.mHandler.runWithScissors(new Runnable() {
                public void run() {
                    UserRecord.this.mHandler.dump(pw, indent);
                }
            }, 1000)) {
                pw.println(indent + "<could not dump handler state>");
            }
        }

        public String toString() {
            return "User " + this.mUserId;
        }
    }

    public MediaRouterService(Context context) {
        this.mContext = context;
        Watchdog.getInstance().addMonitor(this);
        this.mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
        this.mAudioPlayerStateMonitor = AudioPlayerStateMonitor.getInstance();
        this.mAudioPlayerStateMonitor.registerListener(new AudioPlayerStateMonitor.OnAudioPlayerActiveStateChangedListener() {
            final Runnable mRestoreBluetoothA2dpRunnable = new Runnable() {
                public void run() {
                    MediaRouterService.this.restoreBluetoothA2dp();
                }
            };

            public void onAudioPlayerActiveStateChanged(AudioPlaybackConfiguration config, boolean isRemoved) {
                boolean active = !isRemoved && config.isActive();
                int pii = config.getPlayerInterfaceId();
                int uid = config.getClientUid();
                int idx = MediaRouterService.this.mActivePlayerMinPriorityQueue.indexOf(pii);
                if (idx >= 0) {
                    MediaRouterService.this.mActivePlayerMinPriorityQueue.remove(idx);
                    MediaRouterService.this.mActivePlayerUidMinPriorityQueue.remove(idx);
                }
                int restoreUid = -1;
                if (active) {
                    MediaRouterService.this.mActivePlayerMinPriorityQueue.add(config.getPlayerInterfaceId());
                    MediaRouterService.this.mActivePlayerUidMinPriorityQueue.add(uid);
                    restoreUid = uid;
                } else if (MediaRouterService.this.mActivePlayerUidMinPriorityQueue.size() > 0) {
                    restoreUid = MediaRouterService.this.mActivePlayerUidMinPriorityQueue.get(MediaRouterService.this.mActivePlayerUidMinPriorityQueue.size() - 1);
                }
                MediaRouterService.this.mHandler.removeCallbacks(this.mRestoreBluetoothA2dpRunnable);
                if (restoreUid >= 0) {
                    MediaRouterService.this.restoreRoute(restoreUid);
                    Slog.d(MediaRouterService.TAG, "onAudioPlayerActiveStateChanged: uid=" + uid + ", active=" + active + ", restoreUid=" + restoreUid);
                    return;
                }
                MediaRouterService.this.mHandler.postDelayed(this.mRestoreBluetoothA2dpRunnable, 500);
                Slog.d(MediaRouterService.TAG, "onAudioPlayerActiveStateChanged: uid=" + uid + ", active=" + active + ", delaying");
            }
        }, this.mHandler);
        this.mAudioPlayerStateMonitor.registerSelfIntoAudioServiceIfNeeded(this.mAudioService);
        try {
            AudioRoutesInfo audioRoutes = this.mAudioService.startWatchingRoutes(new IAudioRoutesObserver.Stub() {
                public void dispatchAudioRoutesChanged(AudioRoutesInfo newRoutes) {
                    synchronized (MediaRouterService.this.mLock) {
                        if (newRoutes.mainType != MediaRouterService.this.mAudioRouteMainType) {
                            boolean z = false;
                            if ((newRoutes.mainType & 19) != 0) {
                                if (HwFrameworkFactory.getVRSystemServiceManager().isVRDeviceConnected()) {
                                    if (newRoutes.mainType != 16) {
                                        if ((newRoutes.mainType & 8) != 0) {
                                        }
                                    }
                                }
                                MediaRouterService.this.mGlobalBluetoothA2dpOn = false;
                                Slog.w(MediaRouterService.TAG, "headset was plugged in:" + MediaRouterService.this.mGlobalBluetoothA2dpOn);
                                MediaRouterService.this.mAudioRouteMainType = newRoutes.mainType;
                                MediaRouterService.this.restoreBluetoothA2dp();
                            }
                            MediaRouterService mediaRouterService = MediaRouterService.this;
                            if (newRoutes.bluetoothName == null) {
                                if (MediaRouterService.this.mActiveBluetoothDevice == null) {
                                    mediaRouterService.mGlobalBluetoothA2dpOn = z;
                                    Slog.w(MediaRouterService.TAG, "headset was plugged out:" + MediaRouterService.this.mGlobalBluetoothA2dpOn);
                                    MediaRouterService.this.mAudioRouteMainType = newRoutes.mainType;
                                    MediaRouterService.this.restoreBluetoothA2dp();
                                }
                            }
                            z = true;
                            mediaRouterService.mGlobalBluetoothA2dpOn = z;
                            Slog.w(MediaRouterService.TAG, "headset was plugged out:" + MediaRouterService.this.mGlobalBluetoothA2dpOn);
                            MediaRouterService.this.mAudioRouteMainType = newRoutes.mainType;
                            MediaRouterService.this.restoreBluetoothA2dp();
                        }
                    }
                }
            });
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException in the audio service.");
        }
        Context context2 = context;
        context2.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, new IntentFilter("android.bluetooth.a2dp.profile.action.ACTIVE_DEVICE_CHANGED"), null, null);
    }

    public void systemRunning() {
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.USER_SWITCHED")) {
                    MediaRouterService.this.switchUser();
                }
            }
        }, new IntentFilter("android.intent.action.USER_SWITCHED"));
        switchUser();
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    public void registerClientAsUser(IMediaRouterClient client, String packageName, int userId) {
        if (client != null) {
            int uid = Binder.getCallingUid();
            String str = packageName;
            if (validatePackageName(uid, str)) {
                int pid = Binder.getCallingPid();
                int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, false, true, "registerClientAsUser", str);
                boolean trusted = this.mContext.checkCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY") == 0;
                long token = Binder.clearCallingIdentity();
                try {
                    Object obj = this.mLock;
                    synchronized (obj) {
                        Object obj2 = obj;
                        registerClientLocked(client, uid, pid, str, resolvedUserId, trusted);
                        Binder.restoreCallingIdentity(token);
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } else {
                throw new SecurityException("packageName must match the calling uid");
            }
        } else {
            String str2 = packageName;
            throw new IllegalArgumentException("client must not be null");
        }
    }

    public void unregisterClient(IMediaRouterClient client) {
        if (client != null) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    unregisterClientLocked(client, false);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("client must not be null");
        }
    }

    public MediaRouterClientState getState(IMediaRouterClient client) {
        MediaRouterClientState stateLocked;
        if (client != null) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    stateLocked = getStateLocked(client);
                }
                Binder.restoreCallingIdentity(token);
                return stateLocked;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("client must not be null");
        }
    }

    public boolean isPlaybackActive(IMediaRouterClient client) {
        ClientRecord clientRecord;
        if (client != null) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    clientRecord = this.mAllClientRecords.get(client.asBinder());
                }
                if (clientRecord != null) {
                    boolean isPlaybackActive = this.mAudioPlayerStateMonitor.isPlaybackActive(clientRecord.mUid);
                    Binder.restoreCallingIdentity(token);
                    return isPlaybackActive;
                }
                Binder.restoreCallingIdentity(token);
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("client must not be null");
        }
    }

    public void setDiscoveryRequest(IMediaRouterClient client, int routeTypes, boolean activeScan) {
        if (client != null) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    setDiscoveryRequestLocked(client, routeTypes, activeScan);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("client must not be null");
        }
    }

    public void setSelectedRoute(IMediaRouterClient client, String routeId, boolean explicit) {
        if (client != null) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    setSelectedRouteLocked(client, routeId, explicit);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("client must not be null");
        }
    }

    public void requestSetVolume(IMediaRouterClient client, String routeId, int volume) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        } else if (routeId != null) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    requestSetVolumeLocked(client, routeId, volume);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("routeId must not be null");
        }
    }

    public void requestUpdateVolume(IMediaRouterClient client, String routeId, int direction) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        } else if (routeId != null) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    requestUpdateVolumeLocked(client, routeId, direction);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("routeId must not be null");
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("MEDIA ROUTER SERVICE (dumpsys media_router)");
            pw.println();
            pw.println("Global state");
            pw.println("  mCurrentUserId=" + this.mCurrentUserId);
            synchronized (this.mLock) {
                int count = this.mUserRecords.size();
                for (int i = 0; i < count; i++) {
                    pw.println();
                    this.mUserRecords.valueAt(i).dump(pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreBluetoothA2dp() {
        boolean a2dpOn;
        BluetoothDevice btDevice;
        try {
            synchronized (this.mLock) {
                a2dpOn = this.mGlobalBluetoothA2dpOn;
                btDevice = this.mActiveBluetoothDevice;
            }
            if (btDevice != null) {
                Slog.v(TAG, "restoreBluetoothA2dp(" + a2dpOn + ")");
                this.mAudioService.setBluetoothA2dpOn(a2dpOn);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "RemoteException while calling setBluetoothA2dpOn.");
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreRoute(int uid) {
        ClientRecord clientRecord = null;
        synchronized (this.mLock) {
            UserRecord userRecord = this.mUserRecords.get(UserHandle.getUserId(uid));
            if (userRecord != null && userRecord.mClientRecords != null) {
                Iterator<ClientRecord> it = userRecord.mClientRecords.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ClientRecord cr = it.next();
                    if (validatePackageName(uid, cr.mPackageName)) {
                        clientRecord = cr;
                        break;
                    }
                }
            }
        }
        if (clientRecord != null) {
            try {
                clientRecord.mClient.onRestoreRoute();
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to call onRestoreRoute. Client probably died.");
            }
        } else {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    MediaRouterService.this.restoreBluetoothA2dp();
                }
            }, 500);
        }
    }

    /* access modifiers changed from: package-private */
    public void switchUser() {
        synchronized (this.mLock) {
            int userId = ActivityManager.getCurrentUser();
            if (this.mCurrentUserId != userId) {
                int oldUserId = this.mCurrentUserId;
                this.mCurrentUserId = userId;
                UserRecord oldUser = this.mUserRecords.get(oldUserId);
                if (oldUser != null) {
                    oldUser.mHandler.sendEmptyMessage(2);
                    disposeUserIfNeededLocked(oldUser);
                }
                UserRecord newUser = this.mUserRecords.get(userId);
                if (newUser != null) {
                    newUser.mHandler.sendEmptyMessage(1);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clientDied(ClientRecord clientRecord) {
        synchronized (this.mLock) {
            unregisterClientLocked(clientRecord.mClient, true);
        }
    }

    private void registerClientLocked(IMediaRouterClient client, int uid, int pid, String packageName, int userId, boolean trusted) {
        int i = userId;
        IBinder binder = client.asBinder();
        if (this.mAllClientRecords.get(binder) == null) {
            boolean newUser = false;
            UserRecord userRecord = this.mUserRecords.get(i);
            if (userRecord == null) {
                userRecord = new UserRecord(i);
                newUser = true;
            }
            boolean newUser2 = newUser;
            UserRecord userRecord2 = userRecord;
            ClientRecord clientRecord = new ClientRecord(userRecord2, client, uid, pid, packageName, trusted);
            try {
                binder.linkToDeath(clientRecord, 0);
                if (newUser2) {
                    this.mUserRecords.put(i, userRecord2);
                    initializeUserLocked(userRecord2);
                }
                userRecord2.mClientRecords.add(clientRecord);
                this.mAllClientRecords.put(binder, clientRecord);
                initializeClientLocked(clientRecord);
                ClientRecord clientRecord2 = clientRecord;
            } catch (RemoteException ex) {
                RemoteException remoteException = ex;
                throw new RuntimeException("Media router client died prematurely.", ex);
            }
        }
    }

    private void unregisterClientLocked(IMediaRouterClient client, boolean died) {
        ClientRecord clientRecord = this.mAllClientRecords.remove(client.asBinder());
        if (clientRecord != null) {
            UserRecord userRecord = clientRecord.mUserRecord;
            userRecord.mClientRecords.remove(clientRecord);
            disposeClientLocked(clientRecord, died);
            disposeUserIfNeededLocked(userRecord);
        }
    }

    private MediaRouterClientState getStateLocked(IMediaRouterClient client) {
        ClientRecord clientRecord = this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            return clientRecord.getState();
        }
        return null;
    }

    private void setDiscoveryRequestLocked(IMediaRouterClient client, int routeTypes, boolean activeScan) {
        ClientRecord clientRecord = this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            if (!clientRecord.mTrusted) {
                routeTypes &= -5;
            }
            if (clientRecord.mRouteTypes != routeTypes || clientRecord.mActiveScan != activeScan) {
                Slog.d(TAG, clientRecord + ": Set discovery request, routeTypes=0x" + Integer.toHexString(routeTypes) + ", activeScan=" + activeScan);
                clientRecord.mRouteTypes = routeTypes;
                clientRecord.mActiveScan = activeScan;
                clientRecord.mUserRecord.mHandler.sendEmptyMessage(3);
            }
        }
    }

    private void setSelectedRouteLocked(IMediaRouterClient client, String routeId, boolean explicit) {
        ClientRecord clientRecord = this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            String oldRouteId = clientRecord.mSelectedRouteId;
            if (!Objects.equals(routeId, oldRouteId)) {
                Slog.d(TAG, clientRecord + ": Set selected route, routeId=" + routeId + ", oldRouteId=" + oldRouteId + ", explicit=" + explicit);
                clientRecord.mSelectedRouteId = routeId;
                if (explicit && clientRecord.mTrusted) {
                    if (oldRouteId != null) {
                        clientRecord.mUserRecord.mHandler.obtainMessage(5, oldRouteId).sendToTarget();
                    }
                    if (routeId != null) {
                        clientRecord.mUserRecord.mHandler.obtainMessage(4, routeId).sendToTarget();
                    }
                }
            }
        }
    }

    private void requestSetVolumeLocked(IMediaRouterClient client, String routeId, int volume) {
        ClientRecord clientRecord = this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            clientRecord.mUserRecord.mHandler.obtainMessage(6, volume, 0, routeId).sendToTarget();
        }
    }

    private void requestUpdateVolumeLocked(IMediaRouterClient client, String routeId, int direction) {
        ClientRecord clientRecord = this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            clientRecord.mUserRecord.mHandler.obtainMessage(7, direction, 0, routeId).sendToTarget();
        }
    }

    private void initializeUserLocked(UserRecord userRecord) {
        Slog.d(TAG, userRecord + ": Initialized");
        if (userRecord.mUserId == this.mCurrentUserId) {
            userRecord.mHandler.sendEmptyMessage(1);
        }
    }

    private void disposeUserIfNeededLocked(UserRecord userRecord) {
        if (userRecord.mUserId != this.mCurrentUserId && userRecord.mClientRecords.isEmpty()) {
            Slog.d(TAG, userRecord + ": Disposed");
            this.mUserRecords.remove(userRecord.mUserId);
        }
    }

    private void initializeClientLocked(ClientRecord clientRecord) {
        Slog.d(TAG, clientRecord + ": Registered");
    }

    private void disposeClientLocked(ClientRecord clientRecord, boolean died) {
        if (died) {
            Slog.d(TAG, clientRecord + ": Died!");
        } else {
            Slog.d(TAG, clientRecord + ": Unregistered");
        }
        if (clientRecord.mRouteTypes != 0 || clientRecord.mActiveScan) {
            clientRecord.mUserRecord.mHandler.sendEmptyMessage(3);
        }
        clientRecord.dispose();
    }

    private boolean validatePackageName(int uid, String packageName) {
        if (packageName != null) {
            String[] packageNames = this.mContext.getPackageManager().getPackagesForUid(uid);
            if (packageNames != null) {
                for (String n : packageNames) {
                    if (n.equals(packageName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
