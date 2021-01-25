package android.app;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInstantAppResolver extends IInterface {
    void getInstantAppIntentFilterList(Intent intent, int[] iArr, int i, String str, IRemoteCallback iRemoteCallback) throws RemoteException;

    void getInstantAppResolveInfoList(Intent intent, int[] iArr, int i, String str, int i2, IRemoteCallback iRemoteCallback) throws RemoteException;

    public static class Default implements IInstantAppResolver {
        @Override // android.app.IInstantAppResolver
        public void getInstantAppResolveInfoList(Intent sanitizedIntent, int[] hostDigestPrefix, int userId, String token, int sequence, IRemoteCallback callback) throws RemoteException {
        }

        @Override // android.app.IInstantAppResolver
        public void getInstantAppIntentFilterList(Intent sanitizedIntent, int[] hostDigestPrefix, int userId, String token, IRemoteCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInstantAppResolver {
        private static final String DESCRIPTOR = "android.app.IInstantAppResolver";
        static final int TRANSACTION_getInstantAppIntentFilterList = 2;
        static final int TRANSACTION_getInstantAppResolveInfoList = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInstantAppResolver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInstantAppResolver)) {
                return new Proxy(obj);
            }
            return (IInstantAppResolver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getInstantAppResolveInfoList";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "getInstantAppIntentFilterList";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg0;
            Intent _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                getInstantAppResolveInfoList(_arg0, data.createIntArray(), data.readInt(), data.readString(), data.readInt(), IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                getInstantAppIntentFilterList(_arg02, data.createIntArray(), data.readInt(), data.readString(), IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IInstantAppResolver {
            public static IInstantAppResolver sDefaultImpl;
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

            @Override // android.app.IInstantAppResolver
            public void getInstantAppResolveInfoList(Intent sanitizedIntent, int[] hostDigestPrefix, int userId, String token, int sequence, IRemoteCallback callback) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sanitizedIntent != null) {
                        _data.writeInt(1);
                        sanitizedIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeIntArray(hostDigestPrefix);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeString(token);
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(sequence);
                            _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().getInstantAppResolveInfoList(sanitizedIntent, hostDigestPrefix, userId, token, sequence, callback);
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IInstantAppResolver
            public void getInstantAppIntentFilterList(Intent sanitizedIntent, int[] hostDigestPrefix, int userId, String token, IRemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sanitizedIntent != null) {
                        _data.writeInt(1);
                        sanitizedIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeIntArray(hostDigestPrefix);
                    _data.writeInt(userId);
                    _data.writeString(token);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getInstantAppIntentFilterList(sanitizedIntent, hostDigestPrefix, userId, token, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInstantAppResolver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInstantAppResolver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
