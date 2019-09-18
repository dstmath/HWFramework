package com.android.internal.telephony.euicc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.euicc.EuiccRulesAuthTable;

public interface IGetRulesAuthTableCallback extends IInterface {

    public static abstract class Stub extends Binder implements IGetRulesAuthTableCallback {
        private static final String DESCRIPTOR = "com.android.internal.telephony.euicc.IGetRulesAuthTableCallback";
        static final int TRANSACTION_onComplete = 1;

        private static class Proxy implements IGetRulesAuthTableCallback {
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

            public void onComplete(int resultCode, EuiccRulesAuthTable rat) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resultCode);
                    if (rat != null) {
                        _data.writeInt(1);
                        rat.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGetRulesAuthTableCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGetRulesAuthTableCallback)) {
                return new Proxy(obj);
            }
            return (IGetRulesAuthTableCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            EuiccRulesAuthTable _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (EuiccRulesAuthTable) EuiccRulesAuthTable.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onComplete(_arg0, _arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onComplete(int i, EuiccRulesAuthTable euiccRulesAuthTable) throws RemoteException;
}
