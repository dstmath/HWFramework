package com.android.internal.telephony.cdma;

import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.google.android.mms.pdu.PduPart;
import java.util.ArrayList;

public class HwCustPlusAndIddNddConvertUtils {
    private static final boolean DBG_NUM = false;
    static final String LOG_TAG = "HwCustPlusAndIddNddConvertUtils";
    public static final String PLUS_PREFIX = "+";
    private static HwCustPlusCountryListTable plusCountryList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils.<clinit>():void");
    }

    private static HwCustMccIddNddSid getNetworkMccIddList() {
        HwCustMccIddNddSid mccIddNddSid = null;
        if (plusCountryList != null) {
            String findMcc = SystemProperties.get("ril.currMcc", "");
            if (findMcc == null || TextUtils.isEmpty(findMcc)) {
                Rlog.e(LOG_TAG, "plus: getNetworkMccIddList could not find mcc in ril.curMcc");
                return null;
            }
            HwCustPlusCountryListTable hwCustPlusCountryListTable = plusCountryList;
            mccIddNddSid = HwCustPlusCountryListTable.getItemFromCountryListByMcc(findMcc);
        }
        Rlog.d(LOG_TAG, "plus: getNetworkMccIddList mccIddNddSid = " + mccIddNddSid);
        return mccIddNddSid;
    }

    public static String getCurMccBySidLtmoff(String sSid) {
        String findMcc;
        String sMcc = SystemProperties.get("ril.radio.cdma.nw_mcc", "");
        String sLtmoff = SystemProperties.get("ril.radio.cdma.ltmoffset", "");
        Rlog.d(LOG_TAG, "plus: getCurMccBySidLtmoff Mcc = " + sMcc + ",Sid = " + sSid + ",Ltmoff = " + sLtmoff);
        HwCustPlusCountryListTable hwCustPlusCountryListTable = plusCountryList;
        ArrayList<HwCustMccSidLtmOff> itemList = HwCustPlusCountryListTable.getItemsFromSidConflictTableBySid(sSid);
        if (itemList == null || itemList.size() == 0) {
            Rlog.d(LOG_TAG, "plus: no mcc_array found in ConflictTable, try to get mcc in Main Table");
            hwCustPlusCountryListTable = plusCountryList;
            findMcc = HwCustPlusCountryListTable.getMccFromMainTableBySid(sSid);
        } else {
            Rlog.d(LOG_TAG, "plus: more than 2 mcc found in ConflictTable");
            findMcc = plusCountryList.getCcFromConflictTableByLTM(itemList, sLtmoff);
        }
        if (findMcc == null) {
            Rlog.e(LOG_TAG, "plus: could not find mcc by sid and ltmoff, use Network Mcc anyway");
            findMcc = sMcc;
        }
        Rlog.d(LOG_TAG, "plus: getCurMccBySidLtmoff, mcc = " + findMcc);
        return findMcc;
    }

    private static String removePlusAddIdd(String number, HwCustMccIddNddSid mccIddNddSid) {
        if (number == null || mccIddNddSid == null || !number.startsWith(PLUS_PREFIX)) {
            Rlog.e(LOG_TAG, "plus: removePlusAddIdd input param invalid");
            return number;
        }
        String formatNum = number;
        String sCC = mccIddNddSid.Cc;
        Rlog.d(LOG_TAG, "plus: number auto format correctly, mccIddNddSid = " + mccIddNddSid.toString());
        if (number.startsWith(sCC, 1)) {
            formatNum = mccIddNddSid.Idd + number.substring(1, number.length());
        } else {
            formatNum = mccIddNddSid.Idd + number.substring(1, number.length());
        }
        return formatNum;
    }

    public static String replacePlusCodeWithIddNdd(String number) {
        if (number == null || number.length() == 0 || !number.startsWith(PLUS_PREFIX)) {
            Rlog.w(LOG_TAG, "plus: replacePlusCodeWithIddNdd invalid number, no need to replacePlusCode");
            return number;
        }
        HwCustMccIddNddSid mccIddNddSid = getNetworkMccIddList();
        if (mccIddNddSid != null) {
            return removePlusAddIdd(number, mccIddNddSid);
        }
        Rlog.e(LOG_TAG, "plus: replacePlusCodeWithIddNdd find no operator that match the MCC");
        return number;
    }

    public static String replaceIddNddWithPlus(String number, int toa) {
        if (number == null || number.length() == 0) {
            Rlog.e(LOG_TAG, "plus: replaceIddNddWithPlus please check the param ");
            return number;
        }
        HwCustMccIddNddSid mccIddNddSid = getNetworkMccIddList();
        if (mccIddNddSid == null) {
            Rlog.e(LOG_TAG, "plus: replaceIddNddWithPlus find no operator that match the MCC ");
            return number;
        }
        Rlog.d(LOG_TAG, "plus: replaceIddNddWithPlus mccIddNddSid =" + mccIddNddSid);
        int ccIndex = 0;
        if (number.startsWith(PLUS_PREFIX)) {
            ccIndex = 1;
        }
        if (number.startsWith(mccIddNddSid.Idd, ccIndex)) {
            ccIndex += mccIddNddSid.Idd.length();
            toa = PduPart.P_SEC;
        }
        return PhoneNumberUtils.stringFromStringAndTOA(number.substring(ccIndex, number.length()), toa);
    }

    private static HwCustMccIddNddSid getMccIddListForSms() {
        String mcc = SystemProperties.get("ril.radio.cdma.icc_mcc", "");
        Rlog.d(LOG_TAG, "plus: getMccIddListForSms Mcc = " + mcc);
        if (plusCountryList == null) {
            return null;
        }
        Rlog.d(LOG_TAG, "plus: [canFormatPlusCodeForSms] Mcc = " + mcc);
        if (mcc == null || mcc.length() == 0) {
            return null;
        }
        HwCustPlusCountryListTable hwCustPlusCountryListTable = plusCountryList;
        HwCustMccIddNddSid mccIddNddSid = HwCustPlusCountryListTable.getItemFromCountryListByMcc(mcc);
        Rlog.d(LOG_TAG, "plus: getMccIddListForSms getItemFromCountryListByMcc mccIddNddSid = " + mccIddNddSid);
        return mccIddNddSid;
    }

    public static String replacePlusCodeWithIddNddForSms(String number) {
        if (number == null || number.length() == 0 || !number.startsWith(PLUS_PREFIX)) {
            Rlog.e(LOG_TAG, "plus: replacePlusCodeWithIddNddForSms faild ,invalid param");
            return number;
        }
        HwCustMccIddNddSid mccIddNddSid = getMccIddListForSms();
        if (mccIddNddSid != null) {
            return removePlusAddIdd(number, mccIddNddSid);
        }
        Rlog.e(LOG_TAG, "plus: replacePlusCodeWithIddNddForSms faild ,mccIddNddSid is null");
        return number;
    }

    public static String replaceIddNddWithPlusForSms(String number) {
        if (number == null || number.length() == 0) {
            Rlog.d(LOG_TAG, "plus: [replaceIddNddWithPlusForSms] please check the param ");
            return number;
        }
        String formatNumber = number;
        if (!number.startsWith(PLUS_PREFIX)) {
            HwCustMccIddNddSid mccIddNddSid = getMccIddListForSms();
            if (mccIddNddSid == null) {
                Rlog.d(LOG_TAG, "plus: [replaceIddNddWithPlusForSms] find no operator that match the MCC ");
                return number;
            }
            String Idd = mccIddNddSid.Idd;
            Rlog.d(LOG_TAG, "plus: [replaceIddNddWithPlusForSms] find match the cc, Idd = " + Idd);
            if (number.startsWith(Idd) && number.length() > Idd.length()) {
                formatNumber = PLUS_PREFIX + number.substring(Idd.length(), number.length());
            }
        }
        return formatNumber;
    }
}
