package com.huawei.nb.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.callback.IAIFetchCallback;
import com.huawei.nb.callback.IAISubscribeCallback;
import com.huawei.nb.callback.IDeleteResInfoCallBack;
import com.huawei.nb.callback.IUpdatePackageCallBack;
import com.huawei.nb.callback.IUpdatePackageCheckCallBack;
import com.huawei.nb.container.ObjectContainer;
import com.huawei.nb.notification.IAIObserver;
import com.huawei.nb.notification.ModelObserverInfo;

public interface IAIServiceCall extends IInterface {
    void deleteResInfoAgent(ObjectContainer objectContainer, IDeleteResInfoCallBack iDeleteResInfoCallBack) throws RemoteException;

    ObjectContainer insertResInfoAgent(ObjectContainer objectContainer) throws RemoteException;

    int registerObserver(ModelObserverInfo modelObserverInfo, IAIObserver iAIObserver, IAISubscribeCallback iAISubscribeCallback) throws RemoteException;

    ObjectContainer requestAiModel(ObjectContainer objectContainer) throws RemoteException;

    int requestAiModelAsync(ObjectContainer objectContainer, IAIFetchCallback iAIFetchCallback) throws RemoteException;

    int unregisterObserver(ModelObserverInfo modelObserverInfo, IAIObserver iAIObserver, IAISubscribeCallback iAISubscribeCallback) throws RemoteException;

    void updatePackageAgent(ObjectContainer objectContainer, IUpdatePackageCallBack iUpdatePackageCallBack, long j, long j2, boolean z) throws RemoteException;

    void updatePackageCheckAgent(ObjectContainer objectContainer, IUpdatePackageCheckCallBack iUpdatePackageCheckCallBack) throws RemoteException;

    boolean updateResInfoAgent(ObjectContainer objectContainer) throws RemoteException;

    public static abstract class Stub extends Binder implements IAIServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.service.IAIServiceCall";
        static final int TRANSACTION_deleteResInfoAgent = 5;
        static final int TRANSACTION_insertResInfoAgent = 6;
        static final int TRANSACTION_registerObserver = 8;
        static final int TRANSACTION_requestAiModel = 1;
        static final int TRANSACTION_requestAiModelAsync = 2;
        static final int TRANSACTION_unregisterObserver = 9;
        static final int TRANSACTION_updatePackageAgent = 4;
        static final int TRANSACTION_updatePackageCheckAgent = 3;
        static final int TRANSACTION_updateResInfoAgent = 7;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAIServiceCall asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IAIServiceCall)) {
                return new Proxy(iBinder);
            }
            return (IAIServiceCall) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                ObjectContainer objectContainer = null;
                ModelObserverInfo modelObserverInfo = null;
                ModelObserverInfo modelObserverInfo2 = null;
                ObjectContainer objectContainer2 = null;
                ObjectContainer objectContainer3 = null;
                ObjectContainer objectContainer4 = null;
                ObjectContainer objectContainer5 = null;
                ObjectContainer objectContainer6 = null;
                ObjectContainer objectContainer7 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        ObjectContainer requestAiModel = requestAiModel(objectContainer);
                        parcel2.writeNoException();
                        if (requestAiModel != null) {
                            parcel2.writeInt(1);
                            requestAiModel.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer7 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        int requestAiModelAsync = requestAiModelAsync(objectContainer7, IAIFetchCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(requestAiModelAsync);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer6 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        updatePackageCheckAgent(objectContainer6, IUpdatePackageCheckCallBack.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer5 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        updatePackageAgent(objectContainer5, IUpdatePackageCallBack.Stub.asInterface(parcel.readStrongBinder()), parcel.readLong(), parcel.readLong(), parcel.readInt() != 0);
                        parcel2.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer4 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        deleteResInfoAgent(objectContainer4, IDeleteResInfoCallBack.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer3 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        ObjectContainer insertResInfoAgent = insertResInfoAgent(objectContainer3);
                        parcel2.writeNoException();
                        if (insertResInfoAgent != null) {
                            parcel2.writeInt(1);
                            insertResInfoAgent.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer2 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean updateResInfoAgent = updateResInfoAgent(objectContainer2);
                        parcel2.writeNoException();
                        parcel2.writeInt(updateResInfoAgent ? 1 : 0);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            modelObserverInfo2 = ModelObserverInfo.CREATOR.createFromParcel(parcel);
                        }
                        int registerObserver = registerObserver(modelObserverInfo2, IAIObserver.Stub.asInterface(parcel.readStrongBinder()), IAISubscribeCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerObserver);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            modelObserverInfo = ModelObserverInfo.CREATOR.createFromParcel(parcel);
                        }
                        int unregisterObserver = unregisterObserver(modelObserverInfo, IAIObserver.Stub.asInterface(parcel.readStrongBinder()), IAISubscribeCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(unregisterObserver);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IAIServiceCall {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public ObjectContainer requestAiModel(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? ObjectContainer.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public int requestAiModelAsync(ObjectContainer objectContainer, IAIFetchCallback iAIFetchCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iAIFetchCallback != null ? iAIFetchCallback.asBinder() : null);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public void updatePackageCheckAgent(ObjectContainer objectContainer, IUpdatePackageCheckCallBack iUpdatePackageCheckCallBack) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iUpdatePackageCheckCallBack != null ? iUpdatePackageCheckCallBack.asBinder() : null);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public void updatePackageAgent(ObjectContainer objectContainer, IUpdatePackageCallBack iUpdatePackageCallBack, long j, long j2, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iUpdatePackageCallBack != null ? iUpdatePackageCallBack.asBinder() : null);
                    obtain.writeLong(j);
                    obtain.writeLong(j2);
                    if (!z) {
                        i = 0;
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public void deleteResInfoAgent(ObjectContainer objectContainer, IDeleteResInfoCallBack iDeleteResInfoCallBack) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iDeleteResInfoCallBack != null ? iDeleteResInfoCallBack.asBinder() : null);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public ObjectContainer insertResInfoAgent(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? ObjectContainer.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public boolean updateResInfoAgent(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public int registerObserver(ModelObserverInfo modelObserverInfo, IAIObserver iAIObserver, IAISubscribeCallback iAISubscribeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (modelObserverInfo != null) {
                        obtain.writeInt(1);
                        modelObserverInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iAIObserver != null ? iAIObserver.asBinder() : null);
                    if (iAISubscribeCallback != null) {
                        iBinder = iAISubscribeCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IAIServiceCall
            public int unregisterObserver(ModelObserverInfo modelObserverInfo, IAIObserver iAIObserver, IAISubscribeCallback iAISubscribeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (modelObserverInfo != null) {
                        obtain.writeInt(1);
                        modelObserverInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iAIObserver != null ? iAIObserver.asBinder() : null);
                    if (iAISubscribeCallback != null) {
                        iBinder = iAISubscribeCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
