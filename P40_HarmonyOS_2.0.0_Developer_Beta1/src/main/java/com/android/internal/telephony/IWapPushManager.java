package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWapPushManager extends IInterface {
    @UnsupportedAppUsage
    boolean addPackage(String str, String str2, String str3, String str4, int i, boolean z, boolean z2) throws RemoteException;

    @UnsupportedAppUsage
    boolean deletePackage(String str, String str2, String str3, String str4) throws RemoteException;

    int processMessage(String str, String str2, Intent intent) throws RemoteException;

    @UnsupportedAppUsage
    boolean updatePackage(String str, String str2, String str3, String str4, int i, boolean z, boolean z2) throws RemoteException;

    public static class Default implements IWapPushManager {
        @Override // com.android.internal.telephony.IWapPushManager
        public int processMessage(String app_id, String content_type, Intent intent) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IWapPushManager
        public boolean addPackage(String x_app_id, String content_type, String package_name, String class_name, int app_type, boolean need_signature, boolean further_processing) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IWapPushManager
        public boolean updatePackage(String x_app_id, String content_type, String package_name, String class_name, int app_type, boolean need_signature, boolean further_processing) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IWapPushManager
        public boolean deletePackage(String x_app_id, String content_type, String package_name, String class_name) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWapPushManager {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IWapPushManager";
        static final int TRANSACTION_addPackage = 2;
        static final int TRANSACTION_deletePackage = 4;
        static final int TRANSACTION_processMessage = 1;
        static final int TRANSACTION_updatePackage = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWapPushManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWapPushManager)) {
                return new Proxy(obj);
            }
            return (IWapPushManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "processMessage";
            }
            if (transactionCode == 2) {
                return "addPackage";
            }
            if (transactionCode == 3) {
                return "updatePackage";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "deletePackage";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                String _arg1 = data.readString();
                if (data.readInt() != 0) {
                    _arg2 = Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                int _result = processMessage(_arg0, _arg1, _arg2);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean addPackage = addPackage(data.readString(), data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt() != 0);
                reply.writeNoException();
                reply.writeInt(addPackage ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean updatePackage = updatePackage(data.readString(), data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt() != 0);
                reply.writeNoException();
                reply.writeInt(updatePackage ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                boolean deletePackage = deletePackage(data.readString(), data.readString(), data.readString(), data.readString());
                reply.writeNoException();
                reply.writeInt(deletePackage ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IWapPushManager {
            public static IWapPushManager sDefaultImpl;
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

            @Override // com.android.internal.telephony.IWapPushManager
            public int processMessage(String app_id, String content_type, Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(app_id);
                    _data.writeString(content_type);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().processMessage(app_id, content_type, intent);
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

            @Override // com.android.internal.telephony.IWapPushManager
            public boolean addPackage(String x_app_id, String content_type, String package_name, String class_name, int app_type, boolean need_signature, boolean further_processing) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(x_app_id);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(content_type);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(package_name);
                        try {
                            _data.writeString(class_name);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(app_type);
                            boolean _result = true;
                            _data.writeInt(need_signature ? 1 : 0);
                            _data.writeInt(further_processing ? 1 : 0);
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean addPackage = Stub.getDefaultImpl().addPackage(x_app_id, content_type, package_name, class_name, app_type, need_signature, further_processing);
                            _reply.recycle();
                            _data.recycle();
                            return addPackage;
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

            @Override // com.android.internal.telephony.IWapPushManager
            public boolean updatePackage(String x_app_id, String content_type, String package_name, String class_name, int app_type, boolean need_signature, boolean further_processing) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(x_app_id);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(content_type);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(package_name);
                        try {
                            _data.writeString(class_name);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(app_type);
                            boolean _result = true;
                            _data.writeInt(need_signature ? 1 : 0);
                            _data.writeInt(further_processing ? 1 : 0);
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updatePackage = Stub.getDefaultImpl().updatePackage(x_app_id, content_type, package_name, class_name, app_type, need_signature, further_processing);
                            _reply.recycle();
                            _data.recycle();
                            return updatePackage;
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

            @Override // com.android.internal.telephony.IWapPushManager
            public boolean deletePackage(String x_app_id, String content_type, String package_name, String class_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(x_app_id);
                    _data.writeString(content_type);
                    _data.writeString(package_name);
                    _data.writeString(class_name);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deletePackage(x_app_id, content_type, package_name, class_name);
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

        public static boolean setDefaultImpl(IWapPushManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWapPushManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
