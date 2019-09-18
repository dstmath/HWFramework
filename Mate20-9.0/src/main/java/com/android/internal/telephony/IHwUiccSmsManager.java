package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwUiccSmsManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwUiccSmsManager {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwUiccSmsManager";
        static final int TRANSACTION_getMeidOrPesn = 4;
        static final int TRANSACTION_getSmscAddrForSubscriber = 1;
        static final int TRANSACTION_isUimSupportMeid = 3;
        static final int TRANSACTION_setCellBroadcastRangeListForSubscriber = 6;
        static final int TRANSACTION_setEnabledSingleShiftTables = 7;
        static final int TRANSACTION_setMeidOrPesn = 5;
        static final int TRANSACTION_setSmsCodingNationalCode = 8;
        static final int TRANSACTION_setSmscAddrForSubscriber = 2;

        private static class Proxy implements IHwUiccSmsManager {
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

            public String getSmscAddrForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSmscAddrForSubscriber(int subId, String smscAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(smscAddr);
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

            public boolean isUimSupportMeid(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public String getMeidOrPesn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setMeidOrPesn(int subId, String meid, String pesn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(meid);
                    _data.writeString(pesn);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
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

            public boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeIntArray(messageIds);
                    _data.writeInt(ranType);
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

            public void setEnabledSingleShiftTables(int[] tables) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(tables);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSmsCodingNationalCode(String code) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(code);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwUiccSmsManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwUiccSmsManager)) {
                return new Proxy(obj);
            }
            return (IHwUiccSmsManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getSmscAddrForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = setSmscAddrForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result3 = isUimSupportMeid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getMeidOrPesn(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result5 = setMeidOrPesn(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = setCellBroadcastRangeListForSubscriber(data.readInt(), data.createIntArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setEnabledSingleShiftTables(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setSmsCodingNationalCode(data.readString());
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
    }

    String getMeidOrPesn(int i) throws RemoteException;

    String getSmscAddrForSubscriber(int i) throws RemoteException;

    boolean isUimSupportMeid(int i) throws RemoteException;

    boolean setCellBroadcastRangeListForSubscriber(int i, int[] iArr, int i2) throws RemoteException;

    void setEnabledSingleShiftTables(int[] iArr) throws RemoteException;

    boolean setMeidOrPesn(int i, String str, String str2) throws RemoteException;

    void setSmsCodingNationalCode(String str) throws RemoteException;

    boolean setSmscAddrForSubscriber(int i, String str) throws RemoteException;
}
