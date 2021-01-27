package org.bouncycastle.asn1.x500.style;

import java.util.Hashtable;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.i18n.MessageBundle;

public class RFC4519Style extends AbstractX500NameStyle {
    private static final Hashtable DefaultLookUp = new Hashtable();
    private static final Hashtable DefaultSymbols = new Hashtable();
    public static final X500NameStyle INSTANCE = new RFC4519Style();
    public static final ASN1ObjectIdentifier businessCategory = new ASN1ObjectIdentifier("2.5.4.15").intern();
    public static final ASN1ObjectIdentifier c = new ASN1ObjectIdentifier("2.5.4.6").intern();
    public static final ASN1ObjectIdentifier cn = new ASN1ObjectIdentifier("2.5.4.3").intern();
    public static final ASN1ObjectIdentifier dc = new ASN1ObjectIdentifier("0.9.2342.19200300.100.1.25").intern();
    public static final ASN1ObjectIdentifier description = new ASN1ObjectIdentifier("2.5.4.13").intern();
    public static final ASN1ObjectIdentifier destinationIndicator = new ASN1ObjectIdentifier("2.5.4.27").intern();
    public static final ASN1ObjectIdentifier distinguishedName = new ASN1ObjectIdentifier("2.5.4.49").intern();
    public static final ASN1ObjectIdentifier dnQualifier = new ASN1ObjectIdentifier("2.5.4.46").intern();
    public static final ASN1ObjectIdentifier enhancedSearchGuide = new ASN1ObjectIdentifier("2.5.4.47").intern();
    public static final ASN1ObjectIdentifier facsimileTelephoneNumber = new ASN1ObjectIdentifier("2.5.4.23").intern();
    public static final ASN1ObjectIdentifier generationQualifier = new ASN1ObjectIdentifier("2.5.4.44").intern();
    public static final ASN1ObjectIdentifier givenName = new ASN1ObjectIdentifier("2.5.4.42").intern();
    public static final ASN1ObjectIdentifier houseIdentifier = new ASN1ObjectIdentifier("2.5.4.51").intern();
    public static final ASN1ObjectIdentifier initials = new ASN1ObjectIdentifier("2.5.4.43").intern();
    public static final ASN1ObjectIdentifier internationalISDNNumber = new ASN1ObjectIdentifier("2.5.4.25").intern();
    public static final ASN1ObjectIdentifier l = new ASN1ObjectIdentifier("2.5.4.7").intern();
    public static final ASN1ObjectIdentifier member = new ASN1ObjectIdentifier("2.5.4.31").intern();
    public static final ASN1ObjectIdentifier name = new ASN1ObjectIdentifier("2.5.4.41").intern();
    public static final ASN1ObjectIdentifier o = new ASN1ObjectIdentifier("2.5.4.10").intern();
    public static final ASN1ObjectIdentifier ou = new ASN1ObjectIdentifier("2.5.4.11").intern();
    public static final ASN1ObjectIdentifier owner = new ASN1ObjectIdentifier("2.5.4.32").intern();
    public static final ASN1ObjectIdentifier physicalDeliveryOfficeName = new ASN1ObjectIdentifier("2.5.4.19").intern();
    public static final ASN1ObjectIdentifier postOfficeBox = new ASN1ObjectIdentifier("2.5.4.18").intern();
    public static final ASN1ObjectIdentifier postalAddress = new ASN1ObjectIdentifier("2.5.4.16").intern();
    public static final ASN1ObjectIdentifier postalCode = new ASN1ObjectIdentifier("2.5.4.17").intern();
    public static final ASN1ObjectIdentifier preferredDeliveryMethod = new ASN1ObjectIdentifier("2.5.4.28").intern();
    public static final ASN1ObjectIdentifier registeredAddress = new ASN1ObjectIdentifier("2.5.4.26").intern();
    public static final ASN1ObjectIdentifier roleOccupant = new ASN1ObjectIdentifier("2.5.4.33").intern();
    public static final ASN1ObjectIdentifier searchGuide = new ASN1ObjectIdentifier("2.5.4.14").intern();
    public static final ASN1ObjectIdentifier seeAlso = new ASN1ObjectIdentifier("2.5.4.34").intern();
    public static final ASN1ObjectIdentifier serialNumber = new ASN1ObjectIdentifier("2.5.4.5").intern();
    public static final ASN1ObjectIdentifier sn = new ASN1ObjectIdentifier("2.5.4.4").intern();
    public static final ASN1ObjectIdentifier st = new ASN1ObjectIdentifier("2.5.4.8").intern();
    public static final ASN1ObjectIdentifier street = new ASN1ObjectIdentifier("2.5.4.9").intern();
    public static final ASN1ObjectIdentifier telephoneNumber = new ASN1ObjectIdentifier("2.5.4.20").intern();
    public static final ASN1ObjectIdentifier teletexTerminalIdentifier = new ASN1ObjectIdentifier("2.5.4.22").intern();
    public static final ASN1ObjectIdentifier telexNumber = new ASN1ObjectIdentifier("2.5.4.21").intern();
    public static final ASN1ObjectIdentifier title = new ASN1ObjectIdentifier("2.5.4.12").intern();
    public static final ASN1ObjectIdentifier uid = new ASN1ObjectIdentifier("0.9.2342.19200300.100.1.1").intern();
    public static final ASN1ObjectIdentifier uniqueMember = new ASN1ObjectIdentifier("2.5.4.50").intern();
    public static final ASN1ObjectIdentifier userPassword = new ASN1ObjectIdentifier("2.5.4.35").intern();
    public static final ASN1ObjectIdentifier x121Address = new ASN1ObjectIdentifier("2.5.4.24").intern();
    public static final ASN1ObjectIdentifier x500UniqueIdentifier = new ASN1ObjectIdentifier("2.5.4.45").intern();
    protected final Hashtable defaultLookUp = copyHashTable(DefaultLookUp);
    protected final Hashtable defaultSymbols = copyHashTable(DefaultSymbols);

