package com.android.server.devicepolicy;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import com.huawei.utils.reflect.EasyInvokeUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HwDevicePolicyManagerServiceUtil extends EasyInvokeUtils {
    private static final String DOMAIN_SEPARATOR = "\\.";
    public static final String EXCHANGE_DOMAIN = "domain";
    public static final String EXCHANGE_ID = "id";
    public static final String EXCHANGE_INCOMING_FIELD = "incomingfield";
    public static final String EXCHANGE_INCOMING_URI = "incominguri";
    public static final String EXCHANGE_INCOMING_USERNAME = "incomingusername";
    public static final String EXCHANGE_LABEL = "label";
    public static final String EXCHANGE_OUTCOMING_URI = "outgoinguri";
    public static final String EXCHANGE_OUTCOMING_USERNAME = "outgoingusername";
    private static final int MAX_ADDRS = 1000;
    private static final int MAX_LENGTH_OF_PACKAGE_NAME = 1000;
    private static final int MAX_PACKAGES = 200;
    private static final String TAG = "HwDevicePolicyManagerServiceUtil";
    private static final String USB_STORAGE = "usb";
    private static final char WILD_CHARACTER = '?';
    private static final String WILD_STRING = "*";
    private static Context mContext = null;

    public static boolean initialize(Context context) {
        if (context == null) {
            Log.e(TAG, "initialize context is null");
            return false;
        }
        mContext = context;
        return true;
    }

    public static boolean matchProvider(String testDomain, String providerDomain) {
        if (testDomain == null || providerDomain == null) {
            return false;
        }
        String[] testParts = testDomain.split(DOMAIN_SEPARATOR);
        String[] providerParts = providerDomain.split(DOMAIN_SEPARATOR);
        if (testParts.length != providerParts.length) {
            return false;
        }
        for (int i = 0; i < testParts.length; i++) {
            String testPart = testParts[i].toLowerCase(Locale.US);
            String providerPart = providerParts[i].toLowerCase(Locale.US);
            if (!providerPart.equals(WILD_STRING) && (matchWithWildcards(testPart, providerPart) ^ 1) != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchWithWildcards(String testPart, String providerPart) {
        if (testPart == null || providerPart == null) {
            return false;
        }
        int providerLength = providerPart.length();
        if (testPart.length() != providerLength) {
            return false;
        }
        for (int i = 0; i < providerLength; i++) {
            char testChar = testPart.charAt(i);
            char providerChar = providerPart.charAt(i);
            if (testChar != providerChar && providerChar != WILD_CHARACTER) {
                return false;
            }
        }
        return true;
    }

    public static void addListWithoutDuplicate(List<String> originalList, List<String> addList) {
        if (originalList != null && addList != null) {
            Set<String> set = new HashSet(originalList);
            for (String str : addList) {
                if (!TextUtils.isEmpty(str) && set.add(str)) {
                    originalList.add(str);
                }
            }
        }
    }

    public static void removeItemsFromList(List<String> originalList, List<String> removeList) {
        if (originalList != null && removeList != null) {
            Set<String> removeSet = new HashSet(removeList);
            List<String> newList = new ArrayList();
            for (String str : originalList) {
                if (!removeSet.contains(str)) {
                    newList.add(str);
                }
            }
            originalList.clear();
            originalList.addAll(newList);
        }
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void isOverLimit(List<String> originalList, List<String> addList, int limits) {
        if (originalList != null && addList != null && originalList.size() + addList.size() > limits) {
            throw new IllegalArgumentException("The added packages exceeds the supply: " + limits);
        }
    }

    public static void isOverLimit(List<String> originalList, List<String> addList) {
        isOverLimit(originalList, addList, 200);
    }

    public static boolean isValidatePackageNames(List<String> packages) {
        if (packages == null || packages.size() == 0) {
            return true;
        }
        for (String packageName : packages) {
            if (!isValidatePackageName(packageName)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidatePackageName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        int N = name.length();
        boolean hasSep = false;
        boolean front = true;
        if (N > 1000) {
            return false;
        }
        for (int i = 0; i < N; i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = false;
            } else if (front || ((c < '0' || c > '9') && c != '_')) {
                if (c != '.') {
                    return false;
                }
                hasSep = true;
                front = true;
            }
        }
        return hasSep;
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void isAddrOverLimit(List<String> originalList, List<String> addList) {
        if (originalList != null && addList != null && originalList.size() + addList.size() > 1000) {
            throw new IllegalArgumentException("The added addrs exceeds the supply: 1000");
        }
    }

    public static boolean isValidIPAddrs(List<String> addrList) {
        if (addrList == null || addrList.size() == 0) {
            return false;
        }
        for (String addr : addrList) {
            if (!isValidIPAddr(addr)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidIPAddr(String addr) {
        return Patterns.IP_ADDRESS.matcher(addr).matches();
    }

    public static boolean isValidExchangeParameter(Bundle para) {
        if (para == null || isNull(para.getString(EXCHANGE_ID)) || isNull(para.getString(EXCHANGE_LABEL)) || isNull(para.getString(EXCHANGE_DOMAIN)) || isNull(para.getString(EXCHANGE_INCOMING_URI)) || isNull(para.getString(EXCHANGE_INCOMING_USERNAME)) || isNull(para.getString(EXCHANGE_INCOMING_FIELD)) || isNull(para.getString(EXCHANGE_OUTCOMING_URI)) || isNull(para.getString(EXCHANGE_OUTCOMING_USERNAME))) {
            return false;
        }
        return true;
    }

    public static boolean isNull(String str) {
        return str == null;
    }
}
