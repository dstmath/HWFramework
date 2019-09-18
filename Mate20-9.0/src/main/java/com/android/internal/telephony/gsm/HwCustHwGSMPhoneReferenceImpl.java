package com.android.internal.telephony.gsm;

import android.content.Context;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.SettingsEx;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.CsgSearch;
import com.android.internal.telephony.CsgSearchFactory;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.uicc.IccRecords;
import huawei.cust.HwCfgFilePolicy;
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
        String mImsi = null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        String custOperatorName = null;
        IccRecords r = (this.mPhone == null || this.mPhone.mIccRecords == null) ? null : (IccRecords) this.mPhone.mIccRecords.get();
        String spnName = r != null ? r.getServiceProviderName() : null;
        if (r != null) {
            mImsi = r.getIMSI();
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
        String str = tempName;
        String operatorName = null;
        IccRecords r = (this.mPhone == null || this.mPhone.mIccRecords == null) ? null : (IccRecords) this.mPhone.mIccRecords.get();
        String mImsi = r != null ? r.getIMSI() : null;
        String modifyTheFormat = null;
        Context mContext = this.mPhone != null ? this.mPhone.getContext() : null;
        if (str != null) {
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
        if (TextUtils.isEmpty(rplmn) || TextUtils.isEmpty(modifyTheFormat) || TextUtils.isEmpty(mImsi)) {
            String str2 = rplmn;
            String str3 = radioTechStr;
            return operatorName;
        }
        String operatorName2 = operatorName;
        for (String item : modifyTheFormat.split(";")) {
            String[] plmns = item.split(",");
            if (2 == plmns.length) {
                if (rplmn.equals(plmns[1]) && mImsi.startsWith(plmns[0])) {
                    if (" 4G".equals(radioTechStr)) {
                        operatorName2 = str.concat(" LTE");
                    }
                }
            } else {
                String str4 = rplmn;
            }
            String str5 = radioTechStr;
        }
        String str6 = rplmn;
        String str7 = radioTechStr;
        return operatorName2;
    }

    public ArrayList<OperatorInfo> filterActAndRepeatedItems(ArrayList<OperatorInfo> searchResult) {
        String radioTechStr;
        boolean removeActState;
        int length;
        ArrayList<OperatorInfo> arrayList = searchResult;
        boolean removeActState2 = false;
        boolean hasHwCfgConfig = false;
        try {
            if (this.mPhone != null) {
                Boolean removeAct = (Boolean) HwCfgFilePolicy.getValue("remove_act", SubscriptionManager.getSlotIndex(this.mPhone.getPhoneId()), Boolean.class);
                if (removeAct != null) {
                    removeActState2 = removeAct.booleanValue();
                    hasHwCfgConfig = true;
                }
            }
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception when got remove_act value error:", e);
        }
        if (hasHwCfgConfig && !removeActState2) {
            return arrayList;
        }
        if (!SystemProperties.getBoolean("ro.config.hw_not_show_act", false) && !removeActState2) {
            return arrayList;
        }
        String radioTechStr2 = "";
        ArrayList<OperatorInfo> custResults = new ArrayList<>();
        if (arrayList == null) {
            return custResults;
        }
        int j = 0;
        int list_size = searchResult.size();
        while (j < list_size) {
            OperatorInfo operatorInfo = arrayList.get(j);
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: operatorInfo = " + operatorInfo);
            String longNameWithoutAct = operatorInfo.getOperatorAlphaLong();
            int lastSpaceIndexInLongName = operatorInfo.getOperatorAlphaLong().lastIndexOf(32);
            if (-1 != lastSpaceIndexInLongName) {
                radioTechStr2 = operatorInfo.getOperatorAlphaLong().substring(lastSpaceIndexInLongName);
                if (" 2G".equals(radioTechStr2) || " 3G".equals(radioTechStr2) || " 4G".equals(radioTechStr2)) {
                    longNameWithoutAct = operatorInfo.getOperatorAlphaLong().replace(radioTechStr2, "");
                }
            }
            OperatorInfo rsltInfo = null;
            String NumericRslt = operatorInfo.getOperatorNumeric();
            String NumericRsltWithoutAct = operatorInfo.getOperatorNumericWithoutAct();
            boolean isFound = false;
            int length2 = custResults.size();
            int i = 0;
            while (true) {
                radioTechStr = radioTechStr2;
                int i2 = i;
                if (i2 >= length2) {
                    removeActState = removeActState2;
                    int i3 = length2;
                    break;
                }
                OperatorInfo info = custResults.get(i2);
                String custNumeric = info.getOperatorNumeric();
                removeActState = removeActState2;
                String custNumericWithoutAct = info.getOperatorNumericWithoutAct();
                if (longNameWithoutAct != null) {
                    length = length2;
                    if (!(longNameWithoutAct.equals(info.getOperatorAlphaLong()) == 0 || custNumericWithoutAct == null || !custNumericWithoutAct.equals(NumericRsltWithoutAct))) {
                        if (custNumeric == null || custNumeric.compareTo(NumericRslt) >= 0) {
                            String str = custNumeric;
                            String str2 = custNumericWithoutAct;
                        } else {
                            OperatorInfo operatorInfo2 = info;
                            OperatorInfo info2 = new StringBuilder();
                            String str3 = custNumericWithoutAct;
                            info2.append("filterActAndRepeatedItems: custNumeric=");
                            info2.append(custNumeric);
                            info2.append(" NumericRslt=");
                            info2.append(NumericRslt);
                            Rlog.d(LOG_TAG, info2.toString());
                            custResults.remove(i2);
                            String str4 = custNumeric;
                            rsltInfo = new OperatorInfo(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
                        }
                        isFound = true;
                    }
                } else {
                    length = length2;
                }
                i = i2 + 1;
                radioTechStr2 = radioTechStr;
                removeActState2 = removeActState;
                length2 = length;
                ArrayList<OperatorInfo> arrayList2 = searchResult;
            }
            if (!isFound) {
                rsltInfo = new OperatorInfo(longNameWithoutAct, operatorInfo.getOperatorAlphaShort(), operatorInfo.getOperatorNumeric(), operatorInfo.getState());
            }
            Rlog.d(LOG_TAG, "filterActAndRepeatedItems: rsltInfo = " + rsltInfo);
            if (rsltInfo != null) {
                custResults.add(rsltInfo);
            }
            j++;
            radioTechStr2 = radioTechStr;
            removeActState2 = removeActState;
            arrayList = searchResult;
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
