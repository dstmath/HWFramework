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
import android.telecom.ParcelableCallAnalytics;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import java.util.List;

public interface ITelecomService extends IInterface {

    public static abstract class Stub extends Binder implements ITelecomService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.ITelecomService";
        static final int TRANSACTION_acceptRingingCall = 29;
        static final int TRANSACTION_acceptRingingCallWithVideoState = 30;
        static final int TRANSACTION_addNewIncomingCall = 37;
        static final int TRANSACTION_addNewUnknownCall = 38;
        static final int TRANSACTION_cancelMissedCallsNotification = 31;
        static final int TRANSACTION_clearAccounts = 16;
        static final int TRANSACTION_createManageBlockedNumbersIntent = 42;
        static final int TRANSACTION_dumpCallAnalytics = 23;
        static final int TRANSACTION_enablePhoneAccount = 40;
        static final int TRANSACTION_endCall = 28;
        static final int TRANSACTION_getActiveSubscription = 43;
        static final int TRANSACTION_getAdnUriForPhoneAccount = 34;
        static final int TRANSACTION_getAllPhoneAccountHandles = 11;
        static final int TRANSACTION_getAllPhoneAccounts = 10;
        static final int TRANSACTION_getAllPhoneAccountsCount = 9;
        static final int TRANSACTION_getCallCapablePhoneAccounts = 5;
        static final int TRANSACTION_getCallState = 27;
        static final int TRANSACTION_getCurrentTtyMode = 36;
        static final int TRANSACTION_getDefaultDialerPackage = 21;
        static final int TRANSACTION_getDefaultOutgoingPhoneAccount = 2;
        static final int TRANSACTION_getDefaultPhoneApp = 20;
        static final int TRANSACTION_getLine1Number = 19;
        static final int TRANSACTION_getPhoneAccount = 8;
        static final int TRANSACTION_getPhoneAccountsForPackage = 7;
        static final int TRANSACTION_getPhoneAccountsSupportingScheme = 6;
        static final int TRANSACTION_getSimCallManager = 12;
        static final int TRANSACTION_getSimCallManagerForUser = 13;
        static final int TRANSACTION_getSystemDialerPackage = 22;
        static final int TRANSACTION_getUserSelectedOutgoingPhoneAccount = 3;
        static final int TRANSACTION_getVoiceMailNumber = 18;
        static final int TRANSACTION_handlePinMmi = 32;
        static final int TRANSACTION_handlePinMmiForPhoneAccount = 33;
        static final int TRANSACTION_isInCall = 25;
        static final int TRANSACTION_isRinging = 26;
        static final int TRANSACTION_isTtySupported = 35;
        static final int TRANSACTION_isVoiceMailNumber = 17;
        static final int TRANSACTION_placeCall = 39;
        static final int TRANSACTION_registerPhoneAccount = 14;
        static final int TRANSACTION_setDefaultDialer = 41;
        static final int TRANSACTION_setUserSelectedOutgoingPhoneAccount = 4;
        static final int TRANSACTION_showInCallScreen = 1;
        static final int TRANSACTION_silenceRinger = 24;
        static final int TRANSACTION_switchToOtherActiveSub = 44;
        static final int TRANSACTION_unregisterPhoneAccount = 15;

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
                int i = Stub.TRANSACTION_showInCallScreen;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!showDialpad) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_showInCallScreen, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getDefaultOutgoingPhoneAccount(String uriScheme, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PhoneAccountHandle phoneAccountHandle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uriScheme);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultOutgoingPhoneAccount, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return phoneAccountHandle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PhoneAccountHandle phoneAccountHandle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUserSelectedOutgoingPhoneAccount, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return phoneAccountHandle;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setUserSelectedOutgoingPhoneAccount, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PhoneAccountHandle> getCallCapablePhoneAccounts(boolean includeDisabledAccounts, String callingPackage) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (includeDisabledAccounts) {
                        i = Stub.TRANSACTION_showInCallScreen;
                    }
                    _data.writeInt(i);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getCallCapablePhoneAccounts, _data, _reply, 0);
                    _reply.readException();
                    List<PhoneAccountHandle> _result = _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPhoneAccountsSupportingScheme, _data, _reply, 0);
                    _reply.readException();
                    List<PhoneAccountHandle> _result = _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getPhoneAccountsForPackage, _data, _reply, 0);
                    _reply.readException();
                    List<PhoneAccountHandle> _result = _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccount getPhoneAccount(PhoneAccountHandle account) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PhoneAccount phoneAccount;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (account != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPhoneAccount, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        phoneAccount = (PhoneAccount) PhoneAccount.CREATOR.createFromParcel(_reply);
                    } else {
                        phoneAccount = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return phoneAccount;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAllPhoneAccountsCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAllPhoneAccountsCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAllPhoneAccounts, _data, _reply, 0);
                    _reply.readException();
                    List<PhoneAccount> _result = _reply.createTypedArrayList(PhoneAccount.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAllPhoneAccountHandles, _data, _reply, 0);
                    _reply.readException();
                    List<PhoneAccountHandle> _result = _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getSimCallManager() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PhoneAccountHandle phoneAccountHandle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSimCallManager, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return phoneAccountHandle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PhoneAccountHandle getSimCallManagerForUser(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PhoneAccountHandle phoneAccountHandle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getSimCallManagerForUser, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(_reply);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return phoneAccountHandle;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registerPhoneAccount, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_unregisterPhoneAccount, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearAccounts, _data, _reply, 0);
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
                    if (accountHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(number);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_isVoiceMailNumber, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getVoiceMailNumber, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getLine1Number, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ComponentName getDefaultPhoneApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ComponentName componentName;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultPhoneApp, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(_reply);
                    } else {
                        componentName = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return componentName;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDefaultDialerPackage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDefaultDialerPackage, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSystemDialerPackage, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ParcelableCallAnalytics> dumpCallAnalytics() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dumpCallAnalytics, _data, _reply, 0);
                    _reply.readException();
                    List<ParcelableCallAnalytics> _result = _reply.createTypedArrayList(ParcelableCallAnalytics.CREATOR);
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
                    this.mRemote.transact(Stub.TRANSACTION_silenceRinger, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isInCall, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_isRinging, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCallState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getCallState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean endCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_endCall, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acceptRingingCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_acceptRingingCall, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acceptRingingCallWithVideoState(int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(videoState);
                    this.mRemote.transact(Stub.TRANSACTION_acceptRingingCallWithVideoState, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_cancelMissedCallsNotification, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_handlePinMmi, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handlePinMmiForPhoneAccount(PhoneAccountHandle accountHandle, String dialString, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(dialString);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_handlePinMmiForPhoneAccount, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Uri getAdnUriForPhoneAccount(PhoneAccountHandle accountHandle, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Uri uri;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_getAdnUriForPhoneAccount, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(_reply);
                    } else {
                        uri = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return uri;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_isTtySupported, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_getCurrentTtyMode, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        phoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addNewIncomingCall, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        phoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addNewUnknownCall, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        handle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_placeCall, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enablePhoneAccount(PhoneAccountHandle accountHandle, boolean isEnabled) throws RemoteException {
                int i = Stub.TRANSACTION_showInCallScreen;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (accountHandle != null) {
                        _data.writeInt(Stub.TRANSACTION_showInCallScreen);
                        accountHandle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!isEnabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_enablePhoneAccount, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultDialer, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Intent createManageBlockedNumbersIntent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Intent intent;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_createManageBlockedNumbersIntent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(_reply);
                    } else {
                        intent = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return intent;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActiveSubscription() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getActiveSubscription, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_switchToOtherActiveSub, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PhoneAccountHandle _result;
            PhoneAccountHandle phoneAccountHandle;
            List<PhoneAccountHandle> _result2;
            int _result3;
            boolean _result4;
            String _result5;
            Bundle bundle;
            switch (code) {
                case TRANSACTION_showInCallScreen /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    showInCallScreen(data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDefaultOutgoingPhoneAccount /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getDefaultOutgoingPhoneAccount(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getUserSelectedOutgoingPhoneAccount /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getUserSelectedOutgoingPhoneAccount();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setUserSelectedOutgoingPhoneAccount /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCallCapablePhoneAccounts /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getCallCapablePhoneAccounts(data.readInt() != 0, data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getPhoneAccountsSupportingScheme /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneAccountsSupportingScheme(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getPhoneAccountsForPackage /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPhoneAccountsForPackage(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getPhoneAccount /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    PhoneAccount _result6 = getPhoneAccount(phoneAccountHandle);
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result6.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getAllPhoneAccountsCount /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAllPhoneAccountsCount();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getAllPhoneAccounts /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<PhoneAccount> _result7 = getAllPhoneAccounts();
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case TRANSACTION_getAllPhoneAccountHandles /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAllPhoneAccountHandles();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getSimCallManager /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSimCallManager();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSimCallManagerForUser /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSimCallManagerForUser(data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_registerPhoneAccount /*14*/:
                    PhoneAccount phoneAccount;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccount = (PhoneAccount) PhoneAccount.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccount = null;
                    }
                    registerPhoneAccount(phoneAccount);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterPhoneAccount /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    unregisterPhoneAccount(phoneAccountHandle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearAccounts /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearAccounts(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isVoiceMailNumber /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _result4 = isVoiceMailNumber(phoneAccountHandle, data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_getVoiceMailNumber /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _result5 = getVoiceMailNumber(phoneAccountHandle, data.readString());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_getLine1Number /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _result5 = getLine1Number(phoneAccountHandle, data.readString());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_getDefaultPhoneApp /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    ComponentName _result8 = getDefaultPhoneApp();
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result8.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getDefaultDialerPackage /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getDefaultDialerPackage();
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_getSystemDialerPackage /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getSystemDialerPackage();
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case TRANSACTION_dumpCallAnalytics /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<ParcelableCallAnalytics> _result9 = dumpCallAnalytics();
                    reply.writeNoException();
                    reply.writeTypedList(_result9);
                    return true;
                case TRANSACTION_silenceRinger /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    silenceRinger(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isInCall /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = isInCall(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_isRinging /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = isRinging(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_getCallState /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCallState();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_endCall /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = endCall();
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_acceptRingingCall /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    acceptRingingCall();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_acceptRingingCallWithVideoState /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    acceptRingingCallWithVideoState(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_cancelMissedCallsNotification /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    cancelMissedCallsNotification(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_handlePinMmi /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = handlePinMmi(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_handlePinMmiForPhoneAccount /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _result4 = handlePinMmiForPhoneAccount(phoneAccountHandle, data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_getAdnUriForPhoneAccount /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    Uri _result10 = getAdnUriForPhoneAccount(phoneAccountHandle, data.readString());
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result10.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_isTtySupported /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = isTtySupported(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_getCurrentTtyMode /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getCurrentTtyMode(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_addNewIncomingCall /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    addNewIncomingCall(phoneAccountHandle, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addNewUnknownCall /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    addNewUnknownCall(phoneAccountHandle, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_placeCall /*39*/:
                    Uri uri;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    placeCall(uri, bundle, data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enablePhoneAccount /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        phoneAccountHandle = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        phoneAccountHandle = null;
                    }
                    _result4 = enablePhoneAccount(phoneAccountHandle, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_setDefaultDialer /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = setDefaultDialer(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4 ? TRANSACTION_showInCallScreen : 0);
                    return true;
                case TRANSACTION_createManageBlockedNumbersIntent /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    Intent _result11 = createManageBlockedNumbersIntent();
                    reply.writeNoException();
                    if (_result11 != null) {
                        reply.writeInt(TRANSACTION_showInCallScreen);
                        _result11.writeToParcel(reply, TRANSACTION_showInCallScreen);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getActiveSubscription /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getActiveSubscription();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_switchToOtherActiveSub /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    switchToOtherActiveSub(data.readLong());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void acceptRingingCall() throws RemoteException;

    void acceptRingingCallWithVideoState(int i) throws RemoteException;

    void addNewIncomingCall(PhoneAccountHandle phoneAccountHandle, Bundle bundle) throws RemoteException;

    void addNewUnknownCall(PhoneAccountHandle phoneAccountHandle, Bundle bundle) throws RemoteException;

    void cancelMissedCallsNotification(String str) throws RemoteException;

    void clearAccounts(String str) throws RemoteException;

    Intent createManageBlockedNumbersIntent() throws RemoteException;

    List<ParcelableCallAnalytics> dumpCallAnalytics() throws RemoteException;

    boolean enablePhoneAccount(PhoneAccountHandle phoneAccountHandle, boolean z) throws RemoteException;

    boolean endCall() throws RemoteException;

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

    PhoneAccountHandle getSimCallManager() throws RemoteException;

    PhoneAccountHandle getSimCallManagerForUser(int i) throws RemoteException;

    String getSystemDialerPackage() throws RemoteException;

    PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() throws RemoteException;

    String getVoiceMailNumber(PhoneAccountHandle phoneAccountHandle, String str) throws RemoteException;

    boolean handlePinMmi(String str, String str2) throws RemoteException;

    boolean handlePinMmiForPhoneAccount(PhoneAccountHandle phoneAccountHandle, String str, String str2) throws RemoteException;

    boolean isInCall(String str) throws RemoteException;

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
}
