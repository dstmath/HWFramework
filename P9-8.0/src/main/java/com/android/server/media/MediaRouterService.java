package com.android.server.media;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.IMediaRouterClient;
import android.media.IMediaRouterService.Stub;
import android.media.MediaRouterClientState;
import android.media.MediaRouterClientState.RouteInfo;
import android.media.RemoteDisplayState;
import android.media.RemoteDisplayState.RemoteDisplayInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.media.RemoteDisplayProviderWatcher.Callback;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MediaRouterService extends Stub implements Monitor {
    static final long CONNECTED_TIMEOUT = 60000;
    static final long CONNECTING_TIMEOUT = 5000;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "MediaRouterService";
    private final ArrayMap<IBinder, ClientRecord> mAllClientRecords = new ArrayMap();
    private final Context mContext;
    private int mCurrentUserId = -1;
    private final Object mLock = new Object();
    private final SparseArray<UserRecord> mUserRecords = new SparseArray();

    final class ClientRecord implements DeathRecipient {
        public boolean mActiveScan;
        public final IMediaRouterClient mClient;
        public final String mPackageName;
        public final int mPid;
        public int mRouteTypes;
        public String mSelectedRouteId;
        public final boolean mTrusted;
        public final UserRecord mUserRecord;

        public ClientRecord(UserRecord userRecord, IMediaRouterClient client, int pid, String packageName, boolean trusted) {
            this.mUserRecord = userRecord;
            this.mClient = client;
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

        MediaRouterClientState getState() {
            return this.mTrusted ? this.mUserRecord.mTrustedState : this.mUserRecord.mUntrustedState;
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

    static final class UserHandler extends Handler implements Callback, RemoteDisplayProviderProxy.Callback {
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
        private RouteRecord mGloballySelectedRouteRecord;
        private final ArrayList<ProviderRecord> mProviderRecords = new ArrayList();
        private boolean mRunning;
        private final MediaRouterService mService;
        private final ArrayList<IMediaRouterClient> mTempClients = new ArrayList();
        private final UserRecord mUserRecord;
        private final RemoteDisplayProviderWatcher mWatcher;

        static final class ProviderRecord {
            private RemoteDisplayState mDescriptor;
            private final RemoteDisplayProviderProxy mProvider;
            private final ArrayList<RouteRecord> mRoutes = new ArrayList();
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
                    int i;
                    this.mDescriptor = descriptor;
                    int targetIndex = 0;
                    if (descriptor != null) {
                        if (descriptor.isValid()) {
                            List<RemoteDisplayInfo> routeDescriptors = descriptor.displays;
                            int routeCount = routeDescriptors.size();
                            i = 0;
                            int targetIndex2 = 0;
                            while (i < routeCount) {
                                RemoteDisplayInfo routeDescriptor = (RemoteDisplayInfo) routeDescriptors.get(i);
                                String descriptorId = routeDescriptor.id;
                                int sourceIndex = findRouteByDescriptorId(descriptorId);
                                RouteRecord route;
                                if (sourceIndex < 0) {
                                    route = new RouteRecord(this, descriptorId, assignRouteUniqueId(descriptorId));
                                    targetIndex = targetIndex2 + 1;
                                    this.mRoutes.add(targetIndex2, route);
                                    route.updateDescriptor(routeDescriptor);
                                    changed = true;
                                } else if (sourceIndex < targetIndex2) {
                                    Slog.w(MediaRouterService.TAG, "Ignoring route descriptor with duplicate id: " + routeDescriptor);
                                    targetIndex = targetIndex2;
                                } else {
                                    route = (RouteRecord) this.mRoutes.get(sourceIndex);
                                    targetIndex = targetIndex2 + 1;
                                    Collections.swap(this.mRoutes, sourceIndex, targetIndex2);
                                    changed |= route.updateDescriptor(routeDescriptor);
                                }
                                i++;
                                targetIndex2 = targetIndex;
                            }
                            targetIndex = targetIndex2;
                        } else {
                            Slog.w(MediaRouterService.TAG, "Ignoring invalid descriptor from media route provider: " + this.mProvider.getFlattenedComponentName());
                        }
                    }
                    for (i = this.mRoutes.size() - 1; i >= targetIndex; i--) {
                        ((RouteRecord) this.mRoutes.remove(i)).updateDescriptor(null);
                        changed = true;
                    }
                }
                return changed;
            }

            public void appendClientState(MediaRouterClientState state) {
                int routeCount = this.mRoutes.size();
                for (int i = 0; i < routeCount; i++) {
                    state.routes.add(((RouteRecord) this.mRoutes.get(i)).getInfo());
                }
            }

            public RouteRecord findRouteByUniqueId(String uniqueId) {
                int routeCount = this.mRoutes.size();
                for (int i = 0; i < routeCount; i++) {
                    RouteRecord route = (RouteRecord) this.mRoutes.get(i);
                    if (route.getUniqueId().equals(uniqueId)) {
                        return route;
                    }
                }
                return null;
            }

            private int findRouteByDescriptorId(String descriptorId) {
                int routeCount = this.mRoutes.size();
                for (int i = 0; i < routeCount; i++) {
                    if (((RouteRecord) this.mRoutes.get(i)).getDescriptorId().equals(descriptorId)) {
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
                        ((RouteRecord) this.mRoutes.get(i)).dump(pw, indent);
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
            private RemoteDisplayInfo mDescriptor;
            private final String mDescriptorId;
            private RouteInfo mImmutableInfo;
            private final RouteInfo mMutableInfo;
            private final ProviderRecord mProviderRecord;

            public RouteRecord(ProviderRecord providerRecord, String descriptorId, String uniqueId) {
                this.mProviderRecord = providerRecord;
                this.mDescriptorId = descriptorId;
                this.mMutableInfo = new RouteInfo(uniqueId);
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

            public RouteInfo getInfo() {
                if (this.mImmutableInfo == null) {
                    this.mImmutableInfo = new RouteInfo(this.mMutableInfo);
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

            public boolean updateDescriptor(RemoteDisplayInfo descriptor) {
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

            private static String computeName(RemoteDisplayInfo descriptor) {
                return descriptor.name;
            }

            private static String computeDescription(RemoteDisplayInfo descriptor) {
                String description = descriptor.description;
                return TextUtils.isEmpty(description) ? null : description;
            }

            private static int computeSupportedTypes(RemoteDisplayInfo descriptor) {
                return 7;
            }

            private static boolean computeEnabled(RemoteDisplayInfo descriptor) {
                switch (descriptor.status) {
                    case 2:
                    case 3:
                    case 4:
                        return true;
                    default:
                        return false;
                }
            }

            private static int computeStatusCode(RemoteDisplayInfo descriptor) {
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

            private static int computePlaybackType(RemoteDisplayInfo descriptor) {
                return 1;
            }

            private static int computePlaybackStream(RemoteDisplayInfo descriptor) {
                return 3;
            }

            private static int computeVolume(RemoteDisplayInfo descriptor) {
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

            private static int computeVolumeMax(RemoteDisplayInfo descriptor) {
                int volumeMax = descriptor.volumeMax;
                return volumeMax > 0 ? volumeMax : 0;
            }

            private static int computeVolumeHandling(RemoteDisplayInfo descriptor) {
                switch (descriptor.volumeHandling) {
                    case 1:
                        return 1;
                    default:
                        return 0;
                }
            }

            private static int computePresentationDisplayId(RemoteDisplayInfo descriptor) {
                int displayId = descriptor.presentationDisplayId;
                return displayId < 0 ? -1 : displayId;
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
            String indent = prefix + "  ";
            pw.println(indent + "mRunning=" + this.mRunning);
            pw.println(indent + "mDiscoveryMode=" + this.mDiscoveryMode);
            pw.println(indent + "mGloballySelectedRouteRecord=" + this.mGloballySelectedRouteRecord);
            pw.println(indent + "mConnectionPhase=" + this.mConnectionPhase);
            pw.println(indent + "mConnectionTimeoutReason=" + this.mConnectionTimeoutReason);
            pw.println(indent + "mConnectionTimeoutStartTime=" + (this.mConnectionTimeoutReason != 0 ? TimeUtils.formatUptime(this.mConnectionTimeoutStartTime) : "<n/a>"));
            this.mWatcher.dump(pw, prefix);
            int providerCount = this.mProviderRecords.size();
            if (providerCount != 0) {
                for (int i = 0; i < providerCount; i++) {
                    ((ProviderRecord) this.mProviderRecords.get(i)).dump(pw, prefix);
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
                unselectGloballySelectedRoute();
                this.mWatcher.stop();
            }
        }

        private void updateDiscoveryRequest() {
            int i;
            int newDiscoveryMode;
            int routeTypes = 0;
            int activeScan = 0;
            synchronized (this.mService.mLock) {
                for (i = 0; i < this.mUserRecord.mClientRecords.size(); i++) {
                    ClientRecord clientRecord = (ClientRecord) this.mUserRecord.mClientRecords.get(i);
                    routeTypes |= clientRecord.mRouteTypes;
                    activeScan |= clientRecord.mActiveScan;
                }
            }
            if ((routeTypes & 4) == 0) {
                newDiscoveryMode = 0;
            } else if (activeScan != 0) {
                newDiscoveryMode = 2;
            } else {
                newDiscoveryMode = 1;
            }
            if (this.mDiscoveryMode != newDiscoveryMode) {
                this.mDiscoveryMode = newDiscoveryMode;
                int count = this.mProviderRecords.size();
                for (i = 0; i < count; i++) {
                    ((ProviderRecord) this.mProviderRecords.get(i)).getProvider().setDiscoveryMode(this.mDiscoveryMode);
                }
            }
        }

        private void selectRoute(String routeId) {
            if (routeId == null) {
                return;
            }
            if (this.mGloballySelectedRouteRecord == null || (routeId.equals(this.mGloballySelectedRouteRecord.getUniqueId()) ^ 1) != 0) {
                RouteRecord routeRecord = findRouteRecord(routeId);
                if (routeRecord != null) {
                    unselectGloballySelectedRoute();
                    Slog.i(MediaRouterService.TAG, "Selected global route:" + routeRecord);
                    this.mGloballySelectedRouteRecord = routeRecord;
                    checkGloballySelectedRouteState();
                    routeRecord.getProvider().setSelectedDisplay(routeRecord.getDescriptorId());
                    scheduleUpdateClientState();
                }
            }
        }

        private void unselectRoute(String routeId) {
            if (routeId != null && this.mGloballySelectedRouteRecord != null && routeId.equals(this.mGloballySelectedRouteRecord.getUniqueId())) {
                unselectGloballySelectedRoute();
            }
        }

        private void unselectGloballySelectedRoute() {
            if (this.mGloballySelectedRouteRecord != null) {
                Slog.i(MediaRouterService.TAG, "Unselected global route:" + this.mGloballySelectedRouteRecord);
                this.mGloballySelectedRouteRecord.getProvider().setSelectedDisplay(null);
                this.mGloballySelectedRouteRecord = null;
                checkGloballySelectedRouteState();
                scheduleUpdateClientState();
            }
        }

        private void requestSetVolume(String routeId, int volume) {
            if (this.mGloballySelectedRouteRecord != null && routeId.equals(this.mGloballySelectedRouteRecord.getUniqueId())) {
                this.mGloballySelectedRouteRecord.getProvider().setDisplayVolume(volume);
            }
        }

        private void requestUpdateVolume(String routeId, int direction) {
            if (this.mGloballySelectedRouteRecord != null && routeId.equals(this.mGloballySelectedRouteRecord.getUniqueId())) {
                this.mGloballySelectedRouteRecord.getProvider().adjustDisplayVolume(direction);
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
                ((ProviderRecord) this.mProviderRecords.remove(index)).updateDescriptor(null);
                provider.setCallback(null);
                provider.setDiscoveryMode(0);
                checkGloballySelectedRouteState();
                scheduleUpdateClientState();
            }
        }

        public void onDisplayStateChanged(RemoteDisplayProviderProxy provider, RemoteDisplayState state) {
            updateProvider(provider, state);
        }

        private void updateProvider(RemoteDisplayProviderProxy provider, RemoteDisplayState state) {
            int index = findProviderRecord(provider);
            if (index >= 0 && ((ProviderRecord) this.mProviderRecords.get(index)).updateDescriptor(state)) {
                checkGloballySelectedRouteState();
                scheduleUpdateClientState();
            }
        }

        private void checkGloballySelectedRouteState() {
            if (this.mGloballySelectedRouteRecord == null) {
                this.mConnectionPhase = -1;
                updateConnectionTimeout(0);
            } else if (this.mGloballySelectedRouteRecord.isValid() && (this.mGloballySelectedRouteRecord.isEnabled() ^ 1) == 0) {
                int oldPhase = this.mConnectionPhase;
                this.mConnectionPhase = getConnectionPhase(this.mGloballySelectedRouteRecord.getStatus());
                if (oldPhase < 1 || this.mConnectionPhase >= 1) {
                    switch (this.mConnectionPhase) {
                        case 0:
                            updateConnectionTimeout(3);
                            break;
                        case 1:
                            if (oldPhase != 1) {
                                Slog.i(MediaRouterService.TAG, "Connecting to global route: " + this.mGloballySelectedRouteRecord);
                            }
                            updateConnectionTimeout(4);
                            break;
                        case 2:
                            if (oldPhase != 2) {
                                Slog.i(MediaRouterService.TAG, "Connected to global route: " + this.mGloballySelectedRouteRecord);
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
            } else {
                updateConnectionTimeout(1);
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
            if (this.mConnectionTimeoutReason == 0 || this.mGloballySelectedRouteRecord == null) {
                Log.wtf(MediaRouterService.TAG, "Handled connection timeout for no reason.");
                return;
            }
            switch (this.mConnectionTimeoutReason) {
                case 1:
                    Slog.i(MediaRouterService.TAG, "Global route no longer available: " + this.mGloballySelectedRouteRecord);
                    break;
                case 2:
                    Slog.i(MediaRouterService.TAG, "Global route connection lost: " + this.mGloballySelectedRouteRecord);
                    break;
                case 3:
                    Slog.i(MediaRouterService.TAG, "Global route timed out while waiting for connection attempt to begin after " + (SystemClock.uptimeMillis() - this.mConnectionTimeoutStartTime) + " ms: " + this.mGloballySelectedRouteRecord);
                    break;
                case 4:
                    Slog.i(MediaRouterService.TAG, "Global route timed out while connecting after " + (SystemClock.uptimeMillis() - this.mConnectionTimeoutStartTime) + " ms: " + this.mGloballySelectedRouteRecord);
                    break;
            }
            this.mConnectionTimeoutReason = 0;
            unselectGloballySelectedRoute();
        }

        private void scheduleUpdateClientState() {
            if (!this.mClientStateUpdateScheduled) {
                this.mClientStateUpdateScheduled = true;
                sendEmptyMessage(8);
            }
        }

        private void updateClientState() {
            int i;
            this.mClientStateUpdateScheduled = false;
            String globallySelectedRouteId = this.mGloballySelectedRouteRecord != null ? this.mGloballySelectedRouteRecord.getUniqueId() : null;
            MediaRouterClientState trustedState = new MediaRouterClientState();
            trustedState.globallySelectedRouteId = globallySelectedRouteId;
            int providerCount = this.mProviderRecords.size();
            for (i = 0; i < providerCount; i++) {
                ((ProviderRecord) this.mProviderRecords.get(i)).appendClientState(trustedState);
            }
            MediaRouterClientState untrustedState = new MediaRouterClientState();
            untrustedState.globallySelectedRouteId = globallySelectedRouteId;
            if (globallySelectedRouteId != null) {
                untrustedState.routes.add(trustedState.getRoute(globallySelectedRouteId));
            }
            try {
                int count;
                synchronized (this.mService.mLock) {
                    this.mUserRecord.mTrustedState = trustedState;
                    this.mUserRecord.mUntrustedState = untrustedState;
                    count = this.mUserRecord.mClientRecords.size();
                    for (i = 0; i < count; i++) {
                        this.mTempClients.add(((ClientRecord) this.mUserRecord.mClientRecords.get(i)).mClient);
                    }
                }
                count = this.mTempClients.size();
                for (i = 0; i < count; i++) {
                    try {
                        ((IMediaRouterClient) this.mTempClients.get(i)).onStateChanged();
                    } catch (RemoteException e) {
                    }
                }
            } finally {
                this.mTempClients.clear();
            }
        }

        private int findProviderRecord(RemoteDisplayProviderProxy provider) {
            int count = this.mProviderRecords.size();
            for (int i = 0; i < count; i++) {
                if (((ProviderRecord) this.mProviderRecords.get(i)).getProvider() == provider) {
                    return i;
                }
            }
            return -1;
        }

        private RouteRecord findRouteRecord(String uniqueId) {
            int count = this.mProviderRecords.size();
            for (int i = 0; i < count; i++) {
                RouteRecord record = ((ProviderRecord) this.mProviderRecords.get(i)).findRouteByUniqueId(uniqueId);
                if (record != null) {
                    return record;
                }
            }
            return null;
        }

        private static int getConnectionPhase(int status) {
            switch (status) {
                case 0:
                case 6:
                    return 2;
                case 1:
                case 3:
                    return 0;
                case 2:
                    return 1;
                default:
                    return -1;
            }
        }
    }

    final class UserRecord {
        public final ArrayList<ClientRecord> mClientRecords = new ArrayList();
        public final UserHandler mHandler;
        public MediaRouterClientState mTrustedState;
        public MediaRouterClientState mUntrustedState;
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
                    ((ClientRecord) this.mClientRecords.get(i)).dump(pw, indent);
                }
            } else {
                pw.println(indent + "<no clients>");
            }
            pw.println(indent + "State");
            pw.println(indent + "mTrustedState=" + this.mTrustedState);
            pw.println(indent + "mUntrustedState=" + this.mUntrustedState);
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
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        int uid = Binder.getCallingUid();
        if (validatePackageName(uid, packageName)) {
            int pid = Binder.getCallingPid();
            int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, false, true, "registerClientAsUser", packageName);
            boolean trusted = this.mContext.checkCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY") == 0;
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    registerClientLocked(client, pid, packageName, resolvedUserId, trusted);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("packageName must match the calling uid");
        }
    }

    public void unregisterClient(IMediaRouterClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                unregisterClientLocked(client, false);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public MediaRouterClientState getState(IMediaRouterClient client) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        long token = Binder.clearCallingIdentity();
        try {
            MediaRouterClientState stateLocked;
            synchronized (this.mLock) {
                stateLocked = getStateLocked(client);
            }
            return stateLocked;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void setDiscoveryRequest(IMediaRouterClient client, int routeTypes, boolean activeScan) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                setDiscoveryRequestLocked(client, routeTypes, activeScan);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void setSelectedRoute(IMediaRouterClient client, String routeId, boolean explicit) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        }
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                setSelectedRouteLocked(client, routeId, explicit);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void requestSetVolume(IMediaRouterClient client, String routeId, int volume) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        } else if (routeId == null) {
            throw new IllegalArgumentException("routeId must not be null");
        } else {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    requestSetVolumeLocked(client, routeId, volume);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void requestUpdateVolume(IMediaRouterClient client, String routeId, int direction) {
        if (client == null) {
            throw new IllegalArgumentException("client must not be null");
        } else if (routeId == null) {
            throw new IllegalArgumentException("routeId must not be null");
        } else {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    requestUpdateVolumeLocked(client, routeId, direction);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
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
                    UserRecord userRecord = (UserRecord) this.mUserRecords.valueAt(i);
                    pw.println();
                    userRecord.dump(pw, "");
                }
            }
        }
    }

    void switchUser() {
        synchronized (this.mLock) {
            int userId = ActivityManager.getCurrentUser();
            if (this.mCurrentUserId != userId) {
                int oldUserId = this.mCurrentUserId;
                this.mCurrentUserId = userId;
                UserRecord oldUser = (UserRecord) this.mUserRecords.get(oldUserId);
                if (oldUser != null) {
                    oldUser.mHandler.sendEmptyMessage(2);
                    disposeUserIfNeededLocked(oldUser);
                }
                UserRecord newUser = (UserRecord) this.mUserRecords.get(userId);
                if (newUser != null) {
                    newUser.mHandler.sendEmptyMessage(1);
                }
            }
        }
    }

    void clientDied(ClientRecord clientRecord) {
        synchronized (this.mLock) {
            unregisterClientLocked(clientRecord.mClient, true);
        }
    }

    private void registerClientLocked(IMediaRouterClient client, int pid, String packageName, int userId, boolean trusted) {
        IBinder binder = client.asBinder();
        if (((ClientRecord) this.mAllClientRecords.get(binder)) == null) {
            boolean newUser = false;
            UserRecord userRecord = (UserRecord) this.mUserRecords.get(userId);
            if (userRecord == null) {
                userRecord = new UserRecord(userId);
                newUser = true;
            }
            ClientRecord clientRecord = new ClientRecord(userRecord, client, pid, packageName, trusted);
            try {
                binder.linkToDeath(clientRecord, 0);
                if (newUser) {
                    this.mUserRecords.put(userId, userRecord);
                    initializeUserLocked(userRecord);
                }
                userRecord.mClientRecords.add(clientRecord);
                this.mAllClientRecords.put(binder, clientRecord);
                initializeClientLocked(clientRecord);
            } catch (RemoteException ex) {
                throw new RuntimeException("Media router client died prematurely.", ex);
            }
        }
    }

    private void unregisterClientLocked(IMediaRouterClient client, boolean died) {
        ClientRecord clientRecord = (ClientRecord) this.mAllClientRecords.remove(client.asBinder());
        if (clientRecord != null) {
            UserRecord userRecord = clientRecord.mUserRecord;
            userRecord.mClientRecords.remove(clientRecord);
            disposeClientLocked(clientRecord, died);
            disposeUserIfNeededLocked(userRecord);
        }
    }

    private MediaRouterClientState getStateLocked(IMediaRouterClient client) {
        ClientRecord clientRecord = (ClientRecord) this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            return clientRecord.getState();
        }
        return null;
    }

    private void setDiscoveryRequestLocked(IMediaRouterClient client, int routeTypes, boolean activeScan) {
        ClientRecord clientRecord = (ClientRecord) this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            if (!clientRecord.mTrusted) {
                routeTypes &= -5;
            }
            if (clientRecord.mRouteTypes != routeTypes || clientRecord.mActiveScan != activeScan) {
                if (DEBUG) {
                    Slog.d(TAG, clientRecord + ": Set discovery request, routeTypes=0x" + Integer.toHexString(routeTypes) + ", activeScan=" + activeScan);
                }
                clientRecord.mRouteTypes = routeTypes;
                clientRecord.mActiveScan = activeScan;
                clientRecord.mUserRecord.mHandler.sendEmptyMessage(3);
            }
        }
    }

    private void setSelectedRouteLocked(IMediaRouterClient client, String routeId, boolean explicit) {
        ClientRecord clientRecord = (ClientRecord) this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            String oldRouteId = clientRecord.mSelectedRouteId;
            if (!Objects.equals(routeId, oldRouteId)) {
                if (DEBUG) {
                    Slog.d(TAG, clientRecord + ": Set selected route, routeId=" + routeId + ", oldRouteId=" + oldRouteId + ", explicit=" + explicit);
                }
                clientRecord.mSelectedRouteId = routeId;
                if (explicit) {
                    if (oldRouteId != null) {
                        clientRecord.mUserRecord.mHandler.obtainMessage(5, oldRouteId).sendToTarget();
                    }
                    if (routeId != null && clientRecord.mTrusted) {
                        clientRecord.mUserRecord.mHandler.obtainMessage(4, routeId).sendToTarget();
                    }
                }
            }
        }
    }

    private void requestSetVolumeLocked(IMediaRouterClient client, String routeId, int volume) {
        ClientRecord clientRecord = (ClientRecord) this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            clientRecord.mUserRecord.mHandler.obtainMessage(6, volume, 0, routeId).sendToTarget();
        }
    }

    private void requestUpdateVolumeLocked(IMediaRouterClient client, String routeId, int direction) {
        ClientRecord clientRecord = (ClientRecord) this.mAllClientRecords.get(client.asBinder());
        if (clientRecord != null) {
            clientRecord.mUserRecord.mHandler.obtainMessage(7, direction, 0, routeId).sendToTarget();
        }
    }

    private void initializeUserLocked(UserRecord userRecord) {
        if (DEBUG) {
            Slog.d(TAG, userRecord + ": Initialized");
        }
        if (userRecord.mUserId == this.mCurrentUserId) {
            userRecord.mHandler.sendEmptyMessage(1);
        }
    }

    private void disposeUserIfNeededLocked(UserRecord userRecord) {
        if (userRecord.mUserId != this.mCurrentUserId && userRecord.mClientRecords.isEmpty()) {
            if (DEBUG) {
                Slog.d(TAG, userRecord + ": Disposed");
            }
            this.mUserRecords.remove(userRecord.mUserId);
        }
    }

    private void initializeClientLocked(ClientRecord clientRecord) {
        if (DEBUG) {
            Slog.d(TAG, clientRecord + ": Registered");
        }
    }

    private void disposeClientLocked(ClientRecord clientRecord, boolean died) {
        if (DEBUG) {
            if (died) {
                Slog.d(TAG, clientRecord + ": Died!");
            } else {
                Slog.d(TAG, clientRecord + ": Unregistered");
            }
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
