package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentValues;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.Rlog;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public class UiccPhoneBookController extends IIccPhoneBook.Stub implements IUiccPhoneBookControllerInner {
    private static final String TAG = "UiccPhoneBookController";
    private IHwUiccPhoneBookControllerEx mHwUiccPhoneBookControllerEx = null;
    @UnsupportedAppUsage
    private Phone[] mPhone;

    @UnsupportedAppUsage
    public UiccPhoneBookController(Phone[] phone) {
        if (ServiceManager.getService("simphonebook") == null) {
            ServiceManager.addService("simphonebook", this);
        }
        this.mPhone = phone;
        this.mHwUiccPhoneBookControllerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwUiccPhoneBookController(this);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        return updateAdnRecordsInEfBySearchForSubscriber(getDefaultSubscription(), efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfBySearch(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfBySearch iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        return updateAdnRecordsInEfByIndexForSubscriber(getDefaultSubscription(), efid, newTag, newPhoneNumber, index, pin2);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.updateAdnRecordsInEfByIndex(efid, newTag, newPhoneNumber, index, pin2);
        }
        Rlog.e(TAG, "updateAdnRecordsInEfByIndex iccPbkIntMgr is null for Subscription:" + subId);
        return false;
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int[] getAdnRecordsSize(int efid) throws RemoteException {
        return getAdnRecordsSizeForSubscriber(getDefaultSubscription(), efid);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsSize(efid);
        }
        Rlog.e(TAG, "getAdnRecordsSize iccPbkIntMgr is null for Subscription:" + subId);
        return null;
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
        return getAdnRecordsInEfForSubscriber(getDefaultSubscription(), efid);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(subId);
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAdnRecordsInEf(efid);
        }
        Rlog.e(TAG, "getAdnRecordsInEf iccPbkIntMgr isnull for Subscription:" + subId);
        return null;
    }

    @UnsupportedAppUsage
    private IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager(int subId) {
        try {
            return this.mPhone[SubscriptionController.getInstance().getPhoneId(subId)].getIccPhoneBookInterfaceManager();
        } catch (NullPointerException e) {
            Rlog.e(TAG, "NullPointerException For subscription :" + subId);
            return null;
        } catch (ArrayIndexOutOfBoundsException e2) {
            Rlog.e(TAG, "ArrayIndexOutOfBoundsException For subscription :" + subId);
            return null;
        }
    }

    @UnsupportedAppUsage
    private int getDefaultSubscription() {
        return PhoneFactory.getDefaultSubscription();
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getAlphaTagEncodingLength(String alphaTag) {
        IccPhoneBookInterfaceManager iccPbkIntMgr = getIccPhoneBookInterfaceManager(getDefaultSubscription());
        if (iccPbkIntMgr != null) {
            return iccPbkIntMgr.getAlphaTagEncodingLength(alphaTag);
        }
        Rlog.e(TAG, "getAlphaTagEncodingLength iccPbkIntMgr is null for Subscription:" + getDefaultSubscription());
        return 0;
    }

    @Override // com.android.internal.telephony.IUiccPhoneBookControllerInner
    public IIccPhoneBookInterfaceManagerInner getIccPhoneBookInterfaceManagerHw(int subId) {
        return getIccPhoneBookInterfaceManager(subId);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) throws RemoteException {
        return updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(getDefaultSubscription(), efid, values, pin2);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(subId, efid, values, pin2);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
        return updateUsimAdnRecordsInEfByIndexUsingSubIdHW(getDefaultSubscription(), efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.updateUsimAdnRecordsInEfByIndexUsingSubIdHW(subId, efid, newTag, newPhoneNumber, newEmails, newAnrNumbers, sEf_id, index, pin2);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getAdnCountHW() throws RemoteException {
        return getAdnCountUsingSubIdHW(getDefaultSubscription());
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getAdnCountUsingSubIdHW(int subId) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.getAdnCountUsingSubIdHW(subId);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getAnrCountHW() throws RemoteException {
        return getAnrCountUsingSubIdHW(getDefaultSubscription());
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getAnrCountUsingSubIdHW(int subId) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.getAnrCountUsingSubIdHW(subId);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getEmailCountHW() throws RemoteException {
        return getEmailCountUsingSubIdHW(getDefaultSubscription());
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getEmailCountUsingSubIdHW(int subId) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.getEmailCountUsingSubIdHW(subId);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getSpareAnrCountHW() throws RemoteException {
        return getSpareAnrCountUsingSubIdHW(getDefaultSubscription());
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getSpareAnrCountUsingSubIdHW(int subId) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.getSpareAnrCountUsingSubIdHW(subId);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getSpareEmailCountHW() throws RemoteException {
        return getSpareEmailCountUsingSubIdHW(getDefaultSubscription());
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getSpareEmailCountUsingSubIdHW(int subId) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.getSpareEmailCountUsingSubIdHW(subId);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int[] getRecordsSizeHW() throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.getRecordsSizeUsingSubIdHW(getDefaultSubscription());
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int[] getRecordsSizeUsingSubIdHW(int subId) throws RemoteException {
        return this.mHwUiccPhoneBookControllerEx.getRecordsSizeUsingSubIdHW(subId);
    }

    @Override // com.android.internal.telephony.IIccPhoneBook
    public int getSpareExt1CountUsingSubIdHW(int subId) {
        return this.mHwUiccPhoneBookControllerEx.getSpareExt1CountUsingSubIdHW(subId);
    }
}
