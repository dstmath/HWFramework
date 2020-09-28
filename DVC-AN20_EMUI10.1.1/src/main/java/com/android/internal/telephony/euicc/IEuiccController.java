package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;

public interface IEuiccController extends IInterface {
    void continueOperation(int i, Intent intent, Bundle bundle) throws RemoteException;

    void deleteSubscription(int i, int i2, String str, PendingIntent pendingIntent) throws RemoteException;

    void downloadSubscription(int i, DownloadableSubscription downloadableSubscription, boolean z, String str, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    void eraseSubscriptions(int i, PendingIntent pendingIntent) throws RemoteException;

    void getDefaultDownloadableSubscriptionList(int i, String str, PendingIntent pendingIntent) throws RemoteException;

    void getDownloadableSubscriptionMetadata(int i, DownloadableSubscription downloadableSubscription, String str, PendingIntent pendingIntent) throws RemoteException;

    String getEid(int i, String str) throws RemoteException;

    EuiccInfo getEuiccInfo(int i) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    int getOtaStatus(int i) throws RemoteException;

    void retainSubscriptionsForFactoryReset(int i, PendingIntent pendingIntent) throws RemoteException;

    void switchToSubscription(int i, int i2, String str, PendingIntent pendingIntent) throws RemoteException;

    void updateSubscriptionNickname(int i, int i2, String str, String str2, PendingIntent pendingIntent) throws RemoteException;

    public static class Default implements IEuiccController {
        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void continueOperation(int cardId, Intent resolutionIntent, Bundle resolutionExtras) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void getDownloadableSubscriptionMetadata(int cardId, DownloadableSubscription subscription, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void getDefaultDownloadableSubscriptionList(int cardId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public String getEid(int cardId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public int getOtaStatus(int cardId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void downloadSubscription(int cardId, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, Bundle resolvedBundle, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public EuiccInfo getEuiccInfo(int cardId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void deleteSubscription(int cardId, int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void switchToSubscription(int cardId, int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void updateSubscriptionNickname(int cardId, int subscriptionId, String nickname, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void eraseSubscriptions(int cardId, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public void retainSubscriptionsForFactoryReset(int cardId, PendingIntent callbackIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.euicc.IEuiccController
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEuiccController {
        private static final String DESCRIPTOR = "com.android.internal.telephony.euicc.IEuiccController";
        static final int TRANSACTION_continueOperation = 1;
        static final int TRANSACTION_deleteSubscription = 8;
        static final int TRANSACTION_downloadSubscription = 6;
        static final int TRANSACTION_eraseSubscriptions = 11;
        static final int TRANSACTION_getDefaultDownloadableSubscriptionList = 3;
        static final int TRANSACTION_getDownloadableSubscriptionMetadata = 2;
        static final int TRANSACTION_getEid = 4;
        static final int TRANSACTION_getEuiccInfo = 7;
        static final int TRANSACTION_getHwInnerService = 13;
        static final int TRANSACTION_getOtaStatus = 5;
        static final int TRANSACTION_retainSubscriptionsForFactoryReset = 12;
        static final int TRANSACTION_switchToSubscription = 9;
        static final int TRANSACTION_updateSubscriptionNickname = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEuiccController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEuiccController)) {
                return new Proxy(obj);
            }
            return (IEuiccController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "continueOperation";
                case 2:
                    return "getDownloadableSubscriptionMetadata";
                case 3:
                    return "getDefaultDownloadableSubscriptionList";
                case 4:
                    return "getEid";
                case 5:
                    return "getOtaStatus";
                case 6:
                    return "downloadSubscription";
                case 7:
                    return "getEuiccInfo";
                case 8:
                    return "deleteSubscription";
                case 9:
                    return "switchToSubscription";
                case 10:
                    return "updateSubscriptionNickname";
                case 11:
                    return "eraseSubscriptions";
                case 12:
                    return "retainSubscriptionsForFactoryReset";
                case 13:
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
            Intent _arg1;
            Bundle _arg2;
            DownloadableSubscription _arg12;
            PendingIntent _arg3;
            PendingIntent _arg22;
            DownloadableSubscription _arg13;
            Bundle _arg4;
            PendingIntent _arg5;
            PendingIntent _arg32;
            PendingIntent _arg33;
            PendingIntent _arg42;
            PendingIntent _arg14;
            PendingIntent _arg15;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        continueOperation(_arg0, _arg1, _arg2);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = DownloadableSubscription.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        String _arg23 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        getDownloadableSubscriptionMetadata(_arg02, _arg12, _arg23, _arg3);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        getDefaultDownloadableSubscriptionList(_arg03, _arg16, _arg22);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getEid(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getOtaStatus(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = DownloadableSubscription.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean _arg24 = data.readInt() != 0;
                        String _arg34 = data.readString();
                        if (data.readInt() != 0) {
                            _arg4 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg5 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        downloadSubscription(_arg04, _arg13, _arg24, _arg34, _arg4, _arg5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        EuiccInfo _result3 = getEuiccInfo(data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        int _arg17 = data.readInt();
                        String _arg25 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        deleteSubscription(_arg05, _arg17, _arg25, _arg32);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        int _arg18 = data.readInt();
                        String _arg26 = data.readString();
                        if (data.readInt() != 0) {
                            _arg33 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg33 = null;
                        }
                        switchToSubscription(_arg06, _arg18, _arg26, _arg33);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        int _arg19 = data.readInt();
                        String _arg27 = data.readString();
                        String _arg35 = data.readString();
                        if (data.readInt() != 0) {
                            _arg42 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        updateSubscriptionNickname(_arg07, _arg19, _arg27, _arg35, _arg42);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg14 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        eraseSubscriptions(_arg08, _arg14);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg15 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        retainSubscriptionsForFactoryReset(_arg09, _arg15);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result4 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result4);
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
        public static class Proxy implements IEuiccController {
            public static IEuiccController sDefaultImpl;
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

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void continueOperation(int cardId, Intent resolutionIntent, Bundle resolutionExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    if (resolutionIntent != null) {
                        _data.writeInt(1);
                        resolutionIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (resolutionExtras != null) {
                        _data.writeInt(1);
                        resolutionExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().continueOperation(cardId, resolutionIntent, resolutionExtras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void getDownloadableSubscriptionMetadata(int cardId, DownloadableSubscription subscription, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    if (subscription != null) {
                        _data.writeInt(1);
                        subscription.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getDownloadableSubscriptionMetadata(cardId, subscription, callingPackage, callbackIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void getDefaultDownloadableSubscriptionList(int cardId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getDefaultDownloadableSubscriptionList(cardId, callingPackage, callbackIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public String getEid(int cardId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEid(cardId, callingPackage);
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

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public int getOtaStatus(int cardId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOtaStatus(cardId);
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

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void downloadSubscription(int cardId, DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, Bundle resolvedBundle, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(cardId);
                        if (subscription != null) {
                            _data.writeInt(1);
                            subscription.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeInt(switchAfterDownload ? 1 : 0);
                        try {
                            _data.writeString(callingPackage);
                            if (resolvedBundle != null) {
                                _data.writeInt(1);
                                resolvedBundle.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (callbackIntent != null) {
                                _data.writeInt(1);
                                callbackIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th) {
                            th = th;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().downloadSubscription(cardId, subscription, switchAfterDownload, callingPackage, resolvedBundle, callbackIntent);
                        _data.recycle();
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

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public EuiccInfo getEuiccInfo(int cardId) throws RemoteException {
                EuiccInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEuiccInfo(cardId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = EuiccInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void deleteSubscription(int cardId, int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    _data.writeInt(subscriptionId);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().deleteSubscription(cardId, subscriptionId, callingPackage, callbackIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void switchToSubscription(int cardId, int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    _data.writeInt(subscriptionId);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().switchToSubscription(cardId, subscriptionId, callingPackage, callbackIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void updateSubscriptionNickname(int cardId, int subscriptionId, String nickname, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    _data.writeInt(subscriptionId);
                    _data.writeString(nickname);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateSubscriptionNickname(cardId, subscriptionId, nickname, callingPackage, callbackIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void eraseSubscriptions(int cardId, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().eraseSubscriptions(cardId, callbackIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public void retainSubscriptionsForFactoryReset(int cardId, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cardId);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().retainSubscriptionsForFactoryReset(cardId, callbackIntent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.euicc.IEuiccController
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IEuiccController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEuiccController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
