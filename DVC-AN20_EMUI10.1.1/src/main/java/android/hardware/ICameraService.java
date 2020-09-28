package android.hardware;

import android.hardware.ICamera;
import android.hardware.ICameraClient;
import android.hardware.ICameraServiceListener;
import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.VendorTagDescriptor;
import android.hardware.camera2.params.VendorTagDescriptorCache;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;

public interface ICameraService extends IInterface {
    public static final int API_VERSION_1 = 1;
    public static final int API_VERSION_2 = 2;
    public static final int CAMERA_HAL_API_VERSION_UNSPECIFIED = -1;
    public static final int CAMERA_TYPE_ALL = 1;
    public static final int CAMERA_TYPE_BACKWARD_COMPATIBLE = 0;
    public static final int DEVICE_STATE_BACK_COVERED = 1;
    public static final int DEVICE_STATE_FOLDED = 4;
    public static final int DEVICE_STATE_FRONT_COVERED = 2;
    public static final int DEVICE_STATE_LAST_FRAMEWORK_BIT = Integer.MIN_VALUE;
    public static final int DEVICE_STATE_NORMAL = 0;
    public static final int ERROR_ALREADY_EXISTS = 2;
    public static final int ERROR_CAMERA_IN_USE = 7;
    public static final int ERROR_DEPRECATED_HAL = 9;
    public static final int ERROR_DISABLED = 6;
    public static final int ERROR_DISCONNECTED = 4;
    public static final int ERROR_ILLEGAL_ARGUMENT = 3;
    public static final int ERROR_INVALID_OPERATION = 10;
    public static final int ERROR_MAX_CAMERAS_IN_USE = 8;
    public static final int ERROR_PERMISSION_DENIED = 1;
    public static final int ERROR_TIMED_OUT = 5;
    public static final int EVENT_NONE = 0;
    public static final int EVENT_USER_SWITCHED = 1;
    public static final int USE_CALLING_PID = -1;
    public static final int USE_CALLING_UID = -1;

    CameraStatus[] addListener(ICameraServiceListener iCameraServiceListener) throws RemoteException;

    ICamera connect(ICameraClient iCameraClient, int i, String str, int i2, int i3) throws RemoteException;

    ICameraDeviceUser connectDevice(ICameraDeviceCallbacks iCameraDeviceCallbacks, String str, String str2, int i) throws RemoteException;

    ICamera connectLegacy(ICameraClient iCameraClient, int i, int i2, String str, int i3) throws RemoteException;

    CameraMetadataNative getCameraCharacteristics(String str) throws RemoteException;

    CameraInfo getCameraInfo(int i) throws RemoteException;

    VendorTagDescriptorCache getCameraVendorTagCache() throws RemoteException;

    VendorTagDescriptor getCameraVendorTagDescriptor() throws RemoteException;

    String getLegacyParameters(int i) throws RemoteException;

    int getNumberOfCameras(int i) throws RemoteException;

    boolean isHiddenPhysicalCamera(String str) throws RemoteException;

    void notifyDeviceStateChange(long j) throws RemoteException;

    void notifySystemEvent(int i, int[] iArr) throws RemoteException;

    void removeListener(ICameraServiceListener iCameraServiceListener) throws RemoteException;

    boolean requestPreviewImage(String str, Surface surface) throws RemoteException;

    void setCommand(String str, String str2) throws RemoteException;

    void setTorchMode(String str, boolean z, IBinder iBinder) throws RemoteException;

    boolean supportsCameraApi(String str, int i) throws RemoteException;

    public static class Default implements ICameraService {
        @Override // android.hardware.ICameraService
        public int getNumberOfCameras(int type) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.ICameraService
        public CameraInfo getCameraInfo(int cameraId) throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public ICamera connect(ICameraClient client, int cameraId, String opPackageName, int clientUid, int clientPid) throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public ICameraDeviceUser connectDevice(ICameraDeviceCallbacks callbacks, String cameraId, String opPackageName, int clientUid) throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public ICamera connectLegacy(ICameraClient client, int cameraId, int halVersion, String opPackageName, int clientUid) throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public CameraStatus[] addListener(ICameraServiceListener listener) throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public void removeListener(ICameraServiceListener listener) throws RemoteException {
        }

