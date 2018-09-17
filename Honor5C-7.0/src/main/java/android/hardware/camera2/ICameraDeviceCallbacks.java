package android.hardware.camera2;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICameraDeviceCallbacks extends IInterface {
    public static final int ERROR_CAMERA_BUFFER = 5;
    public static final int ERROR_CAMERA_DEVICE = 1;
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
                        _data.writeInt(Stub.TRANSACTION_onDeviceError);
                        resultExtras.writeToParcel(_data, ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    } else {
                        _data.writeInt(ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onDeviceError, _data, null, Stub.TRANSACTION_onDeviceError);
                } finally {
                    _data.recycle();
                }
            }

            public void onDeviceIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onDeviceIdle, _data, null, Stub.TRANSACTION_onDeviceError);
                } finally {
                    _data.recycle();
                }
            }

            public void onCaptureStarted(CaptureResultExtras resultExtras, long timestamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resultExtras != null) {
                        _data.writeInt(Stub.TRANSACTION_onDeviceError);
                        resultExtras.writeToParcel(_data, ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    } else {
                        _data.writeInt(ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    }
                    _data.writeLong(timestamp);
                    this.mRemote.transact(Stub.TRANSACTION_onCaptureStarted, _data, null, Stub.TRANSACTION_onDeviceError);
                } finally {
                    _data.recycle();
                }
            }

            public void onResultReceived(CameraMetadataNative result, CaptureResultExtras resultExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(Stub.TRANSACTION_onDeviceError);
                        result.writeToParcel(_data, ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    } else {
                        _data.writeInt(ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    }
                    if (resultExtras != null) {
                        _data.writeInt(Stub.TRANSACTION_onDeviceError);
                        resultExtras.writeToParcel(_data, ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    } else {
                        _data.writeInt(ICameraDeviceCallbacks.ERROR_CAMERA_DISCONNECTED);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onResultReceived, _data, null, Stub.TRANSACTION_onDeviceError);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrepared(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    this.mRemote.transact(Stub.TRANSACTION_onPrepared, _data, null, Stub.TRANSACTION_onDeviceError);
                } finally {
                    _data.recycle();
                }
            }

            public void onRepeatingRequestError(long lastFrameNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(lastFrameNumber);
                    this.mRemote.transact(Stub.TRANSACTION_onRepeatingRequestError, _data, null, Stub.TRANSACTION_onDeviceError);
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
            CaptureResultExtras captureResultExtras;
            switch (code) {
                case TRANSACTION_onDeviceError /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        captureResultExtras = (CaptureResultExtras) CaptureResultExtras.CREATOR.createFromParcel(data);
                    } else {
                        captureResultExtras = null;
                    }
                    onDeviceError(_arg0, captureResultExtras);
                    return true;
                case TRANSACTION_onDeviceIdle /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDeviceIdle();
                    return true;
                case TRANSACTION_onCaptureStarted /*3*/:
                    CaptureResultExtras captureResultExtras2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        captureResultExtras2 = (CaptureResultExtras) CaptureResultExtras.CREATOR.createFromParcel(data);
                    } else {
                        captureResultExtras2 = null;
                    }
                    onCaptureStarted(captureResultExtras2, data.readLong());
                    return true;
                case TRANSACTION_onResultReceived /*4*/:
                    CameraMetadataNative cameraMetadataNative;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        cameraMetadataNative = (CameraMetadataNative) CameraMetadataNative.CREATOR.createFromParcel(data);
                    } else {
                        cameraMetadataNative = null;
                    }
                    if (data.readInt() != 0) {
                        captureResultExtras = (CaptureResultExtras) CaptureResultExtras.CREATOR.createFromParcel(data);
                    } else {
                        captureResultExtras = null;
                    }
                    onResultReceived(cameraMetadataNative, captureResultExtras);
                    return true;
                case TRANSACTION_onPrepared /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onPrepared(data.readInt());
                    return true;
                case TRANSACTION_onRepeatingRequestError /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onRepeatingRequestError(data.readLong());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onCaptureStarted(CaptureResultExtras captureResultExtras, long j) throws RemoteException;

    void onDeviceError(int i, CaptureResultExtras captureResultExtras) throws RemoteException;

    void onDeviceIdle() throws RemoteException;

    void onPrepared(int i) throws RemoteException;

    void onRepeatingRequestError(long j) throws RemoteException;

    void onResultReceived(CameraMetadataNative cameraMetadataNative, CaptureResultExtras captureResultExtras) throws RemoteException;
}
