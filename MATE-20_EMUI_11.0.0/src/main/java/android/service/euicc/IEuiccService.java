package android.service.euicc;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.euicc.IDeleteSubscriptionCallback;
import android.service.euicc.IDownloadSubscriptionCallback;
import android.service.euicc.IEraseSubscriptionsCallback;
import android.service.euicc.IGetDefaultDownloadableSubscriptionListCallback;
import android.service.euicc.IGetDownloadableSubscriptionMetadataCallback;
import android.service.euicc.IGetEidCallback;
import android.service.euicc.IGetEuiccInfoCallback;
import android.service.euicc.IGetEuiccProfileInfoListCallback;
import android.service.euicc.IGetInnerServiceCallback;
import android.service.euicc.IGetOtaStatusCallback;
import android.service.euicc.IOtaStatusChangedCallback;
import android.service.euicc.IRetainSubscriptionsForFactoryResetCallback;
import android.service.euicc.ISwitchToSubscriptionCallback;
import android.service.euicc.IUpdateSubscriptionNicknameCallback;
import android.telephony.euicc.DownloadableSubscription;

public interface IEuiccService extends IInterface {
    void deleteSubscription(int i, String str, IDeleteSubscriptionCallback iDeleteSubscriptionCallback) throws RemoteException;

    void downloadSubscription(int i, DownloadableSubscription downloadableSubscription, boolean z, boolean z2, Bundle bundle, IDownloadSubscriptionCallback iDownloadSubscriptionCallback) throws RemoteException;

    void eraseSubscriptions(int i, IEraseSubscriptionsCallback iEraseSubscriptionsCallback) throws RemoteException;

    void getDefaultDownloadableSubscriptionList(int i, boolean z, IGetDefaultDownloadableSubscriptionListCallback iGetDefaultDownloadableSubscriptionListCallback) throws RemoteException;

    void getDownloadableSubscriptionMetadata(int i, DownloadableSubscription downloadableSubscription, boolean z, IGetDownloadableSubscriptionMetadataCallback iGetDownloadableSubscriptionMetadataCallback) throws RemoteException;

    void getEid(int i, IGetEidCallback iGetEidCallback) throws RemoteException;

    void getEuiccInfo(int i, IGetEuiccInfoCallback iGetEuiccInfoCallback) throws RemoteException;

    void getEuiccProfileInfoList(int i, IGetEuiccProfileInfoListCallback iGetEuiccProfileInfoListCallback) throws RemoteException;

    void getHwInnerService(IGetInnerServiceCallback iGetInnerServiceCallback) throws RemoteException;

    void getOtaStatus(int i, IGetOtaStatusCallback iGetOtaStatusCallback) throws RemoteException;

    void retainSubscriptionsForFactoryReset(int i, IRetainSubscriptionsForFactoryResetCallback iRetainSubscriptionsForFactoryResetCallback) throws RemoteException;

    void startOtaIfNecessary(int i, IOtaStatusChangedCallback iOtaStatusChangedCallback) throws RemoteException;

    void switchToSubscription(int i, String str, boolean z, ISwitchToSubscriptionCallback iSwitchToSubscriptionCallback) throws RemoteException;

    void updateSubscriptionNickname(int i, String str, String str2, IUpdateSubscriptionNicknameCallback iUpdateSubscriptionNicknameCallback) throws RemoteException;

