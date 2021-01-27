package com.android.server.media;

import android.app.ActivityManager;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.media.AudioManagerInternal;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IRemoteVolumeController;
import android.media.MediaController2;
import android.media.Session2CommandGroup;
import android.media.Session2Token;
import android.media.session.IActiveSessionsListener;
import android.media.session.ICallback;
import android.media.session.IOnMediaKeyListener;
import android.media.session.IOnVolumeKeyLongPressListener;
import android.media.session.ISession;
import android.media.session.ISession2TokensListener;
import android.media.session.ISessionCallback;
import android.media.session.ISessionManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.media.AudioPlayerStateMonitor;
import com.android.server.media.MediaSessionStack;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MediaSessionService extends SystemService implements Watchdog.Monitor {
    static final boolean DEBUG = true;
    private static final boolean DEBUG_KEY_EVENT = true;
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    private static final int IAWARE_APP_TYPE_MUSIC = 7;
    private static final int IAWARE_TRANSACTION_GETAPPTYPEINFO = 10;
    private static final int MEDIA_KEY_LISTENER_TIMEOUT = 1000;
    private static final int NO_ERROR = 0;
    private static final String TAG = "MediaSessionService";
    private static final int WAKELOCK_TIMEOUT = 5000;
    private ActivityManager mActivityManager;
    private AudioManagerInternal mAudioManagerInternal;
    private AudioPlayerStateMonitor mAudioPlayerStateMonitor;
    private IAudioService mAudioService;
    private ContentResolver mContentResolver;
    private final Context mContext;
    private FullUserRecord mCurrentFullUserRecord;
    @GuardedBy({"mLock"})
    private final SparseIntArray mFullUserIds = new SparseIntArray();
    private MediaSessionRecord mGlobalPrioritySession;
    private final MessageHandler mHandler = new MessageHandler();
    private boolean mHasFeatureLeanback;
    private IBinder mIAwareCMSService;
    private KeyguardManager mKeyguardManager;
    private final Object mLock = new Object();
    private final int mLongPressTimeout;
    private final PowerManager.WakeLock mMediaEventWakeLock;
    private final INotificationManager mNotificationManager;
    @GuardedBy({"mLock"})
    final RemoteCallbackList<IRemoteVolumeController> mRemoteVolumeControllers = new RemoteCallbackList<>();
    @GuardedBy({"mLock"})
    private final List<Session2TokensListenerRecord> mSession2TokensListenerRecords = new ArrayList();
    @GuardedBy({"mLock"})
    private final SparseArray<List<Session2Token>> mSession2TokensPerUser = new SparseArray<>();
    private final SessionManagerImpl mSessionManagerImpl;
    @GuardedBy({"mLock"})
    private final ArrayList<SessionsListenerRecord> mSessionsListeners = new ArrayList<>();
    private SettingsObserver mSettingsObserver;
    @GuardedBy({"mLock"})
    private final SparseArray<FullUserRecord> mUserRecords = new SparseArray<>();

    public MediaSessionService(Context context) {
        super(context);
        this.mContext = context;
        this.mSessionManagerImpl = new SessionManagerImpl();
        this.mMediaEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "handleMediaEvent");
        this.mLongPressTimeout = ViewConfiguration.getLongPressTimeout();
        this.mNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.media.MediaSessionService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.media.MediaSessionService$SessionManagerImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("media_session", this.mSessionManagerImpl);
        Watchdog.getInstance().addMonitor(this);
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mAudioService = getAudioService();
        this.mAudioManagerInternal = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mAudioPlayerStateMonitor = AudioPlayerStateMonitor.getInstance(this.mContext);
        this.mAudioPlayerStateMonitor.registerListener(new AudioPlayerStateMonitor.OnAudioPlayerActiveStateChangedListener() {
            /* class com.android.server.media.$$Lambda$MediaSessionService$za_9dlUSlnaiZw6eCdPVEZq0XLw */

            @Override // com.android.server.media.AudioPlayerStateMonitor.OnAudioPlayerActiveStateChangedListener
            public final void onAudioPlayerActiveStateChanged(AudioPlaybackConfiguration audioPlaybackConfiguration, boolean z) {
                MediaSessionService.this.lambda$onStart$0$MediaSessionService(audioPlaybackConfiguration, z);
            }
        }, null);
        this.mContentResolver = this.mContext.getContentResolver();
        this.mSettingsObserver = new SettingsObserver();
        this.mSettingsObserver.observe();
        this.mHasFeatureLeanback = this.mContext.getPackageManager().hasSystemFeature("android.software.leanback");
        updateUser();
    }

    public /* synthetic */ void lambda$onStart$0$MediaSessionService(AudioPlaybackConfiguration config, boolean isRemoved) {
        if (config.getPlayerType() != 3) {
            synchronized (this.mLock) {
                FullUserRecord user = getFullUserRecordLocked(UserHandle.getUserId(config.getClientUid()));
                if (user != null) {
                    user.mPriorityStack.updateMediaButtonSessionIfNeeded();
                }
            }
        }
    }

    private IAudioService getAudioService() {
        return IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGlobalPriorityActiveLocked() {
        MediaSessionRecord mediaSessionRecord = this.mGlobalPrioritySession;
        return mediaSessionRecord != null && mediaSessionRecord.isActive();
    }

    /* access modifiers changed from: package-private */
    public void updateSession(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user == null) {
                Log.w(TAG, "Unknown session updated. Ignoring.");
                return;
            }
            if ((record.getFlags() & 65536) != 0) {
                Log.i(TAG, "Global priority session is updated, active=" + record.isActive());
                user.pushAddressedPlayerChangedLocked();
            } else if (!user.mPriorityStack.contains(record)) {
                Log.w(TAG, "Unknown session updated. Ignoring.");
                return;
            } else {
                user.mPriorityStack.onSessionStateChange(record);
            }
            this.mHandler.postSessionsChanged(record.getUserId());
        }
    }

    /* access modifiers changed from: package-private */
    public void setGlobalPrioritySession(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (this.mGlobalPrioritySession != record) {
                Log.i(TAG, "Global priority session is changed from " + this.mGlobalPrioritySession + " to " + record);
                this.mGlobalPrioritySession = record;
                if (user != null && user.mPriorityStack.contains(record)) {
                    user.mPriorityStack.removeSession(record);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<MediaSessionRecord> getActiveSessionsLocked(int userId) {
        List<MediaSessionRecord> records = new ArrayList<>();
        if (userId == -1) {
            int size = this.mUserRecords.size();
            for (int i = 0; i < size; i++) {
                records.addAll(this.mUserRecords.valueAt(i).mPriorityStack.getActiveSessions(userId));
            }
        } else {
            FullUserRecord user = getFullUserRecordLocked(userId);
            if (user == null) {
                Log.w(TAG, "getSessions failed. Unknown user " + userId);
                return records;
            }
            records.addAll(user.mPriorityStack.getActiveSessions(userId));
        }
        if (isGlobalPriorityActiveLocked() && (userId == -1 || userId == this.mGlobalPrioritySession.getUserId())) {
            if (records.contains(this.mGlobalPrioritySession)) {
                records.remove(this.mGlobalPrioritySession);
            }
            records.add(0, this.mGlobalPrioritySession);
        }
        return records;
    }

    /* access modifiers changed from: package-private */
    public List<Session2Token> getSession2TokensLocked(int userId) {
        List<Session2Token> list = new ArrayList<>();
        if (userId == -1) {
            for (int i = 0; i < this.mSession2TokensPerUser.size(); i++) {
                list.addAll(this.mSession2TokensPerUser.valueAt(i));
            }
        } else {
            list.addAll(this.mSession2TokensPerUser.get(userId));
        }
        return list;
    }

    public void notifyRemoteVolumeChanged(int flags, MediaSessionRecord session) {
        if (session.isActive()) {
            synchronized (this.mLock) {
                int size = this.mRemoteVolumeControllers.beginBroadcast();
                MediaSession.Token token = session.getSessionToken();
                for (int i = size - 1; i >= 0; i--) {
                    try {
                        this.mRemoteVolumeControllers.getBroadcastItem(i).remoteVolumeChanged(token, flags);
                    } catch (Exception e) {
                        Log.w(TAG, "Error sending volume change.", e);
                    }
                }
                this.mRemoteVolumeControllers.finishBroadcast();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onSessionPlaystateChanged(MediaSessionRecord record, int oldState, int newState) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user != null) {
                if (user.mPriorityStack.contains(record)) {
                    user.mPriorityStack.onPlaystateChanged(record, oldState, newState);
                    return;
                }
            }
            Log.i(TAG, "Unknown session changed playback state. Ignoring.");
        }
    }

    /* access modifiers changed from: package-private */
    public void onSessionPlaybackTypeChanged(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user != null) {
                if (user.mPriorityStack.contains(record)) {
                    pushRemoteVolumeUpdateLocked(record.getUserId());
                    return;
                }
            }
            Log.i(TAG, "Unknown session changed playback type. Ignoring.");
        }
    }

    @Override // com.android.server.SystemService
    public void onStartUser(int userId) {
        Log.i(TAG, "onStartUser: " + userId);
        updateUser();
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userId) {
        Log.i(TAG, "onSwitchUser: " + userId);
        updateUser();
    }

    @Override // com.android.server.SystemService
    public void onStopUser(int userId) {
        Log.i(TAG, "onStopUser: " + userId);
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(userId);
            if (user != null) {
                if (user.mFullUserId == userId) {
                    user.destroySessionsForUserLocked(-1);
                    this.mUserRecords.remove(userId);
                } else {
                    user.destroySessionsForUserLocked(userId);
                }
            }
            this.mSession2TokensPerUser.remove(userId);
            updateUser();
        }
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    /* access modifiers changed from: protected */
    public void enforcePhoneStatePermission(int pid, int uid) {
        if (this.mContext.checkPermission("android.permission.MODIFY_PHONE_STATE", pid, uid) != 0) {
            throw new SecurityException("Must hold the MODIFY_PHONE_STATE permission.");
        }
    }

    /* access modifiers changed from: package-private */
    public void sessionDied(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    /* access modifiers changed from: package-private */
    public void destroySession(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    private void updateUser() {
        synchronized (this.mLock) {
            this.mFullUserIds.clear();
            List<UserInfo> allUsers = ((UserManager) this.mContext.getSystemService("user")).getUsers();
            if (allUsers != null) {
                for (UserInfo userInfo : allUsers) {
                    if (userInfo.isManagedProfile()) {
                        this.mFullUserIds.put(userInfo.id, userInfo.profileGroupId);
                    } else {
                        this.mFullUserIds.put(userInfo.id, userInfo.id);
                        if (this.mUserRecords.get(userInfo.id) == null) {
                            this.mUserRecords.put(userInfo.id, new FullUserRecord(userInfo.id));
                        }
                    }
                    if (this.mSession2TokensPerUser.get(userInfo.id) == null) {
                        this.mSession2TokensPerUser.put(userInfo.id, new ArrayList());
                    }
                }
            }
            int currentFullUserId = ActivityManager.getCurrentUser();
            this.mCurrentFullUserRecord = this.mUserRecords.get(currentFullUserId);
            if (this.mCurrentFullUserRecord == null) {
                Log.w(TAG, "Cannot find FullUserInfo for the current user " + currentFullUserId);
                this.mCurrentFullUserRecord = new FullUserRecord(currentFullUserId);
                this.mUserRecords.put(currentFullUserId, this.mCurrentFullUserRecord);
                if (this.mSession2TokensPerUser.get(currentFullUserId) == null) {
                    this.mSession2TokensPerUser.put(currentFullUserId, new ArrayList());
                }
            }
            this.mFullUserIds.put(currentFullUserId, currentFullUserId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateActiveSessionListeners() {
        synchronized (this.mLock) {
            for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                SessionsListenerRecord listener = this.mSessionsListeners.get(i);
                try {
                    enforceMediaPermissions(listener.componentName, listener.pid, listener.uid, listener.userId);
                } catch (SecurityException e) {
                    Log.i(TAG, "ActiveSessionsListener " + listener.componentName + " is no longer authorized. Disconnecting.");
                    this.mSessionsListeners.remove(i);
                    try {
                        listener.listener.onActiveSessionsChanged(new ArrayList());
                    } catch (Exception e2) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destroySessionLocked(MediaSessionRecord session) {
        Log.i(TAG, "Destroying " + session);
        FullUserRecord user = getFullUserRecordLocked(session.getUserId());
        if (this.mGlobalPrioritySession == session) {
            this.mGlobalPrioritySession = null;
            if (session.isActive() && user != null) {
                user.pushAddressedPlayerChangedLocked();
            }
        } else if (user != null) {
            user.mPriorityStack.removeSession(session);
        }
        try {
            session.getCallback().asBinder().unlinkToDeath(session, 0);
        } catch (Exception e) {
        }
        session.onDestroy();
        this.mHandler.postSessionsChanged(session.getUserId());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforcePackageName(String packageName, int uid) {
        if (!TextUtils.isEmpty(packageName)) {
            for (String str : this.mContext.getPackageManager().getPackagesForUid(uid)) {
                if (packageName.equals(str)) {
                    return;
                }
            }
            throw new IllegalArgumentException("packageName is not owned by the calling process");
        }
        throw new IllegalArgumentException("packageName may not be empty");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceMediaPermissions(ComponentName compName, int pid, int uid, int resolvedUserId) {
        if (!hasStatusBarServicePermission(pid, uid) && this.mContext.checkPermission("android.permission.MEDIA_CONTENT_CONTROL", pid, uid) != 0 && !isEnabledNotificationListener(compName, UserHandle.getUserId(uid), resolvedUserId)) {
            throw new SecurityException("Missing permission to control media.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasStatusBarServicePermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.STATUS_BAR_SERVICE", pid, uid) == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enforceStatusBarServicePermission(String action, int pid, int uid) {
        if (!hasStatusBarServicePermission(pid, uid)) {
            throw new SecurityException("Only System UI and Settings may " + action);
        }
    }

    private boolean isEnabledNotificationListener(ComponentName compName, int userId, int forUserId) {
        if (userId != forUserId) {
            return false;
        }
        Log.i(TAG, "Checking if enabled notification listener " + compName);
        if (compName != null) {
            try {
                return this.mNotificationManager.isNotificationListenerAccessGrantedForUser(compName, userId);
            } catch (RemoteException e) {
                Log.w(TAG, "Dead NotificationManager in isEnabledNotificationListener", e);
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MediaSessionRecord createSessionInternal(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag, Bundle sessionInfo) throws RemoteException {
        MediaSessionRecord createSessionLocked;
        synchronized (this.mLock) {
            createSessionLocked = createSessionLocked(callerPid, callerUid, userId, callerPackageName, cb, tag, sessionInfo);
        }
        return createSessionLocked;
    }

    private MediaSessionRecord createSessionLocked(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag, Bundle sessionInfo) {
        FullUserRecord user = getFullUserRecordLocked(userId);
        if (user != null) {
            MediaSessionRecord session = new MediaSessionRecord(callerPid, callerUid, userId, callerPackageName, cb, tag, sessionInfo, this, this.mHandler.getLooper());
            try {
                cb.asBinder().linkToDeath(session, 0);
                user.mPriorityStack.addSession(session);
                this.mHandler.postSessionsChanged(userId);
                Log.i(TAG, "Created session for " + callerPackageName + " with tag " + tag);
                return session;
            } catch (RemoteException e) {
                throw new RuntimeException("Media Session owner died prematurely.", e);
            }
        } else {
            Log.w(TAG, "Request from invalid user: " + userId + ", pkg=" + callerPackageName);
            throw new RuntimeException("Session request from invalid user.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findIndexOfSessionsListenerLocked(IActiveSessionsListener listener) {
        for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
            if (this.mSessionsListeners.get(i).listener.asBinder() == listener.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int findIndexOfSession2TokensListenerLocked(ISession2TokensListener listener) {
        for (int i = this.mSession2TokensListenerRecords.size() - 1; i >= 0; i--) {
            if (this.mSession2TokensListenerRecords.get(i).listener.asBinder() == listener.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushSessionsChanged(int userId) {
        synchronized (this.mLock) {
            if (getFullUserRecordLocked(userId) == null) {
                Log.w(TAG, "pushSessionsChanged failed. No user with id=" + userId);
                return;
            }
            List<MediaSessionRecord> records = getActiveSessionsLocked(userId);
            int size = records.size();
            ArrayList<MediaSession.Token> tokens = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                tokens.add(records.get(i).getSessionToken());
            }
            pushRemoteVolumeUpdateLocked(userId);
            for (int i2 = this.mSessionsListeners.size() - 1; i2 >= 0; i2--) {
                SessionsListenerRecord record = this.mSessionsListeners.get(i2);
                if (record.userId == -1 || record.userId == userId) {
                    try {
                        record.listener.onActiveSessionsChanged(tokens);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Dead ActiveSessionsListener in pushSessionsChanged, removing", e);
                        this.mSessionsListeners.remove(i2);
                    }
                }
            }
        }
    }

    private void pushRemoteVolumeUpdateLocked(int userId) {
        FullUserRecord user = getFullUserRecordLocked(userId);
        if (user == null) {
            Log.w(TAG, "pushRemoteVolumeUpdateLocked failed. No user with id=" + userId);
            return;
        }
        synchronized (this.mLock) {
            int size = this.mRemoteVolumeControllers.beginBroadcast();
            MediaSessionRecord record = user.mPriorityStack.getDefaultRemoteSession(userId);
            MediaSession.Token token = record == null ? null : record.getSessionToken();
            for (int i = size - 1; i >= 0; i--) {
                try {
                    this.mRemoteVolumeControllers.getBroadcastItem(i).updateRemoteController(token);
                } catch (Exception e) {
                    Log.w(TAG, "Error sending default remote volume.", e);
                }
            }
            this.mRemoteVolumeControllers.finishBroadcast();
        }
    }

    /* access modifiers changed from: package-private */
    public void pushSession2TokensChangedLocked(int userId) {
        List<Session2Token> allSession2Tokens = getSession2TokensLocked(-1);
        List<Session2Token> session2Tokens = getSession2TokensLocked(userId);
        for (int i = this.mSession2TokensListenerRecords.size() - 1; i >= 0; i--) {
            Session2TokensListenerRecord listenerRecord = this.mSession2TokensListenerRecords.get(i);
            try {
                if (listenerRecord.userId == -1) {
                    listenerRecord.listener.onSession2TokensChanged(allSession2Tokens);
                } else if (listenerRecord.userId == userId) {
                    listenerRecord.listener.onSession2TokensChanged(session2Tokens);
                }
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to notify Session2Token change. Removing listener.", e);
                this.mSession2TokensListenerRecords.remove(i);
            }
        }
    }

    public void onMediaButtonReceiverChanged(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            MediaSessionRecord mediaButtonSession = user.mPriorityStack.getMediaButtonSession();
            if (record == mediaButtonSession) {
                user.rememberMediaButtonReceiverLocked(mediaButtonSession);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getCallingPackageName(int uid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null || packages.length <= 0) {
            return "";
        }
        return packages[0];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchVolumeKeyLongPressLocked(KeyEvent keyEvent) {
        if (this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener != null) {
            try {
                this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener.onVolumeKeyLongPress(keyEvent);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to send " + keyEvent + " to volume key long-press listener");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FullUserRecord getFullUserRecordLocked(int userId) {
        int fullUserId = this.mFullUserIds.get(userId, -1);
        if (fullUserId < 0) {
            return null;
        }
        return this.mUserRecords.get(fullUserId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MediaSessionRecord getMediaSessionRecordLocked(MediaSession.Token sessionToken) {
        FullUserRecord user = getFullUserRecordLocked(UserHandle.getUserId(sessionToken.getUid()));
        if (user != null) {
            return user.mPriorityStack.getMediaSessionRecord(sessionToken);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final class FullUserRecord implements MediaSessionStack.OnMediaButtonSessionChangedListener {
        private static final String COMPONENT_NAME_USER_ID_DELIM = ",";
        public static final int COMPONENT_TYPE_ACTIVITY = 2;
        public static final int COMPONENT_TYPE_BROADCAST = 1;
        public static final int COMPONENT_TYPE_INVALID = 0;
        public static final int COMPONENT_TYPE_SERVICE = 3;
        private ICallback mCallback;
        private IBinder.DeathRecipient mCallbackBinder;
        private final int mFullUserId;
        private boolean mInitialDownMusicOnly;
        private KeyEvent mInitialDownVolumeKeyEvent;
        private int mInitialDownVolumeStream;
        private PendingIntent mLastMediaButtonReceiver;
        private IOnMediaKeyListener mOnMediaKeyListener;
        private int mOnMediaKeyListenerUid;
        private IOnVolumeKeyLongPressListener mOnVolumeKeyLongPressListener;
        private int mOnVolumeKeyLongPressListenerUid;
        private final MediaSessionStack mPriorityStack;
        private ComponentName mRestoredMediaButtonReceiver;
        private int mRestoredMediaButtonReceiverComponentType;
        private int mRestoredMediaButtonReceiverUserId;

        FullUserRecord(int fullUserId) {
            String[] tokens;
            this.mFullUserId = fullUserId;
            this.mPriorityStack = new MediaSessionStack(MediaSessionService.this.mAudioPlayerStateMonitor, this);
            String mediaButtonReceiverInfo = Settings.Secure.getStringForUser(MediaSessionService.this.mContentResolver, "media_button_receiver", this.mFullUserId);
            if (mediaButtonReceiverInfo != null && (tokens = mediaButtonReceiverInfo.split(COMPONENT_NAME_USER_ID_DELIM)) != null) {
                if (tokens.length == 2 || tokens.length == 3) {
                    this.mRestoredMediaButtonReceiver = ComponentName.unflattenFromString(tokens[0]);
                    this.mRestoredMediaButtonReceiverUserId = Integer.parseInt(tokens[1]);
                    if (tokens.length == 3) {
                        this.mRestoredMediaButtonReceiverComponentType = Integer.parseInt(tokens[2]);
                    } else {
                        this.mRestoredMediaButtonReceiverComponentType = getComponentType(this.mRestoredMediaButtonReceiver);
                    }
                }
            }
        }

        public void destroySessionsForUserLocked(int userId) {
            for (MediaSessionRecord session : this.mPriorityStack.getPriorityList(false, userId)) {
                MediaSessionService.this.destroySessionLocked(session);
            }
        }

        public void dumpLocked(PrintWriter pw, String prefix) {
            pw.print(prefix + "Record for full_user=" + this.mFullUserId);
            int size = MediaSessionService.this.mFullUserIds.size();
            for (int i = 0; i < size; i++) {
                if (MediaSessionService.this.mFullUserIds.keyAt(i) != MediaSessionService.this.mFullUserIds.valueAt(i) && MediaSessionService.this.mFullUserIds.valueAt(i) == this.mFullUserId) {
                    pw.print(", profile_user=" + MediaSessionService.this.mFullUserIds.keyAt(i));
                }
            }
            pw.println();
            String indent = prefix + "  ";
            pw.println(indent + "Volume key long-press listener: " + this.mOnVolumeKeyLongPressListener);
            pw.println(indent + "Volume key long-press listener package: " + MediaSessionService.this.getCallingPackageName(this.mOnVolumeKeyLongPressListenerUid));
            pw.println(indent + "Media key listener: " + this.mOnMediaKeyListener);
            pw.println(indent + "Media key listener package: " + MediaSessionService.this.getCallingPackageName(this.mOnMediaKeyListenerUid));
            pw.println(indent + "Callback: " + this.mCallback);
            pw.println(indent + "Last MediaButtonReceiver: " + this.mLastMediaButtonReceiver);
            pw.println(indent + "Restored MediaButtonReceiver: " + this.mRestoredMediaButtonReceiver);
            pw.println(indent + "Restored MediaButtonReceiverComponentType: " + this.mRestoredMediaButtonReceiverComponentType);
            this.mPriorityStack.dump(pw, indent);
            pw.println(indent + "Session2Tokens:");
            for (int i2 = 0; i2 < MediaSessionService.this.mSession2TokensPerUser.size(); i2++) {
                List<Session2Token> list = (List) MediaSessionService.this.mSession2TokensPerUser.valueAt(i2);
                if (!(list == null || list.size() == 0)) {
                    Iterator<Session2Token> it = list.iterator();
                    while (it.hasNext()) {
                        pw.println(indent + "  " + it.next());
                    }
                }
            }
        }

        @Override // com.android.server.media.MediaSessionStack.OnMediaButtonSessionChangedListener
        public void onMediaButtonSessionChanged(MediaSessionRecord oldMediaButtonSession, MediaSessionRecord newMediaButtonSession) {
            Log.i(MediaSessionService.TAG, "Media button session is changed to " + newMediaButtonSession);
            synchronized (MediaSessionService.this.mLock) {
                if (oldMediaButtonSession != null) {
                    try {
                        MediaSessionService.this.mHandler.postSessionsChanged(oldMediaButtonSession.getUserId());
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (newMediaButtonSession != null) {
                    rememberMediaButtonReceiverLocked(newMediaButtonSession);
                    MediaSessionService.this.mHandler.postSessionsChanged(newMediaButtonSession.getUserId());
                }
                pushAddressedPlayerChangedLocked();
            }
        }

        private IBinder getIAwareCMSService() {
            if (MediaSessionService.this.mIAwareCMSService == null) {
                MediaSessionService.this.mIAwareCMSService = ServiceManager.getService("IAwareCMSService");
            }
            return MediaSessionService.this.mIAwareCMSService;
        }

        private boolean isAudioApp(String packageName) {
            IBinder service = getIAwareCMSService();
            if (!(service == null || packageName == null)) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("android.rms.iaware.ICMSManager");
                data.writeString(packageName);
                try {
                    if (!service.transact(10, data, reply, 0)) {
                        return false;
                    }
                    reply.readExceptionCode();
                    if (reply.readInt() != 0 && reply.readInt() == 7) {
                        Log.i(MediaSessionService.TAG, "isAudioApp:" + packageName + " is music app");
                        return true;
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
            return false;
        }

        private boolean checkAppHasButtonReceiver(String packageName) {
            if (packageName == null) {
                Log.i(MediaSessionService.TAG, " checkAppHasButtonReceiver : false, packageName is null");
                return false;
            }
            Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
            mediaButtonIntent.setPackage(packageName);
            List<ResolveInfo> ril = MediaSessionService.this.getContext().getPackageManager().queryBroadcastReceivers(mediaButtonIntent, 0);
            if (ril == null || ril.size() == 0) {
                Log.i(MediaSessionService.TAG, " checkAppHasButtonReceiver : false, packageName: " + packageName);
                return false;
            }
            Log.i(MediaSessionService.TAG, " checkAppHasButtonReceiver : true, packageName: " + packageName);
            return true;
        }

        public void rememberMediaButtonReceiverLocked(MediaSessionRecord record) {
            ComponentName component;
            PendingIntent receiver = record.getMediaButtonReceiver();
            this.mRestoredMediaButtonReceiverComponentType = 0;
            boolean shouldSaveReceiver = false;
            String mediaButtonReceiverInfo = "";
            if (!(receiver == null || (component = receiver.getIntent().getComponent()) == null || !record.getPackageName().equals(component.getPackageName()))) {
                mediaButtonReceiverInfo = String.join(COMPONENT_NAME_USER_ID_DELIM, component.flattenToString(), String.valueOf(record.getUserId()), String.valueOf(getComponentType(component)));
                String packageName = component.getPackageName();
                if (checkAppHasButtonReceiver(packageName) && isAudioApp(packageName)) {
                    shouldSaveReceiver = true;
                    this.mRestoredMediaButtonReceiver = component;
                    this.mLastMediaButtonReceiver = receiver;
                }
            }
            if (shouldSaveReceiver) {
                Settings.Secure.putStringForUser(MediaSessionService.this.mContentResolver, "media_button_receiver", mediaButtonReceiverInfo, this.mFullUserId);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void pushAddressedPlayerChangedLocked() {
            if (this.mCallback != null) {
                try {
                    MediaSessionRecord mediaButtonSession = getMediaButtonSessionLocked();
                    if (mediaButtonSession != null) {
                        this.mCallback.onAddressedPlayerChangedToMediaSession(mediaButtonSession.getSessionToken());
                    } else if (MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver != null) {
                        this.mCallback.onAddressedPlayerChangedToMediaButtonReceiver(MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver.getIntent().getComponent());
                    } else if (MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiver != null) {
                        this.mCallback.onAddressedPlayerChangedToMediaButtonReceiver(MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiver);
                    }
                } catch (RemoteException e) {
                    Log.w(MediaSessionService.TAG, "Failed to pushAddressedPlayerChangedLocked", e);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private MediaSessionRecord getMediaButtonSessionLocked() {
            return MediaSessionService.this.isGlobalPriorityActiveLocked() ? MediaSessionService.this.mGlobalPrioritySession : this.mPriorityStack.getMediaButtonSession();
        }

        private int getComponentType(ComponentName componentName) {
            if (componentName == null) {
                return 0;
            }
            PackageManager pm = MediaSessionService.this.mContext.getPackageManager();
            try {
                if (pm.getActivityInfo(componentName, 786433) != null) {
                    return 2;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
            try {
                if (pm.getServiceInfo(componentName, 786436) != null) {
                    return 3;
                }
                return 1;
            } catch (PackageManager.NameNotFoundException e2) {
                return 1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class SessionsListenerRecord implements IBinder.DeathRecipient {
        public final ComponentName componentName;
        public final IActiveSessionsListener listener;
        public final int pid;
        public final int uid;
        public final int userId;

        SessionsListenerRecord(IActiveSessionsListener listener2, ComponentName componentName2, int userId2, int pid2, int uid2) {
            this.listener = listener2;
            this.componentName = componentName2;
            this.userId = userId2;
            this.pid = pid2;
            this.uid = uid2;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (MediaSessionService.this.mLock) {
                MediaSessionService.this.mSessionsListeners.remove(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class Session2TokensListenerRecord implements IBinder.DeathRecipient {
        public final ISession2TokensListener listener;
        public final int userId;

        Session2TokensListenerRecord(ISession2TokensListener listener2, int userId2) {
            this.listener = listener2;
            this.userId = userId2;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (MediaSessionService.this.mLock) {
                MediaSessionService.this.mSession2TokensListenerRecords.remove(this);
            }
        }
    }

    final class SettingsObserver extends ContentObserver {
        private final Uri mSecureSettingsUri;

        private SettingsObserver() {
            super(null);
            this.mSecureSettingsUri = Settings.Secure.getUriFor("enabled_notification_listeners");
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void observe() {
            MediaSessionService.this.mContentResolver.registerContentObserver(this.mSecureSettingsUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            MediaSessionService.this.updateActiveSessionListeners();
        }
    }

    /* access modifiers changed from: package-private */
    public class SessionManagerImpl extends ISessionManager.Stub {
        private static final String EXTRA_WAKELOCK_ACQUIRED = "android.media.AudioService.WAKELOCK_ACQUIRED";
        private static final int WAKELOCK_RELEASE_ON_FINISHED = 1980;
        private IHwBehaviorCollectManager mHwBehaviorManager;
        BroadcastReceiver mKeyEventDone = new BroadcastReceiver() {
            /* class com.android.server.media.MediaSessionService.SessionManagerImpl.AnonymousClass5 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                Bundle extras;
                if (intent != null && (extras = intent.getExtras()) != null) {
                    synchronized (MediaSessionService.this.mLock) {
                        if (extras.containsKey(SessionManagerImpl.EXTRA_WAKELOCK_ACQUIRED) && MediaSessionService.this.mMediaEventWakeLock.isHeld()) {
                            MediaSessionService.this.mMediaEventWakeLock.release();
                        }
                    }
                }
            }
        };
        private KeyEventWakeLockReceiver mKeyEventReceiver = new KeyEventWakeLockReceiver(MediaSessionService.this.mHandler);
        private boolean mVoiceButtonDown = false;
        private boolean mVoiceButtonHandled = false;

        SessionManagerImpl() {
        }

        public ISession createSession(String packageName, ISessionCallback cb, String tag, Bundle sessionInfo, int userId) throws RemoteException {
            Throwable th;
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                try {
                    MediaSessionService.this.enforcePackageName(packageName, uid);
                    int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, false, true, "createSession", packageName);
                    if (cb != null) {
                        ISession sessionBinder = MediaSessionService.this.createSessionInternal(pid, uid, resolvedUserId, packageName, cb, tag, sessionInfo).getSessionBinder();
                        Binder.restoreCallingIdentity(token);
                        return sessionBinder;
                    }
                    throw new IllegalArgumentException("Controller callback cannot be null");
                } catch (Throwable th2) {
                    th = th2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void notifySession2Created(Session2Token sessionToken) throws RemoteException {
            Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                Log.i(MediaSessionService.TAG, "Session2 is created " + sessionToken);
                if (uid == sessionToken.getUid()) {
                    new MediaController2.Builder(MediaSessionService.this.mContext, sessionToken).setControllerCallback(new HandlerExecutor(MediaSessionService.this.mHandler), new Controller2Callback(sessionToken)).build();
                    return;
                }
                throw new SecurityException("Unexpected Session2Token's UID, expected=" + uid + " but actually=" + sessionToken.getUid());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public List<MediaSession.Token> getSessions(ComponentName componentName, int userId) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = verifySessionsRequest(componentName, userId, pid, uid);
                ArrayList<MediaSession.Token> tokens = new ArrayList<>();
                synchronized (MediaSessionService.this.mLock) {
                    for (MediaSessionRecord record : MediaSessionService.this.getActiveSessionsLocked(resolvedUserId)) {
                        tokens.add(record.getSessionToken());
                    }
                }
                return tokens;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public ParceledListSlice getSession2Tokens(int userId) {
            List<Session2Token> result;
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, true, true, "getSession2Tokens", null);
                synchronized (MediaSessionService.this.mLock) {
                    result = MediaSessionService.this.getSession2TokensLocked(resolvedUserId);
                }
                return new ParceledListSlice(result);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void addSessionsListener(IActiveSessionsListener listener, ComponentName componentName, int userId) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = verifySessionsRequest(componentName, userId, pid, uid);
                synchronized (MediaSessionService.this.mLock) {
                    if (MediaSessionService.this.findIndexOfSessionsListenerLocked(listener) != -1) {
                        Log.w(MediaSessionService.TAG, "ActiveSessionsListener is already added, ignoring");
                        return;
                    }
                    SessionsListenerRecord record = new SessionsListenerRecord(listener, componentName, resolvedUserId, pid, uid);
                    try {
                        listener.asBinder().linkToDeath(record, 0);
                        MediaSessionService.this.mSessionsListeners.add(record);
                        Binder.restoreCallingIdentity(token);
                    } catch (RemoteException e) {
                        Log.e(MediaSessionService.TAG, "ActiveSessionsListener is dead, ignoring it", e);
                        Binder.restoreCallingIdentity(token);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void removeSessionsListener(IActiveSessionsListener listener) throws RemoteException {
            synchronized (MediaSessionService.this.mLock) {
                int index = MediaSessionService.this.findIndexOfSessionsListenerLocked(listener);
                if (index != -1) {
                    SessionsListenerRecord record = (SessionsListenerRecord) MediaSessionService.this.mSessionsListeners.remove(index);
                    try {
                        record.listener.asBinder().unlinkToDeath(record, 0);
                    } catch (Exception e) {
                    }
                }
            }
        }

        public void addSession2TokensListener(ISession2TokensListener listener, int userId) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, true, true, "addSession2TokensListener", null);
                synchronized (MediaSessionService.this.mLock) {
                    if (MediaSessionService.this.findIndexOfSession2TokensListenerLocked(listener) >= 0) {
                        Log.w(MediaSessionService.TAG, "addSession2TokensListener is already added, ignoring");
                        return;
                    }
                    MediaSessionService.this.mSession2TokensListenerRecords.add(new Session2TokensListenerRecord(listener, resolvedUserId));
                    Binder.restoreCallingIdentity(token);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void removeSession2TokensListener(ISession2TokensListener listener) {
            Binder.getCallingPid();
            Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (MediaSessionService.this.mLock) {
                    int index = MediaSessionService.this.findIndexOfSession2TokensListenerLocked(listener);
                    if (index >= 0) {
                        Session2TokensListenerRecord listenerRecord = (Session2TokensListenerRecord) MediaSessionService.this.mSession2TokensListenerRecords.remove(index);
                        try {
                            listenerRecord.listener.asBinder().unlinkToDeath(listenerRecord, 0);
                        } catch (Exception e) {
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void dispatchMediaKeyEvent(String packageName, boolean asSystemService, KeyEvent keyEvent, boolean needWakeLock) {
            if (keyEvent == null || !KeyEvent.isMediaSessionKey(keyEvent.getKeyCode())) {
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-media key event.");
                return;
            }
            int keyCode = keyEvent.getKeyCode();
            if (!HwDeviceManager.disallowOp(66) || keyCode == 91 || keyCode == 130) {
                int pid = Binder.getCallingPid();
                int uid = Binder.getCallingUid();
                long token = Binder.clearCallingIdentity();
                try {
                    Log.i(MediaSessionService.TAG, "dispatchMediaKeyEvent, pkg=" + packageName + " pid=" + pid + ", uid=" + uid + ", asSystem=" + asSystemService + ", event=" + keyEvent);
                    if (!isUserSetupComplete()) {
                        Slog.i(MediaSessionService.TAG, "Not dispatching media key event because user setup is in progress.");
                    } else if (!MediaSessionService.HWRIDEMODE_FEATURE_SUPPORTED || !SystemProperties.getBoolean("sys.ride_mode", false)) {
                        synchronized (MediaSessionService.this.mLock) {
                            boolean isGlobalPriorityActive = MediaSessionService.this.isGlobalPriorityActiveLocked();
                            if (!isGlobalPriorityActive || uid == 1000) {
                                if (!isGlobalPriorityActive && MediaSessionService.this.mCurrentFullUserRecord.mOnMediaKeyListener != null) {
                                    Log.i(MediaSessionService.TAG, "Send " + keyEvent + " to the media key listener");
                                    try {
                                        MediaSessionService.this.mCurrentFullUserRecord.mOnMediaKeyListener.onMediaKey(keyEvent, new MediaKeyListenerResultReceiver(packageName, pid, uid, asSystemService, keyEvent, needWakeLock));
                                        Binder.restoreCallingIdentity(token);
                                        return;
                                    } catch (RemoteException e) {
                                        Log.w(MediaSessionService.TAG, "Failed to send " + keyEvent + " to the media key listener");
                                    }
                                }
                                if (isGlobalPriorityActive || !isVoiceKey(keyEvent.getKeyCode())) {
                                    dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent, needWakeLock);
                                } else {
                                    handleVoiceKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent, needWakeLock);
                                }
                                Binder.restoreCallingIdentity(token);
                                return;
                            }
                            Slog.i(MediaSessionService.TAG, "Only the system can dispatch media key event to the global priority session.");
                            Binder.restoreCallingIdentity(token);
                        }
                    } else {
                        Slog.i(MediaSessionService.TAG, "Not dispatching media key event because Ride mode is enabled");
                        Binder.restoreCallingIdentity(token);
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                Log.i(MediaSessionService.TAG, "MDM disabled the media control key: " + KeyEvent.keyCodeToString(keyCode));
            }
        }

        public boolean dispatchMediaKeyEventToSessionAsSystemService(String packageName, MediaSession.Token sessionToken, KeyEvent keyEvent) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (MediaSessionService.this.mLock) {
                    MediaSessionRecord record = MediaSessionService.this.getMediaSessionRecordLocked(sessionToken);
                    if (record == null) {
                        Log.w(MediaSessionService.TAG, "Failed to find session to dispatch key event.");
                        return false;
                    }
                    Log.i(MediaSessionService.TAG, "dispatchMediaKeyEventToSessionAsSystemService, pkg=" + packageName + ", pid=" + pid + ", uid=" + uid + ", sessionToken=" + sessionToken + ", event=" + keyEvent + ", session=" + record);
                    boolean sendMediaButton = record.sendMediaButton(packageName, pid, uid, true, keyEvent, 0, null);
                    Binder.restoreCallingIdentity(token);
                    return sendMediaButton;
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setCallback(ICallback callback) {
            Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (UserHandle.isSameApp(uid, 1002)) {
                    synchronized (MediaSessionService.this.mLock) {
                        int userId = UserHandle.getUserId(uid);
                        final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                        if (user != null) {
                            if (user.mFullUserId == userId) {
                                Log.i(MediaSessionService.TAG, "The user.mCallback = " + user.mCallback + ",callback = " + callback + " is set by uid = " + uid);
                                if (user.mCallback != null) {
                                    user.mCallback.asBinder().unlinkToDeath(user.mCallbackBinder, 0);
                                    user.mCallbackBinder = null;
                                }
                                user.mCallback = callback;
                                if (user.mCallback == null) {
                                    Binder.restoreCallingIdentity(token);
                                    return;
                                }
                                try {
                                    user.mCallbackBinder = new IBinder.DeathRecipient() {
                                        /* class com.android.server.media.MediaSessionService.SessionManagerImpl.AnonymousClass1 */

                                        @Override // android.os.IBinder.DeathRecipient
                                        public void binderDied() {
                                            synchronized (MediaSessionService.this.mLock) {
                                                if (user.mCallback != null) {
                                                    user.mCallback.asBinder().unlinkToDeath(this, 0);
                                                }
                                                user.mCallback = null;
                                                user.mCallbackBinder = null;
                                            }
                                        }
                                    };
                                    user.mCallback.asBinder().linkToDeath(user.mCallbackBinder, 0);
                                    user.pushAddressedPlayerChangedLocked();
                                } catch (RemoteException e) {
                                    Log.w(MediaSessionService.TAG, "Failed to set callback", e);
                                    user.mCallback = null;
                                    user.mCallbackBinder = null;
                                }
                                Binder.restoreCallingIdentity(token);
                                return;
                            }
                        }
                        Log.w(MediaSessionService.TAG, "Only the full user can set the callback, userId=" + userId);
                        return;
                    }
                }
                throw new SecurityException("Only Bluetooth service processes can set Callback");
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setOnVolumeKeyLongPressListener(IOnVolumeKeyLongPressListener listener) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaSessionService.this.mContext.checkPermission("android.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER", pid, uid) == 0) {
                    synchronized (MediaSessionService.this.mLock) {
                        int userId = UserHandle.getUserId(uid);
                        final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                        if (user != null) {
                            if (user.mFullUserId == userId) {
                                if (user.mOnVolumeKeyLongPressListener == null || user.mOnVolumeKeyLongPressListenerUid == uid) {
                                    user.mOnVolumeKeyLongPressListener = listener;
                                    user.mOnVolumeKeyLongPressListenerUid = uid;
                                    Log.i(MediaSessionService.TAG, "The volume key long-press listener " + listener + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                                    if (user.mOnVolumeKeyLongPressListener != null) {
                                        try {
                                            user.mOnVolumeKeyLongPressListener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                                                /* class com.android.server.media.MediaSessionService.SessionManagerImpl.AnonymousClass2 */

                                                @Override // android.os.IBinder.DeathRecipient
                                                public void binderDied() {
                                                    synchronized (MediaSessionService.this.mLock) {
                                                        if (user.mOnVolumeKeyLongPressListener != null) {
                                                            user.mOnVolumeKeyLongPressListener.asBinder().unlinkToDeath(this, 0);
                                                        }
                                                        user.mOnVolumeKeyLongPressListener = null;
                                                    }
                                                }
                                            }, 0);
                                        } catch (RemoteException e) {
                                            Log.w(MediaSessionService.TAG, "Failed to set death recipient " + user.mOnVolumeKeyLongPressListener);
                                            user.mOnVolumeKeyLongPressListener = null;
                                        }
                                    }
                                    Binder.restoreCallingIdentity(token);
                                    return;
                                }
                                Log.w(MediaSessionService.TAG, "The volume key long-press listener cannot be reset by another app , mOnVolumeKeyLongPressListener=" + user.mOnVolumeKeyLongPressListenerUid + ", uid=" + uid);
                                Binder.restoreCallingIdentity(token);
                                return;
                            }
                        }
                        Log.w(MediaSessionService.TAG, "Only the full user can set the volume key long-press listener, userId=" + userId);
                        return;
                    }
                }
                throw new SecurityException("Must hold the SET_VOLUME_KEY_LONG_PRESS_LISTENER permission.");
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setOnMediaKeyListener(IOnMediaKeyListener listener) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaSessionService.this.mContext.checkPermission("android.permission.SET_MEDIA_KEY_LISTENER", pid, uid) == 0) {
                    synchronized (MediaSessionService.this.mLock) {
                        int userId = UserHandle.getUserId(uid);
                        final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                        if (user != null) {
                            if (user.mFullUserId == userId) {
                                if (user.mOnMediaKeyListener == null || user.mOnMediaKeyListenerUid == uid) {
                                    user.mOnMediaKeyListener = listener;
                                    user.mOnMediaKeyListenerUid = uid;
                                    Log.i(MediaSessionService.TAG, "The media key listener " + user.mOnMediaKeyListener + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                                    if (user.mOnMediaKeyListener != null) {
                                        try {
                                            user.mOnMediaKeyListener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                                                /* class com.android.server.media.MediaSessionService.SessionManagerImpl.AnonymousClass3 */

                                                @Override // android.os.IBinder.DeathRecipient
                                                public void binderDied() {
                                                    synchronized (MediaSessionService.this.mLock) {
                                                        if (user.mOnMediaKeyListener != null) {
                                                            user.mOnMediaKeyListener.asBinder().unlinkToDeath(this, 0);
                                                        }
                                                        user.mOnMediaKeyListener = null;
                                                    }
                                                }
                                            }, 0);
                                        } catch (RemoteException e) {
                                            Log.w(MediaSessionService.TAG, "Failed to set death recipient " + user.mOnMediaKeyListener);
                                            user.mOnMediaKeyListener = null;
                                        }
                                    }
                                    Binder.restoreCallingIdentity(token);
                                    return;
                                }
                                Log.w(MediaSessionService.TAG, "The media key listener cannot be reset by another app. , mOnMediaKeyListenerUid=" + user.mOnMediaKeyListenerUid + ", uid=" + uid);
                                Binder.restoreCallingIdentity(token);
                                return;
                            }
                        }
                        Log.w(MediaSessionService.TAG, "Only the full user can set the media key listener, userId=" + userId);
                        return;
                    }
                }
                throw new SecurityException("Must hold the SET_MEDIA_KEY_LISTENER permission.");
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void dispatchVolumeKeyEvent(String packageName, String opPackageName, boolean asSystemService, KeyEvent keyEvent, int stream, boolean musicOnly) {
            if (keyEvent == null || !(keyEvent.getKeyCode() == 24 || keyEvent.getKeyCode() == 25 || keyEvent.getKeyCode() == 164)) {
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-volume key event.");
                return;
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            Log.i(MediaSessionService.TAG, "dispatchVolumeKeyEvent, pkg=" + packageName + ", opPkg=" + opPackageName + ", pid=" + pid + ", uid=" + uid + ", asSystem=" + asSystemService + ", event=" + keyEvent + ", stream=" + stream + ", musicOnly=" + musicOnly);
            try {
                synchronized (MediaSessionService.this.mLock) {
                    if (!MediaSessionService.this.isGlobalPriorityActiveLocked()) {
                        if (MediaSessionService.this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener != null) {
                            if (keyEvent.getAction() == 0) {
                                if (keyEvent.getRepeatCount() == 0) {
                                    MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent = KeyEvent.obtain(keyEvent);
                                    MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeStream = stream;
                                    MediaSessionService.this.mCurrentFullUserRecord.mInitialDownMusicOnly = musicOnly;
                                    MediaSessionService.this.mHandler.sendMessageDelayed(MediaSessionService.this.mHandler.obtainMessage(2, MediaSessionService.this.mCurrentFullUserRecord.mFullUserId, 0), (long) MediaSessionService.this.mLongPressTimeout);
                                }
                                if (keyEvent.getRepeatCount() > 0 || keyEvent.isLongPress()) {
                                    MediaSessionService.this.mHandler.removeMessages(2);
                                    if (MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent != null) {
                                        MediaSessionService.this.dispatchVolumeKeyLongPressLocked(MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent);
                                        MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent = null;
                                    }
                                    MediaSessionService.this.dispatchVolumeKeyLongPressLocked(keyEvent);
                                }
                            } else {
                                MediaSessionService.this.mHandler.removeMessages(2);
                                if (MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent == null || MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent.getDownTime() != keyEvent.getDownTime()) {
                                    MediaSessionService.this.dispatchVolumeKeyLongPressLocked(keyEvent);
                                } else {
                                    dispatchVolumeKeyEventLocked(packageName, opPackageName, pid, uid, asSystemService, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeStream, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownMusicOnly);
                                    dispatchVolumeKeyEventLocked(packageName, opPackageName, pid, uid, asSystemService, keyEvent, stream, musicOnly);
                                }
                            }
                        }
                    }
                    dispatchVolumeKeyEventLocked(packageName, opPackageName, pid, uid, asSystemService, keyEvent, stream, musicOnly);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private void dispatchVolumeKeyEventLocked(String packageName, String opPackageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, int stream, boolean musicOnly) {
            int flags;
            boolean up = false;
            boolean down = keyEvent.getAction() == 0;
            if (keyEvent.getAction() == 1) {
                up = true;
            }
            int direction = 0;
            boolean isMute = false;
            int keyCode = keyEvent.getKeyCode();
            if (keyCode == 24) {
                direction = 1;
            } else if (keyCode == 25) {
                direction = -1;
            } else if (keyCode == 164) {
                isMute = true;
            }
            if (down || up) {
                if (musicOnly) {
                    flags = 4096 | 512;
                } else if (up) {
                    flags = 4096 | 20;
                } else {
                    flags = 4096 | 17;
                }
                if (direction != 0) {
                    if (up) {
                        direction = 0;
                    }
                    dispatchAdjustVolumeLocked(packageName, opPackageName, pid, uid, asSystemService, stream, direction, flags);
                } else if (isMute && down && keyEvent.getRepeatCount() == 0) {
                    dispatchAdjustVolumeLocked(packageName, opPackageName, pid, uid, asSystemService, stream, 101, flags);
                }
            }
        }

        public void dispatchVolumeKeyEventToSessionAsSystemService(String packageName, String opPackageName, MediaSession.Token sessionToken, KeyEvent keyEvent) {
            Throwable th;
            Throwable th2;
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (MediaSessionService.this.mLock) {
                    try {
                        MediaSessionRecord record = MediaSessionService.this.getMediaSessionRecordLocked(sessionToken);
                        if (record == null) {
                            try {
                                Log.w(MediaSessionService.TAG, "Failed to find session to dispatch key event, token=" + sessionToken + ". Fallbacks to the default handling.");
                                dispatchVolumeKeyEventLocked(packageName, opPackageName, pid, uid, true, keyEvent, Integer.MIN_VALUE, false);
                                Binder.restoreCallingIdentity(token);
                                return;
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("dispatchVolumeKeyEventToSessionAsSystemService, pkg=");
                            try {
                                sb.append(packageName);
                                sb.append(", opPkg=");
                            } catch (Throwable th4) {
                                th2 = th4;
                                throw th2;
                            }
                            try {
                                sb.append(opPackageName);
                                sb.append(", pid=");
                                sb.append(pid);
                                sb.append(", uid=");
                                sb.append(uid);
                                sb.append(", sessionToken=");
                                sb.append(sessionToken);
                                sb.append(", event=");
                                try {
                                    sb.append(keyEvent);
                                    sb.append(", session=");
                                    sb.append(record);
                                    Log.i(MediaSessionService.TAG, sb.toString());
                                    int action = keyEvent.getAction();
                                    if (action == 0) {
                                        int direction = 0;
                                        int keyCode = keyEvent.getKeyCode();
                                        if (keyCode == 24) {
                                            direction = 1;
                                        } else if (keyCode == 25) {
                                            direction = -1;
                                        } else if (keyCode == 164) {
                                            direction = 101;
                                        }
                                        record.adjustVolume(packageName, opPackageName, pid, uid, null, true, direction, 1, false);
                                    } else if (action == 1) {
                                        try {
                                            record.adjustVolume(packageName, opPackageName, pid, uid, null, true, 0, 4116, false);
                                        } catch (Throwable th5) {
                                            th2 = th5;
                                        }
                                    }
                                    Binder.restoreCallingIdentity(token);
                                    return;
                                } catch (Throwable th6) {
                                    th2 = th6;
                                    throw th2;
                                }
                            } catch (Throwable th7) {
                                th2 = th7;
                                throw th2;
                            }
                        }
                    } catch (Throwable th8) {
                        th2 = th8;
                        throw th2;
                    }
                }
                try {
                    throw th2;
                } catch (Throwable th9) {
                    th = th9;
                }
            } catch (Throwable th10) {
                th = th10;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void dispatchAdjustVolume(String packageName, String opPackageName, int suggestedStream, int delta, int flags) {
            sendBehavior(IHwBehaviorCollectManager.BehaviorId.MEDIASESSION_DISPATCHADJUSTVOLUME, new Object[0]);
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            Log.i(MediaSessionService.TAG, "dispatchAdjustVolume, pkg=" + packageName + ", opPkg=" + opPackageName + ", suggestedStream=" + suggestedStream + ", delta=" + delta + ", flags=" + flags);
            try {
                synchronized (MediaSessionService.this.mLock) {
                    dispatchAdjustVolumeLocked(packageName, opPackageName, pid, uid, false, suggestedStream, delta, flags);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void registerRemoteVolumeController(IRemoteVolumeController rvc) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            synchronized (MediaSessionService.this.mLock) {
                try {
                    MediaSessionService.this.enforceStatusBarServicePermission("listen for volume changes", pid, uid);
                    MediaSessionService.this.mRemoteVolumeControllers.register(rvc);
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
        }

        public void unregisterRemoteVolumeController(IRemoteVolumeController rvc) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            synchronized (MediaSessionService.this.mLock) {
                try {
                    MediaSessionService.this.enforceStatusBarServicePermission("listen for volume changes", pid, uid);
                    MediaSessionService.this.mRemoteVolumeControllers.unregister(rvc);
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
        }

        public boolean isGlobalPriorityActive() {
            boolean isGlobalPriorityActiveLocked;
            synchronized (MediaSessionService.this.mLock) {
                isGlobalPriorityActiveLocked = MediaSessionService.this.isGlobalPriorityActiveLocked();
            }
            return isGlobalPriorityActiveLocked;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(MediaSessionService.this.mContext, MediaSessionService.TAG, pw)) {
                pw.println("MEDIA SESSION SERVICE (dumpsys media_session)");
                pw.println();
                synchronized (MediaSessionService.this.mLock) {
                    pw.println(MediaSessionService.this.mSessionsListeners.size() + " sessions listeners.");
                    pw.println("Global priority session is " + MediaSessionService.this.mGlobalPrioritySession);
                    if (MediaSessionService.this.mGlobalPrioritySession != null) {
                        MediaSessionService.this.mGlobalPrioritySession.dump(pw, "  ");
                    }
                    pw.println("User Records:");
                    int count = MediaSessionService.this.mUserRecords.size();
                    for (int i = 0; i < count; i++) {
                        ((FullUserRecord) MediaSessionService.this.mUserRecords.valueAt(i)).dumpLocked(pw, "");
                    }
                    MediaSessionService.this.mAudioPlayerStateMonitor.dump(MediaSessionService.this.mContext, pw, "");
                }
            }
        }

        public boolean isTrusted(String controllerPackageName, int controllerPid, int controllerUid) throws RemoteException {
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                return hasMediaControlPermission(UserHandle.getUserId(uid), controllerPackageName, controllerPid, controllerUid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private int verifySessionsRequest(ComponentName componentName, int userId, int pid, int uid) {
            String packageName = null;
            if (componentName != null) {
                packageName = componentName.getPackageName();
                MediaSessionService.this.enforcePackageName(packageName, uid);
            }
            int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, true, true, "getSessions", packageName);
            MediaSessionService.this.enforceMediaPermissions(componentName, pid, uid, resolvedUserId);
            return resolvedUserId;
        }

        private boolean hasMediaControlPermission(int resolvedUserId, String packageName, int pid, int uid) throws RemoteException {
            if (MediaSessionService.this.hasStatusBarServicePermission(pid, uid) || uid == 1000 || MediaSessionService.this.mContext.checkPermission("android.permission.MEDIA_CONTENT_CONTROL", pid, uid) == 0) {
                return true;
            }
            Log.i(MediaSessionService.TAG, packageName + " (uid=" + uid + ") hasn't granted MEDIA_CONTENT_CONTROL");
            int userId = UserHandle.getUserId(uid);
            if (resolvedUserId != userId) {
                return false;
            }
            List<ComponentName> enabledNotificationListeners = MediaSessionService.this.mNotificationManager.getEnabledNotificationListeners(userId);
            if (enabledNotificationListeners != null) {
                for (int i = 0; i < enabledNotificationListeners.size(); i++) {
                    if (TextUtils.equals(packageName, enabledNotificationListeners.get(i).getPackageName())) {
                        return true;
                    }
                }
            }
            Log.i(MediaSessionService.TAG, packageName + " (uid=" + uid + ") doesn't have an enabled notification listener");
            return false;
        }

        private void dispatchAdjustVolumeLocked(final String packageName, final String opPackageName, int pid, final int uid, final boolean asSystemService, final int suggestedStream, final int direction, final int flags) {
            MediaSessionRecord session;
            boolean preferSuggestedStream;
            if (MediaSessionService.this.isGlobalPriorityActiveLocked()) {
                session = MediaSessionService.this.mGlobalPrioritySession;
            } else {
                session = MediaSessionService.this.mCurrentFullUserRecord.mPriorityStack.getDefaultVolumeSession();
            }
            if (!isValidLocalStreamType(suggestedStream) || !AudioSystem.isStreamActive(suggestedStream, 0)) {
                preferSuggestedStream = false;
            } else {
                preferSuggestedStream = true;
            }
            Log.i(MediaSessionService.TAG, "Adjusting " + session + " by " + direction + ". flags=" + flags + ", suggestedStream=" + suggestedStream + ", preferSuggestedStream=" + preferSuggestedStream);
            if (session != null && !preferSuggestedStream) {
                session.adjustVolume(packageName, opPackageName, pid, uid, null, asSystemService, direction, flags, true);
            } else if ((flags & 512) == 0 || AudioSystem.isStreamActive(3, 0)) {
                MediaSessionService.this.mHandler.post(new Runnable() {
                    /* class com.android.server.media.MediaSessionService.SessionManagerImpl.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        int callingUid;
                        String callingOpPackageName;
                        if (asSystemService) {
                            callingOpPackageName = MediaSessionService.this.mContext.getOpPackageName();
                            callingUid = Process.myUid();
                            if (callingOpPackageName == null) {
                                Log.e(MediaSessionService.TAG, "mContext.getOpPackageName() is null");
                            }
                        } else {
                            callingOpPackageName = opPackageName;
                            callingUid = uid;
                            if (callingOpPackageName == null) {
                                Log.e(MediaSessionService.TAG, "opPackageName is null");
                            }
                        }
                        try {
                            MediaSessionService.this.mAudioManagerInternal.adjustSuggestedStreamVolumeForUid(suggestedStream, direction, flags, callingOpPackageName, callingUid);
                        } catch (IllegalArgumentException | SecurityException e) {
                            Log.e(MediaSessionService.TAG, "Cannot adjust volume: direction=" + direction + ", suggestedStream=" + suggestedStream + ", flags=" + flags + ", packageName=" + packageName + ", uid=" + uid + ", asSystemService=" + asSystemService, e);
                        }
                    }
                });
            } else {
                Log.i(MediaSessionService.TAG, "No active session to adjust, skipping media only volume event");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void handleVoiceKeyEventLocked(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, boolean needWakeLock) {
            int action = keyEvent.getAction();
            boolean isLongPress = (keyEvent.getFlags() & 128) != 0;
            if (action == 0) {
                if (keyEvent.getRepeatCount() == 0) {
                    this.mVoiceButtonDown = true;
                    this.mVoiceButtonHandled = false;
                } else if (this.mVoiceButtonDown && !this.mVoiceButtonHandled && isLongPress) {
                    this.mVoiceButtonHandled = true;
                    startVoiceInput(needWakeLock);
                }
            } else if (action != 1) {
            } else {
                if (this.mVoiceButtonDown) {
                    this.mVoiceButtonDown = false;
                    if (!this.mVoiceButtonHandled && !keyEvent.isCanceled()) {
                        dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, KeyEvent.changeAction(keyEvent, 0), needWakeLock);
                        dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent, needWakeLock);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dispatchMediaKeyEventLocked(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, boolean needWakeLock) {
            ComponentName componentName;
            MediaSessionRecord session = MediaSessionService.this.mCurrentFullUserRecord.getMediaButtonSessionLocked();
            int i = -1;
            if (session != null) {
                Log.i(MediaSessionService.TAG, "Sending " + keyEvent + " to " + session);
                if (keyEvent.getAction() == 0) {
                    LogPower.push(148, "mediakey", session.mPackageName, Integer.toString(session.mOwnerPid), new String[]{String.valueOf(keyEvent.getKeyCode())});
                }
                if (needWakeLock) {
                    this.mKeyEventReceiver.aquireWakeLockLocked();
                }
                if (needWakeLock) {
                    i = this.mKeyEventReceiver.mLastTimeoutId;
                }
                session.sendMediaButton(packageName, pid, uid, asSystemService, keyEvent, i, this.mKeyEventReceiver);
                if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                    try {
                        MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaSession(keyEvent, session.getSessionToken());
                    } catch (RemoteException e) {
                        Log.w(MediaSessionService.TAG, "Failed to send callback", e);
                    }
                }
            } else if (MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver == null && MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiver == null) {
                Log.i(MediaSessionService.TAG, "Sending media key ordered broadcast");
                if (needWakeLock) {
                    MediaSessionService.this.mMediaEventWakeLock.acquire();
                }
                Intent keyIntent = new Intent("android.intent.action.MEDIA_BUTTON", (Uri) null);
                keyIntent.addFlags(268435456);
                keyIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                if (needWakeLock) {
                    keyIntent.putExtra(EXTRA_WAKELOCK_ACQUIRED, WAKELOCK_RELEASE_ON_FINISHED);
                }
                if (checkPackage("com.android.mediacenter")) {
                    Log.i(MediaSessionService.TAG, "Sending media key to mediacenter apk");
                    keyIntent.setPackage("com.android.mediacenter");
                    keyIntent.addFlags(32);
                } else if (checkPackage("com.huawei.music")) {
                    Log.i(MediaSessionService.TAG, "Sending media key to mediacenter apk");
                    keyIntent.setPackage("com.huawei.music");
                    keyIntent.addFlags(32);
                }
                MediaSessionService.this.getContext().sendOrderedBroadcastAsUser(keyIntent, UserHandle.CURRENT, null, this.mKeyEventDone, MediaSessionService.this.mHandler, -1, null, null);
            } else {
                if (needWakeLock) {
                    this.mKeyEventReceiver.aquireWakeLockLocked();
                }
                Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
                mediaButtonIntent.addFlags(268435456);
                mediaButtonIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                mediaButtonIntent.putExtra("android.intent.extra.PACKAGE_NAME", asSystemService ? MediaSessionService.this.mContext.getPackageName() : packageName);
                try {
                    if (MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver != null) {
                        PendingIntent receiver = MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver;
                        Log.i(MediaSessionService.TAG, "Sending " + keyEvent + " to the last known PendingIntent " + receiver);
                        Context context = MediaSessionService.this.mContext;
                        if (needWakeLock) {
                            i = this.mKeyEventReceiver.mLastTimeoutId;
                        }
                        receiver.send(context, i, mediaButtonIntent, this.mKeyEventReceiver, MediaSessionService.this.mHandler);
                        if (!(MediaSessionService.this.mCurrentFullUserRecord.mCallback == null || (componentName = MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver.getIntent().getComponent()) == null)) {
                            MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaButtonReceiver(keyEvent, componentName);
                        }
                        return;
                    }
                    ComponentName receiver2 = MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiver;
                    int componentType = MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiverComponentType;
                    UserHandle userHandle = UserHandle.of(MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiverUserId);
                    Log.i(MediaSessionService.TAG, "Sending " + keyEvent + " to the restored intent " + receiver2 + ", type=" + componentType);
                    mediaButtonIntent.setComponent(receiver2);
                    if (componentType == 2) {
                        MediaSessionService.this.mContext.startActivityAsUser(mediaButtonIntent, userHandle);
                    } else if (componentType != 3) {
                        try {
                            MediaSessionService.this.mContext.sendBroadcastAsUser(mediaButtonIntent, userHandle);
                        } catch (Exception e2) {
                            Log.w(MediaSessionService.TAG, "Error sending media button to the restored intent " + receiver2 + ", type=" + componentType, e2);
                        }
                    } else {
                        MediaSessionService.this.mContext.startForegroundServiceAsUser(mediaButtonIntent, userHandle);
                    }
                    if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                        MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaButtonReceiver(keyEvent, receiver2);
                    }
                } catch (PendingIntent.CanceledException e3) {
                    Log.i(MediaSessionService.TAG, "Error sending key event to media button receiver " + MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver, e3);
                } catch (RemoteException e4) {
                    Log.w(MediaSessionService.TAG, "Failed to send callback", e4);
                }
            }
        }

        private boolean checkPackage(String packageName) {
            if (packageName == null || packageName.equals("")) {
                return false;
            }
            try {
                MediaSessionService.this.getContext().getPackageManager().getApplicationInfo(packageName, 8192);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        private void startVoiceInput(boolean needWakeLock) {
            Intent voiceIntent;
            PowerManager pm = (PowerManager) MediaSessionService.this.mContext.getSystemService("power");
            boolean z = true;
            boolean isLocked = MediaSessionService.this.mKeyguardManager != null && MediaSessionService.this.mKeyguardManager.isKeyguardLocked();
            if (isLocked || !pm.isScreenOn()) {
                voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
                if (!isLocked || !MediaSessionService.this.mKeyguardManager.isKeyguardSecure()) {
                    z = false;
                }
                voiceIntent.putExtra("android.speech.extras.EXTRA_SECURE", z);
                Log.i(MediaSessionService.TAG, "voice-based interactions: about to use ACTION_VOICE_SEARCH_HANDS_FREE");
            } else {
                voiceIntent = new Intent("android.speech.action.WEB_SEARCH");
                Log.i(MediaSessionService.TAG, "voice-based interactions: about to use ACTION_WEB_SEARCH");
            }
            if (needWakeLock) {
                MediaSessionService.this.mMediaEventWakeLock.acquire();
            }
            try {
                voiceIntent.setFlags(276824064);
                Log.i(MediaSessionService.TAG, "voiceIntent: " + voiceIntent);
                MediaSessionService.this.mContext.startActivityAsUser(voiceIntent, UserHandle.CURRENT);
                if (!needWakeLock) {
                    return;
                }
            } catch (ActivityNotFoundException e) {
                Log.w(MediaSessionService.TAG, "No activity for search: " + e);
                if (!needWakeLock) {
                    return;
                }
            } catch (Throwable th) {
                if (needWakeLock) {
                    MediaSessionService.this.mMediaEventWakeLock.release();
                }
                throw th;
            }
            MediaSessionService.this.mMediaEventWakeLock.release();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isVoiceKey(int keyCode) {
            return keyCode == 79 || (!MediaSessionService.this.mHasFeatureLeanback && keyCode == 85);
        }

        private boolean isUserSetupComplete() {
            return Settings.Secure.getIntForUser(MediaSessionService.this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0;
        }

        private boolean isValidLocalStreamType(int streamType) {
            return streamType >= 0 && streamType <= 5;
        }

        private void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
            if (this.mHwBehaviorManager == null) {
                this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
            }
            IHwBehaviorCollectManager iHwBehaviorCollectManager = this.mHwBehaviorManager;
            if (iHwBehaviorCollectManager == null) {
                Log.w(MediaSessionService.TAG, "HwBehaviorCollectManager is null");
            } else if (params == null || params.length == 0) {
                this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
            } else {
                iHwBehaviorCollectManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid, params);
            }
        }

        private class MediaKeyListenerResultReceiver extends ResultReceiver implements Runnable {
            private final boolean mAsSystemService;
            private boolean mHandled;
            private final KeyEvent mKeyEvent;
            private final boolean mNeedWakeLock;
            private final String mPackageName;
            private final int mPid;
            private final int mUid;

            private MediaKeyListenerResultReceiver(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, boolean needWakeLock) {
                super(MediaSessionService.this.mHandler);
                MediaSessionService.this.mHandler.postDelayed(this, 1000);
                this.mPackageName = packageName;
                this.mPid = pid;
                this.mUid = uid;
                this.mAsSystemService = asSystemService;
                this.mKeyEvent = keyEvent;
                this.mNeedWakeLock = needWakeLock;
            }

            @Override // java.lang.Runnable
            public void run() {
                Log.i(MediaSessionService.TAG, "The media key listener is timed-out for " + this.mKeyEvent);
                dispatchMediaKeyEvent();
            }

            /* access modifiers changed from: protected */
            @Override // android.os.ResultReceiver
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == 1) {
                    this.mHandled = true;
                    MediaSessionService.this.mHandler.removeCallbacks(this);
                    return;
                }
                dispatchMediaKeyEvent();
            }

            private void dispatchMediaKeyEvent() {
                if (!this.mHandled) {
                    this.mHandled = true;
                    MediaSessionService.this.mHandler.removeCallbacks(this);
                    synchronized (MediaSessionService.this.mLock) {
                        if (MediaSessionService.this.isGlobalPriorityActiveLocked() || !SessionManagerImpl.this.isVoiceKey(this.mKeyEvent.getKeyCode())) {
                            SessionManagerImpl.this.dispatchMediaKeyEventLocked(this.mPackageName, this.mPid, this.mUid, this.mAsSystemService, this.mKeyEvent, this.mNeedWakeLock);
                        } else {
                            SessionManagerImpl.this.handleVoiceKeyEventLocked(this.mPackageName, this.mPid, this.mUid, this.mAsSystemService, this.mKeyEvent, this.mNeedWakeLock);
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public class KeyEventWakeLockReceiver extends ResultReceiver implements Runnable, PendingIntent.OnFinished {
            private final Handler mHandler;
            private int mLastTimeoutId = 0;
            private int mRefCount = 0;

            KeyEventWakeLockReceiver(Handler handler) {
                super(handler);
                this.mHandler = handler;
            }

            public void onTimeout() {
                synchronized (MediaSessionService.this.mLock) {
                    if (this.mRefCount != 0) {
                        this.mLastTimeoutId++;
                        this.mRefCount = 0;
                        releaseWakeLockLocked();
                    }
                }
            }

            public void aquireWakeLockLocked() {
                if (this.mRefCount == 0) {
                    MediaSessionService.this.mMediaEventWakeLock.acquire();
                }
                this.mRefCount++;
                this.mHandler.removeCallbacks(this);
                this.mHandler.postDelayed(this, 5000);
            }

            @Override // java.lang.Runnable
            public void run() {
                onTimeout();
            }

            /* access modifiers changed from: protected */
            @Override // android.os.ResultReceiver
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode >= this.mLastTimeoutId) {
                    synchronized (MediaSessionService.this.mLock) {
                        if (this.mRefCount > 0) {
                            this.mRefCount--;
                            if (this.mRefCount == 0) {
                                releaseWakeLockLocked();
                            }
                        }
                    }
                }
            }

            private void releaseWakeLockLocked() {
                MediaSessionService.this.mMediaEventWakeLock.release();
                this.mHandler.removeCallbacks(this);
            }

            @Override // android.app.PendingIntent.OnFinished
            public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
                onReceiveResult(resultCode, null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class MessageHandler extends Handler {
        private static final int MSG_SESSIONS_CHANGED = 1;
        private static final int MSG_VOLUME_INITIAL_DOWN = 2;
        private final SparseArray<Integer> mIntegerCache = new SparseArray<>();

        MessageHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                MediaSessionService.this.pushSessionsChanged(((Integer) msg.obj).intValue());
            } else if (i == 2) {
                synchronized (MediaSessionService.this.mLock) {
                    FullUserRecord user = (FullUserRecord) MediaSessionService.this.mUserRecords.get(msg.arg1);
                    if (!(user == null || user.mInitialDownVolumeKeyEvent == null)) {
                        MediaSessionService.this.dispatchVolumeKeyLongPressLocked(user.mInitialDownVolumeKeyEvent);
                        user.mInitialDownVolumeKeyEvent = null;
                    }
                }
            }
        }

        public void postSessionsChanged(int userId) {
            Integer userIdInteger = this.mIntegerCache.get(userId);
            if (userIdInteger == null) {
                userIdInteger = Integer.valueOf(userId);
                this.mIntegerCache.put(userId, userIdInteger);
            }
            removeMessages(1, userIdInteger);
            obtainMessage(1, userIdInteger).sendToTarget();
        }
    }

    private class Controller2Callback extends MediaController2.ControllerCallback {
        private final Session2Token mToken;

        Controller2Callback(Session2Token token) {
            this.mToken = token;
        }

        @Override // android.media.MediaController2.ControllerCallback
        public void onConnected(MediaController2 controller, Session2CommandGroup allowedCommands) {
            synchronized (MediaSessionService.this.mLock) {
                int userId = UserHandle.getUserId(this.mToken.getUid());
                ((List) MediaSessionService.this.mSession2TokensPerUser.get(userId)).add(this.mToken);
                MediaSessionService.this.pushSession2TokensChangedLocked(userId);
            }
        }

        @Override // android.media.MediaController2.ControllerCallback
        public void onDisconnected(MediaController2 controller) {
            synchronized (MediaSessionService.this.mLock) {
                int userId = UserHandle.getUserId(this.mToken.getUid());
                ((List) MediaSessionService.this.mSession2TokensPerUser.get(userId)).remove(this.mToken);
                MediaSessionService.this.pushSession2TokensChangedLocked(userId);
            }
        }
    }
}
