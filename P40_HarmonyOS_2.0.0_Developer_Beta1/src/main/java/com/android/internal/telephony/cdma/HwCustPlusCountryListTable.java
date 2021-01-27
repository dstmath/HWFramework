package com.android.internal.telephony.cdma;

import android.telephony.Rlog;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public class HwCustPlusCountryListTable {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "CDMA-HwCustPlusCountryListTable";
    private static final HwCustMccIddNddSid[] MCC_IDD_NDD_SIDS = HwCustTelephonyPlusCode.MCC_IDD_NDD_SID_MAP_SUPPORT;
    private static final HwCustMccSidLtmOff[] MCC_SID_LTM_OFFS = HwCustTelephonyPlusCode.MCC_SID_LTM_OFF_MAP_SUPPORT;
    private static final int PARAM_FOR_OFFSET = 2;
    private static final HwCustPlusCountryListTable S_INSTANCE = new HwCustPlusCountryListTable();

    private HwCustPlusCountryListTable() {
    }

    public static HwCustPlusCountryListTable getInstance() {
        return S_INSTANCE;
    }

    public static HwCustMccIddNddSid getItemFromCountryListByMcc(String mccString) {
        Rlog.d(LOG_TAG, "plus: getItemFromCountryListByMcc mcc = " + mccString);
        int mcc = getIntFromString(mccString);
        HwCustMccIddNddSid[] hwCustMccIddNddSidArr = MCC_IDD_NDD_SIDS;
        for (HwCustMccIddNddSid item : hwCustMccIddNddSidArr) {
            if (mcc == item.Mcc) {
                Rlog.d(LOG_TAG, "plus: Now find mccIddNddSid = " + item);
                return item;
            }
        }
        Rlog.e(LOG_TAG, "plus: can't find one that match the Mcc");
        return null;
    }

    public static int getIntFromString(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
            return -1;
        }
    }

    public static ArrayList<HwCustMccSidLtmOff> getItemsFromSidConflictTableBySid(String ssid) {
        int sid = getIntFromString(ssid);
        ArrayList<HwCustMccSidLtmOff> itemList = new ArrayList<>();
        HwCustMccSidLtmOff[] hwCustMccSidLtmOffArr = MCC_SID_LTM_OFFS;
        for (HwCustMccSidLtmOff item : hwCustMccSidLtmOffArr) {
            if (sid == item.Sid) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    public static String getMccFromMainTableBySid(String ssid) {
        int sid = getIntFromString(ssid);
        String mcc = null;
        HwCustMccIddNddSid[] hwCustMccIddNddSidArr = MCC_IDD_NDD_SIDS;
        int length = hwCustMccIddNddSidArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            HwCustMccIddNddSid item = hwCustMccIddNddSidArr[i];
            if (sid <= item.SidMax && sid >= item.SidMin) {
                mcc = Integer.toString(item.Mcc);
                break;
            }
            i++;
        }
        Rlog.d(LOG_TAG, "plus: getMccFromMainTableBySid mcc = " + mcc);
        return mcc;
    }

    public static String getCcFromConflictTableByLTM(ArrayList<HwCustMccSidLtmOff> itemList, String sLtmOff) {
        Rlog.d(LOG_TAG, "plus:  getCcFromConflictTableByLTM sLtmOff = " + sLtmOff);
        if (itemList == null || itemList.size() == 0) {
            Rlog.e(LOG_TAG, "plus: [getCcFromConflictTableByLTM] please check the param ");
            return null;
        }
        String findMcc = null;
        try {
            int ltmOff = Integer.parseInt(sLtmOff);
            Iterator<HwCustMccSidLtmOff> it = itemList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HwCustMccSidLtmOff item = it.next();
                int max = item.LtmOffMax * 2;
                int min = item.LtmOffMin * 2;
                if (ltmOff <= max && ltmOff >= min) {
                    findMcc = Integer.toString(item.Mcc);
                    break;
                }
            }
            Rlog.d(LOG_TAG, "plus: find one that match the ltmOff mcc = " + findMcc);
            return findMcc;
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }
}