    public static class Default implements IEuiccService {
        @Override // android.service.euicc.IEuiccService
        public void downloadSubscription(int slotId, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, Bundle resolvedBundle, IDownloadSubscriptionCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void getDownloadableSubscriptionMetadata(int slotId, DownloadableSubscription subscription, boolean forceDeactivateSim, IGetDownloadableSubscriptionMetadataCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void getEid(int slotId, IGetEidCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void getOtaStatus(int slotId, IGetOtaStatusCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void startOtaIfNecessary(int slotId, IOtaStatusChangedCallback statusChangedCallback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void getEuiccProfileInfoList(int slotId, IGetEuiccProfileInfoListCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void getDefaultDownloadableSubscriptionList(int slotId, boolean forceDeactivateSim, IGetDefaultDownloadableSubscriptionListCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void getEuiccInfo(int slotId, IGetEuiccInfoCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void deleteSubscription(int slotId, String iccid, IDeleteSubscriptionCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void switchToSubscription(int slotId, String iccid, boolean forceDeactivateSim, ISwitchToSubscriptionCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void updateSubscriptionNickname(int slotId, String iccid, String nickname, IUpdateSubscriptionNicknameCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void eraseSubscriptions(int slotId, IEraseSubscriptionsCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void retainSubscriptionsForFactoryReset(int slotId, IRetainSubscriptionsForFactoryResetCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IEuiccService
        public void getHwInnerService(IGetInnerServiceCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEuiccService {
        private static final String DESCRIPTOR = "android.service.euicc.IEuiccService";
        static final int TRANSACTION_deleteSubscription = 9;
        static final int TRANSACTION_downloadSubscription = 1;
        static final int TRANSACTION_eraseSubscriptions = 12;
        static final int TRANSACTION_getDefaultDownloadableSubscriptionList = 7;
        static final int TRANSACTION_getDownloadableSubscriptionMetadata = 2;
        static final int TRANSACTION_getEid = 3;
        static final int TRANSACTION_getEuiccInfo = 8;
        static final int TRANSACTION_getEuiccProfileInfoList = 6;
        static final int TRANSACTION_getHwInnerService = 14;
        static final int TRANSACTION_getOtaStatus = 4;
        static final int TRANSACTION_retainSubscriptionsForFactoryReset = 13;
        static final int TRANSACTION_startOtaIfNecessary = 5;
        static final int TRANSACTION_switchToSubscription = 10;
        static final int TRANSACTION_updateSubscriptionNickname = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEuiccService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEuiccService)) {
                return new Proxy(obj);
            }
            return (IEuiccService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "downloadSubscription";
                case 2:
                    return "getDownloadableSubscriptionMetadata";
                case 3:
                    return "getEid";
                case 4:
                    return "getOtaStatus";
                case 5:
                    return "startOtaIfNecessary";
                case 6:
                    return "getEuiccProfileInfoList";
                case 7:
                    return "getDefaultDownloadableSubscriptionList";
                case 8:
                    return "getEuiccInfo";
                case 9:
                    return "deleteSubscription";
                case 10:
                    return "switchToSubscription";
                case 11:
                    return "updateSubscriptionNickname";
                case 12:
                    return "eraseSubscriptions";
                case 13:
                    return "retainSubscriptionsForFactoryReset";
                case 14:
                    return "getHwInnerService";
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
            DownloadableSubscription _arg1;
            Bundle _arg4;
            DownloadableSubscription _arg12;
            if (code != 1598968902) {
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = DownloadableSubscription.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean _arg22 = data.readInt() != 0;
                        boolean _arg3 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg4 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        downloadSubscription(_arg0, _arg1, _arg22, _arg3, _arg4, IDownloadSubscriptionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = DownloadableSubscription.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        getDownloadableSubscriptionMetadata(_arg02, _arg12, _arg2, IGetDownloadableSubscriptionMetadataCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        getEid(data.readInt(), IGetEidCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        getOtaStatus(data.readInt(), IGetOtaStatusCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        startOtaIfNecessary(data.readInt(), IOtaStatusChangedCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        getEuiccProfileInfoList(data.readInt(), IGetEuiccProfileInfoListCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        getDefaultDownloadableSubscriptionList(_arg03, _arg2, IGetDefaultDownloadableSubscriptionListCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        getEuiccInfo(data.readInt(), IGetEuiccInfoCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        deleteSubscription(data.readInt(), data.readString(), IDeleteSubscriptionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        switchToSubscription(_arg04, _arg13, _arg2, ISwitchToSubscriptionCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        updateSubscriptionNickname(data.readInt(), data.readString(), data.readString(), IUpdateSubscriptionNicknameCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        eraseSubscriptions(data.readInt(), IEraseSubscriptionsCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        retainSubscriptionsForFactoryReset(data.readInt(), IRetainSubscriptionsForFactoryResetCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        getHwInnerService(IGetInnerServiceCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IEuiccService {
            public static IEuiccService sDefaultImpl;
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

            @Override // android.service.euicc.IEuiccService
            public void downloadSubscription(int slotId, DownloadableSubscription subscription, boolean switchAfterDownload, boolean forceDeactivateSim, Bundle resolvedBundle, IDownloadSubscriptionCallback callback) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(slotId);
                        if (subscription != null) {
                            _data.writeInt(1);
                            subscription.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeInt(switchAfterDownload ? 1 : 0);
                        _data.writeInt(forceDeactivateSim ? 1 : 0);
                        if (resolvedBundle != null) {
                            _data.writeInt(1);
                            resolvedBundle.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                        try {
                            if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().downloadSubscription(slotId, subscription, switchAfterDownload, forceDeactivateSim, resolvedBundle, callback);
                            _data.recycle();
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void getDownloadableSubscriptionMetadata(int slotId, DownloadableSubscription subscription, boolean forceDeactivateSim, IGetDownloadableSubscriptionMetadataCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    int i = 0;
                    if (subscription != null) {
                        _data.writeInt(1);
                        subscription.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (forceDeactivateSim) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getDownloadableSubscriptionMetadata(slotId, subscription, forceDeactivateSim, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void getEid(int slotId, IGetEidCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getEid(slotId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void getOtaStatus(int slotId, IGetOtaStatusCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getOtaStatus(slotId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void startOtaIfNecessary(int slotId, IOtaStatusChangedCallback statusChangedCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(statusChangedCallback != null ? statusChangedCallback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startOtaIfNecessary(slotId, statusChangedCallback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void getEuiccProfileInfoList(int slotId, IGetEuiccProfileInfoListCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getEuiccProfileInfoList(slotId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void getDefaultDownloadableSubscriptionList(int slotId, boolean forceDeactivateSim, IGetDefaultDownloadableSubscriptionListCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(forceDeactivateSim ? 1 : 0);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getDefaultDownloadableSubscriptionList(slotId, forceDeactivateSim, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void getEuiccInfo(int slotId, IGetEuiccInfoCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getEuiccInfo(slotId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void deleteSubscription(int slotId, String iccid, IDeleteSubscriptionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeString(iccid);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().deleteSubscription(slotId, iccid, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void switchToSubscription(int slotId, String iccid, boolean forceDeactivateSim, ISwitchToSubscriptionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeString(iccid);
                    _data.writeInt(forceDeactivateSim ? 1 : 0);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().switchToSubscription(slotId, iccid, forceDeactivateSim, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void updateSubscriptionNickname(int slotId, String iccid, String nickname, IUpdateSubscriptionNicknameCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeString(iccid);
                    _data.writeString(nickname);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateSubscriptionNickname(slotId, iccid, nickname, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void eraseSubscriptions(int slotId, IEraseSubscriptionsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().eraseSubscriptions(slotId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void retainSubscriptionsForFactoryReset(int slotId, IRetainSubscriptionsForFactoryResetCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().retainSubscriptionsForFactoryReset(slotId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IEuiccService
            public void getHwInnerService(IGetInnerServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getHwInnerService(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IEuiccService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEuiccService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
