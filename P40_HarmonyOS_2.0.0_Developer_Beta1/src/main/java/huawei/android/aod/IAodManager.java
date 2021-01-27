package huawei.android.aod;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IAodManager extends IInterface {
    void beginUpdate() throws RemoteException;

    void configAndStart(AodConfigInfo aodConfigInfo, int i, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void configAndUpdate(AodConfigInfo aodConfigInfo, int i, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void configTpInApMode(int i) throws RemoteException;

    void endUpdate() throws RemoteException;

    Bundle getAodInfo(int i) throws RemoteException;

    int getAodStatus() throws RemoteException;

    int getDeviceNodeFD() throws RemoteException;

    void pause() throws RemoteException;

    void pauseAodWithScreenState(int i) throws RemoteException;

    void resume() throws RemoteException;

    void sendCommandToTp(int i, String str) throws RemoteException;

    void setAodConfig(AodConfigInfo aodConfigInfo) throws RemoteException;

    void setBitmapByMemoryFile(int i, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void setPowerState(int i) throws RemoteException;

    void start() throws RemoteException;

    void stop() throws RemoteException;

    void updateAodVolumeInfo(AodVolumeInfo aodVolumeInfo) throws RemoteException;

    public static class Default implements IAodManager {
        @Override // huawei.android.aod.IAodManager
        public void setAodConfig(AodConfigInfo aodInfo) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void start() throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void configAndStart(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void stop() throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void pause() throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void pauseAodWithScreenState(int screenState) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void resume() throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void beginUpdate() throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void configAndUpdate(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void endUpdate() throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void setBitmapByMemoryFile(int fileSize, ParcelFileDescriptor pfd) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public int getDeviceNodeFD() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.aod.IAodManager
        public void setPowerState(int state) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public int getAodStatus() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.aod.IAodManager
        public void updateAodVolumeInfo(AodVolumeInfo aodVolumeInfo) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public void sendCommandToTp(int featureFlag, String cmdStr) throws RemoteException {
        }

        @Override // huawei.android.aod.IAodManager
        public Bundle getAodInfo(int infoType) throws RemoteException {
            return null;
        }

        @Override // huawei.android.aod.IAodManager
        public void configTpInApMode(int aodState) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAodManager {
        private static final String DESCRIPTOR = "huawei.android.aod.IAodManager";
        static final int TRANSACTION_beginUpdate = 8;
        static final int TRANSACTION_configAndStart = 3;
        static final int TRANSACTION_configAndUpdate = 9;
        static final int TRANSACTION_configTpInApMode = 18;
        static final int TRANSACTION_endUpdate = 10;
        static final int TRANSACTION_getAodInfo = 17;
        static final int TRANSACTION_getAodStatus = 14;
        static final int TRANSACTION_getDeviceNodeFD = 12;
        static final int TRANSACTION_pause = 5;
        static final int TRANSACTION_pauseAodWithScreenState = 6;
        static final int TRANSACTION_resume = 7;
        static final int TRANSACTION_sendCommandToTp = 16;
        static final int TRANSACTION_setAodConfig = 1;
        static final int TRANSACTION_setBitmapByMemoryFile = 11;
        static final int TRANSACTION_setPowerState = 13;
        static final int TRANSACTION_start = 2;
        static final int TRANSACTION_stop = 4;
        static final int TRANSACTION_updateAodVolumeInfo = 15;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAodManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAodManager)) {
                return new Proxy(obj);
            }
            return (IAodManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AodConfigInfo _arg0;
            AodConfigInfo _arg02;
            ParcelFileDescriptor _arg2;
            AodConfigInfo _arg03;
            ParcelFileDescriptor _arg22;
            ParcelFileDescriptor _arg1;
            AodVolumeInfo _arg04;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AodConfigInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setAodConfig(_arg0);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        start();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = AodConfigInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        configAndStart(_arg02, _arg12, _arg2);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        stop();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        pause();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        pauseAodWithScreenState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        resume();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        beginUpdate();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = AodConfigInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        configAndUpdate(_arg03, _arg13, _arg22);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        endUpdate();
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setBitmapByMemoryFile(_arg05, _arg1);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getDeviceNodeFD();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        setPowerState(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getAodStatus();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = AodVolumeInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        updateAodVolumeInfo(_arg04);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        sendCommandToTp(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result3 = getAodInfo(data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        configTpInApMode(data.readInt());
                        reply.writeNoException();
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
        public static class Proxy implements IAodManager {
            public static IAodManager sDefaultImpl;
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

            @Override // huawei.android.aod.IAodManager
            public void setAodConfig(AodConfigInfo aodInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (aodInfo != null) {
                        _data.writeInt(1);
                        aodInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAodConfig(aodInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void start() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().start();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void configAndStart(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (aodInfo != null) {
                        _data.writeInt(1);
                        aodInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(fileSize);
                    if (pfd != null) {
                        _data.writeInt(1);
                        pfd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().configAndStart(aodInfo, fileSize, pfd);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stop();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pause();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void pauseAodWithScreenState(int screenState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(screenState);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pauseAodWithScreenState(screenState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void resume() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resume();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void beginUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().beginUpdate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void configAndUpdate(AodConfigInfo aodInfo, int fileSize, ParcelFileDescriptor pfd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (aodInfo != null) {
                        _data.writeInt(1);
                        aodInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(fileSize);
                    if (pfd != null) {
                        _data.writeInt(1);
                        pfd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().configAndUpdate(aodInfo, fileSize, pfd);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void endUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().endUpdate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void setBitmapByMemoryFile(int fileSize, ParcelFileDescriptor pfd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fileSize);
                    if (pfd != null) {
                        _data.writeInt(1);
                        pfd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBitmapByMemoryFile(fileSize, pfd);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public int getDeviceNodeFD() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceNodeFD();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void setPowerState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPowerState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public int getAodStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAodStatus();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void updateAodVolumeInfo(AodVolumeInfo aodVolumeInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (aodVolumeInfo != null) {
                        _data.writeInt(1);
                        aodVolumeInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateAodVolumeInfo(aodVolumeInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void sendCommandToTp(int featureFlag, String cmdStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureFlag);
                    _data.writeString(cmdStr);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendCommandToTp(featureFlag, cmdStr);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public Bundle getAodInfo(int infoType) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(infoType);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAodInfo(infoType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.aod.IAodManager
            public void configTpInApMode(int aodState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(aodState);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().configTpInApMode(aodState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAodManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAodManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