    static {
        DefaultSymbols.put(businessCategory, "businessCategory");
        DefaultSymbols.put(c, "c");
        DefaultSymbols.put(cn, "cn");
        DefaultSymbols.put(dc, "dc");
        DefaultSymbols.put(description, "description");
        DefaultSymbols.put(destinationIndicator, "destinationIndicator");
        DefaultSymbols.put(distinguishedName, "distinguishedName");
        DefaultSymbols.put(dnQualifier, "dnQualifier");
        DefaultSymbols.put(enhancedSearchGuide, "enhancedSearchGuide");
        DefaultSymbols.put(facsimileTelephoneNumber, "facsimileTelephoneNumber");
        DefaultSymbols.put(generationQualifier, "generationQualifier");
        DefaultSymbols.put(givenName, "givenName");
        DefaultSymbols.put(houseIdentifier, "houseIdentifier");
        DefaultSymbols.put(initials, "initials");
        DefaultSymbols.put(internationalISDNNumber, "internationalISDNNumber");
        DefaultSymbols.put(l, "l");
        DefaultSymbols.put(member, "member");
        DefaultSymbols.put(name, "name");
        DefaultSymbols.put(o, "o");
        DefaultSymbols.put(ou, "ou");
        DefaultSymbols.put(owner, "owner");
        DefaultSymbols.put(physicalDeliveryOfficeName, "physicalDeliveryOfficeName");
        DefaultSymbols.put(postalAddress, "postalAddress");
        DefaultSymbols.put(postalCode, "postalCode");
        DefaultSymbols.put(postOfficeBox, "postOfficeBox");
        DefaultSymbols.put(preferredDeliveryMethod, "preferredDeliveryMethod");
        DefaultSymbols.put(registeredAddress, "registeredAddress");
        DefaultSymbols.put(roleOccupant, "roleOccupant");
        DefaultSymbols.put(searchGuide, "searchGuide");
        DefaultSymbols.put(seeAlso, "seeAlso");
        DefaultSymbols.put(serialNumber, "serialNumber");
        DefaultSymbols.put(sn, "sn");
        DefaultSymbols.put(st, "st");
        DefaultSymbols.put(street, "street");
        DefaultSymbols.put(telephoneNumber, "telephoneNumber");
        DefaultSymbols.put(teletexTerminalIdentifier, "teletexTerminalIdentifier");
        DefaultSymbols.put(telexNumber, "telexNumber");
        DefaultSymbols.put(title, MessageBundle.TITLE_ENTRY);
        DefaultSymbols.put(uid, "uid");
        DefaultSymbols.put(uniqueMember, "uniqueMember");
        DefaultSymbols.put(userPassword, "userPassword");
        DefaultSymbols.put(x121Address, "x121Address");
        DefaultSymbols.put(x500UniqueIdentifier, "x500UniqueIdentifier");
        DefaultLookUp.put("businesscategory", businessCategory);
        DefaultLookUp.put("c", c);
        DefaultLookUp.put("cn", cn);
        DefaultLookUp.put("dc", dc);
        DefaultLookUp.put("description", description);
        DefaultLookUp.put("destinationindicator", destinationIndicator);
        DefaultLookUp.put("distinguishedname", distinguishedName);
        DefaultLookUp.put("dnqualifier", dnQualifier);
        DefaultLookUp.put("enhancedsearchguide", enhancedSearchGuide);
        DefaultLookUp.put("facsimiletelephonenumber", facsimileTelephoneNumber);
        DefaultLookUp.put("generationqualifier", generationQualifier);
        DefaultLookUp.put("givenname", givenName);
        DefaultLookUp.put("houseidentifier", houseIdentifier);
        DefaultLookUp.put("initials", initials);
        DefaultLookUp.put("internationalisdnnumber", internationalISDNNumber);
        DefaultLookUp.put("l", l);
        DefaultLookUp.put("member", member);
        DefaultLookUp.put("name", name);
        DefaultLookUp.put("o", o);
        DefaultLookUp.put("ou", ou);
        DefaultLookUp.put("owner", owner);
        DefaultLookUp.put("physicaldeliveryofficename", physicalDeliveryOfficeName);
        DefaultLookUp.put("postaladdress", postalAddress);
        DefaultLookUp.put("postalcode", postalCode);
        DefaultLookUp.put("postofficebox", postOfficeBox);
        DefaultLookUp.put("preferreddeliverymethod", preferredDeliveryMethod);
        DefaultLookUp.put("registeredaddress", registeredAddress);
        DefaultLookUp.put("roleoccupant", roleOccupant);
        DefaultLookUp.put("searchguide", searchGuide);
        DefaultLookUp.put("seealso", seeAlso);
        DefaultLookUp.put("serialnumber", serialNumber);
        DefaultLookUp.put("sn", sn);
        DefaultLookUp.put("st", st);
        DefaultLookUp.put("street", street);
        DefaultLookUp.put("telephonenumber", telephoneNumber);
        DefaultLookUp.put("teletexterminalidentifier", teletexTerminalIdentifier);
        DefaultLookUp.put("telexnumber", telexNumber);
        DefaultLookUp.put(MessageBundle.TITLE_ENTRY, title);
        DefaultLookUp.put("uid", uid);
        DefaultLookUp.put("uniquemember", uniqueMember);
        DefaultLookUp.put("userpassword", userPassword);
        DefaultLookUp.put("x121address", x121Address);
        DefaultLookUp.put("x500uniqueidentifier", x500UniqueIdentifier);
    }

