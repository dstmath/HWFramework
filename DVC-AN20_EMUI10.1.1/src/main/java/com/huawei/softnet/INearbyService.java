package com.huawei.softnet;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.softnet.connect.DataPayload;
import com.huawei.softnet.nearby.NearAdvertiseOption;
import com.huawei.softnet.nearby.NearConnectOption;
import com.huawei.softnet.nearby.NearConnectionCallback;
import com.huawei.softnet.nearby.NearDataCallback;
import com.huawei.softnet.nearby.NearDevConfig;
import com.huawei.softnet.nearby.NearDiscoveryCallback;
import com.huawei.softnet.nearby.NearListenOption;
import com.huawei.softnet.nearby.NearServiceDesc;
import com.huawei.softnet.nearby.NearServiceFilter;
import java.util.List;

public interface INearbyService extends IInterface {
    int acceptConnect(String str, String str2, NearConnectOption nearConnectOption, NearDataCallback nearDataCallback) throws RemoteException;

    int connect(String str, String str2, NearConnectOption nearConnectOption, NearConnectionCallback nearConnectionCallback) throws RemoteException;

    int destroy(String str) throws RemoteException;

    int disconnect(String str, String str2) throws RemoteException;

    int disconnectAll(String str) throws RemoteException;

    int publish(String str, NearAdvertiseOption nearAdvertiseOption, NearConnectionCallback nearConnectionCallback) throws RemoteException;

    int rejectConnect(String str, String str2) throws RemoteException;

    int sendData(String str, String str2, DataPayload dataPayload) throws RemoteException;

    int setConfig(String str, NearDevConfig nearDevConfig) throws RemoteException;

    int startAdvertising(String str, List<NearServiceDesc> list, NearListenOption nearListenOption, NearConnectionCallback nearConnectionCallback) throws RemoteException;

    int startDiscovery(String str, NearAdvertiseOption nearAdvertiseOption, List<NearServiceFilter> list, NearDiscoveryCallback nearDiscoveryCallback) throws RemoteException;

    int stopAdvertising(String str) throws RemoteException;

    int stopDiscovery(String str) throws RemoteException;

    int subscribe(String str, NearListenOption nearListenOption, NearDiscoveryCallback nearDiscoveryCallback) throws RemoteException;

    int unpublish(String str) throws RemoteException;

    int unsubscribe(String str) throws RemoteException;

