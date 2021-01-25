package org.bouncycastle.util;

public class IPAddress {
    private static boolean isMaskValue(String str, int i) {
        try {
            int parseInt = Integer.parseInt(str);
            return parseInt >= 0 && parseInt <= i;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValid(String str) {
        return isValidIPv4(str) || isValidIPv6(str);
    }

    public static boolean isValidIPv4(String str) {
        int indexOf;
        if (str.length() == 0) {
            return false;
        }
        String str2 = str + ".";
        int i = 0;
        int i2 = 0;
        while (i < str2.length() && (indexOf = str2.indexOf(46, i)) > i) {
            if (i2 == 4) {
                return false;
            }
            try {
                int parseInt = Integer.parseInt(str2.substring(i, indexOf));
                if (parseInt < 0 || parseInt > 255) {
                    return false;
                }
                i = indexOf + 1;
                i2++;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return i2 == 4;
    }

    public static boolean isValidIPv4WithNetmask(String str) {
        int indexOf = str.indexOf("/");
        String substring = str.substring(indexOf + 1);
        if (indexOf <= 0 || !isValidIPv4(str.substring(0, indexOf))) {
            return false;
        }
        return isValidIPv4(substring) || isMaskValue(substring, 32);
    }

    public static boolean isValidIPv6(String str) {
        int indexOf;
        if (str.length() == 0) {
            return false;
        }
        String str2 = str + ":";
        int i = 0;
        int i2 = 0;
        boolean z = false;
        while (i < str2.length() && (indexOf = str2.indexOf(58, i)) >= i) {
            if (i2 == 8) {
                return false;
            }
            if (i != indexOf) {
                String substring = str2.substring(i, indexOf);
                if (indexOf != str2.length() - 1 || substring.indexOf(46) <= 0) {
                    try {
                        int parseInt = Integer.parseInt(str2.substring(i, indexOf), 16);
                        if (parseInt < 0 || parseInt > 65535) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                } else if (!isValidIPv4(substring)) {
                    return false;
                } else {
                    i2++;
                }
            } else if (indexOf != 1 && indexOf != str2.length() - 1 && z) {
                return false;
            } else {
                z = true;
            }
            i = indexOf + 1;
            i2++;
        }
        return i2 == 8 || z;
    }

    public static boolean isValidIPv6WithNetmask(String str) {
        int indexOf = str.indexOf("/");
        String substring = str.substring(indexOf + 1);
        if (indexOf <= 0 || !isValidIPv6(str.substring(0, indexOf))) {
            return false;
        }
        return isValidIPv6(substring) || isMaskValue(substring, 128);
    }

    public static boolean isValidWithNetMask(String str) {
        return isValidIPv4WithNetmask(str) || isValidIPv6WithNetmask(str);
    }
}
