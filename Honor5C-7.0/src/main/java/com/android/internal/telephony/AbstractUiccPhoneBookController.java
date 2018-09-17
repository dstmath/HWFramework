package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.RemoteException;
import com.android.internal.telephony.IIccPhoneBook.Stub;

public abstract class AbstractUiccPhoneBookController extends Stub {
    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) throws RemoteException {
        return false;
    }

    public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
        return false;
    }

    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
        return false;
    }

    public boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
        return false;
    }

    public int getAdnCountHW() throws RemoteException {
        return 0;
    }

    public int getAdnCountUsingSubIdHW(int subId) throws RemoteException {
        return 0;
    }

    public int getAnrCountHW() throws RemoteException {
        return 0;
    }

    public int getAnrCountUsingSubIdHW(int subId) throws RemoteException {
        return 0;
    }

    public int getEmailCountHW() throws RemoteException {
        return 0;
    }

    public int getEmailCountUsingSubIdHW(int subId) throws RemoteException {
        return 0;
    }

    public int getSpareAnrCountHW() throws RemoteException {
        return 0;
    }

    public int getSpareAnrCountUsingSubIdHW(int subId) throws RemoteException {
        return 0;
    }

    public int getSpareEmailCountHW() throws RemoteException {
        return 0;
    }

    public int getSpareEmailCountUsingSubIdHW(int subId) throws RemoteException {
        return 0;
    }

    public int[] getRecordsSizeHW() throws RemoteException {
        return new int[0];
    }

    public int[] getRecordsSizeUsingSubIdHW(int subId) throws RemoteException {
        return new int[0];
    }

    public int getSpareExt1CountUsingSubIdHW(int subId) {
        return 0;
    }
}
