package com.android.internal.telephony;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.IFinancialSmsCallback;
import java.util.List;

public interface ISms extends IInterface {
    int checkSmsShortCodeDestination(int i, String str, String str2, String str3) throws RemoteException;

    boolean copyMessageToIccEfForSubscriber(int i, String str, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    String createAppSpecificSmsToken(int i, String str, PendingIntent pendingIntent) throws RemoteException;

    String createAppSpecificSmsTokenWithPackageInfo(int i, String str, String str2, PendingIntent pendingIntent) throws RemoteException;

    boolean disableCellBroadcastForSubscriber(int i, int i2, int i3) throws RemoteException;

    boolean disableCellBroadcastRangeForSubscriber(int i, int i2, int i3, int i4) throws RemoteException;

    boolean enableCellBroadcastForSubscriber(int i, int i2, int i3) throws RemoteException;

    boolean enableCellBroadcastRangeForSubscriber(int i, int i2, int i3, int i4) throws RemoteException;

    List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int i, String str) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    String getImsSmsFormatForSubscriber(int i) throws RemoteException;

    int getPreferredSmsSubscription() throws RemoteException;

    int getPremiumSmsPermission(String str) throws RemoteException;

    int getPremiumSmsPermissionForSubscriber(int i, String str) throws RemoteException;

    void getSmsMessagesForFinancialApp(int i, String str, Bundle bundle, IFinancialSmsCallback iFinancialSmsCallback) throws RemoteException;

    void injectSmsPduForSubscriber(int i, byte[] bArr, String str, PendingIntent pendingIntent) throws RemoteException;

    boolean isImsSmsSupportedForSubscriber(int i) throws RemoteException;

    boolean isSMSPromptEnabled() throws RemoteException;

    boolean isSmsSimPickActivityNeeded(int i) throws RemoteException;

