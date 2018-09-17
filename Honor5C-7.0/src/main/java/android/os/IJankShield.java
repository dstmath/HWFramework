package android.os;

public interface IJankShield extends IInterface {

    public static abstract class Stub extends Binder implements IJankShield {
        private static final String DESCRIPTOR = "android.os.IJankShield";
        private static final String TAG = "JankShield";
        static final int TRANSACTION_checkPerfBug = 4;
        static final int TRANSACTION_getJankAppInfo = 5;
        static final int TRANSACTION_getJankCpuInfo = 7;
        static final int TRANSACTION_getJankProductInfo = 6;
        static final int TRANSACTION_getState = 1;
        static final int TRANSACTION_insertJankBd = 3;
        static final int TRANSACTION_insertJankEvent = 2;

        private static class Proxy implements IJankShield {
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

            public boolean getState(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_getState, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void insertJankEvent(JankEventData jankevent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (jankevent != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        jankevent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_insertJankEvent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void insertJankBd(JankBdData jankbd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (jankbd != null) {
                        _data.writeInt(Stub.TRANSACTION_getState);
                        jankbd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_insertJankBd, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public JankCheckPerfBug checkPerfBug() throws RemoteException {
                JankCheckPerfBug jankCheckPerfBug = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_checkPerfBug, _data, _reply, 0);
                    _reply.readException();
                    jankCheckPerfBug = (JankCheckPerfBug) JankCheckPerfBug.CREATOR.createFromParcel(_reply);
                    return jankCheckPerfBug;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public JankAppInfo getJankAppInfo(String packageName) throws RemoteException {
                JankAppInfo jankAppInfo = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getJankAppInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == Stub.TRANSACTION_getState) {
                        jankAppInfo = (JankAppInfo) JankAppInfo.CREATOR.createFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return jankAppInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public JankProductInfo getJankProductInfo() throws RemoteException {
                JankProductInfo jankProductInfo = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getJankProductInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == Stub.TRANSACTION_getState) {
                        jankProductInfo = (JankProductInfo) JankProductInfo.CREATOR.createFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return jankProductInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public JankCpuInfo getJankCpuInfo(int topN) throws RemoteException {
                JankCpuInfo jankCpuInfo = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(topN);
                    this.mRemote.transact(Stub.TRANSACTION_getJankCpuInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == Stub.TRANSACTION_getState) {
                        jankCpuInfo = (JankCpuInfo) JankCpuInfo.CREATOR.createFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return jankCpuInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IJankShield asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IJankShield)) {
                return new Proxy(obj);
            }
            return (IJankShield) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            switch (code) {
                case TRANSACTION_getState /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = getState(data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_getState;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_insertJankEvent /*2*/:
                    JankEventData jankEventData;
                    data.enforceInterface(DESCRIPTOR);
                    if (TRANSACTION_getState == data.readInt()) {
                        jankEventData = (JankEventData) JankEventData.CREATOR.createFromParcel(data);
                    } else {
                        jankEventData = null;
                    }
                    insertJankEvent(jankEventData);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_insertJankBd /*3*/:
                    JankBdData jankBdData;
                    data.enforceInterface(DESCRIPTOR);
                    if (TRANSACTION_getState == data.readInt()) {
                        jankBdData = (JankBdData) JankBdData.CREATOR.createFromParcel(data);
                    } else {
                        jankBdData = null;
                    }
                    insertJankBd(jankBdData);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_checkPerfBug /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    JankCheckPerfBug jankcheckbug = checkPerfBug();
                    if (jankcheckbug != null) {
                        reply.writeNoException();
                        jankcheckbug.writeToParcel(reply, 0);
                    }
                    return true;
                case TRANSACTION_getJankAppInfo /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    JankAppInfo jankappinfo = getJankAppInfo(data.readString());
                    reply.writeNoException();
                    if (jankappinfo != null) {
                        reply.writeInt(TRANSACTION_getState);
                        jankappinfo.writeToParcel(reply, 0);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getJankProductInfo /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    JankProductInfo jankproductinfo = getJankProductInfo();
                    reply.writeNoException();
                    if (jankproductinfo != null) {
                        reply.writeInt(TRANSACTION_getState);
                        jankproductinfo.writeToParcel(reply, 0);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getJankCpuInfo /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    JankCpuInfo jankcpuinfo = getJankCpuInfo(data.readInt());
                    reply.writeNoException();
                    if (jankcpuinfo != null) {
                        reply.writeInt(TRANSACTION_getState);
                        jankcpuinfo.writeToParcel(reply, 0);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    JankCheckPerfBug checkPerfBug() throws RemoteException;

    JankAppInfo getJankAppInfo(String str) throws RemoteException;

    JankCpuInfo getJankCpuInfo(int i) throws RemoteException;

    JankProductInfo getJankProductInfo() throws RemoteException;

    boolean getState(String str) throws RemoteException;

    void insertJankBd(JankBdData jankBdData) throws RemoteException;

    void insertJankEvent(JankEventData jankEventData) throws RemoteException;
}
