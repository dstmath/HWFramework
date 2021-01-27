package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IVold;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.HexDump;

public final class HwSdCryptdService {
    private static final String BROADCAST_HW_SD_CRYPTD_STATE = "com.huawei.android.HWSDCRYPTD_STATE";
    private static final String BROADCAST_PERMISSION = "com.huawei.hwSdCryptd.permission.RECV_HWSDCRYPTD_RESULT";
    private static final String BYTES_EMPTY_MSG = "!";
    private static final String EXTRA_ENABLE = "enable";
    private static final String EXTRA_EVENT_CODE = "code";
    private static final String EXTRA_EVENT_MSG = "message";
    private static final boolean IS_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String MOUNT_SERVICE_NAME = "mount";
    private static final int MSG_DO_BROADCAST = 1;
    private static final int MSG_DO_MOUNT = 0;
    private static final int RESPONSE_CODE_ERROR = -1;
    private static final int RESPONSE_CODE_OK = 0;
    private static final int RESPONSE_SDCRYPTD_FAILED = 906;
    private static final int RESPONSE_SDCRYPTD_MOUNT = 905;
    private static final int RESPONSE_SDCRYPTD_MOUNT_LENGTH = 2;
    private static final int RESPONSE_SDCRYPTD_RESULT_LENGTH = 3;
    private static final int RESPONSE_SDCRYPTD_SUCCESS = 907;
    private static final String TAG = "HwSdCryptdService";
    private static final String VOLD_SERVICE_NAME = "vold";
    private static volatile HwSdCryptdService sInstance = null;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mThread;

