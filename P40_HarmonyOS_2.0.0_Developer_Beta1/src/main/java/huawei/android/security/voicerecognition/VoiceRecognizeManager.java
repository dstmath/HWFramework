package huawei.android.security.voicerecognition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.hwpartsecurity.BuildConfig;
import huawei.android.security.voicerecognition.IHeadsetStatusCallback;
import huawei.android.security.voicerecognition.IVoiceAuthCallback;
import huawei.android.security.voicerecognition.IVoiceRecognizeService;
import huawei.android.security.voicerecognition.IVoiceRecognizeServiceReceiver;
import java.util.List;

public class VoiceRecognizeManager implements IBinder.DeathRecipient {
    private static final String BIND_FROM_SDK = "bind_from_sdk";
    private static final int DEFAULT_ERROR_INT = -1;
    private static final int DEFAULT_ERROR_LONG = -1;
    private static final int MSG_AUTH_FAILED = 6;
    private static final int MSG_AUTH_SUCCESS = 5;
    private static final int MSG_ENROLL_ACQUIRE = 2;
    private static final int MSG_ENROLL_CANCEL_RET = 3;
    private static final int MSG_ENROLL_RESULT = 1;
    private static final int MSG_HEADSET_STATUS_CHANGE = 7;
    private static final int MSG_HEADSET_STATUS_CHANGE_TO_CLIENT = 8;
    private static final int MSG_REMOVE_RET = 4;
    private static final int SDK_BIND_VALUE = 1;
    private static final String TAG = "VoiceRecognizeManager";
    private static final int VOICEID_ENROLL_BUSY = 1;
    private static final int VOICEID_ENROLL_FAILED = 4;
    private static final int VOICEID_ENROLL_NOT_CONNECT = 2;
    private static final int VOICEID_ENROLL_OK = 0;
    private static final int VOICEID_ENROLL_TIMEOUT = 3;
    private static final int VOICEID_TYPE_AUTH_RET = 5;
    private static final int VOICEID_TYPE_ENROLL_ACQUIRE = 2;
    private static final int VOICEID_TYPE_ENROLL_CANCEL_RET = 3;
    private static final int VOICEID_TYPE_ENROLL_RET = 1;
    private static final int VOICEID_TYPE_REMOVE_RET = 4;
    private static final String VOICERECOGNIZE_SERVICE = "voicerecognition";
    private static final String VOICE_ENABLE_SWITCH = "voiceid_enable_switch";
    private static final int VOICE_ENABLE_SWITCH_DEFAULT = -1;
    private static final int VOICE_ENABLE_VALUE = 1;
    private static final String VOICE_SERVICE_CLASS = "com.huawei.securityserver.HwVoiceRecognizeService";
    private static final String VOICE_SERVICE_PACKAGE = "com.huawei.securityserver";
    private static boolean sIsBindService = false;
    private static final Object sLock = new Object();
    private AuthCallback mAuthCallback = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        /* class huawei.android.security.voicerecognition.VoiceRecognizeManager.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            synchronized (VoiceRecognizeManager.sLock) {
                Log.i(VoiceRecognizeManager.TAG, "on service connect voiceRecognizeService.");
                boolean unused = VoiceRecognizeManager.sIsBindService = true;
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(VoiceRecognizeManager.TAG, "on service disconnect voiceRecognizeService.");
        }
    };
    private Context mContext;
    private EnrollmentCallback mEnrollCallback;
    private Handler mHandler;
    private IHeadsetStatusCallback mHeadsetStatusCallback = new IHeadsetStatusCallback.Stub() {
        /* class huawei.android.security.voicerecognition.VoiceRecognizeManager.AnonymousClass4 */

