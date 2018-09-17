package com.android.org.bouncycastle.util;

public class IPAddress {
    public static boolean isValid(String address) {
        return !isValidIPv4(address) ? isValidIPv6(address) : true;
    }

    public static boolean isValidWithNetMask(String address) {
        return !isValidIPv4WithNetmask(address) ? isValidIPv6WithNetmask(address) : true;
    }

    public static boolean isValidIPv4(String address) {
        boolean z = false;
        if (address.length() == 0) {
            return false;
        }
        int octets = 0;
        String temp = address + ".";
        int start = 0;
        while (start < temp.length()) {
            int pos = temp.indexOf(46, start);
            if (pos <= start) {
                break;
            } else if (octets == 4) {
                return false;
            } else {
                try {
                    int octet = Integer.parseInt(temp.substring(start, pos));
                    if (octet < 0 || octet > 255) {
                        return false;
                    }
                    start = pos + 1;
                    octets++;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        if (octets == 4) {
            z = true;
        }
        return z;
    }

    public static boolean isValidIPv4WithNetmask(String address) {
        int index = address.indexOf("/");
        String mask = address.substring(index + 1);
        if (index <= 0 || !isValidIPv4(address.substring(0, index))) {
            return false;
        }
        return !isValidIPv4(mask) ? isMaskValue(mask, 32) : true;
    }

    public static boolean isValidIPv6WithNetmask(String address) {
        int index = address.indexOf("/");
        String mask = address.substring(index + 1);
        if (index <= 0 || !isValidIPv6(address.substring(0, index))) {
            return false;
        }
        return !isValidIPv6(mask) ? isMaskValue(mask, 128) : true;
    }

    private static boolean isMaskValue(String component, int size) {
        boolean z = false;
        try {
            int value = Integer.parseInt(component);
            if (value >= 0 && value <= size) {
                z = true;
            }
            return z;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidIPv6(String address) {
        if (address.length() == 0) {
            return false;
        }
        int octets = 0;
        String temp = address + ":";
        boolean doubleColonFound = false;
        int start = 0;
        while (start < temp.length()) {
            int pos = temp.indexOf(58, start);
            if (pos < start) {
                break;
            } else if (octets == 8) {
                return false;
            } else {
                if (start != pos) {
                    String value = temp.substring(start, pos);
                    if (pos != temp.length() - 1 || value.indexOf(46) <= 0) {
                        try {
                            int octet = Integer.parseInt(temp.substring(start, pos), 16);
                            if (octet < 0 || octet > 65535) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    } else if (!isValidIPv4(value)) {
                        return false;
                    } else {
                        octets++;
                    }
                } else if (pos != 1 && pos != temp.length() - 1 && doubleColonFound) {
                    return false;
                } else {
                    doubleColonFound = true;
                }
                start = pos + 1;
                octets++;
            }
        }
        if (octets == 8) {
            doubleColonFound = true;
        }
        return doubleColonFound;
    }
}
