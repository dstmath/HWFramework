package com.android.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.telecom.VideoProfile.CameraCapabilities;

public interface IImsVideoCallCallback extends IInterface {

    public static abstract class Stub extends Binder implements IImsVideoCallCallback {
        private static final String DESCRIPTOR = "com.android.ims.internal.IImsVideoCallCallback";
        static final int TRANSACTION_changeCallDataUsage = 5;
        static final int TRANSACTION_changeCameraCapabilities = 6;
        static final int TRANSACTION_changePeerDimensions = 4;
        static final int TRANSACTION_changeVideoQuality = 7;
        static final int TRANSACTION_handleCallSessionEvent = 3;
        static final int TRANSACTION_receiveSessionModifyRequest = 1;
        static final int TRANSACTION_receiveSessionModifyResponse = 2;

        private static class Proxy implements IImsVideoCallCallback {
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

            public void receiveSessionModifyRequest(VideoProfile videoProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (videoProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_receiveSessionModifyRequest);
                        videoProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_receiveSessionModifyRequest, _data, null, Stub.TRANSACTION_receiveSessionModifyRequest);
                } finally {
                    _data.recycle();
                }
            }

            public void receiveSessionModifyResponse(int status, VideoProfile requestedProfile, VideoProfile responseProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (requestedProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_receiveSessionModifyRequest);
                        requestedProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (responseProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_receiveSessionModifyRequest);
                        responseProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_receiveSessionModifyResponse, _data, null, Stub.TRANSACTION_receiveSessionModifyRequest);
                } finally {
                    _data.recycle();
                }
            }

            public void handleCallSessionEvent(int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    this.mRemote.transact(Stub.TRANSACTION_handleCallSessionEvent, _data, null, Stub.TRANSACTION_receiveSessionModifyRequest);
                } finally {
                    _data.recycle();
                }
            }

            public void changePeerDimensions(int width, int height) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    this.mRemote.transact(Stub.TRANSACTION_changePeerDimensions, _data, null, Stub.TRANSACTION_receiveSessionModifyRequest);
                } finally {
                    _data.recycle();
                }
            }

            public void changeCallDataUsage(long dataUsage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(dataUsage);
                    this.mRemote.transact(Stub.TRANSACTION_changeCallDataUsage, _data, null, Stub.TRANSACTION_receiveSessionModifyRequest);
                } finally {
                    _data.recycle();
                }
            }

            public void changeCameraCapabilities(CameraCapabilities cameraCapabilities) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cameraCapabilities != null) {
                        _data.writeInt(Stub.TRANSACTION_receiveSessionModifyRequest);
                        cameraCapabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_changeCameraCapabilities, _data, null, Stub.TRANSACTION_receiveSessionModifyRequest);
                } finally {
                    _data.recycle();
                }
            }

            public void changeVideoQuality(int videoQuality) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(videoQuality);
                    this.mRemote.transact(Stub.TRANSACTION_changeVideoQuality, _data, null, Stub.TRANSACTION_receiveSessionModifyRequest);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsVideoCallCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsVideoCallCallback)) {
                return new Proxy(obj);
            }
            return (IImsVideoCallCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_receiveSessionModifyRequest /*1*/:
                    VideoProfile videoProfile;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        videoProfile = (VideoProfile) VideoProfile.CREATOR.createFromParcel(data);
                    } else {
                        videoProfile = null;
                    }
                    receiveSessionModifyRequest(videoProfile);
                    return true;
                case TRANSACTION_receiveSessionModifyResponse /*2*/:
                    VideoProfile videoProfile2;
                    VideoProfile videoProfile3;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        videoProfile2 = (VideoProfile) VideoProfile.CREATOR.createFromParcel(data);
                    } else {
                        videoProfile2 = null;
                    }
                    if (data.readInt() != 0) {
                        videoProfile3 = (VideoProfile) VideoProfile.CREATOR.createFromParcel(data);
                    } else {
                        videoProfile3 = null;
                    }
                    receiveSessionModifyResponse(_arg0, videoProfile2, videoProfile3);
                    return true;
                case TRANSACTION_handleCallSessionEvent /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    handleCallSessionEvent(data.readInt());
                    return true;
                case TRANSACTION_changePeerDimensions /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    changePeerDimensions(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_changeCallDataUsage /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    changeCallDataUsage(data.readLong());
                    return true;
                case TRANSACTION_changeCameraCapabilities /*6*/:
                    CameraCapabilities cameraCapabilities;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        cameraCapabilities = (CameraCapabilities) CameraCapabilities.CREATOR.createFromParcel(data);
                    } else {
                        cameraCapabilities = null;
                    }
                    changeCameraCapabilities(cameraCapabilities);
                    return true;
                case TRANSACTION_changeVideoQuality /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    changeVideoQuality(data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void changeCallDataUsage(long j) throws RemoteException;

    void changeCameraCapabilities(CameraCapabilities cameraCapabilities) throws RemoteException;

    void changePeerDimensions(int i, int i2) throws RemoteException;

    void changeVideoQuality(int i) throws RemoteException;

    void handleCallSessionEvent(int i) throws RemoteException;

    void receiveSessionModifyRequest(VideoProfile videoProfile) throws RemoteException;

    void receiveSessionModifyResponse(int i, VideoProfile videoProfile, VideoProfile videoProfile2) throws RemoteException;
}
