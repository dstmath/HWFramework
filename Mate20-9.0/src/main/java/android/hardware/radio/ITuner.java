package android.hardware.radio;

import android.graphics.Bitmap;
import android.hardware.radio.ProgramList;
import android.hardware.radio.RadioManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface ITuner extends IInterface {

    public static abstract class Stub extends Binder implements ITuner {
        private static final String DESCRIPTOR = "android.hardware.radio.ITuner";
        static final int TRANSACTION_cancel = 10;
        static final int TRANSACTION_cancelAnnouncement = 11;
        static final int TRANSACTION_close = 1;
        static final int TRANSACTION_getConfiguration = 4;
        static final int TRANSACTION_getImage = 12;
        static final int TRANSACTION_getParameters = 20;
        static final int TRANSACTION_isClosed = 2;
        static final int TRANSACTION_isConfigFlagSet = 17;
        static final int TRANSACTION_isConfigFlagSupported = 16;
        static final int TRANSACTION_isMuted = 6;
        static final int TRANSACTION_scan = 8;
        static final int TRANSACTION_setConfigFlag = 18;
        static final int TRANSACTION_setConfiguration = 3;
        static final int TRANSACTION_setMuted = 5;
        static final int TRANSACTION_setParameters = 19;
        static final int TRANSACTION_startBackgroundScan = 13;
        static final int TRANSACTION_startProgramListUpdates = 14;
        static final int TRANSACTION_step = 7;
        static final int TRANSACTION_stopProgramListUpdates = 15;
        static final int TRANSACTION_tune = 9;

        private static class Proxy implements ITuner {
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

            public void close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isClosed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setConfiguration(RadioManager.BandConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RadioManager.BandConfig getConfiguration() throws RemoteException {
                RadioManager.BandConfig _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RadioManager.BandConfig.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMuted(boolean mute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mute);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isMuted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void step(boolean directionDown, boolean skipSubChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(directionDown);
                    _data.writeInt(skipSubChannel);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void scan(boolean directionDown, boolean skipSubChannel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(directionDown);
                    _data.writeInt(skipSubChannel);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void tune(ProgramSelector selector) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (selector != null) {
                        _data.writeInt(1);
                        selector.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelAnnouncement() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bitmap getImage(int id) throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean startBackgroundScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startProgramListUpdates(ProgramList.Filter filter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopProgramListUpdates() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConfigFlagSupported(int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConfigFlagSet(int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setConfigFlag(int flag, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    _data.writeInt(value);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map setParameters(Map parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(parameters);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map getParameters(List<String> keys) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(keys);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readHashMap(getClass().getClassLoader());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITuner asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITuner)) {
                return new Proxy(obj);
            }
            return (ITuner) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v19, resolved type: android.hardware.radio.ProgramSelector} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v25, resolved type: android.hardware.radio.ProgramList$Filter} */
        /* JADX WARNING: type inference failed for: r1v1 */
        /* JADX WARNING: type inference failed for: r1v3, types: [android.hardware.radio.RadioManager$BandConfig] */
        /* JADX WARNING: type inference failed for: r1v36 */
        /* JADX WARNING: type inference failed for: r1v37 */
        /* JADX WARNING: type inference failed for: r1v38 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg0 = 0;
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        close();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = isClosed();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = RadioManager.BandConfig.CREATOR.createFromParcel(data);
                        }
                        setConfiguration(_arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        RadioManager.BandConfig _result2 = getConfiguration();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setMuted(_arg1);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isMuted();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg02 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        step(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg03 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        scan(_arg03, _arg1);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProgramSelector.CREATOR.createFromParcel(data);
                        }
                        tune(_arg0);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        cancel();
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAnnouncement();
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result4 = getImage(data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result5 = startBackgroundScan();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProgramList.Filter.CREATOR.createFromParcel(data);
                        }
                        startProgramListUpdates(_arg0);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        stopProgramListUpdates();
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = isConfigFlagSupported(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result7 = isConfigFlagSet(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setConfigFlag(_arg04, _arg1);
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result8 = setParameters(data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeMap(_result8);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result9 = getParameters(data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeMap(_result9);
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

    void cancel() throws RemoteException;

    void cancelAnnouncement() throws RemoteException;

    void close() throws RemoteException;

    RadioManager.BandConfig getConfiguration() throws RemoteException;

    Bitmap getImage(int i) throws RemoteException;

    Map getParameters(List<String> list) throws RemoteException;

    boolean isClosed() throws RemoteException;

    boolean isConfigFlagSet(int i) throws RemoteException;

    boolean isConfigFlagSupported(int i) throws RemoteException;

    boolean isMuted() throws RemoteException;

    void scan(boolean z, boolean z2) throws RemoteException;

    void setConfigFlag(int i, boolean z) throws RemoteException;

    void setConfiguration(RadioManager.BandConfig bandConfig) throws RemoteException;

    void setMuted(boolean z) throws RemoteException;

    Map setParameters(Map map) throws RemoteException;

    boolean startBackgroundScan() throws RemoteException;

    void startProgramListUpdates(ProgramList.Filter filter) throws RemoteException;

    void step(boolean z, boolean z2) throws RemoteException;

    void stopProgramListUpdates() throws RemoteException;

    void tune(ProgramSelector programSelector) throws RemoteException;
}
