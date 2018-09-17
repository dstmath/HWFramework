package com.android.internal.telephony.cdma;

import android.telephony.Rlog;
import android.util.Log;
import android.util.SparseIntArray;
import java.util.ArrayList;

public class HwCustPlusCountryListTable {
    private static final boolean DBG = true;
    private static SparseIntArray FindOutSidMap = null;
    static final String LOG_TAG = "CDMA-HwCustPlusCountryListTable";
    protected static final HwCustMccIddNddSid[] MccIddNddSidMap = null;
    protected static final HwCustMccSidLtmOff[] MccSidLtmOffMap = null;
    static final int PARAM_FOR_OFFSET = 2;
    static final Object sInstSync = null;
    private static final HwCustPlusCountryListTable sInstance = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.HwCustPlusCountryListTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.HwCustPlusCountryListTable.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.HwCustPlusCountryListTable.<clinit>():void");
    }

    public static HwCustPlusCountryListTable getInstance() {
        return sInstance;
    }

    private HwCustPlusCountryListTable() {
    }

    public static HwCustMccIddNddSid getItemFromCountryListByMcc(String sMcc) {
        Rlog.d(LOG_TAG, "plus: getItemFromCountryListByMcc mcc = " + sMcc);
        int mcc = getIntFromString(sMcc);
        for (HwCustMccIddNddSid item : MccIddNddSidMap) {
            if (mcc == item.Mcc) {
                Rlog.d(LOG_TAG, "plus: Now find mccIddNddSid = " + item);
                return item;
            }
        }
        Rlog.e(LOG_TAG, "plus: can't find one that match the Mcc");
        return null;
    }

    public static int getIntFromString(String str) {
        int intValue = -1;
        try {
            intValue = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Rlog.e(LOG_TAG, Log.getStackTraceString(e));
        }
        return intValue;
    }

    public static ArrayList<HwCustMccSidLtmOff> getItemsFromSidConflictTableBySid(String sSid) {
        int sid = getIntFromString(sSid);
        ArrayList<HwCustMccSidLtmOff> itemList = new ArrayList();
        for (HwCustMccSidLtmOff item : MccSidLtmOffMap) {
            if (sid == item.Sid) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    public static String getMccFromMainTableBySid(String sSid) {
        int sid = getIntFromString(sSid);
        String mcc = null;
        for (HwCustMccIddNddSid item : MccIddNddSidMap) {
            if (sid <= item.SidMax && sid >= item.SidMin) {
                mcc = Integer.toString(item.Mcc);
                break;
            }
        }
        Rlog.d(LOG_TAG, "plus: getMccFromMainTableBySid mcc = " + mcc);
        return mcc;
    }

    public String getCcFromConflictTableByLTM(ArrayList<HwCustMccSidLtmOff> itemList, String sLtm_off) {
        Rlog.d(LOG_TAG, "plus:  getCcFromConflictTableByLTM sLtm_off = " + sLtm_off);
        if (itemList == null || itemList.size() == 0) {
            Rlog.e(LOG_TAG, "plus: [getCcFromConflictTableByLTM] please check the param ");
            return null;
        }
        String FindMcc = null;
        try {
            int ltm_off = Integer.parseInt(sLtm_off);
            for (HwCustMccSidLtmOff item : itemList) {
                int min = item.LtmOffMin * PARAM_FOR_OFFSET;
                if (ltm_off <= item.LtmOffMax * PARAM_FOR_OFFSET && ltm_off >= min) {
                    FindMcc = Integer.toString(item.Mcc);
                    break;
                }
            }
            Rlog.d(LOG_TAG, "plus: find one that match the ltm_off mcc = " + FindMcc);
            return FindMcc;
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            return null;
        }
    }
}
