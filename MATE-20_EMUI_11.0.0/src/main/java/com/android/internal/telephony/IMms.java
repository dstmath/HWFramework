package com.android.internal.telephony;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMms extends IInterface {
    Uri addMultimediaMessageDraft(String str, Uri uri) throws RemoteException;

    Uri addTextMessageDraft(String str, String str2, String str3) throws RemoteException;

    boolean archiveStoredConversation(String str, long j, boolean z) throws RemoteException;

    boolean deleteStoredConversation(String str, long j) throws RemoteException;

    boolean deleteStoredMessage(String str, Uri uri) throws RemoteException;

    void downloadMessage(int i, String str, String str2, Uri uri, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    boolean getAutoPersisting() throws RemoteException;

    Bundle getCarrierConfigValues(int i) throws RemoteException;

    Uri importMultimediaMessage(String str, Uri uri, String str2, long j, boolean z, boolean z2) throws RemoteException;

    Uri importTextMessage(String str, String str2, int i, String str3, long j, boolean z, boolean z2) throws RemoteException;

    void sendMessage(int i, String str, Uri uri, String str2, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    void sendStoredMessage(int i, String str, Uri uri, Bundle bundle, PendingIntent pendingIntent) throws RemoteException;

    void setAutoPersisting(String str, boolean z) throws RemoteException;

    boolean updateStoredMessageStatus(String str, Uri uri, ContentValues contentValues) throws RemoteException;

    public static class Default implements IMms {
        @Override // com.android.internal.telephony.IMms
        public void sendMessage(int subId, String callingPkg, Uri contentUri, String locationUrl, Bundle configOverrides, PendingIntent sentIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IMms
        public void downloadMessage(int subId, String callingPkg, String locationUrl, Uri contentUri, Bundle configOverrides, PendingIntent downloadedIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IMms
        public Bundle getCarrierConfigValues(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IMms
        public Uri importTextMessage(String callingPkg, String address, int type, String text, long timestampMillis, boolean seen, boolean read) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IMms
        public Uri importMultimediaMessage(String callingPkg, Uri contentUri, String messageId, long timestampSecs, boolean seen, boolean read) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IMms
        public boolean deleteStoredMessage(String callingPkg, Uri messageUri) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IMms
        public boolean deleteStoredConversation(String callingPkg, long conversationId) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IMms
        public boolean updateStoredMessageStatus(String callingPkg, Uri messageUri, ContentValues statusValues) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IMms
        public boolean archiveStoredConversation(String callingPkg, long conversationId, boolean archived) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IMms
        public Uri addTextMessageDraft(String callingPkg, String address, String text) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IMms
        public Uri addMultimediaMessageDraft(String callingPkg, Uri contentUri) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IMms
        public void sendStoredMessage(int subId, String callingPkg, Uri messageUri, Bundle configOverrides, PendingIntent sentIntent) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IMms
        public void setAutoPersisting(String callingPkg, boolean enabled) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IMms
        public boolean getAutoPersisting() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMms {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IMms";
        static final int TRANSACTION_addMultimediaMessageDraft = 11;
        static final int TRANSACTION_addTextMessageDraft = 10;
        static final int TRANSACTION_archiveStoredConversation = 9;
        static final int TRANSACTION_deleteStoredConversation = 7;
        static final int TRANSACTION_deleteStoredMessage = 6;
        static final int TRANSACTION_downloadMessage = 2;
        static final int TRANSACTION_getAutoPersisting = 14;
        static final int TRANSACTION_getCarrierConfigValues = 3;
        static final int TRANSACTION_importMultimediaMessage = 5;
        static final int TRANSACTION_importTextMessage = 4;
        static final int TRANSACTION_sendMessage = 1;
        static final int TRANSACTION_sendStoredMessage = 12;
        static final int TRANSACTION_setAutoPersisting = 13;
        static final int TRANSACTION_updateStoredMessageStatus = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMms asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMms)) {
                return new Proxy(obj);
            }
            return (IMms) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "sendMessage";
                case 2:
                    return "downloadMessage";
                case 3:
                    return "getCarrierConfigValues";
                case 4:
                    return "importTextMessage";
                case 5:
                    return "importMultimediaMessage";
                case 6:
                    return "deleteStoredMessage";
                case 7:
                    return "deleteStoredConversation";
                case 8:
                    return "updateStoredMessageStatus";
                case 9:
                    return "archiveStoredConversation";
                case 10:
                    return "addTextMessageDraft";
                case 11:
                    return "addMultimediaMessageDraft";
                case 12:
                    return "sendStoredMessage";
                case 13:
                    return "setAutoPersisting";
                case 14:
                    return "getAutoPersisting";
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
            Uri _arg2;
            Bundle _arg4;
            PendingIntent _arg5;
            Uri _arg3;
            Bundle _arg42;
            PendingIntent _arg52;
            Uri _arg1;
            Uri _arg12;
            Uri _arg13;
            ContentValues _arg22;
            Uri _arg14;
            Uri _arg23;
            Bundle _arg32;
            PendingIntent _arg43;
            if (code != 1598968902) {
                boolean _arg15 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg16 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        String _arg33 = data.readString();
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
                        sendMessage(_arg0, _arg16, _arg2, _arg33, _arg4, _arg5);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        String _arg17 = data.readString();
                        String _arg24 = data.readString();
                        if (data.readInt() != 0) {
                            _arg3 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg42 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg52 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg52 = null;
                        }
                        downloadMessage(_arg02, _arg17, _arg24, _arg3, _arg42, _arg52);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result = getCarrierConfigValues(data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Uri _result2 = importTextMessage(data.readString(), data.readString(), data.readInt(), data.readString(), data.readLong(), data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        Uri _result3 = importMultimediaMessage(_arg03, _arg1, data.readString(), data.readLong(), data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean deleteStoredMessage = deleteStoredMessage(_arg04, _arg12);
                        reply.writeNoException();
                        reply.writeInt(deleteStoredMessage ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deleteStoredConversation = deleteStoredConversation(data.readString(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(deleteStoredConversation ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg22 = ContentValues.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        boolean updateStoredMessageStatus = updateStoredMessageStatus(_arg05, _arg13, _arg22);
                        reply.writeNoException();
                        reply.writeInt(updateStoredMessageStatus ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        long _arg18 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        boolean archiveStoredConversation = archiveStoredConversation(_arg06, _arg18, _arg15);
                        reply.writeNoException();
                        reply.writeInt(archiveStoredConversation ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        Uri _result4 = addTextMessageDraft(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        Uri _result5 = addMultimediaMessageDraft(_arg07, _arg14);
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        String _arg19 = data.readString();
                        if (data.readInt() != 0) {
                            _arg23 = Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg23 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg43 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg43 = null;
                        }
                        sendStoredMessage(_arg08, _arg19, _arg23, _arg32, _arg43);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg09 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = true;
                        }
                        setAutoPersisting(_arg09, _arg15);
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean autoPersisting = getAutoPersisting();
                        reply.writeNoException();
                        reply.writeInt(autoPersisting ? 1 : 0);
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
        public static class Proxy implements IMms {
            public static IMms sDefaultImpl;
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

            @Override // com.android.internal.telephony.IMms
            public void sendMessage(int subId, String callingPkg, Uri contentUri, String locationUrl, Bundle configOverrides, PendingIntent sentIntent) throws RemoteException {
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
                        if (contentUri != null) {
                            _data.writeInt(1);
                            contentUri.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(locationUrl);
                            if (configOverrides != null) {
                                _data.writeInt(1);
                                configOverrides.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (sentIntent != null) {
                                _data.writeInt(1);
                                sentIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendMessage(subId, callingPkg, contentUri, locationUrl, configOverrides, sentIntent);
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

            @Override // com.android.internal.telephony.IMms
            public void downloadMessage(int subId, String callingPkg, String locationUrl, Uri contentUri, Bundle configOverrides, PendingIntent downloadedIntent) throws RemoteException {
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
                            _data.writeString(locationUrl);
                            if (contentUri != null) {
                                _data.writeInt(1);
                                contentUri.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (configOverrides != null) {
                                _data.writeInt(1);
                                configOverrides.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (downloadedIntent != null) {
                                _data.writeInt(1);
                                downloadedIntent.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().downloadMessage(subId, callingPkg, locationUrl, contentUri, configOverrides, downloadedIntent);
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

            @Override // com.android.internal.telephony.IMms
            public Bundle getCarrierConfigValues(int subId) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCarrierConfigValues(subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.IMms
            public Uri importTextMessage(String callingPkg, String address, int type, String text, long timestampMillis, boolean seen, boolean read) throws RemoteException {
                Throwable th;
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPkg);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(address);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(type);
                        try {
                            _data.writeString(text);
                            _data.writeLong(timestampMillis);
                            int i = 1;
                            _data.writeInt(seen ? 1 : 0);
                            if (!read) {
                                i = 0;
                            }
                            _data.writeInt(i);
                            if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = Uri.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            Uri importTextMessage = Stub.getDefaultImpl().importTextMessage(callingPkg, address, type, text, timestampMillis, seen, read);
                            _reply.recycle();
                            _data.recycle();
                            return importTextMessage;
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

            @Override // com.android.internal.telephony.IMms
            public Uri importMultimediaMessage(String callingPkg, Uri contentUri, String messageId, long timestampSecs, boolean seen, boolean read) throws RemoteException {
                Throwable th;
                int i;
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(callingPkg);
                        i = 1;
                        if (contentUri != null) {
                            _data.writeInt(1);
                            contentUri.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(messageId);
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
                        _data.writeLong(timestampSecs);
                        _data.writeInt(seen ? 1 : 0);
                        if (!read) {
                            i = 0;
                        }
                        _data.writeInt(i);
                        if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = Uri.CREATOR.createFromParcel(_reply);
                            } else {
                                _result = null;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        Uri importMultimediaMessage = Stub.getDefaultImpl().importMultimediaMessage(callingPkg, contentUri, messageId, timestampSecs, seen, read);
                        _reply.recycle();
                        _data.recycle();
                        return importMultimediaMessage;
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

            @Override // com.android.internal.telephony.IMms
            public boolean deleteStoredMessage(String callingPkg, Uri messageUri) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    boolean _result = true;
                    if (messageUri != null) {
                        _data.writeInt(1);
                        messageUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteStoredMessage(callingPkg, messageUri);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IMms
            public boolean deleteStoredConversation(String callingPkg, long conversationId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeLong(conversationId);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteStoredConversation(callingPkg, conversationId);
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

            @Override // com.android.internal.telephony.IMms
            public boolean updateStoredMessageStatus(String callingPkg, Uri messageUri, ContentValues statusValues) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    boolean _result = true;
                    if (messageUri != null) {
                        _data.writeInt(1);
                        messageUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (statusValues != null) {
                        _data.writeInt(1);
                        statusValues.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateStoredMessageStatus(callingPkg, messageUri, statusValues);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IMms
            public boolean archiveStoredConversation(String callingPkg, long conversationId, boolean archived) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeLong(conversationId);
                    boolean _result = true;
                    _data.writeInt(archived ? 1 : 0);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().archiveStoredConversation(callingPkg, conversationId, archived);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IMms
            public Uri addTextMessageDraft(String callingPkg, String address, String text) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(address);
                    _data.writeString(text);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addTextMessageDraft(callingPkg, address, text);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.IMms
            public Uri addMultimediaMessageDraft(String callingPkg, Uri contentUri) throws RemoteException {
                Uri _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (contentUri != null) {
                        _data.writeInt(1);
                        contentUri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addMultimediaMessageDraft(callingPkg, contentUri);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Uri.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.IMms
            public void sendStoredMessage(int subId, String callingPkg, Uri messageUri, Bundle configOverrides, PendingIntent sentIntent) throws RemoteException {
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
                    if (configOverrides != null) {
                        _data.writeInt(1);
                        configOverrides.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (sentIntent != null) {
                        _data.writeInt(1);
                        sentIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendStoredMessage(subId, callingPkg, messageUri, configOverrides, sentIntent);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IMms
            public void setAutoPersisting(String callingPkg, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAutoPersisting(callingPkg, enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IMms
            public boolean getAutoPersisting() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAutoPersisting();
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
        }

        public static boolean setDefaultImpl(IMms impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMms getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
