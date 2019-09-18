package com.android.internal.telecom;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomAnalytics;
import java.util.List;

public interface ITelecomService extends IInterface {

    public static abstract class Stub extends Binder implements ITelecomService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.ITelecomService";
        static final int TRANSACTION_acceptHandover = 50;
        static final int TRANSACTION_acceptRingingCall = 31;
        static final int TRANSACTION_acceptRingingCallWithVideoState = 32;
        static final int TRANSACTION_addNewIncomingCall = 39;
        static final int TRANSACTION_addNewUnknownCall = 40;
        static final int TRANSACTION_cancelMissedCallsNotification = 33;
        static final int TRANSACTION_clearAccounts = 17;
        static final int TRANSACTION_createManageBlockedNumbersIntent = 44;
        static final int TRANSACTION_dumpCallAnalytics = 24;
        static final int TRANSACTION_enablePhoneAccount = 42;
        static final int TRANSACTION_endCall = 30;
        static final int TRANSACTION_getActiveSubscription = 47;
        static final int TRANSACTION_getAdnUriForPhoneAccount = 36;
        static final int TRANSACTION_getAllPhoneAccountHandles = 12;
        static final int TRANSACTION_getAllPhoneAccounts = 11;
        static final int TRANSACTION_getAllPhoneAccountsCount = 10;
        static final int TRANSACTION_getCallCapablePhoneAccounts = 5;
        static final int TRANSACTION_getCallState = 29;
        static final int TRANSACTION_getCurrentTtyMode = 38;
        static final int TRANSACTION_getDefaultDialerPackage = 22;
        static final int TRANSACTION_getDefaultOutgoingPhoneAccount = 2;
        static final int TRANSACTION_getDefaultPhoneApp = 21;
        static final int TRANSACTION_getLine1Number = 20;
        static final int TRANSACTION_getPhoneAccount = 9;
        static final int TRANSACTION_getPhoneAccountsForPackage = 8;
        static final int TRANSACTION_getPhoneAccountsSupportingScheme = 7;
        static final int TRANSACTION_getSelfManagedPhoneAccounts = 6;
        static final int TRANSACTION_getSimCallManager = 13;
        static final int TRANSACTION_getSimCallManagerForUser = 14;
        static final int TRANSACTION_getSystemDialerPackage = 23;
        static final int TRANSACTION_getUserSelectedOutgoingPhoneAccount = 3;
        static final int TRANSACTION_getVoiceMailNumber = 19;
        static final int TRANSACTION_handlePinMmi = 34;
        static final int TRANSACTION_handlePinMmiForPhoneAccount = 35;
        static final int TRANSACTION_isInCall = 26;
        static final int TRANSACTION_isInManagedCall = 27;
        static final int TRANSACTION_isIncomingCallPermitted = 45;
        static final int TRANSACTION_isOutgoingCallPermitted = 46;
        static final int TRANSACTION_isRinging = 28;
        static final int TRANSACTION_isTtySupported = 37;
        static final int TRANSACTION_isVoiceMailNumber = 18;
        static final int TRANSACTION_placeCall = 41;
        static final int TRANSACTION_registerPhoneAccount = 15;
        static final int TRANSACTION_setDefaultDialer = 43;
        static final int TRANSACTION_setUserSelectedOutgoingPhoneAccount = 4;
        static final int TRANSACTION_showInCallScreen = 1;
        static final int TRANSACTION_silenceRinger = 25;
        static final int TRANSACTION_switchToOtherActiveSub = 48;
        static final int TRANSACTION_unregisterPhoneAccount = 16;
        static final int TRANSACTION_waitOnHandlers = 49;

        private static class Proxy implements ITelecomService {
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