    private HwSdCryptdService(Context context) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
        this.mThread = new HandlerThread(TAG);
        this.mThread.start();
        this.mHandler = new HwSdCryptdHandler(this.mThread.getLooper());
    }

    public static synchronized HwSdCryptdService getInstance(Context context) {
        HwSdCryptdService hwSdCryptdService;
        synchronized (HwSdCryptdService.class) {
            if (sInstance == null) {
                sInstance = new HwSdCryptdService(context);
            }
            hwSdCryptdService = sInstance;
        }
        return hwSdCryptdService;
    }

    public static IStorageManager getMountService() {
        IBinder service = ServiceManager.getService(MOUNT_SERVICE_NAME);
        if (service != null) {
            return IStorageManager.Stub.asInterface(service);
        }
        Log.e(TAG, "getMountService: service not found.");
        return null;
    }

    public int setSdCardCryptdEnable(boolean isEnabled, String volumeId) {
        if (!checkPermission()) {
            Log.e(TAG, "setSdCardCryptdEnable: permission denied.");
            return -1;
        }
        if (IS_DEBUG) {
            Log.i(TAG, "setSdCardCryptdEnable: isEnabled: " + isEnabled + ", volId: " + volumeId);
        }
        IVold vold = IVold.Stub.asInterface(ServiceManager.getService(VOLD_SERVICE_NAME));
        if (vold == null) {
            Log.e(TAG, "setSdCardCryptdEnable: vold is null.");
            return -1;
        } else if (isEnabled) {
            try {
                vold.cryptsdEnable(volumeId);
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "setSdCardCryptdEnable: unknown exception.");
                return -1;
            }
        } else {
            vold.cryptsdDisable(volumeId);
            return 0;
        }
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        if (!checkPermission()) {
            Log.e(TAG, "addSdCardUserKeyAuth: permission denied.");
            return -1;
        }
        if (IS_DEBUG) {
            Log.i(TAG, "addSdCardUserKeyAuth: userId: " + userId);
        }
        IVold vold = IVold.Stub.asInterface(ServiceManager.getService(VOLD_SERVICE_NAME));
        if (vold == null) {
            Log.e(TAG, "addSdCardUserKeyAuth: vold is null.");
            return -1;
        }
        try {
            vold.cryptsdAddKeyAuth(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "addSdCardUserKeyAuth: unknown exception.");
            return -1;
        }
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        if (!checkPermission()) {
            Log.e(TAG, "unlockSdCardKey: permission denied.");
            return -1;
        }
        if (IS_DEBUG) {
            Log.i(TAG, "unlockSdCardKey: userId: " + userId);
        }
        IVold vold = IVold.Stub.asInterface(ServiceManager.getService(VOLD_SERVICE_NAME));
        if (vold == null) {
            Log.e(TAG, "unlockSdCardKey: vold is null.");
            return -1;
        }
        try {
            vold.cryptsdUnlockKey(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "unlockSdCardKey: unknown exception.");
            return -1;
        }
    }

    public int backupSecretkey() {
        if (!checkPermission()) {
            Log.e(TAG, "backupSecretkey: permission denied.");
            return -1;
        }
        if (IS_DEBUG) {
            Log.i(TAG, "backupSecretkey: start.");
        }
        IVold vold = IVold.Stub.asInterface(ServiceManager.getService(VOLD_SERVICE_NAME));
        if (vold == null) {
            Log.e(TAG, "backupSecretkey: vold is null.");
            return -1;
        }
        try {
            vold.cryptsdBackupInfo();
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "backupSecretkey: unknown exception.");
            return -1;
        }
    }

    public void handleEventMessage(String message) {
        if (IS_DEBUG) {
            Log.i(TAG, "handleEventMessage: received event: " + message);
        }
        if (message != null) {
            String[] unEscapeArgs = NativeDaemonEvent.unescapeArgs(message);
            if (!ArrayUtils.isEmpty(unEscapeArgs)) {
                int code = -1;
                try {
                    code = Integer.parseInt(unEscapeArgs[0]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "handleEventMessage: invalid number format.");
                }
                if (code == 905) {
                    if (unEscapeArgs.length == 2) {
                        this.mHandler.obtainMessage(0, unEscapeArgs[1]).sendToTarget();
                    }
                } else if (code != 907 && code != 906) {
                    Log.e(TAG, "handleEventMessage: Invalid response code.");
                } else if (unEscapeArgs.length == 3) {
                    Intent intent = new Intent(BROADCAST_HW_SD_CRYPTD_STATE);
                    intent.putExtra(EXTRA_EVENT_CODE, code);
                    intent.putExtra(EXTRA_ENABLE, unEscapeArgs[1]);
                    intent.putExtra(EXTRA_EVENT_MSG, unEscapeArgs[2]);
                    this.mHandler.obtainMessage(1, intent).sendToTarget();
                }
            }
        }
    }

    private String encodeBytes(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return "!";
        }
        return HexDump.toHexString(bytes);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcast(Intent intent) {
        if (IS_DEBUG) {
            Log.i(TAG, "sendBroadcast: hwcryptd state changed.");
        }
        Context context = this.mContext;
        if (context != null) {
            context.sendBroadcastAsUser(intent, UserHandle.ALL, BROADCAST_PERMISSION);
        }
    }

    private boolean checkPermission() {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            return true;
        }
        Log.i(TAG, "checkPermission: permission denied. CallingPid is: " + Binder.getCallingPid() + ", callingUid is: " + Binder.getCallingUid());
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doMount(String name) {
        IStorageManager mountService = getMountService();
        if (mountService == null) {
            Log.e(TAG, "doMount: mount service not found.");
            return;
        }
        try {
            mountService.mount(name);
        } catch (RemoteException e) {
            Log.e(TAG, "doMount: mount exception.");
        }
    }

    private final class HwSdCryptdHandler extends Handler {
        private HwSdCryptdHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                Log.i(HwSdCryptdService.TAG, "handleMessage: do mount action.");
                Object object = msg.obj;
                if (object instanceof String) {
                    HwSdCryptdService.this.doMount((String) object);
                }
            } else if (i == 1) {
                Log.i(HwSdCryptdService.TAG, "handleMessage: send broadcast.");
                Object obj = msg.obj;
                if (obj instanceof Intent) {
                    HwSdCryptdService.this.sendBroadcast((Intent) obj);
                }
            }
        }
    }
}
