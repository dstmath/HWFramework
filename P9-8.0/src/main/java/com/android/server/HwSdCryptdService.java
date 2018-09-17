package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.HexDump;
import com.android.server.NativeDaemonConnector.SensitiveArg;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.HwActivityManagerService;

public final class HwSdCryptdService implements INativeDaemonConnectorCallbacks, Monitor {
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
    private static final int RESPONSE_SDCRYPTD_FAILED = 906;
    private static final int RESPONSE_SDCRYPTD_MOUNT = 905;
    private static final int RESPONSE_SDCRYPTD_SUCCESS = 907;
    private static final String TAG = "HwSdCryptdService";
    private static final boolean WATCHDOG_ENABLE = false;
    private static volatile HwSdCryptdService mInstance = null;
    private Context mContext;
    private NativeDaemonConnector mCryptConnector = new NativeDaemonConnector(this, "cryptd2", HwActivityManagerService.SERVICE_ADJ, CRYPTD_TAG, 25, null);
    private Handler mHandler;
    private HandlerThread mThread;

    private final class HwSdCryptdHandler extends Handler {
        public HwSdCryptdHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.i(HwSdCryptdService.TAG, "handleMessage MSG_DO_MOUNT");
                    String volId = msg.obj;
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
                    HwSdCryptdService.this.sendBroadcast(msg.obj);
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
        this.mCryptConnector.setDebug(true);
        new Thread(this.mCryptConnector, CRYPTD_TAG).start();
        this.mThread = new HandlerThread("HwSdCryptdHandler");
        this.mThread.start();
        this.mHandler = new HwSdCryptdHandler(this.mThread.getLooper());
    }

    public int setSdCardCryptdEnable(boolean enable, String volId) {
        if (!checkPermission("setSdCardCryptdEnable")) {
            return -1;
        }
        Log.i(TAG, "setCryptdEnable: " + enable + ",volId: " + volId);
        int code = -1;
        if (this.mCryptConnector != null) {
            try {
                NativeDaemonConnector nativeDaemonConnector = this.mCryptConnector;
                String str = "cryptsd";
                Object[] objArr = new Object[2];
                objArr[0] = enable ? EXTRA_ENABLE : "disable";
                objArr[1] = volId;
                code = nativeDaemonConnector.execute(str, objArr).getCode();
            } catch (NativeDaemonConnectorException e) {
                code = e.getCode();
                Log.e(TAG, "Unexpected response : code = " + code);
            } catch (Exception e2) {
                Log.e(TAG, "setSdCardCryptdEnable has exception : " + e2);
            }
        }
        return code;
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        Log.i(TAG, "addSdCardUserKeyAuth,userId: " + userId);
        if (!checkPermission("addSdCardUserKeyAuth")) {
            return -1;
        }
        int code = -1;
        if (this.mCryptConnector != null) {
            try {
                code = this.mCryptConnector.execute("cryptsd", new Object[]{"add_key_auth", Integer.valueOf(userId), Integer.valueOf(serialNumber), encodeBytes(token), encodeBytes(secret)}).getCode();
            } catch (NativeDaemonConnectorException e) {
                code = e.getCode();
                Log.e(TAG, "Unexpected response : code = " + code);
            } catch (Exception e2) {
                Log.e(TAG, "addSdCardUserKeyAuth has exception : " + e2);
            }
        }
        return code;
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        Log.i(TAG, "unlockSdCardKey,userId: " + userId);
        if (!checkPermission("unlockSdCardKey")) {
            return -1;
        }
        int code = -1;
        if (this.mCryptConnector != null) {
            try {
                code = this.mCryptConnector.execute("cryptsd", new Object[]{"unlock_key", Integer.valueOf(userId), Integer.valueOf(serialNumber), encodeBytes(token), encodeBytes(secret)}).getCode();
            } catch (NativeDaemonConnectorException e) {
                code = e.getCode();
                Log.e(TAG, "Unexpected response : code = " + code);
            } catch (Exception e2) {
                Log.e(TAG, "unlockSdCardKey has exception : " + e2);
            }
        }
        return code;
    }

    public int backupSecretkey() {
        Log.i(TAG, "backupSecretkey");
        if (!checkPermission("backupSecretkey")) {
            return -1;
        }
        int code = -1;
        if (this.mCryptConnector != null) {
            try {
                code = this.mCryptConnector.execute("cryptsd", new Object[]{"backup_info"}).getCode();
            } catch (NativeDaemonConnectorException e) {
                code = e.getCode();
                Log.e(TAG, "Unexpected response : code = " + code);
            } catch (Exception e2) {
                Log.e(TAG, "unlockSdCardKey has exception : " + e2);
            }
        }
        return code;
    }

    private SensitiveArg encodeBytes(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return new SensitiveArg("!");
        }
        return new SensitiveArg(HexDump.toHexString(bytes));
    }

    private void sendBroadcast(Intent intent) {
        Log.i(TAG, "sendBroadcast:" + intent.getAction());
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BROADCAST_PERSSION);
    }

    private boolean checkPermission(String method) {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            return true;
        }
        int pid = Binder.getCallingPid();
        Log.i(TAG, method + "called no permission, callingPid is " + pid + ",callingUid is " + Binder.getCallingUid());
        return false;
    }

    public void onDaemonConnected() {
        Log.i(TAG, "onDaemonConnected");
    }

    public boolean onCheckHoldWakeLock(int code) {
        Log.i(TAG, "onCheckHoldWakeLock, code " + code);
        return false;
    }

    public boolean onEvent(int code, String raw, String[] cooked) {
        Log.i(TAG, "receive event: " + code);
        StringBuilder cookedBuilder = new StringBuilder();
        cookedBuilder.append("onEvent::");
        if (cooked == null) {
            return false;
        }
        cookedBuilder.append(" cooked = ");
        for (String str : cooked) {
            cookedBuilder.append(" ");
            cookedBuilder.append(str);
        }
        Log.i(TAG, cookedBuilder.toString());
        if (RESPONSE_SDCRYPTD_MOUNT == code) {
            if (cooked.length == 2) {
                this.mHandler.obtainMessage(0, cooked[1]).sendToTarget();
            }
        } else if ((RESPONSE_SDCRYPTD_SUCCESS == code || RESPONSE_SDCRYPTD_FAILED == code) && cooked.length == 3) {
            Intent intent = new Intent(BROADCAST_HWSDCRYPTD_STATE);
            intent.putExtra(EXTRA_EVENT_CODE, code);
            intent.putExtra(EXTRA_ENABLE, cooked[1]);
            intent.putExtra(EXTRA_EVENT_MSG, cooked[2]);
            this.mHandler.obtainMessage(1, intent).sendToTarget();
        }
        return true;
    }

    public void monitor() {
        if (this.mCryptConnector != null) {
            this.mCryptConnector.monitor();
        }
    }

    public static IStorageManager getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return Stub.asInterface(service);
        }
        Log.e(TAG, "getMountService ERROR");
        return null;
    }
}
