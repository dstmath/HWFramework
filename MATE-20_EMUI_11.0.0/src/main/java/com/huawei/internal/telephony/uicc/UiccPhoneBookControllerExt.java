package com.huawei.internal.telephony.uicc;

import android.content.ContentValues;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.IIccPhoneBook;
import java.util.List;

public class UiccPhoneBookControllerExt {
    private static final String TAG = "UiccPhoneBookControllerExt";

    public static List<AdnRecordExt> getAdnRecordsInEfForSubscriberHw(int efType, int subId) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return AdnRecordExt.convertAdnRecordToExt(iccIpb.getAdnRecordsInEfForSubscriber(subId, efType));
            }
            return null;
        } catch (RemoteException e) {
            return null;
        } catch (SecurityException e2) {
            log("SecurityException catched!");
            return null;
        }
    }

    public static boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsInEfBySearchForSubscriber(subId, efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
            }
            return false;
        } catch (RemoteException e) {
            log("RemoteException catched!");
            return false;
        } catch (SecurityException e2) {
            log("SecurityException catched!");
            return false;
        }
    }

    public static boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsInEfByIndexForSubscriber(subId, efid, newTag, newPhoneNumber, index, pin2);
            }
            return false;
        } catch (RemoteException e) {
            log("RemoteException catched!");
            return false;
        } catch (SecurityException e2) {
            log("SecurityException catched!");
            return false;
        }
    }

    private static void log(String msg) {
        Rlog.i(TAG, msg);
    }

    public static boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(subId, efid, values, pin2);
            }
            return false;
        } catch (RemoteException e) {
            log("RemoteException catched!");
            return false;
        } catch (SecurityException e2) {
            log("SecurityException catched!");
            return false;
        }
    }

    public static boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        try {
            IIccPhoneBook iccIpb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService("simphonebook"));
            if (iccIpb != null) {
                return iccIpb.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
            }
            return false;
        } catch (RemoteException e) {
            log("RemoteException catched!");
            return false;
        } catch (SecurityException e2) {
            log("SecurityException catched!");
            return false;
        }
    }
}
