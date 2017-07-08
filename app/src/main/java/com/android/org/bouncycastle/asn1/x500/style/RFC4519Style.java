package com.android.org.bouncycastle.asn1.x500.style;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERIA5String;
import com.android.org.bouncycastle.asn1.DERPrintableString;
import com.android.org.bouncycastle.asn1.x500.RDN;
import com.android.org.bouncycastle.asn1.x500.X500Name;
import com.android.org.bouncycastle.asn1.x500.X500NameStyle;
import java.util.Hashtable;

public class RFC4519Style extends AbstractX500NameStyle {
    private static final Hashtable DefaultLookUp = null;
    private static final Hashtable DefaultSymbols = null;
    public static final X500NameStyle INSTANCE = null;
    public static final ASN1ObjectIdentifier businessCategory = null;
    public static final ASN1ObjectIdentifier c = null;
    public static final ASN1ObjectIdentifier cn = null;
    public static final ASN1ObjectIdentifier dc = null;
    public static final ASN1ObjectIdentifier description = null;
    public static final ASN1ObjectIdentifier destinationIndicator = null;
    public static final ASN1ObjectIdentifier distinguishedName = null;
    public static final ASN1ObjectIdentifier dnQualifier = null;
    public static final ASN1ObjectIdentifier enhancedSearchGuide = null;
    public static final ASN1ObjectIdentifier facsimileTelephoneNumber = null;
    public static final ASN1ObjectIdentifier generationQualifier = null;
    public static final ASN1ObjectIdentifier givenName = null;
    public static final ASN1ObjectIdentifier houseIdentifier = null;
    public static final ASN1ObjectIdentifier initials = null;
    public static final ASN1ObjectIdentifier internationalISDNNumber = null;
    public static final ASN1ObjectIdentifier l = null;
    public static final ASN1ObjectIdentifier member = null;
    public static final ASN1ObjectIdentifier name = null;
    public static final ASN1ObjectIdentifier o = null;
    public static final ASN1ObjectIdentifier ou = null;
    public static final ASN1ObjectIdentifier owner = null;
    public static final ASN1ObjectIdentifier physicalDeliveryOfficeName = null;
    public static final ASN1ObjectIdentifier postOfficeBox = null;
    public static final ASN1ObjectIdentifier postalAddress = null;
    public static final ASN1ObjectIdentifier postalCode = null;
    public static final ASN1ObjectIdentifier preferredDeliveryMethod = null;
    public static final ASN1ObjectIdentifier registeredAddress = null;
    public static final ASN1ObjectIdentifier roleOccupant = null;
    public static final ASN1ObjectIdentifier searchGuide = null;
    public static final ASN1ObjectIdentifier seeAlso = null;
    public static final ASN1ObjectIdentifier serialNumber = null;
    public static final ASN1ObjectIdentifier sn = null;
    public static final ASN1ObjectIdentifier st = null;
    public static final ASN1ObjectIdentifier street = null;
    public static final ASN1ObjectIdentifier telephoneNumber = null;
    public static final ASN1ObjectIdentifier teletexTerminalIdentifier = null;
    public static final ASN1ObjectIdentifier telexNumber = null;
    public static final ASN1ObjectIdentifier title = null;
    public static final ASN1ObjectIdentifier uid = null;
    public static final ASN1ObjectIdentifier uniqueMember = null;
    public static final ASN1ObjectIdentifier userPassword = null;
    public static final ASN1ObjectIdentifier x121Address = null;
    public static final ASN1ObjectIdentifier x500UniqueIdentifier = null;
    protected final Hashtable defaultLookUp;
    protected final Hashtable defaultSymbols;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.x500.style.RFC4519Style.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.x500.style.RFC4519Style.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x500.style.RFC4519Style.<clinit>():void");
    }

    protected RFC4519Style() {
        this.defaultSymbols = AbstractX500NameStyle.copyHashTable(DefaultSymbols);
        this.defaultLookUp = AbstractX500NameStyle.copyHashTable(DefaultLookUp);
    }

    protected ASN1Encodable encodeStringValue(ASN1ObjectIdentifier oid, String value) {
        if (oid.equals(dc)) {
            return new DERIA5String(value);
        }
        if (oid.equals(c) || oid.equals(serialNumber) || oid.equals(dnQualifier) || oid.equals(telephoneNumber)) {
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
        RDN[] tmp = IETFUtils.rDNsFromString(dirName, this);
        RDN[] res = new RDN[tmp.length];
        for (int i = 0; i != tmp.length; i++) {
            res[(res.length - i) - 1] = tmp[i];
        }
        return res;
    }

    public String toString(X500Name name) {
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        RDN[] rdns = name.getRDNs();
        for (int i = rdns.length - 1; i >= 0; i--) {
            if (first) {
                first = false;
            } else {
                buf.append(',');
            }
            IETFUtils.appendRDN(buf, rdns[i], this.defaultSymbols);
        }
        return buf.toString();
    }
}
