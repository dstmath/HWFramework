package com.nxp.nfc;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INxpNfcAdapterExtras extends IInterface {
    Bundle closeuicc(String str, IBinder iBinder) throws RemoteException;

    void deliverSeIntent(String str, Intent intent) throws RemoteException;

    byte[] doGetRouting() throws RemoteException;

    boolean eSEChipReset(String str) throws RemoteException;

    Bundle getAtr(String str) throws RemoteException;

    int getSecureElementTechList(String str) throws RemoteException;

    byte[] getSecureElementUid(String str) throws RemoteException;

    int getSelectedUicc() throws RemoteException;

    void notifyCheckCertResult(String str, boolean z) throws RemoteException;

    Bundle openuicc(String str, IBinder iBinder) throws RemoteException;

    boolean reset(String str) throws RemoteException;

    int selectUicc(int i) throws RemoteException;

    Bundle transceiveuicc(String str, byte[] bArr) throws RemoteException;

    public static class Default implements INxpNfcAdapterExtras {
        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public int getSecureElementTechList(String pkg) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public byte[] getSecureElementUid(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public boolean reset(String pkg) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public Bundle getAtr(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public byte[] doGetRouting() throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public void notifyCheckCertResult(String pkg, boolean success) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public void deliverSeIntent(String pkg, Intent seIntent) throws RemoteException {
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public int selectUicc(int uiccSlot) throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public int getSelectedUicc() throws RemoteException {
            return 0;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public Bundle openuicc(String pkg, IBinder b) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public Bundle closeuicc(String pkg, IBinder b) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public Bundle transceiveuicc(String pkg, byte[] data_in) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public boolean eSEChipReset(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INxpNfcAdapterExtras {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapterExtras";
        static final int TRANSACTION_closeuicc = 11;
        static final int TRANSACTION_deliverSeIntent = 7;
        static final int TRANSACTION_doGetRouting = 5;
        static final int TRANSACTION_eSEChipReset = 13;
        static final int TRANSACTION_getAtr = 4;
        static final int TRANSACTION_getSecureElementTechList = 1;
        static final int TRANSACTION_getSecureElementUid = 2;
        static final int TRANSACTION_getSelectedUicc = 9;
        static final int TRANSACTION_notifyCheckCertResult = 6;
        static final int TRANSACTION_openuicc = 10;
        static final int TRANSACTION_reset = 3;
        static final int TRANSACTION_selectUicc = 8;
        static final int TRANSACTION_transceiveuicc = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpNfcAdapterExtras asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpNfcAdapterExtras)) {
                return new Proxy(obj);
            }
            return (INxpNfcAdapterExtras) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg1;
            if (code != 1598968902) {
                boolean _arg12 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getSecureElementTechList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = getSecureElementUid(data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean reset = reset(data.readString());
                        reply.writeNoException();
                        reply.writeInt(reset ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result3 = getAtr(data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result4 = doGetRouting();
                        reply.writeNoException();
                        reply.writeByteArray(_result4);
                        return true;
                    case TRANSACTION_notifyCheckCertResult /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        notifyCheckCertResult(_arg0, _arg12);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_deliverSeIntent /* 7 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (Intent) Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        deliverSeIntent(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_selectUicc /* 8 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = selectUicc(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case TRANSACTION_getSelectedUicc /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getSelectedUicc();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case TRANSACTION_openuicc /* 10 */:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result7 = openuicc(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_closeuicc /* 11 */:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result8 = closeuicc(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_transceiveuicc /* 12 */:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result9 = transceiveuicc(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_eSEChipReset /* 13 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean eSEChipReset = eSEChipReset(data.readString());
                        reply.writeNoException();
                        reply.writeInt(eSEChipReset ? 1 : 0);
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
        public static class Proxy implements INxpNfcAdapterExtras {
            public static INxpNfcAdapterExtras sDefaultImpl;
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public int getSecureElementTechList(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureElementTechList(pkg);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public byte[] getSecureElementUid(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureElementUid(pkg);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public boolean reset(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reset(pkg);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public Bundle getAtr(String pkg) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAtr(pkg);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public byte[] doGetRouting() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().doGetRouting();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public void notifyCheckCertResult(String pkg, boolean success) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(success ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_notifyCheckCertResult, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyCheckCertResult(pkg, success);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public void deliverSeIntent(String pkg, Intent seIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (seIntent != null) {
                        _data.writeInt(1);
                        seIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_deliverSeIntent, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deliverSeIntent(pkg, seIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public int selectUicc(int uiccSlot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uiccSlot);
                    if (!this.mRemote.transact(Stub.TRANSACTION_selectUicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().selectUicc(uiccSlot);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public int getSelectedUicc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getSelectedUicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSelectedUicc();
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public Bundle openuicc(String pkg, IBinder b) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    if (!this.mRemote.transact(Stub.TRANSACTION_openuicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openuicc(pkg, b);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public Bundle closeuicc(String pkg, IBinder b) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    if (!this.mRemote.transact(Stub.TRANSACTION_closeuicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().closeuicc(pkg, b);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public Bundle transceiveuicc(String pkg, byte[] data_in) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeByteArray(data_in);
                    if (!this.mRemote.transact(Stub.TRANSACTION_transceiveuicc, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().transceiveuicc(pkg, data_in);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public boolean eSEChipReset(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_eSEChipReset, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().eSEChipReset(pkg);
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
        }

        public static boolean setDefaultImpl(INxpNfcAdapterExtras impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INxpNfcAdapterExtras getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
