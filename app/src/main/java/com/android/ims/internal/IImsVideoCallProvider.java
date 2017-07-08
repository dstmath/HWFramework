package com.android.ims.internal;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.view.Surface;

public interface IImsVideoCallProvider extends IInterface {

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

        private static class Proxy implements IImsVideoCallProvider {
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

            public void setCallback(IImsVideoCallCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setCallback, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setCamera(String cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    this.mRemote.transact(Stub.TRANSACTION_setCamera, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setPreviewSurface(Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(Stub.TRANSACTION_setCallback);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setPreviewSurface, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setDisplaySurface(Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(Stub.TRANSACTION_setCallback);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setDisplaySurface, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setDeviceOrientation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    this.mRemote.transact(Stub.TRANSACTION_setDeviceOrientation, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setZoom(float value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(value);
                    this.mRemote.transact(Stub.TRANSACTION_setZoom, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fromProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_setCallback);
                        fromProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (toProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_setCallback);
                        toProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendSessionModifyRequest, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void sendSessionModifyResponse(VideoProfile responseProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (responseProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_setCallback);
                        responseProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendSessionModifyResponse, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void requestCameraCapabilities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_requestCameraCapabilities, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void requestCallDataUsage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_requestCallDataUsage, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setPauseImage(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_setCallback);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setPauseImage, _data, null, Stub.TRANSACTION_setCallback);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Surface surface;
            VideoProfile videoProfile;
            switch (code) {
                case TRANSACTION_setCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCallback(com.android.ims.internal.IImsVideoCallCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setCamera /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCamera(data.readString());
                    return true;
                case TRANSACTION_setPreviewSurface /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    setPreviewSurface(surface);
                    return true;
                case TRANSACTION_setDisplaySurface /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    setDisplaySurface(surface);
                    return true;
                case TRANSACTION_setDeviceOrientation /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDeviceOrientation(data.readInt());
                    return true;
                case TRANSACTION_setZoom /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setZoom(data.readFloat());
                    return true;
                case TRANSACTION_sendSessionModifyRequest /*7*/:
                    VideoProfile videoProfile2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        videoProfile = (VideoProfile) VideoProfile.CREATOR.createFromParcel(data);
                    } else {
                        videoProfile = null;
                    }
                    if (data.readInt() != 0) {
                        videoProfile2 = (VideoProfile) VideoProfile.CREATOR.createFromParcel(data);
                    } else {
                        videoProfile2 = null;
                    }
                    sendSessionModifyRequest(videoProfile, videoProfile2);
                    return true;
                case TRANSACTION_sendSessionModifyResponse /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        videoProfile = (VideoProfile) VideoProfile.CREATOR.createFromParcel(data);
                    } else {
                        videoProfile = null;
                    }
                    sendSessionModifyResponse(videoProfile);
                    return true;
                case TRANSACTION_requestCameraCapabilities /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestCameraCapabilities();
                    return true;
                case TRANSACTION_requestCallDataUsage /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestCallDataUsage();
                    return true;
                case TRANSACTION_setPauseImage /*11*/:
                    Uri uri;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    setPauseImage(uri);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void requestCallDataUsage() throws RemoteException;

    void requestCameraCapabilities() throws RemoteException;

    void sendSessionModifyRequest(VideoProfile videoProfile, VideoProfile videoProfile2) throws RemoteException;

    void sendSessionModifyResponse(VideoProfile videoProfile) throws RemoteException;

    void setCallback(IImsVideoCallCallback iImsVideoCallCallback) throws RemoteException;

    void setCamera(String str) throws RemoteException;

    void setDeviceOrientation(int i) throws RemoteException;

    void setDisplaySurface(Surface surface) throws RemoteException;

    void setPauseImage(Uri uri) throws RemoteException;

    void setPreviewSurface(Surface surface) throws RemoteException;

    void setZoom(float f) throws RemoteException;
}
