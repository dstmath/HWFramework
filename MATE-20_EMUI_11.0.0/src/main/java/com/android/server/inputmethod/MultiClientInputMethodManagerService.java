package com.android.server.inputmethod;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.view.InputChannel;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.view.inputmethod.InputMethodSystemProperty;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.inputmethod.IMultiClientInputMethod;
import com.android.internal.inputmethod.IMultiClientInputMethodPrivilegedOperations;
import com.android.internal.inputmethod.IMultiClientInputMethodSession;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import com.android.internal.view.IInputMethodSession;
import com.android.internal.view.InputBindResult;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.wm.WindowManagerInternal;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

public final class MultiClientInputMethodManagerService {
    private static final boolean DEBUG = false;
    private static final int IME_CONNECTION_UNIFIED_BIND_FLAGS = 1140850693;
    private static final String PER_DISPLAY_FOCUS_DISABLED_WARNING_MSG = "Consider rebuilding the system image after enabling config_perDisplayFocusEnabled to make IME focus compatible with multi-client IME mode.";
    private static final String PER_DISPLAY_FOCUS_DISABLED_WARNING_TITLE = "config_perDisplayFocusEnabled is not true.";
    private static final long RECONNECT_DELAY_MSEC = 1000;
    private static final String TAG = "MultiClientInputMethodManagerService";
    private static final ComponentName sImeComponentName = InputMethodSystemProperty.sMultiClientImeComponentName;

    @Retention(RetentionPolicy.SOURCE)
    private @interface InputMethodClientState {
        public static final int ALREADY_SENT_BIND_RESULT = 4;
        public static final int READY_TO_SEND_FIRST_BIND_RESULT = 3;
        public static final int REGISTERED = 1;
        public static final int UNREGISTERED = 5;
        public static final int WAITING_FOR_IME_SESSION = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface PerUserState {
        public static final int SERVICE_CONNECTED = 5;
        public static final int SERVICE_NOT_QUERIED = 2;
        public static final int SERVICE_RECOGNIZED = 3;
        public static final int UNBIND_CALLED = 6;
        public static final int USER_LOCKED = 1;
        public static final int WAITING_SERVICE_CONNECTED = 4;
    }

    /* access modifiers changed from: private */
    public static void reportNotSupported() {
    }

    private MultiClientInputMethodManagerService() {
    }

    public static final class Lifecycle extends SystemService {
        private final ApiCallbacks mApiCallbacks;
        private final OnWorkerThreadCallback mOnWorkerThreadCallback;

        public Lifecycle(Context context) {
            super(context);
            final UserToInputMethodInfoMap userIdToInputMethodInfoMapper = new UserToInputMethodInfoMap();
            UserDataMap userDataMap = new UserDataMap();
            HandlerThread workerThread = new HandlerThread(MultiClientInputMethodManagerService.TAG);
            workerThread.start();
            this.mApiCallbacks = new ApiCallbacks(context, userDataMap, userIdToInputMethodInfoMapper);
            this.mOnWorkerThreadCallback = new OnWorkerThreadCallback(context, userDataMap, userIdToInputMethodInfoMapper, new Handler(workerThread.getLooper(), $$Lambda$MultiClientInputMethodManagerService$Lifecycle$brDWlq0PKf6TnYydoJqmbKEO15A.INSTANCE, true));
            LocalServices.addService(InputMethodManagerInternal.class, new InputMethodManagerInternal() {
                /* class com.android.server.inputmethod.MultiClientInputMethodManagerService.Lifecycle.AnonymousClass1 */

                @Override // com.android.server.inputmethod.InputMethodManagerInternal
                public void setInteractive(boolean interactive) {
                    MultiClientInputMethodManagerService.reportNotSupported();
                }

                @Override // com.android.server.inputmethod.InputMethodManagerInternal
                public void hideCurrentInputMethod() {
                    MultiClientInputMethodManagerService.reportNotSupported();
                }

                @Override // com.android.server.inputmethod.InputMethodManagerInternal
                public List<InputMethodInfo> getInputMethodListAsUser(int userId) {
                    return userIdToInputMethodInfoMapper.getAsList(userId);
                }

                @Override // com.android.server.inputmethod.InputMethodManagerInternal
                public List<InputMethodInfo> getEnabledInputMethodListAsUser(int userId) {
                    return userIdToInputMethodInfoMapper.getAsList(userId);
                }
            });
        }

