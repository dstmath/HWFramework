package com.nxp.nfc.gsma.internal;

import android.nfc.cardemulation.NxpApduServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface INxpNfcController extends IInterface {
    boolean commitOffHostService(int i, String str, String str2, NxpApduServiceInfo nxpApduServiceInfo) throws RemoteException;

    boolean deleteOffHostService(int i, String str, NxpApduServiceInfo nxpApduServiceInfo) throws RemoteException;

    boolean enableMultiEvt_NxptransactionReception(String str, String str2) throws RemoteException;

    void enableMultiReception(String str, String str2) throws RemoteException;

    NxpApduServiceInfo getDefaultOffHostService(int i, String str) throws RemoteException;

    List<NxpApduServiceInfo> getOffHostServices(int i, String str) throws RemoteException;

    public static class Default implements INxpNfcController {
        @Override // com.nxp.nfc.gsma.internal.INxpNfcController
        public boolean deleteOffHostService(int userId, String packageName, NxpApduServiceInfo service) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.gsma.internal.INxpNfcController
        public List<NxpApduServiceInfo> getOffHostServices(int userId, String packageName) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.gsma.internal.INxpNfcController
        public NxpApduServiceInfo getDefaultOffHostService(int userId, String packageName) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.gsma.internal.INxpNfcController
        public boolean commitOffHostService(int userId, String packageName, String serviceName, NxpApduServiceInfo service) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.gsma.internal.INxpNfcController
        public boolean enableMultiEvt_NxptransactionReception(String packageName, String seName) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.gsma.internal.INxpNfcController
        public void enableMultiReception(String pkg, String seName) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INxpNfcController {
        private static final String DESCRIPTOR = "com.nxp.nfc.gsma.internal.INxpNfcController";
        static final int TRANSACTION_commitOffHostService = 4;
        static final int TRANSACTION_deleteOffHostService = 1;
        static final int TRANSACTION_enableMultiEvt_NxptransactionReception = 5;
        static final int TRANSACTION_enableMultiReception = 6;
        static final int TRANSACTION_getDefaultOffHostService = 3;
        static final int TRANSACTION_getOffHostServices = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpNfcController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpNfcController)) {
                return new Proxy(obj);
            }
            return (INxpNfcController) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NxpApduServiceInfo _arg2;
            NxpApduServiceInfo _arg3;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = NxpApduServiceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean deleteOffHostService = deleteOffHostService(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(deleteOffHostService ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<NxpApduServiceInfo> _result = getOffHostServices(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        NxpApduServiceInfo _result2 = getDefaultOffHostService(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        String _arg12 = data.readString();
                        String _arg22 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = NxpApduServiceInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        boolean commitOffHostService = commitOffHostService(_arg02, _arg12, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(commitOffHostService ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableMultiEvt_NxptransactionReception = enableMultiEvt_NxptransactionReception(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(enableMultiEvt_NxptransactionReception ? 1 : 0);
                        return true;
                    case TRANSACTION_enableMultiReception /*{ENCODED_INT: 6}*/:
                        data.enforceInterface(DESCRIPTOR);
                        enableMultiReception(data.readString(), data.readString());
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
        public static class Proxy implements INxpNfcController {
            public static INxpNfcController sDefaultImpl;
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

            @Override // com.nxp.nfc.gsma.internal.INxpNfcController
            public boolean deleteOffHostService(int userId, String packageName, NxpApduServiceInfo service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteOffHostService(userId, packageName, service);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.gsma.internal.INxpNfcController
            public List<NxpApduServiceInfo> getOffHostServices(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOffHostServices(userId, packageName);
                    }
                    _reply.readException();
                    List<NxpApduServiceInfo> _result = _reply.createTypedArrayList(NxpApduServiceInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.gsma.internal.INxpNfcController
            public NxpApduServiceInfo getDefaultOffHostService(int userId, String packageName) throws RemoteException {
                NxpApduServiceInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultOffHostService(userId, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NxpApduServiceInfo.CREATOR.createFromParcel(_reply);
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

            @Override // com.nxp.nfc.gsma.internal.INxpNfcController
            public boolean commitOffHostService(int userId, String packageName, String serviceName, NxpApduServiceInfo service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    _data.writeString(serviceName);
                    boolean _result = true;
                    if (service != null) {
                        _data.writeInt(1);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().commitOffHostService(userId, packageName, serviceName, service);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.nfc.gsma.internal.INxpNfcController
            public boolean enableMultiEvt_NxptransactionReception(String packageName, String seName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(seName);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableMultiEvt_NxptransactionReception(packageName, seName);
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

            @Override // com.nxp.nfc.gsma.internal.INxpNfcController
            public void enableMultiReception(String pkg, String seName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(seName);
                    if (this.mRemote.transact(Stub.TRANSACTION_enableMultiReception, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableMultiReception(pkg, seName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INxpNfcController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INxpNfcController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
