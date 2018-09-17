package java.security.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.security.auth.x500.X500Principal;
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificatePoliciesExtension;
import sun.security.x509.CertificatePolicyId;
import sun.security.x509.CertificatePolicySet;
import sun.security.x509.DNSName;
import sun.security.x509.EDIPartyName;
import sun.security.x509.ExtendedKeyUsageExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.GeneralNames;
import sun.security.x509.GeneralSubtree;
import sun.security.x509.GeneralSubtrees;
import sun.security.x509.IPAddressName;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.OIDName;
import sun.security.x509.OtherName;
import sun.security.x509.PolicyInformation;
import sun.security.x509.PrivateKeyUsageExtension;
import sun.security.x509.RFC822Name;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.URIName;
import sun.security.x509.X400Address;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509Key;

public class X509CertSelector implements CertSelector {
    private static final ObjectIdentifier ANY_EXTENDED_KEY_USAGE = null;
    private static final int CERT_POLICIES_ID = 3;
    private static final int EXTENDED_KEY_USAGE_ID = 4;
    private static final String[] EXTENSION_OIDS = null;
    private static final Boolean FALSE = null;
    static final int NAME_ANY = 0;
    private static final int NAME_CONSTRAINTS_ID = 2;
    static final int NAME_DIRECTORY = 4;
    static final int NAME_DNS = 2;
    static final int NAME_EDI = 5;
    static final int NAME_IP = 7;
    static final int NAME_OID = 8;
    static final int NAME_RFC822 = 1;
    static final int NAME_URI = 6;
    static final int NAME_X400 = 3;
    private static final int NUM_OF_EXTENSIONS = 5;
    private static final int PRIVATE_KEY_USAGE_ID = 0;
    private static final int SUBJECT_ALT_NAME_ID = 1;
    private static final Debug debug = null;
    private byte[] authorityKeyID;
    private int basicConstraints;
    private Date certificateValid;
    private X500Principal issuer;
    private Set<ObjectIdentifier> keyPurposeOIDSet;
    private Set<String> keyPurposeSet;
    private boolean[] keyUsage;
    private boolean matchAllSubjectAltNames;
    private NameConstraintsExtension nc;
    private byte[] ncBytes;
    private Set<GeneralNameInterface> pathToGeneralNames;
    private Set<List<?>> pathToNames;
    private CertificatePolicySet policy;
    private Set<String> policySet;
    private Date privateKeyValid;
    private BigInteger serialNumber;
    private X500Principal subject;
    private Set<GeneralNameInterface> subjectAlternativeGeneralNames;
    private Set<List<?>> subjectAlternativeNames;
    private byte[] subjectKeyID;
    private PublicKey subjectPublicKey;
    private ObjectIdentifier subjectPublicKeyAlgID;
    private byte[] subjectPublicKeyBytes;
    private X509Certificate x509Cert;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.cert.X509CertSelector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.cert.X509CertSelector.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.security.cert.X509CertSelector.<clinit>():void");
    }

    public X509CertSelector() {
        this.basicConstraints = -1;
        this.matchAllSubjectAltNames = true;
    }

    public void setCertificate(X509Certificate cert) {
        this.x509Cert = cert;
    }

    public void setSerialNumber(BigInteger serial) {
        this.serialNumber = serial;
    }

    public void setIssuer(X500Principal issuer) {
        this.issuer = issuer;
    }

    public void setIssuer(String issuerDN) throws IOException {
        if (issuerDN == null) {
            this.issuer = null;
        } else {
            this.issuer = new X500Name(issuerDN).asX500Principal();
        }
    }

    public void setIssuer(byte[] issuerDN) throws IOException {
        X500Principal x500Principal = null;
        if (issuerDN != null) {
            x500Principal = new X500Principal(issuerDN);
        }
        try {
            this.issuer = x500Principal;
        } catch (IllegalArgumentException e) {
            throw ((IOException) new IOException("Invalid name").initCause(e));
        }
    }

    public void setSubject(X500Principal subject) {
        this.subject = subject;
    }

    public void setSubject(String subjectDN) throws IOException {
        if (subjectDN == null) {
            this.subject = null;
        } else {
            this.subject = new X500Name(subjectDN).asX500Principal();
        }
    }

    public void setSubject(byte[] subjectDN) throws IOException {
        X500Principal x500Principal = null;
        if (subjectDN != null) {
            x500Principal = new X500Principal(subjectDN);
        }
        try {
            this.subject = x500Principal;
        } catch (IllegalArgumentException e) {
            throw ((IOException) new IOException("Invalid name").initCause(e));
        }
    }

    public void setSubjectKeyIdentifier(byte[] subjectKeyID) {
        if (subjectKeyID == null) {
            this.subjectKeyID = null;
        } else {
            this.subjectKeyID = (byte[]) subjectKeyID.clone();
        }
    }

    public void setAuthorityKeyIdentifier(byte[] authorityKeyID) {
        if (authorityKeyID == null) {
            this.authorityKeyID = null;
        } else {
            this.authorityKeyID = (byte[]) authorityKeyID.clone();
        }
    }

    public void setCertificateValid(Date certValid) {
        if (certValid == null) {
            this.certificateValid = null;
        } else {
            this.certificateValid = (Date) certValid.clone();
        }
    }

    public void setPrivateKeyValid(Date privateKeyValid) {
        if (privateKeyValid == null) {
            this.privateKeyValid = null;
        } else {
            this.privateKeyValid = (Date) privateKeyValid.clone();
        }
    }

    public void setSubjectPublicKeyAlgID(String oid) throws IOException {
        if (oid == null) {
            this.subjectPublicKeyAlgID = null;
        } else {
            this.subjectPublicKeyAlgID = new ObjectIdentifier(oid);
        }
    }

    public void setSubjectPublicKey(PublicKey key) {
        if (key == null) {
            this.subjectPublicKey = null;
            this.subjectPublicKeyBytes = null;
            return;
        }
        this.subjectPublicKey = key;
        this.subjectPublicKeyBytes = key.getEncoded();
    }

    public void setSubjectPublicKey(byte[] key) throws IOException {
        if (key == null) {
            this.subjectPublicKey = null;
            this.subjectPublicKeyBytes = null;
            return;
        }
        this.subjectPublicKeyBytes = (byte[]) key.clone();
        this.subjectPublicKey = X509Key.parse(new DerValue(this.subjectPublicKeyBytes));
    }

    public void setKeyUsage(boolean[] keyUsage) {
        if (keyUsage == null) {
            this.keyUsage = null;
        } else {
            this.keyUsage = (boolean[]) keyUsage.clone();
        }
    }

    public void setExtendedKeyUsage(Set<String> keyPurposeSet) throws IOException {
        if (keyPurposeSet == null || keyPurposeSet.isEmpty()) {
            this.keyPurposeSet = null;
            this.keyPurposeOIDSet = null;
            return;
        }
        this.keyPurposeSet = Collections.unmodifiableSet(new HashSet((Collection) keyPurposeSet));
        this.keyPurposeOIDSet = new HashSet();
        for (String s : this.keyPurposeSet) {
            this.keyPurposeOIDSet.add(new ObjectIdentifier(s));
        }
    }

    public void setMatchAllSubjectAltNames(boolean matchAllNames) {
        this.matchAllSubjectAltNames = matchAllNames;
    }

    public void setSubjectAlternativeNames(Collection<List<?>> names) throws IOException {
        if (names == null) {
            this.subjectAlternativeNames = null;
            this.subjectAlternativeGeneralNames = null;
        } else if (names.isEmpty()) {
            this.subjectAlternativeNames = null;
            this.subjectAlternativeGeneralNames = null;
        } else {
            Set<List<?>> tempNames = cloneAndCheckNames(names);
            this.subjectAlternativeGeneralNames = parseNames(tempNames);
            this.subjectAlternativeNames = tempNames;
        }
    }

    public void addSubjectAlternativeName(int type, String name) throws IOException {
        addSubjectAlternativeNameInternal(type, name);
    }

    public void addSubjectAlternativeName(int type, byte[] name) throws IOException {
        addSubjectAlternativeNameInternal(type, name.clone());
    }

    private void addSubjectAlternativeNameInternal(int type, Object name) throws IOException {
        GeneralNameInterface tempName = makeGeneralNameInterface(type, name);
        if (this.subjectAlternativeNames == null) {
            this.subjectAlternativeNames = new HashSet();
        }
        if (this.subjectAlternativeGeneralNames == null) {
            this.subjectAlternativeGeneralNames = new HashSet();
        }
        List<Object> list = new ArrayList((int) NAME_DNS);
        list.add(Integer.valueOf(type));
        list.add(name);
        this.subjectAlternativeNames.add(list);
        this.subjectAlternativeGeneralNames.add(tempName);
    }

    private static Set<GeneralNameInterface> parseNames(Collection<List<?>> names) throws IOException {
        Set<GeneralNameInterface> genNames = new HashSet();
        for (List<?> nameList : names) {
            if (nameList.size() != NAME_DNS) {
                throw new IOException("name list size not 2");
            }
            Object o = nameList.get(PRIVATE_KEY_USAGE_ID);
            if (o instanceof Integer) {
                genNames.add(makeGeneralNameInterface(((Integer) o).intValue(), nameList.get(SUBJECT_ALT_NAME_ID)));
            } else {
                throw new IOException("expected an Integer");
            }
        }
        return genNames;
    }

    static boolean equalNames(Collection object1, Collection object2) {
        if (object1 != null && object2 != null) {
            return object1.equals(object2);
        }
        return object1 == object2;
    }

    static GeneralNameInterface makeGeneralNameInterface(int type, Object name) throws IOException {
        GeneralNameInterface result;
        if (debug != null) {
            debug.println("X509CertSelector.makeGeneralNameInterface(" + type + ")...");
        }
        if (name instanceof String) {
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() name is String: " + name);
            }
            switch (type) {
                case SUBJECT_ALT_NAME_ID /*1*/:
                    result = new RFC822Name((String) name);
                    break;
                case NAME_DNS /*2*/:
                    result = new DNSName((String) name);
                    break;
                case NAME_DIRECTORY /*4*/:
                    result = new X500Name((String) name);
                    break;
                case NAME_URI /*6*/:
                    result = new URIName((String) name);
                    break;
                case NAME_IP /*7*/:
                    result = new IPAddressName((String) name);
                    break;
                case NAME_OID /*8*/:
                    result = new OIDName((String) name);
                    break;
                default:
                    throw new IOException("unable to parse String names of type " + type);
            }
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() result: " + result.toString());
            }
        } else if (name instanceof byte[]) {
            DerValue val = new DerValue((byte[]) name);
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() is byte[]");
            }
            switch (type) {
                case PRIVATE_KEY_USAGE_ID /*0*/:
                    result = new OtherName(val);
                    break;
                case SUBJECT_ALT_NAME_ID /*1*/:
                    result = new RFC822Name(val);
                    break;
                case NAME_DNS /*2*/:
                    result = new DNSName(val);
                    break;
                case NAME_X400 /*3*/:
                    result = new X400Address(val);
                    break;
                case NAME_DIRECTORY /*4*/:
                    result = new X500Name(val);
                    break;
                case NUM_OF_EXTENSIONS /*5*/:
                    result = new EDIPartyName(val);
                    break;
                case NAME_URI /*6*/:
                    result = new URIName(val);
                    break;
                case NAME_IP /*7*/:
                    result = new IPAddressName(val);
                    break;
                case NAME_OID /*8*/:
                    result = new OIDName(val);
                    break;
                default:
                    throw new IOException("unable to parse byte array names of type " + type);
            }
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralNameInterface() result: " + result.toString());
            }
        } else {
            if (debug != null) {
                debug.println("X509CertSelector.makeGeneralName() input name not String or byte array");
            }
            throw new IOException("name not String or byte array");
        }
        return result;
    }

    public void setNameConstraints(byte[] bytes) throws IOException {
        if (bytes == null) {
            this.ncBytes = null;
            this.nc = null;
            return;
        }
        this.ncBytes = (byte[]) bytes.clone();
        this.nc = new NameConstraintsExtension(FALSE, (Object) bytes);
    }

    public void setBasicConstraints(int minMaxPathLen) {
        if (minMaxPathLen < -2) {
            throw new IllegalArgumentException("basic constraints less than -2");
        }
        this.basicConstraints = minMaxPathLen;
    }

    public void setPolicy(Set<String> certPolicySet) throws IOException {
        if (certPolicySet == null) {
            this.policySet = null;
            this.policy = null;
            return;
        }
        Set<String> tempSet = Collections.unmodifiableSet(new HashSet((Collection) certPolicySet));
        Vector polIdVector = new Vector();
        for (Object o : tempSet) {
            if (o instanceof String) {
                polIdVector.add(new CertificatePolicyId(new ObjectIdentifier((String) o)));
            } else {
                throw new IOException("non String in certPolicySet");
            }
        }
        this.policySet = tempSet;
        this.policy = new CertificatePolicySet(polIdVector);
    }

    public void setPathToNames(Collection<List<?>> names) throws IOException {
        if (names == null || names.isEmpty()) {
            this.pathToNames = null;
            this.pathToGeneralNames = null;
            return;
        }
        Set<List<?>> tempNames = cloneAndCheckNames(names);
        this.pathToGeneralNames = parseNames(tempNames);
        this.pathToNames = tempNames;
    }

    void setPathToNamesInternal(Set<GeneralNameInterface> names) {
        this.pathToNames = Collections.emptySet();
        this.pathToGeneralNames = names;
    }

    public void addPathToName(int type, String name) throws IOException {
        addPathToNameInternal(type, name);
    }

    public void addPathToName(int type, byte[] name) throws IOException {
        addPathToNameInternal(type, name.clone());
    }

    private void addPathToNameInternal(int type, Object name) throws IOException {
        GeneralNameInterface tempName = makeGeneralNameInterface(type, name);
        if (this.pathToGeneralNames == null) {
            this.pathToNames = new HashSet();
            this.pathToGeneralNames = new HashSet();
        }
        List<Object> list = new ArrayList((int) NAME_DNS);
        list.add(Integer.valueOf(type));
        list.add(name);
        this.pathToNames.add(list);
        this.pathToGeneralNames.add(tempName);
    }

    public X509Certificate getCertificate() {
        return this.x509Cert;
    }

    public BigInteger getSerialNumber() {
        return this.serialNumber;
    }

    public X500Principal getIssuer() {
        return this.issuer;
    }

    public String getIssuerAsString() {
        return this.issuer == null ? null : this.issuer.getName();
    }

    public byte[] getIssuerAsBytes() throws IOException {
        return this.issuer == null ? null : this.issuer.getEncoded();
    }

    public X500Principal getSubject() {
        return this.subject;
    }

    public String getSubjectAsString() {
        return this.subject == null ? null : this.subject.getName();
    }

    public byte[] getSubjectAsBytes() throws IOException {
        return this.subject == null ? null : this.subject.getEncoded();
    }

    public byte[] getSubjectKeyIdentifier() {
        if (this.subjectKeyID == null) {
            return null;
        }
        return (byte[]) this.subjectKeyID.clone();
    }

    public byte[] getAuthorityKeyIdentifier() {
        if (this.authorityKeyID == null) {
            return null;
        }
        return (byte[]) this.authorityKeyID.clone();
    }

    public Date getCertificateValid() {
        if (this.certificateValid == null) {
            return null;
        }
        return (Date) this.certificateValid.clone();
    }

    public Date getPrivateKeyValid() {
        if (this.privateKeyValid == null) {
            return null;
        }
        return (Date) this.privateKeyValid.clone();
    }

    public String getSubjectPublicKeyAlgID() {
        if (this.subjectPublicKeyAlgID == null) {
            return null;
        }
        return this.subjectPublicKeyAlgID.toString();
    }

    public PublicKey getSubjectPublicKey() {
        return this.subjectPublicKey;
    }

    public boolean[] getKeyUsage() {
        if (this.keyUsage == null) {
            return null;
        }
        return (boolean[]) this.keyUsage.clone();
    }

    public Set<String> getExtendedKeyUsage() {
        return this.keyPurposeSet;
    }

    public boolean getMatchAllSubjectAltNames() {
        return this.matchAllSubjectAltNames;
    }

    public Collection<List<?>> getSubjectAlternativeNames() {
        if (this.subjectAlternativeNames == null) {
            return null;
        }
        return cloneNames(this.subjectAlternativeNames);
    }

    private static Set<List<?>> cloneNames(Collection<List<?>> names) {
        try {
            return cloneAndCheckNames(names);
        } catch (IOException e) {
            throw new RuntimeException("cloneNames encountered IOException: " + e.getMessage());
        }
    }

    private static Set<List<?>> cloneAndCheckNames(Collection<List<?>> names) throws IOException {
        Set<List<?>> namesCopy = new HashSet();
        for (Object o : names) {
            Object o2;
            if (o2 instanceof List) {
                namesCopy.add(new ArrayList((List) o2));
            } else {
                throw new IOException("expected a List");
            }
        }
        for (List<Object> nameList : namesCopy) {
            if (nameList.size() != NAME_DNS) {
                throw new IOException("name list size not 2");
            }
            o2 = nameList.get(PRIVATE_KEY_USAGE_ID);
            if (o2 instanceof Integer) {
                int nameType = ((Integer) o2).intValue();
                if (nameType < 0 || nameType > NAME_OID) {
                    throw new IOException("name type not 0-8");
                }
                Object nameObject = nameList.get(SUBJECT_ALT_NAME_ID);
                if (!(nameObject instanceof byte[]) && !(nameObject instanceof String)) {
                    if (debug != null) {
                        debug.println("X509CertSelector.cloneAndCheckNames() name not byte array");
                    }
                    throw new IOException("name not byte array or String");
                } else if (nameObject instanceof byte[]) {
                    nameList.set(SUBJECT_ALT_NAME_ID, ((byte[]) nameObject).clone());
                }
            } else {
                throw new IOException("expected an Integer");
            }
        }
        return namesCopy;
    }

    public byte[] getNameConstraints() {
        if (this.ncBytes == null) {
            return null;
        }
        return (byte[]) this.ncBytes.clone();
    }

    public int getBasicConstraints() {
        return this.basicConstraints;
    }

    public Set<String> getPolicy() {
        return this.policySet;
    }

    public Collection<List<?>> getPathToNames() {
        if (this.pathToNames == null) {
            return null;
        }
        return cloneNames(this.pathToNames);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("X509CertSelector: [\n");
        if (this.x509Cert != null) {
            sb.append("  Certificate: " + this.x509Cert.toString() + "\n");
        }
        if (this.serialNumber != null) {
            sb.append("  Serial Number: " + this.serialNumber.toString() + "\n");
        }
        if (this.issuer != null) {
            sb.append("  Issuer: " + getIssuerAsString() + "\n");
        }
        if (this.subject != null) {
            sb.append("  Subject: " + getSubjectAsString() + "\n");
        }
        sb.append("  matchAllSubjectAltNames flag: " + String.valueOf(this.matchAllSubjectAltNames) + "\n");
        if (this.subjectAlternativeNames != null) {
            sb.append("  SubjectAlternativeNames:\n");
            for (List<?> list : this.subjectAlternativeNames) {
                sb.append("    type " + list.get(PRIVATE_KEY_USAGE_ID) + ", name " + list.get(SUBJECT_ALT_NAME_ID) + "\n");
            }
        }
        if (this.subjectKeyID != null) {
            sb.append("  Subject Key Identifier: " + new HexDumpEncoder().encodeBuffer(this.subjectKeyID) + "\n");
        }
        if (this.authorityKeyID != null) {
            sb.append("  Authority Key Identifier: " + new HexDumpEncoder().encodeBuffer(this.authorityKeyID) + "\n");
        }
        if (this.certificateValid != null) {
            sb.append("  Certificate Valid: " + this.certificateValid.toString() + "\n");
        }
        if (this.privateKeyValid != null) {
            sb.append("  Private Key Valid: " + this.privateKeyValid.toString() + "\n");
        }
        if (this.subjectPublicKeyAlgID != null) {
            sb.append("  Subject Public Key AlgID: " + this.subjectPublicKeyAlgID.toString() + "\n");
        }
        if (this.subjectPublicKey != null) {
            sb.append("  Subject Public Key: " + this.subjectPublicKey.toString() + "\n");
        }
        if (this.keyUsage != null) {
            sb.append("  Key Usage: " + keyUsageToString(this.keyUsage) + "\n");
        }
        if (this.keyPurposeSet != null) {
            sb.append("  Extended Key Usage: " + this.keyPurposeSet.toString() + "\n");
        }
        if (this.policy != null) {
            sb.append("  Policy: " + this.policy.toString() + "\n");
        }
        if (this.pathToGeneralNames != null) {
            sb.append("  Path to names:\n");
            for (Object obj : this.pathToGeneralNames) {
                sb.append("    " + obj + "\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String keyUsageToString(boolean[] k) {
        String s = "KeyUsage [\n";
        try {
            if (k[PRIVATE_KEY_USAGE_ID]) {
                s = s + "  DigitalSignature\n";
            }
            if (k[SUBJECT_ALT_NAME_ID]) {
                s = s + "  Non_repudiation\n";
            }
            if (k[NAME_DNS]) {
                s = s + "  Key_Encipherment\n";
            }
            if (k[NAME_X400]) {
                s = s + "  Data_Encipherment\n";
            }
            if (k[NAME_DIRECTORY]) {
                s = s + "  Key_Agreement\n";
            }
            if (k[NUM_OF_EXTENSIONS]) {
                s = s + "  Key_CertSign\n";
            }
            if (k[NAME_URI]) {
                s = s + "  Crl_Sign\n";
            }
            if (k[NAME_IP]) {
                s = s + "  Encipher_Only\n";
            }
            if (k[NAME_OID]) {
                s = s + "  Decipher_Only\n";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return s + "]\n";
    }

    private static Extension getExtensionObject(X509Certificate cert, int extId) throws IOException {
        if (cert instanceof X509CertImpl) {
            X509CertImpl impl = (X509CertImpl) cert;
            switch (extId) {
                case PRIVATE_KEY_USAGE_ID /*0*/:
                    return impl.getPrivateKeyUsageExtension();
                case SUBJECT_ALT_NAME_ID /*1*/:
                    return impl.getSubjectAlternativeNameExtension();
                case NAME_DNS /*2*/:
                    return impl.getNameConstraintsExtension();
                case NAME_X400 /*3*/:
                    return impl.getCertificatePoliciesExtension();
                case NAME_DIRECTORY /*4*/:
                    return impl.getExtendedKeyUsageExtension();
                default:
                    return null;
            }
        }
        byte[] rawExtVal = cert.getExtensionValue(EXTENSION_OIDS[extId]);
        if (rawExtVal == null) {
            return null;
        }
        Object encoded = new DerInputStream(rawExtVal).getOctetString();
        switch (extId) {
            case PRIVATE_KEY_USAGE_ID /*0*/:
                try {
                    return new PrivateKeyUsageExtension(FALSE, encoded);
                } catch (CertificateException ex) {
                    throw new IOException(ex.getMessage());
                }
            case SUBJECT_ALT_NAME_ID /*1*/:
                return new SubjectAlternativeNameExtension(FALSE, encoded);
            case NAME_DNS /*2*/:
                return new NameConstraintsExtension(FALSE, encoded);
            case NAME_X400 /*3*/:
                return new CertificatePoliciesExtension(FALSE, encoded);
            case NAME_DIRECTORY /*4*/:
                return new ExtendedKeyUsageExtension(FALSE, encoded);
            default:
                return null;
        }
    }

    public boolean match(Certificate cert) {
        boolean result = false;
        if (!(cert instanceof X509Certificate)) {
            return false;
        }
        X509Certificate xcert = (X509Certificate) cert;
        if (debug != null) {
            debug.println("X509CertSelector.match(SN: " + xcert.getSerialNumber().toString(16) + "\n  Issuer: " + xcert.getIssuerDN() + "\n  Subject: " + xcert.getSubjectDN() + ")");
        }
        if (this.x509Cert != null && !this.x509Cert.equals(xcert)) {
            if (debug != null) {
                debug.println("X509CertSelector.match: certs don't match");
            }
            return false;
        } else if (this.serialNumber != null && !this.serialNumber.equals(xcert.getSerialNumber())) {
            if (debug != null) {
                debug.println("X509CertSelector.match: serial numbers don't match");
            }
            return false;
        } else if (this.issuer != null && !this.issuer.equals(xcert.getIssuerX500Principal())) {
            if (debug != null) {
                debug.println("X509CertSelector.match: issuer DNs don't match");
            }
            return false;
        } else if (this.subject == null || this.subject.equals(xcert.getSubjectX500Principal())) {
            if (this.certificateValid != null) {
                try {
                    xcert.checkValidity(this.certificateValid);
                } catch (CertificateException e) {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: certificate not within validity period");
                    }
                    return false;
                }
            }
            if (this.subjectPublicKeyBytes != null) {
                if (!Arrays.equals(this.subjectPublicKeyBytes, xcert.getPublicKey().getEncoded())) {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: subject public keys don't match");
                    }
                    return false;
                }
            }
            if (matchBasicConstraints(xcert) && matchKeyUsage(xcert) && matchExtendedKeyUsage(xcert) && matchSubjectKeyID(xcert) && matchAuthorityKeyID(xcert) && matchPrivateKeyValid(xcert) && matchSubjectPublicKeyAlgID(xcert) && matchPolicy(xcert) && matchSubjectAlternativeNames(xcert) && matchPathToNames(xcert)) {
                result = matchNameConstraints(xcert);
            }
            if (result && debug != null) {
                debug.println("X509CertSelector.match returning: true");
            }
            return result;
        } else {
            if (debug != null) {
                debug.println("X509CertSelector.match: subject DNs don't match");
            }
            return false;
        }
    }

    private boolean matchSubjectKeyID(X509Certificate xcert) {
        if (this.subjectKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue("2.5.29.14");
            if (extVal == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no subject key ID extension");
                }
                return false;
            }
            byte[] certSubjectKeyID = new DerInputStream(extVal).getOctetString();
            if (certSubjectKeyID != null && Arrays.equals(this.subjectKeyID, certSubjectKeyID)) {
                return true;
            }
            if (debug != null) {
                debug.println("X509CertSelector.match: subject key IDs don't match");
            }
            return false;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: exception in subject key ID check");
            }
            return false;
        }
    }

    private boolean matchAuthorityKeyID(X509Certificate xcert) {
        if (this.authorityKeyID == null) {
            return true;
        }
        try {
            byte[] extVal = xcert.getExtensionValue("2.5.29.35");
            if (extVal == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no authority key ID extension");
                }
                return false;
            }
            byte[] certAuthKeyID = new DerInputStream(extVal).getOctetString();
            if (certAuthKeyID != null && Arrays.equals(this.authorityKeyID, certAuthKeyID)) {
                return true;
            }
            if (debug != null) {
                debug.println("X509CertSelector.match: authority key IDs don't match");
            }
            return false;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: exception in authority key ID check");
            }
            return false;
        }
    }

    private boolean matchPrivateKeyValid(X509Certificate xcert) {
        String time;
        if (this.privateKeyValid == null) {
            return true;
        }
        PrivateKeyUsageExtension privateKeyUsageExtension = null;
        try {
            privateKeyUsageExtension = (PrivateKeyUsageExtension) getExtensionObject(xcert, PRIVATE_KEY_USAGE_ID);
            if (privateKeyUsageExtension != null) {
                privateKeyUsageExtension.valid(this.privateKeyValid);
            }
            return true;
        } catch (CertificateExpiredException e1) {
            if (debug != null) {
                time = "n/a";
                try {
                    time = ((Date) privateKeyUsageExtension.get(PrivateKeyUsageExtension.NOT_AFTER)).toString();
                } catch (CertificateException e) {
                }
                debug.println("X509CertSelector.match: private key usage not within validity date; ext.NOT_After: " + time + "; X509CertSelector: " + toString());
                e1.printStackTrace();
            }
            return false;
        } catch (CertificateNotYetValidException e2) {
            if (debug != null) {
                time = "n/a";
                try {
                    time = ((Date) privateKeyUsageExtension.get(PrivateKeyUsageExtension.NOT_BEFORE)).toString();
                } catch (CertificateException e3) {
                }
                debug.println("X509CertSelector.match: private key usage not within validity date; ext.NOT_BEFORE: " + time + "; X509CertSelector: " + toString());
                e2.printStackTrace();
            }
            return false;
        } catch (IOException e4) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in private key usage check; X509CertSelector: " + toString());
                e4.printStackTrace();
            }
            return false;
        }
    }

    private boolean matchSubjectPublicKeyAlgID(X509Certificate xcert) {
        if (this.subjectPublicKeyAlgID == null) {
            return true;
        }
        try {
            DerValue val = new DerValue(xcert.getPublicKey().getEncoded());
            if (val.tag != 48) {
                throw new IOException("invalid key format");
            }
            AlgorithmId algID = AlgorithmId.parse(val.data.getDerValue());
            if (debug != null) {
                debug.println("X509CertSelector.match: subjectPublicKeyAlgID = " + this.subjectPublicKeyAlgID + ", xcert subjectPublicKeyAlgID = " + algID.getOID());
            }
            if (this.subjectPublicKeyAlgID.equals(algID.getOID())) {
                return true;
            }
            if (debug != null) {
                debug.println("X509CertSelector.match: subject public key alg IDs don't match");
            }
            return false;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in subject public key algorithm OID check");
            }
            return false;
        }
    }

    private boolean matchKeyUsage(X509Certificate xcert) {
        if (this.keyUsage == null) {
            return true;
        }
        boolean[] certKeyUsage = xcert.getKeyUsage();
        if (certKeyUsage != null) {
            int keyBit = PRIVATE_KEY_USAGE_ID;
            while (keyBit < this.keyUsage.length) {
                if (!this.keyUsage[keyBit] || (keyBit < certKeyUsage.length && certKeyUsage[keyBit])) {
                    keyBit += SUBJECT_ALT_NAME_ID;
                } else {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: key usage bits don't match");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchExtendedKeyUsage(X509Certificate xcert) {
        if (this.keyPurposeSet == null || this.keyPurposeSet.isEmpty()) {
            return true;
        }
        try {
            ExtendedKeyUsageExtension ext = (ExtendedKeyUsageExtension) getExtensionObject(xcert, NAME_DIRECTORY);
            if (ext != null) {
                Vector<ObjectIdentifier> certKeyPurposeVector = (Vector) ext.get(ExtendedKeyUsageExtension.USAGES);
                if (!(certKeyPurposeVector.contains(ANY_EXTENDED_KEY_USAGE) || certKeyPurposeVector.containsAll(this.keyPurposeOIDSet))) {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: cert failed extendedKeyUsage criterion");
                    }
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in extended key usage check");
            }
            return false;
        }
    }

    private boolean matchSubjectAlternativeNames(X509Certificate xcert) {
        if (this.subjectAlternativeNames == null || this.subjectAlternativeNames.isEmpty()) {
            return true;
        }
        try {
            SubjectAlternativeNameExtension sanExt = (SubjectAlternativeNameExtension) getExtensionObject(xcert, SUBJECT_ALT_NAME_ID);
            if (sanExt == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no subject alternative name extension");
                }
                return false;
            }
            GeneralNames certNames = sanExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME);
            Iterator<GeneralNameInterface> i = this.subjectAlternativeGeneralNames.iterator();
            while (i.hasNext()) {
                Object matchName = (GeneralNameInterface) i.next();
                boolean found = false;
                Iterator<GeneralName> t = certNames.iterator();
                while (t.hasNext() && !found) {
                    found = ((GeneralName) t.next()).getName().equals(matchName);
                }
                if (found || (!this.matchAllSubjectAltNames && i.hasNext())) {
                    if (found && !this.matchAllSubjectAltNames) {
                        break;
                    }
                } else {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: subject alternative name " + matchName + " not found");
                    }
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in subject alternative name check");
            }
            return false;
        }
    }

    private boolean matchNameConstraints(X509Certificate xcert) {
        if (this.nc == null) {
            return true;
        }
        try {
            if (this.nc.verify(xcert)) {
                return true;
            }
            if (debug != null) {
                debug.println("X509CertSelector.match: name constraints not satisfied");
            }
            return false;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in name constraints check");
            }
            return false;
        }
    }

    private boolean matchPolicy(X509Certificate xcert) {
        if (this.policy == null) {
            return true;
        }
        try {
            CertificatePoliciesExtension ext = (CertificatePoliciesExtension) getExtensionObject(xcert, NAME_X400);
            if (ext == null) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: no certificate policy extension");
                }
                return false;
            }
            List<PolicyInformation> policies = ext.get(CertificatePoliciesExtension.POLICIES);
            List<CertificatePolicyId> policyIDs = new ArrayList(policies.size());
            for (PolicyInformation info : policies) {
                policyIDs.add(info.getPolicyIdentifier());
            }
            if (this.policy != null) {
                boolean foundOne = false;
                if (!this.policy.getCertPolicyIds().isEmpty()) {
                    for (CertificatePolicyId id : this.policy.getCertPolicyIds()) {
                        if (policyIDs.contains(id)) {
                            foundOne = true;
                            break;
                        }
                    }
                    if (!foundOne) {
                        if (debug != null) {
                            debug.println("X509CertSelector.match: cert failed policyAny criterion");
                        }
                        return false;
                    }
                } else if (policyIDs.isEmpty()) {
                    if (debug != null) {
                        debug.println("X509CertSelector.match: cert failed policyAny criterion");
                    }
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in certificate policy ID check");
            }
            return false;
        }
    }

    private boolean matchPathToNames(X509Certificate xcert) {
        if (this.pathToGeneralNames == null) {
            return true;
        }
        try {
            NameConstraintsExtension ext = (NameConstraintsExtension) getExtensionObject(xcert, NAME_DNS);
            if (ext == null) {
                return true;
            }
            if (debug != null) {
                Debug debug = debug;
                if (Debug.isOn("certpath")) {
                    debug.println("X509CertSelector.match pathToNames:\n");
                    for (Object obj : this.pathToGeneralNames) {
                        debug.println("    " + obj + "\n");
                    }
                }
            }
            GeneralSubtrees permitted = ext.get(NameConstraintsExtension.PERMITTED_SUBTREES);
            GeneralSubtrees excluded = ext.get(NameConstraintsExtension.EXCLUDED_SUBTREES);
            if (excluded == null || matchExcluded(excluded)) {
                return permitted == null || matchPermitted(permitted);
            } else {
                return false;
            }
        } catch (IOException e) {
            if (debug != null) {
                debug.println("X509CertSelector.match: IOException in name constraints check");
            }
            return false;
        }
    }

    private boolean matchExcluded(GeneralSubtrees excluded) {
        Iterator<GeneralSubtree> t = excluded.iterator();
        while (t.hasNext()) {
            GeneralNameInterface excludedName = ((GeneralSubtree) t.next()).getName().getName();
            for (Object pathToName : this.pathToGeneralNames) {
                if (excludedName.getType() == pathToName.getType()) {
                    switch (pathToName.constrains(excludedName)) {
                        case PRIVATE_KEY_USAGE_ID /*0*/:
                        case NAME_DNS /*2*/:
                            if (debug != null) {
                                debug.println("X509CertSelector.match: name constraints inhibit path to specified name");
                                debug.println("X509CertSelector.match: excluded name: " + pathToName);
                            }
                            return false;
                        default:
                            break;
                    }
                }
            }
        }
        return true;
    }

    private boolean matchPermitted(GeneralSubtrees permitted) {
        for (GeneralNameInterface pathToName : this.pathToGeneralNames) {
            Iterator<GeneralSubtree> t = permitted.iterator();
            boolean permittedNameFound = false;
            boolean nameTypeFound = false;
            String names = "";
            while (t.hasNext() && !permittedNameFound) {
                Object permittedName = ((GeneralSubtree) t.next()).getName().getName();
                if (permittedName.getType() == pathToName.getType()) {
                    nameTypeFound = true;
                    names = names + "  " + permittedName;
                    switch (pathToName.constrains(permittedName)) {
                        case PRIVATE_KEY_USAGE_ID /*0*/:
                        case NAME_DNS /*2*/:
                            permittedNameFound = true;
                            break;
                        default:
                            break;
                    }
                }
            }
            if (!permittedNameFound && nameTypeFound) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: name constraints inhibit path to specified name; permitted names of type " + pathToName.getType() + ": " + names);
                }
                return false;
            }
        }
        return true;
    }

    private boolean matchBasicConstraints(X509Certificate xcert) {
        if (this.basicConstraints == -1) {
            return true;
        }
        int maxPathLen = xcert.getBasicConstraints();
        if (this.basicConstraints == -2) {
            if (maxPathLen != -1) {
                if (debug != null) {
                    debug.println("X509CertSelector.match: not an EE cert");
                }
                return false;
            }
        } else if (maxPathLen < this.basicConstraints) {
            if (debug != null) {
                debug.println("X509CertSelector.match: maxPathLen too small (" + maxPathLen + " < " + this.basicConstraints + ")");
            }
            return false;
        }
        return true;
    }

    private static Set<?> cloneSet(Set<?> set) {
        if (set instanceof HashSet) {
            return (Set) ((HashSet) set).clone();
        }
        return new HashSet((Collection) set);
    }

    public Object clone() {
        try {
            X509CertSelector copy = (X509CertSelector) super.clone();
            if (this.subjectAlternativeNames != null) {
                copy.subjectAlternativeNames = cloneSet(this.subjectAlternativeNames);
                copy.subjectAlternativeGeneralNames = cloneSet(this.subjectAlternativeGeneralNames);
            }
            if (this.pathToGeneralNames != null) {
                copy.pathToNames = cloneSet(this.pathToNames);
                copy.pathToGeneralNames = cloneSet(this.pathToGeneralNames);
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}
