package com.android.server.trust;

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
import android.service.trust.ITrustAgentServiceCallback.Stub;
import android.util.Log;
import android.util.Slog;
import java.util.Collections;
import java.util.List;

public class TrustAgentWrapper {
    private static final String DATA_DURATION = "duration";
    private static final boolean DEBUG = false;
    private static final String EXTRA_COMPONENT_NAME = "componentName";
    private static final int MSG_GRANT_TRUST = 1;
    private static final int MSG_MANAGING_TRUST = 6;
    private static final int MSG_RESTART_TIMEOUT = 4;
    private static final int MSG_REVOKE_TRUST = 2;
    private static final int MSG_SET_TRUST_AGENT_FEATURES_COMPLETED = 5;
    private static final int MSG_TRUST_TIMEOUT = 3;
    private static final String PERMISSION = "android.permission.PROVIDE_TRUST_AGENT";
    private static final long RESTART_TIMEOUT_MILLIS = 300000;
    private static final String TAG = "TrustAgentWrapper";
    private static final String TRUST_EXPIRED_ACTION = "android.server.trust.TRUST_EXPIRED_ACTION";
    private final Intent mAlarmIntent;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmPendingIntent;
    private boolean mBound;
    private final BroadcastReceiver mBroadcastReceiver;
    private ITrustAgentServiceCallback mCallback;
    private final ServiceConnection mConnection;
    private final Context mContext;
    private final Handler mHandler;
    private boolean mManagingTrust;
    private long mMaximumTimeToLock;
    private CharSequence mMessage;
    private final ComponentName mName;
    private boolean mPendingSuccessfulUnlock;
    private long mScheduledRestartUptimeMillis;
    private IBinder mSetTrustAgentFeaturesToken;
    private ITrustAgentService mTrustAgentService;
    private boolean mTrustDisabledByDpm;
    private final TrustManagerService mTrustManagerService;
    private boolean mTrusted;
    private final int mUserId;

