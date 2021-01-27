package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public interface IIccPhoneBook extends IInterface {
    int getAdnCountHW() throws RemoteException;

    int getAdnCountUsingSubIdHW(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsInEf(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsInEfForSubscriber(int i, int i2) throws RemoteException;

    int[] getAdnRecordsSize(int i) throws RemoteException;

    int[] getAdnRecordsSizeForSubscriber(int i, int i2) throws RemoteException;

    int getAlphaTagEncodingLength(String str) throws RemoteException;

    int getAnrCountHW() throws RemoteException;

    int getAnrCountUsingSubIdHW(int i) throws RemoteException;

    int getEmailCountHW() throws RemoteException;

    int getEmailCountUsingSubIdHW(int i) throws RemoteException;

    int[] getRecordsSizeHW() throws RemoteException;

    int[] getRecordsSizeUsingSubIdHW(int i) throws RemoteException;

    int getSpareAnrCountHW() throws RemoteException;

    int getSpareAnrCountUsingSubIdHW(int i) throws RemoteException;

    int getSpareEmailCountHW() throws RemoteException;

    int getSpareEmailCountUsingSubIdHW(int i) throws RemoteException;

    int getSpareExt1CountUsingSubIdHW(int i) throws RemoteException;

    boolean updateAdnRecordsInEfByIndex(int i, String str, String str2, int i2, String str3) throws RemoteException;

    boolean updateAdnRecordsInEfByIndexForSubscriber(int i, int i2, String str, String str2, int i3, String str3) throws RemoteException;

    boolean updateAdnRecordsInEfBySearch(int i, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateAdnRecordsInEfBySearchForSubscriber(int i, int i2, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int i, ContentValues contentValues, String str) throws RemoteException;

    boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int i, int i2, ContentValues contentValues, String str) throws RemoteException;

    boolean updateUsimAdnRecordsInEfByIndexHW(int i, String str, String str2, String[] strArr, String[] strArr2, int i2, int i3, String str3) throws RemoteException;

    boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int i, int i2, String str, String str2, String[] strArr, String[] strArr2, int i3, int i4, String str3) throws RemoteException;

    public static class Default implements IIccPhoneBook {
        @Override // com.android.internal.telephony.IIccPhoneBook
        public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int[] getAdnRecordsSize(int efid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getAlphaTagEncodingLength(String alphaTag) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getAdnCountHW() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getAdnCountUsingSubIdHW(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getAnrCountHW() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getAnrCountUsingSubIdHW(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getEmailCountHW() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getEmailCountUsingSubIdHW(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getSpareAnrCountHW() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getSpareAnrCountUsingSubIdHW(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getSpareEmailCountHW() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getSpareEmailCountUsingSubIdHW(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int[] getRecordsSizeHW() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int[] getRecordsSizeUsingSubIdHW(int subId) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int getSpareExt1CountUsingSubIdHW(int subId) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIccPhoneBook {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IIccPhoneBook";
        static final int TRANSACTION_getAdnCountHW = 14;
        static final int TRANSACTION_getAdnCountUsingSubIdHW = 15;
        static final int TRANSACTION_getAdnRecordsInEf = 1;
        static final int TRANSACTION_getAdnRecordsInEfForSubscriber = 2;
        static final int TRANSACTION_getAdnRecordsSize = 7;
        static final int TRANSACTION_getAdnRecordsSizeForSubscriber = 8;
        static final int TRANSACTION_getAlphaTagEncodingLength = 9;
        static final int TRANSACTION_getAnrCountHW = 16;
        static final int TRANSACTION_getAnrCountUsingSubIdHW = 17;
        static final int TRANSACTION_getEmailCountHW = 18;
        static final int TRANSACTION_getEmailCountUsingSubIdHW = 19;
        static final int TRANSACTION_getRecordsSizeHW = 24;
        static final int TRANSACTION_getRecordsSizeUsingSubIdHW = 25;
        static final int TRANSACTION_getSpareAnrCountHW = 20;
        static final int TRANSACTION_getSpareAnrCountUsingSubIdHW = 21;
        static final int TRANSACTION_getSpareEmailCountHW = 22;
        static final int TRANSACTION_getSpareEmailCountUsingSubIdHW = 23;
        static final int TRANSACTION_getSpareExt1CountUsingSubIdHW = 26;
        static final int TRANSACTION_updateAdnRecordsInEfByIndex = 5;
        static final int TRANSACTION_updateAdnRecordsInEfByIndexForSubscriber = 6;
        static final int TRANSACTION_updateAdnRecordsInEfBySearch = 3;
        static final int TRANSACTION_updateAdnRecordsInEfBySearchForSubscriber = 4;
        static final int TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchHW = 10;
        static final int TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW = 11;
        static final int TRANSACTION_updateUsimAdnRecordsInEfByIndexHW = 12;
        static final int TRANSACTION_updateUsimAdnRecordsInEfByIndexUsingSubIdHW = 13;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIccPhoneBook asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIccPhoneBook)) {
                return new Proxy(obj);
            }
            return (IIccPhoneBook) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ContentValues _arg1;
            ContentValues _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<AdnRecord> _result = getAdnRecordsInEf(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<AdnRecord> _result2 = getAdnRecordsInEfForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfBySearch = updateAdnRecordsInEfBySearch(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfBySearch ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfBySearchForSubscriber = updateAdnRecordsInEfBySearchForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfBySearchForSubscriber ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfByIndex = updateAdnRecordsInEfByIndex(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfByIndex ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfByIndexForSubscriber = updateAdnRecordsInEfByIndexForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfByIndexForSubscriber ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result3 = getAdnRecordsSize(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result3);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result4 = getAdnRecordsSizeForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result4);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getAlphaTagEncodingLength(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean updateAdnRecordsWithContentValuesInEfBySearchHW = updateAdnRecordsWithContentValuesInEfBySearchHW(_arg0, _arg1, data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsWithContentValuesInEfBySearchHW ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW = updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(_arg02, _arg12, _arg2, data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateUsimAdnRecordsInEfByIndexHW = updateUsimAdnRecordsInEfByIndexHW(data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateUsimAdnRecordsInEfByIndexHW ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW = updateUsimAdnRecordsInEfByIndexUsingSubIdHW(data.readInt(), data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateUsimAdnRecordsInEfByIndexUsingSubIdHW ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getAdnCountHW();
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getAdnCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getAnrCountHW();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getAnrCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getEmailCountHW();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getEmailCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getSpareAnrCountHW();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getSpareAnrCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getSpareEmailCountHW();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getSpareEmailCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result16 = getRecordsSizeHW();
                        reply.writeNoException();
                        reply.writeIntArray(_result16);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result17 = getRecordsSizeUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result17);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result18 = getSpareExt1CountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result18);
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
        public static class Proxy implements IIccPhoneBook {
            public static IIccPhoneBook sDefaultImpl;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsInEf(efid);
                    }
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsInEfForSubscriber(subId, efid);
                    }
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(oldTag);
                        try {
                            _data.writeString(oldPhoneNumber);
                            try {
                                _data.writeString(newTag);
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
                        _data.writeString(newPhoneNumber);
                        try {
                            _data.writeString(pin2);
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
                            boolean updateAdnRecordsInEfBySearch = Stub.getDefaultImpl().updateAdnRecordsInEfBySearch(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfBySearch;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
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
                        _data.writeInt(efid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(oldTag);
                        try {
                            _data.writeString(oldPhoneNumber);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(newTag);
                            _data.writeString(newPhoneNumber);
                            _data.writeString(pin2);
                            boolean _result = false;
                            if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateAdnRecordsInEfBySearchForSubscriber = Stub.getDefaultImpl().updateAdnRecordsInEfBySearchForSubscriber(subId, efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfBySearchForSubscriber;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newTag);
                        try {
                            _data.writeString(newPhoneNumber);
                            try {
                                _data.writeInt(index);
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
                        _data.writeString(pin2);
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateAdnRecordsInEfByIndex = Stub.getDefaultImpl().updateAdnRecordsInEfByIndex(efid, newTag, newPhoneNumber, index, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfByIndex;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
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
                        _data.writeInt(efid);
                        try {
                            _data.writeString(newTag);
                            try {
                                _data.writeString(newPhoneNumber);
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
                        _data.writeInt(index);
                        try {
                            _data.writeString(pin2);
                            boolean _result = false;
                            if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateAdnRecordsInEfByIndexForSubscriber = Stub.getDefaultImpl().updateAdnRecordsInEfByIndexForSubscriber(subId, efid, newTag, newPhoneNumber, index, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfByIndexForSubscriber;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int[] getAdnRecordsSize(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsSize(efid);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsSizeForSubscriber(subId, efid);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getAlphaTagEncodingLength(String alphaTag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alphaTag);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAlphaTagEncodingLength(alphaTag);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    boolean _result = true;
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pin2);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateAdnRecordsWithContentValuesInEfBySearchHW(efid, values, pin2);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    boolean _result = true;
                    if (values != null) {
                        _data.writeInt(1);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pin2);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(subId, efid, values, pin2);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newTag);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newPhoneNumber);
                        try {
                            _data.writeStringArray(newEmails);
                            _data.writeStringArray(newAnrNumbers);
                            _data.writeInt(sEf_id);
                            _data.writeInt(index);
                            _data.writeString(pin2);
                            boolean _result = false;
                            if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateUsimAdnRecordsInEfByIndexHW = Stub.getDefaultImpl().updateUsimAdnRecordsInEfByIndexHW(efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateUsimAdnRecordsInEfByIndexHW;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
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
                        _data.writeInt(efid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newTag);
                        _data.writeString(newPhoneNumber);
                        _data.writeStringArray(newEmails);
                        _data.writeStringArray(newAnrNumbers);
                        _data.writeInt(sEf_id);
                        _data.writeInt(index);
                        _data.writeString(pin2);
                        boolean _result = false;
                        if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = true;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW = Stub.getDefaultImpl().updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
                        _reply.recycle();
                        _data.recycle();
                        return updateUsimAdnRecordsInEfByIndexUsingSubIdHW;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getAdnCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnCountHW();
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getAdnCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnCountUsingSubIdHW(subId);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getAnrCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAnrCountHW();
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getAnrCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAnrCountUsingSubIdHW(subId);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getEmailCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEmailCountHW();
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getEmailCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEmailCountUsingSubIdHW(subId);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getSpareAnrCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSpareAnrCountHW();
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getSpareAnrCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSpareAnrCountUsingSubIdHW(subId);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getSpareEmailCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSpareEmailCountHW();
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getSpareEmailCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSpareEmailCountUsingSubIdHW(subId);
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int[] getRecordsSizeHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecordsSizeHW();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int[] getRecordsSizeUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecordsSizeUsingSubIdHW(subId);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int getSpareExt1CountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSpareExt1CountUsingSubIdHW(subId);
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

        public static boolean setDefaultImpl(IIccPhoneBook impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIccPhoneBook getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
