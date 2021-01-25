package com.huawei.facerecognize;

import android.os.IHwBinder;
import android.os.NativeHandle;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.huawei.annotation.HwSystemApi;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognize;
import vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback;
import vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeDataCallback;

@HwSystemApi
public class FaceHidlAdapter {
    private static final int FAIL = -1;
    private static final int INVALID_CALLBACK_STATUS = -1;
    private static final int INVALID_FACE_ID = -1;
    private static final String SERVICE_THREAD_NAME = "facerecognize";
    private static final String TAG = "FaceHidlAdapter";
    private static final Object sHidlLock = new Object();
    private IBiometricsFaceRecognize mFaceIdDaemon = getFaceRecognizeDaemon();
    private FaceIdHidlCallback mFaceIdHidlCallback;
    private FaceIdHidlDataCallback mFaceIdHidlDataCallback;
    private FaceIdHidlDeathRecipient mFaceIdHidlDeathRecipient;
    private long mHalDeviceId = 0;

    public static class AuthPayResultCallbackWrapper {
        public int faceId = -1;
        public ArrayList<Byte> reserve = null;
        public int result = 1;
        public ArrayList<Byte> tokenResult = null;
    }

    public interface FaceIdHidlServiceDiedCallbackWarpper {
        void onServiceDied();
    }

    public interface FaceRecognizeCallbackWarpper {
        void onAuthenticationAcquired(int i);

        void onAuthenticationResult(int i, int i2);

        void onCancel(int i);

        void onEnrollAcquired(int i, int i2);

        void onEnrollResult(int i, int i2, int i3);

        void onInitResult(int i);

        void onReleaseResult(int i);

        void onRemovedResult(int i, int i2);
    }

    public interface FaceRecognizeDataCallbackWarpper {
        int sendBigData(int i, int i2, FileDescriptor fileDescriptor, ArrayList<Byte> arrayList);

        int sendData(int i, ArrayList<Byte> arrayList, ArrayList<Byte> arrayList2);
    }

    public static class GetStatusResultCallbackWrapper {
        public ArrayList<Byte> reserve = null;
        public int result = 1;
        public int status1 = -1;
        public int status2 = -1;
    }

    private synchronized IBiometricsFaceRecognize getFaceRecognizeDaemon() {
        if (this.mFaceIdDaemon == null) {
            Slog.v(TAG, "mDaemon was null, reconnect to facerecognition");
            getFaceDaemonByMode();
            if (this.mFaceIdDaemon == null) {
                Slog.e(TAG, "face recognition HIDL not available");
                return null;
            }
        }
        return this.mFaceIdDaemon;
    }

    private void getFaceDaemonByMode() {
        try {
            this.mFaceIdDaemon = IBiometricsFaceRecognize.getService(false);
        } catch (NoSuchElementException e) {
            Slog.e(TAG, "Get facerecognize daemon NoSuchElementException");
        } catch (RemoteException e2) {
            Slog.e(TAG, "Failed to get facerecognize biometric interface" + e2.getMessage());
        }
    }

    public boolean isServiceConnected() {
        return this.mFaceIdDaemon != null;
    }

