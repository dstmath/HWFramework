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
        static final int TRANSACTION_getOtaStatus = 5;
        static final int TRANSACTION_retainSubscriptionsForFactoryReset = 12;
        static final int TRANSACTION_switchToSubscription = 9;
        static final int TRANSACTION_updateSubscriptionNickname = 10;

        private static class Proxy implements IEuiccController {
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

            public void continueOperation(Intent resolutionIntent, Bundle resolutionExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getDownloadableSubscriptionMetadata(DownloadableSubscription subscription, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getDefaultDownloadableSubscriptionList(String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public String getEid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getOtaStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void downloadSubscription(DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (subscription != null) {
                        _data.writeInt(1);
                        subscription.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(switchAfterDownload);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public EuiccInfo getEuiccInfo() throws RemoteException {
                EuiccInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (EuiccInfo) EuiccInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteSubscription(int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void switchToSubscription(int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateSubscriptionNickname(int subscriptionId, String nickname, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    _data.writeString(nickname);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void eraseSubscriptions(PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void retainSubscriptionsForFactoryReset(PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v13, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v18, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v22, resolved type: android.app.PendingIntent} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v26, resolved type: android.app.PendingIntent} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v30 */
        /* JADX WARNING: type inference failed for: r3v33 */
        /* JADX WARNING: type inference failed for: r3v36 */
        /* JADX WARNING: type inference failed for: r3v37 */
        /* JADX WARNING: type inference failed for: r3v38 */
        /* JADX WARNING: type inference failed for: r3v39 */
        /* JADX WARNING: type inference failed for: r3v40 */
        /* JADX WARNING: type inference failed for: r3v41 */
        /* JADX WARNING: type inference failed for: r3v42 */
        /* JADX WARNING: type inference failed for: r3v43 */
        /* JADX WARNING: type inference failed for: r3v44 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg0;
            DownloadableSubscription _arg02;
            DownloadableSubscription _arg03;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ? _arg2 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Intent) Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        continueOperation(_arg0, _arg2);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (DownloadableSubscription) DownloadableSubscription.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        getDownloadableSubscriptionMetadata(_arg02, _arg12, _arg2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        getDefaultDownloadableSubscriptionList(_arg04, _arg2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getEid();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getOtaStatus();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (DownloadableSubscription) DownloadableSubscription.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        String _arg22 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        downloadSubscription(_arg03, _arg1, _arg22, _arg2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        EuiccInfo _result3 = getEuiccInfo();
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
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        deleteSubscription(_arg05, _arg13, _arg2);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        switchToSubscription(_arg06, _arg14, _arg2);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        String _arg15 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        updateSubscriptionNickname(_arg07, _arg15, _arg2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        eraseSubscriptions(_arg2);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        retainSubscriptionsForFactoryReset(_arg2);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void continueOperation(Intent intent, Bundle bundle) throws RemoteException;

    void deleteSubscription(int i, String str, PendingIntent pendingIntent) throws RemoteException;

    void downloadSubscription(DownloadableSubscription downloadableSubscription, boolean z, String str, PendingIntent pendingIntent) throws RemoteException;

    void eraseSubscriptions(PendingIntent pendingIntent) throws RemoteException;

    void getDefaultDownloadableSubscriptionList(String str, PendingIntent pendingIntent) throws RemoteException;

    void getDownloadableSubscriptionMetadata(DownloadableSubscription downloadableSubscription, String str, PendingIntent pendingIntent) throws RemoteException;

    String getEid() throws RemoteException;

    EuiccInfo getEuiccInfo() throws RemoteException;

    int getOtaStatus() throws RemoteException;

    void retainSubscriptionsForFactoryReset(PendingIntent pendingIntent) throws RemoteException;

    void switchToSubscription(int i, String str, PendingIntent pendingIntent) throws RemoteException;

    void updateSubscriptionNickname(int i, String str, PendingIntent pendingIntent) throws RemoteException;
}
