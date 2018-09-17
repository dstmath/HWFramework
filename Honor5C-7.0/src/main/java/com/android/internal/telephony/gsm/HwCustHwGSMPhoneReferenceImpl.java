package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CsgSearch;
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
            this.mCsgSrch = new CsgSearch(mGSMPhone);
        } else {
            this.mCsgSrch = null;
        }
    }

    public String getCustOperatorNameBySpn(String rplmn, String tempName) {
        Context context = this.mPhone != null ? this.mPhone.getContext() : null;
        Object obj = null;
        IccRecords r = (this.mPhone == null || this.mPhone.mIccRecords == null) ? null : (IccRecords) this.mPhone.mIccRecords.get();
        Object serviceProviderName = r != null ? r.getServiceProviderName() : null;
        Object imsi = r != null ? r.getIMSI() : null;
        if (context != null) {
            try {
                obj = Systemex.getString(context.getContentResolver(), "hw_cust_custOperatorName");
                Rlog.d(LOG_TAG, "hw_cust_custOperatorName = " + obj);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got hw_cust_custOperatorName value", e);
            }
        }
        if (TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(r0) || TextUtils.isEmpty(imsi)) {
            return tempName;
        }
        for (String item : r0.split(";")) {
            String[] plmns = item.split(",");
            if (search_Length == plmns.length && rplmn.equals(plmns[1]) && imsi.startsWith(plmns[0])) {
                if (TextUtils.isEmpty(serviceProviderName)) {
                    tempName = plmns[3];
                } else {
                    tempName = serviceProviderName.concat(plmns[format_Length]);
                }
            }
        }
        return tempName;
    }

    public String modifyTheFormatName(String rplmn, String tempName, String radioTechStr) {
        IccRecords r = (this.mPhone == null || this.mPhone.mIccRecords == null) ? null : (IccRecords) this.mPhone.mIccRecords.get();
        Object imsi = r != null ? r.getIMSI() : null;
        Object obj = null;
        Context context = this.mPhone != null ? this.mPhone.getContext() : null;
        String concat = tempName != null ? tempName.concat(radioTechStr) : null;
        if (context != null) {
            try {
                obj = Systemex.getString(context.getContentResolver(), "hw_cust_modifytheformat");
                Rlog.d(LOG_TAG, "hw_cust_modifytheformat = " + obj);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got hw_cust_modifytheformat value", e);
            }
        }
        if (TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(r6) || TextUtils.isEmpty(imsi)) {
            return concat;
        }
        for (String item : r6.split(";")) {
            String[] plmns = item.split(",");
            if (format_Length == plmns.length && rplmn.equals(plmns[1]) && imsi.startsWith(plmns[0]) && " 4G".equals(radioTechStr)) {
                concat = tempName.concat(" LTE");
            }
        }
        return concat;
    }

    public ArrayList<OperatorInfo> filterActAndRepeatedItems(ArrayList<OperatorInfo> searchResult) {
        if (!SystemProperties.getBoolean("ro.config.hw_not_show_act", false)) {
            return searchResult;
        }
        String radioTechStr = "";
        ArrayList<OperatorInfo> custResults = new ArrayList();
        for (OperatorInfo operatorInfo : searchResult) {
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: operatorInfo = " + operatorInfo);
            String longNameWithoutAct = operatorInfo.getOperatorAlphaLong();
            int lastSpaceIndexInLongName = operatorInfo.getOperatorAlphaLong().lastIndexOf(32);
            if (-1 != lastSpaceIndexInLongName) {
                radioTechStr = operatorInfo.getOperatorAlphaLong().substring(lastSpaceIndexInLongName);
                if (" 2G".equals(radioTechStr) || " 3G".equals(radioTechStr) || " 4G".equals(radioTechStr)) {
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong().replace(radioTechStr, "");
                }
            }
            Object obj = null;
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
                    if (custNumeric != null && custNumeric.compareTo(NumericRslt) < 0) {
                        custResults.remove(i);
                        obj = new OperatorInfo(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
                    }
                    isFound = true;
                    if (!isFound) {
                        obj = new OperatorInfo(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
                    }
                    Rlog.d(LOG_TAG, "filterActAndRepeatedItems: rsltInfo = " + obj);
                    if (obj != null) {
                        custResults.add(obj);
                    }
                }
            }
            if (isFound) {
                obj = new OperatorInfo(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
            }
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: rsltInfo = " + obj);
            if (obj != null) {
                custResults.add(obj);
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