    public boolean isLinkToDeath(FaceIdHidlServiceDiedCallbackWarpper serviceDiedCallback) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "isLinkToDeath: no face recognize HAL!");
            return false;
        }
        this.mFaceIdHidlDeathRecipient = new FaceIdHidlDeathRecipient(serviceDiedCallback);
        boolean isLink = daemon.asBinder().linkToDeath(this.mFaceIdHidlDeathRecipient, 0);
        if (!isLink) {
            Slog.e(TAG, "face recognize HAL link failed");
        }
        return isLink;
    }

    public long setNotify(FaceRecognizeCallbackWarpper faceRecognizeCallback) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "setNotify: no face recognize HAL!");
            return 0;
        }
        this.mFaceIdHidlCallback = new FaceIdHidlCallback(faceRecognizeCallback);
        try {
            this.mHalDeviceId = daemon.setNotify(this.mFaceIdHidlCallback);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to open facerecognize HAL" + e.getMessage());
            this.mFaceIdDaemon = null;
            this.mHalDeviceId = 0;
        }
        Slog.d(TAG, "getFaceRecognizeDaemonHAL id: " + this.mHalDeviceId);
        return this.mHalDeviceId;
    }

    public int setDataCallback(FaceRecognizeDataCallbackWarpper faceRecognizeDataCallback) {
        Slog.i(TAG, "setDataCallback begin");
        if (faceRecognizeDataCallback == null) {
            Slog.e(TAG, "setDataCallback: bad param");
            return -1;
        }
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "setDataCallback: no face recognize HAL!");
            return 6;
        }
        this.mFaceIdHidlDataCallback = new FaceIdHidlDataCallback(faceRecognizeDataCallback);
        try {
            return daemon.setDataCallback(this.mFaceIdHidlDataCallback);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to send data callback" + e.getMessage());
            return 6;
        }
    }

    public int sendBigData(int dataType, int dataSize, FileDescriptor fileDescriptor, ArrayList<Byte> extra) {
        if (fileDescriptor == null) {
            Slog.e(TAG, "sendBigData: bad param");
            return -1;
        }
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "setDataCallback: no face recognize HAL!");
            return 6;
        }
        NativeHandle memHdl = new NativeHandle(fileDescriptor, false);
        if (!memHdl.hasSingleFileDescriptor()) {
            Slog.e(TAG, "sendBigData: not support muilty fds");
            try {
                memHdl.close();
            } catch (IOException e) {
                Slog.e(TAG, "memHdl close failed" + e.getMessage());
            }
            return -1;
        }
        try {
            int sendBigData = daemon.sendBigData(dataType, dataSize, memHdl, extra);
            try {
                memHdl.close();
            } catch (IOException e2) {
                Slog.e(TAG, "memHdl close failed" + e2.getMessage());
            }
            return sendBigData;
        } catch (RemoteException e3) {
            Slog.e(TAG, "Failed to send big data" + e3.getMessage());
            try {
                memHdl.close();
            } catch (IOException e4) {
                Slog.e(TAG, "memHdl close failed" + e4.getMessage());
            }
            return 6;
        } catch (Throwable th) {
            try {
                memHdl.close();
            } catch (IOException e5) {
                Slog.e(TAG, "memHdl close failed" + e5.getMessage());
            }
            throw th;
        }
    }

    public int sendData(int dataType, ArrayList<Byte> data, ArrayList<Byte> extra) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "setDataCallback: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.sendData(dataType, data, extra);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to send data" + e.getMessage());
            return 6;
        }
    }

    public int init(String packageName) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "init: no face recognize HAL!");
            return 6;
        }
        try {
            int val = daemon.init(packageName);
            Slog.d(TAG, "daemon init result val = " + val + " package = " + packageName);
            return val;
        } catch (RemoteException e) {
            Slog.e(TAG, "init failed" + e.getMessage());
            return 6;
        }
    }

    public int cancelInit() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "cancelInit: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.cancelInit();
        } catch (RemoteException e) {
            Slog.e(TAG, "cancelInit: failed" + e.getMessage());
            return 6;
        }
    }

    public int release(String packageName) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "release: no face recognize HAL!");
            return 6;
        }
        try {
            int val = daemon.release(packageName);
            Slog.d(TAG, "release result val = " + val + " package = " + packageName);
            return val;
        } catch (RemoteException e) {
            Slog.e(TAG, "release failed" + e.getMessage());
            return 6;
        }
    }

    public int cancelRelease() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "cancelRelease: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.cancelRelease();
        } catch (RemoteException e) {
            Slog.e(TAG, "cancelRelease: failed" + e.getMessage());
            return 6;
        }
    }

    public long preEnroll() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "preEnroll: no face recognize HAL!");
            return 0;
        }
        try {
            return daemon.preEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "preEnroll failed" + e.getMessage());
            return 0;
        }
    }

    public int enroll(byte[] cryptoToken, int userId) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "enroll: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.enroll(cryptoToken, userId);
        } catch (RemoteException e) {
            Slog.e(TAG, "enroll: failed" + e.getMessage());
            return 6;
        }
    }

    public void postEnroll() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "postEnroll: no face recognize HAL!");
            return;
        }
        try {
            daemon.postEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "postEnroll failed" + e.getMessage());
        }
    }

    public int cancelEnroll() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "cancelEnroll: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.cancelEnroll();
        } catch (RemoteException e) {
            Slog.e(TAG, "cancelEnroll: failed" + e.getMessage());
            return 6;
        }
    }

    public int cancelAuthenticate() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "cancelAuthenticate: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.cancelAuthenticate();
        } catch (RemoteException e) {
            Slog.e(TAG, "cancelAuthenticate: failed" + e.getMessage());
            return 6;
        }
    }

    public int cancelRemove() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "cancelRemove: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.cancelRemove();
        } catch (RemoteException e) {
            Slog.e(TAG, "cancelRemove: failed" + e.getMessage());
            return 6;
        }
    }

    public int remove(int userId, int faceId) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "remove: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.remove(userId, faceId);
        } catch (RemoteException e) {
            Slog.e(TAG, "remove: failed" + e.getMessage());
            return 6;
        }
    }

    public ArrayList<Integer> setActiveGroup(int userId) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            return null;
        }
        try {
            return daemon.setActiveGroup(userId);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to setActiveGroup():" + e.getMessage());
            return null;
        }
    }

    public int authenticate(long sessionId, int userId) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "authenticate: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.authenticate(sessionId, userId);
        } catch (RemoteException e) {
            Slog.e(TAG, "authenticate: failed" + e.getMessage());
            return 6;
        }
    }

    public int sendImageData(String data) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "sendImageData: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.sendImageData(data);
        } catch (RemoteException e) {
            Slog.e(TAG, "sendImageData: failed" + e.getMessage());
            return 6;
        }
    }

    public int setSecureFaceMode(int mode) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "setSecureFaceMode: no face recognize HAL!");
            return 6;
        }
        try {
            int val = daemon.setSecureFaceMode(mode);
            Slog.d(TAG, "daemon setSecureFaceMode result val = " + val);
            return val;
        } catch (RemoteException e) {
            Slog.e(TAG, "setSecureFaceMode: failed" + e.getMessage());
            return 6;
        }
    }

    public int getAngleDim() {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "getAngleDim: no face recognize HAL!");
            return 0;
        }
        try {
            return daemon.getAngleDim();
        } catch (RemoteException e) {
            Slog.e(TAG, "getAngleDim failed" + e.getMessage());
            return 0;
        }
    }

    public int setFlag(int flag) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "setFlag: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.setFlag(flag);
        } catch (RemoteException e) {
            Slog.e(TAG, "setFlag: failed" + e.getMessage());
            return 6;
        }
    }

    public int prepare(ArrayList<Byte> aaidList, ArrayList<Byte> nonceList, ArrayList<Byte> extralist) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "prepare: no face recognize HAL!");
            return 6;
        }
        try {
            return daemon.prepare(aaidList, nonceList, extralist);
        } catch (RemoteException e) {
            Slog.e(TAG, "prepare failed" + e.getMessage());
            return 6;
        }
    }

    public void getUvt(AuthPayResultCallbackWrapper authPayResultWrapper) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "getUvt: no face recognize HAL!");
            return;
        }
        try {
            AuthPayResultCallback authPayResult = new AuthPayResultCallback();
            daemon.getUvt(authPayResult);
            authPayResultWrapper.faceId = authPayResult.faceId;
            authPayResultWrapper.result = authPayResult.result;
            authPayResultWrapper.tokenResult = authPayResult.tokenResult;
            authPayResultWrapper.reserve = authPayResult.reserve;
        } catch (RemoteException e) {
            Slog.e(TAG, "getUvt failed" + e.getMessage());
        }
    }

    public int sendCommand(int cmd, ArrayList<Byte> parm) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "sendCommand: no face recognize HAL!");
            return -1;
        }
        try {
            return daemon.sendCommand(cmd, parm);
        } catch (RemoteException e) {
            Slog.e(TAG, "sendCommand failed" + e.getMessage());
            return -1;
        }
    }

    public void getStatus(int statusId, GetStatusResultCallbackWrapper getStatusResultWrapper) {
        IBiometricsFaceRecognize daemon = getFaceRecognizeDaemon();
        if (daemon == null) {
            Slog.w(TAG, "getStatus: no face recognize HAL!");
            return;
        }
        try {
            GetStatusResultCallback getStatusResult = new GetStatusResultCallback();
            daemon.getStatus(statusId, getStatusResult);
            getStatusResultWrapper.result = getStatusResult.result;
            getStatusResultWrapper.status1 = getStatusResult.status1;
            getStatusResultWrapper.status2 = getStatusResult.status2;
            getStatusResultWrapper.reserve = getStatusResult.reserve;
        } catch (RemoteException e) {
            Slog.e(TAG, "getStatus failed" + e.getMessage());
        }
    }

    private class FaceIdHidlCallback extends IBiometricsFaceRecognizeClientCallback.Stub {
        private FaceRecognizeCallbackWarpper mFaceRecognizeCallbackWarpper;

        private FaceIdHidlCallback(FaceRecognizeCallbackWarpper callback) {
            this.mFaceRecognizeCallbackWarpper = callback;
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onEnrollResult(int faceId, int userId, int errorCode) {
            this.mFaceRecognizeCallbackWarpper.onEnrollResult(faceId, userId, errorCode);
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onEnrollAcquired(int acquiredInfo, int process) {
            this.mFaceRecognizeCallbackWarpper.onEnrollAcquired(acquiredInfo, process);
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onAuthenticationResult(int userId, int errorCode) {
            this.mFaceRecognizeCallbackWarpper.onAuthenticationResult(userId, errorCode);
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onAuthenticationAcquired(int acquiredInfo) {
            this.mFaceRecognizeCallbackWarpper.onAuthenticationAcquired(acquiredInfo);
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onRemovedResult(int userId, int errorCode) {
            this.mFaceRecognizeCallbackWarpper.onRemovedResult(userId, errorCode);
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onCancel(int type) {
            this.mFaceRecognizeCallbackWarpper.onCancel(type);
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onInitResult(int errorCode) {
            this.mFaceRecognizeCallbackWarpper.onInitResult(errorCode);
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeClientCallback
        public void onReleaseResult(int errorCode) {
            this.mFaceRecognizeCallbackWarpper.onReleaseResult(errorCode);
        }
    }

    private class FaceIdHidlDataCallback extends IBiometricsFaceRecognizeDataCallback.Stub {
        private FaceRecognizeDataCallbackWarpper mFaceRecognizeDataCallbackWarpper;

        private FaceIdHidlDataCallback(FaceRecognizeDataCallbackWarpper callback) {
            this.mFaceRecognizeDataCallbackWarpper = callback;
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeDataCallback
        public int sendBigData(int dataType, int dataSize, NativeHandle memHdl, ArrayList<Byte> extra) {
            if (memHdl == null) {
                Slog.e(FaceHidlAdapter.TAG, "sendBigDate: bad param");
                return -1;
            } else if (memHdl.hasSingleFileDescriptor()) {
                return this.mFaceRecognizeDataCallbackWarpper.sendBigData(dataType, dataSize, memHdl.getFileDescriptor(), extra);
            } else {
                Slog.e(FaceHidlAdapter.TAG, "sendBigData: not support muilty fds");
                return -1;
            }
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognizeDataCallback
        public int sendData(int dataType, ArrayList<Byte> data, ArrayList<Byte> extra) {
            return this.mFaceRecognizeDataCallbackWarpper.sendData(dataType, data, extra);
        }
    }

    private class FaceIdHidlDeathRecipient implements IHwBinder.DeathRecipient {
        private FaceIdHidlServiceDiedCallbackWarpper mFaceIdServiceDiedCallback;

        private FaceIdHidlDeathRecipient(FaceIdHidlServiceDiedCallbackWarpper callback) {
            this.mFaceIdServiceDiedCallback = callback;
        }

        public void serviceDied(long cookie) {
            Log.e(FaceHidlAdapter.TAG, "face recognize HAL died");
            synchronized (FaceHidlAdapter.sHidlLock) {
                FaceHidlAdapter.this.mFaceIdDaemon = null;
                FaceHidlAdapter.this.mHalDeviceId = 0;
                this.mFaceIdServiceDiedCallback.onServiceDied();
            }
        }
    }

    private static class AuthPayResultCallback implements IBiometricsFaceRecognize.getUvtCallback {
        public int faceId;
        public ArrayList<Byte> reserve;
        public int result;
        public ArrayList<Byte> tokenResult;

        private AuthPayResultCallback() {
            this.result = 1;
            this.faceId = -1;
            this.tokenResult = null;
            this.reserve = null;
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognize.getUvtCallback
        public void onValues(int paramInt1, int paramInt2, ArrayList<Byte> paramArrayList, ArrayList<Byte> paramArrayList1) {
            this.result = paramInt1;
            this.faceId = paramInt2;
            this.tokenResult = paramArrayList;
            this.reserve = paramArrayList1;
        }
    }

    private static class GetStatusResultCallback implements IBiometricsFaceRecognize.getStatusCallback {
        public ArrayList<Byte> reserve;
        public int result;
        public int status1;
        public int status2;

        private GetStatusResultCallback() {
            this.result = 1;
            this.status1 = -1;
            this.status2 = -1;
            this.reserve = null;
        }

        @Override // vendor.huawei.hardware.biometrics.hwfacerecognize.V2_0.IBiometricsFaceRecognize.getStatusCallback
        public void onValues(int ret, int paramInt1, int paramInt2, ArrayList<Byte> paramArrayList) {
            this.result = ret;
            this.status1 = paramInt1;
            this.status2 = paramInt2;
            this.reserve = paramArrayList;
        }
    }
}
