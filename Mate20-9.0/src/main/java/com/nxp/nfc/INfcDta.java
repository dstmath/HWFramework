package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INfcDta extends IInterface {

    public static abstract class Stub extends Binder implements INfcDta {
        private static final String DESCRIPTOR = "com.nxp.nfc.INfcDta";
        static final int TRANSACTION_snepDtaCmd = 1;

        private static class Proxy implements INfcDta {
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

            public boolean snepDtaCmd(String cmdType, String serviceName, int serviceSap, int miu, int rwSize, int testCaseId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cmdType);
                    _data.writeString(serviceName);
                    _data.writeInt(serviceSap);
                    _data.writeInt(miu);
                    _data.writeInt(rwSize);
                    _data.writeInt(testCaseId);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
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
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = reply;
            if (i == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean _result = snepDtaCmd(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                parcel.writeInt(_result);
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                parcel.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean snepDtaCmd(String str, String str2, int i, int i2, int i3, int i4) throws RemoteException;
}
