package com.android.internal.telephony;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ISms extends IInterface {

    public static abstract class Stub extends Binder implements ISms {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ISms";
        static final int TRANSACTION_copyMessageToIccEfForSubscriber = 3;
        static final int TRANSACTION_disableCellBroadcastForSubscriber = 11;
        static final int TRANSACTION_disableCellBroadcastRangeForSubscriber = 13;
        static final int TRANSACTION_enableCellBroadcastForSubscriber = 10;
        static final int TRANSACTION_enableCellBroadcastRangeForSubscriber = 12;
        static final int TRANSACTION_getAllMessagesFromIccEfForSubscriber = 1;
        static final int TRANSACTION_getImsSmsFormatForSubscriber = 23;
        static final int TRANSACTION_getMeidOrPesn = 32;
        static final int TRANSACTION_getPreferredSmsSubscription = 22;
        static final int TRANSACTION_getPremiumSmsPermission = 14;
        static final int TRANSACTION_getPremiumSmsPermissionForSubscriber = 15;
        static final int TRANSACTION_getSmscAddr = 27;
        static final int TRANSACTION_getSmscAddrForSubscriber = 29;
        static final int TRANSACTION_injectSmsPduForSubscriber = 8;
        static final int TRANSACTION_isImsSmsSupportedForSubscriber = 18;
        static final int TRANSACTION_isSMSPromptEnabled = 24;
        static final int TRANSACTION_isSmsSimPickActivityNeeded = 19;
        static final int TRANSACTION_isUimSupportMeid = 31;
        static final int TRANSACTION_sendDataForSubscriber = 4;
        static final int TRANSACTION_sendDataForSubscriberWithSelfPermissions = 5;
        static final int TRANSACTION_sendMultipartTextForSubscriber = 9;
        static final int TRANSACTION_sendStoredMultipartText = 26;
        static final int TRANSACTION_sendStoredText = 25;
        static final int TRANSACTION_sendTextForSubscriber = 6;
        static final int TRANSACTION_sendTextForSubscriberWithSelfPermissions = 7;
        static final int TRANSACTION_setEnabledSingleShiftTables = 20;
        static final int TRANSACTION_setMeidOrPesn = 33;
        static final int TRANSACTION_setPremiumSmsPermission = 16;
        static final int TRANSACTION_setPremiumSmsPermissionForSubscriber = 17;
        static final int TRANSACTION_setSmsCodingNationalCode = 21;
        static final int TRANSACTION_setSmscAddr = 28;
        static final int TRANSACTION_setSmscAddrForSubscriber = 30;
        static final int TRANSACTION_updateMessageOnIccEfForSubscriber = 2;

        private static class Proxy implements ISms {
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

            public List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int subId, String callingPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    this.mRemote.transact(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    List<SmsRawData> _result = _reply.createTypedArrayList(SmsRawData.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateMessageOnIccEfForSubscriber(int subId, String callingPkg, int messageIndex, int newStatus, byte[] pdu) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeInt(messageIndex);
                    _data.writeInt(newStatus);
                    _data.writeByteArray(pdu);
                    this.mRemote.transact(Stub.TRANSACTION_updateMessageOnIccEfForSubscriber, _data, _reply, 0);
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

            public boolean copyMessageToIccEfForSubscriber(int subId, String callingPkg, int status, byte[] pdu, byte[] smsc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeInt(status);
                    _data.writeByteArray(pdu);
                    _data.writeByteArray(smsc);
                    this.mRemote.transact(Stub.TRANSACTION_copyMessageToIccEfForSubscriber, _data, _reply, 0);
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

            public void sendDataForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(destAddr);
                    _data.writeString(scAddr);
                    _data.writeInt(destPort);
                    _data.writeByteArray(data);
                    if (sentIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deliveryIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        deliveryIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendDataForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendDataForSubscriberWithSelfPermissions(int subId, String callingPkg, String destAddr, String scAddr, int destPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(destAddr);
                    _data.writeString(scAddr);
                    _data.writeInt(destPort);
                    _data.writeByteArray(data);
                    if (sentIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deliveryIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        deliveryIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendDataForSubscriberWithSelfPermissions, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendTextForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                int i = Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(destAddr);
                    _data.writeString(scAddr);
                    _data.writeString(text);
                    if (sentIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deliveryIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        deliveryIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!persistMessageForNonDefaultSmsApp) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_sendTextForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(destAddr);
                    _data.writeString(scAddr);
                    _data.writeString(text);
                    if (sentIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deliveryIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        deliveryIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendTextForSubscriberWithSelfPermissions, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void injectSmsPduForSubscriber(int subId, byte[] pdu, String format, PendingIntent receivedIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeByteArray(pdu);
                    _data.writeString(format);
                    if (receivedIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        receivedIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_injectSmsPduForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendMultipartTextForSubscriber(int subId, String callingPkg, String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    _data.writeString(destinationAddress);
                    _data.writeString(scAddress);
                    _data.writeStringList(parts);
                    _data.writeTypedList(sentIntents);
                    _data.writeTypedList(deliveryIntents);
                    if (persistMessageForNonDefaultSmsApp) {
                        i = Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_sendMultipartTextForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(messageIdentifier);
                    _data.writeInt(ranType);
                    this.mRemote.transact(Stub.TRANSACTION_enableCellBroadcastForSubscriber, _data, _reply, 0);
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

            public boolean disableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(messageIdentifier);
                    _data.writeInt(ranType);
                    this.mRemote.transact(Stub.TRANSACTION_disableCellBroadcastForSubscriber, _data, _reply, 0);
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

            public boolean enableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(startMessageId);
                    _data.writeInt(endMessageId);
                    _data.writeInt(ranType);
                    this.mRemote.transact(Stub.TRANSACTION_enableCellBroadcastRangeForSubscriber, _data, _reply, 0);
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

            public boolean disableCellBroadcastRangeForSubscriber(int subId, int startMessageId, int endMessageId, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(startMessageId);
                    _data.writeInt(endMessageId);
                    _data.writeInt(ranType);
                    this.mRemote.transact(Stub.TRANSACTION_disableCellBroadcastRangeForSubscriber, _data, _reply, 0);
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

            public int getPremiumSmsPermission(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getPremiumSmsPermission, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPremiumSmsPermissionForSubscriber(int subId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_getPremiumSmsPermissionForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPremiumSmsPermission(String packageName, int permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(permission);
                    this.mRemote.transact(Stub.TRANSACTION_setPremiumSmsPermission, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPremiumSmsPermissionForSubscriber(int subId, String packageName, int permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(packageName);
                    _data.writeInt(permission);
                    this.mRemote.transact(Stub.TRANSACTION_setPremiumSmsPermissionForSubscriber, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isImsSmsSupportedForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_isImsSmsSupportedForSubscriber, _data, _reply, 0);
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

            public boolean isSmsSimPickActivityNeeded(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_isSmsSimPickActivityNeeded, _data, _reply, 0);
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

            public void setEnabledSingleShiftTables(int[] tables) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(tables);
                    this.mRemote.transact(Stub.TRANSACTION_setEnabledSingleShiftTables, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setSmsCodingNationalCode(String code) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(code);
                    this.mRemote.transact(Stub.TRANSACTION_setSmsCodingNationalCode, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPreferredSmsSubscription() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPreferredSmsSubscription, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getImsSmsFormatForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getImsSmsFormatForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSMSPromptEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSMSPromptEnabled, _data, _reply, 0);
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

            public void sendStoredText(int subId, String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (messageUri != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        messageUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(scAddress);
                    if (sentIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (deliveryIntent != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        deliveryIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_sendStoredText, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendStoredMultipartText(int subId, String callingPkg, Uri messageUri, String scAddress, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (messageUri != null) {
                        _data.writeInt(Stub.TRANSACTION_getAllMessagesFromIccEfForSubscriber);
                        messageUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(scAddress);
                    _data.writeTypedList(sentIntents);
                    _data.writeTypedList(deliveryIntents);
                    this.mRemote.transact(Stub.TRANSACTION_sendStoredMultipartText, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSmscAddr() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSmscAddr, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSmscAddr(String smscAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(smscAddr);
                    this.mRemote.transact(Stub.TRANSACTION_setSmscAddr, _data, _reply, 0);
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

            public String getSmscAddrForSubscriber(long subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getSmscAddrForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSmscAddrForSubscriber(long subId, String smscAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(subId);
                    _data.writeString(smscAddr);
                    this.mRemote.transact(Stub.TRANSACTION_setSmscAddrForSubscriber, _data, _reply, 0);
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

            public boolean isUimSupportMeid(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_isUimSupportMeid, _data, _reply, 0);
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

            public String getMeidOrPesn(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getMeidOrPesn, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setMeidOrPesn(int subId, String meid, String pesn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(meid);
                    _data.writeString(pesn);
                    this.mRemote.transact(Stub.TRANSACTION_setMeidOrPesn, _data, _reply, 0);
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
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _arg0;
            String _arg1;
            String _arg2;
            String _arg3;
            int _arg4;
            byte[] _arg5;
            PendingIntent pendingIntent;
            PendingIntent pendingIntent2;
            String _arg42;
            PendingIntent pendingIntent3;
            int _result2;
            String _result3;
            Uri uri;
            switch (code) {
                case TRANSACTION_getAllMessagesFromIccEfForSubscriber /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<SmsRawData> _result4 = getAllMessagesFromIccEfForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case TRANSACTION_updateMessageOnIccEfForSubscriber /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = updateMessageOnIccEfForSubscriber(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_copyMessageToIccEfForSubscriber /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = copyMessageToIccEfForSubscriber(data.readInt(), data.readString(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_sendDataForSubscriber /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg3 = data.readString();
                    _arg4 = data.readInt();
                    _arg5 = data.createByteArray();
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent2 = null;
                    }
                    sendDataForSubscriber(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, pendingIntent, pendingIntent2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendDataForSubscriberWithSelfPermissions /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg3 = data.readString();
                    _arg4 = data.readInt();
                    _arg5 = data.createByteArray();
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent2 = null;
                    }
                    sendDataForSubscriberWithSelfPermissions(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, pendingIntent, pendingIntent2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendTextForSubscriber /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg3 = data.readString();
                    _arg42 = data.readString();
                    if (data.readInt() != 0) {
                        pendingIntent3 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent3 = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    sendTextForSubscriber(_arg0, _arg1, _arg2, _arg3, _arg42, pendingIntent3, pendingIntent, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendTextForSubscriberWithSelfPermissions /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readString();
                    _arg3 = data.readString();
                    _arg42 = data.readString();
                    if (data.readInt() != 0) {
                        pendingIntent3 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent3 = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    sendTextForSubscriberWithSelfPermissions(_arg0, _arg1, _arg2, _arg3, _arg42, pendingIntent3, pendingIntent);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_injectSmsPduForSubscriber /*8*/:
                    PendingIntent pendingIntent4;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    byte[] _arg12 = data.createByteArray();
                    _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        pendingIntent4 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent4 = null;
                    }
                    injectSmsPduForSubscriber(_arg0, _arg12, _arg2, pendingIntent4);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendMultipartTextForSubscriber /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    sendMultipartTextForSubscriber(data.readInt(), data.readString(), data.readString(), data.readString(), data.createStringArrayList(), data.createTypedArrayList(PendingIntent.CREATOR), data.createTypedArrayList(PendingIntent.CREATOR), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableCellBroadcastForSubscriber /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableCellBroadcastForSubscriber(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_disableCellBroadcastForSubscriber /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableCellBroadcastForSubscriber(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_enableCellBroadcastRangeForSubscriber /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = enableCellBroadcastRangeForSubscriber(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_disableCellBroadcastRangeForSubscriber /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = disableCellBroadcastRangeForSubscriber(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_getPremiumSmsPermission /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPremiumSmsPermission(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getPremiumSmsPermissionForSubscriber /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPremiumSmsPermissionForSubscriber(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setPremiumSmsPermission /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPremiumSmsPermission(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setPremiumSmsPermissionForSubscriber /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPremiumSmsPermissionForSubscriber(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isImsSmsSupportedForSubscriber /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isImsSmsSupportedForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_isSmsSimPickActivityNeeded /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSmsSimPickActivityNeeded(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_setEnabledSingleShiftTables /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    setEnabledSingleShiftTables(data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setSmsCodingNationalCode /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    setSmsCodingNationalCode(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPreferredSmsSubscription /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPreferredSmsSubscription();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_getImsSmsFormatForSubscriber /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getImsSmsFormatForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_isSMSPromptEnabled /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSMSPromptEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_sendStoredText /*25*/:
                    PendingIntent pendingIntent5;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    _arg3 = data.readString();
                    if (data.readInt() != 0) {
                        pendingIntent5 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent5 = null;
                    }
                    if (data.readInt() != 0) {
                        pendingIntent3 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent3 = null;
                    }
                    sendStoredText(_arg0, _arg1, uri, _arg3, pendingIntent5, pendingIntent3);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_sendStoredMultipartText /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    sendStoredMultipartText(_arg0, _arg1, uri, data.readString(), data.createTypedArrayList(PendingIntent.CREATOR), data.createTypedArrayList(PendingIntent.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getSmscAddr /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getSmscAddr();
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_setSmscAddr /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setSmscAddr(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_getSmscAddrForSubscriber /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getSmscAddrForSubscriber(data.readLong());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_setSmscAddrForSubscriber /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setSmscAddrForSubscriber(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_isUimSupportMeid /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isUimSupportMeid(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case TRANSACTION_getMeidOrPesn /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getMeidOrPesn(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_setMeidOrPesn /*33*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setMeidOrPesn(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getAllMessagesFromIccEfForSubscriber : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean copyMessageToIccEfForSubscriber(int i, String str, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    boolean disableCellBroadcastForSubscriber(int i, int i2, int i3) throws RemoteException;

    boolean disableCellBroadcastRangeForSubscriber(int i, int i2, int i3, int i4) throws RemoteException;

    boolean enableCellBroadcastForSubscriber(int i, int i2, int i3) throws RemoteException;

    boolean enableCellBroadcastRangeForSubscriber(int i, int i2, int i3, int i4) throws RemoteException;

    List<SmsRawData> getAllMessagesFromIccEfForSubscriber(int i, String str) throws RemoteException;

    String getImsSmsFormatForSubscriber(int i) throws RemoteException;

    String getMeidOrPesn(int i) throws RemoteException;

    int getPreferredSmsSubscription() throws RemoteException;

    int getPremiumSmsPermission(String str) throws RemoteException;

    int getPremiumSmsPermissionForSubscriber(int i, String str) throws RemoteException;

    String getSmscAddr() throws RemoteException;

    String getSmscAddrForSubscriber(long j) throws RemoteException;

    void injectSmsPduForSubscriber(int i, byte[] bArr, String str, PendingIntent pendingIntent) throws RemoteException;

    boolean isImsSmsSupportedForSubscriber(int i) throws RemoteException;

    boolean isSMSPromptEnabled() throws RemoteException;

    boolean isSmsSimPickActivityNeeded(int i) throws RemoteException;

    boolean isUimSupportMeid(int i) throws RemoteException;

    void sendDataForSubscriber(int i, String str, String str2, String str3, int i2, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void sendDataForSubscriberWithSelfPermissions(int i, String str, String str2, String str3, int i2, byte[] bArr, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void sendMultipartTextForSubscriber(int i, String str, String str2, String str3, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, boolean z) throws RemoteException;

    void sendStoredMultipartText(int i, String str, Uri uri, String str2, List<PendingIntent> list, List<PendingIntent> list2) throws RemoteException;

    void sendStoredText(int i, String str, Uri uri, String str2, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void sendTextForSubscriber(int i, String str, String str2, String str3, String str4, PendingIntent pendingIntent, PendingIntent pendingIntent2, boolean z) throws RemoteException;

    void sendTextForSubscriberWithSelfPermissions(int i, String str, String str2, String str3, String str4, PendingIntent pendingIntent, PendingIntent pendingIntent2) throws RemoteException;

    void setEnabledSingleShiftTables(int[] iArr) throws RemoteException;

    boolean setMeidOrPesn(int i, String str, String str2) throws RemoteException;

    void setPremiumSmsPermission(String str, int i) throws RemoteException;

    void setPremiumSmsPermissionForSubscriber(int i, String str, int i2) throws RemoteException;

    void setSmsCodingNationalCode(String str) throws RemoteException;

    boolean setSmscAddr(String str) throws RemoteException;

    boolean setSmscAddrForSubscriber(long j, String str) throws RemoteException;

    boolean updateMessageOnIccEfForSubscriber(int i, String str, int i2, int i3, byte[] bArr) throws RemoteException;
}
