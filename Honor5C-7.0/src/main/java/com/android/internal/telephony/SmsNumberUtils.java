package com.android.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.HbpcdLookup.MccIdd;
import com.android.internal.telephony.HbpcdLookup.MccLookup;
import com.android.internal.telephony.HbpcdLookup.MccSidRange;
import java.util.ArrayList;
import java.util.HashMap;

public class SmsNumberUtils {
    private static int[] ALL_COUNTRY_CODES = null;
    private static final int CDMA_HOME_NETWORK = 1;
    private static final int CDMA_ROAMING_NETWORK = 2;
    private static final boolean DBG = false;
    private static final int GSM_UMTS_NETWORK = 0;
    private static HashMap<String, ArrayList<String>> IDDS_MAPS = null;
    private static int MAX_COUNTRY_CODES_LENGTH = 0;
    private static final int MIN_COUNTRY_AREA_LOCAL_LENGTH = 10;
    private static final int NANP_CC = 1;
    private static final String NANP_IDD = "011";
    private static final int NANP_LONG_LENGTH = 11;
    private static final int NANP_MEDIUM_LENGTH = 10;
    private static final String NANP_NDD = "1";
    private static final int NANP_SHORT_LENGTH = 7;
    private static final int NP_CC_AREA_LOCAL = 104;
    private static final int NP_HOMEIDD_CC_AREA_LOCAL = 101;
    private static final int NP_INTERNATIONAL_BEGIN = 100;
    private static final int NP_LOCALIDD_CC_AREA_LOCAL = 103;
    private static final int NP_NANP_AREA_LOCAL = 2;
    private static final int NP_NANP_BEGIN = 1;
    private static final int NP_NANP_LOCAL = 1;
    private static final int NP_NANP_LOCALIDD_CC_AREA_LOCAL = 5;
    private static final int NP_NANP_NBPCD_CC_AREA_LOCAL = 4;
    private static final int NP_NANP_NBPCD_HOMEIDD_CC_AREA_LOCAL = 6;
    private static final int NP_NANP_NDD_AREA_LOCAL = 3;
    private static final int NP_NBPCD_CC_AREA_LOCAL = 102;
    private static final int NP_NBPCD_HOMEIDD_CC_AREA_LOCAL = 100;
    private static final int NP_NONE = 0;
    private static final String PLUS_SIGN = "+";
    private static final String TAG = "SmsNumberUtils";

    private static class NumberEntry {
        public String IDD;
        public int countryCode;
        public String number;

        public NumberEntry(String number) {
            this.number = number;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.SmsNumberUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.SmsNumberUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.SmsNumberUtils.<clinit>():void");
    }

