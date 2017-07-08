package android.hardware;

import android.hardware.camera2.ICameraDeviceCallbacks;
import android.hardware.camera2.ICameraDeviceUser;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.VendorTagDescriptor;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICameraService extends IInterface {
    public static final int API_VERSION_1 = 1;
    public static final int API_VERSION_2 = 2;
    public static final int CAMERA_HAL_API_VERSION_UNSPECIFIED = -1;
    public static final int CAMERA_TYPE_ALL = 1;
    public static final int CAMERA_TYPE_BACKWARD_COMPATIBLE = 0;
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

    public static abstract class Stub extends Binder implements ICameraService {
        private static final String DESCRIPTOR = "android.hardware.ICameraService";
        static final int TRANSACTION_addListener = 6;
        static final int TRANSACTION_connect = 3;
        static final int TRANSACTION_connectDevice = 4;
        static final int TRANSACTION_connectLegacy = 5;
        static final int TRANSACTION_getCameraCharacteristics = 8;
        static final int TRANSACTION_getCameraInfo = 2;
        static final int TRANSACTION_getCameraVendorTagDescriptor = 9;
        static final int TRANSACTION_getLegacyParameters = 10;
        static final int TRANSACTION_getNumberOfCameras = 1;
        static final int TRANSACTION_notifySystemEvent = 13;
        static final int TRANSACTION_removeListener = 7;
        static final int TRANSACTION_setTorchMode = 12;
        static final int TRANSACTION_supportsCameraApi = 11;

        private static class Proxy implements ICameraService {
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

            public int getNumberOfCameras(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_getNumberOfCameras, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CameraInfo getCameraInfo(int cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CameraInfo cameraInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cameraId);
                    this.mRemote.transact(Stub.TRANSACTION_getCameraInfo, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        cameraInfo = (CameraInfo) CameraInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        cameraInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return cameraInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ICamera connect(ICameraClient client, int cameraId, String opPackageName, int clientUid, int clientPid) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(cameraId);
                    _data.writeString(opPackageName);
                    _data.writeInt(clientUid);
                    _data.writeInt(clientPid);
                    this.mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    ICamera _result = android.hardware.ICamera.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ICameraDeviceUser connectDevice(ICameraDeviceCallbacks callbacks, int cameraId, String opPackageName, int clientUid) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(cameraId);
                    _data.writeString(opPackageName);
                    _data.writeInt(clientUid);
                    this.mRemote.transact(Stub.TRANSACTION_connectDevice, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    ICameraDeviceUser _result = android.hardware.camera2.ICameraDeviceUser.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ICamera connectLegacy(ICameraClient client, int cameraId, int halVersion, String opPackageName, int clientUid) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(cameraId);
                    _data.writeInt(halVersion);
                    _data.writeString(opPackageName);
                    _data.writeInt(clientUid);
                    this.mRemote.transact(Stub.TRANSACTION_connectLegacy, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    ICamera _result = android.hardware.ICamera.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addListener(ICameraServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addListener, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeListener(ICameraServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeListener, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CameraMetadataNative getCameraCharacteristics(int cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CameraMetadataNative cameraMetadataNative;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cameraId);
                    this.mRemote.transact(Stub.TRANSACTION_getCameraCharacteristics, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        cameraMetadataNative = (CameraMetadataNative) CameraMetadataNative.CREATOR.createFromParcel(_reply);
                    } else {
                        cameraMetadataNative = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return cameraMetadataNative;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VendorTagDescriptor getCameraVendorTagDescriptor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    VendorTagDescriptor vendorTagDescriptor;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCameraVendorTagDescriptor, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        vendorTagDescriptor = (VendorTagDescriptor) VendorTagDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        vendorTagDescriptor = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return vendorTagDescriptor;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLegacyParameters(int cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cameraId);
                    this.mRemote.transact(Stub.TRANSACTION_getLegacyParameters, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supportsCameraApi(int cameraId, int apiVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cameraId);
                    _data.writeInt(apiVersion);
                    this.mRemote.transact(Stub.TRANSACTION_supportsCameraApi, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTorchMode(String CameraId, boolean enabled, IBinder clientBinder) throws RemoteException {
                int i = ICameraService.EVENT_NONE;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(CameraId);
                    if (enabled) {
                        i = Stub.TRANSACTION_getNumberOfCameras;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(clientBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setTorchMode, _data, _reply, ICameraService.EVENT_NONE);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifySystemEvent(int eventId, int[] args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    _data.writeIntArray(args);
                    this.mRemote.transact(Stub.TRANSACTION_notifySystemEvent, _data, null, Stub.TRANSACTION_getNumberOfCameras);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ICamera _result;
            switch (code) {
                case TRANSACTION_getNumberOfCameras /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = getNumberOfCameras(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getCameraInfo /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    CameraInfo _result3 = getCameraInfo(data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getNumberOfCameras);
                        _result3.writeToParcel(reply, TRANSACTION_getNumberOfCameras);
                    } else {
                        reply.writeInt(ICameraService.EVENT_NONE);
                    }
                    return true;
                case TRANSACTION_connect /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = connect(android.hardware.ICameraClient.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_connectDevice /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    ICameraDeviceUser _result4 = connectDevice(android.hardware.camera2.ICameraDeviceCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result4 != null ? _result4.asBinder() : null);
                    return true;
                case TRANSACTION_connectLegacy /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = connectLegacy(android.hardware.ICameraClient.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                    return true;
                case TRANSACTION_addListener /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    addListener(android.hardware.ICameraServiceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeListener /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeListener(android.hardware.ICameraServiceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCameraCharacteristics /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    CameraMetadataNative _result5 = getCameraCharacteristics(data.readInt());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_getNumberOfCameras);
                        _result5.writeToParcel(reply, TRANSACTION_getNumberOfCameras);
                    } else {
                        reply.writeInt(ICameraService.EVENT_NONE);
                    }
                    return true;
                case TRANSACTION_getCameraVendorTagDescriptor /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    VendorTagDescriptor _result6 = getCameraVendorTagDescriptor();
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getNumberOfCameras);
                        _result6.writeToParcel(reply, TRANSACTION_getNumberOfCameras);
                    } else {
                        reply.writeInt(ICameraService.EVENT_NONE);
                    }
                    return true;
                case TRANSACTION_getLegacyParameters /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result7 = getLegacyParameters(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result7);
                    return true;
                case TRANSACTION_supportsCameraApi /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result8 = supportsCameraApi(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result8 ? TRANSACTION_getNumberOfCameras : ICameraService.EVENT_NONE);
                    return true;
                case TRANSACTION_setTorchMode /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    setTorchMode(data.readString(), data.readInt() != 0, data.readStrongBinder());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_notifySystemEvent /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifySystemEvent(data.readInt(), data.createIntArray());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addListener(ICameraServiceListener iCameraServiceListener) throws RemoteException;

    ICamera connect(ICameraClient iCameraClient, int i, String str, int i2, int i3) throws RemoteException;

    ICameraDeviceUser connectDevice(ICameraDeviceCallbacks iCameraDeviceCallbacks, int i, String str, int i2) throws RemoteException;

    ICamera connectLegacy(ICameraClient iCameraClient, int i, int i2, String str, int i3) throws RemoteException;

    CameraMetadataNative getCameraCharacteristics(int i) throws RemoteException;

    CameraInfo getCameraInfo(int i) throws RemoteException;

    VendorTagDescriptor getCameraVendorTagDescriptor() throws RemoteException;

    String getLegacyParameters(int i) throws RemoteException;

    int getNumberOfCameras(int i) throws RemoteException;

    void notifySystemEvent(int i, int[] iArr) throws RemoteException;

    void removeListener(ICameraServiceListener iCameraServiceListener) throws RemoteException;

    void setTorchMode(String str, boolean z, IBinder iBinder) throws RemoteException;

    boolean supportsCameraApi(int i, int i2) throws RemoteException;
}
