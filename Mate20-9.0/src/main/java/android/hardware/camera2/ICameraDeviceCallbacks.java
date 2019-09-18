package android.hardware.camera2;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PhysicalCaptureResultInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICameraDeviceCallbacks extends IInterface {
    public static final int ERROR_CAMERA_BUFFER = 5;
    public static final int ERROR_CAMERA_DEVICE = 1;
    public static final int ERROR_CAMERA_DISABLED = 6;
    public static final int ERROR_CAMERA_DISCONNECTED = 0;
    public static final int ERROR_CAMERA_INVALID_ERROR = -1;
    public static final int ERROR_CAMERA_REQUEST = 3;
    public static final int ERROR_CAMERA_RESULT = 4;
    public static final int ERROR_CAMERA_SERVICE = 2;

    public static abstract class Stub extends Binder implements ICameraDeviceCallbacks {
        private static final String DESCRIPTOR = "android.hardware.camera2.ICameraDeviceCallbacks";
        static final int TRANSACTION_onCaptureStarted = 3;
        static final int TRANSACTION_onDeviceError = 1;
        static final int TRANSACTION_onDeviceIdle = 2;
        static final int TRANSACTION_onPrepared = 5;
        static final int TRANSACTION_onRepeatingRequestError = 6;
        static final int TRANSACTION_onRequestQueueEmpty = 7;
        static final int TRANSACTION_onResultReceived = 4;

        private static class Proxy implements ICameraDeviceCallbacks {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onDeviceError(int errorCode, CaptureResultExtras resultExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (resultExtras != null) {
                        _data.writeInt(1);
                        resultExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resultExtras != null) {
                        _data.writeInt(1);
                        resultExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(timestamp);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras, PhysicalCaptureResultInfo[] physicalCaptureResultInfos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (resultExtras != null) {
                        _data.writeInt(1);
                        resultExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedArray(physicalCaptureResultInfos, 0);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepared(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRepeatingRequestError(long lastFrameNumber, int repeatingRequestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(lastFrameNumber);
                    _data.writeInt(repeatingRequestId);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRequestQueueEmpty() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICameraDeviceCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICameraDeviceCallbacks)) {
                return new Proxy(obj);
            }
            return (ICameraDeviceCallbacks) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CameraMetadataNative _arg0;
            if (code != 1598968902) {
                CaptureResultExtras _arg1 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = CaptureResultExtras.CREATOR.createFromParcel(data);
                        }
                        onDeviceError(_arg02, _arg1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onDeviceIdle();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = CaptureResultExtras.CREATOR.createFromParcel(data);
                        }
                        onCaptureStarted(_arg1, data.readLong());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CameraMetadataNative.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = CaptureResultExtras.CREATOR.createFromParcel(data);
                        }
                        onResultReceived(_arg0, _arg1, (PhysicalCaptureResultInfo[]) data.createTypedArray(PhysicalCaptureResultInfo.CREATOR));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onPrepared(data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onRepeatingRequestError(data.readLong(), data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onRequestQueueEmpty();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onCaptureStarted(CaptureResultExtras captureResultExtras, long j) throws RemoteException;

    void onDeviceError(int i, CaptureResultExtras captureResultExtras) throws RemoteException;

    void onDeviceIdle() throws RemoteException;

    void onPrepared(int i) throws RemoteException;

    void onRepeatingRequestError(long j, int i) throws RemoteException;

    void onRequestQueueEmpty() throws RemoteException;

    void onResultReceived(CameraMetadataNative cameraMetadataNative, CaptureResultExtras captureResultExtras, PhysicalCaptureResultInfo[] physicalCaptureResultInfoArr) throws RemoteException;
}
