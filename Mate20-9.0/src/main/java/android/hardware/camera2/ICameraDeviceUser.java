package android.hardware.camera2;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.utils.SubmitInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;

public interface ICameraDeviceUser extends IInterface {
    public static final int CONSTRAINED_HIGH_SPEED_MODE = 1;
    public static final int NORMAL_MODE = 0;
    public static final int NO_IN_FLIGHT_REPEATING_FRAMES = -1;
    public static final int TEMPLATE_MANUAL = 6;
    public static final int TEMPLATE_PREVIEW = 1;
    public static final int TEMPLATE_RECORD = 3;
    public static final int TEMPLATE_STILL_CAPTURE = 2;
    public static final int TEMPLATE_VIDEO_SNAPSHOT = 4;
    public static final int TEMPLATE_ZERO_SHUTTER_LAG = 5;
    public static final int VENDOR_MODE_START = 32768;

    public static abstract class Stub extends Binder implements ICameraDeviceUser {
        private static final String DESCRIPTOR = "android.hardware.camera2.ICameraDeviceUser";
        static final int TRANSACTION_beginConfigure = 5;
        static final int TRANSACTION_cancelRequest = 4;
        static final int TRANSACTION_createDefaultRequest = 11;
        static final int TRANSACTION_createInputStream = 9;
        static final int TRANSACTION_createStream = 8;
        static final int TRANSACTION_deleteStream = 7;
        static final int TRANSACTION_disconnect = 1;
        static final int TRANSACTION_endConfigure = 6;
        static final int TRANSACTION_finalizeOutputConfigurations = 19;
        static final int TRANSACTION_flush = 14;
        static final int TRANSACTION_getCameraInfo = 12;
        static final int TRANSACTION_getInputSurface = 10;
        static final int TRANSACTION_prepare = 15;
        static final int TRANSACTION_prepare2 = 17;
        static final int TRANSACTION_submitRequest = 2;
        static final int TRANSACTION_submitRequestList = 3;
        static final int TRANSACTION_tearDown = 16;
        static final int TRANSACTION_updateOutputConfiguration = 18;
        static final int TRANSACTION_waitUntilIdle = 13;

        private static class Proxy implements ICameraDeviceUser {
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

            public void disconnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SubmitInfo submitRequest(CaptureRequest request, boolean streaming) throws RemoteException {
                SubmitInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(streaming);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SubmitInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SubmitInfo submitRequestList(CaptureRequest[] requestList, boolean streaming) throws RemoteException {
                SubmitInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(requestList, 0);
                    _data.writeInt(streaming);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SubmitInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long cancelRequest(int requestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void beginConfigure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endConfigure(int operatingMode, CameraMetadataNative sessionParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(operatingMode);
                    if (sessionParams != null) {
                        _data.writeInt(1);
                        sessionParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteStream(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createStream(OutputConfiguration outputConfiguration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (outputConfiguration != null) {
                        _data.writeInt(1);
                        outputConfiguration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createInputStream(int width, int height, int format) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(format);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Surface getInputSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Surface) Surface.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CameraMetadataNative createDefaultRequest(int templateId) throws RemoteException {
                CameraMetadataNative _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(templateId);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CameraMetadataNative.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CameraMetadataNative getCameraInfo() throws RemoteException {
                CameraMetadataNative _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CameraMetadataNative.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void waitUntilIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long flush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepare(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void tearDown(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepare2(int maxCount, int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxCount);
                    _data.writeInt(streamId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateOutputConfiguration(int streamId, OutputConfiguration outputConfiguration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    if (outputConfiguration != null) {
                        _data.writeInt(1);
                        outputConfiguration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finalizeOutputConfigurations(int streamId, OutputConfiguration outputConfiguration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    if (outputConfiguration != null) {
                        _data.writeInt(1);
                        outputConfiguration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICameraDeviceUser asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICameraDeviceUser)) {
                return new Proxy(obj);
            }
            return (ICameraDeviceUser) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.hardware.camera2.CaptureRequest} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: android.hardware.camera2.impl.CameraMetadataNative} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: android.hardware.camera2.params.OutputConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v26, resolved type: android.hardware.camera2.params.OutputConfiguration} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v30, resolved type: android.hardware.camera2.params.OutputConfiguration} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v35 */
        /* JADX WARNING: type inference failed for: r1v36 */
        /* JADX WARNING: type inference failed for: r1v37 */
        /* JADX WARNING: type inference failed for: r1v38 */
        /* JADX WARNING: type inference failed for: r1v39 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg1 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        disconnect();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = CaptureRequest.CREATOR.createFromParcel(data);
                        }
                        SubmitInfo _result = submitRequest(_arg1, data.readInt() != 0);
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        SubmitInfo _result2 = submitRequestList((CaptureRequest[]) data.createTypedArray(CaptureRequest.CREATOR), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        long _result3 = cancelRequest(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        beginConfigure();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = CameraMetadataNative.CREATOR.createFromParcel(data);
                        }
                        endConfigure(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        deleteStream(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = OutputConfiguration.CREATOR.createFromParcel(data);
                        }
                        int _result4 = createStream(_arg1);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = createInputStream(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        Surface _result6 = getInputSurface();
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        CameraMetadataNative _result7 = createDefaultRequest(data.readInt());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        CameraMetadataNative _result8 = getCameraInfo();
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        waitUntilIdle();
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        long _result9 = flush();
                        reply.writeNoException();
                        reply.writeLong(_result9);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        prepare(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        tearDown(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        prepare2(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = OutputConfiguration.CREATOR.createFromParcel(data);
                        }
                        updateOutputConfiguration(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = OutputConfiguration.CREATOR.createFromParcel(data);
                        }
                        finalizeOutputConfigurations(_arg03, _arg1);
                        reply.writeNoException();
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

    void beginConfigure() throws RemoteException;

    long cancelRequest(int i) throws RemoteException;

    CameraMetadataNative createDefaultRequest(int i) throws RemoteException;

    int createInputStream(int i, int i2, int i3) throws RemoteException;

    int createStream(OutputConfiguration outputConfiguration) throws RemoteException;

    void deleteStream(int i) throws RemoteException;

    void disconnect() throws RemoteException;

    void endConfigure(int i, CameraMetadataNative cameraMetadataNative) throws RemoteException;

    void finalizeOutputConfigurations(int i, OutputConfiguration outputConfiguration) throws RemoteException;

    long flush() throws RemoteException;

    CameraMetadataNative getCameraInfo() throws RemoteException;

    Surface getInputSurface() throws RemoteException;

    void prepare(int i) throws RemoteException;

    void prepare2(int i, int i2) throws RemoteException;

    SubmitInfo submitRequest(CaptureRequest captureRequest, boolean z) throws RemoteException;

    SubmitInfo submitRequestList(CaptureRequest[] captureRequestArr, boolean z) throws RemoteException;

    void tearDown(int i) throws RemoteException;

    void updateOutputConfiguration(int i, OutputConfiguration outputConfiguration) throws RemoteException;

    void waitUntilIdle() throws RemoteException;
}
