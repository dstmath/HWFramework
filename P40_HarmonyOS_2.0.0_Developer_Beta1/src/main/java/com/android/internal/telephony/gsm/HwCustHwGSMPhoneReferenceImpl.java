package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.SettingsEx;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CsgSearch;
import com.android.internal.telephony.CsgSearchFactory;
import com.android.internal.telephony.uicc.IccRecords;
import com.huawei.internal.telephony.OperatorInfoEx;
import com.huawei.internal.telephony.PhoneExt;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;

public class HwCustHwGSMPhoneReferenceImpl extends HwCustHwGSMPhoneReference {
    private static final String LOG_TAG = "HwCustHwGSMPhoneReferenceImpl";
    private static final int format_Length = 2;
    private static final int search_Length = 4;
    private CsgSearch mCsgSrch;

    public HwCustHwGSMPhoneReferenceImpl(PhoneExt phoneExt) {
        super(phoneExt);
        if (!CsgSearch.isSupportCsgSearch() || this.mPhone == null) {
            this.mCsgSrch = null;
        } else {
            this.mCsgSrch = CsgSearchFactory.createCsgSearch(this.mPhone);
        }
    }

    public String getCustOperatorNameBySpn(String rplmn, String tempName) {
        String mImsi = null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        String custOperatorName = null;
        IccRecords iccRecords = this.mPhone != null ? this.mPhone.getIccRecords() : null;
        String spnName = iccRecords != null ? iccRecords.getServiceProviderName() : null;
        if (iccRecords != null) {
            mImsi = iccRecords.getIMSI();
        }
        if (mContext != null) {
            try {
                custOperatorName = SettingsEx.Systemex.getString(mContext.getContentResolver(), "hw_cust_custOperatorName");
                Rlog.d(LOG_TAG, "hw_cust_custOperatorName = " + custOperatorName);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got hw_cust_custOperatorName value", e);
            }
        }
        if (TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(custOperatorName) || TextUtils.isEmpty(mImsi)) {
            return tempName;
        }
        String tempName2 = tempName;
        for (String item : custOperatorName.split(";")) {
            String[] plmns = item.split(",");
            if (4 == plmns.length && rplmn.equals(plmns[1]) && mImsi.startsWith(plmns[0])) {
                if (!TextUtils.isEmpty(spnName)) {
                    tempName2 = spnName.concat(plmns[2]);
                } else {
                    tempName2 = plmns[3];
                }
            }
        }
        return tempName2;
    }

    public String modifyTheFormatName(String rplmn, String tempName, String radioTechStr) {
        String operatorName = null;
        IccRecords iccRecords = this.mPhone != null ? this.mPhone.getIccRecords() : null;
        String mImsi = iccRecords != null ? iccRecords.getIMSI() : null;
        String modifyTheFormat = null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        if (tempName != null) {
            operatorName = tempName.concat(radioTechStr);
        }
        if (mContext != null) {
            try {
                modifyTheFormat = SettingsEx.Systemex.getString(mContext.getContentResolver(), "hw_cust_modifytheformat");
                Rlog.d(LOG_TAG, "hw_cust_modifytheformat = " + modifyTheFormat);
            } catch (Exception e) {
                Rlog.e(LOG_TAG, "Exception when got hw_cust_modifytheformat value", e);
            }
        }
        if (!TextUtils.isEmpty(rplmn) && !TextUtils.isEmpty(modifyTheFormat)) {
            if (!TextUtils.isEmpty(mImsi)) {
                String operatorName2 = operatorName;
                for (String item : modifyTheFormat.split(";")) {
                    String[] plmns = item.split(",");
                    if (2 == plmns.length) {
                        if (rplmn.equals(plmns[1]) && mImsi.startsWith(plmns[0])) {
                            if (" 4G".equals(radioTechStr)) {
                                operatorName2 = tempName.concat(" LTE");
                            }
                        }
                    }
                }
                return operatorName2;
            }
        }
        return operatorName;
    }

    public ArrayList<OperatorInfoEx> filterActAndRepeatedItems(ArrayList<OperatorInfoEx> searchResult) {
        Boolean removeAct;
        boolean removeActState = false;
        boolean hasHwCfgConfig = false;
        try {
            if (!(this.mPhone == null || (removeAct = (Boolean) HwCfgFilePolicy.getValue("remove_act", this.mPhone.getPhoneId(), Boolean.class)) == null)) {
                removeActState = removeAct.booleanValue();
                hasHwCfgConfig = true;
            }
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception when got remove_act value error:", e);
        }
        if (hasHwCfgConfig && !removeActState) {
            return searchResult;
        }
        if (!SystemProperties.getBoolean("ro.config.hw_not_show_act", false) && !removeActState) {
            return searchResult;
        }
        ArrayList<OperatorInfoEx> custResults = new ArrayList<>();
        if (searchResult == null) {
            return custResults;
        }
        getCustResults(searchResult, custResults);
        return custResults;
    }

