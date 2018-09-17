package com.nxp.nfc;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INxpNfcAdapterExtras extends IInterface {

    public static abstract class Stub extends Binder implements INxpNfcAdapterExtras {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapterExtras";
        static final int TRANSACTION_deliverSeIntent = 7;
        static final int TRANSACTION_doGetRouting = 5;
        static final int TRANSACTION_getAtr = 4;
        static final int TRANSACTION_getSecureElementTechList = 1;
        static final int TRANSACTION_getSecureElementUid = 2;
        static final int TRANSACTION_getSelectedUicc = 9;
        static final int TRANSACTION_notifyCheckCertResult = 6;
        static final int TRANSACTION_reset = 3;
        static final int TRANSACTION_selectUicc = 8;

        private static class Proxy implements INxpNfcAdapterExtras {
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

            public int getSecureElementTechList(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getSecureElementTechList, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getSecureElementUid(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getSecureElementUid, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean reset(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_reset, _data, _reply, 0);
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

            public Bundle getAtr(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getAtr, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] doGetRouting() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_doGetRouting, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyCheckCertResult(String pkg, boolean success) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (success) {
                        i = Stub.TRANSACTION_getSecureElementTechList;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_notifyCheckCertResult, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deliverSeIntent(String pkg, Intent seIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (seIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getSecureElementTechList);
                        seIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deliverSeIntent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int selectUicc(int uiccSlot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uiccSlot);
                    this.mRemote.transact(Stub.TRANSACTION_selectUicc, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSelectedUicc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSelectedUicc, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            int _result;
            byte[] _result2;
            String _arg0;
            switch (code) {
                case TRANSACTION_getSecureElementTechList /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSecureElementTechList(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getSecureElementUid /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getSecureElementUid(data.readString());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_reset /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = reset(data.readString());
                    reply.writeNoException();
                    if (_result3) {
                        i = TRANSACTION_getSecureElementTechList;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_getAtr /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result4 = getAtr(data.readString());
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_getSecureElementTechList);
                        _result4.writeToParcel(reply, TRANSACTION_getSecureElementTechList);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_doGetRouting /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = doGetRouting();
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_notifyCheckCertResult /*6*/:
                    boolean _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    } else {
                        _arg1 = false;
                    }
                    notifyCheckCertResult(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deliverSeIntent /*7*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    deliverSeIntent(_arg0, intent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_selectUicc /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = selectUicc(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getSelectedUicc /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSelectedUicc();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void deliverSeIntent(String str, Intent intent) throws RemoteException;

    byte[] doGetRouting() throws RemoteException;

    Bundle getAtr(String str) throws RemoteException;

    int getSecureElementTechList(String str) throws RemoteException;

    byte[] getSecureElementUid(String str) throws RemoteException;

    int getSelectedUicc() throws RemoteException;

    void notifyCheckCertResult(String str, boolean z) throws RemoteException;

    boolean reset(String str) throws RemoteException;

    int selectUicc(int i) throws RemoteException;
}