        public void onHeadsetStatusChange(int status) {
            if (VoiceRecognizeManager.this.mHandler != null) {
                VoiceRecognizeManager.this.mHandler.obtainMessage(8, status, 0).sendToTarget();
            } else {
                VoiceRecognizeManager.this.sendHeadsetStatusChangedToClient(status);
            }
        }
    };
    private HeadsetStatusCallback mHeadsetStatusCb;
    private RemoveCallback mRemoveCallback;
    private IVoiceRecognizeService mService = null;
    private IBinder mServiceBinder = null;
    private IVoiceRecognizeServiceReceiver mServiceReceiver = new IVoiceRecognizeServiceReceiver.Stub() {
        /* class huawei.android.security.voicerecognition.VoiceRecognizeManager.AnonymousClass1 */

        public void onOptCallback(int type, int code, int subCode1, int subCode2) {
            if (type != 1) {
                if (type != 2) {
                    if (type != 3) {
                        if (type == 4) {
                            if (VoiceRecognizeManager.this.mHandler != null) {
                                VoiceRecognizeManager.this.mHandler.obtainMessage(4, code, 0, null).sendToTarget();
                            } else {
                                VoiceRecognizeManager.this.sendRemoveResult(code);
                            }
                        }
                    } else if (VoiceRecognizeManager.this.mHandler != null) {
                        VoiceRecognizeManager.this.mHandler.obtainMessage(3, code, 0, null).sendToTarget();
                    } else {
                        VoiceRecognizeManager.this.sendRemoveResult(code);
                    }
                } else if (VoiceRecognizeManager.this.mHandler != null) {
                    VoiceRecognizeManager.this.mHandler.obtainMessage(2, code, subCode1, null).sendToTarget();
                } else {
                    VoiceRecognizeManager.this.sendEnrollAcquire(code, subCode1);
                }
            } else if (VoiceRecognizeManager.this.mHandler != null) {
                VoiceRecognizeManager.this.mHandler.obtainMessage(1, code, subCode1, null).sendToTarget();
            } else {
                VoiceRecognizeManager.this.sendEnrollResult(code, subCode1);
            }
        }
    };
    private IBinder mToken = new Binder();
    private IVoiceAuthCallback mVoiceAuthCallback = new IVoiceAuthCallback.Stub() {
        /* class huawei.android.security.voicerecognition.VoiceRecognizeManager.AnonymousClass3 */

        public void onReceiveAuthVoice(int userId, int type) {
            if (VoiceRecognizeManager.this.mHandler != null) {
                VoiceRecognizeManager.this.mHandler.obtainMessage(5, userId, type, null).sendToTarget();
            } else {
                VoiceRecognizeManager.this.sendAuthSuccess(userId, type);
            }
        }

        public void onReceiveUnAuthVoice(int userId, int type) {
            if (VoiceRecognizeManager.this.mHandler != null) {
                VoiceRecognizeManager.this.mHandler.obtainMessage(6, userId, type).sendToTarget();
            } else {
                VoiceRecognizeManager.this.sendAuthFailed(userId, type);
            }
        }

        public void onHeadsetStatusChange(int status) {
            if (VoiceRecognizeManager.this.mHandler != null) {
                VoiceRecognizeManager.this.mHandler.obtainMessage(7, status, 0, null).sendToTarget();
            } else {
                VoiceRecognizeManager.this.sendHeadsetStatusChange(status);
            }
        }
    };

    public VoiceRecognizeManager(Context context) {
        this.mContext = context;
        if (context != null) {
            this.mHandler = new MyHandler(this.mContext);
        } else {
            this.mHandler = null;
        }
    }

    public void setAuthCallback(AuthCallback authCallback) {
        if (isVoiceExist()) {
            this.mService = getService();
            IVoiceRecognizeService iVoiceRecognizeService = this.mService;
            if (iVoiceRecognizeService != null) {
                try {
                    this.mAuthCallback = authCallback;
                    iVoiceRecognizeService.setAuthCallback(this.mVoiceAuthCallback);
                } catch (RemoteException e) {
                    Log.e(TAG, "set authcallback failed, because " + e);
                }
            } else {
                Log.e(TAG, "setAuthCallback failed because get service failed");
            }
        }
    }

    public boolean setHeadsetStatusCallback(HeadsetStatusCallback headsetStatusCallback) {
        if (!isVoiceExist()) {
            return false;
        }
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                iVoiceRecognizeService.setHeadsetStatusCallback(this.mHeadsetStatusCallback);
                this.mHeadsetStatusCb = headsetStatusCallback;
                return true;
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "setHeadsetStatusCallback failed because " + e);
            }
        } else {
            Log.e(TAG, "setHeadsetStatusCallback failed because get service failed");
            return false;
        }
    }

    public boolean enroll(byte[] authToken, int flags, EnrollmentCallback callback) {
        if (authToken == null || callback == null) {
            Log.e(TAG, "enroll failed because invalid parameter");
            return false;
        }
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                iVoiceRecognizeService.enroll(this.mToken, authToken, flags, 0, this.mServiceReceiver, BuildConfig.FLAVOR);
                this.mEnrollCallback = callback;
                return true;
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "enroll failed because " + e);
            }
        } else {
            Log.e(TAG, "enroll failed because get service failed");
            return false;
        }
    }

    public boolean cancelEnroll() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                iVoiceRecognizeService.cancelEnroll(this.mToken);
                return true;
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "cancel enroll failed because " + e);
                return false;
            }
        } else {
            Log.e(TAG, "cancel enroll failed because get service failed");
            return false;
        }
    }

    public boolean continueEnroll() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                iVoiceRecognizeService.continueEnroll();
                return true;
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "continueEnroll failed because " + e);
                return false;
            }
        } else {
            Log.e(TAG, "continueEnroll failed because get service failed");
            return false;
        }
    }

    public boolean remove(int voiceId, RemoveCallback callback) {
        if (callback == null) {
            return false;
        }
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                iVoiceRecognizeService.remove(this.mToken, voiceId, 0, this.mServiceReceiver);
                this.mRemoveCallback = callback;
                return true;
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "remove failed because " + e);
            }
        } else {
            Log.e(TAG, "remove failed because get service failed");
            return false;
        }
    }

    public boolean removeAll(RemoveCallback callback) {
        if (callback == null) {
            return false;
        }
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                iVoiceRecognizeService.removeAll(this.mToken, 0, this.mServiceReceiver);
                this.mRemoveCallback = callback;
                return true;
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "remove failed because " + e);
            }
        } else {
            Log.e(TAG, "remove failed because get service failed");
            return false;
        }
    }

    public int[] getEnrolledVoiceIdList() {
        if (!isVoiceExist()) {
            return new int[0];
        }
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getEnrolledVoiceIdList(0, BuildConfig.FLAVOR);
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getEnrolledVoiceIdList failed because " + e);
            }
        } else {
            Log.e(TAG, "getEnrolledVoiceIdList failed because get service failed");
            return new int[0];
        }
    }

    public long preEnroll() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.preEnroll();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "preEnroll failed because " + e);
                return 0;
            }
        } else {
            Log.e(TAG, "preEnroll failed because get service failed");
            return 0;
        }
    }

    public boolean postEnroll() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                if (iVoiceRecognizeService.postEnroll() == 0) {
                    return true;
                }
                return false;
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "postEnroll failed because " + e);
            }
        } else {
            Log.e(TAG, "postEnroll failed because get service failed");
            return false;
        }
    }

    public void resetTimeout() {
        if (isVoiceExist()) {
            this.mService = getService();
            IVoiceRecognizeService iVoiceRecognizeService = this.mService;
            if (iVoiceRecognizeService != null) {
                try {
                    iVoiceRecognizeService.resetTimeout();
                } catch (RemoteException e) {
                    this.mService = null;
                    Log.e(TAG, "resetTimeout failed because " + e);
                }
            } else {
                Log.e(TAG, "resetTimeout failed because get service failed");
            }
        }
    }

    public int getRemainingNum() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getRemainingNum();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getRemainingNum failed because " + e);
                return -1;
            }
        } else {
            Log.e(TAG, "getRemainingNum failed because get service failed");
            return -1;
        }
    }

    public int getTotalAuthFailedTimes() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getTotalAuthFailedTimes();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getTotalAuthFailedTimes failed because " + e);
                return -1;
            }
        } else {
            Log.e(TAG, "getTotalAuthFailedTimes failed because get service failed");
            return -1;
        }
    }

    public long getRemainingTime() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getRemainingTime();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getRemainingTime failed because " + e);
                return -1;
            }
        } else {
            Log.e(TAG, "getRemainingTime failed because get service failed");
            return -1;
        }
    }

    public int getHeadsetStatus() {
        if (!isVoiceExist()) {
            return -1;
        }
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getHeadsetStatus();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getHeadsetStatus failed because " + e);
            }
        } else {
            Log.e(TAG, "getHeadsetStatus failed because get service failed");
            return -1;
        }
    }

    public List<String> getVoiceEnrollStringList() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getVoiceEnrollStringList();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getVoiceEnrollStringList failed because " + e);
            }
        } else {
            Log.e(TAG, "getVoiceEnrollStringList failed because get service failed");
            return null;
        }
    }

    public int getHeadsetStatus(String mac) {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getHeadsetStatusByMac(mac);
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getHeadsetStatusByMac failed because " + e);
                return -1;
            }
        } else {
            Log.e(TAG, "getHeadsetStatusByMac failed because get service failed");
            return -1;
        }
    }

    public int getVoiceIdUserUpdateStatus() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getVoiceIdUserUpdateStatus(0);
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getVoiceIdUserUpdataStatus failed because " + e.getMessage());
                return -1;
            }
        } else {
            Log.e(TAG, "getVoiceIdUserUpdataStatus failed because get service failed");
            return -1;
        }
    }

    public int[] getVoiceIdSupportCommandList() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getVoiceIdSupportCommandList();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getVoiceIdSupportCommandList failed because " + e.getMessage());
            }
        } else {
            Log.e(TAG, "getVoiceIdSupportCommandList failed because get service failed");
            return new int[0];
        }
    }

    public VoiceCommandList getVoiceCommandList() {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                return iVoiceRecognizeService.getVoiceCommandList();
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "getVoiceCommandList failed because " + e);
            }
        } else {
            Log.e(TAG, "getVoiceCommandList failed because get service failed");
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEnrollResult(int ret, int id) {
        EnrollmentCallback enrollmentCallback = this.mEnrollCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollResult(ret, id);
        } else {
            Log.e(TAG, "enroll result send failed because callback is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEnrollAcquire(int acquireInfo, int subInfo) {
        EnrollmentCallback enrollmentCallback = this.mEnrollCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollAcquire(acquireInfo, subInfo);
        } else {
            Log.e(TAG, "acquire info send failed because callback is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEnrollCancelResult(int ret) {
        EnrollmentCallback enrollmentCallback = this.mEnrollCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollCancelResult(ret);
        } else {
            Log.e(TAG, "enroll cancel result send failed because callback is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRemoveResult(int ret) {
        RemoveCallback removeCallback = this.mRemoveCallback;
        if (removeCallback != null) {
            removeCallback.onRemoveResult(ret);
        } else {
            Log.e(TAG, "remove result send failed because callback is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAuthSuccess(int userId, int type) {
        AuthCallback authCallback = this.mAuthCallback;
        if (authCallback != null) {
            boolean ret = authCallback.onReceiveAuthVoice(userId, type);
            Log.i(TAG, "Send auth result returns " + ret);
            if (ret) {
                startVoiceActivity(type);
                return;
            }
            return;
        }
        Log.e(TAG, "auth ok send failed because callback is null");
    }

    private void startVoiceActivity(int type) {
        this.mService = getService();
        IVoiceRecognizeService iVoiceRecognizeService = this.mService;
        if (iVoiceRecognizeService != null) {
            try {
                iVoiceRecognizeService.startVoiceActivity(type);
            } catch (RemoteException e) {
                this.mService = null;
                Log.e(TAG, "startVoiceActivity failed because " + e);
            }
        } else {
            Log.e(TAG, "startVoiceActivity failed because get service failed");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAuthFailed(int userId, int type) {
        AuthCallback authCallback = this.mAuthCallback;
        if (authCallback != null) {
            authCallback.onReceiveUnAuthVoice(userId, type, 0);
        } else {
            Log.e(TAG, "auth fail send failed because callback is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendHeadsetStatusChange(int status) {
        AuthCallback authCallback = this.mAuthCallback;
        if (authCallback != null) {
            authCallback.onHeadsetStatusChange(status);
        } else {
            Log.e(TAG, "authCallback send HeadsetStatusChange failed because callback is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendHeadsetStatusChangedToClient(int status) {
        HeadsetStatusCallback headsetStatusCallback = this.mHeadsetStatusCb;
        if (headsetStatusCallback != null) {
            headsetStatusCallback.onHeadsetStatusChange(status);
            return;
        }
        Log.e(TAG, "sendHeadsetStatusChanged to client " + status);
    }

    private boolean isVoiceExist() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        int voiceListValue = Settings.Secure.getInt(context.getContentResolver(), VOICE_ENABLE_SWITCH, -1);
        if (ServiceManagerEx.getService(VOICERECOGNIZE_SERVICE) != null || voiceListValue == 1) {
            return true;
        }
        return false;
    }

    private IVoiceRecognizeService getService() {
        synchronized (sLock) {
            if (!sIsBindService && this.mContext != null) {
                Log.i(TAG, "sdk get service.");
                Intent intent = new Intent();
                intent.putExtra(BIND_FROM_SDK, 1);
                intent.setComponent(new ComponentName(VOICE_SERVICE_PACKAGE, VOICE_SERVICE_CLASS));
                this.mContext.bindService(intent, this.mConnection, 1);
            }
            if (this.mService == null) {
                if (this.mServiceBinder != null) {
                    this.mServiceBinder.unlinkToDeath(this, 0);
                }
                this.mServiceBinder = ServiceManagerEx.getService(VOICERECOGNIZE_SERVICE);
                if (this.mServiceBinder == null) {
                    Log.e(TAG, "getService binder null");
                    return null;
                }
                this.mService = IVoiceRecognizeService.Stub.asInterface(this.mServiceBinder);
                if (this.mService == null) {
                    Log.e(TAG, "getService Service null");
                } else {
                    try {
                        this.mServiceBinder.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Log.e(TAG, "getService linkToDeath fail" + e.getMessage());
                    }
                }
            }
            return this.mService;
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (sLock) {
            Log.e(TAG, "service binder died");
            if (this.mServiceBinder != null) {
                this.mServiceBinder.unlinkToDeath(this, 0);
                this.mServiceBinder = null;
            }
            sIsBindService = false;
            this.mService = null;
        }
    }

    public static abstract class EnrollmentCallback {
        public void onEnrollResult(int ret, int id) {
        }

        public void onEnrollAcquire(int acquireInfo, int subInfo) {
        }

        public void onEnrollCancelResult(int ret) {
        }
    }

    public static abstract class RemoveCallback {
        public void onRemoveResult(int ret) {
        }
    }

    public static abstract class AuthCallback {
        public boolean onReceiveAuthVoice(int userId, int type) {
            return false;
        }

        public void onReceiveUnAuthVoice(int userId, int type, int totalFailedTimes) {
        }

        public void onHeadsetStatusChange(int status) {
        }
    }

    public static abstract class HeadsetStatusCallback {
        public void onHeadsetStatusChange(int status) {
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Context context) {
            super(context.getMainLooper());
        }

        private MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        VoiceRecognizeManager.this.sendEnrollResult(msg.arg1, msg.arg2);
                        return;
                    case 2:
                        VoiceRecognizeManager.this.sendEnrollAcquire(msg.arg1, msg.arg2);
                        return;
                    case 3:
                        VoiceRecognizeManager.this.sendEnrollCancelResult(msg.arg1);
                        return;
                    case 4:
                        VoiceRecognizeManager.this.sendRemoveResult(msg.arg1);
                        return;
                    case 5:
                        VoiceRecognizeManager.this.sendAuthSuccess(msg.arg1, msg.arg2);
                        return;
                    case 6:
                        VoiceRecognizeManager.this.sendAuthFailed(msg.arg1, msg.arg2);
                        return;
                    case 7:
                        VoiceRecognizeManager.this.sendHeadsetStatusChange(msg.arg1);
                        return;
                    case 8:
                        VoiceRecognizeManager.this.sendHeadsetStatusChangedToClient(msg.arg1);
                        return;
                    default:
                        return;
                }
            }
        }
    }
}
