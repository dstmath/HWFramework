package com.android.internal.telecom;

import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.VideoProfile;
import android.view.Surface;

public interface IVideoProvider extends IInterface {

    public static abstract class Stub extends Binder implements IVideoProvider {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IVideoProvider";
        static final int TRANSACTION_addVideoCallback = 1;
        static final int TRANSACTION_removeVideoCallback = 2;
        static final int TRANSACTION_requestCallDataUsage = 11;
        static final int TRANSACTION_requestCameraCapabilities = 10;
        static final int TRANSACTION_sendSessionModifyRequest = 8;
        static final int TRANSACTION_sendSessionModifyResponse = 9;
        static final int TRANSACTION_setCamera = 3;
        static final int TRANSACTION_setDeviceOrientation = 6;
        static final int TRANSACTION_setDisplaySurface = 5;
        static final int TRANSACTION_setPauseImage = 12;
        static final int TRANSACTION_setPreviewSurface = 4;
        static final int TRANSACTION_setZoom = 7;

        private static class Proxy implements IVideoProvider {
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

            public void addVideoCallback(IBinder videoCallbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(videoCallbackBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addVideoCallback, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void removeVideoCallback(IBinder videoCallbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(videoCallbackBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeVideoCallback, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setCamera(String cameraId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cameraId);
                    this.mRemote.transact(Stub.TRANSACTION_setCamera, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setPreviewSurface(Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(Stub.TRANSACTION_addVideoCallback);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setPreviewSurface, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setDisplaySurface(Surface surface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (surface != null) {
                        _data.writeInt(Stub.TRANSACTION_addVideoCallback);
                        surface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setDisplaySurface, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setDeviceOrientation(int rotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rotation);
                    this.mRemote.transact(Stub.TRANSACTION_setDeviceOrientation, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setZoom(float value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(value);
                    this.mRemote.transact(Stub.TRANSACTION_setZoom, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void sendSessionModifyRequest(VideoProfile fromProfile, VideoProfile toProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fromProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_addVideoCallback);
                        fromProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (toProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_addVideoCallback);
                        toProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendSessionModifyRequest, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void sendSessionModifyResponse(VideoProfile responseProfile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (responseProfile != null) {
                        _data.writeInt(Stub.TRANSACTION_addVideoCallback);
                        responseProfile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendSessionModifyResponse, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void requestCameraCapabilities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_requestCameraCapabilities, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void requestCallDataUsage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_requestCallDataUsage, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void setPauseImage(Uri uri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (uri != null) {
                        _data.writeInt(Stub.TRANSACTION_addVideoCallback);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setPauseImage, _data, null, Stub.TRANSACTION_addVideoCallback);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVideoProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVideoProvider)) {
                return new Proxy(obj);
            }
            return (IVideoProvider) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Surface surface;
            VideoProfile videoProfile;
            switch (code) {
                case TRANSACTION_addVideoCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    addVideoCallback(data.readStrongBinder());
                    return true;
                case TRANSACTION_removeVideoCallback /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeVideoCallback(data.readStrongBinder());
                    return true;
                case TRANSACTION_setCamera /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCamera(data.readString());
                    return true;
                case TRANSACTION_setPreviewSurface /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    setPreviewSurface(surface);
                    return true;
                case TRANSACTION_setDisplaySurface /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    setDisplaySurface(surface);
                    return true;
                case TRANSACTION_setDeviceOrientation /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDeviceOrientation(data.readInt());
                    return true;
                case TRANSACTION_setZoom /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setZoom(data.readFloat());
                    return true;
                case TRANSACTION_sendSessionModifyRequest /*8*/:
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
                case TRANSACTION_sendSessionModifyResponse /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        videoProfile = (VideoProfile) VideoProfile.CREATOR.createFromParcel(data);
                    } else {
                        videoProfile = null;
                    }
                    sendSessionModifyResponse(videoProfile);
                    return true;
                case TRANSACTION_requestCameraCapabilities /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestCameraCapabilities();
                    return true;
                case TRANSACTION_requestCallDataUsage /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    requestCallDataUsage();
                    return true;
                case TRANSACTION_setPauseImage /*12*/:
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

    void addVideoCallback(IBinder iBinder) throws RemoteException;

    void removeVideoCallback(IBinder iBinder) throws RemoteException;

    void requestCallDataUsage() throws RemoteException;

    void requestCameraCapabilities() throws RemoteException;

    void sendSessionModifyRequest(VideoProfile videoProfile, VideoProfile videoProfile2) throws RemoteException;

    void sendSessionModifyResponse(VideoProfile videoProfile) throws RemoteException;

    void setCamera(String str) throws RemoteException;

    void setDeviceOrientation(int i) throws RemoteException;

    void setDisplaySurface(Surface surface) throws RemoteException;

    void setPauseImage(Uri uri) throws RemoteException;

    void setPreviewSurface(Surface surface) throws RemoteException;

    void setZoom(float f) throws RemoteException;
}
