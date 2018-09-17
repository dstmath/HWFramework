package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Choice;
import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERIA5String;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.util.IPAddress;
import java.io.IOException;
import java.util.StringTokenizer;

public class GeneralName extends ASN1Object implements ASN1Choice {
    public static final int dNSName = 2;
    public static final int directoryName = 4;
    public static final int ediPartyName = 5;
    public static final int iPAddress = 7;
    public static final int otherName = 0;
    public static final int registeredID = 8;
    public static final int rfc822Name = 1;
    public static final int uniformResourceIdentifier = 6;
    public static final int x400Address = 3;
    private ASN1Encodable obj;
    private int tag;

    public GeneralName(X509Name dirName) {
        this.obj = X500Name.getInstance(dirName);
        this.tag = 4;
    }

    public GeneralName(X500Name dirName) {
        this.obj = dirName;
        this.tag = 4;
    }

    public GeneralName(int tag, ASN1Encodable name) {
        this.obj = name;
        this.tag = tag;
    }

    public GeneralName(int tag, String name) {
        this.tag = tag;
        if (tag == 1 || tag == 2 || tag == 6) {
            this.obj = new DERIA5String(name);
        } else if (tag == 8) {
            this.obj = new ASN1ObjectIdentifier(name);
        } else if (tag == 4) {
            this.obj = new X500Name(name);
        } else if (tag == 7) {
            byte[] enc = toGeneralNameEncoding(name);
            if (enc != null) {
                this.obj = new DEROctetString(enc);
                return;
            }
            throw new IllegalArgumentException("IP Address is invalid");
        } else {
            throw new IllegalArgumentException("can't process String for tag: " + tag);
        }
    }

    public static GeneralName getInstance(Object obj) {
        if (obj == null || (obj instanceof GeneralName)) {
            return (GeneralName) obj;
        }
        if (obj instanceof ASN1TaggedObject) {
            ASN1TaggedObject tagObj = (ASN1TaggedObject) obj;
            int tag = tagObj.getTagNo();
            switch (tag) {
                case 0:
                    return new GeneralName(tag, ASN1Sequence.getInstance(tagObj, false));
                case 1:
                    return new GeneralName(tag, DERIA5String.getInstance(tagObj, false));
                case 2:
                    return new GeneralName(tag, DERIA5String.getInstance(tagObj, false));
                case 3:
                    throw new IllegalArgumentException("unknown tag: " + tag);
                case 4:
                    return new GeneralName(tag, X500Name.getInstance(tagObj, true));
                case 5:
                    return new GeneralName(tag, ASN1Sequence.getInstance(tagObj, false));
                case 6:
                    return new GeneralName(tag, DERIA5String.getInstance(tagObj, false));
                case 7:
                    return new GeneralName(tag, ASN1OctetString.getInstance(tagObj, false));
                case 8:
                    return new GeneralName(tag, ASN1ObjectIdentifier.getInstance(tagObj, false));
            }
        }
        if (obj instanceof byte[]) {
            try {
                return getInstance(ASN1Primitive.fromByteArray((byte[]) obj));
            } catch (IOException e) {
                throw new IllegalArgumentException("unable to parse encoded general name");
            }
        }
        throw new IllegalArgumentException("unknown object in getInstance: " + obj.getClass().getName());
    }

    public static GeneralName getInstance(ASN1TaggedObject tagObj, boolean explicit) {
        return getInstance(ASN1TaggedObject.getInstance(tagObj, true));
    }

    public int getTagNo() {
        return this.tag;
    }

