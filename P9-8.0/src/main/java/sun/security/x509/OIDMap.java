package sun.security.x509;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import sun.security.util.ObjectIdentifier;

public class OIDMap {
    private static final String AUTH_INFO_ACCESS = "x509.info.extensions.AuthorityInfoAccess";
    private static final String AUTH_KEY_IDENTIFIER = "x509.info.extensions.AuthorityKeyIdentifier";
    private static final String BASIC_CONSTRAINTS = "x509.info.extensions.BasicConstraints";
    private static final String CERT_ISSUER = "x509.info.extensions.CertificateIssuer";
    private static final String CERT_POLICIES = "x509.info.extensions.CertificatePolicies";
    private static final String CRL_DIST_POINTS = "x509.info.extensions.CRLDistributionPoints";
    private static final String CRL_NUMBER = "x509.info.extensions.CRLNumber";
    private static final String CRL_REASON = "x509.info.extensions.CRLReasonCode";
    private static final String DELTA_CRL_INDICATOR = "x509.info.extensions.DeltaCRLIndicator";
    private static final String EXT_KEY_USAGE = "x509.info.extensions.ExtendedKeyUsage";
    private static final String FRESHEST_CRL = "x509.info.extensions.FreshestCRL";
    private static final String INHIBIT_ANY_POLICY = "x509.info.extensions.InhibitAnyPolicy";
    private static final String ISSUER_ALT_NAME = "x509.info.extensions.IssuerAlternativeName";
    private static final String ISSUING_DIST_POINT = "x509.info.extensions.IssuingDistributionPoint";
    private static final String KEY_USAGE = "x509.info.extensions.KeyUsage";
    private static final String NAME_CONSTRAINTS = "x509.info.extensions.NameConstraints";
    private static final String NETSCAPE_CERT = "x509.info.extensions.NetscapeCertType";
    private static final int[] NetscapeCertType_data = new int[]{2, 16, 840, 1, 113730, 1, 1};
    private static final String OCSPNOCHECK = "x509.info.extensions.OCSPNoCheck";
    private static final String POLICY_CONSTRAINTS = "x509.info.extensions.PolicyConstraints";
    private static final String POLICY_MAPPINGS = "x509.info.extensions.PolicyMappings";
    private static final String PRIVATE_KEY_USAGE = "x509.info.extensions.PrivateKeyUsage";
    private static final String ROOT = "x509.info.extensions";
    private static final String SUBJECT_INFO_ACCESS = "x509.info.extensions.SubjectInfoAccess";
    private static final String SUB_ALT_NAME = "x509.info.extensions.SubjectAlternativeName";
    private static final String SUB_KEY_IDENTIFIER = "x509.info.extensions.SubjectKeyIdentifier";
    private static final Map<String, OIDInfo> nameMap = new HashMap();
    private static final Map<ObjectIdentifier, OIDInfo> oidMap = new HashMap();

    private static class OIDInfo {
        private volatile Class<?> clazz;
        final String name;
        final ObjectIdentifier oid;

        OIDInfo(String name, ObjectIdentifier oid, Class<?> clazz) {
            this.name = name;
            this.oid = oid;
            this.clazz = clazz;
        }

        Class<?> getClazz() throws CertificateException {
            return this.clazz;
        }
    }

    private OIDMap() {
    }

