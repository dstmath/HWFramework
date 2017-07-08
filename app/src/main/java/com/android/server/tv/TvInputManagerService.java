package com.android.server.tv;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Rect;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.PlaybackParams;
import android.media.tv.DvbDeviceInfo;
import android.media.tv.ITvInputClient;
import android.media.tv.ITvInputHardware;
import android.media.tv.ITvInputHardwareCallback;
import android.media.tv.ITvInputManager.Stub;
import android.media.tv.ITvInputManagerCallback;
import android.media.tv.ITvInputService;
import android.media.tv.ITvInputServiceCallback;
import android.media.tv.ITvInputSession;
import android.media.tv.ITvInputSessionCallback;
import android.media.tv.TvContentRating;
import android.media.tv.TvContentRatingSystemInfo;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;
import android.media.tv.TvContract.Programs;
import android.media.tv.TvContract.WatchedPrograms;
import android.media.tv.TvInputHardwareInfo;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputInfo.Builder;
import android.media.tv.TvStreamConfig;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
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
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.IoThread;
import com.android.server.SystemService;
import com.android.server.am.ProcessList;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TvInputManagerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String TAG = "TvInputManagerService";
    private static final Pattern sFrontEndDevicePattern = null;
    private final Context mContext;
    private int mCurrentUserId;
    private final Object mLock;
    private final TvInputHardwareManager mTvInputHardwareManager;
    private final SparseArray<UserState> mUserStates;
    private final WatchLogHandler mWatchLogHandler;

    private final class BinderService extends Stub {

        /* renamed from: com.android.server.tv.TvInputManagerService.BinderService.1 */
        class AnonymousClass1 implements DeathRecipient {
            final /* synthetic */ ITvInputManagerCallback val$callback;
            final /* synthetic */ UserState val$userState;

            AnonymousClass1(UserState val$userState, ITvInputManagerCallback val$callback) {
                this.val$userState = val$userState;
                this.val$callback = val$callback;
            }

            public void binderDied() {
                synchronized (TvInputManagerService.this.mLock) {
                    if (this.val$userState.callbackSet != null) {
                        this.val$userState.callbackSet.remove(this.val$callback);
                    }
                }
            }
        }

        private BinderService() {
        }

        public List<TvInputInfo> getTvInputList(int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputList");
            long identity = Binder.clearCallingIdentity();
            try {
                List<TvInputInfo> inputList;
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    inputList = new ArrayList();
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
            TvInputInfo tvInputInfo = null;
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputInfo");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputState state = (TvInputState) TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).inputMap.get(inputId);
                    if (state != null) {
                        tvInputInfo = state.info;
                    }
                }
                return tvInputInfo;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void updateTvInputInfo(TvInputInfo inputInfo, int userId) {
            String inputInfoPackageName = inputInfo.getServiceInfo().packageName;
            String callingPackageName = getCallingPackageName();
            if (TextUtils.equals(inputInfoPackageName, callingPackageName)) {
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
                return "unknown";
            }
            return packages[0];
        }

        public int getTvInputState(String inputId, int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvInputState");
            long identity = Binder.clearCallingIdentity();
            try {
                int -get1;
                synchronized (TvInputManagerService.this.mLock) {
                    TvInputState state = (TvInputState) TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).inputMap.get(inputId);
                    -get1 = state == null ? 0 : state.state;
                }
                return -get1;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public List<TvContentRatingSystemInfo> getTvContentRatingSystemList(int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getTvContentRatingSystemList");
            long identity = Binder.clearCallingIdentity();
            try {
                List<TvContentRatingSystemInfo> -get2;
                synchronized (TvInputManagerService.this.mLock) {
                    -get2 = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).contentRatingSystemList;
                }
                return -get2;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void registerCallback(ITvInputManagerCallback callback, int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "registerCallback");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    userState.callbackSet.add(callback);
                    try {
                        callback.asBinder().linkToDeath(new AnonymousClass1(userState, callback), 0);
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
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean isParentalControlsEnabled(int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "isParentalControlsEnabled");
            long identity = Binder.clearCallingIdentity();
            try {
                boolean isParentalControlsEnabled;
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
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "isRatingBlocked");
            long identity = Binder.clearCallingIdentity();
            try {
                boolean isRatingBlocked;
                synchronized (TvInputManagerService.this.mLock) {
                    isRatingBlocked = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId).persistentDataStore.isRatingBlocked(TvContentRating.unflattenFromString(rating));
                }
                return isRatingBlocked;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public List<String> getBlockedRatings(int userId) {
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), Binder.getCallingUid(), userId, "getBlockedRatings");
            long identity = Binder.clearCallingIdentity();
            try {
                List<String> ratings;
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    ratings = new ArrayList();
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
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "createSession");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    if (userId == TvInputManagerService.this.mCurrentUserId || isRecordingSession) {
                        UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                        TvInputState inputState = (TvInputState) userState.inputMap.get(inputId);
                        if (inputState == null) {
                            Slog.w(TvInputManagerService.TAG, "Failed to find input state for inputId=" + inputId);
                            TvInputManagerService.this.sendSessionTokenToClientLocked(client, inputId, null, null, seq);
                            return;
                        }
                        TvInputInfo info = inputState.info;
                        ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(info.getComponent());
                        if (serviceState == null) {
                            ServiceState serviceState2 = new ServiceState(info.getComponent(), resolvedUserId, null);
                            userState.serviceStateMap.put(info.getComponent(), serviceState2);
                        }
                        if (serviceState.reconnecting) {
                            TvInputManagerService.this.sendSessionTokenToClientLocked(client, inputId, null, null, seq);
                            Binder.restoreCallingIdentity(identity);
                            return;
                        }
                        IBinder sessionToken = new Binder();
                        userState.sessionStateMap.put(sessionToken, new SessionState(sessionToken, info.getId(), info.getComponent(), isRecordingSession, client, seq, callingUid, resolvedUserId, null));
                        serviceState.sessionTokens.add(sessionToken);
                        if (serviceState.service != null) {
                            TvInputManagerService.this.createSessionInternalLocked(serviceState.service, sessionToken, resolvedUserId);
                        } else {
                            TvInputManagerService.this.updateServiceConnectionLocked(info.getComponent(), resolvedUserId);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return;
                    }
                    TvInputManagerService.this.sendSessionTokenToClientLocked(client, inputId, null, null, seq);
                    Binder.restoreCallingIdentity(identity);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
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
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "setMainSession");
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    if (userState.mainSessionToken == sessionToken) {
                        return;
                    }
                    IBinder oldMainSessionToken = userState.mainSessionToken;
                    userState.mainSessionToken = sessionToken;
                    if (sessionToken != null) {
                        TvInputManagerService.this.setMainLocked(sessionToken, true, callingUid, userId);
                    }
                    if (oldMainSessionToken != null) {
                        TvInputManagerService.this.setMainLocked(oldMainSessionToken, TvInputManagerService.DEBUG, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, userId);
                    }
                    Binder.restoreCallingIdentity(identity);
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
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
                            TvInputManagerService.this.getSessionLocked(sessionState.hardwareSessionToken, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, resolvedUserId).setSurface(surface);
                        }
                    } catch (Exception e) {
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
                            TvInputManagerService.this.getSessionLocked(sessionState.hardwareSessionToken, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, resolvedUserId).dispatchSurfaceChanged(format, width, height);
                        }
                    } catch (Exception e) {
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
                            TvInputManagerService.this.getSessionLocked(sessionState.hardwareSessionToken, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, resolvedUserId).setVolume(volume > 0.0f ? 1.0f : 0.0f);
                        }
                    } catch (Exception e) {
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
                    try {
                        TvInputManagerService.this.getSessionLocked(sessionToken, callingUid, resolvedUserId).tune(channelUri, params);
                        if (TvContract.isChannelUriForPassthroughInput(channelUri)) {
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
                        Binder.restoreCallingIdentity(identity);
                    } catch (Exception e) {
                        Slog.e(TvInputManagerService.TAG, "error in tune", e);
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                    } catch (Exception e) {
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
                List<TvInputHardwareInfo> hardwareList = TvInputManagerService.this.mTvInputHardwareManager.getHardwareList();
                return hardwareList;
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
                ITvInputHardware acquireHardware = TvInputManagerService.this.mTvInputHardwareManager.acquireHardware(deviceId, callback, info, callingUid, TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "acquireTvInputHardware"));
                return acquireHardware;
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
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.DVB_DEVICE") != 0) {
                throw new SecurityException("Requires DVB_DEVICE permission");
            }
            long identity = Binder.clearCallingIdentity();
            try {
                ArrayList<DvbDeviceInfo> deviceInfos = new ArrayList();
                for (String fileName : new File("/dev").list()) {
                    Matcher matcher = TvInputManagerService.sFrontEndDevicePattern.matcher(fileName);
                    if (matcher.find()) {
                        deviceInfos.add(new DvbDeviceInfo(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
                    }
                }
                List<DvbDeviceInfo> unmodifiableList = Collections.unmodifiableList(deviceInfos);
                return unmodifiableList;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public ParcelFileDescriptor openDvbDevice(DvbDeviceInfo info, int device) throws RemoteException {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.DVB_DEVICE") != 0) {
                throw new SecurityException("Requires DVB_DEVICE permission");
            }
            String deviceFileName;
            long identity = Binder.clearCallingIdentity();
            switch (device) {
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    deviceFileName = String.format("/dev/dvb%d.demux%d", new Object[]{Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId())});
                    break;
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    deviceFileName = String.format("/dev/dvb%d.dvr%d", new Object[]{Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId())});
                    break;
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    deviceFileName = String.format("/dev/dvb%d.frontend%d", new Object[]{Integer.valueOf(info.getAdapterId()), Integer.valueOf(info.getDeviceId())});
                    break;
                default:
                    try {
                        throw new IllegalArgumentException("Invalid DVB device: " + device);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
            }
            try {
                int i;
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
        }

        public List<TvStreamConfig> getAvailableTvStreamConfigList(String inputId, int userId) throws RemoteException {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.CAPTURE_TV_INPUT") != 0) {
                throw new SecurityException("Requires CAPTURE_TV_INPUT permission");
            }
            long identity = Binder.clearCallingIdentity();
            int callingUid = Binder.getCallingUid();
            try {
                List<TvStreamConfig> availableTvStreamConfigList = TvInputManagerService.this.mTvInputHardwareManager.getAvailableTvStreamConfigList(inputId, callingUid, TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "getAvailableTvStreamConfigList"));
                return availableTvStreamConfigList;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean captureFrame(String inputId, Surface surface, TvStreamConfig config, int userId) throws RemoteException {
            if (TvInputManagerService.this.mContext.checkCallingPermission("android.permission.CAPTURE_TV_INPUT") != 0) {
                throw new SecurityException("Requires CAPTURE_TV_INPUT permission");
            }
            long identity = Binder.clearCallingIdentity();
            int callingUid = Binder.getCallingUid();
            int resolvedUserId = TvInputManagerService.this.resolveCallingUserId(Binder.getCallingPid(), callingUid, userId, "captureFrame");
            String hardwareInputId = null;
            try {
                synchronized (TvInputManagerService.this.mLock) {
                    UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(resolvedUserId);
                    if (userState.inputMap.get(inputId) == null) {
                        Slog.e(TvInputManagerService.TAG, "input not found for " + inputId);
                        return TvInputManagerService.DEBUG;
                    }
                    String str;
                    for (SessionState sessionState : userState.sessionStateMap.values()) {
                        if (sessionState.inputId.equals(inputId) && sessionState.hardwareSessionToken != null) {
                            hardwareInputId = ((SessionState) userState.sessionStateMap.get(sessionState.hardwareSessionToken)).inputId;
                            break;
                        }
                    }
                    TvInputHardwareManager -get3 = TvInputManagerService.this.mTvInputHardwareManager;
                    if (hardwareInputId != null) {
                        str = hardwareInputId;
                    } else {
                        str = inputId;
                    }
                    boolean captureFrame = -get3.captureFrame(str, surface, config, callingUid, resolvedUserId);
                    Binder.restoreCallingIdentity(identity);
                    return captureFrame;
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean isSingleSessionActive(int userId) throws RemoteException {
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
                    return TvInputManagerService.DEBUG;
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
            if (TvInputManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump TvInputManager from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            synchronized (TvInputManagerService.this.mLock) {
                pw.println("User Ids (Current user: " + TvInputManagerService.this.mCurrentUserId + "):");
                pw.increaseIndent();
                int i = 0;
                while (true) {
                    if (i >= TvInputManagerService.this.mUserStates.size()) {
                        break;
                    }
                    pw.println(Integer.valueOf(TvInputManagerService.this.mUserStates.keyAt(i)));
                    i++;
                }
                pw.decreaseIndent();
                i = 0;
                while (true) {
                    if (i < TvInputManagerService.this.mUserStates.size()) {
                        int userId = TvInputManagerService.this.mUserStates.keyAt(i);
                        UserState userState = TvInputManagerService.this.getOrCreateUserStateLocked(userId);
                        pw.println("UserState (" + userId + "):");
                        pw.increaseIndent();
                        pw.println("inputMap: inputId -> TvInputState");
                        pw.increaseIndent();
                        for (Entry<String, TvInputState> entry : userState.inputMap.entrySet()) {
                            pw.println(((String) entry.getKey()) + ": " + entry.getValue());
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
                        for (Entry<IBinder, ClientState> entry2 : userState.clientStateMap.entrySet()) {
                            ClientState client = (ClientState) entry2.getValue();
                            pw.println(entry2.getKey() + ": " + client);
                            pw.increaseIndent();
                            pw.println("sessionTokens:");
                            pw.increaseIndent();
                            for (IBinder token : client.sessionTokens) {
                                pw.println("" + token);
                            }
                            pw.decreaseIndent();
                            pw.println("clientTokens: " + client.clientToken);
                            pw.println("userId: " + client.userId);
                            pw.decreaseIndent();
                        }
                        pw.decreaseIndent();
                        pw.println("serviceStateMap: ComponentName -> ServiceState");
                        pw.increaseIndent();
                        for (Entry<ComponentName, ServiceState> entry3 : userState.serviceStateMap.entrySet()) {
                            ServiceState service = (ServiceState) entry3.getValue();
                            pw.println(entry3.getKey() + ": " + service);
                            pw.increaseIndent();
                            pw.println("sessionTokens:");
                            pw.increaseIndent();
                            for (IBinder token2 : service.sessionTokens) {
                                pw.println("" + token2);
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
                        for (Entry<IBinder, SessionState> entry4 : userState.sessionStateMap.entrySet()) {
                            SessionState session = (SessionState) entry4.getValue();
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
                        i++;
                    }
                }
            }
            TvInputManagerService.this.mTvInputHardwareManager.dump(fd, writer, args);
        }
    }

    private final class ClientState implements DeathRecipient {
        private IBinder clientToken;
        private final List<IBinder> sessionTokens;
        private final int userId;

        ClientState(IBinder clientToken, int userId) {
            this.sessionTokens = new ArrayList();
            this.clientToken = clientToken;
            this.userId = userId;
        }

        public boolean isEmpty() {
            return this.sessionTokens.isEmpty();
        }

        public void binderDied() {
            synchronized (TvInputManagerService.this.mLock) {
                ClientState clientState = (ClientState) TvInputManagerService.this.getOrCreateUserStateLocked(this.userId).clientStateMap.get(this.clientToken);
                if (clientState != null) {
                    while (clientState.sessionTokens.size() > 0) {
                        TvInputManagerService.this.releaseSessionLocked((IBinder) clientState.sessionTokens.get(0), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, this.userId);
                    }
                }
                this.clientToken = null;
            }
        }
    }

    private final class HardwareListener implements Listener {
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onHdmiDeviceUpdated(String inputId, HdmiDeviceInfo deviceInfo) {
            synchronized (TvInputManagerService.this.mLock) {
                Integer state;
                switch (deviceInfo.getDevicePowerStatus()) {
                    case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                        state = Integer.valueOf(0);
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    case H.REPORT_LOSING_FOCUS /*3*/:
                        state = Integer.valueOf(1);
                        break;
                    default:
                        state = null;
                        break;
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
                for (IBinder sessionToken : serviceState.sessionTokens) {
                    TvInputManagerService.this.createSessionInternalLocked(serviceState.service, sessionToken, this.mUserId);
                }
                for (TvInputState inputState : userState.inputMap.values()) {
                    if (inputState.info.getComponent().equals(component) && inputState.state != 0) {
                        TvInputManagerService.this.notifyInputStateChangedLocked(userState, inputState.info.getId(), inputState.state, null);
                    }
                }
                if (serviceState.isHardware) {
                    serviceState.hardwareInputList.clear();
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
                        } catch (RemoteException e22) {
                            Slog.e(TvInputManagerService.TAG, "error in notifyHdmiDeviceAdded", e22);
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
                        serviceState.reconnecting = true;
                        serviceState.bound = TvInputManagerService.DEBUG;
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
            TvInputManagerService.this.getServiceStateLocked(this.mComponent, this.mUserId).hardwareInputList.add(inputInfo);
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
                ServiceState serviceState = TvInputManagerService.this.getServiceStateLocked(this.mComponent, this.mUserId);
                boolean removed = TvInputManagerService.DEBUG;
                Iterator<TvInputInfo> it = serviceState.hardwareInputList.iterator();
                while (it.hasNext()) {
                    if (((TvInputInfo) it.next()).getId().equals(inputId)) {
                        it.remove();
                        removed = true;
                        break;
                    }
                }
                if (removed) {
                    TvInputManagerService.this.buildTvInputListLocked(this.mUserId, null);
                    TvInputManagerService.this.mTvInputHardwareManager.removeHardwareInput(inputId);
                } else {
                    Slog.e(TvInputManagerService.TAG, "failed to remove input " + inputId);
                }
            }
        }
    }

    private final class ServiceState {
        private boolean bound;
        private ServiceCallback callback;
        private final ComponentName component;
        private final ServiceConnection connection;
        private final List<TvInputInfo> hardwareInputList;
        private final boolean isHardware;
        private boolean reconnecting;
        private ITvInputService service;
        private final List<IBinder> sessionTokens;

        private ServiceState(ComponentName component, int userId) {
            this.sessionTokens = new ArrayList();
            this.hardwareInputList = new ArrayList();
            this.component = component;
            this.connection = new InputServiceConnection(component, userId, null);
            this.isHardware = TvInputManagerService.hasHardwarePermission(TvInputManagerService.this.mContext.getPackageManager(), component);
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
                        return TvInputManagerService.DEBUG;
                    }
                }
                clientState.sessionTokens.add(this.mSessionState.sessionToken);
                return true;
            } catch (RemoteException e2) {
                Slog.e(TvInputManagerService.TAG, "session process has already died", e2);
                return TvInputManagerService.DEBUG;
            }
        }

        public void onChannelRetuned(Uri channelUri) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onChannelRetuned(channelUri, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onChannelRetuned", e);
                }
            }
        }

        public void onTracksChanged(List<TvTrackInfo> tracks) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onTracksChanged(tracks, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onTracksChanged", e);
                }
            }
        }

        public void onTrackSelected(int type, String trackId) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onTrackSelected(type, trackId, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onTrackSelected", e);
                }
            }
        }

        public void onVideoAvailable() {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onVideoAvailable(this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onVideoAvailable", e);
                }
            }
        }

        public void onVideoUnavailable(int reason) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onVideoUnavailable(reason, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onVideoUnavailable", e);
                }
            }
        }

        public void onContentAllowed() {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onContentAllowed(this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onContentAllowed", e);
                }
            }
        }

        public void onContentBlocked(String rating) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onContentBlocked(rating, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onContentBlocked", e);
                }
            }
        }

        public void onLayoutSurface(int left, int top, int right, int bottom) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onLayoutSurface(left, top, right, bottom, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onLayoutSurface", e);
                }
            }
        }

        public void onSessionEvent(String eventType, Bundle eventArgs) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onSessionEvent(eventType, eventArgs, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onSessionEvent", e);
                }
            }
        }

        public void onTimeShiftStatusChanged(int status) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onTimeShiftStatusChanged(status, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onTimeShiftStatusChanged", e);
                }
            }
        }

        public void onTimeShiftStartPositionChanged(long timeMs) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onTimeShiftStartPositionChanged(timeMs, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onTimeShiftStartPositionChanged", e);
                }
            }
        }

        public void onTimeShiftCurrentPositionChanged(long timeMs) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onTimeShiftCurrentPositionChanged(timeMs, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onTimeShiftCurrentPositionChanged", e);
                }
            }
        }

        public void onTuned(Uri channelUri) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onTuned(this.mSessionState.seq, channelUri);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onTuned", e);
                }
            }
        }

        public void onRecordingStopped(Uri recordedProgramUri) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onRecordingStopped(recordedProgramUri, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onRecordingStopped", e);
                }
            }
        }

        public void onError(int error) {
            synchronized (TvInputManagerService.this.mLock) {
                if (this.mSessionState.session == null || this.mSessionState.client == null) {
                    return;
                }
                try {
                    this.mSessionState.client.onError(error, this.mSessionState.seq);
                } catch (RemoteException e) {
                    Slog.e(TvInputManagerService.TAG, "error in onError", e);
                }
            }
        }
    }

    private static class SessionNotFoundException extends IllegalArgumentException {
        public SessionNotFoundException(String name) {
            super(name);
        }
    }

    private final class SessionState implements DeathRecipient {
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

        private SessionState(IBinder sessionToken, String inputId, ComponentName componentName, boolean isRecordingSession, ITvInputClient client, int seq, int callingUid, int userId) {
            this.sessionToken = sessionToken;
            this.inputId = inputId;
            this.componentName = componentName;
            this.isRecordingSession = isRecordingSession;
            this.client = client;
            this.seq = seq;
            this.callingUid = callingUid;
            this.userId = userId;
        }

        public void binderDied() {
            synchronized (TvInputManagerService.this.mLock) {
                this.session = null;
                TvInputManagerService.this.clearSessionAndNotifyClientLocked(this);
            }
        }
    }

    private static final class TvInputState {
        private TvInputInfo info;
        private int state;

        private TvInputState() {
            this.state = 0;
        }

        public String toString() {
            return "info: " + this.info + "; state: " + this.state;
        }
    }

    private static final class UserState {
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
            SomeArgs args;
            IBinder sessionToken;
            ContentValues values;
            switch (msg.what) {
                case MSG_LOG_WATCH_START /*1*/:
                    args = msg.obj;
                    String packageName = args.arg1;
                    long watchStartTime = ((Long) args.arg2).longValue();
                    long channelId = ((Long) args.arg3).longValue();
                    Bundle tuneParams = args.arg4;
                    sessionToken = args.arg5;
                    values = new ContentValues();
                    values.put("package_name", packageName);
                    values.put("watch_start_time_utc_millis", Long.valueOf(watchStartTime));
                    values.put("channel_id", Long.valueOf(channelId));
                    if (tuneParams != null) {
                        values.put("tune_params", encodeTuneParams(tuneParams));
                    }
                    values.put("session_token", sessionToken.toString());
                    this.mContentResolver.insert(WatchedPrograms.CONTENT_URI, values);
                    args.recycle();
                case MSG_LOG_WATCH_END /*2*/:
                    args = (SomeArgs) msg.obj;
                    sessionToken = (IBinder) args.arg1;
                    long watchEndTime = ((Long) args.arg2).longValue();
                    values = new ContentValues();
                    values.put("watch_end_time_utc_millis", Long.valueOf(watchEndTime));
                    values.put("session_token", sessionToken.toString());
                    this.mContentResolver.insert(WatchedPrograms.CONTENT_URI, values);
                    args.recycle();
                case MSG_SWITCH_CONTENT_RESOLVER /*3*/:
                    this.mContentResolver = (ContentResolver) msg.obj;
                default:
                    Slog.w(TvInputManagerService.TAG, "unhandled message code: " + msg.what);
            }
        }

        private String encodeTuneParams(Bundle tuneParams) {
            StringBuilder builder = new StringBuilder();
            Iterator<String> it = tuneParams.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
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
            String ENCODING_TARGET_CHARACTERS = "%=,";
            StringBuilder builder = new StringBuilder();
            char[] toCharArray = src.toCharArray();
            int length = toCharArray.length;
            for (int i = 0; i < length; i += MSG_LOG_WATCH_START) {
                char ch = toCharArray[i];
                if ("%=,".indexOf(ch) >= 0) {
                    builder.append('%');
                }
                builder.append(ch);
            }
            return builder.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.tv.TvInputManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.tv.TvInputManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.tv.TvInputManagerService.<clinit>():void");
    }

    public TvInputManagerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mCurrentUserId = 0;
        this.mUserStates = new SparseArray();
        this.mContext = context;
        this.mWatchLogHandler = new WatchLogHandler(this.mContext.getContentResolver(), IoThread.get().getLooper());
        this.mTvInputHardwareManager = new TvInputHardwareManager(context, new HardwareListener());
        synchronized (this.mLock) {
            getOrCreateUserStateLocked(this.mCurrentUserId);
        }
    }

    public void onStart() {
        publishBinderService("tv_input", new BinderService());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            registerBroadcastReceivers();
        } else if (phase == NetdResponseCode.InterfaceChange) {
            synchronized (this.mLock) {
                buildTvInputListLocked(this.mCurrentUserId, null);
                buildTvContentRatingSystemListLocked(this.mCurrentUserId);
            }
        }
        this.mTvInputHardwareManager.onBootPhase(phase);
    }

    public void onUnlockUser(int userHandle) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId != userHandle) {
                return;
            }
            buildTvInputListLocked(this.mCurrentUserId, null);
            buildTvContentRatingSystemListLocked(this.mCurrentUserId);
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

            public void onPackageRemoved(String packageName, int uid) {
                synchronized (TvInputManagerService.this.mLock) {
                    if (TvInputManagerService.this.getOrCreateUserStateLocked(getChangingUserId()).packageSet.contains(packageName)) {
                        ArrayList<ContentProviderOperation> operations = new ArrayList();
                        String selection = "package_name=?";
                        String[] selectionArgs = new String[]{packageName};
                        operations.add(ContentProviderOperation.newDelete(Channels.CONTENT_URI).withSelection(selection, selectionArgs).build());
                        operations.add(ContentProviderOperation.newDelete(Programs.CONTENT_URI).withSelection(selection, selectionArgs).build());
                        operations.add(ContentProviderOperation.newDelete(WatchedPrograms.CONTENT_URI).withSelection(selection, selectionArgs).build());
                        try {
                            ContentProviderResult[] results = TvInputManagerService.this.getContentResolverForUser(getChangingUserId()).applyBatch("android.media.tv", operations);
                        } catch (Exception e) {
                            Slog.e(TvInputManagerService.TAG, "error in applyBatch", e);
                        }
                        return;
                    }
                }
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

    private static boolean hasHardwarePermission(PackageManager pm, ComponentName component) {
        return pm.checkPermission("android.permission.TV_INPUT_HARDWARE", component.getPackageName()) == 0 ? true : DEBUG;
    }

    private void buildTvInputListLocked(int userId, String[] updatedPackages) {
        ComponentName component;
        ServiceState serviceState;
        UserState userState = getOrCreateUserStateLocked(userId);
        userState.packageSet.clear();
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServicesAsUser(new Intent("android.media.tv.TvInputService"), 132, userId);
        List<TvInputInfo> inputList = new ArrayList();
        for (ResolveInfo ri : services) {
            ServiceInfo si = ri.serviceInfo;
            if ("android.permission.BIND_TV_INPUT".equals(si.permission)) {
                component = new ComponentName(si.packageName, si.name);
                if (hasHardwarePermission(pm, component)) {
                    serviceState = (ServiceState) userState.serviceStateMap.get(component);
                    if (serviceState == null) {
                        userState.serviceStateMap.put(component, new ServiceState(component, userId, null));
                        updateServiceConnectionLocked(component, userId);
                    } else {
                        inputList.addAll(serviceState.hardwareInputList);
                    }
                } else {
                    try {
                        inputList.add(new Builder(this.mContext, ri).build());
                    } catch (Exception e) {
                        Slog.e(TAG, "failed to load TV input " + si.name, e);
                    }
                }
                userState.packageSet.add(si.packageName);
            } else {
                Slog.w(TAG, "Skipping TV input " + si.name + ": it does not require the permission " + "android.permission.BIND_TV_INPUT");
            }
        }
        Map<String, TvInputState> inputMap = new HashMap();
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
                component = ((TvInputState) inputMap.get(inputId)).info.getComponent();
                for (String updatedPackage : updatedPackages) {
                    if (component.getPackageName().equals(updatedPackage)) {
                        updateServiceConnectionLocked(component, userId);
                        notifyInputUpdatedLocked(userState, inputId);
                        break;
                    }
                }
            }
        }
        for (String inputId2 : userState.inputMap.keySet()) {
            if (!inputMap.containsKey(inputId2)) {
                serviceState = (ServiceState) userState.serviceStateMap.get(((TvInputState) userState.inputMap.get(inputId2)).info.getComponent());
                if (serviceState != null) {
                    abortPendingCreateSessionRequestsLocked(serviceState, inputId2, userId);
                }
                notifyInputRemovedLocked(userState, inputId2);
            }
        }
        userState.inputMap.clear();
        userState.inputMap = inputMap;
    }

    private void buildTvContentRatingSystemListLocked(int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        userState.contentRatingSystemList.clear();
        for (ResolveInfo resolveInfo : this.mContext.getPackageManager().queryBroadcastReceivers(new Intent("android.media.tv.action.QUERY_CONTENT_RATING_SYSTEMS"), DumpState.DUMP_PACKAGES)) {
            ActivityInfo receiver = resolveInfo.activityInfo;
            Bundle metaData = receiver.metaData;
            if (metaData != null) {
                int xmlResId = metaData.getInt("android.media.tv.metadata.CONTENT_RATING_SYSTEMS");
                if (xmlResId == 0) {
                    Slog.w(TAG, "Missing meta-data 'android.media.tv.metadata.CONTENT_RATING_SYSTEMS' on receiver " + receiver.packageName + "/" + receiver.name);
                } else {
                    userState.contentRatingSystemList.add(TvContentRatingSystemInfo.createTvContentRatingSystemInfo(xmlResId, receiver.applicationInfo));
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void switchUser(int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId) {
                return;
            }
            UserState userState = (UserState) this.mUserStates.get(this.mCurrentUserId);
            List<SessionState> sessionStatesToRelease = new ArrayList();
            for (SessionState sessionState : userState.sessionStateMap.values()) {
                if (!(sessionState.session == null || sessionState.isRecordingSession)) {
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
                ServiceState serviceState = (ServiceState) userState.serviceStateMap.get((ComponentName) it.next());
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
                releaseSessionLocked(sessionState.sessionToken, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, state.userId);
                try {
                    sessionState.client.onSessionReleased(sessionState.seq);
                } catch (RemoteException e2) {
                    Slog.e(TAG, "error in onSessionReleased", e2);
                }
            }
        }
        removeSessionStateLocked(state.sessionToken, state.userId);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeUser(int userId) {
        synchronized (this.mLock) {
            UserState userState = (UserState) this.mUserStates.get(userId);
            if (userState == null) {
                return;
            }
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

    private ContentResolver getContentResolverForUser(int userId) {
        Context context;
        UserHandle user = new UserHandle(userId);
        try {
            context = this.mContext.createPackageContextAsUser("android", 0, user);
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "failed to create package context as user " + user);
            context = this.mContext;
        }
        return context.getContentResolver();
    }

    private UserState getOrCreateUserStateLocked(int userId) {
        UserState userState = (UserState) this.mUserStates.get(userId);
        if (userState != null) {
            return userState;
        }
        userState = new UserState(userId, null);
        this.mUserStates.put(userId, userState);
        return userState;
    }

    private ServiceState getServiceStateLocked(ComponentName component, int userId) {
        ServiceState serviceState = (ServiceState) getOrCreateUserStateLocked(userId).serviceStateMap.get(component);
        if (serviceState != null) {
            return serviceState;
        }
        throw new IllegalStateException("Service state not found for " + component + " (userId=" + userId + ")");
    }

    private SessionState getSessionStateLocked(IBinder sessionToken, int callingUid, int userId) {
        SessionState sessionState = (SessionState) getOrCreateUserStateLocked(userId).sessionStateMap.get(sessionToken);
        if (sessionState == null) {
            throw new SessionNotFoundException("Session state not found for token " + sessionToken);
        } else if (callingUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || callingUid == sessionState.callingUid) {
            return sessionState;
        } else {
            throw new SecurityException("Illegal access to the session with token " + sessionToken + " from uid " + callingUid);
        }
    }

    private ITvInputSession getSessionLocked(IBinder sessionToken, int callingUid, int userId) {
        return getSessionLocked(getSessionStateLocked(sessionToken, callingUid, userId));
    }

    private ITvInputSession getSessionLocked(SessionState sessionState) {
        ITvInputSession session = sessionState.session;
        if (session != null) {
            return session;
        }
        throw new IllegalStateException("Session not yet created for token " + sessionState.sessionToken);
    }

    private int resolveCallingUserId(int callingPid, int callingUid, int requestedUserId, String methodName) {
        return ActivityManager.handleIncomingUser(callingPid, callingUid, requestedUserId, DEBUG, DEBUG, methodName, null);
    }

    private void updateServiceConnectionLocked(ComponentName component, int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        ServiceState serviceState = (ServiceState) userState.serviceStateMap.get(component);
        if (serviceState != null) {
            if (serviceState.reconnecting) {
                if (serviceState.sessionTokens.isEmpty()) {
                    serviceState.reconnecting = DEBUG;
                } else {
                    return;
                }
            }
            boolean -get5 = userId == this.mCurrentUserId ? serviceState.sessionTokens.isEmpty() ? serviceState.isHardware : true : serviceState.sessionTokens.isEmpty() ? DEBUG : true;
            if (serviceState.service == null && r2) {
                if (!serviceState.bound) {
                    serviceState.bound = this.mContext.bindServiceAsUser(new Intent("android.media.tv.TvInputService").setComponent(component), serviceState.connection, 33554433, new UserHandle(userId));
                }
            } else if (!(serviceState.service == null || r2)) {
                this.mContext.unbindService(serviceState.connection);
                userState.serviceStateMap.remove(component);
            }
        }
    }

    private void abortPendingCreateSessionRequestsLocked(ServiceState serviceState, String inputId, int userId) {
        UserState userState = getOrCreateUserStateLocked(userId);
        List<SessionState> sessionsToAbort = new ArrayList();
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

    private void createSessionInternalLocked(ITvInputService service, IBinder sessionToken, int userId) {
        SessionState sessionState = (SessionState) getOrCreateUserStateLocked(userId).sessionStateMap.get(sessionToken);
        InputChannel[] channels = InputChannel.openInputChannelPair(sessionToken.toString());
        ITvInputSessionCallback callback = new SessionCallback(sessionState, channels);
        try {
            if (sessionState.isRecordingSession) {
                service.createRecordingSession(callback, sessionState.inputId);
            } else {
                service.createSession(channels[1], callback, sessionState.inputId);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "error in createSession", e);
            removeSessionStateLocked(sessionToken, userId);
            sendSessionTokenToClientLocked(sessionState.client, sessionState.inputId, null, null, sessionState.seq);
        }
        channels[1].dispose();
    }

    private void sendSessionTokenToClientLocked(ITvInputClient client, String inputId, IBinder sessionToken, InputChannel channel, int seq) {
        try {
            client.onSessionCreated(inputId, sessionToken, channel, seq);
        } catch (RemoteException e) {
            Slog.e(TAG, "error in onSessionCreated", e);
        }
    }

    private void releaseSessionLocked(IBinder sessionToken, int callingUid, int userId) {
        try {
            SessionState sessionState = getSessionStateLocked(sessionToken, callingUid, userId);
            if (sessionState.session != null) {
                if (sessionToken == getOrCreateUserStateLocked(userId).mainSessionToken) {
                    setMainLocked(sessionToken, DEBUG, callingUid, userId);
                }
                sessionState.session.release();
            }
            if (sessionState != null) {
                sessionState.session = null;
            }
        } catch (Exception e) {
            Slog.e(TAG, "error in releaseSession", e);
            if (null != null) {
                null.session = null;
            }
        } catch (Throwable th) {
            if (null != null) {
                null.session = null;
            }
        }
        removeSessionStateLocked(sessionToken, userId);
    }

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

    private void setMainLocked(IBinder sessionToken, boolean isMain, int callingUid, int userId) {
        try {
            SessionState sessionState = getSessionStateLocked(sessionToken, callingUid, userId);
            if (sessionState.hardwareSessionToken != null) {
                sessionState = getSessionStateLocked(sessionState.hardwareSessionToken, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, userId);
            }
            if (getServiceStateLocked(sessionState.componentName, userId).isHardware) {
                getSessionLocked(sessionState).setMain(isMain);
            }
        } catch (Exception e) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
}
