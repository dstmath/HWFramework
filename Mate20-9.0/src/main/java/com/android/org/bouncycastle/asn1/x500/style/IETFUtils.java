package com.android.org.bouncycastle.asn1.x500.style;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1String;
import com.android.org.bouncycastle.asn1.DERUniversalString;
import com.android.org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import com.android.org.bouncycastle.asn1.x500.RDN;
import com.android.org.bouncycastle.asn1.x500.X500NameBuilder;
import com.android.org.bouncycastle.asn1.x500.X500NameStyle;
import com.android.org.bouncycastle.util.Strings;
import com.android.org.bouncycastle.util.encoders.Hex;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class IETFUtils {
    private static String unescape(String elt) {
        String str = elt;
        if (elt.length() == 0 || (str.indexOf(92) < 0 && str.indexOf(34) < 0)) {
            return elt.trim();
        }
        char[] elts = elt.toCharArray();
        StringBuffer buf = new StringBuffer(elt.length());
        int start = 0;
        if (elts[0] == '\\' && elts[1] == '#') {
            start = 2;
            buf.append("\\#");
        }
        boolean nonWhiteSpaceEncountered = false;
        int lastEscaped = 0;
        char hex1 = 0;
        boolean quoted = false;
        boolean escaped = false;
        for (int i = start; i != elts.length; i++) {
            char c = elts[i];
            if (c != ' ') {
                nonWhiteSpaceEncountered = true;
            }
            if (c == '\"') {
                if (!escaped) {
                    quoted = !quoted;
                } else {
                    buf.append(c);
                }
                escaped = false;
            } else if (c == '\\' && !escaped && !quoted) {
                escaped = true;
                lastEscaped = buf.length();
            } else if (c != ' ' || escaped || nonWhiteSpaceEncountered) {
                if (!escaped || !isHexDigit(c)) {
                    buf.append(c);
                    escaped = false;
                } else if (hex1 != 0) {
                    buf.append((char) ((convertHex(hex1) * 16) + convertHex(c)));
                    escaped = false;
                    hex1 = 0;
                } else {
                    hex1 = c;
                }
            }
        }
        if (buf.length() > 0) {
            while (buf.charAt(buf.length() - 1) == ' ' && lastEscaped != buf.length() - 1) {
                buf.setLength(buf.length() - 1);
            }
        }
        return buf.toString();
    }

    private static boolean isHexDigit(char c) {
        return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
    }

    private static int convertHex(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        if ('a' > c || c > 'f') {
            return (c - 'A') + 10;
        }
        return (c - 'a') + 10;
    }

    public static RDN[] rDNsFromString(String name, X500NameStyle x500Style) {
        X500NameTokenizer nTok = new X500NameTokenizer(name);
        X500NameBuilder builder = new X500NameBuilder(x500Style);
        while (nTok.hasMoreTokens()) {
            String token = nTok.nextToken();
            if (token.indexOf(43) > 0) {
                X500NameTokenizer pTok = new X500NameTokenizer(token, '+');
                X500NameTokenizer vTok = new X500NameTokenizer(pTok.nextToken(), '=');
                String attr = vTok.nextToken();
                if (vTok.hasMoreTokens()) {
                    String value = vTok.nextToken();
                    ASN1ObjectIdentifier oid = x500Style.attrNameToOID(attr.trim());
                    if (pTok.hasMoreTokens()) {
                        Vector oids = new Vector();
                        Vector values = new Vector();
                        oids.addElement(oid);
                        values.addElement(unescape(value));
                        while (pTok.hasMoreTokens()) {
                            X500NameTokenizer vTok2 = new X500NameTokenizer(pTok.nextToken(), '=');
                            String attr2 = vTok2.nextToken();
                            if (vTok2.hasMoreTokens()) {
                                String value2 = vTok2.nextToken();
                                oids.addElement(x500Style.attrNameToOID(attr2.trim()));
                                values.addElement(unescape(value2));
                            } else {
                                throw new IllegalArgumentException("badly formatted directory string");
                            }
                        }
                        builder.addMultiValuedRDN(toOIDArray(oids), toValueArray(values));
                    } else {
                        builder.addRDN(oid, unescape(value));
                    }
                } else {
                    throw new IllegalArgumentException("badly formatted directory string");
                }
            } else {
                X500NameTokenizer vTok3 = new X500NameTokenizer(token, '=');
                String attr3 = vTok3.nextToken();
                if (vTok3.hasMoreTokens()) {
                    builder.addRDN(x500Style.attrNameToOID(attr3.trim()), unescape(vTok3.nextToken()));
                } else {
                    throw new IllegalArgumentException("badly formatted directory string");
                }
            }
        }
        return builder.build().getRDNs();
    }

    private static String[] toValueArray(Vector values) {
        String[] tmp = new String[values.size()];
        for (int i = 0; i != tmp.length; i++) {
            tmp[i] = (String) values.elementAt(i);
        }
        return tmp;
    }

    private static ASN1ObjectIdentifier[] toOIDArray(Vector oids) {
        ASN1ObjectIdentifier[] tmp = new ASN1ObjectIdentifier[oids.size()];
        for (int i = 0; i != tmp.length; i++) {
            tmp[i] = (ASN1ObjectIdentifier) oids.elementAt(i);
        }
        return tmp;
    }

    public static String[] findAttrNamesForOID(ASN1ObjectIdentifier oid, Hashtable lookup) {
        int count = 0;
        Enumeration en = lookup.elements();
        while (en.hasMoreElements()) {
            if (oid.equals(en.nextElement())) {
                count++;
            }
        }
        String[] aliases = new String[count];
        int count2 = 0;
        Enumeration en2 = lookup.keys();
        while (en2.hasMoreElements()) {
            String key = (String) en2.nextElement();
            if (oid.equals(lookup.get(key))) {
                aliases[count2] = key;
                count2++;
            }
        }
        return aliases;
    }

    public static ASN1ObjectIdentifier decodeAttrName(String name, Hashtable lookUp) {
        if (Strings.toUpperCase(name).startsWith("OID.")) {
            return new ASN1ObjectIdentifier(name.substring(4));
        }
        if (name.charAt(0) >= '0' && name.charAt(0) <= '9') {
            return new ASN1ObjectIdentifier(name);
        }
        ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) lookUp.get(Strings.toLowerCase(name));
        if (oid != null) {
            return oid;
        }
        throw new IllegalArgumentException("Unknown object id - " + name + " - passed to distinguished name");
    }

    public static ASN1Encodable valueFromHexString(String str, int off) throws IOException {
        byte[] data = new byte[((str.length() - off) / 2)];
        for (int index = 0; index != data.length; index++) {
            data[index] = (byte) ((convertHex(str.charAt((index * 2) + off)) << 4) | convertHex(str.charAt((index * 2) + off + 1)));
        }
        return ASN1Primitive.fromByteArray(data);
    }

    public static void appendRDN(StringBuffer buf, RDN rdn, Hashtable oidSymbols) {
        if (rdn.isMultiValued()) {
            AttributeTypeAndValue[] atv = rdn.getTypesAndValues();
            boolean firstAtv = true;
            for (int j = 0; j != atv.length; j++) {
                if (firstAtv) {
                    firstAtv = false;
                } else {
                    buf.append('+');
                }
                appendTypeAndValue(buf, atv[j], oidSymbols);
            }
        } else if (rdn.getFirst() != null) {
            appendTypeAndValue(buf, rdn.getFirst(), oidSymbols);
        }
    }

    public static void appendTypeAndValue(StringBuffer buf, AttributeTypeAndValue typeAndValue, Hashtable oidSymbols) {
        String sym = (String) oidSymbols.get(typeAndValue.getType());
        if (sym != null) {
            buf.append(sym);
        } else {
            buf.append(typeAndValue.getType().getId());
        }
        buf.append('=');
        buf.append(valueToString(typeAndValue.getValue()));
    }

    public static String valueToString(ASN1Encodable value) {
        StringBuffer vBuf = new StringBuffer();
        if (!(value instanceof ASN1String) || (value instanceof DERUniversalString)) {
            try {
                vBuf.append("#" + bytesToString(Hex.encode(value.toASN1Primitive().getEncoded(ASN1Encoding.DER))));
            } catch (IOException e) {
                throw new IllegalArgumentException("Other value has no encoded form");
            }
        } else {
            String v = ((ASN1String) value).getString();
            if (v.length() <= 0 || v.charAt(0) != '#') {
                vBuf.append(v);
            } else {
                vBuf.append("\\" + v);
            }
        }
        int end = vBuf.length();
        int index = 0;
        if (vBuf.length() >= 2 && vBuf.charAt(0) == '\\' && vBuf.charAt(1) == '#') {
            index = 0 + 2;
        }
        while (index != end) {
            if (vBuf.charAt(index) == ',' || vBuf.charAt(index) == '\"' || vBuf.charAt(index) == '\\' || vBuf.charAt(index) == '+' || vBuf.charAt(index) == '=' || vBuf.charAt(index) == '<' || vBuf.charAt(index) == '>' || vBuf.charAt(index) == ';') {
                vBuf.insert(index, "\\");
                index++;
                end++;
            }
            index++;
        }
        int start = 0;
        if (vBuf.length() > 0) {
            while (vBuf.length() > start && vBuf.charAt(start) == ' ') {
                vBuf.insert(start, "\\");
                start += 2;
            }
        }
        int endBuf = vBuf.length() - 1;
        while (endBuf >= 0 && vBuf.charAt(endBuf) == ' ') {
            vBuf.insert(endBuf, '\\');
            endBuf--;
        }
        return vBuf.toString();
    }

    private static String bytesToString(byte[] data) {
        char[] cs = new char[data.length];
        for (int i = 0; i != cs.length; i++) {
            cs[i] = (char) (data[i] & 255);
        }
        return new String(cs);
    }

    public static String canonicalize(String s) {
        int start;
        String value = Strings.toLowerCase(s);
        int start2 = 0;
        if (value.length() > 0 && value.charAt(0) == '#') {
            ASN1Primitive obj = decodeObject(value);
            if (obj instanceof ASN1String) {
                value = Strings.toLowerCase(((ASN1String) obj).getString());
            }
        }
        if (value.length() > 1) {
            while (true) {
                start = start2;
                if (start + 1 < value.length() && value.charAt(start) == '\\' && value.charAt(start + 1) == ' ') {
                    start2 = start + 2;
                } else {
                    int end = value.length() - 1;
                }
            }
            int end2 = value.length() - 1;
            while (end2 - 1 > 0 && value.charAt(end2 - 1) == '\\' && value.charAt(end2) == ' ') {
                end2 -= 2;
            }
            if (start > 0 || end2 < value.length() - 1) {
                value = value.substring(start, end2 + 1);
            }
        }
        return stripInternalSpaces(value);
    }

    private static ASN1Primitive decodeObject(String oValue) {
        try {
            return ASN1Primitive.fromByteArray(Hex.decode(oValue.substring(1)));
        } catch (IOException e) {
            throw new IllegalStateException("unknown encoding in name: " + e);
        }
    }

    public static String stripInternalSpaces(String str) {
        StringBuffer res = new StringBuffer();
        if (str.length() != 0) {
            char c1 = str.charAt(0);
            res.append(c1);
            for (int k = 1; k < str.length(); k++) {
                char c2 = str.charAt(k);
                if (c1 != ' ' || c2 != ' ') {
                    res.append(c2);
                }
                c1 = c2;
            }
        }
        return res.toString();
    }

    public static boolean rDNAreEqual(RDN rdn1, RDN rdn2) {
        if (rdn1.isMultiValued()) {
            if (!rdn2.isMultiValued()) {
                return false;
            }
            AttributeTypeAndValue[] atvs1 = rdn1.getTypesAndValues();
            AttributeTypeAndValue[] atvs2 = rdn2.getTypesAndValues();
            if (atvs1.length != atvs2.length) {
                return false;
            }
            for (int i = 0; i != atvs1.length; i++) {
                if (!atvAreEqual(atvs1[i], atvs2[i])) {
                    return false;
                }
            }
            return true;
        } else if (!rdn2.isMultiValued()) {
            return atvAreEqual(rdn1.getFirst(), rdn2.getFirst());
        } else {
            return false;
        }
    }

    private static boolean atvAreEqual(AttributeTypeAndValue atv1, AttributeTypeAndValue atv2) {
        if (atv1 == atv2) {
            return true;
        }
        if (atv1 != null && atv2 != null && atv1.getType().equals(atv2.getType()) && canonicalize(valueToString(atv1.getValue())).equals(canonicalize(valueToString(atv2.getValue())))) {
            return true;
        }
        return false;
    }
}
