package com.android.server.wifi.hotspot2;

import com.android.server.wifi.util.TelephonyUtil;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public abstract class Utils {
    private static final int EUI48Length = 6;
    private static final long EUI48Mask = 281474976710655L;
    private static final int EUI64Length = 8;
    private static final String[] PLMNText = {"org", "3gppnetwork", "mcc*", "mnc*", "wlan"};
    public static final long UNSET_TIME = -1;

    public static String hs2LogTag(Class c) {
        return "HS20";
    }

    public static List<String> splitDomain(String domain) {
        if (domain.endsWith(".")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        int at = domain.indexOf(64);
        if (at >= 0) {
            domain = domain.substring(at + 1);
        }
        String[] labels = domain.toLowerCase(Locale.ENGLISH).split("\\.");
        LinkedList<String> labelList = new LinkedList<>();
        for (String label : labels) {
            labelList.addFirst(label);
        }
        return labelList;
    }

    public static long parseMac(String s) {
        if (s != null) {
            long mac = 0;
            int count = 0;
            for (int n = 0; n < s.length(); n++) {
                int nibble = fromHex(s.charAt(n), true);
                if (nibble >= 0) {
                    mac = (mac << 4) | ((long) nibble);
                    count++;
                }
            }
            if (count >= 12 && (count & 1) != 1) {
                return mac;
            }
            throw new IllegalArgumentException("Bad MAC address: '" + s + "'");
        }
        throw new IllegalArgumentException("Null MAC adddress");
    }

    public static String macToString(long mac) {
        int len = (-281474976710656L & mac) != 0 ? 8 : 6;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int n = (len - 1) * 8; n >= 0; n -= 8) {
            if (first) {
                first = false;
            } else {
                sb.append(':');
            }
            sb.append(String.format("%02x", Long.valueOf((mac >>> n) & 255)));
        }
        return sb.toString();
    }

    public static String getMccMnc(List<String> domain) {
        if (domain.size() != PLMNText.length) {
            return null;
        }
        int n = 0;
        while (true) {
            String[] strArr = PLMNText;
            if (n < strArr.length) {
                String expect = strArr[n];
                if (!domain.get(n).regionMatches(0, expect, 0, expect.endsWith("*") ? expect.length() - 1 : expect.length())) {
                    return null;
                }
                n++;
            } else {
                String prefix = domain.get(2).substring(3) + domain.get(3).substring(3);
                for (int n2 = 0; n2 < prefix.length(); n2++) {
                    char ch = prefix.charAt(n2);
                    if (ch < '0' || ch > '9') {
                        return null;
                    }
                }
                return prefix;
            }
        }
    }

    public static String getRealmForMccMnc(String mccmnc) {
        if (mccmnc == null) {
            return null;
        }
        if (mccmnc.length() != 5 && mccmnc.length() != 6) {
            return null;
        }
        String mcc = mccmnc.substring(0, 3);
        String mnc = mccmnc.substring(3);
        if (mnc.length() == 2) {
            mnc = "0" + mnc;
        }
        return String.format(TelephonyUtil.THREE_GPP_NAI_REALM_FORMAT, mnc, mcc);
    }

    public static boolean isCarrierEapMethod(int eapMethod) {
        return eapMethod == 18 || eapMethod == 23 || eapMethod == 50;
    }

    public static String roamingConsortiumsToString(long[] ois) {
        if (ois == null) {
            return "null";
        }
        List<Long> list = new ArrayList<>(ois.length);
        for (long oi : ois) {
            list.add(Long.valueOf(oi));
        }
        return roamingConsortiumsToString(list);
    }

    public static String roamingConsortiumsToString(Collection<Long> ois) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Long l : ois) {
            long oi = l.longValue();
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            if (Long.numberOfLeadingZeros(oi) > 40) {
                sb.append(String.format("%06x", Long.valueOf(oi)));
            } else {
                sb.append(String.format("%010x", Long.valueOf(oi)));
            }
        }
        return sb.toString();
    }

    public static String toUnicodeEscapedString(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int n = 0; n < s.length(); n++) {
            char ch = s.charAt(n);
            if (ch < ' ' || ch >= 127) {
                sb.append("\\u");
                sb.append(String.format("%04x", Integer.valueOf(ch)));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String toHexString(byte[] data) {
        if (data == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(data.length * 3);
        boolean first = true;
        for (byte b : data) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(String.format("%02x", Integer.valueOf(b & 255)));
        }
        return sb.toString();
    }

    public static String toHex(byte[] octets) {
        StringBuilder sb = new StringBuilder(octets.length * 2);
        int length = octets.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", Integer.valueOf(octets[i] & 255)));
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String text) {
        if ((text.length() & 1) != 1) {
            byte[] data = new byte[(text.length() >> 1)];
            int position = 0;
            for (int n = 0; n < text.length(); n += 2) {
                data[position] = (byte) (((fromHex(text.charAt(n), false) & 15) << 4) | (fromHex(text.charAt(n + 1), false) & 15));
                position++;
            }
            return data;
        }
        throw new NumberFormatException("Odd length hex string: " + text.length());
    }

    public static int fromHex(char ch, boolean lenient) throws NumberFormatException {
        if (ch <= '9' && ch >= '0') {
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'f') {
            return (ch + '\n') - 97;
        }
        if (ch <= 'F' && ch >= 'A') {
            return (ch + '\n') - 65;
        }
        if (lenient) {
            return -1;
        }
        throw new NumberFormatException("Bad hex-character: " + ch);
    }

    private static char toAscii(int b) {
        if (b < 32 || b >= 127) {
            return '.';
        }
        return (char) b;
    }

    static boolean isDecimal(String s) {
        for (int n = 0; n < s.length(); n++) {
            char ch = s.charAt(n);
            if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static <T extends Comparable> int compare(Comparable<T> c1, T c2) {
        if (c1 == null) {
            return c2 == null ? 0 : -1;
        }
        if (c2 == null) {
            return 1;
        }
        return c1.compareTo(c2);
    }

    public static String bytesToBingoCard(ByteBuffer data, int len) {
        ByteBuffer dup = data.duplicate();
        dup.limit(dup.position() + len);
        return bytesToBingoCard(dup);
    }

    public static String bytesToBingoCard(ByteBuffer data) {
        ByteBuffer dup = data.duplicate();
        StringBuilder sbx = new StringBuilder();
        while (dup.hasRemaining()) {
            sbx.append(String.format("%02x ", Integer.valueOf(dup.get() & 255)));
        }
        ByteBuffer dup2 = data.duplicate();
        sbx.append(' ');
        while (dup2.hasRemaining()) {
            sbx.append(String.format("%c", Character.valueOf(toAscii(dup2.get() & 255))));
        }
        return sbx.toString();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x002a: APUT  (r12v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r0v2 java.lang.String) */
    public static String toHMS(long millis) {
        long time = millis >= 0 ? millis : -millis;
        long tmp = time / 1000;
        long ms = time - (1000 * tmp);
        long tmp2 = tmp / 60;
        long s = tmp - (tmp2 * 60);
        long tmp3 = tmp2 / 60;
        long m = tmp2 - (60 * tmp3);
        Object[] objArr = new Object[5];
        objArr[0] = millis < 0 ? "-" : "";
        objArr[1] = Long.valueOf(tmp3);
        objArr[2] = Long.valueOf(m);
        objArr[3] = Long.valueOf(s);
        objArr[4] = Long.valueOf(ms);
        return String.format("%s%d:%02d:%02d.%03d", objArr);
    }

    public static String toUTCString(long ms) {
        if (ms < 0) {
            return "unset";
        }
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(ms);
        return String.format("%4d/%02d/%02d %2d:%02d:%02dZ", Integer.valueOf(c.get(1)), Integer.valueOf(c.get(2) + 1), Integer.valueOf(c.get(5)), Integer.valueOf(c.get(11)), Integer.valueOf(c.get(12)), Integer.valueOf(c.get(13)));
    }

    public static String unquote(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() <= 1 || !s.startsWith("\"") || !s.endsWith("\"")) {
            return s;
        }
        return s.substring(1, s.length() - 1);
    }
}
