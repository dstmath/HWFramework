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
    void createRecordingSession(ITvInputSessionCallback iTvInputSessionCallback, String str) throws RemoteException;

    void createSession(InputChannel inputChannel, ITvInputSessionCallback iTvInputSessionCallback, String str) throws RemoteException;

    void notifyHardwareAdded(TvInputHardwareInfo tvInputHardwareInfo) throws RemoteException;

    void notifyHardwareRemoved(TvInputHardwareInfo tvInputHardwareInfo) throws RemoteException;

    void notifyHdmiDeviceAdded(HdmiDeviceInfo hdmiDeviceInfo) throws RemoteException;

    void notifyHdmiDeviceRemoved(HdmiDeviceInfo hdmiDeviceInfo) throws RemoteException;

    void registerCallback(ITvInputServiceCallback iTvInputServiceCallback) throws RemoteException;

    void unregisterCallback(ITvInputServiceCallback iTvInputServiceCallback) throws RemoteException;

    public static class Default implements ITvInputService {
        @Override // android.media.tv.ITvInputService
        public void registerCallback(ITvInputServiceCallback callback) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputService
        public void unregisterCallback(ITvInputServiceCallback callback) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputService
        public void createSession(InputChannel channel, ITvInputSessionCallback callback, String inputId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputService
        public void createRecordingSession(ITvInputSessionCallback callback, String inputId) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputService
        public void notifyHardwareAdded(TvInputHardwareInfo hardwareInfo) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputService
        public void notifyHardwareRemoved(TvInputHardwareInfo hardwareInfo) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputService
        public void notifyHdmiDeviceAdded(HdmiDeviceInfo deviceInfo) throws RemoteException {
        }

        @Override // android.media.tv.ITvInputService
        public void notifyHdmiDeviceRemoved(HdmiDeviceInfo deviceInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerCallback";
                case 2:
                    return "unregisterCallback";
                case 3:
                    return "createSession";
                case 4:
                    return "createRecordingSession";
                case 5:
                    return "notifyHardwareAdded";
                case 6:
                    return "notifyHardwareRemoved";
                case 7:
                    return "notifyHdmiDeviceAdded";
                case 8:
                    return "notifyHdmiDeviceRemoved";
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
            InputChannel _arg0;
            TvInputHardwareInfo _arg02;
            TvInputHardwareInfo _arg03;
            HdmiDeviceInfo _arg04;
            HdmiDeviceInfo _arg05;
            if (code != 1598968902) {
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
                            _arg0 = InputChannel.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
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
                            _arg02 = TvInputHardwareInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        notifyHardwareAdded(_arg02);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = TvInputHardwareInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        notifyHardwareRemoved(_arg03);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = HdmiDeviceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        notifyHdmiDeviceAdded(_arg04);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = HdmiDeviceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        notifyHdmiDeviceRemoved(_arg05);
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
        public static class Proxy implements ITvInputService {
            public static ITvInputService sDefaultImpl;
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

            @Override // android.media.tv.ITvInputService
            public void registerCallback(ITvInputServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().registerCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void unregisterCallback(ITvInputServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unregisterCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
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
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().createSession(channel, callback, inputId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
            public void createRecordingSession(ITvInputSessionCallback callback, String inputId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeString(inputId);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().createRecordingSession(callback, inputId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
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
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyHardwareAdded(hardwareInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
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
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyHardwareRemoved(hardwareInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
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
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyHdmiDeviceAdded(deviceInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvInputService
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
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyHdmiDeviceRemoved(deviceInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITvInputService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITvInputService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
