package android.service.appprediction;

import android.app.prediction.AppPredictionContext;
import android.app.prediction.AppPredictionSessionId;
import android.app.prediction.AppTargetEvent;
import android.app.prediction.IPredictionCallback;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPredictionService extends IInterface {
    void notifyAppTargetEvent(AppPredictionSessionId appPredictionSessionId, AppTargetEvent appTargetEvent) throws RemoteException;

    void notifyLaunchLocationShown(AppPredictionSessionId appPredictionSessionId, String str, ParceledListSlice parceledListSlice) throws RemoteException;

    void onCreatePredictionSession(AppPredictionContext appPredictionContext, AppPredictionSessionId appPredictionSessionId) throws RemoteException;

    void onDestroyPredictionSession(AppPredictionSessionId appPredictionSessionId) throws RemoteException;

    void registerPredictionUpdates(AppPredictionSessionId appPredictionSessionId, IPredictionCallback iPredictionCallback) throws RemoteException;

    void requestPredictionUpdate(AppPredictionSessionId appPredictionSessionId) throws RemoteException;

    void sortAppTargets(AppPredictionSessionId appPredictionSessionId, ParceledListSlice parceledListSlice, IPredictionCallback iPredictionCallback) throws RemoteException;

    void unregisterPredictionUpdates(AppPredictionSessionId appPredictionSessionId, IPredictionCallback iPredictionCallback) throws RemoteException;

    public static class Default implements IPredictionService {
        @Override // android.service.appprediction.IPredictionService
        public void onCreatePredictionSession(AppPredictionContext context, AppPredictionSessionId sessionId) throws RemoteException {
        }

        @Override // android.service.appprediction.IPredictionService
        public void notifyAppTargetEvent(AppPredictionSessionId sessionId, AppTargetEvent event) throws RemoteException {
        }

        @Override // android.service.appprediction.IPredictionService
        public void notifyLaunchLocationShown(AppPredictionSessionId sessionId, String launchLocation, ParceledListSlice targetIds) throws RemoteException {
        }

        @Override // android.service.appprediction.IPredictionService
        public void sortAppTargets(AppPredictionSessionId sessionId, ParceledListSlice targets, IPredictionCallback callback) throws RemoteException {
        }

        @Override // android.service.appprediction.IPredictionService
        public void registerPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) throws RemoteException {
        }

        @Override // android.service.appprediction.IPredictionService
        public void unregisterPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) throws RemoteException {
        }

        @Override // android.service.appprediction.IPredictionService
        public void requestPredictionUpdate(AppPredictionSessionId sessionId) throws RemoteException {
        }

        @Override // android.service.appprediction.IPredictionService
        public void onDestroyPredictionSession(AppPredictionSessionId sessionId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPredictionService {
        private static final String DESCRIPTOR = "android.service.appprediction.IPredictionService";
        static final int TRANSACTION_notifyAppTargetEvent = 2;
        static final int TRANSACTION_notifyLaunchLocationShown = 3;
        static final int TRANSACTION_onCreatePredictionSession = 1;
        static final int TRANSACTION_onDestroyPredictionSession = 8;
        static final int TRANSACTION_registerPredictionUpdates = 5;
        static final int TRANSACTION_requestPredictionUpdate = 7;
        static final int TRANSACTION_sortAppTargets = 4;
        static final int TRANSACTION_unregisterPredictionUpdates = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPredictionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPredictionService)) {
                return new Proxy(obj);
            }
            return (IPredictionService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onCreatePredictionSession";
                case 2:
                    return "notifyAppTargetEvent";
                case 3:
                    return "notifyLaunchLocationShown";
                case 4:
                    return "sortAppTargets";
                case 5:
                    return "registerPredictionUpdates";
                case 6:
                    return "unregisterPredictionUpdates";
                case 7:
                    return "requestPredictionUpdate";
                case 8:
                    return "onDestroyPredictionSession";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AppPredictionContext _arg0;
            AppPredictionSessionId _arg1;
            AppPredictionSessionId _arg02;
            AppTargetEvent _arg12;
            AppPredictionSessionId _arg03;
            ParceledListSlice _arg2;
            AppPredictionSessionId _arg04;
            ParceledListSlice _arg13;
            AppPredictionSessionId _arg05;
            AppPredictionSessionId _arg06;
            AppPredictionSessionId _arg07;
            AppPredictionSessionId _arg08;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AppPredictionContext.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onCreatePredictionSession(_arg0, _arg1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = AppTargetEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        notifyAppTargetEvent(_arg02, _arg12);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        notifyLaunchLocationShown(_arg03, _arg14, _arg2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        sortAppTargets(_arg04, _arg13, IPredictionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        registerPredictionUpdates(_arg05, IPredictionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        unregisterPredictionUpdates(_arg06, IPredictionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        requestPredictionUpdate(_arg07);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = AppPredictionSessionId.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        onDestroyPredictionSession(_arg08);
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
        public static class Proxy implements IPredictionService {
            public static IPredictionService sDefaultImpl;
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

            @Override // android.service.appprediction.IPredictionService
            public void onCreatePredictionSession(AppPredictionContext context, AppPredictionSessionId sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (context != null) {
                        _data.writeInt(1);
                        context.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onCreatePredictionSession(context, sessionId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.appprediction.IPredictionService
            public void notifyAppTargetEvent(AppPredictionSessionId sessionId, AppTargetEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyAppTargetEvent(sessionId, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.appprediction.IPredictionService
            public void notifyLaunchLocationShown(AppPredictionSessionId sessionId, String launchLocation, ParceledListSlice targetIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(launchLocation);
                    if (targetIds != null) {
                        _data.writeInt(1);
                        targetIds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyLaunchLocationShown(sessionId, launchLocation, targetIds);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.appprediction.IPredictionService
            public void sortAppTargets(AppPredictionSessionId sessionId, ParceledListSlice targets, IPredictionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (targets != null) {
                        _data.writeInt(1);
                        targets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sortAppTargets(sessionId, targets, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.appprediction.IPredictionService
            public void registerPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().registerPredictionUpdates(sessionId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.appprediction.IPredictionService
            public void unregisterPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unregisterPredictionUpdates(sessionId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.appprediction.IPredictionService
            public void requestPredictionUpdate(AppPredictionSessionId sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestPredictionUpdate(sessionId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.appprediction.IPredictionService
            public void onDestroyPredictionSession(AppPredictionSessionId sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionId != null) {
                        _data.writeInt(1);
                        sessionId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDestroyPredictionSession(sessionId);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPredictionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPredictionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