    static {
        addInternal("x509.info.extensions.SubjectKeyIdentifier", PKIXExtensions.SubjectKey_Id, SubjectKeyIdentifierExtension.class);
        addInternal("x509.info.extensions.KeyUsage", PKIXExtensions.KeyUsage_Id, KeyUsageExtension.class);
        addInternal("x509.info.extensions.PrivateKeyUsage", PKIXExtensions.PrivateKeyUsage_Id, PrivateKeyUsageExtension.class);
        addInternal("x509.info.extensions.SubjectAlternativeName", PKIXExtensions.SubjectAlternativeName_Id, SubjectAlternativeNameExtension.class);
        addInternal("x509.info.extensions.IssuerAlternativeName", PKIXExtensions.IssuerAlternativeName_Id, IssuerAlternativeNameExtension.class);
        addInternal("x509.info.extensions.BasicConstraints", PKIXExtensions.BasicConstraints_Id, BasicConstraintsExtension.class);
        addInternal(CRL_NUMBER, PKIXExtensions.CRLNumber_Id, CRLNumberExtension.class);
        addInternal(CRL_REASON, PKIXExtensions.ReasonCode_Id, CRLReasonCodeExtension.class);
        addInternal("x509.info.extensions.NameConstraints", PKIXExtensions.NameConstraints_Id, NameConstraintsExtension.class);
        addInternal("x509.info.extensions.PolicyMappings", PKIXExtensions.PolicyMappings_Id, PolicyMappingsExtension.class);
        addInternal("x509.info.extensions.AuthorityKeyIdentifier", PKIXExtensions.AuthorityKey_Id, AuthorityKeyIdentifierExtension.class);
        addInternal("x509.info.extensions.PolicyConstraints", PKIXExtensions.PolicyConstraints_Id, PolicyConstraintsExtension.class);
        addInternal("x509.info.extensions.NetscapeCertType", ObjectIdentifier.newInternal(new int[]{2, 16, 840, 1, 113730, 1, 1}), NetscapeCertTypeExtension.class);
        addInternal("x509.info.extensions.CertificatePolicies", PKIXExtensions.CertificatePolicies_Id, CertificatePoliciesExtension.class);
        addInternal("x509.info.extensions.ExtendedKeyUsage", PKIXExtensions.ExtendedKeyUsage_Id, ExtendedKeyUsageExtension.class);
        addInternal("x509.info.extensions.InhibitAnyPolicy", PKIXExtensions.InhibitAnyPolicy_Id, InhibitAnyPolicyExtension.class);
        addInternal("x509.info.extensions.CRLDistributionPoints", PKIXExtensions.CRLDistributionPoints_Id, CRLDistributionPointsExtension.class);
        addInternal(CERT_ISSUER, PKIXExtensions.CertificateIssuer_Id, CertificateIssuerExtension.class);
        addInternal("x509.info.extensions.SubjectInfoAccess", PKIXExtensions.SubjectInfoAccess_Id, SubjectInfoAccessExtension.class);
        addInternal("x509.info.extensions.AuthorityInfoAccess", PKIXExtensions.AuthInfoAccess_Id, AuthorityInfoAccessExtension.class);
        addInternal("x509.info.extensions.IssuingDistributionPoint", PKIXExtensions.IssuingDistributionPoint_Id, IssuingDistributionPointExtension.class);
        addInternal(DELTA_CRL_INDICATOR, PKIXExtensions.DeltaCRLIndicator_Id, DeltaCRLIndicatorExtension.class);
        addInternal(FRESHEST_CRL, PKIXExtensions.FreshestCRL_Id, FreshestCRLExtension.class);
        addInternal("x509.info.extensions.OCSPNoCheck", PKIXExtensions.OCSPNoCheck_Id, OCSPNoCheckExtension.class);
    }

    private static void addInternal(String name, ObjectIdentifier oid, Class clazz) {
        OIDInfo info = new OIDInfo(name, oid, clazz);
        oidMap.put(oid, info);
        nameMap.put(name, info);
    }

    public static void addAttribute(String name, String oid, Class<?> clazz) throws CertificateException {
        try {
            ObjectIdentifier objId = new ObjectIdentifier(oid);
            OIDInfo info = new OIDInfo(name, objId, clazz);
            if (oidMap.put(objId, info) != null) {
                throw new CertificateException("Object identifier already exists: " + oid);
            } else if (nameMap.put(name, info) != null) {
                throw new CertificateException("Name already exists: " + name);
            }
        } catch (IOException e) {
            throw new CertificateException("Invalid Object identifier: " + oid);
        }
    }

    public static String getName(ObjectIdentifier oid) {
        OIDInfo info = (OIDInfo) oidMap.get(oid);
        if (info == null) {
            return null;
        }
        return info.name;
    }

    public static ObjectIdentifier getOID(String name) {
        OIDInfo info = (OIDInfo) nameMap.get(name);
        if (info == null) {
            return null;
        }
        return info.oid;
    }

    public static Class<?> getClass(String name) throws CertificateException {
        OIDInfo info = (OIDInfo) nameMap.get(name);
        if (info == null) {
            return null;
        }
        return info.getClazz();
    }

    public static Class<?> getClass(ObjectIdentifier oid) throws CertificateException {
        OIDInfo info = (OIDInfo) oidMap.get(oid);
        if (info == null) {
            return null;
        }
        return info.getClazz();
    }
}
