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

        private static class Proxy implements ITunerCallback {
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

            public void onError(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onTrafficAnnouncement(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onEmergencyAnnouncement(boolean active) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(active);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onAntennaState(boolean connected) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(connected);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onBackgroundScanAvailabilityChange(boolean isAvailable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isAvailable);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onBackgroundScanComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onProgramListChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onParametersUpdated(Map parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(parameters);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v0, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v8, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v9, resolved type: android.hardware.radio.RadioManager$ProgramInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v15, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v21, resolved type: android.hardware.radio.ProgramList$Chunk} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v19, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v20, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v21, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v22, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX WARNING: type inference failed for: r1v5, types: [android.hardware.radio.RadioManager$BandConfig] */
        /* JADX WARNING: type inference failed for: r3v7, types: [android.hardware.radio.RadioManager$BandConfig] */
        /* JADX WARNING: type inference failed for: r3v10, types: [android.hardware.radio.RadioManager$ProgramInfo] */
        /* JADX WARNING: type inference failed for: r3v17, types: [android.hardware.radio.ProgramList$Chunk] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                ProgramSelector _arg1 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = ProgramSelector.CREATOR.createFromParcel(data);
                        }
                        onTuneFailed(_arg02, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = RadioManager.BandConfig.CREATOR.createFromParcel(data);
                        }
                        onConfigurationChanged(_arg1);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = RadioManager.ProgramInfo.CREATOR.createFromParcel(data);
                        }
                        onCurrentProgramInfoChanged(_arg1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onTrafficAnnouncement(_arg0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onEmergencyAnnouncement(_arg0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onAntennaState(_arg0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onBackgroundScanAvailabilityChange(_arg0);
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
                            _arg1 = ProgramList.Chunk.CREATOR.createFromParcel(data);
                        }
                        onProgramListUpdated(_arg1);
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
    }

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
}
