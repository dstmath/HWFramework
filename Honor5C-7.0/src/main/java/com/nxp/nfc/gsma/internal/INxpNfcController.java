package com.nxp.nfc.gsma.internal;

import android.nfc.cardemulation.ApduServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface INxpNfcController extends IInterface {

    public static abstract class Stub extends Binder implements INxpNfcController {
        private static final String DESCRIPTOR = "com.nxp.nfc.gsma.internal.INxpNfcController";
        static final int TRANSACTION_commitOffHostService = 4;
        static final int TRANSACTION_deleteOffHostService = 1;
        static final int TRANSACTION_enableMultiEvt_NxptransactionReception = 5;
        static final int TRANSACTION_enableMultiReception = 6;
        static final int TRANSACTION_getDefaultOffHostService = 3;
        static final int TRANSACTION_getOffHostServices = 2;

        private static class Proxy implements INxpNfcController {
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

            public boolean deleteOffHostService(int userId, String packageName, ApduServiceInfo service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    if (service != null) {
                        _data.writeInt(Stub.TRANSACTION_deleteOffHostService);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_deleteOffHostService, _data, _reply, 0);
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

            public List<ApduServiceInfo> getOffHostServices(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getOffHostServices, _data, _reply, 0);
                    _reply.readException();
                    List<ApduServiceInfo> _result = _reply.createTypedArrayList(ApduServiceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ApduServiceInfo getDefaultOffHostService(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ApduServiceInfo apduServiceInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultOffHostService, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        apduServiceInfo = (ApduServiceInfo) ApduServiceInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        apduServiceInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return apduServiceInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean commitOffHostService(int userId, String packageName, String serviceName, ApduServiceInfo service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    _data.writeString(serviceName);
                    if (service != null) {
                        _data.writeInt(Stub.TRANSACTION_deleteOffHostService);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_commitOffHostService, _data, _reply, 0);
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

            public boolean enableMultiEvt_NxptransactionReception(String packageName, String seName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(seName);
                    this.mRemote.transact(Stub.TRANSACTION_enableMultiEvt_NxptransactionReception, _data, _reply, 0);
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

            public void enableMultiReception(String pkg, String seName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(seName);
                    this.mRemote.transact(Stub.TRANSACTION_enableMultiReception, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            String _arg1;
            boolean _result;
            switch (code) {
                case TRANSACTION_deleteOffHostService /*1*/:
                    ApduServiceInfo apduServiceInfo;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        apduServiceInfo = (ApduServiceInfo) ApduServiceInfo.CREATOR.createFromParcel(data);
                    } else {
                        apduServiceInfo = null;
                    }
                    _result = deleteOffHostService(_arg0, _arg1, apduServiceInfo);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_deleteOffHostService : 0);
                    return true;
                case TRANSACTION_getOffHostServices /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<ApduServiceInfo> _result2 = getOffHostServices(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getDefaultOffHostService /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    ApduServiceInfo _result3 = getDefaultOffHostService(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_deleteOffHostService);
                        _result3.writeToParcel(reply, TRANSACTION_deleteOffHostService);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_commitOffHostService /*4*/:
                    ApduServiceInfo apduServiceInfo2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    String _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        apduServiceInfo2 = (ApduServiceInfo) ApduServiceInfo.CREATOR.createFromParcel(data);
                    } else {
                        apduServiceInfo2 = null;
                    }
                    _result = commitOffHostService(_arg0, _arg1, _arg2, apduServiceInfo2);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_deleteOffHostService : 0);
                    return true;
                case TRANSACTION_enableMultiEvt_NxptransactionReception /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableMultiEvt_NxptransactionReception(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_deleteOffHostService : 0);
                    return true;
                case TRANSACTION_enableMultiReception /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableMultiReception(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean commitOffHostService(int i, String str, String str2, ApduServiceInfo apduServiceInfo) throws RemoteException;

    boolean deleteOffHostService(int i, String str, ApduServiceInfo apduServiceInfo) throws RemoteException;

    boolean enableMultiEvt_NxptransactionReception(String str, String str2) throws RemoteException;

    void enableMultiReception(String str, String str2) throws RemoteException;

    ApduServiceInfo getDefaultOffHostService(int i, String str) throws RemoteException;

    List<ApduServiceInfo> getOffHostServices(int i, String str) throws RemoteException;
}
