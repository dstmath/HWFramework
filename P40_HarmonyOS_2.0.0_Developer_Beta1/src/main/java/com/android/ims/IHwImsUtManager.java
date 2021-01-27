package com.android.ims;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwImsUtManager extends IInterface {
    String getUtIMPUFromNetwork(int i) throws RemoteException;

    boolean isSupportCFT(int i) throws RemoteException;

    boolean isUtEnable(int i) throws RemoteException;

    void processECT(int i) throws RemoteException;

    int queryCallForwardForServiceClass(int i, int i2, String str, int i3) throws RemoteException;

    int updateCallBarringOption(int i, String str, int i2, boolean z, int i3, String[] strArr) throws RemoteException;

    int updateCallForwardUncondTimer(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) throws RemoteException;

    public static class Default implements IHwImsUtManager {
        @Override // com.android.ims.IHwImsUtManager
        public boolean isSupportCFT(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.android.ims.IHwImsUtManager
        public boolean isUtEnable(int phoneId) throws RemoteException {
            return false;
        }

        @Override // com.android.ims.IHwImsUtManager
        public int updateCallForwardUncondTimer(int phoneId, int starthour, int startminute, int endhour, int endminute, int action, int condition, String number) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.IHwImsUtManager
        public int updateCallBarringOption(int phoneId, String password, int cbType, boolean enable, int serviceClass, String[] barrList) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.IHwImsUtManager
        public int queryCallForwardForServiceClass(int phoneId, int condition, String number, int serviceClass) throws RemoteException {
            return 0;
        }

        @Override // com.android.ims.IHwImsUtManager
        public void processECT(int phoneId) throws RemoteException {
        }

        @Override // com.android.ims.IHwImsUtManager
        public String getUtIMPUFromNetwork(int phoneId) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwImsUtManager {
        private static final String DESCRIPTOR = "com.android.ims.IHwImsUtManager";
        static final int TRANSACTION_getUtIMPUFromNetwork = 7;
        static final int TRANSACTION_isSupportCFT = 1;
        static final int TRANSACTION_isUtEnable = 2;
        static final int TRANSACTION_processECT = 6;
        static final int TRANSACTION_queryCallForwardForServiceClass = 5;
        static final int TRANSACTION_updateCallBarringOption = 4;
        static final int TRANSACTION_updateCallForwardUncondTimer = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwImsUtManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwImsUtManager)) {
                return new Proxy(obj);
            }
            return (IHwImsUtManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSupportCFT = isSupportCFT(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSupportCFT ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUtEnable = isUtEnable(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUtEnable ? 1 : 0);
                        return true;
                    case TRANSACTION_updateCallForwardUncondTimer /* 3 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = updateCallForwardUncondTimer(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case TRANSACTION_updateCallBarringOption /* 4 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = updateCallBarringOption(data.readInt(), data.readString(), data.readInt(), data.readInt() != 0, data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case TRANSACTION_queryCallForwardForServiceClass /* 5 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = queryCallForwardForServiceClass(data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case TRANSACTION_processECT /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        processECT(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getUtIMPUFromNetwork /* 7 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getUtIMPUFromNetwork(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result4);
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
        public static class Proxy implements IHwImsUtManager {
            public static IHwImsUtManager sDefaultImpl;
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

            @Override // com.android.ims.IHwImsUtManager
            public boolean isSupportCFT(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSupportCFT(phoneId);
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

            @Override // com.android.ims.IHwImsUtManager
            public boolean isUtEnable(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUtEnable(phoneId);
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

            @Override // com.android.ims.IHwImsUtManager
            public int updateCallForwardUncondTimer(int phoneId, int starthour, int startminute, int endhour, int endminute, int action, int condition, String number) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(phoneId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(starthour);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(startminute);
                        try {
                            _data.writeInt(endhour);
                            _data.writeInt(endminute);
                            _data.writeInt(action);
                            _data.writeInt(condition);
                            _data.writeString(number);
                            if (this.mRemote.transact(Stub.TRANSACTION_updateCallForwardUncondTimer, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int updateCallForwardUncondTimer = Stub.getDefaultImpl().updateCallForwardUncondTimer(phoneId, starthour, startminute, endhour, endminute, action, condition, number);
                            _reply.recycle();
                            _data.recycle();
                            return updateCallForwardUncondTimer;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // com.android.ims.IHwImsUtManager
            public int updateCallBarringOption(int phoneId, String password, int cbType, boolean enable, int serviceClass, String[] barrList) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(phoneId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(password);
                        try {
                            _data.writeInt(cbType);
                            _data.writeInt(enable ? 1 : 0);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(serviceClass);
                            try {
                                _data.writeStringArray(barrList);
                            } catch (Throwable th4) {
                                th = th4;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
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
                    try {
                        if (this.mRemote.transact(Stub.TRANSACTION_updateCallBarringOption, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int updateCallBarringOption = Stub.getDefaultImpl().updateCallBarringOption(phoneId, password, cbType, enable, serviceClass, barrList);
                        _reply.recycle();
                        _data.recycle();
                        return updateCallBarringOption;
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.ims.IHwImsUtManager
            public int queryCallForwardForServiceClass(int phoneId, int condition, String number, int serviceClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(condition);
                    _data.writeString(number);
                    _data.writeInt(serviceClass);
                    if (!this.mRemote.transact(Stub.TRANSACTION_queryCallForwardForServiceClass, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryCallForwardForServiceClass(phoneId, condition, number, serviceClass);
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

            @Override // com.android.ims.IHwImsUtManager
            public void processECT(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(Stub.TRANSACTION_processECT, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().processECT(phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.ims.IHwImsUtManager
            public String getUtIMPUFromNetwork(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getUtIMPUFromNetwork, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUtIMPUFromNetwork(phoneId);
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
        }

        public static boolean setDefaultImpl(IHwImsUtManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwImsUtManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