        @Override // android.hardware.ICameraService
        public CameraMetadataNative getCameraCharacteristics(String cameraId) throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public VendorTagDescriptor getCameraVendorTagDescriptor() throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public VendorTagDescriptorCache getCameraVendorTagCache() throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public String getLegacyParameters(int cameraId) throws RemoteException {
            return null;
        }

        @Override // android.hardware.ICameraService
        public boolean supportsCameraApi(String cameraId, int apiVersion) throws RemoteException {
            return false;
        }

        @Override // android.hardware.ICameraService
        public boolean isHiddenPhysicalCamera(String cameraId) throws RemoteException {
            return false;
        }

        @Override // android.hardware.ICameraService
        public void setTorchMode(String cameraId, boolean enabled, IBinder clientBinder) throws RemoteException {
        }

        @Override // android.hardware.ICameraService
        public void notifySystemEvent(int eventId, int[] args) throws RemoteException {
        }

        @Override // android.hardware.ICameraService
        public void notifyDeviceStateChange(long newState) throws RemoteException {
        }

        @Override // android.hardware.ICameraService
        public void setCommand(String commandType, String commandValue) throws RemoteException {
        }

        @Override // android.hardware.ICameraService
        public boolean requestPreviewImage(String cameraId, Surface surface) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICameraService {
        private static final String DESCRIPTOR = "android.hardware.ICameraService";
        static final int TRANSACTION_addListener = 6;
        static final int TRANSACTION_connect = 3;
        static final int TRANSACTION_connectDevice = 4;
        static final int TRANSACTION_connectLegacy = 5;
        static final int TRANSACTION_getCameraCharacteristics = 8;
        static final int TRANSACTION_getCameraInfo = 2;
        static final int TRANSACTION_getCameraVendorTagCache = 10;
        static final int TRANSACTION_getCameraVendorTagDescriptor = 9;
        static final int TRANSACTION_getLegacyParameters = 11;
        static final int TRANSACTION_getNumberOfCameras = 1;
        static final int TRANSACTION_isHiddenPhysicalCamera = 13;
        static final int TRANSACTION_notifyDeviceStateChange = 16;
        static final int TRANSACTION_notifySystemEvent = 15;
        static final int TRANSACTION_removeListener = 7;
        static final int TRANSACTION_requestPreviewImage = 18;
        static final int TRANSACTION_setCommand = 17;
        static final int TRANSACTION_setTorchMode = 14;
        static final int TRANSACTION_supportsCameraApi = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICameraService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICameraService)) {
                return new Proxy(obj);
            }
            return (ICameraService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getNumberOfCameras";
                case 2:
                    return "getCameraInfo";
                case 3:
                    return "connect";
                case 4:
                    return "connectDevice";
                case 5:
                    return "connectLegacy";
                case 6:
                    return "addListener";
                case 7:
                    return "removeListener";
                case 8:
                    return "getCameraCharacteristics";
                case 9:
                    return "getCameraVendorTagDescriptor";
                case 10:
                    return "getCameraVendorTagCache";
                case 11:
                    return "getLegacyParameters";
                case 12:
                    return "supportsCameraApi";
                case 13:
                    return "isHiddenPhysicalCamera";
                case 14:
                    return "setTorchMode";
                case 15:
                    return "notifySystemEvent";
                case 16:
                    return "notifyDeviceStateChange";
                case 17:
                    return "setCommand";
                case 18:
                    return "requestPreviewImage";
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
            Surface _arg1;
            if (code != 1598968902) {
                IBinder iBinder = null;
                IBinder iBinder2 = null;
                IBinder iBinder3 = null;
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getNumberOfCameras(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        CameraInfo _result2 = getCameraInfo(data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ICamera _result3 = connect(ICameraClient.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        ICameraDeviceUser _result4 = connectDevice(ICameraDeviceCallbacks.Stub.asInterface(data.readStrongBinder()), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            iBinder3 = _result4.asBinder();
                        }
                        reply.writeStrongBinder(iBinder3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        ICamera _result5 = connectLegacy(ICameraClient.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            iBinder2 = _result5.asBinder();
                        }
                        reply.writeStrongBinder(iBinder2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        CameraStatus[] _result6 = addListener(ICameraServiceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeTypedArray(_result6, 1);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        removeListener(ICameraServiceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        CameraMetadataNative _result7 = getCameraCharacteristics(data.readString());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        VendorTagDescriptor _result8 = getCameraVendorTagDescriptor();
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        VendorTagDescriptorCache _result9 = getCameraVendorTagCache();
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _result10 = getLegacyParameters(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supportsCameraApi = supportsCameraApi(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(supportsCameraApi ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHiddenPhysicalCamera = isHiddenPhysicalCamera(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isHiddenPhysicalCamera ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setTorchMode(_arg0, _arg12, data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        notifySystemEvent(data.readInt(), data.createIntArray());
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        notifyDeviceStateChange(data.readLong());
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        setCommand(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean requestPreviewImage = requestPreviewImage(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeInt(requestPreviewImage ? 1 : 0);
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
        public static class Proxy implements ICameraService {
            public static ICameraService sDefaultImpl;
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

            @Override // android.hardware.ICameraService
            public int getNumberOfCameras(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNumberOfCameras(type);
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

            @Override // android.hardware.ICameraService
            public CameraInfo getCameraInfo(int cameraId) throws RemoteException {
                CameraInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cameraId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCameraInfo(cameraId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CameraInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.hardware.ICameraService
            public ICamera connect(ICameraClient client, int cameraId, String opPackageName, int clientUid, int clientPid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(cameraId);
                    _data.writeString(opPackageName);
                    _data.writeInt(clientUid);
                    _data.writeInt(clientPid);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connect(client, cameraId, opPackageName, clientUid, clientPid);
                    }
                    _reply.readException();
                    ICamera _result = ICamera.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public ICameraDeviceUser connectDevice(ICameraDeviceCallbacks callbacks, String cameraId, String opPackageName, int clientUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    _data.writeString(cameraId);
                    _data.writeString(opPackageName);
                    _data.writeInt(clientUid);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectDevice(callbacks, cameraId, opPackageName, clientUid);
                    }
                    _reply.readException();
                    ICameraDeviceUser _result = ICameraDeviceUser.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public ICamera connectLegacy(ICameraClient client, int cameraId, int halVersion, String opPackageName, int clientUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    _data.writeInt(cameraId);
                    _data.writeInt(halVersion);
                    _data.writeString(opPackageName);
                    _data.writeInt(clientUid);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectLegacy(client, cameraId, halVersion, opPackageName, clientUid);
                    }
                    _reply.readException();
                    ICamera _result = ICamera.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public CameraStatus[] addListener(ICameraServiceListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addListener(listener);
                    }
                    _reply.readException();
                    CameraStatus[] _result = (CameraStatus[]) _reply.createTypedArray(CameraStatus.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public void removeListener(ICameraServiceListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public CameraMetadataNative getCameraCharacteristics(String cameraId) throws RemoteException {
                CameraMetadataNative _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCameraCharacteristics(cameraId);
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

            @Override // android.hardware.ICameraService
            public VendorTagDescriptor getCameraVendorTagDescriptor() throws RemoteException {
                VendorTagDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCameraVendorTagDescriptor();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VendorTagDescriptor.CREATOR.createFromParcel(_reply);
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

            @Override // android.hardware.ICameraService
            public VendorTagDescriptorCache getCameraVendorTagCache() throws RemoteException {
                VendorTagDescriptorCache _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCameraVendorTagCache();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = VendorTagDescriptorCache.CREATOR.createFromParcel(_reply);
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

            @Override // android.hardware.ICameraService
            public String getLegacyParameters(int cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cameraId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLegacyParameters(cameraId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public boolean supportsCameraApi(String cameraId, int apiVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    _data.writeInt(apiVersion);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportsCameraApi(cameraId, apiVersion);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public boolean isHiddenPhysicalCamera(String cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHiddenPhysicalCamera(cameraId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public void setTorchMode(String cameraId, boolean enabled, IBinder clientBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeStrongBinder(clientBinder);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTorchMode(cameraId, enabled, clientBinder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public void notifySystemEvent(int eventId, int[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    _data.writeIntArray(args);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifySystemEvent(eventId, args);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public void notifyDeviceStateChange(long newState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(newState);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyDeviceStateChange(newState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public void setCommand(String commandType, String commandValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(commandType);
                    _data.writeString(commandValue);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCommand(commandType, commandValue);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ICameraService
            public boolean requestPreviewImage(String cameraId, Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    boolean _result = true;
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestPreviewImage(cameraId, surface);
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
        }

        public static boolean setDefaultImpl(ICameraService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICameraService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
