package com.android.org.bouncycastle.jce.provider;

import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERIA5String;
import com.android.org.bouncycastle.asn1.x509.GeneralName;
import com.android.org.bouncycastle.asn1.x509.GeneralSubtree;
import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Integers;
import com.android.org.bouncycastle.util.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PKIXNameConstraintValidator {
    private Set excludedSubtreesDN = new HashSet();
    private Set excludedSubtreesDNS = new HashSet();
    private Set excludedSubtreesEmail = new HashSet();
    private Set excludedSubtreesIP = new HashSet();
    private Set excludedSubtreesURI = new HashSet();
    private Set permittedSubtreesDN;
    private Set permittedSubtreesDNS;
    private Set permittedSubtreesEmail;
    private Set permittedSubtreesIP;
    private Set permittedSubtreesURI;

    private static boolean withinDNSubtree(ASN1Sequence dns, ASN1Sequence subtree) {
        if (subtree.size() < 1 || subtree.size() > dns.size()) {
            return false;
        }
        for (int j = subtree.size() - 1; j >= 0; j--) {
            if (!subtree.getObjectAt(j).equals(dns.getObjectAt(j))) {
                return false;
            }
        }
        return true;
    }

    public void checkPermittedDN(ASN1Sequence dns) throws PKIXNameConstraintValidatorException {
        checkPermittedDN(this.permittedSubtreesDN, dns);
    }

    public void checkExcludedDN(ASN1Sequence dns) throws PKIXNameConstraintValidatorException {
        checkExcludedDN(this.excludedSubtreesDN, dns);
    }

    private void checkPermittedDN(Set permitted, ASN1Sequence dns) throws PKIXNameConstraintValidatorException {
        if (permitted != null) {
            if (!permitted.isEmpty() || dns.size() != 0) {
                for (ASN1Sequence subtree : permitted) {
                    if (withinDNSubtree(dns, subtree)) {
                        return;
                    }
                }
                throw new PKIXNameConstraintValidatorException("Subject distinguished name is not from a permitted subtree");
            }
        }
    }

    private void checkExcludedDN(Set excluded, ASN1Sequence dns) throws PKIXNameConstraintValidatorException {
        if (!excluded.isEmpty()) {
            for (ASN1Sequence subtree : excluded) {
                if (withinDNSubtree(dns, subtree)) {
                    throw new PKIXNameConstraintValidatorException("Subject distinguished name is from an excluded subtree");
                }
            }
        }
    }

    private Set intersectDN(Set permitted, Set dns) {
        Set intersect = new HashSet();
        for (GeneralSubtree base : dns) {
            ASN1Sequence dn = ASN1Sequence.getInstance(base.getBase().getName().toASN1Primitive());
            if (permitted != null) {
                for (ASN1Sequence subtree : permitted) {
                    if (withinDNSubtree(dn, subtree)) {
                        intersect.add(dn);
                    } else if (withinDNSubtree(subtree, dn)) {
                        intersect.add(subtree);
                    }
                }
            } else if (dn != null) {
                intersect.add(dn);
            }
        }
        return intersect;
    }

    private Set unionDN(Set excluded, ASN1Sequence dn) {
        if (!excluded.isEmpty()) {
            Set intersect = new HashSet();
            for (ASN1Sequence subtree : excluded) {
                if (withinDNSubtree(dn, subtree)) {
                    intersect.add(subtree);
                } else if (withinDNSubtree(subtree, dn)) {
                    intersect.add(dn);
                } else {
                    intersect.add(subtree);
                    intersect.add(dn);
                }
            }
            return intersect;
        } else if (dn == null) {
            return excluded;
        } else {
            excluded.add(dn);
            return excluded;
        }
    }

    private Set intersectEmail(Set permitted, Set emails) {
        Set intersect = new HashSet();
        for (GeneralSubtree base : emails) {
            String email = extractNameAsString(base.getBase());
            if (permitted != null) {
                for (String _permitted : permitted) {
                    intersectEmail(email, _permitted, intersect);
                }
            } else if (email != null) {
                intersect.add(email);
            }
        }
        return intersect;
    }

    private Set unionEmail(Set excluded, String email) {
        if (!excluded.isEmpty()) {
            Set union = new HashSet();
            for (String _excluded : excluded) {
                unionEmail(_excluded, email, union);
            }
            return union;
        } else if (email == null) {
            return excluded;
        } else {
            excluded.add(email);
            return excluded;
        }
    }

    private Set intersectIP(Set permitted, Set ips) {
        Set intersect = new HashSet();
        for (GeneralSubtree base : ips) {
            byte[] ip = ASN1OctetString.getInstance(base.getBase().getName()).getOctets();
            if (permitted != null) {
                for (byte[] _permitted : permitted) {
                    intersect.addAll(intersectIPRange(_permitted, ip));
                }
            } else if (ip != null) {
                intersect.add(ip);
            }
        }
        return intersect;
    }

    private Set unionIP(Set excluded, byte[] ip) {
        if (!excluded.isEmpty()) {
            Set union = new HashSet();
            for (byte[] _excluded : excluded) {
                union.addAll(unionIPRange(_excluded, ip));
            }
            return union;
        } else if (ip == null) {
            return excluded;
        } else {
            excluded.add(ip);
            return excluded;
        }
    }

    private Set unionIPRange(byte[] ipWithSubmask1, byte[] ipWithSubmask2) {
        Set set = new HashSet();
        if (Arrays.areEqual(ipWithSubmask1, ipWithSubmask2)) {
            set.add(ipWithSubmask1);
        } else {
            set.add(ipWithSubmask1);
            set.add(ipWithSubmask2);
        }
        return set;
    }

    private Set intersectIPRange(byte[] ipWithSubmask1, byte[] ipWithSubmask2) {
        if (ipWithSubmask1.length != ipWithSubmask2.length) {
            return Collections.EMPTY_SET;
        }
        byte[][] temp = extractIPsAndSubnetMasks(ipWithSubmask1, ipWithSubmask2);
        byte[] ip1 = temp[0];
        byte[] subnetmask1 = temp[1];
        byte[] ip2 = temp[2];
        byte[] subnetmask2 = temp[3];
        byte[][] minMax = minMaxIPs(ip1, subnetmask1, ip2, subnetmask2);
        if (compareTo(max(minMax[0], minMax[2]), min(minMax[1], minMax[3])) == 1) {
            return Collections.EMPTY_SET;
        }
        return Collections.singleton(ipWithSubnetMask(or(minMax[0], minMax[2]), or(subnetmask1, subnetmask2)));
    }

    private byte[] ipWithSubnetMask(byte[] ip, byte[] subnetMask) {
        int ipLength = ip.length;
        byte[] temp = new byte[(ipLength * 2)];
        System.arraycopy(ip, 0, temp, 0, ipLength);
        System.arraycopy(subnetMask, 0, temp, ipLength, ipLength);
        return temp;
    }

    private byte[][] extractIPsAndSubnetMasks(byte[] ipWithSubmask1, byte[] ipWithSubmask2) {
        int ipLength = ipWithSubmask1.length / 2;
        byte[] subnetmask1 = new byte[ipLength];
        System.arraycopy(ipWithSubmask1, 0, new byte[ipLength], 0, ipLength);
        System.arraycopy(ipWithSubmask1, ipLength, subnetmask1, 0, ipLength);
        byte[] subnetmask2 = new byte[ipLength];
        System.arraycopy(ipWithSubmask2, 0, new byte[ipLength], 0, ipLength);
        System.arraycopy(ipWithSubmask2, ipLength, subnetmask2, 0, ipLength);
        return new byte[][]{ip1, subnetmask1, ip2, subnetmask2};
    }

    private byte[][] minMaxIPs(byte[] ip1, byte[] subnetmask1, byte[] ip2, byte[] subnetmask2) {
        int ipLength = ip1.length;
        byte[] min1 = new byte[ipLength];
        byte[] max1 = new byte[ipLength];
        byte[] min2 = new byte[ipLength];
        byte[] max2 = new byte[ipLength];
        for (int i = 0; i < ipLength; i++) {
            min1[i] = (byte) (ip1[i] & subnetmask1[i]);
            max1[i] = (byte) ((ip1[i] & subnetmask1[i]) | (~subnetmask1[i]));
            min2[i] = (byte) (ip2[i] & subnetmask2[i]);
            max2[i] = (byte) ((ip2[i] & subnetmask2[i]) | (~subnetmask2[i]));
        }
        return new byte[][]{min1, max1, min2, max2};
    }

    private void checkPermittedEmail(Set permitted, String email) throws PKIXNameConstraintValidatorException {
        if (permitted != null) {
            for (String str : permitted) {
                if (emailIsConstrained(email, str)) {
                    return;
                }
            }
            if (email.length() != 0 || permitted.size() != 0) {
                throw new PKIXNameConstraintValidatorException("Subject email address is not from a permitted subtree.");
            }
        }
    }

    private void checkExcludedEmail(Set excluded, String email) throws PKIXNameConstraintValidatorException {
        if (!excluded.isEmpty()) {
            for (String str : excluded) {
                if (emailIsConstrained(email, str)) {
                    throw new PKIXNameConstraintValidatorException("Email address is from an excluded subtree.");
                }
            }
        }
    }

    private void checkPermittedIP(Set permitted, byte[] ip) throws PKIXNameConstraintValidatorException {
        if (permitted != null) {
            for (byte[] ipWithSubnet : permitted) {
                if (isIPConstrained(ip, ipWithSubnet)) {
                    return;
                }
            }
            if (ip.length != 0 || permitted.size() != 0) {
                throw new PKIXNameConstraintValidatorException("IP is not from a permitted subtree.");
            }
        }
    }

    private void checkExcludedIP(Set excluded, byte[] ip) throws PKIXNameConstraintValidatorException {
        if (!excluded.isEmpty()) {
            for (byte[] ipWithSubnet : excluded) {
                if (isIPConstrained(ip, ipWithSubnet)) {
                    throw new PKIXNameConstraintValidatorException("IP is from an excluded subtree.");
                }
            }
        }
    }

    private boolean isIPConstrained(byte[] ip, byte[] constraint) {
        int ipLength = ip.length;
        if (ipLength != constraint.length / 2) {
            return false;
        }
        byte[] subnetMask = new byte[ipLength];
        System.arraycopy(constraint, ipLength, subnetMask, 0, ipLength);
        byte[] permittedSubnetAddress = new byte[ipLength];
        byte[] ipSubnetAddress = new byte[ipLength];
        for (int i = 0; i < ipLength; i++) {
            permittedSubnetAddress[i] = (byte) (constraint[i] & subnetMask[i]);
            ipSubnetAddress[i] = (byte) (ip[i] & subnetMask[i]);
        }
        return Arrays.areEqual(permittedSubnetAddress, ipSubnetAddress);
    }

    private boolean emailIsConstrained(String email, String constraint) {
        String sub = email.substring(email.indexOf(64) + 1);
        if (constraint.indexOf(64) != -1) {
            return email.equalsIgnoreCase(constraint) || sub.equalsIgnoreCase(constraint.substring(1));
        } else {
            boolean z;
            if (constraint.charAt(0) == '.') {
                z = true;
            } else {
                z = false;
            }
            if (z) {
                if (withinDomain(sub, constraint)) {
                    return true;
                }
            } else if (sub.equalsIgnoreCase(constraint)) {
                return true;
            }
        }
    }

    private boolean withinDomain(String testDomain, String domain) {
        String tempDomain = domain;
        if (domain.startsWith(".")) {
            tempDomain = domain.substring(1);
        }
        String[] domainParts = Strings.split(tempDomain, '.');
        String[] testDomainParts = Strings.split(testDomain, '.');
        if (testDomainParts.length <= domainParts.length) {
            return false;
        }
        int d = testDomainParts.length - domainParts.length;
        for (int i = -1; i < domainParts.length; i++) {
            if (i == -1) {
                if (testDomainParts[i + d].equals("")) {
                    return false;
                }
            } else if (!domainParts[i].equalsIgnoreCase(testDomainParts[i + d])) {
                return false;
            }
        }
        return true;
    }

    private void checkPermittedDNS(Set permitted, String dns) throws PKIXNameConstraintValidatorException {
        if (permitted != null) {
            for (String str : permitted) {
                if (!withinDomain(dns, str)) {
                    if (dns.equalsIgnoreCase(str)) {
                    }
                }
                return;
            }
            if (dns.length() != 0 || permitted.size() != 0) {
                throw new PKIXNameConstraintValidatorException("DNS is not from a permitted subtree.");
            }
        }
    }

    private void checkExcludedDNS(Set excluded, String dns) throws PKIXNameConstraintValidatorException {
        if (!excluded.isEmpty()) {
            for (String str : excluded) {
                if (!withinDomain(dns, str)) {
                    if (dns.equalsIgnoreCase(str)) {
                    }
                }
                throw new PKIXNameConstraintValidatorException("DNS is from an excluded subtree.");
            }
        }
    }

    private void unionEmail(String email1, String email2, Set union) {
        if (email1.indexOf(64) != -1) {
            String _sub = email1.substring(email1.indexOf(64) + 1);
            if (email2.indexOf(64) != -1) {
                if (email1.equalsIgnoreCase(email2)) {
                    union.add(email1);
                    return;
                }
                union.add(email1);
                union.add(email2);
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    union.add(email2);
                    return;
                }
                union.add(email1);
                union.add(email2);
            } else if (_sub.equalsIgnoreCase(email2)) {
                union.add(email2);
            } else {
                union.add(email1);
                union.add(email2);
            }
        } else if (email1.startsWith(".")) {
            if (email2.indexOf(64) != -1) {
                if (withinDomain(email2.substring(email1.indexOf(64) + 1), email1)) {
                    union.add(email1);
                    return;
                }
                union.add(email1);
                union.add(email2);
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equalsIgnoreCase(email2)) {
                    union.add(email2);
                } else if (withinDomain(email2, email1)) {
                    union.add(email1);
                } else {
                    union.add(email1);
                    union.add(email2);
                }
            } else if (withinDomain(email2, email1)) {
                union.add(email1);
            } else {
                union.add(email1);
                union.add(email2);
            }
        } else if (email2.indexOf(64) != -1) {
            if (email2.substring(email1.indexOf(64) + 1).equalsIgnoreCase(email1)) {
                union.add(email1);
                return;
            }
            union.add(email1);
            union.add(email2);
        } else if (email2.startsWith(".")) {
            if (withinDomain(email1, email2)) {
                union.add(email2);
                return;
            }
            union.add(email1);
            union.add(email2);
        } else if (email1.equalsIgnoreCase(email2)) {
            union.add(email1);
        } else {
            union.add(email1);
            union.add(email2);
        }
    }

    private void unionURI(String email1, String email2, Set union) {
        if (email1.indexOf(64) != -1) {
            String _sub = email1.substring(email1.indexOf(64) + 1);
            if (email2.indexOf(64) != -1) {
                if (email1.equalsIgnoreCase(email2)) {
                    union.add(email1);
                    return;
                }
                union.add(email1);
                union.add(email2);
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    union.add(email2);
                    return;
                }
                union.add(email1);
                union.add(email2);
            } else if (_sub.equalsIgnoreCase(email2)) {
                union.add(email2);
            } else {
                union.add(email1);
                union.add(email2);
            }
        } else if (email1.startsWith(".")) {
            if (email2.indexOf(64) != -1) {
                if (withinDomain(email2.substring(email1.indexOf(64) + 1), email1)) {
                    union.add(email1);
                    return;
                }
                union.add(email1);
                union.add(email2);
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equalsIgnoreCase(email2)) {
                    union.add(email2);
                } else if (withinDomain(email2, email1)) {
                    union.add(email1);
                } else {
                    union.add(email1);
                    union.add(email2);
                }
            } else if (withinDomain(email2, email1)) {
                union.add(email1);
            } else {
                union.add(email1);
                union.add(email2);
            }
        } else if (email2.indexOf(64) != -1) {
            if (email2.substring(email1.indexOf(64) + 1).equalsIgnoreCase(email1)) {
                union.add(email1);
                return;
            }
            union.add(email1);
            union.add(email2);
        } else if (email2.startsWith(".")) {
            if (withinDomain(email1, email2)) {
                union.add(email2);
                return;
            }
            union.add(email1);
            union.add(email2);
        } else if (email1.equalsIgnoreCase(email2)) {
            union.add(email1);
        } else {
            union.add(email1);
            union.add(email2);
        }
    }

    private Set intersectDNS(Set permitted, Set dnss) {
        Set intersect = new HashSet();
        for (GeneralSubtree base : dnss) {
            String dns = extractNameAsString(base.getBase());
            if (permitted != null) {
                for (String _permitted : permitted) {
                    if (withinDomain(_permitted, dns)) {
                        intersect.add(_permitted);
                    } else if (withinDomain(dns, _permitted)) {
                        intersect.add(dns);
                    }
                }
            } else if (dns != null) {
                intersect.add(dns);
            }
        }
        return intersect;
    }

    protected Set unionDNS(Set excluded, String dns) {
        if (!excluded.isEmpty()) {
            Set union = new HashSet();
            for (String _permitted : excluded) {
                if (withinDomain(_permitted, dns)) {
                    union.add(dns);
                } else if (withinDomain(dns, _permitted)) {
                    union.add(_permitted);
                } else {
                    union.add(_permitted);
                    union.add(dns);
                }
            }
            return union;
        } else if (dns == null) {
            return excluded;
        } else {
            excluded.add(dns);
            return excluded;
        }
    }

    private void intersectEmail(String email1, String email2, Set intersect) {
        if (email1.indexOf(64) != -1) {
            String _sub = email1.substring(email1.indexOf(64) + 1);
            if (email2.indexOf(64) != -1) {
                if (email1.equalsIgnoreCase(email2)) {
                    intersect.add(email1);
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    intersect.add(email1);
                }
            } else if (_sub.equalsIgnoreCase(email2)) {
                intersect.add(email1);
            }
        } else if (email1.startsWith(".")) {
            if (email2.indexOf(64) != -1) {
                if (withinDomain(email2.substring(email1.indexOf(64) + 1), email1)) {
                    intersect.add(email2);
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equalsIgnoreCase(email2)) {
                    intersect.add(email1);
                } else if (withinDomain(email2, email1)) {
                    intersect.add(email2);
                }
            } else if (withinDomain(email2, email1)) {
                intersect.add(email2);
            }
        } else if (email2.indexOf(64) != -1) {
            if (email2.substring(email2.indexOf(64) + 1).equalsIgnoreCase(email1)) {
                intersect.add(email2);
            }
        } else if (email2.startsWith(".")) {
            if (withinDomain(email1, email2)) {
                intersect.add(email1);
            }
        } else if (email1.equalsIgnoreCase(email2)) {
            intersect.add(email1);
        }
    }

    private void checkExcludedURI(Set excluded, String uri) throws PKIXNameConstraintValidatorException {
        if (!excluded.isEmpty()) {
            for (String str : excluded) {
                if (isUriConstrained(uri, str)) {
                    throw new PKIXNameConstraintValidatorException("URI is from an excluded subtree.");
                }
            }
        }
    }

    private Set intersectURI(Set permitted, Set uris) {
        Set intersect = new HashSet();
        for (GeneralSubtree base : uris) {
            String uri = extractNameAsString(base.getBase());
            if (permitted != null) {
                for (String _permitted : permitted) {
                    intersectURI(_permitted, uri, intersect);
                }
            } else if (uri != null) {
                intersect.add(uri);
            }
        }
        return intersect;
    }

    private Set unionURI(Set excluded, String uri) {
        if (!excluded.isEmpty()) {
            Set union = new HashSet();
            for (String _excluded : excluded) {
                unionURI(_excluded, uri, union);
            }
            return union;
        } else if (uri == null) {
            return excluded;
        } else {
            excluded.add(uri);
            return excluded;
        }
    }

    private void intersectURI(String email1, String email2, Set intersect) {
        if (email1.indexOf(64) != -1) {
            String _sub = email1.substring(email1.indexOf(64) + 1);
            if (email2.indexOf(64) != -1) {
                if (email1.equalsIgnoreCase(email2)) {
                    intersect.add(email1);
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(_sub, email2)) {
                    intersect.add(email1);
                }
            } else if (_sub.equalsIgnoreCase(email2)) {
                intersect.add(email1);
            }
        } else if (email1.startsWith(".")) {
            if (email2.indexOf(64) != -1) {
                if (withinDomain(email2.substring(email1.indexOf(64) + 1), email1)) {
                    intersect.add(email2);
                }
            } else if (email2.startsWith(".")) {
                if (withinDomain(email1, email2) || email1.equalsIgnoreCase(email2)) {
                    intersect.add(email1);
                } else if (withinDomain(email2, email1)) {
                    intersect.add(email2);
                }
            } else if (withinDomain(email2, email1)) {
                intersect.add(email2);
            }
        } else if (email2.indexOf(64) != -1) {
            if (email2.substring(email2.indexOf(64) + 1).equalsIgnoreCase(email1)) {
                intersect.add(email2);
            }
        } else if (email2.startsWith(".")) {
            if (withinDomain(email1, email2)) {
                intersect.add(email1);
            }
        } else if (email1.equalsIgnoreCase(email2)) {
            intersect.add(email1);
        }
    }

    private void checkPermittedURI(Set permitted, String uri) throws PKIXNameConstraintValidatorException {
        if (permitted != null) {
            for (String str : permitted) {
                if (isUriConstrained(uri, str)) {
                    return;
                }
            }
            if (uri.length() != 0 || permitted.size() != 0) {
                throw new PKIXNameConstraintValidatorException("URI is not from a permitted subtree.");
            }
        }
    }

    private boolean isUriConstrained(String uri, String constraint) {
        String host = extractHostFromURL(uri);
        if (constraint.startsWith(".")) {
            if (withinDomain(host, constraint)) {
                return true;
            }
        } else if (host.equalsIgnoreCase(constraint)) {
            return true;
        }
        return false;
    }

    private static String extractHostFromURL(String url) {
        String sub = url.substring(url.indexOf(58) + 1);
        if (sub.indexOf("//") != -1) {
            sub = sub.substring(sub.indexOf("//") + 2);
        }
        if (sub.lastIndexOf(58) != -1) {
            sub = sub.substring(0, sub.lastIndexOf(58));
        }
        sub = sub.substring(sub.indexOf(58) + 1);
        sub = sub.substring(sub.indexOf(64) + 1);
        if (sub.indexOf(47) != -1) {
            return sub.substring(0, sub.indexOf(47));
        }
        return sub;
    }

    public void checkPermitted(GeneralName name) throws PKIXNameConstraintValidatorException {
        switch (name.getTagNo()) {
            case 1:
                checkPermittedEmail(this.permittedSubtreesEmail, extractNameAsString(name));
                return;
            case 2:
                checkPermittedDNS(this.permittedSubtreesDNS, DERIA5String.getInstance(name.getName()).getString());
                return;
            case 4:
                checkPermittedDN(ASN1Sequence.getInstance(name.getName().toASN1Primitive()));
                return;
            case 6:
                checkPermittedURI(this.permittedSubtreesURI, DERIA5String.getInstance(name.getName()).getString());
                return;
            case 7:
                checkPermittedIP(this.permittedSubtreesIP, ASN1OctetString.getInstance(name.getName()).getOctets());
                return;
            default:
                return;
        }
    }

    public void checkExcluded(GeneralName name) throws PKIXNameConstraintValidatorException {
        switch (name.getTagNo()) {
            case 1:
                checkExcludedEmail(this.excludedSubtreesEmail, extractNameAsString(name));
                return;
            case 2:
                checkExcludedDNS(this.excludedSubtreesDNS, DERIA5String.getInstance(name.getName()).getString());
                return;
            case 4:
                checkExcludedDN(ASN1Sequence.getInstance(name.getName().toASN1Primitive()));
                return;
            case 6:
                checkExcludedURI(this.excludedSubtreesURI, DERIA5String.getInstance(name.getName()).getString());
                return;
            case 7:
                checkExcludedIP(this.excludedSubtreesIP, ASN1OctetString.getInstance(name.getName()).getOctets());
                return;
            default:
                return;
        }
    }

    public void intersectPermittedSubtree(GeneralSubtree permitted) {
        intersectPermittedSubtree(new GeneralSubtree[]{permitted});
    }

    public void intersectPermittedSubtree(GeneralSubtree[] permitted) {
        Map subtreesMap = new HashMap();
        for (int i = 0; i != permitted.length; i++) {
            GeneralSubtree subtree = permitted[i];
            Integer tagNo = Integers.valueOf(subtree.getBase().getTagNo());
            if (subtreesMap.get(tagNo) == null) {
                subtreesMap.put(tagNo, new HashSet());
            }
            ((Set) subtreesMap.get(tagNo)).add(subtree);
        }
        for (Entry entry : subtreesMap.entrySet()) {
            switch (((Integer) entry.getKey()).intValue()) {
                case 1:
                    this.permittedSubtreesEmail = intersectEmail(this.permittedSubtreesEmail, (Set) entry.getValue());
                    break;
                case 2:
                    this.permittedSubtreesDNS = intersectDNS(this.permittedSubtreesDNS, (Set) entry.getValue());
                    break;
                case 4:
                    this.permittedSubtreesDN = intersectDN(this.permittedSubtreesDN, (Set) entry.getValue());
                    break;
                case 6:
                    this.permittedSubtreesURI = intersectURI(this.permittedSubtreesURI, (Set) entry.getValue());
                    break;
                case 7:
                    this.permittedSubtreesIP = intersectIP(this.permittedSubtreesIP, (Set) entry.getValue());
                    break;
                default:
                    break;
            }
        }
    }

    private String extractNameAsString(GeneralName name) {
        return DERIA5String.getInstance(name.getName()).getString();
    }

    public void intersectEmptyPermittedSubtree(int nameType) {
        switch (nameType) {
            case 1:
                this.permittedSubtreesEmail = new HashSet();
                return;
            case 2:
                this.permittedSubtreesDNS = new HashSet();
                return;
            case 4:
                this.permittedSubtreesDN = new HashSet();
                return;
            case 6:
                this.permittedSubtreesURI = new HashSet();
                return;
            case 7:
                this.permittedSubtreesIP = new HashSet();
                return;
            default:
                return;
        }
    }

    public void addExcludedSubtree(GeneralSubtree subtree) {
        GeneralName base = subtree.getBase();
        switch (base.getTagNo()) {
            case 1:
                this.excludedSubtreesEmail = unionEmail(this.excludedSubtreesEmail, extractNameAsString(base));
                return;
            case 2:
                this.excludedSubtreesDNS = unionDNS(this.excludedSubtreesDNS, extractNameAsString(base));
                return;
            case 4:
                this.excludedSubtreesDN = unionDN(this.excludedSubtreesDN, (ASN1Sequence) base.getName().toASN1Primitive());
                return;
            case 6:
                this.excludedSubtreesURI = unionURI(this.excludedSubtreesURI, extractNameAsString(base));
                return;
            case 7:
                this.excludedSubtreesIP = unionIP(this.excludedSubtreesIP, ASN1OctetString.getInstance(base.getName()).getOctets());
                return;
            default:
                return;
        }
    }

    private static byte[] max(byte[] ip1, byte[] ip2) {
        for (int i = 0; i < ip1.length; i++) {
            if ((ip1[i] & 65535) > (ip2[i] & 65535)) {
                return ip1;
            }
        }
        return ip2;
    }

    private static byte[] min(byte[] ip1, byte[] ip2) {
        for (int i = 0; i < ip1.length; i++) {
            if ((ip1[i] & 65535) < (ip2[i] & 65535)) {
                return ip1;
            }
        }
        return ip2;
    }

    private static int compareTo(byte[] ip1, byte[] ip2) {
        if (Arrays.areEqual(ip1, ip2)) {
            return 0;
        }
        if (Arrays.areEqual(max(ip1, ip2), ip1)) {
            return 1;
        }
        return -1;
    }

    private static byte[] or(byte[] ip1, byte[] ip2) {
        byte[] temp = new byte[ip1.length];
        for (int i = 0; i < ip1.length; i++) {
            temp[i] = (byte) (ip1[i] | ip2[i]);
        }
        return temp;
    }

    public int hashCode() {
        return ((((((((hashCollection(this.excludedSubtreesDN) + hashCollection(this.excludedSubtreesDNS)) + hashCollection(this.excludedSubtreesEmail)) + hashCollection(this.excludedSubtreesIP)) + hashCollection(this.excludedSubtreesURI)) + hashCollection(this.permittedSubtreesDN)) + hashCollection(this.permittedSubtreesDNS)) + hashCollection(this.permittedSubtreesEmail)) + hashCollection(this.permittedSubtreesIP)) + hashCollection(this.permittedSubtreesURI);
    }

    private int hashCollection(Collection coll) {
        if (coll == null) {
            return 0;
        }
        int hash = 0;
        for (Object o : coll) {
            if (o instanceof byte[]) {
                hash += Arrays.hashCode((byte[]) o);
            } else {
                hash += o.hashCode();
            }
        }
        return hash;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof PKIXNameConstraintValidator)) {
            return false;
        }
        PKIXNameConstraintValidator constraintValidator = (PKIXNameConstraintValidator) o;
        if (collectionsAreEqual(constraintValidator.excludedSubtreesDN, this.excludedSubtreesDN) && collectionsAreEqual(constraintValidator.excludedSubtreesDNS, this.excludedSubtreesDNS) && collectionsAreEqual(constraintValidator.excludedSubtreesEmail, this.excludedSubtreesEmail) && collectionsAreEqual(constraintValidator.excludedSubtreesIP, this.excludedSubtreesIP) && collectionsAreEqual(constraintValidator.excludedSubtreesURI, this.excludedSubtreesURI) && collectionsAreEqual(constraintValidator.permittedSubtreesDN, this.permittedSubtreesDN) && collectionsAreEqual(constraintValidator.permittedSubtreesDNS, this.permittedSubtreesDNS) && collectionsAreEqual(constraintValidator.permittedSubtreesEmail, this.permittedSubtreesEmail) && collectionsAreEqual(constraintValidator.permittedSubtreesIP, this.permittedSubtreesIP)) {
            z = collectionsAreEqual(constraintValidator.permittedSubtreesURI, this.permittedSubtreesURI);
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean collectionsAreEqual(Collection coll1, Collection coll2) {
        if (coll1 == coll2) {
            return true;
        }
        if (coll1 == null || coll2 == null || coll1.size() != coll2.size()) {
            return false;
        }
        for (Object a : coll1) {
            boolean found = false;
            for (Object b : coll2) {
                if (equals(a, b)) {
                    found = true;
                    continue;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if ((o1 instanceof byte[]) && (o2 instanceof byte[])) {
            return Arrays.areEqual((byte[]) o1, (byte[]) o2);
        }
        return o1.equals(o2);
    }

    private String stringifyIP(byte[] ip) {
        int i;
        String temp = "";
        for (i = 0; i < ip.length / 2; i++) {
            temp = temp + Integer.toString(ip[i] & 255) + ".";
        }
        temp = temp.substring(0, temp.length() - 1) + "/";
        for (i = ip.length / 2; i < ip.length; i++) {
            temp = temp + Integer.toString(ip[i] & 255) + ".";
        }
        return temp.substring(0, temp.length() - 1);
    }

    private String stringifyIPCollection(Set ips) {
        String temp = "" + "[";
        for (byte[] stringifyIP : ips) {
            temp = temp + stringifyIP(stringifyIP) + ",";
        }
        if (temp.length() > 1) {
            temp = temp.substring(0, temp.length() - 1);
        }
        return temp + "]";
    }

    public String toString() {
        String temp = "" + "permitted:\n";
        if (this.permittedSubtreesDN != null) {
            temp = (temp + "DN:\n") + this.permittedSubtreesDN.toString() + "\n";
        }
        if (this.permittedSubtreesDNS != null) {
            temp = (temp + "DNS:\n") + this.permittedSubtreesDNS.toString() + "\n";
        }
        if (this.permittedSubtreesEmail != null) {
            temp = (temp + "Email:\n") + this.permittedSubtreesEmail.toString() + "\n";
        }
        if (this.permittedSubtreesURI != null) {
            temp = (temp + "URI:\n") + this.permittedSubtreesURI.toString() + "\n";
        }
        if (this.permittedSubtreesIP != null) {
            temp = (temp + "IP:\n") + stringifyIPCollection(this.permittedSubtreesIP) + "\n";
        }
        temp = temp + "excluded:\n";
        if (!this.excludedSubtreesDN.isEmpty()) {
            temp = (temp + "DN:\n") + this.excludedSubtreesDN.toString() + "\n";
        }
        if (!this.excludedSubtreesDNS.isEmpty()) {
            temp = (temp + "DNS:\n") + this.excludedSubtreesDNS.toString() + "\n";
        }
        if (!this.excludedSubtreesEmail.isEmpty()) {
            temp = (temp + "Email:\n") + this.excludedSubtreesEmail.toString() + "\n";
        }
        if (!this.excludedSubtreesURI.isEmpty()) {
            temp = (temp + "URI:\n") + this.excludedSubtreesURI.toString() + "\n";
        }
        if (this.excludedSubtreesIP.isEmpty()) {
            return temp;
        }
        return (temp + "IP:\n") + stringifyIPCollection(this.excludedSubtreesIP) + "\n";
    }
}