    private static String formatNumber(Context context, String number, String activeMcc, int networkType) {
        if (number == null) {
            throw new IllegalArgumentException("number is null");
        } else if (activeMcc == null || activeMcc.trim().length() == 0) {
            throw new IllegalArgumentException("activeMcc is null or empty!");
        } else {
            String networkPortionNumber = PhoneNumberUtils.extractNetworkPortion(number);
            if (networkPortionNumber == null || networkPortionNumber.length() == 0) {
                throw new IllegalArgumentException("Number is invalid!");
            }
            NumberEntry numberEntry = new NumberEntry(networkPortionNumber);
            ArrayList<String> allIDDs = getAllIDDs(context, activeMcc);
            int nanpState = checkNANP(numberEntry, allIDDs);
            if (DBG) {
                Rlog.d(TAG, "NANP type: " + getNumberPlanType(nanpState));
            }
            if (nanpState == NP_NANP_LOCAL || nanpState == NP_NANP_AREA_LOCAL || nanpState == NP_NANP_NDD_AREA_LOCAL) {
                return networkPortionNumber;
            }
            if (nanpState != NP_NANP_NBPCD_CC_AREA_LOCAL) {
                if (nanpState == NP_NANP_LOCALIDD_CC_AREA_LOCAL) {
                    if (networkType == NP_NANP_LOCAL) {
                        return networkPortionNumber;
                    }
                    if (networkType == 0) {
                        return PLUS_SIGN + networkPortionNumber.substring(numberEntry.IDD != null ? numberEntry.IDD.length() : NP_NONE);
                    } else if (networkType == NP_NANP_AREA_LOCAL) {
                        return networkPortionNumber.substring(numberEntry.IDD != null ? numberEntry.IDD.length() : NP_NONE);
                    }
                }
                int internationalState = checkInternationalNumberPlan(context, numberEntry, allIDDs, NANP_IDD);
                if (DBG) {
                    Rlog.d(TAG, "International type: " + getNumberPlanType(internationalState));
                }
                String returnNumber = null;
                switch (internationalState) {
                    case NP_NBPCD_HOMEIDD_CC_AREA_LOCAL /*100*/:
                        if (networkType == 0) {
                            returnNumber = networkPortionNumber.substring(NP_NANP_LOCAL);
                            break;
                        }
                        break;
                    case NP_HOMEIDD_CC_AREA_LOCAL /*101*/:
                        returnNumber = networkPortionNumber;
                        break;
                    case NP_NBPCD_CC_AREA_LOCAL /*102*/:
                        returnNumber = NANP_IDD + networkPortionNumber.substring(NP_NANP_LOCAL);
                        break;
                    case NP_LOCALIDD_CC_AREA_LOCAL /*103*/:
                        if (networkType == 0 || networkType == NP_NANP_AREA_LOCAL) {
                            returnNumber = NANP_IDD + networkPortionNumber.substring(numberEntry.IDD != null ? numberEntry.IDD.length() : NP_NONE);
                            break;
                        }
                    case NP_CC_AREA_LOCAL /*104*/:
                        int countryCode = numberEntry.countryCode;
                        if (!(inExceptionListForNpCcAreaLocal(numberEntry) || networkPortionNumber.length() < NANP_LONG_LENGTH || countryCode == NP_NANP_LOCAL)) {
                            returnNumber = NANP_IDD + networkPortionNumber;
                            break;
                        }
                    default:
                        if (networkPortionNumber.startsWith(PLUS_SIGN) && (networkType == NP_NANP_LOCAL || networkType == NP_NANP_AREA_LOCAL)) {
                            if (!networkPortionNumber.startsWith("+011")) {
                                returnNumber = NANP_IDD + networkPortionNumber.substring(NP_NANP_LOCAL);
                                break;
                            }
                            returnNumber = networkPortionNumber.substring(NP_NANP_LOCAL);
                            break;
                        }
                }
                if (returnNumber == null) {
                    returnNumber = networkPortionNumber;
                }
                return returnNumber;
            } else if (networkType == NP_NANP_LOCAL || networkType == NP_NANP_AREA_LOCAL) {
                return networkPortionNumber.substring(NP_NANP_LOCAL);
            } else {
                return networkPortionNumber;
            }
        }
    }

