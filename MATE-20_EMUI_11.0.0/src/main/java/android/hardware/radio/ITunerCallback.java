package android.hardware.radio;

import android.hardware.radio.ProgramList;
import android.hardware.radio.RadioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface ITunerCallback extends IInterface {
    void onAntennaState(boolean z) throws RemoteException;

    void onBackgroundScanAvailabilityChange(boolean z) throws RemoteException;

    void onBackgroundScanComplete() throws RemoteException;

    void onConfigurationChanged(RadioManager.BandConfig bandConfig) throws RemoteException;

    void onCurrentProgramInfoChanged(RadioManager.ProgramInfo programInfo) throws RemoteException;

    void onEmergencyAnnouncement(boolean z) throws RemoteException;

    void onError(int i) throws RemoteException;

    void onParametersUpdated(Map map) throws RemoteException;

    void onProgramListChanged() throws RemoteException;

    void onProgramListUpdated(ProgramList.Chunk chunk) throws RemoteException;

    void onTrafficAnnouncement(boolean z) throws RemoteException;

    void onTuneFailed(int i, ProgramSelector programSelector) throws RemoteException;

    public static class Default implements ITunerCallback {
        @Override // android.hardware.radio.ITunerCallback
        public void onError(int status) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onTuneFailed(int result, ProgramSelector selector) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onConfigurationChanged(RadioManager.BandConfig config) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onCurrentProgramInfoChanged(RadioManager.ProgramInfo info) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onTrafficAnnouncement(boolean active) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onEmergencyAnnouncement(boolean active) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onAntennaState(boolean connected) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onBackgroundScanAvailabilityChange(boolean isAvailable) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onBackgroundScanComplete() throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onProgramListChanged() throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onProgramListUpdated(ProgramList.Chunk chunk) throws RemoteException {
        }

        @Override // android.hardware.radio.ITunerCallback
        public void onParametersUpdated(Map parameters) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITunerCallback {
        private static final String DESCRIPTOR = "android.hardware.radio.ITunerCallback";
        static final int TRANSACTION_onAntennaState = 7;
        static final int TRANSACTION_onBackgroundScanAvailabilityChange = 8;
        static final int TRANSACTION_onBackgroundScanComplete = 9;
        static final int TRANSACTION_onConfigurationChanged = 3;
        static final int TRANSACTION_onCurrentProgramInfoChanged = 4;
        static final int TRANSACTION_onEmergencyAnnouncement = 6;
        static final int TRANSACTION_onError = 1;
        static final int TRANSACTION_onParametersUpdated = 12;
        static final int TRANSACTION_onProgramListChanged = 10;
        static final int TRANSACTION_onProgramListUpdated = 11;
        static final int TRANSACTION_onTrafficAnnouncement = 5;
        static final int TRANSACTION_onTuneFailed = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITunerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITunerCallback)) {
                return new Proxy(obj);
            }
            return (ITunerCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onError";
                case 2:
                    return "onTuneFailed";
                case 3:
                    return "onConfigurationChanged";
                case 4:
                    return "onCurrentProgramInfoChanged";
                case 5:
                    return "onTrafficAnnouncement";
                case 6:
                    return "onEmergencyAnnouncement";
                case 7:
                    return "onAntennaState";
                case 8:
                    return "onBackgroundScanAvailabilityChange";
                case 9:
                    return "onBackgroundScanComplete";
                case 10:
                    return "onProgramListChanged";
                case 11:
                    return "onProgramListUpdated";
                case 12:
                    return "onParametersUpdated";
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
            ProgramSelector _arg1;
            RadioManager.BandConfig _arg0;
            RadioManager.ProgramInfo _arg02;
            ProgramList.Chunk _arg03;
            if (code != 1598968902) {
                boolean _arg04 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ProgramSelector.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onTuneFailed(_arg05, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = RadioManager.BandConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onConfigurationChanged(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = RadioManager.ProgramInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onCurrentProgramInfoChanged(_arg02);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onTrafficAnnouncement(_arg04);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onEmergencyAnnouncement(_arg04);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onAntennaState(_arg04);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onBackgroundScanAvailabilityChange(_arg04);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onBackgroundScanComplete();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onProgramListChanged();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ProgramList.Chunk.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onProgramListUpdated(_arg03);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        onParametersUpdated(data.readHashMap(getClass().getClassLoader()));
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
        public static class Proxy implements ITunerCallback {
            public static ITunerCallback sDefaultImpl;
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

            @Override // android.hardware.radio.ITunerCallback
            public void onError(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onTuneFailed(int result, ProgramSelector selector) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result);
                    if (selector != null) {
                        _data.writeInt(1);
                        selector.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTuneFailed(result, selector);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onConfigurationChanged(RadioManager.BandConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConfigurationChanged(config);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onCurrentProgramInfoChanged(RadioManager.ProgramInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCurrentProgramInfoChanged(info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onTrafficAnnouncement(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active ? 1 : 0);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTrafficAnnouncement(active);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onEmergencyAnnouncement(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active ? 1 : 0);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEmergencyAnnouncement(active);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onAntennaState(boolean connected) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(connected ? 1 : 0);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAntennaState(connected);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onBackgroundScanAvailabilityChange(boolean isAvailable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isAvailable ? 1 : 0);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBackgroundScanAvailabilityChange(isAvailable);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onBackgroundScanComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBackgroundScanComplete();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onProgramListChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProgramListChanged();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onProgramListUpdated(ProgramList.Chunk chunk) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (chunk != null) {
                        _data.writeInt(1);
                        chunk.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onProgramListUpdated(chunk);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.radio.ITunerCallback
            public void onParametersUpdated(Map parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(parameters);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onParametersUpdated(parameters);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITunerCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITunerCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
