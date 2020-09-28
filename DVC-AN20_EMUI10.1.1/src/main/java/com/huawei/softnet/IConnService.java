package com.huawei.softnet;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.softnet.connect.DataPayload;
import com.huawei.softnet.connect.IConnectOption;
import com.huawei.softnet.connect.IConnectionCallback;
import com.huawei.softnet.connect.IDataCallback;
import com.huawei.softnet.connect.IDevConfig;
import com.huawei.softnet.connect.IDiscoveryCallback;
import com.huawei.softnet.connect.IDiscoveryOption;
import com.huawei.softnet.connect.IPublishOption;

public interface IConnService extends IInterface {
    int acceptConnect(String str, String str2, String str3, IDataCallback iDataCallback) throws RemoteException;

    int connect(String str, String str2, String str3, IConnectOption iConnectOption, IConnectionCallback iConnectionCallback) throws RemoteException;

    int destroy(String str) throws RemoteException;

    int disconnect(String str, String str2, String str3) throws RemoteException;

    int disconnectAll(String str) throws RemoteException;

    int publishService(String str, IPublishOption iPublishOption, IConnectionCallback iConnectionCallback) throws RemoteException;

    int rejectConnect(String str, String str2, String str3) throws RemoteException;

    int sendBlock(String str, String str2, String str3, byte[] bArr, int i, String str4) throws RemoteException;

    int sendByte(String str, String str2, String str3, byte[] bArr, int i, String str4) throws RemoteException;

    int sendFile(String str, String str2, String str3, String str4, String str5, String str6) throws RemoteException;

    int sendStream(String str, String str2, String str3, DataPayload dataPayload, String str4) throws RemoteException;

    int setConfig(String str, IDevConfig iDevConfig) throws RemoteException;

    int startDiscovery(String str, IDiscoveryOption iDiscoveryOption, IDiscoveryCallback iDiscoveryCallback) throws RemoteException;

    int stopDiscovery(String str, int i) throws RemoteException;

    int unPublishService(String str, int i) throws RemoteException;

