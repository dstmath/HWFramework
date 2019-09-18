package android.media.tv;

import android.hardware.hdmi.HdmiDeviceInfo;
import android.media.tv.ITvInputServiceCallback;
import android.media.tv.ITvInputSessionCallback;
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
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unregisterCallback(ITvInputServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createSession(InputChannel channel, ITvInputSessionCallback callback, String inputId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (channel != null) {
                        _data.writeInt(1);
                        channel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(inputId);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createRecordingSession(ITvInputSessionCallback callback, String inputId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(inputId);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHardwareAdded(TvInputHardwareInfo hardwareInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hardwareInfo != null) {
                        _data.writeInt(1);
                        hardwareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHardwareRemoved(TvInputHardwareInfo hardwareInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hardwareInfo != null) {
                        _data.writeInt(1);
                        hardwareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void notifyHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        _data.writeInt(1);
                        deviceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
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
            if (code != 1598968902) {
                InputChannel _arg0 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallback(ITvInputServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterCallback(ITvInputServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (InputChannel) InputChannel.CREATOR.createFromParcel(data);
                        }
                        createSession(_arg0, ITvInputSessionCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        createRecordingSession(ITvInputSessionCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = TvInputHardwareInfo.CREATOR.createFromParcel(data);
                        }
                        notifyHardwareAdded(_arg0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = TvInputHardwareInfo.CREATOR.createFromParcel(data);
                        }
                        notifyHardwareRemoved(_arg0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = HdmiDeviceInfo.CREATOR.createFromParcel(data);
                        }
                        notifyHdmiDeviceAdded(_arg0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = HdmiDeviceInfo.CREATOR.createFromParcel(data);
                        }
                        notifyHdmiDeviceRemoved(_arg0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
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
