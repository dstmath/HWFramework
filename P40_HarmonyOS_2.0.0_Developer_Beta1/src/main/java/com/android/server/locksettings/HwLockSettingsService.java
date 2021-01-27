package com.android.server.locksettings;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.encrypt.ISDCardCryptedHelper;
import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.security.keystore.KeyProtection;
import android.service.gatekeeper.GateKeeperResponse;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.HwServiceFactory;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.locksettings.HwSyntheticPasswordManager;
import com.android.server.locksettings.LockSettingsService;
import com.android.server.locksettings.LockSettingsStorage;
import com.android.server.locksettings.SyntheticPasswordManager;
import com.huawei.android.app.HiEventEx;
import com.huawei.android.app.HiViewEx;
import com.huawei.android.os.UserManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.coauth.auth.RemotePasswordManager;
import com.huawei.pwdprotect.PwdProtectManager;
import com.huawei.security.HwKeystoreManager;
import huawei.android.security.IHwBehaviorCollectManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;

public class HwLockSettingsService extends LockSettingsService {
    private static final Intent ACTION_NULL = new Intent("android.intent.action.MAIN");
    private static final int AUTH_SOLUTION_STRONG = 1;
    private static final int AUTH_SOLUTION_WEAK = 0;
    private static final String CHARACTERISTICS_PROP = SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT);
    public static final String DB_KEY_PRIVACY_USER_PWD_PROTECT = "privacy_user_pwd_protect";
    private static final int DEFAULT_REMAIN_ALLOW_RETRY_COUNT = 100;
    private static final int DEFAULT_USER_NUM = 7;
    public static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
    private static final String EMPTY_APP_NAME = "";
    private static final boolean ENHANCED_GK_RULE = SystemProperties.getBoolean("ro.build.enhanced_gk_rule", false);
    private static final boolean IS_TV = ("tv".equals(CHARACTERISTICS_PROP) || "mobiletv".equals(CHARACTERISTICS_PROP));
    private static final String JSON_ERROR_COUNT = "error";
    private static final String JSON_LEVEL = "level";
    private static final String JSON_START_ELAPSED = "elapsed";
    private static final String JSON_START_RTC = "rtc";
    private static final String JSON_STOP_ELAPSED = "stop";
    private static final String KEY_CREDENTIAL_LEN = "lockscreen.hw_credential_len";
    private static final String KEY_HAS_HW_LOCK_HINT = "lockscreen.has_hw_hint_info";
    private static final String KEY_HW_LOCK_HINT = "lockscreen.hw_hint_info";
    private static final String KEY_HW_PIN_TYPE = "lockscreen.pin_type";
    private static final long KEY_UNLOCK_TYPE_NOT_SET = -1;
    private static final String KEY_UPDATE_WEAK_AUTH_TIME = "lockscreen.hw_weak_auth_time";
    public static final String LOCK_PASSWORD_FILE2 = "password2.key";
    public static final String NO_PWD_FOR_PWD_PROTECT = "no_pwd_for_protect_protect";
    private static final String NRLTAG = "LockSettingsRule";
    private static final int PASSWORD_STATUS_CHANGED = 2;
    private static final int PASSWORD_STATUS_OFF = 0;
    private static final int PASSWORD_STATUS_ON = 1;
    private static final String PERMISSION = "android.permission.ACCESS_KEYGUARD_SECURE_STORAGE";
    private static final String PERMISSION_GET_LOCK_PASSWORD_CHANGED = "com.huawei.permission.GET_LOCK_PASSWORD_CHANGED";
    private static final String PKG_HWOUC = "com.huawei.android.hwouc";
    private static final String PKG_PRIVATE_SPACE = "com.huawei.privatespace";
    private static final String PKG_SECURITYMGR = "com.huawei.securitymgr";
    private static final String PKG_SETTINGS = "com.android.settings";
    private static final String PKG_SYSTEMUI = "com.android.systemui";
    private static final String PKG_SYSTEM_MANAGER = "com.huawei.systemmanager";
    private static final String PROFILE_KEY_USER_HINT_DECRYPT = "profile_key_user_hint_decrypt_";
    private static final String PROFILE_KEY_USER_HINT_ENCRYPT = "profile_key_user_hint_encrypt_";
    private static final String PWD_ERROR_COUNT = "gk_rule_error_count";
    private static final String PWD_REGEX = "^\\d{4,32}$";
    private static final String PWD_START_TIME_ELAPSED = "password_start_time_elapsed";
    private static final String PWD_VERIFY_INFO = "password_verification_information";
    private static final String RECEIVER_ACTION_LOCK_PASSWORD_CHANGED = "com.huawei.locksettingsservice.action.LOCK_PASSWORD_CHANGED";
    private static final String RECEIVER_PACKAGE = "com.huawei.hwid";
    private static final String REMAIN_ALLOW_RETRY_COUNT = "remain_allow_retry_count";
    private static final int REMAIN_ALLOW_RETRY_COUNT_MIN = 1;
    private static final String REMAIN_LOCKED_TIME = "remain_locked_time";
    private static final String REMAIN_LOCKED_TIME_START_ELAPSED = "remain_locked_time_start_elapsed";
    private static final String REMAIN_LOCKED_TIME_START_RTC = "remain_locked_time_start_rtc";
    private static final int SECURITY_LOCK_SETTINGS = 2;
    private static final String STRONG_AUTH_SOLUTION_FLAG = "strong_auth_solution_flag";
    private static final String STRONG_AUTH_SOLUTION_WEAVER_FLAG = "strong_auth_solution_weaver_flag";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TAG = "HwLKSS";
    private static final long TEE_GATEKEEPER_VERSION_V5 = 5;
    public static final int TRANSACTION_CHECKVISITORPASSWORD = 1002;
    public static final int TRANSACTION_SETLOCKVISITORPASSWORD = 1001;
    private static final int TYPE_PRIVSPACE_PWD_PROTECT_RESET_FAIL = 991310132;
    private static final int TYPE_PRIVSPACE_PWD_PROTECT_RESET_SUCCESS = 991310131;
    private static final String USER_LOCK_HINT_FILE = "hw_lock_hint.key";
    private static final int WEAK_AUTH_FACE = 10001;
    private static final int WEAK_AUTH_FINGER = 10002;
    private static final long WEAVER_VERSION_V1 = 1;
    private static boolean loadLibraryFailed;
    private static ArrayList<Integer> mUseGKRulePids = new ArrayList<>();
    private static Map<Integer, Integer> remainAllowedCount = new HashMap();
    private static HashSet<Integer> userSets = new HashSet<>(7);
    private final String ACTION_PRIVACY_USER_ADDED_FINISHED = "com.huawei.android.lockSettingService.action.USER_ADDED_FINISHED ";
    private final Context mContext;
    private Pattern mPwdPattern;

    private native int nativeGetAllowedRetryCountByUserId(int i, int i2);

    static {
        loadLibraryFailed = false;
        try {
            if (ENHANCED_GK_RULE) {
                loadLibraryFailed = true;
            } else {
                System.loadLibrary("locksettings_jni");
            }
        } catch (UnsatisfiedLinkError e) {
            loadLibraryFailed = true;
            Slog.d(TAG, "liblocksettings_jni library not found!");
        }
        ACTION_NULL.addCategory("android.intent.category.HOME");
    }

    public HwLockSettingsService(Context context) {
        super(new HwInjector(context));
        this.mContext = context;
        this.mPwdPattern = Pattern.compile(PWD_REGEX);
    }

    static class HwInjector extends LockSettingsService.Injector {
        public HwInjector(Context context) {
            super(context);
        }

        public LockSettingsStrongAuth getStrongAuth() {
            return new HwLockSettingsStrongAuth(this.mContext);
        }

        public SyntheticPasswordManager getSyntheticPasswordManager(LockSettingsStorage storage) {
            return new HwSyntheticPasswordManager(getContext(), storage, getUserManager(), new PasswordSlotManager());
        }

        /* access modifiers changed from: package-private */
        public LockSettingsStorage newStorage() {
            return new HwLockSettingsStorage(this.mContext);
        }
    }

    private void setVisitorLockPassword(String password, int userId) throws RemoteException {
        checkWritePermission(userId);
        flog(TAG, "setVisitorLockPassword U" + userId);
        setKeystorePassword(password.getBytes(), userId);
    }

    public boolean checkVisitorPassword(String password, int userId) throws RemoteException {
        Slog.e(TAG, "checkVisitorPassword is deprecated");
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 1001) {
            data.enforceInterface(DESCRIPTOR);
            setVisitorLockPassword(data.readString(), data.readInt());
            reply.writeInt(0);
            reply.writeNoException();
            return true;
        } else if (code != 1002) {
            return HwLockSettingsService.super.onTransact(code, data, reply, flags);
        } else {
            data.enforceInterface(DESCRIPTOR);
            if (checkVisitorPassword(data.readString(), data.readInt())) {
                reply.writeInt(0);
            } else {
                reply.writeInt(1);
            }
            reply.writeNoException();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public int getOldCredentialType(int userId) {
        LockSettingsStorage.CredentialHash oldCredentialHash = this.mStorage.readCredentialHash(userId);
        if (oldCredentialHash != null) {
            return oldCredentialHash.type;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getPasswordStatus(int currentCredentialType, int oldCredentialType) {
        Slog.i(TAG, "getPasswordStatus, currentCredentialType=" + currentCredentialType + ", oldCredentialType=" + oldCredentialType);
        if (currentCredentialType == -1) {
            return 0;
        }
        if (oldCredentialType == -1) {
            return 1;
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public void notifyPasswordStatusChanged(int userId, int status) {
        Intent intent = new Intent(RECEIVER_ACTION_LOCK_PASSWORD_CHANGED);
        intent.setPackage(RECEIVER_PACKAGE);
        intent.putExtra("status", status);
        Slog.i(TAG, "notifyPasswordStatusChanged:" + status + ", userId:" + userId);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId), PERMISSION_GET_LOCK_PASSWORD_CHANGED);
        if (status != 0) {
            this.mStrongAuth.reportSuccessfulStrongAuthUnlock(userId);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyModifyPwdForPrivSpacePwdProtect(String credential, String savedCredential, int userId) {
        if (isPrivSpacePwdProtectOpened() && isPrivacyUser(this.mContext, userId)) {
            Slog.i(TAG, "notify privPw ");
            if (!PwdProtectManager.getInstance().modifyPrivPwd(credential)) {
                Log.e(TAG, "modifyPrivSpacePn fail");
            }
            if (UserHandle.myUserId() == 0) {
                HiEventEx eventEx = new HiEventEx((int) TYPE_PRIVSPACE_PWD_PROTECT_RESET_SUCCESS);
                eventEx.putAppInfo(this.mContext);
                HiViewEx.report(eventEx);
            }
        }
        if (isPrivSpacePwdProtectOpened() && userId == 0) {
            Slog.i(TAG, "notify mainPw ");
            if (TextUtils.isEmpty(savedCredential)) {
                savedCredential = NO_PWD_FOR_PWD_PROTECT;
            }
            if (TextUtils.isEmpty(credential)) {
                credential = NO_PWD_FOR_PWD_PROTECT;
            }
            if (!PwdProtectManager.getInstance().modifyMainPwd(savedCredential, credential)) {
                Log.e(TAG, "modifyMainSpacePn fail");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyBigDataForPwdProtectFail(int userId) {
        if (isPrivSpacePwdProtectOpened() && isPrivacyUser(this.mContext, userId) && UserHandle.myUserId() == 0) {
            HiEventEx eventEx = new HiEventEx((int) TYPE_PRIVSPACE_PWD_PROTECT_RESET_FAIL);
            eventEx.putAppInfo(this.mContext);
            HiViewEx.report(eventEx);
        }
    }

    private boolean isPrivacyUser(Context context, int userId) {
        if (context == null) {
            return false;
        }
        return UserManagerEx.isHwHiddenSpace(UserManagerEx.getUserInfoEx((UserManager) context.getSystemService("user"), userId));
    }

    private boolean isPrivSpacePwdProtectOpened() {
        Context context = this.mContext;
        if (context != null && Settings.Global.getInt(context.getContentResolver(), DB_KEY_PRIVACY_USER_PWD_PROTECT, 0) == 1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0119, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0121, code lost:
        throw new java.lang.RuntimeException("Failed to encrypt key", r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0119 A[ExcHandler: IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException (r0v11 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:4:0x00b7] */
    @VisibleForTesting
    private void saveUserHintMessage(int userId, String hint) throws RuntimeException, IOException {
        byte[] randomLockSeed = hint.getBytes(StandardCharsets.UTF_8);
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        try {
            keyStore.setEntry(PROFILE_KEY_USER_HINT_ENCRYPT + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(1).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
            keyStore.setEntry(PROFILE_KEY_USER_HINT_DECRYPT + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(2).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setCriticalToDeviceEncryption(true).build());
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, (SecretKey) keyStore.getKey(PROFILE_KEY_USER_HINT_ENCRYPT + userId, null));
            byte[] encryptionResult = cipher.doFinal(randomLockSeed);
            byte[] iv = cipher.getIV();
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    if (iv.length == 12) {
                        outputStream.write(iv);
                        outputStream.write(encryptionResult);
                        writeHwUserLockHint(userId, outputStream.toByteArray());
                        return;
                    }
                    throw new RuntimeException("Invalid iv length: " + iv.length);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to concatenate byte arrays", e);
                }
            } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
            }
        } finally {
            keyStore.deleteEntry(PROFILE_KEY_USER_HINT_ENCRYPT + userId);
        }
    }

    @VisibleForTesting
    private String getUserHintMessage(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CertificateException, IOException {
        byte[] storedData = readHwUserLockHint(userId);
        if (storedData != null) {
            byte[] iv = Arrays.copyOfRange(storedData, 0, 12);
            byte[] encryptedPassword = Arrays.copyOfRange(storedData, 12, storedData.length);
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, (SecretKey) keyStore.getKey(PROFILE_KEY_USER_HINT_DECRYPT + userId, null), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encryptedPassword), StandardCharsets.UTF_8);
        }
        throw new FileNotFoundException("Child profile lock file not found");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getHwUserLockHintFile(int userId) {
        return this.mStorage.getLockCredentialFilePathForUser(userId, USER_LOCK_HINT_FILE);
    }

    @VisibleForTesting
    private void writeHwUserLockHint(int userId, byte[] lock) {
        this.mStorage.writeFile(getHwUserLockHintFile(userId), lock);
    }

    private byte[] readHwUserLockHint(int userId) {
        return this.mStorage.readFile(getHwUserLockHintFile(userId));
    }

    private boolean hasHwUserLockHint(int userId) {
        return this.mStorage.hasFile(getHwUserLockHintFile(userId));
    }

    public void setString(String key, String value, int userId) {
        if (KEY_HW_LOCK_HINT.equals(key)) {
            checkWritePermission(userId);
            if ("com.android.settings".equals(getPackageNameFromPid(this.mContext, Binder.getCallingPid()))) {
                try {
                    if (TextUtils.isEmpty(value)) {
                        this.mStorage.deleteFile(getHwUserLockHintFile(userId));
                        Log.v(TAG, "delete UserHintMessage succ : " + key);
                        return;
                    }
                    saveUserHintMessage(userId, value);
                    Log.v(TAG, "save UserHintMessage succ : " + key);
                    return;
                } catch (IOException | RuntimeException e) {
                    Log.e(TAG, "save fail");
                }
            }
        }
        if (PWD_VERIFY_INFO.equals(key) && isUseGKRule(userId)) {
            resetTime(userId);
        } else if (!PWD_START_TIME_ELAPSED.equals(key) || !isUseGKRule(userId)) {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCKSETTINGS_SETSTRING);
            }
            HwLockSettingsService.super.setString(key, value, userId);
        } else {
            try {
                JSONObject obj = new JSONObject(value);
                setStartTime(userId, obj.getLong(JSON_START_RTC), obj.getLong(JSON_START_ELAPSED), obj.getLong(JSON_STOP_ELAPSED));
            } catch (JSONException e2) {
                Log.e(TAG, "setStartTime JSONException");
            }
        }
    }

    private boolean isCalledFromSysUI(int pid) {
        return "com.android.systemui".equals(getPackageNameFromPid(this.mContext, pid));
    }

    public long getLong(String key, long defaultValue, int userId) {
        if (!KEY_CREDENTIAL_LEN.equals(key) || isCalledFromSettings(Binder.getCallingPid())) {
            return HwLockSettingsService.super.getLong(key, defaultValue, userId);
        }
        return KEY_UNLOCK_TYPE_NOT_SET;
    }

    public void setLong(String key, long value, int userId) {
        if (KEY_CREDENTIAL_LEN.equals(key)) {
            Log.i(TAG, "invalid set credential from UID: " + Binder.getCallingUid());
        } else if (!KEY_UPDATE_WEAK_AUTH_TIME.equals(key)) {
            IHwBehaviorCollectManager manager = HwFrameworkFactory.getHwBehaviorCollectManager();
            if (manager != null) {
                manager.sendBehavior(IHwBehaviorCollectManager.BehaviorId.LOCKSETTINGS_SETLONG);
            }
            HwLockSettingsService.super.setLong(key, value, userId);
        } else if (isCalledFromSysUI(Binder.getCallingPid())) {
            reportSuccessfulWeakAuthUnlock((int) value, userId);
        }
    }

    /* access modifiers changed from: protected */
    public void setLockCredentialInternal(byte[] credential, int credentialType, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange, boolean isLockTiedToParent) throws RemoteException {
        try {
            HwLockSettingsService.super.setLockCredentialInternal(credential, credentialType, savedCredential, requestedQuality, userId, allowUntrustedChange, isLockTiedToParent);
            updateCredentialLength(credential, userId, 0);
            flog(TAG, "setLockCredentialInternal U" + userId + " quality:" + requestedQuality + " uid:" + Binder.getCallingUid() + " pid:" + Binder.getCallingPid() + getPackageNameFromPid(this.mContext, Binder.getCallingPid()));
        } catch (Exception e) {
            Log.e(TAG, "setLockCredentialInternal U" + userId + " have exception");
            flog(TAG, "setLockCredentialInternal U" + userId + " exception: " + Log.getStackTraceString(e));
            LockSettingsStorage lockSettingsStorage = this.mStorage;
            StringBuilder sb = new StringBuilder();
            sb.append("set lock credential error ");
            sb.append(Log.getStackTraceString(e));
            lockSettingsStorage.reportCriticalError((int) HwArbitrationDefs.MSG_CELL_STATE_DISCONNECT, sb.toString());
            throw e;
        }
    }

    private void reportSuccessfulWeakAuthUnlock(int type, int userId) {
        if ("com.android.systemui".equals(getPackageNameFromPid(this.mContext, Binder.getCallingPid()))) {
            if ((getStrongAuthForUser(userId) & 16) != 0) {
                Slog.e(TAG, "report WeakAuth but already timeout " + type);
            } else if (hasModifiedStrongAuthTime(userId)) {
                Slog.w(TAG, "report WeakAuth return, strongAuthTime is modified");
            } else if (type == 10001 || type == 10002) {
                this.mStrongAuth.reportSuccessfulWeakAuthUnlock(userId);
            }
        }
    }

    private boolean hasModifiedStrongAuthTime(int userId) {
        Context context = this.mContext;
        if (context == null || ((DevicePolicyManager) context.getSystemService("device_policy")).getRequiredStrongAuthTimeout(null, userId) == 259200000) {
            return false;
        }
        return true;
    }

    public String getString(String key, String defaultValue, int userId) {
        if (KEY_HW_LOCK_HINT.equals(key) && hasHwUserLockHint(userId)) {
            checkWritePermission(userId);
            String callProcessAppName = getPackageNameFromPid(this.mContext, Binder.getCallingPid());
            if ("com.android.systemui".equals(callProcessAppName) || "com.android.settings".equals(callProcessAppName)) {
                try {
                    String hintInfo = getUserHintMessage(userId);
                    return TextUtils.isEmpty(hintInfo) ? defaultValue : hintInfo;
                } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                    Log.e(TAG, "get UserHintMessage fail");
                }
            }
        }
        if (PWD_VERIFY_INFO.equals(key) && isUseGKRule(userId)) {
            return getVerifyInfo(userId).toString();
        }
        if (!PWD_ERROR_COUNT.equals(key) || !isUseGKRule(userId)) {
            return HwLockSettingsService.super.getString(key, defaultValue, userId);
        }
        return String.valueOf(getErrorCount(userId));
    }

    public boolean getBoolean(String key, boolean defaultValue, int userId) {
        checkWritePermission(userId);
        if (KEY_HAS_HW_LOCK_HINT.equals(key)) {
            return hasHwUserLockHint(userId);
        }
        return HwLockSettingsService.super.getBoolean(key, defaultValue, userId);
    }

    private static String getPackageNameFromPid(Context context, int pid) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        if (am == null) {
            Log.e(TAG, "can't get ACTIVITY_SERVICE");
            return "";
        }
        List<ActivityManager.RunningAppProcessInfo> acts = am.getRunningAppProcesses();
        if (acts == null) {
            Log.e(TAG, "can't get Running App");
            return "";
        }
        int len = acts.size();
        for (int i = 0; i < len; i++) {
            ActivityManager.RunningAppProcessInfo rapInfo = acts.get(i);
            if (rapInfo.pid == pid) {
                if (canUseGKRulePid(rapInfo.processName)) {
                    mUseGKRulePids.add(Integer.valueOf(pid));
                }
                return rapInfo.processName;
            }
        }
        return "";
    }

    private VerifyCredentialResponse verifyCredentialEx(int userId, LockSettingsStorage.CredentialHash storedHash, String credential, boolean hasChallenge, long challenge, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if ((storedHash == null || storedHash.hash.length == 0) && TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no stored Password/Pattern, verifyCredential success");
            return VerifyCredentialResponse.OK;
        } else if (storedHash == null || TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no entered Password/Pattern, verifyCredential ERROR");
            return VerifyCredentialResponse.ERROR;
        } else {
            StrictMode.noteDiskRead();
            try {
                if (getGateKeeperService() == null) {
                    return VerifyCredentialResponse.ERROR;
                }
                return convertResponse(getGateKeeperService().verifyChallenge(userId, challenge, storedHash.hash, credential.getBytes(StandardCharsets.UTF_8)));
            } catch (RemoteException e) {
                return VerifyCredentialResponse.ERROR;
            }
        }
    }

    private VerifyCredentialResponse doVerifyCredentialEx(String credential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        LockSettingsStorage.CredentialHash storedHash = this.mStorage.readCredentialHashEx(userId);
        if (storedHash != null && storedHash.hash != null && storedHash.hash.length != 0) {
            return verifyCredentialEx(userId, storedHash, credential, hasChallenge, challenge, progressCallback);
        }
        Slog.w(TAG, "no Pattern saved VerifyPattern success");
        return VerifyCredentialResponse.OK;
    }

    private boolean checkPasswordMatch(String password) {
        return this.mPwdPattern.matcher(password).matches();
    }

    private boolean checkPasswordEx(String password, int userId, ICheckCredentialProgressCallback progressCallback) {
        if (password == null || "".equals(password)) {
            return false;
        }
        try {
            if (doVerifyCredentialEx(password, 2, false, 0, userId, progressCallback).getResponseCode() == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void resetKeyStore(int userId) throws RemoteException {
        HwLockSettingsService.super.resetKeyStore(userId);
        flog(TAG, "resetKeyStore fin U" + userId);
    }

    private byte[] enrollCredentialEx(byte[] enrolledHandle, String enrolledCredential, String toEnroll, int userId) throws RemoteException {
        byte[] toEnrollBytes = null;
        byte[] enrolledCredentialBytes = enrolledCredential == null ? null : enrolledCredential.getBytes(StandardCharsets.UTF_8);
        if (toEnroll != null) {
            toEnrollBytes = toEnroll.getBytes(StandardCharsets.UTF_8);
        }
        if (getGateKeeperService() == null) {
            Slog.e(TAG, "getGateKeeperService fail.");
            return new byte[0];
        }
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, enrolledCredentialBytes, toEnrollBytes);
        if (response != null) {
            return response.getPayload();
        }
        Slog.w(TAG, "enrollCredential response null");
        return new byte[0];
    }

    private void checkPasswordReadPermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsRead");
    }

    private VerifyCredentialResponse convertResponse(GateKeeperResponse gateKeeperResponse) {
        return VerifyCredentialResponse.fromGateKeeperResponse(gateKeeperResponse);
    }

    public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) {
        checkWritePermission(userHandle);
        if (!checkPasswordMatch(password)) {
            Slog.i(TAG, "wrong unlock password");
            return false;
        }
        warnLog(TAG, "setExtendLockScreenPassword U" + userHandle + " UID:" + Binder.getCallingUid());
        byte[] bArr = new byte[0];
        try {
            byte[] enrolledHandle = enrollCredentialEx(this.mStorage.readCredentialHashEx(userHandle).hash, null, password, userHandle);
            if (enrolledHandle.length == 0) {
                return false;
            }
            this.mStorage.writeCredentialHashEx(LockSettingsStorage.CredentialHash.create(enrolledHandle, 2), userHandle);
            Intent intent = new Intent("com.huawei.intent.action.OPERATOR_REMOTE_LOCK");
            intent.setPackage("com.android.systemui");
            intent.putExtra("PhoneNumber", phoneNumber);
            this.mContext.sendBroadcast(intent);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean clearExtendLockScreenPassword(String password, int userHandle) {
        checkPasswordReadPermission(userHandle);
        warnLog(TAG, "clearExtendLockScreenPassword U" + userHandle + " UID:" + Binder.getCallingUid());
        if (!this.mStorage.hasSetPassword(userHandle)) {
            Slog.i(TAG, "has not set password");
            return false;
        } else if (!checkPasswordMatch(password)) {
            Slog.i(TAG, "cannot match password");
            return false;
        } else if (checkPasswordEx(password, userHandle, null)) {
            this.mStorage.deleteExPasswordFile(userHandle);
            Intent intent = new Intent("com.huawei.intent.action.OPERATOR_REMOTE_UNLOCK");
            intent.setPackage("com.android.systemui");
            this.mContext.sendBroadcast(intent);
            return true;
        } else {
            Slog.e(TAG, "wrong unlock password");
            return false;
        }
    }

    public void setLockCredential(byte[] credential, int type, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange) throws RemoteException {
        try {
            if (!IS_TV) {
                clearPasswordType(userId);
                warnLog(TAG, "SetLockCredential U" + userId + " T" + type + " UID:" + Binder.getCallingUid());
                HwLockSettingsService.super.setLockCredential(credential, type, savedCredential, requestedQuality, userId, allowUntrustedChange);
                setSecurityCredential(credential, type, userId, null);
                this.mStrongAuth.updateSetCredentialTime(userId);
                if (credential != null) {
                    if (savedCredential != null) {
                        notifyModifyPwdForPrivSpacePwdProtect(new String(credential, StandardCharsets.UTF_8), new String(savedCredential, StandardCharsets.UTF_8), userId);
                        warnLog(TAG, "SetLockCredential U" + userId + " T" + type + " success.");
                        return;
                    }
                }
                warnLog(TAG, "SetLockCredential U" + userId + " Illegal parameters.");
            }
        } catch (Exception e) {
            Log.e(TAG, "SetLockCredential U" + userId + " have exception");
            flog(TAG, "SetLockCredential U" + userId + " exception: " + Log.getStackTraceString(e));
            throw e;
        }
    }

    private void setSecurityCredential(byte[] credential, int type, int userId, VerifyCredentialResponse response) {
        updateRemotePassword(credential, type, userId, response);
        try {
            setCredentialForHwKeystore(userId, credential);
        } catch (NoExtAPIException e) {
            Log.e(TAG, "HwKeystoreManager: setCredentialForHwKeystore NoExtAPIException");
        } catch (Exception e2) {
            Log.e(TAG, "HwKeystoreManager: setCredentialForHwKeystore unknown exception");
        }
    }

    private void checkSecurityCredential(byte[] credential, int type, int userId, VerifyCredentialResponse response) {
        updateRemotePassword(credential, type, userId, response);
        try {
            checkCredentialForHwKeystore(userId, credential, response);
        } catch (NoExtAPIException e) {
            Log.e(TAG, "HwKeystoreManager: checkCredentialForHwKeystore NoExtAPIException");
        } catch (Exception e2) {
            Log.e(TAG, "HwKeystoreManager: checkCredentialForHwKeystore unknown exception");
        }
    }

    private void checkCredentialForHwKeystore(int userId, byte[] credential, VerifyCredentialResponse response) {
        if (!userSets.contains(Integer.valueOf(userId))) {
            if (response == null || response.getResponseCode() != 0) {
                Log.e(TAG, "checkCredentialForHwKeystore credential is invalid.");
                return;
            }
            userSets.add(Integer.valueOf(userId));
            Log.i(TAG, "checkCredentialForHwKeystore: user id " + userId + " first unlocks.");
            HwKeystoreManager hwKeystore = HwKeystoreManager.getInstance();
            if (hwKeystore == null) {
                Log.e(TAG, "checkCredentialForHwKeystore, hwKeystore is null");
                return;
            }
            try {
                hwKeystore.unlock(userId, new String(credential, StandardCharsets.ISO_8859_1));
            } catch (NoExtAPIException e) {
                Log.e(TAG, "HwKeystoreManager: unlock NoExtAPIException");
            } catch (Exception e2) {
                Log.e(TAG, "HwKeystoreManager: unlock unknown exception");
            }
        }
    }

    private void setCredentialForHwKeystore(int userId, byte[] credential) {
        HwKeystoreManager hwKeystore = HwKeystoreManager.getInstance();
        if (hwKeystore == null) {
            Log.e(TAG, "setCredentialForHwKeystore, hwKeystore is null");
            return;
        }
        try {
            hwKeystore.onUserCredentialChanged(userId, credential == null ? null : new String(credential, StandardCharsets.ISO_8859_1));
        } catch (NoExtAPIException e) {
            Log.e(TAG, "HwKeystoreManager: onUserCredentialChanged NoExtAPIException");
        } catch (Exception e2) {
            Log.e(TAG, "HwKeystoreManager: onUserCredentialChanged unknown exception");
        }
    }

    public void systemReady() {
        HwLockSettingsService.super.systemReady();
        initRemotePassword();
    }

    private void updateRemotePassword(byte[] credential, int type, int userId, VerifyCredentialResponse response) {
        boolean isForceUpate;
        if (userId == 0) {
            if (response == null) {
                isForceUpate = true;
            } else if (response.getResponseCode() == 0) {
                isForceUpate = false;
            } else {
                return;
            }
            try {
                updateRemotePasswordInner(credential, type, userId, isForceUpate);
            } catch (NoExtAPIException e) {
                Log.e(TAG, "RemotePin: update NoExtAPIException");
            } catch (Exception e2) {
                Log.e(TAG, "RemotePin: update unknown exception");
            }
        }
    }

    private void updateRemotePasswordInner(byte[] credential, int type, int userId, boolean isForceUpate) {
        RemotePasswordManager remotePin = RemotePasswordManager.getInstance(this.mContext);
        if (remotePin != null) {
            remotePin.updateRemotePassword(credential, type, userId, isForceUpate, new RemotePasswordManager.IVerifyCallback() {
                /* class com.android.server.locksettings.HwLockSettingsService.AnonymousClass1 */

                public byte[] onVerifyCredential(byte[] lockCredential, int credentialType, long challenge, int userId) {
                    try {
                        VerifyCredentialResponse response = HwLockSettingsService.this.verifyCredential(lockCredential, credentialType, challenge, userId);
                        if (response != null && response.getResponseCode() == 0) {
                            return response.getPayload();
                        }
                    } catch (RemoteException e) {
                        Log.e(HwLockSettingsService.TAG, "RemotePin: verify remote exception");
                    } catch (Exception e2) {
                        Log.e(HwLockSettingsService.TAG, "RemotePin: verify unknown exception");
                    }
                    return new byte[0];
                }
            });
        }
    }

    private void initRemotePassword() {
        try {
            RemotePasswordManager remotePin = RemotePasswordManager.getInstance(this.mContext);
            if (remotePin != null) {
                remotePin.prepare();
            }
        } catch (NoExtAPIException e) {
            Log.e(TAG, "RemotePin: init NoExtAPIException");
        } catch (Exception e2) {
            Log.e(TAG, "RemotePin: init unknown exception");
        }
    }

    public boolean setLockCredentialWithToken(byte[] credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) throws RemoteException {
        int oldCredentialType = getOldCredentialType(userId);
        clearPasswordType(userId);
        warnLog(TAG, "setLockCredentialWithToken U" + userId + " T" + type + " UID:" + Binder.getCallingUid());
        boolean result = HwLockSettingsService.super.setLockCredentialWithToken(credential, type, tokenHandle, token, requestedQuality, userId);
        if (result) {
            notifyPasswordStatusChanged(userId, getPasswordStatus(type, oldCredentialType));
            updateCredentialLength(credential, userId, 0);
            Slog.w(TAG, "setLockCredentialWithToken succ");
        }
        return result;
    }

    private void clearPasswordType(int userId) throws RemoteException {
        HwLockSettingsService.super.setLong(KEY_HW_PIN_TYPE, (long) KEY_UNLOCK_TYPE_NOT_SET, userId);
    }

    private int getErrorCount(int userId) {
        return Integer.parseInt(this.mStorage.readKeyValue(JSON_ERROR_COUNT, "0", userId));
    }

    private void addErrorCount(int userId) {
        int errorCount = getErrorCount(userId) + 1;
        Slog.w(NRLTAG, "locksettingsservice addErrorCount=" + errorCount + ", userId " + userId);
        this.mStorage.writeKeyValue(JSON_ERROR_COUNT, String.valueOf(errorCount), userId);
    }

    private void resetErrorCount(int userId) {
        Slog.w(NRLTAG, "locksettingsservice resetErrorCount, userId" + userId);
        this.mStorage.writeKeyValue(JSON_ERROR_COUNT, String.valueOf(0), userId);
        resetTime(userId);
    }

    private void setStartTime(int userId, long startTimeRTC, long startTimeElapsed, long stopTimeInFuture) {
        if (startTimeElapsed >= SystemClock.elapsedRealtime() || stopTimeInFuture > SystemClock.elapsedRealtime()) {
            this.mStorage.writeKeyValue(JSON_START_RTC, String.valueOf(startTimeRTC), userId);
            this.mStorage.writeKeyValue(JSON_START_ELAPSED, String.valueOf(startTimeElapsed), userId);
        }
    }

    private void resetTime(int userId) {
        this.mStorage.writeKeyValue(JSON_START_RTC, String.valueOf(0), userId);
        this.mStorage.writeKeyValue(JSON_START_ELAPSED, String.valueOf(0), userId);
    }

    private long getStartTimeRTC(int userId) {
        return Long.parseLong(this.mStorage.readKeyValue(JSON_START_RTC, "0", userId));
    }

    private long getStartTimeElapsed(int userId) {
        return Long.parseLong(this.mStorage.readKeyValue(JSON_START_ELAPSED, "0", userId));
    }

    private synchronized JSONObject getVerifyInfo(int userId) {
        JSONObject obj;
        obj = new JSONObject();
        try {
            obj.put(JSON_LEVEL, 2);
            obj.put(JSON_ERROR_COUNT, getErrorCount(userId));
            obj.put(JSON_START_RTC, getStartTimeRTC(userId));
            obj.put(JSON_START_ELAPSED, getStartTimeElapsed(userId));
        } catch (JSONException e) {
            Slog.e(TAG, "toJson error", e);
        }
        return obj;
    }

    /* access modifiers changed from: protected */
    public void setKeystorePassword(byte[] password, int userHandle) {
        HwLockSettingsService.super.setKeystorePassword(password, userHandle);
        warnLog(TAG, "setKeystorePassword fin.U" + userHandle);
    }

    private boolean isCalledFromSettings(int pid) {
        if (mUseGKRulePids.contains(Integer.valueOf(pid))) {
            return true;
        }
        return canUseGKRulePid(getPackageNameFromPid(this.mContext, pid));
    }

    private static boolean canUseGKRulePid(String name) {
        return "com.android.settings".equals(name) || "com.huawei.android.hwouc".equals(name) || "com.android.systemui".equals(name) || PKG_SECURITYMGR.equals(name) || PKG_PRIVATE_SPACE.equals(name) || "com.huawei.systemmanager".equals(name);
    }

    public VerifyCredentialResponse checkCredential(byte[] credential, int type, final int userId, final ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        ICheckCredentialProgressCallback callback;
        if (!isUseGKRule(userId)) {
            callback = HwLockSettingsService.super.checkCredential(credential, type, userId, progressCallback);
        } else {
            ICheckCredentialProgressCallback response = HwLockSettingsService.super.checkCredential(credential, type, userId, new ICheckCredentialProgressCallback.Stub() {
                /* class com.android.server.locksettings.HwLockSettingsService.AnonymousClass2 */

                public void onCredentialVerified() throws RemoteException {
                    ICheckCredentialProgressCallback iCheckCredentialProgressCallback = progressCallback;
                    if (iCheckCredentialProgressCallback != null) {
                        iCheckCredentialProgressCallback.onCredentialVerified();
                    }
                    HwLockSettingsService.this.checkError(0, userId, 0);
                }
            });
            checkError(response.getResponseCode(), userId, response.getTimeout());
            callback = response;
        }
        warnLog(TAG, "checkCredential U" + userId + " R" + callback.getResponseCode() + " type:" + type + " uid:" + Binder.getCallingUid() + " pid:" + Binder.getCallingPid());
        updateCredentialLength(credential, userId, callback.getResponseCode());
        updateLockedTimeAndRetryCount(userId, callback);
        checkSecurityCredential(credential, type, userId, callback);
        return callback;
    }

    public VerifyCredentialResponse verifyCredential(byte[] credential, int type, long challenge, int userId) throws RemoteException {
        if (credential == null || credential.length == 0) {
            this.mLockPatternUtils.monitorCheckPassword(1002, (Exception) null);
        }
        VerifyCredentialResponse response = HwLockSettingsService.super.verifyCredential(credential, type, challenge, userId);
        if (isUseGKRule(userId)) {
            checkError(response.getResponseCode(), userId, response.getTimeout());
        }
        warnLog(TAG, "verifyCredential U" + userId + " R" + response.getResponseCode() + " type:" + type + " uid:" + Binder.getCallingUid() + " pid:" + Binder.getCallingPid());
        updateCredentialLength(credential, userId, response.getResponseCode());
        updateLockedTimeAndRetryCount(userId, response);
        return response;
    }

    private void updateCredentialLength(byte[] credential, int userId, int responseCode) {
        if (userId >= 0 && responseCode == 0) {
            long storedLen = HwLockSettingsService.super.getLong(KEY_CREDENTIAL_LEN, 0, userId);
            long len = (long) (credential == null ? 0 : credential.length);
            Slog.i(TAG, "updateCredentialLength userId:" + userId + " storedLen " + storedLen + " len " + len);
            if (storedLen != len) {
                HwLockSettingsService.super.setLong(KEY_CREDENTIAL_LEN, len, userId);
                flog(TAG, "updateCredentialLength U" + userId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkError(int responseCode, int userId, int timeOut) {
        warnLog(TAG, "checkError rCode:" + responseCode + " U" + userId + " " + timeOut);
        if (responseCode == -1) {
            addErrorCount(userId);
        } else if (responseCode == 0) {
            resetErrorCount(userId);
            resetStrongAuth(userId);
        } else if (responseCode == 1) {
            long startTimeElapsed = SystemClock.elapsedRealtime();
            setStartTime(userId, System.currentTimeMillis(), startTimeElapsed, startTimeElapsed + ((long) timeOut));
            addErrorCount(userId);
        }
    }

    private void resetStrongAuth(int userId) {
        if ((getStrongAuthForUser(userId) & 8) != 0) {
            Slog.w(NRLTAG, "clear AFTER_LOCKOUT_AUTH flag after verifyCredential");
            requireStrongAuth(0, userId);
        }
    }

    private boolean isUseGKRule(int userId) {
        if (!ENHANCED_GK_RULE || !isCalledFromSettings(Binder.getCallingPid())) {
            return false;
        }
        checkWritePermission(userId);
        return true;
    }

    /* access modifiers changed from: protected */
    public void onUserAdded(int userHandle) {
        UserInfo userInfo = this.mUserManager.getUserInfo(userHandle);
        if (userInfo != null && userInfo.isHwHiddenSpace()) {
            Intent finishIntent = new Intent("com.huawei.android.lockSettingService.action.USER_ADDED_FINISHED ");
            finishIntent.putExtra("android.intent.extra.user_handle", userHandle);
            Slog.d(TAG, "notify that hiden user has been added.");
            this.mContext.sendBroadcastAsUser(finishIntent, UserHandle.ALL, "android.permission.MANAGE_USERS");
        }
    }

    /* access modifiers changed from: protected */
    public void addSDCardUserKeyAuth(int userId, UserInfo userInfo, byte[] token, byte[] secret) {
        ISDCardCryptedHelper helper = HwServiceFactory.getSDCardCryptedHelper();
        if (helper != null) {
            helper.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
        }
    }

    /* access modifiers changed from: protected */
    public void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        if (userId != 2147483646) {
            HwLockSettingsService.super.fixateNewestUserKeyAuth(userId);
        }
    }

    /* access modifiers changed from: protected */
    public void showEncryptionNotificationForUsers() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = users.get(i);
            if (!user.isClonedProfile()) {
                UserHandle userHandle = user.getUserHandle();
                Slog.i(TAG, "user.id = " + user.id);
                if (isUserSecure(user.id) && !this.mUserManager.isUserUnlockingOrUnlocked(userHandle) && !user.isManagedProfile()) {
                    Slog.i(TAG, "has password,show notification");
                    showEncryptionNotificationForUser(userHandle);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void showEncryptionNotificationForUser(UserHandle user) {
        Resources resource = this.mContext.getResources();
        showEncryptionNotification(user, resource.getText(33685896), resource.getText(33685897), resource.getText(17041441), PendingIntent.getBroadcast(this.mContext, 0, ACTION_NULL, 134217728));
    }

    /* access modifiers changed from: protected */
    public SyntheticPasswordManager.AuthenticationToken initializeSyntheticPasswordLocked(byte[] credentialHash, byte[] credential, int credentialType, int requestedQuality, int userId) throws RemoteException {
        if (checkHasBlobFile(userId)) {
            this.mStorage.reportCriticalError((int) HwArbitrationDefs.MSG_CELL_STATE_DISCONNECT, "update when blob exists");
            flog(TAG, "Initialize SyntheticPassword has unfinish op. U" + userId);
        } else {
            flog(TAG, "Initialize SyntheticPassword U" + userId);
        }
        return HwLockSettingsService.super.initializeSyntheticPasswordLocked(credentialHash, credential, credentialType, requestedQuality, userId);
    }

    private boolean checkHasBlobFile(int userId) {
        File[] filesList;
        File dir = new File("/data/system_de/" + userId + "spblob");
        if (!dir.exists() || !dir.isDirectory() || (filesList = dir.listFiles()) == null) {
            return false;
        }
        for (File file : filesList) {
            if (file.isFile() && file.getAbsolutePath().endsWith(".secdis")) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void unlockKeystore(byte[] password, int userHandle) {
        HwLockSettingsService.super.unlockKeystore(password, userHandle);
        warnLog(TAG, "unlockKeystore, U" + userHandle);
    }

    /* access modifiers changed from: package-private */
    public void warnLog(String tag, String msg) {
        Slog.w(tag, msg);
        this.mStorage.flog(tag, msg);
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
        this.mStorage.flog(tag, msg);
    }

    /* access modifiers changed from: protected */
    public void updateLockedTimeAndRetryCount(final int userId, VerifyCredentialResponse response) {
        if (response == null || userId < 0) {
            Slog.w(TAG, "updateRemainAllowRetryCount response is null userId " + userId);
            return;
        }
        updateRemainCountMaps(userId, response.getResponseCode());
        updateRemainLockedTime(userId, (long) response.getTimeout());
        this.mHandler.post(new Runnable() {
            /* class com.android.server.locksettings.HwLockSettingsService.AnonymousClass3 */

            @Override // java.lang.Runnable
            public void run() {
                HwLockSettingsService.this.updateRemainAllowRetryCount(userId);
            }
        });
    }

    private void updateRemainCountMaps(int userId, int responseCode) {
        int remainCount;
        Integer remainCount2 = remainAllowedCount.get(Integer.valueOf(userId));
        if (remainCount2 == null) {
            remainCount2 = Integer.valueOf((int) HwLockSettingsService.super.getLong(REMAIN_ALLOW_RETRY_COUNT, 100, userId));
        }
        if (responseCode == -1) {
            remainCount = Integer.valueOf(remainCount2.intValue() - 1);
        } else if (responseCode == 0) {
            remainCount = 100;
        } else {
            remainCount = 0;
        }
        remainAllowedCount.put(Integer.valueOf(userId), remainCount);
    }

    private boolean isUsedWeaver(int userId) {
        return HwLockSettingsService.super.getLong(STRONG_AUTH_SOLUTION_WEAVER_FLAG, 0, userId) > 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRemainAllowRetryCount(int userId) {
        int newRemainCount = 100;
        try {
            if (isUsedWeaver(userId)) {
                HwSyntheticPasswordManager.WeaverLockedStatus status = this.mSpManager.getWeaverLockedStatus(getSyntheticPasswordHandleLocked(userId), userId);
                Slog.i(TAG, "updateRemainAllowRetryCount from weaver success:" + status.isSuccess);
                if (status.isSuccess) {
                    updateRemainLockedTime(userId, status.timeout);
                    newRemainCount = status.remainCount;
                }
            } else if (!loadLibraryFailed) {
                int tempCount = nativeGetAllowedRetryCountByUserId(userId, SyntheticPasswordManager.fakeUid(userId));
                Slog.i(TAG, "updateRemainAllowRetryCount from jni tempCount " + tempCount);
                newRemainCount = tempCount < 0 ? 100 : tempCount;
            } else {
                Slog.i(TAG, "updateRemainAllowRetryCount load library failed. " + loadLibraryFailed);
            }
        } catch (Exception e) {
            Slog.i(TAG, "updateRemainAllowRetryCount error.");
        }
        remainAllowedCount.put(Integer.valueOf(userId), Integer.valueOf(newRemainCount));
        long storedRemainCount = HwLockSettingsService.super.getLong(REMAIN_ALLOW_RETRY_COUNT, 100, userId);
        if (storedRemainCount != ((long) newRemainCount)) {
            HwLockSettingsService.super.setLong(REMAIN_ALLOW_RETRY_COUNT, (long) newRemainCount, userId);
        }
        Slog.i(TAG, "updateRemainAllowCount userId:" + userId + " new " + newRemainCount + " " + storedRemainCount);
    }

    private void updateRemainLockedTime(int userId, long lockedTime) {
        if (lockedTime != HwLockSettingsService.super.getLong(REMAIN_LOCKED_TIME, 0, userId)) {
            HwLockSettingsService.super.setLong(REMAIN_LOCKED_TIME, lockedTime, userId);
        }
        if (lockedTime == 0) {
            Slog.i(TAG, "updateRemainLockedTime userId:" + userId + " lockedTime 0");
            return;
        }
        long startTimeRTC = System.currentTimeMillis();
        HwLockSettingsService.super.setLong(REMAIN_LOCKED_TIME_START_RTC, startTimeRTC, userId);
        long startTimeElapsed = SystemClock.elapsedRealtime();
        HwLockSettingsService.super.setLong(REMAIN_LOCKED_TIME_START_ELAPSED, startTimeElapsed, userId);
        Slog.i(TAG, "updateRemainLockedTime userId:" + userId + " lockedTime " + lockedTime + " startTimeRTC " + startTimeRTC + " startTimeElapsed " + startTimeElapsed);
    }

    /* JADX INFO: Multiple debug info for r14v1 long: [D('time' long), D('startTimeRTC' long)] */
    /* access modifiers changed from: protected */
    public void updateRemainLockedTimeAfterReboot() {
        List<UserInfo> infos = this.mUserManager.getUsers(true);
        Slog.i(TAG, "updateRemainLockedTimeAfterReboot");
        if (infos == null) {
            Slog.w(TAG, "updateRemainLockedTimeAfterReboot error.");
            return;
        }
        for (UserInfo info : infos) {
            long storedLockedTime = getLong(REMAIN_LOCKED_TIME, 0, info.id);
            if (storedLockedTime > 0) {
                if (info.id >= 0) {
                    long currTimeElapsed = SystemClock.elapsedRealtime();
                    setLong(REMAIN_LOCKED_TIME_START_ELAPSED, currTimeElapsed, info.id);
                    long startTimeRTC = getLong(REMAIN_LOCKED_TIME_START_RTC, 0, info.id);
                    long currTimeRTC = System.currentTimeMillis();
                    Slog.i(TAG, "updateRemainLockedTime currRTC " + currTimeRTC + " startRTC " + startTimeRTC);
                    if (currTimeRTC <= startTimeRTC) {
                        infos = infos;
                    } else {
                        long startTimeRTC2 = storedLockedTime - (currTimeRTC - startTimeRTC);
                        long remainLockedTime = 0;
                        if (startTimeRTC2 > 0) {
                            remainLockedTime = startTimeRTC2;
                        }
                        setLong(REMAIN_LOCKED_TIME, remainLockedTime, info.id);
                        setLong(REMAIN_LOCKED_TIME_START_RTC, currTimeRTC, info.id);
                        Slog.i(TAG, "updateRemainLockedTimeAfterReboot userId " + info.id + " currTimeRTC " + currTimeRTC + " Elapsed currTimeElapsed " + currTimeElapsed + " time " + startTimeRTC2);
                        infos = infos;
                    }
                }
            }
        }
    }

    public int getRemainAllowedRetryCount(int userId) {
        Integer count = remainAllowedCount.get(Integer.valueOf(userId));
        if (count == null) {
            count = Integer.valueOf((int) HwLockSettingsService.super.getLong(REMAIN_ALLOW_RETRY_COUNT, 100, userId));
            remainAllowedCount.put(Integer.valueOf(userId), count);
            Slog.i(TAG, "getRemainAllowedRetryCount userId:" + userId + " from storage");
        }
        if (count.intValue() <= 0 && getRemainLockedTime(userId) <= 0) {
            Slog.i(TAG, "getRemainAllowedRetryCount userId:" + userId + " count is 0, time is 0");
            count = 1;
            remainAllowedCount.put(Integer.valueOf(userId), count);
        }
        letPwdBackendPrepareWork(userId);
        Slog.i(TAG, "getRemainAllowedRetryCount userId:" + userId + " count " + count);
        return count.intValue();
    }

    public long getRemainLockedTime(int userId) {
        long storedLockedTime = HwLockSettingsService.super.getLong(REMAIN_LOCKED_TIME, 0, userId);
        if (storedLockedTime <= 0) {
            Slog.i(TAG, "getRemainLockedTime ret 0 userId:" + userId);
            return 0;
        }
        long startTimeElapsed = HwLockSettingsService.super.getLong(REMAIN_LOCKED_TIME_START_ELAPSED, 0, userId);
        long currTimeElapsed = SystemClock.elapsedRealtime();
        long time = storedLockedTime;
        Slog.i(TAG, "getRemainLockedTime currTimeElapsed:" + currTimeElapsed + " startTimeElapsed " + startTimeElapsed);
        if (currTimeElapsed > startTimeElapsed) {
            time = storedLockedTime - (currTimeElapsed - startTimeElapsed);
        }
        Slog.i(TAG, "getRemainLockedTime uid:" + userId + " time " + time + "ms storedTime " + storedLockedTime);
        if (time <= 0) {
            return 0;
        }
        return time;
    }

    public int getStrongAuthSolution(int userId) {
        long weaverFlag = HwLockSettingsService.super.getLong(STRONG_AUTH_SOLUTION_WEAVER_FLAG, 0, userId);
        Slog.i(TAG, "getStrongAuthSolution userId:" + userId + " weaverFlag " + weaverFlag);
        if (weaverFlag == WEAVER_VERSION_V1) {
            return 1;
        }
        long flag = HwLockSettingsService.super.getLong(STRONG_AUTH_SOLUTION_FLAG, 0, userId);
        Slog.i(TAG, "getStrongAuthSolution userId:" + userId + " flag " + flag);
        if (flag == TEE_GATEKEEPER_VERSION_V5) {
            return 1;
        }
        return 0;
    }

    public int getPasswordBackendStatus() {
        if (this.mSpManager instanceof HwSyntheticPasswordManager) {
            return this.mSpManager.getPasswordBackendStatus();
        }
        return 10;
    }

    private void letPwdBackendPrepareWork(final int userId) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.locksettings.HwLockSettingsService.AnonymousClass4 */

            @Override // java.lang.Runnable
            public void run() {
                long handle = HwLockSettingsService.this.getSyntheticPasswordHandleLocked(userId);
                if (HwLockSettingsService.this.mSpManager instanceof HwSyntheticPasswordManager) {
                    try {
                        HwLockSettingsService.this.mSpManager.weaverPrepareWork(handle, userId);
                    } catch (RemoteException e) {
                        Slog.w(HwLockSettingsService.TAG, "letPwdBackendPrepareWork error.");
                    }
                }
            }
        });
    }
}