    public TrustAgentWrapper(Context context, TrustManagerService trustManagerService, Intent intent, UserHandle user) {
        this.mPendingSuccessfulUnlock = DEBUG;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ComponentName component = (ComponentName) intent.getParcelableExtra(TrustAgentWrapper.EXTRA_COMPONENT_NAME);
                if (TrustAgentWrapper.TRUST_EXPIRED_ACTION.equals(intent.getAction()) && TrustAgentWrapper.this.mName.equals(component)) {
                    TrustAgentWrapper.this.mHandler.removeMessages(TrustAgentWrapper.MSG_TRUST_TIMEOUT);
                    TrustAgentWrapper.this.mHandler.sendEmptyMessage(TrustAgentWrapper.MSG_TRUST_TIMEOUT);
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TrustAgentWrapper.MSG_GRANT_TRUST /*1*/:
                        if (TrustAgentWrapper.this.isConnected()) {
                            TrustAgentWrapper.this.mTrusted = true;
                            TrustAgentWrapper.this.mMessage = (CharSequence) msg.obj;
                            int flags = msg.arg1;
                            long durationMs = msg.getData().getLong(TrustAgentWrapper.DATA_DURATION);
                            if (durationMs > 0) {
                                long duration;
                                if (TrustAgentWrapper.this.mMaximumTimeToLock != 0) {
                                    duration = Math.min(durationMs, TrustAgentWrapper.this.mMaximumTimeToLock);
                                } else {
                                    duration = durationMs;
                                }
                                long expiration = SystemClock.elapsedRealtime() + duration;
                                TrustAgentWrapper.this.mAlarmPendingIntent = PendingIntent.getBroadcast(TrustAgentWrapper.this.mContext, 0, TrustAgentWrapper.this.mAlarmIntent, 268435456);
                                TrustAgentWrapper.this.mAlarmManager.set(TrustAgentWrapper.MSG_REVOKE_TRUST, expiration, TrustAgentWrapper.this.mAlarmPendingIntent);
                            }
                            TrustAgentWrapper.this.mTrustManagerService.mArchive.logGrantTrust(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName, TrustAgentWrapper.this.mMessage != null ? TrustAgentWrapper.this.mMessage.toString() : null, durationMs, flags);
                            TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, flags);
                            break;
                        }
                        Log.w(TrustAgentWrapper.TAG, "Agent is not connected, cannot grant trust: " + TrustAgentWrapper.this.mName.flattenToShortString());
                        return;
                    case TrustAgentWrapper.MSG_REVOKE_TRUST /*2*/:
                        break;
                    case TrustAgentWrapper.MSG_TRUST_TIMEOUT /*3*/:
                        TrustAgentWrapper.this.mTrustManagerService.mArchive.logTrustTimeout(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName);
                        TrustAgentWrapper.this.onTrustTimeout();
                        break;
                    case TrustAgentWrapper.MSG_RESTART_TIMEOUT /*4*/:
                        TrustAgentWrapper.this.destroy();
                        TrustAgentWrapper.this.mTrustManagerService.resetAgent(TrustAgentWrapper.this.mName, TrustAgentWrapper.this.mUserId);
                        break;
                    case TrustAgentWrapper.MSG_SET_TRUST_AGENT_FEATURES_COMPLETED /*5*/:
                        IBinder token = msg.obj;
                        boolean result = msg.arg1 != 0 ? true : TrustAgentWrapper.DEBUG;
                        if (TrustAgentWrapper.this.mSetTrustAgentFeaturesToken == token) {
                            TrustAgentWrapper.this.mSetTrustAgentFeaturesToken = null;
                            if (TrustAgentWrapper.this.mTrustDisabledByDpm && result) {
                                TrustAgentWrapper.this.mTrustDisabledByDpm = TrustAgentWrapper.DEBUG;
                                TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
                                break;
                            }
                        }
                        break;
                    case TrustAgentWrapper.MSG_MANAGING_TRUST /*6*/:
                        TrustAgentWrapper.this.mManagingTrust = msg.arg1 != 0 ? true : TrustAgentWrapper.DEBUG;
                        if (!TrustAgentWrapper.this.mManagingTrust) {
                            TrustAgentWrapper.this.mTrusted = TrustAgentWrapper.DEBUG;
                            TrustAgentWrapper.this.mMessage = null;
                        }
                        TrustAgentWrapper.this.mTrustManagerService.mArchive.logManagingTrust(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName, TrustAgentWrapper.this.mManagingTrust);
                        TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
                        break;
                }
                TrustAgentWrapper.this.mTrusted = TrustAgentWrapper.DEBUG;
                TrustAgentWrapper.this.mMessage = null;
                TrustAgentWrapper.this.mHandler.removeMessages(TrustAgentWrapper.MSG_TRUST_TIMEOUT);
                if (msg.what == TrustAgentWrapper.MSG_REVOKE_TRUST) {
                    TrustAgentWrapper.this.mTrustManagerService.mArchive.logRevokeTrust(TrustAgentWrapper.this.mUserId, TrustAgentWrapper.this.mName);
                }
                TrustAgentWrapper.this.mTrustManagerService.updateTrust(TrustAgentWrapper.this.mUserId, 0);
            }
        };
        this.mCallback = new Stub() {
            public void grantTrust(CharSequence userMessage, long durationMs, int flags) {
                Message msg = TrustAgentWrapper.this.mHandler.obtainMessage(TrustAgentWrapper.MSG_GRANT_TRUST, flags, 0, userMessage);
                msg.getData().putLong(TrustAgentWrapper.DATA_DURATION, durationMs);
                msg.sendToTarget();
            }

            public void revokeTrust() {
                TrustAgentWrapper.this.mHandler.sendEmptyMessage(TrustAgentWrapper.MSG_REVOKE_TRUST);
            }

            public void setManagingTrust(boolean managingTrust) {
                int i;
                Handler -get6 = TrustAgentWrapper.this.mHandler;
                if (managingTrust) {
                    i = TrustAgentWrapper.MSG_GRANT_TRUST;
                } else {
                    i = 0;
                }
                -get6.obtainMessage(TrustAgentWrapper.MSG_MANAGING_TRUST, i, 0).sendToTarget();
            }

            public void onConfigureCompleted(boolean result, IBinder token) {
                int i;
                Handler -get6 = TrustAgentWrapper.this.mHandler;
                if (result) {
                    i = TrustAgentWrapper.MSG_GRANT_TRUST;
                } else {
                    i = 0;
                }
                -get6.obtainMessage(TrustAgentWrapper.MSG_SET_TRUST_AGENT_FEATURES_COMPLETED, i, 0, token).sendToTarget();
            }
        };
        this.mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                TrustAgentWrapper.this.mHandler.removeMessages(TrustAgentWrapper.MSG_RESTART_TIMEOUT);
                TrustAgentWrapper.this.mTrustAgentService = ITrustAgentService.Stub.asInterface(service);
                TrustAgentWrapper.this.mTrustManagerService.mArchive.logAgentConnected(TrustAgentWrapper.this.mUserId, name);
                TrustAgentWrapper.this.setCallback(TrustAgentWrapper.this.mCallback);
                TrustAgentWrapper.this.updateDevicePolicyFeatures();
                if (TrustAgentWrapper.this.mPendingSuccessfulUnlock) {
                    TrustAgentWrapper.this.onUnlockAttempt(true);
                    TrustAgentWrapper.this.mPendingSuccessfulUnlock = TrustAgentWrapper.DEBUG;
                }
                if (TrustAgentWrapper.this.mTrustManagerService.isDeviceLockedInner(TrustAgentWrapper.this.mUserId)) {
                    TrustAgentWrapper.this.onDeviceLocked();
                } else {
                    TrustAgentWrapper.this.onDeviceUnlocked();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                TrustAgentWrapper.this.mTrustAgentService = null;
                TrustAgentWrapper.this.mManagingTrust = TrustAgentWrapper.DEBUG;
                TrustAgentWrapper.this.mSetTrustAgentFeaturesToken = null;
                TrustAgentWrapper.this.mTrustManagerService.mArchive.logAgentDied(TrustAgentWrapper.this.mUserId, name);
                TrustAgentWrapper.this.mHandler.sendEmptyMessage(TrustAgentWrapper.MSG_REVOKE_TRUST);
                if (TrustAgentWrapper.this.mBound) {
                    TrustAgentWrapper.this.scheduleRestart();
                }
            }
        };
        this.mContext = context;
        this.mTrustManagerService = trustManagerService;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mUserId = user.getIdentifier();
        this.mName = intent.getComponent();
        this.mAlarmIntent = new Intent(TRUST_EXPIRED_ACTION).putExtra(EXTRA_COMPONENT_NAME, this.mName);
        this.mAlarmIntent.setData(Uri.parse(this.mAlarmIntent.toUri(MSG_GRANT_TRUST)));
        this.mAlarmIntent.setPackage(context.getPackageName());
        IntentFilter alarmFilter = new IntentFilter(TRUST_EXPIRED_ACTION);
        alarmFilter.addDataScheme(this.mAlarmIntent.getScheme());
        alarmFilter.addDataPath(this.mAlarmIntent.toUri(MSG_GRANT_TRUST), 0);
        scheduleRestart();
        this.mBound = context.bindServiceAsUser(intent, this.mConnection, 67108865, user);
        if (this.mBound) {
            this.mContext.registerReceiver(this.mBroadcastReceiver, alarmFilter, PERMISSION, null);
        } else {
            Log.e(TAG, "Can't bind to TrustAgent " + this.mName.flattenToShortString());
        }
    }

    private void onError(Exception e) {
        Slog.w(TAG, "Remote Exception", e);
    }

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

    private void setCallback(ITrustAgentServiceCallback callback) {
        try {
            if (this.mTrustAgentService != null) {
                this.mTrustAgentService.setCallback(callback);
            }
        } catch (RemoteException e) {
            onError(e);
        }
    }

    boolean updateDevicePolicyFeatures() {
        boolean trustDisabled = DEBUG;
        try {
            if (this.mTrustAgentService != null) {
                DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
                if ((dpm.getKeyguardDisabledFeatures(null, this.mUserId) & 16) != 0) {
                    List<PersistableBundle> config = dpm.getTrustAgentConfiguration(null, this.mName, this.mUserId);
                    trustDisabled = true;
                    if (config != null && config.size() > 0) {
                        this.mSetTrustAgentFeaturesToken = new Binder();
                        this.mTrustAgentService.onConfigure(config, this.mSetTrustAgentFeaturesToken);
                    }
                } else {
                    this.mTrustAgentService.onConfigure(Collections.EMPTY_LIST, null);
                }
                long maxTimeToLock = dpm.getMaximumTimeToLockForUserAndProfiles(this.mUserId);
                if (maxTimeToLock != this.mMaximumTimeToLock) {
                    this.mMaximumTimeToLock = maxTimeToLock;
                    if (this.mAlarmPendingIntent != null) {
                        this.mAlarmManager.cancel(this.mAlarmPendingIntent);
                        this.mAlarmPendingIntent = null;
                        this.mHandler.sendEmptyMessage(MSG_TRUST_TIMEOUT);
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
        return (this.mTrusted && this.mManagingTrust && !this.mTrustDisabledByDpm) ? true : DEBUG;
    }

    public boolean isManagingTrust() {
        return (!this.mManagingTrust || this.mTrustDisabledByDpm) ? DEBUG : true;
    }

    public CharSequence getMessage() {
        return this.mMessage;
    }

    public void destroy() {
        this.mHandler.removeMessages(MSG_RESTART_TIMEOUT);
        if (this.mBound) {
            this.mTrustManagerService.mArchive.logAgentStopped(this.mUserId, this.mName);
            this.mContext.unbindService(this.mConnection);
            this.mBound = DEBUG;
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mTrustAgentService = null;
            this.mSetTrustAgentFeaturesToken = null;
            this.mHandler.sendEmptyMessage(MSG_REVOKE_TRUST);
        }
    }

    public boolean isConnected() {
        return this.mTrustAgentService != null ? true : DEBUG;
    }

    public boolean isBound() {
        return this.mBound;
    }

    public long getScheduledRestartUptimeMillis() {
        return this.mScheduledRestartUptimeMillis;
    }

    private void scheduleRestart() {
        this.mHandler.removeMessages(MSG_RESTART_TIMEOUT);
        this.mScheduledRestartUptimeMillis = SystemClock.uptimeMillis() + RESTART_TIMEOUT_MILLIS;
        this.mHandler.sendEmptyMessageAtTime(MSG_RESTART_TIMEOUT, this.mScheduledRestartUptimeMillis);
    }
}
