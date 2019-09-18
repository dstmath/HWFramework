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

        private static class Proxy implements IIccPhoneBook {
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

            public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(AdnRecord.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(AdnRecord.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    _data.writeString(oldTag);
                    _data.writeString(oldPhoneNumber);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeString(pin2);
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

            public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    _data.writeString(oldTag);
                    _data.writeString(oldPhoneNumber);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeString(pin2);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeInt(index);
                    _data.writeString(pin2);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
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

            public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeInt(index);
                    _data.writeString(pin2);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
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

            public int[] getAdnRecordsSize(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAlphaTagEncodingLength(String alphaTag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alphaTag);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(10, _data, _reply, 0);
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
                    this.mRemote.transact(11, _data, _reply, 0);
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

            public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeStringArray(newEmails);
                    _data.writeStringArray(newAnrNumbers);
                    _data.writeInt(sEf_id);
                    _data.writeInt(index);
                    _data.writeString(pin2);
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

            public boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    _data.writeString(newTag);
                    _data.writeString(newPhoneNumber);
                    _data.writeStringArray(newEmails);
                    _data.writeStringArray(newAnrNumbers);
                    _data.writeInt(sEf_id);
                    _data.writeInt(index);
                    _data.writeString(pin2);
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

            public int getAdnCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAdnCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAnrCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAnrCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getEmailCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getEmailCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareAnrCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareAnrCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareEmailCountHW() throws RemoteException {
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

            public int getSpareEmailCountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRecordsSizeHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRecordsSizeUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSpareExt1CountUsingSubIdHW(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ContentValues _arg2 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<AdnRecord> _result = getAdnRecordsInEf(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<AdnRecord> _result2 = getAdnRecordsInEfForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = updateAdnRecordsInEfBySearch(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = updateAdnRecordsInEfBySearchForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = updateAdnRecordsInEfByIndex(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result6 = updateAdnRecordsInEfByIndexForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result7 = getAdnRecordsSize(data.readInt());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result7);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result8 = getAdnRecordsSizeForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result8);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result9 = getAlphaTagEncodingLength(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (ContentValues) ContentValues.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result10 = updateAdnRecordsWithContentValuesInEfBySearchHW(_arg0, _arg2, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = (ContentValues) ContentValues.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result11 = updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(_arg02, _arg1, _arg2, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = updateUsimAdnRecordsInEfByIndexHW(data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result13 = updateUsimAdnRecordsInEfByIndexUsingSubIdHW(data.readInt(), data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result14 = getAdnCountHW();
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result15 = getAdnCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result16 = getAnrCountHW();
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result17 = getAnrCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result18 = getEmailCountHW();
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result19 = getEmailCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result20 = getSpareAnrCountHW();
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result21 = getSpareAnrCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result22 = getSpareEmailCountHW();
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result23 = getSpareEmailCountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result24 = getRecordsSizeHW();
                        reply.writeNoException();
                        parcel2.writeIntArray(_result24);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int[] _result25 = getRecordsSizeUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        parcel2.writeIntArray(_result25);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result26 = getSpareExt1CountUsingSubIdHW(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result26);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

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
}
