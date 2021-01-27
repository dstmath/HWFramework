package com.android.server;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.os.storage.DiskInfo;
import android.os.storage.IStorageManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.os.BackgroundThread;
import com.android.server.display.HwUibcReceiver;
import com.android.server.storage.Hex;
import com.android.server.utils.Calculator;
import com.android.server.utils.PBKDF2WithHmacSHA1;
import com.android.server.utils.RandomUtil;
import com.android.server.utils.Sha;
import java.nio.charset.Charset;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class HwSdLockService {
    public static final String ACTION_LOCKED_SD_ADDED = "android.intent.action.HWSDLOCK_LOCKED_SD_ADDED";
    public static final String ACTION_LOCKED_SD_AUTO_UNLOCK_FAILED = "android.intent.action.HWSDLOCK_AUTO_UNLOCK_FAILED";
    public static final String ACTION_LOCKED_SD_CLEAR_COMPLETED = "android.intent.action.HWSDLOCK_CLEAR_COMPLETED";
    public static final String ACTION_LOCKED_SD_FORCE_ERASE_COMPLETED = "android.intent.action.HWSDLOCK_FORCE_ERASE_COMPLETED";
    public static final String ACTION_LOCKED_SD_REMOVED = "android.intent.action.HWSDLOCK_LOCKED_SD_REMOVED";
    public static final String ACTION_LOCKED_SD_SET_PWD_COMPLETED = "android.intent.action.HWSDLOCK_SET_PWD_COMPLETED";
    public static final String ACTION_LOCKED_SD_UNLOCK_COMPLETED = "android.intent.action.HWSDLOCK_UNLOCK_COMPLETED";
    private static final String[] COLUMNS_FOR_QUERY_IV = {COLUMN_IV};
    private static final String[] COLUMNS_FOR_QUERY_VALUE = {COLUMN_VALUE};
    private static final String COLUMN_IV = "iv";
    private static final String COLUMN_KEY = "cid";
    private static final String COLUMN_VALUE = "password";
    private static final boolean DEBUG = false;
    private static final Object DEFAULT = new Object();
    private static final String DEFAULT_IMEI = "135792468013579";
    private static final int MAX_CONTAINERS = 250;
    static final int MAX_ERASE_TIME = 180000;
    public static final int MSG_VOLUME_LOCKED_DISK_ADD = 1;
    public static final int MSG_VOLUME_LOCKED_DISK_REMOVED = 2;
    public static final int MSG_VOLUME_NODE_CHANGE = 3;
    private static final String PERMISSION = "com.huawei.hwsdlock.permission.ACCESS_HWSDCARD_SECURE";
    private static final String PRIVATE_KEY_FILE = "/data/system/SdLockPrivate.key";
    private static final String PUBLIC_KEY_FILE = "/data/system/SdLockPublic.key";
    public static final int RESPONSE_CODE_OK = 200;
    private static final String S1_KEY = "ad246a7d0ff4a09da7ecdadcf836a487";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TABLE = "sdlocksettings";
    private static final String TAG = "HwSdLockService";
    private static final String VOLD_TAG = "VoldConnector";
    public static final int VOLUME_DISK_ERASING = 645;
    public static final int VOLUME_DISK_LOCKED = 670;
    public static final int VOLUME_DISK_UNENCRYPTED = 639;
    public static final int VOLUME_DISK_UNLOCKED = 671;
    public static final int VOLUME_LOCKED_DISK_ADD = 672;
    public static final int VOLUME_LOCKED_DISK_REMOVED = 673;
    public static final int VOLUME_NODE_CREATED = 674;
    private static volatile HwSdLockService mInstance = null;
    private final Object lock = new Object();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwSdLockService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, final Intent intent) {
            HwSdLockService.this.mHwSDLockHandler.post(new Runnable() {
                /* class com.android.server.HwSdLockService.AnonymousClass1.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSdLockService.this.handleBootCompleted(intent);
                }
            });
        }
    };
    private Context mContext;
    private Runnable mEraseStateRunable = new Runnable() {
        /* class com.android.server.HwSdLockService.AnonymousClass6 */

        @Override // java.lang.Runnable
        public void run() {
            HwSdLockService.this.mErasingSDLock = false;
        }
    };
    private boolean mErasingSDLock = false;
    private String mExternalStorageVolumeID = null;
    private boolean mForceEraseCompleted = false;
    private Handler mHandler;
    Handler mHwSDLockHandler;
    private final HandlerThread mHwSDLockThread;
    private final DatabaseHelper mOpenHelper;
    ProgressDialog mProgressDialog = null;
    private StorageEventListener mStorageListener = new StorageEventListener() {
        /* class com.android.server.HwSdLockService.AnonymousClass3 */

        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.i(HwSdLockService.TAG, "onStorageStateChanged : newState " + newState + ", oldState " + oldState);
            if (newState.equals("mounted") && HwSdLockService.this.mSystemReady) {
                HwSdLockService.this.autoUnlockSD();
            }
        }
    };
    private StorageManager mStorageManager;
    private boolean mSystemReady = false;
    private volatile IVold mVold;

    public static synchronized HwSdLockService getInstance(Context context) {
        HwSdLockService hwSdLockService;
        synchronized (HwSdLockService.class) {
            if (mInstance == null) {
                mInstance = new HwSdLockService(context);
            }
            hwSdLockService = mInstance;
        }
        return hwSdLockService;
    }

    private final class HwSdLockHandler extends Handler {
        public HwSdLockHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                Log.w(HwSdLockService.TAG, "receive event: onLockedDiskAdd");
                HwSdLockService.this.sendBroadcast(HwSdLockService.ACTION_LOCKED_SD_ADDED);
                HwSdLockService.this.autoUnlockSD();
            } else if (i == 2) {
                Log.w(HwSdLockService.TAG, "receive event: onLockedDiskRemove");
                HwSdLockService.this.resetErasing();
                HwSdLockService.this.sendBroadcast(HwSdLockService.ACTION_LOCKED_SD_REMOVED);
            } else if (i == 3) {
                synchronized (HwSdLockService.this.lock) {
                    if (HwSdLockService.this.mForceEraseCompleted) {
                        HwSdLockService.this.mForceEraseCompleted = false;
                        Log.i(HwSdLockService.TAG, "format begin");
                        new Thread(new Runnable() {
                            /* class com.android.server.HwSdLockService.HwSdLockHandler.AnonymousClass1 */

                            @Override // java.lang.Runnable
                            public void run() {
                                HwSdLockService.this.formatSD();
                            }
                        }).start();
                    } else {
                        Log.i(HwSdLockService.TAG, "receive event:VOLUME_NODE_CREATED, autoUnlockSD");
                        HwSdLockService.this.autoUnlockSD();
                    }
                }
            }
        }
    }

    public void onLockedDiskAdd() {
        this.mHwSDLockHandler.obtainMessage(1).sendToTarget();
    }

    public void onLockedDiskRemove() {
        this.mHwSDLockHandler.obtainMessage(2).sendToTarget();
    }

    public void onLockedDiskChange() {
        this.mHwSDLockHandler.obtainMessage(3).sendToTarget();
    }

    public HwSdLockService(Context context) {
        this.mContext = context;
        this.mHwSDLockThread = new HandlerThread(TAG);
        this.mHwSDLockThread.start();
        this.mHwSDLockHandler = new HwSdLockHandler(this.mHwSDLockThread.getLooper());
        this.mOpenHelper = new DatabaseHelper(this.mContext);
        this.mStorageManager = (StorageManager) this.mContext.getSystemService("storage");
        this.mStorageManager.registerListener(this.mStorageListener);
        IntentFilter bootCompleteFilter = new IntentFilter();
        bootCompleteFilter.setPriority(1000);
        bootCompleteFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, bootCompleteFilter);
        this.mHandler = UiThread.getHandler();
        lambda$connect$0$HwSdLockService();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* renamed from: connect */
    public void lambda$connect$0$HwSdLockService() {
        IBinder binder = ServiceManager.getService("vold");
        if (binder != null) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.android.server.HwSdLockService.AnonymousClass2 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        Slog.w(HwSdLockService.TAG, "vold died; reconnecting");
                        HwSdLockService.this.mVold = null;
                        HwSdLockService.this.lambda$connect$0$HwSdLockService();
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
                /* class com.android.server.$$Lambda$HwSdLockService$ND1unlY9yNP8AczS6iPL5TQAq9A */

                @Override // java.lang.Runnable
                public final void run() {
                    HwSdLockService.this.lambda$connect$0$HwSdLockService();
                }
            }, 1000);
        }
    }

    public void handleBootCompleted(Intent intent) {
        if (intent == null) {
            Log.i(TAG, "mBroadcastReceiver onReceive, the intent is null!");
            return;
        }
        String action = intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            autoUnlockSD();
            this.mSystemReady = true;
            return;
        }
        Log.i(TAG, "received worng broadcast " + action);
    }

    public int setSDLockPassword(String pw) {
        checkPermission(Binder.getCallingUid());
        if (this.mErasingSDLock) {
            Log.e(TAG, "excute setSDLockPassword command failed, erasing now");
            return VOLUME_DISK_ERASING;
        } else if (pw == null) {
            Log.e(TAG, "setSDLockPassword, password is null");
            return -1;
        } else {
            int ret = -1;
            if (getSDLockState() == 670) {
                Log.e(TAG, "setSDLockPassword, SDCard locked now, need unlock first");
                return -1;
            }
            String key = getSDCardId();
            if (key == null) {
                Log.e(TAG, "setSDLockPassword, get SDCard Id error");
                return -1;
            }
            if (readKeyValue(key, null) != null) {
                clearSDLockPassword();
            }
            try {
                if (this.mVold != null) {
                    this.mVold.sdlockSetPassword(pw);
                    ret = 200;
                }
            } catch (Exception e) {
                Log.e(TAG, "setSDLockPassword error");
            }
            if (ret == 200) {
                writeKeyValue(key, pw);
                sendBroadcast(ACTION_LOCKED_SD_SET_PWD_COMPLETED);
            }
            return ret;
        }
    }

    public int clearSDLockPassword() {
        checkPermission(Binder.getCallingUid());
        if (this.mErasingSDLock) {
            Log.e(TAG, "excute clearSDLockPassword command failed, erasing now");
            return VOLUME_DISK_ERASING;
        } else if (getSDLockState() == 670) {
            Log.e(TAG, "clearSDLockPassword, SDCard locked now, need unlock first");
            return -1;
        } else {
            String key = getSDCardId();
            if (key == null) {
                Log.e(TAG, "clearSDLockPassword, get SDCard Id error");
                return -1;
            }
            String encryptPwd = readKeyValue(key, null);
            String encryptPwdIv = readKeyIv(key, null);
            String decryptPwd = null;
            if (!(encryptPwdIv == null || encryptPwd == null)) {
                try {
                    decryptPwd = aesDecrypt(encryptPwd, Hex.decodeHex(encryptPwdIv.toCharArray()));
                } catch (Exception e) {
                    Log.e(TAG, "clearSDLockPassword Decrypt error");
                }
            }
            int ret = unlockSDCard(decryptPwd);
            if (ret != 200) {
                return ret;
            }
            try {
                if (this.mVold != null) {
                    this.mVold.sdlockClearPassword(decryptPwd);
                    ret = 200;
                }
            } catch (Exception e2) {
                Log.e(TAG, "clearPassWord error");
            }
            if (ret == 200) {
                deleteKeyValue(key);
                sendBroadcast(ACTION_LOCKED_SD_CLEAR_COMPLETED);
            }
            return ret;
        }
    }

    public int unlockSDCard(String pw) {
        checkPermission(Binder.getCallingUid());
        if (this.mErasingSDLock) {
            Log.e(TAG, "excute unlockSDCard command failed, erasing now");
            return VOLUME_DISK_ERASING;
        } else if (pw == null) {
            Log.e(TAG, "unlockSDCard, password is null");
            return -1;
        } else if (getSDLockState() == 671) {
            return 200;
        } else {
            int code = 0;
            try {
                if (this.mVold != null) {
                    this.mVold.sdlockUnlock(pw);
                    code = 200;
                }
            } catch (Exception e) {
                Log.e(TAG, "unlockSDCard error");
            }
            if (200 == code) {
                sendBroadcast(ACTION_LOCKED_SD_UNLOCK_COMPLETED);
                writeKeyValue(getSDCardId(), pw);
            } else {
                Log.e(TAG, "unlockSDCard error , cause Cmd unlocked failed");
            }
            return code;
        }
    }

    public void eraseSDLock() {
        checkPermission(Binder.getCallingUid());
        if (this.mErasingSDLock) {
            Log.e(TAG, "excute eraseSDLock command failed, erasing now");
        } else if (getSDLockState() != 670) {
            Log.e(TAG, "erase failed, this sdcard is not in lock state");
        } else {
            final String key = getSDCardId();
            if (key == null) {
                Log.e(TAG, "erase failed, cannot get SD card ID");
                return;
            }
            this.mHandler.post(new Runnable() {
                /* class com.android.server.HwSdLockService.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    if (HwSdLockService.this.mProgressDialog == null) {
                        int themeID = HwSdLockService.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
                        HwSdLockService hwSdLockService = HwSdLockService.this;
                        hwSdLockService.mProgressDialog = new ProgressDialog(hwSdLockService.mContext, themeID);
                        HwSdLockService.this.mProgressDialog.setIndeterminate(true);
                        HwSdLockService.this.mProgressDialog.setCancelable(true);
                        HwSdLockService.this.mProgressDialog.getWindow().setType(2003);
                        HwSdLockService.this.mProgressDialog.setMessage(HwSdLockService.this.mContext.getText(17041085));
                    }
                    HwSdLockService.this.mProgressDialog.show();
                }
            });
            this.mErasingSDLock = true;
            this.mHandler.postDelayed(this.mEraseStateRunable, 180000);
            new Thread(new Runnable() {
                /* class com.android.server.HwSdLockService.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    HwSdLockService.this.handleEraseSDLock(key);
                }
            }).start();
        }
    }

    private void showToast() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.HwSdLockService.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                Toast.makeText(HwSdLockService.this.mContext, HwSdLockService.this.mContext.getString(33685829), 0).show();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEraseSDLock(String key) {
        int code = -1;
        synchronized (this.lock) {
            try {
                if (this.mVold != null) {
                    this.mVold.sdlockErase();
                    code = 200;
                }
            } catch (Exception e) {
                Log.e(TAG, "handleEraseSDLock error");
            }
            if (200 == code) {
                this.mForceEraseCompleted = true;
                deleteKeyValue(key);
            } else {
                Log.e(TAG, "eraseSDLock error, code " + code);
                resetErasing();
                showToast();
            }
        }
    }

    public int getSDLockState() {
        checkPermission(Binder.getCallingUid());
        if (this.mErasingSDLock) {
            Log.e(TAG, "excute getSDLockState command failed, erasing now");
            return VOLUME_DISK_ERASING;
        } else if (this.mVold == null) {
            Log.e(TAG, "getSDLockState failed, mVold is null");
            return -1;
        } else {
            try {
                String state = this.mVold.sdlockPoll();
                if (state == null) {
                    Log.e(TAG, "getSDLockState failed, state is null");
                    return -1;
                }
                String state2 = state.trim();
                if (state2.equals("locked")) {
                    return VOLUME_DISK_LOCKED;
                }
                if (state2.equals("unlocked")) {
                    return VOLUME_DISK_UNLOCKED;
                }
                if (state2.equals("none")) {
                    return VOLUME_DISK_UNENCRYPTED;
                }
                Log.w(TAG, "unknown state");
                return -1;
            } catch (Exception e) {
                Log.e(TAG, "getSDLockState error");
                return -1;
            }
        }
    }

    public String getSDCardId() {
        checkPermission(Binder.getCallingUid());
        if (this.mErasingSDLock) {
            Log.e(TAG, "excute getSDCardId command failed, erasing now");
            return null;
        } else if (this.mVold == null) {
            Log.e(TAG, "getSDLockState failed, mVold is null");
            return null;
        } else {
            try {
                String cid = this.mVold.sdlockGetCid();
                if (cid != null) {
                    return cid.trim();
                }
                Log.e(TAG, "getSDCardId failed, cid is null");
                return null;
            } catch (Exception e) {
                Log.e(TAG, "getSDCardId error");
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBroadcast(String action) {
        this.mContext.sendBroadcastAsUser(new Intent(action), UserHandle.ALL, "com.huawei.hwsdlock.permission.RECV_HWSDLOCK_STATE");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int formatSD() {
        int ret = -1;
        IStorageManager service = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        if (service != null) {
            DiskInfo externalStorageVolumeDiskInfo = getExternalStorageVolumeDisk();
            if (externalStorageVolumeDiskInfo == null) {
                Log.e(TAG, "erase failed, cannot get SD card Disk Info");
                showToast();
                return -1;
            }
            this.mExternalStorageVolumeID = externalStorageVolumeDiskInfo.getId();
            String str = this.mExternalStorageVolumeID;
            if (str == null) {
                Log.e(TAG, "erase failed, cannot get SD card mount ID");
                showToast();
                return -1;
            }
            try {
                service.partitionPublic(str);
                ret = 0;
            } catch (Exception e) {
                Log.e(TAG, "Unable to invoke IMountService.formatMedia()");
            }
        } else {
            showToast();
            Log.e(TAG, "Unable to locate IMountService");
        }
        resetErasing();
        if (ret == 0) {
            sendBroadcast(ACTION_LOCKED_SD_FORCE_ERASE_COMPLETED);
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetErasing() {
        this.mErasingSDLock = false;
        this.mExternalStorageVolumeID = null;
        this.mHandler.removeCallbacks(this.mEraseStateRunable);
        this.mHandler.post(new Runnable() {
            /* class com.android.server.HwSdLockService.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                if (HwSdLockService.this.mProgressDialog != null) {
                    HwSdLockService.this.mProgressDialog.dismiss();
                    HwSdLockService.this.mProgressDialog = null;
                }
            }
        });
    }

    private String getICSStoragePath() {
        String pathString = null;
        StorageVolume[] storageVolumes = ((StorageManager) this.mContext.getSystemService("storage")).getVolumeList();
        for (int i = 0; i < storageVolumes.length; i++) {
            if (!storageVolumes[i].isEmulated()) {
                pathString = storageVolumes[i].getPath();
                if (!pathString.contains("usb")) {
                    break;
                }
                Log.i(TAG, "getICSStoragePath =" + pathString);
            }
        }
        return pathString;
    }

    private StorageVolume getExternalStorageVolume() {
        StorageVolume[] storageVolumes = ((StorageManager) this.mContext.getSystemService("storage")).getVolumeList();
        for (int i = 0; i < storageVolumes.length; i++) {
            if (!storageVolumes[i].isEmulated()) {
                return storageVolumes[i];
            }
        }
        return null;
    }

    private DiskInfo getExternalStorageVolumeDisk() {
        for (DiskInfo disk : ((StorageManager) this.mContext.getSystemService("storage")).getDisks()) {
            if (disk.isSd()) {
                return disk;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean autoUnlockSD() {
        boolean unlocked = false;
        if (getSDLockState() != 670) {
            return true;
        }
        String key = getSDCardId();
        if (key != null) {
            String encryptPwd = readKeyValue(key, null);
            String encryptPwdIv = readKeyIv(key, null);
            if (encryptPwd == null || encryptPwdIv == null) {
                Log.i(TAG, "it's a new locked sdcard");
            } else {
                String decryptPwd = null;
                try {
                    decryptPwd = aesDecrypt(encryptPwd, Hex.decodeHex(encryptPwdIv.toCharArray()));
                } catch (Exception e) {
                    Log.e(TAG, "autoUnlockSD Decrypt error");
                }
                if (unlockSDCard(decryptPwd) == 200) {
                    Log.i(TAG, "unlock sdcard ok");
                    unlocked = true;
                } else {
                    Log.i(TAG, "unlock sdcard error");
                }
            }
        }
        if (unlocked) {
            return unlocked;
        }
        sendBroadcast(ACTION_LOCKED_SD_AUTO_UNLOCK_FAILED);
        return unlocked;
    }

    private final void checkPermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "need com.huawei.hwsdlock.permission.ACCESS_HWSDCARD_SECURE");
    }

    private final void checkWritePermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "HwSdLockService Write");
    }

    private final void checkPasswordReadPermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "HwSdLockService Read");
    }

    public void writeKeyValue(String key, String value) {
        writeKeyValue(this.mOpenHelper.getWritableDatabase(), key, value);
    }

    public void writeKeyValue(SQLiteDatabase db, String key, String value) {
        if (key == null) {
            Log.e(TAG, "writeKeyValue, sdcard id is null");
            return;
        }
        byte[] ivBytes = RandomUtil.random(16);
        String ivStr = new String(Hex.encodeHex(ivBytes));
        try {
            String aesEncryptPwd = aesEncrypt(value, ivBytes);
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_KEY, key);
            cv.put(COLUMN_VALUE, aesEncryptPwd);
            cv.put(COLUMN_IV, ivStr);
            db.beginTransaction();
            try {
                db.delete(TABLE, "cid=?", new String[]{key});
                db.insert(TABLE, null, cv);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (Exception e) {
            Log.e(TAG, "writeKeyValue Encrypt error");
        }
    }

    public void deleteKeyValue(String key) {
        if (key == null) {
            Log.e(TAG, "deleteKeyValue, sdcard id is null");
            return;
        }
        SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE, COLUMNS_FOR_QUERY_VALUE, "cid=?", new String[]{key}, null, null, null);
        if (cursor != null) {
            try {
                db.delete(TABLE, "cid=?", new String[]{key});
            } finally {
                cursor.close();
            }
        }
    }

    public String readKeyValue(String key, String defaultValue) {
        Object result = DEFAULT;
        if (key == null) {
            Log.e(TAG, "readKeyValue, sdcard id is null");
            return null;
        }
        Cursor cursor = this.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_QUERY_VALUE, "cid=?", new String[]{key}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
            cursor.close();
        }
        return result == DEFAULT ? defaultValue : (String) result;
    }

    public String readKeyIv(String key, String defaultValue) {
        Object result = DEFAULT;
        if (key == null) {
            Log.e(TAG, "readKeyIv, sdcard id is null");
            return null;
        }
        Cursor cursor = this.mOpenHelper.getReadableDatabase().query(TABLE, COLUMNS_FOR_QUERY_IV, "cid=?", new String[]{key}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(0);
            }
            cursor.close();
        }
        return result == DEFAULT ? defaultValue : (String) result;
    }

    /* access modifiers changed from: package-private */
    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "sdlocksettings.db";
        private static final int DATABASE_VERSION = 2;
        private static final String TAG = "SdLockSettingsDB";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 2);
            setWriteAheadLoggingEnabled(true);
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE sdlocksettings (_id INTEGER PRIMARY KEY AUTOINCREMENT,cid TEXT,password TEXT,iv TEXT);");
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase db) {
            createTable(db);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            int upgradeVersion = oldVersion;
            if (upgradeVersion == 1) {
                upgradeVersion = 2;
            }
            if (upgradeVersion != 2) {
                Log.w(TAG, "Failed to upgrade database!");
            }
        }
    }

    public String aesEncrypt(String password, byte[] ivBytes) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(genAESKey(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, skeySpec, new IvParameterSpec(ivBytes));
        String ciphertextStr = bcd2Str(cipher.doFinal(password.getBytes(Charset.defaultCharset())));
        String ivStr = bcd2Str(ivBytes);
        return ivStr + ciphertextStr;
    }

    public String aesDecrypt(String password, byte[] ivBytes) throws Exception {
        byte[] bytes = password.substring(bcd2Str(ivBytes).length(), password.length()).getBytes(Charset.defaultCharset());
        byte[] bcd = ASCII_To_BCD(bytes, bytes.length);
        SecretKeySpec newKey = new SecretKeySpec(genAESKey(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(2, newKey, new IvParameterSpec(ivBytes));
        return new String(cipher.doFinal(bcd), Charset.defaultCharset());
    }

    private byte[] genMasterKey() throws Exception {
        return Calculator.calculator(Hex.decodeHex(Sha.C1_KEY.toCharArray()), Hex.decodeHex(RandomUtil.C2_KEY.toCharArray()), Hex.decodeHex(PBKDF2WithHmacSHA1.C3_KEY.toCharArray()));
    }

    private byte[] genAESKey() throws Exception {
        byte[] s1 = Hex.decodeHex(S1_KEY.toCharArray());
        String imei = ((TelephonyManager) this.mContext.getSystemService("phone")).getDeviceId();
        if (imei == null) {
            Log.e(TAG, "cannot get right number, set to default");
            imei = DEFAULT_IMEI;
        }
        return PBKDF2WithHmacSHA1.generateStorngPasswordHash(new String(genMasterKey(), Charset.defaultCharset()), new String(Calculator.xor(s1, imei.getBytes(Charset.defaultCharset())), Charset.defaultCharset()), 1000);
    }

    public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
        int j;
        byte[] bcd = new byte[(asc_len / 2)];
        int j2 = 0;
        int i = 0;
        while (i < (asc_len + 1) / 2) {
            int j3 = j2 + 1;
            bcd[i] = asc_to_bcd(ascii[j2]);
            if (j3 >= asc_len) {
                j = 0;
            } else {
                int j4 = j3 + 1;
                int asc_to_bcd = asc_to_bcd(ascii[j3]);
                j3 = j4;
                j = asc_to_bcd;
            }
            bcd[i] = (byte) (j + (bcd[i] << 4));
            i++;
            j2 = j3;
        }
        return bcd;
    }

    public static byte asc_to_bcd(byte asc) {
        if (asc >= 48 && asc <= 57) {
            return (byte) (asc - 48);
        }
        if (asc >= 65 && asc <= 70) {
            return (byte) ((asc - 65) + 10);
        }
        if (asc < 97 || asc > 102) {
            return (byte) (asc - 48);
        }
        return (byte) ((asc - 97) + 10);
    }

    public static String bcd2Str(byte[] bytes) {
        char[] temp = new char[(bytes.length * 2)];
        for (int i = 0; i < bytes.length; i++) {
            char val = (char) (((bytes[i] & 240) >> 4) & 15);
            temp[i * 2] = (char) (val > '\t' ? (val + 'A') - 10 : val + '0');
            char val2 = (char) (bytes[i] & HwUibcReceiver.CurrentPacket.INPUT_MASK);
            temp[(i * 2) + 1] = (char) (val2 > '\t' ? (val2 + 'A') - 10 : val2 + '0');
        }
        return new String(temp);
    }

    public static String[] splitString(String string, int len) {
        String str;
        int x = string.length() / len;
        int y = string.length() % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        String[] strings = new String[(x + z)];
        for (int i = 0; i < x + z; i++) {
            if (i != (x + z) - 1 || y == 0) {
                str = string.substring(i * len, (i * len) + len);
            } else {
                str = string.substring(i * len, (i * len) + y);
            }
            strings[i] = str;
        }
        return strings;
    }

    public static byte[][] splitArray(byte[] data, int len) {
        int x = data.length / len;
        int y = data.length % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        byte[][] arrays = new byte[(x + z)][];
        for (int i = 0; i < x + z; i++) {
            byte[] arr = new byte[len];
            if (i != (x + z) - 1 || y == 0) {
                System.arraycopy(data, i * len, arr, 0, len);
            } else {
                System.arraycopy(data, i * len, arr, 0, y);
            }
            arrays[i] = arr;
        }
        return arrays;
    }
}
