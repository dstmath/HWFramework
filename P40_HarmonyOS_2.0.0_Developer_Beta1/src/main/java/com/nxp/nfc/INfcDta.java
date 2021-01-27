package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INfcDta extends IInterface {
    boolean snepDtaCmd(String str, String str2, int i, int i2, int i3, int i4) throws RemoteException;

    public static class Default implements INfcDta {
        @Override // com.nxp.nfc.INfcDta
        public boolean snepDtaCmd(String cmdType, String serviceName, int serviceSap, int miu, int rwSize, int testCaseId) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcDta {
        private static final String DESCRIPTOR = "com.nxp.nfc.INfcDta";
        static final int TRANSACTION_snepDtaCmd = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcDta asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcDta)) {
                return new Proxy(obj);
            }
            return (INfcDta) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean snepDtaCmd = snepDtaCmd(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(snepDtaCmd ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INfcDta {
            public static INfcDta sDefaultImpl;
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

            @Override // com.nxp.nfc.INfcDta
            public boolean snepDtaCmd(String cmdType, String serviceName, int serviceSap, int miu, int rwSize, int testCaseId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(cmdType);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(serviceName);
                        try {
                            _data.writeInt(serviceSap);
                            try {
                                _data.writeInt(miu);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(rwSize);
                        try {
                            _data.writeInt(testCaseId);
                            boolean _result = false;
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean snepDtaCmd = Stub.getDefaultImpl().snepDtaCmd(cmdType, serviceName, serviceSap, miu, rwSize, testCaseId);
                            _reply.recycle();
                            _data.recycle();
                            return snepDtaCmd;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }
        }

        public static boolean setDefaultImpl(INfcDta impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcDta getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
