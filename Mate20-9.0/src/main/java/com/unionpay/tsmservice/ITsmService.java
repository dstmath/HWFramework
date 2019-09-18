package com.unionpay.tsmservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.unionpay.tsmservice.ITsmActivityCallback;
import com.unionpay.tsmservice.ITsmCallback;
import com.unionpay.tsmservice.ITsmProgressCallback;
import com.unionpay.tsmservice.OnSafetyKeyboardCallback;
import com.unionpay.tsmservice.data.Constant;
import com.unionpay.tsmservice.request.AcquireSEAppListRequestParams;
import com.unionpay.tsmservice.request.ActivateVendorPayRequestParams;
import com.unionpay.tsmservice.request.AddCardToVendorPayRequestParams;
import com.unionpay.tsmservice.request.AppDataUpdateRequestParams;
import com.unionpay.tsmservice.request.AppDeleteRequestParams;
import com.unionpay.tsmservice.request.AppDownloadApplyRequestParams;
import com.unionpay.tsmservice.request.AppDownloadRequestParams;
import com.unionpay.tsmservice.request.AppLockRequestParams;
import com.unionpay.tsmservice.request.AppUnlockRequestParams;
import com.unionpay.tsmservice.request.CardListStatusChangedRequestParams;
import com.unionpay.tsmservice.request.CheckSSamsungPayRequestParams;
import com.unionpay.tsmservice.request.CheckSupportCardApplyRequestParams;
import com.unionpay.tsmservice.request.ClearEncryptDataRequestParams;
import com.unionpay.tsmservice.request.CloseChannelRequestParams;
import com.unionpay.tsmservice.request.ECashTopUpRequestParams;
import com.unionpay.tsmservice.request.EncryptDataRequestParams;
import com.unionpay.tsmservice.request.ExecuteCmdRequestParams;
import com.unionpay.tsmservice.request.GetAccountBalanceRequestParams;
import com.unionpay.tsmservice.request.GetAccountInfoRequestParams;
import com.unionpay.tsmservice.request.GetAppDetailRequestParams;
import com.unionpay.tsmservice.request.GetAppListRequestParams;
import com.unionpay.tsmservice.request.GetAppStatusRequestParams;
import com.unionpay.tsmservice.request.GetAssociatedAppRequestParams;
import com.unionpay.tsmservice.request.GetCardInfoBySpayRequestParams;
import com.unionpay.tsmservice.request.GetCardInfoRequestParams;
import com.unionpay.tsmservice.request.GetCurrentWalletClientRequestParams;
import com.unionpay.tsmservice.request.GetDefaultCardRequestParams;
import com.unionpay.tsmservice.request.GetEncryptDataRequestParams;
import com.unionpay.tsmservice.request.GetMessageDetailsRequestParams;
import com.unionpay.tsmservice.request.GetSMSAuthCodeRequestParams;
import com.unionpay.tsmservice.request.GetSeAppListRequestParams;
import com.unionpay.tsmservice.request.GetSeIdRequestParams;
import com.unionpay.tsmservice.request.GetTransElementsRequestParams;
import com.unionpay.tsmservice.request.GetTransRecordRequestParams;
import com.unionpay.tsmservice.request.GetTransactionDetailsRequestParams;
import com.unionpay.tsmservice.request.GetVendorPayStatusRequestParams;
import com.unionpay.tsmservice.request.HideAppApplyRequestParams;
import com.unionpay.tsmservice.request.HideSafetyKeyboardRequestParams;
import com.unionpay.tsmservice.request.InitRequestParams;
import com.unionpay.tsmservice.request.OnlinePaymentVerifyRequestParams;
import com.unionpay.tsmservice.request.OpenChannelRequestParams;
import com.unionpay.tsmservice.request.PreDownloadRequestParams;
import com.unionpay.tsmservice.request.QueryVendorPayStatusRequestParams;
import com.unionpay.tsmservice.request.SafetyKeyboardRequestParams;
import com.unionpay.tsmservice.request.SendApduRequestParams;
import com.unionpay.tsmservice.request.SetDefaultCardRequestParams;
import com.unionpay.tsmservice.request.SetSamsungDefWalletRequestParams;
import com.unionpay.tsmservice.request.StartCardApplyRequestParams;

public interface ITsmService extends IInterface {

    public static abstract class Stub extends Binder implements ITsmService {

        private static class a implements ITsmService {
            private IBinder a;

            a(IBinder iBinder) {
                this.a = iBinder;
            }

