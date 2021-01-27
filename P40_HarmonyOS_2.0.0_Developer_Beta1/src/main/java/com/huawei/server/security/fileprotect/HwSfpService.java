package com.huawei.server.security.fileprotect;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.content.ContextEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.InstalldEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.server.security.core.IHwSecurityPlugin;
import com.huawei.util.LogEx;
import huawei.android.security.IHwLockStateChangeCallback;
import huawei.android.security.IHwSfpService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwSfpService extends IHwSfpService.Stub implements IHwSecurityPlugin {
    private static final String ACTION_LOCK_SCREEN = "lockScreen";
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        /* class com.huawei.server.security.fileprotect.HwSfpService.AnonymousClass1 */

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public IHwSecurityPlugin createPlugin(Context context) {
            if (HwSfpService.sInstance == null) {
                HwSfpService unused = HwSfpService.sInstance = new HwSfpService(context);
            }
            return HwSfpService.sInstance;
        }

        @Override // com.huawei.server.security.core.IHwSecurityPlugin.Creator
        public String getPluginPermission() {
            return null;
        }
    };
    private static final int DEFAULT_ARG = 0;
    private static final int DEFAULT_CAPACITY = 4;
    private static final int DEFAULT_LIST_SIZE = 4;
    private static final String DEFAULT_USER_ID = "0";
    private static final long DELAY_DELETE = 10000;
    private static final String DELETE_CLASS_KEY = "DELETE_CLASS_KEY";
    private static final String EMPTY_STRING = "";
    private static final int ERROR_CODE_FAILED = -1;
    private static final int ERROR_CODE_INVALID_PARAM = -3;
    private static final int ERROR_CODE_LISTENER_NOT_REGISTERED = -6;
    private static final int ERROR_CODE_LISTENER_REGISTERED = -5;
    private static final int ERROR_CODE_OK = 0;
    private static final int ERROR_CODE_REMOTE_ERROR = -4;
    private static final int ERROR_CODE_SERVICE_NOT_FOUND = -2;
    private static final int EVT_LOCK_SCREEN = 1000;
    private static final int EVT_RELAY_DELETE_CLASS_KEY_TIME_OUT = 1001;
    private static final int FIRST_INDEX = 0;
    private static final int FLAG_DEATH_RECIPIENT = 0;
    private static final int FLAG_LOCAL_STATE = 1;
    private static final String INTENT_SLIDE_UNLOCK = "slideUnlock";
    private static final String INTENT_SMART_UNLOCK = "smartUnlock";
    private static final String INTENT_USER_ID = "userId";
    private static final int INVALID_USER_ID = -1;
    private static final boolean IS_DEBUG = (SystemPropertiesEx.get("ro.secure", "1").equals(DEFAULT_USER_ID) || LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final boolean IS_SUPPORT_SDP = SystemPropertiesEx.getBoolean("ro.config.support_iudf", false);
    private static final String KEY_DESC_PREFIX_ERR = "V1[keydesc_ERR:";
    private static final Object LOCK = new Object();
    private static final String LOCK_PERMISSION = "com.isec.lockScreenBroadcast";
    private static final int LOCK_STATE_ERROR = -1;
    private static final int LOCK_STATE_LOCKED = 1;
    private static final int LOCK_STATE_UNLOCKED = 0;
    private static final String[] MULTIPLE_USER_PATH = {"/data/user/0/", "/storage/emulated/0/"};
    private static final String PERMISSION_GET_SCREEN_LOCK_STATE = "com.huawei.fileprotect.sfpservice.permissions.GET_SCREEN_LOCK_STATE";
    private static final String POLICY_CONFIG_FILE = "sfpconfig.json";
    private static final int PRELOAD_STATUS = 1;
    private static final String SPLIT_LABEL = ":";
    private static final String TAG = "HwSfpService";
    private static HwSfpService sInstance;
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private final Map<Integer, AlarmListener> mListenerMap;
    private final List<Integer> mLockScreenUserIdList;
    private final Map<SfpCallbackProxyKey, SfpCallbackProxy> mLockStateChangeCallbackMap;
    private final Map<Integer, Integer> mLockStateMap;
    private List<PackStoragePolicy> mPackagePolicies;

    private HwSfpService(Context context) {
        this.mLockStateMap = new HashMap(4);
        this.mLockStateChangeCallbackMap = new HashMap(4);
        this.mLockScreenUserIdList = new ArrayList(4);
        this.mListenerMap = new HashMap(4);
        this.mPackagePolicies = new ArrayList(4);
        this.mContext = context;
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new MyHandler(this.mHandlerThread.getLooper());
    }

    public static HwSfpService getInstance() {
        return sInstance;
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStart() {
        Log.i(TAG, "onStart");
        this.mPackagePolicies = PackStoragePolicy.parse(this.mContext, POLICY_CONFIG_FILE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOCK_SCREEN);
        ContextEx.registerReceiverAsUser(this.mContext, new LockScreenReceiver(), UserHandleEx.ALL, filter, LOCK_PERMISSION, (Handler) null);
    }

    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public void onStop() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: com.huawei.server.security.fileprotect.HwSfpService */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.huawei.server.security.core.IHwSecurityPlugin
    public IBinder asBinder() {
        return this;
    }

    public String getKeyDesc(int userId, int storageType) {
        String result = null;
        long token = Binder.clearCallingIdentity();
        try {
            UserInfoExt userInfo = getUserInfo(userId);
            if (userInfo != null) {
                result = getKeyDescInner(userId, userInfo.getSerialNumber(), storageType);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public List<String> getSensitiveDataPolicyList() {
        List<String> result = new ArrayList<>(4);
        if (!IS_SUPPORT_SDP) {
            return result;
        }
        for (PackStoragePolicy packPolicy : this.mPackagePolicies) {
            result.add(packPolicy.getPackageName());
        }
        return result;
    }

    public void notifyUnlockScreen(int userId) {
        if (IS_DEBUG) {
            Log.i(TAG, "notifyUnlockScreen: user id is " + userId);
        }
        setIsLocked(false, userId);
        synchronized (LOCK) {
            AlarmListener listener = this.mListenerMap.getOrDefault(Integer.valueOf(userId), null);
            if (listener != null) {
                cancelTimer(listener);
                this.mLockScreenUserIdList.remove(Integer.valueOf(userId));
                this.mListenerMap.remove(Integer.valueOf(userId));
            } else {
                Log.w(TAG, "notifyUnlockScreen: the AlarmListener is null!");
            }
        }
    }

    public void execPolicies(int userId, int serialNumber) {
        Map<Integer, String> keyDescMap = new HashMap<>(4);
        String eceKeyDesc = getKeyDescInner(userId, serialNumber, 2);
        String seceKeyDesc = getKeyDescInner(userId, serialNumber, 3);
        if (TextUtils.isEmpty(eceKeyDesc) || TextUtils.isEmpty(seceKeyDesc)) {
            Log.d(TAG, "cannot get the eceKeyDesc or seceKeyDesc: userId is " + userId);
            return;
        }
        keyDescMap.put(2, eceKeyDesc);
        keyDescMap.put(3, seceKeyDesc);
        for (PackStoragePolicy packagePolicy : this.mPackagePolicies) {
            if (StorageManagerExt.getPreLoadPolicyFlag(userId, serialNumber) != 1) {
                return;
            }
            if (!isInstalledApp(packagePolicy.getPackageName(), userId)) {
                Log.d(TAG, "package is not found: " + packagePolicy.getPackageName());
            } else {
                for (PathPolicy pathPolicy : packagePolicy.getPolicies()) {
                    if (pathPolicy.getEncryptionType() != -1) {
                        String path = convertPathToUser(pathPolicy.getPath(), userId);
                        if (!TextUtils.isEmpty(path)) {
                            setFileXattr(path, keyDescMap.get(Integer.valueOf(pathPolicy.getEncryptionType())), pathPolicy.getEncryptionType(), pathPolicy.getFileType());
                        }
                    }
                }
            }
        }
    }

    public int getLockState(int userId, int flag) {
        if (!checkPermission()) {
            return -1;
        }
        if (flag == 1) {
            return getScreenLockState(userId);
        }
        Log.w(TAG, "getLockState: input the invalid flag!");
        return -1;
    }

    public int registerLockStateChangeCallback(int flag, IHwLockStateChangeCallback callback) {
        if (!checkPermission()) {
            return -1;
        }
        if (callback != null) {
            return getSfpCallbackProxy(new SfpCallbackProxyKey(callback)).registerCallback();
        }
        Log.w(TAG, "registerLockStateChangeCallback: input the invalid callback!");
        return -3;
    }

    public int unregisterLockStateChangeCallback(IHwLockStateChangeCallback callback) {
        if (!checkPermission()) {
            return -1;
        }
        if (callback != null) {
            return getSfpCallbackProxy(new SfpCallbackProxyKey(callback)).unregisterCallback();
        }
        Log.w(TAG, "unregisterLockStateChangeCallback: input the invalid callback!");
        return -3;
    }

    public int executePolicy(String path, int userId) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            Log.w(TAG, "executePolicy: PMS is null!");
            return -1;
        }
        String callingName = packageManager.getNameForUid(getCallingUid());
        if (callingName == null) {
            Log.w(TAG, "executePolicy: callingName is null!");
            return -3;
        }
        String callingName2 = callingName.split(SPLIT_LABEL)[0];
        long token = Binder.clearCallingIdentity();
        try {
            return executePolicyInner(path, userId, callingName2);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private String getKeyDescInner(int userId, int serialNumber, int storageType) {
        try {
            String origin = StorageManagerExt.getKeyDesc(userId, serialNumber, storageType);
            if (origin == null || origin.startsWith(KEY_DESC_PREFIX_ERR)) {
                return "";
            }
            return origin;
        } catch (RuntimeException e) {
            Log.e(TAG, "getKeyDescInner: RuntimeException occurs!");
            return "";
        }
    }

    private int executePolicyInner(String path, int userId, String packageName) {
        if (packageName == null) {
            Log.w(TAG, "executePolicyInner: packageName is null!");
            return -3;
        }
        UserInfoExt userInfo = getUserInfo(userId);
        if (userInfo == null) {
            Log.e(TAG, "executePolicyInner: user info is null!");
            return -1;
        }
        int serialNumber = userInfo.getSerialNumber();
        for (PackStoragePolicy packagePolicy : this.mPackagePolicies) {
            if (packageName.equals(packagePolicy.getPackageName()) && isInstalledApp(packagePolicy.getPackageName(), userId)) {
                for (PathPolicy pathPolicy : packagePolicy.getPolicies()) {
                    if (pathPolicy.getEncryptionType() != -1) {
                        String policyPath = convertPathToUser(pathPolicy.getPath(), userId);
                        if (!TextUtils.isEmpty(policyPath) && policyPath.equals(path)) {
                            String keyDesc = getKeyDescInner(userId, serialNumber, pathPolicy.getEncryptionType());
                            if (TextUtils.isEmpty(keyDesc)) {
                                Log.w(TAG, "executePolicyInner: cannot get the keyDesc, userId is " + userId);
                                return -1;
                            } else if (setFileXattr(policyPath, keyDesc, pathPolicy.getEncryptionType(), pathPolicy.getFileType())) {
                                return 0;
                            } else {
                                Log.e(TAG, "executePolicyInner: set file attribute failed!");
                                return -1;
                            }
                        }
                    }
                }
                continue;
            }
        }
        return -3;
    }

    private int getScreenLockState(int userId) {
        int intValue;
        synchronized (this.mLockStateMap) {
            intValue = this.mLockStateMap.getOrDefault(Integer.valueOf(userId), -1).intValue();
        }
        return intValue;
    }

    private SfpCallbackProxy getSfpCallbackProxy(SfpCallbackProxyKey key) {
        SfpCallbackProxy orDefault;
        synchronized (this.mLockStateChangeCallbackMap) {
            orDefault = this.mLockStateChangeCallbackMap.getOrDefault(key, new SfpCallbackProxy(key));
        }
        return orDefault;
    }

    private boolean checkPermission() {
        Context context = this.mContext;
        if (context == null) {
            Log.w(TAG, "checkPermission: context is null!");
            return false;
        } else if (context.checkCallingPermission(PERMISSION_GET_SCREEN_LOCK_STATE) == 0) {
            return true;
        } else {
            Log.w(TAG, "checkPermission: permission error!");
            return false;
        }
    }

    private void setIsLocked(boolean isLocked, int userId) {
        if (isLocked) {
            UserInfoExt userInfo = getUserInfo(userId);
            if (userInfo == null) {
                Log.e(TAG, "setIsLocked: user info is null!");
                return;
            }
            int preloadStatus = StorageManagerExt.getPreLoadPolicyFlag(userId, userInfo.getSerialNumber());
            if (preloadStatus != 1) {
                Log.w(TAG, "setIsLocked: preloadStatus error: " + preloadStatus);
                return;
            }
        }
        synchronized (this.mLockStateMap) {
            this.mLockStateMap.put(Integer.valueOf(userId), Integer.valueOf(isLocked ? 1 : 0));
        }
        invokeAllLockStateChangeCallback(userId);
    }

    private void invokeAllLockStateChangeCallback(int userId) {
        List<SfpCallbackProxy> callbackProxies = new ArrayList<>(4);
        synchronized (this.mLockStateChangeCallbackMap) {
            callbackProxies.addAll(this.mLockStateChangeCallbackMap.values());
        }
        int state = getScreenLockState(userId);
        for (SfpCallbackProxy callbackProxy : callbackProxies) {
            callbackProxy.notifyLockStateChanged(userId, state);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onLockScreen(int userId) {
        if (IS_DEBUG) {
            Log.i(TAG, "onLockScreen: userId " + userId);
        }
        UserInfoExt userInfo = getUserInfo(userId);
        if (userInfo == null) {
            Log.e(TAG, "onLockScreen: user info is null!");
            return;
        }
        execPolicies(userId, userInfo.getSerialNumber());
        setIsLocked(true, userId);
        startTimer(userId);
    }

    private void startTimer(int userId) {
        Throwable th;
        if (IS_DEBUG) {
            Log.i(TAG, "startTimer: userId " + userId);
        }
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (alarmManager == null) {
            Log.e(TAG, "startTimer: alarmManager is null!");
            return;
        }
        synchronized (LOCK) {
            try {
                AlarmListener listener = this.mListenerMap.getOrDefault(Integer.valueOf(userId), null);
                if (listener != null) {
                    Log.e(TAG, "startTimer: the same timer!");
                    this.mListenerMap.remove(Integer.valueOf(userId));
                    this.mLockScreenUserIdList.remove(Integer.valueOf(userId));
                    cancelTimer(listener);
                }
                AlarmListener listener2 = new AlarmListener();
                try {
                    this.mLockScreenUserIdList.add(Integer.valueOf(userId));
                    this.mListenerMap.put(Integer.valueOf(userId), listener2);
                    alarmManager.set(2, DELAY_DELETE + SystemClock.elapsedRealtime(), DELETE_CLASS_KEY, listener2, this.mHandler);
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private void cancelTimer(AlarmListener listener) {
        Log.i(TAG, "cancleTimer");
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (alarmManager == null) {
            Log.e(TAG, "cancleTimer: alarmManager is null!");
        } else {
            alarmManager.cancel(listener);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserInfoExt getUserInfo(int userId) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager == null) {
            Log.e(TAG, "getUserInfo: UserManager is not found!");
            return null;
        }
        UserInfoExt userInfo = UserManagerExt.getUserInfoEx(userManager, userId);
        if (userInfo == null) {
            Log.e(TAG, "getUserInfo: cannot get the UserInfoExt: userId is " + userId);
        }
        return userInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deleteKey(int userId, int serialNumber) {
        StorageManagerExt.lockUserScreenISec(userId, serialNumber);
    }

    private String convertPathToUser(String path, int userId) {
        for (String subPath : MULTIPLE_USER_PATH) {
            if (path.startsWith(subPath)) {
                return path.replaceFirst(DEFAULT_USER_ID, String.valueOf(userId));
            }
        }
        return "";
    }

    private boolean isInstalledApp(String packageName, int userId) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            Log.w(TAG, "isInstalledApp: PMS is null!");
            return false;
        }
        try {
            PackageManagerExt.getApplicationInfoAsUser(packageManager, packageName, 0, userId);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "isInstalledApp: packageName not found!");
            return false;
        }
    }

    private boolean setFileXattr(String path, String keyDesc, int storageType, int fileType) {
        try {
            InstalldEx.setFileXattr(path, keyDesc, storageType, fileType);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "setFileXattr: set attribute failed!");
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class AlarmListener implements AlarmManager.OnAlarmListener {
        private AlarmListener() {
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            int userId = -1;
            synchronized (HwSfpService.LOCK) {
                if (!HwSfpService.this.mLockScreenUserIdList.isEmpty()) {
                    userId = ((Integer) HwSfpService.this.mLockScreenUserIdList.remove(0)).intValue();
                } else {
                    Log.e(HwSfpService.TAG, "onAlarm: mLockScreenUserIdList size is 0!");
                }
                if (HwSfpService.IS_DEBUG) {
                    Log.i(HwSfpService.TAG, "onAlarm: timeout userId is " + userId);
                }
                HwSfpService.this.mListenerMap.remove(Integer.valueOf(userId));
            }
            UserInfoExt userInfo = HwSfpService.this.getUserInfo(userId);
            if (userInfo == null) {
                Log.e(HwSfpService.TAG, "onAlarm: user info is null!");
            } else {
                HwSfpService.this.deleteKey(userId, userInfo.getSerialNumber());
            }
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == HwSfpService.EVT_LOCK_SCREEN) {
                HwSfpService.this.onLockScreen(msg.arg1);
            }
        }
    }

    private class LockScreenReceiver extends BroadcastReceiver {
        private LockScreenReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Log.e(HwSfpService.TAG, "onReceive context or intent is null!");
            } else if (HwSfpService.ACTION_LOCK_SCREEN.equals(intent.getAction())) {
                boolean isSmartUnlock = intent.getBooleanExtra(HwSfpService.INTENT_SMART_UNLOCK, false);
                boolean isSlideUnlock = intent.getBooleanExtra(HwSfpService.INTENT_SLIDE_UNLOCK, false);
                if (isSmartUnlock || isSlideUnlock) {
                    Log.d(HwSfpService.TAG, "skip the broadcast: smartUnlock is " + isSmartUnlock + " and slideUnlock is " + isSlideUnlock);
                    return;
                }
                int userId = intent.getIntExtra("userId", -1);
                if (userId == -1) {
                    Log.e(HwSfpService.TAG, "skip the broadcast: userId is null!");
                    return;
                }
                if (HwSfpService.IS_DEBUG) {
                    Log.i(HwSfpService.TAG, "onReceive ACTION_LOCK_SCREEN userId " + userId);
                }
                Message.obtain(HwSfpService.this.mHandler, HwSfpService.EVT_LOCK_SCREEN, userId, 0).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public class SfpCallbackProxyKey {
        private final IHwLockStateChangeCallback mCallback;
        private final int mCallingPid;
        private final int mInnerHashCode;

        private SfpCallbackProxyKey(IHwLockStateChangeCallback callback) {
            this.mCallingPid = Binder.getCallingPid();
            if (callback == null) {
                Log.e(HwSfpService.TAG, "SfpCallbackProxyKey: callback null!");
                this.mInnerHashCode = 0;
                this.mCallback = null;
                return;
            }
            try {
                this.mInnerHashCode = callback.innerHashCode();
                this.mCallback = callback;
            } catch (RemoteException e) {
                Log.e(HwSfpService.TAG, "SfpCallbackProxyKey: remote error!");
                this.mInnerHashCode = 0;
                this.mCallback = null;
            }
        }

        public int hashCode() {
            return this.mInnerHashCode;
        }

        public boolean equals(Object obj) {
            IHwLockStateChangeCallback iHwLockStateChangeCallback = this.mCallback;
            if (iHwLockStateChangeCallback == null || !(obj instanceof SfpCallbackProxyKey)) {
                return false;
            }
            SfpCallbackProxyKey otherProxy = (SfpCallbackProxyKey) obj;
            if (otherProxy.mCallingPid != this.mCallingPid) {
                return false;
            }
            try {
                return iHwLockStateChangeCallback.isInnerEquals(otherProxy.mCallback);
            } catch (RemoteException e) {
                Log.e(HwSfpService.TAG, "isInnerEquals: remote error!");
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class SfpCallbackProxy implements IBinder.DeathRecipient {
        private final IHwLockStateChangeCallback mCallback;
        private final SfpCallbackProxyKey mProxyKey;

        private SfpCallbackProxy(SfpCallbackProxyKey proxyKey) {
            this.mProxyKey = proxyKey;
            if (proxyKey == null) {
                this.mCallback = null;
            } else {
                this.mCallback = proxyKey.mCallback;
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            int errorCode = unregisterCallback();
            Log.e(HwSfpService.TAG, "binderDied: unregistered errorCode " + errorCode);
        }

        private boolean linkToDeath() {
            IHwLockStateChangeCallback iHwLockStateChangeCallback = this.mCallback;
            if (iHwLockStateChangeCallback == null) {
                Log.e(HwSfpService.TAG, "linkToDeath: callback null!");
                return false;
            }
            try {
                iHwLockStateChangeCallback.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.e(HwSfpService.TAG, "linkToDeath: remote error!");
                return false;
            }
        }

        private void unlinkToDeath() {
            IHwLockStateChangeCallback iHwLockStateChangeCallback = this.mCallback;
            if (iHwLockStateChangeCallback == null) {
                Log.e(HwSfpService.TAG, "unlinkToDeath: callback null!");
            } else {
                iHwLockStateChangeCallback.asBinder().unlinkToDeath(this, 0);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void notifyLockStateChanged(int userId, int state) {
            IHwLockStateChangeCallback iHwLockStateChangeCallback = this.mCallback;
            if (iHwLockStateChangeCallback == null) {
                Log.e(HwSfpService.TAG, "notifyLockStateChanged: callback null!");
                return;
            }
            try {
                iHwLockStateChangeCallback.onLockStateChanged(userId, state);
            } catch (RemoteException e) {
                Log.e(HwSfpService.TAG, "notifyLockStateChanged: remote error!");
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int registerCallback() {
            if (this.mCallback == null) {
                Log.e(HwSfpService.TAG, "registerCallback: callback null!");
                return -3;
            }
            synchronized (HwSfpService.this.mLockStateChangeCallbackMap) {
                if (HwSfpService.this.mLockStateChangeCallbackMap.containsKey(this.mProxyKey)) {
                    Log.e(HwSfpService.TAG, "registerCallback: callback is exist!");
                    return -5;
                } else if (!linkToDeath()) {
                    return -1;
                } else {
                    HwSfpService.this.mLockStateChangeCallbackMap.put(this.mProxyKey, this);
                    return 0;
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int unregisterCallback() {
            if (this.mCallback == null) {
                Log.e(HwSfpService.TAG, "unregisterCallback: callback null!");
                return -3;
            }
            synchronized (HwSfpService.this.mLockStateChangeCallbackMap) {
                if (!HwSfpService.this.mLockStateChangeCallbackMap.containsKey(this.mProxyKey)) {
                    Log.e(HwSfpService.TAG, "unregisterCallback: callback is not exist!");
                    return -6;
                }
                unlinkToDeath();
                HwSfpService.this.mLockStateChangeCallbackMap.remove(this.mProxyKey);
                return 0;
            }
        }
    }
}
