package com.android.server;

import android.os.Handler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class FingerprintDataInterface {
    static final int AUTHENTICATE_FAIL = 2;
    static final int AUTHENTICATE_NONE = 0;
    static final int AUTHENTICATE_SUCCESS = 1;
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean DEBUG_FPLOG = false;
    public static final String TAG = "FpDataCollector";
    private static FingerprintDataInterface instance = null;
    static final Object mLock = new Object[0];
    private final int CODE_FINGERPRINT_FORBID_GOTOSLEEP;
    private final int CODE_IS_WAIT_AUTHEN;
    private final int CODE_POWER_KEYCODE;
    private final int CODE_SEND_UNLOCK_LIGHTBRIGHT;
    private final String DESCRIPTOR_FINGERPRINT_SERVICE;
    private final int SCREENOFF_UNLOCK;
    private final int SCREENON_BACKLIGHT_UNLOCK;
    private final int SCREENON_UNLOCK;
    private int isAuthenticated;
    private Handler mHandler;
    private boolean mScreenOnAuthenticated;
    private boolean mScreenOnCaptureCompleted;
    private boolean mScreenOnFingerDown;
    private boolean mSupportPowerFp;

    static {
        boolean z = false;
        if (DEBUG) {
            z = true;
        }
        DEBUG_FPLOG = z;
    }

    public FingerprintDataInterface() {
        this.DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
        this.CODE_SEND_UNLOCK_LIGHTBRIGHT = 1121;
        this.CODE_FINGERPRINT_FORBID_GOTOSLEEP = 1125;
        this.CODE_POWER_KEYCODE = 1126;
        this.CODE_IS_WAIT_AUTHEN = 1127;
        this.SCREENOFF_UNLOCK = 1;
        this.SCREENON_UNLOCK = 2;
        this.SCREENON_BACKLIGHT_UNLOCK = 3;
        this.mHandler = null;
        this.mSupportPowerFp = false;
        this.isAuthenticated = 0;
        this.mHandler = new Handler();
        initPropHwFpType();
    }

    private void initPropHwFpType() {
        String config = SystemProperties.get("ro.config.hw_fp_type", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        Log.i(TAG, "powerfp initPropHwFpType config=" + config);
        String[] bufs = config.split(",");
        if (bufs.length == 4) {
            this.mSupportPowerFp = "3".equals(bufs[0]);
        }
    }

    private boolean isSupportPowerFp() {
        return this.mSupportPowerFp;
    }

    public static FingerprintDataInterface getInstance() {
        FingerprintDataInterface fingerprintDataInterface;
        synchronized (mLock) {
            if (instance == null) {
                Log.d(TAG, "new intance in getInstance");
                instance = new FingerprintDataInterface();
            }
            fingerprintDataInterface = instance;
        }
        return fingerprintDataInterface;
    }

    public void reportFingerDown() {
        Log.d(TAG, "receive finger press down");
        boolean screenOnTmp = isScreenOn();
        synchronized (this) {
            this.mScreenOnFingerDown = screenOnTmp;
            this.mScreenOnAuthenticated = false;
            this.mScreenOnCaptureCompleted = false;
        }
    }

    public void reportCaptureCompleted() {
        Log.d(TAG, "fingerprint capture completed");
        boolean screenOnTmp = isScreenOn();
        synchronized (this) {
            this.mScreenOnCaptureCompleted = screenOnTmp;
            this.mScreenOnAuthenticated = false;
        }
    }

    public void reportFingerprintAuthenticated(boolean authenticated) {
        Log.d(TAG, "fingerprint authenticated result:" + authenticated);
        boolean screenOnTmp = isScreenOn();
        synchronized (this) {
            this.isAuthenticated = authenticated ? 1 : 2;
            this.mScreenOnAuthenticated = screenOnTmp;
        }
        reportScreenTurnedOn();
    }

    public void reportScreenStateOn(String stateStr) {
        Log.d(TAG, "DisplayPowerState :" + stateStr);
        if ("ON".equals(stateStr) && this.isAuthenticated == 1) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintDataInterface.this.sendUnlockAndLightbright(3);
                }
            });
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x001a  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0023  */
    public void reportScreenTurnedOn() {
        boolean mScreenOnInAuthenticating;
        boolean isScreenStateOnCurr = isScreenOn();
        synchronized (this) {
            if (!this.mScreenOnFingerDown && !this.mScreenOnCaptureCompleted) {
                if (!this.mScreenOnAuthenticated) {
                    mScreenOnInAuthenticating = false;
                    if (this.isAuthenticated != 0) {
                        Log.d(TAG, "case xxx, not a fingerprint unlock ");
                        return;
                    }
                    if (!mScreenOnInAuthenticating) {
                        if (!isScreenStateOnCurr) {
                            if (this.isAuthenticated == 2) {
                                Log.d(TAG, "case 000, black unlock fail");
                            } else {
                                Log.d(TAG, "case 001, wait for unlock screen on report");
                                return;
                            }
                        } else if (this.isAuthenticated == 2) {
                            Log.d(TAG, "case 010, screen on after unlock fail");
                        } else {
                            Log.d(TAG, "case 011, screen on after unlock succ");
                            this.mHandler.post(new Runnable() {
                                public void run() {
                                    FingerprintDataInterface.this.sendUnlockAndLightbright(1);
                                }
                            });
                        }
                    } else if (!isScreenStateOnCurr) {
                        if (this.isAuthenticated == 2) {
                            Log.d(TAG, "case 100, unlock fail and screen off by hand");
                        } else {
                            Log.d(TAG, "case 101, unlock succ but screen off by hand");
                        }
                    } else if (this.isAuthenticated == 2) {
                        Log.d(TAG, "case 110, unlock fail during screen on");
                    } else {
                        Log.d(TAG, "case 111, unlock succ during screen on");
                        this.mHandler.post(new Runnable() {
                            public void run() {
                                FingerprintDataInterface.this.sendUnlockAndLightbright(2);
                            }
                        });
                    }
                    this.isAuthenticated = 0;
                    return;
                }
            }
            mScreenOnInAuthenticating = true;
            if (this.isAuthenticated != 0) {
            }
        }
    }

    private boolean isScreenOn() {
        try {
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (power != null) {
                return power.isInteractive();
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "can not connect to powermanagerservice");
            return true;
        }
    }

    public boolean isPowerFpForbidGotoSleep() {
        boolean result = false;
        if (!isSupportPowerFp()) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            IBinder b = ServiceManager.getService("fingerprint");
            if (b != null) {
                _data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintService");
                b.transact(1125, _data, _reply, 0);
                _reply.readException();
                result = _reply.readBoolean();
            }
        } catch (Exception localRemoteException) {
            Log.e(TAG, localRemoteException.getMessage());
        } catch (Throwable th) {
            _reply.recycle();
            _data.recycle();
            throw th;
        }
        _reply.recycle();
        _data.recycle();
        return result;
    }

    public void sendPowerKeyCode(int keyCode, boolean isDown, boolean interactive) {
        if (isSupportPowerFp()) {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            IBinder b = ServiceManager.getService("fingerprint");
            if (b != null) {
                try {
                    _data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintService");
                    _data.writeInt(keyCode);
                    _data.writeBoolean(isDown);
                    _data.writeBoolean(interactive);
                    b.transact(1126, _data, _reply, 0);
                    _reply.readException();
                } catch (RemoteException localRemoteException) {
                    localRemoteException.printStackTrace();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
            _reply.recycle();
            _data.recycle();
        }
    }

    public boolean isNeedWaitForAuthenticate() {
        boolean result = false;
        if (!isSupportPowerFp()) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintService");
                b.transact(1127, _data, _reply, 0);
                _reply.readException();
                result = _reply.readBoolean();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        Log.e(TAG, "isNeedWaitForAuthenticate:" + result);
        return result;
    }

    /* access modifiers changed from: private */
    public void sendUnlockAndLightbright(int unlockType) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken("android.hardware.fingerprint.IFingerprintService");
                _data.writeInt(unlockType);
                b.transact(1121, _data, _reply, 0);
                _reply.readException();
                int result = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }
}
