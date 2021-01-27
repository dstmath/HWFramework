package android.telephony.mbms.vendor;

import android.content.ContentResolver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.mbms.IGroupCallCallback;
import android.telephony.mbms.IMbmsGroupCallSessionCallback;
import java.util.List;

public interface IMbmsGroupCallService extends IInterface {
    void dispose(int i) throws RemoteException;

    int initialize(IMbmsGroupCallSessionCallback iMbmsGroupCallSessionCallback, int i) throws RemoteException;

    int startGroupCall(int i, long j, List list, List list2, IGroupCallCallback iGroupCallCallback) throws RemoteException;

    void stopGroupCall(int i, long j) throws RemoteException;

    void updateGroupCall(int i, long j, List list, List list2) throws RemoteException;

    public static class Default implements IMbmsGroupCallService {
        @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
        public int initialize(IMbmsGroupCallSessionCallback callback, int subId) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
        public void stopGroupCall(int subId, long tmgi) throws RemoteException {
        }

        @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
        public void updateGroupCall(int subscriptionId, long tmgi, List saiList, List frequencyList) throws RemoteException {
        }

        @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
        public int startGroupCall(int subscriptionId, long tmgi, List saiList, List frequencyList, IGroupCallCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
        public void dispose(int subId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMbmsGroupCallService {
        private static final String DESCRIPTOR = "android.telephony.mbms.vendor.IMbmsGroupCallService";
        static final int TRANSACTION_dispose = 5;
        static final int TRANSACTION_initialize = 1;
        static final int TRANSACTION_startGroupCall = 4;
        static final int TRANSACTION_stopGroupCall = 2;
        static final int TRANSACTION_updateGroupCall = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMbmsGroupCallService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMbmsGroupCallService)) {
                return new Proxy(obj);
            }
            return (IMbmsGroupCallService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return ContentResolver.SYNC_EXTRAS_INITIALIZE;
            }
            if (transactionCode == 2) {
                return "stopGroupCall";
            }
            if (transactionCode == 3) {
                return "updateGroupCall";
            }
            if (transactionCode == 4) {
                return "startGroupCall";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "dispose";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = initialize(IMbmsGroupCallSessionCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                stopGroupCall(data.readInt(), data.readLong());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                long _arg1 = data.readLong();
                ClassLoader cl = getClass().getClassLoader();
                updateGroupCall(_arg0, _arg1, data.readArrayList(cl), data.readArrayList(cl));
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                long _arg12 = data.readLong();
                ClassLoader cl2 = getClass().getClassLoader();
                int _result2 = startGroupCall(_arg02, _arg12, data.readArrayList(cl2), data.readArrayList(cl2), IGroupCallCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                dispose(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMbmsGroupCallService {
            public static IMbmsGroupCallService sDefaultImpl;
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

            @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
            public int initialize(IMbmsGroupCallSessionCallback callback, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().initialize(callback, subId);
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

            @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
            public void stopGroupCall(int subId, long tmgi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeLong(tmgi);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopGroupCall(subId, tmgi);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
            public void updateGroupCall(int subscriptionId, long tmgi, List saiList, List frequencyList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    _data.writeLong(tmgi);
                    _data.writeList(saiList);
                    _data.writeList(frequencyList);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateGroupCall(subscriptionId, tmgi, saiList, frequencyList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
            public int startGroupCall(int subscriptionId, long tmgi, List saiList, List frequencyList, IGroupCallCallback callback) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subscriptionId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(tmgi);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeList(saiList);
                        try {
                            _data.writeList(frequencyList);
                            _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int startGroupCall = Stub.getDefaultImpl().startGroupCall(subscriptionId, tmgi, saiList, frequencyList, callback);
                            _reply.recycle();
                            _data.recycle();
                            return startGroupCall;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // android.telephony.mbms.vendor.IMbmsGroupCallService
            public void dispose(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dispose(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMbmsGroupCallService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMbmsGroupCallService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
