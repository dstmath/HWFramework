package com.android.org.bouncycastle.asn1.x500.style;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1GeneralizedTime;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERIA5String;
import com.android.org.bouncycastle.asn1.DERPrintableString;
import com.android.org.bouncycastle.asn1.x500.RDN;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x500.X500NameStyle;
import java.util.Hashtable;

public class BCStyle extends AbstractX500NameStyle {
    public static final ASN1ObjectIdentifier BUSINESS_CATEGORY = null;
    public static final ASN1ObjectIdentifier C = null;
    public static final ASN1ObjectIdentifier CN = null;
    public static final ASN1ObjectIdentifier COUNTRY_OF_CITIZENSHIP = null;
    public static final ASN1ObjectIdentifier COUNTRY_OF_RESIDENCE = null;
    public static final ASN1ObjectIdentifier DATE_OF_BIRTH = null;
    public static final ASN1ObjectIdentifier DC = null;
    public static final ASN1ObjectIdentifier DMD_NAME = null;
    public static final ASN1ObjectIdentifier DN_QUALIFIER = null;
    private static final Hashtable DefaultLookUp = null;
    private static final Hashtable DefaultSymbols = null;
    public static final ASN1ObjectIdentifier E = null;
    public static final ASN1ObjectIdentifier EmailAddress = null;
    public static final ASN1ObjectIdentifier GENDER = null;
    public static final ASN1ObjectIdentifier GENERATION = null;
    public static final ASN1ObjectIdentifier GIVENNAME = null;
    public static final ASN1ObjectIdentifier INITIALS = null;
    public static final X500NameStyle INSTANCE = null;
    public static final ASN1ObjectIdentifier L = null;
    public static final ASN1ObjectIdentifier NAME = null;
    public static final ASN1ObjectIdentifier NAME_AT_BIRTH = null;
    public static final ASN1ObjectIdentifier O = null;
    public static final ASN1ObjectIdentifier OU = null;
    public static final ASN1ObjectIdentifier PLACE_OF_BIRTH = null;
    public static final ASN1ObjectIdentifier POSTAL_ADDRESS = null;
    public static final ASN1ObjectIdentifier POSTAL_CODE = null;
    public static final ASN1ObjectIdentifier PSEUDONYM = null;
    public static final ASN1ObjectIdentifier SERIALNUMBER = null;
    public static final ASN1ObjectIdentifier SN = null;
    public static final ASN1ObjectIdentifier ST = null;
    public static final ASN1ObjectIdentifier STREET = null;
    public static final ASN1ObjectIdentifier SURNAME = null;
    public static final ASN1ObjectIdentifier T = null;
    public static final ASN1ObjectIdentifier TELEPHONE_NUMBER = null;
    public static final ASN1ObjectIdentifier UID = null;
    public static final ASN1ObjectIdentifier UNIQUE_IDENTIFIER = null;
    public static final ASN1ObjectIdentifier UnstructuredAddress = null;
    public static final ASN1ObjectIdentifier UnstructuredName = null;
    protected final Hashtable defaultLookUp;
    protected final Hashtable defaultSymbols;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.x500.style.BCStyle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.x500.style.BCStyle.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x500.style.BCStyle.<clinit>():void");
    }

    protected BCStyle() {
        this.defaultSymbols = AbstractX500NameStyle.copyHashTable(DefaultSymbols);
        this.defaultLookUp = AbstractX500NameStyle.copyHashTable(DefaultLookUp);
    }

    protected ASN1Encodable encodeStringValue(ASN1ObjectIdentifier oid, String value) {
        if (oid.equals(EmailAddress) || oid.equals(DC)) {
            return new DERIA5String(value);
        }
        if (oid.equals(DATE_OF_BIRTH)) {
            return new ASN1GeneralizedTime(value);
        }
        if (oid.equals(C) || oid.equals(SN) || oid.equals(DN_QUALIFIER) || oid.equals(TELEPHONE_NUMBER)) {
            return new DERPrintableString(value);
        }
        return super.encodeStringValue(oid, value);
    }

    public String oidToDisplayName(ASN1ObjectIdentifier oid) {
        return (String) DefaultSymbols.get(oid);
    }

    public String[] oidToAttrNames(ASN1ObjectIdentifier oid) {
        return IETFUtils.findAttrNamesForOID(oid, this.defaultLookUp);
    }

    public ASN1ObjectIdentifier attrNameToOID(String attrName) {
        return IETFUtils.decodeAttrName(attrName, this.defaultLookUp);
    }

    public RDN[] fromString(String dirName) {
        return IETFUtils.rDNsFromString(dirName, this);
    }

    public String toString(X500Name name) {
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        RDN[] rdns = name.getRDNs();
        for (RDN appendRDN : rdns) {
            if (first) {
                first = false;
            } else {
                buf.append(',');
            }
            IETFUtils.appendRDN(buf, appendRDN, this.defaultSymbols);
        }
        return buf.toString();
    }
}
