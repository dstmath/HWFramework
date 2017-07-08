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
        this.tag = directoryName;
    }

    public GeneralName(X500Name dirName) {
        this.obj = dirName;
        this.tag = directoryName;
    }

    public GeneralName(int tag, ASN1Encodable name) {
        this.obj = name;
        this.tag = tag;
    }

    public GeneralName(int tag, String name) {
        this.tag = tag;
        if (tag == rfc822Name || tag == dNSName || tag == uniformResourceIdentifier) {
            this.obj = new DERIA5String(name);
        } else if (tag == registeredID) {
            this.obj = new ASN1ObjectIdentifier(name);
        } else if (tag == directoryName) {
            this.obj = new X500Name(name);
        } else if (tag == iPAddress) {
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
                case otherName /*0*/:
                    return new GeneralName(tag, ASN1Sequence.getInstance(tagObj, false));
                case rfc822Name /*1*/:
                    return new GeneralName(tag, DERIA5String.getInstance(tagObj, false));
                case dNSName /*2*/:
                    return new GeneralName(tag, DERIA5String.getInstance(tagObj, false));
                case x400Address /*3*/:
                    throw new IllegalArgumentException("unknown tag: " + tag);
                case directoryName /*4*/:
                    return new GeneralName(tag, X500Name.getInstance(tagObj, true));
                case ediPartyName /*5*/:
                    return new GeneralName(tag, ASN1Sequence.getInstance(tagObj, false));
                case uniformResourceIdentifier /*6*/:
                    return new GeneralName(tag, DERIA5String.getInstance(tagObj, false));
                case iPAddress /*7*/:
                    return new GeneralName(tag, ASN1OctetString.getInstance(tagObj, false));
                case registeredID /*8*/:
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
            case rfc822Name /*1*/:
            case dNSName /*2*/:
            case uniformResourceIdentifier /*6*/:
                buf.append(DERIA5String.getInstance(this.obj).getString());
                break;
            case directoryName /*4*/:
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
                copyInts(parseIPv6(ip), addr, otherName);
                return addr;
            }
            int[] parsedIp;
            addr = new byte[32];
            copyInts(parseIPv6(ip.substring(otherName, slashIndex)), addr, otherName);
            mask = ip.substring(slashIndex + rfc822Name);
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
                addr = new byte[directoryName];
                parseIPv4(ip, addr, otherName);
                return addr;
            }
            addr = new byte[registeredID];
            parseIPv4(ip.substring(otherName, slashIndex), addr, otherName);
            mask = ip.substring(slashIndex + rfc822Name);
            if (mask.indexOf(46) > 0) {
                parseIPv4(mask, addr, directoryName);
            } else {
                parseIPv4Mask(mask, addr, directoryName);
            }
            return addr;
        }
    }

    private void parseIPv4Mask(String mask, byte[] addr, int offset) {
        int maskVal = Integer.parseInt(mask);
        for (int i = otherName; i != maskVal; i += rfc822Name) {
            int i2 = (i / registeredID) + offset;
            addr[i2] = (byte) (addr[i2] | (rfc822Name << (7 - (i % registeredID))));
        }
    }

    private void parseIPv4(String ip, byte[] addr, int offset) {
        StringTokenizer sTok = new StringTokenizer(ip, "./");
        int index = otherName;
        while (sTok.hasMoreTokens()) {
            int index2 = index + rfc822Name;
            addr[offset + index] = (byte) Integer.parseInt(sTok.nextToken());
            index = index2;
        }
    }

    private int[] parseMask(String mask) {
        int[] res = new int[registeredID];
        int maskVal = Integer.parseInt(mask);
        for (int i = otherName; i != maskVal; i += rfc822Name) {
            int i2 = i / 16;
            res[i2] = res[i2] | (rfc822Name << (15 - (i % 16)));
        }
        return res;
    }

    private void copyInts(int[] parsedIp, byte[] addr, int offSet) {
        for (int i = otherName; i != parsedIp.length; i += rfc822Name) {
            addr[(i * dNSName) + offSet] = (byte) (parsedIp[i] >> registeredID);
            addr[((i * dNSName) + rfc822Name) + offSet] = (byte) parsedIp[i];
        }
    }

    private int[] parseIPv6(String ip) {
        StringTokenizer sTok = new StringTokenizer(ip, ":", true);
        int index = otherName;
        int[] val = new int[registeredID];
        if (ip.charAt(otherName) == ':' && ip.charAt(rfc822Name) == ':') {
            sTok.nextToken();
        }
        int doubleColon = -1;
        while (sTok.hasMoreTokens()) {
            String e = sTok.nextToken();
            int index2;
            if (e.equals(":")) {
                doubleColon = index;
                index2 = index + rfc822Name;
                val[index] = otherName;
                index = index2;
            } else if (e.indexOf(46) < 0) {
                index2 = index + rfc822Name;
                val[index] = Integer.parseInt(e, 16);
                if (sTok.hasMoreTokens()) {
                    sTok.nextToken();
                    index = index2;
                } else {
                    index = index2;
                }
            } else {
                StringTokenizer eTok = new StringTokenizer(e, ".");
                index2 = index + rfc822Name;
                val[index] = (Integer.parseInt(eTok.nextToken()) << registeredID) | Integer.parseInt(eTok.nextToken());
                index = index2 + rfc822Name;
                val[index2] = (Integer.parseInt(eTok.nextToken()) << registeredID) | Integer.parseInt(eTok.nextToken());
            }
        }
        if (index != val.length) {
            System.arraycopy(val, doubleColon, val, val.length - (index - doubleColon), index - doubleColon);
            for (int i = doubleColon; i != val.length - (index - doubleColon); i += rfc822Name) {
                val[i] = otherName;
            }
        }
        return val;
    }

    public ASN1Primitive toASN1Primitive() {
        if (this.tag == directoryName) {
            return new DERTaggedObject(true, this.tag, this.obj);
        }
        return new DERTaggedObject(false, this.tag, this.obj);
    }
}
