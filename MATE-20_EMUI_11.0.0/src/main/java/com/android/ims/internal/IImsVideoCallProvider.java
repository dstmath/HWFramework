package com.android.ims.internal;

import android.annotation.UnsupportedAppUsage;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.view.Surface;
import com.android.ims.internal.IImsVideoCallCallback;

public interface IImsVideoCallProvider extends IInterface {
    void requestCallDataUsage() throws RemoteException;

    void requestCameraCapabilities() throws RemoteException;

    void sendSessionModifyRequest(VideoProfile videoProfile, VideoProfile videoProfile2) throws RemoteException;

    void sendSessionModifyResponse(VideoProfile videoProfile) throws RemoteException;

    @UnsupportedAppUsage
    void setCallback(IImsVideoCallCallback iImsVideoCallCallback) throws RemoteException;

    void setCamera(String str, int i) throws RemoteException;

    void setDeviceOrientation(int i) throws RemoteException;

    void setDisplaySurface(Surface surface) throws RemoteException;

    void setPauseImage(Uri uri) throws RemoteException;

    void setPreviewSurface(Surface surface) throws RemoteException;

    void setZoom(float f) throws RemoteException;

    public static class Default implements IImsVideoCallProvider {
        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void setCallback(IImsVideoCallCallback callback) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void setCamera(String cameraId, int uid) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void setPreviewSurface(Surface surface) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void setDisplaySurface(Surface surface) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void setDeviceOrientation(int rotation) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void setZoom(float value) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void sendSessionModifyResponse(VideoProfile responseProfile) throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void requestCameraCapabilities() throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void requestCallDataUsage() throws RemoteException {
        }

        @Override // com.android.ims.internal.IImsVideoCallProvider
        public void setPauseImage(Uri uri) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsVideoCallProvider {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsVideoCallProvider";
        static final int TRANSACTION_requestCallDataUsage = 10;
        static final int TRANSACTION_requestCameraCapabilities = 9;
        static final int TRANSACTION_sendSessionModifyRequest = 7;
        static final int TRANSACTION_sendSessionModifyResponse = 8;
        static final int TRANSACTION_setCallback = 1;
        static final int TRANSACTION_setCamera = 2;
        static final int TRANSACTION_setDeviceOrientation = 5;
        static final int TRANSACTION_setDisplaySurface = 4;
        static final int TRANSACTION_setPauseImage = 11;
        static final int TRANSACTION_setPreviewSurface = 3;
        static final int TRANSACTION_setZoom = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsVideoCallProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsVideoCallProvider)) {
                return new Proxy(obj);
            }
            return (IImsVideoCallProvider) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setCallback";
                case 2:
                    return "setCamera";
                case 3:
                    return "setPreviewSurface";
                case 4:
                    return "setDisplaySurface";
                case 5:
                    return "setDeviceOrientation";
                case 6:
                    return "setZoom";
                case 7:
                    return "sendSessionModifyRequest";
                case 8:
                    return "sendSessionModifyResponse";
                case 9:
                    return "requestCameraCapabilities";
                case 10:
                    return "requestCallDataUsage";
                case 11:
                    return "setPauseImage";
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
            Surface _arg0;
            Surface _arg02;
            VideoProfile _arg03;
            VideoProfile _arg1;
            VideoProfile _arg04;
            Uri _arg05;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setCallback(IImsVideoCallCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setCamera(data.readString(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setPreviewSurface(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Surface.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setDisplaySurface(_arg02);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setDeviceOrientation(data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setZoom(data.readFloat());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = VideoProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = VideoProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        sendSessionModifyRequest(_arg03, _arg1);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = VideoProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        sendSessionModifyResponse(_arg04);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        requestCameraCapabilities();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        requestCallDataUsage();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        setPauseImage(_arg05);
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
        public static class Proxy implements IImsVideoCallProvider {
            public static IImsVideoCallProvider sDefaultImpl;
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

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void setCallback(IImsVideoCallCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void setCamera(String cameraId, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setCamera(cameraId, uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void setPreviewSurface(Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPreviewSurface(surface);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void setDisplaySurface(Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(1);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setDisplaySurface(surface);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void setDeviceOrientation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setDeviceOrientation(rotation);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void setZoom(float value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(value);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setZoom(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fromProfile != null) {
                        _data.writeInt(1);
                        fromProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (toProfile != null) {
                        _data.writeInt(1);
                        toProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendSessionModifyRequest(fromProfile, toProfile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void sendSessionModifyResponse(VideoProfile responseProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (responseProfile != null) {
                        _data.writeInt(1);
                        responseProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendSessionModifyResponse(responseProfile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void requestCameraCapabilities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestCameraCapabilities();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void requestCallDataUsage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestCallDataUsage();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.ims.internal.IImsVideoCallProvider
            public void setPauseImage(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPauseImage(uri);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IImsVideoCallProvider impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImsVideoCallProvider getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