    private void getCustResults(ArrayList<OperatorInfoEx> searchResult, ArrayList<OperatorInfoEx> custResults) {
        int list_size = searchResult.size();
        for (int j = 0; j < list_size; j++) {
            OperatorInfoEx operatorInfo = searchResult.get(j);
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: operatorInfo = " + operatorInfo);
            OperatorInfoEx rsltInfo = filterRepeatedItem(custResults, operatorInfo, judgeWhetherContainXg(operatorInfo));
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: rsltInfo = " + rsltInfo);
            if (rsltInfo != null) {
                custResults.add(rsltInfo);
            }
        }
    }

    private OperatorInfoEx filterRepeatedItem(ArrayList<OperatorInfoEx> custResults, OperatorInfoEx operatorInfo, String longNameWithoutAct) {
        OperatorInfoEx rsltInfo = null;
        String NumericRslt = operatorInfo.getOperatorNumeric();
        String numericRsltWithoutAct = operatorInfo.getOperatorNumericWithoutAct();
        boolean isFound = false;
        int length = custResults.size();
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            OperatorInfoEx info = custResults.get(i);
            String custNumeric = info.getOperatorNumeric();
            String custNumericWithoutAct = info.getOperatorNumericWithoutAct();
            boolean isEqualCustNumericWithoutAct = true;
            boolean isEqualLongNameWithoutAct = longNameWithoutAct != null && longNameWithoutAct.equals(info.getOperatorAlphaLong());
            if (custNumericWithoutAct == null || !custNumericWithoutAct.equals(numericRsltWithoutAct)) {
                isEqualCustNumericWithoutAct = false;
            }
            if (!isEqualLongNameWithoutAct || !isEqualCustNumericWithoutAct) {
                i++;
            } else {
                if (custNumeric != null && custNumeric.compareTo(NumericRslt) < 0) {
                    Rlog.d(LOG_TAG, "filterActAndRepeatedItems:custNumeric = " + custNumeric + " NumericRslt = " + NumericRslt);
                    custResults.remove(i);
                    rsltInfo = OperatorInfoEx.makeOperatorInfoEx(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
                }
                isFound = true;
            }
        }
        if (!isFound) {
            return OperatorInfoEx.makeOperatorInfoEx(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
        }
        return rsltInfo;
    }

    private String judgeWhetherContainXg(OperatorInfoEx operatorInfo) {
        String longNameWithoutAct = operatorInfo.getOperatorAlphaLong();
        int lastSpaceIndexInLongName = operatorInfo.getOperatorAlphaLong().lastIndexOf(32);
        if (-1 == lastSpaceIndexInLongName) {
            return longNameWithoutAct;
        }
        String radioTechStr = operatorInfo.getOperatorAlphaLong().substring(lastSpaceIndexInLongName);
        if (" 2G".equals(radioTechStr) || " 3G".equals(radioTechStr) || " 4G".equals(radioTechStr)) {
            return operatorInfo.getOperatorAlphaLong().replace(radioTechStr, "");
        }
        return longNameWithoutAct;
    }

    public void selectCsgNetworkManually(Message response) {
        CsgSearch csgSearch = this.mCsgSrch;
        if (csgSearch != null) {
            csgSearch.selectCsgNetworkManually(response);
        }
    }

    public void judgeToLaunchCsgPeriodicSearchTimer() {
        CsgSearch csgSearch = this.mCsgSrch;
        if (csgSearch != null) {
            csgSearch.judgeToLaunchCsgPeriodicSearchTimer();
        }
    }

    public void registerForCsgRecordsLoadedEvent() {
        CsgSearch csgSearch = this.mCsgSrch;
        if (csgSearch != null) {
            csgSearch.registerForCsgRecordsLoadedEvent();
        }
    }

    public void unregisterForCsgRecordsLoadedEvent() {
        CsgSearch csgSearch = this.mCsgSrch;
        if (csgSearch != null) {
            csgSearch.unregisterForCsgRecordsLoadedEvent();
        }
    }
}