    private static ArrayList<String> getAllIDDs(Context context, String mcc) {
        ArrayList<String> allIDDs = (ArrayList) IDDS_MAPS.get(mcc);
        if (allIDDs != null) {
            return allIDDs;
        }
        allIDDs = new ArrayList();
        String[] projection = new String[NP_NANP_AREA_LOCAL];
        projection[NP_NONE] = MccIdd.IDD;
        projection[NP_NANP_LOCAL] = MccSidRange.MCC;
        String str = null;
        String[] strArr = null;
        if (mcc != null) {
            str = "MCC=?";
            strArr = new String[NP_NANP_LOCAL];
            strArr[NP_NONE] = mcc;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MccIdd.CONTENT_URI, projection, str, strArr, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String idd = cursor.getString(NP_NONE);
                    if (!allIDDs.contains(idd)) {
                        allIDDs.add(idd);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Rlog.e(TAG, "Can't access HbpcdLookup database", e);
            IDDS_MAPS.put(mcc, allIDDs);
            if (DBG) {
                Rlog.d(TAG, "MCC = " + mcc + ", all IDDs = " + allIDDs);
            }
            return allIDDs;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        IDDS_MAPS.put(mcc, allIDDs);
        if (DBG) {
            Rlog.d(TAG, "MCC = " + mcc + ", all IDDs = " + allIDDs);
        }
        return allIDDs;
    }

    private static int checkNANP(NumberEntry numberEntry, ArrayList<String> allIDDs) {
        boolean isNANP = DBG;
        String number = numberEntry.number;
        if (number.length() == NANP_SHORT_LENGTH) {
            char firstChar = number.charAt(NP_NONE);
            if (firstChar >= '2' && firstChar <= '9') {
                isNANP = true;
                for (int i = NP_NANP_LOCAL; i < NANP_SHORT_LENGTH; i += NP_NANP_LOCAL) {
                    if (!PhoneNumberUtils.isISODigit(number.charAt(i))) {
                        isNANP = DBG;
                        break;
                    }
                }
            }
            if (isNANP) {
                return NP_NANP_LOCAL;
            }
        } else if (number.length() == NANP_MEDIUM_LENGTH) {
            if (isNANP(number)) {
                return NP_NANP_AREA_LOCAL;
            }
        } else if (number.length() == NANP_LONG_LENGTH) {
            if (isNANP(number)) {
                return NP_NANP_NDD_AREA_LOCAL;
            }
        } else if (number.startsWith(PLUS_SIGN)) {
            number = number.substring(NP_NANP_LOCAL);
            if (number.length() == NANP_LONG_LENGTH) {
                if (isNANP(number)) {
                    return NP_NANP_NBPCD_CC_AREA_LOCAL;
                }
            } else if (number.startsWith(NANP_IDD) && number.length() == 14 && isNANP(number.substring(NP_NANP_NDD_AREA_LOCAL))) {
                return NP_NANP_NBPCD_HOMEIDD_CC_AREA_LOCAL;
            }
        } else {
            for (String idd : allIDDs) {
                if (number.startsWith(idd)) {
                    String number2 = number.substring(idd.length());
                    if (number2 != null && number2.startsWith(String.valueOf(NP_NANP_LOCAL)) && isNANP(number2)) {
                        numberEntry.IDD = idd;
                        return NP_NANP_LOCALIDD_CC_AREA_LOCAL;
                    }
                }
            }
        }
        return NP_NONE;
    }

    private static boolean isNANP(String number) {
        if (number.length() != NANP_MEDIUM_LENGTH && (number.length() != NANP_LONG_LENGTH || !number.startsWith(NANP_NDD))) {
            return DBG;
        }
        if (number.length() == NANP_LONG_LENGTH) {
            number = number.substring(NP_NANP_LOCAL);
        }
        return PhoneNumberUtils.isNanp(number);
    }

    private static int checkInternationalNumberPlan(Context context, NumberEntry numberEntry, ArrayList<String> allIDDs, String homeIDD) {
        String number = numberEntry.number;
        int countryCode;
        if (number.startsWith(PLUS_SIGN)) {
            String numberNoNBPCD = number.substring(NP_NANP_LOCAL);
            if (numberNoNBPCD.startsWith(homeIDD)) {
                countryCode = getCountryCode(context, numberNoNBPCD.substring(homeIDD.length()));
                if (countryCode > 0) {
                    numberEntry.countryCode = countryCode;
                    return NP_NBPCD_HOMEIDD_CC_AREA_LOCAL;
                }
            }
            countryCode = getCountryCode(context, numberNoNBPCD);
            if (countryCode > 0) {
                numberEntry.countryCode = countryCode;
                return NP_NBPCD_CC_AREA_LOCAL;
            }
        } else if (number.startsWith(homeIDD)) {
            countryCode = getCountryCode(context, number.substring(homeIDD.length()));
            if (countryCode > 0) {
                numberEntry.countryCode = countryCode;
                return NP_HOMEIDD_CC_AREA_LOCAL;
            }
        } else {
            for (String exitCode : allIDDs) {
                if (number.startsWith(exitCode)) {
                    countryCode = getCountryCode(context, number.substring(exitCode.length()));
                    if (countryCode > 0) {
                        numberEntry.countryCode = countryCode;
                        numberEntry.IDD = exitCode;
                        return NP_LOCALIDD_CC_AREA_LOCAL;
                    }
                }
            }
            if (!number.startsWith(ProxyController.MODEM_0)) {
                countryCode = getCountryCode(context, number);
                if (countryCode > 0) {
                    numberEntry.countryCode = countryCode;
                    return NP_CC_AREA_LOCAL;
                }
            }
        }
        return NP_NONE;
    }

    private static int getCountryCode(Context context, String number) {
        if (number.length() >= NANP_MEDIUM_LENGTH) {
            int[] allCCs = getAllCountryCodes(context);
            if (allCCs == null) {
                return -1;
            }
            int i;
            int[] ccArray = new int[MAX_COUNTRY_CODES_LENGTH];
            for (i = NP_NONE; i < MAX_COUNTRY_CODES_LENGTH; i += NP_NANP_LOCAL) {
                ccArray[i] = Integer.parseInt(number.substring(NP_NONE, i + NP_NANP_LOCAL));
            }
            for (i = NP_NONE; i < allCCs.length; i += NP_NANP_LOCAL) {
                int tempCC = allCCs[i];
                for (int j = NP_NONE; j < MAX_COUNTRY_CODES_LENGTH; j += NP_NANP_LOCAL) {
                    if (tempCC == ccArray[j]) {
                        if (DBG) {
                            Rlog.d(TAG, "Country code = " + tempCC);
                        }
                        return tempCC;
                    }
                }
            }
        }
        return -1;
    }

    private static int[] getAllCountryCodes(Context context) {
        if (ALL_COUNTRY_CODES != null) {
            return ALL_COUNTRY_CODES;
        }
        Cursor cursor = null;
        try {
            String[] projection = new String[NP_NANP_LOCAL];
            projection[NP_NONE] = MccLookup.COUNTRY_CODE;
            cursor = context.getContentResolver().query(MccLookup.CONTENT_URI, projection, null, null, null);
            if (cursor.getCount() > 0) {
                ALL_COUNTRY_CODES = new int[cursor.getCount()];
                int i = NP_NONE;
                while (cursor.moveToNext()) {
                    int countryCode = cursor.getInt(NP_NONE);
                    int i2 = i + NP_NANP_LOCAL;
                    ALL_COUNTRY_CODES[i] = countryCode;
                    int length = String.valueOf(countryCode).trim().length();
                    if (length > MAX_COUNTRY_CODES_LENGTH) {
                        MAX_COUNTRY_CODES_LENGTH = length;
                    }
                    i = i2;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Rlog.e(TAG, "Can't access HbpcdLookup database", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ALL_COUNTRY_CODES;
    }

    private static boolean inExceptionListForNpCcAreaLocal(NumberEntry numberEntry) {
        int countryCode = numberEntry.countryCode;
        if (numberEntry.number.length() != 12) {
            return DBG;
        }
        if (countryCode == NANP_SHORT_LENGTH || countryCode == 20 || countryCode == 65 || countryCode == 90) {
            return true;
        }
        return DBG;
    }

    private static String getNumberPlanType(int state) {
        String numberPlanType = "Number Plan type (" + state + "): ";
        if (state == NP_NANP_LOCAL) {
            return "NP_NANP_LOCAL";
        }
        if (state == NP_NANP_AREA_LOCAL) {
            return "NP_NANP_AREA_LOCAL";
        }
        if (state == NP_NANP_NDD_AREA_LOCAL) {
            return "NP_NANP_NDD_AREA_LOCAL";
        }
        if (state == NP_NANP_NBPCD_CC_AREA_LOCAL) {
            return "NP_NANP_NBPCD_CC_AREA_LOCAL";
        }
        if (state == NP_NANP_LOCALIDD_CC_AREA_LOCAL) {
            return "NP_NANP_LOCALIDD_CC_AREA_LOCAL";
        }
        if (state == NP_NANP_NBPCD_HOMEIDD_CC_AREA_LOCAL) {
            return "NP_NANP_NBPCD_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == NP_NBPCD_HOMEIDD_CC_AREA_LOCAL) {
            return "NP_NBPCD_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == NP_HOMEIDD_CC_AREA_LOCAL) {
            return "NP_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == NP_NBPCD_CC_AREA_LOCAL) {
            return "NP_NBPCD_CC_AREA_LOCAL";
        }
        if (state == NP_LOCALIDD_CC_AREA_LOCAL) {
            return "NP_LOCALIDD_CC_AREA_LOCAL";
        }
        if (state == NP_CC_AREA_LOCAL) {
            return "NP_CC_AREA_LOCAL";
        }
        return "Unknown type";
    }

    public static String filterDestAddr(Phone phone, String destAddr) {
        if (DBG) {
            Rlog.d(TAG, "enter filterDestAddr. destAddr=\"xxxx\"");
        }
        if (destAddr == null || !PhoneNumberUtils.isGlobalPhoneNumber(destAddr)) {
            Rlog.w(TAG, "destAddr xxxx is not a global phone number! Nothing changed.");
            return destAddr;
        }
        String networkOperator = TelephonyManager.from(phone.getContext()).getNetworkOperator(phone.getSubId());
        String result = null;
        if (needToConvert(phone)) {
            int networkType = getNetworkType(phone);
            if (!(networkType == -1 || TextUtils.isEmpty(networkOperator))) {
                String networkMcc = networkOperator.substring(NP_NONE, NP_NANP_NDD_AREA_LOCAL);
                if (networkMcc != null && networkMcc.trim().length() > 0) {
                    result = formatNumber(phone.getContext(), destAddr, networkMcc, networkType);
                }
            }
        }
        if (DBG) {
            Rlog.d(TAG, "destAddr is " + (result != null ? "formatted." : "not formatted."));
            Rlog.d(TAG, "leave filterDestAddr, new destAddr=\"xxxx\"");
        }
        if (result == null) {
            result = destAddr;
        }
        return result;
    }

    private static int getNetworkType(Phone phone) {
        int phoneType = phone.getPhoneType();
        if (phoneType == NP_NANP_LOCAL) {
            return NP_NONE;
        }
        if (phoneType == NP_NANP_AREA_LOCAL) {
            if (isInternationalRoaming(phone)) {
                return NP_NANP_AREA_LOCAL;
            }
            return NP_NANP_LOCAL;
        } else if (!DBG) {
            return -1;
        } else {
            Rlog.w(TAG, "warning! unknown mPhoneType value=" + phoneType);
            return -1;
        }
    }

    private static boolean isInternationalRoaming(Phone phone) {
        boolean internationalRoaming;
        boolean z = DBG;
        String operatorIsoCountry = TelephonyManager.from(phone.getContext()).getNetworkCountryIsoForPhone(phone.getPhoneId());
        String simIsoCountry = TelephonyManager.from(phone.getContext()).getSimCountryIsoForPhone(phone.getPhoneId());
        if (TextUtils.isEmpty(operatorIsoCountry) || TextUtils.isEmpty(simIsoCountry)) {
            internationalRoaming = DBG;
        } else {
            if (!simIsoCountry.equals(operatorIsoCountry)) {
                z = true;
            }
            internationalRoaming = z;
        }
        if (!internationalRoaming) {
            return internationalRoaming;
        }
        if ("us".equals(simIsoCountry)) {
            return "vi".equals(operatorIsoCountry) ? DBG : true;
        } else {
            if ("vi".equals(simIsoCountry)) {
                return "us".equals(operatorIsoCountry) ? DBG : true;
            } else {
                return internationalRoaming;
            }
        }
    }

    private static boolean needToConvert(Phone phone) {
        boolean z = DBG;
        String[] listArray = phone.getContext().getResources().getStringArray(17236039);
        if (listArray == null || listArray.length <= 0) {
            return DBG;
        }
        for (int i = NP_NONE; i < listArray.length; i += NP_NANP_LOCAL) {
            if (!TextUtils.isEmpty(listArray[i])) {
                String[] needToConvertArray = listArray[i].split(";");
                if (needToConvertArray != null && needToConvertArray.length > 0) {
                    if (needToConvertArray.length == NP_NANP_LOCAL) {
                        z = "true".equalsIgnoreCase(needToConvertArray[NP_NONE]);
                    } else if (needToConvertArray.length == NP_NANP_AREA_LOCAL && !TextUtils.isEmpty(needToConvertArray[NP_NANP_LOCAL]) && compareGid1(phone, needToConvertArray[NP_NANP_LOCAL])) {
                        return "true".equalsIgnoreCase(needToConvertArray[NP_NONE]);
                    }
                }
            }
        }
        return z;
    }

    private static boolean compareGid1(Phone phone, String serviceGid1) {
        int i = NP_NONE;
        String gid1 = phone.getGroupIdLevel1();
        boolean ret = true;
        if (TextUtils.isEmpty(serviceGid1)) {
            if (DBG) {
                Rlog.d(TAG, "compareGid1 serviceGid is empty, return " + true);
            }
            return true;
        }
        int gid_length = serviceGid1.length();
        if (gid1 != null && gid1.length() >= gid_length) {
            i = gid1.substring(NP_NONE, gid_length).equalsIgnoreCase(serviceGid1);
        }
        if (i == 0) {
            if (DBG) {
                Rlog.d(TAG, " gid1 " + gid1 + " serviceGid1 " + serviceGid1);
            }
            ret = DBG;
        }
        if (DBG) {
            Rlog.d(TAG, "compareGid1 is " + (ret ? "Same" : "Different"));
        }
        return ret;
    }
}
