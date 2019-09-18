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
    private static final String DATA_USER_ID = "user_id";
    /* access modifiers changed from: private */
    public static final boolean DEBUG = TrustManagerService.DEBUG;
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
    private static final long RESTART_TIMEOUT_MILLIS = 300000;
    private static final String TAG = "TrustAgentWrapper";
    private static final String TRUST_EXPIRED_ACTION = "android.server.trust.TRUST_EXPIRED_ACTION";
    /* access modifiers changed from: private */
    public final Intent mAlarmIntent;
    /* access modifiers changed from: private */
    public AlarmManager mAlarmManager;
    /* access modifiers changed from: private */
    public PendingIntent mAlarmPendingIntent;
    /* access modifiers changed from: private */
    public boolean mBound;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ComponentName component = (ComponentName) intent.getParcelableExtra(TrustAgentWrapper.EXTRA_COMPONENT_NAME);
            if (TrustAgentWrapper.TRUST_EXPIRED_ACTION.equals(intent.getAction()) && TrustAgentWrapper.this.mName.equals(component)) {
                TrustAgentWrapper.this.mHandler.removeMessages(3);
                TrustAgentWrapper.this.mHandler.sendEmptyMessage(3);
            }
        }
    };
    /* access modifiers changed from: private */
    public ITrustAgentServiceCallback mCallback = new ITrustAgentServiceCallback.Stub() {
        public void grantTrust(CharSequence userMessage, long durationMs, int flags) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "enableTrust(" + userMessage + ", durationMs = " + durationMs + ", flags = " + flags + ")");
            }
            Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(1, flags, 0, userMessage);
            msg.getData().putLong(TrustAgentWrapper.DATA_DURATION, durationMs);
            msg.sendToTarget();
        }

        public void revokeTrust() {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "revokeTrust()");
            }
            TrustAgentWrapper.this.mHandler.sendEmptyMessage(2);
        }

        public void setManagingTrust(boolean managingTrust) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "managingTrust()");
            }
            TrustAgentWrapper.this.mHandler.obtainMessage(6, managingTrust, 0).sendToTarget();
        }

        public void onConfigureCompleted(boolean result, IBinder token) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "onSetTrustAgentFeaturesEnabledCompleted(result=" + result);
            }
            TrustAgentWrapper.this.mHandler.obtainMessage(5, result, 0, token).sendToTarget();
        }

        public void addEscrowToken(byte[] token, int userId) {
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17956874)) {
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
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17956874)) {
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
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17956874)) {
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
            if (!TrustAgentWrapper.this.mContext.getResources().getBoolean(17956874)) {
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
                Slog.d(TrustAgentWrapper.TAG, "Showing keyguard error message: " + message);
            }
            Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(11);
            msg.getData().putCharSequence(TrustAgentWrapper.DATA_MESSAGE, message);
            msg.sendToTarget();
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "TrustAgent started : " + name.flattenToString());
            }
            TrustAgentWrapper.this.mHandler.removeMessages(4);
            ITrustAgentService unused = TrustAgentWrapper.this.mTrustAgentService = ITrustAgentService.Stub.asInterface(service);
            TrustAgentWrapper.this.mTrustManagerService.mArchive.logAgentConnected(TrustAgentWrapper.this.mUserId, name);
            TrustAgentWrapper.this.setCallback(TrustAgentWrapper.this.mCallback);
            TrustAgentWrapper.this.updateDevicePolicyFeatures();
            if (TrustAgentWrapper.this.mPendingSuccessfulUnlock) {
                TrustAgentWrapper.this.onUnlockAttempt(true);
                boolean unused2 = TrustAgentWrapper.this.mPendingSuccessfulUnlock = false;
            }
            if (TrustAgentWrapper.this.mTrustManagerService.isDeviceLockedInner(TrustAgentWrapper.this.mUserId)) {
                TrustAgentWrapper.this.onDeviceLocked();
            } else {
                TrustAgentWrapper.this.onDeviceUnlocked();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            if (TrustAgentWrapper.DEBUG) {
                Slog.d(TrustAgentWrapper.TAG, "TrustAgent disconnected : " + name.flattenToShortString());
            }
            ITrustAgentService unused = TrustAgentWrapper.this.mTrustAgentService = null;
            boolean unused2 = TrustAgentWrapper.this.mManagingTrust = false;
            IBinder unused3 = TrustAgentWrapper.this.mSetTrustAgentFeaturesToken = null;
            TrustAgentWrapper.this.mTrustManagerService.mArchive.logAgentDied(TrustAgentWrapper.this.mUserId, name);
            TrustAgentWrapper.this.mHandler.sendEmptyMessage(2);
            if (TrustAgentWrapper.this.mBound) {
                TrustAgentWrapper.this.scheduleRestart();
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            long duration;
            String str = null;
            boolean z = true;
            boolean resultDeliverred = false;
            switch (msg.what) {
                case 1:
                    if (TrustAgentWrapper.this.isConnected()) {
                        boolean unused = TrustAgentWrapper.this.mTrusted = true;
                        CharSequence unused2 = TrustAgentWrapper.this.mMessage = (CharSequence) msg.obj;
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
                            PendingIntent unused3 = TrustAgentWrapper.this.mAlarmPendingIntent = PendingIntent.getBroadcast(TrustAgentWrapper.this.mContext, 0, TrustAgentWrapper.this.mAlarmIntent, 268435456);
                            TrustAgentWrapper.this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + duration, TrustAgentWrapper.this.mAlarmPendingIntent);
                        }
                        TrustArchive trustArchive = TrustAgentWrapper.this.mTrustManagerService.mArchive;
                        int access$1000 = TrustAgentWrapper.this.mUserId;
                        ComponentName access$000 = TrustAgentWrapper.this.mName;
                        if (TrustAgentWrapper.this.mMessage != null) {
                            str = TrustAgentWrapper.this.mMessage.toString();
                        }
                        trustArchive.logGrantTrust(access$1000, access$000, str, durationMs, flags);
                        TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, flags);
                        break;
                    } else {
                        Log.w(TrustAgentWrapper.TAG, "Agent is not connected, cannot grant trust: " + TrustAgentWrapper.this.mName.flattenToShortString());
                        return;
                    }
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
                    break;
                case 5:
                    IBinder token = (IBinder) msg.obj;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    boolean result = z;
                    if (TrustAgentWrapper.this.mSetTrustAgentFeaturesToken != token) {
                        if (TrustAgentWrapper.DEBUG) {
                            Slog.w(TrustAgentWrapper.TAG, "Ignoring MSG_SET_TRUST_AGENT_FEATURES_COMPLETED with obsolete token: " + TrustAgentWrapper.this.mName.flattenToShortString());
                            break;
                        }
                    } else {
                        IBinder unused4 = TrustAgentWrapper.this.mSetTrustAgentFeaturesToken = null;
                        if (TrustAgentWrapper.this.mTrustDisabledByDpm && result) {
                            if (TrustAgentWrapper.DEBUG) {
                                Slog.d(TrustAgentWrapper.TAG, "Re-enabling agent because it acknowledged enabled features: " + TrustAgentWrapper.this.mName.flattenToShortString());
                            }
                            boolean unused5 = TrustAgentWrapper.this.mTrustDisabledByDpm = false;
                            TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
                            break;
                        }
                    }
                    break;
                case 6:
                    TrustAgentWrapper trustAgentWrapper = TrustAgentWrapper.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    boolean unused6 = trustAgentWrapper.mManagingTrust = z;
                    if (!TrustAgentWrapper.this.mManagingTrust) {
                        boolean unused7 = TrustAgentWrapper.this.mTrusted = false;
                        CharSequence unused8 = TrustAgentWrapper.this.mMessage = null;
                    }
                    TrustAgentWrapper.this.mTrustManagerService.mArchive.logManagingTrust(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName, TrustAgentWrapper.this.mManagingTrust);
                    TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
                    break;
                case 7:
                    byte[] eToken = msg.getData().getByteArray(TrustAgentWrapper.DATA_ESCROW_TOKEN);
                    int userId = msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID);
                    long handle = TrustAgentWrapper.this.mTrustManagerService.addEscrowToken(eToken, userId);
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
                        break;
                    }
                    break;
                case 8:
                    long handle2 = msg.getData().getLong(TrustAgentWrapper.DATA_HANDLE);
                    boolean success = TrustAgentWrapper.this.mTrustManagerService.removeEscrowToken(handle2, msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID));
                    try {
                        if (TrustAgentWrapper.this.mTrustAgentService != null) {
                            TrustAgentWrapper.this.mTrustAgentService.onEscrowTokenRemoved(handle2, success);
                            break;
                        }
                    } catch (RemoteException e2) {
                        TrustAgentWrapper.this.onError(e2);
                        break;
                    }
                    break;
                case 9:
                    long handle3 = msg.getData().getLong(TrustAgentWrapper.DATA_HANDLE);
                    boolean active = TrustAgentWrapper.this.mTrustManagerService.isEscrowTokenActive(handle3, msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID));
                    try {
                        if (TrustAgentWrapper.this.mTrustAgentService != null) {
                            ITrustAgentService access$1600 = TrustAgentWrapper.this.mTrustAgentService;
                            if (!active) {
                                z = false;
                            }
                            access$1600.onTokenStateReceived(handle3, z ? 1 : 0);
                            break;
                        }
                    } catch (RemoteException e3) {
                        TrustAgentWrapper.this.onError(e3);
                        break;
                    }
                    break;
                case 10:
                    TrustAgentWrapper.this.mTrustManagerService.unlockUserWithToken(msg.getData().getLong(TrustAgentWrapper.DATA_HANDLE), msg.getData().getByteArray(TrustAgentWrapper.DATA_ESCROW_TOKEN), msg.getData().getInt(TrustAgentWrapper.DATA_USER_ID));
                    break;
                case 11:
                    TrustAgentWrapper.this.mTrustManagerService.showKeyguardErrorMessage(msg.getData().getCharSequence(TrustAgentWrapper.DATA_MESSAGE));
                    break;
            }
            boolean unused9 = TrustAgentWrapper.this.mTrusted = false;
            CharSequence unused10 = TrustAgentWrapper.this.mMessage = null;
            TrustAgentWrapper.this.mHandler.removeMessages(3);
            if (msg.what == 2) {
                TrustAgentWrapper.this.mTrustManagerService.mArchive.logRevokeTrust(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName);
            }
            TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
        }
    };
    /* access modifiers changed from: private */
    public boolean mManagingTrust;
    /* access modifiers changed from: private */
    public long mMaximumTimeToLock;
    /* access modifiers changed from: private */
    public CharSequence mMessage;
    /* access modifiers changed from: private */
    public final ComponentName mName;
    /* access modifiers changed from: private */
    public boolean mPendingSuccessfulUnlock = false;
    private long mScheduledRestartUptimeMillis;
    /* access modifiers changed from: private */
    public IBinder mSetTrustAgentFeaturesToken;
    /* access modifiers changed from: private */
    public ITrustAgentService mTrustAgentService;
    /* access modifiers changed from: private */
    public boolean mTrustDisabledByDpm;
    /* access modifiers changed from: private */
    public final TrustManagerService mTrustManagerService;
    /* access modifiers changed from: private */
    public boolean mTrusted;
    /* access modifiers changed from: private */
    public final int mUserId;

    public TrustAgentWrapper(Context context, TrustManagerService trustManagerService, Intent intent, UserHandle user) {
        this.mContext = context;
        this.mTrustManagerService = trustManagerService;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUserId = user.getIdentifier();
        this.mName = intent.getComponent();
        this.mAlarmIntent = new Intent(TRUST_EXPIRED_ACTION).putExtra(EXTRA_COMPONENT_NAME, this.mName);
        this.mAlarmIntent.setData(Uri.parse(this.mAlarmIntent.toUri(1)));
        this.mAlarmIntent.setPackage(context.getPackageName());
        IntentFilter alarmFilter = new IntentFilter(TRUST_EXPIRED_ACTION);
        alarmFilter.addDataScheme(this.mAlarmIntent.getScheme());
        alarmFilter.addDataPath(this.mAlarmIntent.toUri(1), 0);
        scheduleRestart();
        this.mBound = context.bindServiceAsUser(intent, this.mConnection, 67108865, user);
        if (this.mBound) {
            this.mContext.registerReceiver(this.mBroadcastReceiver, alarmFilter, PERMISSION, null);
            return;
        }
        Log.e(TAG, "Can't bind to TrustAgent " + this.mName.flattenToShortString());
    }

    /* access modifiers changed from: private */
    public void onError(Exception e) {
        Slog.w(TAG, "Exception ", e);
    }

    /* access modifiers changed from: private */
    public void onTrustTimeout() {
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

    /* access modifiers changed from: private */
    public void setCallback(ITrustAgentServiceCallback callback) {
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
                    this.mTrustAgentService.onConfigure(Collections.EMPTY_LIST, null);
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
    public void scheduleRestart() {
        this.mHandler.removeMessages(4);
        this.mScheduledRestartUptimeMillis = SystemClock.uptimeMillis() + 300000;
        this.mHandler.sendEmptyMessageAtTime(4, this.mScheduledRestartUptimeMillis);
    }
}