    public ASN1Encodable getName() {
        return this.obj;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.tag);
        buf.append(": ");
        switch (this.tag) {
            case 1:
            case 2:
            case 6:
                buf.append(DERIA5String.getInstance(this.obj).getString());
                break;
            case 4:
                buf.append(X500Name.getInstance(this.obj).toString());
                break;
            default:
                buf.append(this.obj.toString());
                break;
        }
        return buf.toString();
    }

    private byte[] toGeneralNameEncoding(String ip) {
        int slashIndex;
        byte[] addr;
        String mask;
        if (IPAddress.isValidIPv6WithNetmask(ip) || IPAddress.isValidIPv6(ip)) {
            slashIndex = ip.indexOf(47);
            if (slashIndex < 0) {
                addr = new byte[16];
                copyInts(parseIPv6(ip), addr, 0);
                return addr;
            }
            int[] parsedIp;
            addr = new byte[32];
            copyInts(parseIPv6(ip.substring(0, slashIndex)), addr, 0);
            mask = ip.substring(slashIndex + 1);
            if (mask.indexOf(58) > 0) {
                parsedIp = parseIPv6(mask);
            } else {
                parsedIp = parseMask(mask);
            }
            copyInts(parsedIp, addr, 16);
            return addr;
        } else if (!IPAddress.isValidIPv4WithNetmask(ip) && !IPAddress.isValidIPv4(ip)) {
            return null;
        } else {
            slashIndex = ip.indexOf(47);
            if (slashIndex < 0) {
                addr = new byte[4];
                parseIPv4(ip, addr, 0);
                return addr;
            }
            addr = new byte[8];
            parseIPv4(ip.substring(0, slashIndex), addr, 0);
            mask = ip.substring(slashIndex + 1);
            if (mask.indexOf(46) > 0) {
                parseIPv4(mask, addr, 4);
            } else {
                parseIPv4Mask(mask, addr, 4);
            }
            return addr;
        }
    }

    private void parseIPv4Mask(String mask, byte[] addr, int offset) {
        int maskVal = Integer.parseInt(mask);
        for (int i = 0; i != maskVal; i++) {
            int i2 = (i / 8) + offset;
            addr[i2] = (byte) (addr[i2] | (1 << (7 - (i % 8))));
        }
    }

    private void parseIPv4(String ip, byte[] addr, int offset) {
        StringTokenizer sTok = new StringTokenizer(ip, "./");
        int index = 0;
        while (sTok.hasMoreTokens()) {
            int index2 = index + 1;
            addr[offset + index] = (byte) Integer.parseInt(sTok.nextToken());
            index = index2;
        }
    }

    private int[] parseMask(String mask) {
        int[] res = new int[8];
        int maskVal = Integer.parseInt(mask);
        for (int i = 0; i != maskVal; i++) {
            int i2 = i / 16;
            res[i2] = res[i2] | (1 << (15 - (i % 16)));
        }
        return res;
    }

    private void copyInts(int[] parsedIp, byte[] addr, int offSet) {
        for (int i = 0; i != parsedIp.length; i++) {
            addr[(i * 2) + offSet] = (byte) (parsedIp[i] >> 8);
            addr[((i * 2) + 1) + offSet] = (byte) parsedIp[i];
        }
    }

    private int[] parseIPv6(String ip) {
        StringTokenizer sTok = new StringTokenizer(ip, ":", true);
        int index = 0;
        int[] val = new int[8];
        if (ip.charAt(0) == ':' && ip.charAt(1) == ':') {
            sTok.nextToken();
        }
        int doubleColon = -1;
        while (sTok.hasMoreTokens()) {
            String e = sTok.nextToken();
            int index2;
            if (e.equals(":")) {
                doubleColon = index;
                index2 = index + 1;
                val[index] = 0;
                index = index2;
            } else if (e.indexOf(46) < 0) {
                index2 = index + 1;
                val[index] = Integer.parseInt(e, 16);
                if (sTok.hasMoreTokens()) {
                    sTok.nextToken();
                    index = index2;
                } else {
                    index = index2;
                }
            } else {
                StringTokenizer eTok = new StringTokenizer(e, ".");
                index2 = index + 1;
                val[index] = (Integer.parseInt(eTok.nextToken()) << 8) | Integer.parseInt(eTok.nextToken());
                index = index2 + 1;
                val[index2] = (Integer.parseInt(eTok.nextToken()) << 8) | Integer.parseInt(eTok.nextToken());
            }
        }
        if (index != val.length) {
            System.arraycopy(val, doubleColon, val, val.length - (index - doubleColon), index - doubleColon);
            for (int i = doubleColon; i != val.length - (index - doubleColon); i++) {
                val[i] = 0;
            }
        }
        return val;
    }

    public ASN1Primitive toASN1Primitive() {
        if (this.tag == 4) {
            return new DERTaggedObject(true, this.tag, this.obj);
        }
        return new DERTaggedObject(false, this.tag, this.obj);
    }
}
