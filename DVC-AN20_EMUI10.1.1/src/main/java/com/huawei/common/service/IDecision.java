package com.huawei.common.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.common.service.IDecisionCallback;
import java.util.List;
import java.util.Map;

public interface IDecision extends IInterface {
    int batchLog(String str, List list, List list2) throws RemoteException;

    void executeEvent(Map map, IDecisionCallback iDecisionCallback) throws RemoteException;

    int infer(String str, String str2, Map map) throws RemoteException;

    int insertBusinessData(Map map) throws RemoteException;

    int log(String str, String str2, int i) throws RemoteException;

    int register(String str, int i, Map map, Map map2) throws RemoteException;

    int removeBusinessData(String str, String str2) throws RemoteException;

    int unRegister(String str) throws RemoteException;

    int update(String str, String str2) throws RemoteException;

    public static class Default implements IDecision {
        @Override // com.huawei.common.service.IDecision
        public void executeEvent(Map fact, IDecisionCallback callback) throws RemoteException {
        }

        @Override // com.huawei.common.service.IDecision
        public int insertBusinessData(Map fact) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.common.service.IDecision
        public int removeBusinessData(String category, String id) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.common.service.IDecision
        public int register(String sceneName, int algorithm, Map featureSchema, Map extend) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.common.service.IDecision
        public int unRegister(String sceneName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.common.service.IDecision
        public int update(String sceneName, String updateContent) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.common.service.IDecision
        public int log(String sceneName, String feature, int label) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.common.service.IDecision
        public int batchLog(String sceneName, List features, List labels) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.common.service.IDecision
        public int infer(String sceneName, String feature, Map result) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDecision {
        private static final String DESCRIPTOR = "com.huawei.common.service.IDecision";
        static final int TRANSACTION_batchLog = 8;
        static final int TRANSACTION_executeEvent = 1;
        static final int TRANSACTION_infer = 9;
        static final int TRANSACTION_insertBusinessData = 2;
        static final int TRANSACTION_log = 7;
        static final int TRANSACTION_register = 4;
        static final int TRANSACTION_removeBusinessData = 3;
        static final int TRANSACTION_unRegister = 5;
        static final int TRANSACTION_update = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDecision asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDecision)) {
                return new Proxy(obj);
            }
            return (IDecision) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        executeEvent(data.readHashMap(getClass().getClassLoader()), IDecisionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = insertBusinessData(data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = removeBusinessData(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        int _arg1 = data.readInt();
                        ClassLoader cl = getClass().getClassLoader();
                        int _result3 = register(_arg0, _arg1, data.readHashMap(cl), data.readHashMap(cl));
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = unRegister(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = update(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = log(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        ClassLoader cl2 = getClass().getClassLoader();
                        int _result7 = batchLog(_arg02, data.readArrayList(cl2), data.readArrayList(cl2));
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        String _arg12 = data.readString();
                        Map _arg2 = data.readHashMap(getClass().getClassLoader());
                        int _result8 = infer(_arg03, _arg12, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        reply.writeMap(_arg2);
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
        public static class Proxy implements IDecision {
            public static IDecision sDefaultImpl;
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

            @Override // com.huawei.common.service.IDecision
            public void executeEvent(Map fact, IDecisionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(fact);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().executeEvent(fact, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.common.service.IDecision
            public int insertBusinessData(Map fact) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(fact);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().insertBusinessData(fact);
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

            @Override // com.huawei.common.service.IDecision
            public int removeBusinessData(String category, String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(category);
                    _data.writeString(id);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeBusinessData(category, id);
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

            @Override // com.huawei.common.service.IDecision
            public int register(String sceneName, int algorithm, Map featureSchema, Map extend) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sceneName);
                    _data.writeInt(algorithm);
                    _data.writeMap(featureSchema);
                    _data.writeMap(extend);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().register(sceneName, algorithm, featureSchema, extend);
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

            @Override // com.huawei.common.service.IDecision
            public int unRegister(String sceneName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sceneName);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegister(sceneName);
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

            @Override // com.huawei.common.service.IDecision
            public int update(String sceneName, String updateContent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sceneName);
                    _data.writeString(updateContent);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().update(sceneName, updateContent);
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

            @Override // com.huawei.common.service.IDecision
            public int log(String sceneName, String feature, int label) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sceneName);
                    _data.writeString(feature);
                    _data.writeInt(label);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().log(sceneName, feature, label);
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

            @Override // com.huawei.common.service.IDecision
            public int batchLog(String sceneName, List features, List labels) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sceneName);
                    _data.writeList(features);
                    _data.writeList(labels);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().batchLog(sceneName, features, labels);
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

            @Override // com.huawei.common.service.IDecision
            public int infer(String sceneName, String feature, Map result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sceneName);
                    _data.writeString(feature);
                    _data.writeMap(result);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().infer(sceneName, feature, result);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDecision impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDecision getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
