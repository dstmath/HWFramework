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
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.HexDump;

public final class HwSdCryptdService {
    private static final String BROADCAST_HWSDCRYPTD_STATE = "com.huawei.android.HWSDCRYPTD_STATE";
    private static final String BROADCAST_PERSSION = "com.huawei.hwSdCryptd.permission.RECV_HWSDCRYPTD_RESULT";
    private static final String CRYPTD_TAG = "SdCryptdConnector";
    private static final boolean DEBUG = true;
    private static final String EXTRA_ENABLE = "enable";
    private static final String EXTRA_EVENT_CODE = "code";
    private static final String EXTRA_EVENT_MSG = "message";
    private static final int MAX_CONTAINERS = 250;
    private static final int MSG_DO_BROADCAST = 1;
    private static final int MSG_DO_MOUNT = 0;
    private static final int RESPONSE_CODE_ERROR = -1;
    private static final int RESPONSE_CODE_OK = 0;
    private static final int RESPONSE_SDCRYPTD_FAILED = 906;
    private static final int RESPONSE_SDCRYPTD_MOUNT = 905;
    private static final int RESPONSE_SDCRYPTD_SUCCESS = 907;
    private static final String TAG = "HwSdCryptdService";
    private static volatile HwSdCryptdService mInstance = null;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mThread = new HandlerThread("HwSdCryptdHandler");
    /* access modifiers changed from: private */
    public volatile IVold mVold;

    private final class HwSdCryptdHandler extends Handler {
        public HwSdCryptdHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.i(HwSdCryptdService.TAG, "handleMessage MSG_DO_MOUNT");
                    String volId = (String) msg.obj;
                    IStorageManager mountService = HwSdCryptdService.getMountService();
                    if (mountService == null) {
                        Log.e(HwSdCryptdService.TAG, "unMount cannot get IMountService service.");
                        return;
                    }
                    try {
                        mountService.mount(volId);
                        return;
                    } catch (Exception e) {
                        Log.e(HwSdCryptdService.TAG, "HwSdCryptdHandler mountService has exception : " + e);
                        return;
                    }
                case 1:
                    Log.i(HwSdCryptdService.TAG, "handleMessage MSG_DO_BROADCAST");
                    HwSdCryptdService.this.sendBroadcast((Intent) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public static synchronized HwSdCryptdService getInstance(Context context) {
        HwSdCryptdService hwSdCryptdService;
        synchronized (HwSdCryptdService.class) {
            if (mInstance == null) {
                mInstance = new HwSdCryptdService(context);
            }
            hwSdCryptdService = mInstance;
        }
        return hwSdCryptdService;
    }

    public HwSdCryptdService(Context context) {
        this.mContext = context;
        this.mThread.start();
        this.mHandler = new HwSdCryptdHandler(this.mThread.getLooper());
        connect();
    }

    /* access modifiers changed from: private */
    public void connect() {
        IBinder binder = ServiceManager.getService("vold");
        if (binder != null) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        Slog.w(HwSdCryptdService.TAG, "vold died; reconnecting");
                        IVold unused = HwSdCryptdService.this.mVold = null;
                        HwSdCryptdService.this.connect();
                    }
                }, 0);
            } catch (RemoteException e) {
                binder = null;
            }
        }
        if (binder != null) {
            this.mVold = IVold.Stub.asInterface(binder);
        } else {
            Slog.w(TAG, "vold not found; trying again");
        }
        if (this.mVold == null) {
            BackgroundThread.getHandler().postDelayed(new Runnable() {
                public final void run() {
                    HwSdCryptdService.this.connect();
                }
            }, 1000);
        }
    }

    public int setSdCardCryptdEnable(boolean enable, String volId) {
        if (!checkPermission("setSdCardCryptdEnable")) {
            return -1;
        }
        Log.i(TAG, "setCryptdEnable: " + enable + ",volId: " + volId);
        int code = -1;
        try {
            if (this.mVold != null) {
                if (enable) {
                    this.mVold.cryptsdEnable(volId);
                } else {
                    this.mVold.cryptsdDisable(volId);
                }
                code = 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "setSdCardCryptdEnable has exception : " + e);
        }
        return code;
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        Log.i(TAG, "addSdCardUserKeyAuth,userId: " + userId);
        if (!checkPermission("addSdCardUserKeyAuth")) {
            return -1;
        }
        int code = -1;
        try {
            if (this.mVold != null) {
                this.mVold.cryptsdAddKeyAuth(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
                code = 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "addSdCardUserKeyAuth has exception : " + e);
        }
        return code;
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        Log.i(TAG, "unlockSdCardKey,userId: " + userId);
        if (!checkPermission("unlockSdCardKey")) {
            return -1;
        }
        int code = -1;
        try {
            if (this.mVold != null) {
                this.mVold.cryptsdUnlockKey(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
                code = 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "unlockSdCardKey has exception : " + e);
        }
        return code;
    }

    public int backupSecretkey() {
        Log.i(TAG, "backupSecretkey");
        if (!checkPermission("backupSecretkey")) {
            return -1;
        }
        int code = -1;
        try {
            if (this.mVold != null) {
                this.mVold.cryptsdBackupInfo();
                code = 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "backupSecretkey has exception : " + e);
        }
        return code;
    }

    private String encodeBytes(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return "!";
        }
        return HexDump.toHexString(bytes);
    }

    /* access modifiers changed from: private */
    public void sendBroadcast(Intent intent) {
        Log.i(TAG, "sendBroadcast:" + intent.getAction());
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BROADCAST_PERSSION);
    }

    private boolean checkPermission(String method) {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            return true;
        }
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        Log.i(TAG, method + "called no permission, callingPid is " + pid + ",callingUid is " + uid);
        return false;
    }

    public void onCryptsdMessage(String message) {
        Log.i(TAG, "receive event: " + message);
        if (message != null) {
            String[] cooked = NativeDaemonEvent.unescapeArgs(message);
            if (!ArrayUtils.isEmpty(cooked)) {
                int code = -1;
                try {
                    code = Integer.parseInt(cooked[0]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "parseInt has exception : " + e);
                }
                if (RESPONSE_SDCRYPTD_MOUNT == code) {
                    if (cooked.length == 2) {
                        this.mHandler.obtainMessage(0, cooked[1]).sendToTarget();
                    }
                } else if ((RESPONSE_SDCRYPTD_SUCCESS == code || RESPONSE_SDCRYPTD_FAILED == code) && cooked.length == 3) {
                    Intent intent = new Intent(BROADCAST_HWSDCRYPTD_STATE);
                    intent.putExtra("code", code);
                    intent.putExtra(EXTRA_ENABLE, cooked[1]);
                    intent.putExtra(EXTRA_EVENT_MSG, cooked[2]);
                    this.mHandler.obtainMessage(1, intent).sendToTarget();
                }
            }
        }
    }

    public static IStorageManager getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IStorageManager.Stub.asInterface(service);
        }
        Log.e(TAG, "getMountService ERROR");
        return null;
    }
}
