package com.android.server.devicepolicy;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import com.huawei.utils.reflect.EasyInvokeUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HwDevicePolicyManagerServiceUtil extends EasyInvokeUtils {
    private static final String DOMAIN_SEPARATOR = "\\.";
    private static final String[] EXCHANGE_ITEM = {"id", "label", "domain", "incominguri", "incomingusername", "incomingfield", "outgoinguri", "outgoingusername"};
    private static final int MAX_ADDRS = 1000;
    private static final int MAX_LENGTH_OF_PACKAGE_NAME = 1000;
    private static final int MAX_PACKAGES = 200;
    private static final String TAG = "HwDevicePolicyManagerServiceUtil";
    private static final String USB_STORAGE = "usb";
    private static final char WILD_CHARACTER = '?';
    private static final String WILD_STRING = "*";
    private static Context sContext = null;

    public static boolean initialize(Context context) {
        if (context == null) {
            HwLog.e(TAG, "initialize context is null");
            return false;
        }
        sContext = context;
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
            String testPart = testParts[i].toLowerCase(Locale.ROOT);
            String providerPart = providerParts[i].toLowerCase(Locale.ROOT);
            if (!(WILD_STRING.equals(providerPart) || matchWithWildcards(testPart, providerPart))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isParameterValid(String string1, String string2) {
        if (string1 == null || string2 == null || string1.length() != string2.length()) {
            return false;
        }
        return true;
    }

    public static boolean matchWithWildcards(String testPart, String providerPart) {
        if (!isParameterValid(testPart, providerPart)) {
            return false;
        }
        int length = providerPart.length();
        for (int i = 0; i < length; i++) {
            char testChar = testPart.charAt(i);
            char providerChar = providerPart.charAt(i);
            if (!(testChar == providerChar || providerChar == '?')) {
                return false;
            }
        }
        return true;
    }

    public static void addListWithoutDuplicate(List<String> originalList, List<String> addList) {
        if (!(originalList == null || addList == null)) {
            Set<String> set = new HashSet<>(originalList);
            for (String str : addList) {
                if (!TextUtils.isEmpty(str) && set.add(str)) {
                    originalList.add(str);
                }
            }
        }
    }

    public static void removeItemsFromList(List<String> originalList, List<String> removeList) {
        if (!(originalList == null || removeList == null)) {
            Set<String> removeSet = new HashSet<>(removeList);
            List<String> newList = new ArrayList<>();
            for (String str : originalList) {
                if (!removeSet.contains(str)) {
                    newList.add(str);
                }
            }
            originalList.clear();
            originalList.addAll(newList);
        }
    }

    public static void isOverLimit(List<String> originalList, List<String> addList, int limits) throws IllegalArgumentException {
        if (addList != null) {
            if (addList.size() + (originalList == null ? 0 : originalList.size()) > limits) {
                throw new IllegalArgumentException("The added packages exceeds the supply: " + limits);
            }
        }
    }

    public static void isOverLimit(List<String> originalList, List<String> addList) {
        isOverLimit(originalList, addList, MAX_PACKAGES);
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
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        int length = name.length();
        boolean isHasSep = false;
        boolean isFront = true;
        if (length > 1000) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (Character.isLowerCase(c) || Character.isUpperCase(c)) {
                isFront = false;
            } else if (isFront || (!Character.isDigit(c) && c != '_')) {
                if (c != '.') {
                    return false;
                }
                isHasSep = true;
                isFront = true;
            }
        }
        return isHasSep;
    }

    public static void isAddrOverLimit(List<String> originalList, List<String> addList) {
        if (originalList != null && addList != null && originalList.size() + addList.size() > 1000) {
            throw new IllegalArgumentException("The added addrs exceeds the supply: 1000");
        }
    }

    public static boolean isValidIpAddrs(List<String> addrList) {
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
        if (para == null) {
            return false;
        }
        int i = 0;
        while (true) {
            String[] strArr = EXCHANGE_ITEM;
            if (i >= strArr.length) {
                return true;
            }
            if (para.getString(strArr[i]) == null) {
                return false;
            }
            i++;
        }
    }
}
