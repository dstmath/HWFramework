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
                    this.mRemote.transact(Stub.TRANSACTION_getAdnRecordsInEf, _data, _reply, 0);
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAdnRecordsInEfForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_updateAdnRecordsInEfBySearch, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateAdnRecordsInEfBySearchForSubscriber, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateAdnRecordsInEfByIndex, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateAdnRecordsInEfByIndexForSubscriber, _data, _reply, 0);
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

            public int[] getAdnRecordsSize(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    this.mRemote.transact(Stub.TRANSACTION_getAdnRecordsSize, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAdnRecordsSizeForSubscriber, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAlphaTagEncodingLength, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    if (values != null) {
                        _data.writeInt(Stub.TRANSACTION_getAdnRecordsInEf);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pin2);
                    this.mRemote.transact(Stub.TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchHW, _data, _reply, 0);
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

            public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (values != null) {
                        _data.writeInt(Stub.TRANSACTION_getAdnRecordsInEf);
                        values.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pin2);
                    this.mRemote.transact(Stub.TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateUsimAdnRecordsInEfByIndexHW, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateUsimAdnRecordsInEfByIndexUsingSubIdHW, _data, _reply, 0);
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

            public int getAdnCountHW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAdnCountHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAdnCountUsingSubIdHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAnrCountHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getAnrCountUsingSubIdHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getEmailCountHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getEmailCountUsingSubIdHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSpareAnrCountHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSpareAnrCountUsingSubIdHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSpareEmailCountHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSpareEmailCountUsingSubIdHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getRecordsSizeHW, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getRecordsSizeUsingSubIdHW, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getSpareExt1CountUsingSubIdHW, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
            List<AdnRecord> _result;
            boolean _result2;
            int[] _result3;
            int _result4;
            int _arg0;
            switch (code) {
                case TRANSACTION_getAdnRecordsInEf /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdnRecordsInEf(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_getAdnRecordsInEfForSubscriber /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdnRecordsInEfForSubscriber(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_updateAdnRecordsInEfBySearch /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfBySearch(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_updateAdnRecordsInEfBySearchForSubscriber /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfBySearchForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_updateAdnRecordsInEfByIndex /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfByIndex(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_updateAdnRecordsInEfByIndexForSubscriber /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfByIndexForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_getAdnRecordsSize /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAdnRecordsSize(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_getAdnRecordsSizeForSubscriber /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAdnRecordsSizeForSubscriber(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_getAlphaTagEncodingLength /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAlphaTagEncodingLength(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchHW /*10*/:
                    ContentValues contentValues;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        contentValues = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                    } else {
                        contentValues = null;
                    }
                    _result2 = updateAdnRecordsWithContentValuesInEfBySearchHW(_arg0, contentValues, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW /*11*/:
                    ContentValues contentValues2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        contentValues2 = (ContentValues) ContentValues.CREATOR.createFromParcel(data);
                    } else {
                        contentValues2 = null;
                    }
                    _result2 = updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(_arg0, _arg1, contentValues2, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_updateUsimAdnRecordsInEfByIndexHW /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateUsimAdnRecordsInEfByIndexHW(data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_updateUsimAdnRecordsInEfByIndexUsingSubIdHW /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateUsimAdnRecordsInEfByIndexUsingSubIdHW(data.readInt(), data.readInt(), data.readString(), data.readString(), data.createStringArray(), data.createStringArray(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getAdnRecordsInEf : 0);
                    return true;
                case TRANSACTION_getAdnCountHW /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnCountHW();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getAdnCountUsingSubIdHW /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnCountUsingSubIdHW(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getAnrCountHW /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAnrCountHW();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getAnrCountUsingSubIdHW /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAnrCountUsingSubIdHW(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getEmailCountHW /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getEmailCountHW();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getEmailCountUsingSubIdHW /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getEmailCountUsingSubIdHW(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getSpareAnrCountHW /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareAnrCountHW();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getSpareAnrCountUsingSubIdHW /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareAnrCountUsingSubIdHW(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getSpareEmailCountHW /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareEmailCountHW();
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getSpareEmailCountUsingSubIdHW /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareEmailCountUsingSubIdHW(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_getRecordsSizeHW /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getRecordsSizeHW();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_getRecordsSizeUsingSubIdHW /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getRecordsSizeUsingSubIdHW(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_getSpareExt1CountUsingSubIdHW /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSpareExt1CountUsingSubIdHW(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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
