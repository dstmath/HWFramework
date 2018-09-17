package com.android.org.bouncycastle.jce;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x500.style.BCStyle;
import java.util.Hashtable;
import java.util.Vector;

public class X509PrincipalEx {
    public static X509Principal getX509Principal(String sCommonName, String sOrganisationUnit, String sOrganisation, String sLocality, String sState, String sCountryCode, String sEmailAddress) {
        Hashtable<ASN1ObjectIdentifier, String> attrs = new Hashtable();
        Vector<ASN1ObjectIdentifier> vOrder = new Vector();
        if (sCommonName != null) {
            attrs.put(BCStyle.CN, sCommonName);
            vOrder.add(0, BCStyle.CN);
        }
        if (sOrganisationUnit != null) {
            attrs.put(BCStyle.OU, sOrganisationUnit);
            vOrder.add(0, BCStyle.OU);
        }
        if (sOrganisation != null) {
            attrs.put(BCStyle.O, sOrganisation);
            vOrder.add(0, BCStyle.O);
        }
        if (sLocality != null) {
            attrs.put(BCStyle.L, sLocality);
            vOrder.add(0, BCStyle.L);
        }
        if (sState != null) {
            attrs.put(BCStyle.ST, sState);
            vOrder.add(0, BCStyle.ST);
        }
        if (sCountryCode != null) {
            attrs.put(BCStyle.C, sCountryCode);
            vOrder.add(0, BCStyle.C);
        }
        if (sEmailAddress != null) {
            attrs.put(BCStyle.E, sEmailAddress);
            vOrder.add(0, BCStyle.E);
        }
        return new X509Principal(vOrder, attrs);
    }
}