    public static class Default implements INearbyService {
        @Override // com.huawei.softnet.INearbyService
        public int startDiscovery(String moduleId, NearAdvertiseOption option, List<NearServiceFilter> list, NearDiscoveryCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int stopDiscovery(String moduleId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int startAdvertising(String moduleId, List<NearServiceDesc> list, NearListenOption option, NearConnectionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int stopAdvertising(String moduleId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int publish(String moduleId, NearAdvertiseOption option, NearConnectionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int unpublish(String moduleId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int subscribe(String moduleId, NearListenOption option, NearDiscoveryCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int unsubscribe(String moduleId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int setConfig(String moduleId, NearDevConfig config) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int connect(String moduleId, String deviceId, NearConnectOption connectOption, NearConnectionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int disconnect(String moduleId, String deviceId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int acceptConnect(String moduleId, String deviceId, NearConnectOption connectOption, NearDataCallback callback) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int rejectConnect(String moduleId, String deviceId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int sendData(String moduleId, String deviceId, DataPayload data) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int disconnectAll(String moduleId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.INearbyService
        public int destroy(String moduleId) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INearbyService {
        private static final String DESCRIPTOR = "com.huawei.softnet.INearbyService";
        static final int TRANSACTION_acceptConnect = 12;
        static final int TRANSACTION_connect = 10;
        static final int TRANSACTION_destroy = 16;
        static final int TRANSACTION_disconnect = 11;
        static final int TRANSACTION_disconnectAll = 15;
        static final int TRANSACTION_publish = 5;
        static final int TRANSACTION_rejectConnect = 13;
        static final int TRANSACTION_sendData = 14;
        static final int TRANSACTION_setConfig = 9;
        static final int TRANSACTION_startAdvertising = 3;
        static final int TRANSACTION_startDiscovery = 1;
        static final int TRANSACTION_stopAdvertising = 4;
        static final int TRANSACTION_stopDiscovery = 2;
        static final int TRANSACTION_subscribe = 7;
        static final int TRANSACTION_unpublish = 6;
        static final int TRANSACTION_unsubscribe = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INearbyService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INearbyService)) {
                return new Proxy(obj);
            }
            return (INearbyService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NearAdvertiseOption _arg1;
            NearListenOption _arg2;
            NearAdvertiseOption _arg12;
            NearListenOption _arg13;
            NearDevConfig _arg14;
            NearConnectOption _arg22;
            NearConnectOption _arg23;
            DataPayload _arg24;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = NearAdvertiseOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result = startDiscovery(_arg0, _arg1, data.createTypedArrayList(NearServiceFilter.CREATOR), NearDiscoveryCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = stopDiscovery(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        List<NearServiceDesc> _arg15 = data.createTypedArrayList(NearServiceDesc.CREATOR);
                        if (data.readInt() != 0) {
                            _arg2 = NearListenOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result3 = startAdvertising(_arg02, _arg15, _arg2, NearConnectionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = stopAdvertising(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = NearAdvertiseOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        int _result5 = publish(_arg03, _arg12, NearConnectionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = unpublish(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = NearListenOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        int _result7 = subscribe(_arg04, _arg13, NearDiscoveryCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = unsubscribe(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = NearDevConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        int _result9 = setConfig(_arg05, _arg14);
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = NearConnectOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _result10 = connect(_arg06, _arg16, _arg22, NearConnectionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = disconnect(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = NearConnectOption.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        int _result12 = acceptConnect(_arg07, _arg17, _arg23, NearDataCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = rejectConnect(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg08 = data.readString();
                        String _arg18 = data.readString();
                        if (data.readInt() != 0) {
                            _arg24 = DataPayload.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        int _result14 = sendData(_arg08, _arg18, _arg24);
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = disconnectAll(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result16 = destroy(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result16);
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
        public static class Proxy implements INearbyService {
            public static INearbyService sDefaultImpl;
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

            @Override // com.huawei.softnet.INearbyService
            public int startDiscovery(String moduleId, NearAdvertiseOption option, List<NearServiceFilter> condition, NearDiscoveryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (option != null) {
                        _data.writeInt(1);
                        option.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(condition);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDiscovery(moduleId, option, condition, callback);
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

            @Override // com.huawei.softnet.INearbyService
            public int stopDiscovery(String moduleId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDiscovery(moduleId);
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

            @Override // com.huawei.softnet.INearbyService
            public int startAdvertising(String moduleId, List<NearServiceDesc> response, NearListenOption option, NearConnectionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    _data.writeTypedList(response);
                    if (option != null) {
                        _data.writeInt(1);
                        option.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startAdvertising(moduleId, response, option, callback);
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

            @Override // com.huawei.softnet.INearbyService
            public int stopAdvertising(String moduleId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopAdvertising(moduleId);
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

            @Override // com.huawei.softnet.INearbyService
            public int publish(String moduleId, NearAdvertiseOption option, NearConnectionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (option != null) {
                        _data.writeInt(1);
                        option.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().publish(moduleId, option, callback);
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

            @Override // com.huawei.softnet.INearbyService
            public int unpublish(String moduleId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unpublish(moduleId);
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

            @Override // com.huawei.softnet.INearbyService
            public int subscribe(String moduleId, NearListenOption option, NearDiscoveryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (option != null) {
                        _data.writeInt(1);
                        option.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().subscribe(moduleId, option, callback);
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

            @Override // com.huawei.softnet.INearbyService
            public int unsubscribe(String moduleId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unsubscribe(moduleId);
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

            @Override // com.huawei.softnet.INearbyService
            public int setConfig(String moduleId, NearDevConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setConfig(moduleId, config);
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

            @Override // com.huawei.softnet.INearbyService
            public int connect(String moduleId, String deviceId, NearConnectOption connectOption, NearConnectionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    _data.writeString(deviceId);
                    if (connectOption != null) {
                        _data.writeInt(1);
                        connectOption.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connect(moduleId, deviceId, connectOption, callback);
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

            @Override // com.huawei.softnet.INearbyService
            public int disconnect(String moduleId, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    _data.writeString(deviceId);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnect(moduleId, deviceId);
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

            @Override // com.huawei.softnet.INearbyService
            public int acceptConnect(String moduleId, String deviceId, NearConnectOption connectOption, NearDataCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    _data.writeString(deviceId);
                    if (connectOption != null) {
                        _data.writeInt(1);
                        connectOption.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acceptConnect(moduleId, deviceId, connectOption, callback);
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

            @Override // com.huawei.softnet.INearbyService
            public int rejectConnect(String moduleId, String deviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    _data.writeString(deviceId);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().rejectConnect(moduleId, deviceId);
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

            @Override // com.huawei.softnet.INearbyService
            public int sendData(String moduleId, String deviceId, DataPayload data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    _data.writeString(deviceId);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendData(moduleId, deviceId, data);
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

            @Override // com.huawei.softnet.INearbyService
            public int disconnectAll(String moduleId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disconnectAll(moduleId);
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

            @Override // com.huawei.softnet.INearbyService
            public int destroy(String moduleId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(moduleId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().destroy(moduleId);
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

        public static boolean setDefaultImpl(INearbyService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INearbyService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
