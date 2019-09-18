package com.android.server.media;

import android.app.ActivityManager;
import android.app.AppGlobals;
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
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.media.AudioPlaybackConfiguration;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IRemoteVolumeController;
import android.media.ISessionTokensListener;
import android.media.MediaController2;
import android.media.SessionToken2;
import android.media.session.IActiveSessionsListener;
import android.media.session.ICallback;
import android.media.session.IOnMediaKeyListener;
import android.media.session.IOnVolumeKeyLongPressListener;
import android.media.session.ISession;
import android.media.session.ISessionCallback;
import android.media.session.ISessionManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.server.HwServiceFactory;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.media.AudioPlayerStateMonitor;
import com.android.server.media.MediaSessionStack;
import com.android.server.wm.WindowManagerService;
import com.huawei.pgmng.log.LogPower;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MediaSessionService extends SystemService implements Watchdog.Monitor {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final boolean DEBUG_KEY_EVENT = true;
    /* access modifiers changed from: private */
    public static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    private static final int IAWARE_APP_TYPE_MUSIC = 7;
    private static final int IAWARE_TRANSACTION_GETAPPTYPEINFO = 10;
    private static final int MEDIA_KEY_LISTENER_TIMEOUT = 1000;
    private static final int NO_ERROR = 0;
    private static final String TAG = "MediaSessionService";
    static final boolean USE_MEDIA2_APIS = false;
    private static final int WAKELOCK_TIMEOUT = 5000;
    /* access modifiers changed from: private */
    public AudioPlayerStateMonitor mAudioPlayerStateMonitor;
    /* access modifiers changed from: private */
    public IAudioService mAudioService;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public FullUserRecord mCurrentFullUserRecord;
    /* access modifiers changed from: private */
    public final SparseIntArray mFullUserIds = new SparseIntArray();
    /* access modifiers changed from: private */
    public MediaSessionRecord mGlobalPrioritySession;
    /* access modifiers changed from: private */
    public final MessageHandler mHandler = new MessageHandler();
    /* access modifiers changed from: private */
    public boolean mHasFeatureLeanback;
    /* access modifiers changed from: private */
    public IBinder mIAwareCMSService;
    /* access modifiers changed from: private */
    public KeyguardManager mKeyguardManager;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final int mLongPressTimeout;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mMediaEventWakeLock;
    /* access modifiers changed from: private */
    public final INotificationManager mNotificationManager;
    private final IPackageManager mPackageManager;
    /* access modifiers changed from: private */
    public IRemoteVolumeController mRvc;
    private final SessionManagerImpl mSessionManagerImpl = new SessionManagerImpl();
    /* access modifiers changed from: private */
    public final Map<SessionToken2, MediaController2> mSessionRecords = new ArrayMap();
    /* access modifiers changed from: private */
    public final List<SessionTokensListenerRecord> mSessionTokensListeners = new ArrayList();
    /* access modifiers changed from: private */
    public final ArrayList<SessionsListenerRecord> mSessionsListeners = new ArrayList<>();
    private SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public final SparseArray<FullUserRecord> mUserRecords = new SparseArray<>();

    private class ControllerCallback extends MediaController2.ControllerCallback {
        private final SessionToken2 mToken;

        ControllerCallback(SessionToken2 token) {
            this.mToken = token;
        }

        public void onDisconnected(MediaController2 controller) {
            MediaSessionService.this.destroySession2Internal(this.mToken);
        }
    }

    final class FullUserRecord implements MediaSessionStack.OnMediaButtonSessionChangedListener {
        private static final String COMPONENT_NAME_USER_ID_DELIM = ",";
        /* access modifiers changed from: private */
        public ICallback mCallback;
        /* access modifiers changed from: private */
        public final int mFullUserId;
        /* access modifiers changed from: private */
        public boolean mInitialDownMusicOnly;
        /* access modifiers changed from: private */
        public KeyEvent mInitialDownVolumeKeyEvent;
        /* access modifiers changed from: private */
        public int mInitialDownVolumeStream;
        /* access modifiers changed from: private */
        public PendingIntent mLastMediaButtonReceiver;
        /* access modifiers changed from: private */
        public IOnMediaKeyListener mOnMediaKeyListener;
        /* access modifiers changed from: private */
        public int mOnMediaKeyListenerUid;
        /* access modifiers changed from: private */
        public IOnVolumeKeyLongPressListener mOnVolumeKeyLongPressListener;
        /* access modifiers changed from: private */
        public int mOnVolumeKeyLongPressListenerUid;
        /* access modifiers changed from: private */
        public final MediaSessionStack mPriorityStack;
        /* access modifiers changed from: private */
        public ComponentName mRestoredMediaButtonReceiver;
        /* access modifiers changed from: private */
        public int mRestoredMediaButtonReceiverUserId;

        public FullUserRecord(int fullUserId) {
            this.mFullUserId = fullUserId;
            this.mPriorityStack = new MediaSessionStack(MediaSessionService.this.mAudioPlayerStateMonitor, this);
            String mediaButtonReceiver = Settings.Secure.getStringForUser(MediaSessionService.this.mContentResolver, "media_button_receiver", this.mFullUserId);
            if (mediaButtonReceiver != null) {
                String[] tokens = mediaButtonReceiver.split(COMPONENT_NAME_USER_ID_DELIM);
                if (tokens != null && tokens.length == 2) {
                    this.mRestoredMediaButtonReceiver = ComponentName.unflattenFromString(tokens[0]);
                    this.mRestoredMediaButtonReceiverUserId = Integer.parseInt(tokens[1]);
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
            pw.println(indent + "Volume key long-press listener: " + this.mOnVolumeKeyLongPressListener);
            pw.println(indent + "Volume key long-press listener package: " + MediaSessionService.this.getCallingPackageName(this.mOnVolumeKeyLongPressListenerUid));
            pw.println(indent + "Media key listener: " + this.mOnMediaKeyListener);
            pw.println(indent + "Media key listener package: " + MediaSessionService.this.getCallingPackageName(this.mOnMediaKeyListenerUid));
            pw.println(indent + "Callback: " + this.mCallback);
            pw.println(indent + "Last MediaButtonReceiver: " + this.mLastMediaButtonReceiver);
            pw.println(indent + "Restored MediaButtonReceiver: " + this.mRestoredMediaButtonReceiver);
            this.mPriorityStack.dump(pw, prefix + "  ");
        }

        public void onMediaButtonSessionChanged(MediaSessionRecord oldMediaButtonSession, MediaSessionRecord newMediaButtonSession) {
            Log.d(MediaSessionService.TAG, "Media button session is changed to " + newMediaButtonSession);
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

        public void rememberMediaButtonReceiverLocked(MediaSessionRecord record) {
            PendingIntent receiver = record.getMediaButtonReceiver();
            this.mLastMediaButtonReceiver = receiver;
            boolean shouldSaveReceiver = false;
            String componentName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            if (receiver != null) {
                ComponentName component = receiver.getIntent().getComponent();
                if (component != null && record.getPackageName().equals(component.getPackageName())) {
                    componentName = component.flattenToString();
                    String packageName = component.getPackageName();
                    if (checkAppHasButtonReceiver(packageName) && isAudioApp(packageName)) {
                        shouldSaveReceiver = true;
                        this.mRestoredMediaButtonReceiver = component;
                    }
                }
            }
            if (shouldSaveReceiver) {
                ContentResolver access$1400 = MediaSessionService.this.mContentResolver;
                Settings.Secure.putStringForUser(access$1400, "media_button_receiver", componentName + COMPONENT_NAME_USER_ID_DELIM + record.getUserId(), this.mFullUserId);
            }
        }

        private boolean checkAppHasButtonReceiver(String packageName) {
            if (packageName == null) {
                Log.v(MediaSessionService.TAG, " checkAppHasButtonReceiver : false, packageName is null");
                return false;
            }
            Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
            mediaButtonIntent.setPackage(packageName);
            List<ResolveInfo> ril = MediaSessionService.this.getContext().getPackageManager().queryBroadcastReceivers(mediaButtonIntent, 0);
            if (ril == null || ril.size() == 0) {
                Log.v(MediaSessionService.TAG, " checkAppHasButtonReceiver : false, packageName: " + packageName);
                return false;
            }
            Log.v(MediaSessionService.TAG, " checkAppHasButtonReceiver : true, packageName: " + packageName);
            return true;
        }

        private IBinder getIAwareCMSService() {
            if (MediaSessionService.this.mIAwareCMSService == null) {
                IBinder unused = MediaSessionService.this.mIAwareCMSService = ServiceManager.getService("IAwareCMSService");
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

        /* access modifiers changed from: private */
        public void pushAddressedPlayerChangedLocked() {
            if (this.mCallback != null) {
                try {
                    MediaSessionRecord mediaButtonSession = getMediaButtonSessionLocked();
                    if (mediaButtonSession != null) {
                        this.mCallback.onAddressedPlayerChangedToMediaSession(new MediaSession.Token(mediaButtonSession.getControllerBinder()));
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
        public MediaSessionRecord getMediaButtonSessionLocked() {
            return MediaSessionService.this.isGlobalPriorityActiveLocked() ? MediaSessionService.this.mGlobalPrioritySession : this.mPriorityStack.getMediaButtonSession();
        }
    }

    final class MessageHandler extends Handler {
        private static final int MSG_SESSIONS_CHANGED = 1;
        private static final int MSG_SESSIONS_TOKENS_CHANGED = 3;
        private static final int MSG_VOLUME_INITIAL_DOWN = 2;
        private final SparseArray<Integer> mIntegerCache = new SparseArray<>();

        MessageHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MediaSessionService.this.pushSessionsChanged(((Integer) msg.obj).intValue());
                    return;
                case 2:
                    synchronized (MediaSessionService.this.mLock) {
                        FullUserRecord user = (FullUserRecord) MediaSessionService.this.mUserRecords.get(msg.arg1);
                        if (!(user == null || user.mInitialDownVolumeKeyEvent == null)) {
                            MediaSessionService.this.dispatchVolumeKeyLongPressLocked(user.mInitialDownVolumeKeyEvent);
                            KeyEvent unused = user.mInitialDownVolumeKeyEvent = null;
                        }
                    }
                    return;
                case 3:
                    MediaSessionService.this.pushSessionTokensChanged(((Integer) msg.obj).intValue());
                    return;
                default:
                    return;
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

    class SessionManagerImpl extends ISessionManager.Stub {
        private static final String EXTRA_WAKELOCK_ACQUIRED = "android.media.AudioService.WAKELOCK_ACQUIRED";
        private static final int WAKELOCK_RELEASE_ON_FINISHED = 1980;
        private IHwBehaviorCollectManager mHwBehaviorManager;
        BroadcastReceiver mKeyEventDone = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        synchronized (MediaSessionService.this.mLock) {
                            if (extras.containsKey(SessionManagerImpl.EXTRA_WAKELOCK_ACQUIRED) && MediaSessionService.this.mMediaEventWakeLock.isHeld()) {
                                MediaSessionService.this.mMediaEventWakeLock.release();
                            }
                        }
                    }
                }
            }
        };
        private KeyEventWakeLockReceiver mKeyEventReceiver = new KeyEventWakeLockReceiver(MediaSessionService.this.mHandler);
        private boolean mVoiceButtonDown = false;
        private boolean mVoiceButtonHandled = false;

        class KeyEventWakeLockReceiver extends ResultReceiver implements Runnable, PendingIntent.OnFinished {
            private final Handler mHandler;
            /* access modifiers changed from: private */
            public int mLastTimeoutId = 0;
            private int mRefCount = 0;

            public KeyEventWakeLockReceiver(Handler handler) {
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

            public void run() {
                onTimeout();
            }

            /* access modifiers changed from: protected */
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

            public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
                onReceiveResult(resultCode, null);
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

            public void run() {
                Log.d(MediaSessionService.TAG, "The media key listener is timed-out for " + this.mKeyEvent);
                dispatchMediaKeyEvent();
            }

            /* access modifiers changed from: protected */
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

        SessionManagerImpl() {
        }

        public ISession createSession(String packageName, ISessionCallback cb, String tag, int userId) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionService.this.enforcePackageName(packageName, uid);
                int resolvedUserId = ActivityManager.handleIncomingUser(pid, uid, userId, false, true, "createSession", packageName);
                if (cb != null) {
                    return MediaSessionService.this.createSessionInternal(pid, uid, resolvedUserId, packageName, cb, tag).getSessionBinder();
                }
                throw new IllegalArgumentException("Controller callback cannot be null");
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public List<IBinder> getSessions(ComponentName componentName, int userId) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int resolvedUserId = verifySessionsRequest(componentName, userId, pid, uid);
                ArrayList<IBinder> binders = new ArrayList<>();
                synchronized (MediaSessionService.this.mLock) {
                    for (MediaSessionRecord record : MediaSessionService.this.getActiveSessionsLocked(resolvedUserId)) {
                        binders.add(record.getControllerBinder().asBinder());
                    }
                }
                Binder.restoreCallingIdentity(token);
                return binders;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void addSessionsListener(IActiveSessionsListener listener, ComponentName componentName, int userId) throws RemoteException {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            ComponentName componentName2 = componentName;
            try {
                int resolvedUserId = verifySessionsRequest(componentName2, userId, pid, uid);
                synchronized (MediaSessionService.this.mLock) {
                    IActiveSessionsListener iActiveSessionsListener = listener;
                    int index = MediaSessionService.this.findIndexOfSessionsListenerLocked(iActiveSessionsListener);
                    if (index != -1) {
                        Log.w(MediaSessionService.TAG, "ActiveSessionsListener is already added, ignoring");
                        return;
                    }
                    int i = index;
                    SessionsListenerRecord sessionsListenerRecord = new SessionsListenerRecord(iActiveSessionsListener, componentName2, resolvedUserId, pid, uid);
                    SessionsListenerRecord record = sessionsListenerRecord;
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
                        record.mListener.asBinder().unlinkToDeath(record, 0);
                    } catch (Exception e) {
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:58:0x0141 A[Catch:{ all -> 0x0173 }] */
        public void dispatchMediaKeyEvent(String packageName, boolean asSystemService, KeyEvent keyEvent, boolean needWakeLock) {
            KeyEvent keyEvent2 = keyEvent;
            if (keyEvent2 == null || !KeyEvent.isMediaKey(keyEvent.getKeyCode())) {
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-media key event.");
                return;
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                List<MediaSessionRecord> records = MediaSessionService.this.getActiveSessionsLocked(-1);
                int size = records.size();
                StringBuilder sb = new StringBuilder();
                sb.append("dispatchMediaKeyEvent, pkg=");
                String str = packageName;
                sb.append(str);
                sb.append(" pid=");
                sb.append(pid);
                sb.append(", uid=");
                sb.append(uid);
                sb.append(", asSystem=");
                sb.append(asSystemService);
                sb.append(", record_size=");
                sb.append(size);
                sb.append(", event=");
                sb.append(keyEvent2);
                Log.d(MediaSessionService.TAG, sb.toString());
                if (!isUserSetupComplete()) {
                    Slog.i(MediaSessionService.TAG, "Not dispatching media key event because user setup is in progress.");
                } else if (!MediaSessionService.HWRIDEMODE_FEATURE_SUPPORTED || !SystemProperties.getBoolean("sys.ride_mode", false)) {
                    synchronized (MediaSessionService.this.mLock) {
                        try {
                            boolean isGlobalPriorityActive = MediaSessionService.this.isGlobalPriorityActiveLocked();
                            if (!isGlobalPriorityActive || uid == 1000) {
                                if (!isGlobalPriorityActive) {
                                    if (MediaSessionService.this.mCurrentFullUserRecord.mOnMediaKeyListener != null) {
                                        Log.d(MediaSessionService.TAG, "Send " + keyEvent2 + " to the media key listener");
                                        try {
                                            IOnMediaKeyListener access$3100 = MediaSessionService.this.mCurrentFullUserRecord.mOnMediaKeyListener;
                                            r1 = r1;
                                            MediaKeyListenerResultReceiver mediaKeyListenerResultReceiver = r1;
                                            int i = size;
                                            List<MediaSessionRecord> list = records;
                                            try {
                                                MediaKeyListenerResultReceiver mediaKeyListenerResultReceiver2 = new MediaKeyListenerResultReceiver(str, pid, uid, asSystemService, keyEvent2, needWakeLock);
                                                access$3100.onMediaKey(keyEvent2, mediaKeyListenerResultReceiver);
                                            } catch (RemoteException e) {
                                                Log.w(MediaSessionService.TAG, "Failed to send " + keyEvent2 + " to the media key listener");
                                                if (!isGlobalPriorityActive) {
                                                }
                                                dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent2, needWakeLock);
                                                Binder.restoreCallingIdentity(token);
                                                return;
                                            }
                                        } catch (RemoteException e2) {
                                            int i2 = size;
                                            List<MediaSessionRecord> list2 = records;
                                            Log.w(MediaSessionService.TAG, "Failed to send " + keyEvent2 + " to the media key listener");
                                            if (!isGlobalPriorityActive) {
                                            }
                                            dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent2, needWakeLock);
                                            Binder.restoreCallingIdentity(token);
                                            return;
                                        }
                                        try {
                                            Binder.restoreCallingIdentity(token);
                                            return;
                                        } catch (Throwable th) {
                                            th = th;
                                            throw th;
                                        }
                                    }
                                }
                                List<MediaSessionRecord> list3 = records;
                                if (!isGlobalPriorityActive || !isVoiceKey(keyEvent.getKeyCode())) {
                                    dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent2, needWakeLock);
                                } else {
                                    handleVoiceKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent2, needWakeLock);
                                }
                            } else {
                                try {
                                    Slog.i(MediaSessionService.TAG, "Only the system can dispatch media key event to the global priority session.");
                                    Binder.restoreCallingIdentity(token);
                                } catch (Throwable th2) {
                                    th = th2;
                                    int i3 = size;
                                    List<MediaSessionRecord> list4 = records;
                                    throw th;
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            int i4 = size;
                            List<MediaSessionRecord> list5 = records;
                            throw th;
                        }
                    }
                } else {
                    Slog.i(MediaSessionService.TAG, "Not dispatching media key event because Ride mode is enabled");
                    Binder.restoreCallingIdentity(token);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setCallback(ICallback callback) {
            int callingPid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (UserHandle.isSameApp(uid, 1002)) {
                    synchronized (MediaSessionService.this.mLock) {
                        int userId = UserHandle.getUserId(uid);
                        final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                        if (user != null) {
                            if (user.mFullUserId == userId) {
                                ICallback unused = user.mCallback = callback;
                                Log.d(MediaSessionService.TAG, "The callback " + user.mCallback + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                                if (user.mCallback == null) {
                                    Binder.restoreCallingIdentity(token);
                                    return;
                                }
                                try {
                                    user.mCallback.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                                        public void binderDied() {
                                            synchronized (MediaSessionService.this.mLock) {
                                                ICallback unused = user.mCallback = null;
                                            }
                                        }
                                    }, 0);
                                    user.pushAddressedPlayerChangedLocked();
                                } catch (RemoteException e) {
                                    Log.w(MediaSessionService.TAG, "Failed to set callback", e);
                                    ICallback unused2 = user.mCallback = null;
                                }
                            }
                        }
                        Log.w(MediaSessionService.TAG, "Only the full user can set the callback, userId=" + userId);
                        return;
                    }
                }
                throw new SecurityException("Only Bluetooth service processes can set Callback");
                Binder.restoreCallingIdentity(token);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setOnVolumeKeyLongPressListener(IOnVolumeKeyLongPressListener listener) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaSessionService.this.getContext().checkPermission("android.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER", pid, uid) == 0) {
                    synchronized (MediaSessionService.this.mLock) {
                        int userId = UserHandle.getUserId(uid);
                        final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                        if (user != null) {
                            if (user.mFullUserId == userId) {
                                if (user.mOnVolumeKeyLongPressListener == null || user.mOnVolumeKeyLongPressListenerUid == uid) {
                                    IOnVolumeKeyLongPressListener unused = user.mOnVolumeKeyLongPressListener = listener;
                                    int unused2 = user.mOnVolumeKeyLongPressListenerUid = uid;
                                    Log.d(MediaSessionService.TAG, "The volume key long-press listener " + listener + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                                    if (user.mOnVolumeKeyLongPressListener != null) {
                                        try {
                                            user.mOnVolumeKeyLongPressListener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                                                public void binderDied() {
                                                    synchronized (MediaSessionService.this.mLock) {
                                                        IOnVolumeKeyLongPressListener unused = user.mOnVolumeKeyLongPressListener = null;
                                                    }
                                                }
                                            }, 0);
                                        } catch (RemoteException e) {
                                            Log.w(MediaSessionService.TAG, "Failed to set death recipient " + user.mOnVolumeKeyLongPressListener);
                                            IOnVolumeKeyLongPressListener unused3 = user.mOnVolumeKeyLongPressListener = null;
                                        }
                                    }
                                } else {
                                    Log.w(MediaSessionService.TAG, "The volume key long-press listener cannot be reset by another app , mOnVolumeKeyLongPressListener=" + user.mOnVolumeKeyLongPressListenerUid + ", uid=" + uid);
                                    Binder.restoreCallingIdentity(token);
                                    return;
                                }
                            }
                        }
                        Log.w(MediaSessionService.TAG, "Only the full user can set the volume key long-press listener, userId=" + userId);
                        return;
                    }
                }
                throw new SecurityException("Must hold the SET_VOLUME_KEY_LONG_PRESS_LISTENER permission.");
                Binder.restoreCallingIdentity(token);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setOnMediaKeyListener(IOnMediaKeyListener listener) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaSessionService.this.getContext().checkPermission("android.permission.SET_MEDIA_KEY_LISTENER", pid, uid) == 0) {
                    synchronized (MediaSessionService.this.mLock) {
                        int userId = UserHandle.getUserId(uid);
                        final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                        if (user != null) {
                            if (user.mFullUserId == userId) {
                                if (user.mOnMediaKeyListener == null || user.mOnMediaKeyListenerUid == uid) {
                                    IOnMediaKeyListener unused = user.mOnMediaKeyListener = listener;
                                    int unused2 = user.mOnMediaKeyListenerUid = uid;
                                    Log.d(MediaSessionService.TAG, "The media key listener " + user.mOnMediaKeyListener + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                                    if (user.mOnMediaKeyListener != null) {
                                        try {
                                            user.mOnMediaKeyListener.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                                                public void binderDied() {
                                                    synchronized (MediaSessionService.this.mLock) {
                                                        IOnMediaKeyListener unused = user.mOnMediaKeyListener = null;
                                                    }
                                                }
                                            }, 0);
                                        } catch (RemoteException e) {
                                            Log.w(MediaSessionService.TAG, "Failed to set death recipient " + user.mOnMediaKeyListener);
                                            IOnMediaKeyListener unused3 = user.mOnMediaKeyListener = null;
                                        }
                                    }
                                } else {
                                    Log.w(MediaSessionService.TAG, "The media key listener cannot be reset by another app. , mOnMediaKeyListenerUid=" + user.mOnMediaKeyListenerUid + ", uid=" + uid);
                                    Binder.restoreCallingIdentity(token);
                                    return;
                                }
                            }
                        }
                        Log.w(MediaSessionService.TAG, "Only the full user can set the media key listener, userId=" + userId);
                        return;
                    }
                }
                throw new SecurityException("Must hold the SET_MEDIA_KEY_LISTENER permission.");
                Binder.restoreCallingIdentity(token);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:54:0x01a9, code lost:
            return;
         */
        public void dispatchVolumeKeyEvent(String packageName, boolean asSystemService, KeyEvent keyEvent, int stream, boolean musicOnly) {
            KeyEvent keyEvent2 = keyEvent;
            if (keyEvent2 == null || !(keyEvent.getKeyCode() == 24 || keyEvent.getKeyCode() == 25 || keyEvent.getKeyCode() == 164)) {
                String str = packageName;
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-volume key event.");
                return;
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            StringBuilder sb = new StringBuilder();
            sb.append("dispatchVolumeKeyEvent, pkg=");
            String str2 = packageName;
            sb.append(str2);
            sb.append(", pid=");
            sb.append(pid);
            sb.append(", uid=");
            sb.append(uid);
            sb.append(", asSystem=");
            boolean z = asSystemService;
            sb.append(z);
            sb.append(", event=");
            sb.append(keyEvent2);
            Log.d(MediaSessionService.TAG, sb.toString());
            try {
                synchronized (MediaSessionService.this.mLock) {
                    try {
                        if (!MediaSessionService.this.isGlobalPriorityActiveLocked()) {
                            if (MediaSessionService.this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener != null) {
                                if (keyEvent.getAction() == 0) {
                                    try {
                                        if (keyEvent.getRepeatCount() == 0) {
                                            KeyEvent unused = MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent = KeyEvent.obtain(keyEvent);
                                            int unused2 = MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeStream = stream;
                                            boolean unused3 = MediaSessionService.this.mCurrentFullUserRecord.mInitialDownMusicOnly = musicOnly;
                                            MediaSessionService.this.mHandler.sendMessageDelayed(MediaSessionService.this.mHandler.obtainMessage(2, MediaSessionService.this.mCurrentFullUserRecord.mFullUserId, 0), (long) MediaSessionService.this.mLongPressTimeout);
                                        } else {
                                            int i = stream;
                                            boolean z2 = musicOnly;
                                        }
                                        if (keyEvent.getRepeatCount() > 0 || keyEvent.isLongPress()) {
                                            MediaSessionService.this.mHandler.removeMessages(2);
                                            if (MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent != null) {
                                                MediaSessionService.this.dispatchVolumeKeyLongPressLocked(MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent);
                                                KeyEvent unused4 = MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent = null;
                                            }
                                            MediaSessionService.this.dispatchVolumeKeyLongPressLocked(keyEvent2);
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        int i2 = stream;
                                        boolean z3 = musicOnly;
                                        throw th;
                                    }
                                } else {
                                    int i3 = stream;
                                    boolean z4 = musicOnly;
                                    MediaSessionService.this.mHandler.removeMessages(2);
                                    if (MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent == null || MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent.getDownTime() != keyEvent.getDownTime()) {
                                        MediaSessionService.this.dispatchVolumeKeyLongPressLocked(keyEvent2);
                                    } else {
                                        dispatchVolumeKeyEventLocked(str2, pid, uid, z, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeStream, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownMusicOnly);
                                        dispatchVolumeKeyEventLocked(str2, pid, uid, asSystemService, keyEvent2, stream, musicOnly);
                                    }
                                }
                            }
                        }
                        dispatchVolumeKeyEventLocked(str2, pid, uid, asSystemService, keyEvent2, stream, musicOnly);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private void dispatchVolumeKeyEventLocked(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, int stream, boolean musicOnly) {
            int flags;
            boolean up = false;
            boolean down = keyEvent.getAction() == 0;
            if (keyEvent.getAction() == 1) {
                up = true;
            }
            int direction = 0;
            boolean isMute = false;
            int keyCode = keyEvent.getKeyCode();
            if (keyCode != 164) {
                switch (keyCode) {
                    case 24:
                        direction = 1;
                        break;
                    case WindowManagerService.H.SHOW_STRICT_MODE_VIOLATION /*25*/:
                        direction = -1;
                        break;
                }
            } else {
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
                    dispatchAdjustVolumeLocked(packageName, pid, uid, asSystemService, stream, direction, flags);
                } else if (isMute && down && keyEvent.getRepeatCount() == 0) {
                    dispatchAdjustVolumeLocked(packageName, pid, uid, asSystemService, stream, 101, flags);
                }
            }
        }

        public void dispatchAdjustVolume(String packageName, int suggestedStream, int delta, int flags) {
            sendBehavior(IHwBehaviorCollectManager.BehaviorId.MEDIASESSION_DISPATCHADJUSTVOLUME, new Object[0]);
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (MediaSessionService.this.mLock) {
                    dispatchAdjustVolumeLocked(packageName, pid, uid, false, suggestedStream, delta, flags);
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void setRemoteVolumeController(IRemoteVolumeController rvc) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionService.this.enforceSystemUiPermission("listen for volume changes", pid, uid);
                IRemoteVolumeController unused = MediaSessionService.this.mRvc = rvc;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isGlobalPriorityActive() {
            boolean access$2200;
            synchronized (MediaSessionService.this.mLock) {
                access$2200 = MediaSessionService.this.isGlobalPriorityActiveLocked();
            }
            return access$2200;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(MediaSessionService.this.getContext(), MediaSessionService.TAG, pw)) {
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
                        ((FullUserRecord) MediaSessionService.this.mUserRecords.valueAt(i)).dumpLocked(pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    }
                    MediaSessionService.this.mAudioPlayerStateMonitor.dump(MediaSessionService.this.getContext(), pw, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
            }
        }

        public boolean isTrusted(String controllerPackageName, int controllerPid, int controllerUid) throws RemoteException {
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaSessionService.this.getContext().getPackageManager().getPackageUidAsUser(controllerPackageName, UserHandle.getUserId(controllerUid)) == controllerUid) {
                    return hasMediaControlPermission(UserHandle.getUserId(uid), controllerPackageName, controllerPid, controllerUid);
                }
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "Package name " + controllerPackageName + " doesn't match with the uid " + controllerUid);
                }
                return false;
            } catch (PackageManager.NameNotFoundException e) {
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "Package " + controllerPackageName + " doesn't exist");
                }
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean createSession2(Bundle sessionToken) {
            return false;
        }

        public void destroySession2(Bundle sessionToken) {
        }

        public List<Bundle> getSessionTokens(boolean activeSessionOnly, boolean sessionServiceOnly, String packageName) throws RemoteException {
            return null;
        }

        public void addSessionTokensListener(ISessionTokensListener listener, int userId, String packageName) throws RemoteException {
        }

        public void removeSessionTokensListener(ISessionTokensListener listener, String packageName) throws RemoteException {
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

        private int verifySessionsRequest2(int targetUserId, String callerPackageName, int callerPid, int callerUid) throws RemoteException {
            int resolvedUserId = ActivityManager.handleIncomingUser(callerPid, callerUid, targetUserId, true, true, "getSessionTokens", callerPackageName);
            if (hasMediaControlPermission(resolvedUserId, callerPackageName, callerPid, callerUid)) {
                return resolvedUserId;
            }
            throw new SecurityException("Missing permission to control media.");
        }

        private boolean hasMediaControlPermission(int resolvedUserId, String packageName, int pid, int uid) throws RemoteException {
            if (MediaSessionService.this.isCurrentVolumeController(pid, uid) || uid == 1000 || MediaSessionService.this.getContext().checkPermission("android.permission.MEDIA_CONTENT_CONTROL", pid, uid) == 0) {
                return true;
            }
            if (MediaSessionService.DEBUG) {
                Log.d(MediaSessionService.TAG, packageName + " (uid=" + uid + ") hasn't granted MEDIA_CONTENT_CONTROL");
            }
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
            if (MediaSessionService.DEBUG) {
                Log.d(MediaSessionService.TAG, packageName + " (uid=" + uid + ") doesn't have an enabled notification listener");
            }
            return false;
        }

        private void dispatchAdjustVolumeLocked(String packageName, int pid, int uid, boolean asSystemService, int suggestedStream, int direction, int flags) {
            MediaSessionRecord mediaSessionRecord;
            int suggestedStream2 = suggestedStream;
            final int i = direction;
            final int i2 = flags;
            if (MediaSessionService.this.isGlobalPriorityActiveLocked()) {
                mediaSessionRecord = MediaSessionService.this.mGlobalPrioritySession;
            } else {
                mediaSessionRecord = MediaSessionService.this.mCurrentFullUserRecord.mPriorityStack.getDefaultVolumeSession();
            }
            MediaSessionRecord session = mediaSessionRecord;
            boolean preferSuggestedStream = false;
            if (isValidLocalStreamType(suggestedStream2) && AudioSystem.isStreamActive(suggestedStream2, 0)) {
                preferSuggestedStream = true;
            }
            boolean preferSuggestedStream2 = preferSuggestedStream;
            Log.d(MediaSessionService.TAG, "Adjusting " + session + " by " + i + ". flags=" + i2 + ", suggestedStream=" + suggestedStream2 + ", preferSuggestedStream=" + preferSuggestedStream2);
            if (session == null || preferSuggestedStream2) {
                if ((i2 & 512) != 0 && !AudioSystem.isStreamActive(3, 0)) {
                    if (suggestedStream2 != Integer.MIN_VALUE || !AudioSystem.isStreamActive(0, 0)) {
                        Log.d(MediaSessionService.TAG, "No active session to adjust, skipping media only volume event");
                        return;
                    } else {
                        Log.d(MediaSessionService.TAG, "set suggestedStream to voice call");
                        suggestedStream2 = 0;
                    }
                }
                final int temp = suggestedStream2;
                MediaSessionService.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            MediaSessionService.this.mAudioService.adjustSuggestedStreamVolume(i, temp, i2, MediaSessionService.this.getContext().getOpPackageName(), MediaSessionService.TAG);
                        } catch (RemoteException e) {
                            Log.e(MediaSessionService.TAG, "Error adjusting default volume.", e);
                        } catch (IllegalArgumentException e2) {
                            Log.e(MediaSessionService.TAG, "IllegalArgument when adjust stream volume.", e2);
                        }
                    }
                });
            } else {
                session.adjustVolume(packageName, pid, uid, null, asSystemService, i, i2, true);
            }
        }

        /* access modifiers changed from: private */
        public void handleVoiceKeyEventLocked(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, boolean needWakeLock) {
            int action = keyEvent.getAction();
            boolean isLongPress = (keyEvent.getFlags() & 128) != 0;
            if (action != 0) {
                boolean z = needWakeLock;
                if (action == 1 && this.mVoiceButtonDown) {
                    this.mVoiceButtonDown = false;
                    if (!this.mVoiceButtonHandled && !keyEvent.isCanceled()) {
                        dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, KeyEvent.changeAction(keyEvent, 0), z);
                        dispatchMediaKeyEventLocked(packageName, pid, uid, asSystemService, keyEvent, needWakeLock);
                        return;
                    }
                }
            } else if (keyEvent.getRepeatCount() == 0) {
                this.mVoiceButtonDown = true;
                this.mVoiceButtonHandled = false;
                KeyEvent keyEvent2 = keyEvent;
                boolean z2 = needWakeLock;
                return;
            } else if (!this.mVoiceButtonDown || this.mVoiceButtonHandled || !isLongPress) {
                boolean z3 = needWakeLock;
            } else {
                this.mVoiceButtonHandled = true;
                startVoiceInput(needWakeLock);
            }
            KeyEvent keyEvent3 = keyEvent;
        }

        /* access modifiers changed from: private */
        public void dispatchMediaKeyEventLocked(String packageName, int pid, int uid, boolean asSystemService, KeyEvent keyEvent, boolean needWakeLock) {
            KeyEvent keyEvent2 = keyEvent;
            MediaSessionRecord session = MediaSessionService.this.mCurrentFullUserRecord.getMediaButtonSessionLocked();
            int i = -1;
            if (session != null) {
                Log.d(MediaSessionService.TAG, "Sending " + keyEvent2 + " to " + session);
                if (keyEvent.getAction() == 0) {
                    HwServiceFactory.reportMediaKeyToIAware(session.mOwnerUid);
                    LogPower.push(148, "mediakey", session.mPackageName, Integer.toString(session.mOwnerPid), new String[]{String.valueOf(keyEvent.getKeyCode())});
                }
                if (needWakeLock) {
                    this.mKeyEventReceiver.aquireWakeLockLocked();
                }
                if (needWakeLock) {
                    i = this.mKeyEventReceiver.mLastTimeoutId;
                }
                session.sendMediaButton(packageName, pid, uid, asSystemService, keyEvent2, i, this.mKeyEventReceiver);
                if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                    try {
                        MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaSession(keyEvent2, new MediaSession.Token(session.getControllerBinder()));
                    } catch (RemoteException e) {
                        Log.w(MediaSessionService.TAG, "Failed to send callback", e);
                    }
                }
            } else if (MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver == null && MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiver == null) {
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "Sending media key ordered broadcast");
                }
                if (needWakeLock) {
                    MediaSessionService.this.mMediaEventWakeLock.acquire();
                }
                Intent keyIntent = new Intent("android.intent.action.MEDIA_BUTTON", null);
                keyIntent.addFlags(268435456);
                keyIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent2);
                if (needWakeLock) {
                    keyIntent.putExtra(EXTRA_WAKELOCK_ACQUIRED, WAKELOCK_RELEASE_ON_FINISHED);
                }
                if (checkPackage("com.android.mediacenter")) {
                    Log.d(MediaSessionService.TAG, "Sending media key to mediacenter apk");
                    keyIntent.setPackage("com.android.mediacenter");
                    keyIntent.addFlags(32);
                }
                MediaSessionService.this.getContext().sendOrderedBroadcastAsUser(keyIntent, UserHandle.CURRENT, null, this.mKeyEventDone, MediaSessionService.this.mHandler, -1, null, null);
            } else {
                if (needWakeLock) {
                    this.mKeyEventReceiver.aquireWakeLockLocked();
                }
                Intent mediaButtonIntent = new Intent("android.intent.action.MEDIA_BUTTON");
                mediaButtonIntent.addFlags(268435456);
                mediaButtonIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent2);
                mediaButtonIntent.putExtra("android.intent.extra.PACKAGE_NAME", asSystemService ? MediaSessionService.this.getContext().getPackageName() : packageName);
                try {
                    if (MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver != null) {
                        PendingIntent receiver = MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver;
                        Log.d(MediaSessionService.TAG, "Sending " + keyEvent2 + " to the last known PendingIntent " + receiver);
                        Context context = MediaSessionService.this.getContext();
                        if (needWakeLock) {
                            i = this.mKeyEventReceiver.mLastTimeoutId;
                        }
                        receiver.send(context, i, mediaButtonIntent, this.mKeyEventReceiver, MediaSessionService.this.mHandler);
                        if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                            ComponentName componentName = MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver.getIntent().getComponent();
                            if (componentName != null) {
                                MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaButtonReceiver(keyEvent2, componentName);
                            }
                        }
                        return;
                    }
                    ComponentName receiver2 = MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiver;
                    Log.d(MediaSessionService.TAG, "Sending " + keyEvent2 + " to the restored intent " + receiver2);
                    mediaButtonIntent.setComponent(receiver2);
                    MediaSessionService.this.getContext().sendBroadcastAsUser(mediaButtonIntent, UserHandle.of(MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiverUserId));
                    if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                        MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaButtonReceiver(keyEvent2, receiver2);
                    }
                } catch (PendingIntent.CanceledException e2) {
                    Log.i(MediaSessionService.TAG, "Error sending key event to media button receiver " + MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver, e2);
                } catch (RemoteException e3) {
                    Log.w(MediaSessionService.TAG, "Failed to send callback", e3);
                }
            }
        }

        private boolean checkPackage(String packageName) {
            if (packageName == null || BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(packageName)) {
                return false;
            }
            try {
                ApplicationInfo applicationInfo = MediaSessionService.this.getContext().getPackageManager().getApplicationInfo(packageName, 8192);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        private void startVoiceInput(boolean needWakeLock) {
            Intent voiceIntent;
            PowerManager pm = (PowerManager) MediaSessionService.this.getContext().getSystemService("power");
            boolean z = false;
            boolean isLocked = MediaSessionService.this.mKeyguardManager != null && MediaSessionService.this.mKeyguardManager.isKeyguardLocked();
            if (isLocked || !pm.isScreenOn()) {
                voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
                if (isLocked && MediaSessionService.this.mKeyguardManager.isKeyguardSecure()) {
                    z = true;
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
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "voiceIntent: " + voiceIntent);
                }
                MediaSessionService.this.getContext().startActivityAsUser(voiceIntent, UserHandle.CURRENT);
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
        public boolean isVoiceKey(int keyCode) {
            return keyCode == 79 || (!MediaSessionService.this.mHasFeatureLeanback && keyCode == 85);
        }

        private boolean isUserSetupComplete() {
            return Settings.Secure.getIntForUser(MediaSessionService.this.getContext().getContentResolver(), "user_setup_complete", 0, -2) != 0;
        }

        private boolean isValidLocalStreamType(int streamType) {
            return streamType >= 0 && streamType <= 5;
        }

        private void sendBehavior(IHwBehaviorCollectManager.BehaviorId bid, Object... params) {
            if (this.mHwBehaviorManager == null) {
                this.mHwBehaviorManager = HwFrameworkFactory.getHwBehaviorCollectManager();
            }
            if (this.mHwBehaviorManager == null) {
                Log.w(MediaSessionService.TAG, "HwBehaviorCollectManager is null");
            } else if (params == null || params.length == 0) {
                this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid);
            } else {
                this.mHwBehaviorManager.sendBehavior(Binder.getCallingUid(), Binder.getCallingPid(), bid, params);
            }
        }
    }

    private final class SessionTokensListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final ISessionTokensListener mListener;
        /* access modifiers changed from: private */
        public final int mUserId;

        public SessionTokensListenerRecord(ISessionTokensListener listener, int userId) {
            this.mListener = listener;
            this.mUserId = userId;
        }

        public void binderDied() {
            synchronized (MediaSessionService.this.mLock) {
                MediaSessionService.this.mSessionTokensListeners.remove(this);
            }
        }
    }

    final class SessionsListenerRecord implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final ComponentName mComponentName;
        /* access modifiers changed from: private */
        public final IActiveSessionsListener mListener;
        /* access modifiers changed from: private */
        public final int mPid;
        /* access modifiers changed from: private */
        public final int mUid;
        /* access modifiers changed from: private */
        public final int mUserId;

        public SessionsListenerRecord(IActiveSessionsListener listener, ComponentName componentName, int userId, int pid, int uid) {
            this.mListener = listener;
            this.mComponentName = componentName;
            this.mUserId = userId;
            this.mPid = pid;
            this.mUid = uid;
        }

        public void binderDied() {
            synchronized (MediaSessionService.this.mLock) {
                MediaSessionService.this.mSessionsListeners.remove(this);
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
        public void observe() {
            MediaSessionService.this.mContentResolver.registerContentObserver(this.mSecureSettingsUri, false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri) {
            MediaSessionService.this.updateActiveSessionListeners();
        }
    }

    public MediaSessionService(Context context) {
        super(context);
        this.mMediaEventWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "handleMediaEvent");
        this.mLongPressTimeout = ViewConfiguration.getLongPressTimeout();
        this.mNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        this.mPackageManager = AppGlobals.getPackageManager();
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.media.MediaSessionService$SessionManagerImpl, android.os.IBinder] */
    public void onStart() {
        publishBinderService("media_session", this.mSessionManagerImpl);
        Watchdog.getInstance().addMonitor(this);
        this.mKeyguardManager = (KeyguardManager) getContext().getSystemService("keyguard");
        this.mAudioService = getAudioService();
        this.mAudioPlayerStateMonitor = AudioPlayerStateMonitor.getInstance();
        this.mAudioPlayerStateMonitor.registerListener(new AudioPlayerStateMonitor.OnAudioPlayerActiveStateChangedListener() {
            public final void onAudioPlayerActiveStateChanged(AudioPlaybackConfiguration audioPlaybackConfiguration, boolean z) {
                MediaSessionService.lambda$onStart$0(MediaSessionService.this, audioPlaybackConfiguration, z);
            }
        }, null);
        this.mAudioPlayerStateMonitor.registerSelfIntoAudioServiceIfNeeded(this.mAudioService);
        this.mContentResolver = getContext().getContentResolver();
        this.mSettingsObserver = new SettingsObserver();
        this.mSettingsObserver.observe();
        this.mHasFeatureLeanback = getContext().getPackageManager().hasSystemFeature("android.software.leanback");
        updateUser();
        registerPackageBroadcastReceivers();
        buildMediaSessionService2List();
    }

    public static /* synthetic */ void lambda$onStart$0(MediaSessionService mediaSessionService, AudioPlaybackConfiguration config, boolean isRemoved) {
        if (!isRemoved && config.isActive() && config.getPlayerType() != 3) {
            synchronized (mediaSessionService.mLock) {
                FullUserRecord user = mediaSessionService.getFullUserRecordLocked(UserHandle.getUserId(config.getClientUid()));
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
    public boolean isGlobalPriorityActiveLocked() {
        return this.mGlobalPrioritySession != null && this.mGlobalPrioritySession.isActive();
    }

    public void updateSession(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user == null) {
                Log.w(TAG, "Unknown session updated. Ignoring.");
                return;
            }
            if ((record.getFlags() & 65536) != 0) {
                Log.d(TAG, "Global priority session is updated, active=" + record.isActive());
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

    public void setGlobalPrioritySession(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (this.mGlobalPrioritySession != record) {
                Log.d(TAG, "Global priority session is changed from " + this.mGlobalPrioritySession + " to " + record);
                this.mGlobalPrioritySession = record;
                if (user != null && user.mPriorityStack.contains(record)) {
                    user.mPriorityStack.removeSession(record);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public List<MediaSessionRecord> getActiveSessionsLocked(int userId) {
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

    public void notifyRemoteVolumeChanged(int flags, MediaSessionRecord session) {
        if (this.mRvc != null && session.isActive()) {
            try {
                this.mRvc.remoteVolumeChanged(session.getControllerBinder(), flags);
            } catch (Exception e) {
                Log.wtf(TAG, "Error sending volume change to system UI.", e);
            }
        }
    }

    public void onSessionPlaystateChanged(MediaSessionRecord record, int oldState, int newState) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user != null) {
                if (user.mPriorityStack.contains(record)) {
                    user.mPriorityStack.onPlaystateChanged(record, oldState, newState);
                    return;
                }
            }
            Log.d(TAG, "Unknown session changed playback state. Ignoring.");
        }
    }

    public void onSessionPlaybackTypeChanged(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user != null) {
                if (user.mPriorityStack.contains(record)) {
                    pushRemoteVolumeUpdateLocked(record.getUserId());
                    return;
                }
            }
            Log.d(TAG, "Unknown session changed playback type. Ignoring.");
        }
    }

    public void onStartUser(int userId) {
        if (DEBUG) {
            Log.d(TAG, "onStartUser: " + userId);
        }
        updateUser();
    }

    public void onSwitchUser(int userId) {
        if (DEBUG) {
            Log.d(TAG, "onSwitchUser: " + userId);
        }
        updateUser();
    }

    public void onStopUser(int userId) {
        if (DEBUG) {
            Log.d(TAG, "onStopUser: " + userId);
        }
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
            updateUser();
        }
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    /* access modifiers changed from: protected */
    public void enforcePhoneStatePermission(int pid, int uid) {
        if (getContext().checkPermission("android.permission.MODIFY_PHONE_STATE", pid, uid) != 0) {
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
            List<UserInfo> allUsers = ((UserManager) getContext().getSystemService("user")).getUsers();
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
                }
            }
            int currentFullUserId = ActivityManager.getCurrentUser();
            this.mCurrentFullUserRecord = this.mUserRecords.get(currentFullUserId);
            if (this.mCurrentFullUserRecord == null) {
                Log.w(TAG, "Cannot find FullUserInfo for the current user " + currentFullUserId);
                this.mCurrentFullUserRecord = new FullUserRecord(currentFullUserId);
                this.mUserRecords.put(currentFullUserId, this.mCurrentFullUserRecord);
            }
            this.mFullUserIds.put(currentFullUserId, currentFullUserId);
        }
    }

    /* access modifiers changed from: private */
    public void updateActiveSessionListeners() {
        synchronized (this.mLock) {
            for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                SessionsListenerRecord listener = this.mSessionsListeners.get(i);
                try {
                    enforceMediaPermissions(listener.mComponentName, listener.mPid, listener.mUid, listener.mUserId);
                } catch (SecurityException e) {
                    Log.i(TAG, "ActiveSessionsListener " + listener.mComponentName + " is no longer authorized. Disconnecting.");
                    this.mSessionsListeners.remove(i);
                    try {
                        listener.mListener.onActiveSessionsChanged(new ArrayList());
                    } catch (Exception e2) {
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void destroySessionLocked(MediaSessionRecord session) {
        Log.d(TAG, "Destroying " + session);
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

    private void registerPackageBroadcastReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGES_SUSPENDED");
        filter.addAction("android.intent.action.PACKAGES_UNSUSPENDED");
        filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        filter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        getContext().registerReceiverAsUser(new BroadcastReceiver() {
            /* JADX WARNING: Can't fix incorrect switch cases order */
            /* JADX WARNING: Code restructure failed: missing block: B:10:0x0055, code lost:
                if (r3.equals("android.intent.action.PACKAGE_ADDED") != false) goto L_0x009f;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a3, code lost:
                if (r1 != false) goto L_0x00ab;
             */
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("android.intent.extra.user_handle", -10000) == -10000) {
                    Log.w(MediaSessionService.TAG, "Intent broadcast does not contain user handle: " + intent);
                    return;
                }
                char c = 0;
                boolean isReplacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                if (MediaSessionService.DEBUG) {
                    Log.d(MediaSessionService.TAG, "Received change in packages, intent=" + intent);
                }
                String action = intent.getAction();
                switch (action.hashCode()) {
                    case -1403934493:
                        if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE")) {
                            c = 3;
                            break;
                        }
                    case -1338021860:
                        if (action.equals("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE")) {
                            c = 2;
                            break;
                        }
                    case -1001645458:
                        if (action.equals("android.intent.action.PACKAGES_SUSPENDED")) {
                            c = 5;
                            break;
                        }
                    case -810471698:
                        if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                            c = 7;
                            break;
                        }
                    case 172491798:
                        if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                            c = 4;
                            break;
                        }
                    case 525384130:
                        if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                            c = 1;
                            break;
                        }
                    case 1290767157:
                        if (action.equals("android.intent.action.PACKAGES_UNSUSPENDED")) {
                            c = 6;
                            break;
                        }
                    case 1544582882:
                        break;
                    default:
                        c = 65535;
                        break;
                }
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        break;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        MediaSessionService.this.buildMediaSessionService2List();
                        break;
                }
            }
        }, UserHandle.ALL, filter, null, BackgroundThread.getHandler());
    }

    /* access modifiers changed from: private */
    public void buildMediaSessionService2List() {
    }

    /* access modifiers changed from: private */
    public void enforcePackageName(String packageName, int uid) {
        if (!TextUtils.isEmpty(packageName)) {
            String[] packages = getContext().getPackageManager().getPackagesForUid(uid);
            int packageCount = packages.length;
            int i = 0;
            while (i < packageCount) {
                if (!packageName.equals(packages[i])) {
                    i++;
                } else {
                    return;
                }
            }
            throw new IllegalArgumentException("packageName is not owned by the calling process");
        }
        throw new IllegalArgumentException("packageName may not be empty");
    }

    /* access modifiers changed from: private */
    public void enforceMediaPermissions(ComponentName compName, int pid, int uid, int resolvedUserId) {
        if (!isCurrentVolumeController(pid, uid) && getContext().checkPermission("android.permission.MEDIA_CONTENT_CONTROL", pid, uid) != 0 && !isEnabledNotificationListener(compName, UserHandle.getUserId(uid), resolvedUserId)) {
            throw new SecurityException("Missing permission to control media.");
        }
    }

    /* access modifiers changed from: private */
    public boolean isCurrentVolumeController(int pid, int uid) {
        return getContext().checkPermission("android.permission.STATUS_BAR_SERVICE", pid, uid) == 0;
    }

    /* access modifiers changed from: private */
    public void enforceSystemUiPermission(String action, int pid, int uid) {
        if (!isCurrentVolumeController(pid, uid)) {
            throw new SecurityException("Only system ui may " + action);
        }
    }

    private boolean isEnabledNotificationListener(ComponentName compName, int userId, int forUserId) {
        if (userId != forUserId) {
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "Checking if enabled notification listener " + compName);
        }
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
    public MediaSessionRecord createSessionInternal(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) throws RemoteException {
        MediaSessionRecord createSessionLocked;
        synchronized (this.mLock) {
            createSessionLocked = createSessionLocked(callerPid, callerUid, userId, callerPackageName, cb, tag);
        }
        return createSessionLocked;
    }

    private MediaSessionRecord createSessionLocked(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) {
        int i = userId;
        FullUserRecord user = getFullUserRecordLocked(i);
        if (user != null) {
            MediaSessionRecord mediaSessionRecord = new MediaSessionRecord(callerPid, callerUid, i, callerPackageName, cb, tag, this, this.mHandler.getLooper());
            MediaSessionRecord session = mediaSessionRecord;
            try {
                cb.asBinder().linkToDeath(session, 0);
                user.mPriorityStack.addSession(session);
                this.mHandler.postSessionsChanged(i);
                Log.d(TAG, "Created session for " + callerPackageName + " with tag " + tag + " from pid" + callerPid);
                return session;
            } catch (RemoteException e) {
                int i2 = callerPid;
                String str = callerPackageName;
                String str2 = tag;
                throw new RuntimeException("Media Session owner died prematurely.", e);
            }
        } else {
            int i3 = callerPid;
            String str3 = callerPackageName;
            String str4 = tag;
            Log.wtf(TAG, "Request from invalid user: " + i);
            throw new RuntimeException("Session request from invalid user.");
        }
    }

    /* access modifiers changed from: private */
    public int findIndexOfSessionsListenerLocked(IActiveSessionsListener listener) {
        for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
            if (this.mSessionsListeners.get(i).mListener.asBinder() == listener.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void pushSessionsChanged(int userId) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(userId);
            if (user == null) {
                Log.w(TAG, "pushSessionsChanged failed. No user with id=" + userId);
                return;
            }
            List<MediaSessionRecord> records = getActiveSessionsLocked(userId);
            int size = records.size();
            if (size > 0 && records.get(0).isPlaybackActive()) {
                user.rememberMediaButtonReceiverLocked(records.get(0));
            } else if (user != null) {
                PendingIntent unused = user.mLastMediaButtonReceiver = null;
            }
            user.pushAddressedPlayerChangedLocked();
            ArrayList<MediaSession.Token> tokens = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                tokens.add(new MediaSession.Token(records.get(i).getControllerBinder()));
            }
            pushRemoteVolumeUpdateLocked(userId);
            for (int i2 = this.mSessionsListeners.size() - 1; i2 >= 0; i2--) {
                SessionsListenerRecord record = this.mSessionsListeners.get(i2);
                if (record.mUserId == -1 || record.mUserId == userId) {
                    try {
                        record.mListener.onActiveSessionsChanged(tokens);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Dead ActiveSessionsListener in pushSessionsChanged, removing", e);
                        this.mSessionsListeners.remove(i2);
                    }
                }
            }
        }
    }

    private void pushRemoteVolumeUpdateLocked(int userId) {
        if (this.mRvc != null) {
            try {
                FullUserRecord user = getFullUserRecordLocked(userId);
                if (user == null) {
                    Log.w(TAG, "pushRemoteVolumeUpdateLocked failed. No user with id=" + userId);
                    return;
                }
                MediaSessionRecord record = user.mPriorityStack.getDefaultRemoteSession(userId);
                this.mRvc.updateRemoteController(record == null ? null : record.getControllerBinder());
            } catch (RemoteException e) {
                Log.wtf(TAG, "Error sending default remote volume to sys ui.", e);
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
    public String getCallingPackageName(int uid) {
        String[] packages = getContext().getPackageManager().getPackagesForUid(uid);
        if (packages == null || packages.length <= 0) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        return packages[0];
    }

    /* access modifiers changed from: private */
    public void dispatchVolumeKeyLongPressLocked(KeyEvent keyEvent) {
        if (this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener != null) {
            try {
                this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener.onVolumeKeyLongPress(keyEvent);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to send " + keyEvent + " to volume key long-press listener");
            }
        }
    }

    /* access modifiers changed from: private */
    public FullUserRecord getFullUserRecordLocked(int userId) {
        int fullUserId = this.mFullUserIds.get(userId, -1);
        if (fullUserId < 0) {
            return null;
        }
        return this.mUserRecords.get(fullUserId);
    }

    /* access modifiers changed from: package-private */
    public void destroySession2Internal(SessionToken2 token) {
        boolean notifySessionTokensUpdated;
        synchronized (this.mLock) {
            if (token.getType() == 0) {
                notifySessionTokensUpdated = false | removeSessionRecordLocked(token);
            } else {
                notifySessionTokensUpdated = false | addSessionRecordLocked(token);
            }
            if (notifySessionTokensUpdated) {
                postSessionTokensUpdated(UserHandle.getUserId(token.getUid()));
            }
        }
    }

    /* access modifiers changed from: private */
    public void postSessionTokensUpdated(int userId) {
        this.mHandler.obtainMessage(3, Integer.valueOf(userId)).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void pushSessionTokensChanged(int userId) {
        synchronized (this.mLock) {
            List<Bundle> tokens = new ArrayList<>();
            for (SessionToken2 token : this.mSessionRecords.keySet()) {
                if (UserHandle.getUserId(token.getUid()) == userId || -1 == userId) {
                    tokens.add(token.toBundle());
                }
            }
            for (SessionTokensListenerRecord record : this.mSessionTokensListeners) {
                if (record.mUserId == userId || record.mUserId == -1) {
                    try {
                        record.mListener.onSessionTokensChanged(tokens);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Failed to notify session tokens changed", e);
                    }
                }
            }
        }
    }

    private boolean addSessionRecordLocked(SessionToken2 token) {
        return addSessionRecordLocked(token, null);
    }

    /* access modifiers changed from: private */
    public boolean addSessionRecordLocked(SessionToken2 token, MediaController2 controller) {
        if (this.mSessionRecords.containsKey(token) && this.mSessionRecords.get(token) == controller) {
            return false;
        }
        this.mSessionRecords.put(token, controller);
        return true;
    }

    private boolean removeSessionRecordLocked(SessionToken2 token) {
        if (!this.mSessionRecords.containsKey(token)) {
            return false;
        }
        this.mSessionRecords.remove(token);
        return true;
    }
}
