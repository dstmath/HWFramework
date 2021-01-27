package com.android.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.VideoProfile;

public interface IVideoCallback extends IInterface {
    void changeCallDataUsage(long j) throws RemoteException;

    void changeCameraCapabilities(VideoProfile.CameraCapabilities cameraCapabilities) throws RemoteException;

    void changePeerDimensions(int i, int i2) throws RemoteException;

    void changeVideoQuality(int i) throws RemoteException;

    void handleCallSessionEvent(int i) throws RemoteException;

    void receiveSessionModifyRequest(VideoProfile videoProfile) throws RemoteException;

    void receiveSessionModifyResponse(int i, VideoProfile videoProfile, VideoProfile videoProfile2) throws RemoteException;

    public static class Default implements IVideoCallback {
        @Override // com.android.internal.telecom.IVideoCallback
        public void receiveSessionModifyRequest(VideoProfile videoProfile) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void handleCallSessionEvent(int event) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changePeerDimensions(int width, int height) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changeCallDataUsage(long dataUsage) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changeCameraCapabilities(VideoProfile.CameraCapabilities cameraCapabilities) throws RemoteException {
        }

        @Override // com.android.internal.telecom.IVideoCallback
        public void changeVideoQuality(int videoQuality) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVideoCallback {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IVideoCallback";
        static final int TRANSACTION_changeCallDataUsage = 5;
        static final int TRANSACTION_changeCameraCapabilities = 6;
        static final int TRANSACTION_changePeerDimensions = 4;
        static final int TRANSACTION_changeVideoQuality = 7;
        static final int TRANSACTION_handleCallSessionEvent = 3;
        static final int TRANSACTION_receiveSessionModifyRequest = 1;
        static final int TRANSACTION_receiveSessionModifyResponse = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVideoCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVideoCallback)) {
                return new Proxy(obj);
            }
            return (IVideoCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "receiveSessionModifyRequest";
                case 2:
                    return "receiveSessionModifyResponse";
                case 3:
                    return "handleCallSessionEvent";
                case 4:
                    return "changePeerDimensions";
                case 5:
                    return "changeCallDataUsage";
                case 6:
                    return "changeCameraCapabilities";
                case 7:
                    return "changeVideoQuality";
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
            VideoProfile _arg0;
            VideoProfile _arg1;
            VideoProfile _arg2;
            VideoProfile.CameraCapabilities _arg02;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = VideoProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        receiveSessionModifyRequest(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = VideoProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = VideoProfile.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        receiveSessionModifyResponse(_arg03, _arg1, _arg2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        handleCallSessionEvent(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        changePeerDimensions(data.readInt(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        changeCallDataUsage(data.readLong());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = VideoProfile.CameraCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        changeCameraCapabilities(_arg02);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        changeVideoQuality(data.readInt());
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
        public static class Proxy implements IVideoCallback {
            public static IVideoCallback sDefaultImpl;
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

            @Override // com.android.internal.telecom.IVideoCallback
            public void receiveSessionModifyRequest(VideoProfile videoProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (videoProfile != null) {
                        _data.writeInt(1);
                        videoProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().receiveSessionModifyRequest(videoProfile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (requestedProfile != null) {
                        _data.writeInt(1);
                        requestedProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (responseProfile != null) {
                        _data.writeInt(1);
                        responseProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().receiveSessionModifyResponse(status, requestedProfile, responseProfile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void handleCallSessionEvent(int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handleCallSessionEvent(event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changePeerDimensions(int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().changePeerDimensions(width, height);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeCallDataUsage(long dataUsage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(dataUsage);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().changeCallDataUsage(dataUsage);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeCameraCapabilities(VideoProfile.CameraCapabilities cameraCapabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cameraCapabilities != null) {
                        _data.writeInt(1);
                        cameraCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().changeCameraCapabilities(cameraCapabilities);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telecom.IVideoCallback
            public void changeVideoQuality(int videoQuality) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(videoQuality);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().changeVideoQuality(videoQuality);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVideoCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVideoCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
