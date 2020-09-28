package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IHwSecurityDiagnoseCallback;

public interface IHwSecurityDiagnosePlugin extends IInterface {
    boolean componentValid(String str) throws RemoteException;

    void getRootStatus(IHwSecurityDiagnoseCallback iHwSecurityDiagnoseCallback) throws RemoteException;

    int getRootStatusSync() throws RemoteException;

    int getStpStatusByCategory(int i, boolean z, boolean z2, char[] cArr, int[] iArr) throws RemoteException;

    int getSystemStatus() throws RemoteException;

    int getSystemStatusSync() throws RemoteException;

    int report(int i, Bundle bundle) throws RemoteException;

    void sendComponentInfo(Bundle bundle) throws RemoteException;

    int sendThreatenInfo(int i, byte b, byte b2, byte b3, String str, String str2) throws RemoteException;

    int startKernelDetection(int i) throws RemoteException;

    int stopKernelDetection(int i) throws RemoteException;

    int updateKernelDetectionConfig(int[] iArr) throws RemoteException;

    public static class Default implements IHwSecurityDiagnosePlugin {
        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int report(int reporterId, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int getSystemStatus() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public void sendComponentInfo(Bundle data) throws RemoteException {
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public boolean componentValid(String componentName) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public void getRootStatus(IHwSecurityDiagnoseCallback callback) throws RemoteException {
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int getRootStatusSync() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int getSystemStatusSync() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String addition_info) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int getStpStatusByCategory(int category, boolean inDetail, boolean withHistory, char[] outBuff, int[] outBuffLen) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int stopKernelDetection(int uid) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int updateKernelDetectionConfig(int[] conf) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwSecurityDiagnosePlugin
        public int startKernelDetection(int uid) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwSecurityDiagnosePlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwSecurityDiagnosePlugin";
        static final int TRANSACTION_componentValid = 4;
        static final int TRANSACTION_getRootStatus = 5;
        static final int TRANSACTION_getRootStatusSync = 6;
        static final int TRANSACTION_getStpStatusByCategory = 9;
        static final int TRANSACTION_getSystemStatus = 2;
        static final int TRANSACTION_getSystemStatusSync = 7;
        static final int TRANSACTION_report = 1;
        static final int TRANSACTION_sendComponentInfo = 3;
        static final int TRANSACTION_sendThreatenInfo = 8;
        static final int TRANSACTION_startKernelDetection = 12;
        static final int TRANSACTION_stopKernelDetection = 10;
        static final int TRANSACTION_updateKernelDetectionConfig = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwSecurityDiagnosePlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSecurityDiagnosePlugin)) {
                return new Proxy(obj);
            }
            return (IHwSecurityDiagnosePlugin) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            Bundle _arg0;
            char[] _arg3;
            int[] _arg4;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result = report(_arg02, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getSystemStatus();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        sendComponentInfo(_arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean componentValid = componentValid(data.readString());
                        reply.writeNoException();
                        reply.writeInt(componentValid ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        getRootStatus(IHwSecurityDiagnoseCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getRootStatusSync();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSystemStatusSync();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = sendThreatenInfo(data.readInt(), data.readByte(), data.readByte(), data.readByte(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        boolean _arg12 = data.readInt() != 0;
                        boolean _arg2 = data.readInt() != 0;
                        int _arg3_length = data.readInt();
                        if (_arg3_length < 0) {
                            _arg3 = null;
                        } else {
                            _arg3 = new char[_arg3_length];
                        }
                        int _arg4_length = data.readInt();
                        if (_arg4_length < 0) {
                            _arg4 = null;
                        } else {
                            _arg4 = new int[_arg4_length];
                        }
                        int _result6 = getStpStatusByCategory(_arg03, _arg12, _arg2, _arg3, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        reply.writeCharArray(_arg3);
                        reply.writeIntArray(_arg4);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = stopKernelDetection(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = updateKernelDetectionConfig(data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = startKernelDetection(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
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
        public static class Proxy implements IHwSecurityDiagnosePlugin {
            public static IHwSecurityDiagnosePlugin sDefaultImpl;
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int report(int reporterId, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reporterId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().report(reporterId, data);
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int getSystemStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemStatus();
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public void sendComponentInfo(Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendComponentInfo(data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public boolean componentValid(String componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(componentName);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().componentValid(componentName);
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public void getRootStatus(IHwSecurityDiagnoseCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getRootStatus(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int getRootStatusSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRootStatusSync();
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int getSystemStatusSync() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSystemStatusSync();
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String addition_info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(id);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByte(status);
                        try {
                            _data.writeByte(credible);
                            try {
                                _data.writeByte(version);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(name);
                        try {
                            _data.writeString(addition_info);
                            if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sendThreatenInfo = Stub.getDefaultImpl().sendThreatenInfo(id, status, credible, version, name, addition_info);
                            _reply.recycle();
                            _data.recycle();
                            return sendThreatenInfo;
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int getStpStatusByCategory(int category, boolean inDetail, boolean withHistory, char[] outBuff, int[] outBuffLen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(category);
                    int i = 1;
                    _data.writeInt(inDetail ? 1 : 0);
                    if (!withHistory) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (outBuff == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(outBuff.length);
                    }
                    if (outBuffLen == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(outBuffLen.length);
                    }
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStpStatusByCategory(category, inDetail, withHistory, outBuff, outBuffLen);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readCharArray(outBuff);
                    _reply.readIntArray(outBuffLen);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int stopKernelDetection(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopKernelDetection(uid);
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int updateKernelDetectionConfig(int[] conf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(conf);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateKernelDetectionConfig(conf);
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

            @Override // huawei.android.security.IHwSecurityDiagnosePlugin
            public int startKernelDetection(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startKernelDetection(uid);
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
        }

        public static boolean setDefaultImpl(IHwSecurityDiagnosePlugin impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSecurityDiagnosePlugin getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
