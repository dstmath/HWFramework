package huawei.com.android.server.policy;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hiai.awareness.client.AwarenessEnvelope;
import com.huawei.hiai.awareness.client.AwarenessFence;
import com.huawei.hiai.awareness.client.AwarenessManager;
import com.huawei.hiai.awareness.client.AwarenessRequest;
import com.huawei.hiai.awareness.client.AwarenessResult;
import com.huawei.hiai.awareness.client.AwarenessServiceConnection;
import com.huawei.hiai.awareness.client.OnEnvelopeReceiver;
import com.huawei.hiai.awareness.client.OnResultListener;

public class HwSwingFaceNumberFenceManager {
    private static final int CAMERA_FOV_RATIO = SystemPropertiesEx.getInt("hw_mc.faceunlock_swing", 72);
    private static final int HANDLE_DELAY = 500;
    private static final int RECONNECT_MAX_COUNT = 3;
    private static final long RECOONECT_FACE_WAIT_TIME_MS = 5000;
    private static final String TAG = "HwSwingFaceNumberFenceManager";
    private static HwSwingFaceNumberFenceManager sHwSwingFaceNumberFenceManager = null;
    private AwarenessManager mAwarenessManager;
    private int mAwarenessReconnectTimes;
    private AwarenessServiceConnection mAwarenessServiceConnection = new AwarenessServiceConnection() {
        /* class huawei.com.android.server.policy.HwSwingFaceNumberFenceManager.AnonymousClass3 */

        @Override // com.huawei.hiai.awareness.client.AwarenessServiceConnection
        public void onConnected() {
            HwSwingFaceNumberFenceManager.this.mIsAwarenessConnected = true;
            HwSwingFaceNumberFenceManager.this.mAwarenessReconnectTimes = 0;
            Log.i(HwSwingFaceNumberFenceManager.TAG, "mAwarenessServiceConnection onServiceConnected");
            HwSwingFaceNumberFenceManager.this.enableFaceNumberFence();
        }

        @Override // com.huawei.hiai.awareness.client.AwarenessServiceConnection
        public void onDisconnected() {
            HwSwingFaceNumberFenceManager.this.mIsAwarenessConnected = false;
            Log.i(HwSwingFaceNumberFenceManager.TAG, "wait 5000ms to reconnect");
            if (HwSwingFaceNumberFenceManager.this.mHandler != null) {
                HwSwingFaceNumberFenceManager.this.mHandler.postDelayed(new Runnable() {
                    /* class huawei.com.android.server.policy.HwSwingFaceNumberFenceManager.AnonymousClass3.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        HwSwingFaceNumberFenceManager.access$108(HwSwingFaceNumberFenceManager.this);
                        if (!HwSwingFaceNumberFenceManager.this.mIsAwarenessConnected && HwSwingFaceNumberFenceManager.this.mAwarenessReconnectTimes < 3) {
                            Log.i(HwSwingFaceNumberFenceManager.TAG, "mAwarenessHandler try connectService " + HwSwingFaceNumberFenceManager.this.mAwarenessReconnectTimes);
                            if (!HwSwingFaceNumberFenceManager.this.mAwarenessManager.connectService(HwSwingFaceNumberFenceManager.this.mAwarenessServiceConnection)) {
                                Log.i(HwSwingFaceNumberFenceManager.TAG, "connectService failed!");
                                HwSwingFaceNumberFenceManager.this.mHandler.postDelayed(this, HwSwingFaceNumberFenceManager.RECOONECT_FACE_WAIT_TIME_MS);
                            }
                        }
                    }
                }, HwSwingFaceNumberFenceManager.RECOONECT_FACE_WAIT_TIME_MS);
            }
        }
    };
    private Context mContext = null;
    private int mFaceNumber = 0;
    private Runnable mHandleForDetecteFace = null;
    private Handler mHandler = null;
    private boolean mIsAwarenessConnected = false;
    private boolean mIsFaceNumberFenceRegistered = false;
    private final Object mLock = new Object();
    private OnEnvelopeReceiver mOnEnvelopeReceiver = new OnEnvelopeReceiver.Stub() {
        /* class huawei.com.android.server.policy.HwSwingFaceNumberFenceManager.AnonymousClass4 */

        @Override // com.huawei.hiai.awareness.client.OnEnvelopeReceiver
        public void onReceive(AwarenessEnvelope envelope) throws RemoteException {
            if (envelope == null) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "onReceive envelope is null");
                return;
            }
            AwarenessFence fence = AwarenessFence.parseFrom(envelope);
            if (fence == null || fence.getState() == null) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "handleFenceResult fence is null");
                return;
            }
            int currentState = fence.getState().getCurrentState();
            if (currentState != 1) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "handleFenceResult currentState is " + currentState);
                return;
            }
            Bundle bundle = fence.getState().getExtras();
            if (bundle == null) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "handleFenceResult bundle is null");
                return;
            }
            synchronized (HwSwingFaceNumberFenceManager.this.mLock) {
                HwSwingFaceNumberFenceManager.this.mFaceNumber = bundle.getInt("face_number");
                Log.i(HwSwingFaceNumberFenceManager.TAG, "mAwarenessListener faceNumber: " + HwSwingFaceNumberFenceManager.this.mFaceNumber);
                HwSwingFaceNumberFenceManager.this.mLock.notifyAll();
            }
            if (HwSwingFaceNumberFenceManager.this.mFaceNumber > 0 && HwSwingFaceNumberFenceManager.this.mHandleForDetecteFace != null) {
                HwSwingFaceNumberFenceManager.this.mHandler.postDelayed(HwSwingFaceNumberFenceManager.this.mHandleForDetecteFace, 500);
            }
        }
    };
    private OnResultListener mOnRegisterResultListener = new OnResultListener.Stub() {
        /* class huawei.com.android.server.policy.HwSwingFaceNumberFenceManager.AnonymousClass1 */

        @Override // com.huawei.hiai.awareness.client.OnResultListener
        public void onResult(AwarenessResult awarenessResult) throws RemoteException {
            if (awarenessResult == null) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "Register awarenessResult is null");
            } else if (!awarenessResult.isSuccessful()) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "Register awarenessResult is fail");
            }
        }
    };
    private OnResultListener mOnUnRegisterResultListener = new OnResultListener.Stub() {
        /* class huawei.com.android.server.policy.HwSwingFaceNumberFenceManager.AnonymousClass2 */

        @Override // com.huawei.hiai.awareness.client.OnResultListener
        public void onResult(AwarenessResult awarenessResult) throws RemoteException {
            if (awarenessResult == null) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "UnRegister awarenessResult is null");
            } else if (!awarenessResult.isSuccessful()) {
                Log.w(HwSwingFaceNumberFenceManager.TAG, "UnRegister awarenessResult is fail");
            }
        }
    };
    private AwarenessFence mSwingFaceNumberFence;

    static /* synthetic */ int access$108(HwSwingFaceNumberFenceManager x0) {
        int i = x0.mAwarenessReconnectTimes;
        x0.mAwarenessReconnectTimes = i + 1;
        return i;
    }

    private HwSwingFaceNumberFenceManager(Context context, Handler handler) {
        this.mContext = context;
        this.mAwarenessManager = new AwarenessManager(this.mContext);
        Log.i(TAG, "create face number fence");
        this.mSwingFaceNumberFence = AwarenessFence.create("face_number_fence").putArg("fov", CAMERA_FOV_RATIO);
        this.mHandler = handler;
    }

    public static synchronized HwSwingFaceNumberFenceManager getInstance(Context context, Handler handler) {
        HwSwingFaceNumberFenceManager hwSwingFaceNumberFenceManager;
        synchronized (HwSwingFaceNumberFenceManager.class) {
            if (sHwSwingFaceNumberFenceManager == null) {
                sHwSwingFaceNumberFenceManager = new HwSwingFaceNumberFenceManager(context, handler);
            }
            hwSwingFaceNumberFenceManager = sHwSwingFaceNumberFenceManager;
        }
        return hwSwingFaceNumberFenceManager;
    }

    public boolean isRegistered() {
        return this.mIsFaceNumberFenceRegistered;
    }

    public boolean registerAwarenessFence(Runnable func) {
        this.mHandleForDetecteFace = func;
        if (this.mIsAwarenessConnected) {
            return enableFaceNumberFence();
        }
        boolean isSuccess = this.mAwarenessManager.connectService(this.mAwarenessServiceConnection);
        Log.w(TAG, "connectService success:" + isSuccess);
        return isSuccess;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean enableFaceNumberFence() {
        if (this.mIsFaceNumberFenceRegistered) {
            Log.w(TAG, "swing face number fence already registered");
            return true;
        }
        OnEnvelopeReceiver onEnvelopeReceiver = this.mOnEnvelopeReceiver;
        if (onEnvelopeReceiver == null) {
            Log.w(TAG, "no valid OnEnvelopeReceiver");
            return false;
        }
        if (this.mAwarenessManager != null) {
            this.mIsFaceNumberFenceRegistered = this.mAwarenessManager.dispatch(AwarenessRequest.registerFence(this.mSwingFaceNumberFence, onEnvelopeReceiver).addOnResultListener(this.mOnRegisterResultListener));
        }
        Log.i(TAG, "registerAwarenessFence mIsRegistered " + this.mIsFaceNumberFenceRegistered);
        return this.mIsFaceNumberFenceRegistered;
    }

    public void unregisterAwarenessFence() {
        if (!this.mIsAwarenessConnected) {
            Log.i(TAG, "awareness service not connected");
            return;
        }
        if (this.mAwarenessManager != null && this.mIsFaceNumberFenceRegistered) {
            if (this.mAwarenessManager.dispatch(AwarenessRequest.unregisterFence(this.mSwingFaceNumberFence).addOnResultListener(this.mOnUnRegisterResultListener))) {
                this.mIsFaceNumberFenceRegistered = false;
                this.mFaceNumber = 0;
            }
        }
        Log.i(TAG, "unregisterAwarenessFence mIsRegistered " + this.mIsFaceNumberFenceRegistered);
    }

    public int getFaceNumber() {
        Log.i(TAG, "face number:" + this.mFaceNumber);
        return this.mFaceNumber;
    }

    /* access modifiers changed from: package-private */
    public Object getLockObject() {
        return this.mLock;
    }
}
