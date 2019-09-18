package com.huawei.hiai.awareness.service;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.hiai.awareness.service.IRequestCallBack;

public interface IAwarenessService extends IInterface {

    public static abstract class Stub extends Binder implements IAwarenessService {
        private static final String DESCRIPTOR = "com.huawei.hiai.awareness.service.IAwarenessService";
        static final int TRANSACTION_checkServerVersion = 12;
        static final int TRANSACTION_getAwarenessApiVersion = 13;
        static final int TRANSACTION_getCurrentAwareness = 14;
        static final int TRANSACTION_getCurrentStatus = 1;
        static final int TRANSACTION_getExtendFenceTriggerResult = 18;
        static final int TRANSACTION_getFenceTriggerResult = 11;
        static final int TRANSACTION_getSupportAwarenessCapability = 19;
        static final int TRANSACTION_isIntegrateSensorHub = 23;
        static final int TRANSACTION_registerAppLifeChangeFence = 24;
        static final int TRANSACTION_registerAppUseTotalTimeFence = 5;
        static final int TRANSACTION_registerBroadcastEventFence = 16;
        static final int TRANSACTION_registerCustomLocationFence = 15;
        static final int TRANSACTION_registerDeviceStatusFence = 21;
        static final int TRANSACTION_registerDeviceUseTotalTimeFence = 7;
        static final int TRANSACTION_registerLocationFence = 4;
        static final int TRANSACTION_registerMotionFence = 2;
        static final int TRANSACTION_registerMovementFence = 20;
        static final int TRANSACTION_registerOneAppContinuousUseTimeFence = 6;
        static final int TRANSACTION_registerScreenUnlockFence = 9;
        static final int TRANSACTION_registerScreenUnlockTotalNumberFence = 8;
        static final int TRANSACTION_registerTimeFence = 3;
        static final int TRANSACTION_setReportPeriod = 22;
        static final int TRANSACTION_unRegisterExtendFence = 17;
        static final int TRANSACTION_unRegisterFence = 10;

        private static class Proxy implements IAwarenessService {
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