    public static class Default implements IConnService {
        @Override // com.huawei.softnet.IConnService
        public int startDiscovery(String moduleName, IDiscoveryOption option, IDiscoveryCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int stopDiscovery(String moduleName, int discoveryMode) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int publishService(String moduleName, IPublishOption option, IConnectionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int unPublishService(String moduleName, int publishMode) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int connect(String localModule, String remoteDeviceId, String remoteModule, IConnectOption option, IConnectionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int disconnect(String localModule, String remoteDeviceId, String remoteModule) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int acceptConnect(String localModule, String remoteDeviceId, String remoteModule, IDataCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int rejectConnect(String localModule, String remoteDeviceId, String remoteModule) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int sendByte(String localModule, String remoteDeviceId, String remoteModule, byte[] data, int len, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int sendBlock(String localModule, String remoteDeviceId, String remoteModule, byte[] data, int len, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int sendFile(String localModule, String remoteDeviceId, String remoteModule, String sourceFile, String destFilePath, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int sendStream(String localModule, String remoteDeviceId, String remoteModule, DataPayload streamData, String extInfo) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int setConfig(String moduleName, IDevConfig config) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int disconnectAll(String moduleName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.IConnService
        public int destroy(String moduleName) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConnService {
        private static final String DESCRIPTOR = "com.huawei.softnet.IConnService";
        static final int TRANSACTION_acceptConnect = 7;
        static final int TRANSACTION_connect = 5;
        static final int TRANSACTION_destroy = 15;
        static final int TRANSACTION_disconnect = 6;
        static final int TRANSACTION_disconnectAll = 14;
        static final int TRANSACTION_publishService = 3;
        static final int TRANSACTION_rejectConnect = 8;
        static final int TRANSACTION_sendBlock = 10;
        static final int TRANSACTION_sendByte = 9;
        static final int TRANSACTION_sendFile = 11;
        static final int TRANSACTION_sendStream = 12;
        static final int TRANSACTION_setConfig = 13;
        static final int TRANSACTION_startDiscovery = 1;
        static final int TRANSACTION_stopDiscovery = 2;
        static final int TRANSACTION_unPublishService = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnService)) {
                return new Proxy(obj);
            }
            return (IConnService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IDiscoveryOption _arg1;
            IPublishOption _arg12;
            IConnectOption _arg3;
            DataPayload _arg32;
            IDevConfig _arg13;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = IDiscoveryOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result = startDiscovery(_arg0, _arg1, IDiscoveryCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = stopDiscovery(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = IPublishOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result3 = publishService(_arg02, _arg12, IConnectionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = unPublishService(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        String _arg14 = data.readString();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = IConnectOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result5 = connect(_arg03, _arg14, _arg2, _arg3, IConnectionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = disconnect(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = acceptConnect(data.readString(), data.readString(), data.readString(), IDataCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = rejectConnect(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = sendByte(data.readString(), data.readString(), data.readString(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = sendBlock(data.readString(), data.readString(), data.readString(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = sendFile(data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        String _arg15 = data.readString();
                        String _arg22 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = DataPayload.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        int _result12 = sendStream(_arg04, _arg15, _arg22, _arg32, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = IDevConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        int _result13 = setConfig(_arg05, _arg13);
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = disconnectAll(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = destroy(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
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
        public static class Proxy implements IConnService {
            public static IConnService sDefaultImpl;
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

            @Override // com.huawei.softnet.IConnService
            public int startDiscovery(String moduleName, IDiscoveryOption option, IDiscoveryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (option != null) {
                        _data.writeInt(1);
                        option.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDiscovery(moduleName, option, callback);
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

            @Override // com.huawei.softnet.IConnService
            public int stopDiscovery(String moduleName, int discoveryMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeInt(discoveryMode);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDiscovery(moduleName, discoveryMode);
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

            @Override // com.huawei.softnet.IConnService
            public int publishService(String moduleName, IPublishOption option, IConnectionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (option != null) {
                        _data.writeInt(1);
                        option.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().publishService(moduleName, option, callback);
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

            @Override // com.huawei.softnet.IConnService
            public int unPublishService(String moduleName, int publishMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    _data.writeInt(publishMode);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unPublishService(moduleName, publishMode);
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

            @Override // com.huawei.softnet.IConnService
            public int connect(String localModule, String remoteDeviceId, String remoteModule, IConnectOption option, IConnectionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localModule);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModule);
                    if (option != null) {
                        _data.writeInt(1);
                        option.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connect(localModule, remoteDeviceId, remoteModule, option, callback);
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

            @Override // com.huawei.softnet.IConnService
            public int disconnect(String localModule, String remoteDeviceId, String remoteModule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localModule);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModule);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnect(localModule, remoteDeviceId, remoteModule);
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

            @Override // com.huawei.softnet.IConnService
            public int acceptConnect(String localModule, String remoteDeviceId, String remoteModule, IDataCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localModule);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModule);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acceptConnect(localModule, remoteDeviceId, remoteModule, callback);
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

            @Override // com.huawei.softnet.IConnService
            public int rejectConnect(String localModule, String remoteDeviceId, String remoteModule) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localModule);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModule);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().rejectConnect(localModule, remoteDeviceId, remoteModule);
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

            @Override // com.huawei.softnet.IConnService
            public int sendByte(String localModule, String remoteDeviceId, String remoteModule, byte[] data, int len, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(localModule);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(remoteDeviceId);
                        try {
                            _data.writeString(remoteModule);
                            try {
                                _data.writeByteArray(data);
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
                        _data.writeInt(len);
                        try {
                            _data.writeString(extInfo);
                            if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sendByte = Stub.getDefaultImpl().sendByte(localModule, remoteDeviceId, remoteModule, data, len, extInfo);
                            _reply.recycle();
                            _data.recycle();
                            return sendByte;
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

            @Override // com.huawei.softnet.IConnService
            public int sendBlock(String localModule, String remoteDeviceId, String remoteModule, byte[] data, int len, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(localModule);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(remoteDeviceId);
                        try {
                            _data.writeString(remoteModule);
                            try {
                                _data.writeByteArray(data);
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
                        _data.writeInt(len);
                        try {
                            _data.writeString(extInfo);
                            if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sendBlock = Stub.getDefaultImpl().sendBlock(localModule, remoteDeviceId, remoteModule, data, len, extInfo);
                            _reply.recycle();
                            _data.recycle();
                            return sendBlock;
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

            @Override // com.huawei.softnet.IConnService
            public int sendFile(String localModule, String remoteDeviceId, String remoteModule, String sourceFile, String destFilePath, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(localModule);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(remoteDeviceId);
                        try {
                            _data.writeString(remoteModule);
                            try {
                                _data.writeString(sourceFile);
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
                        _data.writeString(destFilePath);
                        try {
                            _data.writeString(extInfo);
                            if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int _result = _reply.readInt();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int sendFile = Stub.getDefaultImpl().sendFile(localModule, remoteDeviceId, remoteModule, sourceFile, destFilePath, extInfo);
                            _reply.recycle();
                            _data.recycle();
                            return sendFile;
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

            @Override // com.huawei.softnet.IConnService
            public int sendStream(String localModule, String remoteDeviceId, String remoteModule, DataPayload streamData, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(localModule);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModule);
                    if (streamData != null) {
                        _data.writeInt(1);
                        streamData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(extInfo);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendStream(localModule, remoteDeviceId, remoteModule, streamData, extInfo);
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

            @Override // com.huawei.softnet.IConnService
            public int setConfig(String moduleName, IDevConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setConfig(moduleName, config);
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

            @Override // com.huawei.softnet.IConnService
            public int disconnectAll(String moduleName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnectAll(moduleName);
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

            @Override // com.huawei.softnet.IConnService
            public int destroy(String moduleName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleName);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().destroy(moduleName);
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

        public static boolean setDefaultImpl(IConnService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConnService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
