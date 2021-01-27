package com.huawei.dmsdpsdk2;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SharedMemory;
import android.system.ErrnoException;
import com.huawei.dmsdp.devicevirtualization.CameraDataCallback;
import com.huawei.dmsdpsdk2.ICameraDataCallback;
import com.huawei.dmsdpsdk2.util.Util;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Map;

public class CameraDataCallbackTransport extends ICameraDataCallback.Stub {
    private static final int EVENT_VIR_CAMERA_DATA_DONE = 1;
    private static final int SHARE_BUFFER_NUM = 8;
    private static final String TAG = "CameraDataCallbackTransport";
    private CameraDataCallback mCallback;
    private final Handler mHandler = new Handler(this.mHandlerThread.getLooper()) {
        /* class com.huawei.dmsdpsdk2.CameraDataCallbackTransport.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            CameraDataCallbackTransport.this.handleMessage(msg);
        }
    };
    private final HandlerThread mHandlerThread;

    public CameraDataCallbackTransport(CameraDataCallback callback, HandlerThread looper) {
        this.mCallback = callback;
        this.mHandlerThread = looper;
    }

    public void stopHandlerThread() {
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
            HwLog.i(TAG, "Stop handler thread, name: " + this.mHandlerThread.getName());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMessage(Message msg) {
        HwLog.d(TAG, "handleMessage: " + msg.what);
        if (msg.what != 1) {
            HwLog.e(TAG, "Unknown message id:" + msg.what + ", can not be here!");
        } else if (msg.obj instanceof VirCameraBufferWrapper) {
            VirCameraBufferWrapper wrapper = (VirCameraBufferWrapper) msg.obj;
            handleVirCameraBufferDone(wrapper.getShm(), wrapper.getParams());
        }
    }

    private void handleVirCameraBufferDone(SharedMemory shm, Map<String, Object> params) {
        if (Build.VERSION.SDK_INT < 27) {
            HwLog.e(TAG, "handleVirCameraBufferDone sdk version in invalid");
            return;
        }
        try {
            int len = shm.getSize() / 8;
            int bufferId = ((Integer) params.get("bufferId")).intValue();
            ByteBuffer bb = shm.mapReadOnly();
            HwLog.d(TAG, "handleVirCameraBufferDone, bufferId " + Util.anonymizeData((String) params.get("appId")) + ", bufferId:" + bufferId + ", offset:" + (len * bufferId) + ", len:" + len + ", index: " + ((Long) params.get("index")));
            bb.position(len * bufferId);
            byte[] result = new byte[len];
            bb.get(result);
            params.remove("bufferId");
            this.mCallback.onVirCameraBufferDone(result, params);
            SharedMemory.unmap(bb);
            shm.close();
            HwLog.i(TAG, "clear sheared memory");
        } catch (ErrnoException | IllegalArgumentException | BufferUnderflowException e) {
            HwLog.e(TAG, "handleVirCameraBufferDone exception:" + e.getMessage());
        }
    }

    @Override // com.huawei.dmsdpsdk2.ICameraDataCallback
    public void onVirCameraBufferDone(SharedMemory shm, Map params) throws RemoteException {
        sendMessage(1, -1, new VirCameraBufferWrapper(shm, params));
    }

    private void sendMessage(int msgWhat, int arg, Object obj) {
        if (!this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(msgWhat, arg, -1, obj), 0)) {
            HwLog.e(TAG, "sendMessage error here");
        }
    }

    /* access modifiers changed from: private */
    public static class VirCameraBufferWrapper {
        private Map<String, Object> params;
        private SharedMemory shm;

        public VirCameraBufferWrapper(SharedMemory shm2, Map<String, Object> params2) {
            this.shm = shm2;
            this.params = params2;
        }

        public SharedMemory getShm() {
            return this.shm;
        }

        public Map<String, Object> getParams() {
            return this.params;
        }
    }
}
