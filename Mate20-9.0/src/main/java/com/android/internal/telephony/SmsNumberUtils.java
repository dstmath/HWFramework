package com.android.internal.telephony;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Binder;
import android.os.Build;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.HbpcdLookup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SmsNumberUtils {
    private static int[] ALL_COUNTRY_CODES = null;
    private static final int CDMA_HOME_NETWORK = 1;
    private static final int CDMA_ROAMING_NETWORK = 2;
    private static final boolean DBG = Build.IS_DEBUGGABLE;
    private static final int GSM_UMTS_NETWORK = 0;
    private static HashMap<String, ArrayList<String>> IDDS_MAPS = new HashMap<>();
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

        public NumberEntry(String number2) {
            this.number = number2;
        }
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
            if (nanpState == 1 || nanpState == 2 || nanpState == 3) {
                return networkPortionNumber;
            }
            if (nanpState != 4) {
                int iddLength = 0;
                if (nanpState == 5) {
                    if (networkType == 1) {
                        return networkPortionNumber;
                    }
                    if (networkType == 0) {
                        if (numberEntry.IDD != null) {
                            iddLength = numberEntry.IDD.length();
                        }
                        return PLUS_SIGN + networkPortionNumber.substring(iddLength);
                    } else if (networkType == 2) {
                        if (numberEntry.IDD != null) {
                            iddLength = numberEntry.IDD.length();
                        }
                        return networkPortionNumber.substring(iddLength);
                    }
                }
                int internationalState = checkInternationalNumberPlan(context, numberEntry, allIDDs, NANP_IDD);
                if (DBG) {
                    Rlog.d(TAG, "International type: " + getNumberPlanType(internationalState));
                }
                String returnNumber = null;
                switch (internationalState) {
                    case 100:
                        if (networkType == 0) {
                            returnNumber = networkPortionNumber.substring(1);
                            break;
                        }
                        break;
                    case 101:
                        returnNumber = networkPortionNumber;
                        break;
                    case 102:
                        returnNumber = NANP_IDD + networkPortionNumber.substring(1);
                        break;
                    case NP_LOCALIDD_CC_AREA_LOCAL /*103*/:
                        if (networkType == 0 || networkType == 2) {
                            if (numberEntry.IDD != null) {
                                iddLength = numberEntry.IDD.length();
                            }
                            returnNumber = NANP_IDD + networkPortionNumber.substring(iddLength);
                            break;
                        }
                    case 104:
                        int countryCode = numberEntry.countryCode;
                        if (!inExceptionListForNpCcAreaLocal(numberEntry) && networkPortionNumber.length() >= 11 && countryCode != 1) {
                            returnNumber = NANP_IDD + networkPortionNumber;
                            break;
                        }
                    default:
                        if (networkPortionNumber.startsWith(PLUS_SIGN) && (networkType == 1 || networkType == 2)) {
                            if (!networkPortionNumber.startsWith("+011")) {
                                returnNumber = NANP_IDD + networkPortionNumber.substring(1);
                                break;
                            } else {
                                returnNumber = networkPortionNumber.substring(1);
                                break;
                            }
                        }
                }
                if (returnNumber == null) {
                    returnNumber = networkPortionNumber;
                }
                return returnNumber;
            } else if (networkType == 1 || networkType == 2) {
                return networkPortionNumber.substring(1);
            } else {
                return networkPortionNumber;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0050, code lost:
        if (r10 != null) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0052, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
        if (r10 == null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0063, code lost:
        IDDS_MAPS.put(r12, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006a, code lost:
        if (DBG == false) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006c, code lost:
        android.telephony.Rlog.d(TAG, "MCC = " + r12 + ", all IDDs = " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008a, code lost:
        return r0;
     */
    private static ArrayList<String> getAllIDDs(Context context, String mcc) {
        ArrayList<String> allIDDs = IDDS_MAPS.get(mcc);
        if (allIDDs != null) {
            return allIDDs;
        }
        ArrayList<String> allIDDs2 = new ArrayList<>();
        String[] projection = {HbpcdLookup.MccIdd.IDD, "MCC"};
        String where = null;
        String[] selectionArgs = null;
        if (mcc != null) {
            where = "MCC=?";
            selectionArgs = new String[]{mcc};
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(HbpcdLookup.MccIdd.CONTENT_URI, projection, where, selectionArgs, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String idd = cursor.getString(0);
                    if (!allIDDs2.contains(idd)) {
                        allIDDs2.add(idd);
                    }
                }
            }
        } catch (SQLException e) {
            Rlog.e(TAG, "Can't access HbpcdLookup database", e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private static int checkNANP(NumberEntry numberEntry, ArrayList<String> allIDDs) {
        boolean isNANP = false;
        String number = numberEntry.number;
        if (number.length() == 7) {
            char firstChar = number.charAt(0);
            if (firstChar >= '2' && firstChar <= '9') {
                isNANP = true;
                int i = 1;
                while (true) {
                    if (i >= 7) {
                        break;
                    } else if (!PhoneNumberUtils.isISODigit(number.charAt(i))) {
                        isNANP = false;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (isNANP) {
                return 1;
            }
        } else if (number.length() == 10) {
            if (isNANP(number)) {
                return 2;
            }
        } else if (number.length() == 11) {
            if (isNANP(number)) {
                return 3;
            }
        } else if (number.startsWith(PLUS_SIGN)) {
            String number2 = number.substring(1);
            if (number2.length() == 11) {
                if (isNANP(number2)) {
                    return 4;
                }
            } else if (number2.startsWith(NANP_IDD) && number2.length() == 14 && isNANP(number2.substring(3))) {
                return 6;
            }
        } else {
            Iterator<String> it = allIDDs.iterator();
            while (it.hasNext()) {
                String idd = it.next();
                if (number.startsWith(idd)) {
                    String number22 = number.substring(idd.length());
                    if (number22 != null && number22.startsWith(String.valueOf(1)) && isNANP(number22)) {
                        numberEntry.IDD = idd;
                        return 5;
                    }
                }
            }
        }
        return 0;
    }

    private static boolean isNANP(String number) {
        if (number.length() != 10 && (number.length() != 11 || !number.startsWith("1"))) {
            return false;
        }
        if (number.length() == 11) {
            number = number.substring(1);
        }
        return PhoneNumberUtils.isNanp(number);
    }

    private static int checkInternationalNumberPlan(Context context, NumberEntry numberEntry, ArrayList<String> allIDDs, String homeIDD) {
        String number = numberEntry.number;
        if (number.startsWith(PLUS_SIGN)) {
            String numberNoNBPCD = number.substring(1);
            if (numberNoNBPCD.startsWith(homeIDD)) {
                int countryCode = getCountryCode(context, numberNoNBPCD.substring(homeIDD.length()));
                int countryCode2 = countryCode;
                if (countryCode > 0) {
                    numberEntry.countryCode = countryCode2;
                    return 100;
                }
            } else {
                int countryCode3 = getCountryCode(context, numberNoNBPCD);
                int countryCode4 = countryCode3;
                if (countryCode3 > 0) {
                    numberEntry.countryCode = countryCode4;
                    return 102;
                }
            }
        } else if (number.startsWith(homeIDD)) {
            int countryCode5 = getCountryCode(context, number.substring(homeIDD.length()));
            int countryCode6 = countryCode5;
            if (countryCode5 > 0) {
                numberEntry.countryCode = countryCode6;
                return 101;
            }
        } else {
            Iterator<String> it = allIDDs.iterator();
            while (it.hasNext()) {
                String exitCode = it.next();
                if (number.startsWith(exitCode)) {
                    int countryCode7 = getCountryCode(context, number.substring(exitCode.length()));
                    int countryCode8 = countryCode7;
                    if (countryCode7 > 0) {
                        numberEntry.countryCode = countryCode8;
                        numberEntry.IDD = exitCode;
                        return NP_LOCALIDD_CC_AREA_LOCAL;
                    }
                }
            }
            if (!number.startsWith(ProxyController.MODEM_0)) {
                int countryCode9 = getCountryCode(context, number);
                int countryCode10 = countryCode9;
                if (countryCode9 > 0) {
                    numberEntry.countryCode = countryCode10;
                    return 104;
                }
            }
        }
        return 0;
    }

    private static int getCountryCode(Context context, String number) {
        if (number.length() >= 10) {
            int[] allCCs = getAllCountryCodes(context);
            if (allCCs == null) {
                return -1;
            }
            int[] ccArray = new int[MAX_COUNTRY_CODES_LENGTH];
            for (int i = 0; i < MAX_COUNTRY_CODES_LENGTH; i++) {
                ccArray[i] = Integer.parseInt(number.substring(0, i + 1));
            }
            for (int tempCC : allCCs) {
                for (int j = 0; j < MAX_COUNTRY_CODES_LENGTH; j++) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
        if (r0 != null) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0053, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
        if (r0 == null) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0066, code lost:
        return ALL_COUNTRY_CODES;
     */
    private static int[] getAllCountryCodes(Context context) {
        if (ALL_COUNTRY_CODES != null) {
            return ALL_COUNTRY_CODES;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(HbpcdLookup.MccLookup.CONTENT_URI, new String[]{HbpcdLookup.MccLookup.COUNTRY_CODE}, null, null, null);
            if (cursor.getCount() > 0) {
                ALL_COUNTRY_CODES = new int[cursor.getCount()];
                int i = 0;
                while (cursor.moveToNext()) {
                    int countryCode = cursor.getInt(0);
                    int i2 = i + 1;
                    ALL_COUNTRY_CODES[i] = countryCode;
                    int length = String.valueOf(countryCode).trim().length();
                    if (length > MAX_COUNTRY_CODES_LENGTH) {
                        MAX_COUNTRY_CODES_LENGTH = length;
                    }
                    i = i2;
                }
            }
        } catch (SQLException e) {
            Rlog.e(TAG, "Can't access HbpcdLookup database", e);
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private static boolean inExceptionListForNpCcAreaLocal(NumberEntry numberEntry) {
        int countryCode = numberEntry.countryCode;
        return numberEntry.number.length() == 12 && (countryCode == 7 || countryCode == 20 || countryCode == 65 || countryCode == 90);
    }

    private static String getNumberPlanType(int state) {
        String str = "Number Plan type (" + state + "): ";
        if (state == 1) {
            return "NP_NANP_LOCAL";
        }
        if (state == 2) {
            return "NP_NANP_AREA_LOCAL";
        }
        if (state == 3) {
            return "NP_NANP_NDD_AREA_LOCAL";
        }
        if (state == 4) {
            return "NP_NANP_NBPCD_CC_AREA_LOCAL";
        }
        if (state == 5) {
            return "NP_NANP_LOCALIDD_CC_AREA_LOCAL";
        }
        if (state == 6) {
            return "NP_NANP_NBPCD_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == 100) {
            return "NP_NBPCD_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == 101) {
            return "NP_HOMEIDD_CC_AREA_LOCAL";
        }
        if (state == 102) {
            return "NP_NBPCD_CC_AREA_LOCAL";
        }
        if (state == NP_LOCALIDD_CC_AREA_LOCAL) {
            return "NP_LOCALIDD_CC_AREA_LOCAL";
        }
        if (state == 104) {
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
            if (networkType != -1 && !TextUtils.isEmpty(networkOperator)) {
                String networkMcc = networkOperator.substring(0, 3);
                if (networkMcc != null && networkMcc.trim().length() > 0) {
                    result = formatNumber(phone.getContext(), destAddr, networkMcc, networkType);
                }
            }
        }
        if (DBG != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("destAddr is ");
            sb.append(result != null ? "formatted." : "not formatted.");
            Rlog.d(TAG, sb.toString());
            Rlog.d(TAG, "leave filterDestAddr, new destAddr=\"xxxx\"");
        }
        return result != null ? result : destAddr;
    }

    private static int getNetworkType(Phone phone) {
        int phoneType = phone.getPhoneType();
        if (phoneType == 1) {
            return 0;
        }
        if (phoneType == 2) {
            if (isInternationalRoaming(phone)) {
                return 2;
            }
            return 1;
        } else if (!DBG) {
            return -1;
        } else {
            Rlog.w(TAG, "warning! unknown mPhoneType value=" + phoneType);
            return -1;
        }
    }

    private static boolean isInternationalRoaming(Phone phone) {
        String operatorIsoCountry = TelephonyManager.from(phone.getContext()).getNetworkCountryIsoForPhone(phone.getPhoneId());
        String simIsoCountry = TelephonyManager.from(phone.getContext()).getSimCountryIsoForPhone(phone.getPhoneId());
        boolean internationalRoaming = !TextUtils.isEmpty(operatorIsoCountry) && !TextUtils.isEmpty(simIsoCountry) && !simIsoCountry.equals(operatorIsoCountry);
        if (!internationalRoaming) {
            return internationalRoaming;
        }
        if ("us".equals(simIsoCountry)) {
            return true ^ "vi".equals(operatorIsoCountry);
        }
        if ("vi".equals(simIsoCountry)) {
            return true ^ "us".equals(operatorIsoCountry);
        }
        return internationalRoaming;
    }

    private static boolean needToConvert(Phone phone) {
        long identity = Binder.clearCallingIdentity();
        try {
            CarrierConfigManager configManager = (CarrierConfigManager) phone.getContext().getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle bundle = configManager.getConfig();
                if (bundle != null) {
                    return bundle.getBoolean("sms_requires_destination_number_conversion_bool");
                }
            }
            Binder.restoreCallingIdentity(identity);
            return false;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static boolean compareGid1(Phone phone, String serviceGid1) {
        String gid1 = phone.getGroupIdLevel1();
        boolean ret = true;
        if (TextUtils.isEmpty(serviceGid1)) {
            if (DBG) {
                Rlog.d(TAG, "compareGid1 serviceGid is empty, return " + true);
            }
            return true;
        }
        int gid_length = serviceGid1.length();
        if (gid1 == null || gid1.length() < gid_length || !gid1.substring(0, gid_length).equalsIgnoreCase(serviceGid1)) {
            if (DBG) {
                Rlog.d(TAG, " gid1 " + gid1 + " serviceGid1 " + serviceGid1);
            }
            ret = false;
        }
        if (DBG) {
            StringBuilder sb = new StringBuilder();
            sb.append("compareGid1 is ");
            sb.append(ret ? "Same" : "Different");
            Rlog.d(TAG, sb.toString());
        }
        return ret;
    }
}
