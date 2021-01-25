package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwUiccSmsManager extends IInterface {
    String getMeidOrPesn(int i) throws RemoteException;

    String getSmscAddrForSubscriber(int i) throws RemoteException;

    boolean isUimSupportMeid(int i) throws RemoteException;

    void processSmsAntiAttack(int i, int i2, int i3, Message message) throws RemoteException;

    boolean setCellBroadcastRangeListForSubscriber(int i, int[] iArr, int i2) throws RemoteException;

    void setEnabledSingleShiftTables(int[] iArr) throws RemoteException;

    boolean setMeidOrPesn(int i, String str, String str2) throws RemoteException;

    void setSmsCodingNationalCode(String str) throws RemoteException;

    boolean setSmscAddrForSubscriber(int i, String str) throws RemoteException;

    public static class Default implements IHwUiccSmsManager {
        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public String getSmscAddrForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean setSmscAddrForSubscriber(int subId, String smscAddr) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean isUimSupportMeid(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public String getMeidOrPesn(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean setMeidOrPesn(int subId, String meid, String pesn) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public void setEnabledSingleShiftTables(int[] tables) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public void setSmsCodingNationalCode(String code) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IHwUiccSmsManager
        public void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message msg) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwUiccSmsManager {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IHwUiccSmsManager";
        static final int TRANSACTION_getMeidOrPesn = 4;
        static final int TRANSACTION_getSmscAddrForSubscriber = 1;
        static final int TRANSACTION_isUimSupportMeid = 3;
        static final int TRANSACTION_processSmsAntiAttack = 9;
        static final int TRANSACTION_setCellBroadcastRangeListForSubscriber = 6;
        static final int TRANSACTION_setEnabledSingleShiftTables = 7;
        static final int TRANSACTION_setMeidOrPesn = 5;
        static final int TRANSACTION_setSmsCodingNationalCode = 8;
        static final int TRANSACTION_setSmscAddrForSubscriber = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Message _arg3;
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
                        boolean smscAddrForSubscriber = setSmscAddrForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(smscAddrForSubscriber ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUimSupportMeid = isUimSupportMeid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUimSupportMeid ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getMeidOrPesn(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean meidOrPesn = setMeidOrPesn(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(meidOrPesn ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cellBroadcastRangeListForSubscriber = setCellBroadcastRangeListForSubscriber(data.readInt(), data.createIntArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(cellBroadcastRangeListForSubscriber ? 1 : 0);
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
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        int _arg2 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        processSmsAntiAttack(_arg0, _arg1, _arg2, _arg3);
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
        public static class Proxy implements IHwUiccSmsManager {
            public static IHwUiccSmsManager sDefaultImpl;
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

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public String getSmscAddrForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSmscAddrForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public boolean setSmscAddrForSubscriber(int subId, String smscAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(smscAddr);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSmscAddrForSubscriber(subId, smscAddr);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public boolean isUimSupportMeid(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUimSupportMeid(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public String getMeidOrPesn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMeidOrPesn(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public boolean setMeidOrPesn(int subId, String meid, String pesn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(meid);
                    _data.writeString(pesn);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMeidOrPesn(subId, meid, pesn);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeIntArray(messageIds);
                    _data.writeInt(ranType);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCellBroadcastRangeListForSubscriber(subId, messageIds, ranType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public void setEnabledSingleShiftTables(int[] tables) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(tables);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setEnabledSingleShiftTables(tables);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public void setSmsCodingNationalCode(String code) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(code);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSmsCodingNationalCode(code);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IHwUiccSmsManager
            public void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(serviceType);
                    _data.writeInt(smsType);
                    _data.writeInt(slotId);
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().processSmsAntiAttack(serviceType, smsType, slotId, msg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwUiccSmsManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwUiccSmsManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
