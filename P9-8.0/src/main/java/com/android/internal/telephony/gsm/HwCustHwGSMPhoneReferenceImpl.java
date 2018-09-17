package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CsgSearch;
import com.android.internal.telephony.CsgSearchFactory;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.uicc.IccRecords;
import java.util.ArrayList;

public class HwCustHwGSMPhoneReferenceImpl extends HwCustHwGSMPhoneReference {
    private static final String LOG_TAG = "HwCustHwGSMPhoneReferenceImpl";
    private static final int format_Length = 2;
    private static final int search_Length = 4;
    private CsgSearch mCsgSrch;

    public HwCustHwGSMPhoneReferenceImpl(GsmCdmaPhone mGSMPhone) {
        super(mGSMPhone);
        if (CsgSearch.isSupportCsgSearch()) {
            this.mCsgSrch = CsgSearchFactory.createCsgSearch(mGSMPhone);
        } else {
            this.mCsgSrch = null;
        }
    }

    public String getCustOperatorNameBySpn(String rplmn, String tempName) {
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        Object obj = null;
        IccRecords r = (this.mPhone == null || this.mPhone.mIccRecords == null) ? null : (IccRecords) this.mPhone.mIccRecords.get();
        Object spnName = r != null ? r.getServiceProviderName() : null;
        Object mImsi = r != null ? r.getIMSI() : null;
        if (mContext != null) {
            try {
                obj = Systemex.getString(mContext.getContentResolver(), "hw_cust_custOperatorName");
                Rlog.d(LOG_TAG, "hw_cust_custOperatorName = " + obj);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got hw_cust_custOperatorName value", e);
            }
        }
        if (TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(obj) || TextUtils.isEmpty(mImsi)) {
            return tempName;
        }
        for (String item : obj.split(";")) {
            String[] plmns = item.split(",");
            if (4 == plmns.length && rplmn.equals(plmns[1]) && mImsi.startsWith(plmns[0])) {
                if (TextUtils.isEmpty(spnName)) {
                    tempName = plmns[3];
                } else {
                    tempName = spnName.concat(plmns[2]);
                }
            }
        }
        return tempName;
    }

    public String modifyTheFormatName(String rplmn, String tempName, String radioTechStr) {
        IccRecords r = (this.mPhone == null || this.mPhone.mIccRecords == null) ? null : (IccRecords) this.mPhone.mIccRecords.get();
        Object mImsi = r != null ? r.getIMSI() : null;
        Object obj = null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        String operatorName = tempName != null ? tempName.concat(radioTechStr) : null;
        if (mContext != null) {
            try {
                obj = Systemex.getString(mContext.getContentResolver(), "hw_cust_modifytheformat");
                Rlog.d(LOG_TAG, "hw_cust_modifytheformat = " + obj);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got hw_cust_modifytheformat value", e);
            }
        }
        if (TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(obj) || TextUtils.isEmpty(mImsi)) {
            return operatorName;
        }
        for (String item : obj.split(";")) {
            String[] plmns = item.split(",");
            if (2 == plmns.length && rplmn.equals(plmns[1]) && mImsi.startsWith(plmns[0]) && " 4G".equals(radioTechStr)) {
                operatorName = tempName.concat(" LTE");
            }
        }
        return operatorName;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00e5  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0123 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x011e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<OperatorInfo> filterActAndRepeatedItems(ArrayList<OperatorInfo> searchResult) {
        if (!SystemProperties.getBoolean("ro.config.hw_not_show_act", false)) {
            return searchResult;
        }
        String radioTechStr = "";
        ArrayList<OperatorInfo> custResults = new ArrayList();
        if (searchResult == null) {
            return custResults;
        }
        int list_size = searchResult.size();
        for (int j = 0; j < list_size; j++) {
            OperatorInfo operatorInfo = (OperatorInfo) searchResult.get(j);
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: operatorInfo = " + operatorInfo);
            String longNameWithoutAct = operatorInfo.getOperatorAlphaLong();
            int lastSpaceIndexInLongName = operatorInfo.getOperatorAlphaLong().lastIndexOf(32);
            if (-1 != lastSpaceIndexInLongName) {
                radioTechStr = operatorInfo.getOperatorAlphaLong().substring(lastSpaceIndexInLongName);
                if (" 2G".equals(radioTechStr) || " 3G".equals(radioTechStr) || " 4G".equals(radioTechStr)) {
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong().replace(radioTechStr, "");
                }
            }
            OperatorInfo operatorInfo2 = null;
            String NumericRslt = operatorInfo.getOperatorNumeric();
            boolean isFound = false;
            int length = custResults.size();
            int i = 0;
            while (i < length) {
                OperatorInfo info = (OperatorInfo) custResults.get(i);
                String custNumeric = info.getOperatorNumeric();
                if (longNameWithoutAct == null || !longNameWithoutAct.equals(info.getOperatorAlphaLong())) {
                    i++;
                } else {
                    OperatorInfo operatorInfo3;
                    if (custNumeric != null && custNumeric.compareTo(NumericRslt) < 0) {
                        custResults.remove(i);
                        operatorInfo3 = new OperatorInfo(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
                    }
                    isFound = true;
                    if (!isFound) {
                        operatorInfo3 = new OperatorInfo(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
                    }
                    Rlog.d(LOG_TAG, "filterActAndRepeatedItems: rsltInfo = " + operatorInfo2);
                    if (operatorInfo2 == null) {
                        custResults.add(operatorInfo2);
                    }
                }
            }
            if (isFound) {
            }
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: rsltInfo = " + operatorInfo2);
            if (operatorInfo2 == null) {
            }
        }
        return custResults;
    }

    public void selectCsgNetworkManually(Message response) {
        if (this.mCsgSrch != null) {
            this.mCsgSrch.selectCsgNetworkManually(response);
        }
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        if (this.mCsgSrch != null) {
            this.mCsgSrch.judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public void registerForCsgRecordsLoadedEvent() {
        if (this.mCsgSrch != null) {
            this.mCsgSrch.registerForCsgRecordsLoadedEvent();
        }
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        if (this.mCsgSrch != null) {
            this.mCsgSrch.unregisterForCsgRecordsLoadedEvent();
        }
    }
}
