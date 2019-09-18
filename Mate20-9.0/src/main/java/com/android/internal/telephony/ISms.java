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
        static final int TRANSACTION_createAppSpecificSmsToken = 27;
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(SmsRawData.CREATOR);
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
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
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
                    this.mRemote.transact(4, _data, _reply, 0);
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendTextForSubscriber(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
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
                    _data.writeInt(persistMessageForNonDefaultSmsApp);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendTextForSubscriberWithSelfPermissions(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessage) throws RemoteException {
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
                    _data.writeInt(persistMessage);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendTextForSubscriberWithOptions(int subId, String callingPkg, String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) throws RemoteException {
                PendingIntent pendingIntent = sentIntent;
                PendingIntent pendingIntent2 = deliveryIntent;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                        try {
                            _data.writeString(callingPkg);
                            try {
                                _data.writeString(destAddr);
                            } catch (Throwable th) {
                                th = th;
                                String str = scAddr;
                                String str2 = text;
                                boolean z = persistMessageForNonDefaultSmsApp;
                                int i = priority;
                                boolean z2 = expectMore;
                                int i2 = validityPeriod;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            String str3 = destAddr;
                            String str4 = scAddr;
                            String str22 = text;
                            boolean z3 = persistMessageForNonDefaultSmsApp;
                            int i3 = priority;
                            boolean z22 = expectMore;
                            int i22 = validityPeriod;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        String str5 = callingPkg;
                        String str32 = destAddr;
                        String str42 = scAddr;
                        String str222 = text;
                        boolean z32 = persistMessageForNonDefaultSmsApp;
                        int i32 = priority;
                        boolean z222 = expectMore;
                        int i222 = validityPeriod;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(scAddr);
                        try {
                            _data.writeString(text);
                            if (pendingIntent != null) {
                                _data.writeInt(1);
                                pendingIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (pendingIntent2 != null) {
                                _data.writeInt(1);
                                pendingIntent2.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            boolean z322 = persistMessageForNonDefaultSmsApp;
                            int i322 = priority;
                            boolean z2222 = expectMore;
                            int i2222 = validityPeriod;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(persistMessageForNonDefaultSmsApp ? 1 : 0);
                            try {
                                _data.writeInt(priority);
                            } catch (Throwable th5) {
                                th = th5;
                                boolean z22222 = expectMore;
                                int i22222 = validityPeriod;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            int i3222 = priority;
                            boolean z222222 = expectMore;
                            int i222222 = validityPeriod;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        String str2222 = text;
                        boolean z3222 = persistMessageForNonDefaultSmsApp;
                        int i32222 = priority;
                        boolean z2222222 = expectMore;
                        int i2222222 = validityPeriod;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(expectMore ? 1 : 0);
                        try {
                            _data.writeInt(validityPeriod);
                            try {
                                this.mRemote.transact(8, _data, _reply, 0);
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                            } catch (Throwable th8) {
                                th = th8;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        int i22222222 = validityPeriod;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th11) {
                    th = th11;
                    int i4 = subId;
                    String str52 = callingPkg;
                    String str322 = destAddr;
                    String str422 = scAddr;
                    String str22222 = text;
                    boolean z32222 = persistMessageForNonDefaultSmsApp;
                    int i322222 = priority;
                    boolean z22222222 = expectMore;
                    int i222222222 = validityPeriod;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
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
                        _data.writeInt(1);
                        receivedIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendMultipartTextForSubscriber(int subId, String callingPkg, String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp) throws RemoteException {
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
                    _data.writeInt(persistMessageForNonDefaultSmsApp);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendMultipartTextForSubscriberWithOptions(int subId, String callingPkg, String destinationAddress, String scAddress, List<String> parts, List<PendingIntent> sentIntents, List<PendingIntent> deliveryIntents, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    try {
                        _data.writeString(callingPkg);
                        try {
                            _data.writeString(destinationAddress);
                            try {
                                _data.writeString(scAddress);
                            } catch (Throwable th) {
                                th = th;
                                List<String> list = parts;
                                List<PendingIntent> list2 = sentIntents;
                                List<PendingIntent> list3 = deliveryIntents;
                                boolean z = persistMessageForNonDefaultSmsApp;
                                int i = priority;
                                boolean z2 = expectMore;
                                int i2 = validityPeriod;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            String str = scAddress;
                            List<String> list4 = parts;
                            List<PendingIntent> list22 = sentIntents;
                            List<PendingIntent> list32 = deliveryIntents;
                            boolean z3 = persistMessageForNonDefaultSmsApp;
                            int i3 = priority;
                            boolean z22 = expectMore;
                            int i22 = validityPeriod;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        String str2 = destinationAddress;
                        String str3 = scAddress;
                        List<String> list42 = parts;
                        List<PendingIntent> list222 = sentIntents;
                        List<PendingIntent> list322 = deliveryIntents;
                        boolean z32 = persistMessageForNonDefaultSmsApp;
                        int i32 = priority;
                        boolean z222 = expectMore;
                        int i222 = validityPeriod;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStringList(parts);
                        try {
                            _data.writeTypedList(sentIntents);
                            try {
                                _data.writeTypedList(deliveryIntents);
                            } catch (Throwable th4) {
                                th = th4;
                                boolean z322 = persistMessageForNonDefaultSmsApp;
                                int i322 = priority;
                                boolean z2222 = expectMore;
                                int i2222 = validityPeriod;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            List<PendingIntent> list3222 = deliveryIntents;
                            boolean z3222 = persistMessageForNonDefaultSmsApp;
                            int i3222 = priority;
                            boolean z22222 = expectMore;
                            int i22222 = validityPeriod;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        List<PendingIntent> list2222 = sentIntents;
                        List<PendingIntent> list32222 = deliveryIntents;
                        boolean z32222 = persistMessageForNonDefaultSmsApp;
                        int i32222 = priority;
                        boolean z222222 = expectMore;
                        int i222222 = validityPeriod;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(persistMessageForNonDefaultSmsApp ? 1 : 0);
                        try {
                            _data.writeInt(priority);
                            try {
                                _data.writeInt(expectMore ? 1 : 0);
                            } catch (Throwable th7) {
                                th = th7;
                                int i2222222 = validityPeriod;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            boolean z2222222 = expectMore;
                            int i22222222 = validityPeriod;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th9) {
                        th = th9;
                        int i322222 = priority;
                        boolean z22222222 = expectMore;
                        int i222222222 = validityPeriod;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(validityPeriod);
                        try {
                            this.mRemote.transact(11, _data, _reply, 0);
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th10) {
                            th = th10;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    String str4 = callingPkg;
                    String str22 = destinationAddress;
                    String str32 = scAddress;
                    List<String> list422 = parts;
                    List<PendingIntent> list22222 = sentIntents;
                    List<PendingIntent> list322222 = deliveryIntents;
                    boolean z322222 = persistMessageForNonDefaultSmsApp;
                    int i3222222 = priority;
                    boolean z222222222 = expectMore;
                    int i2222222222 = validityPeriod;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
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
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
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

            public boolean disableCellBroadcastForSubscriber(int subId, int messageIdentifier, int ranType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(messageIdentifier);
                    _data.writeInt(ranType);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
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
                    this.mRemote.transact(14, _data, _reply, 0);
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
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public int getPremiumSmsPermission(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(18, _data, _reply, 0);
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
                    this.mRemote.transact(19, _data, _reply, 0);
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
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
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

            public boolean isSmsSimPickActivityNeeded(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public int getPreferredSmsSubscription() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
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
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
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
                    boolean _result = false;
                    this.mRemote.transact(24, _data, _reply, 0);
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

            public void sendStoredText(int subId, String callingPkg, Uri messageUri, String scAddress, PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPkg);
                    if (messageUri != null) {
                        _data.writeInt(1);
                        messageUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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
                    this.mRemote.transact(25, _data, _reply, 0);
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
                        _data.writeInt(1);
                        messageUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(scAddress);
                    _data.writeTypedList(sentIntents);
                    _data.writeTypedList(deliveryIntents);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
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

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        public boolean onTransact(int r28, android.os.Parcel r29, android.os.Parcel r30, int r31) throws android.os.RemoteException {
            /*
                r27 = this;
                r12 = r27
                r13 = r28
                r14 = r29
                r15 = r30
                java.lang.String r11 = "com.android.internal.telephony.ISms"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r16 = 1
                if (r13 == r0) goto L_0x04c3
                r0 = 0
                r1 = 0
                switch(r13) {
                    case 1: goto L_0x04ac;
                    case 2: goto L_0x0483;
                    case 3: goto L_0x045a;
                    case 4: goto L_0x040b;
                    case 5: goto L_0x03bc;
                    case 6: goto L_0x0369;
                    case 7: goto L_0x0316;
                    case 8: goto L_0x02aa;
                    case 9: goto L_0x0283;
                    case 10: goto L_0x0242;
                    case 11: goto L_0x01e8;
                    case 12: goto L_0x01ce;
                    case 13: goto L_0x01b4;
                    case 14: goto L_0x0196;
                    case 15: goto L_0x0178;
                    case 16: goto L_0x0166;
                    case 17: goto L_0x0150;
                    case 18: goto L_0x013e;
                    case 19: goto L_0x0128;
                    case 20: goto L_0x0116;
                    case 21: goto L_0x0104;
                    case 22: goto L_0x00f6;
                    case 23: goto L_0x00e4;
                    case 24: goto L_0x00d6;
                    case 25: goto L_0x0089;
                    case 26: goto L_0x004f;
                    case 27: goto L_0x0029;
                    case 28: goto L_0x001b;
                    default: goto L_0x0016;
                }
            L_0x0016:
                boolean r0 = super.onTransact(r28, r29, r30, r31)
                return r0
            L_0x001b:
                r14.enforceInterface(r11)
                android.os.IBinder r0 = r27.getHwInnerService()
                r30.writeNoException()
                r15.writeStrongBinder(r0)
                return r16
            L_0x0029:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                java.lang.String r2 = r29.readString()
                int r3 = r29.readInt()
                if (r3 == 0) goto L_0x0043
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
                goto L_0x0044
            L_0x0043:
            L_0x0044:
                java.lang.String r3 = r12.createAppSpecificSmsToken(r0, r2, r1)
                r30.writeNoException()
                r15.writeString(r3)
                return r16
            L_0x004f:
                r14.enforceInterface(r11)
                int r7 = r29.readInt()
                java.lang.String r8 = r29.readString()
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x006a
                android.os.Parcelable$Creator r0 = android.net.Uri.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.net.Uri r0 = (android.net.Uri) r0
                r3 = r0
                goto L_0x006b
            L_0x006a:
                r3 = r1
            L_0x006b:
                java.lang.String r9 = r29.readString()
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.util.ArrayList r10 = r14.createTypedArrayList(r0)
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.util.ArrayList r17 = r14.createTypedArrayList(r0)
                r0 = r12
                r1 = r7
                r2 = r8
                r4 = r9
                r5 = r10
                r6 = r17
                r0.sendStoredMultipartText(r1, r2, r3, r4, r5, r6)
                r30.writeNoException()
                return r16
            L_0x0089:
                r14.enforceInterface(r11)
                int r7 = r29.readInt()
                java.lang.String r8 = r29.readString()
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x00a4
                android.os.Parcelable$Creator r0 = android.net.Uri.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.net.Uri r0 = (android.net.Uri) r0
                r3 = r0
                goto L_0x00a5
            L_0x00a4:
                r3 = r1
            L_0x00a5:
                java.lang.String r9 = r29.readString()
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x00b9
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.app.PendingIntent r0 = (android.app.PendingIntent) r0
                r5 = r0
                goto L_0x00ba
            L_0x00b9:
                r5 = r1
            L_0x00ba:
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x00ca
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.app.PendingIntent r0 = (android.app.PendingIntent) r0
                r6 = r0
                goto L_0x00cb
            L_0x00ca:
                r6 = r1
            L_0x00cb:
                r0 = r12
                r1 = r7
                r2 = r8
                r4 = r9
                r0.sendStoredText(r1, r2, r3, r4, r5, r6)
                r30.writeNoException()
                return r16
            L_0x00d6:
                r14.enforceInterface(r11)
                boolean r0 = r27.isSMSPromptEnabled()
                r30.writeNoException()
                r15.writeInt(r0)
                return r16
            L_0x00e4:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                java.lang.String r1 = r12.getImsSmsFormatForSubscriber(r0)
                r30.writeNoException()
                r15.writeString(r1)
                return r16
            L_0x00f6:
                r14.enforceInterface(r11)
                int r0 = r27.getPreferredSmsSubscription()
                r30.writeNoException()
                r15.writeInt(r0)
                return r16
            L_0x0104:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                boolean r1 = r12.isSmsSimPickActivityNeeded(r0)
                r30.writeNoException()
                r15.writeInt(r1)
                return r16
            L_0x0116:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                boolean r1 = r12.isImsSmsSupportedForSubscriber(r0)
                r30.writeNoException()
                r15.writeInt(r1)
                return r16
            L_0x0128:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                java.lang.String r1 = r29.readString()
                int r2 = r29.readInt()
                r12.setPremiumSmsPermissionForSubscriber(r0, r1, r2)
                r30.writeNoException()
                return r16
            L_0x013e:
                r14.enforceInterface(r11)
                java.lang.String r0 = r29.readString()
                int r1 = r29.readInt()
                r12.setPremiumSmsPermission(r0, r1)
                r30.writeNoException()
                return r16
            L_0x0150:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                java.lang.String r1 = r29.readString()
                int r2 = r12.getPremiumSmsPermissionForSubscriber(r0, r1)
                r30.writeNoException()
                r15.writeInt(r2)
                return r16
            L_0x0166:
                r14.enforceInterface(r11)
                java.lang.String r0 = r29.readString()
                int r1 = r12.getPremiumSmsPermission(r0)
                r30.writeNoException()
                r15.writeInt(r1)
                return r16
            L_0x0178:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                int r1 = r29.readInt()
                int r2 = r29.readInt()
                int r3 = r29.readInt()
                boolean r4 = r12.disableCellBroadcastRangeForSubscriber(r0, r1, r2, r3)
                r30.writeNoException()
                r15.writeInt(r4)
                return r16
            L_0x0196:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                int r1 = r29.readInt()
                int r2 = r29.readInt()
                int r3 = r29.readInt()
                boolean r4 = r12.enableCellBroadcastRangeForSubscriber(r0, r1, r2, r3)
                r30.writeNoException()
                r15.writeInt(r4)
                return r16
            L_0x01b4:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                int r1 = r29.readInt()
                int r2 = r29.readInt()
                boolean r3 = r12.disableCellBroadcastForSubscriber(r0, r1, r2)
                r30.writeNoException()
                r15.writeInt(r3)
                return r16
            L_0x01ce:
                r14.enforceInterface(r11)
                int r0 = r29.readInt()
                int r1 = r29.readInt()
                int r2 = r29.readInt()
                boolean r3 = r12.enableCellBroadcastForSubscriber(r0, r1, r2)
                r30.writeNoException()
                r15.writeInt(r3)
                return r16
            L_0x01e8:
                r14.enforceInterface(r11)
                int r17 = r29.readInt()
                java.lang.String r18 = r29.readString()
                java.lang.String r19 = r29.readString()
                java.lang.String r20 = r29.readString()
                java.util.ArrayList r21 = r29.createStringArrayList()
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.util.ArrayList r22 = r14.createTypedArrayList(r1)
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.util.ArrayList r23 = r14.createTypedArrayList(r1)
                int r1 = r29.readInt()
                if (r1 == 0) goto L_0x0214
                r8 = r16
                goto L_0x0215
            L_0x0214:
                r8 = r0
            L_0x0215:
                int r24 = r29.readInt()
                int r1 = r29.readInt()
                if (r1 == 0) goto L_0x0222
                r10 = r16
                goto L_0x0223
            L_0x0222:
                r10 = r0
            L_0x0223:
                int r25 = r29.readInt()
                r0 = r12
                r1 = r17
                r2 = r18
                r3 = r19
                r4 = r20
                r5 = r21
                r6 = r22
                r7 = r23
                r9 = r24
                r13 = r11
                r11 = r25
                r0.sendMultipartTextForSubscriberWithOptions(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r30.writeNoException()
                return r16
            L_0x0242:
                r13 = r11
                r14.enforceInterface(r13)
                int r9 = r29.readInt()
                java.lang.String r10 = r29.readString()
                java.lang.String r11 = r29.readString()
                java.lang.String r17 = r29.readString()
                java.util.ArrayList r18 = r29.createStringArrayList()
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.util.ArrayList r19 = r14.createTypedArrayList(r1)
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.util.ArrayList r20 = r14.createTypedArrayList(r1)
                int r1 = r29.readInt()
                if (r1 == 0) goto L_0x026f
                r8 = r16
                goto L_0x0270
            L_0x026f:
                r8 = r0
            L_0x0270:
                r0 = r12
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r17
                r5 = r18
                r6 = r19
                r7 = r20
                r0.sendMultipartTextForSubscriber(r1, r2, r3, r4, r5, r6, r7, r8)
                r30.writeNoException()
                return r16
            L_0x0283:
                r13 = r11
                r14.enforceInterface(r13)
                int r0 = r29.readInt()
                byte[] r2 = r29.createByteArray()
                java.lang.String r3 = r29.readString()
                int r4 = r29.readInt()
                if (r4 == 0) goto L_0x02a2
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
                goto L_0x02a3
            L_0x02a2:
            L_0x02a3:
                r12.injectSmsPduForSubscriber(r0, r2, r3, r1)
                r30.writeNoException()
                return r16
            L_0x02aa:
                r13 = r11
                r14.enforceInterface(r13)
                int r17 = r29.readInt()
                java.lang.String r18 = r29.readString()
                java.lang.String r19 = r29.readString()
                java.lang.String r20 = r29.readString()
                java.lang.String r21 = r29.readString()
                int r2 = r29.readInt()
                if (r2 == 0) goto L_0x02d2
                android.os.Parcelable$Creator r2 = android.app.PendingIntent.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r14)
                android.app.PendingIntent r2 = (android.app.PendingIntent) r2
                r6 = r2
                goto L_0x02d3
            L_0x02d2:
                r6 = r1
            L_0x02d3:
                int r2 = r29.readInt()
                if (r2 == 0) goto L_0x02e3
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
            L_0x02e1:
                r7 = r1
                goto L_0x02e4
            L_0x02e3:
                goto L_0x02e1
            L_0x02e4:
                int r1 = r29.readInt()
                if (r1 == 0) goto L_0x02ed
                r8 = r16
                goto L_0x02ee
            L_0x02ed:
                r8 = r0
            L_0x02ee:
                int r22 = r29.readInt()
                int r1 = r29.readInt()
                if (r1 == 0) goto L_0x02fb
                r10 = r16
                goto L_0x02fc
            L_0x02fb:
                r10 = r0
            L_0x02fc:
                int r23 = r29.readInt()
                r0 = r12
                r1 = r17
                r2 = r18
                r3 = r19
                r4 = r20
                r5 = r21
                r9 = r22
                r11 = r23
                r0.sendTextForSubscriberWithOptions(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r30.writeNoException()
                return r16
            L_0x0316:
                r13 = r11
                r14.enforceInterface(r13)
                int r9 = r29.readInt()
                java.lang.String r10 = r29.readString()
                java.lang.String r11 = r29.readString()
                java.lang.String r17 = r29.readString()
                java.lang.String r18 = r29.readString()
                int r2 = r29.readInt()
                if (r2 == 0) goto L_0x033e
                android.os.Parcelable$Creator r2 = android.app.PendingIntent.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r14)
                android.app.PendingIntent r2 = (android.app.PendingIntent) r2
                r6 = r2
                goto L_0x033f
            L_0x033e:
                r6 = r1
            L_0x033f:
                int r2 = r29.readInt()
                if (r2 == 0) goto L_0x034f
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
            L_0x034d:
                r7 = r1
                goto L_0x0350
            L_0x034f:
                goto L_0x034d
            L_0x0350:
                int r1 = r29.readInt()
                if (r1 == 0) goto L_0x0359
                r8 = r16
                goto L_0x035a
            L_0x0359:
                r8 = r0
            L_0x035a:
                r0 = r12
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r17
                r5 = r18
                r0.sendTextForSubscriberWithSelfPermissions(r1, r2, r3, r4, r5, r6, r7, r8)
                r30.writeNoException()
                return r16
            L_0x0369:
                r13 = r11
                r14.enforceInterface(r13)
                int r9 = r29.readInt()
                java.lang.String r10 = r29.readString()
                java.lang.String r11 = r29.readString()
                java.lang.String r17 = r29.readString()
                java.lang.String r18 = r29.readString()
                int r2 = r29.readInt()
                if (r2 == 0) goto L_0x0391
                android.os.Parcelable$Creator r2 = android.app.PendingIntent.CREATOR
                java.lang.Object r2 = r2.createFromParcel(r14)
                android.app.PendingIntent r2 = (android.app.PendingIntent) r2
                r6 = r2
                goto L_0x0392
            L_0x0391:
                r6 = r1
            L_0x0392:
                int r2 = r29.readInt()
                if (r2 == 0) goto L_0x03a2
                android.os.Parcelable$Creator r1 = android.app.PendingIntent.CREATOR
                java.lang.Object r1 = r1.createFromParcel(r14)
                android.app.PendingIntent r1 = (android.app.PendingIntent) r1
            L_0x03a0:
                r7 = r1
                goto L_0x03a3
            L_0x03a2:
                goto L_0x03a0
            L_0x03a3:
                int r1 = r29.readInt()
                if (r1 == 0) goto L_0x03ac
                r8 = r16
                goto L_0x03ad
            L_0x03ac:
                r8 = r0
            L_0x03ad:
                r0 = r12
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r17
                r5 = r18
                r0.sendTextForSubscriber(r1, r2, r3, r4, r5, r6, r7, r8)
                r30.writeNoException()
                return r16
            L_0x03bc:
                r13 = r11
                r14.enforceInterface(r13)
                int r9 = r29.readInt()
                java.lang.String r10 = r29.readString()
                java.lang.String r11 = r29.readString()
                java.lang.String r17 = r29.readString()
                int r18 = r29.readInt()
                byte[] r19 = r29.createByteArray()
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x03e8
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.app.PendingIntent r0 = (android.app.PendingIntent) r0
                r7 = r0
                goto L_0x03e9
            L_0x03e8:
                r7 = r1
            L_0x03e9:
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x03f9
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.app.PendingIntent r0 = (android.app.PendingIntent) r0
                r8 = r0
                goto L_0x03fa
            L_0x03f9:
                r8 = r1
            L_0x03fa:
                r0 = r12
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r17
                r5 = r18
                r6 = r19
                r0.sendDataForSubscriberWithSelfPermissions(r1, r2, r3, r4, r5, r6, r7, r8)
                r30.writeNoException()
                return r16
            L_0x040b:
                r13 = r11
                r14.enforceInterface(r13)
                int r9 = r29.readInt()
                java.lang.String r10 = r29.readString()
                java.lang.String r11 = r29.readString()
                java.lang.String r17 = r29.readString()
                int r18 = r29.readInt()
                byte[] r19 = r29.createByteArray()
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x0437
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.app.PendingIntent r0 = (android.app.PendingIntent) r0
                r7 = r0
                goto L_0x0438
            L_0x0437:
                r7 = r1
            L_0x0438:
                int r0 = r29.readInt()
                if (r0 == 0) goto L_0x0448
                android.os.Parcelable$Creator r0 = android.app.PendingIntent.CREATOR
                java.lang.Object r0 = r0.createFromParcel(r14)
                android.app.PendingIntent r0 = (android.app.PendingIntent) r0
                r8 = r0
                goto L_0x0449
            L_0x0448:
                r8 = r1
            L_0x0449:
                r0 = r12
                r1 = r9
                r2 = r10
                r3 = r11
                r4 = r17
                r5 = r18
                r6 = r19
                r0.sendDataForSubscriber(r1, r2, r3, r4, r5, r6, r7, r8)
                r30.writeNoException()
                return r16
            L_0x045a:
                r13 = r11
                r14.enforceInterface(r13)
                int r6 = r29.readInt()
                java.lang.String r7 = r29.readString()
                int r8 = r29.readInt()
                byte[] r9 = r29.createByteArray()
                byte[] r10 = r29.createByteArray()
                r0 = r12
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r10
                boolean r0 = r0.copyMessageToIccEfForSubscriber(r1, r2, r3, r4, r5)
                r30.writeNoException()
                r15.writeInt(r0)
                return r16
            L_0x0483:
                r13 = r11
                r14.enforceInterface(r13)
                int r6 = r29.readInt()
                java.lang.String r7 = r29.readString()
                int r8 = r29.readInt()
                int r9 = r29.readInt()
                byte[] r10 = r29.createByteArray()
                r0 = r12
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r10
                boolean r0 = r0.updateMessageOnIccEfForSubscriber(r1, r2, r3, r4, r5)
                r30.writeNoException()
                r15.writeInt(r0)
                return r16
            L_0x04ac:
                r13 = r11
                r14.enforceInterface(r13)
                int r0 = r29.readInt()
                java.lang.String r1 = r29.readString()
                java.util.List r2 = r12.getAllMessagesFromIccEfForSubscriber(r0, r1)
                r30.writeNoException()
                r15.writeTypedList(r2)
                return r16
            L_0x04c3:
                r13 = r11
                r15.writeString(r13)
                return r16
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ISms.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    boolean copyMessageToIccEfForSubscriber(int i, String str, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    String createAppSpecificSmsToken(int i, String str, PendingIntent pendingIntent) throws RemoteException;

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
}
