package com.android.server.trust;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.trust.ITrustAgentService;
import android.service.trust.ITrustAgentServiceCallback;
import android.util.Log;
import android.util.Slog;
import java.util.Collections;
import java.util.List;

@TargetApi(21)
public class TrustAgentWrapper {
    private static final String DATA_DURATION = "duration";
    private static final String DATA_ESCROW_TOKEN = "escrow_token";
    private static final String DATA_HANDLE = "handle";
    private static final String DATA_MESSAGE = "message";
    private static final String DATA_NEW_AUTH = "new_auth";
    private static final String DATA_USER_ID = "user_id";
    private static final boolean DEBUG = TrustManagerService.DEBUG;
    private static final String EXTRA_COMPONENT_NAME = "componentName";
    private static final int MSG_ADD_ESCROW_TOKEN = 7;
    private static final int MSG_ESCROW_TOKEN_STATE = 9;
    private static final int MSG_GRANT_TRUST = 1;
    private static final int MSG_MANAGING_TRUST = 6;
    private static final int MSG_REMOVE_ESCROW_TOKEN = 8;
    private static final int MSG_RESTART_TIMEOUT = 4;
    private static final int MSG_REVOKE_TRUST = 2;
    private static final int MSG_SET_TRUST_AGENT_FEATURES_COMPLETED = 5;
    private static final int MSG_SHOW_KEYGUARD_ERROR_MESSAGE = 11;
    private static final int MSG_TRUST_TIMEOUT = 3;
    private static final int MSG_UNLOCK_USER = 10;
    private static final String PERMISSION = "android.permission.PROVIDE_TRUST_AGENT";
    private static final long RESTART_AGENT_NOW_MILLIS = 1000;
    private static final long RESTART_TIMEOUT_MILLIS = 300000;
    private static final String TAG = "TrustAgentWrapper";
    private static final String TRUST_EXPIRED_ACTION = "android.server.trust.TRUST_EXPIRED_ACTION";
    private final Intent mAlarmIntent;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmPendingIntent;
    private boolean mBound;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.trust.TrustAgentWrapper.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            ComponentName component = (ComponentName) intent.getParcelableExtra(TrustAgentWrapper.EXTRA_COMPONENT_NAME);
            String action = intent.getAction();
            if (TrustAgentWrapper.TRUST_EXPIRED_ACTION.equals(action) && TrustAgentWrapper.this.mName.equals(component)) {
                TrustAgentWrapper.this.mHandler.removeMessages(3);
                TrustAgentWrapper.this.mHandler.sendEmptyMessage(3);
            } else if (!"android.intent.action.SCREEN_OFF".equals(action)) {
                Log.d(TrustAgentWrapper.TAG, "ignore " + action);
            } else if (TrustAgentWrapper.this.mRestartDiedAgent) {
                Log.d(TrustAgentWrapper.TAG, "screen off and restart");
                TrustAgentWrapper.this.mRestartDiedAgent = false;
                TrustAgentWrapper.this.scheduleRestart(1000);
            }
        }
    };
    private ITrustAgentServiceCallback mCallback = new ITrustAgentServiceCallback.Stub() {
        /* class com.android.server.trust.TrustAgentWrapper.AnonymousClass3 */

        public void grantTrust(CharSequence userMessage, long durationMs, int flags) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "enableTrust(" + ((Object) userMessage) + ", durationMs = " + durationMs + ", flags = " + flags + ")");
            }
            Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(1, flags, 0, userMessage);
            msg.getData().putLong(TrustAgentWrapper.DATA_DURATION, durationMs);
            msg.getData().putBoolean(TrustAgentWrapper.DATA_NEW_AUTH, false);
            msg.sendToTarget();
        }

        public void grantTrustInNewAuth(CharSequence userMessage, long durationMs, int flags, boolean isNewAuth) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "enableTrust(" + ((Object) userMessage) + ", durationMs = " + durationMs + ", flags = " + flags + ")");
            }
            Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(1, flags, 0, userMessage);
            msg.getData().putLong(TrustAgentWrapper.DATA_DURATION, durationMs);
            msg.getData().putBoolean(TrustAgentWrapper.DATA_NEW_AUTH, isNewAuth);
            msg.sendToTarget();
        }

        public void revokeTrust() {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "revokeTrust()");
            }
            TrustAgentWrapper.this.mHandler.sendEmptyMessage(2);
        }

        public void revokeTrustWithPara(boolean isFeatureAndScreenOff) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "revokeTrustWithPara()");
            }
            TrustAgentWrapper.this.mHandler.obtainMessage(2, Boolean.valueOf(isFeatureAndScreenOff)).sendToTarget();
        }

        public void setManagingTrust(boolean managingTrust) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "managingTrust()");
            }
            TrustAgentWrapper.this.mHandler.obtainMessage(6, managingTrust ? 1 : 0, 0).sendToTarget();
        }

        public void onConfigureCompleted(boolean result, IBinder token) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "onSetTrustAgentFeaturesEnabledCompleted(result=" + result);
            }
            TrustAgentWrapper.this.mHandler.obtainMessage(5, result ? 1 : 0, 0, token).sendToTarget();
        }

        public void addEscrowToken(byte[] token, int userId) {
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17891345)) {
                if (TrustAgentWrapper.DEBUG) {
                    Slog.d(TrustAgentWrapper.TAG, "adding escrow token for user " + userId);
                }
                Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(7);
                msg.getData().putByteArray(TrustAgentWrapper.DATA_ESCROW_TOKEN, token);
                msg.getData().putInt(TrustAgentWrapper.DATA_USER_ID, userId);
                msg.sendToTarget();
                return;
            }
            throw new SecurityException("Escrow token API is not allowed.");
        }

        public void isEscrowTokenActive(long handle, int userId) {
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17891345)) {
                if (TrustAgentWrapper.DEBUG) {
                    Slog.d(TrustAgentWrapper.TAG, "checking the state of escrow token on user " + userId);
                }
                Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(9);
                msg.getData().putLong(TrustAgentWrapper.DATA_HANDLE, handle);
                msg.getData().putInt(TrustAgentWrapper.DATA_USER_ID, userId);
                msg.sendToTarget();
                return;
            }
            throw new SecurityException("Escrow token API is not allowed.");
        }

        public void removeEscrowToken(long handle, int userId) {
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17891345)) {
                if (TrustAgentWrapper.DEBUG) {
                    Slog.d(TrustAgentWrapper.TAG, "removing escrow token on user " + userId);
                }
                Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(8);
                msg.getData().putLong(TrustAgentWrapper.DATA_HANDLE, handle);
                msg.getData().putInt(TrustAgentWrapper.DATA_USER_ID, userId);
                msg.sendToTarget();
                return;
            }
            throw new SecurityException("Escrow token API is not allowed.");
        }

        public void unlockUserWithToken(long handle, byte[] token, int userId) {
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17891345)) {
                if (TrustAgentWrapper.DEBUG) {
                    Slog.d(TrustAgentWrapper.TAG, "unlocking user " + userId);
                }
                Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(10);
                msg.getData().putInt(TrustAgentWrapper.DATA_USER_ID, userId);
                msg.getData().putLong(TrustAgentWrapper.DATA_HANDLE, handle);
                msg.getData().putByteArray(TrustAgentWrapper.DATA_ESCROW_TOKEN, token);
                msg.sendToTarget();
                return;
            }
            throw new SecurityException("Escrow token API is not allowed.");
        }

        public void showKeyguardErrorMessage(CharSequence message) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "Showing keyguard error message: " + ((Object) message));
            }
            Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(11);
            msg.getData().putCharSequence(TrustAgentWrapper.DATA_MESSAGE, message);
            msg.sendToTarget();
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        /* class com.android.server.trust.TrustAgentWrapper.AnonymousClass4 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "TrustAgent started : " + name.flattenToString());
            }
            TrustAgentWrapper.this.mRestartDiedAgent = false;
            TrustAgentWrapper.this.mHandler.removeMessages(4);
            TrustAgentWrapper.this.mTrustAgentService = ITrustAgentService.Stub.asInterface(service);
            TrustAgentWrapper.this.mTrustManagerService.mArchive.logAgentConnected(TrustAgentWrapper.this.mUserId, name);
            TrustAgentWrapper trustAgentWrapper = TrustAgentWrapper.this;
            trustAgentWrapper.setCallback(trustAgentWrapper.mCallback);
            TrustAgentWrapper.this.updateDevicePolicyFeatures();
            if (TrustAgentWrapper.this.mPendingSuccessfulUnlock) {
                TrustAgentWrapper.this.onUnlockAttempt(true);
                TrustAgentWrapper.this.mPendingSuccessfulUnlock = false;
            }
            if (TrustAgentWrapper.this.mTrustManagerService.isDeviceLockedInner(TrustAgentWrapper.this.mUserId)) {
                TrustAgentWrapper.this.onDeviceLocked();
            } else {
                TrustAgentWrapper.this.onDeviceUnlocked();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "TrustAgent disconnected : " + name.flattenToShortString());
            }
            TrustAgentWrapper.this.mTrustAgentService = null;
            TrustAgentWrapper.this.mManagingTrust = false;
            TrustAgentWrapper.this.mSetTrustAgentFeaturesToken = null;
            TrustAgentWrapper.this.mTrustManagerService.mArchive.logAgentDied(TrustAgentWrapper.this.mUserId, name);
            TrustAgentWrapper.this.mHandler.sendEmptyMessage(2);
            if (TrustAgentWrapper.this.mBound) {
                TrustAgentWrapper.this.mRestartDiedAgent = true;
                TrustAgentWrapper.this.scheduleRestart(300000);
            }
        }
    };
    private final Context mContext;
    private final Handler mHandler = new Handler() {
        /* class com.android.server.trust.TrustAgentWrapper.AnonymousClass2 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            long duration;
            boolean result = true;
            int i = 1;
            boolean z = true;
            String str = null;
            switch (msg.what) {
                case 1:
                    if (!TrustAgentWrapper.this.isConnected()) {
                        Log.w(TrustAgentWrapper.TAG, "Agent is not connected, cannot grant trust: " + TrustAgentWrapper.this.mName.flattenToShortString());
                        return;
                    }
                    TrustAgentWrapper.this.mTrusted = true;
                    TrustAgentWrapper.this.mMessage = (CharSequence) msg.obj;
                    int flags = msg.arg1;
                    long durationMs = msg.getData().getLong(TrustAgentWrapper.DATA_DURATION);
                    if (durationMs > 0) {
                        if (TrustAgentWrapper.this.mMaximumTimeToLock != 0) {
                            duration = Math.min(durationMs, TrustAgentWrapper.this.mMaximumTimeToLock);
                            if (TrustAgentWrapper.DEBUG) {
                                Slog.d(TrustAgentWrapper.TAG, "DPM lock timeout in effect. Timeout adjusted from " + durationMs + " to " + duration);
                            }
                        } else {
                            duration = durationMs;
                        }
                        TrustAgentWrapper trustAgentWrapper = TrustAgentWrapper.this;
                        trustAgentWrapper.mAlarmPendingIntent = PendingIntent.getBroadcast(trustAgentWrapper.mContext, 0, TrustAgentWrapper.this.mAlarmIntent, 268435456);
                        TrustAgentWrapper.this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + duration, TrustAgentWrapper.this.mAlarmPendingIntent);
                    }
                    TrustArchive trustArchive = TrustAgentWrapper.this.mTrustManagerService.mArchive;
                    int i2 = TrustAgentWrapper.this.mUserId;
                    ComponentName componentName = TrustAgentWrapper.this.mName;
                    if (TrustAgentWrapper.this.mMessage != null) {
                        str = TrustAgentWrapper.this.mMessage.toString();
                    }
                    trustArchive.logGrantTrust(i2, componentName, str, durationMs, flags);
                    TrustAgentWrapper.this.mTrustManagerService.updateTrustFromGrand(TrustAgentWrapper.this.mUserId, flags, msg.getData().getBoolean(TrustAgentWrapper.DATA_NEW_AUTH));
                    return;
                case 2:
                    break;
                case 3:
                    if (TrustAgentWrapper.DEBUG) {
                        Slog.d(TrustAgentWrapper.TAG, "Trust timed out : " + TrustAgentWrapper.this.mName.flattenToShortString());
                    }
                    TrustAgentWrapper.this.mTrustManagerService.mArchive.logTrustTimeout(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName);
                    TrustAgentWrapper.this.onTrustTimeout();
                    break;
                case 4:
                    Slog.w(TrustAgentWrapper.TAG, "Connection attempt to agent " + TrustAgentWrapper.this.mName.flattenToShortString() + " timed out, rebinding");
                    TrustAgentWrapper.this.destroy();
                    TrustAgentWrapper.this.mTrustManagerService.resetAgent(TrustAgentWrapper.this.mName, TrustAgentWrapper.this.mUserId);
                    return;
                case 5:
                    IBinder token = (IBinder) msg.obj;
                    if (msg.arg1 == 0) {
                        result = false;
                    }
                    if (TrustAgentWrapper.this.mSetTrustAgentFeaturesToken == token) {
                        TrustAgentWrapper.this.mSetTrustAgentFeaturesToken = null;
                        if (TrustAgentWrapper.this.mTrustDisabledByDpm && result) {
                            if (TrustAgentWrapper.DEBUG) {
                                Slog.d(TrustAgentWrapper.TAG, "Re-enabling agent because it acknowledged enabled features: " + TrustAgentWrapper.this.mName.flattenToShortString());
                            }
                            TrustAgentWrapper.this.mTrustDisabledByDpm = false;
                            TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
                            return;
                        }
                        return;
                    } else if (TrustAgentWrapper.DEBUG) {
                        Slog.w(TrustAgentWrapper.TAG, "Ignoring MSG_SET_TRUST_AGENT_FEATURES_COMPLETED with obsolete token: " + TrustAgentWrapper.this.mName.flattenToShortString());
                        return;
                    } else {
                        return;
                    }
                case 6:
                    TrustAgentWrapper trustAgentWrapper2 = TrustAgentWrapper.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    trustAgentWrapper2.mManagingTrust = z;
                    if (!TrustAgentWrapper.this.mManagingTrust) {
                        TrustAgentWrapper.this.mTrusted = false;
                        TrustAgentWrapper.this.mMessage = null;
                    }
                    TrustAgentWrapper.this.mTrustManagerService.mArchive.logManagingTrust(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName, TrustAgentWrapper.this.mManagingTrust);
                    TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
                    return;
                case 7:
                    byte[] eToken = msg.getData().getByteArray(TrustAgentWrapper.DATA_ESCROW_TOKEN);
                    int userId = msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID);
                    long handle = TrustAgentWrapper.this.mTrustManagerService.addEscrowToken(eToken, userId);
                    boolean resultDeliverred = false;
                    try {
                        if (TrustAgentWrapper.this.mTrustAgentService != null) {
                            TrustAgentWrapper.this.mTrustAgentService.onEscrowTokenAdded(eToken, handle, UserHandle.of(userId));
                            resultDeliverred = true;
                        }
                    } catch (RemoteException e) {
                        TrustAgentWrapper.this.onError(e);
                    }
                    if (!resultDeliverred) {
                        TrustAgentWrapper.this.mTrustManagerService.removeEscrowToken(handle, userId);
                        return;
                    }
                    return;
                case 8:
                    long handle2 = msg.getData().getLong(TrustAgentWrapper.DATA_HANDLE);
                    boolean success = TrustAgentWrapper.this.mTrustManagerService.removeEscrowToken(handle2, msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID));
                    try {
                        if (TrustAgentWrapper.this.mTrustAgentService != null) {
                            TrustAgentWrapper.this.mTrustAgentService.onEscrowTokenRemoved(handle2, success);
                            return;
                        }
                        return;
                    } catch (RemoteException e2) {
                        TrustAgentWrapper.this.onError(e2);
                        return;
                    }
                case 9:
                    long handle3 = msg.getData().getLong(TrustAgentWrapper.DATA_HANDLE);
                    boolean active = TrustAgentWrapper.this.mTrustManagerService.isEscrowTokenActive(handle3, msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID));
                    try {
                        if (TrustAgentWrapper.this.mTrustAgentService != null) {
                            ITrustAgentService iTrustAgentService = TrustAgentWrapper.this.mTrustAgentService;
                            if (!active) {
                                i = 0;
                            }
                            iTrustAgentService.onTokenStateReceived(handle3, i);
                            return;
                        }
                        return;
                    } catch (RemoteException e3) {
                        TrustAgentWrapper.this.onError(e3);
                        return;
                    }
                case 10:
                    TrustAgentWrapper.this.mTrustManagerService.unlockUserWithToken(msg.getData().getLong(TrustAgentWrapper.DATA_HANDLE), msg.getData().getByteArray(TrustAgentWrapper.DATA_ESCROW_TOKEN), msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID));
                    return;
                case 11:
                    TrustAgentWrapper.this.mTrustManagerService.showKeyguardErrorMessage(msg.getData().getCharSequence(TrustAgentWrapper.DATA_MESSAGE));
                    return;
                default:
                    return;
            }
            TrustAgentWrapper.this.mTrusted = false;
            TrustAgentWrapper.this.mMessage = null;
            TrustAgentWrapper.this.mHandler.removeMessages(3);
            if (msg.what == 2) {
                TrustAgentWrapper.this.mTrustManagerService.mArchive.logRevokeTrust(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName);
            }
            if (msg.obj instanceof Boolean) {
                TrustAgentWrapper.this.mTrustManagerService.updateTrustFromRevoke(TrustAgentWrapper.this.mUserId, 0, ((Boolean) msg.obj).booleanValue());
            } else {
                TrustAgentWrapper.this.mTrustManagerService.updateTrustFromRevoke(TrustAgentWrapper.this.mUserId, 0, false);
            }
        }
    };
    private boolean mManagingTrust;
    private long mMaximumTimeToLock;
    private CharSequence mMessage;
    private final ComponentName mName;
    private boolean mPendingSuccessfulUnlock = false;
    private boolean mRestartDiedAgent = false;
    private long mScheduledRestartUptimeMillis;
    private IBinder mSetTrustAgentFeaturesToken;
    private ITrustAgentService mTrustAgentService;
    private boolean mTrustDisabledByDpm;
    private final TrustManagerService mTrustManagerService;
    private boolean mTrusted;
    private final int mUserId;

    public TrustAgentWrapper(Context context, TrustManagerService trustManagerService, Intent intent, UserHandle user) {
        this.mContext = context;
        this.mTrustManagerService = trustManagerService;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUserId = user.getIdentifier();
        this.mName = intent.getComponent();
        this.mAlarmIntent = new Intent(TRUST_EXPIRED_ACTION).putExtra(EXTRA_COMPONENT_NAME, this.mName);
        Intent intent2 = this.mAlarmIntent;
        intent2.setData(Uri.parse(intent2.toUri(1)));
        this.mAlarmIntent.setPackage(context.getPackageName());
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mBroadcastReceiver, screenFilter, null, null);
        Log.i(TAG, "register:ACTION_SCREEN_OFF");
        IntentFilter alarmFilter = new IntentFilter(TRUST_EXPIRED_ACTION);
        alarmFilter.addDataScheme(this.mAlarmIntent.getScheme());
        alarmFilter.addDataPath(this.mAlarmIntent.toUri(1), 0);
        scheduleRestart(300000);
        this.mBound = context.bindServiceAsUser(intent, this.mConnection, 67108865, user);
        if (this.mBound) {
            this.mContext.registerReceiver(this.mBroadcastReceiver, alarmFilter, PERMISSION, null);
            return;
        }
        Log.e(TAG, "Can't bind to TrustAgent " + this.mName.flattenToShortString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onError(Exception e) {
        Slog.w(TAG, "Exception ", e);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTrustTimeout() {
        try {
            if (this.mTrustAgentService != null) {
                this.mTrustAgentService.onTrustTimeout();
            }
        } catch (RemoteException e) {
            onError(e);
        }
    }

    public void onUnlockAttempt(boolean successful) {
        try {
            if (this.mTrustAgentService != null) {
                this.mTrustAgentService.onUnlockAttempt(successful);
            } else {
                this.mPendingSuccessfulUnlock = successful;
            }
        } catch (RemoteException e) {
            onError(e);
        }
    }

    public void onUnlockLockout(int timeoutMs) {
        try {
            if (this.mTrustAgentService != null) {
                this.mTrustAgentService.onUnlockLockout(timeoutMs);
            }
        } catch (RemoteException e) {
            onError(e);
        }
    }

    public void onDeviceLocked() {
        try {
            if (this.mTrustAgentService != null) {
                this.mTrustAgentService.onDeviceLocked();
            }
        } catch (RemoteException e) {
            onError(e);
        }
    }

    public void onDeviceUnlocked() {
        try {
            if (this.mTrustAgentService != null) {
                this.mTrustAgentService.onDeviceUnlocked();
            }
        } catch (RemoteException e) {
            onError(e);
        }
    }

    public void onEscrowTokenActivated(long handle, int userId) {
        if (DEBUG) {
            Slog.d(TAG, "onEscrowTokenActivated: " + handle + " user: " + userId);
        }
        ITrustAgentService iTrustAgentService = this.mTrustAgentService;
        if (iTrustAgentService != null) {
            try {
                iTrustAgentService.onTokenStateReceived(handle, 1);
            } catch (RemoteException e) {
                onError(e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCallback(ITrustAgentServiceCallback callback) {
        try {
            if (this.mTrustAgentService != null) {
                this.mTrustAgentService.setCallback(callback);
            }
        } catch (RemoteException e) {
            onError(e);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateDevicePolicyFeatures() {
        boolean trustDisabled = false;
        if (DEBUG) {
            Slog.d(TAG, "updateDevicePolicyFeatures(" + this.mName + ")");
        }
        try {
            if (this.mTrustAgentService != null) {
                DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
                if ((dpm.getKeyguardDisabledFeatures(null, this.mUserId) & 16) != 0) {
                    List<PersistableBundle> config = dpm.getTrustAgentConfiguration(null, this.mName, this.mUserId);
                    trustDisabled = true;
                    if (DEBUG) {
                        Slog.d(TAG, "Detected trust agents disabled. Config = " + config);
                    }
                    if (config != null && config.size() > 0) {
                        if (DEBUG) {
                            Slog.d(TAG, "TrustAgent " + this.mName.flattenToShortString() + " disabled until it acknowledges " + config);
                        }
                        this.mSetTrustAgentFeaturesToken = new Binder();
                        this.mTrustAgentService.onConfigure(config, this.mSetTrustAgentFeaturesToken);
                    }
                } else {
                    this.mTrustAgentService.onConfigure(Collections.EMPTY_LIST, (IBinder) null);
                }
                long maxTimeToLock = dpm.getMaximumTimeToLock(null, this.mUserId);
                if (maxTimeToLock != this.mMaximumTimeToLock) {
                    this.mMaximumTimeToLock = maxTimeToLock;
                    if (this.mAlarmPendingIntent != null) {
                        this.mAlarmManager.cancel(this.mAlarmPendingIntent);
                        this.mAlarmPendingIntent = null;
                        this.mHandler.sendEmptyMessage(3);
                    }
                }
            }
        } catch (RemoteException e) {
            onError(e);
        }
        if (this.mTrustDisabledByDpm != trustDisabled) {
            this.mTrustDisabledByDpm = trustDisabled;
            this.mTrustManagerService.updateTrust(this.mUserId, 0);
        }
        return trustDisabled;
    }

    public boolean isTrusted() {
        return this.mTrusted && this.mManagingTrust && !this.mTrustDisabledByDpm;
    }

    public boolean isManagingTrust() {
        return this.mManagingTrust && !this.mTrustDisabledByDpm;
    }

    public CharSequence getMessage() {
        return this.mMessage;
    }

    public void destroy() {
        this.mHandler.removeMessages(4);
        if (this.mBound) {
            if (DEBUG) {
                Slog.d(TAG, "TrustAgent unbound : " + this.mName.flattenToShortString());
            }
            this.mTrustManagerService.mArchive.logAgentStopped(this.mUserId, this.mName);
            this.mContext.unbindService(this.mConnection);
            this.mBound = false;
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mTrustAgentService = null;
            this.mSetTrustAgentFeaturesToken = null;
            this.mHandler.sendEmptyMessage(2);
        }
    }

    public boolean isConnected() {
        return this.mTrustAgentService != null;
    }

    public boolean isBound() {
        return this.mBound;
    }

    public long getScheduledRestartUptimeMillis() {
        return this.mScheduledRestartUptimeMillis;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleRestart(long delayTimeMillis) {
        this.mHandler.removeMessages(4);
        this.mScheduledRestartUptimeMillis = SystemClock.uptimeMillis() + delayTimeMillis;
        this.mHandler.sendEmptyMessageAtTime(4, this.mScheduledRestartUptimeMillis);
    }
}
