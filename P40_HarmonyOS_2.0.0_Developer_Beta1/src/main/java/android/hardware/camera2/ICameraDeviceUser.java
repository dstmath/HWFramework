package android.hardware.camera2;

import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
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

    boolean isSessionConfigurationSupported(SessionConfiguration sessionConfiguration) throws RemoteException;

    void prepare(int i) throws RemoteException;

    void prepare2(int i, int i2) throws RemoteException;

    SubmitInfo submitRequest(CaptureRequest captureRequest, boolean z) throws RemoteException;

    SubmitInfo submitRequestList(CaptureRequest[] captureRequestArr, boolean z) throws RemoteException;

    void tearDown(int i) throws RemoteException;

    void updateOutputConfiguration(int i, OutputConfiguration outputConfiguration) throws RemoteException;

    void waitUntilIdle() throws RemoteException;

    public static class Default implements ICameraDeviceUser {
        @Override // android.hardware.camera2.ICameraDeviceUser
        public void disconnect() throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public SubmitInfo submitRequest(CaptureRequest request, boolean streaming) throws RemoteException {
            return null;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public SubmitInfo submitRequestList(CaptureRequest[] requestList, boolean streaming) throws RemoteException {
            return null;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public long cancelRequest(int requestId) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void beginConfigure() throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void endConfigure(int operatingMode, CameraMetadataNative sessionParams) throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public boolean isSessionConfigurationSupported(SessionConfiguration sessionConfiguration) throws RemoteException {
            return false;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void deleteStream(int streamId) throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public int createStream(OutputConfiguration outputConfiguration) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public int createInputStream(int width, int height, int format) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public Surface getInputSurface() throws RemoteException {
            return null;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public CameraMetadataNative createDefaultRequest(int templateId) throws RemoteException {
            return null;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public CameraMetadataNative getCameraInfo() throws RemoteException {
            return null;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void waitUntilIdle() throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public long flush() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void prepare(int streamId) throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void tearDown(int streamId) throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void prepare2(int maxCount, int streamId) throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void updateOutputConfiguration(int streamId, OutputConfiguration outputConfiguration) throws RemoteException {
        }

        @Override // android.hardware.camera2.ICameraDeviceUser
        public void finalizeOutputConfigurations(int streamId, OutputConfiguration outputConfiguration) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICameraDeviceUser {
        private static final String DESCRIPTOR = "android.hardware.camera2.ICameraDeviceUser";
        static final int TRANSACTION_beginConfigure = 5;
        static final int TRANSACTION_cancelRequest = 4;
        static final int TRANSACTION_createDefaultRequest = 12;
        static final int TRANSACTION_createInputStream = 10;
        static final int TRANSACTION_createStream = 9;
        static final int TRANSACTION_deleteStream = 8;
        static final int TRANSACTION_disconnect = 1;
        static final int TRANSACTION_endConfigure = 6;
        static final int TRANSACTION_finalizeOutputConfigurations = 20;
        static final int TRANSACTION_flush = 15;
        static final int TRANSACTION_getCameraInfo = 13;
        static final int TRANSACTION_getInputSurface = 11;
        static final int TRANSACTION_isSessionConfigurationSupported = 7;
        static final int TRANSACTION_prepare = 16;
        static final int TRANSACTION_prepare2 = 18;
        static final int TRANSACTION_submitRequest = 2;
        static final int TRANSACTION_submitRequestList = 3;
        static final int TRANSACTION_tearDown = 17;
        static final int TRANSACTION_updateOutputConfiguration = 19;
        static final int TRANSACTION_waitUntilIdle = 14;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "disconnect";
                case 2:
                    return "submitRequest";
                case 3:
                    return "submitRequestList";
                case 4:
                    return "cancelRequest";
                case 5:
                    return "beginConfigure";
                case 6:
                    return "endConfigure";
                case 7:
                    return "isSessionConfigurationSupported";
                case 8:
                    return "deleteStream";
                case 9:
                    return "createStream";
                case 10:
                    return "createInputStream";
                case 11:
                    return "getInputSurface";
                case 12:
                    return "createDefaultRequest";
                case 13:
                    return "getCameraInfo";
                case 14:
                    return "waitUntilIdle";
                case 15:
                    return "flush";
                case 16:
                    return "prepare";
                case 17:
                    return "tearDown";
                case 18:
                    return "prepare2";
                case 19:
                    return "updateOutputConfiguration";
                case 20:
                    return "finalizeOutputConfigurations";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            CaptureRequest _arg0;
            CameraMetadataNative _arg1;
            SessionConfiguration _arg02;
            OutputConfiguration _arg03;
            OutputConfiguration _arg12;
            OutputConfiguration _arg13;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        disconnect();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CaptureRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        SubmitInfo _result = submitRequest(_arg0, data.readInt() != 0);
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
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = CameraMetadataNative.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        endConfigure(_arg04, _arg1);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = SessionConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean isSessionConfigurationSupported = isSessionConfigurationSupported(_arg02);
                        reply.writeNoException();
                        reply.writeInt(isSessionConfigurationSupported ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        deleteStream(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = OutputConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _result4 = createStream(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = createInputStream(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 11:
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
                    case 12:
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
                    case 13:
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
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        waitUntilIdle();
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        long _result9 = flush();
                        reply.writeNoException();
                        reply.writeLong(_result9);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        prepare(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        tearDown(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        prepare2(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = OutputConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        updateOutputConfiguration(_arg05, _arg12);
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = OutputConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        finalizeOutputConfigurations(_arg06, _arg13);
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

        /* access modifiers changed from: private */
        public static class Proxy implements ICameraDeviceUser {
            public static ICameraDeviceUser sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public void disconnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disconnect();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public SubmitInfo submitRequest(CaptureRequest request, boolean streaming) throws RemoteException {
                SubmitInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!streaming) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().submitRequest(request, streaming);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SubmitInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public SubmitInfo submitRequestList(CaptureRequest[] requestList, boolean streaming) throws RemoteException {
                SubmitInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(requestList, 0);
                    _data.writeInt(streaming ? 1 : 0);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().submitRequestList(requestList, streaming);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SubmitInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public long cancelRequest(int requestId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancelRequest(requestId);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public void beginConfigure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().beginConfigure();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
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
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().endConfigure(operatingMode, sessionParams);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public boolean isSessionConfigurationSupported(SessionConfiguration sessionConfiguration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (sessionConfiguration != null) {
                        _data.writeInt(1);
                        sessionConfiguration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSessionConfigurationSupported(sessionConfiguration);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public void deleteStream(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteStream(streamId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
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
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createStream(outputConfiguration);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public int createInputStream(int width, int height, int format) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(format);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createInputStream(width, height, format);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public Surface getInputSurface() throws RemoteException {
                Surface _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInputSurface();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Surface.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public CameraMetadataNative createDefaultRequest(int templateId) throws RemoteException {
                CameraMetadataNative _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(templateId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createDefaultRequest(templateId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CameraMetadataNative.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public CameraMetadataNative getCameraInfo() throws RemoteException {
                CameraMetadataNative _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCameraInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CameraMetadataNative.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public void waitUntilIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().waitUntilIdle();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public long flush() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().flush();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public void prepare(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepare(streamId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public void tearDown(int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamId);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tearDown(streamId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
            public void prepare2(int maxCount, int streamId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxCount);
                    _data.writeInt(streamId);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepare2(maxCount, streamId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
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
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateOutputConfiguration(streamId, outputConfiguration);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.camera2.ICameraDeviceUser
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
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finalizeOutputConfigurations(streamId, outputConfiguration);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICameraDeviceUser impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICameraDeviceUser getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
