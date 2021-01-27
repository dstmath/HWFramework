package android.os;

public interface IProcessInfoService extends IInterface {
    void getProcessStatesAndOomScoresFromPids(int[] iArr, int[] iArr2, int[] iArr3) throws RemoteException;

    void getProcessStatesFromPids(int[] iArr, int[] iArr2) throws RemoteException;

    public static class Default implements IProcessInfoService {
        @Override // android.os.IProcessInfoService
        public void getProcessStatesFromPids(int[] pids, int[] states) throws RemoteException {
        }

        @Override // android.os.IProcessInfoService
        public void getProcessStatesAndOomScoresFromPids(int[] pids, int[] states, int[] scores) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IProcessInfoService {
        private static final String DESCRIPTOR = "android.os.IProcessInfoService";
        static final int TRANSACTION_getProcessStatesAndOomScoresFromPids = 2;
        static final int TRANSACTION_getProcessStatesFromPids = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IProcessInfoService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IProcessInfoService)) {
                return new Proxy(obj);
            }
            return (IProcessInfoService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getProcessStatesFromPids";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "getProcessStatesAndOomScoresFromPids";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int[] _arg1;
            int[] _arg12;
            int[] _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int[] _arg0 = data.createIntArray();
                int _arg1_length = data.readInt();
                if (_arg1_length < 0) {
                    _arg1 = null;
                } else {
                    _arg1 = new int[_arg1_length];
                }
                getProcessStatesFromPids(_arg0, _arg1);
                reply.writeNoException();
                reply.writeIntArray(_arg1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int[] _arg02 = data.createIntArray();
                int _arg1_length2 = data.readInt();
                if (_arg1_length2 < 0) {
                    _arg12 = null;
                } else {
                    _arg12 = new int[_arg1_length2];
                }
                int _arg2_length = data.readInt();
                if (_arg2_length < 0) {
                    _arg2 = null;
                } else {
                    _arg2 = new int[_arg2_length];
                }
                getProcessStatesAndOomScoresFromPids(_arg02, _arg12, _arg2);
                reply.writeNoException();
                reply.writeIntArray(_arg12);
                reply.writeIntArray(_arg2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IProcessInfoService {
            public static IProcessInfoService sDefaultImpl;
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

            @Override // android.os.IProcessInfoService
            public void getProcessStatesFromPids(int[] pids, int[] states) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    if (states == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(states.length);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readIntArray(states);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getProcessStatesFromPids(pids, states);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IProcessInfoService
            public void getProcessStatesAndOomScoresFromPids(int[] pids, int[] states, int[] scores) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(pids);
                    if (states == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(states.length);
                    }
                    if (scores == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(scores.length);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readIntArray(states);
                        _reply.readIntArray(scores);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getProcessStatesAndOomScoresFromPids(pids, states, scores);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IProcessInfoService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IProcessInfoService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
