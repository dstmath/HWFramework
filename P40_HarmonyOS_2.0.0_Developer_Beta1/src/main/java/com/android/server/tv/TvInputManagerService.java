package com.android.server.tv;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Rect;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.PlaybackParams;
import android.media.tv.DvbDeviceInfo;
import android.media.tv.ITvInputClient;
import android.media.tv.ITvInputHardware;
import android.media.tv.ITvInputHardwareCallback;
import android.media.tv.ITvInputManager;
import android.media.tv.ITvInputManagerCallback;
import android.media.tv.ITvInputService;
import android.media.tv.ITvInputServiceCallback;
import android.media.tv.ITvInputSession;
import android.media.tv.ITvInputSessionCallback;
import android.media.tv.TvContentRating;
import android.media.tv.TvContentRatingSystemInfo;
import android.media.tv.TvContract;
import android.media.tv.TvInputHardwareInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvStreamConfig;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.Surface;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.IoThread;
import com.android.server.SystemService;
import com.android.server.UiModeManagerService;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.tv.TvInputHardwareManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TvInputManagerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String DVB_DIRECTORY = "/dev/dvb";
    private static final String TAG = "TvInputManagerService";
    private static final Pattern sAdapterDirPattern = Pattern.compile("^adapter([0-9]+)$");
    private static final Pattern sFrontEndDevicePattern = Pattern.compile("^dvb([0-9]+)\\.frontend([0-9]+)$");
    private static final Pattern sFrontEndInAdapterDirPattern = Pattern.compile("^frontend([0-9]+)$");
    private final Context mContext;
    private int mCurrentUserId = 0;
    private IBinder.DeathRecipient mDeathRecipient;
    private final Object mLock = new Object();
    private final TvInputHardwareManager mTvInputHardwareManager;
    private final SparseArray<UserState> mUserStates = new SparseArray<>();
    private final WatchLogHandler mWatchLogHandler;

    public TvInputManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mWatchLogHandler = new WatchLogHandler(this.mContext.getContentResolver(), IoThread.get().getLooper());
        this.mTvInputHardwareManager = new TvInputHardwareManager(context, new HardwareListener());
        synchronized (this.mLock) {
            getOrCreateUserStateLocked(this.mCurrentUserId);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.tv.TvInputManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.tv.TvInputManagerService$BinderService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("tv_input", new BinderService());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            registerBroadcastReceivers();
        } else if (phase == 600) {
            synchronized (this.mLock) {
                buildTvInputListLocked(this.mCurrentUserId, null);
                buildTvContentRatingSystemListLocked(this.mCurrentUserId);
            }
        }
        this.mTvInputHardwareManager.onBootPhase(phase);
    }

    @Override // com.android.server.SystemService
    public void onUnlockUser(int userHandle) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userHandle) {
                buildTvInputListLocked(this.mCurrentUserId, null);
                buildTvContentRatingSystemListLocked(this.mCurrentUserId);
            }
        }
    }

    private void registerBroadcastReceivers() {
        new PackageMonitor() {
            /* class com.android.server.tv.TvInputManagerService.AnonymousClass1 */

            private void buildTvInputList(String[] packages) {
                synchronized (TvInputManagerService.this.mLock) {
                    if (TvInputManagerService.this.mCurrentUserId == getChangingUserId()) {
                        TvInputManagerService.this.buildTvInputListLocked(TvInputManagerService.this.mCurrentUserId, packages);
                        TvInputManagerService.this.buildTvContentRatingSystemListLocked(TvInputManagerService.this.mCurrentUserId);
                    }
                }
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                buildTvInputList(new String[]{packageName});
            }

            public void onPackagesAvailable(String[] packages) {
                if (isReplacing()) {
                    buildTvInputList(packages);
                }
            }

            public void onPackagesUnavailable(String[] packages) {
                if (isReplacing()) {
                    buildTvInputList(packages);
                }
            }

            public void onSomePackagesChanged() {
                if (!isReplacing()) {
                    buildTvInputList(null);
                }
            }

            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                return true;
            }
        }.register(this.mContext, (Looper) null, UserHandle.ALL, true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.tv.TvInputManagerService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    TvInputManagerService.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    TvInputManagerService.this.removeUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
    }

    /* access modifiers changed from: private */
    public static boolean hasHardwarePermission(PackageManager pm, ComponentName component) {
        return pm.checkPermission("android.permission.TV_INPUT_HARDWARE", component.getPackageName()) == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void buildTvInputListLocked(int userId, String[] updatedPackages) {
        UserState userState = getOrCreateUserStateLocked(userId);
        userState.packageSet.clear();
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServicesAsUser(new Intent("android.media.tv.TvInputService"), 132, userId);
        List<TvInputInfo> inputList = new ArrayList<>();
        for (ResolveInfo ri : services) {
            ServiceInfo si = ri.serviceInfo;
            if (!"android.permission.BIND_TV_INPUT".equals(si.permission)) {
                Slog.w(TAG, "Skipping TV input " + si.name + ": it does not require the permission android.permission.BIND_TV_INPUT");
            } else {
                ComponentName component = new ComponentName(si.packageName, si.name);
                if (hasHardwarePermission(pm, component)) {
                    ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(component);
                    if (serviceState == null) {
                        userState.serviceStateMap.put(component, new ServiceState(component, userId));
                        updateServiceConnectionLocked(component, userId);
                    } else {
                        inputList.addAll(serviceState.hardwareInputMap.values());
                    }
                } else {
                    try {
                        inputList.add(new TvInputInfo.Builder(this.mContext, ri).build());
                    } catch (Exception e) {
                        Slog.e(TAG, "failed to load TV input " + si.name, e);
                    }
                }
                userState.packageSet.add(si.packageName);
            }
        }
        Map<String, TvInputState> inputMap = new HashMap<>();
        for (TvInputInfo info : inputList) {
            TvInputState inputState = (TvInputState) userState.inputMap.get(info.getId());
            if (inputState == null) {
                inputState = new TvInputState();
            }
            inputState.info = info;
            inputMap.put(info.getId(), inputState);
        }
        for (String inputId : inputMap.keySet()) {
            if (!userState.inputMap.containsKey(inputId)) {
                notifyInputAddedLocked(userState, inputId);
            } else if (updatedPackages != null) {
                ComponentName component2 = inputMap.get(inputId).info.getComponent();
                int length = updatedPackages.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    if (component2.getPackageName().equals(updatedPackages[i])) {
                        updateServiceConnectionLocked(component2, userId);
                        notifyInputUpdatedLocked(userState, inputId);
                        break;
                    }
                    i++;
                }
            }
        }
        for (String inputId2 : userState.inputMap.keySet()) {
            if (!inputMap.containsKey(inputId2)) {
                ServiceState serviceState2 = (ServiceState) userState.serviceStateMap.get(((TvInputState) userState.inputMap.get(inputId2)).info.getComponent());
                if (serviceState2 != null) {
                    abortPendingCreateSessionRequestsLocked(serviceState2, inputId2, userId);
                }
                notifyInputRemovedLocked(userState, inputId2);
            }
        }
        userState.inputMap.clear();
        userState.inputMap = inputMap;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void buildTvContentRatingSystemListLocked(int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        userState.contentRatingSystemList.clear();
        for (ResolveInfo resolveInfo : this.mContext.getPackageManager().queryBroadcastReceivers(new Intent("android.media.tv.action.QUERY_CONTENT_RATING_SYSTEMS"), 128)) {
            ActivityInfo receiver = resolveInfo.activityInfo;
            Bundle metaData = receiver.metaData;
            if (metaData != null) {
                int xmlResId = metaData.getInt("android.media.tv.metadata.CONTENT_RATING_SYSTEMS");
                if (xmlResId == 0) {
                    Slog.w(TAG, "Missing meta-data 'android.media.tv.metadata.CONTENT_RATING_SYSTEMS' on receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name);
                } else {
                    userState.contentRatingSystemList.add(TvContentRatingSystemInfo.createTvContentRatingSystemInfo(xmlResId, receiver.applicationInfo));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void switchUser(int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId != userId) {
                UserState userState = this.mUserStates.get(this.mCurrentUserId);
                List<SessionState> sessionStatesToRelease = new ArrayList<>();
                for (SessionState sessionState : userState.sessionStateMap.values()) {
                    if (sessionState.session != null && !sessionState.isRecordingSession) {
                        sessionStatesToRelease.add(sessionState);
                    }
                }
                for (SessionState sessionState2 : sessionStatesToRelease) {
                    try {
                        sessionState2.session.release();
                    } catch (RemoteException e) {
                        Slog.e(TAG, "error in release", e);
                    }
                    clearSessionAndNotifyClientLocked(sessionState2);
                }
                Iterator<ComponentName> it = userState.serviceStateMap.keySet().iterator();
                while (it.hasNext()) {
                    ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(it.next());
                    if (serviceState != null && serviceState.sessionTokens.isEmpty()) {
                        if (serviceState.callback != null) {
                            try {
                                serviceState.service.unregisterCallback(serviceState.callback);
                            } catch (RemoteException e2) {
                                Slog.e(TAG, "error in unregisterCallback", e2);
                            }
                        }
                        this.mContext.unbindService(serviceState.connection);
                        it.remove();
                    }
                }
                this.mCurrentUserId = userId;
                getOrCreateUserStateLocked(userId);
                buildTvInputListLocked(userId, null);
                buildTvContentRatingSystemListLocked(userId);
                this.mWatchLogHandler.obtainMessage(3, getContentResolverForUser(userId)).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearSessionAndNotifyClientLocked(SessionState state) {
        if (state.client != null) {
            try {
                state.client.onSessionReleased(state.seq);
            } catch (RemoteException e) {
                Slog.e(TAG, "error in onSessionReleased", e);
            }
        }
        for (SessionState sessionState : getOrCreateUserStateLocked(state.userId).sessionStateMap.values()) {
            if (state.sessionToken == sessionState.hardwareSessionToken) {
                releaseSessionLocked(sessionState.sessionToken, 1000, state.userId);
                try {
                    sessionState.client.onSessionReleased(sessionState.seq);
                } catch (RemoteException e2) {
                    Slog.e(TAG, "error in onSessionReleased", e2);
                }
            }
        }
        removeSessionStateLocked(state.sessionToken, state.userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeUser(int userId) {
        synchronized (this.mLock) {
            UserState userState = this.mUserStates.get(userId);
            if (userState != null) {
                for (SessionState state : userState.sessionStateMap.values()) {
                    if (state.session != null) {
                        try {
                            state.session.release();
                        } catch (RemoteException e) {
                            Slog.e(TAG, "error in release", e);
                        }
                    }
                }
                userState.sessionStateMap.clear();
                for (ServiceState serviceState : userState.serviceStateMap.values()) {
                    if (serviceState.service != null) {
                        if (serviceState.callback != null) {
                            try {
                                serviceState.service.unregisterCallback(serviceState.callback);
                            } catch (RemoteException e2) {
                                Slog.e(TAG, "error in unregisterCallback", e2);
                            }
                        }
                        this.mContext.unbindService(serviceState.connection);
                    }
                }
                userState.serviceStateMap.clear();
                userState.inputMap.clear();
                userState.packageSet.clear();
                userState.contentRatingSystemList.clear();
                userState.clientStateMap.clear();
                userState.callbackSet.clear();
                userState.mainSessionToken = null;
                this.mUserStates.remove(userId);
            }
        }
    }

    private ContentResolver getContentResolverForUser(int userId) {
        Context context;
        UserHandle user = new UserHandle(userId);
        try {
            context = this.mContext.createPackageContextAsUser(PackageManagerService.PLATFORM_PACKAGE_NAME, 0, user);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "failed to create package context as user " + user);
            context = this.mContext;
        }
        return context.getContentResolver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserState getOrCreateUserStateLocked(int userId) {
        UserState userState = this.mUserStates.get(userId);
        if (userState != null) {
            return userState;
        }
        UserState userState2 = new UserState(this.mContext, userId);
        this.mUserStates.put(userId, userState2);
        return userState2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ServiceState getServiceStateLocked(ComponentName component, int userId) {
        ServiceState serviceState = (ServiceState) getOrCreateUserStateLocked(userId).serviceStateMap.get(component);
        if (serviceState != null) {
            return serviceState;
        }
        throw new IllegalStateException("Service state not found for " + component + " (userId=" + userId + ")");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private SessionState getSessionStateLocked(IBinder sessionToken, int callingUid, int userId) {
        SessionState sessionState = (SessionState) getOrCreateUserStateLocked(userId).sessionStateMap.get(sessionToken);
        if (sessionState == null) {
            throw new SessionNotFoundException("Session state not found for token " + sessionToken);
        } else if (callingUid == 1000 || callingUid == sessionState.callingUid) {
            return sessionState;
        } else {
            throw new SecurityException("Illegal access to the session with token " + sessionToken + " from uid " + callingUid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ITvInputSession getSessionLocked(IBinder sessionToken, int callingUid, int userId) {
        return getSessionLocked(getSessionStateLocked(sessionToken, callingUid, userId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ITvInputSession getSessionLocked(SessionState sessionState) {
        ITvInputSession session = sessionState.session;
        if (session != null) {
            return session;
        }
        throw new IllegalStateException("Session not yet created for token " + sessionState.sessionToken);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int resolveCallingUserId(int callingPid, int callingUid, int requestedUserId, String methodName) {
        return ActivityManager.handleIncomingUser(callingPid, callingUid, requestedUserId, false, false, methodName, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateServiceConnectionLocked(ComponentName component, int userId) {
        boolean shouldBind;
        UserState userState = getOrCreateUserStateLocked(userId);
        ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(component);
        if (serviceState != null) {
            boolean z = false;
            if (serviceState.reconnecting) {
                if (serviceState.sessionTokens.isEmpty()) {
                    serviceState.reconnecting = false;
                } else {
                    return;
                }
            }
            if (userId == this.mCurrentUserId) {
                if (!serviceState.sessionTokens.isEmpty() || serviceState.isHardware) {
                    z = true;
                }
                shouldBind = z;
            } else {
                shouldBind = !serviceState.sessionTokens.isEmpty();
            }
            if (serviceState.service != null || !shouldBind) {
                if (serviceState.service != null && !shouldBind) {
                    this.mContext.unbindService(serviceState.connection);
                    userState.serviceStateMap.remove(component);
                }
            } else if (!serviceState.bound) {
                serviceState.bound = this.mContext.bindServiceAsUser(new Intent("android.media.tv.TvInputService").setComponent(component), serviceState.connection, 33554433, new UserHandle(userId));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void abortPendingCreateSessionRequestsLocked(ServiceState serviceState, String inputId, int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        List<SessionState> sessionsToAbort = new ArrayList<>();
        for (IBinder sessionToken : serviceState.sessionTokens) {
            SessionState sessionState = (SessionState) userState.sessionStateMap.get(sessionToken);
            if (sessionState.session == null && (inputId == null || sessionState.inputId.equals(inputId))) {
                sessionsToAbort.add(sessionState);
            }
        }
        for (SessionState sessionState2 : sessionsToAbort) {
            removeSessionStateLocked(sessionState2.sessionToken, sessionState2.userId);
            sendSessionTokenToClientLocked(sessionState2.client, sessionState2.inputId, null, null, sessionState2.seq);
        }
        updateServiceConnectionLocked(serviceState.component, userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean createSessionInternalLocked(ITvInputService service, IBinder sessionToken, int userId) {
        SessionState sessionState = (SessionState) getOrCreateUserStateLocked(userId).sessionStateMap.get(sessionToken);
        RemoteException[] channels = InputChannel.openInputChannelPair(sessionToken.toString());
        ITvInputSessionCallback callback = new SessionCallback(sessionState, channels);
        boolean created = true;
        try {
            if (sessionState.isRecordingSession) {
                service.createRecordingSession(callback, sessionState.inputId);
            } else {
                service.createSession(channels[1], callback, sessionState.inputId);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "error in createSession", e);
            sendSessionTokenToClientLocked(sessionState.client, sessionState.inputId, null, null, sessionState.seq);
            created = false;
        }
        channels[1].dispose();
        return created;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendSessionTokenToClientLocked(ITvInputClient client, String inputId, IBinder sessionToken, InputChannel channel, int seq) {
        try {
            client.onSessionCreated(inputId, sessionToken, channel, seq);
        } catch (RemoteException e) {
            Slog.e(TAG, "error in onSessionCreated", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003e, code lost:
        if (0 == 0) goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
        removeSessionStateLocked(r6, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002d, code lost:
        if (r0 != null) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002f, code lost:
        r0.session = null;
     */
    private void releaseSessionLocked(IBinder sessionToken, int callingUid, int userId) {
        SessionState sessionState = null;
        try {
            sessionState = getSessionStateLocked(sessionToken, callingUid, userId);
            if (sessionState.session != null) {
                if (sessionToken == getOrCreateUserStateLocked(userId).mainSessionToken) {
                    setMainLocked(sessionToken, false, callingUid, userId);
                }
                sessionState.session.asBinder().unlinkToDeath(sessionState, 0);
                sessionState.session.release();
            }
        } catch (RemoteException | SessionNotFoundException e) {
            Slog.e(TAG, "error in releaseSession", e);
        } catch (Throwable th) {
            if (0 != 0) {
                null.session = null;
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeSessionStateLocked(IBinder sessionToken, int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        if (sessionToken == userState.mainSessionToken) {
            userState.mainSessionToken = null;
        }
        SessionState sessionState = (SessionState) userState.sessionStateMap.remove(sessionToken);
        if (sessionState != null) {
            ClientState clientState = (ClientState) userState.clientStateMap.get(sessionState.client.asBinder());
            if (clientState != null) {
                clientState.sessionTokens.remove(sessionToken);
                if (clientState.isEmpty()) {
                    userState.clientStateMap.remove(sessionState.client.asBinder());
                    sessionState.client.asBinder().unlinkToDeath(clientState, 0);
                }
            }
            ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(sessionState.componentName);
            if (serviceState != null) {
                serviceState.sessionTokens.remove(sessionToken);
            }
            updateServiceConnectionLocked(sessionState.componentName, userId);
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = sessionToken;
            args.arg2 = Long.valueOf(System.currentTimeMillis());
            this.mWatchLogHandler.obtainMessage(2, args).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMainLocked(IBinder sessionToken, boolean isMain, int callingUid, int userId) {
        try {
            SessionState sessionState = getSessionStateLocked(sessionToken, callingUid, userId);
            if (sessionState.hardwareSessionToken != null) {
                sessionState = getSessionStateLocked(sessionState.hardwareSessionToken, 1000, userId);
            }
            if (getServiceStateLocked(sessionState.componentName, userId).isHardware) {
                getSessionLocked(sessionState).setMain(isMain);
            }
        } catch (RemoteException | SessionNotFoundException e) {
            Slog.e(TAG, "error in setMain", e);
        }
    }

    private void notifyInputAddedLocked(UserState userState, String inputId) {
        for (ITvInputManagerCallback callback : userState.callbackSet) {
            try {
                callback.onInputAdded(inputId);
            } catch (RemoteException e) {
                Slog.e(TAG, "failed to report added input to callback", e);
            }
        }
    }

    private void notifyInputRemovedLocked(UserState userState, String inputId) {
        for (ITvInputManagerCallback callback : userState.callbackSet) {
            try {
                callback.onInputRemoved(inputId);
            } catch (RemoteException e) {
                Slog.e(TAG, "failed to report removed input to callback", e);
            }
        }
    }

    private void notifyInputUpdatedLocked(UserState userState, String inputId) {
        for (ITvInputManagerCallback callback : userState.callbackSet) {
            try {
                callback.onInputUpdated(inputId);
            } catch (RemoteException e) {
                Slog.e(TAG, "failed to report updated input to callback", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyInputStateChangedLocked(UserState userState, String inputId, int state, ITvInputManagerCallback targetCallback) {
        if (targetCallback == null) {
            for (ITvInputManagerCallback callback : userState.callbackSet) {
                try {
                    callback.onInputStateChanged(inputId, state);
                } catch (RemoteException e) {
                    Slog.e(TAG, "failed to report state change to callback", e);
                }
            }
            return;
        }
        try {
            targetCallback.onInputStateChanged(inputId, state);
        } catch (RemoteException e2) {
            Slog.e(TAG, "failed to report state change to callback", e2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTvInputInfoLocked(UserState userState, TvInputInfo inputInfo) {
        String inputId = inputInfo.getId();
        TvInputState inputState = (TvInputState) userState.inputMap.get(inputId);
        if (inputState == null) {
            Slog.e(TAG, "failed to set input info - unknown input id " + inputId);
            return;
        }
        inputState.info = inputInfo;
        for (ITvInputManagerCallback callback : userState.callbackSet) {
            try {
                callback.onTvInputInfoUpdated(inputInfo);
            } catch (RemoteException e) {
                Slog.e(TAG, "failed to report updated input info to callback", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStateLocked(String inputId, int state, int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        TvInputState inputState = (TvInputState) userState.inputMap.get(inputId);
        ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(inputState.info.getComponent());
        int oldState = inputState.state;
        inputState.state = state;
        if ((serviceState == null || serviceState.service != null || (serviceState.sessionTokens.isEmpty() && !serviceState.isHardware)) && oldState != state) {
            notifyInputStateChangedLocked(userState, inputId, state, null);
        }
    }

    private final class BinderService extends ITvInputManager.Stub {
        private BinderService() {
        }

        public List<TvInputInfo> getTvInputList(int userId) {
            List<TvInputInfo> inputList;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputList");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    inputList = new ArrayList<>();
                    for (TvInputState state : userState.inputMap.values()) {
                        inputList.add(state.info);
                    }
                }
                return inputList;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public TvInputInfo getTvInputInfo(String inputId, int userId) {
            TvInputInfo tvInputInfo;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputInfo");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputState state = (TvInputState) TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).inputMap.get(inputId);
                    tvInputInfo = state == null ? null : state.info;
                }
                return tvInputInfo;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void updateTvInputInfo(TvInputInfo inputInfo, int userId) {
            String inputInfoPackageName = inputInfo.getServiceInfo().packageName;
            String callingPackageName = getCallingPackageName();
            if (TextUtils.equals(inputInfoPackageName, callingPackageName) || TvInputManagerService.this.mContext.checkCallingPermission("android.permission.WRITE_SECURE_SETTINGS") == 0) {
                int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "updateTvInputInfo");
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (TvInputManagerService.this.mLock) {
                        TvInputManagerService.this.updateTvInputInfoLocked(TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId), inputInfo);
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new IllegalArgumentException("calling package " + callingPackageName + " is not allowed to change TvInputInfo for " + inputInfoPackageName);
            }
        }

        private String getCallingPackageName() {
            String[] packages = TvInputManagerService.this.mContext.getPackageManager().getPackagesForUid(Binder.getCallingUid());
            if (packages == null || packages.length <= 0) {
                return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
            }
            return packages[0];
        }

        public int getTvInputState(String inputId, int userId) {
            int i;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputState");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputState state = (TvInputState) TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).inputMap.get(inputId);
                    i = state == null ? 0 : state.state;
                }
                return i;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public List<TvContentRatingSystemInfo> getTvContentRatingSystemList(int userId) {
            List<TvContentRatingSystemInfo> list;
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.READ_CONTENT_RATING_SYSTEMS") == 0) {
                int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvContentRatingSystemList");
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (TvInputManagerService.this.mLock) {
                        list = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).contentRatingSystemList;
                    }
                    return list;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new SecurityException("The caller does not have permission to read content rating systems");
            }
        }

        public void sendTvInputNotifyIntent(Intent intent, int userId) {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.NOTIFY_TV_INPUTS") != 0) {
                throw new SecurityException("The caller: " + getCallingPackageName() + " doesn't have permission: android.permission.NOTIFY_TV_INPUTS");
            } else if (!TextUtils.isEmpty(intent.getPackage())) {
                String action = intent.getAction();
                char c = 65535;
                int hashCode = action.hashCode();
                if (hashCode != -160295064) {
                    if (hashCode != 1568780589) {
                        if (hashCode == 2011523553 && action.equals("android.media.tv.action.PREVIEW_PROGRAM_ADDED_TO_WATCH_NEXT")) {
                            c = 2;
                        }
                    } else if (action.equals("android.media.tv.action.PREVIEW_PROGRAM_BROWSABLE_DISABLED")) {
                        c = 0;
                    }
                } else if (action.equals("android.media.tv.action.WATCH_NEXT_PROGRAM_BROWSABLE_DISABLED")) {
                    c = 1;
                }
                if (c != 0) {
                    if (c != 1) {
                        if (c != 2) {
                            throw new IllegalArgumentException("Invalid TV input notifying action: " + intent.getAction());
                        } else if (intent.getLongExtra("android.media.tv.extra.PREVIEW_PROGRAM_ID", -1) < 0) {
                            throw new IllegalArgumentException("Invalid preview program ID.");
                        } else if (intent.getLongExtra("android.media.tv.extra.WATCH_NEXT_PROGRAM_ID", -1) < 0) {
                            throw new IllegalArgumentException("Invalid watch next program ID.");
                        }
                    } else if (intent.getLongExtra("android.media.tv.extra.WATCH_NEXT_PROGRAM_ID", -1) < 0) {
                        throw new IllegalArgumentException("Invalid watch next program ID.");
                    }
                } else if (intent.getLongExtra("android.media.tv.extra.PREVIEW_PROGRAM_ID", -1) < 0) {
                    throw new IllegalArgumentException("Invalid preview program ID.");
                }
                int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "sendTvInputNotifyIntent");
                long identity = Binder.clearCallingIdentity();
                try {
                    TvInputManagerService.this.getContext().sendBroadcastAsUser(intent, new UserHandle(resolvedUserId));
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new IllegalArgumentException("Must specify package name to notify.");
            }
        }

        public void registerCallback(final ITvInputManagerCallback callback, int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "registerCallback");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    final UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    userState.callbackSet.add(callback);
                    TvInputManagerService.this.mDeathRecipient = new IBinder.DeathRecipient() {
                        /* class com.android.server.tv.TvInputManagerService.BinderService.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (TvInputManagerService.this.mLock) {
                                if (userState.callbackSet != null) {
                                    userState.callbackSet.remove(callback);
                                }
                            }
                        }
                    };
                    try {
                        callback.asBinder().linkToDeath(TvInputManagerService.this.mDeathRecipient, 0);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "client process has already died", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void unregisterCallback(ITvInputManagerCallback callback, int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "unregisterCallback");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).callbackSet.remove(callback);
                    callback.asBinder().unlinkToDeath(TvInputManagerService.this.mDeathRecipient, 0);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean isParentalControlsEnabled(int userId) {
            boolean isParentalControlsEnabled;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "isParentalControlsEnabled");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    isParentalControlsEnabled = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).persistentDataStore.isParentalControlsEnabled();
                }
                return isParentalControlsEnabled;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setParentalControlsEnabled(boolean enabled, int userId) {
            ensureParentalControlsPermission();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "setParentalControlsEnabled");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).persistentDataStore.setParentalControlsEnabled(enabled);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean isRatingBlocked(String rating, int userId) {
            boolean isRatingBlocked;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "isRatingBlocked");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    isRatingBlocked = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).persistentDataStore.isRatingBlocked(TvContentRating.unflattenFromString(rating));
                }
                return isRatingBlocked;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public List<String> getBlockedRatings(int userId) {
            List<String> ratings;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getBlockedRatings");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    ratings = new ArrayList<>();
                    for (TvContentRating rating : userState.persistentDataStore.getBlockedRatings()) {
                        ratings.add(rating.flattenToString());
                    }
                }
                return ratings;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void addBlockedRating(String rating, int userId) {
            ensureParentalControlsPermission();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "addBlockedRating");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).persistentDataStore.addBlockedRating(TvContentRating.unflattenFromString(rating));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void removeBlockedRating(String rating, int userId) {
            ensureParentalControlsPermission();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "removeBlockedRating");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).persistentDataStore.removeBlockedRating(TvContentRating.unflattenFromString(rating));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private void ensureParentalControlsPermission() {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.MODIFY_PARENTAL_CONTROLS") != 0) {
                throw new SecurityException("The caller does not have parental controls permission");
            }
        }

        public void createSession(ITvInputClient client, String inputId, boolean isRecordingSession, int seq, int userId) {
            Throwable th;
            Throwable th2;
            ServiceState serviceState;
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "createSession");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        if (userId == TvInputManagerService.this.mCurrentUserId || isRecordingSession) {
                            UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                            TvInputState inputState = (TvInputState) userState.inputMap.get(inputId);
                            if (inputState == null) {
                                Slog.w(TvInputManagerService.TAG, "Failed to find input state for inputId=" + inputId);
                                TvInputManagerService.this.sendSessionTokenToClientLocked(client, inputId, null, null, seq);
                                Binder.restoreCallingIdentity(identity);
                                return;
                            }
                            TvInputInfo info = inputState.info;
                            ServiceState serviceState2 = (ServiceState) userState.serviceStateMap.get(info.getComponent());
                            if (serviceState2 == null) {
                                ServiceState serviceState3 = new ServiceState(info.getComponent(), resolvedUserId);
                                userState.serviceStateMap.put(info.getComponent(), serviceState3);
                                serviceState = serviceState3;
                            } else {
                                serviceState = serviceState2;
                            }
                            if (serviceState.reconnecting) {
                                TvInputManagerService.this.sendSessionTokenToClientLocked(client, inputId, null, null, seq);
                                Binder.restoreCallingIdentity(identity);
                                return;
                            }
                            Binder binder = new Binder();
                            try {
                                userState.sessionStateMap.put(binder, new SessionState(binder, info.getId(), info.getComponent(), isRecordingSession, client, seq, callingUid, resolvedUserId));
                                serviceState.sessionTokens.add(binder);
                                if (serviceState.service == null) {
                                    TvInputManagerService.this.updateServiceConnectionLocked(info.getComponent(), resolvedUserId);
                                } else if (!TvInputManagerService.this.createSessionInternalLocked(serviceState.service, binder, resolvedUserId)) {
                                    TvInputManagerService.this.removeSessionStateLocked(binder, resolvedUserId);
                                }
                                Binder.restoreCallingIdentity(identity);
                                return;
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        } else {
                            try {
                                TvInputManagerService.this.sendSessionTokenToClientLocked(client, inputId, null, null, seq);
                                Binder.restoreCallingIdentity(identity);
                                return;
                            } catch (Throwable th4) {
                                th2 = th4;
                            }
                        }
                    } catch (Throwable th5) {
                        th2 = th5;
                    }
                }
                try {
                    throw th2;
                } catch (Throwable th6) {
                    th = th6;
                }
            } catch (Throwable th7) {
                th = th7;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void releaseSession(IBinder sessionToken, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "releaseSession");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputManagerService.this.releaseSessionLocked(sessionToken, callingUid, resolvedUserId);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setMainSession(IBinder sessionToken, int userId) {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.CHANGE_HDMI_CEC_ACTIVE_SOURCE") == 0) {
                int callingUid = Binder.getCallingUid();
                int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "setMainSession");
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (TvInputManagerService.this.mLock) {
                        UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                        if (userState.mainSessionToken != sessionToken) {
                            IBinder oldMainSessionToken = userState.mainSessionToken;
                            userState.mainSessionToken = sessionToken;
                            if (sessionToken != null) {
                                TvInputManagerService.this.setMainLocked(sessionToken, true, callingUid, userId);
                            }
                            if (oldMainSessionToken != null) {
                                TvInputManagerService.this.setMainLocked(oldMainSessionToken, false, 1000, userId);
                            }
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new SecurityException("The caller does not have CHANGE_HDMI_CEC_ACTIVE_SOURCE permission");
            }
        }

        public void setSurface(IBinder sessionToken, Surface surface, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "setSurface");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        SessionState sessionState = TvInputManagerService.this.getSessionStateLocked(sessionToken, callingUid, resolvedUserId);
                        if (sessionState.hardwareSessionToken == null) {
                            TvInputManagerService.this.getSessionLocked(sessionState).setSurface(surface);
                        } else {
                            TvInputManagerService.this.getSessionLocked(sessionState.hardwareSessionToken, 1000, resolvedUserId).setSurface(surface);
                        }
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in setSurface", e);
                    }
                }
            } finally {
                if (surface != null) {
                    surface.release();
                }
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void dispatchSurfaceChanged(IBinder sessionToken, int format, int width, int height, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "dispatchSurfaceChanged");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        SessionState sessionState = TvInputManagerService.this.getSessionStateLocked(sessionToken, callingUid, resolvedUserId);
                        TvInputManagerService.this.getSessionLocked(sessionState).dispatchSurfaceChanged(format, width, height);
                        if (sessionState.hardwareSessionToken != null) {
                            TvInputManagerService.this.getSessionLocked(sessionState.hardwareSessionToken, 1000, resolvedUserId).dispatchSurfaceChanged(format, width, height);
                        }
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in dispatchSurfaceChanged", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setVolume(IBinder sessionToken, float volume, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "setVolume");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        SessionState sessionState = TvInputManagerService.this.getSessionStateLocked(sessionToken, callingUid, resolvedUserId);
                        TvInputManagerService.this.getSessionLocked(sessionState).setVolume(volume);
                        if (sessionState.hardwareSessionToken != null) {
                            ITvInputSession sessionLocked = TvInputManagerService.this.getSessionLocked(sessionState.hardwareSessionToken, 1000, resolvedUserId);
                            float f = 0.0f;
                            if (volume > 0.0f) {
                                f = 1.0f;
                            }
                            sessionLocked.setVolume(f);
                        }
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in setVolume", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void tune(IBinder sessionToken, Uri channelUri, Bundle params, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "tune");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).tune(channelUri, params);
                    if (!TvContract.isChannelUriForPassthroughInput(channelUri)) {
                        try {
                            SessionState sessionState = (SessionState) TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).sessionStateMap.get(sessionToken);
                            if (sessionState.isRecordingSession) {
                                Binder.restoreCallingIdentity(identity);
                                return;
                            }
                            SomeArgs args = SomeArgs.obtain();
                            args.arg1 = sessionState.componentName.getPackageName();
                            args.arg2 = Long.valueOf(System.currentTimeMillis());
                            args.arg3 = Long.valueOf(ContentUris.parseId(channelUri));
                            args.arg4 = params;
                            args.arg5 = sessionToken;
                            TvInputManagerService.this.mWatchLogHandler.obtainMessage(1, args).sendToTarget();
                            Binder.restoreCallingIdentity(identity);
                        } catch (RemoteException | SessionNotFoundException e) {
                            Slog.e(TvInputManagerService.TAG, "error in tune", e);
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void unblockContent(IBinder sessionToken, String unblockedRating, int userId) {
            ensureParentalControlsPermission();
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "unblockContent");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).unblockContent(unblockedRating);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in unblockContent", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void setCaptionEnabled(IBinder sessionToken, boolean enabled, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "setCaptionEnabled");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).setCaptionEnabled(enabled);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in setCaptionEnabled", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void selectTrack(IBinder sessionToken, int type, String trackId, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "selectTrack");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).selectTrack(type, trackId);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in selectTrack", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void sendAppPrivateCommand(IBinder sessionToken, String command, Bundle data, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "sendAppPrivateCommand");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).appPrivateCommand(command, data);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in appPrivateCommand", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void createOverlayView(IBinder sessionToken, IBinder windowToken, Rect frame, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "createOverlayView");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).createOverlayView(windowToken, frame);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in createOverlayView", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void relayoutOverlayView(IBinder sessionToken, Rect frame, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "relayoutOverlayView");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).relayoutOverlayView(frame);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in relayoutOverlayView", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void removeOverlayView(IBinder sessionToken, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "removeOverlayView");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).removeOverlayView();
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in removeOverlayView", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void timeShiftPlay(IBinder sessionToken, Uri recordedProgramUri, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "timeShiftPlay");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).timeShiftPlay(recordedProgramUri);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in timeShiftPlay", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void timeShiftPause(IBinder sessionToken, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "timeShiftPause");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).timeShiftPause();
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in timeShiftPause", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void timeShiftResume(IBinder sessionToken, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "timeShiftResume");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).timeShiftResume();
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in timeShiftResume", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void timeShiftSeekTo(IBinder sessionToken, long timeMs, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "timeShiftSeekTo");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).timeShiftSeekTo(timeMs);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in timeShiftSeekTo", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void timeShiftSetPlaybackParams(IBinder sessionToken, PlaybackParams params, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "timeShiftSetPlaybackParams");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).timeShiftSetPlaybackParams(params);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in timeShiftSetPlaybackParams", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void timeShiftEnablePositionTracking(IBinder sessionToken, boolean enable, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "timeShiftEnablePositionTracking");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).timeShiftEnablePositionTracking(enable);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in timeShiftEnablePositionTracking", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void startRecording(IBinder sessionToken, Uri programUri, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "startRecording");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).startRecording(programUri);
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in startRecording", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void stopRecording(IBinder sessionToken, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "stopRecording");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).stopRecording();
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in stopRecording", e);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public List<TvInputHardwareInfo> getHardwareList() throws RemoteException {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.TV_INPUT_HARDWARE") != 0) {
                return null;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return TvInputManagerService.this.mTvInputHardwareManager.getHardwareList();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public ITvInputHardware acquireTvInputHardware(int deviceId, ITvInputHardwareCallback callback, TvInputInfo info, int userId) throws RemoteException {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.TV_INPUT_HARDWARE") != 0) {
                return null;
            }
            long identity = Binder.clearCallingIdentity();
            int callingUid = Binder.getCallingUid();
            try {
                return TvInputManagerService.this.mTvInputHardwareManager.acquireHardware(deviceId, callback, info, callingUid, TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "acquireTvInputHardware"));
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void releaseTvInputHardware(int deviceId, ITvInputHardware hardware, int userId) throws RemoteException {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.TV_INPUT_HARDWARE") == 0) {
                long identity = Binder.clearCallingIdentity();
                int callingUid = Binder.getCallingUid();
                try {
                    TvInputManagerService.this.mTvInputHardwareManager.releaseHardware(deviceId, hardware, callingUid, TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "releaseTvInputHardware"));
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public List<DvbDeviceInfo> getDvbDeviceList() throws RemoteException {
            int i;
            List<DvbDeviceInfo> list;
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.DVB_DEVICE") == 0) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ArrayList<DvbDeviceInfo> deviceInfosFromPattern1 = new ArrayList<>();
                    File devDirectory = new File("/dev");
                    String[] list2 = devDirectory.list();
                    int length = list2.length;
                    boolean dvbDirectoryFound = false;
                    int i2 = 0;
                    while (true) {
                        i = 1;
                        if (i2 >= length) {
                            break;
                        }
                        String fileName = list2[i2];
                        Matcher matcher = TvInputManagerService.sFrontEndDevicePattern.matcher(fileName);
                        if (matcher.find()) {
                            deviceInfosFromPattern1.add(new DvbDeviceInfo(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
                        }
                        if (TextUtils.equals("dvb", fileName)) {
                            dvbDirectoryFound = true;
                        }
                        i2++;
                    }
                    if (!dvbDirectoryFound) {
                        return Collections.unmodifiableList(deviceInfosFromPattern1);
                    }
                    File dvbDirectory = new File(TvInputManagerService.DVB_DIRECTORY);
                    ArrayList<DvbDeviceInfo> deviceInfosFromPattern2 = new ArrayList<>();
                    String[] list3 = dvbDirectory.list();
                    int length2 = list3.length;
                    int i3 = 0;
                    while (i3 < length2) {
                        String fileNameInDvb = list3[i3];
                        Matcher adapterMatcher = TvInputManagerService.sAdapterDirPattern.matcher(fileNameInDvb);
                        if (adapterMatcher.find()) {
                            int adapterId = Integer.parseInt(adapterMatcher.group(i));
                            String[] list4 = new File("/dev/dvb/" + fileNameInDvb).list();
                            int length3 = list4.length;
                            int i4 = 0;
                            while (i4 < length3) {
                                Matcher frontendMatcher = TvInputManagerService.sFrontEndInAdapterDirPattern.matcher(list4[i4]);
                                if (frontendMatcher.find()) {
                                    deviceInfosFromPattern2.add(new DvbDeviceInfo(adapterId, Integer.parseInt(frontendMatcher.group(1))));
                                }
                                i4++;
                                devDirectory = devDirectory;
                                dvbDirectory = dvbDirectory;
                            }
                        }
                        i3++;
                        devDirectory = devDirectory;
                        dvbDirectory = dvbDirectory;
                        i = 1;
                    }
                    if (deviceInfosFromPattern2.isEmpty()) {
                        list = Collections.unmodifiableList(deviceInfosFromPattern1);
                    } else {
                        list = Collections.unmodifiableList(deviceInfosFromPattern2);
                    }
                    Binder.restoreCallingIdentity(identity);
                    return list;
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } else {
                throw new SecurityException("Requires DVB_DEVICE permission");
            }
        }

        /* JADX INFO: Multiple debug info for r2v9 int: [D('adapterDirectory' java.io.File), D('dvbDeviceFound' boolean)] */
        /* JADX INFO: Multiple debug info for r3v8 java.lang.String: [D('fileNameInAdapter' java.lang.String), D('devDirectory' java.io.File)] */
        public ParcelFileDescriptor openDvbDevice(DvbDeviceInfo info, int device) throws RemoteException {
            String deviceFileName;
            int i;
            String[] strArr;
            File devDirectory;
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.DVB_DEVICE") == 0) {
                File devDirectory2 = new File("/dev");
                String[] list = devDirectory2.list();
                int length = list.length;
                boolean dvbDeviceFound = false;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    }
                    if (TextUtils.equals("dvb", list[i2])) {
                        String[] list2 = new File(TvInputManagerService.DVB_DIRECTORY).list();
                        int length2 = list2.length;
                        boolean dvbDeviceFound2 = dvbDeviceFound;
                        int i3 = 0;
                        while (true) {
                            if (i3 >= length2) {
                                devDirectory = devDirectory2;
                                strArr = list;
                                dvbDeviceFound = dvbDeviceFound2;
                                break;
                            }
                            String fileNameInDvb = list2[i3];
                            if (TvInputManagerService.sAdapterDirPattern.matcher(fileNameInDvb).find()) {
                                String[] list3 = new File("/dev/dvb/" + fileNameInDvb).list();
                                int length3 = list3.length;
                                int i4 = 0;
                                while (true) {
                                    if (i4 >= length3) {
                                        devDirectory = devDirectory2;
                                        strArr = list;
                                        break;
                                    }
                                    devDirectory = devDirectory2;
                                    strArr = list;
                                    if (TvInputManagerService.sFrontEndInAdapterDirPattern.matcher(list3[i4]).find()) {
                                        dvbDeviceFound2 = true;
                                        break;
                                    }
                                    i4++;
                                    devDirectory2 = devDirectory;
                                    list = strArr;
                                }
                            } else {
                                devDirectory = devDirectory2;
                                strArr = list;
                            }
                            if (dvbDeviceFound2) {
                                dvbDeviceFound = dvbDeviceFound2;
                                break;
                            }
                            i3++;
                            devDirectory2 = devDirectory;
                            list = strArr;
                        }
                    } else {
                        devDirectory = devDirectory2;
                        strArr = list;
                    }
                    if (dvbDeviceFound) {
                        break;
                    }
                    i2++;
                    devDirectory2 = devDirectory;
                    list = strArr;
                }
                long identity = Binder.clearCallingIdentity();
                if (device == 0) {
                    deviceFileName = String.format(dvbDeviceFound ? "/dev/dvb/adapter%d/demux%d" : "/dev/dvb%d.demux%d", Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId()));
                } else if (device == 1) {
                    deviceFileName = String.format(dvbDeviceFound ? "/dev/dvb/adapter%d/dvr%d" : "/dev/dvb%d.dvr%d", Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId()));
                } else if (device == 2) {
                    try {
                        deviceFileName = String.format(dvbDeviceFound ? "/dev/dvb/adapter%d/frontend%d" : "/dev/dvb%d.frontend%d", Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId()));
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid DVB device: " + device);
                }
                try {
                    File file = new File(deviceFileName);
                    if (2 == device) {
                        i = 805306368;
                    } else {
                        i = 268435456;
                    }
                    ParcelFileDescriptor open = ParcelFileDescriptor.open(file, i);
                    Binder.restoreCallingIdentity(identity);
                    return open;
                } catch (FileNotFoundException e) {
                    Binder.restoreCallingIdentity(identity);
                    return null;
                }
            } else {
                throw new SecurityException("Requires DVB_DEVICE permission");
            }
        }

        public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId, int userId) throws RemoteException {
            ensureCaptureTvInputPermission();
            long identity = Binder.clearCallingIdentity();
            int callingUid = Binder.getCallingUid();
            try {
                return TvInputManagerService.this.mTvInputHardwareManager.getAvailableTvStreamConfigList(inputId, callingUid, TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "getAvailableTvStreamConfigList"));
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config, int userId) throws RemoteException {
            Throwable th;
            String hardwareInputId;
            ensureCaptureTvInputPermission();
            long identity = Binder.clearCallingIdentity();
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "captureFrame");
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                        if (userState.inputMap.get(inputId) == null) {
                            Slog.e(TvInputManagerService.TAG, "input not found for " + inputId);
                            return false;
                        }
                        Iterator it = userState.sessionStateMap.values().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                hardwareInputId = null;
                                break;
                            }
                            SessionState sessionState = (SessionState) it.next();
                            if (sessionState.inputId.equals(inputId) && sessionState.hardwareSessionToken != null) {
                                hardwareInputId = ((SessionState) userState.sessionStateMap.get(sessionState.hardwareSessionToken)).inputId;
                                break;
                            }
                        }
                        try {
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                boolean captureFrame = TvInputManagerService.this.mTvInputHardwareManager.captureFrame(hardwareInputId != null ? hardwareInputId : inputId, surface, config, callingUid, resolvedUserId);
                Binder.restoreCallingIdentity(identity);
                return captureFrame;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean isSingleSessionActive(int userId) throws RemoteException {
            ensureCaptureTvInputPermission();
            long identity = Binder.clearCallingIdentity();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "isSingleSessionActive");
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    if (userState.sessionStateMap.size() == 1) {
                        return true;
                    }
                    if (userState.sessionStateMap.size() == 2) {
                        SessionState[] sessionStates = (SessionState[]) userState.sessionStateMap.values().toArray(new SessionState[2]);
                        if (!(sessionStates[0].hardwareSessionToken == null && sessionStates[1].hardwareSessionToken == null)) {
                            Binder.restoreCallingIdentity(identity);
                            return true;
                        }
                    }
                    Binder.restoreCallingIdentity(identity);
                    return false;
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        private void ensureCaptureTvInputPermission() {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.CAPTURE_TV_INPUT") != 0) {
                throw new SecurityException("Requires CAPTURE_TV_INPUT permission");
            }
        }

        public void requestChannelBrowsable(Uri channelUri, int userId) throws RemoteException {
            String callingPackageName = getCallingPackageName();
            long identity = Binder.clearCallingIdentity();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "requestChannelBrowsable");
            try {
                Intent intent = new Intent("android.media.tv.action.CHANNEL_BROWSABLE_REQUESTED");
                List<ResolveInfo> list = TvInputManagerService.this.getContext().getPackageManager().queryBroadcastReceivers(intent, 0);
                if (list != null) {
                    for (ResolveInfo info : list) {
                        String receiverPackageName = info.activityInfo.packageName;
                        intent.putExtra("android.media.tv.extra.CHANNEL_ID", ContentUris.parseId(channelUri));
                        intent.putExtra("android.media.tv.extra.PACKAGE_NAME", callingPackageName);
                        intent.setPackage(receiverPackageName);
                        TvInputManagerService.this.getContext().sendBroadcastAsUser(intent, new UserHandle(resolvedUserId));
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
            if (DumpUtils.checkDumpPermission(TvInputManagerService.this.mContext, TvInputManagerService.TAG, pw)) {
                synchronized (TvInputManagerService.this.mLock) {
                    pw.println("User Ids (Current user: " + TvInputManagerService.this.mCurrentUserId + "):");
                    pw.increaseIndent();
                    for (int i = 0; i < TvInputManagerService.this.mUserStates.size(); i++) {
                        pw.println(Integer.valueOf(TvInputManagerService.this.mUserStates.keyAt(i)));
                    }
                    pw.decreaseIndent();
                    for (int i2 = 0; i2 < TvInputManagerService.this.mUserStates.size(); i2++) {
                        int userId = TvInputManagerService.this.mUserStates.keyAt(i2);
                        UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(userId);
                        pw.println("UserState (" + userId + "):");
                        pw.increaseIndent();
                        pw.println("inputMap: inputId -> TvInputState");
                        pw.increaseIndent();
                        for (Map.Entry<String, TvInputState> entry : userState.inputMap.entrySet()) {
                            pw.println(entry.getKey() + ": " + entry.getValue());
                        }
                        pw.decreaseIndent();
                        pw.println("packageSet:");
                        pw.increaseIndent();
                        for (String packageName : userState.packageSet) {
                            pw.println(packageName);
                        }
                        pw.decreaseIndent();
                        pw.println("clientStateMap: ITvInputClient -> ClientState");
                        pw.increaseIndent();
                        for (Map.Entry<IBinder, ClientState> entry2 : userState.clientStateMap.entrySet()) {
                            ClientState client = entry2.getValue();
                            pw.println(entry2.getKey() + ": " + client);
                            pw.increaseIndent();
                            pw.println("sessionTokens:");
                            pw.increaseIndent();
                            Iterator it = client.sessionTokens.iterator();
                            while (it.hasNext()) {
                                pw.println("" + ((IBinder) it.next()));
                            }
                            pw.decreaseIndent();
                            pw.println("clientTokens: " + client.clientToken);
                            pw.println("userId: " + client.userId);
                            pw.decreaseIndent();
                        }
                        pw.decreaseIndent();
                        pw.println("serviceStateMap: ComponentName -> ServiceState");
                        pw.increaseIndent();
                        for (Map.Entry<ComponentName, ServiceState> entry3 : userState.serviceStateMap.entrySet()) {
                            ServiceState service = entry3.getValue();
                            pw.println(entry3.getKey() + ": " + service);
                            pw.increaseIndent();
                            pw.println("sessionTokens:");
                            pw.increaseIndent();
                            Iterator it2 = service.sessionTokens.iterator();
                            while (it2.hasNext()) {
                                pw.println("" + ((IBinder) it2.next()));
                            }
                            pw.decreaseIndent();
                            pw.println("service: " + service.service);
                            pw.println("callback: " + service.callback);
                            pw.println("bound: " + service.bound);
                            pw.println("reconnecting: " + service.reconnecting);
                            pw.decreaseIndent();
                        }
                        pw.decreaseIndent();
                        pw.println("sessionStateMap: ITvInputSession -> SessionState");
                        pw.increaseIndent();
                        for (Map.Entry<IBinder, SessionState> entry4 : userState.sessionStateMap.entrySet()) {
                            SessionState session = entry4.getValue();
                            pw.println(entry4.getKey() + ": " + session);
                            pw.increaseIndent();
                            pw.println("inputId: " + session.inputId);
                            pw.println("client: " + session.client);
                            pw.println("seq: " + session.seq);
                            pw.println("callingUid: " + session.callingUid);
                            pw.println("userId: " + session.userId);
                            pw.println("sessionToken: " + session.sessionToken);
                            pw.println("session: " + session.session);
                            pw.println("logUri: " + session.logUri);
                            pw.println("hardwareSessionToken: " + session.hardwareSessionToken);
                            pw.decreaseIndent();
                        }
                        pw.decreaseIndent();
                        pw.println("callbackSet:");
                        pw.increaseIndent();
                        for (ITvInputManagerCallback callback : userState.callbackSet) {
                            pw.println(callback.toString());
                        }
                        pw.decreaseIndent();
                        pw.println("mainSessionToken: " + userState.mainSessionToken);
                        pw.decreaseIndent();
                    }
                }
                TvInputManagerService.this.mTvInputHardwareManager.dump(fd, writer, args);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class UserState {
        private final Set<ITvInputManagerCallback> callbackSet;
        private final Map<IBinder, ClientState> clientStateMap;
        private final List<TvContentRatingSystemInfo> contentRatingSystemList;
        private Map<String, TvInputState> inputMap;
        private IBinder mainSessionToken;
        private final Set<String> packageSet;
        private final PersistentDataStore persistentDataStore;
        private final Map<ComponentName, ServiceState> serviceStateMap;
        private final Map<IBinder, SessionState> sessionStateMap;

        private UserState(Context context, int userId) {
            this.inputMap = new HashMap();
            this.packageSet = new HashSet();
            this.contentRatingSystemList = new ArrayList();
            this.clientStateMap = new HashMap();
            this.serviceStateMap = new HashMap();
            this.sessionStateMap = new HashMap();
            this.callbackSet = new HashSet();
            this.mainSessionToken = null;
            this.persistentDataStore = new PersistentDataStore(context, userId);
        }
    }

    /* access modifiers changed from: private */
    public final class ClientState implements IBinder.DeathRecipient {
        private IBinder clientToken;
        private final List<IBinder> sessionTokens = new ArrayList();
        private final int userId;

        ClientState(IBinder clientToken2, int userId2) {
            this.clientToken = clientToken2;
            this.userId = userId2;
        }

        public boolean isEmpty() {
            return this.sessionTokens.isEmpty();
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (TvInputManagerService.this.mLock) {
                ClientState clientState = (ClientState) TvInputManagerService.this.getOrCreateUserStateLocked(this.userId).clientStateMap.get(this.clientToken);
                if (clientState != null) {
                    while (clientState.sessionTokens.size() > 0) {
                        TvInputManagerService.this.releaseSessionLocked(clientState.sessionTokens.get(0), 1000, this.userId);
                    }
                }
                this.clientToken = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceState {
        private boolean bound;
        private ServiceCallback callback;
        private final ComponentName component;
        private final ServiceConnection connection;
        private final Map<String, TvInputInfo> hardwareInputMap;
        private final boolean isHardware;
        private boolean reconnecting;
        private ITvInputService service;
        private final List<IBinder> sessionTokens;

        private ServiceState(ComponentName component2, int userId) {
            this.sessionTokens = new ArrayList();
            this.hardwareInputMap = new HashMap();
            this.component = component2;
            this.connection = new InputServiceConnection(component2, userId);
            this.isHardware = TvInputManagerService.hasHardwarePermission(TvInputManagerService.this.mContext.getPackageManager(), component2);
        }
    }

    /* access modifiers changed from: private */
    public static final class TvInputState {
        private TvInputInfo info;
        private int state;

        private TvInputState() {
            this.state = 0;
        }

        public String toString() {
            return "info: " + this.info + "; state: " + this.state;
        }
    }

    /* access modifiers changed from: private */
    public final class SessionState implements IBinder.DeathRecipient {
        private final int callingUid;
        private final ITvInputClient client;
        private final ComponentName componentName;
        private IBinder hardwareSessionToken;
        private final String inputId;
        private final boolean isRecordingSession;
        private Uri logUri;
        private final int seq;
        private ITvInputSession session;
        private final IBinder sessionToken;
        private final int userId;

        private SessionState(IBinder sessionToken2, String inputId2, ComponentName componentName2, boolean isRecordingSession2, ITvInputClient client2, int seq2, int callingUid2, int userId2) {
            this.sessionToken = sessionToken2;
            this.inputId = inputId2;
            this.componentName = componentName2;
            this.isRecordingSession = isRecordingSession2;
            this.client = client2;
            this.seq = seq2;
            this.callingUid = callingUid2;
            this.userId = userId2;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (TvInputManagerService.this.mLock) {
                this.session = null;
                TvInputManagerService.this.clearSessionAndNotifyClientLocked(this);
            }
        }
    }

    private final class InputServiceConnection implements ServiceConnection {
        private final ComponentName mComponent;
        private final int mUserId;

        private InputServiceConnection(ComponentName component, int userId) {
            this.mComponent = component;
            this.mUserId = userId;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName component, IBinder service) {
            synchronized (TvInputManagerService.this.mLock) {
                UserState userState = (UserState) TvInputManagerService.this.mUserStates.get(this.mUserId);
                if (userState == null) {
                    TvInputManagerService.this.mContext.unbindService(this);
                    return;
                }
                ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(this.mComponent);
                serviceState.service = ITvInputService.Stub.asInterface(service);
                if (serviceState.isHardware && serviceState.callback == null) {
                    serviceState.callback = new ServiceCallback(this.mComponent, this.mUserId);
                    try {
                        serviceState.service.registerCallback(serviceState.callback);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in registerCallback", e);
                    }
                }
                List<IBinder> tokensToBeRemoved = new ArrayList<>();
                for (IBinder sessionToken : serviceState.sessionTokens) {
                    if (!TvInputManagerService.this.createSessionInternalLocked(serviceState.service, sessionToken, this.mUserId)) {
                        tokensToBeRemoved.add(sessionToken);
                    }
                }
                for (IBinder sessionToken2 : tokensToBeRemoved) {
                    TvInputManagerService.this.removeSessionStateLocked(sessionToken2, this.mUserId);
                }
                for (TvInputState inputState : userState.inputMap.values()) {
                    if (inputState.info.getComponent().equals(component) && inputState.state != 0) {
                        TvInputManagerService.this.notifyInputStateChangedLocked(userState, inputState.info.getId(), inputState.state, null);
                    }
                }
                if (serviceState.isHardware) {
                    serviceState.hardwareInputMap.clear();
                    for (TvInputHardwareInfo hardware : TvInputManagerService.this.mTvInputHardwareManager.getHardwareList()) {
                        try {
                            serviceState.service.notifyHardwareAdded(hardware);
                        } catch (RemoteException e2) {
                            Slog.e(TvInputManagerService.TAG, "error in notifyHardwareAdded", e2);
                        }
                    }
                    for (HdmiDeviceInfo device : TvInputManagerService.this.mTvInputHardwareManager.getHdmiDeviceList()) {
                        try {
                            serviceState.service.notifyHdmiDeviceAdded(device);
                        } catch (RemoteException e3) {
                            Slog.e(TvInputManagerService.TAG, "error in notifyHdmiDeviceAdded", e3);
                        }
                    }
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName component) {
            if (this.mComponent.equals(component)) {
                synchronized (TvInputManagerService.this.mLock) {
                    ServiceState serviceState = (ServiceState) TvInputManagerService.this.getOrCreateUserStateLocked(this.mUserId).serviceStateMap.get(this.mComponent);
                    if (serviceState != null) {
                        serviceState.reconnecting = true;
                        serviceState.bound = false;
                        serviceState.service = null;
                        serviceState.callback = null;
                        TvInputManagerService.this.abortPendingCreateSessionRequestsLocked(serviceState, null, this.mUserId);
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Mismatched ComponentName: " + this.mComponent + " (expected), " + component + " (actual).");
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceCallback extends ITvInputServiceCallback.Stub {
        private final ComponentName mComponent;
        private final int mUserId;

        ServiceCallback(ComponentName component, int userId) {
            this.mComponent = component;
            this.mUserId = userId;
        }

        private void ensureHardwarePermission() {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.TV_INPUT_HARDWARE") != 0) {
                throw new SecurityException("The caller does not have hardware permission");
            }
        }

        private void ensureValidInput(TvInputInfo inputInfo) {
            if (inputInfo.getId() == null || !this.mComponent.equals(inputInfo.getComponent())) {
                throw new IllegalArgumentException("Invalid TvInputInfo");
            }
        }

        private void addHardwareInputLocked(TvInputInfo inputInfo) {
            TvInputManagerService.this.getServiceStateLocked(this.mComponent, this.mUserId).hardwareInputMap.put(inputInfo.getId(), inputInfo);
            TvInputManagerService.this.buildTvInputListLocked(this.mUserId, null);
        }

        public void addHardwareInput(int deviceId, TvInputInfo inputInfo) {
            ensureHardwarePermission();
            ensureValidInput(inputInfo);
            synchronized (TvInputManagerService.this.mLock) {
                TvInputManagerService.this.mTvInputHardwareManager.addHardwareInput(deviceId, inputInfo);
                addHardwareInputLocked(inputInfo);
            }
        }

        public void addHdmiInput(int id, TvInputInfo inputInfo) {
            ensureHardwarePermission();
            ensureValidInput(inputInfo);
            synchronized (TvInputManagerService.this.mLock) {
                TvInputManagerService.this.mTvInputHardwareManager.addHdmiInput(id, inputInfo);
                addHardwareInputLocked(inputInfo);
            }
        }

        public void removeHardwareInput(String inputId) {
            ensureHardwarePermission();
            synchronized (TvInputManagerService.this.mLock) {
                if (TvInputManagerService.this.getServiceStateLocked(this.mComponent, this.mUserId).hardwareInputMap.remove(inputId) != null) {
                    TvInputManagerService.this.buildTvInputListLocked(this.mUserId, null);
                    TvInputManagerService.this.mTvInputHardwareManager.removeHardwareInput(inputId);
                } else {
                    Slog.e(TvInputManagerService.TAG, "failed to remove input " + inputId);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class SessionCallback extends ITvInputSessionCallback.Stub {
        private final InputChannel[] mChannels;
        private final SessionState mSessionState;

        SessionCallback(SessionState sessionState, InputChannel[] channels) {
            this.mSessionState = sessionState;
            this.mChannels = channels;
        }

        public void onSessionCreated(ITvInputSession session, IBinder hardwareSessionToken) {
            synchronized (TvInputManagerService.this.mLock) {
                this.mSessionState.session = session;
                this.mSessionState.hardwareSessionToken = hardwareSessionToken;
                if (session == null || !addSessionTokenToClientStateLocked(session)) {
                    TvInputManagerService.this.removeSessionStateLocked(this.mSessionState.sessionToken, this.mSessionState.userId);
                    TvInputManagerService.this.sendSessionTokenToClientLocked(this.mSessionState.client, this.mSessionState.inputId, null, null, this.mSessionState.seq);
                } else {
                    TvInputManagerService.this.sendSessionTokenToClientLocked(this.mSessionState.client, this.mSessionState.inputId, this.mSessionState.sessionToken, this.mChannels[0], this.mSessionState.seq);
                }
                this.mChannels[0].dispose();
            }
        }

        private boolean addSessionTokenToClientStateLocked(ITvInputSession session) {
            try {
                session.asBinder().linkToDeath(this.mSessionState, 0);
                IBinder clientToken = this.mSessionState.client.asBinder();
                UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(this.mSessionState.userId);
                ClientState clientState = (ClientState) userState.clientStateMap.get(clientToken);
                if (clientState == null) {
                    clientState = new ClientState(clientToken, this.mSessionState.userId);
                    try {
                        clientToken.linkToDeath(clientState, 0);
                        userState.clientStateMap.put(clientToken, clientState);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "client process has already died", e);
                        return false;
                    }
                }
                clientState.sessionTokens.add(this.mSessionState.sessionToken);
                return true;
            } catch (RemoteException e2) {
                Slog.e(TvInputManagerService.TAG, "session process has already died", e2);
                return false;
            }
        }

        public void onChannelRetuned(Uri channelUri) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onChannelRetuned(channelUri, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onChannelRetuned", e);
                    }
                }
            }
        }

        public void onTracksChanged(List<TvTrackInfo> tracks) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onTracksChanged(tracks, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onTracksChanged", e);
                    }
                }
            }
        }

        public void onTrackSelected(int type, String trackId) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onTrackSelected(type, trackId, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onTrackSelected", e);
                    }
                }
            }
        }

        public void onVideoAvailable() {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onVideoAvailable(this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onVideoAvailable", e);
                    }
                }
            }
        }

        public void onVideoUnavailable(int reason) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onVideoUnavailable(reason, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onVideoUnavailable", e);
                    }
                }
            }
        }

        public void onContentAllowed() {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onContentAllowed(this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onContentAllowed", e);
                    }
                }
            }
        }

        public void onContentBlocked(String rating) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onContentBlocked(rating, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onContentBlocked", e);
                    }
                }
            }
        }

        public void onLayoutSurface(int left, int top, int right, int bottom) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onLayoutSurface(left, top, right, bottom, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onLayoutSurface", e);
                    }
                }
            }
        }

        public void onSessionEvent(String eventType, Bundle eventArgs) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onSessionEvent(eventType, eventArgs, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onSessionEvent", e);
                    }
                }
            }
        }

        public void onTimeShiftStatusChanged(int status) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onTimeShiftStatusChanged(status, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onTimeShiftStatusChanged", e);
                    }
                }
            }
        }

        public void onTimeShiftStartPositionChanged(long timeMs) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onTimeShiftStartPositionChanged(timeMs, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onTimeShiftStartPositionChanged", e);
                    }
                }
            }
        }

        public void onTimeShiftCurrentPositionChanged(long timeMs) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onTimeShiftCurrentPositionChanged(timeMs, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onTimeShiftCurrentPositionChanged", e);
                    }
                }
            }
        }

        public void onTuned(Uri channelUri) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onTuned(this.mSessionState.seq, channelUri);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onTuned", e);
                    }
                }
            }
        }

        public void onRecordingStopped(Uri recordedProgramUri) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onRecordingStopped(recordedProgramUri, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onRecordingStopped", e);
                    }
                }
            }
        }

        public void onError(int error) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session != null && this.mSessionState.client != null) {
                    try {
                        this.mSessionState.client.onError(error, this.mSessionState.seq);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in onError", e);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class WatchLogHandler extends Handler {
        static final int MSG_LOG_WATCH_END = 2;
        static final int MSG_LOG_WATCH_START = 1;
        static final int MSG_SWITCH_CONTENT_RESOLVER = 3;
        private ContentResolver mContentResolver;

        WatchLogHandler(ContentResolver contentResolver, Looper looper) {
            super(looper);
            this.mContentResolver = contentResolver;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                SomeArgs args = (SomeArgs) msg.obj;
                long watchStartTime = ((Long) args.arg2).longValue();
                long channelId = ((Long) args.arg3).longValue();
                Bundle tuneParams = (Bundle) args.arg4;
                IBinder sessionToken = (IBinder) args.arg5;
                ContentValues values = new ContentValues();
                values.put("package_name", (String) args.arg1);
                values.put("watch_start_time_utc_millis", Long.valueOf(watchStartTime));
                values.put("channel_id", Long.valueOf(channelId));
                if (tuneParams != null) {
                    values.put("tune_params", encodeTuneParams(tuneParams));
                }
                values.put("session_token", sessionToken.toString());
                this.mContentResolver.insert(TvContract.WatchedPrograms.CONTENT_URI, values);
                args.recycle();
            } else if (i == 2) {
                SomeArgs args2 = (SomeArgs) msg.obj;
                long watchEndTime = ((Long) args2.arg2).longValue();
                ContentValues values2 = new ContentValues();
                values2.put("watch_end_time_utc_millis", Long.valueOf(watchEndTime));
                values2.put("session_token", ((IBinder) args2.arg1).toString());
                this.mContentResolver.insert(TvContract.WatchedPrograms.CONTENT_URI, values2);
                args2.recycle();
            } else if (i != 3) {
                Slog.w(TvInputManagerService.TAG, "unhandled message code: " + msg.what);
            } else {
                this.mContentResolver = (ContentResolver) msg.obj;
            }
        }

        private String encodeTuneParams(Bundle tuneParams) {
            StringBuilder builder = new StringBuilder();
            Iterator<String> it = tuneParams.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Object value = tuneParams.get(key);
                if (value != null) {
                    builder.append(replaceEscapeCharacters(key));
                    builder.append("=");
                    builder.append(replaceEscapeCharacters(value.toString()));
                    if (it.hasNext()) {
                        builder.append(", ");
                    }
                }
            }
            return builder.toString();
        }

        private String replaceEscapeCharacters(String src) {
            StringBuilder builder = new StringBuilder();
            char[] charArray = src.toCharArray();
            for (char ch : charArray) {
                if ("%=,".indexOf(ch) >= 0) {
                    builder.append('%');
                }
                builder.append(ch);
            }
            return builder.toString();
        }
    }

    private final class HardwareListener implements TvInputHardwareManager.Listener {
        private HardwareListener() {
        }

        @Override // com.android.server.tv.TvInputHardwareManager.Listener
        public void onStateChanged(String inputId, int state) {
            synchronized (TvInputManagerService.this.mLock) {
                TvInputManagerService.this.setStateLocked(inputId, state, TvInputManagerService.this.mCurrentUserId);
            }
        }

        @Override // com.android.server.tv.TvInputHardwareManager.Listener
        public void onHardwareDeviceAdded(TvInputHardwareInfo info) {
            synchronized (TvInputManagerService.this.mLock) {
                for (ServiceState serviceState : TvInputManagerService.this.getOrCreateUserStateLocked(TvInputManagerService.this.mCurrentUserId).serviceStateMap.values()) {
                    if (serviceState.isHardware && serviceState.service != null) {
                        try {
                            serviceState.service.notifyHardwareAdded(info);
                        } catch (RemoteException e) {
                            Slog.e(TvInputManagerService.TAG, "error in notifyHardwareAdded", e);
                        }
                    }
                }
            }
        }

        @Override // com.android.server.tv.TvInputHardwareManager.Listener
        public void onHardwareDeviceRemoved(TvInputHardwareInfo info) {
            synchronized (TvInputManagerService.this.mLock) {
                for (ServiceState serviceState : TvInputManagerService.this.getOrCreateUserStateLocked(TvInputManagerService.this.mCurrentUserId).serviceStateMap.values()) {
                    if (serviceState.isHardware && serviceState.service != null) {
                        try {
                            serviceState.service.notifyHardwareRemoved(info);
                        } catch (RemoteException e) {
                            Slog.e(TvInputManagerService.TAG, "error in notifyHardwareRemoved", e);
                        }
                    }
                }
            }
        }

        @Override // com.android.server.tv.TvInputHardwareManager.Listener
        public void onHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) {
            synchronized (TvInputManagerService.this.mLock) {
                for (ServiceState serviceState : TvInputManagerService.this.getOrCreateUserStateLocked(TvInputManagerService.this.mCurrentUserId).serviceStateMap.values()) {
                    if (serviceState.isHardware && serviceState.service != null) {
                        try {
                            serviceState.service.notifyHdmiDeviceAdded(deviceInfo);
                        } catch (RemoteException e) {
                            Slog.e(TvInputManagerService.TAG, "error in notifyHdmiDeviceAdded", e);
                        }
                    }
                }
            }
        }

        @Override // com.android.server.tv.TvInputHardwareManager.Listener
        public void onHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) {
            synchronized (TvInputManagerService.this.mLock) {
                for (ServiceState serviceState : TvInputManagerService.this.getOrCreateUserStateLocked(TvInputManagerService.this.mCurrentUserId).serviceStateMap.values()) {
                    if (serviceState.isHardware && serviceState.service != null) {
                        try {
                            serviceState.service.notifyHdmiDeviceRemoved(deviceInfo);
                        } catch (RemoteException e) {
                            Slog.e(TvInputManagerService.TAG, "error in notifyHdmiDeviceRemoved", e);
                        }
                    }
                }
            }
        }

        @Override // com.android.server.tv.TvInputHardwareManager.Listener
        public void onHdmiDeviceUpdated(String inputId, HdmiDeviceInfo deviceInfo) {
            Integer state;
            synchronized (TvInputManagerService.this.mLock) {
                int devicePowerStatus = deviceInfo.getDevicePowerStatus();
                if (devicePowerStatus == 0) {
                    state = 0;
                } else if (devicePowerStatus == 1 || devicePowerStatus == 2 || devicePowerStatus == 3) {
                    state = 1;
                } else {
                    state = null;
                }
                if (state != null) {
                    TvInputManagerService.this.setStateLocked(inputId, state.intValue(), TvInputManagerService.this.mCurrentUserId);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class SessionNotFoundException extends IllegalArgumentException {
        public SessionNotFoundException(String name) {
            super(name);
        }
    }
}
