package com.huawei.security.deviceauth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.trustcircle.TrustCircleManager;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.coauth.pool.CoAuthResService;
import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.deviceauth.ICallbackMethods;
import com.huawei.security.deviceauth.IHichainService;
import huawei.android.security.IAuthSessionListener;
import huawei.android.security.ISignCallback;
import huawei.android.security.ITrustCircleManager;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class HichainAuthManager {
    public static final int ACROSS_ACCOUNT_AUTH = 2;
    private static final int AUTH_ERROR = -1;
    private static final int AUTH_ID_ERROR = -1;
    private static final int AUTH_START_REQUEST = 17;
    public static final String AUTH_TAG_AUTH_FORM = "authForm";
    public static final String AUTH_TAG_DEVICE_ID = "deviceId";
    public static final String AUTH_TAG_KEY_LENGTH = "keyLength";
    public static final String AUTH_TAG_PEER_ID = "peerAuthId";
    public static final String AUTH_TAG_PEER_TYPE = "peerUserType";
    public static final String AUTH_TAG_PKG_NAME = "servicePkgName";
    public static final String AUTH_TAG_SELF_ID = "selfAuthId";
    public static final String AUTH_TAG_SELF_TYPE = "selfUserType";
    public static final String AUTH_TAG_SERVICE_TYPE = "serviceType";
    public static final String AUTH_TAG_SESSION_KEY = "sessionKey";
    public static final String AUTH_TAG_USER_ID = "userId";
    private static final String DAS_NAME = "com.huawei.deviceauth.HwDeviceAuthService";
    private static final String DAS_PACKAGE_NAME = "com.huawei.deviceauth";
    private static final int DATA_NOT_EXIST = -1;
    private static final int DEFAULT_KEY_LENGTH = 32;
    public static final int FAILED = -1;
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static final int IDENTICAL_ACCOUNT_AUTH = 1;
    private static final int LATCH_COUNT = 1;
    private static final int LIFE_CYCLE_ERROR = -1;
    private static final int MAP_SIZE = 16;
    public static final int NON_ACCOUNT_AUTH = 0;
    private static final int RECEIVER_WITH_FINISH = 3;
    private static final int RECEIVER_WITH_MAC = 1;
    private static final int RESULT_OK = 0;
    private static final int SENDER_SEND_START = 0;
    private static final int SENDER_WITH_MAC = 2;
    private static final long SERVICE_RUNNING_TIME = 300000;
    public static final int SUCCESS = 0;
    private static final String TAG = "HichainAuthManager";
    private static final String TRUSTCIRCLE_MANAGER_SERVICE = "trustcircle_manager_service";
    private static final int TRUSTCIRCLE_PLUGIN_ID = 5;
    private static final int WAIT_LOCK_TIME = 500;
    private static IHichainService sDeviceAuthService = null;
    private static volatile HichainAuthManager sHichainAuthManager;
    private static Object sLock = new Object();
    private static Map<String, JSONObject> sRequestInfo = new HashMap(16);
    private static Map<String, JSONObject> sTmpRequestParams = new HashMap(16);
    private final IBinder mBinderToken = new Binder();
    private final ExecutorService mCallbackJob = Executors.newSingleThreadExecutor();
    private final String mCallerPackageName;
    private final Context mContext;
    private ServiceConnection mDeviceAuthServiceConn;
    private TrustCircleManager.LoginCallback mLoginCallback;
    private TrustCircleManager.LogoutCallback mLogoutCallback;
    private volatile boolean mRunningFlag;
    private Timer mTimer;
    private UnbindTask mTimerTask;
    private TrustCircleManager.UnregisterCallback mUnregisterCallback;

    public interface HichainAuthCallback {
        void onError(long j, int i, int i2, String str);

        void onFinish(long j, int i, String str);

        String onRequest(long j, int i, String str);

        void onSessionKeyReturned(long j, byte[] bArr);

        boolean onTransmit(long j, byte[] bArr);
    }

    public interface HichainReqSignCallback {
        void onSignFinish(long j, int i, byte[] bArr);
    }

    private HichainAuthManager(Context context) {
        this.mContext = context;
        this.mCallerPackageName = context.getPackageName();
        connectDeviceAuthService();
    }

    private ServiceConnection getServiceConnection(final CountDownLatch countDownLatch) {
        return new ServiceConnection() {
            /* class com.huawei.security.deviceauth.HichainAuthManager.AnonymousClass1 */

            public void onServiceDisconnected(ComponentName name) {
                Log.i(HichainAuthManager.TAG, "onServiceDisconnected");
                IHichainService unused = HichainAuthManager.sDeviceAuthService = null;
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(HichainAuthManager.TAG, "onServiceConnected");
                IHichainService unused = HichainAuthManager.sDeviceAuthService = IHichainService.Stub.asInterface(service);
                try {
                    service.linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.security.deviceauth.HichainAuthManager.AnonymousClass1.AnonymousClass1 */

                        public void binderDied() {
                            Log.e(HichainAuthManager.TAG, "binderDied");
                            IHichainService unused = HichainAuthManager.sDeviceAuthService = null;
                        }
                    }, 0);
                    if (countDownLatch != null) {
                        countDownLatch.countDown();
                    }
                } catch (RemoteException e) {
                    IHichainService unused2 = HichainAuthManager.sDeviceAuthService = null;
                    Log.e(HichainAuthManager.TAG, "RemoteException when call binder linkToDeath");
                }
            }
        };
    }

    private void connectDeviceAuthService() {
        Log.i(TAG, "start connectDeviceAuthService");
        if (sDeviceAuthService != null) {
            startUnbindTimer();
            return;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(DAS_PACKAGE_NAME, DAS_NAME));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        this.mDeviceAuthServiceConn = getServiceConnection(countDownLatch);
        try {
            Log.i(TAG, this.mCallerPackageName + " connects HwDeviceAuthService...");
            if (!this.mContext.bindService(intent, 65, this.mCallbackJob, this.mDeviceAuthServiceConn)) {
                Log.e(TAG, "connect HwDeviceAuthService fail");
            } else if (!countDownLatch.await(500, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "wait HwDeviceAuthService connection timeout");
            } else {
                startUnbindTimer();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "can't find HwDeviceAuthService or has no permission to access it");
        } catch (InterruptedException e2) {
            Log.e(TAG, "can't wait service connection, return directly");
        }
    }

    private void startUnbindTimer() {
        Log.i(TAG, "start unbindTimer");
        Timer timer = this.mTimer;
        if (timer != null) {
            timer.cancel();
            this.mTimerTask.cancel();
        }
        this.mTimer = new Timer();
        this.mTimerTask = new UnbindTask();
        this.mTimer.schedule(this.mTimerTask, SERVICE_RUNNING_TIME, SERVICE_RUNNING_TIME);
    }

    public static HichainAuthManager getInstance(Context context) {
        Log.i(TAG, "start getInstance");
        if (sHichainAuthManager == null) {
            synchronized (HichainAuthManager.class) {
                if (context == null) {
                    Log.e(TAG, "Invalid parameters : context is null");
                    return sHichainAuthManager;
                }
                Log.d(TAG, "HichainAuthManager instance created by " + context.getPackageName());
                if (sHichainAuthManager == null) {
                    sHichainAuthManager = new HichainAuthManager(context);
                }
            }
        }
        if (sDeviceAuthService == null) {
            Log.w(TAG, "HwDeviceAuthService has not connected yet");
        }
        return sHichainAuthManager;
    }

    public int authDevice(HichainAuthCallback callback, long authReqId, String authParams) {
        this.mRunningFlag = true;
        Log.i(TAG, "start authDevice");
        int ret = -1;
        if (authParams == null || callback == null) {
            Log.e(TAG, "Invalid parameters :authParams or callback is null");
            this.mRunningFlag = false;
            return -1;
        }
        String authReqIdStr = String.valueOf(authReqId);
        if (sRequestInfo.containsKey(authReqIdStr)) {
            Log.e(TAG, "authReqId conflict");
            this.mRunningFlag = false;
            return -1;
        }
        JSONObject jsonAuthParams = parseJsonString(authParams);
        if (jsonAuthParams == null) {
            Log.e(TAG, "Invalid parameters : bad authParams format");
            this.mRunningFlag = false;
            return -1;
        }
        int authForm = jsonAuthParams.optInt(AUTH_TAG_AUTH_FORM, -1);
        if (authForm < 0) {
            Log.e(TAG, "Invalid parameter : no authForm");
            this.mRunningFlag = false;
            return -1;
        }
        if (authForm == 0) {
            Log.i(TAG, "invoke DeviceAuthService authentication");
            ret = startDasAuth(callback, authReqIdStr, jsonAuthParams);
        } else if (authForm == 1 || authForm == 2) {
            Log.i(TAG, "invoke TCIS authentication");
            ret = startTcisAuth(callback, authReqId, authParams);
        } else {
            Log.e(TAG, "Invalid parameter : illegal authForm");
        }
        if (ret == 0) {
            sRequestInfo.put(authReqIdStr, jsonAuthParams);
            Log.i(TAG, "invoke authentication of Id " + authReqId + " ok. There are " + sRequestInfo.size() + " requests in processing");
        }
        this.mRunningFlag = false;
        return ret;
    }

    public boolean processAuthData(HichainAuthCallback callback, long authReqId, byte[] data) {
        int authForm;
        this.mRunningFlag = true;
        Log.i(TAG, "start processAuthData");
        boolean ret = false;
        if (callback == null) {
            Log.e(TAG, "Invalid parameters : callback is null");
            this.mRunningFlag = false;
            return false;
        }
        String dataStr = bytesToString(data);
        JSONObject jsonData = parseJsonString(dataStr);
        if (jsonData == null) {
            this.mRunningFlag = false;
            return false;
        }
        int authForm2 = jsonData.optInt(AUTH_TAG_AUTH_FORM, -1);
        if (authForm2 < 0) {
            Log.w(TAG, "no authForm, do non-account authentication");
            authForm = 0;
        } else {
            authForm = authForm2;
        }
        if (authForm == 0) {
            Log.i(TAG, "DeviceAuthService receives data");
            ret = processDasData(callback, authReqId, jsonData, data);
        } else if (authForm == 1 || authForm == 2) {
            Log.i(TAG, "TCIS receives data");
            ret = processTcisData(callback, authReqId, authForm, jsonData, dataStr);
        } else {
            Log.e(TAG, "Invalid passthrough data");
        }
        this.mRunningFlag = false;
        return ret;
    }

    public int cancelAuthRequest(long authReqId) {
        this.mRunningFlag = true;
        Log.i(TAG, "start cancelAuthRequest");
        int ret = -1;
        String authReqIdStr = String.valueOf(authReqId);
        JSONObject jsonAuthParams = sRequestInfo.get(authReqIdStr);
        if (jsonAuthParams == null) {
            Log.w(TAG, "didn't find the specified request");
            this.mRunningFlag = false;
            return 0;
        }
        int authForm = jsonAuthParams.optInt(AUTH_TAG_AUTH_FORM, -1);
        if (authForm < 0) {
            Log.e(TAG, "Invalid parameter : no authForm");
            this.mRunningFlag = false;
            return -1;
        }
        if (authForm == 0) {
            ret = cancelDasAuthRequest(authReqIdStr);
        } else if (authForm == 1 || authForm == 2) {
            ret = cancelTcisAuthRequest(authReqId, jsonAuthParams.optString(AUTH_TAG_PKG_NAME, null));
        } else {
            Log.e(TAG, "Invalid parameter : illegal authForm");
        }
        if (ret == 0) {
            sRequestInfo.remove(authReqIdStr);
            Log.i(TAG, "Request " + authReqIdStr + " has been cancelled successfully. There are " + sRequestInfo.size() + " requests in processing");
        }
        this.mRunningFlag = false;
        return ret;
    }

    public int requestSignature(HichainReqSignCallback callback, long signReqId, byte[] signReqParams) {
        Log.i(TAG, "start requestSignature");
        if (callback == null || signReqParams == null) {
            Log.e(TAG, "Invalid parameters: callback or signReqParams is null");
            return -1;
        }
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService == null) {
            Log.e(TAG, "get TrustCirclePlugin fail");
            return -1;
        }
        try {
            return tcisService.requestSignature(new SignRequestCallbackInner(callback), signReqId, signReqParams);
        } catch (RemoteException e) {
            Log.e(TAG, "active TCIS requestSignature failed");
            return -1;
        }
    }

    private int cancelDasAuthRequest(String authReqIdStr) {
        Log.i(TAG, "cancel DeviceAuthService authentication request of ID = " + authReqIdStr);
        connectDeviceAuthService();
        try {
            if (sDeviceAuthService != null) {
                return sDeviceAuthService.cancel(this.mCallerPackageName, authReqIdStr, this.mBinderToken);
            }
            Log.e(TAG, "service disconnected when call cancelAuthRequest");
            connectDeviceAuthService();
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException occurs when call cancelAuthRequest");
            return -1;
        }
    }

    private int cancelTcisAuthRequest(long authReqId, String pkgName) {
        Log.i(TAG, "cancel TCIS authentication request of ID = " + authReqId);
        if (pkgName == null) {
            Log.e(TAG, "Invalid parameter : empty package name");
            return -1;
        }
        long reqId = ((long) pkgName.hashCode()) ^ authReqId;
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService == null) {
            Log.e(TAG, "get TrustCircleManager fail");
            return -1;
        }
        try {
            tcisService.cancelAuthentication(reqId);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "cancel TCIS authentication failed");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public class DasAuthCallback extends ICallbackMethods.Stub {
        private HichainAuthCallback mCallback;

        private DasAuthCallback(HichainAuthCallback callback) {
            this.mCallback = callback;
        }

        public void onOperationFinished(String reqId, int operationCode, int result) {
            Log.i(HichainAuthManager.TAG, "start onOperationFinished");
            JSONObject authParams = (JSONObject) HichainAuthManager.sRequestInfo.get(reqId);
            if (this.mCallback != null) {
                JSONObject returnObj = new JSONObject();
                if (authParams != null) {
                    try {
                        returnObj.put(HichainAuthManager.AUTH_TAG_PKG_NAME, authParams.optString(HichainAuthManager.AUTH_TAG_PKG_NAME, null));
                        returnObj.put(HichainAuthManager.AUTH_TAG_PEER_ID, authParams.optString(HichainAuthManager.AUTH_TAG_PEER_ID, null));
                        returnObj.put(HichainAuthManager.AUTH_TAG_PEER_TYPE, authParams.optInt(HichainAuthManager.AUTH_TAG_PEER_TYPE, -1));
                    } catch (JSONException e) {
                        Log.e(HichainAuthManager.TAG, "pack error return data fail");
                    }
                }
                try {
                    Log.i(HichainAuthManager.TAG, "invoke onError callback");
                    this.mCallback.onError(Long.parseLong(reqId), 0, result, returnObj.toString());
                } catch (Exception e2) {
                    Log.e(HichainAuthManager.TAG, "return operation result error");
                }
            }
            HichainAuthManager.sRequestInfo.remove(reqId);
            Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
        }

        public void onOperationFinishedWithData(String reqId, int operationCode, int result, byte[] returnData) {
            Log.i(HichainAuthManager.TAG, "start onOperationFinishedWithData");
            long authReqId = Long.parseLong(reqId);
            JSONObject authParams = (JSONObject) HichainAuthManager.sRequestInfo.get(reqId);
            if (this.mCallback != null) {
                JSONObject returnObj = new JSONObject();
                if (result == 0) {
                    try {
                        try {
                            returnObj.put(HichainAuthManager.AUTH_TAG_SESSION_KEY, HichainAuthManager.this.toHexString(returnData));
                            if (authParams != null) {
                                returnObj.put(HichainAuthManager.AUTH_TAG_PKG_NAME, authParams.optString(HichainAuthManager.AUTH_TAG_PKG_NAME, null));
                            }
                            Log.i(HichainAuthManager.TAG, "invoke onFinish callback");
                            this.mCallback.onFinish(authReqId, 0, returnObj.toString());
                        } catch (JSONException e) {
                            Log.e(HichainAuthManager.TAG, "pack return data fail");
                            HichainAuthManager.sRequestInfo.remove(reqId);
                            Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
                        } catch (Exception e2) {
                            Log.e(HichainAuthManager.TAG, "return operation result and data error");
                            HichainAuthManager.sRequestInfo.remove(reqId);
                            Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
                        }
                    } catch (JSONException e3) {
                        Log.e(HichainAuthManager.TAG, "pack return data fail");
                        HichainAuthManager.sRequestInfo.remove(reqId);
                        Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
                    } catch (Exception e4) {
                        Log.e(HichainAuthManager.TAG, "return operation result and data error");
                        HichainAuthManager.sRequestInfo.remove(reqId);
                        Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
                    }
                } else {
                    if (authParams != null) {
                        returnObj.put(HichainAuthManager.AUTH_TAG_PKG_NAME, authParams.optString(HichainAuthManager.AUTH_TAG_PKG_NAME, null));
                        returnObj.put(HichainAuthManager.AUTH_TAG_PEER_ID, authParams.optString(HichainAuthManager.AUTH_TAG_PEER_ID, null));
                        returnObj.put(HichainAuthManager.AUTH_TAG_PEER_TYPE, authParams.optInt(HichainAuthManager.AUTH_TAG_PEER_TYPE, -1));
                    }
                    Log.i(HichainAuthManager.TAG, "invoke onError callback");
                    this.mCallback.onError(authReqId, 0, result, returnObj.toString());
                }
            }
            HichainAuthManager.sRequestInfo.remove(reqId);
            Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
        }

        public boolean onPassthroughDataGenerated(String reqId, byte[] passthroughData) {
            Log.i(HichainAuthManager.TAG, "start onPassthroughDataGenerated");
            if (this.mCallback == null) {
                return false;
            }
            try {
                Log.i(HichainAuthManager.TAG, "invoke onTransmit callback");
                return this.mCallback.onTransmit(Long.parseLong(reqId), passthroughData);
            } catch (Exception e) {
                Log.e(HichainAuthManager.TAG, "can't transmit passthrough data");
                return false;
            }
        }

        public ConfirmParams onReceiveRequest(String reqId, int operationCode) {
            Log.i(HichainAuthManager.TAG, "start onReceiveRequest");
            ConfirmParams confirmParams = new ConfirmParams(ReturnCode.REQUEST_REJECTED, BuildConfig.FLAVOR, 0);
            if (operationCode != OperationCode.AUTHENTICATE.toInt()) {
                return confirmParams;
            }
            String authParams = null;
            if (this.mCallback != null) {
                try {
                    JSONObject reqParams = (JSONObject) HichainAuthManager.sTmpRequestParams.get(reqId);
                    if (reqParams == null) {
                        Log.e(HichainAuthManager.TAG, "didn't find server request parameters");
                        return confirmParams;
                    }
                    HichainAuthManager.sTmpRequestParams.remove(reqId);
                    long authReqId = Long.parseLong(reqId);
                    Log.i(HichainAuthManager.TAG, "invoke onRequest callback");
                    authParams = this.mCallback.onRequest(authReqId, 0, reqParams.toString());
                } catch (Exception e) {
                    Log.e(HichainAuthManager.TAG, "acquire upper permission error");
                }
            }
            if (authParams == null) {
                return confirmParams;
            }
            JSONObject jsonAuthParams = HichainAuthManager.this.parseJsonString(authParams);
            if (jsonAuthParams == null) {
                Log.e(HichainAuthManager.TAG, "Invalid authParams : bad JSON format");
                return confirmParams;
            }
            int keyLength = jsonAuthParams.optInt(HichainAuthManager.AUTH_TAG_KEY_LENGTH);
            if (keyLength <= 0) {
                keyLength = 32;
            }
            confirmParams.setKeyLength(keyLength);
            confirmParams.setServiceType(jsonAuthParams.optString(HichainAuthManager.AUTH_TAG_SERVICE_TYPE, null));
            confirmParams.setPeerId(HichainAuthManager.this.toBytesFromHex(jsonAuthParams.optString(HichainAuthManager.AUTH_TAG_PEER_ID, null)));
            confirmParams.setPeerType(jsonAuthParams.optInt(HichainAuthManager.AUTH_TAG_PEER_TYPE, -1));
            confirmParams.setSelfId(HichainAuthManager.this.toBytesFromHex(jsonAuthParams.optString(HichainAuthManager.AUTH_TAG_SELF_ID, null)));
            confirmParams.setSelfType(jsonAuthParams.optInt(HichainAuthManager.AUTH_TAG_SELF_TYPE, -1));
            confirmParams.setConfirmation(ReturnCode.REQUEST_ACCEPTED);
            HichainAuthManager.sRequestInfo.put(reqId, jsonAuthParams);
            Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
            return confirmParams;
        }

        public void onSessionKeyReturned(String reqId, byte[] sessionKey) {
            Log.i(HichainAuthManager.TAG, "DeviceAuthService session key returned of request " + reqId);
            if (this.mCallback != null) {
                try {
                    Log.i(HichainAuthManager.TAG, "invoke onSessionKeyReturned callback");
                    this.mCallback.onSessionKeyReturned(Long.parseLong(reqId), sessionKey);
                } catch (AbstractMethodError e) {
                    Log.w(HichainAuthManager.TAG, "no onSessionKeyReturned method, pass");
                } catch (Exception e2) {
                    Log.e(HichainAuthManager.TAG, "return sessionKey error");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class AuthSessionCallbackInner extends IAuthSessionListener.Stub {
        private HichainAuthCallback mCallback;

        private AuthSessionCallbackInner(HichainAuthCallback callback) {
            this.mCallback = callback;
        }

        public String onRequest(long authReqId, int authForm, String reqParams) {
            Log.i(HichainAuthManager.TAG, "start onRequest");
            String authParams = null;
            HichainAuthCallback hichainAuthCallback = this.mCallback;
            if (hichainAuthCallback != null) {
                try {
                    authParams = hichainAuthCallback.onRequest(authReqId, authForm, reqParams);
                    JSONObject jsonAuthParams = HichainAuthManager.this.parseJsonString(authParams);
                    if (jsonAuthParams != null) {
                        jsonAuthParams.put(HichainAuthManager.AUTH_TAG_AUTH_FORM, authForm);
                        HichainAuthManager.sRequestInfo.put(String.valueOf(authReqId), jsonAuthParams);
                        Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
                    }
                } catch (JSONException e) {
                    Log.e(HichainAuthManager.TAG, "pack authParams of TCIS server request error");
                } catch (Exception e2) {
                    Log.e(HichainAuthManager.TAG, "acquire upper permission error");
                }
            }
            return authParams;
        }

        public boolean onTransmit(long authReqId, byte[] data) {
            Log.i(HichainAuthManager.TAG, "start onTransmit");
            HichainAuthCallback hichainAuthCallback = this.mCallback;
            if (hichainAuthCallback == null) {
                return true;
            }
            try {
                hichainAuthCallback.onTransmit(authReqId, data);
                return true;
            } catch (Exception e) {
                Log.e(HichainAuthManager.TAG, "can't transmit passthrough data");
                return true;
            }
        }

        public void onFinish(long authReqId, int authForm, String authReturn) {
            Log.i(HichainAuthManager.TAG, "start onFinish");
            String authReqIdStr = String.valueOf(authReqId);
            if (this.mCallback != null) {
                try {
                    JSONObject returnData = new JSONObject(authReturn);
                    if (returnData.optBoolean("endSession", true)) {
                        this.mCallback.onFinish(authReqId, authForm, authReturn);
                    } else {
                        byte[] sessionKey = HichainAuthManager.this.toBytesFromHex(returnData.optString(HichainAuthManager.AUTH_TAG_SESSION_KEY, null));
                        if (sessionKey == null) {
                            Log.e(HichainAuthManager.TAG, "parse session key error");
                            return;
                        }
                        this.mCallback.onSessionKeyReturned(authReqId, sessionKey);
                    }
                } catch (JSONException e) {
                    Log.e(HichainAuthManager.TAG, "bad format of returned data");
                } catch (AbstractMethodError e2) {
                    Log.w(HichainAuthManager.TAG, "no onSessionKeyReturned method, pass");
                } catch (Exception e3) {
                    Log.e(HichainAuthManager.TAG, "return operation result and data error");
                }
            }
            HichainAuthManager.sRequestInfo.remove(authReqIdStr);
            Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
        }

        public void onError(long authReqId, int authForm, int errorCode, String errorReturn) {
            Log.i(HichainAuthManager.TAG, "start onError");
            String authReqIdStr = String.valueOf(authReqId);
            HichainAuthCallback hichainAuthCallback = this.mCallback;
            if (hichainAuthCallback != null) {
                try {
                    hichainAuthCallback.onError(authReqId, authForm, errorCode, errorReturn);
                } catch (Exception e) {
                    Log.e(HichainAuthManager.TAG, "return operation result and data error");
                }
            }
            HichainAuthManager.sRequestInfo.remove(authReqIdStr);
            Log.i(HichainAuthManager.TAG, "There are " + HichainAuthManager.sRequestInfo.size() + " requests in processing");
        }
    }

    private class SignRequestCallbackInner extends ISignCallback.Stub {
        private HichainReqSignCallback mCallback;

        private SignRequestCallbackInner(HichainReqSignCallback callback) {
            this.mCallback = callback;
        }

        public void onSignFinish(long signReqId, int authForm, byte[] data) {
            Log.i(HichainAuthManager.TAG, "start onSignFinish");
            HichainReqSignCallback hichainReqSignCallback = this.mCallback;
            if (hichainReqSignCallback != null) {
                try {
                    hichainReqSignCallback.onSignFinish(signReqId, authForm, data);
                } catch (Exception e) {
                    Log.e(HichainAuthManager.TAG, "onSignFinish data error");
                }
            }
        }
    }

    private boolean processDasData(HichainAuthCallback callback, long authReqId, JSONObject receiveData, byte[] data) {
        SessionInfo sessionInfo;
        JSONObject payload = receiveData.optJSONObject("payload");
        if (payload == null) {
            Log.e(TAG, "can't find DeviecAuthService payload in authPayload");
            return false;
        }
        String authReqIdStr = String.valueOf(authReqId);
        JSONObject jsonAuthParams = sRequestInfo.get(authReqIdStr);
        if (jsonAuthParams == null) {
            Log.i(TAG, "didn't find the specified request");
            if (receiveData.optInt("message", -1) != 17) {
                Log.e(TAG, "received data is not authentication start request");
                return false;
            }
            sessionInfo = extractSessionInfo(authReqIdStr, payload);
            JSONObject reqParams = new JSONObject();
            try {
                reqParams.put(AUTH_TAG_PKG_NAME, payload.optString("pkgName", this.mCallerPackageName));
                reqParams.put(AUTH_TAG_SERVICE_TYPE, payload.optString(AUTH_TAG_SERVICE_TYPE, null));
                reqParams.put(AUTH_TAG_PEER_ID, payload.optString(AUTH_TAG_PEER_ID, null));
                reqParams.put(AUTH_TAG_PEER_TYPE, payload.optInt(AUTH_TAG_PEER_TYPE, -1));
                reqParams.put(AUTH_TAG_KEY_LENGTH, payload.optInt(AUTH_TAG_KEY_LENGTH));
            } catch (JSONException e) {
                Log.e(TAG, "construct reqParams fail");
            }
            sTmpRequestParams.put(authReqIdStr, reqParams);
        } else {
            sessionInfo = extractSessionInfo(authReqIdStr, jsonAuthParams);
        }
        connectDeviceAuthService();
        IHichainService iHichainService = sDeviceAuthService;
        if (iHichainService != null) {
            try {
                try {
                } catch (RemoteException e2) {
                    Log.e(TAG, "RemoteException occurs when call processDasData");
                    return false;
                }
                try {
                    iHichainService.processReceivedData(this.mCallerPackageName, sessionInfo, new DasAuthCallback(callback), data);
                    return true;
                } catch (RemoteException e3) {
                    Log.e(TAG, "RemoteException occurs when call processDasData");
                    return false;
                }
            } catch (RemoteException e4) {
                Log.e(TAG, "RemoteException occurs when call processDasData");
                return false;
            }
        } else {
            Log.e(TAG, "service disconnected when call processDasData");
            connectDeviceAuthService();
            return false;
        }
    }

    private boolean processTcisData(HichainAuthCallback callback, long authReqId, int authForm, JSONObject receiveData, String dataStr) {
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService == null) {
            Log.e(TAG, "get TrustCircleManager fail");
            return false;
        }
        int step = receiveData.getInt("step");
        if (step == 0) {
            return tcisService.receiveRequest(new AuthSessionCallbackInner(callback), authReqId, dataStr);
        }
        if (step == 1) {
            return tcisService.receiveAuthSessionSyncAck(authReqId, dataStr);
        }
        if (step == 2) {
            return tcisService.receiveSessionAck(authReqId, authForm, receiveData.getString(CoAuthResService.KEY_DATA));
        }
        if (step == 3) {
            return tcisService.receiveSessionFinish(authReqId, authForm, receiveData.getString(CoAuthResService.KEY_DATA));
        }
        try {
            Log.e(TAG, "receiveProgressData, step: " + step + " not support");
            return false;
        } catch (RemoteException | JSONException e) {
            Log.e(TAG, "receiveProgressData, step:");
            return false;
        }
    }

    private SessionInfo extractSessionInfo(String authReqId, JSONObject jsonObject) {
        String serviceType = jsonObject.optString(AUTH_TAG_SERVICE_TYPE, null);
        if (serviceType == null) {
            Log.w(TAG, "no serviceType");
        }
        byte[] selfId = toBytesFromHex(jsonObject.optString(AUTH_TAG_SELF_ID));
        if (selfId == null) {
            Log.w(TAG, "no selfId");
        }
        int selfType = jsonObject.optInt(AUTH_TAG_SELF_TYPE);
        byte[] peerId = toBytesFromHex(jsonObject.optString(AUTH_TAG_PEER_ID, null));
        if (peerId == null) {
            Log.w(TAG, "no peerId");
        }
        return new SessionInfo(authReqId, serviceType, selfId, selfType, peerId, jsonObject.optInt(AUTH_TAG_PEER_TYPE, -1), this.mBinderToken);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:3:0x005e */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r15v1, types: [int] */
    /* JADX WARN: Type inference failed for: r15v2 */
    /* JADX WARN: Type inference failed for: r15v11 */
    /* JADX WARN: Type inference failed for: r15v12 */
    /* JADX WARN: Type inference failed for: r15v13 */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00a3 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00a5 A[RETURN] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private int startDasAuth(HichainAuthCallback callback, String authReqId, JSONObject authParams) {
        ?? r15;
        boolean z;
        String str;
        int ret = ReturnCode.REQUEST_REJECTED;
        ret = ReturnCode.REQUEST_REJECTED;
        String targetPackageName = authParams.optString(AUTH_TAG_PKG_NAME, null);
        int keyLength = authParams.optInt(AUTH_TAG_KEY_LENGTH);
        String optString = authParams.optString(AUTH_TAG_SERVICE_TYPE, null);
        SessionInfo sessionInfo = new SessionInfo(authReqId, optString, toBytesFromHex(authParams.optString(AUTH_TAG_SELF_ID, null)), authParams.optInt(AUTH_TAG_SELF_TYPE, -1), toBytesFromHex(authParams.optString(AUTH_TAG_PEER_ID, null)), authParams.optInt(AUTH_TAG_PEER_TYPE, -1), this.mBinderToken);
        connectDeviceAuthService();
        if (sDeviceAuthService != null) {
            if (targetPackageName != null) {
                try {
                    if (this.mCallerPackageName.equals(targetPackageName)) {
                        z = true;
                    } else {
                        optString = -1;
                        try {
                            ret = sDeviceAuthService.authenticateAcrossProcess(this.mCallerPackageName, targetPackageName, sessionInfo, new DasAuthCallback(callback), keyLength);
                            r15 = optString;
                        } catch (RemoteException e) {
                            str = optString;
                            Log.e(TAG, "RemoteException occurs when call authenticate");
                            r15 = str;
                            if (ret == -2147483642) {
                            }
                        }
                    }
                } catch (RemoteException e2) {
                    str = -1;
                    Log.e(TAG, "RemoteException occurs when call authenticate");
                    r15 = str;
                    if (ret == -2147483642) {
                    }
                }
            } else {
                z = true;
            }
            ret = sDeviceAuthService.authenticate(this.mCallerPackageName, sessionInfo, new DasAuthCallback(callback), keyLength);
            r15 = z;
        } else {
            r15 = -1;
            Log.e(TAG, "service disconnected when call authenticate");
            connectDeviceAuthService();
        }
        if (ret == -2147483642) {
            return 0;
        }
        return r15;
    }

    private int startTcisAuth(HichainAuthCallback callback, long authReqId, String authParams) {
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService == null) {
            Log.e(TAG, "get TrustCirclePlugin fail");
            return -1;
        }
        try {
            return tcisService.initAuthenticateSession(new AuthSessionCallbackInner(callback), authReqId, authParams);
        } catch (RemoteException e) {
            Log.e(TAG, "active TCIS authentication failed");
            return -1;
        }
    }

    private ITrustCircleManager getTrustCircleService() {
        synchronized (sLock) {
            IBinder tcisBinder = ServiceManagerEx.getService(TRUSTCIRCLE_MANAGER_SERVICE);
            if (tcisBinder == null) {
                Log.e(TAG, "getService binder null");
                return null;
            }
            ITrustCircleManager tcisService = ITrustCircleManager.Stub.asInterface(tcisBinder);
            if (tcisService == null) {
                Log.e(TAG, "getService service null");
            }
            return tcisService;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length == 0) {
            return BuildConfig.FLAVOR;
        }
        char[] hexArray = new char[(bytes.length + bytes.length)];
        for (int i = 0; i < bytes.length; i++) {
            int temp = bytes[i] & 255;
            char[] cArr = HEX_ARRAY;
            hexArray[i * 2] = cArr[temp >>> 4];
            hexArray[(i * 2) + 1] = cArr[temp & 15];
        }
        return new String(hexArray);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private byte[] toBytesFromHex(String inHex) {
        if (inHex == null) {
            return null;
        }
        String hexStr = inHex;
        int length = hexStr.length();
        if (length == 0) {
            return new byte[0];
        }
        if (length % 2 != 0) {
            hexStr = "0" + hexStr;
            length++;
        }
        byte[] stringBytes = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            int temp1 = Character.digit(hexStr.charAt(i), 16);
            int temp2 = Character.digit(hexStr.charAt(i + 1), 16);
            if (temp1 == -1 || temp2 == -1) {
                Log.e(TAG, "toBytesFromHex: Invalid input string");
                return new byte[0];
            }
            stringBytes[i / 2] = (byte) ((temp1 << 4) + temp2);
        }
        return stringBytes;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private JSONObject parseJsonString(String jsonStr) {
        if (jsonStr == null) {
            Log.e(TAG, "Invalid parameter : input JSON string is null");
            return null;
        }
        try {
            Object data = new JSONTokener(jsonStr).nextValue();
            if (data instanceof JSONObject) {
                return (JSONObject) data;
            }
            Log.e(TAG, "bad JSON string format : not JSON object");
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "bad JSON string format : parse error");
            return null;
        }
    }

    private static String bytesToString(byte[] inBytes) {
        if (inBytes == null) {
            return null;
        }
        return new String(inBytes, StandardCharsets.UTF_8);
    }

    /* access modifiers changed from: package-private */
    public class UnbindTask extends TimerTask {
        UnbindTask() {
        }

        public void run() {
            if (HichainAuthManager.sRequestInfo.size() == 0 && !HichainAuthManager.this.mRunningFlag) {
                try {
                    HichainAuthManager.this.mContext.unbindService(HichainAuthManager.this.mDeviceAuthServiceConn);
                    Log.i(HichainAuthManager.TAG, HichainAuthManager.this.mCallerPackageName + " service is idle, unbind HwDeviceAuthService");
                } catch (IllegalArgumentException e) {
                    Log.w(HichainAuthManager.TAG, "service has not been connected");
                }
                IHichainService unused = HichainAuthManager.sDeviceAuthService = null;
            }
        }
    }
}
