package android.media.tv;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.InputChannel;

public interface ITvInputService extends IInterface {

    public static abstract class Stub extends Binder implements ITvInputService {
        private static final String DESCRIPTOR = "android.media.tv.ITvInputService";
        static final int TRANSACTION_createRecordingSession = 4;
        static final int TRANSACTION_createSession = 3;
        static final int TRANSACTION_notifyHardwareAdded = 5;
        static final int TRANSACTION_notifyHardwareRemoved = 6;
        static final int TRANSACTION_notifyHdmiDeviceAdded = 7;
        static final int TRANSACTION_notifyHdmiDeviceRemoved = 8;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_unregisterCallback = 2;

        private static class Proxy implements ITvInputService {
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

            public void registerCallback(ITvInputServiceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerCallback, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void unregisterCallback(ITvInputServiceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void createSession(InputChannel channel, ITvInputSessionCallback callback, String inputId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channel != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        channel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(inputId);
                    this.mRemote.transact(Stub.TRANSACTION_createSession, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void createRecordingSession(ITvInputSessionCallback callback, String inputId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(inputId);
                    this.mRemote.transact(Stub.TRANSACTION_createRecordingSession, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHardwareAdded(TvInputHardwareInfo hardwareInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hardwareInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        hardwareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notifyHardwareAdded, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHardwareRemoved(TvInputHardwareInfo hardwareInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hardwareInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        hardwareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notifyHardwareRemoved, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notifyHdmiDeviceAdded, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(Stub.TRANSACTION_registerCallback);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notifyHdmiDeviceRemoved, _data, null, Stub.TRANSACTION_registerCallback);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvInputService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvInputService)) {
                return new Proxy(obj);
            }
            return (ITvInputService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            TvInputHardwareInfo tvInputHardwareInfo;
            HdmiDeviceInfo hdmiDeviceInfo;
            switch (code) {
                case TRANSACTION_registerCallback /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(android.media.tv.ITvInputServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_unregisterCallback /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(android.media.tv.ITvInputServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_createSession /*3*/:
                    InputChannel inputChannel;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        inputChannel = (InputChannel) InputChannel.CREATOR.createFromParcel(data);
                    } else {
                        inputChannel = null;
                    }
                    createSession(inputChannel, android.media.tv.ITvInputSessionCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                    return true;
                case TRANSACTION_createRecordingSession /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    createRecordingSession(android.media.tv.ITvInputSessionCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                    return true;
                case TRANSACTION_notifyHardwareAdded /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tvInputHardwareInfo = (TvInputHardwareInfo) TvInputHardwareInfo.CREATOR.createFromParcel(data);
                    } else {
                        tvInputHardwareInfo = null;
                    }
                    notifyHardwareAdded(tvInputHardwareInfo);
                    return true;
                case TRANSACTION_notifyHardwareRemoved /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        tvInputHardwareInfo = (TvInputHardwareInfo) TvInputHardwareInfo.CREATOR.createFromParcel(data);
                    } else {
                        tvInputHardwareInfo = null;
                    }
                    notifyHardwareRemoved(tvInputHardwareInfo);
                    return true;
                case TRANSACTION_notifyHdmiDeviceAdded /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        hdmiDeviceInfo = (HdmiDeviceInfo) HdmiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        hdmiDeviceInfo = null;
                    }
                    notifyHdmiDeviceAdded(hdmiDeviceInfo);
                    return true;
                case TRANSACTION_notifyHdmiDeviceRemoved /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        hdmiDeviceInfo = (HdmiDeviceInfo) HdmiDeviceInfo.CREATOR.createFromParcel(data);
                    } else {
                        hdmiDeviceInfo = null;
                    }
                    notifyHdmiDeviceRemoved(hdmiDeviceInfo);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void createRecordingSession(ITvInputSessionCallback iTvInputSessionCallback, String str) throws RemoteException;

    void createSession(InputChannel inputChannel, ITvInputSessionCallback iTvInputSessionCallback, String str) throws RemoteException;

    void notifyHardwareAdded(TvInputHardwareInfo tvInputHardwareInfo) throws RemoteException;

    void notifyHardwareRemoved(TvInputHardwareInfo tvInputHardwareInfo) throws RemoteException;

    void notifyHdmiDeviceAdded(HdmiDeviceInfo hdmiDeviceInfo) throws RemoteException;

    void notifyHdmiDeviceRemoved(HdmiDeviceInfo hdmiDeviceInfo) throws RemoteException;

    void registerCallback(ITvInputServiceCallback iTvInputServiceCallback) throws RemoteException;

    void unregisterCallback(ITvInputServiceCallback iTvInputServiceCallback) throws RemoteException;
}