            public void showInCallScreen(boolean showDialpad, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(showDialpad);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getDefaultOutgoingPhoneAccount(String uriScheme, String callingPackage) throws RemoteException {
                PhoneAccountHandle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uriScheme);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() throws RemoteException {
                PhoneAccountHandle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PhoneAccountHandle> getCallCapablePhoneAccounts(boolean includeDisabledAccounts, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(includeDisabledAccounts);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PhoneAccountHandle> getSelfManagedPhoneAccounts(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PhoneAccountHandle> getPhoneAccountsSupportingScheme(String uriScheme, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uriScheme);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PhoneAccountHandle> getPhoneAccountsForPackage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccount getPhoneAccount(PhoneAccountHandle account) throws RemoteException {
                PhoneAccount _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PhoneAccount) PhoneAccount.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAllPhoneAccountsCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PhoneAccount> getAllPhoneAccounts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PhoneAccount.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PhoneAccountHandle> getAllPhoneAccountHandles() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getSimCallManager() throws RemoteException {
                PhoneAccountHandle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getSimCallManagerForUser(int userId) throws RemoteException {
                PhoneAccountHandle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerPhoneAccount(PhoneAccount metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterPhoneAccount(PhoneAccountHandle account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearAccounts(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVoiceMailNumber(PhoneAccountHandle accountHandle, String number, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(number);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(18, _data, _reply, 0);
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

            public String getVoiceMailNumber(PhoneAccountHandle accountHandle, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLine1Number(PhoneAccountHandle accountHandle, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getDefaultPhoneApp() throws RemoteException {
                ComponentName _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDefaultDialerPackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSystemDialerPackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TelecomAnalytics dumpCallAnalytics() throws RemoteException {
                TelecomAnalytics _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (TelecomAnalytics) TelecomAnalytics.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void silenceRinger(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInCall(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(26, _data, _reply, 0);
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

            public boolean isInManagedCall(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(27, _data, _reply, 0);
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

            public boolean isRinging(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(28, _data, _reply, 0);
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

            public int getCallState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean endCall(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(30, _data, _reply, 0);
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

            public void acceptRingingCall(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acceptRingingCallWithVideoState(String callingPackage, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(videoState);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelMissedCallsNotification(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handlePinMmi(String dialString, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(dialString);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(34, _data, _reply, 0);
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

            public boolean handlePinMmiForPhoneAccount(PhoneAccountHandle accountHandle, String dialString, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(dialString);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(35, _data, _reply, 0);
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

            public Uri getAdnUriForPhoneAccount(PhoneAccountHandle accountHandle, String callingPackage) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Uri) Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTtySupported(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(37, _data, _reply, 0);
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

            public int getCurrentTtyMode(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addNewIncomingCall(PhoneAccountHandle phoneAccount, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (phoneAccount != null) {
                        _data.writeInt(1);
                        phoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addNewUnknownCall(PhoneAccountHandle phoneAccount, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (phoneAccount != null) {
                        _data.writeInt(1);
                        phoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void placeCall(Uri handle, Bundle extras, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (handle != null) {
                        _data.writeInt(1);
                        handle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enablePhoneAccount(PhoneAccountHandle accountHandle, boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (accountHandle != null) {
                        _data.writeInt(1);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(isEnabled);
                    this.mRemote.transact(42, _data, _reply, 0);
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

            public boolean setDefaultDialer(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(43, _data, _reply, 0);
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

            public Intent createManageBlockedNumbersIntent() throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Intent) Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isIncomingCallPermitted(PhoneAccountHandle phoneAccountHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (phoneAccountHandle != null) {
                        _data.writeInt(1);
                        phoneAccountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(45, _data, _reply, 0);
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

            public boolean isOutgoingCallPermitted(PhoneAccountHandle phoneAccountHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (phoneAccountHandle != null) {
                        _data.writeInt(1);
                        phoneAccountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(46, _data, _reply, 0);
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

            public int getActiveSubscription() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void switchToOtherActiveSub(long subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(subId);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void waitOnHandlers() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acceptHandover(Uri srcAddr, int videoState, PhoneAccountHandle destAcct) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (srcAddr != null) {
                        _data.writeInt(1);
                        srcAddr.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(videoState);
                    if (destAcct != null) {
                        _data.writeInt(1);
                        destAcct.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITelecomService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITelecomService)) {
                return new Proxy(obj);
            }
            return (ITelecomService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v13, resolved type: android.telecom.PhoneAccountHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v49, resolved type: android.telecom.PhoneAccountHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v55, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v59, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v63, resolved type: android.os.Bundle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v67, resolved type: android.telecom.PhoneAccountHandle} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v82, resolved type: android.telecom.PhoneAccountHandle} */
        /* JADX WARNING: type inference failed for: r3v0 */
        /* JADX WARNING: type inference failed for: r3v5 */
        /* JADX WARNING: type inference failed for: r3v19 */
        /* JADX WARNING: type inference failed for: r3v22 */
        /* JADX WARNING: type inference failed for: r3v25 */
        /* JADX WARNING: type inference failed for: r3v29 */
        /* JADX WARNING: type inference failed for: r3v33 */
        /* JADX WARNING: type inference failed for: r3v45 */
        /* JADX WARNING: type inference failed for: r3v73 */
        /* JADX WARNING: type inference failed for: r3v77 */
        /* JADX WARNING: type inference failed for: r3v86 */
        /* JADX WARNING: type inference failed for: r3v87 */
        /* JADX WARNING: type inference failed for: r3v88 */
        /* JADX WARNING: type inference failed for: r3v89 */
        /* JADX WARNING: type inference failed for: r3v90 */
        /* JADX WARNING: type inference failed for: r3v91 */
        /* JADX WARNING: type inference failed for: r3v92 */
        /* JADX WARNING: type inference failed for: r3v93 */
        /* JADX WARNING: type inference failed for: r3v94 */
        /* JADX WARNING: type inference failed for: r3v95 */
        /* JADX WARNING: type inference failed for: r3v96 */
        /* JADX WARNING: type inference failed for: r3v97 */
        /* JADX WARNING: type inference failed for: r3v98 */
        /* JADX WARNING: type inference failed for: r3v99 */
        /* JADX WARNING: type inference failed for: r3v100 */
        /* JADX WARNING: type inference failed for: r3v101 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PhoneAccountHandle _arg0;
            PhoneAccountHandle _arg02;
            Uri _arg03;
            Uri _arg04;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ? _arg2 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        showInCallScreen(_arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        PhoneAccountHandle _result = getDefaultOutgoingPhoneAccount(data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        PhoneAccountHandle _result2 = getUserSelectedOutgoingPhoneAccount();
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        setUserSelectedOutgoingPhoneAccount(_arg2);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        List<PhoneAccountHandle> _result3 = getCallCapablePhoneAccounts(_arg1, data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List<PhoneAccountHandle> _result4 = getSelfManagedPhoneAccounts(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result4);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<PhoneAccountHandle> _result5 = getPhoneAccountsSupportingScheme(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<PhoneAccountHandle> _result6 = getPhoneAccountsForPackage(data.readString());
                        reply.writeNoException();
                        reply.writeTypedList(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        PhoneAccount _result7 = getPhoneAccount(_arg2);
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getAllPhoneAccountsCount();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        List<PhoneAccount> _result9 = getAllPhoneAccounts();
                        reply.writeNoException();
                        reply.writeTypedList(_result9);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<PhoneAccountHandle> _result10 = getAllPhoneAccountHandles();
                        reply.writeNoException();
                        reply.writeTypedList(_result10);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        PhoneAccountHandle _result11 = getSimCallManager();
                        reply.writeNoException();
                        if (_result11 != null) {
                            reply.writeInt(1);
                            _result11.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        PhoneAccountHandle _result12 = getSimCallManagerForUser(data.readInt());
                        reply.writeNoException();
                        if (_result12 != null) {
                            reply.writeInt(1);
                            _result12.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccount) PhoneAccount.CREATOR.createFromParcel(data);
                        }
                        registerPhoneAccount(_arg2);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        unregisterPhoneAccount(_arg2);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        clearAccounts(data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        boolean _result13 = isVoiceMailNumber(_arg2, data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        String _result14 = getVoiceMailNumber(_arg2, data.readString());
                        reply.writeNoException();
                        reply.writeString(_result14);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        String _result15 = getLine1Number(_arg2, data.readString());
                        reply.writeNoException();
                        reply.writeString(_result15);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        ComponentName _result16 = getDefaultPhoneApp();
                        reply.writeNoException();
                        if (_result16 != null) {
                            reply.writeInt(1);
                            _result16.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        String _result17 = getDefaultDialerPackage();
                        reply.writeNoException();
                        reply.writeString(_result17);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = getSystemDialerPackage();
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        TelecomAnalytics _result19 = dumpCallAnalytics();
                        reply.writeNoException();
                        if (_result19 != null) {
                            reply.writeInt(1);
                            _result19.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        silenceRinger(data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result20 = isInCall(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result20);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result21 = isInManagedCall(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result21);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result22 = isRinging(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result22);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        int _result23 = getCallState();
                        reply.writeNoException();
                        reply.writeInt(_result23);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result24 = endCall(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result24);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        acceptRingingCall(data.readString());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        acceptRingingCallWithVideoState(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        cancelMissedCallsNotification(data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result25 = handlePinMmi(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result25);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        boolean _result26 = handlePinMmiForPhoneAccount(_arg2, data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result26);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        Uri _result27 = getAdnUriForPhoneAccount(_arg2, data.readString());
                        reply.writeNoException();
                        if (_result27 != null) {
                            reply.writeInt(1);
                            _result27.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result28 = isTtySupported(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result28);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        int _result29 = getCurrentTtyMode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result29);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        addNewIncomingCall(_arg0, _arg2);
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        addNewUnknownCall(_arg02, _arg2);
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        placeCall(_arg03, _arg2, data.readString());
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean _result30 = enablePhoneAccount(_arg2, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result30);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result31 = setDefaultDialer(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result31);
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        Intent _result32 = createManageBlockedNumbersIntent();
                        reply.writeNoException();
                        if (_result32 != null) {
                            reply.writeInt(1);
                            _result32.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        boolean _result33 = isIncomingCallPermitted(_arg2);
                        reply.writeNoException();
                        reply.writeInt(_result33);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        boolean _result34 = isOutgoingCallPermitted(_arg2);
                        reply.writeNoException();
                        reply.writeInt(_result34);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _result35 = getActiveSubscription();
                        reply.writeNoException();
                        reply.writeInt(_result35);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        switchToOtherActiveSub(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        waitOnHandlers();
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        }
                        acceptHandover(_arg04, _arg12, _arg2);
                        reply.writeNoException();
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

    void acceptHandover(Uri uri, int i, PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    void acceptRingingCall(String str) throws RemoteException;

    void acceptRingingCallWithVideoState(String str, int i) throws RemoteException;

    void addNewIncomingCall(PhoneAccountHandle phoneAccountHandle, Bundle bundle) throws RemoteException;

    void addNewUnknownCall(PhoneAccountHandle phoneAccountHandle, Bundle bundle) throws RemoteException;

    void cancelMissedCallsNotification(String str) throws RemoteException;

    void clearAccounts(String str) throws RemoteException;

    Intent createManageBlockedNumbersIntent() throws RemoteException;

    TelecomAnalytics dumpCallAnalytics() throws RemoteException;

    boolean enablePhoneAccount(PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    boolean endCall(String str) throws RemoteException;

    int getActiveSubscription() throws RemoteException;

    Uri getAdnUriForPhoneAccount(PhoneAccountHandle phoneAccountHandle, String str) throws RemoteException;

    List<PhoneAccountHandle> getAllPhoneAccountHandles() throws RemoteException;

    List<PhoneAccount> getAllPhoneAccounts() throws RemoteException;

    int getAllPhoneAccountsCount() throws RemoteException;

    List<PhoneAccountHandle> getCallCapablePhoneAccounts(boolean z, String str) throws RemoteException;

    int getCallState() throws RemoteException;

    int getCurrentTtyMode(String str) throws RemoteException;

    String getDefaultDialerPackage() throws RemoteException;

    PhoneAccountHandle getDefaultOutgoingPhoneAccount(String str, String str2) throws RemoteException;

    ComponentName getDefaultPhoneApp() throws RemoteException;

    String getLine1Number(PhoneAccountHandle phoneAccountHandle, String str) throws RemoteException;

    PhoneAccount getPhoneAccount(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    List<PhoneAccountHandle> getPhoneAccountsForPackage(String str) throws RemoteException;

    List<PhoneAccountHandle> getPhoneAccountsSupportingScheme(String str, String str2) throws RemoteException;

    List<PhoneAccountHandle> getSelfManagedPhoneAccounts(String str) throws RemoteException;

    PhoneAccountHandle getSimCallManager() throws RemoteException;

    PhoneAccountHandle getSimCallManagerForUser(int i) throws RemoteException;

    String getSystemDialerPackage() throws RemoteException;

    PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() throws RemoteException;

    String getVoiceMailNumber(PhoneAccountHandle phoneAccountHandle, String str) throws RemoteException;

    boolean handlePinMmi(String str, String str2) throws RemoteException;

    boolean handlePinMmiForPhoneAccount(PhoneAccountHandle phoneAccountHandle, String str, String str2) throws RemoteException;

    boolean isInCall(String str) throws RemoteException;

    boolean isInManagedCall(String str) throws RemoteException;

    boolean isIncomingCallPermitted(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    boolean isOutgoingCallPermitted(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    boolean isRinging(String str) throws RemoteException;

    boolean isTtySupported(String str) throws RemoteException;

    boolean isVoiceMailNumber(PhoneAccountHandle phoneAccountHandle, String str, String str2) throws RemoteException;

    void placeCall(Uri uri, Bundle bundle, String str) throws RemoteException;

    void registerPhoneAccount(PhoneAccount phoneAccount) throws RemoteException;

    boolean setDefaultDialer(String str) throws RemoteException;

    void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    void showInCallScreen(boolean z, String str) throws RemoteException;

    void silenceRinger(String str) throws RemoteException;

    void switchToOtherActiveSub(long j) throws RemoteException;

    void unregisterPhoneAccount(PhoneAccountHandle phoneAccountHandle) throws RemoteException;

    void waitOnHandlers() throws RemoteException;
}
