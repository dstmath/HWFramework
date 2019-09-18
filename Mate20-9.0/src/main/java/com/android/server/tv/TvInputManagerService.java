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
    /* access modifiers changed from: private */
    public static final Pattern sAdapterDirPattern = Pattern.compile("^adapter([0-9]+)$");
    /* access modifiers changed from: private */
    public static final Pattern sFrontEndDevicePattern = Pattern.compile("^dvb([0-9]+)\\.frontend([0-9]+)$");
    /* access modifiers changed from: private */
    public static final Pattern sFrontEndInAdapterDirPattern = Pattern.compile("^frontend([0-9]+)$");
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final TvInputHardwareManager mTvInputHardwareManager;
    /* access modifiers changed from: private */
    public final SparseArray<UserState> mUserStates = new SparseArray<>();
    /* access modifiers changed from: private */
    public final WatchLogHandler mWatchLogHandler;

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
                Binder.restoreCallingIdentity(identity);
                return inputList;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public TvInputInfo getTvInputInfo(String inputId, int userId) {
            TvInputInfo access$1400;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputInfo");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputState state = (TvInputState) TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).inputMap.get(inputId);
                    access$1400 = state == null ? null : state.info;
                }
                Binder.restoreCallingIdentity(identity);
                return access$1400;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
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
            int access$4000;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputState");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputState state = (TvInputState) TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).inputMap.get(inputId);
                    access$4000 = state == null ? 0 : state.state;
                }
                Binder.restoreCallingIdentity(identity);
                return access$4000;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public List<TvContentRatingSystemInfo> getTvContentRatingSystemList(int userId) {
            List<TvContentRatingSystemInfo> access$1500;
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.READ_CONTENT_RATING_SYSTEMS") == 0) {
                int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvContentRatingSystemList");
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (TvInputManagerService.this.mLock) {
                        access$1500 = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).contentRatingSystemList;
                    }
                    Binder.restoreCallingIdentity(identity);
                    return access$1500;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } else {
                throw new SecurityException("The caller does not have permission to read content rating systems");
            }
        }

        public void sendTvInputNotifyIntent(Intent intent, int userId) {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.NOTIFY_TV_INPUTS") != 0) {
                throw new SecurityException("The caller: " + getCallingPackageName() + " doesn't have permission: " + "android.permission.NOTIFY_TV_INPUTS");
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
                switch (c) {
                    case 0:
                        if (intent.getLongExtra("android.media.tv.extra.PREVIEW_PROGRAM_ID", -1) < 0) {
                            throw new IllegalArgumentException("Invalid preview program ID.");
                        }
                        break;
                    case 1:
                        if (intent.getLongExtra("android.media.tv.extra.WATCH_NEXT_PROGRAM_ID", -1) < 0) {
                            throw new IllegalArgumentException("Invalid watch next program ID.");
                        }
                        break;
                    case 2:
                        if (intent.getLongExtra("android.media.tv.extra.PREVIEW_PROGRAM_ID", -1) < 0) {
                            throw new IllegalArgumentException("Invalid preview program ID.");
                        } else if (intent.getLongExtra("android.media.tv.extra.WATCH_NEXT_PROGRAM_ID", -1) < 0) {
                            throw new IllegalArgumentException("Invalid watch next program ID.");
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid TV input notifying action: " + intent.getAction());
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
                    try {
                        callback.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                            public void binderDied() {
                                synchronized (TvInputManagerService.this.mLock) {
                                    if (userState.callbackSet != null) {
                                        userState.callbackSet.remove(callback);
                                    }
                                }
                            }
                        }, 0);
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
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
                return isParentalControlsEnabled;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
                return isRatingBlocked;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
                return ratings;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        private void ensureParentalControlsPermission() {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.MODIFY_PARENTAL_CONTROLS") != 0) {
                throw new SecurityException("The caller does not have parental controls permission");
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:53:0x011c, code lost:
            android.os.Binder.restoreCallingIdentity(r23);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x0122, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x012d, code lost:
            r0 = th;
         */
        public void createSession(ITvInputClient client, String inputId, boolean isRecordingSession, int seq, int userId) {
            long identity;
            String str = inputId;
            int i = userId;
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, i, "createSession");
            long identity2 = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        if (i == TvInputManagerService.this.mCurrentUserId || isRecordingSession) {
                            UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                            TvInputState inputState = (TvInputState) userState.inputMap.get(str);
                            if (inputState == null) {
                                Slog.w(TvInputManagerService.TAG, "Failed to find input state for inputId=" + str);
                                TvInputManagerService.this.sendSessionTokenToClientLocked(client, str, null, null, seq);
                                Binder.restoreCallingIdentity(identity2);
                                return;
                            }
                            TvInputInfo info = inputState.info;
                            ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(info.getComponent());
                            if (serviceState == null) {
                                serviceState = new ServiceState(info.getComponent(), resolvedUserId);
                                userState.serviceStateMap.put(info.getComponent(), serviceState);
                            }
                            ServiceState serviceState2 = serviceState;
                            if (serviceState2.reconnecting) {
                                String str2 = str;
                                ServiceState serviceState3 = serviceState2;
                                TvInputManagerService.this.sendSessionTokenToClientLocked(client, str2, null, null, seq);
                                Binder.restoreCallingIdentity(identity2);
                                return;
                            }
                            ServiceState serviceState4 = serviceState2;
                            IBinder sessionToken = new Binder();
                            TvInputManagerService tvInputManagerService = TvInputManagerService.this;
                            String id = info.getId();
                            ComponentName component = info.getComponent();
                            TvInputInfo info2 = info;
                            r10 = r10;
                            TvInputState tvInputState = inputState;
                            TvInputManagerService tvInputManagerService2 = tvInputManagerService;
                            long identity3 = identity2;
                            int resolvedUserId2 = resolvedUserId;
                            try {
                                SessionState sessionState = new SessionState(sessionToken, id, component, isRecordingSession, client, seq, callingUid, resolvedUserId2);
                                userState.sessionStateMap.put(sessionToken, sessionState);
                                serviceState4.sessionTokens.add(sessionToken);
                                if (serviceState4.service != null) {
                                    try {
                                        TvInputManagerService.this.createSessionInternalLocked(serviceState4.service, sessionToken, resolvedUserId2);
                                    } catch (Throwable th) {
                                        th = th;
                                        identity = identity3;
                                    }
                                } else {
                                    TvInputManagerService.this.updateServiceConnectionLocked(info2.getComponent(), resolvedUserId2);
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                identity = identity3;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                }
                                throw th;
                            }
                        } else {
                            try {
                                TvInputManagerService.this.sendSessionTokenToClientLocked(client, str, null, null, seq);
                                Binder.restoreCallingIdentity(identity2);
                            } catch (Throwable th4) {
                                th = th4;
                                identity = identity2;
                                int i2 = resolvedUserId;
                                int i3 = callingUid;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        identity = identity2;
                        int i4 = resolvedUserId;
                        int i5 = callingUid;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            } catch (Throwable th6) {
                th = th6;
                identity = identity2;
                int i6 = resolvedUserId;
                int i7 = callingUid;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
            android.os.Binder.restoreCallingIdentity(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0059, code lost:
            return;
         */
        public void setMainSession(IBinder sessionToken, int userId) {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.CHANGE_HDMI_CEC_ACTIVE_SOURCE") == 0) {
                int callingUid = Binder.getCallingUid();
                int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "setMainSession");
                long identity = Binder.clearCallingIdentity();
                try {
                    synchronized (TvInputManagerService.this.mLock) {
                        UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                        if (userState.mainSessionToken == sessionToken) {
                            Binder.restoreCallingIdentity(identity);
                            return;
                        }
                        IBinder oldMainSessionToken = userState.mainSessionToken;
                        IBinder unused = userState.mainSessionToken = sessionToken;
                        if (sessionToken != null) {
                            TvInputManagerService.this.setMainLocked(sessionToken, true, callingUid, userId);
                        }
                        if (oldMainSessionToken != null) {
                            TvInputManagerService.this.setMainLocked(oldMainSessionToken, false, 1000, userId);
                        }
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
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
                if (surface != null) {
                    surface.release();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                if (surface != null) {
                    surface.release();
                }
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                            ITvInputSession access$5400 = TvInputManagerService.this.getSessionLocked(sessionState.hardwareSessionToken, 1000, resolvedUserId);
                            float f = 0.0f;
                            if (volume > 0.0f) {
                                f = 1.0f;
                            }
                            access$5400.setVolume(f);
                        }
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in setVolume", e);
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }

        public void tune(IBinder sessionToken, Uri channelUri, Bundle params, int userId) {
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "tune");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).tune(channelUri, params);
                        if (TvContract.isChannelUriForPassthroughInput(channelUri)) {
                            Binder.restoreCallingIdentity(identity);
                            return;
                        }
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
                    } catch (RemoteException | SessionNotFoundException e) {
                        Slog.e(TvInputManagerService.TAG, "error in tune", e);
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
            List<T> list;
            String[] strArr;
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
                        Matcher adapterMatcher = TvInputManagerService.sAdapterDirPattern.matcher(list3[i3]);
                        if (adapterMatcher.find()) {
                            int adapterId = Integer.parseInt(adapterMatcher.group(i));
                            File adapterDirectory = new File("/dev/dvb/" + fileNameInDvb);
                            String[] list4 = adapterDirectory.list();
                            int length3 = list4.length;
                            File file = adapterDirectory;
                            int i4 = 0;
                            while (i4 < length3) {
                                File devDirectory2 = devDirectory;
                                File dvbDirectory2 = dvbDirectory;
                                String fileNameInAdapter = list4[i4];
                                Matcher frontendMatcher = TvInputManagerService.sFrontEndInAdapterDirPattern.matcher(fileNameInAdapter);
                                if (frontendMatcher.find()) {
                                    String str = fileNameInAdapter;
                                    strArr = list3;
                                    deviceInfosFromPattern2.add(new DvbDeviceInfo(adapterId, Integer.parseInt(frontendMatcher.group(1))));
                                } else {
                                    strArr = list3;
                                }
                                i4++;
                                devDirectory = devDirectory2;
                                dvbDirectory = dvbDirectory2;
                                list3 = strArr;
                            }
                        }
                        i3++;
                        devDirectory = devDirectory;
                        dvbDirectory = dvbDirectory;
                        list3 = list3;
                        i = 1;
                    }
                    File file2 = dvbDirectory;
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

        public ParcelFileDescriptor openDvbDevice(DvbDeviceInfo info, int device) throws RemoteException {
            String deviceFileName;
            int i;
            String[] strArr;
            File devDirectory;
            int i2 = device;
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.DVB_DEVICE") == 0) {
                File devDirectory2 = new File("/dev");
                String[] list = devDirectory2.list();
                int length = list.length;
                boolean dvbDeviceFound = false;
                int i3 = 0;
                while (true) {
                    if (i3 >= length) {
                        break;
                    }
                    if (TextUtils.equals("dvb", list[i3])) {
                        String[] list2 = new File(TvInputManagerService.DVB_DIRECTORY).list();
                        int length2 = list2.length;
                        boolean dvbDeviceFound2 = dvbDeviceFound;
                        int i4 = 0;
                        while (true) {
                            if (i4 >= length2) {
                                devDirectory = devDirectory2;
                                strArr = list;
                                dvbDeviceFound = dvbDeviceFound2;
                                break;
                            }
                            if (TvInputManagerService.sAdapterDirPattern.matcher(list2[i4]).find()) {
                                File adapterDirectory = new File("/dev/dvb/" + fileNameInDvb);
                                String[] list3 = adapterDirectory.list();
                                int length3 = list3.length;
                                File file = adapterDirectory;
                                int i5 = 0;
                                while (true) {
                                    if (i5 >= length3) {
                                        break;
                                    }
                                    devDirectory = devDirectory2;
                                    strArr = list;
                                    if (TvInputManagerService.sFrontEndInAdapterDirPattern.matcher(list3[i5]).find()) {
                                        dvbDeviceFound2 = true;
                                        break;
                                    }
                                    i5++;
                                    devDirectory2 = devDirectory;
                                    list = strArr;
                                }
                            }
                            devDirectory = devDirectory2;
                            strArr = list;
                            if (dvbDeviceFound2) {
                                dvbDeviceFound = dvbDeviceFound2;
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
                    if (dvbDeviceFound) {
                        break;
                    }
                    i3++;
                    devDirectory2 = devDirectory;
                    list = strArr;
                }
                long identity = Binder.clearCallingIdentity();
                switch (i2) {
                    case 0:
                        deviceFileName = String.format(dvbDeviceFound ? "/dev/dvb/adapter%d/demux%d" : "/dev/dvb%d.demux%d", new Object[]{Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId())});
                        break;
                    case 1:
                        deviceFileName = String.format(dvbDeviceFound ? "/dev/dvb/adapter%d/dvr%d" : "/dev/dvb%d.dvr%d", new Object[]{Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId())});
                        break;
                    case 2:
                        deviceFileName = String.format(dvbDeviceFound ? "/dev/dvb/adapter%d/frontend%d" : "/dev/dvb%d.frontend%d", new Object[]{Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId())});
                        break;
                    default:
                        try {
                            throw new IllegalArgumentException("Invalid DVB device: " + i2);
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                }
                try {
                    File file2 = new File(deviceFileName);
                    if (2 == i2) {
                        i = 805306368;
                    } else {
                        i = 268435456;
                    }
                    ParcelFileDescriptor open = ParcelFileDescriptor.open(file2, i);
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

        /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
            r2 = com.android.server.tv.TvInputManagerService.access$5600(r11.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0091, code lost:
            if (r10 == null) goto L_0x0095;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x0093, code lost:
            r3 = r10;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0095, code lost:
            r3 = r12;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0096, code lost:
            r2 = r2.captureFrame(r3, r13, r14, r8, r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x009e, code lost:
            android.os.Binder.restoreCallingIdentity(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a1, code lost:
            return r2;
         */
        public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config, int userId) throws RemoteException {
            ensureCaptureTvInputPermission();
            long identity = Binder.clearCallingIdentity();
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "captureFrame");
            String hardwareInputId = null;
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
                                break;
                            }
                            SessionState sessionState = (SessionState) it.next();
                            if (sessionState.inputId.equals(inputId) && sessionState.hardwareSessionToken != null) {
                                hardwareInputId = ((SessionState) userState.sessionStateMap.get(sessionState.hardwareSessionToken)).inputId;
                                break;
                            }
                        }
                        String hardwareInputId2 = hardwareInputId;
                        try {
                        } catch (Throwable th) {
                            th = th;
                            String str = hardwareInputId2;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0062, code lost:
            android.os.Binder.restoreCallingIdentity(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0065, code lost:
            return true;
         */
        public boolean isSingleSessionActive(int userId) throws RemoteException {
            ensureCaptureTvInputPermission();
            long identity = Binder.clearCallingIdentity();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "isSingleSessionActive");
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    if (userState.sessionStateMap.size() == 1) {
                        Binder.restoreCallingIdentity(identity);
                        return true;
                    }
                    if (userState.sessionStateMap.size() == 2) {
                        SessionState[] sessionStates = (SessionState[]) userState.sessionStateMap.values().toArray(new SessionState[2]);
                        if (!(sessionStates[0].hardwareSessionToken == null && sessionStates[1].hardwareSessionToken == null)) {
                        }
                    }
                    Binder.restoreCallingIdentity(identity);
                    return false;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
                throw th;
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
                                pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + ((IBinder) it.next()));
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
                                pw.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + ((IBinder) it2.next()));
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

    private final class ClientState implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public IBinder clientToken;
        /* access modifiers changed from: private */
        public final List<IBinder> sessionTokens = new ArrayList();
        /* access modifiers changed from: private */
        public final int userId;

        ClientState(IBinder clientToken2, int userId2) {
            this.clientToken = clientToken2;
            this.userId = userId2;
        }

        public boolean isEmpty() {
            return this.sessionTokens.isEmpty();
        }

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

    private final class HardwareListener implements TvInputHardwareManager.Listener {
        private HardwareListener() {
        }

        public void onStateChanged(String inputId, int state) {
            synchronized (TvInputManagerService.this.mLock) {
                TvInputManagerService.this.setStateLocked(inputId, state, TvInputManagerService.this.mCurrentUserId);
            }
        }

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

        public void onHdmiDeviceUpdated(String inputId, HdmiDeviceInfo deviceInfo) {
            Integer state;
            synchronized (TvInputManagerService.this.mLock) {
                switch (deviceInfo.getDevicePowerStatus()) {
                    case 0:
                        state = 0;
                        break;
                    case 1:
                    case 2:
                    case 3:
                        state = 1;
                        break;
                    default:
                        state = null;
                        break;
                }
                if (state != null) {
                    TvInputManagerService.this.setStateLocked(inputId, state.intValue(), TvInputManagerService.this.mCurrentUserId);
                }
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

        /* JADX WARNING: Code restructure failed: missing block: B:57:0x0126, code lost:
            return;
         */
        public void onServiceConnected(ComponentName component, IBinder service) {
            synchronized (TvInputManagerService.this.mLock) {
                UserState userState = (UserState) TvInputManagerService.this.mUserStates.get(this.mUserId);
                if (userState == null) {
                    TvInputManagerService.this.mContext.unbindService(this);
                    return;
                }
                ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(this.mComponent);
                ITvInputService unused = serviceState.service = ITvInputService.Stub.asInterface(service);
                if (serviceState.isHardware && serviceState.callback == null) {
                    ServiceCallback unused2 = serviceState.callback = new ServiceCallback(this.mComponent, this.mUserId);
                    try {
                        serviceState.service.registerCallback(serviceState.callback);
                    } catch (RemoteException e) {
                        Slog.e(TvInputManagerService.TAG, "error in registerCallback", e);
                    }
                }
                for (IBinder sessionToken : serviceState.sessionTokens) {
                    TvInputManagerService.this.createSessionInternalLocked(serviceState.service, sessionToken, this.mUserId);
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

        public void onServiceDisconnected(ComponentName component) {
            if (this.mComponent.equals(component)) {
                synchronized (TvInputManagerService.this.mLock) {
                    ServiceState serviceState = (ServiceState) TvInputManagerService.this.getOrCreateUserStateLocked(this.mUserId).serviceStateMap.get(this.mComponent);
                    if (serviceState != null) {
                        boolean unused = serviceState.reconnecting = true;
                        boolean unused2 = serviceState.bound = false;
                        ITvInputService unused3 = serviceState.service = null;
                        ServiceCallback unused4 = serviceState.callback = null;
                        TvInputManagerService.this.abortPendingCreateSessionRequestsLocked(serviceState, null, this.mUserId);
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Mismatched ComponentName: " + this.mComponent + " (expected), " + component + " (actual).");
        }
    }

    private final class ServiceCallback extends ITvInputServiceCallback.Stub {
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

    private final class ServiceState {
        /* access modifiers changed from: private */
        public boolean bound;
        /* access modifiers changed from: private */
        public ServiceCallback callback;
        /* access modifiers changed from: private */
        public final ComponentName component;
        /* access modifiers changed from: private */
        public final ServiceConnection connection;
        /* access modifiers changed from: private */
        public final Map<String, TvInputInfo> hardwareInputMap;
        /* access modifiers changed from: private */
        public final boolean isHardware;
        /* access modifiers changed from: private */
        public boolean reconnecting;
        /* access modifiers changed from: private */
        public ITvInputService service;
        /* access modifiers changed from: private */
        public final List<IBinder> sessionTokens;

        private ServiceState(ComponentName component2, int userId) {
            this.sessionTokens = new ArrayList();
            this.hardwareInputMap = new HashMap();
            this.component = component2;
            this.connection = new InputServiceConnection(component2, userId);
            this.isHardware = TvInputManagerService.hasHardwarePermission(TvInputManagerService.this.mContext.getPackageManager(), component2);
        }
    }

    private final class SessionCallback extends ITvInputSessionCallback.Stub {
        private final InputChannel[] mChannels;
        private final SessionState mSessionState;

        SessionCallback(SessionState sessionState, InputChannel[] channels) {
            this.mSessionState = sessionState;
            this.mChannels = channels;
        }

        public void onSessionCreated(ITvInputSession session, IBinder hardwareSessionToken) {
            synchronized (TvInputManagerService.this.mLock) {
                ITvInputSession unused = this.mSessionState.session = session;
                IBinder unused2 = this.mSessionState.hardwareSessionToken = hardwareSessionToken;
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
            return;
         */
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

    private static class SessionNotFoundException extends IllegalArgumentException {
        public SessionNotFoundException(String name) {
            super(name);
        }
    }

    private final class SessionState implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final int callingUid;
        /* access modifiers changed from: private */
        public final ITvInputClient client;
        /* access modifiers changed from: private */
        public final ComponentName componentName;
        /* access modifiers changed from: private */
        public IBinder hardwareSessionToken;
        /* access modifiers changed from: private */
        public final String inputId;
        /* access modifiers changed from: private */
        public final boolean isRecordingSession;
        /* access modifiers changed from: private */
        public Uri logUri;
        /* access modifiers changed from: private */
        public final int seq;
        /* access modifiers changed from: private */
        public ITvInputSession session;
        /* access modifiers changed from: private */
        public final IBinder sessionToken;
        /* access modifiers changed from: private */
        public final int userId;

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

        public void binderDied() {
            synchronized (TvInputManagerService.this.mLock) {
                this.session = null;
                TvInputManagerService.this.clearSessionAndNotifyClientLocked(this);
            }
        }
    }

    private static final class TvInputState {
        /* access modifiers changed from: private */
        public TvInputInfo info;
        /* access modifiers changed from: private */
        public int state;

        private TvInputState() {
            this.state = 0;
        }

        public String toString() {
            return "info: " + this.info + "; state: " + this.state;
        }
    }

    private static final class UserState {
        /* access modifiers changed from: private */
        public final Set<ITvInputManagerCallback> callbackSet;
        /* access modifiers changed from: private */
        public final Map<IBinder, ClientState> clientStateMap;
        /* access modifiers changed from: private */
        public final List<TvContentRatingSystemInfo> contentRatingSystemList;
        /* access modifiers changed from: private */
        public Map<String, TvInputState> inputMap;
        /* access modifiers changed from: private */
        public IBinder mainSessionToken;
        /* access modifiers changed from: private */
        public final Set<String> packageSet;
        /* access modifiers changed from: private */
        public final PersistentDataStore persistentDataStore;
        /* access modifiers changed from: private */
        public final Map<ComponentName, ServiceState> serviceStateMap;
        /* access modifiers changed from: private */
        public final Map<IBinder, SessionState> sessionStateMap;

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

    private static final class WatchLogHandler extends Handler {
        static final int MSG_LOG_WATCH_END = 2;
        static final int MSG_LOG_WATCH_START = 1;
        static final int MSG_SWITCH_CONTENT_RESOLVER = 3;
        private ContentResolver mContentResolver;

        WatchLogHandler(ContentResolver contentResolver, Looper looper) {
            super(looper);
            this.mContentResolver = contentResolver;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
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
                    return;
                case 2:
                    SomeArgs args2 = (SomeArgs) msg.obj;
                    long watchEndTime = ((Long) args2.arg2).longValue();
                    ContentValues values2 = new ContentValues();
                    values2.put("watch_end_time_utc_millis", Long.valueOf(watchEndTime));
                    values2.put("session_token", ((IBinder) args2.arg1).toString());
                    this.mContentResolver.insert(TvContract.WatchedPrograms.CONTENT_URI, values2);
                    args2.recycle();
                    return;
                case 3:
                    this.mContentResolver = (ContentResolver) msg.obj;
                    return;
                default:
                    Slog.w(TvInputManagerService.TAG, "unhandled message code: " + msg.what);
                    return;
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
            for (char ch : src.toCharArray()) {
                if ("%=,".indexOf(ch) >= 0) {
                    builder.append('%');
                }
                builder.append(ch);
            }
            return builder.toString();
        }
    }

    public TvInputManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mWatchLogHandler = new WatchLogHandler(this.mContext.getContentResolver(), IoThread.get().getLooper());
        this.mTvInputHardwareManager = new TvInputHardwareManager(context, new HardwareListener());
        synchronized (this.mLock) {
            getOrCreateUserStateLocked(this.mCurrentUserId);
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.tv.TvInputManagerService$BinderService, android.os.IBinder] */
    public void onStart() {
        publishBinderService("tv_input", new BinderService());
    }

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
        }.register(this.mContext, null, UserHandle.ALL, true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
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
    public void buildTvInputListLocked(int userId, String[] updatedPackages) {
        UserState userState = getOrCreateUserStateLocked(userId);
        userState.packageSet.clear();
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServicesAsUser(new Intent("android.media.tv.TvInputService"), 132, userId);
        List<TvInputInfo> inputList = new ArrayList<>();
        for (ResolveInfo ri : services) {
            ServiceInfo si = ri.serviceInfo;
            if (!"android.permission.BIND_TV_INPUT".equals(si.permission)) {
                Slog.w(TAG, "Skipping TV input " + si.name + ": it does not require the permission " + "android.permission.BIND_TV_INPUT");
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
            TvInputInfo unused = inputState.info = info;
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
        Map unused2 = userState.inputMap = inputMap;
    }

    /* access modifiers changed from: private */
    public void buildTvContentRatingSystemListLocked(int userId) {
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
    public void switchUser(int userId) {
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
    public void clearSessionAndNotifyClientLocked(SessionState state) {
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
    public void removeUser(int userId) {
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
                IBinder unused = userState.mainSessionToken = null;
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
    public UserState getOrCreateUserStateLocked(int userId) {
        UserState userState = this.mUserStates.get(userId);
        if (userState != null) {
            return userState;
        }
        UserState userState2 = new UserState(this.mContext, userId);
        this.mUserStates.put(userId, userState2);
        return userState2;
    }

    /* access modifiers changed from: private */
    public ServiceState getServiceStateLocked(ComponentName component, int userId) {
        ServiceState serviceState = (ServiceState) getOrCreateUserStateLocked(userId).serviceStateMap.get(component);
        if (serviceState != null) {
            return serviceState;
        }
        throw new IllegalStateException("Service state not found for " + component + " (userId=" + userId + ")");
    }

    /* access modifiers changed from: private */
    public SessionState getSessionStateLocked(IBinder sessionToken, int callingUid, int userId) {
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
    public ITvInputSession getSessionLocked(IBinder sessionToken, int callingUid, int userId) {
        return getSessionLocked(getSessionStateLocked(sessionToken, callingUid, userId));
    }

    /* access modifiers changed from: private */
    public ITvInputSession getSessionLocked(SessionState sessionState) {
        ITvInputSession session = sessionState.session;
        if (session != null) {
            return session;
        }
        throw new IllegalStateException("Session not yet created for token " + sessionState.sessionToken);
    }

    /* access modifiers changed from: private */
    public int resolveCallingUserId(int callingPid, int callingUid, int requestedUserId, String methodName) {
        return ActivityManager.handleIncomingUser(callingPid, callingUid, requestedUserId, false, false, methodName, null);
    }

    /* access modifiers changed from: private */
    public void updateServiceConnectionLocked(ComponentName component, int userId) {
        boolean shouldBind;
        UserState userState = getOrCreateUserStateLocked(userId);
        ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(component);
        if (serviceState != null) {
            boolean z = false;
            if (serviceState.reconnecting) {
                if (serviceState.sessionTokens.isEmpty()) {
                    boolean unused = serviceState.reconnecting = false;
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
                boolean unused2 = serviceState.bound = this.mContext.bindServiceAsUser(new Intent("android.media.tv.TvInputService").setComponent(component), serviceState.connection, 33554433, new UserHandle(userId));
            }
        }
    }

    /* access modifiers changed from: private */
    public void abortPendingCreateSessionRequestsLocked(ServiceState serviceState, String inputId, int userId) {
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
    public void createSessionInternalLocked(ITvInputService service, IBinder sessionToken, int userId) {
        ITvInputService iTvInputService = service;
        IBinder iBinder = sessionToken;
        int i = userId;
        SessionState sessionState = (SessionState) getOrCreateUserStateLocked(i).sessionStateMap.get(iBinder);
        RemoteException[] channels = InputChannel.openInputChannelPair(sessionToken.toString());
        SessionCallback sessionCallback = new SessionCallback(sessionState, channels);
        try {
            if (sessionState.isRecordingSession) {
                iTvInputService.createRecordingSession(sessionCallback, sessionState.inputId);
            } else {
                iTvInputService.createSession(channels[1], sessionCallback, sessionState.inputId);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "error in createSession", e);
            removeSessionStateLocked(iBinder, i);
            sendSessionTokenToClientLocked(sessionState.client, sessionState.inputId, null, null, sessionState.seq);
        }
        channels[1].dispose();
    }

    /* access modifiers changed from: private */
    public void sendSessionTokenToClientLocked(ITvInputClient client, String inputId, IBinder sessionToken, InputChannel channel, int seq) {
        try {
            client.onSessionCreated(inputId, sessionToken, channel, seq);
        } catch (RemoteException e) {
            Slog.e(TAG, "error in onSessionCreated", e);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        if (0 == 0) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0035, code lost:
        removeSessionStateLocked(r6, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0022, code lost:
        if (r1 != null) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0024, code lost:
        com.android.server.tv.TvInputManagerService.SessionState.access$1702(r1, null);
     */
    public void releaseSessionLocked(IBinder sessionToken, int callingUid, int userId) {
        SessionState sessionState = null;
        try {
            sessionState = getSessionStateLocked(sessionToken, callingUid, userId);
            if (sessionState.session != null) {
                if (sessionToken == getOrCreateUserStateLocked(userId).mainSessionToken) {
                    setMainLocked(sessionToken, false, callingUid, userId);
                }
                sessionState.session.release();
            }
        } catch (RemoteException | SessionNotFoundException e) {
            Slog.e(TAG, "error in releaseSession", e);
        } catch (Throwable th) {
            if (0 != 0) {
                ITvInputSession unused = null.session = null;
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void removeSessionStateLocked(IBinder sessionToken, int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        if (sessionToken == userState.mainSessionToken) {
            IBinder unused = userState.mainSessionToken = null;
        }
        SessionState sessionState = (SessionState) userState.sessionStateMap.remove(sessionToken);
        if (sessionState != null) {
            ClientState clientState = (ClientState) userState.clientStateMap.get(sessionState.client.asBinder());
            if (clientState != null) {
                clientState.sessionTokens.remove(sessionToken);
                if (clientState.isEmpty()) {
                    userState.clientStateMap.remove(sessionState.client.asBinder());
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
    public void setMainLocked(IBinder sessionToken, boolean isMain, int callingUid, int userId) {
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
    public void notifyInputStateChangedLocked(UserState userState, String inputId, int state, ITvInputManagerCallback targetCallback) {
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
    public void updateTvInputInfoLocked(UserState userState, TvInputInfo inputInfo) {
        String inputId = inputInfo.getId();
        TvInputState inputState = (TvInputState) userState.inputMap.get(inputId);
        if (inputState == null) {
            Slog.e(TAG, "failed to set input info - unknown input id " + inputId);
            return;
        }
        TvInputInfo unused = inputState.info = inputInfo;
        for (ITvInputManagerCallback callback : userState.callbackSet) {
            try {
                callback.onTvInputInfoUpdated(inputInfo);
            } catch (RemoteException e) {
                Slog.e(TAG, "failed to report updated input info to callback", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setStateLocked(String inputId, int state, int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        TvInputState inputState = (TvInputState) userState.inputMap.get(inputId);
        ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(inputState.info.getComponent());
        int oldState = inputState.state;
        int unused = inputState.state = state;
        if ((serviceState == null || serviceState.service != null || (serviceState.sessionTokens.isEmpty() && !serviceState.isHardware)) && oldState != state) {
            notifyInputStateChangedLocked(userState, inputId, state, null);
        }
    }
}