    void sendDataForSubscriber(int i, String str, String str2, String str3, int i2, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void sendDataForSubscriberWithSelfPermissions(int i, String str, String str2, String str3, int i2, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void sendMultipartTextForSubscriber(int i, String str, String str2, String str3, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean z) throws RemoteException;

    void sendMultipartTextForSubscriberWithOptions(int i, String str, String str2, String str3, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean z, int i2, boolean z2, int i3) throws RemoteException;

    void sendStoredMultipartText(int i, String str, Uri uri, String str2, List<PendingIntent> list, List<PendingIntent> list2) throws RemoteException;

    void sendStoredText(int i, String str, Uri uri, String str2, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void sendTextForSubscriber(int i, String str, String str2, String str3, String str4, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z) throws RemoteException;

    void sendTextForSubscriberWithOptions(int i, String str, String str2, String str3, String str4, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z, int i2, boolean z2, int i3) throws RemoteException;

    void sendTextForSubscriberWithSelfPermissions(int i, String str, String str2, String str3, String str4, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z) throws RemoteException;

    void setPremiumSmsPermission(String str, int i) throws RemoteException;

    void setPremiumSmsPermissionForSubscriber(int i, String str, int i2) throws RemoteException;

    boolean updateMessageOnIccEfForSubscriber(int i, String str, int i2, int i3, byte[] bArr) throws RemoteException;

    public static class Default implements ISms {
        @Override // com.android.internal.telephony.ISms
        public List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int subId, String callingPkg) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ISms
        public boolean updateMessageOnIccEfForSubscriber(int subId, String callingPkg, int messageIndex, int newStatus, byte[] pdu) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public boolean copyMessageToIccEfForSubscriber(int subId, String callingPkg, int status, byte[] pdu, byte[] smsc) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public void sendDataForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void sendDataForSubscriberWithSelfPermissions(int subId, String callingPkg, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void sendTextForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void sendTextForSubscriberWithOptions(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void injectSmsPduForSubscriber(int subId, byte[] pdu, String format, PendingIntent receivedIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void sendMultipartTextForSubscriber(int subId, String callingPkg, String destinationAddress, String scAddress, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void sendMultipartTextForSubscriberWithOptions(int subId, String callingPkg, String destinationAddress, String scAddress, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public boolean enableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public boolean disableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public boolean enableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public boolean disableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public int getPremiumSmsPermission(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ISms
        public int getPremiumSmsPermissionForSubscriber(int subId, String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ISms
        public void setPremiumSmsPermission(String packageName, int permission) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void setPremiumSmsPermissionForSubscriber(int subId, String packageName, int permission) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public boolean isImsSmsSupportedForSubscriber(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public boolean isSmsSimPickActivityNeeded(int subId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public int getPreferredSmsSubscription() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.ISms
        public String getImsSmsFormatForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ISms
        public boolean isSMSPromptEnabled() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.ISms
        public void sendStoredText(int subId, String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public void sendStoredMultipartText(int subId, String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> list, List<PendingIntent> list2) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public String createAppSpecificSmsToken(int subId, String callingPkg, PendingIntent intent) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ISms
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ISms
        public String createAppSpecificSmsTokenWithPackageInfo(int subId, String callingPkg, String prefixes, PendingIntent intent) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.ISms
        public void getSmsMessagesForFinancialApp(int subId, String callingPkg, Bundle params, IFinancialSmsCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.telephony.ISms
        public int checkSmsShortCodeDestination(int subId, String callingApk, String destAddress, String countryIso) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISms {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ISms";
        static final int TRANSACTION_checkSmsShortCodeDestination = 31;
        static final int TRANSACTION_copyMessageToIccEfForSubscriber = 3;
        static final int TRANSACTION_createAppSpecificSmsToken = 27;
        static final int TRANSACTION_createAppSpecificSmsTokenWithPackageInfo = 29;
        static final int TRANSACTION_disableCellBroadcastForSubscriber = 13;
        static final int TRANSACTION_disableCellBroadcastRangeForSubscriber = 15;
        static final int TRANSACTION_enableCellBroadcastForSubscriber = 12;
        static final int TRANSACTION_enableCellBroadcastRangeForSubscriber = 14;
        static final int TRANSACTION_getAllMessagesFromIccEfForSubscriber = 1;
        static final int TRANSACTION_getHwInnerService = 28;
        static final int TRANSACTION_getImsSmsFormatForSubscriber = 23;
        static final int TRANSACTION_getPreferredSmsSubscription = 22;
        static final int TRANSACTION_getPremiumSmsPermission = 16;
        static final int TRANSACTION_getPremiumSmsPermissionForSubscriber = 17;
        static final int TRANSACTION_getSmsMessagesForFinancialApp = 30;
        static final int TRANSACTION_injectSmsPduForSubscriber = 9;
        static final int TRANSACTION_isImsSmsSupportedForSubscriber = 20;
        static final int TRANSACTION_isSMSPromptEnabled = 24;
        static final int TRANSACTION_isSmsSimPickActivityNeeded = 21;
        static final int TRANSACTION_sendDataForSubscriber = 4;
        static final int TRANSACTION_sendDataForSubscriberWithSelfPermissions = 5;
        static final int TRANSACTION_sendMultipartTextForSubscriber = 10;
        static final int TRANSACTION_sendMultipartTextForSubscriberWithOptions = 11;
        static final int TRANSACTION_sendStoredMultipartText = 26;
        static final int TRANSACTION_sendStoredText = 25;
        static final int TRANSACTION_sendTextForSubscriber = 6;
        static final int TRANSACTION_sendTextForSubscriberWithOptions = 8;
        static final int TRANSACTION_sendTextForSubscriberWithSelfPermissions = 7;
        static final int TRANSACTION_setPremiumSmsPermission = 18;
        static final int TRANSACTION_setPremiumSmsPermissionForSubscriber = 19;
        static final int TRANSACTION_updateMessageOnIccEfForSubscriber = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISms asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISms)) {
                return new Proxy(obj);
            }
            return (ISms) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getAllMessagesFromIccEfForSubscriber";
                case 2:
                    return "updateMessageOnIccEfForSubscriber";
                case 3:
                    return "copyMessageToIccEfForSubscriber";
                case 4:
                    return "sendDataForSubscriber";
                case 5:
                    return "sendDataForSubscriberWithSelfPermissions";
                case 6:
                    return "sendTextForSubscriber";
                case 7:
                    return "sendTextForSubscriberWithSelfPermissions";
                case 8:
                    return "sendTextForSubscriberWithOptions";
                case 9:
                    return "injectSmsPduForSubscriber";
                case 10:
                    return "sendMultipartTextForSubscriber";
                case 11:
                    return "sendMultipartTextForSubscriberWithOptions";
                case 12:
                    return "enableCellBroadcastForSubscriber";
                case 13:
                    return "disableCellBroadcastForSubscriber";
                case 14:
                    return "enableCellBroadcastRangeForSubscriber";
                case 15:
                    return "disableCellBroadcastRangeForSubscriber";
                case 16:
                    return "getPremiumSmsPermission";
                case 17:
                    return "getPremiumSmsPermissionForSubscriber";
                case 18:
                    return "setPremiumSmsPermission";
                case 19:
                    return "setPremiumSmsPermissionForSubscriber";
                case 20:
                    return "isImsSmsSupportedForSubscriber";
                case 21:
                    return "isSmsSimPickActivityNeeded";
                case 22:
                    return "getPreferredSmsSubscription";
                case 23:
                    return "getImsSmsFormatForSubscriber";
                case 24:
                    return "isSMSPromptEnabled";
                case 25:
                    return "sendStoredText";
                case 26:
                    return "sendStoredMultipartText";
                case 27:
                    return "createAppSpecificSmsToken";
                case 28:
                    return "getHwInnerService";
                case 29:
                    return "createAppSpecificSmsTokenWithPackageInfo";
                case 30:
                    return "getSmsMessagesForFinancialApp";
                case 31:
                    return "checkSmsShortCodeDestination";
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
            PendingIntent _arg6;
            PendingIntent _arg7;
            PendingIntent _arg62;
            PendingIntent _arg72;
            PendingIntent _arg5;
            PendingIntent _arg63;
            PendingIntent _arg52;
            PendingIntent _arg64;
            PendingIntent _arg53;
            PendingIntent _arg65;
            PendingIntent _arg3;
            Uri _arg2;
            PendingIntent _arg4;
            PendingIntent _arg54;
            Uri _arg22;
            PendingIntent _arg23;
            PendingIntent _arg32;
            Bundle _arg24;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<SmsRawData> _result = getAllMessagesFromIccEfForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateMessageOnIccEfForSubscriber = updateMessageOnIccEfForSubscriber(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(updateMessageOnIccEfForSubscriber ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean copyMessageToIccEfForSubscriber = copyMessageToIccEfForSubscriber(data.readInt(), data.readString(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(copyMessageToIccEfForSubscriber ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg1 = data.readString();
                        String _arg25 = data.readString();
                        String _arg33 = data.readString();
                        int _arg42 = data.readInt();
                        byte[] _arg55 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg6 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg7 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg7 = null;
                        }
                        sendDataForSubscriber(_arg0, _arg1, _arg25, _arg33, _arg42, _arg55, _arg6, _arg7);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        String _arg12 = data.readString();
                        String _arg26 = data.readString();
                        String _arg34 = data.readString();
                        int _arg43 = data.readInt();
                        byte[] _arg56 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg62 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg62 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg72 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg72 = null;
                        }
                        sendDataForSubscriberWithSelfPermissions(_arg02, _arg12, _arg26, _arg34, _arg43, _arg56, _arg62, _arg72);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        String _arg13 = data.readString();
                        String _arg27 = data.readString();
                        String _arg35 = data.readString();
                        String _arg44 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg63 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg63 = null;
                        }
                        sendTextForSubscriber(_arg03, _arg13, _arg27, _arg35, _arg44, _arg5, _arg63, data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String _arg14 = data.readString();
                        String _arg28 = data.readString();
                        String _arg36 = data.readString();
                        String _arg45 = data.readString();
                        if (data.readInt() != 0) {
                            _arg52 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg52 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg64 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg64 = null;
                        }
                        sendTextForSubscriberWithSelfPermissions(_arg04, _arg14, _arg28, _arg36, _arg45, _arg52, _arg64, data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        String _arg15 = data.readString();
                        String _arg29 = data.readString();
                        String _arg37 = data.readString();
                        String _arg46 = data.readString();
                        if (data.readInt() != 0) {
                            _arg53 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg53 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg65 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg65 = null;
                        }
                        sendTextForSubscriberWithOptions(_arg05, _arg15, _arg29, _arg37, _arg46, _arg53, _arg65, data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        byte[] _arg16 = data.createByteArray();
                        String _arg210 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        injectSmsPduForSubscriber(_arg06, _arg16, _arg210, _arg3);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        sendMultipartTextForSubscriber(data.readInt(), data.readString(), data.readString(), data.readString(), data.createStringArrayList(), data.createTypedArrayList(PendingIntent.CREATOR), data.createTypedArrayList(PendingIntent.CREATOR), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        sendMultipartTextForSubscriberWithOptions(data.readInt(), data.readString(), data.readString(), data.readString(), data.createStringArrayList(), data.createTypedArrayList(PendingIntent.CREATOR), data.createTypedArrayList(PendingIntent.CREATOR), data.readInt() != 0, data.readInt(), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableCellBroadcastForSubscriber = enableCellBroadcastForSubscriber(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableCellBroadcastForSubscriber ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableCellBroadcastForSubscriber = disableCellBroadcastForSubscriber(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableCellBroadcastForSubscriber ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableCellBroadcastRangeForSubscriber = enableCellBroadcastRangeForSubscriber(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableCellBroadcastRangeForSubscriber ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableCellBroadcastRangeForSubscriber = disableCellBroadcastRangeForSubscriber(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableCellBroadcastRangeForSubscriber ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getPremiumSmsPermission(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getPremiumSmsPermissionForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        setPremiumSmsPermission(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        setPremiumSmsPermissionForSubscriber(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isImsSmsSupportedForSubscriber = isImsSmsSupportedForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isImsSmsSupportedForSubscriber ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSmsSimPickActivityNeeded = isSmsSimPickActivityNeeded(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isSmsSimPickActivityNeeded ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getPreferredSmsSubscription();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getImsSmsFormatForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSMSPromptEnabled = isSMSPromptEnabled();
                        reply.writeNoException();
                        reply.writeInt(isSMSPromptEnabled ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg07 = data.readInt();
                        String _arg17 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        String _arg38 = data.readString();
                        if (data.readInt() != 0) {
                            _arg4 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg54 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg54 = null;
                        }
                        sendStoredText(_arg07, _arg17, _arg2, _arg38, _arg4, _arg54);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        String _arg18 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        sendStoredMultipartText(_arg08, _arg18, _arg22, data.readString(), data.createTypedArrayList(PendingIntent.CREATOR), data.createTypedArrayList(PendingIntent.CREATOR));
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        String _result6 = createAppSpecificSmsToken(_arg09, _arg19, _arg23);
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result7 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result7);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        String _arg110 = data.readString();
                        String _arg211 = data.readString();
                        if (data.readInt() != 0) {
                            _arg32 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        String _result8 = createAppSpecificSmsTokenWithPackageInfo(_arg010, _arg110, _arg211, _arg32);
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        String _arg111 = data.readString();
                        if (data.readInt() != 0) {
                            _arg24 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg24 = null;
                        }
                        getSmsMessagesForFinancialApp(_arg011, _arg111, _arg24, IFinancialSmsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = checkSmsShortCodeDestination(data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result9);
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
        public static class Proxy implements ISms {
            public static ISms sDefaultImpl;
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

            @Override // com.android.internal.telephony.ISms
            public List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int subId, String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllMessagesFromIccEfForSubscriber(subId, callingPkg);
                    }
                    _reply.readException();
                    List<SmsRawData> _result = _reply.createTypedArrayList(SmsRawData.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public boolean updateMessageOnIccEfForSubscriber(int subId, String callingPkg, int messageIndex, int newStatus, byte[] pdu) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                        try {
                            _data.writeInt(messageIndex);
                            try {
                                _data.writeInt(newStatus);
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
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(pdu);
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateMessageOnIccEfForSubscriber = Stub.getDefaultImpl().updateMessageOnIccEfForSubscriber(subId, callingPkg, messageIndex, newStatus, pdu);
                            _reply.recycle();
                            _data.recycle();
                            return updateMessageOnIccEfForSubscriber;
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
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.ISms
            public boolean copyMessageToIccEfForSubscriber(int subId, String callingPkg, int status, byte[] pdu, byte[] smsc) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                        try {
                            _data.writeInt(status);
                            try {
                                _data.writeByteArray(pdu);
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
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(smsc);
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean copyMessageToIccEfForSubscriber = Stub.getDefaultImpl().copyMessageToIccEfForSubscriber(subId, callingPkg, status, pdu, smsc);
                            _reply.recycle();
                            _data.recycle();
                            return copyMessageToIccEfForSubscriber;
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
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.ISms
            public void sendDataForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                        _data.writeString(destAddr);
                        _data.writeString(scAddr);
                        _data.writeInt(destPort);
                        _data.writeByteArray(data);
                        if (sentIntent != null) {
                            _data.writeInt(1);
                            sentIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (deliveryIntent != null) {
                            _data.writeInt(1);
                            deliveryIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendDataForSubscriber(subId, callingPkg, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
                        _reply.recycle();
                        _data.recycle();
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
            }

            @Override // com.android.internal.telephony.ISms
            public void sendDataForSubscriberWithSelfPermissions(int subId, String callingPkg, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                        _data.writeString(destAddr);
                        _data.writeString(scAddr);
                        _data.writeInt(destPort);
                        _data.writeByteArray(data);
                        if (sentIntent != null) {
                            _data.writeInt(1);
                            sentIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (deliveryIntent != null) {
                            _data.writeInt(1);
                            deliveryIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendDataForSubscriberWithSelfPermissions(subId, callingPkg, destAddr, scAddr, destPort, data, sentIntent, deliveryIntent);
                        _reply.recycle();
                        _data.recycle();
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
            }

            @Override // com.android.internal.telephony.ISms
            public void sendTextForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                        _data.writeString(destAddr);
                        _data.writeString(scAddr);
                        _data.writeString(text);
                        int i = 1;
                        if (sentIntent != null) {
                            _data.writeInt(1);
                            sentIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (deliveryIntent != null) {
                            _data.writeInt(1);
                            deliveryIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (!persistMessageForNonDefaultSmsApp) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendTextForSubscriber(subId, callingPkg, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp);
                        _reply.recycle();
                        _data.recycle();
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
            }

            @Override // com.android.internal.telephony.ISms
            public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                        _data.writeString(destAddr);
                        _data.writeString(scAddr);
                        _data.writeString(text);
                        int i = 1;
                        if (sentIntent != null) {
                            _data.writeInt(1);
                            sentIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (deliveryIntent != null) {
                            _data.writeInt(1);
                            deliveryIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (!persistMessage) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendTextForSubscriberWithSelfPermissions(subId, callingPkg, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessage);
                        _reply.recycle();
                        _data.recycle();
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
            }

            @Override // com.android.internal.telephony.ISms
            public void sendTextForSubscriberWithOptions(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) throws RemoteException {
                Parcel _reply;
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(destAddr);
                    _data.writeString(scAddr);
                    _data.writeString(text);
                    int i = 1;
                    if (sentIntent != null) {
                        try {
                            _data.writeInt(1);
                            sentIntent.writeToParcel(_data, 0);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply = _reply2;
                        }
                    } else {
                        _data.writeInt(0);
                    }
                    if (deliveryIntent != null) {
                        _data.writeInt(1);
                        deliveryIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(persistMessageForNonDefaultSmsApp ? 1 : 0);
                    _data.writeInt(priority);
                    if (!expectMore) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(validityPeriod);
                    if (this.mRemote.transact(8, _data, _reply2, 0) || Stub.getDefaultImpl() == null) {
                        _reply2.readException();
                        _reply2.recycle();
                        _data.recycle();
                        return;
                    }
                    _reply = _reply2;
                    try {
                        Stub.getDefaultImpl().sendTextForSubscriberWithOptions(subId, callingPkg, destAddr, scAddr, text, sentIntent, deliveryIntent, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply = _reply2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.ISms
            public void injectSmsPduForSubscriber(int subId, byte[] pdu, String format, PendingIntent receivedIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeByteArray(pdu);
                    _data.writeString(format);
                    if (receivedIntent != null) {
                        _data.writeInt(1);
                        receivedIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().injectSmsPduForSubscriber(subId, pdu, format, receivedIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public void sendMultipartTextForSubscriber(int subId, String callingPkg, String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(destinationAddress);
                        try {
                            _data.writeString(scAddress);
                            _data.writeStringList(parts);
                            _data.writeTypedList(sentIntents);
                            _data.writeTypedList(deliveryIntents);
                            _data.writeInt(persistMessageForNonDefaultSmsApp ? 1 : 0);
                            if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendMultipartTextForSubscriber(subId, callingPkg, destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // com.android.internal.telephony.ISms
            public void sendMultipartTextForSubscriberWithOptions(int subId, String callingPkg, String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                        _data.writeString(callingPkg);
                        _data.writeString(destinationAddress);
                        _data.writeString(scAddress);
                        _data.writeStringList(parts);
                        _data.writeTypedList(sentIntents);
                        _data.writeTypedList(deliveryIntents);
                        int i = 1;
                        _data.writeInt(persistMessageForNonDefaultSmsApp ? 1 : 0);
                        _data.writeInt(priority);
                        if (!expectMore) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        _data.writeInt(validityPeriod);
                        if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendMultipartTextForSubscriberWithOptions(subId, callingPkg, destinationAddress, scAddress, parts, sentIntents, deliveryIntents, persistMessageForNonDefaultSmsApp, priority, expectMore, validityPeriod);
                        _reply.recycle();
                        _data.recycle();
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
            }

            @Override // com.android.internal.telephony.ISms
            public boolean enableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(messageIdentifier);
                    _data.writeInt(ranType);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableCellBroadcastForSubscriber(subId, messageIdentifier, ranType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public boolean disableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(messageIdentifier);
                    _data.writeInt(ranType);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableCellBroadcastForSubscriber(subId, messageIdentifier, ranType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public boolean enableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(startMessageId);
                    _data.writeInt(endMessageId);
                    _data.writeInt(ranType);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableCellBroadcastRangeForSubscriber(subId, startMessageId, endMessageId, ranType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public boolean disableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(startMessageId);
                    _data.writeInt(endMessageId);
                    _data.writeInt(ranType);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableCellBroadcastRangeForSubscriber(subId, startMessageId, endMessageId, ranType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public int getPremiumSmsPermission(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPremiumSmsPermission(packageName);
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

            @Override // com.android.internal.telephony.ISms
            public int getPremiumSmsPermissionForSubscriber(int subId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPremiumSmsPermissionForSubscriber(subId, packageName);
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

            @Override // com.android.internal.telephony.ISms
            public void setPremiumSmsPermission(String packageName, int permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(permission);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPremiumSmsPermission(packageName, permission);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public void setPremiumSmsPermissionForSubscriber(int subId, String packageName, int permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(packageName);
                    _data.writeInt(permission);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPremiumSmsPermissionForSubscriber(subId, packageName, permission);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public boolean isImsSmsSupportedForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isImsSmsSupportedForSubscriber(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public boolean isSmsSimPickActivityNeeded(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSmsSimPickActivityNeeded(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public int getPreferredSmsSubscription() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreferredSmsSubscription();
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

            @Override // com.android.internal.telephony.ISms
            public String getImsSmsFormatForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImsSmsFormatForSubscriber(subId);
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

            @Override // com.android.internal.telephony.ISms
            public boolean isSMSPromptEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSMSPromptEnabled();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public void sendStoredText(int subId, String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(callingPkg);
                        if (messageUri != null) {
                            _data.writeInt(1);
                            messageUri.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(scAddress);
                            if (sentIntent != null) {
                                _data.writeInt(1);
                                sentIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (deliveryIntent != null) {
                                _data.writeInt(1);
                                deliveryIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendStoredText(subId, callingPkg, messageUri, scAddress, sentIntent, deliveryIntent);
                            _reply.recycle();
                            _data.recycle();
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
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.ISms
            public void sendStoredMultipartText(int subId, String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                        try {
                            _data.writeString(callingPkg);
                            if (messageUri != null) {
                                _data.writeInt(1);
                                messageUri.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
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
                    try {
                        _data.writeString(scAddress);
                        try {
                            _data.writeTypedList(sentIntents);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeTypedList(deliveryIntents);
                            if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendStoredMultipartText(subId, callingPkg, messageUri, scAddress, sentIntents, deliveryIntents);
                            _reply.recycle();
                            _data.recycle();
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

            @Override // com.android.internal.telephony.ISms
            public String createAppSpecificSmsToken(int subId, String callingPkg, PendingIntent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createAppSpecificSmsToken(subId, callingPkg, intent);
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

            @Override // com.android.internal.telephony.ISms
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // com.android.internal.telephony.ISms
            public String createAppSpecificSmsTokenWithPackageInfo(int subId, String callingPkg, String prefixes, PendingIntent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(prefixes);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createAppSpecificSmsTokenWithPackageInfo(subId, callingPkg, prefixes, intent);
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

            @Override // com.android.internal.telephony.ISms
            public void getSmsMessagesForFinancialApp(int subId, String callingPkg, Bundle params, IFinancialSmsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getSmsMessagesForFinancialApp(subId, callingPkg, params, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.ISms
            public int checkSmsShortCodeDestination(int subId, String callingApk, String destAddress, String countryIso) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingApk);
                    _data.writeString(destAddress);
                    _data.writeString(countryIso);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkSmsShortCodeDestination(subId, callingApk, destAddress, countryIso);
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

        public static boolean setDefaultImpl(ISms impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISms getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
