package com.android.server.media;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.PendingIntent.OnFinished;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.media.AudioManagerInternal;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.media.IRemoteVolumeController;
import android.media.session.IActiveSessionsListener;
import android.media.session.ICallback;
import android.media.session.IOnMediaKeyListener;
import android.media.session.IOnVolumeKeyLongPressListener;
import android.media.session.ISession;
import android.media.session.ISessionCallback;
import android.media.session.ISessionController;
import android.media.session.ISessionManager.Stub;
import android.media.session.MediaSession.Token;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import com.android.internal.util.DumpUtils;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MediaSessionService extends SystemService implements Monitor {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final boolean DEBUG_KEY_EVENT = true;
    private static final boolean HWRIDEMODE_FEATURE_SUPPORTED = SystemProperties.getBoolean("ro.config.ride_mode", false);
    private static final int IAWARE_APP_TYPE_MUSIC = 7;
    private static final int IAWARE_TRANSACTION_GETAPPTYPEINFO = 10;
    private static final int MEDIA_KEY_LISTENER_TIMEOUT = 1000;
    private static final int NO_ERROR = 0;
    private static final String TAG = "MediaSessionService";
    private static final int WAKELOCK_TIMEOUT = 5000;
    private AudioManagerInternal mAudioManagerInternal;
    private AudioPlaybackMonitor mAudioPlaybackMonitor;
    private IAudioService mAudioService;
    private ContentResolver mContentResolver;
    private FullUserRecord mCurrentFullUserRecord;
    private final SparseIntArray mFullUserIds = new SparseIntArray();
    private MediaSessionRecord mGlobalPrioritySession;
    private final MessageHandler mHandler = new MessageHandler();
    private IBinder mIAwareCMSService;
    private KeyguardManager mKeyguardManager;
    private final Object mLock = new Object();
    private final int mLongPressTimeout;
    private final WakeLock mMediaEventWakeLock;
    private IRemoteVolumeController mRvc;
    private final SessionManagerImpl mSessionManagerImpl = new SessionManagerImpl();
    private final ArrayList<SessionsListenerRecord> mSessionsListeners = new ArrayList();
    private SettingsObserver mSettingsObserver;
    private final SparseArray<FullUserRecord> mUserRecords = new SparseArray();

    final class FullUserRecord implements OnMediaButtonSessionChangedListener {
        private static final String COMPONENT_NAME_USER_ID_DELIM = ",";
        private ICallback mCallback;
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
        private int mRestoredMediaButtonReceiverUserId;

        public FullUserRecord(int fullUserId) {
            this.mFullUserId = fullUserId;
            this.mPriorityStack = new MediaSessionStack(MediaSessionService.this.mAudioPlaybackMonitor, this);
            String mediaButtonReceiver = Secure.getStringForUser(MediaSessionService.this.mContentResolver, "media_button_receiver", this.mFullUserId);
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
            int i = 0;
            while (i < size) {
                if (MediaSessionService.this.mFullUserIds.keyAt(i) != MediaSessionService.this.mFullUserIds.valueAt(i) && MediaSessionService.this.mFullUserIds.valueAt(i) == this.mFullUserId) {
                    pw.print(", profile_user=" + MediaSessionService.this.mFullUserIds.keyAt(i));
                }
                i++;
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
            this.mPriorityStack.dump(pw, indent);
        }

        public void onMediaButtonSessionChanged(MediaSessionRecord oldMediaButtonSession, MediaSessionRecord newMediaButtonSession) {
            Log.d(MediaSessionService.TAG, "Media button session is changed to " + newMediaButtonSession);
            synchronized (MediaSessionService.this.mLock) {
                if (oldMediaButtonSession != null) {
                    MediaSessionService.this.mHandler.postSessionsChanged(oldMediaButtonSession.getUserId());
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
            String componentName = "";
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
                Secure.putStringForUser(MediaSessionService.this.mContentResolver, "media_button_receiver", componentName + COMPONENT_NAME_USER_ID_DELIM + record.getUserId(), this.mFullUserId);
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

        private void pushAddressedPlayerChangedLocked() {
            if (this.mCallback != null) {
                try {
                    MediaSessionRecord mediaButtonSession = getMediaButtonSessionLocked();
                    if (mediaButtonSession != null) {
                        this.mCallback.onAddressedPlayerChangedToMediaSession(new Token(mediaButtonSession.getControllerBinder()));
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

        private MediaSessionRecord getMediaButtonSessionLocked() {
            return MediaSessionService.this.isGlobalPriorityActiveLocked() ? MediaSessionService.this.mGlobalPrioritySession : this.mPriorityStack.getMediaButtonSession();
        }
    }

    final class MessageHandler extends Handler {
        private static final int MSG_SESSIONS_CHANGED = 1;
        private static final int MSG_VOLUME_INITIAL_DOWN = 2;
        private final SparseArray<Integer> mIntegerCache = new SparseArray();

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
                            user.mInitialDownVolumeKeyEvent = null;
                        }
                    }
                    return;
                default:
                    return;
            }
        }

        public void postSessionsChanged(int userId) {
            Integer userIdInteger = (Integer) this.mIntegerCache.get(userId);
            if (userIdInteger == null) {
                userIdInteger = Integer.valueOf(userId);
                this.mIntegerCache.put(userId, userIdInteger);
            }
            removeMessages(1, userIdInteger);
            obtainMessage(1, userIdInteger).sendToTarget();
        }
    }

    class SessionManagerImpl extends Stub {
        private static final String EXTRA_WAKELOCK_ACQUIRED = "android.media.AudioService.WAKELOCK_ACQUIRED";
        private static final int WAKELOCK_RELEASE_ON_FINISHED = 1980;
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

        class KeyEventWakeLockReceiver extends ResultReceiver implements Runnable, OnFinished {
            private final Handler mHandler;
            private int mLastTimeoutId = 0;
            private int mRefCount = 0;

            public KeyEventWakeLockReceiver(Handler handler) {
                super(handler);
                this.mHandler = handler;
            }

            public void onTimeout() {
                synchronized (MediaSessionService.this.mLock) {
                    if (this.mRefCount == 0) {
                        return;
                    }
                    this.mLastTimeoutId++;
                    this.mRefCount = 0;
                    releaseWakeLockLocked();
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

            protected void onReceiveResult(int resultCode, Bundle resultData) {
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
            private boolean mHandled;
            private KeyEvent mKeyEvent;
            private boolean mNeedWakeLock;

            /* synthetic */ MediaKeyListenerResultReceiver(SessionManagerImpl this$1, KeyEvent keyEvent, boolean needWakeLock, MediaKeyListenerResultReceiver -this3) {
                this(keyEvent, needWakeLock);
            }

            private MediaKeyListenerResultReceiver(KeyEvent keyEvent, boolean needWakeLock) {
                super(MediaSessionService.this.mHandler);
                MediaSessionService.this.mHandler.postDelayed(this, 1000);
                this.mKeyEvent = keyEvent;
                this.mNeedWakeLock = needWakeLock;
            }

            public void run() {
                Log.d(MediaSessionService.TAG, "The media key listener is timed-out for " + this.mKeyEvent);
                dispatchMediaKeyEvent();
            }

            protected void onReceiveResult(int resultCode, Bundle resultData) {
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
                            SessionManagerImpl.this.dispatchMediaKeyEventLocked(this.mKeyEvent, this.mNeedWakeLock);
                        } else {
                            SessionManagerImpl.this.handleVoiceKeyEventLocked(this.mKeyEvent, this.mNeedWakeLock);
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
                if (cb == null) {
                    throw new IllegalArgumentException("Controller callback cannot be null");
                }
                ISession sessionBinder = MediaSessionService.this.createSessionInternal(pid, uid, resolvedUserId, packageName, cb, tag).getSessionBinder();
                return sessionBinder;
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
                ArrayList<IBinder> binders = new ArrayList();
                synchronized (MediaSessionService.this.mLock) {
                    for (MediaSessionRecord record : MediaSessionService.this.getActiveSessionsLocked(resolvedUserId)) {
                        binders.add(record.getControllerBinder().asBinder());
                    }
                }
                return binders;
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
                    } else {
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

        public void dispatchMediaKeyEvent(KeyEvent keyEvent, boolean needWakeLock) {
            if (keyEvent == null || (KeyEvent.isMediaKey(keyEvent.getKeyCode()) ^ 1) != 0) {
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-media key event.");
                return;
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            Log.d(MediaSessionService.TAG, "dispatchMediaKeyEvent, pid=" + pid + ", uid=" + uid + ", event(action=" + keyEvent.getAction() + ", keyCode=" + keyEvent.getKeyCode() + ", flags=" + keyEvent.getFlags() + ", repeatCount=" + keyEvent.getRepeatCount() + ")");
            if (!isUserSetupComplete()) {
                Slog.i(MediaSessionService.TAG, "Not dispatching media key event because user setup is in progress.");
            } else if (MediaSessionService.HWRIDEMODE_FEATURE_SUPPORTED && SystemProperties.getBoolean("sys.ride_mode", false)) {
                Slog.i(MediaSessionService.TAG, "Not dispatching media key event because Ride mode is enabled");
                Binder.restoreCallingIdentity(token);
            } else {
                try {
                    synchronized (MediaSessionService.this.mLock) {
                        boolean isGlobalPriorityActive = MediaSessionService.this.isGlobalPriorityActiveLocked();
                        if (!isGlobalPriorityActive || uid == 1000) {
                            if (!isGlobalPriorityActive) {
                                if (MediaSessionService.this.mCurrentFullUserRecord.mOnMediaKeyListener != null) {
                                    Log.d(MediaSessionService.TAG, "Send " + keyEvent + " to the media key listener");
                                    try {
                                        MediaSessionService.this.mCurrentFullUserRecord.mOnMediaKeyListener.onMediaKey(keyEvent, new MediaKeyListenerResultReceiver(this, keyEvent, needWakeLock, null));
                                        Binder.restoreCallingIdentity(token);
                                        return;
                                    } catch (RemoteException e) {
                                        Log.w(MediaSessionService.TAG, "Failed to send " + keyEvent + " to the media key listener");
                                    }
                                }
                            }
                            if (isGlobalPriorityActive || !isVoiceKey(keyEvent.getKeyCode())) {
                                dispatchMediaKeyEventLocked(keyEvent, needWakeLock);
                            } else {
                                handleVoiceKeyEventLocked(keyEvent, needWakeLock);
                            }
                            Binder.restoreCallingIdentity(token);
                            return;
                        }
                        Slog.i(MediaSessionService.TAG, "Only the system can dispatch media key event to the global priority session.");
                        Binder.restoreCallingIdentity(token);
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        public void setCallback(ICallback callback) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (UserHandle.isSameApp(uid, 1002)) {
                    synchronized (MediaSessionService.this.mLock) {
                        int userId = UserHandle.getUserId(uid);
                        final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                        if (user == null || user.mFullUserId != userId) {
                            Log.w(MediaSessionService.TAG, "Only the full user can set the callback, userId=" + userId);
                        } else {
                            user.mCallback = callback;
                            Log.d(MediaSessionService.TAG, "The callback " + user.mCallback + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                            if (user.mCallback == null) {
                                Binder.restoreCallingIdentity(token);
                                return;
                            }
                            try {
                                user.mCallback.asBinder().linkToDeath(new DeathRecipient() {
                                    public void binderDied() {
                                        synchronized (MediaSessionService.this.mLock) {
                                            user.mCallback = null;
                                        }
                                    }
                                }, 0);
                                user.pushAddressedPlayerChangedLocked();
                            } catch (RemoteException e) {
                                Log.w(MediaSessionService.TAG, "Failed to set callback", e);
                                user.mCallback = null;
                            }
                            Binder.restoreCallingIdentity(token);
                            return;
                        }
                    }
                }
                throw new SecurityException("Only Bluetooth service processes can set Callback");
                return;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setOnVolumeKeyLongPressListener(IOnVolumeKeyLongPressListener listener) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaSessionService.this.getContext().checkPermission("android.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER", pid, uid) != 0) {
                    throw new SecurityException("Must hold the SET_VOLUME_KEY_LONG_PRESS_LISTENER permission.");
                }
                synchronized (MediaSessionService.this.mLock) {
                    int userId = UserHandle.getUserId(uid);
                    final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                    if (user == null || user.mFullUserId != userId) {
                        Log.w(MediaSessionService.TAG, "Only the full user can set the volume key long-press listener, userId=" + userId);
                    } else if (user.mOnVolumeKeyLongPressListener == null || user.mOnVolumeKeyLongPressListenerUid == uid) {
                        user.mOnVolumeKeyLongPressListener = listener;
                        user.mOnVolumeKeyLongPressListenerUid = uid;
                        Log.d(MediaSessionService.TAG, "The volume key long-press listener " + listener + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                        if (user.mOnVolumeKeyLongPressListener != null) {
                            try {
                                user.mOnVolumeKeyLongPressListener.asBinder().linkToDeath(new DeathRecipient() {
                                    public void binderDied() {
                                        synchronized (MediaSessionService.this.mLock) {
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
                    } else {
                        Log.w(MediaSessionService.TAG, "The volume key long-press listener cannot be reset by another app , mOnVolumeKeyLongPressListener=" + user.mOnVolumeKeyLongPressListenerUid + ", uid=" + uid);
                        Binder.restoreCallingIdentity(token);
                        return;
                    }
                }
                return;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setOnMediaKeyListener(IOnMediaKeyListener listener) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaSessionService.this.getContext().checkPermission("android.permission.SET_MEDIA_KEY_LISTENER", pid, uid) != 0) {
                    throw new SecurityException("Must hold the SET_MEDIA_KEY_LISTENER permission.");
                }
                synchronized (MediaSessionService.this.mLock) {
                    int userId = UserHandle.getUserId(uid);
                    final FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(userId);
                    if (user == null || user.mFullUserId != userId) {
                        Log.w(MediaSessionService.TAG, "Only the full user can set the media key listener, userId=" + userId);
                    } else if (user.mOnMediaKeyListener == null || user.mOnMediaKeyListenerUid == uid) {
                        user.mOnMediaKeyListener = listener;
                        user.mOnMediaKeyListenerUid = uid;
                        Log.d(MediaSessionService.TAG, "The media key listener " + user.mOnMediaKeyListener + " is set by " + MediaSessionService.this.getCallingPackageName(uid));
                        if (user.mOnMediaKeyListener != null) {
                            try {
                                user.mOnMediaKeyListener.asBinder().linkToDeath(new DeathRecipient() {
                                    public void binderDied() {
                                        synchronized (MediaSessionService.this.mLock) {
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
                    } else {
                        Log.w(MediaSessionService.TAG, "The media key listener cannot be reset by another app. , mOnMediaKeyListenerUid=" + user.mOnMediaKeyListenerUid + ", uid=" + uid);
                        Binder.restoreCallingIdentity(token);
                        return;
                    }
                }
                return;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void dispatchVolumeKeyEvent(KeyEvent keyEvent, int stream, boolean musicOnly) {
            if (keyEvent == null || !(keyEvent.getKeyCode() == 24 || keyEvent.getKeyCode() == 25 || keyEvent.getKeyCode() == 164)) {
                Log.w(MediaSessionService.TAG, "Attempted to dispatch null or non-volume key event.");
                return;
            }
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            Log.d(MediaSessionService.TAG, "dispatchVolumeKeyEvent, pid=" + pid + ", uid=" + uid + ", event=" + keyEvent);
            try {
                synchronized (MediaSessionService.this.mLock) {
                    if (MediaSessionService.this.isGlobalPriorityActiveLocked() || MediaSessionService.this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener == null) {
                        dispatchVolumeKeyEventLocked(keyEvent, stream, musicOnly);
                    } else if (keyEvent.getAction() == 0) {
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
                            dispatchVolumeKeyEventLocked(MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeKeyEvent, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownVolumeStream, MediaSessionService.this.mCurrentFullUserRecord.mInitialDownMusicOnly);
                            dispatchVolumeKeyEventLocked(keyEvent, stream, musicOnly);
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private void dispatchVolumeKeyEventLocked(KeyEvent keyEvent, int stream, boolean musicOnly) {
            boolean down = keyEvent.getAction() == 0;
            boolean up = keyEvent.getAction() == 1;
            int direction = 0;
            boolean isMute = false;
            switch (keyEvent.getKeyCode()) {
                case 24:
                    direction = 1;
                    break;
                case 25:
                    direction = -1;
                    break;
                case 164:
                    isMute = true;
                    break;
            }
            if (down || up) {
                int flags;
                if (musicOnly) {
                    flags = 4608;
                } else if (up) {
                    flags = 4116;
                } else {
                    flags = 4113;
                }
                if (direction != 0) {
                    if (up) {
                        direction = 0;
                    }
                    dispatchAdjustVolumeLocked(stream, direction, flags);
                } else if (isMute && down && keyEvent.getRepeatCount() == 0) {
                    dispatchAdjustVolumeLocked(stream, 101, flags);
                }
            }
        }

        public void dispatchAdjustVolume(int suggestedStream, int delta, int flags) {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (MediaSessionService.this.mLock) {
                    dispatchAdjustVolumeLocked(suggestedStream, delta, flags);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setRemoteVolumeController(IRemoteVolumeController rvc) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                MediaSessionService.this.enforceSystemUiPermission("listen for volume changes", pid, uid);
                MediaSessionService.this.mRvc = rvc;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isGlobalPriorityActive() {
            boolean -wrap0;
            synchronized (MediaSessionService.this.mLock) {
                -wrap0 = MediaSessionService.this.isGlobalPriorityActiveLocked();
            }
            return -wrap0;
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
                        ((FullUserRecord) MediaSessionService.this.mUserRecords.valueAt(i)).dumpLocked(pw, "");
                    }
                    MediaSessionService.this.mAudioPlaybackMonitor.dump(pw, "");
                }
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

        private void dispatchAdjustVolumeLocked(int suggestedStream, final int direction, final int flags) {
            MediaSessionRecord session;
            if (MediaSessionService.this.isGlobalPriorityActiveLocked()) {
                session = MediaSessionService.this.mGlobalPrioritySession;
            } else {
                session = MediaSessionService.this.mCurrentFullUserRecord.mPriorityStack.getDefaultVolumeSession();
            }
            boolean preferSuggestedStream = false;
            if (isValidLocalStreamType(suggestedStream) && AudioSystem.isStreamActive(suggestedStream, 0)) {
                preferSuggestedStream = true;
            }
            Log.d(MediaSessionService.TAG, "Adjusting " + session + " by " + direction + ". flags=" + flags + ", suggestedStream=" + suggestedStream + ", preferSuggestedStream=" + preferSuggestedStream);
            if (session == null || preferSuggestedStream) {
                if (!((flags & 512) == 0 || (AudioSystem.isStreamActive(3, 0) ^ 1) == 0)) {
                    if (suggestedStream == Integer.MIN_VALUE && AudioSystem.isStreamActive(0, 0)) {
                        Log.d(MediaSessionService.TAG, "set suggestedStream to voice call");
                        suggestedStream = 0;
                    } else {
                        Log.d(MediaSessionService.TAG, "No active session to adjust, skipping media only volume event");
                        return;
                    }
                }
                final int temp = suggestedStream;
                MediaSessionService.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            MediaSessionService.this.mAudioService.adjustSuggestedStreamVolume(direction, temp, flags, MediaSessionService.this.getContext().getOpPackageName(), MediaSessionService.TAG);
                        } catch (RemoteException e) {
                            Log.e(MediaSessionService.TAG, "Error adjusting default volume.", e);
                        } catch (IllegalArgumentException e2) {
                            Log.e(MediaSessionService.TAG, "IllegalArgument when adjust stream volume.", e2);
                        }
                    }
                });
            } else {
                session.adjustVolume(suggestedStream, direction, flags, MediaSessionService.this.getContext().getPackageName(), 1000, true);
            }
        }

        private void handleVoiceKeyEventLocked(KeyEvent keyEvent, boolean needWakeLock) {
            int action = keyEvent.getAction();
            boolean isLongPress = (keyEvent.getFlags() & 128) != 0;
            if (action == 0) {
                if (keyEvent.getRepeatCount() == 0) {
                    this.mVoiceButtonDown = true;
                    this.mVoiceButtonHandled = false;
                } else if (this.mVoiceButtonDown && (this.mVoiceButtonHandled ^ 1) != 0 && isLongPress) {
                    this.mVoiceButtonHandled = true;
                    startVoiceInput(needWakeLock);
                }
            } else if (action == 1 && this.mVoiceButtonDown) {
                this.mVoiceButtonDown = false;
                if (!this.mVoiceButtonHandled && (keyEvent.isCanceled() ^ 1) != 0) {
                    dispatchMediaKeyEventLocked(KeyEvent.changeAction(keyEvent, 0), needWakeLock);
                    dispatchMediaKeyEventLocked(keyEvent, needWakeLock);
                }
            }
        }

        private void dispatchMediaKeyEventLocked(KeyEvent keyEvent, boolean needWakeLock) {
            MediaSessionRecord session = MediaSessionService.this.mCurrentFullUserRecord.getMediaButtonSessionLocked();
            if (session != null) {
                Log.d(MediaSessionService.TAG, "Sending " + keyEvent + " to " + session);
                if (keyEvent.getAction() == 0) {
                    HwServiceFactory.reportMediaKeyToIAware(session.mOwnerUid);
                    LogPower.push(148, "mediakey", session.mPackageName, Integer.toString(session.mOwnerPid), new String[]{String.valueOf(keyEvent.getKeyCode())});
                }
                if (needWakeLock) {
                    this.mKeyEventReceiver.aquireWakeLockLocked();
                }
                session.sendMediaButton(keyEvent, needWakeLock ? this.mKeyEventReceiver.mLastTimeoutId : -1, this.mKeyEventReceiver, 1000, MediaSessionService.this.getContext().getPackageName());
                if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                    try {
                        MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaSession(keyEvent, new Token(session.getControllerBinder()));
                    } catch (Throwable e) {
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
                keyIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
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
                mediaButtonIntent.putExtra("android.intent.extra.KEY_EVENT", keyEvent);
                try {
                    if (MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver != null) {
                        PendingIntent receiver = MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver;
                        Log.d(MediaSessionService.TAG, "Sending " + keyEvent + " to the last known PendingIntent " + receiver);
                        receiver.send(MediaSessionService.this.getContext(), needWakeLock ? this.mKeyEventReceiver.mLastTimeoutId : -1, mediaButtonIntent, this.mKeyEventReceiver, MediaSessionService.this.mHandler);
                        if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                            ComponentName componentName = MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver.getIntent().getComponent();
                            if (componentName != null) {
                                MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaButtonReceiver(keyEvent, componentName);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    ComponentName receiver2 = MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiver;
                    Log.d(MediaSessionService.TAG, "Sending " + keyEvent + " to the restored intent " + receiver2);
                    mediaButtonIntent.setComponent(receiver2);
                    MediaSessionService.this.getContext().sendBroadcastAsUser(mediaButtonIntent, UserHandle.of(MediaSessionService.this.mCurrentFullUserRecord.mRestoredMediaButtonReceiverUserId));
                    if (MediaSessionService.this.mCurrentFullUserRecord.mCallback != null) {
                        MediaSessionService.this.mCurrentFullUserRecord.mCallback.onMediaKeyEventDispatchedToMediaButtonReceiver(keyEvent, receiver2);
                    }
                } catch (Throwable e2) {
                    Log.i(MediaSessionService.TAG, "Error sending key event to media button receiver " + MediaSessionService.this.mCurrentFullUserRecord.mLastMediaButtonReceiver, e2);
                } catch (Throwable e3) {
                    Log.w(MediaSessionService.TAG, "Failed to send callback", e3);
                }
            }
        }

        private boolean checkPackage(String packageName) {
            if (packageName == null || "".equals(packageName)) {
                return false;
            }
            try {
                ApplicationInfo info = MediaSessionService.this.getContext().getPackageManager().getApplicationInfo(packageName, 8192);
                return true;
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        /* JADX WARNING: Failed to extract finally block: empty outs */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void startVoiceInput(boolean needWakeLock) {
            Intent voiceIntent;
            PowerManager pm = (PowerManager) MediaSessionService.this.getContext().getSystemService("power");
            boolean isLocked = MediaSessionService.this.mKeyguardManager != null ? MediaSessionService.this.mKeyguardManager.isKeyguardLocked() : false;
            if (isLocked || !pm.isScreenOn()) {
                voiceIntent = new Intent("android.speech.action.VOICE_SEARCH_HANDS_FREE");
                voiceIntent.putExtra("android.speech.extras.EXTRA_SECURE", isLocked ? MediaSessionService.this.mKeyguardManager.isKeyguardSecure() : false);
                Log.i(MediaSessionService.TAG, "voice-based interactions: about to use ACTION_VOICE_SEARCH_HANDS_FREE");
            } else {
                voiceIntent = new Intent("android.speech.action.WEB_SEARCH");
                Log.i(MediaSessionService.TAG, "voice-based interactions: about to use ACTION_WEB_SEARCH");
            }
            if (needWakeLock) {
                MediaSessionService.this.mMediaEventWakeLock.acquire();
            }
            if (voiceIntent != null) {
                try {
                    voiceIntent.setFlags(276824064);
                    if (MediaSessionService.DEBUG) {
                        Log.d(MediaSessionService.TAG, "voiceIntent: " + voiceIntent);
                    }
                    MediaSessionService.this.getContext().startActivityAsUser(voiceIntent, UserHandle.CURRENT);
                } catch (ActivityNotFoundException e) {
                    Log.w(MediaSessionService.TAG, "No activity for search: " + e);
                    if (needWakeLock) {
                        MediaSessionService.this.mMediaEventWakeLock.release();
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    if (needWakeLock) {
                        MediaSessionService.this.mMediaEventWakeLock.release();
                    }
                    throw th;
                }
            }
            if (needWakeLock) {
                MediaSessionService.this.mMediaEventWakeLock.release();
            }
        }

        private boolean isVoiceKey(int keyCode) {
            if (keyCode == 79 || keyCode == 85) {
                return true;
            }
            return false;
        }

        private boolean isUserSetupComplete() {
            return Secure.getIntForUser(MediaSessionService.this.getContext().getContentResolver(), "user_setup_complete", 0, -2) != 0;
        }

        private boolean isValidLocalStreamType(int streamType) {
            if (streamType < 0 || streamType > 5) {
                return false;
            }
            return true;
        }
    }

    final class SessionsListenerRecord implements DeathRecipient {
        private final ComponentName mComponentName;
        private final IActiveSessionsListener mListener;
        private final int mPid;
        private final int mUid;
        private final int mUserId;

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

        /* synthetic */ SettingsObserver(MediaSessionService this$0, SettingsObserver -this1) {
            this();
        }

        private SettingsObserver() {
            super(null);
            this.mSecureSettingsUri = Secure.getUriFor("enabled_notification_listeners");
        }

        private void observe() {
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
    }

    public void onStart() {
        publishBinderService("media_session", this.mSessionManagerImpl);
        Watchdog.getInstance().addMonitor(this);
        this.mKeyguardManager = (KeyguardManager) getContext().getSystemService("keyguard");
        this.mAudioService = getAudioService();
        this.mAudioPlaybackMonitor = new AudioPlaybackMonitor(getContext(), this.mAudioService, new OnAudioPlaybackStartedListener() {
            public void onAudioPlaybackStarted(int uid) {
                synchronized (MediaSessionService.this.mLock) {
                    FullUserRecord user = MediaSessionService.this.getFullUserRecordLocked(UserHandle.getUserId(uid));
                    if (user != null) {
                        user.mPriorityStack.updateMediaButtonSessionIfNeeded();
                    }
                }
            }
        });
        this.mAudioManagerInternal = (AudioManagerInternal) LocalServices.getService(AudioManagerInternal.class);
        this.mContentResolver = getContext().getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this, null);
        this.mSettingsObserver.observe();
        updateUser();
    }

    private IAudioService getAudioService() {
        return IAudioService.Stub.asInterface(ServiceManager.getService("audio"));
    }

    private boolean isGlobalPriorityActiveLocked() {
        return this.mGlobalPrioritySession != null ? this.mGlobalPrioritySession.isActive() : false;
    }

    public void updateSession(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user == null) {
                Log.w(TAG, "Unknown session updated. Ignoring.");
                return;
            }
            if ((record.getFlags() & 65536) != 0) {
                if (this.mGlobalPrioritySession != record) {
                    Log.d(TAG, "Global priority session is changed from " + this.mGlobalPrioritySession + " to " + record);
                    this.mGlobalPrioritySession = record;
                    if (user != null && user.mPriorityStack.contains(record)) {
                        user.mPriorityStack.removeSession(record);
                    }
                }
                Log.d(TAG, "Global priority session is updated, active=" + record.isActive());
                user.pushAddressedPlayerChangedLocked();
            } else if (user.mPriorityStack.contains(record)) {
                user.mPriorityStack.onSessionStateChange(record);
            } else {
                Log.w(TAG, "Unknown session updated. Ignoring.");
                return;
            }
            this.mHandler.postSessionsChanged(record.getUserId());
        }
    }

    private List<MediaSessionRecord> getActiveSessionsLocked(int userId) {
        List<MediaSessionRecord> records;
        if (userId == -1) {
            records = new ArrayList();
            int size = this.mUserRecords.size();
            for (int i = 0; i < size; i++) {
                records.addAll(((FullUserRecord) this.mUserRecords.valueAt(i)).mPriorityStack.getActiveSessions(userId));
            }
        } else {
            FullUserRecord user = getFullUserRecordLocked(userId);
            if (user == null) {
                Log.w(TAG, "getSessions failed. Unknown user " + userId);
                return new ArrayList();
            }
            records = user.mPriorityStack.getActiveSessions(userId);
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
        if (this.mRvc != null && (session.isActive() ^ 1) == 0) {
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
            if (user == null || (user.mPriorityStack.contains(record) ^ 1) != 0) {
                Log.d(TAG, "Unknown session changed playback state. Ignoring.");
                return;
            }
            user.mPriorityStack.onPlaystateChanged(record, oldState, newState);
        }
    }

    public void onSessionPlaybackTypeChanged(MediaSessionRecord record) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(record.getUserId());
            if (user == null || (user.mPriorityStack.contains(record) ^ 1) != 0) {
                Log.d(TAG, "Unknown session changed playback type. Ignoring.");
                return;
            }
            pushRemoteVolumeUpdateLocked(record.getUserId());
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

    protected void enforcePhoneStatePermission(int pid, int uid) {
        if (getContext().checkPermission("android.permission.MODIFY_PHONE_STATE", pid, uid) != 0) {
            throw new SecurityException("Must hold the MODIFY_PHONE_STATE permission.");
        }
    }

    void sessionDied(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    void destroySession(MediaSessionRecord session) {
        synchronized (this.mLock) {
            destroySessionLocked(session);
        }
    }

    private void updateUser() {
        synchronized (this.mLock) {
            UserManager manager = (UserManager) getContext().getSystemService("user");
            this.mFullUserIds.clear();
            List<UserInfo> allUsers = manager.getUsers();
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
            this.mCurrentFullUserRecord = (FullUserRecord) this.mUserRecords.get(currentFullUserId);
            if (this.mCurrentFullUserRecord == null) {
                Log.w(TAG, "Cannot find FullUserInfo for the current user " + currentFullUserId);
                this.mCurrentFullUserRecord = new FullUserRecord(currentFullUserId);
                this.mUserRecords.put(currentFullUserId, this.mCurrentFullUserRecord);
            }
            this.mFullUserIds.put(currentFullUserId, currentFullUserId);
        }
    }

    private void updateActiveSessionListeners() {
        synchronized (this.mLock) {
            for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                SessionsListenerRecord listener = (SessionsListenerRecord) this.mSessionsListeners.get(i);
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

    private void destroySessionLocked(MediaSessionRecord session) {
        if (DEBUG) {
            Log.d(TAG, "Destroying " + session);
        }
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

    private void enforcePackageName(String packageName, int uid) {
        if (TextUtils.isEmpty(packageName)) {
            throw new IllegalArgumentException("packageName may not be empty");
        }
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

    private void enforceMediaPermissions(ComponentName compName, int pid, int uid, int resolvedUserId) {
        if (!isCurrentVolumeController(uid, pid) && getContext().checkPermission("android.permission.MEDIA_CONTENT_CONTROL", pid, uid) != 0 && (isEnabledNotificationListener(compName, UserHandle.getUserId(uid), resolvedUserId) ^ 1) != 0) {
            throw new SecurityException("Missing permission to control media.");
        }
    }

    private boolean isCurrentVolumeController(int uid, int pid) {
        return getContext().checkPermission("android.permission.STATUS_BAR_SERVICE", pid, uid) == 0;
    }

    private void enforceSystemUiPermission(String action, int pid, int uid) {
        if (!isCurrentVolumeController(uid, pid)) {
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
            String enabledNotifListeners = Secure.getStringForUser(this.mContentResolver, "enabled_notification_listeners", userId);
            if (enabledNotifListeners != null) {
                String[] components = enabledNotifListeners.split(":");
                int i = 0;
                while (i < components.length) {
                    ComponentName component = ComponentName.unflattenFromString(components[i]);
                    if (component == null || !compName.equals(component)) {
                        i++;
                    } else {
                        if (DEBUG) {
                            Log.d(TAG, "ok to get sessions. " + component + " is authorized notification listener");
                        }
                        return true;
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "not ok to get sessions. " + compName + " is not in list of ENABLED_NOTIFICATION_LISTENERS for user " + userId);
            }
        }
        return false;
    }

    private MediaSessionRecord createSessionInternal(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) throws RemoteException {
        MediaSessionRecord createSessionLocked;
        synchronized (this.mLock) {
            createSessionLocked = createSessionLocked(callerPid, callerUid, userId, callerPackageName, cb, tag);
        }
        return createSessionLocked;
    }

    private MediaSessionRecord createSessionLocked(int callerPid, int callerUid, int userId, String callerPackageName, ISessionCallback cb, String tag) {
        FullUserRecord user = getFullUserRecordLocked(userId);
        if (user == null) {
            Log.wtf(TAG, "Request from invalid user: " + userId);
            throw new RuntimeException("Session request from invalid user.");
        }
        MediaSessionRecord session = new MediaSessionRecord(callerPid, callerUid, userId, callerPackageName, cb, tag, this, this.mHandler.getLooper());
        try {
            cb.asBinder().linkToDeath(session, 0);
            user.mPriorityStack.addSession(session);
            this.mHandler.postSessionsChanged(userId);
            if (DEBUG) {
                Log.d(TAG, "Created session for " + callerPackageName + " with tag " + tag);
            }
            return session;
        } catch (RemoteException e) {
            throw new RuntimeException("Media Session owner died prematurely.", e);
        }
    }

    private int findIndexOfSessionsListenerLocked(IActiveSessionsListener listener) {
        for (int i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
            if (((SessionsListenerRecord) this.mSessionsListeners.get(i)).mListener.asBinder() == listener.asBinder()) {
                return i;
            }
        }
        return -1;
    }

    private void pushSessionsChanged(int userId) {
        synchronized (this.mLock) {
            FullUserRecord user = getFullUserRecordLocked(userId);
            if (user == null) {
                Log.w(TAG, "pushSessionsChanged failed. No user with id=" + userId);
                return;
            }
            int i;
            List<MediaSessionRecord> records = getActiveSessionsLocked(userId);
            int size = records.size();
            if (size > 0 && ((MediaSessionRecord) records.get(0)).isPlaybackActive()) {
                user.rememberMediaButtonReceiverLocked((MediaSessionRecord) records.get(0));
            } else if (user != null) {
                user.mLastMediaButtonReceiver = null;
            }
            user.pushAddressedPlayerChangedLocked();
            ArrayList<Token> tokens = new ArrayList();
            for (i = 0; i < size; i++) {
                tokens.add(new Token(((MediaSessionRecord) records.get(i)).getControllerBinder()));
            }
            pushRemoteVolumeUpdateLocked(userId);
            for (i = this.mSessionsListeners.size() - 1; i >= 0; i--) {
                SessionsListenerRecord record = (SessionsListenerRecord) this.mSessionsListeners.get(i);
                if (record.mUserId == -1 || record.mUserId == userId) {
                    try {
                        record.mListener.onActiveSessionsChanged(tokens);
                    } catch (RemoteException e) {
                        Log.w(TAG, "Dead ActiveSessionsListener in pushSessionsChanged, removing", e);
                        this.mSessionsListeners.remove(i);
                    }
                }
            }
        }
    }

    private void pushRemoteVolumeUpdateLocked(int userId) {
        ISessionController iSessionController = null;
        if (this.mRvc != null) {
            try {
                FullUserRecord user = getFullUserRecordLocked(userId);
                if (user == null) {
                    Log.w(TAG, "pushRemoteVolumeUpdateLocked failed. No user with id=" + userId);
                    return;
                }
                MediaSessionRecord record = user.mPriorityStack.getDefaultRemoteSession(userId);
                IRemoteVolumeController iRemoteVolumeController = this.mRvc;
                if (record != null) {
                    iSessionController = record.getControllerBinder();
                }
                iRemoteVolumeController.updateRemoteController(iSessionController);
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

    private String getCallingPackageName(int uid) {
        String[] packages = getContext().getPackageManager().getPackagesForUid(uid);
        if (packages == null || packages.length <= 0) {
            return "";
        }
        return packages[0];
    }

    private void dispatchVolumeKeyLongPressLocked(KeyEvent keyEvent) {
        try {
            this.mCurrentFullUserRecord.mOnVolumeKeyLongPressListener.onVolumeKeyLongPress(keyEvent);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to send " + keyEvent + " to volume key long-press listener");
        }
    }

    private FullUserRecord getFullUserRecordLocked(int userId) {
        int fullUserId = this.mFullUserIds.get(userId, -1);
        if (fullUserId < 0) {
            return null;
        }
        return (FullUserRecord) this.mUserRecords.get(fullUserId);
    }
}