            public final int acquireSEAppList(AcquireSEAppListRequestParams acquireSEAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (acquireSEAppListRequestParams != null) {
                        obtain.writeInt(1);
                        acquireSEAppListRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(51, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int activateVendorPay(ActivateVendorPayRequestParams activateVendorPayRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (activateVendorPayRequestParams != null) {
                        obtain.writeInt(1);
                        activateVendorPayRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(44, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int addCardToVendorPay(AddCardToVendorPayRequestParams addCardToVendorPayRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (addCardToVendorPayRequestParams != null) {
                        obtain.writeInt(1);
                        addCardToVendorPayRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    if (iTsmProgressCallback != null) {
                        iBinder = iTsmProgressCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.a.transact(45, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int appDataUpdate(AppDataUpdateRequestParams appDataUpdateRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (appDataUpdateRequestParams != null) {
                        obtain.writeInt(1);
                        appDataUpdateRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    if (iTsmProgressCallback != null) {
                        iBinder = iTsmProgressCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.a.transact(15, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int appDelete(AppDeleteRequestParams appDeleteRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (appDeleteRequestParams != null) {
                        obtain.writeInt(1);
                        appDeleteRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    if (iTsmProgressCallback != null) {
                        iBinder = iTsmProgressCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.a.transact(14, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int appDownload(AppDownloadRequestParams appDownloadRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (appDownloadRequestParams != null) {
                        obtain.writeInt(1);
                        appDownloadRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    if (iTsmProgressCallback != null) {
                        iBinder = iTsmProgressCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.a.transact(13, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int appDownloadApply(AppDownloadApplyRequestParams appDownloadApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (appDownloadApplyRequestParams != null) {
                        obtain.writeInt(1);
                        appDownloadApplyRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(12, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int appLock(AppLockRequestParams appLockRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (appLockRequestParams != null) {
                        obtain.writeInt(1);
                        appLockRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(16, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int appUnlock(AppUnlockRequestParams appUnlockRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (appUnlockRequestParams != null) {
                        obtain.writeInt(1);
                        appUnlockRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(17, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final IBinder asBinder() {
                return this.a;
            }

            public final int cardListStatusChanged(CardListStatusChangedRequestParams cardListStatusChangedRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (cardListStatusChangedRequestParams != null) {
                        obtain.writeInt(1);
                        cardListStatusChangedRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(42, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int checkSSamsungPay(CheckSSamsungPayRequestParams checkSSamsungPayRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (checkSSamsungPayRequestParams != null) {
                        obtain.writeInt(1);
                        checkSSamsungPayRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(32, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int checkSupportCardApply(CheckSupportCardApplyRequestParams checkSupportCardApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (checkSupportCardApplyRequestParams != null) {
                        obtain.writeInt(1);
                        checkSupportCardApplyRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(39, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int clearEncryptData(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    obtain.writeInt(i);
                    this.a.transact(37, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int clearKeyboardEncryptData(ClearEncryptDataRequestParams clearEncryptDataRequestParams, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (clearEncryptDataRequestParams != null) {
                        obtain.writeInt(1);
                        clearEncryptDataRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    this.a.transact(47, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int closeChannel(CloseChannelRequestParams closeChannelRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (closeChannelRequestParams != null) {
                        obtain.writeInt(1);
                        closeChannelRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(28, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int eCashTopUp(ECashTopUpRequestParams eCashTopUpRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (eCashTopUpRequestParams != null) {
                        obtain.writeInt(1);
                        eCashTopUpRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(19, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int encryptData(EncryptDataRequestParams encryptDataRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (encryptDataRequestParams != null) {
                        obtain.writeInt(1);
                        encryptDataRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int exchangeKey(String str, String[] strArr) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    obtain.writeString(str);
                    if (strArr == null) {
                        obtain.writeInt(-1);
                    } else {
                        obtain.writeInt(strArr.length);
                    }
                    this.a.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readStringArray(strArr);
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int executeCmd(ExecuteCmdRequestParams executeCmdRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (executeCmdRequestParams != null) {
                        obtain.writeInt(1);
                        executeCmdRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    if (iTsmProgressCallback != null) {
                        iBinder = iTsmProgressCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.a.transact(30, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getAccountBalance(GetAccountBalanceRequestParams getAccountBalanceRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getAccountBalanceRequestParams != null) {
                        obtain.writeInt(1);
                        getAccountBalanceRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(22, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getAccountInfo(GetAccountInfoRequestParams getAccountInfoRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getAccountInfoRequestParams != null) {
                        obtain.writeInt(1);
                        getAccountInfoRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(21, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getAppDetail(GetAppDetailRequestParams getAppDetailRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getAppDetailRequestParams != null) {
                        obtain.writeInt(1);
                        getAppDetailRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(10, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getAppList(GetAppListRequestParams getAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getAppListRequestParams != null) {
                        obtain.writeInt(1);
                        getAppListRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getAppStatus(GetAppStatusRequestParams getAppStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getAppStatusRequestParams != null) {
                        obtain.writeInt(1);
                        getAppStatusRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getAssociatedApp(GetAssociatedAppRequestParams getAssociatedAppRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getAssociatedAppRequestParams != null) {
                        obtain.writeInt(1);
                        getAssociatedAppRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getCardInfo(GetCardInfoRequestParams getCardInfoRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getCardInfoRequestParams != null) {
                        obtain.writeInt(1);
                        getCardInfoRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(23, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getCardInfoBySamsungPay(GetCardInfoBySpayRequestParams getCardInfoBySpayRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getCardInfoBySpayRequestParams != null) {
                        obtain.writeInt(1);
                        getCardInfoBySpayRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(31, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getCurrentWalletClient(GetCurrentWalletClientRequestParams getCurrentWalletClientRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getCurrentWalletClientRequestParams != null) {
                        obtain.writeInt(1);
                        getCurrentWalletClientRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(41, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getDefaultCard(GetDefaultCardRequestParams getDefaultCardRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getDefaultCardRequestParams != null) {
                        obtain.writeInt(1);
                        getDefaultCardRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(25, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getEncryptData(GetEncryptDataRequestParams getEncryptDataRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getEncryptDataRequestParams != null) {
                        obtain.writeInt(1);
                        getEncryptDataRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(36, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getMessageDetails(GetMessageDetailsRequestParams getMessageDetailsRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getMessageDetailsRequestParams != null) {
                        obtain.writeInt(1);
                        getMessageDetailsRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(53, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getPubKey(int i, String[] strArr) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    obtain.writeInt(i);
                    if (strArr == null) {
                        obtain.writeInt(-1);
                    } else {
                        obtain.writeInt(strArr.length);
                    }
                    this.a.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    obtain2.readStringArray(strArr);
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getSEAppList(GetSeAppListRequestParams getSeAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getSeAppListRequestParams != null) {
                        obtain.writeInt(1);
                        getSeAppListRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getSEId(GetSeIdRequestParams getSeIdRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getSeIdRequestParams != null) {
                        obtain.writeInt(1);
                        getSeIdRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getSMSAuthCode(GetSMSAuthCodeRequestParams getSMSAuthCodeRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getSMSAuthCodeRequestParams != null) {
                        obtain.writeInt(1);
                        getSMSAuthCodeRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(18, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getTransElements(GetTransElementsRequestParams getTransElementsRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getTransElementsRequestParams != null) {
                        obtain.writeInt(1);
                        getTransElementsRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(11, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getTransRecord(GetTransRecordRequestParams getTransRecordRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getTransRecordRequestParams != null) {
                        obtain.writeInt(1);
                        getTransRecordRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(20, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getTransactionDetails(GetTransactionDetailsRequestParams getTransactionDetailsRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getTransactionDetailsRequestParams != null) {
                        obtain.writeInt(1);
                        getTransactionDetailsRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(52, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int getVendorPayStatus(GetVendorPayStatusRequestParams getVendorPayStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (getVendorPayStatusRequestParams != null) {
                        obtain.writeInt(1);
                        getVendorPayStatusRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(43, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int hideAppApply(HideAppApplyRequestParams hideAppApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (hideAppApplyRequestParams != null) {
                        obtain.writeInt(1);
                        hideAppApplyRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(29, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int hideKeyboard() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    this.a.transact(38, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int hideSafetyKeyboard(HideSafetyKeyboardRequestParams hideSafetyKeyboardRequestParams) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (hideSafetyKeyboardRequestParams != null) {
                        obtain.writeInt(1);
                        hideSafetyKeyboardRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.a.transact(48, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int init(InitRequestParams initRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (initRequestParams != null) {
                        obtain.writeInt(1);
                        initRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int onlinePaymentVerify(OnlinePaymentVerifyRequestParams onlinePaymentVerifyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (onlinePaymentVerifyRequestParams != null) {
                        obtain.writeInt(1);
                        onlinePaymentVerifyRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(46, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int openChannel(OpenChannelRequestParams openChannelRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (openChannelRequestParams != null) {
                        obtain.writeInt(1);
                        openChannelRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(26, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int preDownload(PreDownloadRequestParams preDownloadRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (preDownloadRequestParams != null) {
                        obtain.writeInt(1);
                        preDownloadRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    if (iTsmProgressCallback != null) {
                        iBinder = iTsmProgressCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.a.transact(49, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int queryVendorPayStatus(QueryVendorPayStatusRequestParams queryVendorPayStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (queryVendorPayStatusRequestParams != null) {
                        obtain.writeInt(1);
                        queryVendorPayStatusRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(50, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int sendApdu(SendApduRequestParams sendApduRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (sendApduRequestParams != null) {
                        obtain.writeInt(1);
                        sendApduRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(27, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int setDefaultCard(SetDefaultCardRequestParams setDefaultCardRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (setDefaultCardRequestParams != null) {
                        obtain.writeInt(1);
                        setDefaultCardRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(24, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int setSafetyKeyboardBitmap(SafetyKeyboardRequestParams safetyKeyboardRequestParams) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (safetyKeyboardRequestParams != null) {
                        obtain.writeInt(1);
                        safetyKeyboardRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.a.transact(35, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int setSamsungDefaultWallet(SetSamsungDefWalletRequestParams setSamsungDefWalletRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (setSamsungDefWalletRequestParams != null) {
                        obtain.writeInt(1);
                        setSamsungDefWalletRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(33, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int showSafetyKeyboard(SafetyKeyboardRequestParams safetyKeyboardRequestParams, int i, OnSafetyKeyboardCallback onSafetyKeyboardCallback, ITsmActivityCallback iTsmActivityCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (safetyKeyboardRequestParams != null) {
                        obtain.writeInt(1);
                        safetyKeyboardRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(onSafetyKeyboardCallback != null ? onSafetyKeyboardCallback.asBinder() : null);
                    if (iTsmActivityCallback != null) {
                        iBinder = iTsmActivityCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.a.transact(34, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public final int startCardApply(StartCardApplyRequestParams startCardApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.unionpay.tsmservice.ITsmService");
                    if (startCardApplyRequestParams != null) {
                        obtain.writeInt(1);
                        startCardApplyRequestParams.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iTsmCallback != null ? iTsmCallback.asBinder() : null);
                    this.a.transact(40, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.unionpay.tsmservice.ITsmService");
        }

        public static ITsmService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.unionpay.tsmservice.ITsmService");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof ITsmService)) ? new a(iBinder) : (ITsmService) queryLocalInterface;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.unionpay.tsmservice.request.InitRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: java.lang.String[]} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: java.lang.String[]} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v9, resolved type: com.unionpay.tsmservice.request.EncryptDataRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v12, resolved type: com.unionpay.tsmservice.request.GetSeIdRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: com.unionpay.tsmservice.request.GetAssociatedAppRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v18, resolved type: com.unionpay.tsmservice.request.GetSeAppListRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v21, resolved type: com.unionpay.tsmservice.request.GetAppListRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: com.unionpay.tsmservice.request.GetAppStatusRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v27, resolved type: com.unionpay.tsmservice.request.GetAppDetailRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v30, resolved type: com.unionpay.tsmservice.request.GetTransElementsRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v33, resolved type: com.unionpay.tsmservice.request.AppDownloadApplyRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v36, resolved type: com.unionpay.tsmservice.request.AppDownloadRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v39, resolved type: com.unionpay.tsmservice.request.AppDeleteRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v42, resolved type: com.unionpay.tsmservice.request.AppDataUpdateRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v45, resolved type: com.unionpay.tsmservice.request.AppLockRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: com.unionpay.tsmservice.request.AppUnlockRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v51, resolved type: com.unionpay.tsmservice.request.GetSMSAuthCodeRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v54, resolved type: com.unionpay.tsmservice.request.ECashTopUpRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v57, resolved type: com.unionpay.tsmservice.request.GetTransRecordRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v60, resolved type: com.unionpay.tsmservice.request.GetAccountInfoRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v63, resolved type: com.unionpay.tsmservice.request.GetAccountBalanceRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v66, resolved type: com.unionpay.tsmservice.request.GetCardInfoRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v69, resolved type: com.unionpay.tsmservice.request.SetDefaultCardRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v72, resolved type: com.unionpay.tsmservice.request.GetDefaultCardRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v75, resolved type: com.unionpay.tsmservice.request.OpenChannelRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v78, resolved type: com.unionpay.tsmservice.request.SendApduRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v81, resolved type: com.unionpay.tsmservice.request.CloseChannelRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v84, resolved type: com.unionpay.tsmservice.request.HideAppApplyRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v87, resolved type: com.unionpay.tsmservice.request.ExecuteCmdRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v90, resolved type: com.unionpay.tsmservice.request.GetCardInfoBySpayRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v93, resolved type: com.unionpay.tsmservice.request.CheckSSamsungPayRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v96, resolved type: com.unionpay.tsmservice.request.SetSamsungDefWalletRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v99, resolved type: com.unionpay.tsmservice.request.SafetyKeyboardRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v102, resolved type: com.unionpay.tsmservice.request.SafetyKeyboardRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v105, resolved type: com.unionpay.tsmservice.request.GetEncryptDataRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v108, resolved type: com.unionpay.tsmservice.request.CheckSupportCardApplyRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v111, resolved type: com.unionpay.tsmservice.request.StartCardApplyRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v114, resolved type: com.unionpay.tsmservice.request.GetCurrentWalletClientRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v117, resolved type: com.unionpay.tsmservice.request.CardListStatusChangedRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v120, resolved type: com.unionpay.tsmservice.request.GetVendorPayStatusRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v123, resolved type: com.unionpay.tsmservice.request.ActivateVendorPayRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v126, resolved type: com.unionpay.tsmservice.request.AddCardToVendorPayRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v129, resolved type: com.unionpay.tsmservice.request.OnlinePaymentVerifyRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v132, resolved type: com.unionpay.tsmservice.request.ClearEncryptDataRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v135, resolved type: com.unionpay.tsmservice.request.HideSafetyKeyboardRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v138, resolved type: com.unionpay.tsmservice.request.PreDownloadRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v141, resolved type: com.unionpay.tsmservice.request.QueryVendorPayStatusRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v144, resolved type: com.unionpay.tsmservice.request.AcquireSEAppListRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v147, resolved type: com.unionpay.tsmservice.request.GetTransactionDetailsRequestParams} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v150, resolved type: com.unionpay.tsmservice.request.GetMessageDetailsRequestParams} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v153 */
        /* JADX WARNING: type inference failed for: r0v154 */
        /* JADX WARNING: type inference failed for: r0v155 */
        /* JADX WARNING: type inference failed for: r0v156 */
        /* JADX WARNING: type inference failed for: r0v157 */
        /* JADX WARNING: type inference failed for: r0v158 */
        /* JADX WARNING: type inference failed for: r0v159 */
        /* JADX WARNING: type inference failed for: r0v160 */
        /* JADX WARNING: type inference failed for: r0v161 */
        /* JADX WARNING: type inference failed for: r0v162 */
        /* JADX WARNING: type inference failed for: r0v163 */
        /* JADX WARNING: type inference failed for: r0v164 */
        /* JADX WARNING: type inference failed for: r0v165 */
        /* JADX WARNING: type inference failed for: r0v166 */
        /* JADX WARNING: type inference failed for: r0v167 */
        /* JADX WARNING: type inference failed for: r0v168 */
        /* JADX WARNING: type inference failed for: r0v169 */
        /* JADX WARNING: type inference failed for: r0v170 */
        /* JADX WARNING: type inference failed for: r0v171 */
        /* JADX WARNING: type inference failed for: r0v172 */
        /* JADX WARNING: type inference failed for: r0v173 */
        /* JADX WARNING: type inference failed for: r0v174 */
        /* JADX WARNING: type inference failed for: r0v175 */
        /* JADX WARNING: type inference failed for: r0v176 */
        /* JADX WARNING: type inference failed for: r0v177 */
        /* JADX WARNING: type inference failed for: r0v178 */
        /* JADX WARNING: type inference failed for: r0v179 */
        /* JADX WARNING: type inference failed for: r0v180 */
        /* JADX WARNING: type inference failed for: r0v181 */
        /* JADX WARNING: type inference failed for: r0v182 */
        /* JADX WARNING: type inference failed for: r0v183 */
        /* JADX WARNING: type inference failed for: r0v184 */
        /* JADX WARNING: type inference failed for: r0v185 */
        /* JADX WARNING: type inference failed for: r0v186 */
        /* JADX WARNING: type inference failed for: r0v187 */
        /* JADX WARNING: type inference failed for: r0v188 */
        /* JADX WARNING: type inference failed for: r0v189 */
        /* JADX WARNING: type inference failed for: r0v190 */
        /* JADX WARNING: type inference failed for: r0v191 */
        /* JADX WARNING: type inference failed for: r0v192 */
        /* JADX WARNING: type inference failed for: r0v193 */
        /* JADX WARNING: type inference failed for: r0v194 */
        /* JADX WARNING: type inference failed for: r0v195 */
        /* JADX WARNING: type inference failed for: r0v196 */
        /* JADX WARNING: type inference failed for: r0v197 */
        /* JADX WARNING: type inference failed for: r0v198 */
        /* JADX WARNING: type inference failed for: r0v199 */
        /* JADX WARNING: type inference failed for: r0v200 */
        /* JADX WARNING: type inference failed for: r0v201 */
        /* JADX WARNING: type inference failed for: r0v202 */
        /* JADX WARNING: type inference failed for: r0v203 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                ? r0 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = InitRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int init = init(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(init);
                        return true;
                    case 2:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        int readInt = parcel.readInt();
                        int readInt2 = parcel.readInt();
                        if (readInt2 >= 0) {
                            r0 = new String[readInt2];
                        }
                        int pubKey = getPubKey(readInt, r0);
                        parcel2.writeNoException();
                        parcel2.writeInt(pubKey);
                        parcel2.writeStringArray(r0);
                        return true;
                    case 3:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        String readString = parcel.readString();
                        int readInt3 = parcel.readInt();
                        if (readInt3 >= 0) {
                            r0 = new String[readInt3];
                        }
                        int exchangeKey = exchangeKey(readString, r0);
                        parcel2.writeNoException();
                        parcel2.writeInt(exchangeKey);
                        parcel2.writeStringArray(r0);
                        return true;
                    case 4:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = EncryptDataRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int encryptData = encryptData(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(encryptData);
                        return true;
                    case 5:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetSeIdRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int sEId = getSEId(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(sEId);
                        return true;
                    case 6:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetAssociatedAppRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int associatedApp = getAssociatedApp(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(associatedApp);
                        return true;
                    case 7:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetSeAppListRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int sEAppList = getSEAppList(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(sEAppList);
                        return true;
                    case 8:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetAppListRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appList = getAppList(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appList);
                        return true;
                    case 9:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetAppStatusRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appStatus = getAppStatus(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appStatus);
                        return true;
                    case 10:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetAppDetailRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appDetail = getAppDetail(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appDetail);
                        return true;
                    case 11:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetTransElementsRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int transElements = getTransElements(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(transElements);
                        return true;
                    case 12:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AppDownloadApplyRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appDownloadApply = appDownloadApply(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appDownloadApply);
                        return true;
                    case 13:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AppDownloadRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appDownload = appDownload(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()), ITsmProgressCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appDownload);
                        return true;
                    case 14:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AppDeleteRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appDelete = appDelete(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()), ITsmProgressCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appDelete);
                        return true;
                    case 15:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AppDataUpdateRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appDataUpdate = appDataUpdate(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()), ITsmProgressCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appDataUpdate);
                        return true;
                    case 16:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AppLockRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appLock = appLock(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appLock);
                        return true;
                    case 17:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AppUnlockRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int appUnlock = appUnlock(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(appUnlock);
                        return true;
                    case 18:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetSMSAuthCodeRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int sMSAuthCode = getSMSAuthCode(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(sMSAuthCode);
                        return true;
                    case 19:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = ECashTopUpRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int eCashTopUp = eCashTopUp(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(eCashTopUp);
                        return true;
                    case 20:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetTransRecordRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int transRecord = getTransRecord(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(transRecord);
                        return true;
                    case 21:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetAccountInfoRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int accountInfo = getAccountInfo(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(accountInfo);
                        return true;
                    case 22:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetAccountBalanceRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int accountBalance = getAccountBalance(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(accountBalance);
                        return true;
                    case 23:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetCardInfoRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int cardInfo = getCardInfo(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(cardInfo);
                        return true;
                    case 24:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = SetDefaultCardRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int defaultCard = setDefaultCard(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(defaultCard);
                        return true;
                    case 25:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetDefaultCardRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int defaultCard2 = getDefaultCard(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(defaultCard2);
                        return true;
                    case 26:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = OpenChannelRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int openChannel = openChannel(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(openChannel);
                        return true;
                    case 27:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = SendApduRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int sendApdu = sendApdu(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(sendApdu);
                        return true;
                    case 28:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = CloseChannelRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int closeChannel = closeChannel(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(closeChannel);
                        return true;
                    case Constant.INTERFACE_CHECK_SSAMSUNGPAY:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = HideAppApplyRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int hideAppApply = hideAppApply(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(hideAppApply);
                        return true;
                    case 30:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = ExecuteCmdRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int executeCmd = executeCmd(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()), ITsmProgressCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(executeCmd);
                        return true;
                    case Constant.INTERFACE_GET_ENCRYPT_DATA:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetCardInfoBySpayRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int cardInfoBySamsungPay = getCardInfoBySamsungPay(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(cardInfoBySamsungPay);
                        return true;
                    case 32:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = CheckSSamsungPayRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int checkSSamsungPay = checkSSamsungPay(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(checkSSamsungPay);
                        return true;
                    case Constant.INTERFACE_CLEAR_ENCRYPTDATA:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = SetSamsungDefWalletRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int samsungDefaultWallet = setSamsungDefaultWallet(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(samsungDefaultWallet);
                        return true;
                    case Constant.INTERFACE_HIDE_KEYBOARD:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = SafetyKeyboardRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int showSafetyKeyboard = showSafetyKeyboard(r0, parcel.readInt(), OnSafetyKeyboardCallback.Stub.asInterface(parcel.readStrongBinder()), ITsmActivityCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(showSafetyKeyboard);
                        return true;
                    case Constant.INTERFACE_CARDLIST_STATUS_CHANGED:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = SafetyKeyboardRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int safetyKeyboardBitmap = setSafetyKeyboardBitmap(r0);
                        parcel2.writeNoException();
                        parcel2.writeInt(safetyKeyboardBitmap);
                        return true;
                    case 36:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetEncryptDataRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int encryptData2 = getEncryptData(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(encryptData2);
                        return true;
                    case Constant.INTERFACE_ACTIVATE_VENDOR_PAY:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        int clearEncryptData = clearEncryptData(parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(clearEncryptData);
                        return true;
                    case Constant.INTERFACE_ADD_CARD_TO_VENDOR_PAY:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        int hideKeyboard = hideKeyboard();
                        parcel2.writeNoException();
                        parcel2.writeInt(hideKeyboard);
                        return true;
                    case Constant.INTERFACE_ONLINE_PAYMENT_VERIFY:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = CheckSupportCardApplyRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int checkSupportCardApply = checkSupportCardApply(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(checkSupportCardApply);
                        return true;
                    case 40:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = StartCardApplyRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int startCardApply = startCardApply(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(startCardApply);
                        return true;
                    case Constant.INTERFACE_QUERY_VENDOR_PAY_STATUS:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetCurrentWalletClientRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int currentWalletClient = getCurrentWalletClient(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(currentWalletClient);
                        return true;
                    case Constant.INTERFACE_ACQUIRE_SE_APP_LIST:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = CardListStatusChangedRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int cardListStatusChanged = cardListStatusChanged(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(cardListStatusChanged);
                        return true;
                    case Constant.INTERFACE_GET_TRANSACTION_DETAILS:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetVendorPayStatusRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int vendorPayStatus = getVendorPayStatus(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(vendorPayStatus);
                        return true;
                    case Constant.INTERFACE_GET_MESSAGE_DETAILS:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = ActivateVendorPayRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int activateVendorPay = activateVendorPay(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(activateVendorPay);
                        return true;
                    case Constant.NUM_TSM_INTERFACE:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AddCardToVendorPayRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int addCardToVendorPay = addCardToVendorPay(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()), ITsmProgressCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(addCardToVendorPay);
                        return true;
                    case 46:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = OnlinePaymentVerifyRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int onlinePaymentVerify = onlinePaymentVerify(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(onlinePaymentVerify);
                        return true;
                    case 47:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = ClearEncryptDataRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int clearKeyboardEncryptData = clearKeyboardEncryptData(r0, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(clearKeyboardEncryptData);
                        return true;
                    case 48:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = HideSafetyKeyboardRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int hideSafetyKeyboard = hideSafetyKeyboard(r0);
                        parcel2.writeNoException();
                        parcel2.writeInt(hideSafetyKeyboard);
                        return true;
                    case 49:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = PreDownloadRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int preDownload = preDownload(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()), ITsmProgressCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(preDownload);
                        return true;
                    case 50:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = QueryVendorPayStatusRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int queryVendorPayStatus = queryVendorPayStatus(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(queryVendorPayStatus);
                        return true;
                    case 51:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = AcquireSEAppListRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int acquireSEAppList = acquireSEAppList(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(acquireSEAppList);
                        return true;
                    case 52:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetTransactionDetailsRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int transactionDetails = getTransactionDetails(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(transactionDetails);
                        return true;
                    case 53:
                        parcel.enforceInterface("com.unionpay.tsmservice.ITsmService");
                        if (parcel.readInt() != 0) {
                            r0 = GetMessageDetailsRequestParams.CREATOR.createFromParcel(parcel);
                        }
                        int messageDetails = getMessageDetails(r0, ITsmCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(messageDetails);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString("com.unionpay.tsmservice.ITsmService");
                return true;
            }
        }
    }

    int acquireSEAppList(AcquireSEAppListRequestParams acquireSEAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int activateVendorPay(ActivateVendorPayRequestParams activateVendorPayRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int addCardToVendorPay(AddCardToVendorPayRequestParams addCardToVendorPayRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException;

    int appDataUpdate(AppDataUpdateRequestParams appDataUpdateRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException;

    int appDelete(AppDeleteRequestParams appDeleteRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException;

    int appDownload(AppDownloadRequestParams appDownloadRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException;

    int appDownloadApply(AppDownloadApplyRequestParams appDownloadApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int appLock(AppLockRequestParams appLockRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int appUnlock(AppUnlockRequestParams appUnlockRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int cardListStatusChanged(CardListStatusChangedRequestParams cardListStatusChangedRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int checkSSamsungPay(CheckSSamsungPayRequestParams checkSSamsungPayRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int checkSupportCardApply(CheckSupportCardApplyRequestParams checkSupportCardApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int clearEncryptData(int i) throws RemoteException;

    int clearKeyboardEncryptData(ClearEncryptDataRequestParams clearEncryptDataRequestParams, int i) throws RemoteException;

    int closeChannel(CloseChannelRequestParams closeChannelRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int eCashTopUp(ECashTopUpRequestParams eCashTopUpRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int encryptData(EncryptDataRequestParams encryptDataRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int exchangeKey(String str, String[] strArr) throws RemoteException;

    int executeCmd(ExecuteCmdRequestParams executeCmdRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException;

    int getAccountBalance(GetAccountBalanceRequestParams getAccountBalanceRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getAccountInfo(GetAccountInfoRequestParams getAccountInfoRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getAppDetail(GetAppDetailRequestParams getAppDetailRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getAppList(GetAppListRequestParams getAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getAppStatus(GetAppStatusRequestParams getAppStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getAssociatedApp(GetAssociatedAppRequestParams getAssociatedAppRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getCardInfo(GetCardInfoRequestParams getCardInfoRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getCardInfoBySamsungPay(GetCardInfoBySpayRequestParams getCardInfoBySpayRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getCurrentWalletClient(GetCurrentWalletClientRequestParams getCurrentWalletClientRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getDefaultCard(GetDefaultCardRequestParams getDefaultCardRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getEncryptData(GetEncryptDataRequestParams getEncryptDataRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getMessageDetails(GetMessageDetailsRequestParams getMessageDetailsRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getPubKey(int i, String[] strArr) throws RemoteException;

    int getSEAppList(GetSeAppListRequestParams getSeAppListRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getSEId(GetSeIdRequestParams getSeIdRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getSMSAuthCode(GetSMSAuthCodeRequestParams getSMSAuthCodeRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getTransElements(GetTransElementsRequestParams getTransElementsRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getTransRecord(GetTransRecordRequestParams getTransRecordRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getTransactionDetails(GetTransactionDetailsRequestParams getTransactionDetailsRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int getVendorPayStatus(GetVendorPayStatusRequestParams getVendorPayStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int hideAppApply(HideAppApplyRequestParams hideAppApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int hideKeyboard() throws RemoteException;

    int hideSafetyKeyboard(HideSafetyKeyboardRequestParams hideSafetyKeyboardRequestParams) throws RemoteException;

    int init(InitRequestParams initRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int onlinePaymentVerify(OnlinePaymentVerifyRequestParams onlinePaymentVerifyRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int openChannel(OpenChannelRequestParams openChannelRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int preDownload(PreDownloadRequestParams preDownloadRequestParams, ITsmCallback iTsmCallback, ITsmProgressCallback iTsmProgressCallback) throws RemoteException;

    int queryVendorPayStatus(QueryVendorPayStatusRequestParams queryVendorPayStatusRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int sendApdu(SendApduRequestParams sendApduRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int setDefaultCard(SetDefaultCardRequestParams setDefaultCardRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int setSafetyKeyboardBitmap(SafetyKeyboardRequestParams safetyKeyboardRequestParams) throws RemoteException;

    int setSamsungDefaultWallet(SetSamsungDefWalletRequestParams setSamsungDefWalletRequestParams, ITsmCallback iTsmCallback) throws RemoteException;

    int showSafetyKeyboard(SafetyKeyboardRequestParams safetyKeyboardRequestParams, int i, OnSafetyKeyboardCallback onSafetyKeyboardCallback, ITsmActivityCallback iTsmActivityCallback) throws RemoteException;

    int startCardApply(StartCardApplyRequestParams startCardApplyRequestParams, ITsmCallback iTsmCallback) throws RemoteException;
}