        static /* synthetic */ boolean lambda$new$0(Message msg) {
            return false;
        }

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            this.mOnWorkerThreadCallback.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$Jz_nOUKMszfA8aOqhALo1zh2yrY.INSTANCE, this.mOnWorkerThreadCallback, Integer.valueOf(phase)));
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.inputmethod.MultiClientInputMethodManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.inputmethod.MultiClientInputMethodManagerService$ApiCallbacks, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService("input_method", this.mApiCallbacks);
        }

        @Override // com.android.server.SystemService
        public void onStartUser(int userId) {
            this.mOnWorkerThreadCallback.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$WSIWOuAiWdU2M_nAMbBdbAY3JqQ.INSTANCE, this.mOnWorkerThreadCallback, Integer.valueOf(userId)));
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userId) {
            this.mOnWorkerThreadCallback.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$DAfcEi3wsfG3_Hj9sOIZhg6DC1c.INSTANCE, this.mOnWorkerThreadCallback, Integer.valueOf(userId)));
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userId) {
            this.mOnWorkerThreadCallback.getHandler().sendMessage(PooledLambda.obtainMessage($$Lambda$RXzNxmh28D9UoOHbnsbdOPsUo9Y.INSTANCE, this.mOnWorkerThreadCallback, Integer.valueOf(userId)));
        }
    }

    /* access modifiers changed from: private */
    public static final class OnWorkerThreadCallback {
        private final Context mContext;
        private final Handler mHandler;
        private final UserToInputMethodInfoMap mInputMethodInfoMap;
        private final UserDataMap mUserDataMap;

        OnWorkerThreadCallback(Context context, UserDataMap userDataMap, UserToInputMethodInfoMap inputMethodInfoMap, Handler handler) {
            this.mContext = context;
            this.mUserDataMap = userDataMap;
            this.mInputMethodInfoMap = inputMethodInfoMap;
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public Handler getHandler() {
            return this.mHandler;
        }

        /* access modifiers changed from: private */
        public void tryBindInputMethodService(int userId) {
            PerUserData data = this.mUserDataMap.get(userId);
            if (data == null) {
                Slog.i(MultiClientInputMethodManagerService.TAG, "tryBindInputMethodService is called for an unknown user=" + userId);
                return;
            }
            InputMethodInfo imi = MultiClientInputMethodManagerService.queryInputMethod(this.mContext, userId, MultiClientInputMethodManagerService.sImeComponentName);
            if (imi == null) {
                Slog.w(MultiClientInputMethodManagerService.TAG, "Multi-client InputMethod is not found. component=" + MultiClientInputMethodManagerService.sImeComponentName);
                synchronized (data.mLock) {
                    int i = data.mState;
                    if (i == 1 || i == 2 || i == 3 || i == 6) {
                        this.mInputMethodInfoMap.remove(userId);
                    }
                }
                return;
            }
            synchronized (data.mLock) {
                switch (data.mState) {
                    case 1:
                        return;
                    case 2:
                    case 3:
                    case 6:
                        data.mState = 3;
                        data.mCurrentInputMethodInfo = imi;
                        this.mInputMethodInfoMap.put(userId, imi);
                        if (!data.bindServiceLocked(this.mContext, userId)) {
                            Slog.e(MultiClientInputMethodManagerService.TAG, "Failed to bind Multi-client InputMethod.");
                            return;
                        } else {
                            data.mState = 4;
                            return;
                        }
                    case 4:
                    case 5:
                        return;
                    default:
                        Slog.wtf(MultiClientInputMethodManagerService.TAG, "Unknown state=" + data.mState);
                        return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onStartUser(int userId) {
            this.mUserDataMap.put(userId, new PerUserData(userId, null, 1, this));
        }

        /* access modifiers changed from: package-private */
        public void onUnlockUser(int userId) {
            PerUserData data = this.mUserDataMap.get(userId);
            if (data == null) {
                Slog.i(MultiClientInputMethodManagerService.TAG, "onUnlockUser is called for an unknown user=" + userId);
                return;
            }
            synchronized (data.mLock) {
                if (data.mState != 1) {
                    Slog.wtf(MultiClientInputMethodManagerService.TAG, "Unknown state=" + data.mState);
                } else {
                    data.mState = 2;
                    tryBindInputMethodService(userId);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onStopUser(int userId) {
            this.mInputMethodInfoMap.remove(userId);
            PerUserData data = this.mUserDataMap.removeReturnOld(userId);
            if (data == null) {
                Slog.i(MultiClientInputMethodManagerService.TAG, "onStopUser is called for an unknown user=" + userId);
                return;
            }
            synchronized (data.mLock) {
                int i = data.mState;
                if (!(i == 1 || i == 3)) {
                    if (!(i == 4 || i == 5)) {
                        if (i != 6) {
                            Slog.wtf(MultiClientInputMethodManagerService.TAG, "Unknown state=" + data.mState);
                        }
                    }
                    data.unbindServiceLocked(this.mContext);
                    data.mState = 6;
                    data.mCurrentInputMethod = null;
                    data.onInputMethodDisconnectedLocked();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onServiceConnected(PerUserData data, IMultiClientInputMethod service) {
            synchronized (data.mLock) {
                int i = data.mState;
                if (i == 4) {
                    data.mState = 5;
                    data.mCurrentInputMethod = service;
                    try {
                        data.mCurrentInputMethod.initialize(new ImeCallbacks(data));
                    } catch (RemoteException e) {
                    }
                    data.onInputMethodConnectedLocked();
                } else if (i != 6) {
                    Slog.wtf(MultiClientInputMethodManagerService.TAG, "Unknown state=" + data.mState);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onServiceDisconnected(PerUserData data) {
            WindowManagerInternal windowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            synchronized (data.mLock) {
                int numTokens = data.mDisplayIdToImeWindowTokenMap.size();
                for (int i = 0; i < numTokens; i++) {
                    TokenInfo info = data.mDisplayIdToImeWindowTokenMap.valueAt(i);
                    windowManagerInternal.removeWindowToken(info.mToken, false, info.mDisplayId);
                }
                data.mDisplayIdToImeWindowTokenMap.clear();
                int i2 = data.mState;
                if (i2 == 4 || i2 == 5) {
                    data.mState = 4;
                    data.mCurrentInputMethod = null;
                    data.onInputMethodDisconnectedLocked();
                } else if (i2 != 6) {
                    Slog.wtf(MultiClientInputMethodManagerService.TAG, "Unknown state=" + data.mState);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onBindingDied(PerUserData data) {
            WindowManagerInternal windowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            synchronized (data.mLock) {
                int numTokens = data.mDisplayIdToImeWindowTokenMap.size();
                for (int i = 0; i < numTokens; i++) {
                    TokenInfo info = data.mDisplayIdToImeWindowTokenMap.valueAt(i);
                    windowManagerInternal.removeWindowToken(info.mToken, false, info.mDisplayId);
                }
                data.mDisplayIdToImeWindowTokenMap.clear();
                int i2 = data.mState;
                if (i2 == 4 || i2 == 5) {
                    data.mState = 6;
                    data.mCurrentInputMethod = null;
                    data.onInputMethodDisconnectedLocked();
                    this.mHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$MultiClientInputMethodManagerService$OnWorkerThreadCallback$RXozRqnqnYcbtt5pr216uYhnGCc.INSTANCE, this, Integer.valueOf(data.mUserId)), 1000);
                } else if (i2 != 6) {
                    Slog.wtf(MultiClientInputMethodManagerService.TAG, "Unknown state=" + data.mState);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void onBootPhase(int phase) {
            if (phase == 550) {
                IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
                filter.addDataScheme("package");
                this.mContext.registerReceiver(new BroadcastReceiver() {
                    /* class com.android.server.inputmethod.MultiClientInputMethodManagerService.OnWorkerThreadCallback.AnonymousClass1 */

                    @Override // android.content.BroadcastReceiver
                    public void onReceive(Context context, Intent intent) {
                        OnWorkerThreadCallback.this.onPackageAdded(intent);
                    }
                }, filter, null, this.mHandler);
            } else if (phase == 1000 && !this.mContext.getResources().getBoolean(17891332)) {
                Bundle extras = new Bundle();
                extras.putBoolean("android.allowDuringSetup", true);
                ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).notifyAsUser(MultiClientInputMethodManagerService.TAG, 8, new Notification.Builder(this.mContext, SystemNotificationChannels.VIRTUAL_KEYBOARD).setContentTitle(MultiClientInputMethodManagerService.PER_DISPLAY_FOCUS_DISABLED_WARNING_TITLE).setStyle(new Notification.BigTextStyle().bigText(MultiClientInputMethodManagerService.PER_DISPLAY_FOCUS_DISABLED_WARNING_MSG)).setSmallIcon(17302750).setWhen(0).setOngoing(true).setLocalOnly(true).addExtras(extras).setCategory("sys").setColor(this.mContext.getColor(17170460)).build(), UserHandle.ALL);
            }
        }

        /* access modifiers changed from: package-private */
        public void onPackageAdded(Intent intent) {
            Uri uri = intent.getData();
            if (uri != null && intent.hasExtra("android.intent.extra.UID")) {
                String packageName = uri.getSchemeSpecificPart();
                if (MultiClientInputMethodManagerService.sImeComponentName != null && packageName != null && TextUtils.equals(MultiClientInputMethodManagerService.sImeComponentName.getPackageName(), packageName)) {
                    tryBindInputMethodService(UserHandle.getUserId(intent.getIntExtra("android.intent.extra.UID", 0)));
                }
            }
        }
    }

    private static final class WindowInfo {
        final int mWindowHandle;
        final IBinder mWindowToken;

        WindowInfo(IBinder windowToken, int windowCookie) {
            this.mWindowToken = windowToken;
            this.mWindowHandle = windowCookie;
        }
    }

    private static final class InputMethodClientIdSource {
        @GuardedBy({"InputMethodClientIdSource.class"})
        private static int sNextValue = 0;

        private InputMethodClientIdSource() {
        }

        static synchronized int getNext() {
            int result;
            synchronized (InputMethodClientIdSource.class) {
                result = sNextValue;
                sNextValue++;
                if (sNextValue < 0) {
                    sNextValue = 0;
                }
            }
            return result;
        }
    }

    private static final class WindowHandleSource {
        @GuardedBy({"WindowHandleSource.class"})
        private static int sNextValue = 0;

        private WindowHandleSource() {
        }

        static synchronized int getNext() {
            int result;
            synchronized (WindowHandleSource.class) {
                result = sNextValue;
                sNextValue++;
                if (sNextValue < 0) {
                    sNextValue = 0;
                }
            }
            return result;
        }
    }

    /* access modifiers changed from: private */
    public static final class InputMethodClientInfo {
        @GuardedBy({"PerUserData.mLock"})
        int mBindingSequence;
        final IInputMethodClient mClient;
        final int mClientId;
        @GuardedBy({"PerUserData.mLock"})
        IInputMethodSession mInputMethodSession;
        @GuardedBy({"PerUserData.mLock"})
        IMultiClientInputMethodSession mMSInputMethodSession;
        final int mPid;
        final int mSelfReportedDisplayId;
        @GuardedBy({"PerUserData.mLock"})
        int mState;
        final int mUid;
        @GuardedBy({"PerUserData.mLock"})
        final WeakHashMap<IBinder, WindowInfo> mWindowMap = new WeakHashMap<>();
        @GuardedBy({"PerUserData.mLock"})
        InputChannel mWriteChannel;

        InputMethodClientInfo(IInputMethodClient client, int uid, int pid, int selfReportedDisplayId) {
            this.mClient = client;
            this.mUid = uid;
            this.mPid = pid;
            this.mSelfReportedDisplayId = selfReportedDisplayId;
            this.mClientId = InputMethodClientIdSource.getNext();
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"PerUserData.mLock"})
        public void dumpLocked(FileDescriptor fd, IndentingPrintWriter ipw, String[] args) {
            ipw.println("mState=" + this.mState + ",mBindingSequence=" + this.mBindingSequence + ",mWriteChannel=" + this.mWriteChannel + ",mInputMethodSession=" + this.mInputMethodSession + ",mMSInputMethodSession=" + this.mMSInputMethodSession);
        }
    }

    /* access modifiers changed from: private */
    public static final class UserDataMap {
        @GuardedBy({"mMap"})
        private final SparseArray<PerUserData> mMap;

        private UserDataMap() {
            this.mMap = new SparseArray<>();
        }

        /* access modifiers changed from: package-private */
        public PerUserData get(int userId) {
            PerUserData perUserData;
            synchronized (this.mMap) {
                perUserData = this.mMap.get(userId);
            }
            return perUserData;
        }

        /* access modifiers changed from: package-private */
        public void put(int userId, PerUserData data) {
            synchronized (this.mMap) {
                this.mMap.put(userId, data);
            }
        }

        /* access modifiers changed from: package-private */
        public PerUserData removeReturnOld(int userId) {
            PerUserData perUserData;
            synchronized (this.mMap) {
                perUserData = (PerUserData) this.mMap.removeReturnOld(userId);
            }
            return perUserData;
        }

        /* access modifiers changed from: package-private */
        public void dump(FileDescriptor fd, IndentingPrintWriter ipw, String[] args) {
            synchronized (this.mMap) {
                for (int i = 0; i < this.mMap.size(); i++) {
                    int userId = this.mMap.keyAt(i);
                    PerUserData data = this.mMap.valueAt(i);
                    ipw.println("userId=" + userId + ", data=");
                    if (data != null) {
                        ipw.increaseIndent();
                        data.dump(fd, ipw, args);
                        ipw.decreaseIndent();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class TokenInfo {
        final int mDisplayId;
        final Binder mToken;

        TokenInfo(Binder token, int displayId) {
            this.mToken = token;
            this.mDisplayId = displayId;
        }
    }

    /* access modifiers changed from: private */
    public static final class PerUserData {
        @GuardedBy({"mLock"})
        private SparseArray<InputMethodClientInfo> mClientIdToClientMap = new SparseArray<>();
        @GuardedBy({"mLock"})
        private final ArrayMap<IBinder, InputMethodClientInfo> mClientMap = new ArrayMap<>();
        @GuardedBy({"mLock"})
        IMultiClientInputMethod mCurrentInputMethod;
        @GuardedBy({"mLock"})
        InputMethodInfo mCurrentInputMethodInfo;
        @GuardedBy({"mLock"})
        final ArraySet<TokenInfo> mDisplayIdToImeWindowTokenMap = new ArraySet<>();
        final Object mLock = new Object();
        private final OnWorkerThreadServiceConnection mOnWorkerThreadServiceConnection;
        @GuardedBy({"mLock"})
        int mState;
        private final int mUserId;

        /* access modifiers changed from: private */
        public static final class OnWorkerThreadServiceConnection implements ServiceConnection {
            private final OnWorkerThreadCallback mCallback;
            private final PerUserData mData;

            OnWorkerThreadServiceConnection(PerUserData data, OnWorkerThreadCallback callback) {
                this.mData = data;
                this.mCallback = callback;
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                this.mCallback.onServiceConnected(this.mData, IMultiClientInputMethod.Stub.asInterface(service));
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                this.mCallback.onServiceDisconnected(this.mData);
            }

            @Override // android.content.ServiceConnection
            public void onBindingDied(ComponentName name) {
                this.mCallback.onBindingDied(this.mData);
            }

            /* access modifiers changed from: package-private */
            public Handler getHandler() {
                return this.mCallback.getHandler();
            }
        }

        PerUserData(int userId, InputMethodInfo inputMethodInfo, int initialState, OnWorkerThreadCallback callback) {
            this.mUserId = userId;
            this.mCurrentInputMethodInfo = inputMethodInfo;
            this.mState = initialState;
            this.mOnWorkerThreadServiceConnection = new OnWorkerThreadServiceConnection(this, callback);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean bindServiceLocked(Context context, int userId) {
            Intent intent = new Intent("android.inputmethodservice.MultiClientInputMethodService").setComponent(this.mCurrentInputMethodInfo.getComponent()).putExtra("android.intent.extra.client_label", 17040297).putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(context, 0, new Intent("android.settings.INPUT_METHOD_SETTINGS"), 0));
            OnWorkerThreadServiceConnection onWorkerThreadServiceConnection = this.mOnWorkerThreadServiceConnection;
            return context.bindServiceAsUser(intent, onWorkerThreadServiceConnection, MultiClientInputMethodManagerService.IME_CONNECTION_UNIFIED_BIND_FLAGS, onWorkerThreadServiceConnection.getHandler(), UserHandle.of(userId));
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void unbindServiceLocked(Context context) {
            context.unbindService(this.mOnWorkerThreadServiceConnection);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public InputMethodClientInfo getClientLocked(IInputMethodClient client) {
            return this.mClientMap.get(client.asBinder());
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public InputMethodClientInfo getClientFromIdLocked(int clientId) {
            return this.mClientIdToClientMap.get(clientId);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public InputMethodClientInfo removeClientLocked(IInputMethodClient client) {
            InputMethodClientInfo info = this.mClientMap.remove(client.asBinder());
            if (info != null) {
                this.mClientIdToClientMap.remove(info.mClientId);
            }
            return info;
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void addClientLocked(int uid, int pid, IInputMethodClient client, int selfReportedDisplayId) {
            if (getClientLocked(client) != null) {
                Slog.wtf(MultiClientInputMethodManagerService.TAG, "The same client is added multiple times");
                return;
            }
            try {
                client.asBinder().linkToDeath(new ClientDeathRecipient(this, client), 0);
                InputMethodClientInfo clientInfo = new InputMethodClientInfo(client, uid, pid, selfReportedDisplayId);
                clientInfo.mState = 1;
                this.mClientMap.put(client.asBinder(), clientInfo);
                this.mClientIdToClientMap.put(clientInfo.mClientId, clientInfo);
                if (this.mState == 5) {
                    try {
                        this.mCurrentInputMethod.addClient(clientInfo.mClientId, clientInfo.mPid, clientInfo.mUid, clientInfo.mSelfReportedDisplayId);
                        clientInfo.mState = 2;
                    } catch (RemoteException e) {
                    }
                }
            } catch (RemoteException e2) {
                throw new IllegalStateException(e2);
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void onInputMethodConnectedLocked() {
            int numClients = this.mClientMap.size();
            for (int i = 0; i < numClients; i++) {
                InputMethodClientInfo clientInfo = this.mClientMap.valueAt(i);
                if (clientInfo.mState != 1) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "Unexpected state=" + clientInfo.mState);
                    return;
                }
                try {
                    this.mCurrentInputMethod.addClient(clientInfo.mClientId, clientInfo.mUid, clientInfo.mPid, clientInfo.mSelfReportedDisplayId);
                    clientInfo.mState = 2;
                } catch (RemoteException e) {
                }
            }
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public void onInputMethodDisconnectedLocked() {
            int numClients = this.mClientMap.size();
            for (int i = 0; i < numClients; i++) {
                InputMethodClientInfo clientInfo = this.mClientMap.valueAt(i);
                int i2 = clientInfo.mState;
                if (i2 != 1) {
                    if (i2 == 2) {
                        clientInfo.mState = 1;
                    } else if (i2 == 3) {
                        clientInfo.mState = 1;
                        clientInfo.mInputMethodSession = null;
                        clientInfo.mMSInputMethodSession = null;
                        if (clientInfo.mWriteChannel != null) {
                            clientInfo.mWriteChannel.dispose();
                            clientInfo.mWriteChannel = null;
                        }
                    } else if (i2 == 4) {
                        try {
                            clientInfo.mClient.onUnbindMethod(clientInfo.mBindingSequence, 3);
                        } catch (RemoteException e) {
                        }
                        clientInfo.mState = 1;
                        clientInfo.mInputMethodSession = null;
                        clientInfo.mMSInputMethodSession = null;
                        if (clientInfo.mWriteChannel != null) {
                            clientInfo.mWriteChannel.dispose();
                            clientInfo.mWriteChannel = null;
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(FileDescriptor fd, IndentingPrintWriter ipw, String[] args) {
            synchronized (this.mLock) {
                ipw.println("mState=" + this.mState + ",mCurrentInputMethod=" + this.mCurrentInputMethod + ",mCurrentInputMethodInfo=" + this.mCurrentInputMethodInfo);
                if (this.mCurrentInputMethod != null) {
                    ipw.println(">>Dump CurrentInputMethod>>");
                    ipw.flush();
                    try {
                        TransferPipe.dumpAsync(this.mCurrentInputMethod.asBinder(), fd, args);
                    } catch (RemoteException | IOException e) {
                        ipw.println("Failed to dump input method service: " + e);
                    }
                    ipw.println("<<Dump CurrentInputMethod<<");
                }
                ipw.println("mDisplayIdToImeWindowTokenMap=");
                Iterator<TokenInfo> it = this.mDisplayIdToImeWindowTokenMap.iterator();
                while (it.hasNext()) {
                    TokenInfo info = it.next();
                    ipw.println(" display=" + info.mDisplayId + ",token=" + info.mToken);
                }
                ipw.println("mClientMap=");
                ipw.increaseIndent();
                for (int i = 0; i < this.mClientMap.size(); i++) {
                    ipw.println("binder=" + this.mClientMap.keyAt(i));
                    ipw.println(" InputMethodClientInfo=");
                    InputMethodClientInfo info2 = this.mClientMap.valueAt(i);
                    if (info2 != null) {
                        ipw.increaseIndent();
                        info2.dumpLocked(fd, ipw, args);
                        ipw.decreaseIndent();
                    }
                }
                ipw.decreaseIndent();
                ipw.println("mClientIdToClientMap=");
                ipw.increaseIndent();
                for (int i2 = 0; i2 < this.mClientIdToClientMap.size(); i2++) {
                    ipw.println("clientId=" + this.mClientIdToClientMap.keyAt(i2));
                    ipw.println(" InputMethodClientInfo=");
                    InputMethodClientInfo info3 = this.mClientIdToClientMap.valueAt(i2);
                    if (info3 != null) {
                        ipw.increaseIndent();
                        info3.dumpLocked(fd, ipw, args);
                        ipw.decreaseIndent();
                    }
                    if (info3.mClient != null) {
                        ipw.println(">>DumpClientStart>>");
                        ipw.flush();
                        try {
                            TransferPipe.dumpAsync(info3.mClient.asBinder(), fd, args);
                        } catch (RemoteException | IOException e2) {
                            ipw.println(" Failed to dump client:" + e2);
                        }
                        ipw.println("<<DumpClientEnd<<");
                    }
                }
                ipw.decreaseIndent();
            }
        }

        /* access modifiers changed from: private */
        public static final class ClientDeathRecipient implements IBinder.DeathRecipient {
            private final IInputMethodClient mClient;
            private final PerUserData mPerUserData;

            ClientDeathRecipient(PerUserData perUserData, IInputMethodClient client) {
                this.mPerUserData = perUserData;
                this.mClient = client;
            }

            @Override // android.os.IBinder.DeathRecipient
            public void binderDied() {
                synchronized (this.mPerUserData.mLock) {
                    this.mClient.asBinder().unlinkToDeath(this, 0);
                    InputMethodClientInfo clientInfo = this.mPerUserData.removeClientLocked(this.mClient);
                    if (clientInfo != null) {
                        if (clientInfo.mWriteChannel != null) {
                            clientInfo.mWriteChannel.dispose();
                            clientInfo.mWriteChannel = null;
                        }
                        if (clientInfo.mInputMethodSession != null) {
                            try {
                                clientInfo.mInputMethodSession.finishSession();
                            } catch (RemoteException e) {
                            }
                            clientInfo.mInputMethodSession = null;
                        }
                        clientInfo.mMSInputMethodSession = null;
                        clientInfo.mState = 5;
                        if (this.mPerUserData.mState == 5) {
                            try {
                                this.mPerUserData.mCurrentInputMethod.removeClient(clientInfo.mClientId);
                            } catch (RemoteException e2) {
                            }
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static InputMethodInfo queryInputMethod(Context context, int userId, ComponentName componentName) {
        if (componentName == null) {
            return null;
        }
        List<ResolveInfo> services = context.getPackageManager().queryIntentServicesAsUser(new Intent("android.inputmethodservice.MultiClientInputMethodService").setComponent(componentName), 128, userId);
        if (services.isEmpty()) {
            Slog.e(TAG, "No IME found");
            return null;
        } else if (services.size() > 1) {
            Slog.e(TAG, "Only one IME service is supported.");
            return null;
        } else {
            ResolveInfo ri = services.get(0);
            ServiceInfo si = ri.serviceInfo;
            String imeId = InputMethodInfo.computeId(ri);
            if (!"android.permission.BIND_INPUT_METHOD".equals(si.permission)) {
                Slog.e(TAG, imeId + " must have requiredandroid.permission.BIND_INPUT_METHOD");
                return null;
            } else if (Build.IS_DEBUGGABLE || (1 & si.applicationInfo.flags) != 0) {
                try {
                    return new InputMethodInfo(context, ri);
                } catch (Exception e) {
                    Slog.wtf(TAG, "Unable to load input method " + imeId, e);
                    return null;
                }
            } else {
                Slog.e(TAG, imeId + " must be pre-installed when Build.IS_DEBUGGABLE is false");
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class UserToInputMethodInfoMap {
        @GuardedBy({"mArray"})
        private final SparseArray<InputMethodInfo> mArray;

        private UserToInputMethodInfoMap() {
            this.mArray = new SparseArray<>();
        }

        /* access modifiers changed from: package-private */
        public void put(int userId, InputMethodInfo imi) {
            synchronized (this.mArray) {
                this.mArray.put(userId, imi);
            }
        }

        /* access modifiers changed from: package-private */
        public void remove(int userId) {
            synchronized (this.mArray) {
                this.mArray.remove(userId);
            }
        }

        /* access modifiers changed from: package-private */
        public InputMethodInfo get(int userId) {
            InputMethodInfo inputMethodInfo;
            synchronized (this.mArray) {
                inputMethodInfo = this.mArray.get(userId);
            }
            return inputMethodInfo;
        }

        /* access modifiers changed from: package-private */
        public List<InputMethodInfo> getAsList(int userId) {
            InputMethodInfo info = get(userId);
            if (info == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(info);
        }

        /* access modifiers changed from: package-private */
        public void dump(FileDescriptor fd, IndentingPrintWriter ipw, String[] args) {
            synchronized (this.mArray) {
                for (int i = 0; i < this.mArray.size(); i++) {
                    ipw.println("userId=" + this.mArray.keyAt(i));
                    ipw.println(" InputMethodInfo=" + this.mArray.valueAt(i));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class ImeCallbacks extends IMultiClientInputMethodPrivilegedOperations.Stub {
        private final WindowManagerInternal mIWindowManagerInternal = ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class));
        private final PerUserData mPerUserData;

        ImeCallbacks(PerUserData perUserData) {
            this.mPerUserData = perUserData;
        }

        public IBinder createInputMethodWindowToken(int displayId) {
            synchronized (this.mPerUserData.mLock) {
                int numTokens = this.mPerUserData.mDisplayIdToImeWindowTokenMap.size();
                for (int i = 0; i < numTokens; i++) {
                    TokenInfo tokenInfo = this.mPerUserData.mDisplayIdToImeWindowTokenMap.valueAt(i);
                    if (tokenInfo.mDisplayId == displayId) {
                        return tokenInfo.mToken;
                    }
                }
                Binder token = new Binder();
                Binder.withCleanCallingIdentity(PooledLambda.obtainRunnable($$Lambda$AmbbXLEJhTNO0thyboUFa1hBy_8.INSTANCE, this.mIWindowManagerInternal, token, 2011, Integer.valueOf(displayId)));
                this.mPerUserData.mDisplayIdToImeWindowTokenMap.add(new TokenInfo(token, displayId));
                return token;
            }
        }

        public void deleteInputMethodWindowToken(IBinder token) {
            synchronized (this.mPerUserData.mLock) {
                int numTokens = this.mPerUserData.mDisplayIdToImeWindowTokenMap.size();
                int i = 0;
                while (true) {
                    if (i >= numTokens) {
                        break;
                    }
                    TokenInfo tokenInfo = this.mPerUserData.mDisplayIdToImeWindowTokenMap.valueAt(i);
                    if (tokenInfo.mToken == token) {
                        this.mPerUserData.mDisplayIdToImeWindowTokenMap.remove(tokenInfo);
                        break;
                    }
                    i++;
                }
            }
        }

        public void acceptClient(int clientId, IInputMethodSession inputMethodSession, IMultiClientInputMethodSession multiSessionInputMethodSession, InputChannel writeChannel) {
            synchronized (this.mPerUserData.mLock) {
                InputMethodClientInfo clientInfo = this.mPerUserData.getClientFromIdLocked(clientId);
                if (clientInfo == null) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "Unknown clientId=" + clientId);
                    return;
                }
                if (clientInfo.mState != 2) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "Unexpected state=" + clientInfo.mState);
                } else {
                    try {
                        clientInfo.mClient.setActive(true, false);
                        clientInfo.mState = 3;
                        clientInfo.mWriteChannel = writeChannel;
                        clientInfo.mInputMethodSession = inputMethodSession;
                        clientInfo.mMSInputMethodSession = multiSessionInputMethodSession;
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void reportImeWindowTarget(int clientId, int targetWindowHandle, IBinder imeWindowToken) {
            synchronized (this.mPerUserData.mLock) {
                InputMethodClientInfo clientInfo = this.mPerUserData.getClientFromIdLocked(clientId);
                if (clientInfo == null) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "Unknown clientId=" + clientId);
                    return;
                }
                for (WindowInfo windowInfo : clientInfo.mWindowMap.values()) {
                    if (windowInfo.mWindowHandle == targetWindowHandle) {
                        IBinder iBinder = windowInfo.mWindowToken;
                    }
                }
            }
        }

        public boolean isUidAllowedOnDisplay(int displayId, int uid) {
            return this.mIWindowManagerInternal.isUidAllowedOnDisplay(displayId, uid);
        }

        public void setActive(int clientId, boolean active) {
            synchronized (this.mPerUserData.mLock) {
                InputMethodClientInfo clientInfo = this.mPerUserData.getClientFromIdLocked(clientId);
                if (clientInfo == null) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "Unknown clientId=" + clientId);
                    return;
                }
                try {
                    clientInfo.mClient.setActive(active, false);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private static final class ApiCallbacks extends IInputMethodManager.Stub {
        private final AppOpsManager mAppOpsManager;
        private final Context mContext;
        private final UserToInputMethodInfoMap mInputMethodInfoMap;
        private final UserDataMap mUserDataMap;
        private final WindowManagerInternal mWindowManagerInternal = ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class));

        ApiCallbacks(Context context, UserDataMap userDataMap, UserToInputMethodInfoMap inputMethodInfoMap) {
            this.mContext = context;
            this.mUserDataMap = userDataMap;
            this.mInputMethodInfoMap = inputMethodInfoMap;
            this.mAppOpsManager = (AppOpsManager) context.getSystemService(AppOpsManager.class);
        }

        private boolean checkFocus(int uid, int pid, int displayId) {
            return this.mWindowManagerInternal.isInputMethodClientFocus(uid, pid, displayId);
        }

        public void addClient(IInputMethodClient client, IInputContext inputContext, int selfReportedDisplayId) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int userId = UserHandle.getUserId(callingUid);
            PerUserData data = this.mUserDataMap.get(userId);
            if (data == null) {
                Slog.e(MultiClientInputMethodManagerService.TAG, "addClient() from unknown userId=" + userId + " uid=" + callingUid + " pid=" + callingPid);
                return;
            }
            synchronized (data.mLock) {
                data.addClientLocked(callingUid, callingPid, client, selfReportedDisplayId);
            }
        }

        public List<InputMethodInfo> getInputMethodList(int userId) {
            if (UserHandle.getCallingUserId() != userId) {
                this.mContext.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", null);
            }
            return this.mInputMethodInfoMap.getAsList(userId);
        }

        public List<InputMethodInfo> getEnabledInputMethodList(int userId) {
            if (UserHandle.getCallingUserId() != userId) {
                this.mContext.enforceCallingPermission("android.permission.INTERACT_ACROSS_USERS_FULL", null);
            }
            return this.mInputMethodInfoMap.getAsList(userId);
        }

        public List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId, boolean allowsImplicitlySelectedSubtypes) {
            MultiClientInputMethodManagerService.reportNotSupported();
            return Collections.emptyList();
        }

        public InputMethodSubtype getLastInputMethodSubtype() {
            MultiClientInputMethodManagerService.reportNotSupported();
            return null;
        }

        public boolean showSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int userId = UserHandle.getUserId(callingUid);
            PerUserData data = this.mUserDataMap.get(userId);
            if (data == null) {
                Slog.e(MultiClientInputMethodManagerService.TAG, "showSoftInput() from unknown userId=" + userId + " uid=" + callingUid + " pid=" + callingPid);
                return false;
            }
            synchronized (data.mLock) {
                InputMethodClientInfo clientInfo = data.getClientLocked(client);
                if (clientInfo == null) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "showSoftInput. client not found. ignoring.");
                    return false;
                } else if (clientInfo.mUid != callingUid) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "Expected calling UID=" + clientInfo.mUid + " actual=" + callingUid);
                    return false;
                } else {
                    int i = clientInfo.mState;
                    if (i == 3 || i == 4) {
                        try {
                            clientInfo.mMSInputMethodSession.showSoftInput(flags, resultReceiver);
                        } catch (RemoteException e) {
                        }
                    }
                    return true;
                }
            }
        }

        public boolean hideSoftInput(IInputMethodClient client, int flags, ResultReceiver resultReceiver) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int userId = UserHandle.getUserId(callingUid);
            PerUserData data = this.mUserDataMap.get(userId);
            if (data == null) {
                Slog.e(MultiClientInputMethodManagerService.TAG, "hideSoftInput() from unknown userId=" + userId + " uid=" + callingUid + " pid=" + callingPid);
                return false;
            }
            synchronized (data.mLock) {
                InputMethodClientInfo clientInfo = data.getClientLocked(client);
                if (clientInfo == null) {
                    return false;
                }
                if (clientInfo.mUid != callingUid) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "Expected calling UID=" + clientInfo.mUid + " actual=" + callingUid);
                    return false;
                }
                int i = clientInfo.mState;
                if (i == 3 || i == 4) {
                    try {
                        clientInfo.mMSInputMethodSession.hideSoftInput(flags, resultReceiver);
                    } catch (RemoteException e) {
                    }
                }
                return true;
            }
        }

        public InputBindResult startInputOrWindowGainedFocus(int startInputReason, IInputMethodClient client, IBinder windowToken, int startInputFlags, int softInputMode, int windowFlags, EditorInfo editorInfo, IInputContext inputContext, int missingMethods, int unverifiedTargetSdkVersion) {
            Object obj;
            Throwable th;
            WindowInfo windowInfo;
            int i;
            Object obj2;
            int windowHandle;
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            int userId = UserHandle.getUserId(callingUid);
            if (client == null) {
                return InputBindResult.INVALID_CLIENT;
            }
            boolean packageNameVerified = editorInfo != null && InputMethodUtils.checkIfPackageBelongsToUid(this.mAppOpsManager, callingUid, editorInfo.packageName);
            if (editorInfo == null || packageNameVerified) {
                PerUserData data = this.mUserDataMap.get(userId);
                if (data == null) {
                    Slog.e(MultiClientInputMethodManagerService.TAG, "startInputOrWindowGainedFocus() from unknown userId=" + userId + " uid=" + callingUid + " pid=" + callingPid);
                    return InputBindResult.INVALID_USER;
                }
                Object obj3 = data.mLock;
                synchronized (obj3) {
                    try {
                        InputMethodClientInfo clientInfo = data.getClientLocked(client);
                        if (clientInfo == null) {
                            try {
                                return InputBindResult.INVALID_CLIENT;
                            } catch (Throwable th2) {
                                th = th2;
                                obj = obj3;
                                throw th;
                            }
                        } else if (clientInfo.mUid != callingUid) {
                            Slog.e(MultiClientInputMethodManagerService.TAG, "Expected calling UID=" + clientInfo.mUid + " actual=" + callingUid);
                            return InputBindResult.INVALID_CLIENT;
                        } else {
                            switch (data.mState) {
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 6:
                                    return InputBindResult.IME_NOT_CONNECTED;
                                case 5:
                                    if (windowToken != null) {
                                        WindowInfo windowInfo2 = clientInfo.mWindowMap.get(windowToken);
                                        if (windowInfo2 == null) {
                                            WindowInfo windowInfo3 = new WindowInfo(windowToken, WindowHandleSource.getNext());
                                            clientInfo.mWindowMap.put(windowToken, windowInfo3);
                                            windowInfo = windowInfo3;
                                        } else {
                                            windowInfo = windowInfo2;
                                        }
                                    } else {
                                        windowInfo = null;
                                    }
                                    if (!checkFocus(clientInfo.mUid, clientInfo.mPid, clientInfo.mSelfReportedDisplayId)) {
                                        return InputBindResult.NOT_IME_TARGET_WINDOW;
                                    }
                                    int windowHandle2 = -1;
                                    if (editorInfo == null) {
                                        int i2 = clientInfo.mState;
                                        if (i2 == 3 || i2 == 4) {
                                            if (windowInfo != null) {
                                                windowHandle = windowInfo.mWindowHandle;
                                            } else {
                                                windowHandle = -1;
                                            }
                                            try {
                                                obj2 = obj3;
                                                try {
                                                    clientInfo.mMSInputMethodSession.startInputOrWindowGainedFocus(inputContext, missingMethods, editorInfo, startInputFlags, softInputMode, windowHandle);
                                                } catch (RemoteException e) {
                                                }
                                            } catch (RemoteException e2) {
                                                obj2 = obj3;
                                            }
                                        } else {
                                            obj2 = obj3;
                                        }
                                        InputBindResult inputBindResult = InputBindResult.NULL_EDITOR_INFO;
                                        return inputBindResult;
                                    }
                                    int i3 = clientInfo.mState;
                                    if (i3 == 1 || i3 == 2) {
                                        clientInfo.mBindingSequence++;
                                        if (clientInfo.mBindingSequence < 0) {
                                            clientInfo.mBindingSequence = 0;
                                        }
                                        return new InputBindResult(1, (IInputMethodSession) null, (InputChannel) null, data.mCurrentInputMethodInfo.getId(), clientInfo.mBindingSequence, (Matrix) null);
                                    } else if (i3 == 3 || i3 == 4) {
                                        clientInfo.mBindingSequence++;
                                        if (clientInfo.mBindingSequence < 0) {
                                            clientInfo.mBindingSequence = 0;
                                        }
                                        if (windowInfo != null) {
                                            windowHandle2 = windowInfo.mWindowHandle;
                                        }
                                        try {
                                            i = 4;
                                            try {
                                                clientInfo.mMSInputMethodSession.startInputOrWindowGainedFocus(inputContext, missingMethods, editorInfo, startInputFlags, softInputMode, windowHandle2);
                                            } catch (RemoteException e3) {
                                            }
                                        } catch (RemoteException e4) {
                                            i = 4;
                                        }
                                        clientInfo.mState = i;
                                        return new InputBindResult(0, clientInfo.mInputMethodSession, clientInfo.mWriteChannel.dup(), data.mCurrentInputMethodInfo.getId(), clientInfo.mBindingSequence, (Matrix) null);
                                    } else if (i3 != 5) {
                                        return null;
                                    } else {
                                        Slog.e(MultiClientInputMethodManagerService.TAG, "The client is already unregistered.");
                                        return InputBindResult.INVALID_CLIENT;
                                    }
                                default:
                                    Slog.wtf(MultiClientInputMethodManagerService.TAG, "Unexpected state=" + data.mState);
                                    return InputBindResult.IME_NOT_CONNECTED;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        obj = obj3;
                        throw th;
                    }
                }
            } else {
                Slog.e(MultiClientInputMethodManagerService.TAG, "Rejecting this client as it reported an invalid package name. uid=" + callingUid + " package=" + editorInfo.packageName);
                return InputBindResult.INVALID_PACKAGE_NAME;
            }
        }

        public void showInputMethodPickerFromClient(IInputMethodClient client, int auxiliarySubtypeMode) {
            MultiClientInputMethodManagerService.reportNotSupported();
        }

        public void showInputMethodPickerFromSystem(IInputMethodClient client, int auxiliarySubtypeMode, int displayId) {
            MultiClientInputMethodManagerService.reportNotSupported();
        }

        public void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String inputMethodId) {
            MultiClientInputMethodManagerService.reportNotSupported();
        }

        public boolean isInputMethodPickerShownForTest() {
            MultiClientInputMethodManagerService.reportNotSupported();
            return false;
        }

        public InputMethodSubtype getCurrentInputMethodSubtype() {
            MultiClientInputMethodManagerService.reportNotSupported();
            return null;
        }

        public void setAdditionalInputMethodSubtypes(String imiId, InputMethodSubtype[] subtypes) {
            MultiClientInputMethodManagerService.reportNotSupported();
        }

        public int getInputMethodWindowVisibleHeight() {
            MultiClientInputMethodManagerService.reportNotSupported();
            return 0;
        }

        public void reportActivityView(IInputMethodClient parentClient, int childDisplayId, float[] matrixValues) {
            MultiClientInputMethodManagerService.reportNotSupported();
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        }

        public IBinder getHwInnerService() {
            MultiClientInputMethodManagerService.reportNotSupported();
            return null;
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(this.mContext, MultiClientInputMethodManagerService.TAG, pw)) {
                pw.println("Current Multi Client Input Method Manager state:");
                IndentingPrintWriter ipw = new IndentingPrintWriter(pw, " ");
                ipw.println("mUserDataMap=");
                if (this.mUserDataMap != null) {
                    ipw.increaseIndent();
                    this.mUserDataMap.dump(fd, ipw, args);
                }
            }
        }
    }
}
