package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.RemoteException;
import com.android.internal.telephony.IIccPhoneBook;

public abstract class AbstractUiccPhoneBookController extends IIccPhoneBook.Stub {
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
        return new int[0];
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int[] getRecordsSizeUsingSubIdHW(int subId) throws RemoteException {
        return new int[0];
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getSpareExt1CountUsingSubIdHW(int subId) {
        return 0;
    }
}
