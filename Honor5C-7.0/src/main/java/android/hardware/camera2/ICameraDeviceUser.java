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
    public static final int NO_IN_FLIGHT_REPEATING_FRAMES = -1;
    public static final int TEMPLATE_MANUAL = 6;
    public static final int TEMPLATE_PREVIEW = 1;
    public static final int TEMPLATE_RECORD = 3;
    public static final int TEMPLATE_STILL_CAPTURE = 2;
    public static final int TEMPLATE_VIDEO_SNAPSHOT = 4;
    public static final int TEMPLATE_ZERO_SHUTTER_LAG = 5;

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
        static final int TRANSACTION_flush = 14;
        static final int TRANSACTION_getCameraInfo = 12;
        static final int TRANSACTION_getInputSurface = 10;
        static final int TRANSACTION_prepare = 15;
        static final int TRANSACTION_prepare2 = 17;
        static final int TRANSACTION_submitRequest = 2;
        static final int TRANSACTION_submitRequestList = 3;
        static final int TRANSACTION_tearDown = 16;
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
                    this.mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SubmitInfo submitRequest(CaptureRequest request, boolean streaming) throws RemoteException {
                int i = Stub.TRANSACTION_disconnect;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SubmitInfo submitInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(Stub.TRANSACTION_disconnect);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!streaming) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_submitRequest, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        submitInfo = (SubmitInfo) SubmitInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        submitInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return submitInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SubmitInfo submitRequestList(CaptureRequest[] requestList, boolean streaming) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SubmitInfo submitInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(requestList, 0);
                    if (streaming) {
                        i = Stub.TRANSACTION_disconnect;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_submitRequestList, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        submitInfo = (SubmitInfo) SubmitInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        submitInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return submitInfo;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_cancelRequest, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_beginConfigure, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endConfigure(boolean isConstrainedHighSpeed) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (isConstrainedHighSpeed) {
                        i = Stub.TRANSACTION_disconnect;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_endConfigure, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_deleteStream, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_disconnect);
                        outputConfiguration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_createStream, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_createInputStream, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Surface getInputSurface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Surface surface;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getInputSurface, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(_reply);
                    } else {
                        surface = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return surface;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CameraMetadataNative createDefaultRequest(int templateId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CameraMetadataNative cameraMetadataNative;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(templateId);
                    this.mRemote.transact(Stub.TRANSACTION_createDefaultRequest, _data, _reply, 0);
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

            public CameraMetadataNative getCameraInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CameraMetadataNative cameraMetadataNative;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCameraInfo, _data, _reply, 0);
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

            public void waitUntilIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_waitUntilIdle, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_flush, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_prepare, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_tearDown, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_prepare2, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SubmitInfo _result;
            long _result2;
            int _result3;
            CameraMetadataNative _result4;
            switch (code) {
                case TRANSACTION_disconnect /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_submitRequest /*2*/:
                    CaptureRequest captureRequest;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        captureRequest = (CaptureRequest) CaptureRequest.CREATOR.createFromParcel(data);
                    } else {
                        captureRequest = null;
                    }
                    _result = submitRequest(captureRequest, data.readInt() != 0);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_disconnect);
                        _result.writeToParcel(reply, TRANSACTION_disconnect);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_submitRequestList /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = submitRequestList((CaptureRequest[]) data.createTypedArray(CaptureRequest.CREATOR), data.readInt() != 0);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_disconnect);
                        _result.writeToParcel(reply, TRANSACTION_disconnect);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_cancelRequest /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = cancelRequest(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case TRANSACTION_beginConfigure /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    beginConfigure();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_endConfigure /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    endConfigure(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteStream /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    deleteStream(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createStream /*8*/:
                    OutputConfiguration outputConfiguration;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        outputConfiguration = (OutputConfiguration) OutputConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        outputConfiguration = null;
                    }
                    _result3 = createStream(outputConfiguration);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_createInputStream /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = createInputStream(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getInputSurface /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    Surface _result5 = getInputSurface();
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(TRANSACTION_disconnect);
                        _result5.writeToParcel(reply, TRANSACTION_disconnect);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_createDefaultRequest /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = createDefaultRequest(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_disconnect);
                        _result4.writeToParcel(reply, TRANSACTION_disconnect);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getCameraInfo /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getCameraInfo();
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_disconnect);
                        _result4.writeToParcel(reply, TRANSACTION_disconnect);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_waitUntilIdle /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    waitUntilIdle();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_flush /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = flush();
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                case TRANSACTION_prepare /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    prepare(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_tearDown /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    tearDown(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_prepare2 /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    prepare2(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    void endConfigure(boolean z) throws RemoteException;

    long flush() throws RemoteException;

    CameraMetadataNative getCameraInfo() throws RemoteException;

    Surface getInputSurface() throws RemoteException;

    void prepare(int i) throws RemoteException;

    void prepare2(int i, int i2) throws RemoteException;

    SubmitInfo submitRequest(CaptureRequest captureRequest, boolean z) throws RemoteException;

    SubmitInfo submitRequestList(CaptureRequest[] captureRequestArr, boolean z) throws RemoteException;

    void tearDown(int i) throws RemoteException;

    void waitUntilIdle() throws RemoteException;
}