    protected RFC4519Style() {
    }

    @Override // org.bouncycastle.asn1.x500.X500NameStyle
    public ASN1ObjectIdentifier attrNameToOID(String str) {
        return IETFUtils.decodeAttrName(str, this.defaultLookUp);
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.asn1.x500.style.AbstractX500NameStyle
    public ASN1Encodable encodeStringValue(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
        return aSN1ObjectIdentifier.equals(dc) ? new DERIA5String(str) : (aSN1ObjectIdentifier.equals(c) || aSN1ObjectIdentifier.equals(serialNumber) || aSN1ObjectIdentifier.equals(dnQualifier) || aSN1ObjectIdentifier.equals(telephoneNumber)) ? new DERPrintableString(str) : super.encodeStringValue(aSN1ObjectIdentifier, str);
    }

    @Override // org.bouncycastle.asn1.x500.X500NameStyle
    public RDN[] fromString(String str) {
        RDN[] rDNsFromString = IETFUtils.rDNsFromString(str, this);
        RDN[] rdnArr = new RDN[rDNsFromString.length];
        for (int i = 0; i != rDNsFromString.length; i++) {
            rdnArr[(rdnArr.length - i) - 1] = rDNsFromString[i];
        }
        return rdnArr;
    }

    @Override // org.bouncycastle.asn1.x500.X500NameStyle
    public String[] oidToAttrNames(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return IETFUtils.findAttrNamesForOID(aSN1ObjectIdentifier, this.defaultLookUp);
    }

    @Override // org.bouncycastle.asn1.x500.X500NameStyle
    public String oidToDisplayName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return (String) DefaultSymbols.get(aSN1ObjectIdentifier);
    }

    @Override // org.bouncycastle.asn1.x500.X500NameStyle
    public String toString(X500Name x500Name) {
        StringBuffer stringBuffer = new StringBuffer();
        RDN[] rDNs = x500Name.getRDNs();
        boolean z = true;
        for (int length = rDNs.length - 1; length >= 0; length--) {
            if (z) {
                z = false;
            } else {
                stringBuffer.append(',');
            }
            IETFUtils.appendRDN(stringBuffer, rDNs[length], this.defaultSymbols);
        }
        return stringBuffer.toString();
    }
}