            public RequestResult getCurrentStatus(int type) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerMotionFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent oprationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (oprationPI != null) {
                        _data.writeInt(1);
                        oprationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent oprationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (oprationPI != null) {
                        _data.writeInt(1);
                        oprationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerLocationFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent oprationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (oprationPI != null) {
                        _data.writeInt(1);
                        oprationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerAppUseTotalTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerOneAppContinuousUseTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerDeviceUseTotalTimeFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerScreenUnlockTotalNumberFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerScreenUnlockFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unRegisterFence(IRequestCallBack callback, AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RequestResult getFenceTriggerResult(AwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkServerVersion(int apiVersion) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(apiVersion);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAwarenessApiVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RequestResult getCurrentAwareness(int type, boolean isCustom, Bundle bundle, String callerPackageName) throws RemoteException {
                RequestResult _result;
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!isCustom) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callerPackageName);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerCustomLocationFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent oprationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (oprationPI != null) {
                        _data.writeInt(1);
                        oprationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerBroadcastEventFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unRegisterExtendFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_unRegisterExtendFence, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RequestResult getExtendFenceTriggerResult(ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent operationPI) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operationPI != null) {
                        _data.writeInt(1);
                        operationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getExtendFenceTriggerResult, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RequestResult getSupportAwarenessCapability(int type) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_getSupportAwarenessCapability, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerMovementFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent oprationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (oprationPI != null) {
                        _data.writeInt(1);
                        oprationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registerMovementFence, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerDeviceStatusFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent oprationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (oprationPI != null) {
                        _data.writeInt(1);
                        oprationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registerDeviceStatusFence, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RequestResult setReportPeriod(ExtendAwarenessFence awarenessFence) throws RemoteException {
                RequestResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setReportPeriod, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = RequestResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isIntegrateSensorHub() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isIntegrateSensorHub, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerAppLifeChangeFence(IRequestCallBack callback, ExtendAwarenessFence awarenessFence, Bundle bundle, PendingIntent oprationPI) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (awarenessFence != null) {
                        _data.writeInt(1);
                        awarenessFence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (oprationPI != null) {
                        _data.writeInt(1);
                        oprationPI.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registerAppLifeChangeFence, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAwarenessService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAwarenessService)) {
                return new Proxy(obj);
            }
            return (IAwarenessService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ExtendAwarenessFence _arg1;
            Bundle _arg2;
            PendingIntent _arg3;
            ExtendAwarenessFence _arg0;
            ExtendAwarenessFence _arg12;
            Bundle _arg22;
            PendingIntent _arg32;
            ExtendAwarenessFence _arg13;
            Bundle _arg23;
            PendingIntent _arg33;
            ExtendAwarenessFence _arg02;
            Bundle _arg14;
            PendingIntent _arg24;
            ExtendAwarenessFence _arg15;
            Bundle _arg25;
            PendingIntent _arg34;
            ExtendAwarenessFence _arg16;
            Bundle _arg26;
            PendingIntent _arg35;
            ExtendAwarenessFence _arg17;
            Bundle _arg27;
            PendingIntent _arg36;
            boolean _arg18;
            Bundle _arg28;
            AwarenessFence _arg03;
            Bundle _arg19;
            PendingIntent _arg29;
            AwarenessFence _arg110;
            Bundle _arg210;
            PendingIntent _arg37;
            AwarenessFence _arg111;
            Bundle _arg211;
            PendingIntent _arg38;
            AwarenessFence _arg112;
            Bundle _arg212;
            PendingIntent _arg39;
            AwarenessFence _arg113;
            Bundle _arg213;
            PendingIntent _arg310;
            AwarenessFence _arg114;
            Bundle _arg214;
            PendingIntent _arg311;
            AwarenessFence _arg115;
            Bundle _arg215;
            PendingIntent _arg312;
            AwarenessFence _arg116;
            Bundle _arg216;
            PendingIntent _arg313;
            AwarenessFence _arg117;
            Bundle _arg217;
            PendingIntent _arg314;
            AwarenessFence _arg118;
            Bundle _arg218;
            PendingIntent _arg315;
            int i = 0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    RequestResult _result = getCurrentStatus(data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg04 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg118 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg118 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg218 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg218 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg315 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg315 = null;
                    }
                    boolean _result2 = registerMotionFence(_arg04, _arg118, _arg218, _arg315);
                    reply.writeNoException();
                    if (_result2) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg05 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg117 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg117 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg217 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg217 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg314 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg314 = null;
                    }
                    boolean _result3 = registerTimeFence(_arg05, _arg117, _arg217, _arg314);
                    reply.writeNoException();
                    if (_result3) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg06 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg116 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg116 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg216 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg216 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg313 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg313 = null;
                    }
                    boolean _result4 = registerLocationFence(_arg06, _arg116, _arg216, _arg313);
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg07 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg115 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg115 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg215 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg215 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg312 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg312 = null;
                    }
                    boolean _result5 = registerAppUseTotalTimeFence(_arg07, _arg115, _arg215, _arg312);
                    reply.writeNoException();
                    if (_result5) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg08 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg114 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg114 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg214 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg214 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg311 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg311 = null;
                    }
                    boolean _result6 = registerOneAppContinuousUseTimeFence(_arg08, _arg114, _arg214, _arg311);
                    reply.writeNoException();
                    if (_result6) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg09 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg113 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg113 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg213 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg213 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg310 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg310 = null;
                    }
                    boolean _result7 = registerDeviceUseTotalTimeFence(_arg09, _arg113, _arg213, _arg310);
                    reply.writeNoException();
                    if (_result7) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg010 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg112 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg112 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg212 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg212 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg39 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg39 = null;
                    }
                    boolean _result8 = registerScreenUnlockTotalNumberFence(_arg010, _arg112, _arg212, _arg39);
                    reply.writeNoException();
                    if (_result8) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg011 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg111 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg111 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg211 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg211 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg38 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg38 = null;
                    }
                    boolean _result9 = registerScreenUnlockFence(_arg011, _arg111, _arg211, _arg38);
                    reply.writeNoException();
                    if (_result9) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg012 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg110 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg110 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg210 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg210 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg37 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg37 = null;
                    }
                    boolean _result10 = unRegisterFence(_arg012, _arg110, _arg210, _arg37);
                    reply.writeNoException();
                    if (_result10) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = AwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg19 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg19 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg29 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg29 = null;
                    }
                    RequestResult _result11 = getFenceTriggerResult(_arg03, _arg19, _arg29);
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(1);
                        _result11.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result12 = checkServerVersion(data.readInt());
                    reply.writeNoException();
                    if (_result12) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    String _result13 = getAwarenessApiVersion();
                    reply.writeNoException();
                    reply.writeString(_result13);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg013 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg18 = true;
                    } else {
                        _arg18 = false;
                    }
                    if (data.readInt() != 0) {
                        _arg28 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg28 = null;
                    }
                    RequestResult _result14 = getCurrentAwareness(_arg013, _arg18, _arg28, data.readString());
                    reply.writeNoException();
                    if (_result14 != null) {
                        reply.writeInt(1);
                        _result14.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg014 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg17 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg17 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg27 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg27 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg36 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg36 = null;
                    }
                    boolean _result15 = registerCustomLocationFence(_arg014, _arg17, _arg27, _arg36);
                    reply.writeNoException();
                    if (_result15) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg015 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg16 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg16 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg26 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg26 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg35 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg35 = null;
                    }
                    boolean _result16 = registerBroadcastEventFence(_arg015, _arg16, _arg26, _arg35);
                    reply.writeNoException();
                    if (_result16) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_unRegisterExtendFence /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg016 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg15 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg15 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg25 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg25 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg34 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg34 = null;
                    }
                    boolean _result17 = unRegisterExtendFence(_arg016, _arg15, _arg25, _arg34);
                    reply.writeNoException();
                    if (_result17) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_getExtendFenceTriggerResult /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg14 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg14 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg24 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg24 = null;
                    }
                    RequestResult _result18 = getExtendFenceTriggerResult(_arg02, _arg14, _arg24);
                    reply.writeNoException();
                    if (_result18 != null) {
                        reply.writeInt(1);
                        _result18.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case TRANSACTION_getSupportAwarenessCapability /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    RequestResult _result19 = getSupportAwarenessCapability(data.readInt());
                    reply.writeNoException();
                    if (_result19 != null) {
                        reply.writeInt(1);
                        _result19.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case TRANSACTION_registerMovementFence /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg017 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg13 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg23 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg23 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg33 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg33 = null;
                    }
                    boolean _result20 = registerMovementFence(_arg017, _arg13, _arg23, _arg33);
                    reply.writeNoException();
                    if (_result20) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_registerDeviceStatusFence /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg018 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg12 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg32 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg32 = null;
                    }
                    boolean _result21 = registerDeviceStatusFence(_arg018, _arg12, _arg22, _arg32);
                    reply.writeNoException();
                    if (_result21) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_setReportPeriod /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    RequestResult _result22 = setReportPeriod(_arg0);
                    reply.writeNoException();
                    if (_result22 != null) {
                        reply.writeInt(1);
                        _result22.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case TRANSACTION_isIntegrateSensorHub /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result23 = isIntegrateSensorHub();
                    reply.writeNoException();
                    if (_result23) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_registerAppLifeChangeFence /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    IRequestCallBack _arg019 = IRequestCallBack.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg1 = ExtendAwarenessFence.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    boolean _result24 = registerAppLifeChangeFence(_arg019, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    if (_result24) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean checkServerVersion(int i) throws RemoteException;

    String getAwarenessApiVersion() throws RemoteException;

    RequestResult getCurrentAwareness(int i, boolean z, Bundle bundle, String str) throws RemoteException;

    RequestResult getCurrentStatus(int i) throws RemoteException;

    RequestResult getExtendFenceTriggerResult(ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    RequestResult getFenceTriggerResult(AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    RequestResult getSupportAwarenessCapability(int i) throws RemoteException;

    boolean isIntegrateSensorHub() throws RemoteException;

    boolean registerAppLifeChangeFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerAppUseTotalTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerBroadcastEventFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerCustomLocationFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerDeviceStatusFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerDeviceUseTotalTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerLocationFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerMotionFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerMovementFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerOneAppContinuousUseTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerScreenUnlockFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerScreenUnlockTotalNumberFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean registerTimeFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    RequestResult setReportPeriod(ExtendAwarenessFence extendAwarenessFence) throws RemoteException;

    boolean unRegisterExtendFence(IRequestCallBack iRequestCallBack, ExtendAwarenessFence extendAwarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean unRegisterFence(IRequestCallBack iRequestCallBack, AwarenessFence awarenessFence, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;
}
